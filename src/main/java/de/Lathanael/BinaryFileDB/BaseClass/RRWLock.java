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

import java.util.HashMap;
import java.util.Map;



/**
 * An implementation of a fully reentrant Read-Lock and Write-Lock
 * @author original by: Jakob Jenkov (http://java.dzone.com/news/java-concurrency-read-write-lo?page=0,1)
 * @author modified by: Lathanael (aka Philippe Leipold)
 */
public class RRWLock {

	private Map<Thread, Counter> readingThreads = new HashMap<Thread, Counter>();

	private int writeAccesses = 0;
	private int writeRequests  = 0;
	private Thread writingThread = null;


	/**
	 * Get a read Lock
	 * @throws InterruptedException
	 */
	public synchronized void getReadLock() throws InterruptedException {
		Thread callingThread = Thread.currentThread();
		while(!canRead(callingThread))
			wait();

		Counter c = getReaderCount(callingThread);
		c.count++;
		readingThreads.put(callingThread, c);
	}

	/**
	 * Release a read Lock
	 */
	public synchronized void releaseReadLock() {
		Thread callingThread = Thread.currentThread();
		if(!isReader(callingThread))
			throw new IllegalMonitorStateException("Calling Thread does not hold a read lock on this ReadWriteLock");
		Counter c = getReaderCount(callingThread);
		if(c.count == 1)
			readingThreads.remove(callingThread);
		else
			c.count--;

		notifyAll();
	}

	/**
	 * Get a write Lock
	 * @throws InterruptedException
	 */
	public synchronized void getWriteLock() throws InterruptedException {
		writeRequests++;
		Thread callingThread = Thread.currentThread();
		while(!canWrite(callingThread))
			wait();

		writeRequests--;
		writeAccesses++;
		writingThread = callingThread;
	}

	/**
	 * Release a write Lock
	 * @throws InterruptedException
	 */
	public synchronized void releaseWriteLock() throws InterruptedException {
		if(!isWriter(Thread.currentThread()))
			throw new IllegalMonitorStateException("Calling Thread does not hold the write lock on this ReadWriteLock");

		writeAccesses--;
		if(writeAccesses == 0)
			writingThread = null;

		notifyAll();
	}

	private boolean canRead(Thread callingThread) {
		if(isWriter(callingThread))
			return true;
		if(hasWriter())
			return false;
		if(isReader(callingThread))
			return true;
		if(hasRequests())
			return false;
		return true;
	}

	private boolean canWrite(Thread callingThread) {
		if(isReaderOnly(callingThread))
			return true;
		if(hasReaders())
			return false;
		if(writingThread == null)
			return true;
		if(!isWriter(callingThread))
			return false;
		return true;
	}

	private Counter getReaderCount(Thread callingThread) {
		Counter accessCount = readingThreads.get(callingThread);
		if(accessCount == null)
			return new Counter();

		return accessCount;
	}

	private boolean isWriter(Thread callingThread) {
		return writingThread == callingThread;
	}

	private boolean isReader(Thread callingThread) {
		return readingThreads.get(callingThread) != null;
	}

	private boolean isReaderOnly(Thread callingThread) {
		return readingThreads.size() == 1 && readingThreads.get(callingThread) != null;
	}

	private boolean hasWriter() {
		return writingThread != null;
	}

	private boolean hasReaders() {
		return readingThreads.size() > 0;
	}

	private boolean hasRequests() {
		return writeRequests > 0;
	}
}
