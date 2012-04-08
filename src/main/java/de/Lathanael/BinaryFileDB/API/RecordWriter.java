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

import de.Lathanael.BinaryFileDB.BaseClass.DbByteArrayOutputStream;

/**
 * A class to serialize an object first and call the write-process later.
 * @author original by: Derek Hamner (http://www.javaworld.com/javaworld/jw-01-1999/jw-01-step.html?page=1)
 * @author modified by: Lathanael (aka Philippe Leipold)
 */
public class RecordWriter {

	private String key;
	private DbByteArrayOutputStream out;
	private ObjectOutputStream objOutStr;
	private Object objOut;
	private boolean append = false;

	public RecordWriter(String key) {
		this.key = key;
		out = new DbByteArrayOutputStream();
	}

	public RecordWriter(String key, boolean append) {
		this.key = key;
		out = new DbByteArrayOutputStream();
		this.append = append;
	}

	public String getKey() {
		return key;
	}

	public OutputStream getOutputStream() {
		return out;
	}

	public Object getObjectOut() {
		return objOut;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}

	public boolean isAppend() {
		return append;
	}

	public ObjectOutputStream getObjectOutputStream() throws IOException {
		if (objOutStr == null) {
			objOutStr = new ObjectOutputStream(out);
		}
		return objOutStr;
	}

	/**
	 * Serializes an object so it can be written to a file.
	 * @param o They object which should be serialized
	 * @throws IOException
	 */
	public void writeObject(Object o) throws IOException {
		objOut = o;
		getObjectOutputStream().writeObject(o);
		getObjectOutputStream().flush();
	}

	/**
	 * Returns the number of bytes in the data.
	 */
	public int getDataLength() {
		return out.size();
	}

	/**
	 * Writes the data out to the stream without re-allocating the buffer.
	 */
	public void writeTo(DataOutput str) throws IOException {
		out.writeTo(str);
	}
}
