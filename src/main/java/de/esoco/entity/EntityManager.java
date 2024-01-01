//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.history.HistoryRecord.HistoryType;
import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.comm.Endpoint;
import de.esoco.lib.comm.EndpointFunction;
import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.logging.Log;
import de.esoco.lib.manage.MultiLevelCache;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.manage.TransactionManager;
import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.lib.service.ModificationSyncEndpoint.SyncData;
import de.esoco.storage.Query;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.QueryResult;
import de.esoco.storage.Storage;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;
import de.esoco.storage.StorageManager.MappingFactory;
import de.esoco.storage.StorageMapping;
import de.esoco.storage.StorageRelationTypes;
import de.esoco.storage.StorageRuntimeException;
import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.ListenerType;
import org.obrel.type.ListenerTypes;
import org.obrel.type.MetaTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
import static de.esoco.entity.EntityRelationTypes.NO_ENTITY_LOCKING;
import static de.esoco.entity.EntityRelationTypes.PARENT_ENTITY_ID;
import static de.esoco.entity.EntityRelationTypes.SKIP_NEXT_CHANGE_LOGGING;
import static de.esoco.lib.comm.CommunicationRelationTypes.ENDPOINT_ADDRESS;
import static de.esoco.lib.expression.CollectionPredicates.elementOf;
import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.ifRelation;
import static de.esoco.lib.expression.Predicates.isNull;
import static de.esoco.lib.expression.Predicates.untilCountDown;
import static de.esoco.lib.service.ModificationSyncEndpoint.releaseLock;
import static de.esoco.lib.service.ModificationSyncEndpoint.requestLock;
import static de.esoco.lib.service.ModificationSyncEndpoint.syncRequest;
import static de.esoco.storage.StoragePredicates.like;
import static de.esoco.storage.StorageRelationTypes.STORAGE_MAPPING;
import static org.obrel.type.MetaTypes.MODIFIED;

/**
 * A management class with static method to access, modify, and create
 * entities.
 *
 * @author eso
 */
public class EntityManager {

	/**
	 * The separator character for the prefix of global entity IDs.
	 */
	public static final String GLOBAL_ID_PREFIX_SEPARATOR = "-";

	private static final String MSG_ENTITY_MODFICATION_LOCKED =
		"Modification of %s by %s not possible, currently locked by %s";

	private static final String MSG_CONCURRENT_MODIFICATION =
		"Modification of %s by %s instead of %s";

	private static final String MSG_ENTITY_LOCKED =
		"Entity %s already locked by %s";

	private static final Map<String, Class<? extends Entity>> idPrefixRegistry =
		new HashMap<String, Class<? extends Entity>>();

	private static final MultiLevelCache<String, Entity> entityCache =
		createEntityCache();

	/**
	 * An entity cache implementation that always returns NULL which can be
	 * used
	 * to disabled entity caching.
	 */
	private static final EntityCache<Entity> NO_CACHE =
		new EntityCache<Entity>() {
			@Override
			public void cacheEntity(Entity entity) {
			}

			@Override
			public Entity getEntity(long id) {
				return null;
			}
		};

	/**
	 * A listener relation type for the storing of entities
	 */
	@SuppressWarnings("serial")
	private static final ListenerType<StoreListener, Entity> STORE_LISTENERS =
		ListenerTypes.newListenerType((l, e) -> l.entityStored(e));

	private static final ThreadLocal<String> entityModificationContextId =
		new ThreadLocal<>();

	private static final ThreadLocal<Relatable> entityModificationContext =
		new ThreadLocal<>();

	private static final Lock cacheLock = new ReentrantLock();

	private static final Set<Class<? extends Entity>> deleteEnabledEntities =
		new HashSet<>();

	private static final Map<String, Entity> modifiedEntities =
		new HashMap<>();

	private static final Map<String, Predicate<? super Entity>>
		modificationLockRules = new HashMap<>();

	private static final Map<Class<? extends Entity>, EntityCache<?
		extends Entity>>
		entityCacheMap = new HashMap<>();

	private static final Map<Class<? extends Entity>, EntityDefinition<?>>
		entityDefinitions = new HashMap<>();

	private static SessionManager sessionManager = null;

	private static boolean entityModificationTracking = true;

	private static boolean automaticChangeLogging = true;

	private static boolean usePluralStorageNames = false;

	private static String entitySyncClientId;

	private static String entitySyncContext;

	private static Optional<Endpoint> entitySyncEndpoint = Optional.empty();

	private static boolean syncServiceEnabled = false;

	static {
		RelationTypes.init(EntityManager.class);
	}

	/**
	 * Private, only static use.
	 */
	private EntityManager() {
	}

	/**
	 * Extends the given {@link Predicate} to also match the Entity-prefix if
	 * the given entity class is not NULL.
	 *
	 * @param entityClass The entity class which prefix to match
	 * @param extraAttr   The {@link Predicate} to extend.
	 * @return The extended {@link Predicate}
	 */
	private static <E extends Entity> Predicate<Relatable> addEntityPrefixPredicate(
		Class<E> entityClass, Predicate<Relatable> extraAttr) {
		if (entityClass != null) {
			String idPrefix = getEntityDefinition(entityClass).getIdPrefix();

			extraAttr =
				extraAttr.and(ExtraAttribute.ENTITY.is(like(idPrefix + "%")));
		}

		return extraAttr;
	}

	/**
	 * Adds a listener that will be notified if an entity is stored through the
	 * entity manager.
	 *
	 * @param listener The listener to add
	 */
	public static void addStoreListener(StoreListener listener) {
		getStoreListeners().add(listener);
	}

	/**
	 * Recursively assigns the child entities in a list to their parent
	 * entities.
	 *
	 * @param parent          The parent entity
	 * @param allChildren     The list of all (remaining) children
	 * @param parentAttribute The parent attribute of the children
	 * @param childAttribute  The child attribute of the parent
	 */
	private static <C extends Entity> void assignChildren(C parent,
		List<C> allChildren, RelationType<C> parentAttribute,
		RelationType<List<C>> childAttribute) {
		List<C> children =
			findDirectChildren(parent, allChildren, parentAttribute,
				childAttribute);

		for (C child : children) {
			assignChildren(child, allChildren, parentAttribute,
				childAttribute);
		}
	}

	/**
	 * Signals the beginning of an entity modification. If the entity is cached
	 * it will be marked to allow the detection of concurrent modifications of
	 * the entity instance. To end an active entity modification the method
	 * {@link #endEntityModification(Entity)} must be invoked.
	 *
	 * <p>The tracking of entity modifications is based on the current
	 * modification context that will be set (and cleared) by other framework
	 * classes through the method
	 * {@link #setEntityModificationContext(String, Relatable, boolean)}. If no
	 * modification context is available the ID of the currently active session
	 * will be used instead if an instance of the {@link SessionManager}
	 * interface had been set during application initialization with
	 * {@link #setSessionManager(SessionManager)}.</p>
	 *
	 * <p>This method is synchronized to prevent multiple threads from
	 * concurrently invoking this method for the same entity. It is not
	 * synchronizing on the entity itself because that could cause deadlocks if
	 * multiple elements of an entity hierarchy are marked for modification at
	 * the same time.</p>
	 *
	 * @param entity The entity to be modified
	 * @throws ConcurrentModificationException If the entity is already being
	 *                                         modified by another session
	 */
	static synchronized void beginEntityModification(Entity entity) {
		if (entityModificationTracking && entity.isPersistent() &&
			!entity.hasFlag(NO_ENTITY_LOCKING)) {
			String contextId = getEntityModificationContextId();
			String handle = entity.get(ENTITY_MODIFICATION_HANDLE);
			String entityId = entity.getGlobalId();

			if (handle == null && !modifiedEntities.containsKey(entityId)) {
				checkModificationLockRules(entity, contextId);
				trySyncEndpointLock(entity);

				Relatable context = entityModificationContext.get();

				entity.set(ENTITY_MODIFICATION_HANDLE, contextId);

				if (context != null) {
					context
						.get(CONTEXT_MODIFIED_ENTITIES)
						.put(entityId, entity);
				}

				modifiedEntities.put(entityId, entity);
			} else if (handle == null || !handle.equals(contextId)) {
				throwConcurrentEntityModification(entity,
					MSG_CONCURRENT_MODIFICATION, entity, contextId, handle);
			}
		}
	}

	/**
	 * Stores an entity in the global entity cache for lookup by the global
	 * entity ID. This is done automatically by the methods that query entities
	 * but can also be invoked by application code to cache entities that have
	 * just been created. The given entity must have a valid ID or else an
	 * exception will be thrown. The entity will be cached with a weak
	 * reference
	 * so that it will be removed from the cache if it is no longer referenced
	 * by application code.
	 *
	 * @param entity The entity to cache
	 */
	public static <E extends Entity> void cacheEntity(E entity) {
		if (entity != null && entity.isRoot()) {
			@SuppressWarnings("unchecked")
			EntityCache<E> cache =
				(EntityCache<E>) entityCacheMap.get(entity.getClass());

			if (cache != null) {
				cache.cacheEntity(entity);
			} else {
				entityCache.put(getGlobalEntityId(entity), entity);
				entity.set(CACHE_ENTITY);
			}
		}
	}

