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

package de.Lathanael.BinaryFileDB.API;

import java.io.IOException;
import java.util.Map;

import de.Lathanael.BinaryFileDB.BaseClase.DataWriteQueue;
import de.Lathanael.BinaryFileDB.BaseClase.RecordsFile;
import de.Lathanael.BinaryFileDB.Exception.CacheSizeException;
import de.Lathanael.BinaryFileDB.Exception.QueueException;
import de.Lathanael.BinaryFileDB.Exception.RecordsFileException;

/**
 * This class provides an API to a BinaryFile-DataBase through</br>
 * the use of a {@link de.Lathanael.BinaryFileDB.BaseClase.RecordsFile RecordsFile}.</br>
 * It also provides some methods to handle multiple files of the</br>
 * same type and a WriteQueue to lesser the I/O done by the</br>
 * file-database.
 * @author Lathanael (aka Philippe Leipold)
 */
public class DBAccess {

	private final RecordsFile file;
	private boolean useQueue;
	private DataWriteQueue queue;
	private final int cacheSize;
	private int initialSize = 16;
	private int MAX_KEY_LENGTH = 64, DATA_START_HEADER_LOCATION = 4, RECORD_HEADER_LENGTH = 16, FILE_HEADERS_REGION_LENGTH = 16;

	/**
	 * Creates a new database file. The initialSize parameter determines the</br>
	 * amount of space which is allocated for the index. The index can grow</br>
	 * dynamically, but the parameter is provided to increase efficiency.</br>
	 * The queue function is disabled.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess (String dbPath, int initialSize) throws IOException, RecordsFileException, CacheSizeException {
		useQueue = false;
		cacheSize = 10;
		this.initialSize = initialSize;
		file = new RecordsFile(dbPath, initialSize, cacheSize);
	}

	/**
	 * Opens an existing database and initializes the in-memory index.</br>
	 * The queue function is disabled.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String dbPath, String accessFlags) throws IOException, RecordsFileException, CacheSizeException {
		useQueue = false;
		cacheSize = 10;
		file = new RecordsFile(dbPath, accessFlags, cacheSize);
	}

	/**
	 * Creates a new database file. The initialSize parameter determines the</br>
	 * amount of space which is allocated for the index. The index can grow</br>
	 * dynamically, but the parameter is provided to increase efficiency.
	 * @param path - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @param useQueue - If set to true a queue will be created to lessen IO stress on the Disk
	 * @param queueSize - Size of the queue if it is used it must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String path, int initialSize, boolean useQueue, int queueSize) throws IOException, RecordsFileException, CacheSizeException {
		this.useQueue = useQueue;
		if (useQueue)
			queue = new DataWriteQueue(queueSize);
		cacheSize = 10;
		this.initialSize = initialSize;
		file = new RecordsFile(path, initialSize, cacheSize);
	}

	/**
	 * Opens an existing database and initializes the in-memory index.
	 * @param path - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @param useQueue - If set to true a queue will be created to lessen IO stress on the Disk
	 * @param queueSize - Size of the queue if it is used it must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String path, String accessFlags, boolean useQueue, int queueSize) throws IOException, RecordsFileException, CacheSizeException {
		this.useQueue = useQueue;
		if (useQueue)
			queue = new DataWriteQueue(queueSize);
		cacheSize = 10;
		file = new RecordsFile(path, accessFlags, cacheSize);
	}

	/**
	 * Creates a new database file.	The initialSize parameter determines the</br>
	 * amount of space which is allocated for the index. The index can grow</br>
	 * dynamically, but the parameter is provided to increase</br>
	 * efficiency. Let's the user define the length of keys etc.</br>
	 * The queue function is disabled.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String dbPath, int initialSize, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH) throws IOException, RecordsFileException, CacheSizeException {
		useQueue = false;
		cacheSize = 10;
		this.initialSize = initialSize;
		this.MAX_KEY_LENGTH = MAX_KEY_LENGTH;
		this.DATA_START_HEADER_LOCATION = DATA_START_HEADER_LOCATION;
		this.FILE_HEADERS_REGION_LENGTH = FILE_HEADERS_REGION_LENGTH;
		this.RECORD_HEADER_LENGTH = RECORD_HEADER_LENGTH;
		file = new RecordsFile(dbPath, initialSize, cacheSize, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
	}

	/**
	 * Creates a new database file.	The initialSize parameter determines the</br>
	 * amount of space which is allocated for the index. The index can grow</br>
	 * dynamically, but the parameter is provided to increase</br>
	 * efficiency. Let's the user define the length of keys etc.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @param useQueue - If set to true a queue will be created to lessen IO stress on the Disk
	 * @param queueSize - Size of the queue if it is used it must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String dbPath, int initialSize, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH, boolean useQueue, int queueSize) throws IOException, RecordsFileException, CacheSizeException {
		this.useQueue = useQueue;
		if (useQueue)
			queue = new DataWriteQueue(queueSize);
		cacheSize = 10;
		this.initialSize = initialSize;
		this.MAX_KEY_LENGTH = MAX_KEY_LENGTH;
		this.DATA_START_HEADER_LOCATION = DATA_START_HEADER_LOCATION;
		this.FILE_HEADERS_REGION_LENGTH = FILE_HEADERS_REGION_LENGTH;
		this.RECORD_HEADER_LENGTH = RECORD_HEADER_LENGTH;
		file = new RecordsFile(dbPath, initialSize, cacheSize, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
	}

	/**
	 * Opens an existing database and initializes the in-memory index</br>
	 * and let's the user define the length of keys etc.</br>
	 * The queue function is disabled.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String dbPath, String accessFlags, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH) throws IOException, RecordsFileException, CacheSizeException {
		useQueue = false;
		cacheSize = 10;
		this.MAX_KEY_LENGTH = MAX_KEY_LENGTH;
		this.DATA_START_HEADER_LOCATION = DATA_START_HEADER_LOCATION;
		this.FILE_HEADERS_REGION_LENGTH = FILE_HEADERS_REGION_LENGTH;
		this.RECORD_HEADER_LENGTH = RECORD_HEADER_LENGTH;
		file = new RecordsFile(dbPath, accessFlags, cacheSize, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
	}

	/**
	 * Opens an existing database and initializes the in-memory index</br>
	 * and let's the user define the length of keys etc.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @param useQueue - If set to true a queue will be created to lessen IO stress on the Disk
	 * @param queueSize - Size of the queue if it is used it must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String dbPath, String accessFlags, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH, boolean useQueue, int queueSize) throws IOException, RecordsFileException, CacheSizeException {
		this.useQueue = useQueue;
		if (useQueue)
			queue = new DataWriteQueue(queueSize);
		cacheSize = 10;
		this.MAX_KEY_LENGTH = MAX_KEY_LENGTH;
		this.DATA_START_HEADER_LOCATION = DATA_START_HEADER_LOCATION;
		this.FILE_HEADERS_REGION_LENGTH = FILE_HEADERS_REGION_LENGTH;
		this.RECORD_HEADER_LENGTH = RECORD_HEADER_LENGTH;
		file = new RecordsFile(dbPath, accessFlags, cacheSize, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
	}

	/**
	 * Creates a new database file.	The initialSize parameter determines the</br>
	 * amount of space which is allocated for the index. The index can grow</br>
	 * dynamically, but the parameter is provided to increase efficiency.</br>
	 * The queue function is disabled.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String dbPath, int initialSize, int cacheSize) throws IOException, RecordsFileException, CacheSizeException {
		useQueue = false;
		this.cacheSize = cacheSize;
		this.initialSize = initialSize;
		file = new RecordsFile(dbPath, initialSize, cacheSize);
	}

	/**
	 * Opens an existing database and initializes the in-memory index.</br>
	 * The queue function is disabled.
	 * @param path - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String path, String accessFlags, int cacheSize) throws IOException, RecordsFileException, CacheSizeException {
		useQueue = false;
		this.cacheSize = cacheSize;
		file = new RecordsFile(path, accessFlags, cacheSize);
	}

	/**
	 * Creates a new database file. The initialSize parameter determines the</br>
	 * amount of space which is allocated for the index. The index can grow</br>
	 * dynamically, but the parameter is provided to increase efficiency.
	 * @param path - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @param useQueue - If set to true a queue will be created to lessen IO stress on the Disk
	 * @param queueSize - Size of the queue if it is used it must be greater 0!
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String path, int initialSize, boolean useQueue, int queueSize, int cacheSize) throws IOException, RecordsFileException, CacheSizeException {
		this.useQueue = useQueue;
		if (useQueue)
			queue = new DataWriteQueue(queueSize);
		this.cacheSize = cacheSize;
		this.initialSize = initialSize;
		file = new RecordsFile(path, initialSize, cacheSize);
	}

	/**
	 * Opens an existing database and initializes the in-memory index.
	 * @param path - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @param useQueue - If set to true a queue will be created to lessen IO stress on the Disk
	 * @param queueSize - Size of the queue if it is used it must be greater 0!
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String path, String accessFlags, boolean useQueue, int queueSize, int cacheSize) throws IOException, RecordsFileException, CacheSizeException {
		this.useQueue = useQueue;
		if (useQueue)
			queue = new DataWriteQueue(queueSize);
		this.cacheSize = cacheSize;
		file = new RecordsFile(path, accessFlags, cacheSize);
	}

	/**
	 * Creates a new database file.	The initialSize parameter determines the</br>
	 * amount of space which is allocated for the index. The index can grow</br>
	 * dynamically, but the parameter is provided to increase</br>
	 * efficiency. Let's the user define the length of keys etc.</br>
	 * The queue function is disabled.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String dbPath, int initialSize, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH, int cacheSize) throws IOException, RecordsFileException, CacheSizeException {
		useQueue = false;
		this.cacheSize = cacheSize;
		this.initialSize = initialSize;
		this.MAX_KEY_LENGTH = MAX_KEY_LENGTH;
		this.DATA_START_HEADER_LOCATION = DATA_START_HEADER_LOCATION;
		this.FILE_HEADERS_REGION_LENGTH = FILE_HEADERS_REGION_LENGTH;
		this.RECORD_HEADER_LENGTH = RECORD_HEADER_LENGTH;
		file = new RecordsFile(dbPath, initialSize, cacheSize, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
	}

	/**
	 * Creates a new database file.	The initialSize parameter determines the</br>
	 * amount of space which is allocated for the index. The index can grow</br>
	 * dynamically, but the parameter is provided to increase</br>
	 * efficiency. Let's the user define the length of keys etc.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param initialSize - Size of the db created
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @param useQueue - If set to true a queue will be created to lessen IO stress on the Disk
	 * @param queueSize - Size of the queue if it is used it must be greater 0!
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String dbPath, int initialSize, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH, boolean useQueue, int queueSize,
			int cacheSize) throws IOException, RecordsFileException, CacheSizeException {
		this.useQueue = useQueue;
		if (useQueue)
			queue = new DataWriteQueue(queueSize);
		this.cacheSize = cacheSize;
		this.initialSize = initialSize;
		this.MAX_KEY_LENGTH = MAX_KEY_LENGTH;
		this.DATA_START_HEADER_LOCATION = DATA_START_HEADER_LOCATION;
		this.FILE_HEADERS_REGION_LENGTH = FILE_HEADERS_REGION_LENGTH;
		this.RECORD_HEADER_LENGTH = RECORD_HEADER_LENGTH;
		file = new RecordsFile(dbPath, initialSize, cacheSize, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
	}

	/**
	 * Opens an existing database and initializes the in-memory index</br>
	 * and let's the user define the length of keys etc.</br>
	 * The queue function is disabled.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String dbPath, String accessFlags, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH, int cacheSize) throws IOException, RecordsFileException, CacheSizeException {
		useQueue = false;
		this.cacheSize = cacheSize;
		this.MAX_KEY_LENGTH = MAX_KEY_LENGTH;
		this.DATA_START_HEADER_LOCATION = DATA_START_HEADER_LOCATION;
		this.FILE_HEADERS_REGION_LENGTH = FILE_HEADERS_REGION_LENGTH;
		this.RECORD_HEADER_LENGTH = RECORD_HEADER_LENGTH;
		file = new RecordsFile(dbPath, accessFlags, cacheSize, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
	}

	/**
	 * Opens an existing database and initializes the in-memory index</br>
	 * and let's the user define the length of keys etc.
	 * @param dbPath - Pathname where the file is located as a String
	 * @param accessFlags - Whether the new DBFile should hava read-only "r" or read/write "rw" access
	 * @param MAX_KEY_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#MAX_KEY_LENGTH MAX_KEY_LENGTH}
	 * @param DATA_START_HEADER_LOCATION - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#DATA_START_HEADER_LOCATION DATA_START_HEADER_LOCATION}
	 * @param FILE_HEADERS_REGION_LENGTH - {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#FILE_HEADERS_REGION_LENGTH FILE_HEADERS_REGION_LENGTH}
	 * @param RECORD_HEADER_LENGTH {@link de.Lathanael.BinaryFileDB.BaseClase.BaseRecordsFile#RECORD_HEADER_LENGTH RECORD_HEADER_LENGTH}
	 * @param useQueue - If set to true a queue will be created to lessen IO stress on the Disk
	 * @param queueSize - Size of the queue if it is used it must be greater 0!
	 * @param cacheSize - Size of the initial cache for recently loaded RecordReaders. Must be greater 0!
	 * @throws CacheSizeException
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public DBAccess(String dbPath, String accessFlags, int MAX_KEY_LENGTH, int DATA_START_HEADER_LOCATION,
			int FILE_HEADERS_REGION_LENGTH, int RECORD_HEADER_LENGTH, boolean useQueue, int queueSize,
			int cacheSize) throws IOException, RecordsFileException, CacheSizeException {
		this.useQueue = useQueue;
		if (useQueue)
			queue = new DataWriteQueue(queueSize);
		this.cacheSize = cacheSize;
		this.MAX_KEY_LENGTH = MAX_KEY_LENGTH;
		this.DATA_START_HEADER_LOCATION = DATA_START_HEADER_LOCATION;
		this.FILE_HEADERS_REGION_LENGTH = FILE_HEADERS_REGION_LENGTH;
		this.RECORD_HEADER_LENGTH = RECORD_HEADER_LENGTH;
		file = new RecordsFile(dbPath, accessFlags, cacheSize, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
	}

	/**
	 * This will load and return a new DBAccess instance with standard settings.
	 * @param path - Path to the file
	 * @param accessFlags - read only(r) or read and write(rw) access
	 * @return A new {@link de.Lathanael.BinaryFileDB.API.DBAccess DBAccess} instance
	 * @throws IOException
	 * @throws RecordsFileException
	 * @throws CacheSizeException
	 */
	public DBAccess loadNewDB(String path, String accessFlags) throws IOException, RecordsFileException, CacheSizeException {
		return new DBAccess(path, accessFlags);
	}

