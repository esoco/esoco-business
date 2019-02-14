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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.obrel.core.RelationType;

import static de.esoco.entity.EntityPredicates.forEntity;


/********************************************************************
 * A utility class that provides methods to perform a migration from one entity
 * type to another.
 *
 * @author eso
 */
public class EntityMigrator<S extends Entity, T extends Entity>
	implements Function<S, T>
{
	//~ Static fields/initializers ---------------------------------------------

	private static Set<Entity> aMigratedEntities = new HashSet<Entity>();

	//~ Instance fields --------------------------------------------------------

	private final Class<S> rSourceType;
	private final Class<T> rTargetType;
	private boolean		   bVerbose = false;

	private final Map<RelationType<?>, Function<? super Entity, ?>> aAttributeRules =
		new LinkedHashMap<RelationType<?>, Function<? super Entity, ?>>();

	private final Map<RelationType<? extends List<? extends Entity>>,
					  EntityMigrator<?, ?>>							  aChildMigrators =
		new LinkedHashMap<RelationType<? extends List<? extends Entity>>,
						  EntityMigrator<?, ?>>();

	private T aTarget;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for certain source and target entity definition.
	 *
	 * @param rSourceType The source entity definition
	 * @param rTargetType The target entity definition
	 */
	public EntityMigrator(Class<S> rSourceType, Class<T> rTargetType)
	{
		this.rSourceType = rSourceType;
		this.rTargetType = rTargetType;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Invokes {@link #migrateEntity(Entity)} for the input entity and returns
	 * the result. Any occurring storage exception will be converted into a
	 * runtime exception.
	 *
	 * @see Function#evaluate(Object)
	 */
	@Override
	public T evaluate(S rSource)
	{
		try
		{
			return migrateEntity(rSource);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/***************************************
	 * Returns the class of target entity type.
	 *
	 * @return The target type class
	 */
	public Class<T> getTargetType()
	{
		return rTargetType;
	}

	/***************************************
	 * Migrates a certain set of entities according to the rules of this
	 * instance.
	 *
	 * @param  rCriteria The criteria to limit the migrated entities or NULL for
	 *                   all entities of the source type
	 *
	 * @throws Exception If the migration fails
	 */
	@SuppressWarnings("boxing")
	public void migrateEntities(Predicate<? super Entity> rCriteria)
		throws Exception
	{
		Storage rSourceStorage = StorageManager.getStorage(rSourceType);
		Storage rTargetStorage = StorageManager.getStorage(rTargetType);

		Query<S>	   aQuery    =
			rSourceStorage.query(forEntity(rSourceType, rCriteria));
		QueryResult<S> aEntities = aQuery.execute();

		int nCount = aQuery.size();
		int nStep  = nCount / 50 + 1;

		if (Log.isLevelEnabled(LogLevel.INFO) ||
			Log.isLevelEnabled(LogLevel.DEBUG) ||
			Log.isLevelEnabled(LogLevel.TRACE))
		{
			// disable progress display
			nStep = nCount + 1;
		}

		try
		{
			init();

			System.out.printf(
				"Migrating %s to %s [%d]\n",
				rSourceType,
				rTargetType,
				nCount);

			while (aEntities.hasNext())
			{
				S aSourceEntity = aEntities.next();
				T aTargetEntity = migrateEntity(aSourceEntity);

				if (aTargetEntity != null)
				{
					if (bVerbose)
					{
						System.out.printf("Migrated: \n");
						aSourceEntity.printHierarchy(System.out);
						System.out.printf("To: \n");
						aTargetEntity.printHierarchy(System.out);
					}

					aMigratedEntities.add(aTargetEntity);
					storeEntity(aTargetEntity);
					afterStore(aTargetEntity, aSourceEntity);
				}
				else if (bVerbose)
				{
					System.out.printf("Skipped: " + aSourceEntity);
				}

				if (!bVerbose && (nCount-- % nStep) == 0)
				{
					System.out.print("+");
				}
			}

			System.out.printf("\n");

			finish();
		}
		finally
		{
			try
			{
				aQuery.close();
			}
			finally
			{
				rSourceStorage.release();
				rTargetStorage.release();
			}
		}
	}

	/***************************************
	 * Migrates a single entity according to the rules of this instance. A
	 * subclass may return NULL to omit certain source entities from the
	 * migration.
	 *
	 * @param  rSource The entity to migrate
	 *
	 * @return The resulting entity of the target definition or NULL if the
	 *         entity shall not be migrated
	 *
	 * @throws StorageException     If retrieving an entity from a storage fails
	 * @throws TransactionException If the transaction for storing the target
	 *                              entity fails
	 */
	public T migrateEntity(S rSource) throws StorageException,
											 TransactionException
	{
		assert rSource.getClass() == rSourceType;

		aTarget = ReflectUtil.newInstance(rTargetType);

		migrateAttributes(rSource, aTarget);
		migrateChildren(rSource, aTarget);

		return aTarget;
	}

	/***************************************
	 * Sets a migration rule for an arbitrary entity reference attribute of the
	 * target entities. The rule argument must be a function that retrieves or
	 * generates the global string ID of the referenced entity from the source
	 * entity. If it is NULL the given target attribute will be ignored.
	 *
	 * @param rTargetAttribute The attribute of the target entity to set
	 * @param rRule            A rule that creates the ID of the referenced
	 *                         entity or NULL to ignore the attribute
	 */
	public void setArbitraryEntityReferenceRule(
		RelationType<?>					 rTargetAttribute,
		Function<? super Entity, String> rRule)
	{
		aAttributeRules.put(rTargetAttribute, rRule);
	}

	/***************************************
	 * Sets a simple copy migration rule for a particular set of source and
	 * target attributes.
	 *
	 * @param rSourceAttribute The source attribute to copy
	 * @param rTargetAttribute The target attribute to set
	 */
	public <A> void setAttributeCopyRule(
		RelationType<A> rSourceAttribute,
		RelationType<A> rTargetAttribute)
	{
		setAttributeRule(rTargetAttribute, rSourceAttribute);
	}

	/***************************************
	 * Sets a migration rule for a particular target attribute. The rule
	 * argument must be a function that retrieves or creates the target
	 * attribute value from the source entity. If it is NULL the given target
	 * attribute will be ignored.
	 *
	 * @param rTargetAttribute The attribute of the target entity to set
	 * @param rRule            A rule that creates the target attribute value or
	 *                         NULL to ignore the attribute
	 */
	public <A> void setAttributeRule(
		RelationType<A>				rTargetAttribute,
		Function<? super Entity, A> rRule)
	{
		aAttributeRules.put(rTargetAttribute, rRule);
	}

	/***************************************
	 * Sets an entity migrator that will be used for the migration of a certain
	 * child entity type of the entity that is handled by this instance. If the
	 * migrator is set to NULL the children of the given type will be ignored
	 * during the migration.
	 *
	 * @param rTargetChildAttribute The target child attribute to migrate
	 * @param rMigrator             The child migrator or NULL to ignore
	 *                              children of this type
	 */
	public void setChildMigrator(
		RelationType<? extends List<? extends Entity>> rTargetChildAttribute,
		EntityMigrator<?, ?>						   rMigrator)
	{
		aChildMigrators.put(rTargetChildAttribute, rMigrator);
	}

	/***************************************
	 * Sets a migration rule for a particular entity reference attribute of the
	 * target entities. The rule argument must be a function that retrieves or
	 * creates the integer ID of the referenced entity from the source entity.
	 * If it is NULL the given target attribute will be ignored.
	 *
	 * @param rTargetAttribute The attribute of the target entity to set
	 * @param rRule            A rule that creates the ID of the referenced
	 *                         entity or NULL to ignore the attribute
	 */
	public void setEntityReferenceRule(
		RelationType<?>					  rTargetAttribute,
		Function<? super Entity, Integer> rRule)
	{
		aAttributeRules.put(rTargetAttribute, rRule);
	}

	/***************************************
	 * Sets the target attributes to be ignored by the migration. This is a
	 * shortcut for invoking {@link #setAttributeRule(RelationType, Function)}
	 * with a rule value of NULL.
	 *
	 * @param rAttributes The target attributes to be ignored
	 */
	public void setIgnoredAttributes(RelationType<?>... rAttributes)
	{
		for (RelationType<?> rAttribute : rAttributes)
		{
			setAttributeRule(rAttribute, null);
		}
	}

	/***************************************
	 * Sets the verbose flag of this instance. If the flag is TRUE the source
	 * and target entities processed during migration will be output to the
	 * console. The default value is FALSE.
	 *
	 * @param bVerbose TRUE for verbose mode, FALSE for quiet mode
	 */
	public void setVerbose(boolean bVerbose)
	{
		this.bVerbose = bVerbose;
	}

	/***************************************
	 * Can be overridden when there is need to perform any action on the newly
	 * stored entity.
	 *
	 * @param  rTarget The migrated and stored entity
	 * @param  rSource The entity that has been migrated
	 *
	 * @throws StorageException     If retrieving an entity from a storage fails
	 * @throws TransactionException If the transaction for storing an entity
	 *                              fails
	 */
	protected void afterStore(T rTarget, S rSource) throws StorageException,
														   TransactionException
	{
	}

	/***************************************
	 * This method can be overridden by subclasses to perform cleanup operations
	 * or similar at the end of the migration. It will be invoked after all
	 * entities have been migrated successfully. The default implementation does
	 * nothing.
	 *
	 * @throws Exception If finishing fails
	 */
	protected void finish() throws Exception
	{
	}

	/***************************************
	 * This method can be overridden by subclasses to perform initialization
	 * before of the migration of entities. It will be invoked after the
	 * storages have been initialized but before any entity has been migrated.
	 * The default implementation does nothing.
	 *
	 * @throws Exception If the initialization fails
	 */
	protected void init() throws Exception
	{
	}

	/***************************************
	 * Migrates the attributes from a source entity to the target entity
	 * according to the defined migration rules.
	 *
	 * @param  rSource The source entity
	 * @param  rTarget The target entity
	 *
	 * @throws StorageException If retrieving an entity from a storage fails
	 */
	@SuppressWarnings({ "boxing", "unchecked" })
	protected void migrateAttributes(S rSource, T rTarget)
		throws StorageException
	{
		Set<RelationType<?>> aAttributes =
			new HashSet<RelationType<?>>(
				EntityManager.getEntityDefinition(rTargetType).getAttributes());

		for (Entry<RelationType<?>, Function<? super Entity, ?>> rEntry :
			 aAttributeRules.entrySet())
		{
			RelationType<?>			    rAttribute = rEntry.getKey();
			Function<? super Entity, ?> rRule	   = rEntry.getValue();

			if (rRule != null)
			{
				Object   rValue    = rRule.evaluate(rSource);
				Class<?> rAttrType = rAttribute.getTargetType();

				if (Entity.class.isAssignableFrom(rAttrType))
				{
					if (rValue instanceof String)
					{
						Entity rEntity =
							EntityManager.queryEntity((String) rValue);

						if (rEntity == null)
						{
							throw new IllegalStateException(
								String.format(
									"Could not find referenced entity %s with ID %s",
									rAttrType,
									rValue));
						}

						rValue = rEntity;
					}
					else if (rValue instanceof Integer)
					{
						int nId = ((Integer) rValue).intValue();

						if (nId > 0)
						{
							rValue =
								EntityManager.queryEntity(
									(Class<Entity>) rAttrType,
									nId);

							if (rValue == null)
							{
								throw new IllegalStateException(
									String.format(
										"Could not find referenced entity %s with ID %d",
										rAttrType,
										nId));
							}
						}
						else
						{
							rValue = null;
						}
					}
				}

				rTarget.set((RelationType<Object>) rAttribute, rValue);
			}

			aAttributes.remove(rAttribute);
		}

		if (aAttributes.size() > 0)
		{
			Log.warn("Unconverted attributes: " + aAttributes);
		}
	}

	/***************************************
	 * Migrates the child entities from a source entity to the target entity
	 * according to the defined migration rules.
	 *
	 * @param  rSource The source entity
	 * @param  rTarget The target entity
	 *
	 * @throws StorageException     If retrieving an entity from a storage fails
	 * @throws TransactionException If storing an entity fails
	 */
	protected void migrateChildren(Entity rSource, Entity rTarget)
		throws StorageException, TransactionException
	{
		Set<RelationType<?>> aChildAttributes =
			new HashSet<RelationType<?>>(
				EntityManager.getEntityDefinition(rTargetType)
				.getChildAttributes());

		for (Entry<RelationType<? extends List<? extends Entity>>,
				   EntityMigrator<?, ?>> rEntry : aChildMigrators.entrySet())
		{
			@SuppressWarnings("unchecked")
			RelationType<List<Entity>> rTargetChildAttr =
				(RelationType<List<Entity>>) rEntry.getKey();

			@SuppressWarnings("unchecked")
			EntityMigrator<Entity, Entity> rChildMigrator =
				(EntityMigrator<Entity, Entity>) rEntry.getValue();

			if (rChildMigrator != null)
			{
				@SuppressWarnings("unchecked")
				EntityDefinition<Entity> rSourceDef =
					(EntityDefinition<Entity>) EntityManager
					.getEntityDefinition(rSourceType);

				Collection<Entity> rSourceChildren =
					rSourceDef.getChildren(
						rSource,
						EntityManager.getEntityDefinition(
							rChildMigrator.rSourceType));

				List<Entity> aTargetChildren =
					new ArrayList<Entity>(rSourceChildren.size());

				for (Entity rSourceChild : rSourceChildren)
				{
					Entity aTargetChild =
						rChildMigrator.migrateEntity(rSourceChild);

					if (aTargetChild != null)
					{
						aTargetChildren.add(aTargetChild);
					}
				}

				if (aTargetChildren.size() > 0)
				{
					Entity[] aEntities = new Entity[aTargetChildren.size()];

					rTarget.addChildren(
						rTargetChildAttr,
						aTargetChildren.toArray(aEntities));
				}
			}

			aChildAttributes.remove(rTargetChildAttr);
		}

		if (aChildAttributes.size() > 0)
		{
			Log.warn("Unconverted children: " + aChildAttributes);
		}
	}

	/***************************************
	 * Stores an entity in the target storage.
	 *
	 * @param  rEntity The entity to store
	 *
	 * @throws StorageException If storing the entity fails
	 */
	protected void storeEntity(Entity rEntity) throws StorageException
	{
		Storage rTargetStorage = StorageManager.getStorage(rTargetType);

		// always store the parent to update child references if necessary
		while (rEntity.getParent() != null)
		{
			rEntity = rEntity.getParent();
		}

		try
		{
			rTargetStorage.store(rEntity);
			EntityManager.cacheEntity(rEntity);
		}
		finally
		{
			rTargetStorage.release();
		}
	}
}
