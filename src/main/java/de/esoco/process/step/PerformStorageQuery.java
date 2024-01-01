//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.process.step;

import de.esoco.lib.expression.Function;
import de.esoco.process.ProcessException;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ProcessStep;
import de.esoco.storage.Query;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.QueryResult;
import de.esoco.storage.Storage;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;
import de.esoco.storage.StorageRelationTypes;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static de.esoco.process.ProcessRelationTypes.PROGRESS;
import static de.esoco.process.ProcessRelationTypes.PROGRESS_DESCRIPTION;
import static de.esoco.process.ProcessRelationTypes.PROGRESS_MAXIMUM;
import static de.esoco.process.ProcessRelationTypes.TARGET_PARAM;
import static de.esoco.storage.StorageRelationTypes.STORAGE_QUERY;
import static de.esoco.storage.StorageRelationTypes.STORAGE_QUERY_PREDICATE;
import static de.esoco.storage.StorageRelationTypes.STORAGE_QUERY_RESULT;
import static de.esoco.storage.StorageRelationTypes.STORAGE_QUERY_SIZE;
import static org.obrel.core.RelationTypes.newRelationType;

/**
 * A process step that performs an arbitrary storage query and places the result
 * in a certain target parameter. This step can be invoked until the last object
 * that fulfills the query has been read. After that the target parameter will
 * contain NULL and the query objects will be closed. The query objects will be
 * stored in the parameters {@link StorageRelationTypes#STORAGE_QUERY} and
 * {@link StorageRelationTypes#STORAGE_QUERY_RESULT}. Therefore these parameters
 * must not be used by other steps or objects before the query loop has been
 * completed.
 *
 * <p>The following parameters must or can be set:</p>
 *
 * <ul>
 *   <li>{@link StorageRelationTypes#STORAGE_QUERY_PREDICATE}: the query
 *     predicate to use for the storage query.</li>
 *   <li>{@link ProcessRelationTypes#TARGET_PARAM}: The parameter to store the
 *     query result objects in. It is the responsibility of the application that
 *     creates the step to make sure that the result type of the query and the
 *     target type are compatible or else this step or subsequent process steps
 *     will fail.</li>
 * </ul>
 *
 * <p>The parameter {@link ProcessRelationTypes#PROGRESS} will be incremented
 * with each queried entity.</p>
 *
 * @author eso
 */
public class PerformStorageQuery extends ProcessStep {

	/**
	 * An optional reference to a parameter that stores the query size
	 */
	public static final RelationType<Function<Object, String>>
		QUERY_PROGRESS_FORMAT =
		newRelationType("de.esoco.process.step.QUERY_PROGRESS_FORMAT",
			Function.class);

	private static final long serialVersionUID = 1L;

	static {
		RelationTypes.init(PerformStorageQuery.class);
	}

	/**
	 * Creates a new instance.
	 */
	public PerformStorageQuery() {
	}

	/**
	 * @see ProcessStep#canRollback()
	 */
	@Override
	protected boolean canRollback() {
		return true;
	}

	/**
	 * Closes the query, releases the storage, and removes the query parameters
	 * from the process.
	 *
	 * @see ProcessStep#cleanup()
	 */
	@Override
	protected void cleanup() {
		Query<?> query = getParameter(STORAGE_QUERY);

		if (query != null) {
			Storage storage = query.getStorage();

			try {
				query.close();
			} finally {
				storage.release();
				deleteParameters(STORAGE_QUERY, STORAGE_QUERY_RESULT);
			}
		}
	}

	/**
	 * @see ProcessStep#execute()
	 */
	@Override
	@SuppressWarnings("boxing")
	protected void execute() throws StorageException, ProcessException {
		Query<?> query = getParameter(STORAGE_QUERY);
		QueryResult<?> queryResult = getParameter(STORAGE_QUERY_RESULT);

		Function<Object, String> progressFormat =
			getParameter(QUERY_PROGRESS_FORMAT);

		if (queryResult == null) {
			QueryPredicate<?> queryCriteria =
				checkParameter(STORAGE_QUERY_PREDICATE);

			// create a new storage because multiple invocations may occur from
			// different threads if the process contains a progress display
			// interaction
			Storage storage =
				StorageManager.newStorage(queryCriteria.getQueryType());

			query = storage.query(queryCriteria);
			queryResult = query.execute();

			int querySize = query.size();

			setParameter(STORAGE_QUERY, query);
			setParameter(STORAGE_QUERY_RESULT, queryResult);
			setParameter(STORAGE_QUERY_SIZE, querySize);
			setParameter(PROGRESS_MAXIMUM, querySize);
		}

		Object nextObject = null;

		if (queryResult.hasNext()) {
			nextObject = queryResult.next();
		}

		@SuppressWarnings("unchecked")
		RelationType<Object> targetParam =
			(RelationType<Object>) checkParameter(TARGET_PARAM);

		setParameter(targetParam, nextObject);

		if (nextObject != null) {
			setParameter(PROGRESS, getParameter(PROGRESS) + 1);

			if (progressFormat != null) {
				setParameter(PROGRESS_DESCRIPTION,
					progressFormat.evaluate(nextObject));
			}
		} else {
			cleanup();
		}
	}

	/**
	 * Checks whether the storage query is already initialized.
	 *
	 * @return TRUE if the query is initialized
	 */
	protected boolean isQueryInitialized() {
		return getParameter(STORAGE_QUERY_RESULT) != null;
	}

	/**
	 * @see ProcessStep#rollback()
	 */
	@Override
	protected void rollback() {
		cleanup();
	}
}
