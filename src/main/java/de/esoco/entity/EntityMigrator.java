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

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.logging.Log;
import de.esoco.lib.logging.LogLevel;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.storage.Query;
import de.esoco.storage.QueryResult;
import de.esoco.storage.Storage;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;
import org.obrel.core.RelationType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static de.esoco.entity.EntityPredicates.forEntity;

/**
 * A utility class that provides methods to perform a migration from one entity
 * type to another.
 *
 * @author eso
 */
public class EntityMigrator<S extends Entity, T extends Entity>
	implements Function<S, T> {

	private static final Set<Entity> migratedEntities = new HashSet<Entity>();

	private final Class<S> sourceType;

	private final Class<T> targetType;

	private final Map<RelationType<?>, Function<? super Entity, ?>>
		attributeRules =
		new LinkedHashMap<RelationType<?>, Function<? super Entity, ?>>();

	private final Map<RelationType<? extends List<? extends Entity>>,
		EntityMigrator<?, ?>>
		childMigrators =
		new LinkedHashMap<RelationType<? extends List<? extends Entity>>,
			EntityMigrator<?, ?>>();

	private boolean verbose = false;

	private T target;

	/**
	 * Creates a new instance for certain source and target entity definition.
	 *
	 * @param sourceType The source entity definition
	 * @param targetType The target entity definition
	 */
	public EntityMigrator(Class<S> sourceType, Class<T> targetType) {
		this.sourceType = sourceType;
		this.targetType = targetType;
	}

	/**
	 * Invokes {@link #migrateEntity(Entity)} for the input entity and returns
	 * the result. Any occurring storage exception will be converted into a
	 * runtime exception.
	 *
	 * @see Function#evaluate(Object)
	 */
	@Override
	public T evaluate(S source) {
		try {
			return migrateEntity(source);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the class of target entity type.
	 *
	 * @return The target type class
	 */
	public Class<T> getTargetType() {
		return targetType;
	}

	/**
	 * Migrates a certain set of entities according to the rules of this
	 * instance.
	 *
	 * @param criteria The criteria to limit the migrated entities or NULL for
	 *                 all entities of the source type
	 * @throws Exception If the migration fails
	 */
	@SuppressWarnings("boxing")
	public void migrateEntities(Predicate<? super Entity> criteria)
		throws Exception {
		Storage sourceStorage = StorageManager.getStorage(sourceType);
		Storage targetStorage = StorageManager.getStorage(targetType);

		Query<S> query = sourceStorage.query(forEntity(sourceType, criteria));
		QueryResult<S> entities = query.execute();

		int count = query.size();
		int step = count / 50 + 1;

		if (Log.isLevelEnabled(LogLevel.INFO) ||
			Log.isLevelEnabled(LogLevel.DEBUG) ||
			Log.isLevelEnabled(LogLevel.TRACE)) {
			// disable progress display
			step = count + 1;
		}

		try {
			init();

			System.out.printf("Migrating %s to %s [%d]\n", sourceType,
				targetType, count);

			while (entities.hasNext()) {
				S sourceEntity = entities.next();
				T targetEntity = migrateEntity(sourceEntity);

				if (targetEntity != null) {
					if (verbose) {
						System.out.print("Migrated: \n");
						sourceEntity.printHierarchy(System.out);
						System.out.print("To: \n");
						targetEntity.printHierarchy(System.out);
					}

					migratedEntities.add(targetEntity);
					storeEntity(targetEntity);
					afterStore(targetEntity, sourceEntity);
				} else if (verbose) {
					System.out.printf("Skipped: " + sourceEntity);
				}

				if (!verbose && (count-- % step) == 0) {
					System.out.print("+");
				}
			}

			System.out.print("\n");

			finish();
		} finally {
			try {
				query.close();
			} finally {
				sourceStorage.release();
				targetStorage.release();
			}
		}
	}

	/**
	 * Migrates a single entity according to the rules of this instance. A
	 * subclass may return NULL to omit certain source entities from the
	 * migration.
	 *
	 * @param source The entity to migrate
	 * @return The resulting entity of the target definition or NULL if the
	 * entity shall not be migrated
	 * @throws StorageException     If retrieving an entity from a storage
	 *                              fails
	 * @throws TransactionException If the transaction for storing the target
	 *                              entity fails
	 */
	public T migrateEntity(S source)
		throws StorageException, TransactionException {
		assert source.getClass() == sourceType;

		target = ReflectUtil.newInstance(targetType);

		migrateAttributes(source, target);
		migrateChildren(source, target);

		return target;
	}

	/**
	 * Sets a migration rule for an arbitrary entity reference attribute of the
	 * target entities. The rule argument must be a function that retrieves or
	 * generates the global string ID of the referenced entity from the source
	 * entity. If it is NULL the given target attribute will be ignored.
	 *
	 * @param targetAttribute The attribute of the target entity to set
	 * @param rule            A rule that creates the ID of the referenced
	 *                        entity or NULL to ignore the attribute
	 */
	public void setArbitraryEntityReferenceRule(RelationType<?> targetAttribute,
		Function<? super Entity, String> rule) {
		attributeRules.put(targetAttribute, rule);
	}

	/**
	 * Sets a simple copy migration rule for a particular set of source and
	 * target attributes.
	 *
	 * @param sourceAttribute The source attribute to copy
	 * @param targetAttribute The target attribute to set
	 */
	public <A> void setAttributeCopyRule(RelationType<A> sourceAttribute,
		RelationType<A> targetAttribute) {
		setAttributeRule(targetAttribute, sourceAttribute);
	}

	/**
	 * Sets a migration rule for a particular target attribute. The rule
	 * argument must be a function that retrieves or creates the target
	 * attribute value from the source entity. If it is NULL the given target
	 * attribute will be ignored.
	 *
	 * @param targetAttribute The attribute of the target entity to set
	 * @param rule            A rule that creates the target attribute value or
	 *                        NULL to ignore the attribute
	 */
	public <A> void setAttributeRule(RelationType<A> targetAttribute,
		Function<? super Entity, A> rule) {
		attributeRules.put(targetAttribute, rule);
	}

	/**
	 * Sets an entity migrator that will be used for the migration of a certain
	 * child entity type of the entity that is handled by this instance. If the
	 * migrator is set to NULL the children of the given type will be ignored
	 * during the migration.
	 *
	 * @param targetChildAttribute The target child attribute to migrate
	 * @param migrator             The child migrator or NULL to ignore
	 *                                children
	 *                             of this type
	 */
	public void setChildMigrator(
		RelationType<? extends List<? extends Entity>> targetChildAttribute,
		EntityMigrator<?, ?> migrator) {
		childMigrators.put(targetChildAttribute, migrator);
	}

	/**
	 * Sets a migration rule for a particular entity reference attribute of the
	 * target entities. The rule argument must be a function that retrieves or
	 * creates the integer ID of the referenced entity from the source entity.
	 * If it is NULL the given target attribute will be ignored.
	 *
	 * @param targetAttribute The attribute of the target entity to set
	 * @param rule            A rule that creates the ID of the referenced
	 *                        entity or NULL to ignore the attribute
	 */
	public void setEntityReferenceRule(RelationType<?> targetAttribute,
		Function<? super Entity, Integer> rule) {
		attributeRules.put(targetAttribute, rule);
	}

	/**
	 * Sets the target attributes to be ignored by the migration. This is a
	 * shortcut for invoking {@link #setAttributeRule(RelationType, Function)}
	 * with a rule value of NULL.
	 *
	 * @param attributes The target attributes to be ignored
	 */
	public void setIgnoredAttributes(RelationType<?>... attributes) {
		for (RelationType<?> attribute : attributes) {
			setAttributeRule(attribute, null);
		}
	}

	/**
	 * Sets the verbose flag of this instance. If the flag is TRUE the source
	 * and target entities processed during migration will be output to the
	 * console. The default value is FALSE.
	 *
	 * @param verbose TRUE for verbose mode, FALSE for quiet mode
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Can be overridden when there is need to perform any action on the newly
	 * stored entity.
	 *
	 * @param target The migrated and stored entity
	 * @param source The entity that has been migrated
	 * @throws StorageException     If retrieving an entity from a storage
	 *                              fails
	 * @throws TransactionException If the transaction for storing an entity
	 *                              fails
	 */
	protected void afterStore(T target, S source)
		throws StorageException, TransactionException {
	}

	/**
	 * This method can be overridden by subclasses to perform cleanup
	 * operations
	 * or similar at the end of the migration. It will be invoked after all
	 * entities have been migrated successfully. The default implementation
	 * does
	 * nothing.
	 *
	 * @throws Exception If finishing fails
	 */
	protected void finish() throws Exception {
	}

	/**
	 * This method can be overridden by subclasses to perform initialization
	 * before of the migration of entities. It will be invoked after the
	 * storages have been initialized but before any entity has been migrated.
	 * The default implementation does nothing.
	 *
	 * @throws Exception If the initialization fails
	 */
	protected void init() throws Exception {
	}

	/**
	 * Migrates the attributes from a source entity to the target entity
	 * according to the defined migration rules.
	 *
	 * @param source The source entity
	 * @param target The target entity
	 * @throws StorageException If retrieving an entity from a storage fails
	 */
	@SuppressWarnings({ "boxing", "unchecked" })
	protected void migrateAttributes(S source, T target)
		throws StorageException {
		Set<RelationType<?>> attributes = new HashSet<RelationType<?>>(
			EntityManager.getEntityDefinition(targetType).getAttributes());

		for (Entry<RelationType<?>, Function<? super Entity, ?>> entry :
			attributeRules.entrySet()) {
			RelationType<?> attribute = entry.getKey();
			Function<? super Entity, ?> rule = entry.getValue();

			if (rule != null) {
				Object value = rule.evaluate(source);
				Class<?> attrType = attribute.getTargetType();

				if (Entity.class.isAssignableFrom(attrType)) {
					if (value instanceof String) {
						Entity entity =
							EntityManager.queryEntity((String) value);

						if (entity == null) {
							throw new IllegalStateException(String.format(
								"Could not find referenced entity %s with ID" +
									" " + "%s", attrType, value));
						}

						value = entity;
					} else if (value instanceof Integer) {
						int id = ((Integer) value).intValue();

						if (id > 0) {
							value = EntityManager.queryEntity(
								(Class<Entity>) attrType, id);

							if (value == null) {
								throw new IllegalStateException(String.format(
									"Could not find referenced entity %s " +
										"with" + " " + "ID %d", attrType, id));
							}
						} else {
							value = null;
						}
					}
				}

				target.set((RelationType<Object>) attribute, value);
			}

			attributes.remove(attribute);
		}

		if (attributes.size() > 0) {
			Log.warn("Unconverted attributes: " + attributes);
		}
	}

	/**
	 * Migrates the child entities from a source entity to the target entity
	 * according to the defined migration rules.
	 *
	 * @param source The source entity
	 * @param target The target entity
	 * @throws StorageException     If retrieving an entity from a storage
	 *                              fails
	 * @throws TransactionException If storing an entity fails
	 */
	protected void migrateChildren(Entity source, Entity target)
		throws StorageException, TransactionException {
		Set<RelationType<?>> childAttributes = new HashSet<RelationType<?>>(
			EntityManager.getEntityDefinition(targetType).getChildAttributes());

		for (Entry<RelationType<? extends List<? extends Entity>>,
			EntityMigrator<?, ?>> entry : childMigrators.entrySet()) {
			@SuppressWarnings("unchecked")
			RelationType<List<Entity>> targetChildAttr =
				(RelationType<List<Entity>>) entry.getKey();

			@SuppressWarnings("unchecked")
			EntityMigrator<Entity, Entity> childMigrator =
				(EntityMigrator<Entity, Entity>) entry.getValue();

			if (childMigrator != null) {
				@SuppressWarnings("unchecked")
				EntityDefinition<Entity> sourceDef =
					(EntityDefinition<Entity>) EntityManager.getEntityDefinition(
						sourceType);

				Collection<Entity> sourceChildren =
					sourceDef.getChildren(source,
						EntityManager.getEntityDefinition(
							childMigrator.sourceType));

				List<Entity> targetChildren =
					new ArrayList<Entity>(sourceChildren.size());

				for (Entity sourceChild : sourceChildren) {
					Entity targetChild =
						childMigrator.migrateEntity(sourceChild);

					if (targetChild != null) {
						targetChildren.add(targetChild);
					}
				}

				if (targetChildren.size() > 0) {
					Entity[] entities = new Entity[targetChildren.size()];

					target.addChildren(targetChildAttr,
						targetChildren.toArray(entities));
				}
			}

			childAttributes.remove(targetChildAttr);
		}

		if (childAttributes.size() > 0) {
			Log.warn("Unconverted children: " + childAttributes);
		}
	}

	/**
	 * Stores an entity in the target storage.
	 *
	 * @param entity The entity to store
	 * @throws StorageException If storing the entity fails
	 */
	protected void storeEntity(Entity entity) throws StorageException {
		Storage targetStorage = StorageManager.getStorage(targetType);

		// always store the parent to update child references if necessary
		while (entity.getParent() != null) {
			entity = entity.getParent();
		}

		try {
			targetStorage.store(entity);
			EntityManager.cacheEntity(entity);
		} finally {
			targetStorage.release();
		}
	}
}