	/**
	 * Checks whether an entity is already cached and either replaces it with
	 * the cached version or places it into the entity cache.
	 *
	 * @param entity The entity to check
	 * @return Either an already cached instance of the entity or the now
	 * cached
	 * entity
	 */
	static <E extends Entity> E checkCaching(E entity) {
		cacheLock.lock();

		try {
			@SuppressWarnings("unchecked")
			E cachedEntity =
				(E) getCachedEntity(entity.getClass(), entity.getId());

			if (cachedEntity != null) {
				// use cache entity if available to preserve already
				// loaded entity hierarchies
				entity = cachedEntity;
			} else {
				cacheEntity(entity);
			}
		} finally {
			cacheLock.unlock();
		}

		return entity;
	}

	/**
	 * Checks the modification lock rules that have been registered through the
	 * method {@link #setEntityModificationLock(String, Predicate)}.
	 *
	 * @param entity    The entity that is about to be modified
	 * @param contextId The ID of the modification context that tries to modify
	 *                  the entity
	 */
	@SuppressWarnings("boxing")
	private static void checkModificationLockRules(Entity entity,
		String contextId) {
		for (Entry<String, Predicate<? super Entity>> lockRule :
			modificationLockRules.entrySet()) {
			String lockContextID = lockRule.getKey();

			if (!contextId.equals(lockContextID) &&
				lockRule.getValue().evaluate(entity)) {
				throwConcurrentEntityModification(entity,
					MSG_ENTITY_MODFICATION_LOCKED, entity, contextId,
					lockContextID);
			}
		}
	}

	/**
	 * Checks whether all modified entities in an entity modification context
	 * have been stored. See
	 * {@link #setEntityModificationContext(String, Relatable, boolean)} for
	 * details.
	 *
	 * <p>This method is intended to be used internally by the framework only.
	 * </p>
	 *
	 * @param contextId The ID of the entity modification context to check
	 * @param context   The {@link Relatable} object that serves as the context
	 */
	public static void checkUnsavedEntityModifications(String contextId,
		Relatable context) {
		// copy map to prevent concurrent modification from resetEntity which
		// in tun invokes endEntityModification
		Map<String, Entity> entities =
			new LinkedHashMap<>(context.get(CONTEXT_MODIFIED_ENTITIES));

		for (Entity entity : entities.values()) {
			if (entity.isModified()) {
				try {
					Log.infof(
						"Entity %s modified by %s but not stored, reverting",
						entity, entity.get(ENTITY_MODIFICATION_HANDLE));
					resetEntity(entity);
				} catch (Exception e) {
					Log.warnf(e, "Could not revert entity %s", entity);
				}
			} else {
				Log.errorf("Entity %s not removed from context %s", entity,
					contextId);
			}

			modifiedEntities.remove(entity.getGlobalId());
		}

		context.get(CONTEXT_MODIFIED_ENTITIES).clear();
	}

	/**
	 * Collects all entities in a hierarchy of entities that match a certain
	 * predicate by descending the entity tree at a certain child attribute.
	 *
	 * @param entities       The list of root entities to start searching at
	 * @param childAttribute The child attribute to descend the hierarchy at
	 * @param predicate      The predicate to evaluate the entities with
	 * @return A new list containing the resulting entities (may be empty but
	 * will never be NULL)
	 */
	public static <E extends Entity> List<E> collectDownwards(List<E> entities,
		RelationType<List<E>> childAttribute, Predicate<? super E> predicate) {
		List<E> result = CollectionUtil.collect(entities, predicate);

		for (E entity : entities) {
			result.addAll(
				collectDownwards(entity.get(childAttribute), childAttribute,
					predicate));
		}

		return result;
	}

	/**
	 * Creates the entity cache.
	 *
	 * @return The entity cache instance
	 */
	@SuppressWarnings("boxing")
	private static MultiLevelCache<String, Entity> createEntityCache() {
		int firstLevel = 50;
		int secondLevel = 500;
		int thirdLevel = 5000;

		String cacheSizes = System.getProperty("entity_cache_sizes", null);

		if (cacheSizes != null) {
			try {
				String[] sizes = cacheSizes.split("-");

				firstLevel = Integer.parseInt(sizes[0]);
				secondLevel = Integer.parseInt(sizes[1]);
				thirdLevel = Integer.parseInt(sizes[2]);

				Log.infof("Entity cache levels: %d, %d, %d", firstLevel,
					secondLevel, thirdLevel);
			} catch (Exception e) {
				Log.warn("Invalid entity cache size definition: " + cacheSizes);
			}
		}

		return new MultiLevelCache<String, Entity>(firstLevel, secondLevel,
			thirdLevel);
	}

	/**
	 * Creates a query predicate from a mapping of attribute relation types to
	 * attribute predicates.
	 *
	 * @param criteraMap The mapping from attribute relation types to attribute
	 *                   predicates
	 * @param and        TRUE for an AND concatenation of the attribute
	 *                   predicates, FALSE for OR
	 * @return The resulting query predicate
	 */
	@SuppressWarnings("unchecked")
	private static <E extends Entity> Predicate<E> createQueryPredicate(
		Map<RelationType<?>, Predicate<?>> criteraMap, boolean and) {
		Predicate<E> criteria = null;

		for (Entry<RelationType<?>, Predicate<?>> entry :
			criteraMap.entrySet()) {
			RelationType<Object> attribute =
				(RelationType<Object>) entry.getKey();

			Predicate<Object> criterion = (Predicate<Object>) entry.getValue();

			if (and) {
				criteria =
					Predicates.and(criteria, ifAttribute(attribute,
						criterion));
			} else {
				criteria =
					Predicates.or(criteria, ifAttribute(attribute, criterion));
			}
		}

		return criteria;
	}

	/**
	 * Deletes the given {@link Entity} from the database. Children attached to
	 * this {@link Entity} are <b>not</b> deleted.
	 *
	 * @param entity The {@link Entity} to delete from the database.
	 * @throws StorageException     If accessing the storage fails
	 * @throws TransactionException If the deletion fails
	 * @see #delete(Entity, boolean)
	 */
	public static void delete(Entity entity)
		throws StorageException, TransactionException {
		delete(entity, false);
	}

	/**
	 * Deletes the given {@link Entity} and all of its children from the
	 * database.
	 *
	 * @param entity         The entity to delete.
	 * @param deleteChildren TRUE if all children of the given entity should be
	 *                       deleted as well, FALSE otherwise.
	 * @throws StorageException     If accessing the storage fails
	 * @throws TransactionException If the deletion fails
	 */
	public static void delete(Entity entity, boolean deleteChildren)
		throws StorageException, TransactionException {
		Storage storage = StorageManager.getStorage(entity.getClass());

		TransactionManager.begin();
		TransactionManager.addTransactionElement(storage);

		try {
			if (deleteChildren) {
				deleteChildren(entity, storage);
			}

			storage.delete(entity);
			TransactionManager.commit();
		} catch (Exception e) {
			TransactionManager.rollback();

			throw e;
		}
	}

	/**
	 * Deletes all given {@link Entity Entities} from the database. This does
	 * <b>not</b> delete any child entities that a entity might have
	 *
	 * @param entities The {@link Entity Entities} to delete from the database.
	 * @throws StorageException     If accessing the storage fails
	 * @throws TransactionException If the deletion fails
	 * @see #deleteAll(Collection, boolean)
	 */
	public static void deleteAll(Collection<Entity> entities)
		throws StorageException, TransactionException {
		TransactionManager.begin();

		try {
			for (Entity entity : entities) {
				delete(entity, false);
			}

			TransactionManager.commit();
		} catch (Exception e) {
			TransactionManager.rollback();

			throw e;
		}
	}

	/**
	 * Deletes all given {@link Entity Entities} from the database. This does
	 * <b>also</b> delete any child entities that a entity might have if
	 * deleteChildren is TRUE.
	 *
	 * @param entities       entities The {@link Entity Entities} to delete
	 *                          from
	 *                       the database.
	 * @param deleteChildren TRUE if child entities of an entity should be
	 *                       deleted as well.
	 * @throws StorageException     If accessing the storage fails
	 * @throws TransactionException If the deletion fails
	 */
	public static <E extends Entity> void deleteAll(Collection<E> entities,
		boolean deleteChildren) throws StorageException, TransactionException {
		for (Entity entity : entities) {
			delete(entity, deleteChildren);
		}
	}

	/**
	 * Deletes the children of the given {@link Entity} if any from the
	 * database. Ten entity itself is not deleted.
	 *
	 * @param entity  The {@link Entity}
	 * @param storage The {@link Storage} from which to delete the entity. The
	 *                storage is expected to be initialized and attached to a
	 *                transaction.
	 * @throws StorageException if deleting the children fails
	 */
	private static void deleteChildren(Entity entity, Storage storage)
		throws StorageException {
		Collection<RelationType<List<Entity>>> childAttributes =
			entity.getDefinition().getChildAttributes();

		for (RelationType<List<Entity>> childAttribute : childAttributes) {
			List<Entity> childEntities = entity.get(childAttribute);

			for (Entity childEntity : childEntities) {
				deleteChildren(childEntity, storage);
				storage.delete(childEntity);
			}
		}
	}

