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

import de.esoco.lib.event.ElementEvent.EventType;
import de.esoco.lib.event.EventHandler;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.logging.Log;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.property.HasProperties;
import de.esoco.lib.property.MutableProperties;
import de.esoco.lib.property.StringProperties;
import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextUtil;

import de.esoco.storage.QueryList;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;
import de.esoco.storage.StorageMapping;
import de.esoco.storage.impl.jdbc.JdbcRelationTypes;
import de.esoco.storage.mapping.AbstractStorageMapping;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationEvent;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import static de.esoco.entity.EntityRelationTypes.DISPLAY_PROPERTIES;
import static de.esoco.entity.EntityRelationTypes.ENTITY_ID;
import static de.esoco.entity.EntityRelationTypes.ENTITY_STORE_ORIGIN;
import static de.esoco.entity.EntityRelationTypes.MASTER_ENTITY_ID;
import static de.esoco.entity.EntityRelationTypes.PARENT_ENTITY_ID;
import static de.esoco.entity.EntityRelationTypes.REMOVED_CHILDREN;
import static de.esoco.entity.EntityRelationTypes.ROOT_ATTRIBUTE;
import static de.esoco.entity.ExtraAttributes.EXTRA_ATTRIBUTE_FLAG;

import static de.esoco.storage.StorageRelationTypes.REFERENCE_ATTRIBUTE;
import static de.esoco.storage.StorageRelationTypes.STORAGE_DATATYPE;
import static de.esoco.storage.StorageRelationTypes.STORAGE_MAPPING;
import static de.esoco.storage.StorageRelationTypes.STORAGE_NAME;

import static org.obrel.type.MetaTypes.CHILD_ATTRIBUTE;
import static org.obrel.type.MetaTypes.ELEMENT_DATATYPE;
import static org.obrel.type.MetaTypes.INITIALIZING;
import static org.obrel.type.MetaTypes.MODIFIED;
import static org.obrel.type.MetaTypes.OBJECT_ID_ATTRIBUTE;
import static org.obrel.type.MetaTypes.OBJECT_NAME_ATTRIBUTE;
import static org.obrel.type.MetaTypes.OBJECT_TYPE_ATTRIBUTE;
import static org.obrel.type.MetaTypes.PARENT_ATTRIBUTE;
import static org.obrel.type.StandardTypes.INFO;
import static org.obrel.type.StandardTypes.NAME;
import static org.obrel.type.StandardTypes.PREVIOUS_VALUE;


/********************************************************************
 * The base class for the definition of entities. The easiest way to define an
 * entity is to subclass this class, add relation type constants for all
 * attributes (including children), and invoke one of the constructors with
 * arguments, preferably in conjunction with an assignment to a constant. The
 * default implementation will then collect all static fields and initialize the
 * definition accordingly. Subclasses that need to create entity definitions
 * from other informations (like databases or XML files) should invoke the
 * constructor without parameter and one of the init() methods.
 *
 * @author eso
 */
