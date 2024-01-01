//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.datatype.Pair;
import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.FunctionException;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.logging.Log;
import de.esoco.storage.StorageException;
import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.IntermediateRelation;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.type.MetaTypes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.esoco.entity.EntityRelationTypes.arbitraryEntityAttribute;
import static de.esoco.entity.ExtraAttributes.EXTRA_ATTRIBUTES_NAMESPACE;
import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.storage.StorageRelationTypes.STORAGE_DATATYPE;
import static de.esoco.storage.StorageRelationTypes.STORAGE_LENGTH;
import static org.obrel.core.RelationTypeModifier.FINAL;
import static org.obrel.core.RelationTypes.newType;

/**
 * The entity definition for additional attributes that can be set on entities.
 *
 * @author eso
 */
@RelationTypeNamespace("de.esoco.entity.xattr")
public class ExtraAttribute extends Entity {

	/**
	 * The prefix for global entity IDs
	 */
	public static final String ID_PREFIX = "XA";

	/**
	 * The entity an attribute belongs to
	 */
	public static final RelationType<Entity> ENTITY =
		arbitraryEntityAttribute(FINAL);

	/**
	 * The entity an attribute belongs to
	 */
	public static final RelationType<Entity> OWNER =
		arbitraryEntityAttribute(FINAL);

	/**
	 * The key string that identifies the attribute
	 */
	public static final RelationType<RelationType<?>> KEY = newType(FINAL);

	/**
	 * The value of the attribute
	 */
	public static final RelationType<Object> VALUE = newType();

	/**
	 * A predicate that checks whether the {@link #OWNER} attribute is NULL.
	 */
	public static final Predicate<Relatable> HAS_NO_OWNER =
		ExtraAttribute.OWNER.is(equalTo(null));

	private static final long serialVersionUID = 1L;

	static {
		VALUE.set(STORAGE_DATATYPE, String.class);
		VALUE.set(STORAGE_LENGTH, Integer.valueOf(8000));
	}

	/**
	 * Overridden to always return FALSE because the change logging of entities
	 * includes their extra attributes.
	 *
	 * @see Entity#hasChangeLogging()
	 */
	@Override
	public boolean hasChangeLogging() {
		return false;
	}

	/**
	 * The entity definition.
	 */
	public static class Definition extends EntityDefinition<ExtraAttribute> {

		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new instance.
		 */
		public Definition() {
			super(ExtraAttribute.class);
		}

		/**
		 * Overridden to handle the special namespace of extra attributes
		 * and to
		 * make collection and map value immutable.
		 *
		 * @see EntityDefinition#checkAttributeValue(RelationType, Object)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object checkAttributeValue(RelationType<?> attribute,
			Object value) throws StorageException {
			value = super.checkAttributeValue(attribute, value);

			if (value instanceof List) {
				value = Collections.unmodifiableList((List<Object>) value);
			} else if (value instanceof Set) {
				value = Collections.unmodifiableSet((Set<Object>) value);
			} else if (value instanceof Map) {
				value =
					Collections.unmodifiableMap((Map<Object, Object>) value);
			}

			return value;
		}

		/**
		 * @see EntityDefinition#createObject(List, boolean)
		 */
		@Override
		public ExtraAttribute createObject(List<?> attributeValues,
			boolean asChild) throws StorageException {
			ExtraAttribute xA = super.createObject(attributeValues, asChild);

			xA.set(MetaTypes.INITIALIZING);

			try {
				Relation<RelationType<?>> keyRelation = xA.getRelation(KEY);

				String value = (String) xA.get(VALUE);

				if (keyRelation instanceof IntermediateRelation) {
					String keyName = ((IntermediateRelation<?, ?>) keyRelation)
						.getIntermediateTarget()
						.toString();

					xA.deleteRelation(VALUE);
					xA.set(VALUE,
						rawData -> Conversions.parseValue(rawData.second(),
							getKeyRelationType(rawData.first(), true)),
						new Pair<String, String>(keyName, value));
				} else {
					RelationType<?> key = keyRelation.getTarget();

					xA.set(VALUE, Conversions.parseValue(value, key));
				}

				xA.deleteRelation(MetaTypes.INITIALIZING);
			} catch (FunctionException e) {
				// unwrap any storage exceptions that occur on parsing
				if (e.getCause() instanceof StorageException) {
					throw (StorageException) e.getCause();
				} else {
					throw e;
				}
			}

			return xA;
		}

		/**
		 * Overridden to map the value attribute to a string value.
		 *
		 * @see EntityDefinition#mapValue(RelationType, Object)
		 */
		@Override
		public Object mapValue(RelationType<?> attribute, Object value)
			throws StorageException {
			if (attribute == VALUE) {
				if (value != null) {
					value = Conversions.asString(value);
				}
			} else {
				value = super.mapValue(attribute, value);
			}

			return value;
		}

		/**
		 * Overridden to handle the special namespace of extra attributes.
		 *
		 * @see EntityDefinition#initRelationTypeAttribute(Entity, RelationType,
		 * String)
		 */
		@Override
		protected void initRelationTypeAttribute(ExtraAttribute extraAttribute,
			RelationType<RelationType<?>> relationTypeAttr, String keyName) {
			RelationType<?> type = getKeyRelationType(keyName, false);

			if (type != null) {
				extraAttribute.set(relationTypeAttr, type);
			} else {
				extraAttribute.set(relationTypeAttr,
					key -> getKeyRelationType(key, true), keyName);
			}
		}

		/**
		 * Returns the relation type for a certain extra attribute key.
		 *
		 * @param keyName    The name of the key
		 * @param logMissing TRUE to log an error if the key relation type
		 *                   cannot be resolved
		 * @return The key relation type or NULL for none
		 */
		RelationType<?> getKeyRelationType(String keyName,
			boolean logMissing) {
			RelationType<?> type = RelationType.valueOf(keyName);

			if (type == null) {
				type = RelationType.valueOf(
					EXTRA_ATTRIBUTES_NAMESPACE + "." + keyName);

				if (type == null && logMissing) {
					Log.errorf(
						"Missing extra attribute type %s (in entity " + "%s)",
						keyName, get(ENTITY));
				}
			}

			return type;
		}
	}
}