	/**
	 * Disables the caching of a certain entity type. The given entity class
	 * must be of the exact type for which the caching shall be disabled.
	 *
	 * @param entityClasses The entity type to disabled caching for
	 */
	@SafeVarargs
	public static void disableCaching(
		Class<? extends Entity>... entityClasses) {
		for (Class<? extends Entity> type : entityClasses) {
			entityCacheMap.put(type, NO_CACHE);
		}
	}

	/**
	 * Prevents the deletion of entities of a certain type. After this method
	 * has been invoked it is no longer possible to invoke the method
	 * {@link #delete(Entity, boolean)} for the given entity type.
	 *
	 * @param entityClass The type for which to disable the deletion
	 * @see #enableDeletionOf(Class)
	 * @see #isDeletionEnabledFor(Class)
	 */
	public static void disableDeletionOf(Class<? extends Entity> entityClass) {
		deleteEnabledEntities.remove(entityClass);
	}

	/**
	 * Allows to delete entities of a certain type. This method must be invoked
	 * once before the method {@link #delete(Entity, boolean)} can be invoked
	 * for the given entity type.
	 *
	 * @param entityClass The type for which to enable the deletion
	 * @see #disableDeletionOf(Class)
	 * @see #isDeletionEnabledFor(Class)
	 */
	public static void enableDeletionOf(Class<? extends Entity> entityClass) {
		deleteEnabledEntities.add(entityClass);
	}

	/**
	 * Signals the end of an entity modification that had been started by a
	 * call
	 * to {@link #beginEntityModification(Entity)}.
	 *
	 * @param entity The entity to be modified
	 */
	static synchronized void endEntityModification(Entity entity) {
		if (entityModificationTracking &&
			entity.hasRelation(ENTITY_MODIFICATION_HANDLE)) {
			String contextId = getEntityModificationContextId();
			String handle = entity.get(ENTITY_MODIFICATION_HANDLE);

			if (!handle.equals(contextId)) {
				throwConcurrentEntityModification(entity,
					MSG_CONCURRENT_MODIFICATION, entity, contextId, handle);
			}

			trySyncEndpointRelease(entity);

			Relatable context = entityModificationContext.get();
			String entityId = entity.getGlobalId();

			if (context != null) {
				Entity contextEntity =
					context.get(CONTEXT_MODIFIED_ENTITIES).remove(entityId);

				if (contextEntity != null) {
					context.get(CONTEXT_UPDATED_ENTITIES).add(contextEntity);
				}
			}

			entity.deleteRelation(ENTITY_MODIFICATION_HANDLE);
			modifiedEntities.remove(entityId);
		}
	}

	/**
	 * Performs an entity query and evaluates all entities that match the query
	 * predicate with a function. Optionally a predicate can be given that
	 * stops
	 * the query as soon as the predicate evaluates to FALSE.
	 *
	 * @param entities      The query predicate for the entities to evaluate
	 * @param stopCondition An optional predicate that defines a stop condition
	 *                      by evaluating to FALSE or NULL for none
	 * @param action        A function to evaluate each queried entity
	 * @throws StorageException If the storage access fails
	 */
	@SuppressWarnings("boxing")
	public static <E extends Entity> void evaluateEntities(
		QueryPredicate<E> entities, Predicate<? super E> stopCondition,
		Action<? super E> action) throws StorageException {
		try (EntityIterator<E> iterator = new EntityIterator<>(entities)) {
			while (iterator.hasNext()) {
				E entity = iterator.next();

				if (stopCondition != null && !stopCondition.evaluate(entity)) {
					break;
				}

				action.evaluate(entity);
			}
		} catch (StorageRuntimeException e) {
			throw e.getCause();
		}
	}

	/**
	 * Fetches all global extra attributes for a collections of entities. This
	 * call will only perform a single storage query for all extra attributes
	 * and thus considerably speeds up the querying of extra attributes for
	 * larger sets of entities.
	 *
	 * @param entities The entities to fetch the extra attributes for
	 * @throws StorageException If querying the extra attributes fails
	 */
	public static void fetchExtraAttributes(
		Collection<? extends Entity> entities) throws StorageException {
		QueryPredicate<ExtraAttribute> extraAttributes =
			forEntity(ExtraAttribute.class, ExtraAttribute.OWNER
				.is(equalTo(null))
				.and(ExtraAttribute.ENTITY.is(elementOf(entities))));

		forEach(extraAttributes, xA -> xA
			.get(ExtraAttribute.ENTITY)
			.get(EXTRA_ATTRIBUTE_MAP)
			.put(xA.get(ExtraAttribute.KEY).toString(), xA));

		for (Entity entity : entities) {
			if (!entity.hasRelation(EXTRA_ATTRIBUTES_READ)) {
				entity.set(EXTRA_ATTRIBUTES_READ);
			}
		}
	}

	/**
	 * Fetches the complete child hierarchy of a parent entity.
	 *
	 * @param parent         The parent entity
	 * @param childAttribute The child attribute to fetch the hierarchy of
	 * @throws StorageException If reading a child fails
	 */
	@SuppressWarnings("unchecked")
	public static <P extends Entity, C extends Entity> void fetchHierarchy(
		P parent, RelationType<List<C>> childAttribute)
		throws StorageException {
		EntityDefinition<C> childDef =
			(EntityDefinition<C>) childAttribute.get(STORAGE_MAPPING);
		RelationType<P> childMasterAttribute =
			(RelationType<P>) childDef.getMasterAttribute();
		RelationType<C> childParentAttribute =
			(RelationType<C>) childDef.getParentAttribute();

		if (childMasterAttribute == null) {
			throw new UnsupportedOperationException(
				"fetchHierarchy() is only " + "possible for master-detail " +
					"relations");
		}

		// remove from cache to prevent inconsistencies
		removeCachedEntity(parent);

		Class<C> childType = childDef.getMappedType();
		Storage storage = StorageManager.getStorage(childType);
		List<C> allChildren = new ArrayList<>();

		QueryPredicate<C> queryChildren = forEntity(childType,
			ifAttribute(childMasterAttribute, equalTo(parent)));

		try (Query<C> query = storage.query(queryChildren)) {
			QueryResult<C> result = query.execute();

			while (result.hasNext()) {
				allChildren.add(result.next());
			}
		} finally {
			storage.release();
		}

		List<C> children =
			findDirectChildren(parent, allChildren, childMasterAttribute,
				childAttribute);

		for (C child : children) {
			assignChildren(child, allChildren, childParentAttribute,
				childAttribute);
		}

		cacheEntity(parent);
	}

	/**
	 * Searches a list of child entities for the direct children of a certain
	 * parent and sets them as the children of the parent and removes the found
	 * entities from the input list.
	 *
	 * @param parent          The parent entity
	 * @param allChildren     The list of all children
	 * @param parentAttribute The parent attribute of the children
	 * @param childAttribute  The child attribute of the parent
	 * @return The list of children found
	 */
	@SuppressWarnings({ "unchecked", "boxing" })
	private static <P extends Entity, C extends Entity> List<C> findDirectChildren(
		P parent, List<C> allChildren, RelationType<P> parentAttribute,
		RelationType<List<C>> childAttribute) {
		EntityDefinition<P> parentDef =
			(EntityDefinition<P>) parent.getDefinition();
		EntityDefinition<C> childDef =
			(EntityDefinition<C>) childAttribute.get(STORAGE_MAPPING);

		Predicate<Relatable> childCriteria;

		if (parentDef == childDef || childDef.getMasterAttribute() == null) {
			childCriteria =
				ifRelation(PARENT_ENTITY_ID, equalTo(parent.getId()));
		} else {
			childCriteria =
				ifRelation(MASTER_ENTITY_ID, equalTo(parent.getId())).and(
					ifRelation(PARENT_ENTITY_ID, isNull()));
		}

		List<C> children = CollectionUtil.collect(allChildren, childCriteria);

		for (C child : children) {
			// prevent setting of modified flag
			child.set(MetaTypes.INITIALIZING);

			// parent attribute must be NULL to prevent exception in
			// initChildren()
			child.set(parentAttribute, null);
		}

		parent.set(childAttribute, children);
		parentDef.initChildren(parent, children, childDef, true);
		allChildren.removeAll(children);

		return children;
	}

	/**
	 * Searches for the first entity that matches a certain predicate in a
	 * hierarchy of entities by descending the entity tree at a certain child
	 * attribute.
	 *
	 * @param entities       The list of root entities to start searching at
	 * @param childAttribute The child attribute to descend the hierarchy at
	 * @param predicate      The predicate to evaluate the entities with
	 * @return The first matching entity or NULL if none could be found
	 */
	public static <E extends Entity> E findDownwards(List<E> entities,
		RelationType<List<E>> childAttribute, Predicate<? super E> predicate) {
		E result = CollectionUtil.find(entities, predicate);

		if (result == null) {
			int count = entities.size();

			for (int i = 0; i < count && result == null; i++) {
				List<E> children = entities.get(i).get(childAttribute);

				if (children != null) {
					result = findDownwards(children, childAttribute,
						predicate);
				}
			}
		}

		return result;
	}

