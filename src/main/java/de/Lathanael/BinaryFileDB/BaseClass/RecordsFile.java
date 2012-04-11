/*******************************************************************************
 * Copyright (C) 2012 Philippe Leipold
 *
 * This file is part of BinaryFileDB.
 *
 * BinaryFileDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BinaryFileDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BinaryFileDB. If not, see http://www.gnu.org/licenses/.
 *
 ******************************************************************************/

package de.Lathanael.BinaryFileDB.BaseClass;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import de.Lathanael.BinaryFileDB.API.RecordReader;
import de.Lathanael.BinaryFileDB.Exception.CacheSizeException;
import de.Lathanael.BinaryFileDB.Exception.RecordsFileException;
import de.Lathanael.BinaryFileDB.bukkit.DebugLog;


/**
 * Implements {@link de.Lathanael.BinaryFileDB.BaseClass.BaseRecordsFile BaseRecordsFile} to
 * create a low-level binary file database.
 * @author original by: Derek Hamner (http://www.javaworld.com/javaworld/jw-01-1999/jw-01-step.html?page=1)
 * @author modified by: Lathanael (aka Philippe Leipold)
 */
public class RecordsFile extends BaseRecordsFile {

	/**
	 * ConcurrentHashMap which holds the in-memory index. For efficiency, the entire index
	 * is cached in memory. The ConcurrentHashMap maps a key of type String to an IndexEntry.
	 */
	protected ConcurrentHashMap<String, IndexEntry> memIndex;

	/**
	 * ConcurrentSkipListMap which holds a in-memory map of free Record space and the
	 * corresponding IndexEntry. If there are 2 Records with the same free space only
	 * the first will be mapped in this map.
	 */
	protected ConcurrentSkipListMap<Integer, IndexEntry> freeRecordSpace;

	/**
	 * In-Memory cache of recently accessed records in form of ReacordReaders.
	 */
	protected ConcurrentHashMap<String, RecordReader> cachedRecords;

	/**
	 * The amount of records which should be cached.
	 */
	protected int cacheSize;

