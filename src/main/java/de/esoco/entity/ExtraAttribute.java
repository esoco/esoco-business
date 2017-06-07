//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.lib.expression.function.AbstractFunction;

import de.esoco.storage.StorageException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.IntermediateRelation;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.type.MetaTypes;

import static de.esoco.entity.EntityRelationTypes.newArbitraryEntityAttribute;
import static de.esoco.entity.ExtraAttributes.EXTRA_ATTRIBUTES_NAMESPACE;

import static de.esoco.lib.expression.Predicates.equalTo;

import static de.esoco.storage.StorageRelationTypes.STORAGE_DATATYPE;
import static de.esoco.storage.StorageRelationTypes.STORAGE_LENGTH;

import static org.obrel.core.RelationTypeModifier.FINAL;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * The entity definition for additional attributes that can be set on entities.
 *
 * @author eso
 */
@RelationTypeNamespace("de.esoco.entity.xattr")
public class ExtraAttribute extends Entity
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The prefix for global entity IDs */
	public static final String ID_PREFIX = "XA";

	/** The entity an attribute belongs to */
	public static final RelationType<Entity> ENTITY =
		newArbitraryEntityAttribute(FINAL);

	/** The entity an attribute belongs to */
	public static final RelationType<Entity> OWNER =
		newArbitraryEntityAttribute(FINAL);

	/** The key string that identifies the attribute */
	public static final RelationType<RelationType<?>> KEY = newType(FINAL);

	/** The value of the attribute */
	public static final RelationType<Object> VALUE = newType();

	/** A predicate that checks whether the {@link #OWNER} attribute is NULL. */
	public static final Predicate<Relatable> HAS_NO_OWNER =
		ExtraAttribute.OWNER.is(equalTo(null));

	static
	{
		VALUE.set(STORAGE_DATATYPE, String.class);
		VALUE.set(STORAGE_LENGTH, Integer.valueOf(8000));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to always return FALSE because the change logging of entities
	 * includes their extra attributes.
	 *
	 * @see Entity#isChangeLoggingEnabled()
	 */
	@Override
	public boolean hasChangeLogging()
	{
		return false;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * The entity definition.
	 */
	public static class Definition extends EntityDefinition<ExtraAttribute>
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 */
		public Definition()
		{
			super(ExtraAttribute.class);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Overridden to handle the special namespace of extra attributes and to
		 * make collection and map value immutable.
		 *
		 * @see EntityDefinition#checkAttributeValue(RelationType, Object)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object checkAttributeValue(
			RelationType<?> rAttribute,
			Object			rValue) throws StorageException
		{
			rValue = super.checkAttributeValue(rAttribute, rValue);

			if (rValue instanceof List)
			{
				rValue = Collections.unmodifiableList((List<Object>) rValue);
			}
			else if (rValue instanceof Set)
			{
				rValue = Collections.unmodifiableSet((Set<Object>) rValue);
			}
			else if (rValue instanceof Map)
			{
				rValue =
					Collections.unmodifiableMap((Map<Object, Object>) rValue);
			}

			return rValue;
		}

		/***************************************
		 * @see EntityDefinition#createObject(List, boolean)
		 */
		@Override
		public ExtraAttribute createObject(
			List<?> rAttributeValues,
			boolean bAsChild) throws StorageException
		{
			ExtraAttribute rXA = super.createObject(rAttributeValues, bAsChild);

			rXA.set(MetaTypes.INITIALIZING);

			try
			{
				Relation<RelationType<?>> rKeyRelation = rXA.getRelation(KEY);

				String sValue = (String) rXA.get(VALUE);

				if (rKeyRelation instanceof IntermediateRelation)
				{
					String sKeyName =
						((IntermediateRelation<?, ?>) rKeyRelation)
						.getIntermediateTarget().toString();

					rXA.deleteRelation(VALUE);
					rXA.set(VALUE,
						new AbstractFunction<Pair<String, String>, Object>("ParseExtraAttributeValue")
						{
							@Override
							public Object evaluate(
								Pair<String, String> rRawData)
							{
								return Conversions.parseValue(rRawData.second(),
															  getKeyRelationType(rRawData
																				 .first()));
							}
						},
							new Pair<String, String>(sKeyName, sValue));
				}
				else
				{
					RelationType<?> rKey = rKeyRelation.getTarget();

					rXA.set(VALUE, Conversions.parseValue(sValue, rKey));
				}

				rXA.deleteRelation(MetaTypes.INITIALIZING);
			}
			catch (FunctionException e)
			{
				// unwrap any storage exceptions that occur on parsing
				if (e.getCause() instanceof StorageException)
				{
					throw (StorageException) e.getCause();
				}
				else
				{
					throw e;
				}
			}

			return rXA;
		}

		/***************************************
		 * Overridden to map the value attribute to a string value.
		 *
		 * @see EntityDefinition#mapValue(RelationType, Object)
		 */
		@Override
		public Object mapValue(RelationType<?> rAttribute, Object rValue)
			throws StorageException
		{
			if (rAttribute == VALUE)
			{
				if (rValue != null)
				{
					rValue = Conversions.asString(rValue);
				}
			}
			else
			{
				rValue = super.mapValue(rAttribute, rValue);
			}

			return rValue;
		}

		/***************************************
		 * Overridden to handle the special namespace of extra attributes.
		 *
		 * @see EntityDefinition#initRelationTypeAttribute(Entity, RelationType,
		 *      String)
		 */
		@Override
		protected void initRelationTypeAttribute(
			ExtraAttribute				  rExtraAttribute,
			RelationType<RelationType<?>> rRelationTypeAttr,
			String						  sKeyName)
		{
			RelationType<?> rType = getKeyRelationType(sKeyName);

			if (rType != null)
			{
				rExtraAttribute.set(rRelationTypeAttr, rType);
			}
			else
			{
				rExtraAttribute.set(rRelationTypeAttr,
					new AbstractFunction<String, RelationType<?>>("GetKeyRelationType")
					{
						@Override
						public RelationType<?> evaluate(String sKey)
						{
							return getKeyRelationType(sKey);
						}
					},
									sKeyName);
			}
		}

		/***************************************
		 * Returns the relation type for a certain extra attribute key.
		 *
		 * @param  sKeyName The name of the key
		 *
		 * @return The key relation type or NULL for none
		 */
		RelationType<?> getKeyRelationType(String sKeyName)
		{
			RelationType<?> rType = RelationType.valueOf(sKeyName);

			if (rType == null)
			{
				rType =
					RelationType.valueOf(EXTRA_ATTRIBUTES_NAMESPACE + "." +
										 sKeyName);
			}

			return rType;
		}
	}
}