	/**
	 * Creates a new DBAccess instance with standard settings.
	 * @param path - Path to the file
	 * @param accessFlags  - read only(r) or read and write(rw) access
	 * @return A new {@link de.Lathanael.BinaryFileDB.API.DBAccess DBAccess} instance
	 * @throws IOException
	 * @throws RecordsFileException
	 * @throws CacheSizeException
	 */
	public DBAccess createNewDB(String path, String accessFlags) throws IOException, RecordsFileException, CacheSizeException {
		return new DBAccess(path, initialSize, cacheSize);
	}

	/**
	 * This will load and return a new DBAccess instance with custom</br>
	 * settings specified during creation of this DBAccess instance.
	 * @param path - Path to the file
	 * @param accessFlags - read only(r) or read and write(rw) access
	 * @return A new {@link de.Lathanael.BinaryFileDB.API.DBAccess DBAccess} instance
	 * @throws IOException
	 * @throws RecordsFileException
	 * @throws CacheSizeException
	 */
	public DBAccess loadNewCustomDB(String path, String accessFlags) throws IOException, RecordsFileException, CacheSizeException {
		return new DBAccess(path, accessFlags, cacheSize, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
	}

	/**
	 * Creates a new DBAccess instance with the parameteres</br>
	 * given at the creation of this instance.
	 * @param path - Path to the file
	 * @param accessFlags  - read only(r) or read and write(rw) access
	 * @return A new {@link de.Lathanael.BinaryFileDB.API.DBAccess DBAccess} instance
	 * @throws IOException
	 * @throws RecordsFileException
	 * @throws CacheSizeException
	 */
	public DBAccess createNewCustomDB(String path, String accessFlags) throws IOException, RecordsFileException, CacheSizeException {
		return new DBAccess(path, initialSize, cacheSize, MAX_KEY_LENGTH, DATA_START_HEADER_LOCATION, FILE_HEADERS_REGION_LENGTH, RECORD_HEADER_LENGTH);
	}

	/**
	 * Get the RecordsFile associated with this instance.
	 */
	public RecordsFile getRecordsFile() {
		return file;
	}

	/**
	 * Activates the queue function.
	 */
	public void activateQueue() {
		useQueue = true;
	}

	/**
	 * Deactivates the queue function.
	 */
	public void deactivateQueue() {
		useQueue = false;
	}

	/**
	 * Gets a record as a {@link de.Lathanael.BinaryFileDB.API.RecordReader RecordReader} object from the DB.
	 * </p>
	 * If the WriteQueue is activated it will first check if the entry is scheduled and get the changed Record.
	 * @param key
	 * @return RecordReader object associated to the key or {@code null}
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	public RecordReader getRecord(String key) throws RecordsFileException, IOException {
		RecordWriter rw;
		if (useQueue &&  (rw = queue.getQueuedItem(key)) != null)
			return new RecordReader(key, rw);
		return file.readRecord(key);
	}

	/**
	 * Writes a Record to the DB. Existing ones will be updated.
	 *  </p>
	 * If the queue is activated it will try to add the RecordWriter to it.</br>
	 * If it fails it will write the whole queue to the DB, clear it and finally</br>
	 * add the given RecordWriter to the now empty queue.
	 * @param rw - RecordWriter object which should be written
	 * @throws RecordsFileException
	 * @throws IOException
	 * @throws QueueException
	 */
	public void writeRecord(RecordWriter rw) throws RecordsFileException, IOException, QueueException {
		if (useQueue) {
			if(!queue.addToQueue(rw)) {
				for (Map.Entry<String, RecordWriter> entry: queue.getQueue().entrySet()) {
					boolean append = entry.getValue().isAppend();
					if (file.recordExists(entry.getValue().getKey())) {
						file.updateRecord(entry.getValue());
						continue;
					}
					if (append) {
						file.quickInsertRecord(entry.getValue());
						continue;
					}
					file.insertRecord(entry.getValue());
				}
				queue.clearQueue();
				if (!queue.addToQueue(rw))
					throw new QueueException("Could not add Item to the queue");
			}
		} else {
			if (file.recordExists(rw.getKey()))
				file.updateRecord(rw);
			else if (rw.isAppend())
				file.quickInsertRecord(rw);
			else
				file.insertRecord(rw);
		}
		file.removeFromCache(rw.getKey());
	}

	/**
	 * Writes a Record to the DB without searching for free-space. Existing ones will be updated.
	 * </p>
	 * If the queue is activated it will try to add the RecordWriter to it.</br>
	 * If it fails it will write the whole queue to the DB, clear it and finally</br>
	 * add the given RecordWriter to the now empty queue.
	 * @param rw - RecordWriter object which should be written
	 * @throws IOException
	 * @throws RecordsFileException
	 * @throws QueueException
	 */
	public void appendRecord(RecordWriter rw) throws RecordsFileException, IOException, QueueException {
		rw.setAppend(true);
		if (useQueue) {
			if(!queue.addToQueue(rw)) {
				for (Map.Entry<String, RecordWriter> entry: queue.getQueue().entrySet()) {
					if (file.recordExists(entry.getValue().getKey())) {
						file.updateRecord(entry.getValue());
						continue;
					}
					file.quickInsertRecord(entry.getValue());
				}
				queue.clearQueue();
				if (!queue.addToQueue(rw))
					throw new QueueException("Could not add Item to the queue");
			}
		} else {
			file.quickInsertRecord(rw);
		}
		file.removeFromCache(rw.getKey());
	}

	/**
	 * Writes a Record to the DB ignoring the queue. Existing ones will be updated.
	 * @param rw - RecordWriter object which should be written
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	public void quickWriteRecord(RecordWriter rw) throws RecordsFileException, IOException {
		if (file.recordExists(rw.getKey()))
			file.updateRecord(rw);
		else
			file.insertRecord(rw);
		file.removeFromCache(rw.getKey());
	}

	/**
	 * Writes a Record to the DB without searching for free-space and ignoring the queue.</br>
	 * Existing ones will be updated.
	 * @param rw - RecordWriter object which should be written
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	public void quickInsertRecord(RecordWriter rw) throws RecordsFileException, IOException {
		if (file.recordExists(rw.getKey()))
			file.updateRecord(rw);
		else
			file.quickInsertRecord(rw);
		file.removeFromCache(rw.getKey());
	}

	/**
	 * Deletes the Record associated with the key. This will also remove the record </br>
	 * from the queue if it is activated and the record is in it.
	 * @param key - The key as a String to indicate which Record should be deleted
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	public void deleteRecord(String key) throws RecordsFileException, IOException {
		if (useQueue)
			queue.removeQueuedItem(key);
		file.deleteRecord(key);
		file.removeFromCache(key);
	}

	/**
	 * Closes this instance of the DataBase.
	 * @param unsafe - If true the file will be closed without saving pending records
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public void closeDB(boolean unsafe) throws IOException, RecordsFileException {
		if (unsafe)
			forceCloseDB();
		else
			safelyCloseDB();
	}

	/**
	 * Close the database safley. This will save all things in the queue before closing the file.
	 * @throws RecordsFileException
	 * @throws IOException
	 */
	private void safelyCloseDB() throws IOException, RecordsFileException {
		if (useQueue) {
			for (Map.Entry<String, RecordWriter> entry: queue.getQueue().entrySet()) {
				if (file.recordExists(entry.getValue().getKey())) {
					file.updateRecord(entry.getValue());
					continue;
				}
				file.insertRecord(entry.getValue());
			}
		}
		file.close();
	}

	/**
	 * Close the DB without saving things in the queue
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	private void forceCloseDB() throws IOException, RecordsFileException {
		file.close();
	}
}