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

import java.util.concurrent.ConcurrentHashMap;

import de.Lathanael.BinaryFileDB.API.RecordWriter;
import de.Lathanael.BinaryFileDB.Exception.QueueException;

/**
 * Implements a simple Queue to lesser the IO of the harddisk by reducing write accesses.
 * @author Lathanael (aka Philippe Leipold)
 */
public class DataWriteQueue {

	private ConcurrentHashMap<String, RecordWriter> queue;
	private int counter = 0;
	private int size;

	/**
	 * Constructs a queue object with the given size.
	 * @param queueSize - The initial size of the queue.
	 */
	public DataWriteQueue(int queueSize) {
		queue = new ConcurrentHashMap<String,RecordWriter>((int) (queueSize/0.75)+1);
		size = queueSize;
	}

	/**
	 * Set the size of the queue.
	 * @param size the new size
	 * @throws QueueException - Thrown when the new size is too small
	 */
	public void setSize(int size) throws QueueException {
		if (size < counter)
			throw new QueueException("The size is to small because there are currently" +
					" more objects in the queue than the new size would allow!");
		this.size = size;
	}
	/**
	 * Gets the total size of this queue
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Gets the current size of the queue, meaning how many items are queued.
	 */
	public int getCurrentSize() {
		return counter;
	}

	/**
	 * Adds a {@linkplain de.Lathanael.BinaryFileDB.API.RecordWriter RecordWriter}
	 * to the queue if the current size of the queue allows it.</br>
	 * Already existing entries will be replaced!
	 * @param rw - The RecordWriter object.
	 * @return True if the RecordWriter was added else false.
	 */
	public boolean addToQueue(RecordWriter rw) {
		if (counter < size) {
			Object o;
			 o = queue.put(rw.getKey(), rw);
			if (o != null) {
				return true;
			} else {
				counter++;
				return true;
			}
		} else
			return false;
	}

	/**
	 * Gets the queue of this instance and returns it.
	 */
	public ConcurrentHashMap<String, RecordWriter> getQueue() {
		return queue;
	}

	/**
	 * Gets the queued item which is mapped to the given key.
	 * @param key - String to which the RecordWriter is mapped to
	 * @return The RecordWriter object mapped to the key or {@code null} if none was mapped.
	 */
	public RecordWriter getQueuedItem(String key) {
		RecordWriter rw = queue.get(key);
		return rw;
	}

	/**
	 * Gets the queued iem which is mapped to the given key and removes it from the queue.
	 * @param key - String to which the RecordWriter is mapped to
	 * @return The RecordWriter object mapped to the key or {@code null} if none was mapped.
	 */
	public RecordWriter getAndRemoveQueuedItem(String key) {
		RecordWriter rw = queue.get(key);
		if (rw != null) {
			queue.remove(key);
			counter--;
		}
		return rw;
	}

	/**
	 * Removes the item from the queue if it exists.
	 * @param key - String to which the RecordWriter is mapped to
	 */
	public void removeQueuedItem(String key) {
		Object o = queue.remove(key);
		if (o != null)
			counter--;
	}

	/**
	 * Clears the queue. Make sure you save the items in the queue first!
	 */
	public void clearQueue() {
		queue.clear();
		counter = 0;
	}
}
