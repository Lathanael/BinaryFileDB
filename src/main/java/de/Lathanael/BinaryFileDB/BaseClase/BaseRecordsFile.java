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

package de.Lathanael.BinaryFileDB.BaseClase;

import java.io.*;
import java.util.*;

import de.Lathanael.BinaryFileDB.API.RecordReader;
import de.Lathanael.BinaryFileDB.API.RecordWriter;
import de.Lathanael.BinaryFileDB.Exception.RecordsFileException;

/**
 * Abstract class providing basic functions to create low-level binary file database.
 * @author original by: Derek Hamner (http://www.javaworld.com/javaworld/jw-01-1999/jw-01-step.html?page=1)
 * @author modified by: Lathanael (aka Philippe Leipold)
 */
public abstract class BaseRecordsFile {

	/**
	 * The database file.
	 */
	private RandomAccessFile file;

	/**
	 * Current file pointer to the start of the record data.
	 */
	protected long dataStartPtr;

	/**
	 * Total length in bytes of the global database headers.
	 * </p>
	 * Default value: 16
	 */
	protected final int FILE_HEADERS_REGION_LENGTH;

	/**
	 * Number of bytes in the record header.
	 * </p>
	 * Default value: 16
	 */
	protected final int RECORD_HEADER_LENGTH;

	/**
	 * The length of a key in the index.
	 * </p>
	 * Default value: 64
	 */
	protected final int MAX_KEY_LENGTH;

	/**
	 * The total length of one index entry - the key length plus the record header length.
	 * </p>
	 * Default value: MAX_KEY_LENGTH + RECORD_HEADER_LENGTH = 80
	 */
	protected final int INDEX_ENTRY_LENGTH;

	/**
	 * File pointer to the num records header.
	 * </p>
	 * Default value: 0
	 */
	protected final long NUM_RECORDS_HEADER_LOCATION = 0;

	/**
	 * File pointer to the data start pointer header.
	 * </p>
	 * Default value: 4
	 */
	protected final long DATA_START_HEADER_LOCATION;

