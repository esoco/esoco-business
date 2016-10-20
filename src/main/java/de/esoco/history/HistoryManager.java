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
package de.esoco.history;

import de.esoco.entity.Entity;
import de.esoco.entity.EntityManager;
import de.esoco.entity.ExtraAttributes;

import de.esoco.history.HistoryRecord.HistoryType;
import de.esoco.history.HistoryRecord.ReferenceType;

import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.manage.TransactionException;

import de.esoco.storage.Query;
import de.esoco.storage.QueryResult;
import de.esoco.storage.Storage;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static de.esoco.history.HistoryRecord.DETAILS;
import static de.esoco.history.HistoryRecord.HistoryType.GROUP;
import static de.esoco.history.HistoryRecord.ORIGIN;
import static de.esoco.history.HistoryRecord.PARENT;
import static de.esoco.history.HistoryRecord.REFERENCE;
import static de.esoco.history.HistoryRecord.ROOT_TARGET;
import static de.esoco.history.HistoryRecord.TARGET;
import static de.esoco.history.HistoryRecord.TIME;
import static de.esoco.history.HistoryRecord.TYPE;
import static de.esoco.history.HistoryRecord.VALUE;

import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.greaterOrEqual;
import static de.esoco.lib.expression.Predicates.ifProperty;
import static de.esoco.lib.expression.Predicates.lessOrEqual;

import static de.esoco.storage.StoragePredicates.forType;

import static org.obrel.core.RelationTypes.newFlagType;


/********************************************************************
 * This class contains static methods to query, create, and manipulate history
 * records. The manipulation methods like {@link #begin(Entity, Entity, String)}
 * are thread-based, i.e. they record history entries based on the invoking
 * thread.
 *
 * @author eso
 */
@RelationTypeNamespace("de.esoco.history")
public class HistoryManager
{
	//~ Static fields/initializers ---------------------------------------------

	/** The key for the default templates in {@link #HISTORY_NOTE_TEMPLATES}. */
	public static final String DEFAULT_HISTORY_NOTE_TEMPLATES = "{DEFAULT}";

	/**
	 * A flag relation type that marks objects which create history records.
	 * This can be used as a hint to wrap it in a history group if necessary.
	 */
	public static final RelationType<Boolean> HISTORIZED = newFlagType();

	/**
	 * A configuration extra attribute that contains mapping from process names
	 * to templates for history notes. The map values must be in the form
	 * "[Template]\n[Template]\n..." where each template must have the format
	 * "[Title]|[Text]". [Title] must not contain linefeeds or other control
	 * characters and preferably only text to prevent parsing errors of the
	 * extra attribute. If the [Text] part needs to contain linefeeds they must
	 * be escaped as '$n'.
	 *
	 * <p>The map key is either the name of the process in which the templates
	 * should be applied or the string {@link #DEFAULT_HISTORY_NOTE_TEMPLATES}
	 * for which the value contains the default templates that are always
	 * available.</p>
	 */
	public static final RelationType<Map<String, String>> HISTORY_NOTE_TEMPLATES =
		ExtraAttributes.newOrderedMapExtraAttribute();

	private static ThreadLocal<HistoryRecord> aThreadHistoryGroup =
		new ThreadLocal<HistoryRecord>();