	/**
	 * Invokes an action on all entities of a certain query.
	 *
	 * @param entities The query predicate for the entities
	 * @param action   An action to evaluate each queried entity with
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity> void forEach(QueryPredicate<E> entities,
		Action<? super E> action) throws StorageException {
		evaluateEntities(entities, null, action);
	}

	/**
	 * Invokes an action on all entities that match the given criteria.
	 *
	 * @param entityType entities The query predicate for the entities
	 * @param criteria   The query criteria
	 * @param action     An action to evaluate each queried entity with
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity> void forEach(Class<E> entityType,
		Predicate<? super E> criteria, Action<? super E> action)
		throws StorageException {
		forEach(new QueryPredicate<>(entityType, criteria), action);
	}

	/**
	 * Returns the current capacities of the three levels of the entity cache.
	 *
	 * @return A three-element integer array containing the capacities of the
	 * cache levels
	 */
	public static int[] getCacheCapacity() {
		return entityCache.getCapacity();
	}

	/**
	 * Returns a description of the current entity cache usage.
	 *
	 * @return The cache usage description
	 */
	public static String getCacheUsage() {
		return entityCache.getUsage();
	}

	/**
	 * Returns the cached entity for a particular global entity ID.
	 *
	 * @param globalEntityId The global entity ID
	 * @return The corresponding entity or NULL for none
	 */
	public static Entity getCachedEntity(String globalEntityId) {
		return entityCache.get(globalEntityId);
	}

	/**
	 * Returns an entity with a certain class and ID from the global entity
	 * cache. If no entity with the given ID exists in the cache NULL will be
	 * returned.
	 *
	 * @param entityClass The entity type to lookup in the cache
	 * @param entityId    The ID of the entity to lookup in the cache
	 * @return The cached entity or NULL for none
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E getCachedEntity(Class<E> entityClass,
		long entityId) {
		EntityCache<?> cache = entityCacheMap.get(entityClass);
		Entity result = null;

		if (cache != null) {
			result = cache.getEntity(entityId);
		} else {
			String id = getGlobalEntityId(entityClass, entityId);

			result = getCachedEntity(id);
		}

		return (E) result;
	}

	/**
	 * Returns the distinct values of an entity attribute for a certain entity
	 * query.
	 *
	 * @param attribute The entity attribute
	 * @param entities  The query predicate for the entities
	 * @return A new collection containing the distinct entity attribute values
	 * @throws StorageException If performing the query fails
	 */
	@SuppressWarnings("unchecked")
	public static <T, E extends Entity> Collection<T> getDistinct(
		RelationType<T> attribute, QueryPredicate<E> entities)
		throws StorageException {
		Class<E> queryType = entities.getQueryType();
		Storage storage = StorageManager.getStorage(queryType);
		Collection<T> distinctValues = null;

		try (Query<E> query = storage.query(entities)) {
			distinctValues = (Collection<T>) query.getDistinct(attribute);
		} finally {
			storage.release();
		}

		return distinctValues;
	}

	/**
	 * Returns the number of entities that correspond to certain query
	 * criteria.
	 *
	 * @param entityClass The entity type to query
	 * @param criteria    The criteria to search the entities by
	 * @return The number of entities matching the given criteria
	 * @throws StorageException If accessing the storage fails
	 */
	public static <E extends Entity> int getEntityCount(Class<E> entityClass,
		Predicate<? super E> criteria) throws StorageException {
		Storage storage = StorageManager.getStorage(entityClass);
		int count;

		try {
			Query<E> query = storage.query(forEntity(entityClass, criteria));

			count = query.size();

			query.close();
		} finally {
			storage.release();
		}

		return count;
	}

	/**
	 * Returns the entity definition for a certain entity type.
	 *
	 * @param entityClass The class to return the entity definition for
	 * @return The entity definition for the given type or NULL if none exists
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> EntityDefinition<E> getEntityDefinition(
		Class<E> entityClass) {
		EntityDefinition<E> definition =
			(EntityDefinition<E>) entityDefinitions.get(entityClass);

		if (definition == null) {
			if (StorageManager.getMappingFactory(Entity.class) == null) {
				// if not yet initialized do now to register the mapping
				// factory
				init();
			}

			definition =
				(EntityDefinition<E>) StorageManager.getMapping(entityClass);
			entityDefinitions.put(entityClass, definition);
		}

		return definition;
	}

	/**
	 * Internal method to determine the most specific entity modification
	 * context ID that is currently available.
	 *
	 * @return The context ID
	 */
	private static String getEntityModificationContextId() {
		String contextId = entityModificationContextId.get();

		if (contextId == null && sessionManager != null) {
			contextId = sessionManager.getSessionId();
		}

		if (contextId == null) {
			contextId = Thread.currentThread().getName();
		}

		return contextId;
	}

	/**
	 * Returns the optional {@link Endpoint} of the remote entity sync service.
	 * Such a service is available if it has previously been registered through
	 * {@link #setEntitySyncService(String, String, Endpoint)}. Whether it is
	 * actually used can be queried with {@link #isEntitySyncServiceEnabled()}
	 * and controlled with {@link #setEntitySyncServiceEnabled(boolean)}.
	 *
	 * @return An optional containing the sync service endpoint if present
	 */
	public static final Optional<Endpoint> getEntitySyncEndpoint() {
		return entitySyncEndpoint;
	}

	/**
	 * Returns a global string identifier for a certain entity.
	 *
	 * @param entity The entity to return the global ID for
	 * @return The global entity ID
	 * @throws IllegalArgumentException If no valid ID can be created for the
	 *                                  given entity
	 */
	public static String getGlobalEntityId(Entity entity) {
		long entityId = entity.getId();

		if (entityId < 0) {
			throw new IllegalArgumentException(
				"Entity ID not defined: " + entity);
		}

		return getGlobalEntityId(entity.getClass(), entityId);
	}

	/**
	 * Returns a global string identifier for the combination of a certain
	 * entity definition and numeric ID.
	 *
	 * @param entityClass The entity class
	 * @param entityId    The entity ID
	 * @return The global entity ID for the given parameters
	 */
	public static String getGlobalEntityId(Class<? extends Entity> entityClass,
		long entityId) {
		return getEntityDefinition(entityClass).getIdPrefix() +
			GLOBAL_ID_PREFIX_SEPARATOR + entityId;
	}

	/**
	 * Returns the modification lock rules that have been registered through
	 * {@link #setEntityModificationLock(String, Predicate)}.
	 *
	 * @return A mapping from context IDs to rule predicates
	 */
	public static Map<String, Predicate<? super Entity>> getModificationLockRules() {
		return modificationLockRules;
	}

	/**
	 * Returns a mapping from global entity IDs to the corresponding entities
	 * for all entities that are currently marked as being modified by the
	 * method {@link #beginEntityModification(Entity)}.
	 *
	 * @return The mapping of the modified entities
	 */
	public static final Map<String, Entity> getModifiedEntities() {
		return modifiedEntities;
	}

	/**
	 * Returns the parent hierarchy of a certain entity. The returned list will
	 * contain the parent entities of the given entity starting with the
	 * topmost
	 * parent. The list will always at least contain the argument entity.
	 *
	 * @param entity The entity to return the parent hierarchy of
	 * @return The list of parent entities with the argument entity as the last
	 * element
	 */
	public static <E extends Entity> List<E> getParentHierarchy(E entity) {
		@SuppressWarnings("unchecked")
		RelationType<E> parentAttribute =
			(RelationType<E>) entity.getDefinition().getParentAttribute();

		List<E> hierarchy = new ArrayList<E>();

		hierarchy.add(entity);

		if (parentAttribute != null) {
			while ((entity = entity.get(parentAttribute)) != null) {
				hierarchy.add(0, entity);
			}
		}

		return hierarchy;
	}

	/**
	 * Returns the registered entity types.
	 *
	 * @return The registered entity types
	 */
	public static Collection<Class<? extends Entity>> getRegisteredEntityTypes() {
		return Collections.unmodifiableCollection(idPrefixRegistry.values());
	}

	/**
	 * Returns the entity store listeners.
	 *
	 * @return The list of store listeners
	 */
	private static List<StoreListener> getStoreListeners() {
		return ObjectRelations
			.getRelatable(EntityManager.class)
			.get(STORE_LISTENERS);
	}

	/**
	 * Varargs version of the {@link #init(Collection)} method.
	 *
	 * @see #init(Collection)
	 */
	@SafeVarargs
	public static void init(Class<? extends Entity>... entityClasses) {
		init(Arrays.asList(entityClasses));
	}

	/**
	 * Global entity initialization, must be invoked before any entity
	 * operations are performed. The arguments of this method must be the
	 * entity
	 * classes used by the application. From these the entity definitions will
	 * be initialized so that entity lookups by global IDs are possible.
	 *
	 * @param entityClasses The entity classes used by the application (can be
	 *                      NULL for none)
	 */
	public static void init(Collection<Class<? extends Entity>> entityClasses) {
		EntityRelationTypes.init();
		StorageManager.registerMappingFactory(Entity.class,
			new EntityDefinitionFactory());

		if (entityClasses != null) {
			for (Class<? extends Entity> entityType : entityClasses) {
				getEntityDefinition(entityType);
			}
		}

		getEntityDefinition(ExtraAttribute.class);

		// always disable caching for extra attributes
		disableCaching(ExtraAttribute.class);
	}

