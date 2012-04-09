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
 ******************************************************************************/
package de.Lathanael.BinaryFileDB.BaseClass;

import java.io.*;
import java.nio.ByteBuffer;

import de.Lathanael.BinaryFileDB.Exception.RecordsFileException;

/**
 * IndexEntry object for the DB-file index header
 * @author Lathanael (aka Philippe Leipold)
 */
public class IndexEntry {

	private JavaModUTFConverter converter = new JavaModUTFConverter();
	/**
	 * File pointer to the first byte of record data (8 bytes).
	 */
	protected long dataPointer;

	/**
	 * Actual number of bytes of data held in this record (4 bytes).
	 */
	protected int dataCount;

	/**
	 * Number of bytes of data that this record can hold (4 bytes).
	 */
	protected int dataCapacity;

	/**
	 * Indicates this entry's position in the file index.
	 */
	protected int indexPosition;

	/**
	 * The key used to refer to this index entry
	 */
	protected String key;

	/**
	 * The maximum length allowed for a key
	 */
	protected int maxKeyLength;

	/**
	 * Creates a new IndexEntry object
	 * @param dataPointer - Where in the file is the data located.
	 * @param dataCapacity - The maximum capacity for this entry
	 * @param key - The key with which this entry can be read
	 * @param maxKeyLength - The maximum KeyLength allowed by the {@link de.Lathanael.BinaryFileDB.BaseClass.RecordsFile RecordsFile}
	 */
	protected IndexEntry(long dataPointer, int dataCapacity, String key, int maxKeyLength) {
		if (dataCapacity < 1)
			throw new IllegalArgumentException("Bad record size: " + dataCapacity);
		this.dataPointer = dataPointer;
		this.dataCapacity = dataCapacity;
		this.key = key;
		this.maxKeyLength = maxKeyLength;
		this.dataCount = 0;
	}

	/**
	 * Creates a new IndexEntry which values will be read from file.
	 * @param maxKeyLength - The maximum KeyLength allowed by the {@link de.Lathanael.BinaryFileDB.BaseClass.RecordsFile RecordsFile}
	 */
	protected IndexEntry(int maxKeyLength) {
		this.maxKeyLength = maxKeyLength;
	}

	/**
	 * Gets the key of the entry as a String
	 */
	protected String getKey() {
		return key;
	}

	/**
	 * Set the Key of the entry. Be careful with this as it can corrupt the database-file.
	 */
	protected void setKey(String key) {
		this.key = key;
	}

	/**
	 * Get the position of this entry in the index.
	 */
	protected int getIndexPosition() {
		return indexPosition;
	}

	/**
	 * Set the index-position of the entry. Be careful with this as it can corrupt the database-file.
	 */
	protected void setIndexPosition(int indexPosition) {
		this.indexPosition = indexPosition;
	}

	/**
	 * Gets the maximum capacity associeated with this entry.
	 */
	protected int getDataCapacity() {
		return dataCapacity;
	}

	/**
	 * Gets the free space for this entry.
	 */
	protected int getFreeSpace() {
		return dataCapacity - dataCount;
	}

	protected void read(DataInput in) throws IOException {
		byte[] buffer = new byte[16 + maxKeyLength];
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		in.readFully(buffer, 0, 16 + maxKeyLength);
		key = converter.readUTF(bb);
		bb.position(maxKeyLength);
		dataPointer = bb.getLong();
		dataCapacity = bb.getInt();
		dataCount = bb.getInt();
	}

	protected void write(DataOutput out) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(16 + maxKeyLength);
		buffer.put(converter.writeUTF(key));
		buffer.position(maxKeyLength);
		buffer.putLong(dataPointer).putInt(dataCapacity).putInt(dataCount);
		out.write(buffer.array());
	}

	protected static IndexEntry readEntry(DataInput in, int maxKeyLength) throws IOException {
		IndexEntry entry = new IndexEntry(maxKeyLength);
		entry.read(in);
		return entry;
	}

	/**
	 * Returns a new index entry which occupies the free space of this entry.
	 * Shrinks this entries size by the size of its free space.
	 */
	protected IndexEntry split(String key) throws RecordsFileException {
		long newFp = dataPointer + (long)dataCount;
		IndexEntry newRecord = new IndexEntry(newFp, getFreeSpace(), key, maxKeyLength);
		dataCapacity = dataCount;
		return newRecord;
	}

}