	/**
	 * Creates a new database file.	The initialSize parameter determines the</br>
	 * amount of space which is allocated for the index. The index can grow</br>
	 * dynamically, but the parameter is provided to increase efficiency.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public RecordsFile(String dbPath, int initialSize, int cacheSize) throws IOException, RecordsFileException, CacheSizeException {
		super(dbPath, initialSize);
		if (cacheSize <= 0)
			throw new CacheSizeException();
		memIndex = new ConcurrentHashMap<String, IndexEntry>((int)(initialSize/ 0.75) + 1); // size/0.75 + 1 makes sure the map does not need to grow for the initial amount of data
		freeRecordSpace = new ConcurrentSkipListMap<Integer, IndexEntry>();
		cachedRecords = new ConcurrentHashMap<String, RecordReader>((int)(cacheSize / 0.75) + 1);
		this.cacheSize = cacheSize;
	}

	/**
	 * Creates a new database file.	The initialSize parameter determines the</br>
	 * amount of space which is allocated for the index. The index can grow</br>
	 * dynamically, but the parameter is provided to increase</br>
	 * efficiency. Let's the user define the length of keys etc.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClass.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClass.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClass.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClass.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public RecordsFile(String dbPath, int initialSize, int cacheSize, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH) throws IOException, RecordsFileException, CacheSizeException {
		super(dbPath, initialSize, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
		if (cacheSize <= 0)
			throw new CacheSizeException();
		memIndex = new ConcurrentHashMap<String, IndexEntry>((int)(initialSize/ 0.75) + 1);
		freeRecordSpace = new ConcurrentSkipListMap<Integer, IndexEntry>();
		cachedRecords = new ConcurrentHashMap<String, RecordReader>((int)(cacheSize / 0.75) + 1);
		this.cacheSize = cacheSize;
	}

	/**
	 * Opens an existing database and initializes the in-memory index.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public RecordsFile(String dbPath, String accessFlags, int cacheSize) throws IOException, RecordsFileException, CacheSizeException {
		super(dbPath, accessFlags);
		if (cacheSize <= 0)
			throw new CacheSizeException();
		int numRecords = readNumRecordsHeader();
		memIndex = new ConcurrentHashMap<String, IndexEntry>((int)(numRecords/ 0.75) + 1); // size/0.75 + 1 makes sure the map does not need to grow for current amount of records
		freeRecordSpace = new ConcurrentSkipListMap<Integer, IndexEntry>();
		cachedRecords = new ConcurrentHashMap<String, RecordReader>((int)(cacheSize / 0.75) + 1);
		this.cacheSize = cacheSize;
		for (int i = 0; i < numRecords; i++) {
			IndexEntry entry = readEntryFromIndex(i);
			String key = entry.getKey();
			entry.setIndexPosition(i);
			freeRecordSpace.putIfAbsent(entry.getFreeSpace(), entry);
			memIndex.put(key, entry);
		}
		DebugLog.INSTANCE.info("Loaded database from file:");
		DebugLog.INSTANCE.info("Read number of records: " + numRecords);
		DebugLog.INSTANCE.info("Size of the memIndex: " + memIndex.size());
	}

	/**
	 * Opens an existing database and initializes the in-memory index</br>
	 * and let's the user define the length of keys etc.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClass.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClass.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClass.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClass.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public RecordsFile(String dbPath, String accessFlags, int cacheSize, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH) throws IOException, RecordsFileException, CacheSizeException {
		super(dbPath, accessFlags, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
		if (cacheSize <= 0)
			throw new CacheSizeException();
		int numRecords = readNumRecordsHeader();
		memIndex = new ConcurrentHashMap<String, IndexEntry>((int)(numRecords/ 0.75) + 1);
		freeRecordSpace = new ConcurrentSkipListMap<Integer, IndexEntry>();
		cachedRecords = new ConcurrentHashMap<String, RecordReader>((int)(cacheSize / 0.75) + 1);
		this.cacheSize = cacheSize;
		for (int i = 0; i < numRecords; i++) {
			IndexEntry entry = readEntryFromIndex(i);
			String key = entry.getKey();
			entry.setIndexPosition(i);
			if (entry.getFreeSpace() > 0)
				freeRecordSpace.putIfAbsent(entry.getFreeSpace(), entry);
			memIndex.put(key, entry);
		}
	}

	/**
	 * Returns an enumeration of all the keys in the database.
	 */
	public synchronized Enumeration<String> enumerateKeys() {
		return memIndex.keys();
	}

	/**
	 * Returns the current number of records in the database.
	 */
	public synchronized int getNumRecords() {
		return memIndex.size();
	}

	/**
	 * Checks if there is a record belonging to the given key.
	 */
	public synchronized boolean recordExists(String key) {
		return memIndex.containsKey(key);
	}

	/**
	 * Maps a key to a index entry by looking it up in the in-memory index.
	 * @throws RecordsFileException
	 */
	protected IndexEntry keyToIndexEntry(String key) throws RecordsFileException {
		IndexEntry entry = memIndex.get(key);
		if (entry == null) {
			throw new RecordsFileException("Key not found: " + key);
		}
		return entry;
	}

