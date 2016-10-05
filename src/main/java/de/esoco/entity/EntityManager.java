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
package de.esoco.entity;

import de.esoco.data.SessionManager;

import de.esoco.history.HistoryManager;
import de.esoco.history.HistoryRecord;
import de.esoco.history.HistoryRecord.HistoryType;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.CollectionFunctions;
import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.expression.function.AbstractAction;
import de.esoco.lib.logging.Log;
import de.esoco.lib.manage.MultiLevelCache;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.manage.TransactionManager;
import de.esoco.lib.reflect.ReflectUtil;

import de.esoco.storage.Query;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.QueryResult;
import de.esoco.storage.Storage;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;
import de.esoco.storage.StorageManager.MappingFactory;
import de.esoco.storage.StorageMapping;
import de.esoco.storage.StorageRelationTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.ListenerType;
import org.obrel.type.MetaTypes;

import static de.esoco.entity.EntityPredicates.forEntity;
import static de.esoco.entity.EntityPredicates.ifAttribute;
import static de.esoco.entity.EntityRelationTypes.CACHE_ENTITY;
import static de.esoco.entity.EntityRelationTypes.CONTEXT_MODIFIED_ENTITIES;
import static de.esoco.entity.EntityRelationTypes.CONTEXT_UPDATED_ENTITIES;
import static de.esoco.entity.EntityRelationTypes.DEPENDENT_STORE_ENTITIES;
import static de.esoco.entity.EntityRelationTypes.ENTITY_MODIFICATION_HANDLE;
import static de.esoco.entity.EntityRelationTypes.ENTITY_STORE_ORIGIN;
import static de.esoco.entity.EntityRelationTypes.EXTRA_ATTRIBUTES_READ;
import static de.esoco.entity.EntityRelationTypes.EXTRA_ATTRIBUTE_MAP;
import static de.esoco.entity.EntityRelationTypes.LAST_CHANGE;
import static de.esoco.entity.EntityRelationTypes.MASTER_ENTITY_ID;
import static de.esoco.entity.EntityRelationTypes.PARENT_ENTITY_ID;

import static de.esoco.lib.expression.CollectionPredicates.elementOf;
import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.ifRelation;
import static de.esoco.lib.expression.Predicates.isNull;

import static de.esoco.storage.StoragePredicates.like;
import static de.esoco.storage.StorageRelationTypes.PERSISTENT;
import static de.esoco.storage.StorageRelationTypes.STORAGE_MAPPING;

import static org.obrel.type.MetaTypes.MODIFIED;


/********************************************************************
 * A management class with static method to access, modify, and create entities.
 *
 * @author eso
 */
public class EntityManager
{
	//~ Static fields/initializers ---------------------------------------------

	/** The separator character for the prefix of global entity IDs. */
	public static final String GLOBAL_ID_PREFIX_SEPARATOR = "-";

	private static final String MSG_ENTITY_MODFICATION_LOCKED =
		"Modification of %s by %s not possible, currently locked by %s";

	private static final String MSG_CONCURRENT_MODIFICATION =
		"Modification of %s by %s instead of %s";

	private static final Map<String, Class<? extends Entity>> aIdPrefixRegistry =
		new HashMap<String, Class<? extends Entity>>();

	private static final MultiLevelCache<String, Entity> aEntityCache =
		createEntityCache();

	/**
	 * An entity cache implementation that always returns NULL which can be used
	 * to disabled entity caching.
	 */
	private static final EntityCache<Entity> NO_CACHE =
		new EntityCache<Entity>()
		{
			@Override
			public Entity getEntity(int nId)
			{
				return null;
			}

			@Override
			public void cacheEntity(Entity rEntity)
			{
			}
		};

	/** A listener relation type for the storing of entities */
	@SuppressWarnings("serial")
	private static final ListenerType<StoreListener, Entity> STORE_LISTENERS =
		new ListenerType<StoreListener, Entity>()
		{
			@Override
			protected void notifyListener(
				StoreListener rListener,
				Entity		  rEntity)
			{
				rListener.entityStored(rEntity);
			}
		};

	private static SessionManager rSessionManager		  = null;
	private static boolean		  bAutomaticChangeLogging = true;
	private static Lock			  aCacheLock			  = new ReentrantLock();

	private static final ThreadLocal<String>    aEntityModificationContextId =
		new ThreadLocal<>();
	private static final ThreadLocal<Relatable> aEntityModificationContext   =
		new ThreadLocal<>();

	private static Set<Class<? extends Entity>> aDeleteEnabledEntities =
		new HashSet<>();

	private static Map<String, Entity> aModifiedEntities = new HashMap<>();

	private static Map<String, Predicate<? super Entity>> aModificationLockRules =
		new HashMap<>();

	private static Map<Class<? extends Entity>, EntityCache<? extends Entity>> aEntityCacheMap =
		new HashMap<>();