	/**
	 * Invalidates the global entity cache.
	 */
	public static void invalidateCache() {
		entityCache.clear();
	}

	/**
	 * Returns TRUE if the automatic history logging of entity changes is
	 * enabled. The default value is TRUE.
	 *
	 * @return The automatic change logging state
	 */
	public static final boolean isAutomaticChangeLogging() {
		return automaticChangeLogging;
	}

	/**
	 * Returns the caching enabled for.
	 *
	 * @param entityClass The caching enabled for
	 * @return The caching enabled for
	 */
	public static boolean isCachingEnabledFor(
		Class<? extends Entity> entityClass) {
		return entityCacheMap.get(entityClass) != NO_CACHE;
	}

	/**
	 * Checks whether the deletion of entities is enabled for a certain entity
	 * type.
	 *
	 * @param entityClass The entity type to check
	 * @return TRUE if the deletion of entities with the given type is allowed
	 * @see #enableDeletionOf(Class)
	 * @see #disableDeletionOf(Class)
	 * @see #delete(Entity, boolean)
	 */
	public static boolean isDeletionEnabledFor(
		Class<? extends Entity> entityClass) {
		return deleteEnabledEntities.contains(entityClass);
	}

	/**
	 * Checks whether usage of the remote sync service is enabled for entity
	 * locking.
	 *
	 * @return TRUE if the entity sync service is enabled
	 */
	public static boolean isEntitySyncServiceEnabled() {
		return syncServiceEnabled;
	}

	/**
	 * Checks if storage names (e.g. JDBC table names) are be derived from
	 * entity names as singular (default) or plural. Can be changed with
	 * {@link #setUsePluralStorageNames(boolean)}.
	 *
	 * @return TRUE if storage names are derived as plural
	 */
	public static final boolean isUsePluralStorageNames() {
		return usePluralStorageNames;
	}

	/**
	 * Queries a list of entities that are identified by certain search
	 * criteria.
	 *
	 * @param entityCriteria entityClass The entity type to query
	 * @param max            The maximum number of entities to read
	 * @return A list of entities for the given criteria (may be empty but will
	 * never be NULL)
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity> List<E> queryEntities(
		QueryPredicate<E> entityCriteria, final int max)
		throws StorageException {
		List<E> entities = new ArrayList<E>();

		evaluateEntities(entityCriteria, untilCountDown(max), entities::add);

		return entities;
	}

	/**
	 * Queries a list of entities that are identified by certain search
	 * criteria.
	 *
	 * @param entityClass The entity type to query
	 * @param criteria    The criteria to search the entities by or NULL for
	 *                    none
	 * @param max         The maximum number of entities to read
	 * @return A list of entities for the given criteria (may be empty but will
	 * never be NULL)
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity> List<E> queryEntities(Class<E> entityClass,
		Predicate<? super E> criteria, int max) throws StorageException {
		return queryEntities(forEntity(entityClass, criteria), max);
	}

	/**
	 * Queries a list of entities that are identified by certain attribute
	 * predicates. The attribute predicates are defined in a map that
	 * associates
	 * the attributes with the corresponding predicates. This map is not
	 * type-safe and the caller must ensure that the content of the predicate
	 * mapping is valid.
	 *
	 * <p>Uses the method {@link #queryEntities(Class, Predicate, int)} to
	 * perform the actual query.</p>
	 *
	 * @param entityClass The entity type to query
	 * @param criteraMap  The mapping from attribute relation types to
	 *                       attribute
	 *                    predicates
	 * @param max         The maximum number of entities to read
	 * @param and         TRUE for an AND concatenation of the attribute
	 *                    predicates, FALSE for OR
	 * @return A list of entities for the given criteria (may be empty but will
	 * never be NULL)
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity> List<E> queryEntities(Class<E> entityClass,
		Map<RelationType<?>, Predicate<?>> criteraMap, int max, boolean and)
		throws StorageException {
		return queryEntities(entityClass, createQueryPredicate(criteraMap,
				and),
			max);
	}

	/**
	 * Queries a list of entities that are identified by a certain attribute
	 * value. See the {@link #queryEntities(Class, Predicate, int)} method for
	 * more information.
	 *
	 * @param entityClass The entity type to query
	 * @param attribute   The attribute to search for
	 * @param value       The attribute value to search for
	 * @param max         The maximum number of entities to read
	 * @return A list of entities for the given criteria (may be empty but will
	 * never be NULL)
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity, T> List<E> queryEntities(
		Class<E> entityClass, RelationType<T> attribute, T value, int max)
		throws StorageException {
		return queryEntities(entityClass,
			ifAttribute(attribute, equalTo(value)), max);
	}

	/**
	 * Queries a list of entities that are associated with certain extra
	 * attributes. When creating the extra attribute predicate an application
	 * must consider that extra attributes can be owner-specific. If only
	 * global
	 * extra attributes shall be queried the predicate should include the
	 * predicate {@link ExtraAttribute#HAS_NO_OWNER}.
	 *
	 * @param extraAttributeCriteria The criteria to search the extra
	 *                                  attributes
	 *                               by
	 * @param max                    The maximum number of entities to read
	 * @return A collection of the distinct entities that have matching extra
	 * attributes (maybe empty but will never be NULL)
	 * @throws StorageException If the storage access fails
	 */
	public static Collection<Entity> queryEntitiesByExtraAttribute(
		Predicate<? super ExtraAttribute> extraAttributeCriteria, int max)
		throws StorageException {
		List<ExtraAttribute> extraAttributes =
			queryEntities(ExtraAttribute.class, extraAttributeCriteria, max);

		Set<Entity> result = new HashSet<>(extraAttributes.size());

		for (ExtraAttribute extraAttribute : extraAttributes) {
			result.add(extraAttribute.get(ExtraAttribute.ENTITY));
		}

		return result;
	}

	/**
	 * Queries a list of entities that have a particular extra attribute with
	 * the given value.
	 *
	 * @param extraAttributeKey   The extra attribute key to search for
	 * @param extraAttributeValue The value of the extra attribute with the
	 *                            given key
	 * @param max                 The maximum number of entities to read
	 * @return A collection of the distinct entities that have a matching extra
	 * attribute (maybe empty but will never be NULL)
	 * @throws StorageException If the storage access fails
	 */
	public static <T> Collection<Entity> queryEntitiesByExtraAttribute(
		RelationType<T> extraAttributeKey, T extraAttributeValue, int max)
		throws StorageException {
		return queryEntitiesByExtraAttribute(null, extraAttributeKey,
			extraAttributeValue, max);
	}

	/**
	 * Queries a list of entities with a certain type that have a a global
	 * extra
	 * attribute.The attributes value is not important.
	 *
	 * @param entityClass       The class of the entity to query
	 * @param extraAttributeKey The extra attribute key to search for
	 * @param max               The maximum number of entities to read
	 * @return A collection of the distinct entities that have a matching extra
	 * attribute (may be empty but will never be NULL)
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity, T> Collection<E> queryEntitiesByExtraAttribute(
		Class<E> entityClass, RelationType<T> extraAttributeKey, int max)
		throws StorageException {
		Predicate<Relatable> extraAttr = ExtraAttribute.HAS_NO_OWNER.and(
			ExtraAttribute.KEY.is(equalTo(extraAttributeKey)));

		extraAttr = addEntityPrefixPredicate(entityClass, extraAttr);

		@SuppressWarnings("unchecked")
		Collection<E> entities =
			(Collection<E>) queryEntitiesByExtraAttribute(extraAttr, max);

		return entities;
	}

	/**
	 * Queries a list of entities with a certain type that have a a global
	 * extra
	 * attribute with the given key and value.
	 *
	 * @param entityClass         The class of the entity to query
	 * @param extraAttributeKey   The extra attribute key to search for
	 * @param extraAttributeValue The value of the extra attribute with the
	 *                            given key
	 * @param max                 The maximum number of entities to read
	 * @return A collection of the distinct entities that have a matching extra
	 * attribute (may be empty but will never be NULL)
	 * @throws StorageException If the storage access fails
	 */
	public static <E extends Entity, T> Collection<E> queryEntitiesByExtraAttribute(
		Class<E> entityClass, RelationType<T> extraAttributeKey,
		T extraAttributeValue, int max) throws StorageException {
		String value = Conversions.asString(extraAttributeValue);

		Predicate<Relatable> extraAttr = ExtraAttribute.HAS_NO_OWNER
			.and(ExtraAttribute.KEY.is(equalTo(extraAttributeKey)))
			.and(ExtraAttribute.VALUE.is(equalTo(value)));

		extraAttr = addEntityPrefixPredicate(entityClass, extraAttr);

		@SuppressWarnings("unchecked")
		Collection<E> entities =
			(Collection<E>) queryEntitiesByExtraAttribute(extraAttr, max);

		return entities;
	}

