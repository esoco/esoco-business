//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.entity;

import de.esoco.lib.expression.Action;
import de.esoco.lib.logging.Log;
import de.esoco.lib.manage.Closeable;
import de.esoco.storage.Query;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.QueryResult;
import de.esoco.storage.Storage;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;
import de.esoco.storage.StorageRuntimeException;

import java.util.Iterator;

/**
 * An iterator implementation for the iteration over an entity query. Because
 * the methods of the {@link Iterator} interface don't support throwing
 * exceptions a occurring {@link StorageException} will be converted into a
 * {@link StorageRuntimeException}.
 *
 * <p>Important: because the iterator holds references to storage objects the
 * associated resources must be released after the iterator has been used by
 * invoking the {@link #close()} method. It is recommended to do that in a
 * try-with-resource block if possible.</p>
 *
 * @author eso
 */
public class EntityIterator<E extends Entity>
	implements Iterator<E>, Closeable {

	private final QueryPredicate<E> entities;

	private final boolean useNewStorage;

	private Storage storage = null;

	private Query<E> query;

	private QueryResult<E> queryResult;

	/**
	 * Creates a new instance that performs a certain query on the default
	 * storage for the queried entity type and current thread.
	 *
	 * @param entities The entity query
	 */
	public EntityIterator(QueryPredicate<E> entities) {
		this(entities, false);
	}

	/**
	 * Creates a new instance that performs a certain query.
	 *
	 * @param entities      The entity query to perform
	 * @param useNewStorage TRUE to perform the query on a new storage instance
	 *                      (instead of the shared storage for the current
	 *                      thread)
	 */
	public EntityIterator(QueryPredicate<E> entities, boolean useNewStorage) {
		this.entities = entities;
		this.useNewStorage = useNewStorage;
	}

	/**
	 * Closes the query result and releases the storage. This method must
	 * always
	 * be invoked after an entity iterator has been used to prevent resource
	 * leakage.
	 */
	@Override
	public void close() {
		if (storage != null) {
			try {
				if (query != null) {
					query.close();
				}
			} finally {
				storage.release();
				storage = null;
			}
		}
	}

	/**
	 * Invokes a function on each iterated entity and closes the storage
	 * afterwards. Like in the iterator methods a storage exception that occurs
	 * will be wrapped into a {@link StorageRuntimeException}.
	 *
	 * @param action The function to invoke on each entity
	 */
	public void forEach(Action<E> action) {
		try {
			while (hasNext()) {
				action.evaluate(next());
			}
		} finally {
			close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		try {
			checkPrepareQuery();

			return queryResult.hasNext();
		} catch (StorageException e) {
			handleError(e);

			// not reached, handleError always throws exception
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E next() {
		try {
			E entity = queryResult.next();

			if (entity.isRoot()) {
				entity = EntityManager.checkCaching(entity);
			}

			return entity;
		} catch (StorageException e) {
			handleError(e);

			// not reached, handleError always throws exception
			return null;
		}
	}

	/**
	 * Sets the index of the next entity to be returned by the combination of
	 * calls to {@link #hasNext()} and {@link #next()}. If the index is invalid
	 * for the current query {@link #hasNext()} will return FALSE. This is
	 * based
	 * on {@link QueryResult#setPosition(int, boolean)}, so the same
	 * limitations
	 * apply.
	 *
	 * @param index    The new position of this iterator
	 * @param relative TRUE to set the position relative to the current
	 *                 position, FALSE to set an absolute position relative to
	 *                 the full query
	 */
	public void setPosition(int index, boolean relative) {
		try {
			checkPrepareQuery();
			queryResult.setPosition(index, relative);
		} catch (StorageException e) {
			handleError(e);
		}
	}

	/**
	 * Returns the size of the query result this iterator represents.
	 *
	 * @return The query size
	 * @see Query#size()
	 */
	public int size() {
		try {
			checkPrepareQuery();

			return query.size();
		} catch (StorageException e) {
			handleError(e);

			// not reached, handleError always throws exception
			return 0;
		}
	}

	/**
	 * Prepares the query of this iterator if not yet done.
	 *
	 * @throws StorageException If the storage access fails
	 */
	protected void checkPrepareQuery() throws StorageException {
		if (storage == null) {
			Class<E> queryType = entities.getQueryType();

			storage = useNewStorage ?
			          StorageManager.newStorage(queryType) :
			          StorageManager.getStorage(queryType);

			query = storage.query(entities);
			queryResult = query.execute();
		}
	}

	/**
	 * Performs the error handling if an exception occurred in another iterator
	 * method.
	 *
	 * @param storage e The exception
	 * @throws StorageRuntimeException The converted exception
	 */
	protected void handleError(StorageException storage)
		throws StorageRuntimeException {
		try {
			close();
		} catch (Exception e) {
			Log.error(e.getMessage());
			// continue with original exception
		}

		throw new StorageRuntimeException(storage);
	}
}
