/*
 * Copyright (C) 2014 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.rdf2go;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;

/**
* Created by Albrecht Striffler (denkbares GmbH) on 25.04.14.
*/
public class LockableResultTable implements QueryResultTable {

	private ReentrantReadWriteLock.ReadLock lock;
	private final QueryResultTable table;

	public LockableResultTable(ReentrantReadWriteLock.ReadLock lock, QueryResultTable table) {
		this.lock = lock;
		this.table = table;
	}

	public void lock() {
		lock.lock();
	}

	public void unlock() {
		lock.unlock();
	}

	@Override
	public List<String> getVariables() {
		return table.getVariables();
	}

	@Override
	public ClosableIterator<QueryRow> iterator() {
		return new LockableClosableIterator<QueryRow>(lock, table.iterator());
	}
}
