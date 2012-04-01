/*************************************************************************
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
 * along with BinaryFileDB. If not, see <http://www.gnu.org/licenses/>.
 *
 **************************************************************************/

package de.Lathanael.BinaryFileDB.BaseClass;

import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;

/**
 * Modified implementation of the readUTF() and writeUTF(String s) methods</br>
 * for the modified UTF-8 format found in {@link java.io.DataInput DataInput} and {@link java.io.DataOutput DataOutput}</br>
 * Original Code by Oracle.
 * @author modified by: Lathanael (aka Philippe Leipold)
 */
public class JavaModUTFConverter {

	/**
	 * Nearly the same as: {@link java.io.DataOutput#writeUTF(String s) writeUTF()} of {@link java.io.DataOutput DataOutput}
	 * </p>
	 * Difference: Returns the byte[] array which is created instead of writing it to the stream
	 *	 */
	public final byte[] writeUTF(String key) throws IOException {
		int strlen = key.length();
		int utflen = 0;
		int c, count = 0;

		/* use charAt instead of copying String to char array */
		for (int i = 0; i < strlen; i++) {
			c = key.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				utflen++;
			} else if (c > 0x07FF) {
				utflen += 3;
			} else {
				utflen += 2;
			}
		}

		if (utflen > 65535)
		throw new UTFDataFormatException("Encoded string too long: " + utflen + " bytes, max: 65535 bytes");

		byte[] bytearr = new byte[utflen+2];
		bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
		bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);

		int i=0;
		for (i=0; i < strlen; i++) {
			c = key.charAt(i);
			if (!((c >= 0x0001) && (c <= 0x007F)))
				break;
			bytearr[count++] = (byte) c;
		}

		for (;i < strlen; i++){
			c = key.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				bytearr[count++] = (byte) c;
			} else if (c > 0x07FF) {
				bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
				bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
				bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
			} else {
				bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
				bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
			}
		}
		return bytearr;
	}

	/**
	 * Nearly the same as: {@link java.io.DataInput#readUTF() readUTF()} of {@link java.io.DataInput DataInput}
	 * </p>
	 * Difference: It takes a {@code ByteBuffer} object containing the bytes written</br>
	 * from the writeUTF(String) function and converts it to a String.
	 */
	public final String readUTF(ByteBuffer buffer) throws IOException {
		int utflen = buffer.getShort() & 0xffff;
		byte[] bytearr = new byte[utflen];
		char[] chararr = new char[utflen];

		int c, char2, char3;
		int count = 0;
		int chararr_count=0;

		buffer.get(bytearr, 0, utflen);

		while (count < utflen) {
			c = (int) bytearr[count] & 0xff;
			if (c > 127)
				break;
			count++;
			chararr[chararr_count++]=(char)c;
		}

		while (count < utflen) {
			c = (int) bytearr[count] & 0xff;
			switch (c >> 4) {
				case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
					/* 0xxxxxxx*/
					count++;
					chararr[chararr_count++]=(char)c;
					break;
				case 12: case 13:
					/* 110x xxxx   10xx xxxx*/
					count += 2;
					if (count > utflen)
						throw new UTFDataFormatException("Malformed input: partial character at end");
					char2 = (int) bytearr[count-1];
					if ((char2 & 0xC0) != 0x80)
						throw new UTFDataFormatException("Malformed input around byte " + count);
					chararr[chararr_count++]=(char)(((c & 0x1F) << 6) | (char2 & 0x3F));
					break;
				case 14:
					/* 1110 xxxx  10xx xxxx  10xx xxxx */
					count += 3;
					if (count > utflen)
						throw new UTFDataFormatException(
							"Malformed input: partial character at end");
					char2 = (int) bytearr[count-2];
					char3 = (int) bytearr[count-1];
					if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
						throw new UTFDataFormatException("mMlformed input around byte " + (count-1));
					chararr[chararr_count++]=(char)(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
					break;
				default:
					/* 10xx xxxx,  1111 xxxx */
					throw new UTFDataFormatException(
						"Malformed input around byte " + count);
			}
		}
		// The number of chars produced may be less than utflen
		return new String(chararr, 0, chararr_count);
	}
}
