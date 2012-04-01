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
package de.Lathanael.BinaryFileDB.BaseClase;

import java.io.*;

/**
 * Extends ByteArrayOutputStream to provide a way of writing the buffer to
 * a DataOutput without re-allocating it.
 * @author original by: Derek Hamner (http://www.javaworld.com/javaworld/jw-01-1999/jw-01-step.html?page=1)
 * @author modified by: Lathanael (aka Philippe Leipold)
 */
public class DbByteArrayOutputStream extends ByteArrayOutputStream {

	public DbByteArrayOutputStream() {
		super();
	}

	public DbByteArrayOutputStream(int size) {
		super(size);
	}

	/**
	 * Writes the full contents of the buffer to a DataOutput stream.
	 */
	public synchronized void writeTo (DataOutput dstr) throws IOException {
		byte[] data = super.buf;
		int l = super.size();
		dstr.write(data, 0, l);
	}
}
