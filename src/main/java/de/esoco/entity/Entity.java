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

import de.esoco.entity.EntityDefinition.DisplayMode;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.json.JsonUtil;
import de.esoco.lib.property.MutableProperties;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.StringProperties;
import de.esoco.lib.text.TextConvert;

import de.esoco.storage.AfterStoreHandler;
import de.esoco.storage.StorageException;

import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.obrel.core.IntermediateRelation;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.SerializableRelatedObject;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import static de.esoco.entity.EntityRelationTypes.CACHE_ENTITY;
import static de.esoco.entity.EntityRelationTypes.DEPENDENT_STORE_ENTITIES;
import static de.esoco.entity.EntityRelationTypes.ENTITY_STORE_ORIGIN;
import static de.esoco.entity.EntityRelationTypes.EXTRA_ATTRIBUTES_MODIFIED;
import static de.esoco.entity.EntityRelationTypes.EXTRA_ATTRIBUTES_READ;
import static de.esoco.entity.EntityRelationTypes.EXTRA_ATTRIBUTE_MAP;
import static de.esoco.entity.EntityRelationTypes.REMOVED_CHILDREN;

import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.notNull;

import static de.esoco.storage.StorageRelationTypes.PERSISTENT;
import static de.esoco.storage.StorageRelationTypes.STORAGE_MAPPING;

import static org.obrel.type.MetaTypes.INITIALIZING;
import static org.obrel.type.MetaTypes.MODIFIED;
import static org.obrel.type.StandardTypes.PREVIOUS_VALUE;
import static org.obrel.type.StandardTypes.RELATION_LISTENERS;


/********************************************************************
 * The base class for persistent entities that are created from entity
 * definitions. It can either be used directly or subclassed for entity classes
 * that want to provide additional methods. But all standard attributes from the
 * definition can be accessed directly with the {@link Relatable} methods.
 *
 * <p>Besides the standard attributes from the entity definition an entity can
 * have arbitrary extra attributes which will only be stored if they are set on
 * an entity. These extra attributes can be set and retrieved with the methods
 * {@link #getExtraAttribute(RelationType, Object)} and {@link
 * #setExtraAttribute(RelationType, Object)}.</p>
 *
 * <p>Extra attribute relation types must be created through the factory methods
 * in the class {@link ExtraAttributes} because any other kind of relation type
 * in an entity will be recognized as a standard attribute when analyzed by
 * {@link EntityDefinition}.</p>
 *
 * @author eso
 */