	/**
	 * Queries the entity instance that is identified by a certain global
	 * entity
	 * ID from the storage associated with the corresponding entity definition.
	 * The global ID string must be in the format that is returned by the
	 * method
	 * {@link #getGlobalEntityId(Entity)}.
	 *
	 * @param globalEntityId The global entity ID to query the entity for
	 * @return The entity for the given ID or NULL if none could be found
	 * @throws StorageException         If the storage query fails
	 * @throws IllegalArgumentException If the given entity ID is invalid
	 * @throws IllegalStateException    If no entity definition has been
	 *                                  registered for the given ID or if the
	 *                                  query yields more than one entity
	 */
	public static Entity queryEntity(String globalEntityId)
		throws StorageException {
		Entity entity;
		String[] idElements = globalEntityId.split(GLOBAL_ID_PREFIX_SEPARATOR);

		if (idElements.length != 2 || idElements[0].isEmpty() ||
			idElements[1].isEmpty()) {
			throw new IllegalArgumentException(
				"Invalid entity ID: " + globalEntityId);
		}

		String idPrefix = idElements[0];
		String entityId = idElements[1];
		Class<? extends Entity> entityClass = idPrefixRegistry.get(idPrefix);

		if (entityClass == null) {
			throw new IllegalStateException(
				"No entity registered for ID prefix " + idPrefix);
		}

		entity = queryEntity(entityClass, Integer.parseInt(entityId));

		return entity;
	}

	/**
	 * Queries an entity that is identified by a certain entity ID. This method
	 * will first check the global entity cache for an existing entity with the
	 * given ID. If no entity can be found in the cache it will be queried from
	 * the storage associated with the given entity definition and then put
	 * into
	 * the cache for later queries.
	 *
	 * @param entityClass The entity type to query
	 * @param entityId    The global entity ID to query the entity for
	 * @return The entity for the given ID or NULL if none could be found
	 * @throws StorageException      If the storage access fails
	 * @throws IllegalStateException If the query yields more than one entity
	 */
	@SuppressWarnings("boxing")
	public static <E extends Entity> E queryEntity(Class<E> entityClass,
		long entityId) throws StorageException {
		E entity = getCachedEntity(entityClass, entityId);

		if (entity == null) {
			entity = queryEntity(entityClass,
				getEntityDefinition(entityClass).getIdAttribute(), entityId,
				true);
		}

		return entity;
	}

	/**
	 * Queries an entity that is identified by certain search criteria. The
	 * boolean parameters controls whether additional entities that are found
	 * for the given criteria will be ignored or will cause an error. If the
	 * parameter is TRUE and the storage query yields more than one entity an
	 * {@link IllegalStateException} will be thrown.
	 *
	 * @param entityClass    The entity type to query
	 * @param criteria       The criteria to search by
	 * @param failOnMultiple If TRUE the call fails if multiple entities are
	 *                       found for the given criteria
	 * @return The entity for the given criteria or NULL if none could be found
	 * @throws StorageException      If the storage access fails
	 * @throws IllegalStateException If the boolean parameter is TRUE and the
	 *                               query yields more than one entity
	 */
	public static <E extends Entity> E queryEntity(Class<E> entityClass,
		Predicate<? super E> criteria, boolean failOnMultiple)
		throws StorageException {
		int count = failOnMultiple ? 2 : 1;
		List<E> entities = queryEntities(entityClass, criteria, count);
		int size = entities.size();

		if (size > 1) {
			throw new IllegalStateException(
				"Multiple entities for " + criteria);
		}

		E entity = size > 0 ? entities.get(0) : null;

		return entity;
	}

	/**
	 * Queries an entity that is identified by a certain attribute value. See
	 * the method {@link #queryEntity(Class, Predicate, boolean)} for more
	 * information.
	 *
	 * @param entityClass    The entity type to query
	 * @param attribute      The attribute to search for
	 * @param value          The attribute value to search for
	 * @param failOnMultiple If TRUE the call fails if multiple entities are
	 *                       found for the given criteria
	 * @return The entity for the given attribute value or NULL if none
	 * could be
	 * found
	 * @throws StorageException      If the storage access fails
	 * @throws IllegalStateException If the boolean parameter is TRUE and the
	 *                               query yields more than one entity
	 */
	public static <T, E extends Entity> E queryEntity(Class<E> entityClass,
		RelationType<T> attribute, T value, boolean failOnMultiple)
		throws StorageException {
		return queryEntity(entityClass, ifAttribute(attribute, equalTo(value)),
			failOnMultiple);
	}

	/**
	 * Queries an entity that is identified by certain attribute criteria. Uses
	 * the method {@link #queryEntity(Class, Predicate, boolean)} to perform
	 * the
	 * actual query.
	 *
	 * @param entityClass    The entity type to query
	 * @param criteraMap     The mapping from attribute relation types to
	 *                       attribute predicates
	 * @param and            TRUE for an AND catenation of the attribute
	 *                       predicates, FALSE for OR
	 * @param failOnMultiple If TRUE the call fails if multiple entities are
	 *                       found for the given criteria
	 * @return The entity for the given attribute value or NULL if none
	 * could be
	 * found
	 * @throws StorageException      If the storage access fails
	 * @throws IllegalStateException If the fail on multiple parameter is TRUE
	 *                               and the query yields more than one entity
	 */
	public static <T, E extends Entity> E queryEntity(Class<E> entityClass,
		Map<RelationType<?>, Predicate<?>> criteraMap, boolean and,
		boolean failOnMultiple) throws StorageException {
		return queryEntity(entityClass, createQueryPredicate(criteraMap, and),
			failOnMultiple);
	}

	/**
	 * Queries an entity that has an extra attribute with a particular value.
	 *
	 * @see #queryEntityByExtraAttribute(Class, RelationType, Object, boolean)
	 */
	public static <T> Entity queryEntityByExtraAttribute(
		RelationType<T> extraAttributeKey, T extraAttributeValue,
		boolean failOnMultiple) throws StorageException {
		return queryEntityByExtraAttribute(null, extraAttributeKey,
			extraAttributeValue, failOnMultiple);
	}

	/**
	 * Queries an entity of a certain type that has an extra attribute with a
	 * particular value. This is a convenience method that returns the first
	 * entity that is returned by the method
	 * {@link #queryEntitiesByExtraAttribute(Class, RelationType, Object, int)}
	 * or NULL if no matching entity could be found. The boolean parameter
	 * indicates whether the existence of multiple entities should be
	 * considered
	 * as an inconsistency and therefore result in an exception being thrown.
	 * This parameter should in most cases be set to TRUE.
	 *
	 * @param entityClass         The class of the entity to query
	 * @param extraAttributeKey   The extra attribute key to search for
	 * @param extraAttributeValue The value of the extra attribute with the
	 *                            given key
	 * @param failOnMultiple      If TRUE the call fails if multiple entities
	 *                            are found for the given criteria
	 * @return The entity that has the given extra attribute or NULL for none
	 * @throws StorageException      If the storage access fails
	 * @throws IllegalStateException If the boolean parameter is TRUE and the
	 *                               query yields more than one entity
	 */
	public static <E extends Entity, T> E queryEntityByExtraAttribute(
		Class<E> entityClass, RelationType<T> extraAttributeKey,
		T extraAttributeValue, boolean failOnMultiple) throws StorageException {
		Collection<E> entities =
			queryEntitiesByExtraAttribute(entityClass, extraAttributeKey,
				extraAttributeValue, failOnMultiple ? 2 : 1);

		int size = entities.size();

		if (failOnMultiple && size > 1) {
			throw new IllegalStateException(String.format(
				"Multiple entities for " + "extra attribute %s with value %s",
				extraAttributeKey, extraAttributeValue));
		}

		return size > 0 ? entities.iterator().next() : null;
	}

	/**
	 * Registers an entity cache for a certain entity class.
	 *
	 * @param entityClass The entity class to register the cache for
	 * @param cache       The cache instance for the given entity class
	 */
	public static <E extends Entity> void registerEntityCache(
		Class<E> entityClass, EntityCache<E> cache) {
		entityCacheMap.put(entityClass, cache);
	}

	/**
	 * Registers a certain entity definition to be used for a certain sub-type
	 * of the corresponding entity.
	 *
	 * @param subType    The entity sub-class
	 * @param definition The entity definition to register for the given type
	 */
	static <E extends Entity> void registerEntitySubType(
		Class<? extends E> subType, EntityDefinition<E> definition) {
		entityDefinitions.put(subType, definition);
	}

	/**
	 * Internal method to register an entity type with the manager.
	 *
	 * @param entityClass The class to register
	 * @param definition  The entity definition for the entity class
	 * @throws IllegalArgumentException If either the entity name or the ID
	 *                                  prefix of the given entity definition
	 *                                  has been registered already
	 */
	static <E extends Entity> void registerEntityType(
		Class<? extends E> entityClass, EntityDefinition<E> definition) {
		if (idPrefixRegistry.containsValue(entityClass)) {
			throw new IllegalArgumentException(
				"Duplicate entity registration: " + entityClass);
		}

		String idPrefix = definition.getIdPrefix();

		Class<? extends Entity> existingClass = idPrefixRegistry.get(idPrefix);

		if (existingClass == null) {
			idPrefixRegistry.put(idPrefix, entityClass);
		} else if (!existingClass.isAssignableFrom(entityClass)) {
			throw new IllegalArgumentException(String.format(
				"Duplicate entity ID prefix %s for class %s; " +
					"already defined in %s", idPrefix, entityClass,
				idPrefixRegistry.get(idPrefix)));
		}
	}