	/**
	 * Creates a new database file, initializing the appropriate headers. Enough space is allocated in
	 * </br>the index for the specified initial size.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	protected BaseRecordsFile(String dbPath, int initialSize) throws IOException, RecordsFileException {
		DATA_START_HEADER_LOCATION = 4;
		FILE_HEADERS_REGION_LENGTH = 16;
		RECORD_HEADER_LENGTH = 16;
		MAX_KEY_LENGTH = 64;
		INDEX_ENTRY_LENGTH = MAX_KEY_LENGTH + RECORD_HEADER_LENGTH;
		File f = new File(dbPath);
		if (f.exists()) {
			throw new RecordsFileException("Database already exits: " + dbPath);
		}
		file = new RandomAccessFile(f, "rw");
		dataStartPtr = indexPositionToEntryFp(initialSize);	// Record Data Region starts were the
		setFileLength(dataStartPtr);						// (i+1)th index entry would start.
		writeNumRecordsHeader(0);
		writeDataStartPtrHeader(dataStartPtr);
	}

	/**
	 * Creates a new database file, initializing the appropriate headers. Enough space is allocated in</br>
	 * the index for the specified initial size. The user can define the lenghts of key, header etc!
	 * @param dbPath - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	protected BaseRecordsFile(String dbPath, int initialSize, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH) throws IOException, RecordsFileException {
		this.DATA_START_HEADER_LOCATION = DATA_START_HEADER_LOCATION;
		this.FILE_HEADERS_REGION_LENGTH = FILE_HEADERS_REGION_LENGTH;
		this.RECORD_HEADER_LENGTH = RECORD_HEADER_LENGTH;
		this.MAX_KEY_LENGTH = MAX_KEY_LENGTH;
		this.INDEX_ENTRY_LENGTH = MAX_KEY_LENGTH + RECORD_HEADER_LENGTH;
		File f = new File(dbPath);
		if (f.exists()) {
			throw new RecordsFileException("Database already exits: " + dbPath);
		}
		file = new RandomAccessFile(f, "rw");
		dataStartPtr = indexPositionToEntryFp(initialSize);	// Record Data Region starts were the
		setFileLength(dataStartPtr);						// (i+1)th index entry would start.
		writeNumRecordsHeader(0);
		writeDataStartPtrHeader(dataStartPtr);
	}

	/**
	 * Opens an existing database file and initializes the dataStartPtr. The accessFlags</br>
	 * parameter can be "r" or "rw" -- as defined in RandomAccessFile.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	protected BaseRecordsFile(String dbPath, String accessFlags) throws IOException, RecordsFileException {
		DATA_START_HEADER_LOCATION = 4;
		FILE_HEADERS_REGION_LENGTH = 16;
		RECORD_HEADER_LENGTH = 16;
		MAX_KEY_LENGTH = 64;
		INDEX_ENTRY_LENGTH = MAX_KEY_LENGTH + RECORD_HEADER_LENGTH;
		File f = new File (dbPath);
		if(!f.exists()) {
			throw new RecordsFileException("Database not found: " + dbPath);
		}
		file = new RandomAccessFile(f, accessFlags);
		dataStartPtr = readDataStartHeader();
	}

	/**
	 * Opens an existing database file and initializes the dataStartPtr. The accessFlags</br>
	 * parameter can be "r" or "rw" -- as defined in RandomAccessFile. Lets the user define the</br>
	 * lengths of key, header etc.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	protected BaseRecordsFile(String dbPath, String accessFlags, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH) throws IOException, RecordsFileException {
		this.DATA_START_HEADER_LOCATION = DATA_START_HEADER_LOCATION;
		this.FILE_HEADERS_REGION_LENGTH = FILE_HEADERS_REGION_LENGTH;
		this.RECORD_HEADER_LENGTH = RECORD_HEADER_LENGTH;
		this.MAX_KEY_LENGTH = MAX_KEY_LENGTH;
		this.INDEX_ENTRY_LENGTH = MAX_KEY_LENGTH + RECORD_HEADER_LENGTH;
		File f = new File(dbPath);
		if (f.exists()) {
			throw new RecordsFileException("Database already exits: " + dbPath);
		}
		file = new RandomAccessFile(f, accessFlags);
		dataStartPtr = readDataStartHeader();
	}

	/**
	 * Returns an Enumeration of the keys of all records in the database.
	 */
	public abstract Enumeration<?> enumerateKeys();

	/**
	 * Returns the number or records in the database.
	 */
	public abstract int getNumRecords();

	/**
	 * Checks there is a record with the given key.
	 */
	public abstract boolean recordExists(String key);

	/**
	 * Maps a key to a record header.
	 * @throws RecordsFileException
	 */
	protected abstract IndexEntry keyToIndexEntry(String key) throws RecordsFileException;

	/**
	 * Locates space for a new record of dataLength size and initializes a IndexEntry.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	protected abstract IndexEntry allocateRecord(String key, int dataLength) throws RecordsFileException, IOException;

	/**
	 * Returns the record to which the target file pointer belongs - meaning the specified location
	 * in the file is part of the record data of the RecordHeader which is returned. Returns null if
	 * the location is not part of a record. (O(n) mem accesses)
	 * @throws RecordsFileException
	 */
	protected abstract IndexEntry getIndexAt(long targetFp) throws RecordsFileException;

	/**
	 * Gets the length of the file associated to this DB
	 * @throws IOException
	 */
	protected long getFileLength() throws IOException {
		return file.length();
	}

	/**
	 * Sets the length of the file associated with this DB
	 * @throws IOException
	 */
	protected void setFileLength(long l) throws IOException {
		file.setLength(l);
	}

	/**
	 * Reads the number of records header from the file.
	 * @throws IOException
	 */
	protected int readNumRecordsHeader() throws IOException {
		file.seek(NUM_RECORDS_HEADER_LOCATION);
		return file.readInt();
	}

	/**
	 * Writes the number of records header to the file.
	 * @throws IOException
	 */
	protected void writeNumRecordsHeader(int numRecords) throws IOException {
		file.seek(NUM_RECORDS_HEADER_LOCATION);
		file.writeInt(numRecords);
	}

	/**
	 * Reads the data start pointer header from the file.
	 */
	protected long readDataStartHeader() throws IOException {
		file.seek(DATA_START_HEADER_LOCATION);
		return file.readLong();
	}