	static
	{
		RelationTypes.init(HistoryManager.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private HistoryManager()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Begins a new group of history records for the current thread. The topmost
	 * group and all it's children will be made persistent when a matching call
	 * to {@link #commit(boolean)} is made. A call to the {@link #rollback()}
	 * method will cancel the complete history group hierarchy.
	 *
	 * @param rOrigin The entity from which the record originates (may be NULL)
	 * @param rTarget The target entity referenced by the record (may be NULL)
	 * @param sValue  The value of this record
	 */
	public static void begin(Entity rOrigin, Entity rTarget, String sValue)
	{
		HistoryRecord rGroup = aThreadHistoryGroup.get();

		if (rGroup == null)
		{
			rGroup =
				createRecord(GROUP, rOrigin, rTarget, null, sValue, null, null);
		}
		else
		{
			rGroup = rGroup.addDetail(GROUP, rTarget, sValue, null, null);
		}

		aThreadHistoryGroup.set(rGroup);
	}

	/***************************************
	 * Commits a group of history records. The group must previously have been
	 * started with a call to {@link #begin(Entity, Entity, String)}.
	 *
	 * @param  bKeepIfEmpty TRUE to keep empty groups, FALSE to discard them
	 *
	 * @throws TransactionException If storing the group fails
	 */
	public static void commit(boolean bKeepIfEmpty) throws TransactionException
	{
		HistoryRecord rRecord = aThreadHistoryGroup.get();

		if (rRecord == null)
		{
			throw new IllegalStateException("Unmatched call to commit()");
		}
		else
		{
			HistoryRecord rParent = rRecord.get(PARENT);

			boolean bKeep = !rRecord.get(DETAILS).isEmpty() || bKeepIfEmpty;

			if (rParent != null)
			{
				aThreadHistoryGroup.set(rParent);

				if (!bKeep)
				{
					rParent.get(DETAILS).remove(rRecord);
				}
			}
			else
			{
				aThreadHistoryGroup.remove();

				if (bKeep)
				{
					EntityManager.storeEntity(rRecord, null);
				}
			}
		}
	}

	/***************************************
	 * Returns the history entries for a target object that have a certain type.
	 *
	 * @param  rTarget The target entity to search records for
	 * @param  rType   The type of the history records to query or NULL
	 *
	 * @return A list of history records that match the given criteria (may be
	 *         empty but will never be NULL)
	 *
	 * @throws StorageException         If querying the history storage fails
	 * @throws IllegalArgumentException If the target entity is NULL
	 *
	 * @see    #getHistoryFor(Entity, HistoryType, Date, Date, Entity)
	 */
	public static List<HistoryRecord> getHistoryFor(
		Entity		rTarget,
		HistoryType rType) throws StorageException
	{
		return getHistoryFor(rTarget, rType, null, null, null);
	}

	/***************************************
	 * Returns the history entries for a target object. The returned records can
	 * optionally be constrained to certain criteria. Criteria that are NULL
	 * will be ignored.
	 *
	 * @param  rTarget   The target entity to search records for
	 * @param  rType     The type of the history records to query or NULL
	 * @param  rFromDate The start date for the queried records or NULL
	 * @param  rToDate   The end date for the queried records or NULL
	 * @param  rOrigin   The origin entity to search records for or NULL
	 *
	 * @return A list of history records that match the given criteria (may be
	 *         empty but will never be NULL)
	 *
	 * @throws StorageException         If querying the history storage fails
	 * @throws IllegalArgumentException If the target entity is NULL
	 */
	public static List<HistoryRecord> getHistoryFor(Entity		rTarget,
													HistoryType rType,
													Date		rFromDate,
													Date		rToDate,
													Entity		rOrigin)
		throws StorageException
	{
		if (rTarget == null)
		{
			throw new IllegalArgumentException("Target must not be NULL");
		}

		Predicate<Relatable> aCriteria = null;

		aCriteria = addEqualCriterion(aCriteria, TARGET, rTarget);
		aCriteria = addEqualCriterion(aCriteria, ORIGIN, rOrigin);
		aCriteria = addEqualCriterion(aCriteria, TYPE, rType);
		aCriteria = addTimeCriterion(aCriteria, rFromDate, true);
		aCriteria = addTimeCriterion(aCriteria, rToDate, false);

		Storage rStorage = getHistoryStorage();

		try
		{
			Query<HistoryRecord> rQuery =
				rStorage.query(forType(HistoryRecord.class, aCriteria));

			try
			{
				QueryResult<HistoryRecord> rResult = rQuery.execute();

				List<HistoryRecord> aRecords = new ArrayList<HistoryRecord>();

				while (rResult.hasNext())
				{
					aRecords.add(rResult.next());
				}

				return aRecords;
			}
			finally
			{
				rQuery.close();
			}
		}
		finally
		{
			rStorage.release();
		}
	}

	/***************************************
	 * Returns the storage instance that will be used by history operations from
	 * the current (= invoking) thread to access history records. When the
	 * returned storage instance is no longer needed, the calling code must
	 * invoke the method {@link Storage#release()} to free the storage
	 * resources. See the method {@link StorageManager#getStorage(Object)} for
	 * details.
	 *
	 * <p>This method is intended mainly for internal use by the history
	 * implementation but it may also be used by special application code which
	 * needs to access the history storage and is therefore public. Applications
	 * should be careful when manipulating the history storage, especially when
	 * invoking {@link Storage#commit()} or {@link Storage#rollback()}.
	 * Furthermore, if no history operations have been performed by the current
	 * thread before, the invocation may create a new storage instance and thus
	 * acquire system resources.</p>
	 *
	 * @return The history storage instance for the current thread
	 *
	 * @throws StorageException If acquiring a new storage instance fails
	 */
	public static Storage getHistoryStorage() throws StorageException
	{
		return StorageManager.getStorage(HistoryRecord.class);
	}

	/***************************************
	 * This method should be invoked at application startup to initialize the
	 * history functionality.
	 */
	public static void init()
	{
		EntityManager.init(HistoryRecord.class);
	}

	/***************************************
	 * Allows to check whether a history is recorded for the current thread.
	 *
	 * @return The recording
	 */
	public static boolean isRecording()
	{
		return aThreadHistoryGroup.get() != null;
	}

	/***************************************
	 * Records a history entry. If a history group has been started before by
	 * means of {@link #begin(Entity, Entity, String)} the new entry will be
	 * added as a detail record to that group. If no group is active a single
	 * history record will be created and stored immediately.
	 *
	 * @param  rType   The type of the new history record
	 * @param  rOrigin The entity from which the record originates (may be NULL)
	 * @param  rTarget The target entity referenced by the record (may be NULL)
	 * @param  sValue  The value of this record
	 *
	 * @throws TransactionException If storing the new record fails
	 */
	public static void record(HistoryType rType,
							  Entity	  rOrigin,
							  Entity	  rTarget,
							  String	  sValue) throws TransactionException
	{
		record(rType, rOrigin, rTarget, null, sValue, null, null);
	}

	/***************************************
	 * Records a history entry with a reference value. If a history group has
	 * been started before by means of {@link #begin(Entity, Entity, String)}
	 * the new entry will be added as a detail record to that group. If no group
	 * is active a single history record will be created and stored immediately.
	 *
	 * @param  rType           The type of the new history record
	 * @param  rOrigin         The entity from which the record originates (may
	 *                         be NULL)
	 * @param  rTarget         The target entity referenced by the record (may
	 *                         be NULL)
	 * @param  rRootTarget     The root target entity of the record (may be
	 *                         NULL, will be ignored if a group is active)
	 * @param  sValue          The value of this record
	 * @param  rReferenceType  The type of the reference value
	 * @param  sReferenceValue The reference value
	 *
	 * @throws TransactionException If storing the new record fails
	 */
	public static void record(HistoryType   rType,
							  Entity		rOrigin,
							  Entity		rTarget,
							  Entity		rRootTarget,
							  String		sValue,
							  ReferenceType rReferenceType,
							  String		sReferenceValue)
		throws TransactionException
	{
		if (rType == GROUP)
		{
			throw new IllegalArgumentException("Invalid record type: " + GROUP);
		}

		HistoryRecord rGroup = aThreadHistoryGroup.get();

		if (rGroup == null)
		{
			HistoryRecord aRecord =
				createRecord(rType,
							 rOrigin,
							 rTarget,
							 rRootTarget,
							 sValue,
							 rReferenceType,
							 sReferenceValue);

			EntityManager.storeEntity(aRecord, null);
		}
		else
		{
			rGroup.addDetail(rType,
							 rTarget,
							 sValue,
							 rReferenceType,
							 sReferenceValue);
		}
	}

	/***************************************
	 * Cancels any currently active hierarchy of history records and discards
	 * it. This method must be invoked when an exception occurs after a history
	 * group has been started with {@link #begin(Entity, Entity, String)} to
	 * remove any remaining history group for the current thread. Additional
	 * invocations of this method will be ignored.
	 */
	public static void rollback()
	{
		aThreadHistoryGroup.remove();
	}

	/***************************************
	 * Performs a shutdown of the history manager and frees all allocated
	 * resources.
	 */
	public static void shutdown()
	{
		aThreadHistoryGroup = null;
	}

	/***************************************
	 * Internal method to create a new history record.
	 *
	 * @param  rType           The type of the new history record
	 * @param  rOrigin         The entity from which the record originates
	 * @param  rTarget         The target entity referenced by the record (may
	 *                         be NULL)
	 * @param  rRootTarget     The root target of the record (may be NULL)
	 * @param  sValue          The value of this record
	 * @param  rReferenceType  The type of the reference value or NULL for none
	 * @param  sReferenceValue The reference value or NULL for none
	 *
	 * @return A new history record initialized with the given parameters
	 */
	static HistoryRecord createRecord(HistoryType   rType,
									  Entity		rOrigin,
									  Entity		rTarget,
									  Entity		rRootTarget,
									  String		sValue,
									  ReferenceType rReferenceType,
									  String		sReferenceValue)
	{
		HistoryRecord aRecord = new HistoryRecord();

		aRecord.set(TIME, new Date());
		aRecord.set(TYPE, rType);
		aRecord.set(ORIGIN, rOrigin);
		aRecord.set(ROOT_TARGET, rRootTarget);
		aRecord.set(TARGET, rTarget);
		aRecord.set(VALUE, sValue);

		if (rReferenceType != null && sReferenceValue != null)
		{
			aRecord.set(REFERENCE,
						rReferenceType.name() + ":" + sReferenceValue);
		}

		return aRecord;
	}

	/***************************************
	 * Internal method to conditionally create a new query predicate for a
	 * certain history property. If an existing query is given as the first
	 * argument the newly created predicate will be appended to it with an AND
	 * expression.
	 *
	 * @param  aQuery        An optional existing query or NULL to create
	 * @param  rProperty     The type of the history property to be queried
	 * @param  rCompareValue The value to compare with
	 *
	 * @return A new query object, either as a single query or concatenated with
	 *         the first argument
	 */
	private static Predicate<Relatable> addEqualCriterion(
		Predicate<Relatable> aQuery,
		RelationType<?>		 rProperty,
		Object				 rCompareValue)
	{
		if (rCompareValue != null)
		{
			aQuery =
				Predicates.and(aQuery,
							   ifProperty(rProperty, equalTo(rCompareValue)));
		}

		return aQuery;
	}

	/***************************************
	 * Internal method to conditionally create a new query predicate for the
	 * TIME property of history records. If an existing query is given as the
	 * first argument the newly created predicate will be appended to it with an
	 * AND expression.
	 *
	 * @param  aQuery   An optional existing query or NULL to create
	 * @param  rDate    The date to compare with
	 * @param  bIsStart TRUE for the start date, FALSE for the end date (both
	 *                  inclusive)
	 *
	 * @return A new query object, either as a single query or concatenated with
	 *         the first argument
	 */
	private static Predicate<Relatable> addTimeCriterion(
		Predicate<Relatable> aQuery,
		Date				 rDate,
		boolean				 bIsStart)
	{
		if (rDate != null)
		{
			Predicate<Relatable> aPredicate;

			if (bIsStart)
			{
				aPredicate = ifProperty(TIME, greaterOrEqual(rDate));
			}
			else
			{
				aPredicate = ifProperty(TIME, lessOrEqual(rDate));
			}

			aQuery = Predicates.and(aQuery, aPredicate);
		}

		return aQuery;
	}
}