	static
	{
		RelationTypes.init(EntityManager.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private EntityManager()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Adds a listener that will be notified if an entity is stored through the
	 * entity manager.
	 *
	 * @param rListener The listener to add
	 */
	public static void addStoreListener(StoreListener rListener)
	{
		getStoreListeners().add(rListener);
	}

	/***************************************
	 * Stores an entity in the global entity cache for lookup by the global
	 * entity ID. This is done automatically by the methods that query entities
	 * but can also be invoked by application code to cache entities that have
	 * just been created. The given entity must have a valid ID or else an
	 * exception will be thrown. The entity will be cached with a weak reference
	 * so that it will be removed from the cache if it is no longer referenced
	 * by application code.
	 *
	 * @param rEntity The entity to cache
	 */
	public static <E extends Entity> void cacheEntity(E rEntity)
	{
		if (rEntity != null && rEntity.isRoot())
		{
			@SuppressWarnings("unchecked")
			EntityCache<E> rCache =
				(EntityCache<E>) aEntityCacheMap.get(rEntity.getClass());

			if (rCache != null)
			{
				rCache.cacheEntity(rEntity);
			}
			else
			{
				aEntityCache.put(getGlobalEntityId(rEntity), rEntity);
				rEntity.set(CACHE_ENTITY);
			}
		}
	}

	/***************************************
	 * Checks whether all modified entities in an entity modification context
	 * have been stored. See {@link #setEntityModificationContext(String,
	 * Relatable)} for details.
	 *
	 * <p>This method is intended to be used internally by the framework only.
	 * </p>
	 *
	 * @param sContextId The ID of the entity modification context to check
	 * @param rContext   The {@link Relatable} object that serves as the context
	 */
	public static void checkUnsavedEntityModifications(
		String    sContextId,
		Relatable rContext)
	{
		Map<String, Entity> rEntities = rContext.get(CONTEXT_MODIFIED_ENTITIES);

		for (Entity rEntity : rEntities.values())
		{
			if (rEntity.isModified())
			{
				try
				{
					Log.infof("Entity %s modified by %s but not stored, reverting",
							  rEntity,
							  rEntity.get(ENTITY_MODIFICATION_HANDLE));
					resetEntity(rEntity);
				}
				catch (Exception e)
				{
					Log.warnf(e, "Could not revert entity %s", rEntity);
				}
			}
			else
			{
				Log.errorf("Entity %s not removed from context %s",
						   rEntity,
						   sContextId);
			}

			aModifiedEntities.remove(rEntity.getGlobalId());
		}

		rEntities.clear();
	}

	/***************************************
	 * Collects all entities in a hierarchy of entities that match a certain
	 * predicate by descending the entity tree at a certain child attribute.
	 *
	 * @param  rEntities       The list of root entities to start searching at
	 * @param  rChildAttribute The child attribute to descend the hierarchy at
	 * @param  rPredicate      The predicate to evaluate the entities with
	 *
	 * @return A new list containing the resulting entities (may be empty but
	 *         will never be NULL)
	 */
	public static <E extends Entity> List<E> collectDownwards(
		List<E>				  rEntities,
		RelationType<List<E>> rChildAttribute,
		Predicate<? super E>  rPredicate)
	{
		List<E> aResult = CollectionUtil.collect(rEntities, rPredicate);

		for (E rEntity : rEntities)
		{
			aResult.addAll(collectDownwards(rEntity.get(rChildAttribute),
											rChildAttribute,
											rPredicate));
		}

		return aResult;
	}

	/***************************************
	 * Deletes the given {@link Entity} from the database. Children attached to
	 * this {@link Entity} are <b>not</b> deleted.
	 *
	 * @param  rEntity The {@link Entity} to delete from the database.
	 *
	 * @throws StorageException     If accessing the storage fails
	 * @throws TransactionException If the deletion fails
	 *
	 * @see    #delete(Entity, boolean)
	 */
	public static void delete(Entity rEntity) throws StorageException,
													 TransactionException
	{
		delete(rEntity, false);
	}

	/***************************************
	 * Deletes the given {@link Entity} and all of its children from the
	 * database.
	 *
	 * @param  rEntity         The entity to delete.
	 * @param  bDeleteChildren TRUE if all children of the given entity should
	 *                         be deleted as well, FALSE otherwise.
	 *
	 * @throws StorageException     If accessing the storage fails
	 * @throws TransactionException If the deletion fails
	 */
	public static void delete(Entity rEntity, boolean bDeleteChildren)
		throws StorageException, TransactionException
	{
		Storage rStorage = StorageManager.getStorage(rEntity.getClass());

		TransactionManager.begin();
		TransactionManager.addTransactionElement(rStorage);

		try
		{
			if (bDeleteChildren)
			{
				deleteChildren(rEntity, rStorage);
			}

			rStorage.delete(rEntity);
			TransactionManager.commit();
		}
		catch (Exception e)
		{
			TransactionManager.rollback();

			throw e;
		}
	}

	/***************************************
	 * Deletes all given {@link Entity Entities} from the database. This does
	 * <b>not</b> delete any child entities that a entity might have
	 *
	 * @param  rEntities The {@link Entity Entities} to delete from the
	 *                   database.
	 *
	 * @throws StorageException     If accessing the storage fails
	 * @throws TransactionException If the deletion fails
	 *
	 * @see    #deleteAll(Collection, boolean)
	 */
	public static void deleteAll(Collection<Entity> rEntities)
		throws StorageException, TransactionException
	{
		TransactionManager.begin();

		try
		{
			for (Entity rEntity : rEntities)
			{
				delete(rEntity, false);
			}

			TransactionManager.commit();
		}
		catch (Exception e)
		{
			TransactionManager.rollback();

			throw e;
		}
	}

	/***************************************
	 * Deletes all given {@link Entity Entities} from the database. This does
	 * <b>also</b> delete any child entities that a entity might have if
	 * bDeleteChildren is TRUE.
	 *
	 * @param  rEntities       rEntities The {@link Entity Entities} to delete
	 *                         from the database.
	 * @param  bDeleteChildren TRUE if child entities of an entity should be
	 *                         deleted as well.
	 *
	 * @throws StorageException     If accessing the storage fails
	 * @throws TransactionException If the deletion fails
	 */
	public static <E extends Entity> void deleteAll(
		Collection<E> rEntities,
		boolean		  bDeleteChildren) throws StorageException,
											  TransactionException
	{
		for (Entity rEntity : rEntities)
		{
			delete(rEntity, bDeleteChildren);
		}
	}

	/***************************************
	 * Disables the caching of a certain entity type. The given entity class
	 * must be of the exact type for which the caching shall be disabled.
	 *
	 * @param rEntityClasses The entity type to disabled caching for
	 */
	@SafeVarargs
	public static void disableCaching(Class<? extends Entity>... rEntityClasses)
	{
		for (Class<? extends Entity> rClass : rEntityClasses)
		{
			aEntityCacheMap.put(rClass, NO_CACHE);
		}
	}

	/***************************************
	 * Prevents the deletion of entities of a certain type. After this method
	 * has been invoked it is no longer possible to invoke the method {@link
	 * #delete(Entity, boolean)} for the given entity type.
	 *
	 * @param rEntityClass The type for which to disable the deletion
	 *
	 * @see   #enableDeletionOf(Class)
	 * @see   #isDeletionEnabledFor(Class)
	 */
	public static void disableDeletionOf(Class<? extends Entity> rEntityClass)
	{
		aDeleteEnabledEntities.remove(rEntityClass);
	}

	/***************************************
	 * Allows to delete entities of a certain type. This method must be invoked
	 * once before the method {@link #delete(Entity, boolean)} can be invoked
	 * for the given entity type.
	 *
	 * @param rEntityClass The type for which to enable the deletion
	 *
	 * @see   #disableDeletionOf(Class)
	 * @see   #isDeletionEnabledFor(Class)
	 */
	public static void enableDeletionOf(Class<? extends Entity> rEntityClass)
	{
		aDeleteEnabledEntities.add(rEntityClass);
	}

	/***************************************
	 * Performs an entity query and evaluates all entities that match the query
	 * predicate with a function. Optionally a predicate can be given that stops
	 * the query as soon as the predicate evaluates to FALSE.
	 *
	 * @param  qEntities      The query predicate for the entities to evaluate
	 * @param  pStopCondition An optional predicate that defines a stop
	 *                        condition by evaluating to FALSE or NULL for none
	 * @param  fEvaluation    A function to evaluate each queried entity
	 *
	 * @throws StorageException If the storage access fails
	 */
	@SuppressWarnings("boxing")
	public static <E extends Entity> void evaluateEntities(
		QueryPredicate<E>	   qEntities,
		Predicate<? super E>   pStopCondition,
		Function<? super E, ?> fEvaluation) throws StorageException
	{
		Class<E> rQueryType = qEntities.getQueryType();
		Storage  rStorage   = StorageManager.getStorage(rQueryType);

		try (Query<E> rQuery = rStorage.query(qEntities))
		{
			QueryResult<E> rResult = rQuery.execute();

			while (rResult.hasNext())
			{
				E rEntity = rResult.next();

				if (rEntity.isRoot())
				{
					rEntity = checkCaching(rEntity);
				}

				if (pStopCondition == null || pStopCondition.evaluate(rEntity))
				{
					fEvaluation.evaluate(rEntity);
				}
				else
				{
					break;
				}
			}
		}
		finally
		{
			rStorage.release();
		}
	}

	/***************************************
	 * Fetches all global extra attributes for a collections of entities. This
	 * call will only perform a single storage query for all extra attributes
	 * and thus considerably speeds up the querying of extra attributes for
	 * larger sets of entities.
	 *
	 * @param  rEntities The entities to fetch the extra attributes for
	 *
	 * @throws StorageException If querying the extra attributes fails
	 */
	public static void fetchExtraAttributes(
		Collection<? extends Entity> rEntities) throws StorageException
	{
		QueryPredicate<ExtraAttribute> qExtraAttributes =
			forEntity(ExtraAttribute.class,
					  ExtraAttribute.OWNER.is(equalTo(null))
					  .and(ExtraAttribute.ENTITY.is(elementOf(rEntities))));

		evaluateEntities(qExtraAttributes,
						 null,
			new AbstractAction<ExtraAttribute>("AssignExtraAttribute")
			{
				@Override
				public void execute(ExtraAttribute rXA)
				{
					rXA.get(ExtraAttribute.ENTITY)
					   .get(EXTRA_ATTRIBUTE_MAP)
					   .put(rXA.get(ExtraAttribute.KEY).toString(), rXA);
				}
			});

		for (Entity rEntity : rEntities)
		{
			if (!rEntity.hasRelation(EXTRA_ATTRIBUTES_READ))
			{
				rEntity.set(EXTRA_ATTRIBUTES_READ);
			}
		}
	}

	/***************************************
	 * Fetches the complete child hierarchy of a parent entity.
	 *
	 * @param  rParent         The parent entity
	 * @param  rChildAttribute The child attribute to fetch the hierarchy of
	 *
	 * @throws StorageException If reading a child fails
	 */
	@SuppressWarnings("unchecked")
	public static <P extends Entity, C extends Entity> void fetchHierarchy(
		P					  rParent,
		RelationType<List<C>> rChildAttribute) throws StorageException
	{
		EntityDefinition<C> rChildDef			  =
			(EntityDefinition<C>) rChildAttribute.get(STORAGE_MAPPING);
		RelationType<P>     rChildMasterAttribute =
			(RelationType<P>) rChildDef.getMasterAttribute();
		RelationType<C>     rChildParentAttribute =
			(RelationType<C>) rChildDef.getParentAttribute();

		if (rChildMasterAttribute == null)
		{
			throw new UnsupportedOperationException("fetchHierarchy() is only " +
													"possible for master-detail " +
													"relations");
		}

		// remove from cache to prevent inconsistencies
		removeCachedEntity(rParent);

		Class<C> rChildType   = rChildDef.getMappedType();
		Storage  rStorage     = StorageManager.getStorage(rChildType);
		List<C>  rAllChildren = new ArrayList<>();

		QueryPredicate<C> qChildren =
			forEntity(rChildType,
					  ifAttribute(rChildMasterAttribute, equalTo(rParent)));

		try (Query<C> rQuery = rStorage.query(qChildren))
		{
			QueryResult<C> rResult = rQuery.execute();

			while (rResult.hasNext())
			{
				rAllChildren.add(rResult.next());
			}
		}
		finally
		{
			rStorage.release();
		}

		List<C> rChildren =
			findDirectChildren(rParent,
							   rAllChildren,
							   rChildMasterAttribute,
							   rChildAttribute);

		for (C rChild : rChildren)
		{
			assignChildren(rChild,
						   rAllChildren,
						   rChildParentAttribute,
						   rChildAttribute);
		}

		cacheEntity(rParent);
	}

	/***************************************
	 * Searches for the first entity that matches a certain predicate in a
	 * hierarchy of entities by descending the entity tree at a certain child
	 * attribute.
	 *
	 * @param  rEntities       The list of root entities to start searching at
	 * @param  rChildAttribute The child attribute to descend the hierarchy at
	 * @param  rPredicate      The predicate to evaluate the entities with
	 *
	 * @return The first matching entity or NULL if none could be found
	 */
	public static <E extends Entity> E findDownwards(
		List<E>				  rEntities,
		RelationType<List<E>> rChildAttribute,
		Predicate<? super E>  rPredicate)
	{
		E rResult = CollectionUtil.find(rEntities, rPredicate);

		if (rResult == null)
		{
			int nCount = rEntities.size();

			for (int i = 0; i < nCount && rResult == null; i++)
			{
				List<E> rChildren = rEntities.get(i).get(rChildAttribute);

				if (rChildren != null)
				{
					rResult =
						findDownwards(rChildren, rChildAttribute, rPredicate);
				}
			}
		}

		return rResult;
	}

	/***************************************
	 * Returns the current capacities of the three levels of the entity cache.
	 *
	 * @return A three-element integer array containing the capacities of the
	 *         cache levels
	 */
	public static int[] getCacheCapacity()
	{
		return aEntityCache.getCapacity();
	}

	/***************************************
	 * Returns the cached entity for a particular global entity ID.
	 *
	 * @param  sGlobalEntityId The global entity ID
	 *
	 * @return The corresponding entity or NULL for none
	 */
	public static Entity getCachedEntity(String sGlobalEntityId)
	{
		return aEntityCache.get(sGlobalEntityId);
	}

	/***************************************
	 * Returns an entity with a certain class and ID from the global entity
	 * cache. If no entity with the given ID exists in the cache NULL will be
	 * returned.
	 *
	 * @param  rEntityClass The entity type to lookup in the cache
	 * @param  nEntityId    The ID of the entity to lookup in the cache
	 *
	 * @return The cached entity or NULL for none
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E getCachedEntity(
		Class<E> rEntityClass,
		int		 nEntityId)
	{
		EntityCache<?> rCache  = aEntityCacheMap.get(rEntityClass);
		Entity		   rResult = null;

		if (rCache != null)
		{
			rResult = rCache.getEntity(nEntityId);
		}
		else
		{
			String sId = getGlobalEntityId(rEntityClass, nEntityId);

			rResult = getCachedEntity(sId);
		}

		return (E) rResult;
	}

	/***************************************
	 * Returns a description of the current entity cache usage.
	 *
	 * @return The cache usage description
	 */
	public static String getCacheUsage()
	{
		return aEntityCache.getUsage();
	}

	/***************************************
	 * Returns the distinct values of an entity attribute for a certain entity
	 * query.
	 *
	 * @param  rAttribute The entity attribute
	 * @param  qEntities  The query predicate for the entities
	 *
	 * @return A new collection containing the distinct entity attribute values
	 *
	 * @throws StorageException If performing the query fails
	 */
	@SuppressWarnings("unchecked")
	public static <T, E extends Entity> Collection<T> getDistinct(
		RelationType<T>   rAttribute,
		QueryPredicate<E> qEntities) throws StorageException
	{
		Class<E>	  rQueryType	  = qEntities.getQueryType();
		Storage		  rStorage		  = StorageManager.getStorage(rQueryType);
		Collection<T> aDistinctValues = null;

		try (Query<E> rQuery = rStorage.query(qEntities))
		{
			aDistinctValues = (Collection<T>) rQuery.getDistinct(rAttribute);
		}
		finally
		{
			rStorage.release();
		}

		return aDistinctValues;
	}

	/***************************************
	 * Returns the number of entities that correspond to certain query criteria.
	 *
	 * @param  rEntityClass The entity type to query
	 * @param  rCriteria    The criteria to search the entities by
	 *
	 * @return The number of entities matching the given criteria
	 *
	 * @throws StorageException If accessing the storage fails
	 */
	public static <E extends Entity> int getEntityCount(
		Class<E>			 rEntityClass,
		Predicate<? super E> rCriteria) throws StorageException
	{
		Storage rStorage = StorageManager.getStorage(rEntityClass);
		int     nCount;

		try
		{
			Query<E> rQuery =
				rStorage.query(forEntity(rEntityClass, rCriteria));

			nCount = rQuery.size();

			rQuery.close();
		}
		finally
		{
			rStorage.release();
		}

		return nCount;
	}

	/***************************************
	 * Returns the entity definition for a certain entity type.
	 *
	 * @param  rEntityClass The class to return the entity definition for
	 *
	 * @return The entity definition for the given type or NULL if none exists
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> EntityDefinition<T> getEntityDefinition(
		Class<T> rEntityClass)
	{
		return (EntityDefinition<T>) StorageManager.getMapping(rEntityClass);
	}

	/***************************************
	 * Returns a global string identifier for a certain entity.
	 *
	 * @param  rEntity The entity to return the global ID for
	 *
	 * @return The global entity ID
	 *
	 * @throws IllegalArgumentException If no valid ID can be created for the
	 *                                  given entity
	 */
	public static String getGlobalEntityId(Entity rEntity)
	{
		int nEntityId = rEntity.getId();

		if (nEntityId <= 0)
		{
			throw new IllegalArgumentException("Entity ID not defined: " +
											   rEntity);
		}

		return getGlobalEntityId(rEntity.getClass(), nEntityId);
	}

	/***************************************
	 * Returns a global string identifier for the combination of a certain
	 * entity definition and numeric ID.
	 *
	 * @param  rEntityClass The entity class
	 * @param  nEntityId    The entity ID
	 *
	 * @return The global entity ID for the given parameters
	 */
	public static String getGlobalEntityId(
		Class<? extends Entity> rEntityClass,
		int						nEntityId)
	{
		return getEntityDefinition(rEntityClass).getIdPrefix() +
			   GLOBAL_ID_PREFIX_SEPARATOR + nEntityId;
	}

	/***************************************
	 * Returns the modification lock rules that have been registered through
	 * {@link #setEntityModificationLock(String, Predicate)}.
	 *
	 * @return A mapping from context IDs to rule predicates
	 */
	public static Map<String, Predicate<? super Entity>> getModificationLockRules()
	{
		return aModificationLockRules;
	}

	/***************************************
	 * Returns a mapping from global entity IDs to the corresponding entities
	 * for all entities that are currently marked as being modified by the
	 * method {@link #beginEntityModification(Entity)}.
	 *
	 * @return The mapping of the modified entities
	 */
	public static final Map<String, Entity> getModifiedEntities()
	{
		return aModifiedEntities;
	}

	/***************************************
	 * Returns the parent hierarchy of a certain entity. The returned list will
	 * contain the parent entities of the given entity starting with the topmost
	 * parent. The list will always at least contain the argument entity.
	 *
	 * @param  rEntity The entity to return the parent hierarchy of
	 *
	 * @return The list of parent entities with the argument entity as the last
	 *         element
	 */
	public static <E extends Entity> List<E> getParentHierarchy(E rEntity)
	{
		@SuppressWarnings("unchecked")
		RelationType<E> rParentAttribute =
			(RelationType<E>) rEntity.getDefinition().getParentAttribute();

		List<E> aHierarchy = new ArrayList<E>();

		aHierarchy.add(rEntity);

		if (rParentAttribute != null)
		{
			while ((rEntity = rEntity.get(rParentAttribute)) != null)
			{
				aHierarchy.add(0, rEntity);
			}
		}

		return aHierarchy;
	}

	/***************************************
	 * Returns the registered entity types.
	 *
	 * @return The registered entity types
	 */
	public static Collection<Class<? extends Entity>> getRegisteredEntityTypes()
	{
		return Collections.unmodifiableCollection(aIdPrefixRegistry.values());
	}

	/***************************************
	 * Varargs version of the {@link #init(Collection)} method.
	 *
	 * @see #init(Collection)
	 */
	@SafeVarargs
	public static void init(Class<? extends Entity>... rEntityClasses)
	{
		init(Arrays.asList(rEntityClasses));
	}

	/***************************************
	 * Global entity initialization, must be invoked before any entity
	 * operations are performed. The arguments of this method must be the entity
	 * classes used by the application. From these the entity definitions will
	 * be initialized so that entity lookups by global IDs are possible.
	 *
	 * @param rEntityClasses The entity classes used by the application (can be
	 *                       NULL for none)
	 */
	public static void init(Collection<Class<? extends Entity>> rEntityClasses)
	{
		EntityRelationTypes.init();
		StorageManager.registerMappingFactory(Entity.class,
											  new EntityDefinitionFactory());

		if (rEntityClasses != null)
		{
			for (Class<? extends Entity> rClass : rEntityClasses)
			{
				getEntityDefinition(rClass);
			}
		}

		getEntityDefinition(ExtraAttribute.class);

		// always disable caching for extra attributes
		disableCaching(ExtraAttribute.class);
	}

	/***************************************
	 * Invalidates the global entity cache.
	 */
	public static void invalidateCache()
	{
		aEntityCache.clear();
	}

	/***************************************
	 * Returns TRUE if the automatic history logging of entity changes is
	 * enabled. The default value is TRUE.
	 *
	 * @return The automatic change logging state
	 */
	public static final boolean isAutomaticChangeLogging()
	{
		return bAutomaticChangeLogging;
	}

	/***************************************
	 * Returns the caching enabled for.
	 *
	 * @param  rEntityClass The caching enabled for
	 *
	 * @return The caching enabled for
	 */
	public static boolean isCachingEnabledFor(
		Class<? extends Entity> rEntityClass)
	{
		return aEntityCacheMap.get(rEntityClass) != NO_CACHE;
	}

	/***************************************
	 * Checks whether the deletion of entities is enabled for a certain entity
	 * type.
	 *
	 * @param  rEntityClass The entity type to check
	 *
	 * @return TRUE if the deletion of entities with the given type is allowed
	 *
	 * @see    #enableDeletionOf(Class)
	 * @see    #disableDeletionOf(Class)
	 * @see    #delete(Entity, boolean)
	 */
	public static boolean isDeletionEnabledFor(
		Class<? extends Entity> rEntityClass)
	{
		return aDeleteEnabledEntities.contains(rEntityClass);
	}

	/***************************************
	 * Queries a list of entities that are identified by certain search
	 * criteria.
	 *
	 * @param  qEntities rEntityClass The entity type to query
	 * @param  nMax      The maximum number of entities to read
	 *
	 * @return A list of entities for the given criteria (may be empty but will
	 *         never be NULL)
	 *
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity> List<E> queryEntities(
		QueryPredicate<E> qEntities,
		final int		  nMax) throws StorageException
	{
		List<E> aEntities = new ArrayList<E>();

		evaluateEntities(qEntities,
						 Predicates.countDown(nMax),
						 CollectionFunctions.collectInto(aEntities));

		return aEntities;
	}

	/***************************************
	 * Queries a list of entities that are identified by certain search
	 * criteria.
	 *
	 * @param  rEntityClass The entity type to query
	 * @param  pCriteria    The criteria to search the entities by or NULL for
	 *                      none
	 * @param  nMax         The maximum number of entities to read
	 *
	 * @return A list of entities for the given criteria (may be empty but will
	 *         never be NULL)
	 *
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity> List<E> queryEntities(
		Class<E>			 rEntityClass,
		Predicate<? super E> pCriteria,
		int					 nMax) throws StorageException
	{
		return queryEntities(forEntity(rEntityClass, pCriteria), nMax);
	}

	/***************************************
	 * Queries a list of entities that are identified by certain attribute
	 * predicates. The attribute predicates are defined in a map that associates
	 * the attributes with the corresponding predicates. This map is not
	 * type-safe and the caller must ensure that the content of the predicate
	 * mapping is valid.
	 *
	 * <p>Uses the method {@link #queryEntities(Class, Predicate, int)} to
	 * perform the actual query.</p>
	 *
	 * @param  rEntityClass The entity type to query
	 * @param  rCriteraMap  The mapping from attribute relation types to
	 *                      attribute predicates
	 * @param  nMax         The maximum number of entities to read
	 * @param  bAnd         TRUE for an AND concatenation of the attribute
	 *                      predicates, FALSE for OR
	 *
	 * @return A list of entities for the given criteria (may be empty but will
	 *         never be NULL)
	 *
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity> List<E> queryEntities(
		Class<E>						   rEntityClass,
		Map<RelationType<?>, Predicate<?>> rCriteraMap,
		int								   nMax,
		boolean							   bAnd) throws StorageException
	{
		return queryEntities(rEntityClass,
							 createQueryPredicate(rCriteraMap, bAnd),
							 nMax);
	}

	/***************************************
	 * Queries a list of entities that are identified by a certain attribute
	 * value. See the {@link #queryEntities(EntityDefinition, Predicate, int)}
	 * method for more information.
	 *
	 * @param  rEntityClass The entity type to query
	 * @param  rAttribute   The attribute to search for
	 * @param  rValue       The attribute value to search for
	 * @param  nMax         The maximum number of entities to read
	 *
	 * @return A list of entities for the given criteria (may be empty but will
	 *         never be NULL)
	 *
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity, T> List<E> queryEntities(
		Class<E>		rEntityClass,
		RelationType<T> rAttribute,
		T				rValue,
		int				nMax) throws StorageException
	{
		return queryEntities(rEntityClass,
							 ifAttribute(rAttribute, equalTo(rValue)),
							 nMax);
	}

	/***************************************
	 * Queries a list of entities that are associated with certain extra
	 * attributes. When creating the extra attribute predicate an application
	 * must consider that extra attributes can be owner-specific. If only global
	 * extra attributes shall be queried the predicate should include the
	 * predicate {@link ExtraAttribute#HAS_NO_OWNER}.
	 *
	 * @param  pExtraAttributes The criteria to search the extra attributes by
	 * @param  nMax             The maximum number of entities to read
	 *
	 * @return A collection of the distinct entities that have matching extra
	 *         attributes (may be empty but will never be NULL)
	 *
	 * @throws StorageException If the storage access fails
	 */
	public static Collection<Entity> queryEntitiesByExtraAttribute(
		Predicate<? super ExtraAttribute> pExtraAttributes,
		int								  nMax) throws StorageException
	{
		List<ExtraAttribute> aExtraAttributes =
			queryEntities(ExtraAttribute.class, pExtraAttributes, nMax);

		Set<Entity> aResult = new HashSet<Entity>(aExtraAttributes.size());

		for (ExtraAttribute rExtraAttribute : aExtraAttributes)
		{
			aResult.add(rExtraAttribute.get(ExtraAttribute.ENTITY));
		}

		return aResult;
	}

	/***************************************
	 * Queries a list of entities that have a particular extra attribute with
	 * the given value.
	 *
	 * @param  rExtraAttributeKey   The extra attribute key to search for
	 * @param  rExtraAttributeValue The value of the extra attribute with the
	 *                              given key
	 * @param  nMax                 The maximum number of entities to read
	 *
	 * @return A collection of the distinct entities that have a matching extra
	 *         attribute (may be empty but will never be NULL)
	 *
	 * @throws StorageException If the storage access fails
	 */
	public static <T> Collection<Entity> queryEntitiesByExtraAttribute(
		RelationType<T> rExtraAttributeKey,
		T				rExtraAttributeValue,
		int				nMax) throws StorageException
	{
		return queryEntitiesByExtraAttribute(null,
											 rExtraAttributeKey,
											 rExtraAttributeValue,
											 nMax);
	}

	/***************************************
	 * Queries a list of entities with a certain type that have a a global extra
	 * attribute.The attributes value is not important.
	 *
	 * @param  rEntityClass       The class of the entity to query
	 * @param  rExtraAttributeKey The extra attribute key to search for
	 * @param  nMax               The maximum number of entities to read
	 *
	 * @return A collection of the distinct entities that have a matching extra
	 *         attribute (may be empty but will never be NULL)
	 *
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity, T> Collection<E> queryEntitiesByExtraAttribute(
		Class<E>		rEntityClass,
		RelationType<T> rExtraAttributeKey,
		int				nMax) throws StorageException
	{
		Predicate<Relatable> pExtraAttr =
			ExtraAttribute.HAS_NO_OWNER.and(ExtraAttribute.KEY.is(equalTo(rExtraAttributeKey)));

		pExtraAttr = addEntityPrefixPredicate(rEntityClass, pExtraAttr);

		@SuppressWarnings("unchecked")
		Collection<E> aEntities =
			(Collection<E>) queryEntitiesByExtraAttribute(pExtraAttr, nMax);

		return aEntities;
	}

	/***************************************
	 * Queries a list of entities with a certain type that have a a global extra
	 * attribute with the given key and value.
	 *
	 * @param  rEntityClass         The class of the entity to query
	 * @param  rExtraAttributeKey   The extra attribute key to search for
	 * @param  rExtraAttributeValue The value of the extra attribute with the
	 *                              given key
	 * @param  nMax                 The maximum number of entities to read
	 *
	 * @return A collection of the distinct entities that have a matching extra
	 *         attribute (may be empty but will never be NULL)
	 *
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity, T> Collection<E> queryEntitiesByExtraAttribute(
		Class<E>		rEntityClass,
		RelationType<T> rExtraAttributeKey,
		T				rExtraAttributeValue,
		int				nMax) throws StorageException
	{
		String sValue = Conversions.asString(rExtraAttributeValue);

		Predicate<Relatable> pExtraAttr =
			ExtraAttribute.HAS_NO_OWNER.and(ExtraAttribute.KEY.is(equalTo(rExtraAttributeKey)))
									   .and(ExtraAttribute.VALUE.is(equalTo(sValue)));

		pExtraAttr = addEntityPrefixPredicate(rEntityClass, pExtraAttr);

		@SuppressWarnings("unchecked")
		Collection<E> aEntities =
			(Collection<E>) queryEntitiesByExtraAttribute(pExtraAttr, nMax);

		return aEntities;
	}

	/***************************************
	 * Queries the entity instance that is identified by a certain global entity
	 * ID from the storage associated with the corresponding entity definition.
	 * The global ID string must be in the format that is returned by the method
	 * {@link #getGlobalEntityId(Entity)}.
	 *
	 * @param  sGlobalEntityId The global entity ID to query the entity for
	 *
	 * @return The entity for the given ID or NULL if none could be found
	 *
	 * @throws StorageException         If the storage query fails
	 * @throws IllegalArgumentException If the given entity ID is invalid
	 * @throws IllegalStateException    If no entity definition has been
	 *                                  registered for the given ID or if the
	 *                                  query yields more than one entity
	 */
	public static Entity queryEntity(String sGlobalEntityId)
		throws StorageException
	{
		Entity   rEntity;
		String[] aIdElements =
			sGlobalEntityId.split(GLOBAL_ID_PREFIX_SEPARATOR);

		if (aIdElements.length != 2 ||
			aIdElements[0].length() == 0 ||
			aIdElements[1].length() == 0)
		{
			throw new IllegalArgumentException("Invalid entity ID: " +
											   sGlobalEntityId);
		}

		String				    sIdPrefix    = aIdElements[0];
		String				    sEntityId    = aIdElements[1];
		Class<? extends Entity> rEntityClass = aIdPrefixRegistry.get(sIdPrefix);

		if (rEntityClass == null)
		{
			throw new IllegalStateException("No entity registered for ID prefix " +
											sIdPrefix);
		}

		rEntity = queryEntity(rEntityClass, Integer.parseInt(sEntityId));

		return rEntity;
	}

	/***************************************
	 * Queries an entity that is identified by a certain entity ID. This method
	 * will first check the global entity cache for an existing entity with the
	 * given ID. If no entity can be found in the cache it will be queried from
	 * the storage associated with the given entity definition and then put into
	 * the cache for later queries.
	 *
	 * @param  rEntityClass The entity type to query
	 * @param  nEntityId    The global entity ID to query the entity for
	 *
	 * @return The entity for the given ID or NULL if none could be found
	 *
	 * @throws StorageException      If the storage access fails
	 * @throws IllegalStateException If the query yields more than one entity
	 */
	@SuppressWarnings("boxing")
	public static <E extends Entity> E queryEntity(
		Class<E> rEntityClass,
		int		 nEntityId) throws StorageException
	{
		E rEntity = getCachedEntity(rEntityClass, nEntityId);

		if (rEntity == null)
		{
			rEntity =
				queryEntity(rEntityClass,
							getEntityDefinition(rEntityClass).getIdAttribute(),
							nEntityId,
							true);
		}

		return rEntity;
	}

	/***************************************
	 * Queries an entity that is identified by certain search criteria. The
	 * boolean parameters controls whether additional entities that are found
	 * for the given criteria will be ignored or will cause an error. If the
	 * parameter is TRUE and the storage query yields more than one entity an
	 * {@link IllegalStateException} will be thrown.
	 *
	 * @param  rEntityClass    The entity type to query
	 * @param  pCriteria       The criteria to search by
	 * @param  bFailOnMultiple If TRUE the call fails if multiple entities are
	 *                         found for the given criteria
	 *
	 * @return The entity for the given criteria or NULL if none could be found
	 *
	 * @throws StorageException      If the storage access fails
	 * @throws IllegalStateException If the boolean parameter is TRUE and the
	 *                               query yields more than one entity
	 */
	public static <E extends Entity> E queryEntity(
		Class<E>			 rEntityClass,
		Predicate<? super E> pCriteria,
		boolean				 bFailOnMultiple) throws StorageException
	{
		int     nCount    = bFailOnMultiple ? 2 : 1;
		List<E> rEntities = queryEntities(rEntityClass, pCriteria, nCount);
		int     nSize     = rEntities.size();

		if (nSize > 1)
		{
			throw new IllegalStateException("Multiple entities for " +
											pCriteria);
		}

		E rEntity = nSize > 0 ? rEntities.get(0) : null;

		return rEntity;
	}

	/***************************************
	 * Queries an entity that is identified by a certain attribute value. See
	 * the method {@link #queryEntity(EntityDefinition, Predicate, boolean)} for
	 * more information.
	 *
	 * @param  rEntityClass    The entity type to query
	 * @param  rAttribute      The attribute to search for
	 * @param  rValue          The attribute value to search for
	 * @param  bFailOnMultiple If TRUE the call fails if multiple entities are
	 *                         found for the given criteria
	 *
	 * @return The entity for the given attribute value or NULL if none could be
	 *         found
	 *
	 * @throws StorageException      If the storage access fails
	 * @throws IllegalStateException If the boolean parameter is TRUE and the
	 *                               query yields more than one entity
	 */
	public static <T, E extends Entity> E queryEntity(
		Class<E>		rEntityClass,
		RelationType<T> rAttribute,
		T				rValue,
		boolean			bFailOnMultiple) throws StorageException
	{
		return queryEntity(rEntityClass,
						   ifAttribute(rAttribute, equalTo(rValue)),
						   bFailOnMultiple);
	}

	/***************************************
	 * Queries an entity that is identified by certain attribute criteria. Uses
	 * the method {@link #queryEntity(EntityDefinition, Predicate, boolean)} to
	 * perform the actual query.
	 *
	 * @param  rEntityClass    The entity type to query
	 * @param  rCriteraMap     The mapping from attribute relation types to
	 *                         attribute predicates
	 * @param  bAnd            TRUE for an AND catenation of the attribute
	 *                         predicates, FALSE for OR
	 * @param  bFailOnMultiple If TRUE the call fails if multiple entities are
	 *                         found for the given criteria
	 *
	 * @return The entity for the given attribute value or NULL if none could be
	 *         found
	 *
	 * @throws StorageException      If the storage access fails
	 * @throws IllegalStateException If the fail on multiple parameter is TRUE
	 *                               and the query yields more than one entity
	 */
	public static <T, E extends Entity> E queryEntity(
		Class<E>						   rEntityClass,
		Map<RelationType<?>, Predicate<?>> rCriteraMap,
		boolean							   bAnd,
		boolean							   bFailOnMultiple)
		throws StorageException
	{
		return queryEntity(rEntityClass,
						   createQueryPredicate(rCriteraMap, bAnd),
						   bFailOnMultiple);
	}

	/***************************************
	 * Queries an entity that has an extra attribute with a particular value.
	 *
	 * @see #queryEntityByExtraAttribute(Class, RelationType, Object, boolean)
	 */
	public static <T> Entity queryEntityByExtraAttribute(
		RelationType<T> rExtraAttributeKey,
		T				rExtraAttributeValue,
		boolean			bFailOnMultiple) throws StorageException
	{
		return queryEntityByExtraAttribute(null,
										   rExtraAttributeKey,
										   rExtraAttributeValue,
										   bFailOnMultiple);
	}

	/***************************************
	 * Queries an entity of a certain type that has an extra attribute with a
	 * particular value. This is a convenience method that returns the first
	 * entity that is returned by the method {@link
	 * #queryEntitiesByExtraAttribute(ExtraAttributeType, Object, int)} or NULL
	 * if no matching entity could be found. The boolean parameter indicates
	 * whether the existence of multiple entities should be considered as an
	 * inconsistency and therefore result in an exception being thrown. This
	 * parameter should in most cases be set to TRUE.
	 *
	 * @param  rEntityClass         The class of the entity to query
	 * @param  rExtraAttributeKey   The extra attribute key to search for
	 * @param  rExtraAttributeValue The value of the extra attribute with the
	 *                              given key
	 * @param  bFailOnMultiple      If TRUE the call fails if multiple entities
	 *                              are found for the given criteria
	 *
	 * @return The entity that has the given extra attribute or NULL for none
	 *
	 * @throws StorageException      If the storage access fails
	 * @throws IllegalStateException If the boolean parameter is TRUE and the
	 *                               query yields more than one entity
	 */
	public static <E extends Entity, T> E queryEntityByExtraAttribute(
		Class<E>		rEntityClass,
		RelationType<T> rExtraAttributeKey,
		T				rExtraAttributeValue,
		boolean			bFailOnMultiple) throws StorageException
	{
		Collection<E> rEntities =
			queryEntitiesByExtraAttribute(rEntityClass,
										  rExtraAttributeKey,
										  rExtraAttributeValue,
										  bFailOnMultiple ? 2 : 1);

		int nSize = rEntities.size();

		if (bFailOnMultiple && nSize > 1)
		{
			throw new IllegalStateException(String.format("Multiple entities for " +
														  "extra attribute %s with value %s",
														  rExtraAttributeKey,
														  rExtraAttributeValue));
		}

		return nSize > 0 ? rEntities.iterator().next() : null;
	}

	/***************************************
	 * Registers an entity cache for a certain entity class.
	 *
	 * @param rEntityClass The entity class to register the cache for
	 * @param rCache       The cache instance for the given entity class
	 */
	public static <E extends Entity> void registerEntityCache(
		Class<E>	   rEntityClass,
		EntityCache<E> rCache)
	{
		aEntityCacheMap.put(rEntityClass, rCache);
	}

	/***************************************
	 * Removes an entity from the cache.
	 *
	 * @param rEntity The entity to remove
	 */
	public static void removeCachedEntity(Entity rEntity)
	{
		removeCachedEntity(getGlobalEntityId(rEntity));
	}

	/***************************************
	 * Removes an entity with a certain ID from the cache.
	 *
	 * @param sId The global ID of the entity to remove
	 */
	public static void removeCachedEntity(String sId)
	{
		Entity rRemovedEntity = aEntityCache.remove(sId);

		if (rRemovedEntity != null)
		{
			rRemovedEntity.set(CACHE_ENTITY, Boolean.FALSE);
		}
	}

	/***************************************
	 * Removes an entity cache registration for a certain entity class.
	 *
	 * @param rEntityClass The entity class to remove the cache for
	 */
	public static <E extends Entity> void removeEntityCache(
		Class<E> rEntityClass)
	{
		aEntityCacheMap.remove(rEntityClass);
	}

	/***************************************
	 * Removes an entity modification context that has been set previously with
	 * {@link #setEntityModificationContext(String, Relatable)}.
	 *
	 * @param sContextId The ID of the entity modification context
	 */
	public static void removeEntityModificationContext(String sContextId)
	{
		assert aEntityModificationContextId.get().equals(sContextId);

		aEntityModificationContext.remove();
		aEntityModificationContextId.remove();
	}

	/***************************************
	 * Removes the lock rule for a certain entity modification context. The
	 * removed rule is returned so that the caller could concatenate it into a
	 * new rule and set the combined rule again if necessary.
	 *
	 * @param  sContextId The ID of the entity modification context
	 *
	 * @return The removed rule or NULL for none
	 *
	 * @see    #setEntityModificationLock(String, Predicate)
	 */
	public static Predicate<? super Entity> removeEntityModificationLock(
		String sContextId)
	{
		return aModificationLockRules.remove(sContextId);
	}

	/***************************************
	 * Removes a store listener. See {@link #addStoreListener(StoreListener)}
	 * for details.
	 *
	 * @param rListener The listener to remove
	 */
	public static void removeStoreListener(StoreListener rListener)
	{
		getStoreListeners().remove(rListener);
	}

	/***************************************
	 * Resets an entity to it's current persistent state. This will set all
	 * attributes and references of the given entity to their persistent values
	 * and all other relations to their default values.
	 *
	 * @param  rEntity The entity to reset
	 *
	 * @throws StorageException If retrieving the persistent entity state fails
	 */
	public static void resetEntity(Entity rEntity) throws StorageException
	{
		String sId = rEntity.getGlobalId();

		aCacheLock.lock();

		try
		{
			// remove entity from cache to force a re-fetching
			removeCachedEntity(sId);

			// fetch (and cache) current state from storage
			Entity aStoredEntity = queryEntity(sId);

			// synchronize the existing entity with the persistent state to
			// remove any modifications and update existing references
			ObjectRelations.syncRelations(rEntity, aStoredEntity);
		}
		finally
		{
			aCacheLock.unlock();
		}
	}

	/***************************************
	 * Resets all updated entities in an entity modification context to their
	 * persistent state by invoking {@link #resetEntity(Entity)}.
	 *
	 * <p>This method is intended to be used internally by the framework only.
	 * </p>
	 *
	 * @param rContext The {@link Relatable} object that serves as the context
	 */
	public static void resetEntityModifications(Relatable rContext)
	{
		Set<Entity> rEntities = rContext.get(CONTEXT_UPDATED_ENTITIES);

		for (Entity rEntity : rEntities)
		{
			try
			{
				resetEntity(rEntity);
			}
			catch (Exception e)
			{
				Log.warnf(e, "Could not reset entity %s", rEntity);
			}
		}

		rEntities.clear();
	}

	/***************************************
	 * Sets the state of the automatic history logging of entity changes. If
	 * enabled storing a modified entity with {@link #storeEntity(Entity,
	 * Entity)} will create a history record that contains information about the
	 * change. The default value is TRUE and it should only be set to FALSE for
	 * special purposes like bulk insertions.
	 *
	 * @param bEnabled The new state of the automatic change history logging
	 */
	public static final void setAutomaticChangeLogging(boolean bEnabled)
	{
		bAutomaticChangeLogging = bEnabled;
	}

	/***************************************
	 * Sets the capacity of the different levels of the entity cache. A value of
	 * zero disables the respective cache level. Typically a level with a higher
	 * number should be set to a higher capacity as a lower level because the
	 * higher levels can be more easily be reclaimed by the VM. The first level
	 * is permanent, entities in that level will never be removed automatically.
	 *
	 * @param nFirstLevel  The capacity of the permanent cache level
	 * @param nSecondLevel The capacity of the softly referenced cache level
	 * @param nThirdLevel  nSecondLevel The capacity of the weakly referenced
	 *                     cache level
	 */
	public static void setCacheCapacity(int nFirstLevel,
										int nSecondLevel,
										int nThirdLevel)
	{
		aEntityCache.setCapacity(nFirstLevel, nSecondLevel, nThirdLevel);
	}

	/***************************************
	 * Sets the current entity modification context. This context will then be
	 * used to prevent concurrent modifications of entities. It must be an
	 * instance of {@link Relatable} so that relations for the modification
	 * tracking can be added to it. After the entity modifications have been
	 * performed the method {@link #removeEntityModificationContext(String)}
	 * must be invoked.
	 *
	 * <p>This method is intended to be used internally by the framework only.
	 * </p>
	 *
	 * @param sContextId A unique ID of the entity modification context
	 * @param rContext   A relatable object that serves as the context
	 */
	public static void setEntityModificationContext(
		String    sContextId,
		Relatable rContext)
	{
		assert sContextId != null && rContext != null;

		String sExistingContext = aEntityModificationContextId.get();

		if (sExistingContext != null)
		{
			String sMessage =
				String.format("Modification context already set to %s " +
							  "(tried to set to %s)",
							  sExistingContext,
							  sContextId);

			throw new ConcurrentModificationException(sMessage);
		}

		aEntityModificationContextId.set(sContextId);
		aEntityModificationContext.set(rContext);
	}

	/***************************************
	 * Defines a rule for entities that must not be modified by other
	 * modification contexts but the current one. The rule is a predicate that
	 * must evaluate to TRUE for entities that should be locked against
	 * modification by other contexts. The context ID must be the same as in the
	 * method {@link #setEntityModificationContext(String, Relatable)}.
	 *
	 * <p>All registered modification lock rules will be evaluated by the method
	 * {@link #beginEntityModification(Entity)}. If the rule evaluates to TRUE
	 * for a certain entity in another context than that which registered the
	 * rule a {@link ConcurrentEntityModificationException} will be thrown to
	 * signal that the entity is not available for modification by the
	 * respective context.</p>
	 *
	 * <p>When the context has finished processing the locked entities it must
	 * invoke {@link #removeEntityModificationLock(String)} to clear the
	 * modification rule.</p>
	 *
	 * @param sContextId the ID of the entity modification context
	 * @param pRule      The new locked entity rule
	 */
	public static void setEntityModificationLock(
		String					  sContextId,
		Predicate<? super Entity> pRule)
	{
		aModificationLockRules.put(sContextId, pRule);
	}

	/***************************************
	 * Sets the session manager to be used for user identification and
	 * context-based caching.
	 *
	 * @param rManager The new session manager
	 */
	public static void setSessionManager(SessionManager rManager)
	{
		rSessionManager = rManager;
	}

	/***************************************
	 * Performs a shutdown by freeing all allocated resources.
	 */
	public static void shutdown()
	{
		getStoreListeners().clear();
		aEntityCache.clear();
		aIdPrefixRegistry.clear();
	}

	/***************************************
	 * Stores an entity without a history group.
	 *
	 * @see #storeEntity(Entity, Entity, boolean)
	 */
	public static void storeEntity(Entity rEntity, Entity rChangeOrigin)
		throws TransactionException
	{
		storeEntity(rEntity, rChangeOrigin, false);
	}

	/***************************************
	 * Stores a single entity in the storage that is associated with the
	 * entity's class. The entity and associated history entries will be stored
	 * inside a transaction that is created with {@link TransactionManager}.
	 * Therefore, if another transaction is already active for the current
	 * thread the entity transaction will be added to it and only be committed
	 * when the surrounding transaction is.
	 *
	 * <p>If the flag {@link StorageRelationTypes#PERSISTENT} is set on the
	 * entity the modification of the entity will automatically be recorded in
	 * the history. This default behavior can be disabled by setting the
	 * corresponding flag with {@link #setAutomaticChangeLogging(boolean)} to
	 * FALSE.</p>
	 *
	 * <p>If the entity refers to a list of dependent entities in a relation of
	 * type {@link EntityRelationTypes#DEPENDENT_STORE_ENTITIES} all contained
	 * entities will also be stored by this method in the order in which they
	 * appear in the list after the argument entity and inside the same
	 * transaction.</p>
	 *
	 * @param  rEntity       The entity to store
	 * @param  rChangeOrigin The entity to be stored as the history origin
	 * @param  bHistoryGroup TRUE to record the the entity change inside a
	 *                       history group
	 *
	 * @throws TransactionException If the entity storage transaction fails
	 */
	public static void storeEntity(Entity  rEntity,
								   Entity  rChangeOrigin,
								   boolean bHistoryGroup)
		throws TransactionException
	{
		Entity		 rStoreEntity	    = rEntity;
		List<Entity> rDependentEntities = null;

		if (rEntity.hasRelation(DEPENDENT_STORE_ENTITIES))
		{
			// get dependent entities before checking the hierarchy and delete
			// the relation before storing to prevent recursions
			rDependentEntities = rEntity.get(DEPENDENT_STORE_ENTITIES);
			rEntity.deleteRelation(DEPENDENT_STORE_ENTITIES);
		}

		rEntity = rEntity.checkForHierarchyUpdate();

		boolean bNewEntity = !rEntity.isPersistent();

		TransactionManager.begin();

		try
		{
			Storage rStorage    = StorageManager.getStorage(rEntity.getClass());
			String  sChange     = null;
			boolean bHasChanges = false;

			TransactionManager.addTransactionElement(rStorage);

			if (bAutomaticChangeLogging &&
				!(rEntity instanceof ExtraAttribute) &&
				(!(rEntity instanceof HistoryRecord) ||
				 rEntity.get(HistoryRecord.TYPE) == HistoryType.NOTE))
			{
				sChange     = rEntity.createChangeDescription();
				bHasChanges = (sChange != null && sChange.length() > 0);
			}

			rEntity.set(ENTITY_STORE_ORIGIN, rChangeOrigin);

			if (bHasChanges ||
				(!bAutomaticChangeLogging && rEntity.hasFlag(MODIFIED)))
			{
				rEntity.set(LAST_CHANGE, new Date());
			}

			if (bHistoryGroup)
			{
				HistoryManager.begin(rChangeOrigin,
									 rEntity,
									 "Store " +
									 EntityFunctions.format(rEntity));
			}

			rStorage.store(rEntity);

			if (rStoreEntity != rEntity && rStoreEntity.hasFlag(MODIFIED))
			{
				// store original entity if still modified to make changes
				// persistent even if the parent hierarchy doesn't contain it
				// in the case of not cached entities
				// TODO: check if still necessary
				rStorage.store(rStoreEntity);
			}

			if (bHasChanges)
			{
				// history must be stored after the entity to prevent endless
				// recursion because of the entity reference in the record
				if (bNewEntity)
				{
					@SuppressWarnings("boxing")
					String sNewId = String.format("<NEW:%s>", rEntity.getId());

					sChange = sChange.replaceFirst("<NEW>", sNewId);
				}

				HistoryManager.record(HistoryType.CHANGE,
									  rChangeOrigin,
									  rEntity,
									  sChange);

				System.out.printf("---- CHANGELOG ----\n%s", sChange);
			}

			if (rDependentEntities != null)
			{
				for (Entity rDependentEntity : rDependentEntities)
				{
					storeEntity(rDependentEntity, rChangeOrigin, false);
				}
			}

			if (bHistoryGroup)
			{
				HistoryManager.commit(false);
			}

			TransactionManager.commit();
			cacheEntity(rEntity);

			STORE_LISTENERS.notifyListeners(EntityManager.class, rEntity);
			rEntity.deleteRelation(ENTITY_STORE_ORIGIN);
		}
		catch (Exception e)
		{
			TransactionManager.rollback();

			if (bHistoryGroup)
			{
				HistoryManager.rollback();
			}

			if (e instanceof TransactionException)
			{
				throw (TransactionException) e;
			}
			else
			{
				throw new TransactionException("Could not store entity", e);
			}
		}
	}

	/***************************************
	 * Signals the beginning of an entity modification. If the entity is cached
	 * it will be marked to allow the detection of concurrent modifications of
	 * the entity instance. To end an active entity modification the method
	 * {@link #endEntityModification(Entity)} must be invoked.
	 *
	 * <p>The tracking of entity modifications is based on the current
	 * modification context that will be set (and cleared) by other framework
	 * classes through the method {@link #setEntityModificationContext(String,
	 * Relatable)}. If no modification context is available the ID of the
	 * currently active session will be used instead if an instance of the
	 * {@link SessionManager} interface had been set during application
	 * initialization with {@link #setSessionManager(SessionManager)}.</p>
	 *
	 * <p>This method is synchronized to prevent multiple threads from
	 * concurrently invoking this method for the same entity. It is not
	 * synchronizing on the entity itself because that could cause deadlocks if
	 * multiple elements of an entity hierarchy are marked for modification at
	 * the same time.</p>
	 *
	 * @param  rEntity The entity to be modified
	 *
	 * @throws ConcurrentModificationException If the entity is already being
	 *                                         modified by another session
	 */
	static synchronized void beginEntityModification(Entity rEntity)
	{
		if (rEntity.isPersistent())
		{
			String sContextId = getEntityModificationContextId();

			if (sContextId != null)
			{
				checkModificationLockRules(rEntity, sContextId);

				String sHandle = rEntity.get(ENTITY_MODIFICATION_HANDLE);

				if (sHandle == null)
				{
					rEntity.set(ENTITY_MODIFICATION_HANDLE, sContextId);

					Relatable rContext  = aEntityModificationContext.get();
					String    sEntityId = rEntity.getGlobalId();

					if (rContext != null)
					{
						Map<String, Entity> rEntities =
							rContext.get(CONTEXT_MODIFIED_ENTITIES);

						rEntities.put(sEntityId, rEntity);
					}

					aModifiedEntities.put(sEntityId, rEntity);
				}
				else if (!sHandle.equals(sContextId))
				{
					throwConcurrentEntityModification(rEntity,
													  MSG_CONCURRENT_MODIFICATION,
													  rEntity,
													  sContextId,
													  sHandle);
				}
			}
		}
	}

	/***************************************
	 * Changes the ID of a certain entity. The entity will be reset to a
	 * non-persistent state so that it can be stored as a new entity. All
	 * children will be modified so that they reference the parent with the new
	 * ID but this state will not be persistent until the parent entity is
	 * stored. The original entity will remain unchanged and can be deleted
	 * afterwards if desired.
	 *
	 * @param  rEntity The entity to change the ID of
	 * @param  nNewId  The new entity ID
	 *
	 * @throws StorageException If accessing extra attributes fails
	 */
	@SuppressWarnings("boxing")
	static void changeEntityId(Entity rEntity, int nNewId)
		throws StorageException
	{
		// TODO: work in progress, currently not working reliably
		EntityDefinition<?> rDef = rEntity.getDefinition();

		// first access all XA and children to resolve all on-demand references
		for (ExtraAttribute rXA : rEntity.getExtraAttributeMap().values())
		{
			// mark XA as modified to store the new parent reference
			rXA.set(MODIFIED);
		}

		rEntity.set(EntityRelationTypes.EXTRA_ATTRIBUTES_MODIFIED);

		for (RelationType<List<Entity>> rChildAttr : rDef.getChildAttributes())
		{
			List<Entity> rChildren = rEntity.get(rChildAttr);

			for (Entity rChild : rChildren)
			{
				// mark children as modified to store the new parent reference
				rChild.set(MODIFIED);
			}
		}

		// finally mark entity as not persistent to store it as new instead of
		// updating an existing entity; all XA and children will then be stored
		// with the new parent reference.
		rEntity.set(PERSISTENT, false);
		rEntity.set(rDef.getIdAttribute(), nNewId);
	}

	/***************************************
	 * Signals the end of an entity modification that had been started by a call
	 * to {@link #beginEntityModification(Entity)}.
	 *
	 * @param rEntity The entity to be modified
	 */
	static synchronized void endEntityModification(Entity rEntity)
	{
		if (rEntity.hasRelation(ENTITY_MODIFICATION_HANDLE))
		{
			String sContextId = getEntityModificationContextId();

			if (sContextId != null)
			{
				Relatable rContext  = aEntityModificationContext.get();
				String    sHandle   = rEntity.get(ENTITY_MODIFICATION_HANDLE);
				String    sEntityId = rEntity.getGlobalId();

				if (!sHandle.equals(sContextId))
				{
					throwConcurrentEntityModification(rEntity,
													  MSG_CONCURRENT_MODIFICATION,
													  rEntity,
													  sContextId,
													  sHandle);
				}

				if (rContext != null)
				{
					Entity rContextEntity =
						rContext.get(CONTEXT_MODIFIED_ENTITIES)
								.remove(sEntityId);

					if (rContextEntity != null)
					{
						rContext.get(CONTEXT_UPDATED_ENTITIES)
								.add(rContextEntity);
					}
				}

				rEntity.deleteRelation(ENTITY_MODIFICATION_HANDLE);
				aModifiedEntities.remove(sEntityId);
			}
		}
	}

	/***************************************
	 * Internal method to register an entity type with the manager.
	 *
	 * @param  rEntityClass The class to register
	 * @param  rDefinition  The entity definition for the entity class
	 *
	 * @throws IllegalArgumentException If either the entity name or the ID
	 *                                  prefix of the given entity definition
	 *                                  has been registered already
	 */
	static <E extends Entity> void registerEntityType(
		Class<E>			rEntityClass,
		EntityDefinition<E> rDefinition)
	{
		String sIdPrefix = rDefinition.getIdPrefix();

		if (aIdPrefixRegistry.containsKey(sIdPrefix))
		{
			throw new IllegalArgumentException(String.format("Duplicate entity ID prefix %s; " +
															 "already defined in %s",
															 sIdPrefix,
															 aIdPrefixRegistry
															 .get(sIdPrefix)));
		}

		if (aIdPrefixRegistry.containsValue(rEntityClass))
		{
			throw new IllegalArgumentException("Duplicate entity registration: " +
											   rEntityClass);
		}

		aIdPrefixRegistry.put(sIdPrefix, rEntityClass);
	}

	/***************************************
	 * Extends the given {@link Predicate} to also match the Entity-prefix if
	 * the given entity class is not NULL.
	 *
	 * @param  rEntityClass The entity class which prefix to match
	 * @param  pExtraAttr   The {@link Predicate} to extend.
	 *
	 * @return The extended {@link Predicate}
	 */
	private static <E extends Entity> Predicate<Relatable> addEntityPrefixPredicate(
		Class<E>			 rEntityClass,
		Predicate<Relatable> pExtraAttr)
	{
		if (rEntityClass != null)
		{
			String sIdPrefix = getEntityDefinition(rEntityClass).getIdPrefix();

			pExtraAttr =
				pExtraAttr.and(ExtraAttribute.ENTITY.is(like(sIdPrefix + "%")));
		}

		return pExtraAttr;
	}

	/***************************************
	 * Recursively assigns the child entities in a list to their parent
	 * entities.
	 *
	 * @param rParent          The parent entity
	 * @param rAllChildren     The list of all (remaining) children
	 * @param rParentAttribute The parent attribute of the children
	 * @param rChildAttribute  The child attribute of the parent
	 */
	private static <C extends Entity> void assignChildren(
		C					  rParent,
		List<C>				  rAllChildren,
		RelationType<C>		  rParentAttribute,
		RelationType<List<C>> rChildAttribute)
	{
		List<C> rChildren =
			findDirectChildren(rParent,
							   rAllChildren,
							   rParentAttribute,
							   rChildAttribute);

		for (C rChild : rChildren)
		{
			assignChildren(rChild,
						   rAllChildren,
						   rParentAttribute,
						   rChildAttribute);
		}
	}

	/***************************************
	 * Checks whether an entity is already cached and either replaces it with
	 * the cached version or places it into the entity cache.
	 *
	 * @param  rEntity The entity to check
	 *
	 * @return Either an already cached instance of the entity or the now cached
	 *         entity
	 */
	private static <E extends Entity> E checkCaching(E rEntity)
	{
		aCacheLock.lock();

		try
		{
			@SuppressWarnings("unchecked")
			E rCachedEntity =
				(E) getCachedEntity(rEntity.getClass(), rEntity.getId());

			if (rCachedEntity != null)
			{
				// use cache entity if available to preserve already
				// loaded entity hierarchies
				rEntity = rCachedEntity;
			}
			else
			{
				cacheEntity(rEntity);
			}
		}
		finally
		{
			aCacheLock.unlock();
		}

		return rEntity;
	}

	/***************************************
	 * Checks the modification lock rules that have been registered through the
	 * method {@link #setEntityModificationLock(String, Predicate)}.
	 *
	 * @param rEntity    The entity that is about to be modified
	 * @param sContextId The ID of the modification context that tries to modify
	 *                   the entity
	 */
	@SuppressWarnings("boxing")
	private static void checkModificationLockRules(
		Entity rEntity,
		String sContextId)
	{
		for (Entry<String, Predicate<? super Entity>> rLockRule :
			 aModificationLockRules.entrySet())
		{
			String sLockContextID = rLockRule.getKey();

			if (!sContextId.equals(sLockContextID) &&
				rLockRule.getValue().evaluate(rEntity))
			{
				throwConcurrentEntityModification(rEntity,
												  MSG_ENTITY_MODFICATION_LOCKED,
												  rEntity,
												  sContextId,
												  sLockContextID);
			}
		}
	}

	/***************************************
	 * Creates the entity cache.
	 *
	 * @return The entity cache instance
	 */
	@SuppressWarnings("boxing")
	private static MultiLevelCache<String, Entity> createEntityCache()
	{
		int nFirstLevel  = 50;
		int nSecondLevel = 500;
		int nThirdLevel  = 5000;

		String sCacheSizes = System.getProperty("entity_cache_sizes", null);

		if (sCacheSizes != null)
		{
			try
			{
				String[] aSizes = sCacheSizes.split("-");

				nFirstLevel  = Integer.parseInt(aSizes[0]);
				nSecondLevel = Integer.parseInt(aSizes[1]);
				nThirdLevel  = Integer.parseInt(aSizes[2]);

				Log.infof("Entity cache levels: %d, %d, %d",
						  nFirstLevel,
						  nSecondLevel,
						  nThirdLevel);
			}
			catch (Exception e)
			{
				Log.warn("Invalid entity cache size definition: " +
						 sCacheSizes);
			}
		}

		return new MultiLevelCache<String, Entity>(nFirstLevel,
												   nSecondLevel,
												   nThirdLevel);
	}

	/***************************************
	 * Creates a query predicate from a mapping of attribute relation types to
	 * attribute predicates.
	 *
	 * @param  rCriteraMap The mapping from attribute relation types to
	 *                     attribute predicates
	 * @param  bAnd        TRUE for an AND concatenation of the attribute
	 *                     predicates, FALSE for OR
	 *
	 * @return The resulting query predicate
	 */
	@SuppressWarnings("unchecked")
	private static <E extends Entity> Predicate<E> createQueryPredicate(
		Map<RelationType<?>, Predicate<?>> rCriteraMap,
		boolean							   bAnd)
	{
		Predicate<E> pCriteria = null;

		for (Entry<RelationType<?>, Predicate<?>> rEntry :
			 rCriteraMap.entrySet())
		{
			RelationType<Object> rAttribute =
				(RelationType<Object>) rEntry.getKey();

			Predicate<Object> pCriterion =
				(Predicate<Object>) rEntry.getValue();

			if (bAnd)
			{
				pCriteria =
					Predicates.and(pCriteria,
								   ifAttribute(rAttribute, pCriterion));
			}
			else
			{
				pCriteria =
					Predicates.or(pCriteria,
								  ifAttribute(rAttribute, pCriterion));
			}
		}

		return pCriteria;
	}

	/***************************************
	 * Deletes the children of the given {@link Entity} if any from the
	 * database. Ten entity itself is not deleted.
	 *
	 * @param  rEntity  The {@link Entity}
	 * @param  rStorage The {@link Storage} from which to delete the entity. The
	 *                  storage is expected to be initialized and attached to a
	 *                  transaction.
	 *
	 * @throws StorageException if deleting the children fails
	 */
	private static void deleteChildren(Entity rEntity, Storage rStorage)
		throws StorageException
	{
		Collection<RelationType<List<Entity>>> rChildAttributes =
			rEntity.getDefinition().getChildAttributes();

		for (RelationType<List<Entity>> rChildAttribute : rChildAttributes)
		{
			List<Entity> rChildEntities = rEntity.get(rChildAttribute);

			for (Entity rChildEntity : rChildEntities)
			{
				deleteChildren(rChildEntity, rStorage);
				rStorage.delete(rChildEntity);
			}
		}
	}

	/***************************************
	 * Searches a list of child entities for the direct children of a certain
	 * parent and sets them as the children of the parent and removes the found
	 * entities from the input list.
	 *
	 * @param  rParent          The parent entity
	 * @param  rAllChildren     The list of all children
	 * @param  rParentAttribute The parent attribute of the children
	 * @param  rChildAttribute  The child attribute of the parent
	 *
	 * @return The list of children found
	 */
	@SuppressWarnings({ "unchecked", "boxing" })
	private static <P extends Entity, C extends Entity> List<C> findDirectChildren(
		P					  rParent,
		List<C>				  rAllChildren,
		RelationType<P>		  rParentAttribute,
		RelationType<List<C>> rChildAttribute)
	{
		EntityDefinition<P> rParentDef =
			(EntityDefinition<P>) rParent.getDefinition();
		EntityDefinition<C> rChildDef  =
			(EntityDefinition<C>) rChildAttribute.get(STORAGE_MAPPING);

		Predicate<Relatable> pChildren;

		if (rParentDef == rChildDef || rChildDef.getMasterAttribute() == null)
		{
			pChildren = ifRelation(PARENT_ENTITY_ID, equalTo(rParent.getId()));
		}
		else
		{
			pChildren =
				ifRelation(MASTER_ENTITY_ID, equalTo(rParent.getId())).and(ifRelation(PARENT_ENTITY_ID,
																					  isNull()));
		}

		List<C> rChildren = CollectionUtil.collect(rAllChildren, pChildren);

		for (C rChild : rChildren)
		{
			// prevent setting of modified flag
			rChild.set(MetaTypes.INITIALIZING);

			// parent attribute must be NULL to prevent exception in initChildren()
			rChild.set(rParentAttribute, null);
		}

		rParent.set(rChildAttribute, rChildren);
		rParentDef.initChildren(rParent, rChildren, rChildDef, true);
		rAllChildren.removeAll(rChildren);

		return rChildren;
	}

	/***************************************
	 * Internal method to determine the most specific entity modification
	 * context ID that is currently available.
	 *
	 * @return The context ID
	 */
	private static String getEntityModificationContextId()
	{
		String sContext = aEntityModificationContextId.get();

		if (sContext == null && rSessionManager != null)
		{
			sContext = rSessionManager.getSessionId();
		}

		if (sContext == null)
		{
			sContext = Thread.currentThread().getName();
		}

		return sContext;
	}

	/***************************************
	 * Returns the entity store listeners.
	 *
	 * @return The list of store listeners
	 */
	private static List<StoreListener> getStoreListeners()
	{
		return ObjectRelations.getRelatable(EntityManager.class)
							  .get(STORE_LISTENERS);
	}

	/***************************************
	 * Throws a {@link ConcurrentEntityModificationException} initialized with
	 * the given parameters.
	 *
	 * @param  rEntity        The entity for which to throw the exception
	 * @param  sMessageFormat The message format string
	 * @param  rMessageArgs   The message format arguments
	 *
	 * @throws ConcurrentEntityModificationException
	 */
	private static void throwConcurrentEntityModification(
		Entity    rEntity,
		String    sMessageFormat,
		Object... rMessageArgs) throws ConcurrentEntityModificationException
	{
		sMessageFormat = String.format(sMessageFormat, rMessageArgs);

		throw new ConcurrentEntityModificationException(rEntity,
														sMessageFormat);
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * An interface for event listeners that want to be notified if an entity
	 * has been stored.
	 *
	 * @author eso
	 */
	public static interface StoreListener
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Will be invoked after an entity has been stored through the entity
		 * manager.
		 *
		 * @param rEntity The entity that has been stored
		 */
		public void entityStored(Entity rEntity);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An entity-specific implementation of a storage mapping factory.
	 *
	 * @author eso
	 */
	private static class EntityDefinitionFactory
		implements MappingFactory<Entity>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Implemented to create a new instance of {@link EntityDefinition}. It
		 * first tries to create an instance of an inner class named
		 * 'Definition' (i.e. of the class rType.getName() + "$Definition"). If
		 * it exists it must have a no-argument constructor. If not a standard
		 * entity definition will be created and returned.
		 *
		 * @see MappingFactory#createMapping(Class)
		 */
		@Override
		public StorageMapping<Entity, ?, ?> createMapping(Class<Entity> rType)
		{
			EntityDefinition<Entity> aResult;

			try
			{
				String sDefinitionClass = rType.getName() +
										  "$Definition";

				@SuppressWarnings("unchecked")
				Class<EntityDefinition<Entity>> rTypeDefinitionClass =
					(Class<EntityDefinition<Entity>>) Class.forName(sDefinitionClass);

				aResult = ReflectUtil.newInstance(rTypeDefinitionClass);
			}
			catch (ClassNotFoundException e)
			{
				aResult = new EntityDefinition<Entity>(rType, null);
			}

			return aResult;
		}
	}
}