	/**
	 * Writes the data start pointer header to the file.
	 * @throws IOException
	 */
	protected void writeDataStartPtrHeader(long dataStartPtr) throws IOException {
		file.seek(DATA_START_HEADER_LOCATION);
		file.writeLong(dataStartPtr);
	}


	/**
	 * Returns a file pointer in the index pointing to the first byte
	 * in the key located at the given index position.
	 */
	protected long indexPositionToEntryFp(int pos) {
		return FILE_HEADERS_REGION_LENGTH + (INDEX_ENTRY_LENGTH * pos);
	}

	/**
	 * Reads the ith key from the index.
	 * @throws IOException
	 */
	String readKeyFromIndex(int position) throws IOException {
		file.seek(indexPositionToEntryFp(position));
		return file.readUTF();
	}

	/**
	 * Reads the ith index entry from the index.
	 * @throws IOException
	 */
	IndexEntry readEntryFromIndex(int position) throws IOException {
		file.seek(position);
		return IndexEntry.readEntry(file);
	}

	/**
	 * Writes the ith index entry to the index.
	 * @throws IOException
	 */
	protected void writeEntryToIndex(IndexEntry entry) throws IOException {
		file.seek(entry.indexPosition);
		entry.write(file);
	}

	/**
	 * Appends an entry to end of index. Assumes that insureIndexSpace() has already been called.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	protected void addEntryToIndex(IndexEntry newEntry, int currentNumRecords) throws IOException, RecordsFileException {
		file.seek(indexPositionToEntryFp(currentNumRecords));
		newEntry.write(file);
		newEntry.setIndexPosition(currentNumRecords);
		writeNumRecordsHeader(currentNumRecords + 1);
	}


	/**
	 * Removes the record from the index. Replaces the target with the entry at the
	 * end of the index.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	protected void deleteEntryFromIndex(String key, IndexEntry entry, int currentNumRecords) throws IOException, RecordsFileException {
		if (entry.indexPosition != currentNumRecords - 1) {
			String lastKey = readKeyFromIndex(currentNumRecords - 1);
			IndexEntry last = keyToIndexEntry(lastKey);
			last.setIndexPosition(entry.indexPosition);
			file.seek(indexPositionToEntryFp(last.indexPosition));
			last.write(file);
		}
		writeNumRecordsHeader(currentNumRecords - 1);
	}

	/**
	 * Adds the given record to the database.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	public synchronized void insertRecord(RecordWriter rw) throws RecordsFileException, IOException {
		String key = rw.getKey();
		if (recordExists(key)) {
			throw new RecordsFileException("Key exists: " + key);
		}
		insureIndexSpace(getNumRecords() + 1);
		IndexEntry newEntry = allocateRecord(key, rw.getDataLength());
		writeRecordData(newEntry, rw);
		addEntryToIndex(newEntry, getNumRecords());
	}

	/**
	 * Appends the Record to the end of a file without searching for free space.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	public synchronized void quickInsertRecord(RecordWriter rw) throws RecordsFileException, IOException {
		String key = rw.getKey();
		int dataLength = rw.getDataLength();
		insureIndexSpace(getNumRecords() + 1);
		long fp = getFileLength();
		setFileLength(fp + dataLength);
		IndexEntry newEntry = new IndexEntry(fp, dataLength, key, MAX_KEY_LENGTH);
		writeRecordData(newEntry, rw);
		addEntryToIndex(newEntry, getNumRecords());
	}

	/**
	 * Updates an existing record. If the new contents does not fit in the original record,
	 * then the update is handled by deleting the old record and adding the new.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	public synchronized void updateRecord(RecordWriter rw) throws RecordsFileException, IOException {
		IndexEntry entry = keyToIndexEntry(rw.getKey());
		if (rw.getDataLength() > entry.dataCapacity) {
			deleteRecord(rw.getKey());
			insertRecord(rw);
		} else {
			writeRecordData(entry, rw);
			writeEntryToIndex(entry);
		}
	}

	/**
	 * Reads a record.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	public synchronized RecordReader readRecord(String key) throws RecordsFileException, IOException {
		byte[] data = readRecordData(key);
		return new RecordReader(key, data);
	}

	/**
	 * Reads the data for the record with the given key.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	protected byte[] readRecordData(String key) throws IOException, RecordsFileException {
		return readRecordData(keyToIndexEntry(key));
	}

	/**
	 * Reads the record data for the given record header.
	 * @throws IOException
	 */
	protected byte[] readRecordData(IndexEntry entry) throws IOException {
		byte[] buf = new byte[entry.dataCount];
		file.seek(entry.dataPointer);
		file.readFully(buf);
		return buf;
	}