	/**
	 * Removes an entity from the cache.
	 *
	 * @param entity The entity to remove
	 */
	public static void removeCachedEntity(Entity entity) {
		removeCachedEntity(getGlobalEntityId(entity));
	}

	/**
	 * Removes an entity with a certain ID from the cache.
	 *
	 * @param id The global ID of the entity to remove
	 */
	public static void removeCachedEntity(String id) {
		Entity removedEntity = entityCache.remove(id);

		if (removedEntity != null) {
			removedEntity.set(CACHE_ENTITY, Boolean.FALSE);
		}
	}

	/**
	 * Removes an entity cache registration for a certain entity class.
	 *
	 * @param entityClass The entity class to remove the cache for
	 */
	public static <E extends Entity> void removeEntityCache(
		Class<E> entityClass) {
		entityCacheMap.remove(entityClass);
	}

	/**
	 * Removes an entity modification context that has been set previously with
	 * {@link #setEntityModificationContext(String, Relatable, boolean)}.
	 *
	 * @param contextId      The ID of the entity modification context
	 * @param ignoreExisting TRUE to ignore any different existing context
	 */
	public static void removeEntityModificationContext(String contextId,
		boolean ignoreExisting) {
		if (contextId.equals(entityModificationContextId.get())) {
			entityModificationContext.remove();
			entityModificationContextId.remove();
		} else
			assert ignoreExisting :
				String.format("Modification context mismatch: %s != %s",
					contextId, entityModificationContextId);
	}

	/**
	 * Removes the lock rule for a certain entity modification context. The
	 * removed rule is returned so that the caller could concatenate it into a
	 * new rule and set the combined rule again if necessary.
	 *
	 * @param contextId The ID of the entity modification context
	 * @return The removed rule or NULL for none
	 * @see #setEntityModificationLock(String, Predicate)
	 */
	public static Predicate<? super Entity> removeEntityModificationLock(
		String contextId) {
		return modificationLockRules.remove(contextId);
	}

	/**
	 * Removes a store listener. See {@link #addStoreListener(StoreListener)}
	 * for details.
	 *
	 * @param listener The listener to remove
	 */
	public static void removeStoreListener(StoreListener listener) {
		getStoreListeners().remove(listener);
	}

	/**
	 * Resets an entity to it's current persistent state. This will set all
	 * attributes and references of the given entity to their persistent values
	 * and all other relations to their default values.
	 *
	 * @param entity The entity to reset
	 * @throws StorageException If retrieving the persistent entity state fails
	 */
	public static void resetEntity(Entity entity) throws StorageException {
		String id = entity.getGlobalId();

		cacheLock.lock();

		try {
			// remove entity from cache to force a re-fetching
			removeCachedEntity(id);
			endEntityModification(entity);

			// fetch (and cache) current state from storage
			Entity storedEntity = queryEntity(id);

			// synchronize the existing entity with the persistent state to
			// remove any modifications and update existing references
			ObjectRelations.syncRelations(entity, storedEntity);
		} finally {
			cacheLock.unlock();
		}
	}

	/**
	 * Resets all updated entities in an entity modification context to their
	 * persistent state by invoking {@link #resetEntity(Entity)}.
	 *
	 * <p>This method is intended to be used internally by the framework only.
	 * </p>
	 *
	 * @param context The {@link Relatable} object that serves as the context
	 */
	public static void resetEntityModifications(Relatable context) {
		Set<Entity> entities = context.get(CONTEXT_UPDATED_ENTITIES);

		for (Entity entity : entities) {
			try {
				resetEntity(entity);
			} catch (Exception e) {
				Log.warnf(e, "Could not reset entity %s", entity);
			}
		}

		entities.clear();
	}

	/**
	 * Sets the state of the automatic history logging of entity changes. If
	 * enabled storing a modified entity with
	 * {@link #storeEntity(Entity, Entity)} will create a history record that
	 * contains information about the change. The default value is TRUE and it
	 * should only be set to FALSE for special purposes like bulk insertions.
	 *
	 * @param enabled The new state of the automatic change history logging
	 */
	public static final void setAutomaticChangeLogging(boolean enabled) {
		automaticChangeLogging = enabled;
	}

	/**
	 * Sets the capacity of the different levels of the entity cache. A
	 * value of
	 * zero disables the respective cache level. Typically a level with a
	 * higher
	 * number should be set to a higher capacity as a lower level because the
	 * higher levels can be more easily be reclaimed by the VM. The first level
	 * is permanent, entities in that level will never be removed
	 * automatically.
	 *
	 * @param firstLevel  The capacity of the permanent cache level
	 * @param secondLevel The capacity of the softly referenced cache level
	 * @param thirdLevel  secondLevel The capacity of the weakly referenced
	 *                    cache level
	 */
	public static void setCacheCapacity(int firstLevel, int secondLevel,
		int thirdLevel) {
		entityCache.setCapacity(firstLevel, secondLevel, thirdLevel);
	}

	/**
	 * Sets the entity modification context for the current thread. This
	 * context
	 * will then be used to prevent concurrent modifications of entities. It
	 * must be an instance of {@link Relatable} so that relations for the
	 * modification tracking can be added to it. After the entity modifications
	 * have been performed the method
	 * {@link #removeEntityModificationContext(String, boolean)} must be
	 * invoked.
	 *
	 * <p>This method is intended to be used internally by the framework only.
	 * </p>
	 *
	 * @param contextId    A unique ID of the entity modification context
	 * @param context      A relatable object that serves as the context
	 * @param keepExisting TRUE to keep an existing modification context or
	 *                     FALSE to throw a
	 *                     {@link ConcurrentModificationException} if a context
	 *                     has already been set for the current thread.
	 */
	public static void setEntityModificationContext(String contextId,
		Relatable context, boolean keepExisting) {
		assert contextId != null && context != null;

		String existingContext = entityModificationContextId.get();

		if (existingContext == null) {
			entityModificationContextId.set(contextId);
			entityModificationContext.set(context);
		} else if (!keepExisting) {
			String message = String.format(
				"Modification context already set to %s " +
					"(tried to set to %s)", existingContext, contextId);

			throw new ConcurrentModificationException(message);
		}
	}

	/**
	 * Defines a rule for entities that must not be modified by other
	 * modification contexts but the current one. The rule is a predicate that
	 * must evaluate to TRUE for entities that should be locked against
	 * modification by other contexts. The context ID must be the same as in
	 * the
	 * method
	 * {@link #setEntityModificationContext(String, Relatable, boolean)}.
	 *
	 * <p>All registered modification lock rules will be evaluated by the
	 * method {@link #beginEntityModification(Entity)}. If the rule
	 * evaluates to
	 * TRUE for a certain entity in another context than that which registered
	 * the rule a {@link ConcurrentEntityModificationException} will be thrown
	 * to signal that the entity is not available for modification by the
	 * respective context.</p>
	 *
	 * <p>When the context has finished processing the locked entities it must
	 * invoke {@link #removeEntityModificationLock(String)} to clear the
	 * modification rule.</p>
	 *
	 * @param contextId the ID of the entity modification context
	 * @param rule      The new locked entity rule
	 */
	public static void setEntityModificationLock(String contextId,
		Predicate<? super Entity> rule) {
		modificationLockRules.put(contextId, rule);
	}

	/**
	 * Enables or disables the automatic entity modification tracking.
	 *
	 * @param enabled The new modification tracking state
	 */
	public static void setEntityModificationTracking(boolean enabled) {
		entityModificationTracking = enabled;
	}

	/**
	 * Sets the endpoint of an entity sync service to be used for entity lock
	 * synchronization. The context should be derived from the current
	 * application's execution context, e.g. something like production,
	 * test, or
	 * development. The usage of the service will be enabled by default which
	 * can be queried with {@link #isEntitySyncServiceEnabled()} and controlled
	 * with {@link #setEntitySyncServiceEnabled(boolean)}.
	 *
	 * @param syncClientId A unique identifier of the sync service client
	 * @param syncContext  The application context to sync entities in
	 * @param syncEndpoint The entity sync service endpoint (may be NULL to
	 *                     deactivate)
	 */
	public static void setEntitySyncService(String syncClientId,
		String syncContext, Endpoint syncEndpoint) {
		entitySyncClientId = syncClientId;
		entitySyncContext = syncContext;
		entitySyncEndpoint = Optional.ofNullable(syncEndpoint);
		syncServiceEnabled = entitySyncEndpoint.isPresent();
	}

	/**
	 * Enables or disables the usage of the remote entity sync service for
	 * entity locking.
	 *
	 * @param enabled TRUE to enabled usage of the entity sync service
	 */
	public static void setEntitySyncServiceEnabled(boolean enabled) {
		syncServiceEnabled = enabled;
	}

	/**
	 * Sets the session manager to be used for user identification and
	 * context-based caching.
	 *
	 * @param manager The new session manager
	 */
	public static void setSessionManager(SessionManager manager) {
		sessionManager = manager;
	}