public class Entity extends SerializableRelatedObject
	implements AfterStoreHandler
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	static
	{
		Conversions.registerStringConversion(Entity.class,
											 EntityFunctions.entityToString());
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Initialize this instance.
	 */
	public Entity()
	{
		// modified flag must exist to indicate the modification tracking
		// support to the storage framework, therefore set to FALSE
		set(MODIFIED, Boolean.FALSE);
		get(RELATION_LISTENERS).add(getDefinition());
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Convenience method to set a boolean display property.
	 *
	 * @see #setAttributeDisplayProperty(Class, PropertyName, Object,
	 *      RelationType...)
	 */
	@SuppressWarnings("boxing")
	public static void setAttributeDisplayFlag(
		Class<? extends Entity> rEntityClass,
		PropertyName<Boolean>   rProperty,
		RelationType<?>... 		rAttributes)
	{
		setAttributeDisplayProperty(rEntityClass, rProperty, true, rAttributes);
	}

	/***************************************
	 * Convenience method to set an integer display property.
	 *
	 * @see #setAttributeDisplayProperty(Class, PropertyName, Object,
	 *      RelationType...)
	 */
	@SuppressWarnings("boxing")
	public static void setAttributeDisplayProperty(
		Class<? extends Entity> rEntityClass,
		int						nValue,
		PropertyName<Integer>   rProperty,
		RelationType<?>... 		rAttributes)
	{
		setAttributeDisplayProperty(rEntityClass,
									rProperty,
									nValue,
									rAttributes);
	}

	/***************************************
	 * Helper method for subclasses to set a display property on attributes. If
	 * the attribute doesn't have display attributes yet they will be created
	 * and stored in the {@link EntityRelationTypes#DISPLAY_PROPERTIES}
	 * relation.
	 *
	 * <p>ATTENTION: this setting affects all occurrences of the given
	 * attribute. Properties of attributes that are re-used in different
	 * contexts (e.g. relation types defined in {@link StandardTypes} or
	 * similar) should be set with a {@link
	 * EntityDefinition#ATTRIBUTE_DISPLAY_PROPERTIES_FIELD} instead to prevent
	 * side effects.</p>
	 *
	 * @param rEntityClass The entity class for which to set the property
	 * @param rProperty    The property to set
	 * @param rValue       The property values
	 * @param rAttributes  The attributes to set the display property on
	 */
	public static <T> void setAttributeDisplayProperty(
		Class<? extends Entity> rEntityClass,
		PropertyName<T>			rProperty,
		T						rValue,
		RelationType<?>... 		rAttributes)
	{
		Map<RelationType<?>, MutableProperties> rDisplayPropertiesMap =
			EntityDefinition.getAttributeDisplayProperties(rEntityClass);

		for (RelationType<?> rAttribute : rAttributes)
		{
			MutableProperties rProperties =
				rDisplayPropertiesMap.get(rAttribute);

			if (rProperties == null)
			{
				rProperties = new StringProperties();
				rDisplayPropertiesMap.put(rAttribute, rProperties);
			}

			rProperties.setProperty(rProperty, rValue);
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * A convenience method to add a single child to an entity. Invokes the
	 * method {@link #addChildren(RelationType, Entity...)} .
	 *
	 * @param  rChildAttr The attribute that references the child type
	 * @param  rChild     The child object to add
	 *
	 * @throws IllegalArgumentException If one of the parameters is invalid or
	 *                                  if the child is already stored in this
	 *                                  entity
	 */
	@SuppressWarnings("unchecked")
	public final <C extends Entity> void addChild(
		RelationType<List<C>> rChildAttr,
		C					  rChild)
	{
		addChildren(rChildAttr, rChild);
	}

	/***************************************
	 * A convenience method to add variable number of children to an entity.
	 * Invokes the method {@link #addChildren(RelationType, Collection)}.
	 *
	 * @param  rChildAttr The attribute that references the child type
	 * @param  rChildren  The new child objects to add
	 *
	 * @throws IllegalArgumentException If one of the parameters is invalid or
	 *                                  if one of the children is already stored
	 *                                  in this entity
	 */
	@SuppressWarnings("unchecked")
	public final <C extends Entity> void addChildren(
		RelationType<List<C>> rChildAttr,
		C... 				  rChildren)
	{
		addChildren(rChildAttr, Arrays.asList(rChildren));
	}

	/***************************************
	 * Adds child entities to this entity. This will set the parent references
	 * in the children to this instance. If a child already exists in this
	 * entity an exception will be thrown. It is recommended that entity
	 * children are always added through this method unless an entity subclass
	 * implements (and documents) more specific methods for the management of
	 * child entities.
	 *
	 * @param  rChildAttr The attribute that references the child type
	 * @param  rChildren  The new child objects to add to this entity
	 *
	 * @throws IllegalArgumentException If one of the parameters is invalid or
	 *                                  if one of the children is already stored
	 *                                  in this entity
	 */
	public <C extends Entity> void addChildren(
		RelationType<List<C>> rChildAttr,
		List<C>				  rChildren)
	{
		if (rChildAttr == null || rChildren == null || rChildren.size() == 0)
		{
			throw new IllegalArgumentException("Arguments must not be NULL or empty");
		}

		boolean bInitializing = hasFlag(INITIALIZING);

		// mark as modified to perform child count update in storage framework
		// do before child list modification to allow the modification tracking
		// in EntityDefinition to prohibit the change by throwing an exception
		if (!bInitializing)
		{
			set(MODIFIED);
		}

		@SuppressWarnings("unchecked")
		EntityDefinition<Entity> rDef =
			(EntityDefinition<Entity>) getDefinition();

		EntityDefinition<?> rChildDef =
			(EntityDefinition<?>) rChildAttr.get(STORAGE_MAPPING);

		rDef.initChildren(this, rChildren, rChildDef, bInitializing);
		get(rChildAttr).addAll(rChildren);
	}

	/***************************************
	 * Implemented to store all new and modified extra attributes.
	 *
	 * @see AfterStoreHandler#afterStore()
	 */
	@Override
	public void afterStore() throws Exception
	{
		if (hasFlag(EXTRA_ATTRIBUTES_MODIFIED))
		{
			Entity rStoreOrigin = getUpwards(ENTITY_STORE_ORIGIN);

			for (ExtraAttribute rExtraAttribute :
				 get(EXTRA_ATTRIBUTE_MAP).values())
			{
				if (rExtraAttribute.hasFlag(MODIFIED))
				{
					EntityManager.storeEntity(rExtraAttribute, rStoreOrigin);
				}
			}

			set(EXTRA_ATTRIBUTES_MODIFIED, Boolean.FALSE);
		}
	}

	/***************************************
	 * A method that appends a string description of a certain attribute to a
	 * {@link StringBuilder}. The attribute description will appended as defined
	 * by the given format string which must contain two %s references. The
	 * first one will be replaced by the attribute name after converting it with
	 * the method {@link TextConvert#capitalize(String, String)} and the second
	 * is replaced with the attribute value {@link Object#toString()}. NULL
	 * values will only be included if the boolean parameter is TRUE. Otherwise
	 * no attribute description will be appended at all.
	 *
	 * @param rStringBuilder The string builder to append the attribute
	 *                       description to
	 * @param rAttribute     The relation type of the attribute to append
	 * @param sFormat        The format string for the formatting of the
	 *                       attribute name and value
	 * @param bIncludeNull   TRUE to include NULL values, FALSE to omit them
	 */
	public void appendAttribute(StringBuilder   rStringBuilder,
								RelationType<?> rAttribute,
								String			sFormat,
								boolean			bIncludeNull)
	{
		Object rValue = get(rAttribute);

		if (bIncludeNull || rValue != null)
		{
			String sName =
				TextConvert.capitalize(TextConvert.lastElementOf(rAttribute
																 .getName()),
									   " ");

			if (rValue == null)
			{
				rValue = "NULL";
			}

			rStringBuilder.append(String.format(sFormat, sName, rValue));
		}
	}

	/***************************************
	 * Creates a string for the attributes of a certain display mode.
	 *
	 * @param  eDisplayMode The display mode
	 * @param  sSeparator   The separator string between attributes
	 *
	 * @return The resulting string
	 */
	public String attributeString(DisplayMode eDisplayMode, String sSeparator)
	{
		return attributeString(getDefinition().getDisplayAttributes(eDisplayMode),
							   sSeparator);
	}

	/***************************************
	 * Creates a string for a certain list of entity attributes. NULL values
	 * will be omitted from the output.
	 *
	 * @param  rAttributes The attribute list
	 * @param  sSeparator  The separator string between attributes
	 *
	 * @return The resulting string
	 */
	public String attributeString(
		List<RelationType<?>> rAttributes,
		String				  sSeparator)
	{
		StringBuilder aResult = new StringBuilder();

		for (RelationType<?> rAttribute : rAttributes)
		{
			String sValue = getAttributeValue(rAttribute);

			if (sValue != null)
			{
				aResult.append(sValue).append(sSeparator);
			}
			else
			{
				aResult.append(sSeparator);
			}
		}

		if (aResult.length() > 0)
		{
			aResult.setLength(aResult.length() - sSeparator.length());
		}

		return aResult.toString();
	}

	/***************************************
	 * This method can be overridden by subclasses to check if the instance's
	 * hierarchy needs an update. In such a case the method should return the
	 * topmost parent of the hierarchy which needs to be stored to make all
	 * changes persistent. Otherwise it should return itself to continue with
	 * the standard update. The default implementation returns the topmost
	 * parent entity in this entity's hierarchy that has been marked with the
	 * {@link MetaTypes#MODIFIED} flag.
	 *
	 * @return The topmost entity in the hierarchy to update or THIS
	 */
	public Entity checkForHierarchyUpdate()
	{
		Entity rParent		 = getParent();
		Entity rUpdateEntity = this;

		if (rParent != null && rParent.hasFlag(MODIFIED))
		{
			rUpdateEntity = rParent.checkForHierarchyUpdate();
		}

		return rUpdateEntity;
	}

	/***************************************
	 * Collects all children of a certain type in the hierarchy of this entity
	 * that match a certain predicate.
	 *
	 * @param  rChildAttribute The child attribute to descend the hierarchy at
	 * @param  pCriteria       The predicate to evaluate the child entities with
	 *
	 * @return A new list containing the resulting child entities (may be empty
	 *         but will never be NULL)
	 */
	public <E extends Entity> List<E> collectDownwards(
		RelationType<List<E>> rChildAttribute,
		Predicate<? super E>  pCriteria)
	{
		return EntityManager.collectDownwards(get(rChildAttribute),
											  rChildAttribute,
											  pCriteria);
	}

	/***************************************
	 * Creates a description string for the modified values of this entity and
	 * it's hierarchy. Value changes are recorded by the entity modification
	 * tracking with annotations of type {@link StandardTypes#PREVIOUS_VALUE}.
	 * <<<<<<< HEAD @return The change description string =======
	 *
	 * @return The resulting string >>>>>>> refs/heads/develop
	 */
	public String createChangeDescription()
	{
		return createChangeDescription("");
	}

	/***************************************
	 * Overridden to check whether this entity is equal to another entity.
	 *
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object rObject)
	{
		if (this == rObject)
		{
			return true;
		}

		if (rObject == null || getClass() != rObject.getClass())
		{
			return false;
		}

		Entity rOther = (Entity) rObject;
		int    nId    = getId();

		boolean bModified	   = hasFlag(MODIFIED);
		boolean bOtherModified = rOther.hasFlag(MODIFIED);

		if ((!bModified && bOtherModified) || (bModified && !bOtherModified))
		{
			return false;
		}
		else if (nId != 0 && !(bModified || bOtherModified))
		{
			return nId == rOther.getId();
		}
		else
		{
			return attributesEqual(rOther);
		}
	}

	/***************************************
	 * Searches for a child entity in a hierarchy of children that matches a
	 * certain predicate. If the given children are not hierarchical only the
	 * flat list of children will be searched. This method is based on the
	 * {@link EntityManager#findDownwards(List, RelationTypes, Predicate)}
	 * method.
	 *
	 * @param  rChildAttribute The child attribute to search and descend
	 *                         hierarchically
	 * @param  pCriteria       The predicate to evaluate the children with
	 *
	 * @return The first matching child or NULL if none could be found
	 */
	public <E extends Entity> E findDownwards(
		RelationType<List<E>> rChildAttribute,
		Predicate<? super E>  pCriteria)
	{
		return EntityManager.findDownwards(get(rChildAttribute),
										   rChildAttribute,
										   pCriteria);
	}

	/***************************************
	 * Invokes {@link EntityFunctions#format(Entity)} on this instance.
	 *
	 * @return A formatted string representation of this entity
	 */
	public String format()
	{
		return EntityFunctions.format(this);
	}

	/***************************************
	 * Returns a child that is valid on a certain date. The child must be a
	 * subclass of {@link PeriodEntity} to support the date check.
	 *
	 * @param  rChildAttr     The child attribute to search for
	 * @param  rDate          The date to return the child for
	 * @param  pExtraCriteria An optional predicate to check valid children with
	 *                        or NULL for none
	 * @param  bUpwards       TRUE to look upwards in the hierarchy if not found
	 *                        in this entity
	 *
	 * @return The matching child or NULL if none could be found
	 */
	public <C extends PeriodEntity> C getChild(
		RelationType<List<C>> rChildAttr,
		Date				  rDate,
		Predicate<? super C>  pExtraCriteria,
		boolean				  bUpwards)
	{
		C rResult = null;

		for (C rChild : get(rChildAttr))
		{
			if (rChild.isValidOn(rDate) &&
				(pExtraCriteria == null ||
				 pExtraCriteria.evaluate(rChild) == Boolean.TRUE))
			{
				rResult = rChild;

				break;
			}
		}

		if (rResult == null && bUpwards)
		{
			RelationType<? extends Entity> rParentAttr =
				getDefinition().getParentAttribute(getDefinition());

			if (rParentAttr != null)
			{
				Entity rParent = get(rParentAttr);

				if (rParent != null)
				{
					rResult =
						rParent.getChild(rChildAttr,
										 rDate,
										 pExtraCriteria,
										 bUpwards);
				}
			}
		}

		return rResult;
	}

	/***************************************
	 * Returns the hierarchy of all or certain children of this entity. The
	 * returned list will contain all of this entity's direct and indirect
	 * children of the given attribute. If the predicate parameter is not NULL
	 * only children will be returned of which the predicate evaluation yields
	 * TRUE. If the evaluation of a certain child is FALSE it's own children
	 * will still be evaluated.
	 *
	 * <p>The returned list will never be NULL and can be manipulated freely.
	 * </p>
	 *
	 * @param  rChildAttribute The attribute of the children to return
	 * @param  pCriteria       The optional criteria or NULL for all children
	 *
	 * @return The list of children
	 */
	@SuppressWarnings("boxing")
	public <C extends Entity> List<C> getChildHierarchy(
		RelationType<List<C>> rChildAttribute,
		Predicate<? super C>  pCriteria)
	{
		List<C> rChildren  = get(rChildAttribute);
		List<C> aHierarchy = new ArrayList<C>(rChildren.size());

		for (C rChild : rChildren)
		{
			if (pCriteria != null && pCriteria.evaluate(rChild))
			{
				aHierarchy.add(rChild);
			}

			aHierarchy.addAll(rChild.getChildHierarchy(rChildAttribute,
													   pCriteria));
		}

		return aHierarchy;
	}

	/***************************************
	 * Returns the entity definition for this entity class.
	 *
	 * @return The entity definition
	 */
	public EntityDefinition<?> getDefinition()
	{
		return EntityManager.getEntityDefinition(getClass());
	}

	/***************************************
	 * Returns the value of a certain extra attribute of this entity.
	 *
	 * @param  rKey          The key that identifies the attribute
	 * @param  rDefaultValue The default value to return if no extra attribute
	 *                       with the given key exists
	 *
	 * @return The attribute value or the default value if the attribute is not
	 *         set
	 *
	 * @throws StorageException If retrieving the extra attributes of this
	 *                          entity fails
	 */
	@SuppressWarnings("unchecked")
	public <T> T getExtraAttribute(RelationType<T> rKey, T rDefaultValue)
		throws StorageException
	{
		ExtraAttribute rExtraAttribute =
			getExtraAttributeMap().get(rKey.getSimpleName());

		T rValue = rDefaultValue;

		if (rExtraAttribute != null)
		{
			rValue = (T) rExtraAttribute.get(ExtraAttribute.VALUE);
		}

		return rValue;
	}

	/***************************************
	 * Returns the value of an extra attribute of this entity for a certain
	 * owner entity.
	 *
	 * @param  rOwner        The owner entity of the extra attribute
	 * @param  rKey          The key that identifies the attribute
	 * @param  rDefaultValue The default value to return if no extra attribute
	 *                       with the given key exists
	 * @param  bFallback     TRUE to return the default extra attribute if no
	 *                       attribute for the given owner exists; FALSE to just
	 *                       return the default value
	 *
	 * @return The attribute value or the default value if the attribute is not
	 *         set
	 *
	 * @throws StorageException If querying the extra attribute fails
	 */
	@SuppressWarnings("unchecked")
	public <T> T getExtraAttributeFor(Entity		  rOwner,
									  RelationType<T> rKey,
									  T				  rDefaultValue,
									  boolean		  bFallback)
		throws StorageException
	{
		ExtraAttribute rExtraAttribute = queryExtraAttributeFor(rOwner, rKey);

		T rValue = rDefaultValue;

		if (rExtraAttribute != null)
		{
			rValue = (T) rExtraAttribute.get(ExtraAttribute.VALUE);
		}
		else if (bFallback)
		{
			rValue = getExtraAttribute(rKey, rValue);
		}

		return rValue;
	}

	/***************************************
	 * Returns the keys of the extra attributes that are set on this entity. The
	 * values of the extra attribute can then be queried through the method
	 * {@link #getExtraAttribute(RelationType, Object)}. The returned collection
	 * can be modified freely by the caller.
	 *
	 * @return A collection of the current extra attribute keys
	 *
	 * @throws StorageException If retrieving the extra attributes of this
	 *                          entity fails
	 */
	public Collection<RelationType<?>> getExtraAttributes()
		throws StorageException
	{
		return CollectionUtil.map(getExtraAttributeMap().values(),
								  ExtraAttribute.KEY);
	}

	/***************************************
	 * Returns an extra attribute value from the hierarchy of this entity. If
	 * the value of the given relation type is NULL in this entity it will be
	 * looked up recursively in the parent of the same type (i.e. with the same
	 * entity definition) if such exists.
	 *
	 * @param  rTypedKey The extra attribute key
	 *
	 * @return The extra attribute value
	 *
	 * @throws StorageException If retrieving the extra attributes of this
	 *                          entity fails
	 */
	public <T> T getExtraAttributeUpwards(RelationType<T> rTypedKey)
		throws StorageException
	{
		T rValue = getExtraAttribute(rTypedKey, null);

		if (rValue == null)
		{
			Entity rParent = getParent();

			if ((rParent != null) && (rParent.getClass() == getClass()))
			{
				rValue = rParent.getExtraAttributeUpwards(rTypedKey);
			}
		}

		return rValue;
	}

	/***************************************
	 * Returns the global ID of this entity.
	 *
	 * @see EntityManager#getGlobalEntityId(Entity)
	 */
	public String getGlobalId()
	{
		return EntityManager.getGlobalEntityId(this);
	}

	/***************************************
	 * Returns the ID of this entity.
	 *
	 * @return The entity ID
	 */
	@SuppressWarnings("boxing")
	public int getId()
	{
		return get(getIdAttribute());
	}

	/***************************************
	 * Returns the ID attribute of this entity.
	 *
	 * @return The ID attribute
	 */
	public RelationType<Integer> getIdAttribute()
	{
		return getDefinition().getIdAttribute();
	}

	/***************************************
	 * Returns the parent entity of this entity if available. This will either
	 * be the hierarchy parent or, if it is NULL and a master attribute is
	 * present, the master entity.
	 *
	 * @return The parent entity or NULL if this entity has no parent or if none
	 *         is set
	 */
	public Entity getParent()
	{
		return getDefinition().getParent(this);
	}

	/***************************************
	 * Returns the root parent of this entity's hierarchy. This will either be
	 * the topmost parent of the same entity type or, if a master attribute is
	 * present, the master entity.
	 *
	 * @return The root parent of this entity
	 */
	public Entity getRoot()
	{
		Entity rRoot = this;

		while (rRoot.getParent() != null)
		{
			rRoot = rRoot.getParent();
		}

		return rRoot;
	}

	/***************************************
	 * Returns the type attribute of this entity.
	 *
	 * @return The type attribute
	 */
	public RelationType<Enum<?>> getTypeAttribute()
	{
		return getDefinition().getTypeAttribute();
	}

	/***************************************
	 * Returns an attribute value from the hierarchy of this entity. If the
	 * value of the given relation type is NULL in this entity it will be looked
	 * up recursively in the parent of the same type (i.e. with the same entity
	 * definition) if such exists.
	 *
	 * @param  rType The relation type of the attribute to return the value of
	 *
	 * @return The attribute value (NULL if not set in the hierarchy)
	 */
	public <T> T getUpwards(RelationType<T> rType)
	{
		T rValue = get(rType);

		if (rValue == null)
		{
			Entity rParent = getParent();

			if (rParent != null && rParent.getClass() == getClass())
			{
				rValue = rParent.getUpwards(rType);
			}
		}

		return rValue;
	}

	/***************************************
	 * Checks whether a certain extra attribute has been set on this entity.
	 *
	 * @param  rKey The key that identifies the attribute
	 *
	 * @return TRUE if the attribute exists
	 *
	 * @throws StorageException If retrieving the extra attributes of this
	 *                          entity fails
	 */
	public boolean hasExtraAttribute(RelationType<?> rKey)
		throws StorageException
	{
		return getExtraAttributeMap().containsKey(rKey.getSimpleName());
	}

	/***************************************
	 * A convenience method to check a boolean extra attribute.
	 *
	 * @param  rKey The key of the boolean extra attribute
	 *
	 * @return TRUE only if the extra attribute exists and has the value TRUE
	 *
	 * @throws StorageException If reading the extra attributes fails
	 */
	@SuppressWarnings("boxing")
	public boolean hasExtraAttributeFlag(RelationType<Boolean> rKey)
		throws StorageException
	{
		return getExtraAttribute(rKey, false);
	}

	/***************************************
	 * Returns a hash code that is consistent with the {@link #equals(Object)}
	 * implementation.
	 *
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int nHashCode = 37 * getClass().hashCode();

		if (hasFlag(MODIFIED))
		{
			nHashCode = 37 * nHashCode + attributesHashCode();
		}
		else
		{
			nHashCode = 37 * nHashCode + getId();
		}

		return nHashCode;
	}

	/***************************************
	 * Checks whether this entity is currently cached.
	 *
	 * @return TRUE if this instance is cached
	 */
	public final boolean isCached()
	{
		return hasFlag(CACHE_ENTITY);
	}

	/***************************************
	 * Checks whether this entity is part of a certain hierarchy. That is the
	 * case if this entity is either a the same as the given parent or a
	 * descendant of it.
	 *
	 * @param  rHierarchyParent The hierarchy parent to check this entity
	 *                          against
	 *
	 * @return TRUE if this entity is a part of the given entity's hierarchy
	 */
	public boolean isHierarchyElement(Entity rHierarchyParent)
	{
		Entity rEntity = this;

		while (rEntity != null && rEntity.getId() != rHierarchyParent.getId())
		{
			rEntity = rEntity.getParent();
		}

		return rEntity != null;
	}

	/***************************************
	 * Checks whether this entity is already persistent.
	 *
	 * @return TRUE if this instance is persistent
	 */
	public final boolean isModified()
	{
		return hasFlag(MODIFIED);
	}

	/***************************************
	 * Checks whether this entity is already persistent.
	 *
	 * @return TRUE if this instance is persistent
	 */
	public final boolean isPersistent()
	{
		return hasFlag(PERSISTENT);
	}

	/***************************************
	 * Checks whether this entity is the root in a hierarchy.
	 *
	 * @return TRUE if this entity is the hierarchy root
	 */
	public boolean isRoot()
	{
		EntityDefinition<?> rDefinition = getDefinition();
		RelationType<?>     rParentAttr = rDefinition.getParentAttribute();
		RelationType<?>     rMasterAttr = rDefinition.getMasterAttribute();

		return (rParentAttr == null ||
				getAttributeValue(rParentAttr) == null) &&
			   (rMasterAttr == null ||
				getAttributeValue(rMasterAttr) == null);
	}

	/***************************************
	 * Prints this entity and the hierarchy of it's children to a print stream.
	 * This method is intended to be used for debugging and informational
	 * purposes only. The format of the output may change any time.
	 *
	 * @param rOut The stream to print to
	 */
	public void printHierarchy(PrintStream rOut)
	{
		printHierarchy(rOut, null);
	}

	/***************************************
	 * A convenience method that removes all children of a certain type and
	 * handles the necessary defensive copying of the child list.
	 *
	 * @param rChildAttr The child attribute to remove all children of
	 */
	public <C extends Entity> void removeAllChildren(
		RelationType<List<C>> rChildAttr)
	{
		List<C> rChildren = get(rChildAttr);

		if (rChildren != null && rChildren.size() > 0)
		{
			removeChildren(rChildAttr, new ArrayList<>(rChildren));
		}
	}

	/***************************************
	 * Convenience method to remove a single child from this entity.
	 *
	 * @see #removeChildren(RelationType, List)
	 */
	public <C extends Entity> void removeChild(
		RelationType<List<C>> rChildAttr,
		C					  rChild)
	{
		removeChildren(rChildAttr, Arrays.asList(rChild));
	}

	/***************************************
	 * Removes child entities from this entity. This will set the parent
	 * references in the children to NULL. It is recommended that entity
	 * children are always removed through this method unless an entity subclass
	 * implements (and documents) more specific methods for the management of
	 * child entities. ATTENTION: it is important that the children to be
	 * removed are either actual children of this entity or are equals according
	 * to the implementation of {@link #equals(Object)}.
	 *
	 * <p>To perform an update of the detached children that are no longer
	 * contained in this instances list of children they will be added to the
	 * relation {@link EntityRelationTypes#DEPENDENT_STORE_ENTITIES} so that
	 * they will be updated when this entity is stored.</p>
	 *
	 * @param  rChildAttr The attribute that references the child type
	 * @param  rChildren  The new child objects to add to this entity
	 *
	 * @throws IllegalArgumentException If one of the parameters is invalid or
	 *                                  if one of the children is already stored
	 *                                  in this entity
	 */
	public <C extends Entity> void removeChildren(
		RelationType<List<C>> rChildAttr,
		List<C>				  rChildren)
	{
		if (rChildAttr == null || rChildren == null || rChildren.size() == 0)
		{
			throw new IllegalArgumentException("Arguments must not be NULL or empty");
		}

		// mark as modified to perform child count update in storage framework
		// do before child list modification to allow the modification tracking
		// in EntityDefinition to prohibit the change by throwing an exception
		if (!hasFlag(INITIALIZING))
		{
			set(MODIFIED);
		}

		@SuppressWarnings("unchecked")
		EntityDefinition<Entity> rDef =
			(EntityDefinition<Entity>) getDefinition();

		rDef.detachChildren(this, rChildAttr, rChildren);
		get(rChildAttr).removeAll(rChildren);
		get(DEPENDENT_STORE_ENTITIES).addAll(rChildren);
		getRelation(rChildAttr).get(REMOVED_CHILDREN).addAll(rChildren);
	}

	/***************************************
	 * Stores an extra attribute of this entity. If an attribute with the given
	 * key already exists it will be updated, otherwise a new attribute will be
	 * created.
	 *
	 * @param  rKey   The key that identifies the attribute
	 * @param  rValue The value of the new attribute
	 *
	 * @throws StorageException If retrieving the extra attributes of this
	 *                          entity fails
	 */
	public <T> void setExtraAttribute(RelationType<T> rKey, T rValue)
		throws StorageException
	{
		Map<String, ExtraAttribute> rAttributes = getExtraAttributeMap();

		ExtraAttribute rExtraAttribute = rAttributes.get(rKey.getSimpleName());

		if (rExtraAttribute == null && rValue != null)
		{
			rExtraAttribute = new ExtraAttribute();

			rExtraAttribute.set(ExtraAttribute.ENTITY, this);
			rExtraAttribute.set(ExtraAttribute.KEY, rKey);
		}

		if (rExtraAttribute != null)
		{
			// set as modified before extra attribute is updated to allow the
			// modification tracking in EntityDefinition to prohibit the change
			// by throwing an exception
			set(MODIFIED);

			// always mark attribute as modified even if value is detected as
			// unmodified by the framework to ensure the saving of mutable
			// values like collections
			rExtraAttribute.set(MODIFIED);
			set(EXTRA_ATTRIBUTES_MODIFIED);

			rExtraAttribute.set(ExtraAttribute.VALUE, rValue);
			rAttributes.put(rKey.getSimpleName(), rExtraAttribute);
		}
	}

	/***************************************
	 * Sets an extra attribute for this entity with a certain owner entity.
	 * Other than {@link #setExtraAttribute(RelationType, Object)} this method
	 * directly stores the new or modified extra attribute to avoid the need for
	 * caching. Caching is not necessary because owner-specific extra attributes
	 * are typically used less often than standard extra attributes.
	 *
	 * @param  rOwner        The owner of the extra attribute
	 * @param  rKey          The key that identifies the attribute
	 * @param  rValue        The value of the extra attribute
	 * @param  rChangeOrigin The entity that caused this change
	 *
	 * @throws StorageException If querying for an existing extra attribute
	 *                          fails
	 */
	public <T> void setExtraAttributeFor(Entity			 rOwner,
										 RelationType<T> rKey,
										 T				 rValue,
										 Entity			 rChangeOrigin)
		throws StorageException
	{
		ExtraAttribute rExtraAttribute			    = null;
		boolean		   bAddToDependentStoreEntities = true;

		// search for an existing extra attribute that hasn't been stored yet
		if (hasRelation(DEPENDENT_STORE_ENTITIES))
		{
			for (Entity rEntity : get(DEPENDENT_STORE_ENTITIES))
			{
				if (rEntity instanceof ExtraAttribute &&
					rEntity.get(ExtraAttribute.KEY) == rKey &&
					rEntity.get(ExtraAttribute.OWNER) == rOwner)
				{
					assert rEntity.get(ExtraAttribute.ENTITY) == this;

					rExtraAttribute				 = (ExtraAttribute) rEntity;
					bAddToDependentStoreEntities = false;

					break;
				}
			}
		}

		if (rExtraAttribute == null)
		{
			rExtraAttribute = queryExtraAttributeFor(rOwner, rKey);
		}

		if (rExtraAttribute == null && rValue != null)
		{
			rExtraAttribute = new ExtraAttribute();

			rExtraAttribute.set(ExtraAttribute.ENTITY, this);
			rExtraAttribute.set(ExtraAttribute.OWNER, rOwner);
			rExtraAttribute.set(ExtraAttribute.KEY, rKey);
		}

		if (rExtraAttribute != null)
		{
			// always mark as modified to ensure the saving of mutable values
			rExtraAttribute.set(MODIFIED);
			rExtraAttribute.set(ExtraAttribute.VALUE, rValue);

			if (bAddToDependentStoreEntities)
			{
				get(DEPENDENT_STORE_ENTITIES).add(rExtraAttribute);
			}
		}
	}

	/***************************************
	 * Sets a certain attribute value in all entities of this entity's
	 * hierarchy, including this parent entity. If a criteria predicate is given
	 * only the entities for which the predicate evaluation yields TRUE will
	 * have the attribute value set.
	 *
	 * @param rAttribute The attribute to set
	 * @param rValue     The attribute value
	 * @param pCriteria  Optional criteria for the entities affected or NULL for
	 *                   all
	 */
	@SuppressWarnings("boxing")
	public <T> void setHierarchical(RelationType<T>			  rAttribute,
									T						  rValue,
									Predicate<? super Entity> pCriteria)
	{
		if (pCriteria == null || pCriteria.evaluate(this))
		{
			set(rAttribute, rValue);
		}

		RelationType<List<Entity>> rChildAttr =
			getDefinition().getHierarchyChildAttribute();

		if (rChildAttr != null)
		{
			for (Entity rChild : get(rChildAttr))
			{
				rChild.setHierarchical(rAttribute, rValue, pCriteria);
			}
		}
	}

	/***************************************
	 * Makes the hierarchy of this entity immutable by setting the flag {@link
	 * MetaTypes#IMMUTABLE} recursively on this entity and it's children (but
	 * not on other entity relations).
	 */
	public void setHierarchyImmutable()
	{
		set(MetaTypes.IMMUTABLE);

		for (RelationType<List<Entity>> rChildAttr :
			 getDefinition().getChildAttributes())
		{
			if (hasRelation(rChildAttr))
			{
				for (Entity rChild : get(rChildAttr))
				{
					rChild.setHierarchyImmutable();
				}
			}
		}
	}

	/***************************************
	 * Creates a {@link Predicate} from this entity using the current attribute
	 * values and the {@link Predicates#equalTo(Object)} method to construct it.
	 *
	 * <p>The attribute {@link EntityRelationTypes#ENTITY_ID} is always ignored!
	 * </p>
	 *
	 * @param  bIgnoreNullValues If TRUE attribute values that are NULL are
	 *                           ignored when building the {@link Predicate}
	 *
	 * @return A {@link Predicate} from this entity using the current attribute
	 *         values and the {@link Predicates#equalTo(Object)} method.
	 */
	public <E extends Entity> Predicate<E> toPredicate(
		boolean bIgnoreNullValues)
	{
		List<RelationType<?>> rAttributes =
			new ArrayList<RelationType<?>>(getDefinition().getAttributes());

		rAttributes.remove(EntityRelationTypes.ENTITY_ID);

		return toPredicate(bIgnoreNullValues,
						   rAttributes.toArray(new RelationType<?>[0]));
	}

	/***************************************
	 * Creates a {@link Predicate} from this entity using the current attribute
	 * values for the given attributes and the {@link
	 * Predicates#equalTo(Object)} method to construct it.
	 *
	 * @param  bIgnoreNullValues If TRUE attribute values that are NULL are
	 *                           ignored when building the {@link Predicate}
	 * @param  rAttributes       The attributes to use.
	 *
	 * @return A {@link Predicate} from this entity using the current attribute
	 *         values for the given attributes and the {@link
	 *         Predicates#equalTo(Object)} method.
	 */
	public <E extends Entity> Predicate<E> toPredicate(
		boolean			   bIgnoreNullValues,
		RelationType<?>... rAttributes)
	{
		Predicate<E> pPredicate = notNull();

		for (RelationType<?> rAttribute : rAttributes)
		{
			if (!bIgnoreNullValues || get(rAttribute) != null)
			{
				pPredicate =
					pPredicate.and(rAttribute.is(equalTo(get(rAttribute))));
			}
		}

		return pPredicate;
	}

	/***************************************
	 * Returns a string representation of this entity.
	 *
	 * @return A string describing this entity
	 */
	@Override
	public String toString()
	{
		Object rId     = isPersistent() ? get(getIdAttribute()) : "<NEW>";
		String sThis   = getDescription();
		String sResult;

		if (sThis.length() > 0)
		{
			sResult =
				String.format("%s[%s(%s)]",
							  getDefinition().getEntityName(),
							  sThis,
							  rId);
		}
		else
		{
			sResult =
				String.format("%s[%s]", getDefinition().getEntityName(), rId);
		}

		return sResult;
	}

	/***************************************
	 * Converts an entity to a string that includes certain attributes.
	 *
	 * @param  rAttributes The attributes to include in the string
	 * @param  sSeparator  The separator string between attributes
	 *
	 * @return The string for the given display mode
	 */
	public String toString(
		Collection<RelationType<?>> rAttributes,
		String						sSeparator)
	{
		StringBuilder aResult =
			new StringBuilder(getDefinition().getEntityName());

		aResult.append('[');

		if (rAttributes != null)
		{
			for (RelationType<?> rAttribute : rAttributes)
			{
				appendAttribute(rAttribute, sSeparator, aResult);
			}

			if (aResult.length() > 0)
			{
				aResult.setLength(aResult.length() - sSeparator.length());
			}
		}

		aResult.append(']');

		return aResult.toString();
	}

	/***************************************
	 * Converts an entity to a string according to a certain display mode.
	 *
	 * @param  eDisplayMode The display mode to create the string for
	 * @param  sSeparator   The separator string between attributes
	 *
	 * @return The string for the given display mode
	 */
	public final String toString(DisplayMode eDisplayMode, String sSeparator)
	{
		return toString(getDefinition().getDisplayAttributes(eDisplayMode),
						sSeparator);
	}

	/***************************************
	 * Converts an entity to a string that includes certain attributes.
	 *
	 * @param  sSeparator  The separator string between attributes
	 * @param  rAttributes The attributes to include in the string
	 *
	 * @return The string for the given display mode
	 */
	public String toString(String sSeparator, RelationType<?>... rAttributes)
	{
		return toString(Arrays.asList(rAttributes), sSeparator);
	}

	/***************************************
	 * Checks whether an attribute of this instance has a defined value and
	 * throws an exception if that values is not equal to the given value. If
	 * the current value is not defined, ergo NULL, no exception is thrown.
	 *
	 * @param  rAttr  The attribute to test
	 * @param  rValue The value to check the attribute value against
	 *
	 * @throws IllegalStateException If the attribute value differs from the
	 *                               given value
	 */
	public <T> void verifyAttribute(RelationType<? super T> rAttr, T rValue)
	{
		Object rCurrentValue = this.get(rAttr);

		if (rCurrentValue != null && !rCurrentValue.equals(rValue))
		{
			String sMessage =
				String.format("%s attribute %s != %s",
							  this.getClass().getSimpleName(),
							  rCurrentValue,
							  rValue);

			throw new IllegalStateException(sMessage);
		}
	}

	/***************************************
	 * Returns a description of this entity. The default implementation returns
	 * the result of {@link EntityFunctions#toString()} but subclasses may
	 * override this method to change or modify the result.
	 *
	 * @return
	 */
	protected String getDescription()
	{
		return EntityFunctions.format(this);
	}

	/***************************************
	 * Appends a string description for a certain entity attribute to a string
	 * buffer.
	 *
	 * @param rAttribute The element to append the description of
	 * @param sSeparator The separator string
	 * @param aBuilder   The string buffer to append to
	 */
	void appendAttribute(RelationType<?> rAttribute,
						 String			 sSeparator,
						 StringBuilder   aBuilder)
	{
		String sValue = getAttributeValue(rAttribute);

		aBuilder.append(rAttribute.getSimpleName());
		aBuilder.append('=');
		aBuilder.append(sValue);
		aBuilder.append(sSeparator);
	}

	/***************************************
	 * Internal method to return the extra attributes of this entity. If the
	 * entity attributes haven't been retrieved yet this method will initialize
	 * the attribute map by performing the necessary storage query.
	 *
	 * @return The extra attributes
	 *
	 * @throws StorageException If retrieving the attributes fails
	 */
	Map<String, ExtraAttribute> getExtraAttributeMap() throws StorageException
	{
		Map<String, ExtraAttribute> rExtraAttributeMap =
			get(EXTRA_ATTRIBUTE_MAP);

		if (isPersistent() && !hasRelation(EXTRA_ATTRIBUTES_READ))
		{
			assert !hasFlag(EXTRA_ATTRIBUTES_MODIFIED) : "Invalid state: extra attributes have been modified";

			Predicate<Relatable> pExtraAttr =
				ExtraAttribute.ENTITY.is(equalTo(this))
									 .and(ExtraAttribute.HAS_NO_OWNER);

			List<ExtraAttribute> rExtraAttributes =
				EntityManager.queryEntities(ExtraAttribute.class,
											pExtraAttr,
											Integer.MAX_VALUE);

			for (ExtraAttribute rAttribute : rExtraAttributes)
			{
				Relation<?> rKeyRelation =
					rAttribute.getRelation(ExtraAttribute.KEY);

				String sKey = null;

				if (rKeyRelation instanceof IntermediateRelation)
				{
					sKey =
						((IntermediateRelation<?, ?>) rKeyRelation)
						.getIntermediateTarget().toString();
				}

				// always check for NULL because the intermediate target will be
				// NULL if the relation has already been resolved
				if (sKey == null)
				{
					sKey = rAttribute.get(ExtraAttribute.KEY).getSimpleName();
				}

				assert sKey != null : "Undefined extra attribute key for " +
					   rAttribute;

				rExtraAttributeMap.put(sKey.toString(), rAttribute);
			}

			set(EXTRA_ATTRIBUTES_READ);
		}

		return rExtraAttributeMap;
	}

	/***************************************
	 * Appends an attribute change description to a string builder.
	 *
	 * @param aResult       The string builder to append to
	 * @param sIndent       The indentation
	 * @param sAttr         The name of the attribute to append
	 * @param rAttrRelation The attribute relation
	 * @param bNewEntity    TRUE for a non-persistent entity
	 */
	private void appendAttributeChange(StringBuilder aResult,
									   String		 sIndent,
									   String		 sAttr,
									   Relation<?>   rAttrRelation,
									   boolean		 bNewEntity)
	{
		Object rNewValue = rAttrRelation.getTarget();

		boolean bUpdated =
			(!bNewEntity && rAttrRelation.hasRelation(PREVIOUS_VALUE));

		if (bUpdated || rNewValue != null)
		{
			aResult.append(sIndent);
			JsonUtil.appendName(aResult, sAttr);

			if (bUpdated)
			{
				aResult.append("{\"new\": ");
			}

			JsonUtil.appendValue(aResult, rNewValue);

			if (bUpdated)
			{
				Object rPrevValue = rAttrRelation.get(PREVIOUS_VALUE);

				aResult.append(", \"old\": ");
				JsonUtil.appendValue(aResult, rPrevValue);
				aResult.append("}");
			}

			aResult.append(",\n");
		}
	}

	/***************************************
	 * Appends the change descriptions for modified attributes to a string
	 * builder.
	 *
	 * @param aResult     The string builder
	 * @param rDefinition The definition of this entity
	 * @param sIndent     The indentation
	 * @param bNewEntity  TRUE for a non-persistent entity
	 */
	private void appendAttributeChanges(StringBuilder		aResult,
										EntityDefinition<?> rDefinition,
										String				sIndent,
										boolean				bNewEntity)
	{
		for (RelationType<?> rAttribute : rDefinition.getAttributes())
		{
			Relation<?> rRelation = getRelation(rAttribute);

			if (rRelation != null &&
				(bNewEntity || rRelation.hasRelation(PREVIOUS_VALUE)))
			{
				appendAttributeChange(aResult,
									  sIndent,
									  rAttribute.getSimpleName(),
									  rRelation,
									  bNewEntity);
			}
		}
	}

	/***************************************
	 * Appends the changes of child entities to a change string builder.
	 *
	 * @param aResult         The string builder
	 * @param sIndent         The indentation
	 * @param rChildAttribute The child attribute
	 */
	private void appendChildChanges(StringBuilder			   aResult,
									String					   sIndent,
									RelationType<List<Entity>> rChildAttribute)
	{
		List<Entity>  rChildren     = get(rChildAttribute);
		StringBuilder aChildChanges = new StringBuilder();
		String		  sChildAttr    = rChildAttribute.getSimpleName();

		for (Entity rChild : rChildren)
		{
			String sChildChange = rChild.createChangeDescription(sIndent);

			if (!sChildChange.isEmpty())
			{
				aChildChanges.append(sChildChange);
				aChildChanges.append(",\n");
			}
		}

		if (aChildChanges.length() > 0)
		{
			aChildChanges.setLength(aChildChanges.length() - 2);

			aResult.append(sIndent);
			JsonUtil.appendName(aResult, sChildAttr);
			aResult.append("[\n");
			aResult.append(aChildChanges);
			aResult.append("\n");
			aResult.append(sIndent);
			aResult.append("],\n");
		}

		List<Entity> rRemovedChildren =
			getRelation(rChildAttribute).get(REMOVED_CHILDREN);

		if (!rRemovedChildren.isEmpty())
		{
			aResult.append(sIndent);
			aResult.append("\"");
			aResult.append(sChildAttr);
			aResult.append("-removed\": [");

			for (Entity rRemoved : rRemovedChildren)
			{
				aResult.append(rRemoved.getId());
				aResult.append(", ");
			}

			aResult.append("],\n");
		}
	}

	/***************************************
	 * Appends the change descriptions for modified extra attributes to a string
	 * builder.
	 *
	 * @param aResult    The string builder
	 * @param sIndent    The indentation
	 * @param bNewEntity TRUE for a non-persistent entity
	 */
	private void appendExtraAttributeChanges(StringBuilder aResult,
											 String		   sIndent,
											 boolean	   bNewEntity)
	{
		for (ExtraAttribute rExtraAttribute : get(EXTRA_ATTRIBUTE_MAP).values())
		{
			Relation<Object> rExtraAttrRelation =
				rExtraAttribute.getRelation(ExtraAttribute.VALUE);

			if (rExtraAttribute.hasFlag(MODIFIED) &&
				(bNewEntity || rExtraAttrRelation.hasRelation(PREVIOUS_VALUE)))
			{
				String sName =
					rExtraAttribute.get(ExtraAttribute.KEY).getName();

				appendAttributeChange(aResult,
									  sIndent,
									  sName,
									  rExtraAttrRelation,
									  bNewEntity);
			}
		}
	}

	/***************************************
	 * Performs an equality comparison of this instance's attributes with
	 * another entity, including extra attributes and children.
	 *
	 * @param  rOther The entity to compare with
	 *
	 * @return TRUE if all attributes are equal
	 */
	private boolean attributesEqual(Entity rOther)
	{
		List<RelationType<?>> rCompareAttributes = getCompareAttributes();

		boolean bEqual =
			rCompareAttributes.equals(rOther.getCompareAttributes());

		if (bEqual)
		{
			for (RelationType<?> rAttr : rCompareAttributes)
			{
				Object rValue	   = get(rAttr);
				Object rOtherValue = rOther.get(rAttr);

				if (rValue == null && rOtherValue != null ||
					rValue != null && !rValue.equals(rOtherValue))
				{
					bEqual = false;

					break;
				}
			}

			if (bEqual)
			{
				Map<String, ExtraAttribute> rExtraAttributeMap =
					get(EXTRA_ATTRIBUTE_MAP);

				Map<String, ExtraAttribute> rOtherExtraAttributeMap =
					rOther.get(EXTRA_ATTRIBUTE_MAP);

				if (rExtraAttributeMap != null)
				{
					bEqual = rExtraAttributeMap.equals(rOtherExtraAttributeMap);
				}
				else
				{
					bEqual = rOtherExtraAttributeMap == null;
				}
			}
		}

		return bEqual;
	}

	/***************************************
	 * Calculates the hash code of this entities attributes, including extra
	 * attributes and children.
	 *
	 * @return The attribute hash code
	 */
	private int attributesHashCode()
	{
		int nHashCode = getCompareAttributes().hashCode();

		Map<String, ExtraAttribute> rExtraAttributeMap =
			get(EXTRA_ATTRIBUTE_MAP);

		if (rExtraAttributeMap != null)
		{
			nHashCode = 37 * nHashCode + rExtraAttributeMap.hashCode();
		}

		return nHashCode;
	}

	/***************************************
	 * Internal method to create a description string for the modified values of
	 * this entity and it's hierarchy. Value changes are recorded by the entity
	 * modification tracking with annotations of type {@link
	 * StandardTypes#PREVIOUS_VALUE}.
	 *
	 * @param  sIndent sSubIndent The indentation of the resulting string
	 *
	 * @return The resulting string
	 */
	private String createChangeDescription(String sIndent)
	{
		EntityDefinition<?> rDefinition = getDefinition();
		StringBuilder	    aResult     = new StringBuilder("");
		String			    sSubIndent  = sIndent + "  ";
		boolean			    bNewEntity  = !isPersistent();

		if (hasFlag(MODIFIED))
		{
			appendAttributeChanges(aResult,
								   rDefinition,
								   sSubIndent,
								   bNewEntity);
		}

		if (hasFlag(EXTRA_ATTRIBUTES_MODIFIED))
		{
			appendExtraAttributeChanges(aResult, sSubIndent, bNewEntity);
		}

		Collection<RelationType<List<Entity>>> rChildAttributes =
			rDefinition.getChildAttributes();

		if (rChildAttributes != null)
		{
			for (RelationType<List<Entity>> rChildAttr : rChildAttributes)
			{
				appendChildChanges(aResult, sSubIndent, rChildAttr);
			}
		}

		int nLength = aResult.length();

		if (nLength > 0 || !isPersistent())
		{
			if (nLength >= 2)
			{
				// remove trailing ,\n
				aResult.setLength(nLength - 2);
			}

			@SuppressWarnings("boxing")
			Object rId = isPersistent() ? getId() : "\"<NEW>\"";

			aResult.insert(0,
						   String.format("%s{\n%s\"%s\": %s,\n",
										 sIndent,
										 sSubIndent,
										 getClass().getSimpleName(),
										 rId));
			aResult.append("\n");
			aResult.append(sIndent);
			aResult.append("}");
		}

		return aResult.toString();
	}

	/***************************************
	 * Returns the value of an attribute.
	 *
	 * @param  rAttribute The attribute
	 *
	 * @return The attribute value
	 */
	@SuppressWarnings("boxing")
	private String getAttributeValue(RelationType<?> rAttribute)
	{
		Relation<?> rRelation = getRelation(rAttribute);
		Object	    rValue    = null;

		if (rRelation != null)
		{
			rValue = rRelation.getTarget();
		}

		if (rValue == null && rRelation instanceof IntermediateRelation<?, ?>)
		{
			rValue =
				((IntermediateRelation<?, ?>) rRelation)
				.getIntermediateTarget();
		}

		if (rValue instanceof Entity)
		{
			rValue = ((Entity) rValue).getId();
		}

		return rValue != null ? rValue.toString().replaceAll("[\n\r]", "¶")
							  : null;
	}

	/***************************************
	 * Returns a list of all entity attributes (including extra attributes) that
	 * can be used for equality comparison by {@link #equals(Object)} and the
	 * hash code calculation by {@link #hashCode()} without causing an endless
	 * recursion.
	 *
	 * @return The list of all comparable (extra) attributes
	 */
	private ArrayList<RelationType<?>> getCompareAttributes()
	{
		EntityDefinition<?> rDefinition = getDefinition();

		ArrayList<RelationType<?>> aAttributes =
			new ArrayList<RelationType<?>>(rDefinition.getAttributes());

		aAttributes.remove(rDefinition.getParentAttribute());

		return aAttributes;
	}

	/***************************************
	 * Prints this entity and the hierarchy of it's children to a print stream.
	 * This method is intended to be used for debugging and informational
	 * purposes only. The format of the output may change any time.
	 *
	 * @param rOut    The stream to print to
	 * @param sIndent The indentation of the output
	 */
	private void printHierarchy(PrintStream rOut, String sIndent)
	{
		EntityDefinition<?> rDefinition = getDefinition();
		String			    sPrefix     =
			sIndent != null ? sIndent + "+--" : "";

		sIndent = sIndent != null ? sIndent + "   " : "";

		rOut.println(sPrefix + toString(rDefinition.getAttributes(), ","));

		for (RelationType<?> rChildType : rDefinition.getChildAttributes())
		{
			@SuppressWarnings("unchecked")
			Collection<Entity> rChildren =
				get((RelationType<Collection<Entity>>) rChildType);

			for (Entity rChild : rChildren)
			{
				rChild.printHierarchy(rOut, sIndent);
			}
		}
	}

	/***************************************
	 * Internal method to query a single extra attribute for a certain owner
	 * entity.
	 *
	 * @param  rOwner The owner entity
	 * @param  rKey   The extra attribute key
	 *
	 * @return The matching extra attribute or NULL if none exists
	 *
	 * @throws StorageException If the query fails
	 */
	private <T> ExtraAttribute queryExtraAttributeFor(
		Entity			rOwner,
		RelationType<T> rKey) throws StorageException
	{
		ExtraAttribute rExtraAttribute = null;

		if (isPersistent())
		{
			Predicate<Relatable> pExtraAttr =
				ExtraAttribute.ENTITY.is(equalTo(this))
									 .and(ExtraAttribute.OWNER.is(equalTo(rOwner)))
									 .and(ExtraAttribute.KEY.is(equalTo(rKey)));

			rExtraAttribute =
				EntityManager.queryEntity(ExtraAttribute.class,
										  pExtraAttr,
										  true);
		}

		return rExtraAttribute;
	}
}