	/**
	 * This method searches the file for free space and then returns a IndexEntry</br>
	 * which uses the space.</br>
	 * (O(log(n)) memory accesses if space is in the ConcurrentSkipListMap of free spaces</br>
	 * or O(log(n)+n) if not)
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	protected IndexEntry allocateRecord(String key, int dataLength) throws RecordsFileException, IOException {
		IndexEntry newEntry = null;
		// search for empty space
		// First in the memory-map
		Entry<Integer, IndexEntry> ceil = freeRecordSpace.ceilingEntry(dataLength);
		if (ceil != null) {
			newEntry = ceil.getValue().split(key);
			writeEntryToIndex(ceil.getValue());
			freeRecordSpace.remove(ceil.getKey());
			if (newEntry.getFreeSpace() > 0)
				freeRecordSpace.putIfAbsent(newEntry.getFreeSpace(), newEntry);
		}
		// if map did not contain an entry, make sure there really is no space which is untracked!
		if (newEntry == null) {
			Enumeration<IndexEntry> e = memIndex.elements();
				while (e.hasMoreElements()) {
					IndexEntry next = e.nextElement();
					int free = next.getFreeSpace();
					if (dataLength <= free) {
						newEntry = next.split(key);
						writeEntryToIndex(next);
						freeRecordSpace.remove(free);
						if (newEntry.getFreeSpace() > 0)
							freeRecordSpace.putIfAbsent(newEntry.getFreeSpace(), newEntry);
						break;
					}
					// While we are looping through the records, lets put all free space in the
					// Map if it is not already present. This might result in a better performance
					// for the next search!
					if (free > 0)
						freeRecordSpace.putIfAbsent(free, next);
				}
		}
		if (newEntry == null) {
			// append record to end of file - grows file to allocate space
			long fp = getFileLength();
			setFileLength(fp + dataLength);
			newEntry = new IndexEntry(fp, dataLength, key, MAX_KEY_LENGTH);
		}
		return newEntry;
	}

	/**
	 * Returns the record to which the target file pointer belongs - meaning</br>
	 * that the specified location in the file is part of the record data of the</br>
	 * IndexEntry which is returned. Returns null if the location is not part</br>
	 * of a record. (O(n) mem accesses)
	 * @throws RecordsFileException
	 */
	// TODO: Performance increase with better Big O
	protected IndexEntry getIndexAt(long targetFp) throws RecordsFileException {
		Enumeration<IndexEntry> e = memIndex.elements();
		while (e.hasMoreElements()) {
			IndexEntry next = (IndexEntry) e.nextElement();
			if (targetFp >= next.dataPointer &&
				targetFp < next.dataPointer + (long)next.dataCapacity) {
					return next;
			}
		}
		return null;
	}


	/**
	 * Closes the database.
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public synchronized void close() throws IOException, RecordsFileException {
		try {
			super.close();
		} finally {
			memIndex.clear();
			memIndex = null;
		}
	}

	/**
	 * Adds the new record to the in-memory index and calls the super class</br>
	 * to add
	 * the index entry to the file.
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	@Override
	protected void addEntryToIndex(IndexEntry newEntry, int currentNumRecords) throws IOException, RecordsFileException {
		super.addEntryToIndex(newEntry, currentNumRecords);
		memIndex.put(newEntry.getKey(), newEntry);
	}

	/**
	 * Removes the record from the index. Replaces the target with the entry at the
	 * end of the index.
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	@SuppressWarnings("unused")
	protected void deleteEntryFromIndex(String key, IndexEntry entry, int currentNumRecords) throws IOException, RecordsFileException {
		super.deleteEntryFromIndex(key, entry, currentNumRecords);
		IndexEntry deleted = (IndexEntry)memIndex.remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized RecordReader readRecord(String key) throws RecordsFileException, IOException {
		RecordReader newReader = cachedRecords.get(key);
		if (newReader != null) {
			return newReader;
		}
		byte[] data = readRecordData(key);
		newReader = new RecordReader(key, data);
		cachedRecords.putIfAbsent(key, newReader);
		if (cachedRecords.size() > cacheSize) {
			// cache is full, remove first entry not matching the recently put in record
			Iterator<Entry<String, RecordReader>> it = cachedRecords.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, RecordReader> next = it.next();
				if (next.getKey() != key) {
					it.remove();
					break;
				}
			}
		}
		return newReader;
	}

	/**
	 * Removes a Record from the cache
	 * @param key The string to which the Record is associated
	 */
	public synchronized boolean removeFromCache(String key) {
		Object o = cachedRecords.remove(key);
		if (o != null)
			return true;
		return false;
	}
}
