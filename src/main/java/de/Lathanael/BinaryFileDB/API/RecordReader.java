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
package de.Lathanael.BinaryFileDB.API;

import java.io.*;

/**
 * Deserializes an object from an InputStream
 * @author original by: Derek Hamner (http://www.javaworld.com/javaworld/jw-01-1999/jw-01-step.html?page=1)
 * @author modified by: Lathanael (aka Philippe Leipold)
 */
public class RecordReader {

	private String key;
	private byte[] data;
	private ByteArrayInputStream in;
	private ObjectInputStream objIn;
	private Object obj;

	public RecordReader(String key, byte[] data) {
		this.key = key;
		this.data = data;
		in = new ByteArrayInputStream(data);
	}

	public RecordReader(String key, RecordWriter rw) {
		this.key = key;
		obj = rw.getObjectOut();
	}

	public String getKey() {
		return key;
	}

	public byte[] getData() {
		return data;
	}

	public Object getObjectIn() {
		return obj;
	}

	public InputStream getInputStream() throws IOException {
		return in;
	}

	public ObjectInputStream getObjectInputStream() throws IOException {
		if (objIn == null)
			objIn = new ObjectInputStream(in);
		return objIn;
	}

	/**
	 * Reads the next object in the record using an ObjectInputStream.
	 */
	public Object readObject() throws IOException, OptionalDataException, ClassNotFoundException {
		return getObjectInputStream().readObject();
	}
}