	/**
	 * Globally defines whether storage names (e.g. JDBC table names) should be
	 * derived from entity names as singular (default) or plural.
	 *
	 * @param plural TRUE to use plural storage names
	 */
	public static final void setUsePluralStorageNames(boolean plural) {
		usePluralStorageNames = plural;
	}

	/**
	 * Performs a shutdown by freeing all allocated resources. This will also
	 * invoke the methods {@link StorageManager#shutdown()} and
	 * {@link TransactionManager#shutdown()}.
	 */
	public static void shutdown() {
		getStoreListeners().clear();
		entityCache.clear();
		idPrefixRegistry.clear();
		TransactionManager.shutdown();
		StorageManager.shutdown();
	}

	/**
	 * Stores an entity without a change origin.
	 *
	 * @see #storeEntity(Entity, Entity, boolean)
	 */
	public static void store(Entity entity) throws TransactionException {
		storeEntity(entity, null);
	}

	/**
	 * Stores an entity without a history group.
	 *
	 * @see #storeEntity(Entity, Entity, boolean)
	 */
	public static void storeEntity(Entity entity, Entity changeOrigin)
		throws TransactionException {
		storeEntity(entity, changeOrigin, false);
	}

	/**
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
	 * @param entity       The entity to store
	 * @param changeOrigin The entity to be stored as the history origin
	 * @param historyGroup TRUE to record the the entity change inside a
	 *                        history
	 *                     group
	 * @throws TransactionException If the entity storage transaction fails
	 */
	public static void storeEntity(Entity entity, Entity changeOrigin,
		boolean historyGroup) throws TransactionException {
		Entity storeEntity = entity;
		List<Entity> dependentEntities = null;

		if (entity.hasRelation(DEPENDENT_STORE_ENTITIES)) {
			// get dependent entities before checking the hierarchy and delete
			// the relation before storing to prevent recursions
			dependentEntities = entity.get(DEPENDENT_STORE_ENTITIES);
			entity.deleteRelation(DEPENDENT_STORE_ENTITIES);
		}

		entity = entity.checkForHierarchyUpdate();

		boolean newEntity = !entity.isPersistent();
		Storage storage = StorageManager.getStorage(entity.getClass());

		TransactionManager.begin();

		try {
			String change = null;
			boolean hasChanges = false;

			TransactionManager.addTransactionElement(storage);

			if (automaticChangeLogging) {
				if (entity.hasChangeLogging()) {
					change = entity.createChangeDescription();
					hasChanges = (change != null && change.length() > 0);
				} else {
					// delete flag after skipping of change log
					entity.deleteRelation(SKIP_NEXT_CHANGE_LOGGING);
				}
			}

			entity.set(ENTITY_STORE_ORIGIN, changeOrigin);

			if (hasChanges ||
				(!automaticChangeLogging && entity.hasFlag(MODIFIED))) {
				entity.set(LAST_CHANGE, new Date());
			}

			if (historyGroup) {
				String group = "Store " + entity;

				HistoryManager.begin(changeOrigin, entity, group);
			}

			storage.store(entity);

			if (storeEntity != entity && storeEntity.hasFlag(MODIFIED)) {
				// store original entity if still modified to make changes
				// persistent even if the parent hierarchy doesn't contain it
				// in the case of not cached entities
				// TODO: check if still necessary
				storage.store(storeEntity);
			}

			if (hasChanges) {
				// history must be stored after the entity to prevent endless
				// recursion because of the entity reference in the record
				if (newEntity) {
					@SuppressWarnings("boxing")
					String newId = String.format("<NEW:%s>", entity.getId());

					change = change.replaceFirst("<NEW>", newId);
				}

				HistoryManager.record(HistoryType.CHANGE, changeOrigin, entity,
					change);
			}

			if (dependentEntities != null) {
				for (Entity dependentEntity : dependentEntities) {
					storeEntity(dependentEntity, changeOrigin, false);
				}
			}

			if (historyGroup) {
				HistoryManager.commit(false);
				historyGroup = false;
			}

			TransactionManager.commit();
			cacheEntity(entity);

			STORE_LISTENERS.notifyListeners(EntityManager.class, entity);
			entity.deleteRelation(ENTITY_STORE_ORIGIN);
		} catch (Exception e) {
			if (historyGroup) {
				HistoryManager.rollback();
			}

			if (TransactionManager.isInTransaction()) {
				TransactionManager.rollback();
			}

			if (e instanceof TransactionException) {
				throw (TransactionException) e;
			} else {
				throw new TransactionException("Could not store entity", e);
			}
		} finally {
			storage.release();
		}
	}

	/**
	 * Returns a stream of entities for a certain query. <em>Important:</em>
	 * the
	 * returned stream MUST be closed after use or else a resource leak will
	 * occur. It is recommended to use it as the argument of a
	 * try-with-resource
	 * block to ensure the stream is closed correctly.
	 *
	 * @param entities The entity query
	 * @return A stream of entities
	 */
	public static <E extends Entity> Stream<E> stream(
		QueryPredicate<E> entities) {
		EntityIterator<E> iterator = new EntityIterator<>(entities);
		Spliterator<E> spliterator =
			Spliterators.spliterator(iterator, iterator.size(), 0);

		return StreamSupport
			.stream(spliterator, false)
			.onClose(() -> iterator.close());
	}

	/**
	 * Throws a {@link ConcurrentEntityModificationException} initialized with
	 * the given parameters.
	 *
	 * @param entity        The entity for which to throw the exception
	 * @param messageFormat The message format string
	 * @param messageArgs   The message format arguments
	 */
	private static void throwConcurrentEntityModification(Entity entity,
		String messageFormat, Object... messageArgs)
		throws ConcurrentEntityModificationException {
		messageFormat = String.format(messageFormat, messageArgs);

		throw new ConcurrentEntityModificationException(entity, messageFormat);
	}

	/**
	 * Checks whether a synchronization endpoint is available and if so, tries
	 * to register an entity lock on it.
	 *
	 * @param entity The entity to lock
	 */
	static void trySyncEndpointLock(Entity entity) {
		if (syncServiceEnabled && entitySyncEndpoint.isPresent()) {
			String response = "";

			try {
				EndpointFunction<SyncData, String> requestLock =
					requestLock().from(entitySyncEndpoint.get());

				response = requestLock.send(
					syncRequest(entitySyncClientId, entitySyncContext,
						entity.getGlobalId()));
			} catch (Exception e) {
				// just log but continue with local lock mechanism
				Log.errorf(e, "Error communicating with sync endpoint at %s",
					entitySyncEndpoint.get().get(ENDPOINT_ADDRESS));
			}

			if (!"".equals(response)) {
				throwConcurrentEntityModification(entity, MSG_ENTITY_LOCKED,
					entity, response);
			}
		}
	}

	/**
	 * Checks whether a synchronization endpoint is available and if so,
	 * releases an entity lock from it.
	 *
	 * @param entity The entity to unlock
	 */
	static void trySyncEndpointRelease(Entity entity) {
		if (syncServiceEnabled && entitySyncEndpoint.isPresent()) {
			try {
				EndpointFunction<SyncData, String> releaseLock =
					releaseLock().from(entitySyncEndpoint.get());

				String response = releaseLock.send(
					syncRequest(entitySyncClientId, entitySyncContext,
						entity.getGlobalId()));

				if (!"".equals(response)) {
					Log.warnf("Releasing entity lock for %s failed: %s",
						entity.getGlobalId(), response);
				}
			} catch (Exception e) {
				Log.warnf(e, "Error communicating with sync endpoint at %s",
					entitySyncEndpoint.get().get(ENDPOINT_ADDRESS));
			}
		}
	}

	/**
	 * An interface for event listeners that want to be notified if an entity
	 * has been stored.
	 *
	 * @author eso
	 */
	public interface StoreListener {

		/**
		 * Will be invoked after an entity has been stored through the entity
		 * manager.
		 *
		 * @param entity The entity that has been stored
		 */
		void entityStored(Entity entity);
	}

	/**
	 * An entity-specific implementation of a storage mapping factory.
	 *
	 * @author eso
	 */
	private static class EntityDefinitionFactory
		implements MappingFactory<Entity> {

		/**
		 * Implemented to create a new instance of {@link EntityDefinition}. It
		 * first tries to create an instance of an inner class named
		 * 'Definition' (i.e. of the class type.getName() + "$Definition"). If
		 * it exists it must have a no-argument constructor. If not a standard
		 * entity definition will be created and returned.
		 *
		 * @see MappingFactory#createMapping(Class)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public StorageMapping<Entity, ?, ?> createMapping(Class<Entity> type) {
			EntityDefinition<? extends Entity> result =
				entityDefinitions.get(type);

			if (result == null) {
				try {
					String definitionClass = type.getName() + "$Definition";

					Class<EntityDefinition<Entity>> entityDefinitionClass =
						(Class<EntityDefinition<Entity>>) Class.forName(
							definitionClass);

					result = ReflectUtil.newInstance(entityDefinitionClass);
				} catch (ClassNotFoundException e) {
					result = new EntityDefinition<Entity>(type, null);
				}

				entityDefinitions.put(type, result);
			}

			return (StorageMapping<Entity, ?, ?>) result;
		}
	}
}