	/**
	 * Updates the contents of the given record. A RecordsFileException is thrown if the new data does not
	 * fit in the space allocated to the record. The header's data count is updated, but not
	 * written to the file.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	protected void writeRecordData(IndexEntry entry, RecordWriter rw) throws IOException, RecordsFileException {
		if (rw.getDataLength() > entry.dataCapacity) {
			throw new RecordsFileException ("Record data does not fit");
		}
		entry.dataCount = rw.getDataLength();
		file.seek(entry.dataPointer);
		rw.writeTo((DataOutput)file);
	}


	/**
	 * Updates the contents of the given record. A RecordsFileException is thrown if the new data does not
	 * fit in the space allocated to the record. The header's data count is updated, but not
	 * written to the file.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	protected void writeRecordData(IndexEntry entry, byte[] data) throws IOException, RecordsFileException {
		if (data.length > entry.dataCapacity) {
			throw new RecordsFileException ("Record data does not fit");
		}
		entry.dataCount = data.length;
		file.seek(entry.dataPointer);
		file.write(data, 0, data.length);
	}


	/**
	 * Deletes a record.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	public synchronized void deleteRecord(String key) throws RecordsFileException, IOException {
		IndexEntry delEntry = keyToIndexEntry(key);
		int currentNumRecords = getNumRecords();
		if (getFileLength() == delEntry.dataPointer + delEntry.dataCapacity) {
			// shrink file since this is the last record in the file
			setFileLength(delEntry.dataPointer);
		} else {
			IndexEntry previous = getIndexAt(delEntry.dataPointer - 1);
			if (previous != null) {
				// append space of deleted record onto previous record
				previous.dataCapacity += delEntry.dataCapacity;
				writeEntryToIndex(previous);
			} else {
				// target record is first in the file and is deleted by adding its space to
				// the second record.
				IndexEntry secondEntry = getIndexAt(delEntry.dataPointer + (long)delEntry.dataCapacity);
				byte[] data = readRecordData(secondEntry);
				secondEntry.dataPointer = delEntry.dataPointer;
				secondEntry.dataCapacity += delEntry.dataCapacity;
				writeRecordData(secondEntry, data);
				writeEntryToIndex(secondEntry);
			}
		}
		deleteEntryFromIndex(key, delEntry, currentNumRecords);
	}


	/**
	 * Checks to see if there is space for and additional index entry. If
	 * not, space is created by moving records to the end of the file.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	protected void insureIndexSpace(int requiredNumRecords) throws RecordsFileException, IOException {
		int originalFirstDataCapacity;
		int currentNumRecords = getNumRecords();
		long endIndexPtr = indexPositionToEntryFp(requiredNumRecords);
		if (endIndexPtr > getFileLength() && currentNumRecords == 0) {
			setFileLength(endIndexPtr);
			dataStartPtr = endIndexPtr;
			writeDataStartPtrHeader(dataStartPtr);
			return;
		}
		while (endIndexPtr > dataStartPtr) {
			IndexEntry first = getIndexAt(dataStartPtr);
			byte[] data = readRecordData(first);
			first.dataPointer = getFileLength();
			// If first.dataCapacity is set to the actual data count BEFORE resetting dataStartPtr,
			// and there is free space in 'first', then dataStartPtr will not be reset to the start
			// of the second record. Capture the capacity first and use it to perform the reset.
			originalFirstDataCapacity = first.dataCapacity;
			first.dataCapacity = data.length;
			setFileLength(first.dataPointer + data.length);
			writeRecordData(first, data);
			writeEntryToIndex(first);
			dataStartPtr += originalFirstDataCapacity;
			writeDataStartPtrHeader(dataStartPtr);
		}
	}

	/**
	 * Closes the file.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	public synchronized void close() throws IOException, RecordsFileException {
		try {
			file.close();
		} finally {
			file = null;
		}
	}
}
