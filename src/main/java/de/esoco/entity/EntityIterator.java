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


/********************************************************************
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
public class EntityIterator<E extends Entity> implements Iterator<E>, Closeable
{
	//~ Instance fields --------------------------------------------------------

	private QueryPredicate<E> qEntities;
	private boolean			  bUseNewStorage;

	private Storage		   rStorage     = null;
	private Query<E>	   rQuery;
	private QueryResult<E> rQueryResult;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that performs a certain query on the default
	 * storage for the queried entity type and current thread.
	 *
	 * @param qEntities The entity query
	 */
	public EntityIterator(QueryPredicate<E> qEntities)
	{
		this(qEntities, false);
	}

	/***************************************
	 * Creates a new instance that performs a certain query.
	 *
	 * @param qEntities      The entity query to perform
	 * @param bUseNewStorage TRUE to perform the query on a new storage instance
	 *                       (instead of the shared storage for the current
	 *                       thread)
	 */
	public EntityIterator(QueryPredicate<E> qEntities, boolean bUseNewStorage)
	{
		this.qEntities	    = qEntities;
		this.bUseNewStorage = bUseNewStorage;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Closes the query result and releases the storage. This method must always
	 * be invoked after an entity iterator has been used to prevent resource
	 * leakage.
	 */
	@Override
	public void close()
	{
		if (rStorage != null)
		{
			try
			{
				if (rQuery != null)
				{
					rQuery.close();
				}
			}
			finally
			{
				rStorage.release();
				rStorage = null;
			}
		}
	}

	/***************************************
	 * Invokes a function on each iterated entity and closes the storage
	 * afterwards. Like in the iterator methods a storage exception that occurs
	 * will be wrapped into a {@link StorageRuntimeException}.
	 *
	 * @param fAction The function to invoke on each entity
	 */
	public void forEach(Action<E> fAction)
	{
		try
		{
			while (hasNext())
			{
				fAction.evaluate(next());
			}
		}
		finally
		{
			close();
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext()
	{
		try
		{
			checkPrepareQuery();

			return rQueryResult.hasNext();
		}
		catch (StorageException e)
		{
			handleError(e);

			// not reached, handleError always throws exception
			return false;
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public E next()
	{
		try
		{
			E rEntity = rQueryResult.next();

			if (rEntity.isRoot())
			{
				rEntity = EntityManager.checkCaching(rEntity);
			}

			return rEntity;
		}
		catch (StorageException e)
		{
			handleError(e);

			// not reached, handleError always throws exception
			return null;
		}
	}

	/***************************************
	 * Returns the size of the query result this iterator represents.
	 *
	 * @return The query size
	 *
	 * @see    Query#size()
	 */
	public int size()
	{
		try
		{
			checkPrepareQuery();

			return rQuery.size();
		}
		catch (StorageException e)
		{
			handleError(e);

			// not reached, handleError always throws exception
			return 0;
		}
	}

	/***************************************
	 * Prepares the query of this iterator if not yet done.
	 *
	 * @throws StorageException If the storage access fails
	 */
	protected void checkPrepareQuery() throws StorageException
	{
		if (rStorage == null)
		{
			Class<E> rQueryType = qEntities.getQueryType();

			rStorage =
				bUseNewStorage ? StorageManager.newStorage(rQueryType)
							   : StorageManager.getStorage(rQueryType);

			rQuery		 = rStorage.query(qEntities);
			rQueryResult = rQuery.execute();
		}
	}

	/***************************************
	 * Performs the error handling if an exception occurred in another iterator
	 * method.
	 *
	 * @param  eStorage e The exception
	 *
	 * @throws StorageRuntimeException The converted exception
	 */
	protected void handleError(StorageException eStorage)
		throws StorageRuntimeException
	{
		try
		{
			close();
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
			// continue with original exception
		}

		throw new StorageRuntimeException(eStorage);
	}
}