public class EntityDefinition<E extends Entity>
	extends AbstractStorageMapping<E, RelationType<?>, EntityDefinition<?>>
	implements EventHandler<RelationEvent<?>>, Serializable
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of entity display modes
	 */
	public enum DisplayMode { MINIMAL, COMPACT, FULL, HIERARCHICAL }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/**
	 * The name of the optional static field in an entity class that contains a
	 * collection of the entity attribute relation types.
	 */
	public static final String ENTITY_ATTRIBUTES_FIELD = "ENTITY_ATTRIBUTES";

	/**
	 * The name of the optional static entity field that contains a string
	 * constant with the ID prefix for the global IDs of an entity class
	 */
	public static final String ID_PREFIX_FIELD = "ID_PREFIX";

	/**
	 * The name of the optional static entity field that contains a string
	 * constant with the storage name for an entity class
	 */
	public static final String STORAGE_NAME_FIELD = "STORAGE_NAME";

	/**
	 * The name of the optional static entity field that contains a boolean
	 * value to disable the generation of child count columns in SQL storages.
	 * See the relation type {@link JdbcRelationTypes#SQL_DISABLE_CHILD_COUNTS}
	 * for more information. The boolean value of this field should normally
	 * always be TRUE or the field shouldn't exist at all.
	 */
	public static final String DISABLE_SQL_CHILD_COUNT_FIELD =
		"DISABLE_SQL_CHILD_COUNT";

	/**
	 * The prefix for the names of the optional fields that contain an array
	 * with the display attribute relation types for an entity class. The
	 * possible suffixes are defined by the {@link DisplayMode} enum.
	 */
	public static final String DISPLAY_ATTRIBUTES_FIELD_PREFIX =
		"DISPLAY_ATTRIBUTES_";

	/**
	 * The name of the optional public static final field in an entity class
	 * that contains a mapping from attribute relation types to additional
	 * display properties for that field. Because attributes can be shared
	 * between different entity classes this field provides a way to define
	 * different display attributes for the same field. The datatype of this
	 * field must be Map&lt;RelationType&lt;?&gt;, MutableProperties&gt;.
	 */
	public static final String ATTRIBUTE_DISPLAY_PROPERTIES_FIELD =
		"ATTRIBUTE_DISPLAY_PROPERTIES";

	//~ Instance fields --------------------------------------------------------

	/**
	 * @serial Only the entity class field will be serialized (with default
	 *         serialization) and instances read will then be replaced with
	 *         their singleton by the {@link #readResolve()} method.
	 */
	private Class<E> rEntityClass;

	private transient String						 sEntityName;
	private transient String						 sIdPrefix;
	private transient RelationType<? extends Number> rIdAttribute;
	private transient RelationType<Enum<?>>			 rTypeAttribute;
	private transient RelationType<String>			 rNameAttribute;
	private transient RelationType<? extends Entity> rMasterAttribute;
	private transient RelationType<? extends Entity> rParentAttribute;
	private transient RelationType<? extends Entity> rRootAttribute;
	private transient RelationType<List<Entity>>     rHierarchyChildAttribute;

	private transient List<RelationType<?>>								   aAttributes;
	private transient Map<EntityDefinition<?>, RelationType<List<Entity>>> aChildAttributes;

	private transient Map<String, Class<? extends E>>  aTypeSubClasses = null;
	private transient Map<Class<? extends E>, Enum<?>> aSubClassTypes  = null;

	private final transient Map<DisplayMode, List<RelationType<?>>> aDisplayAttributes =
		new HashMap<DisplayMode, List<RelationType<?>>>();

	private Map<RelationType<?>, ? extends HasProperties> aAttributeDisplayProperties;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance without initializing it. Subclasses that use this
	 * constructor must invoke the init method or else this instance will be in
	 * an unusable state.
	 */
	protected EntityDefinition()
	{
	}

	/***************************************
	 * Creates a new instance from a certain entity class. Internally the method
	 * {@link #init(String, String, Class, List)} will be invoked which will
	 * then evaluate the relation type declarations in the given entity class to
	 * define the entity structure.
	 *
	 * @param rEntityClass The entity class to create the definition for
	 */
	protected EntityDefinition(Class<E> rEntityClass)
	{
		this(rEntityClass, null);
	}

	/***************************************
	 * Creates a new definition from a certain entity class which contains the
	 * attribute relation type definitions.
	 *
	 * @param rEntityClass The entity class to create the definition for
	 * @param sIdPrefix    The prefix for global entity IDs
	 */
	protected EntityDefinition(Class<E> rEntityClass, String sIdPrefix)
	{
		RelationTypes.init(rEntityClass);

		ObjectRelations.getRelatable(rEntityClass).set(STORAGE_MAPPING, this);

		init(
			rEntityClass.getSimpleName(),
			sIdPrefix,
			rEntityClass,
			getAttributeTypes(rEntityClass));
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Initializes the attribute-specific display properties that may be stored
	 * in the optional field {@link #ATTRIBUTE_DISPLAY_PROPERTIES_FIELD} of an
	 * entity class.
	 *
	 * @param  rEntityClass The entity class
	 *
	 * @return The mapping from attributes to display properties
	 */
	@SuppressWarnings("unchecked")
	static Map<RelationType<?>, MutableProperties>
	getAttributeDisplayProperties(Class<? extends Entity> rEntityClass)
	{
		return (Map<RelationType<?>, MutableProperties>) getStaticFieldValue(
			rEntityClass,
			ATTRIBUTE_DISPLAY_PROPERTIES_FIELD);
	}

	/***************************************
	 * Returns the value of a certain static field from an entity class.
	 *
	 * @param  rEntityClass The entity class
	 * @param  sFieldName   The name of the static field to access
	 *
	 * @return The field value
	 */
	static Object getStaticFieldValue(
		Class<? extends Entity> rEntityClass,
		String					sFieldName)
	{
		Object rValue;

		try
		{
			Field rField = rEntityClass.getDeclaredField(sFieldName);

			if (!Modifier.isPublic(rField.getModifiers()))
			{
				rField.setAccessible(true);
			}

			rValue = rField.get(null);
		}
		catch (NoSuchFieldException e)
		{
			rValue = null;
		}
		catch (Exception e)
		{
			throw new IllegalStateException(
				"Could not access field " +
				sFieldName);
		}

		return rValue;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Object checkAttributeValue(RelationType<?> rAttribute, Object rValue)
		throws StorageException
	{
		if (rValue != null &&
			Entity.class.isAssignableFrom(rAttribute.getTargetType()))
		{
			if (rValue instanceof String)
			{
				rValue = EntityManager.queryEntity((String) rValue);
			}
			else if (rValue instanceof Integer)
			{
				int nId = ((Integer) rValue).intValue();

				@SuppressWarnings("unchecked")
				Class<? extends Entity> rEntityType =
					(Class<Entity>) rAttribute.getTargetType();

				rValue = EntityManager.queryEntity(rEntityType, nId);
			}
		}
		else
		{
			rValue = super.checkAttributeValue(rAttribute, rValue);
		}

		return rValue;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public E createObject(List<?> rAttributeValues, boolean bAsChild)
		throws StorageException
	{
		boolean bHasCaching =
			EntityManager.isCachingEnabledFor(getMappedType());

		E aEntity = null;

		if (!bAsChild && bHasCaching)
		{
			aEntity = tryToGetFromParent(rAttributeValues);
		}

		if (aEntity == null)
		{
			try
			{
				aEntity = createNewEntity(rAttributeValues);
			}
			catch (Exception e)
			{
				Log.errorf(
					e,
					"Error creating entity %s from data %s",
					rEntityClass.getSimpleName(),
					rAttributeValues);

				throw e;
			}
		}

		return aEntity;
	}

	/***************************************
	 * Returns the attribute relation type that corresponds to a particular
	 * attribute name. This method is mainly intended to support the
	 * implementation of attribute mappings that are defined with strings.
	 *
	 * @param  sAttributeName The attribute name to return the type for
	 *
	 * @return The matching attribute type or NULL if no such instance exists
	 */
	public final RelationType<?> getAttribute(String sAttributeName)
	{
		for (RelationType<?> rAttr : aAttributes)
		{
			if (rAttr.getName().equals(sAttributeName))
			{
				return rAttr;
			}
		}

		return null;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getAttributeDatatype(RelationType<?> rAttribute)
	{
		return rAttribute.getTargetType();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Collection<RelationType<?>> getAttributes()
	{
		return aAttributes;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Object getAttributeValue(E rEntity, RelationType<?> rAttribute)
		throws StorageException
	{
		return rEntity.get(rAttribute);
	}

	/***************************************
	 * Returns the relation types of the child attributes of this definition.
	 *
	 * @return A collection of the child attribute relation types
	 */
	public Collection<RelationType<List<Entity>>> getChildAttributes()
	{
		if (aChildAttributes != null)
		{
			return aChildAttributes.values();
		}
		else
		{
			return Collections.emptySet();
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Collection<EntityDefinition<?>> getChildMappings()
	{
		if (aChildAttributes != null)
		{
			return aChildAttributes.keySet();
		}
		else
		{
			return Collections.emptySet();
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Entity> getChildren(
		E					rParent,
		EntityDefinition<?> rChildMapping)
	{
		return rParent.get(aChildAttributes.get(rChildMapping));
	}

	/***************************************
	 * Overridden to return a prediate for a type attribute value if a specific
	 * sub-class of the entity type exists.
	 *
	 * @see StorageMapping#getDefaultCriteria(Class)
	 */
	@Override
	public Predicate<E> getDefaultCriteria(Class<? extends E> rType)
	{
		Predicate<E> pDefault = null;

		if (aSubClassTypes != null)
		{
			Enum<?> eType = aSubClassTypes.get(rType);

			if (eType != null)
			{
				pDefault = rTypeAttribute.is(Predicates.equalTo(eType));
			}
		}

		return pDefault;
	}

	/***************************************
	 * Returns the attributes for a certain display mode.
	 *
	 * @param  rDisplayMode The display mode
	 *
	 * @return A collection containing the attributes for the display mode
	 */
	public List<RelationType<?>> getDisplayAttributes(DisplayMode rDisplayMode)
	{
		return aDisplayAttributes.get(rDisplayMode);
	}

	/***************************************
	 * Returns the display properties for a certain attribute. Always returns a
	 * new properties object, even if empty, that can be modified freely by the
	 * receiver.
	 *
	 * @param  rAttribute The attribute to return the display properties for
	 *
	 * @return The display properties for the given attribute
	 */
	public MutableProperties getDisplayProperties(RelationType<?> rAttribute)
	{
		StringProperties aDisplayProperties = new StringProperties();

		addProperties(aDisplayProperties, rAttribute.get(DISPLAY_PROPERTIES));

		if (aAttributeDisplayProperties != null)
		{
			addProperties(
				aDisplayProperties,
				aAttributeDisplayProperties.get(rAttribute));
		}

		return aDisplayProperties;
	}

	/***************************************
	 * Returns the descriptive entity name.
	 *
	 * @return The entity name
	 */
	public final String getEntityName()
	{
		return sEntityName;
	}

	/***************************************
	 * Returns the child attribute for a hierarchy of entities of the same type
	 * as the entity type described by this definition.
	 *
	 * @return The hierarchy child attribute or NULL for none
	 */
	public final RelationType<List<Entity>> getHierarchyChildAttribute()
	{
		return rHierarchyChildAttribute;
	}

	/***************************************
	 * Returns the relation type of the numeric ID attribute in this definition.
	 * The exact datatype depends on the actual ID attribute relation type.
	 * Typically this will be either {@link Integer} or {@link Long}.
	 *
	 * <p>If an entity does not define it's own ID attribute the default ID type
	 * {@link EntityRelationTypes#ENTITY_ID} will be returned.</p>
	 *
	 * @see AbstractStorageMapping#getIdAttribute()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public RelationType<Number> getIdAttribute()
	{
		return (RelationType<Number>) rIdAttribute;
	}

	/***************************************
	 * Returns the prefix that will be added to global entity IDs.
	 *
	 * @return The ID prefix
	 */
	public final String getIdPrefix()
	{
		return sIdPrefix;
	}

	/***************************************
	 * @see AbstractStorageMapping#getMappedType()
	 */
	@Override
	public Class<E> getMappedType()
	{
		return rEntityClass;
	}

	/***************************************
	 * Returns the master attribute if such exists.
	 *
	 * @return The master attribute or NULL for none
	 */
	public RelationType<? extends Entity> getMasterAttribute()
	{
		return rMasterAttribute;
	}

	/***************************************
	 * Returns the relation type of an attribute in this definition that refers
	 * to the name of an entity. This will either be the attribute with the type
	 * {@link StandardTypes#NAME} or any other string attribute that has the
	 * meta relation type {@link MetaTypes#OBJECT_NAME_ATTRIBUTE} set.
	 *
	 * @return The name attribute of the entity or NULL for none
	 */
	public RelationType<String> getNameAttribute()
	{
		return rNameAttribute;
	}

	/***************************************
	 * Returns the parent attribute if such exists.
	 *
	 * @return The parent attribute or NULL for none
	 */
	public RelationType<? extends Entity> getParentAttribute()
	{
		return rParentAttribute;
	}

	/***************************************
	 * @see AbstractStorageMapping#getParentAttribute(StorageMapping)
	 */
	@Override
	public RelationType<? extends Entity> getParentAttribute(
		StorageMapping<?, ?, ?> rParentDefinition)
	{
		RelationType<? extends Entity> rResult = null;

		if (rParentAttribute != null &&
			rParentAttribute.get(STORAGE_MAPPING) == rParentDefinition)
		{
			rResult = rParentAttribute;
		}
		else if (rMasterAttribute != null &&
				 rMasterAttribute.get(STORAGE_MAPPING) == rParentDefinition)
		{
			rResult = rMasterAttribute;
		}

		return rResult;
	}

	/***************************************
	 * Returns the root parent attribute if such exists.
	 *
	 * @return The root attribute or NULL for none
	 */
	public RelationType<? extends Entity> getRootAttribute()
	{
		return rRootAttribute;
	}

	/***************************************
	 * Returns the hierarchy attribute of this definition that references
	 * entities of the same type.
	 *
	 * @return The attribute or NULL for none
	 */
	public RelationType<List<Entity>> getSelfReferenceAttribute()
	{
		RelationType<List<Entity>> rAttr = null;

		if (aChildAttributes != null)
		{
			rAttr = aChildAttributes.get(this);
		}

		return rAttr;
	}

	/***************************************
	 * Returns the relation type of an attribute in this definition that defines
	 * the type of an entity. If a definition subclass does not define such a
	 * type attribute with an enum datatype and a meta-relation with the flag
	 * type {@link MetaTypes#OBJECT_TYPE_ATTRIBUTE} set to TRUE this method will
	 * return NULL.
	 *
	 * @return The type attribute of this definition or NULL for none
	 */
	public RelationType<Enum<?>> getTypeAttribute()
	{
		return rTypeAttribute;
	}

	/***************************************
	 * Event handler implementation that will be registered as a relation
	 * listener on entities to perform the modification tracking for entity
	 * attributes.
	 *
	 * @see EventHandler#handleEvent(de.esoco.lib.event.Event)
	 */
	@Override
	@SuppressWarnings("boxing")
	public void handleEvent(RelationEvent<?> rEvent)
	{
		Entity rEntity = (Entity) rEvent.getSource();

		if (!rEntity.hasFlag(INITIALIZING))
		{
			RelationType<?> rRelationType = rEvent.getElement().getType();
			Object		    rUpdateValue  = rEvent.getUpdateValue();

			if (rRelationType == MODIFIED)
			{
				// can be NULL if relations are copied between objects
				if (rUpdateValue != null)
				{
					handleEntityModification(rEntity, (Boolean) rUpdateValue);
				}
			}

			// exclude ID attribute as it will typically be set after automatic
			// generation by the database (causing an update event for a newly
			// persisted entity). Cases where ONLY the ID is set manually on a
			// persistent object would therefore be excluded from modification
			// detection and need to be handled explicitly (by setting MODIFIED).
			else if (rRelationType != rIdAttribute &&
					 aAttributes.contains(rRelationType))
			{
				EventType   eEventType = rEvent.getType();
				Relation<?> rRelation  = rEvent.getElement();
				Object	    rPrevValue = rRelation.getTarget();
				Object	    rNewValue  = rUpdateValue;

				if (eEventType == EventType.ADD)
				{
					rNewValue  = rPrevValue;
					rPrevValue = null;
				}

				boolean bModified = !Objects.equals(rNewValue, rPrevValue);

				// only store previous value on first change to remember
				// the persistent state, not intermediate changes; upon store
				// the PREVIOUS_VALUE relation will be removed
				if (bModified && !rRelation.hasAnnotation(PREVIOUS_VALUE))
				{
					rRelation.annotate(PREVIOUS_VALUE, rPrevValue);
				}

				if ((bModified || eEventType != EventType.UPDATE) &&
					!rEntity.isModified())
				{
					// this will invoke the == MODIFIED branch above
					rEntity.set(MODIFIED);
				}
			}
		}
	}

	/***************************************
	 * @see AbstractStorageMapping#initChildren(Object, List, StorageMapping)
	 */
	@Override
	public void initChildren(E					 rParent,
							 List<?>			 rChildren,
							 EntityDefinition<?> rChildDef)
	{
		initChildren(rParent, rChildren, rChildDef, true);
	}

	/***************************************
	 * Initializes the child entities of a parent by setting the parent
	 * attributes accordingly. This includes the root and master attributes if
	 * available.
	 *
	 * @param rParent       The parent entity of the children
	 * @param rChildren     The list of child entities to initialize
	 * @param rChildDef     The entity definition of the children
	 * @param bInitializing TRUE if this call occurs during the initialization
	 *                      of the object tree or FALSE for the manipulation of
	 *                      an existing object hierarchy
	 */
	@SuppressWarnings("unchecked")
	public void initChildren(E					 rParent,
							 List<?>			 rChildren,
							 EntityDefinition<?> rChildDef,
							 boolean			 bInitializing)
	{
		assert rParent.getDefinition() == this;

		RelationType<List<Entity>> rChildAttr = aChildAttributes.get(rChildDef);

		Class<?> rChildType = rChildAttr.get(ELEMENT_DATATYPE);
		Entity   rMaster    = null;
		Entity   rRoot	    = null;

		RelationType<E> rChildParentAttr = getChildParentAttribute(rChildDef);

		RelationType<List<Entity>> rChildChildAttr =
			rChildDef.getSelfReferenceAttribute();

		if (rChildDef.rMasterAttribute != null)
		{
			if (this == rChildDef)
			{
				rMaster = rParent.get((RelationType<Entity>) rMasterAttribute);
			}
			else
			{
				rMaster = rParent;
			}
		}

		if (this == rChildDef && rRootAttribute != null)
		{
			rRoot = rParent.get((RelationType<Entity>) rRootAttribute);

			if (rRoot == null)
			{
				rRoot = rParent;
			}
		}

		for (Object rChild : rChildren)
		{
			assert rChildType.isAssignableFrom(rChild.getClass());

			Entity rChildEntity = (Entity) rChild;
			E	   rChildParent = rChildEntity.get(rChildParentAttr);

			if (rChildParent != null && rChildParent.getId() != rParent.getId())
			{
				throw new IllegalArgumentException(
					"Child already has other parent: " +
					rChildEntity.get(rChildParentAttr));
			}

			if (bInitializing)
			{
				rChildEntity.set(INITIALIZING);
			}

			rChildEntity.set(rChildParentAttr, rParent);
			rChildDef.setHierarchyReferences(
				rChildEntity,
				rChildChildAttr,
				rMaster,
				rRoot);

			rChildEntity.deleteRelation(INITIALIZING);
		}
	}

	/***************************************
	 * Forwarded to {@link EntityManager#isDeletionEnabledFor(Class)}.
	 *
	 * @see AbstractStorageMapping#isDeleteAllowed()
	 */
	@Override
	public boolean isDeleteAllowed()
	{
		return EntityManager.isDeletionEnabledFor(rEntityClass);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean isHierarchyAttribute(RelationType<?> rAttribute)
	{
		return rAttribute == rParentAttribute || rAttribute == rRootAttribute ||
			   rAttribute == rMasterAttribute;
	}

	/***************************************
	 * Overridden to map entity references to entity IDs.
	 *
	 * @see AbstractStorageMapping#mapValue(Relatable, Object)
	 */
	@Override
	@SuppressWarnings("boxing")
	public Object mapValue(RelationType<?> rAttribute, Object rValue)
		throws StorageException
	{
		if (rValue instanceof Entity)
		{
			Entity rReferencedEntity = (Entity) rValue;

			// if value is a specific entity (i.e. with a specific entity
			// subclass as the attribute type) only the entity ID will be
			// returned; arbitrary entity references will be mapped to their
			// global ID
			if (rAttribute.getTargetType() != Entity.class)
			{
				rValue = rReferencedEntity.getId();
			}
			else
			{
				rValue = EntityManager.getGlobalEntityId(rReferencedEntity);
			}
		}
		else
		{
			rValue = super.mapValue(rAttribute, rValue);
		}

		return rValue;
	}

	/***************************************
	 * @see AbstractStorageMapping#setAttributeValue(Object, Relatable, Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setAttributeValue(E				  rEntity,
								  RelationType<?> rAttribute,
								  Object		  rValue)
	{
		// will throw an exception if the value type is inappropriate
		rEntity.set((RelationType<Object>) rAttribute, rValue);
	}

	/***************************************
	 * @see AbstractStorageMapping#setChildren(Object, List, StorageMapping)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setChildren(E					rParent,
							List<?>				rChildren,
							EntityDefinition<?> rChildDef)
	{
		RelationType<List<Entity>> rChildAttribute =
			aChildAttributes.get(rChildDef);

		rParent.set(rChildAttribute, (List<Entity>) rChildren);
	}

	/***************************************
	 * Overridden to store the referenced entity with {@link
	 * EntityManager#storeEntity(Entity, Entity)}.
	 *
	 * @see AbstractStorageMapping#storeReference(Relatable, Object)
	 */
	@Override
	public void storeReference(Relatable rSourceObject, E rReferencedEntity)
		throws StorageException
	{
		try
		{
			EntityManager.storeEntity(
				rReferencedEntity,
				rSourceObject.get(ENTITY_STORE_ORIGIN));
		}
		catch (TransactionException e)
		{
			throw new StorageException(e);
		}
	}

	/***************************************
	 * Returns a string representation of this entity for debugging purpose,
	 * which includes a list of it's attribute types.
	 *
	 * @return The debug string representation of this definition
	 */
	public String toDebugString()
	{
		return getClass().getSimpleName() + aAttributes +
			   (aChildAttributes != null ? "," + aChildAttributes.values()
										 : "");
	}

	/***************************************
	 * Returns a string representation of this definition.
	 *
	 * @see Object#toString()
	 */
	@Override
	public final String toString()
	{
		return String.format("EntityDefinition[%s]", sEntityName);
	}

	/***************************************
	 * Creates a new instance of the entity class. If the entity has a type
	 * attribute and a subclass that has the same name as the type value in the
	 * attribute values (converted to camel case) a subclass of that type will
	 * be created instead.
	 *
	 * @param  rAttributeValues The attribute values of the new instance
	 *
	 * @return The new entity
	 */
	protected E createEntityInstance(List<?> rAttributeValues)
	{
		Class<? extends E> rClass = rEntityClass;

		if (aTypeSubClasses != null && rTypeAttribute != null)
		{
			Object rType =
				rAttributeValues.get(getAttributeIndex(rTypeAttribute));

			if (rType != null)
			{
				Class<? extends E> rSubClass = aTypeSubClasses.get(rType);

				if (rSubClass != null)
				{
					rClass = rSubClass;
				}
			}
		}

		return ReflectUtil.newInstance(rClass);
	}

	/***************************************
	 * A method for subclasses to query the index of a particular attribute.
	 *
	 * @param  rAttr The attribute relation type
	 *
	 * @return The attribute index or -1 if not found
	 */
	protected final int getAttributeIndex(RelationType<?> rAttr)
	{
		return aAttributes.indexOf(rAttr);
	}

	/***************************************
	 * Internal initialization method that builds the map of attribute relation
	 * types.
	 *
	 * @param sEntityName  The name of this entity definition
	 * @param sIdPrefix    The prefix for global entity IDs
	 * @param rEntityClass The Entity class to be used for new instances
	 * @param rAttributes  A list containing the attribute relation types
	 */
	@SuppressWarnings("unchecked")
	protected final void init(String				sEntityName,
							  String				sIdPrefix,
							  Class<E>				rEntityClass,
							  List<RelationType<?>> rAttributes)
	{
		this.sEntityName  = sEntityName;
		this.rEntityClass = rEntityClass;

		setIdPrefix(sIdPrefix);
		aAttributes = new ArrayList<RelationType<?>>(rAttributes.size());

		for (RelationType<?> rAttribute : rAttributes)
		{
			// to prevent initialization cycles child attributes will be
			// added later through a special child definition constructor
			if (rAttribute.hasFlag(CHILD_ATTRIBUTE))
			{
				addChildAttribute(rAttribute);
			}
			else if (!rAttribute.hasFlag(EXTRA_ATTRIBUTE_FLAG))
			{
				Class<?> rTargetType = rAttribute.getTargetType();

				if (rAttribute.hasFlag(OBJECT_ID_ATTRIBUTE) &&
					Number.class.isAssignableFrom(rAttribute.getTargetType()))
				{
					rIdAttribute = (RelationType<Number>) rAttribute;
				}
				else if (rAttribute.hasFlag(OBJECT_TYPE_ATTRIBUTE))
				{
					rTypeAttribute = (RelationType<Enum<?>>) rAttribute;

					registerSubTypes();
				}
				else if (rAttribute == StandardTypes.NAME ||
						 rAttribute.hasFlag(OBJECT_NAME_ATTRIBUTE))
				{
					rNameAttribute = (RelationType<String>) rAttribute;
				}
				else if (rAttribute.hasFlag(PARENT_ATTRIBUTE))
				{
					initHierarchyAttribute(
						(RelationType<? extends Entity>) rAttribute);
				}
				else if (rAttribute.hasFlag(ROOT_ATTRIBUTE))
				{
					rRootAttribute =
						initParentAttribute(
							rRootAttribute,
							(RelationType<? extends Entity>) rAttribute);
				}
				else if (Entity.class.isAssignableFrom(rTargetType) &&
						 rTargetType != Entity.class)
				{
					// set entity mapping if not an arbitrary
					// entity reference
					if (rAttribute.get(STORAGE_MAPPING) == null)
					{
						StorageMapping<?, ?, ?> rMapping =
							StorageManager.getMapping(rTargetType);

						assert rMapping instanceof EntityDefinition;

						rAttribute.set(STORAGE_MAPPING, rMapping);
						rAttribute.set(REFERENCE_ATTRIBUTE);
					}
				}

				if (!rAttribute.hasRelation(STORAGE_DATATYPE))
				{
					rAttribute.set(
						STORAGE_DATATYPE,
						rAttribute.getTargetType());
				}

				if (!rAttribute.hasRelation(STORAGE_NAME))
				{
					rAttribute.set(STORAGE_NAME, rAttribute.getSimpleName());
				}

				aAttributes.add(rAttribute);
			}
		}

		if (rIdAttribute == null)
		{
			// if the entity has no own ID attribute insert ENTITY_ID as default
			aAttributes.add(0, ENTITY_ID);
			rIdAttribute = ENTITY_ID;
		}

		aAttributes = Collections.unmodifiableList(aAttributes);

		aAttributeDisplayProperties =
			getAttributeDisplayProperties(rEntityClass);

		set(STORAGE_NAME, getStorageName());
		initDisplayAttributes();
		checkDisableChildCounts();

		EntityManager.registerEntityType(rEntityClass, this);
	}

	/***************************************
	 * Initializes an entity reference attribute with an intermediate relation.
	 *
	 * @param aEntity        The entity to init the relation on
	 * @param rEntityRefAttr The entity reference attribute
	 * @param rReferenceId   The referenced entity's ID
	 */
	@SuppressWarnings("unchecked")
	protected void initEntityReference(E					aEntity,
									   RelationType<Entity> rEntityRefAttr,
									   Object				rReferenceId)
	{
		Class<?>		    rAttrType    = rEntityRefAttr.getTargetType();
		Function<?, Entity> fQueryEntity;

		if (rAttrType == Entity.class)
		{
			// fetch arbitrary entities by their global ID
			fQueryEntity = EntityFunctions.queryEntity();
		}
		else
		{
			fQueryEntity =
				EntityFunctions.queryEntity((Class<Entity>) rAttrType);
		}

		aEntity.set(
			rEntityRefAttr,
			(Function<Object, Entity>) fQueryEntity,
			rReferenceId);
	}

	/***************************************
	 * Initializes an attribute that refers to a relation type. If the relation
	 * type cannot be retrieved by it's name because it hasn't been initialized
	 * yet an intermediate relation will be created that tries to resolve the
	 * relation type on the first access.
	 *
	 * @param rEntity
	 * @param rRelationTypeAttr
	 * @param sTypeName
	 */
	protected void initRelationTypeAttribute(
		E							  rEntity,
		RelationType<RelationType<?>> rRelationTypeAttr,
		String						  sTypeName)
	{
		RelationType<?> rType = RelationType.valueOf(sTypeName);

		if (rType != null)
		{
			rEntity.set(rRelationTypeAttr, rType);
		}
		else
		{
			rEntity.set(rRelationTypeAttr, RelationType::valueOf, sTypeName);
		}
	}

	/***************************************
	 * Implemented to return the singleton entity definition instance for the
	 * entity class that has been read by the deserialization. Defined as
	 * protected and final so that it applies to all subclasses as well.
	 *
	 * @return The resolved entity definition instance
	 *
	 * @throws ObjectStreamException If no entity definition is available for
	 *                               the deserialized class
	 */
	protected final Object readResolve() throws ObjectStreamException
	{
		EntityDefinition<?> rDef =
			EntityManager.getEntityDefinition(rEntityClass);

		if (rDef == null)
		{
			throw new InvalidObjectException(
				"Undefined entity definition: " +
				rDef);
		}

		return rDef;
	}

	/***************************************
	 * Detaches child entities from a parent entity. This will set all parent
	 * references in the child entities to NULL and remove it from the parent's
	 * child list.
	 *
	 * @param rParent    The parent entity
	 * @param rChildAttr The child attribute to remove the child entities from
	 * @param rChildren  The child entities
	 */
	<C extends Entity> void detachChildren(E					 rParent,
										   RelationType<List<C>> rChildAttr,
										   List<C>				 rChildren)
	{
		Class<?>		    rChildType = rChildAttr.get(ELEMENT_DATATYPE);
		EntityDefinition<?> rChildDef  =
			(EntityDefinition<?>) rChildAttr.get(STORAGE_MAPPING);

		RelationType<E> rChildParentAttr = getChildParentAttribute(rChildDef);

		RelationType<List<Entity>> rChildChildAttr =
			rChildDef.getSelfReferenceAttribute();

		for (Object rChild : rChildren)
		{
			assert rChildType.isAssignableFrom(rChild.getClass());

			Entity rChildEntity = (Entity) rChild;

			if (rChildEntity.get(rChildParentAttr) == null)
			{
				throw new IllegalArgumentException(
					"Child has no parent: " +
					rChildEntity);
			}

			rChildEntity.set(rChildParentAttr, null);
			rChildDef.setHierarchyReferences(
				rChildEntity,
				rChildChildAttr,
				null,
				null);
		}
	}

	/***************************************
	 * Returns the parent object of a certain entity.
	 *
	 * @param  rEntity The entity to return the parent of
	 *
	 * @return The parent entity or NULL for none
	 */
	Entity getParent(Entity rEntity)
	{
		Entity rParent = null;

		if (rParentAttribute != null)
		{
			rParent = rEntity.get(rParentAttribute);
		}

		if (rParent == null && rMasterAttribute != null)
		{
			rParent = rEntity.get(rMasterAttribute);
		}

		return rParent;
	}

	/***************************************
	 * Adds a child attribute to this definition.
	 *
	 * @param rAttribute The relation type of the child attribute to add
	 */
	@SuppressWarnings("unchecked")
	private void addChildAttribute(RelationType<?> rAttribute)
	{
		if (aChildAttributes == null)
		{
			aChildAttributes =
				new LinkedHashMap<EntityDefinition<?>,
								  RelationType<List<Entity>>>();
		}

		Class<? extends Entity> rChildClass =
			(Class<? extends Entity>) rAttribute.get(ELEMENT_DATATYPE);

		EntityDefinition<? extends Entity> rChildDef =
			EntityManager.getEntityDefinition(rChildClass);

		StorageMapping<?, ?, ?> rMapping = rAttribute.get(STORAGE_MAPPING);

		if (rMapping == null)
		{
			rAttribute.set(STORAGE_MAPPING, rChildDef);
		}
		else if (rMapping != rChildDef)
		{
			throw new IllegalStateException(
				"Duplicate child attribute with " +
				"different entity definition: " + rAttribute);
		}

		aChildAttributes.put(
			rChildDef,
			(RelationType<List<Entity>>) rAttribute);

		if (rChildClass == rEntityClass)
		{
			rHierarchyChildAttribute = (RelationType<List<Entity>>) rAttribute;
		}
	}

	/***************************************
	 * Helper method to add non-null properties to another properties object.
	 *
	 * @param rProperties           The properties to add to
	 * @param rAdditionalProperties The properties to add
	 */
	private void addProperties(
		MutableProperties rProperties,
		HasProperties	  rAdditionalProperties)
	{
		if (rAdditionalProperties != null)
		{
			rProperties.setProperties(rAdditionalProperties, true);
		}
	}

	/***************************************
	 * Checks whether the generation of child count columns in SQL should be
	 * disabled.
	 */
	private void checkDisableChildCounts()
	{
		Boolean rDisableChildCounts =
			(Boolean) getStaticFieldValue(
				rEntityClass,
				DISABLE_SQL_CHILD_COUNT_FIELD);

		if ((rDisableChildCounts != null && rDisableChildCounts.booleanValue()))
		{
			set(JdbcRelationTypes.SQL_DISABLE_CHILD_COUNTS);
		}
	}

	/***************************************
	 * Checks that a new parent attribute doesn't exist already and returns the
	 * new attribute if appropriate.
	 *
	 * @param  rCurrentAttribute The current attribute value which must be NULL
	 *                           or else an exception will be thrown
	 * @param  rNewAttribute     The new attribute value
	 *
	 * @throws IllegalStateException If the current attribute is not NULL
	 */
	private void checkParentAttribute(
		RelationType<? extends Entity> rCurrentAttribute,
		RelationType<? extends Entity> rNewAttribute)
	{
		if (rCurrentAttribute != null && rCurrentAttribute != rNewAttribute)
		{
			throw new IllegalStateException(
				String.format(
					"Duplicate hierarchy attribute: %s and %s",
					rCurrentAttribute,
					rNewAttribute));
		}
	}

	/***************************************
	 * Internal method to create a new instance of the described entity from a
	 * list of attribute values.
	 *
	 * @param  rAttributeValues The attribute values
	 *
	 * @return The new entity instance
	 *
	 * @throws StorageException If retrieving a referenced entity fails
	 */
	@SuppressWarnings("unchecked")
	private E createNewEntity(List<?> rAttributeValues) throws StorageException
	{
		E   aEntity     = createEntityInstance(rAttributeValues);
		int nValueIndex = 0;

		aEntity.set(INITIALIZING);

		for (RelationType<?> rAttr : aAttributes)
		{
			Object rValue = rAttributeValues.get(nValueIndex++);

			// omit parent and master attributes for child objects if parents
			// are cached because then these will be set by addChildren() of
			// the parent from tryToGetFromParent(); the caching test from
			// createObject() therefore needs to be repeated here
			if (isHierarchyAttribute(rAttr) &&
				EntityManager.isCachingEnabledFor(getMappedType()))
			{
				if (rValue != null)
				{
					Number rId = (Number) rValue;

					if (rAttr == rMasterAttribute)
					{
						aEntity.set(MASTER_ENTITY_ID, rId.longValue());
					}
					else if (rAttr == rParentAttribute)
					{
						aEntity.set(PARENT_ENTITY_ID, rId.longValue());
					}
				}
			}
			else if (rValue != null)
			{
				Class<?> rAttrType = rAttr.getTargetType();

				if (Entity.class.isAssignableFrom(rAttrType))
				{
					initEntityReference(
						aEntity,
						(RelationType<Entity>) rAttr,
						rValue);
				}
				else if (RelationType.class.isAssignableFrom(rAttrType))
				{
					initRelationTypeAttribute(
						aEntity,
						(RelationType<RelationType<?>>) rAttr,
						rValue.toString());
				}
				else
				{
					try
					{
						rValue = checkAttributeValue(rAttr, rValue);
						aEntity.set((RelationType<Object>) rAttr, rValue);
					}
					catch (IllegalArgumentException e)
					{
						String sMessage =
							String.format(
								"Could not map attribute %s.%s: %s",
								getEntityName(),
								rAttr,
								e.getMessage());

						throw new StorageException(sMessage, e);
					}
				}
			}
		}

		aEntity.deleteRelation(INITIALIZING);

		return aEntity;
	}

	/***************************************
	 * Returns the attribute relation types of the given entity. First checks
	 * for a static field with the name {@link #ENTITY_ATTRIBUTES_FIELD}. If
	 * that doesn't exist it collections all static final fields with the
	 * datatype {@link RelationType}.
	 *
	 * @param  rEntityClass The entity class to query the relation types from
	 *
	 * @return The list of attribute relation types
	 */
	private List<RelationType<?>> getAttributeTypes(Class<E> rEntityClass)
	{
		@SuppressWarnings("unchecked")
		List<RelationType<?>> rAttributes =
			(List<RelationType<?>>) getStaticFieldValue(
				rEntityClass,
				ENTITY_ATTRIBUTES_FIELD);

		if (rAttributes == null)
		{
			rAttributes =
				ReflectUtil.collectConstants(
					rEntityClass,
					RelationType.class,
					null,
					true,
					true,
					true);
		}

		return rAttributes;
	}

	/***************************************
	 * Returns a child entity with a certain ID from a parent entity.
	 *
	 * @param  rParent The parent entity to get the child from
	 * @param  rId     The child entity's ID
	 *
	 * @return The corresponding child entity or NULL if none could be found
	 */
	private E getChild(Entity rParent, Number rId)
	{
		@SuppressWarnings("unchecked")
		EntityDefinition<Entity> rParentDef =
			(EntityDefinition<Entity>) rParent.getDefinition();

		@SuppressWarnings("unchecked")
		Collection<E> rChildren =
			(Collection<E>) rParentDef.getChildren(rParent, this);

		for (E rChild : rChildren)
		{
			if (rId.equals(rChild.get(rIdAttribute)))
			{
				return rChild;
			}
		}

		return null;
	}

	/***************************************
	 * Returns the parent attribute of a child definition that refers to
	 * entities of this definition.
	 *
	 * @param  rChildDef The child definition
	 *
	 * @return The parent attribute
	 *
	 * @throws IllegalArgumentException If there is no parent reference for this
	 *                                  definition in the child definition
	 */
	private RelationType<E> getChildParentAttribute(
		EntityDefinition<?> rChildDef)
	{
		@SuppressWarnings("unchecked")
		RelationType<E> rChildParentAttr =
			(RelationType<E>) rChildDef.getParentAttribute(this);

		if (rChildParentAttr == null)
		{
			throw new IllegalArgumentException(
				String.format(
					"No parent attribute in %s for %s",
					rChildDef,
					this));
		}

		return rChildParentAttr;
	}

	/***************************************
	 * Returns the storage name for this definition. This method first tries to
	 * access the field with the name {@link #STORAGE_NAME_FIELD}. If that
	 * doesn't exist the storage name will be generated from the entity name.
	 *
	 * @return The storage name
	 */
	private String getStorageName()
	{
		String sStorageName =
			(String) getStaticFieldValue(rEntityClass, STORAGE_NAME_FIELD);

		if (sStorageName == null)
		{
			sStorageName = TextUtil.toPlural(sEntityName);
		}

		return sStorageName;
	}

	/***************************************
	 * Performs the state changes necessary if an entity's modified state is
	 * toggled.
	 *
	 * @param rEntity   The entity
	 * @param bModified The new modified state
	 */
	private void handleEntityModification(Entity rEntity, boolean bModified)
	{
		if (bModified)
		{
			EntityManager.beginEntityModification(rEntity);

			Entity rParent = rEntity.getParent();

			if (rParent != null)
			{
				// also recursively mark the parent hierarchy as modified to
				// ensure evaluation of the parents on store
				rParent.set(MODIFIED);
			}
		}
		else
		{
			EntityManager.endEntityModification(rEntity);

			// reset all PREVIOUS_VALUE annotations if the entity
			// modification is reset (i.e. the entity has been stored)
			for (RelationType<?> rAttr : aAttributes)
			{
				Relation<?> rRelation = rEntity.getRelation(rAttr);

				if (rRelation != null)
				{
					rRelation.deleteRelation(PREVIOUS_VALUE);
				}
			}

			if (aChildAttributes != null)
			{
				for (RelationType<?> rChildAttr : aChildAttributes.values())
				{
					Relation<?> rChildRelation = getRelation(rChildAttr);

					if (rChildRelation != null)
					{
						rChildRelation.deleteRelation(REMOVED_CHILDREN);
					}
				}
			}
		}
	}

	/***************************************
	 * Initializes the lists of display attributes.
	 */
	private void initDisplayAttributes()
	{
		for (DisplayMode rDisplayMode : DisplayMode.values())
		{
			RelationType<?>[] rModeAttributes =
				(RelationType<?>[]) getStaticFieldValue(
					rEntityClass,
					DISPLAY_ATTRIBUTES_FIELD_PREFIX + rDisplayMode);

			if (rModeAttributes != null)
			{
				assert rModeAttributes.length > 0;

				List<RelationType<?>> rAttrList =
					Collections.unmodifiableList(
						Arrays.asList(rModeAttributes));

				aDisplayAttributes.put(rDisplayMode, rAttrList);
			}
		}

		// default attributes which will only be set if no attributes have been
		// set above for the given display mode
		setDisplayAttributes(DisplayMode.MINIMAL, rIdAttribute, NAME);
		setDisplayAttributes(DisplayMode.COMPACT, rIdAttribute, NAME, INFO);
		setDisplayAttributes(DisplayMode.HIERARCHICAL, aAttributes);

		if (!aDisplayAttributes.containsKey(DisplayMode.FULL))
		{
			List<RelationType<?>> aSimpleAttributes =
				new ArrayList<RelationType<?>>();

			for (RelationType<?> rAttribute : aAttributes)
			{
				if (!Entity.class.isAssignableFrom(rAttribute.getTargetType()))
				{
					aSimpleAttributes.add(rAttribute);
				}
			}

			setDisplayAttributes(DisplayMode.FULL, aSimpleAttributes);
		}
	}

	/***************************************
	 * Internal method to initialize the parent attribute for either a
	 * parent-child or a master-detail relationship.
	 *
	 * @param rAttribute The attribute relation type
	 */
	private void initHierarchyAttribute(
		RelationType<? extends Entity> rAttribute)
	{
		if (rAttribute.getTargetType() == rEntityClass)
		{
			rParentAttribute =
				initParentAttribute(rParentAttribute, rAttribute);
		}
		else
		{
			@SuppressWarnings("unchecked")
			Class<? extends Entity> rMasterClass =
				(Class<? extends Entity>) rAttribute.getTargetType();

			checkParentAttribute(rMasterAttribute, rAttribute);

			if (rMasterAttribute == null)
			{
				// update only if not set already
				rMasterAttribute = rAttribute;
				rMasterAttribute.set(
					STORAGE_MAPPING,
					EntityManager.getEntityDefinition(rMasterClass));
			}
		}
	}

	/***************************************
	 * Initializes a parent attribute for this definition. Such attributes are
	 * identified by one of the flags {@link MetaTypes#PARENT_ATTRIBUTE} or
	 * {@link EntityRelationTypes#ROOT_ATTRIBUTE}.
	 *
	 * @param  rParentAttr The current value of the parent attribute to init
	 * @param  rNewAttr    The new attribute value
	 *
	 * @return
	 */
	private RelationType<? extends Entity> initParentAttribute(
		RelationType<? extends Entity> rParentAttr,
		RelationType<? extends Entity> rNewAttr)
	{
		checkParentAttribute(rParentAttr, rNewAttr);

		if (rParentAttr == null)
		{
			// update only if not set already
			rParentAttr = rNewAttr;
			rParentAttr.set(STORAGE_MAPPING, this);
		}

		return rParentAttr;
	}

	/***************************************
	 * Registers this definition also for sub-classes of this definitions entity
	 * type if they are marked as sub-types without separate definition.
	 */
	private void registerSubTypes()
	{
		Enum<?>[] rEntityTypes =
			(Enum<?>[]) rTypeAttribute.getTargetType().getEnumConstants();

		for (Enum<?> eType : rEntityTypes)
		{
			String sSubTypeClass =
				rEntityClass.getPackage().getName() + "." +
				TextConvert.capitalizedIdentifier(eType.toString());

			try
			{
				@SuppressWarnings("unchecked")
				Class<? extends E> rTypeClass =
					(Class<? extends E>) Class.forName(sSubTypeClass);

				if (rEntityClass.isAssignableFrom(rTypeClass))
				{
					if (aTypeSubClasses == null)
					{
						aTypeSubClasses = new HashMap<>();
						aSubClassTypes  = new HashMap<>();
					}

					EntityManager.registerEntitySubType(rTypeClass, this);
					aTypeSubClasses.put(eType.toString(), rTypeClass);
					aSubClassTypes.put(rTypeClass, eType);
				}
			}
			catch (ClassNotFoundException e)
			{
				// ignore non-existing sub-type and use the base class
			}
		}
	}

	/***************************************
	 * Registers a set of entity attributes for a certain display mode. This
	 * method should be invoked by subclasses to set the attributes that they
	 * want to be displayed in a certain mode. The initialization code sets the
	 * display mode to defaults based on standard properties but in most cases
	 * custom settings should be used.
	 *
	 * <p>There is also a variable argument list variant of this method that
	 * provides a convenient way to register specific attribute types. See
	 * {@link #setDisplayAttributes(DisplayMode, RelationType...)}.</p>
	 *
	 * @param rDisplayMode       The display mode to register the attributes for
	 * @param rDisplayAttributes A collection of the attribute relation types to
	 *                           display in the given mode
	 */
	private void setDisplayAttributes(
		DisplayMode			  rDisplayMode,
		List<RelationType<?>> rDisplayAttributes)
	{
		if (!aDisplayAttributes.containsKey(rDisplayMode))
		{
			assert rDisplayAttributes != null && rDisplayAttributes.size() > 0;

			aDisplayAttributes.put(rDisplayMode, rDisplayAttributes);
		}
	}

	/***************************************
	 * Sets the display attributes for a certain display mode.
	 *
	 * @see #setDisplayAttributes(DisplayMode, List)
	 */
	private void setDisplayAttributes(
		DisplayMode		   rDisplayMode,
		RelationType<?>... rDisplayAttributes)
	{
		if (!aDisplayAttributes.containsKey(rDisplayMode))
		{
			List<RelationType<?>> aAttributeList =
				new ArrayList<RelationType<?>>(
					Arrays.asList(rDisplayAttributes));

			for (RelationType<?> rAttribute : rDisplayAttributes)
			{
				if (!aAttributes.contains(rAttribute))
				{
					aAttributeList.remove(rAttribute);
				}
			}

			setDisplayAttributes(rDisplayMode, aAttributeList);
		}
	}

	/***************************************
	 * Sets the master and root entities recursively on an entity hierarchy. The
	 * master attribute will always be set, the root attribute only if it is not
	 * NULL.
	 *
	 * @param rEntity         The starting entity of the hierarchy
	 * @param rChildAttribute The child attribute to descend the entity's
	 *                        hierarchy at or NULL for none
	 * @param rMaster         The new master entity (may be NULL to clear)
	 * @param rRoot           The new root entity (NULL to ignore)
	 */
	@SuppressWarnings("unchecked")
	private void setHierarchyReferences(
		Entity					   rEntity,
		RelationType<List<Entity>> rChildAttribute,
		Entity					   rMaster,
		Entity					   rRoot)
	{
		if (rMasterAttribute != null)
		{
			rEntity.set((RelationType<Entity>) rMasterAttribute, rMaster);
		}

		if (rRootAttribute != null)
		{
			rEntity.set((RelationType<Entity>) rRootAttribute, rRoot);

			// the (first) root may be set to NULL if hierarchy levels change
			// but then subsequent levels need to reference the current entity
			// as their root
			if (rRoot == null)
			{
				rRoot = rEntity;
			}
		}

		if (rChildAttribute != null)
		{
			List<Entity> rChildren = rEntity.get(rChildAttribute);

			// prevent unnecessary querying of child lists during initialization
			if (!(rEntity.hasFlag(INITIALIZING) &&
				  rChildren instanceof QueryList))
			{
				for (Entity rChild : rChildren)
				{
					setHierarchyReferences(
						rChild,
						rChildAttribute,
						rMaster,
						rRoot);
				}
			}
		}
	}

	/***************************************
	 * Sets the ID prefix and generates it if necessary.
	 *
	 * @param sPrefix The prefix to set or NULL to query it from the field
	 *                {@link #ID_PREFIX_FIELD} or to generate it
	 */
	private void setIdPrefix(String sPrefix)
	{
		if (sPrefix == null)
		{
			sPrefix =
				(String) getStaticFieldValue(rEntityClass, ID_PREFIX_FIELD);

			if (sPrefix == null)
			{
				// default value: the upper case letters of the class name
				sPrefix =
					rEntityClass.getSimpleName()
								.replaceAll("\\p{javaLowerCase}*", "");
			}
		}

		sIdPrefix = sPrefix;
	}

	/***************************************
	 * Internal method to check whether a list of attributes contains a
	 * reference to a parent entity and to return the corresponding parent
	 * entity if possible.
	 *
	 * @param  rAttributeValues The list of attribute values to check
	 *
	 * @return The parent entity or NULL for none
	 *
	 * @throws StorageException If retrieving the parent entity fails
	 */
	private E tryToGetFromParent(List<?> rAttributeValues)
		throws StorageException
	{
		RelationType<?> rParentAttr = null;
		Entity		    rParent     = null;
		Number		    rParentId   = null;
		E			    rEntity     = null;

		if (rParentAttribute != null)
		{
			int nParentIdIndex = aAttributes.indexOf(rParentAttribute);

			rParentId = (Number) rAttributeValues.get(nParentIdIndex);

			if (rParentId != null)
			{
				rParentAttr = rParentAttribute;
			}
		}

		if (rParentAttr == null && rMasterAttribute != null)
		{
			int nParentIdIndex = aAttributes.indexOf(rMasterAttribute);

			rParentId = (Number) rAttributeValues.get(nParentIdIndex);

			if (rParentId != null)
			{
				rParentAttr = rMasterAttribute;
			}
		}

		if (rParentAttr != null)
		{
			@SuppressWarnings("unchecked")
			Class<Entity> rParentClass =
				(Class<Entity>) rParentAttr.getTargetType();

			rParent =
				EntityManager.queryEntity(rParentClass, rParentId.intValue());

			if (rParent != null)
			{
				int    nIdIndex = aAttributes.indexOf(rIdAttribute);
				Number rId	    = (Number) rAttributeValues.get(nIdIndex);

				rEntity = getChild(rParent, rId);
			}
		}

		return rEntity;
	}
}
