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
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.logging.Log;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.property.HasProperties;
import de.esoco.lib.property.MutableProperties;
import de.esoco.lib.property.StringProperties;
import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.lib.text.TextConvert;
import de.esoco.storage.QueryList;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;
import de.esoco.storage.StorageMapping;
import de.esoco.storage.impl.jdbc.JdbcRelationTypes;
import de.esoco.storage.mapping.AbstractStorageMapping;
import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationEvent;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

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

/**
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
	implements EventHandler<RelationEvent<?>>, Serializable {

	/**
	 * Enumeration of entity display modes
	 */
	public enum DisplayMode {MINIMAL, COMPACT, FULL, HIERARCHICAL}

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

	private static final long serialVersionUID = 1L;

	private final transient Map<DisplayMode, List<RelationType<?>>>
		displayAttributes = new EnumMap<>(DisplayMode.class);

	/**
	 *
	 */
	private Class<E> entityClass;

	private transient String entityName;

	private transient String idPrefix;

	private transient RelationType<? extends Number> idAttribute;

	private transient RelationType<Enum<?>> typeAttribute;

	private transient RelationType<String> nameAttribute;

	private transient RelationType<? extends Entity> masterAttribute;

	private transient RelationType<? extends Entity> parentAttribute;

	private transient RelationType<? extends Entity> rootAttribute;

	private transient RelationType<List<Entity>> hierarchyChildAttribute;

	private transient List<RelationType<?>> attributes;

	private transient Map<EntityDefinition<?>, RelationType<List<Entity>>>
		childAttributes;

	private transient Map<String, Class<? extends E>> typeSubClasses = null;

	private transient Map<Class<? extends E>, Enum<?>> subClassTypes = null;

	private transient Map<RelationType<?>, ? extends HasProperties>
		attributeDisplayProperties;

	/**
	 * Creates a new instance without initializing it. Subclasses that use this
	 * constructor must invoke the init method or else this instance will be in
	 * an unusable state.
	 */
	protected EntityDefinition() {
	}

	/**
	 * Creates a new instance from a certain entity class. Internally the
	 * method
	 * {@link #init(String, String, Class, List)} will be invoked which will
	 * then evaluate the relation type declarations in the given entity
	 * class to
	 * define the entity structure.
	 *
	 * @param entityClass The entity class to create the definition for
	 */
	protected EntityDefinition(Class<E> entityClass) {
		this(entityClass, null);
	}

	/**
	 * Creates a new definition from a certain entity class which contains the
	 * attribute relation type definitions.
	 *
	 * @param entityClass The entity class to create the definition for
	 * @param idPrefix    The prefix for global entity IDs
	 */
	protected EntityDefinition(Class<E> entityClass, String idPrefix) {
		RelationTypes.init(entityClass);

		ObjectRelations.getRelatable(entityClass).set(STORAGE_MAPPING, this);

		init(entityClass.getSimpleName(), idPrefix, entityClass,
			getAttributeTypes(entityClass));
	}

	/**
	 * Initializes the attribute-specific display properties that may be stored
	 * in the optional field {@link #ATTRIBUTE_DISPLAY_PROPERTIES_FIELD} of an
	 * entity class.
	 *
	 * @param entityClass The entity class
	 * @return The mapping from attributes to display properties
	 */
	@SuppressWarnings("unchecked")
	static Map<RelationType<?>, MutableProperties> getAttributeDisplayProperties(
		Class<? extends Entity> entityClass) {
		return (Map<RelationType<?>, MutableProperties>) getStaticFieldValue(
			entityClass, ATTRIBUTE_DISPLAY_PROPERTIES_FIELD);
	}

	/**
	 * Returns the value of a certain static field from an entity class.
	 *
	 * @param entityClass The entity class
	 * @param fieldName   The name of the static field to access
	 * @return The field value
	 */
	static Object getStaticFieldValue(Class<? extends Entity> entityClass,
		String fieldName) {
		Object value;

		try {
			Field field = entityClass.getDeclaredField(fieldName);

			if (!Modifier.isPublic(field.getModifiers())) {
				field.setAccessible(true);
			}

			value = field.get(null);
		} catch (NoSuchFieldException e) {
			value = null;
		} catch (Exception e) {
			throw new IllegalStateException(
				"Could not access field " + fieldName);
		}

		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object checkAttributeValue(RelationType<?> attribute,
		Object value) {
		if (value != null &&
			Entity.class.isAssignableFrom(attribute.getTargetType())) {
			if (value instanceof String) {
				value = EntityManager.queryEntity((String) value);
			} else if (value instanceof Integer) {
				int id = ((Integer) value).intValue();

				@SuppressWarnings("unchecked")
				Class<? extends Entity> entityType =
					(Class<Entity>) attribute.getTargetType();

				value = EntityManager.queryEntity(entityType, id);
			}
		} else {
			value = super.checkAttributeValue(attribute, value);
		}

		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E createObject(List<?> attributeValues, boolean asChild) {
		boolean hasCaching =
			EntityManager.isCachingEnabledFor(getMappedType());

		E entity = null;

		if (!asChild && hasCaching) {
			entity = tryToGetFromParent(attributeValues);
		}

		if (entity == null) {
			try {
				entity = createNewEntity(attributeValues);
			} catch (Exception e) {
				Log.errorf(e, "Error creating entity %s from data %s",
					entityClass.getSimpleName(), attributeValues);

				throw e;
			}
		}

		return entity;
	}

	/**
	 * Returns the attribute relation type that corresponds to a particular
	 * attribute name. This method is mainly intended to support the
	 * implementation of attribute mappings that are defined with strings.
	 *
	 * @param attributeName The attribute name to return the type for
	 * @return The matching attribute type or NULL if no such instance exists
	 */
	public final RelationType<?> getAttribute(String attributeName) {
		for (RelationType<?> attr : attributes) {
			if (attr.getName().equals(attributeName)) {
				return attr;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getAttributeDatatype(RelationType<?> attribute) {
		return attribute.getTargetType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAttributeValue(E entity, RelationType<?> attribute) {
		return entity.get(attribute);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<RelationType<?>> getAttributes() {
		return attributes;
	}

	/**
	 * Returns the relation types of the child attributes of this definition.
	 *
	 * @return A collection of the child attribute relation types
	 */
	public Collection<RelationType<List<Entity>>> getChildAttributes() {
		if (childAttributes != null) {
			return childAttributes.values();
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<EntityDefinition<?>> getChildMappings() {
		if (childAttributes != null) {
			return childAttributes.keySet();
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Entity> getChildren(E parent,
		EntityDefinition<?> childMapping) {
		return parent.get(childAttributes.get(childMapping));
	}

	/**
	 * Overridden to return a prediate for a type attribute value if a specific
	 * sub-class of the entity type exists.
	 *
	 * @see StorageMapping#getDefaultCriteria(Class)
	 */
	@Override
	public Predicate<E> getDefaultCriteria(Class<? extends E> type) {
		Predicate<E> defaultCriteria = null;

		if (subClassTypes != null) {
			Enum<?> subClassType = subClassTypes.get(type);

			if (type != null) {
				defaultCriteria =
					typeAttribute.is(Predicates.equalTo(subClassType));
			}
		}

		return defaultCriteria;
	}

	/**
	 * Returns the attributes for a certain display mode.
	 *
	 * @param displayMode The display mode
	 * @return A collection containing the attributes for the display mode
	 */
	public List<RelationType<?>> getDisplayAttributes(DisplayMode displayMode) {
		return displayAttributes.get(displayMode);
	}

	/**
	 * Returns the display properties for a certain attribute. Always returns a
	 * new properties object, even if empty, that can be modified freely by the
	 * receiver.
	 *
	 * @param attribute The attribute to return the display properties for
	 * @return The display properties for the given attribute
	 */
	public MutableProperties getDisplayProperties(RelationType<?> attribute) {
		StringProperties displayProperties = new StringProperties();

		addProperties(displayProperties, attribute.get(DISPLAY_PROPERTIES));

		if (attributeDisplayProperties != null) {
			addProperties(displayProperties,
				attributeDisplayProperties.get(attribute));
		}

		return displayProperties;
	}

	/**
	 * Returns the descriptive entity name.
	 *
	 * @return The entity name
	 */
	public final String getEntityName() {
		return entityName;
	}

	/**
	 * Returns the child attribute for a hierarchy of entities of the same type
	 * as the entity type described by this definition.
	 *
	 * @return The hierarchy child attribute or NULL for none
	 */
	public final RelationType<List<Entity>> getHierarchyChildAttribute() {
		return hierarchyChildAttribute;
	}

	/**
	 * Returns the relation type of the numeric ID attribute in this
	 * definition.
	 * The exact datatype depends on the actual ID attribute relation type.
	 * Typically this will be either {@link Integer} or {@link Long}.
	 *
	 * <p>If an entity does not define it's own ID attribute the default ID
	 * type {@link EntityRelationTypes#ENTITY_ID} will be returned.</p>
	 *
	 * @see AbstractStorageMapping#getIdAttribute()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public RelationType<Number> getIdAttribute() {
		return (RelationType<Number>) idAttribute;
	}

	/**
	 * Returns the prefix that will be added to global entity IDs.
	 *
	 * @return The ID prefix
	 */
	public final String getIdPrefix() {
		return idPrefix;
	}

	/**
	 * @see AbstractStorageMapping#getMappedType()
	 */
	@Override
	public Class<E> getMappedType() {
		return entityClass;
	}

	/**
	 * Returns the master attribute if such exists.
	 *
	 * @return The master attribute or NULL for none
	 */
	public RelationType<? extends Entity> getMasterAttribute() {
		return masterAttribute;
	}

	/**
	 * Returns the relation type of an attribute in this definition that refers
	 * to the name of an entity. This will either be the attribute with the
	 * type
	 * {@link StandardTypes#NAME} or any other string attribute that has the
	 * meta relation type {@link MetaTypes#OBJECT_NAME_ATTRIBUTE} set.
	 *
	 * @return The name attribute of the entity or NULL for none
	 */
	public RelationType<String> getNameAttribute() {
		return nameAttribute;
	}

	/**
	 * Returns the parent attribute if such exists.
	 *
	 * @return The parent attribute or NULL for none
	 */
	public RelationType<? extends Entity> getParentAttribute() {
		return parentAttribute;
	}

	/**
	 * @see AbstractStorageMapping#getParentAttribute(StorageMapping)
	 */
	@Override
	public RelationType<? extends Entity> getParentAttribute(
		StorageMapping<?, ?, ?> parentDefinition) {
		RelationType<? extends Entity> result = null;

		if (parentAttribute != null &&
			parentAttribute.get(STORAGE_MAPPING) == parentDefinition) {
			result = parentAttribute;
		} else if (masterAttribute != null &&
			masterAttribute.get(STORAGE_MAPPING) == parentDefinition) {
			result = masterAttribute;
		}

		return result;
	}

	/**
	 * Returns the root parent attribute if such exists.
	 *
	 * @return The root attribute or NULL for none
	 */
	public RelationType<? extends Entity> getRootAttribute() {
		return rootAttribute;
	}

	/**
	 * Returns the hierarchy attribute of this definition that references
	 * entities of the same type.
	 *
	 * @return The attribute or NULL for none
	 */
	public RelationType<List<Entity>> getSelfReferenceAttribute() {
		RelationType<List<Entity>> attr = null;

		if (childAttributes != null) {
			attr = childAttributes.get(this);
		}

		return attr;
	}

	/**
	 * Returns the relation type of an attribute in this definition that
	 * defines
	 * the type of an entity. If a definition subclass does not define such a
	 * type attribute with an enum datatype and a meta-relation with the flag
	 * type {@link MetaTypes#OBJECT_TYPE_ATTRIBUTE} set to TRUE this method
	 * will
	 * return NULL.
	 *
	 * @return The type attribute of this definition or NULL for none
	 */
	public RelationType<Enum<?>> getTypeAttribute() {
		return typeAttribute;
	}

	/**
	 * Event handler implementation that will be registered as a relation
	 * listener on entities to perform the modification tracking for entity
	 * attributes.
	 *
	 * @see EventHandler#handleEvent(de.esoco.lib.event.Event)
	 */
	@Override
	@SuppressWarnings("boxing")
	public void handleEvent(RelationEvent<?> event) {
		Entity entity = (Entity) event.getSource();

		if (!entity.hasFlag(INITIALIZING)) {
			RelationType<?> relationType = event.getElement().getType();
			Object updateValue = event.getUpdateValue();

			if (relationType == MODIFIED) {
				// can be NULL if relations are copied between objects
				if (updateValue != null) {
					handleEntityModification(entity, (Boolean) updateValue);
				}
			}

			// exclude ID attribute as it will typically be set after automatic
			// generation by the database (causing an update event for a newly
			// persisted entity). Cases where ONLY the ID is set manually on a
			// persistent object would therefore be excluded from modification
			// detection and need to be handled explicitly (by setting
			// MODIFIED).
			else if (relationType != idAttribute &&
				attributes.contains(relationType)) {
				handleAttributeValueChange(event, entity, updateValue);
			}
		}
	}

	/**
	 * @see AbstractStorageMapping#initChildren(Object, List, StorageMapping)
	 */
	@Override
	public void initChildren(E parent, List<?> children,
		EntityDefinition<?> childDef) {
		initChildren(parent, children, childDef, true);
	}

	/**
	 * Initializes the child entities of a parent by setting the parent
	 * attributes accordingly. This includes the root and master attributes if
	 * available.
	 *
	 * @param parent       The parent entity of the children
	 * @param children     The list of child entities to initialize
	 * @param childDef     The entity definition of the children
	 * @param initializing TRUE if this call occurs during the
	 *                        initialization of
	 *                     the object tree or FALSE for the manipulation of an
	 *                     existing object hierarchy
	 */
	@SuppressWarnings("unchecked")
	public void initChildren(E parent, List<?> children,
		EntityDefinition<?> childDef, boolean initializing) {
		if (parent.getDefinition() != this) {
			throw new IllegalArgumentException("Wrong parent for children");
		}

		RelationType<List<Entity>> childAttr = childAttributes.get(childDef);

		Class<?> childType = childAttr.get(ELEMENT_DATATYPE);
		Entity master = null;
		Entity root = null;

		RelationType<E> childParentAttr = getChildParentAttribute(childDef);

		RelationType<List<Entity>> childChildAttr =
			childDef.getSelfReferenceAttribute();

		if (childDef.masterAttribute != null) {
			if (this == childDef) {
				master = parent.get((RelationType<Entity>) masterAttribute);
			} else {
				master = parent;
			}
		}

		if (this == childDef && rootAttribute != null) {
			root = parent.get((RelationType<Entity>) rootAttribute);

			if (root == null) {
				root = parent;
			}
		}

		for (Object child : children) {
			assert childType.isAssignableFrom(child.getClass());

			Entity childEntity = (Entity) child;
			E childParent = childEntity.get(childParentAttr);

			if (childParent != null && childParent.getId() != parent.getId()) {
				throw new IllegalArgumentException(
					"Child already has other parent: " +
						childEntity.get(childParentAttr));
			}

			if (initializing) {
				childEntity.set(INITIALIZING);
			}

			childEntity.set(childParentAttr, parent);
			childDef.setHierarchyReferences(childEntity, childChildAttr,
				master,
				root);

			childEntity.deleteRelation(INITIALIZING);
		}
	}

	/**
	 * Forwarded to {@link EntityManager#isDeletionEnabledFor(Class)}.
	 *
	 * @see AbstractStorageMapping#isDeleteAllowed()
	 */
	@Override
	public boolean isDeleteAllowed() {
		return EntityManager.isDeletionEnabledFor(entityClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isHierarchyAttribute(RelationType<?> attribute) {
		return attribute == parentAttribute || attribute == rootAttribute ||
			attribute == masterAttribute;
	}

	/**
	 * Overridden to map entity references to entity IDs.
	 *
	 * @see AbstractStorageMapping#mapValue(Relatable, Object)
	 */
	@Override
	@SuppressWarnings("boxing")
	public Object mapValue(RelationType<?> attribute, Object value) {
		if (value instanceof Entity) {
			Entity referencedEntity = (Entity) value;

			// if value is a specific entity (i.e. with a specific entity
			// subclass as the attribute type) only the entity ID will be
			// returned; arbitrary entity references will be mapped to their
			// global ID
			if (attribute.getTargetType() != Entity.class) {
				value = referencedEntity.getId();
			} else {
				value = EntityManager.getGlobalEntityId(referencedEntity);
			}
		} else {
			value = super.mapValue(attribute, value);
		}

		return value;
	}

	/**
	 * @see AbstractStorageMapping#setAttributeValue(Object, Relatable, Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setAttributeValue(E entity, RelationType<?> attribute,
		Object value) {
		// will throw an exception if the value type is inappropriate
		entity.set((RelationType<Object>) attribute, value);
	}

	/**
	 * @see AbstractStorageMapping#setChildren(Object, List, StorageMapping)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setChildren(E parent, List<?> children,
		EntityDefinition<?> childDef) {
		RelationType<List<Entity>> childAttribute =
			childAttributes.get(childDef);

		parent.set(childAttribute, (List<Entity>) children);
	}

	/**
	 * Overridden to store the referenced entity with
	 * {@link EntityManager#storeEntity(Entity, Entity)}.
	 *
	 * @see AbstractStorageMapping#storeReference(Relatable, Object)
	 */
	@Override
	public void storeReference(Relatable sourceObject, E referencedEntity) {
		try {
			EntityManager.storeEntity(referencedEntity,
				sourceObject.get(ENTITY_STORE_ORIGIN));
		} catch (TransactionException e) {
			throw new StorageException(e);
		}
	}

	/**
	 * Returns a string representation of this entity for debugging purpose,
	 * which includes a list of it's attribute types.
	 *
	 * @return The debug string representation of this definition
	 */
	public String toDebugString() {
		return getClass().getSimpleName() + attributes +
			(childAttributes != null ? "," + childAttributes.values() : "");
	}

	/**
	 * Returns a string representation of this definition.
	 *
	 * @see Object#toString()
	 */
	@Override
	public final String toString() {
		return String.format("EntityDefinition[%s]", entityName);
	}

	/**
	 * Creates a new instance of the entity class. If the entity has a type
	 * attribute and a subclass that has the same name as the type value in the
	 * attribute values (converted to camel case) a subclass of that type will
	 * be created instead.
	 *
	 * @param attributeValues The attribute values of the new instance
	 * @return The new entity
	 */
	protected E createEntityInstance(List<?> attributeValues) {
		Class<? extends E> type = entityClass;

		if (typeSubClasses != null && typeAttribute != null) {
			Object typeValue =
				attributeValues.get(getAttributeIndex(typeAttribute));

			if (typeValue != null) {
				Class<? extends E> subClass = typeSubClasses.get(type);

				if (subClass != null) {
					type = subClass;
				}
			}
		}

		return ReflectUtil.newInstance(type);
	}

	/**
	 * A method for subclasses to query the index of a particular attribute.
	 *
	 * @param attr The attribute relation type
	 * @return The attribute index or -1 if not found
	 */
	protected final int getAttributeIndex(RelationType<?> attr) {
		return attributes.indexOf(attr);
	}

	/**
	 * Internal initialization method that builds the map of attribute relation
	 * types.
	 *
	 * @param entityName  The name of this entity definition
	 * @param idPrefix    The prefix for global entity IDs
	 * @param entityClass The Entity class to be used for new instances
	 * @param attr        A list containing the attribute relation types
	 */
	protected final void init(String entityName, String idPrefix,
		Class<E> entityClass, List<RelationType<?>> attr) {
		this.entityName = entityName;
		this.entityClass = entityClass;

		setIdPrefix(idPrefix);
		attributes = new ArrayList<>(attr.size());

		for (RelationType<?> attribute : attr) {
			// to prevent initialization cycles child attributes will be
			// added later through a special child definition constructor
			if (attribute.hasFlag(CHILD_ATTRIBUTE)) {
				addChildAttribute(attribute);
			} else if (!attribute.hasFlag(EXTRA_ATTRIBUTE_FLAG)) {
				detectSpecialAttributes(attribute);
				attributes.add(attribute);
			}
		}

		if (idAttribute == null) {
			// if the entity has no own ID attribute insert ENTITY_ID as
			// default
			attributes.add(0, ENTITY_ID);
			idAttribute = ENTITY_ID;
		}

		attributes = Collections.unmodifiableList(attributes);

		attributeDisplayProperties =
			getAttributeDisplayProperties(entityClass);

		set(STORAGE_NAME, getStorageName());
		initDisplayAttributes();
		checkDisableChildCounts();

		EntityManager.registerEntityType(entityClass, this);
	}

	/**
	 * Initializes an entity reference attribute with an intermediate relation.
	 *
	 * @param entity        The entity to init the relation on
	 * @param entityRefAttr The entity reference attribute
	 * @param referenceId   The referenced entity's ID
	 */
	@SuppressWarnings("unchecked")
	protected void initEntityReference(E entity,
		RelationType<Entity> entityRefAttr, Object referenceId) {
		Class<?> attrType = entityRefAttr.getTargetType();

		if (attrType == Entity.class) {
			// fetch arbitrary entities by their global ID
			Function<String, Entity> queryEntity =
				EntityFunctions.queryEntity();
			entity.set(entityRefAttr, i -> queryEntity.apply((String) i),
				referenceId);
		} else {
			Function<Number, Entity> queryEntity =
				EntityFunctions.queryEntity((Class<Entity>) attrType);
			entity.set(entityRefAttr, i -> queryEntity.apply((Number) i),
				referenceId);
		}
	}

	/**
	 * Initializes an attribute that refers to a relation type. If the relation
	 * type cannot be retrieved by it's name because it hasn't been initialized
	 * yet an intermediate relation will be created that tries to resolve the
	 * relation type on the first access.
	 */
	protected void initRelationTypeAttribute(E entity,
		RelationType<RelationType<?>> relationTypeAttr, String typeName) {
		RelationType<?> type = RelationType.valueOf(typeName);

		if (type != null) {
			entity.set(relationTypeAttr, type);
		} else {
			entity.set(relationTypeAttr, RelationType::valueOf, typeName);
		}
	}

	/**
	 * Implemented to return the singleton entity definition instance for the
	 * entity class that has been read by the deserialization. Defined as
	 * protected and final so that it applies to all subclasses as well.
	 *
	 * @return The resolved entity definition instance
	 * @throws ObjectStreamException If no entity definition is available for
	 *                               the deserialized class
	 */
	protected final Object readResolve() throws ObjectStreamException {
		EntityDefinition<?> def =
			EntityManager.getEntityDefinition(entityClass);

		if (def == null) {
			throw new InvalidObjectException(
				"Undefined entity definition: " + def);
		}

		return def;
	}

	/**
	 * Detaches child entities from a parent entity. This will set all parent
	 * references in the child entities to NULL and remove it from the parent's
	 * child list.
	 *
	 * @param parent    The parent entity
	 * @param childAttr The child attribute to remove the child entities from
	 * @param children  The child entities
	 */
	<C extends Entity> void detachChildren(E parent,
		RelationType<List<C>> childAttr, List<C> children) {
		Class<?> childType = childAttr.get(ELEMENT_DATATYPE);
		EntityDefinition<?> childDef =
			(EntityDefinition<?>) childAttr.get(STORAGE_MAPPING);

		RelationType<E> childParentAttr = getChildParentAttribute(childDef);

		RelationType<List<Entity>> childChildAttr =
			childDef.getSelfReferenceAttribute();

		for (C child : children) {
			assert childType.isAssignableFrom(child.getClass());

			Entity childEntity = child;

			if (childEntity.get(childParentAttr) == null) {
				throw new IllegalArgumentException(
					"Child has no parent: " + childEntity);
			}

			childEntity.set(childParentAttr, null);
			childDef.setHierarchyReferences(childEntity, childChildAttr, null,
				null);
		}
	}

	/**
	 * Returns the parent object of a certain entity.
	 *
	 * @param entity The entity to return the parent of
	 * @return The parent entity or NULL for none
	 */
	Entity getParent(Entity entity) {
		Entity parent = null;

		if (parentAttribute != null) {
			parent = entity.get(parentAttribute);
		}

		if (parent == null && masterAttribute != null) {
			parent = entity.get(masterAttribute);
		}

		return parent;
	}

	/**
	 * Adds a child attribute to this definition.
	 *
	 * @param attribute The relation type of the child attribute to add
	 */
	@SuppressWarnings("unchecked")
	private void addChildAttribute(RelationType<?> attribute) {
		if (childAttributes == null) {
			childAttributes = new LinkedHashMap<>();
		}

		Class<? extends Entity> childClass =
			(Class<? extends Entity>) attribute.get(ELEMENT_DATATYPE);

		EntityDefinition<? extends Entity> childDef =
			EntityManager.getEntityDefinition(childClass);

		StorageMapping<?, ?, ?> mapping = attribute.get(STORAGE_MAPPING);

		if (mapping == null) {
			attribute.set(STORAGE_MAPPING, childDef);
		} else if (mapping != childDef) {
			throw new IllegalStateException("Duplicate child attribute with " +
				"different entity definition: " + attribute);
		}

		childAttributes.put(childDef, (RelationType<List<Entity>>) attribute);

		if (childClass == entityClass) {
			hierarchyChildAttribute = (RelationType<List<Entity>>) attribute;
		}
	}

	/**
	 * Helper method to add non-null properties to another properties object.
	 *
	 * @param properties           The properties to add to
	 * @param additionalProperties The properties to add
	 */
	private void addProperties(MutableProperties properties,
		HasProperties additionalProperties) {
		if (additionalProperties != null) {
			properties.setProperties(additionalProperties, true);
		}
	}

	/**
	 * Checks whether the generation of child count columns in SQL should be
	 * disabled.
	 */
	private void checkDisableChildCounts() {
		Boolean disableChildCounts = (Boolean) getStaticFieldValue(entityClass,
			DISABLE_SQL_CHILD_COUNT_FIELD);

		if ((disableChildCounts != null && disableChildCounts.booleanValue())) {
			set(JdbcRelationTypes.SQL_DISABLE_CHILD_COUNTS);
		}
	}

	/**
	 * Checks that a new parent attribute doesn't exist already and returns the
	 * new attribute if appropriate.
	 *
	 * @param currentAttribute The current attribute value which must be
	 *                            NULL or
	 *                         else an exception will be thrown
	 * @param newAttribute     The new attribute value
	 * @throws IllegalStateException If the current attribute is not NULL
	 */
	private void checkParentAttribute(
		RelationType<? extends Entity> currentAttribute,
		RelationType<? extends Entity> newAttribute) {
		if (currentAttribute != null && currentAttribute != newAttribute) {
			throw new IllegalStateException(
				String.format("Duplicate hierarchy attribute: %s and %s",
					currentAttribute, newAttribute));
		}
	}

	/**
	 * Internal method to create a new instance of the described entity from a
	 * list of attribute values.
	 *
	 * @param attributeValues The attribute values
	 * @return The new entity instance
	 *
	 * <p>@ If retrieving a referenced entity fails</p>
	 */
	@SuppressWarnings("unchecked")
	private E createNewEntity(List<?> attributeValues) {
		E entity = createEntityInstance(attributeValues);
		int valueIndex = 0;

		entity.set(INITIALIZING);

		for (RelationType<?> attr : attributes) {
			Object value = attributeValues.get(valueIndex++);

			// omit parent and master attributes for child objects if parents
			// are cached because then these will be set by addChildren() of
			// the parent from tryToGetFromParent(); the caching test from
			// createObject() therefore needs to be repeated here
			if (isHierarchyAttribute(attr) &&
				EntityManager.isCachingEnabledFor(getMappedType())) {
				setHierarchyAttributeValue(entity, attr, value);
			} else if (value != null) {
				Class<?> attrType = attr.getTargetType();

				if (Entity.class.isAssignableFrom(attrType)) {
					initEntityReference(entity, (RelationType<Entity>) attr,
						value);
				} else if (RelationType.class.isAssignableFrom(attrType)) {
					initRelationTypeAttribute(entity,
						(RelationType<RelationType<?>>) attr,
						value.toString());
				} else {
					try {
						value = checkAttributeValue(attr, value);
						entity.set((RelationType<Object>) attr, value);
					} catch (IllegalArgumentException e) {
						String message =
							String.format("Could not map attribute %s.%s: %s",
								getEntityName(), attr, e.getMessage());

						throw new StorageException(message, e);
					}
				}
			}
		}

		entity.deleteRelation(INITIALIZING);

		return entity;
	}

	/**
	 * Assigns the fields containing special attributes if applicable.
	 *
	 * @param attribute The attribute to check
	 */
	@SuppressWarnings("unchecked")
	private void detectSpecialAttributes(RelationType<?> attribute) {
		Class<?> targetType = attribute.getTargetType();

		if (attribute.hasFlag(OBJECT_ID_ATTRIBUTE) &&
			Number.class.isAssignableFrom(attribute.getTargetType())) {
			idAttribute = (RelationType<Number>) attribute;
		} else if (attribute.hasFlag(OBJECT_TYPE_ATTRIBUTE)) {
			typeAttribute = (RelationType<Enum<?>>) attribute;

			registerSubTypes();
		} else if (attribute == StandardTypes.NAME ||
			attribute.hasFlag(OBJECT_NAME_ATTRIBUTE)) {
			nameAttribute = (RelationType<String>) attribute;
		} else if (attribute.hasFlag(PARENT_ATTRIBUTE)) {
			initHierarchyAttribute((RelationType<? extends Entity>) attribute);
		} else if (attribute.hasFlag(ROOT_ATTRIBUTE)) {
			rootAttribute = initParentAttribute(rootAttribute,
				(RelationType<? extends Entity>) attribute);
		} else if (Entity.class.isAssignableFrom(targetType) &&
			targetType != Entity.class) {
			initAttributeStorageMapping(attribute);
		}

		if (!attribute.hasRelation(STORAGE_DATATYPE)) {
			attribute.set(STORAGE_DATATYPE, attribute.getTargetType());
		}

		if (!attribute.hasRelation(STORAGE_NAME)) {
			attribute.set(STORAGE_NAME, attribute.getSimpleName());
		}
	}

	/**
	 * Returns the attribute relation types of the given entity. First checks
	 * for a static field with the name {@link #ENTITY_ATTRIBUTES_FIELD}. If
	 * that doesn't exist it collections all static final fields with the
	 * datatype {@link RelationType}.
	 *
	 * @param entityClass The entity class to query the relation types from
	 * @return The list of attribute relation types
	 */
	private List<RelationType<?>> getAttributeTypes(Class<E> entityClass) {
		@SuppressWarnings("unchecked")
		List<RelationType<?>> attributes =
			(List<RelationType<?>>) getStaticFieldValue(entityClass,
				ENTITY_ATTRIBUTES_FIELD);

		if (attributes == null) {
			attributes =
				ReflectUtil.collectConstants(entityClass, RelationType.class,
					null, true, true, true);
		}

		return attributes;
	}

	/**
	 * Returns a child entity with a certain ID from a parent entity.
	 *
	 * @param parent The parent entity to get the child from
	 * @param id     The child entity's ID
	 * @return The corresponding child entity or NULL if none could be found
	 */
	private E getChild(Entity parent, Number id) {
		@SuppressWarnings("unchecked")
		EntityDefinition<Entity> parentDef =
			(EntityDefinition<Entity>) parent.getDefinition();

		@SuppressWarnings("unchecked")
		Collection<E> children =
			(Collection<E>) parentDef.getChildren(parent, this);

		for (E child : children) {
			if (id.equals(child.get(idAttribute))) {
				return child;
			}
		}

		return null;
	}

	/**
	 * Returns the parent attribute of a child definition that refers to
	 * entities of this definition.
	 *
	 * @param childDef The child definition
	 * @return The parent attribute
	 * @throws IllegalArgumentException If there is no parent reference for
	 * this
	 *                                  definition in the child definition
	 */
	private RelationType<E> getChildParentAttribute(
		EntityDefinition<?> childDef) {
		@SuppressWarnings("unchecked")
		RelationType<E> childParentAttr =
			(RelationType<E>) childDef.getParentAttribute(this);

		if (childParentAttr == null) {
			throw new IllegalArgumentException(
				String.format("No parent attribute in %s for %s", childDef,
					this));
		}

		return childParentAttr;
	}

	/**
	 * Returns the storage name for this definition. This method first tries to
	 * access the field with the name {@link #STORAGE_NAME_FIELD}. If that
	 * doesn't exist the storage name will be generated from the entity name.
	 *
	 * @return The storage name
	 */
	private String getStorageName() {
		String storageName =
			(String) getStaticFieldValue(entityClass, STORAGE_NAME_FIELD);

		if (storageName == null) {
			storageName = EntityManager.isUsePluralStorageNames() ?
			              TextConvert.toPlural(entityName) :
			              entityName;
		}

		return storageName;
	}

	/**
	 * Handles the change of an attribute value.
	 *
	 * @param event       The relation event that occurred
	 * @param entity      The target entity
	 * @param updateValue The new attribute value
	 */
	private void handleAttributeValueChange(RelationEvent<?> event,
		Entity entity, Object updateValue) {
		EventType eventType = event.getType();
		Relation<?> relation = event.getElement();
		Object prevValue = relation.getTarget();
		Object newValue = updateValue;

		if (eventType == EventType.ADD) {
			newValue = prevValue;
			prevValue = null;
		}

		boolean modified = !Objects.equals(newValue, prevValue);

		// only store previous value on first change to remember
		// the persistent state, not intermediate changes; upon store
		// the PREVIOUS_VALUE relation will be removed
		if (modified && !relation.hasAnnotation(PREVIOUS_VALUE)) {
			relation.annotate(PREVIOUS_VALUE, prevValue);
		}

		if ((modified || eventType != EventType.UPDATE) &&
			!entity.isModified()) {
			// this will invoke the MODIFIED branch of handleEvent
			entity.set(MODIFIED);
		}
	}

	/**
	 * Performs the state changes necessary if an entity's modified state is
	 * toggled.
	 *
	 * @param entity   The entity
	 * @param modified The new modified state
	 */
	private void handleEntityModification(Entity entity, boolean modified) {
		if (modified) {
			EntityManager.beginEntityModification(entity);

			Entity parent = entity.getParent();

			if (parent != null) {
				// also recursively mark the parent hierarchy as modified to
				// ensure evaluation of the parents on store
				parent.set(MODIFIED);
			}
		} else {
			EntityManager.endEntityModification(entity);

			// reset all PREVIOUS_VALUE and REMOVED_CHILDREN annotations if the
			// entity modification is reset (i.e. the entity has been stored)
			removePreviousValues(entity);
		}
	}

	/**
	 * Initializes the storage mapping for a certain entity reference
	 * attribute.
	 *
	 * @param attribute The attribute relation type
	 */
	private void initAttributeStorageMapping(RelationType<?> attribute) {
		// set entity mapping if not an arbitrary
		// entity reference
		if (attribute.get(STORAGE_MAPPING) == null) {
			StorageMapping<?, ?, ?> mapping =
				StorageManager.getMapping(attribute.getTargetType());

			assert mapping instanceof EntityDefinition;

			attribute.set(STORAGE_MAPPING, mapping);

			// only set to TRUE if not explicitly set to FALSE
			if (!attribute.hasRelation(REFERENCE_ATTRIBUTE)) {
				attribute.set(REFERENCE_ATTRIBUTE);
			}
		}
	}

	/**
	 * Initializes the lists of display attributes.
	 */
	private void initDisplayAttributes() {
		for (DisplayMode displayMode : DisplayMode.values()) {
			RelationType<?>[] modeAttributes =
				(RelationType<?>[]) getStaticFieldValue(entityClass,
					DISPLAY_ATTRIBUTES_FIELD_PREFIX + displayMode);

			if (modeAttributes != null) {
				assert modeAttributes.length > 0;

				List<RelationType<?>> attrList =
					Collections.unmodifiableList(Arrays.asList(modeAttributes));

				displayAttributes.put(displayMode, attrList);
			}
		}

		// default attributes which will only be set if no attributes have been
		// set above for the given display mode
		setDisplayAttributes(DisplayMode.MINIMAL, idAttribute, NAME);
		setDisplayAttributes(DisplayMode.COMPACT, idAttribute, NAME, INFO);
		setDisplayAttributes(DisplayMode.HIERARCHICAL, attributes);

		if (!displayAttributes.containsKey(DisplayMode.FULL)) {
			List<RelationType<?>> simpleAttributes = new ArrayList<>();

			for (RelationType<?> attribute : attributes) {
				if (!Entity.class.isAssignableFrom(attribute.getTargetType())) {
					simpleAttributes.add(attribute);
				}
			}

			setDisplayAttributes(DisplayMode.FULL, simpleAttributes);
		}
	}

	/**
	 * Internal method to initialize the parent attribute for either a
	 * parent-child or a master-detail relationship.
	 *
	 * @param attribute The attribute relation type
	 */
	private void initHierarchyAttribute(
		RelationType<? extends Entity> attribute) {
		if (attribute.getTargetType() == entityClass) {
			parentAttribute = initParentAttribute(parentAttribute, attribute);
		} else {
			@SuppressWarnings("unchecked")
			Class<? extends Entity> masterClass =
				(Class<? extends Entity>) attribute.getTargetType();

			checkParentAttribute(masterAttribute, attribute);

			if (masterAttribute == null) {
				// update only if not set already
				masterAttribute = attribute;
				masterAttribute.set(STORAGE_MAPPING,
					EntityManager.getEntityDefinition(masterClass));
			}
		}
	}

	/**
	 * Initializes a parent attribute for this definition. Such attributes are
	 * identified by one of the flags {@link MetaTypes#PARENT_ATTRIBUTE} or
	 * {@link EntityRelationTypes#ROOT_ATTRIBUTE}.
	 *
	 * @param parentAttr The current value of the parent attribute to init
	 * @param newAttr    The new attribute value
	 */
	private RelationType<? extends Entity> initParentAttribute(
		RelationType<? extends Entity> parentAttr,
		RelationType<? extends Entity> newAttr) {
		checkParentAttribute(parentAttr, newAttr);

		if (parentAttr == null) {
			// update only if not set already
			parentAttr = newAttr;
			parentAttr.set(STORAGE_MAPPING, this);
		}

		return parentAttr;
	}

	/**
	 * Registers this definition also for sub-classes of this definitions
	 * entity
	 * type if they are marked as sub-types without separate definition.
	 */
	private void registerSubTypes() {
		Enum<?>[] entityTypes =
			(Enum<?>[]) typeAttribute.getTargetType().getEnumConstants();

		for (Enum<?> type : entityTypes) {
			String subTypeClass = entityClass.getPackage().getName() + "." +
				TextConvert.capitalizedIdentifier(type.toString());

			try {
				@SuppressWarnings("unchecked")
				Class<? extends E> typeClass =
					(Class<? extends E>) Class.forName(subTypeClass);

				if (entityClass.isAssignableFrom(typeClass)) {
					if (typeSubClasses == null) {
						typeSubClasses = new HashMap<>();
						subClassTypes = new HashMap<>();
					}

					EntityManager.registerEntitySubType(typeClass, this);
					typeSubClasses.put(type.toString(), typeClass);
					subClassTypes.put(typeClass, type);
				}
			} catch (ClassNotFoundException e) {
				// ignore non-existing sub-type and use the base class
			}
		}
	}

	/**
	 * Remove all previous value annotations from the attribute relations.
	 *
	 * @param entity The target entity
	 */
	private void removePreviousValues(Entity entity) {
		for (RelationType<?> attr : attributes) {
			Relation<?> relation = entity.getRelation(attr);

			if (relation != null) {
				relation.deleteRelation(PREVIOUS_VALUE);
			}
		}

		if (childAttributes != null) {
			for (RelationType<?> childAttr : childAttributes.values()) {
				Relation<?> childRelation = getRelation(childAttr);

				if (childRelation != null) {
					childRelation.deleteRelation(REMOVED_CHILDREN);
				}
			}
		}
	}

	/**
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
	 * @param displayMode The display mode to register the attributes for
	 * @param attributes  A collection of the attribute relation types to
	 *                    display in the given mode
	 */
	private void setDisplayAttributes(DisplayMode displayMode,
		List<RelationType<?>> attributes) {
		if (!displayAttributes.containsKey(displayMode)) {
			assert attributes != null && !attributes.isEmpty();

			displayAttributes.put(displayMode, attributes);
		}
	}

	/**
	 * Sets the display attributes for a certain display mode.
	 *
	 * @see #setDisplayAttributes(DisplayMode, List)
	 */
	private void setDisplayAttributes(DisplayMode displayMode,
		RelationType<?>... attr) {
		if (!displayAttributes.containsKey(displayMode)) {
			List<RelationType<?>> attributeList =
				new ArrayList<>(Arrays.asList(attr));

			for (RelationType<?> attribute : attr) {
				if (!attributes.contains(attribute)) {
					attributeList.remove(attribute);
				}
			}

			setDisplayAttributes(displayMode, attributeList);
		}
	}

	/**
	 * Sets the value of a hierarchy attribute if it is a valid {@link Number}.
	 *
	 * @param entity The target entity
	 * @param attr   The hierarchy attribute
	 * @param value  The attribute value
	 */
	private void setHierarchyAttributeValue(E entity, RelationType<?> attr,
		Object value) {
		if (value != null) {
			Number id = (Number) value;

			if (attr == masterAttribute) {
				entity.set(MASTER_ENTITY_ID, id.longValue());
			} else if (attr == parentAttribute) {
				entity.set(PARENT_ENTITY_ID, id.longValue());
			}
		}
	}

	/**
	 * Sets the master and root entities recursively on an entity hierarchy.
	 * The
	 * master attribute will always be set, the root attribute only if it is
	 * not
	 * NULL.
	 *
	 * @param entity         The starting entity of the hierarchy
	 * @param childAttribute The child attribute to descend the entity's
	 *                       hierarchy at or NULL for none
	 * @param master         The new master entity (may be NULL to clear)
	 * @param root           The new root entity (NULL to ignore)
	 */
	@SuppressWarnings("unchecked")
	private void setHierarchyReferences(Entity entity,
		RelationType<List<Entity>> childAttribute, Entity master,
		Entity root) {
		if (masterAttribute != null) {
			entity.set((RelationType<Entity>) masterAttribute, master);
		}

		if (rootAttribute != null) {
			entity.set((RelationType<Entity>) rootAttribute, root);

			// the (first) root may be set to NULL if hierarchy levels change
			// but then subsequent levels need to reference the current entity
			// as their root
			if (root == null) {
				root = entity;
			}
		}

		if (childAttribute != null) {
			List<Entity> children = entity.get(childAttribute);

			// prevent unnecessary querying of child lists during
			// initialization
			if (!(entity.hasFlag(INITIALIZING) &&
				children instanceof QueryList)) {
				for (Entity child : children) {
					setHierarchyReferences(child, childAttribute, master,
						root);
				}
			}
		}
	}

	/**
	 * Sets the ID prefix and generates it if necessary.
	 *
	 * @param prefix The prefix to set or NULL to query it from the field
	 *               {@link #ID_PREFIX_FIELD} or to generate it
	 */
	private void setIdPrefix(String prefix) {
		if (prefix == null) {
			prefix = (String) getStaticFieldValue(entityClass,
				ID_PREFIX_FIELD);

			if (prefix == null) {
				// default value: the upper case letters of the class name
				prefix = entityClass
					.getSimpleName()
					.replaceAll("\\p{javaLowerCase}*", "");
			}
		}

		idPrefix = prefix;
	}

	/**
	 * Internal method to check whether a list of attributes contains a
	 * reference to a parent entity and to return the corresponding parent
	 * entity if possible.
	 *
	 * @param attributeValues The list of attribute values to check
	 * @return The parent entity or NULL for none
	 *
	 * <p>@ If retrieving the parent entity fails</p>
	 */
	private E tryToGetFromParent(List<?> attributeValues) {
		RelationType<?> parentAttr = null;
		Entity parent = null;
		Number parentId = null;
		E entity = null;

		if (parentAttribute != null) {
			int parentIdIndex = attributes.indexOf(parentAttribute);

			parentId = (Number) attributeValues.get(parentIdIndex);

			if (parentId != null) {
				parentAttr = parentAttribute;
			}
		}

		if (parentAttr == null && masterAttribute != null) {
			int parentIdIndex = attributes.indexOf(masterAttribute);

			parentId = (Number) attributeValues.get(parentIdIndex);

			if (parentId != null) {
				parentAttr = masterAttribute;
			}
		}

		if (parentAttr != null) {
			@SuppressWarnings("unchecked")
			Class<Entity> parentClass =
				(Class<Entity>) parentAttr.getTargetType();

			parent =
				EntityManager.queryEntity(parentClass, parentId.intValue());

			if (parent != null) {
				int idIndex = attributes.indexOf(idAttribute);
				Number id = (Number) attributeValues.get(idIndex);

				entity = getChild(parent, id);
			}
		}

		return entity;
	}
}
