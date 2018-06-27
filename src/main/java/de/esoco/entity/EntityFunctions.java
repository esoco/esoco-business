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

import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.FunctionException;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.expression.function.AbstractBinaryFunction;
import de.esoco.lib.expression.function.AbstractInvertibleFunction;
import de.esoco.lib.expression.function.ExceptionMappingFunction;
import de.esoco.lib.expression.function.GetElement;
import de.esoco.lib.text.TextConvert;

import de.esoco.storage.StorageException;

import org.obrel.core.RelationType;

import static de.esoco.entity.EntityPredicates.ifAttribute;


/********************************************************************
 * Contains factory method for entity-specific instances of {@link Function}.
 *
 * @author eso
 */
public class EntityFunctions
{
	//~ Static fields/initializers ---------------------------------------------

	private static final InvertibleFunction<Entity, String> ENTITY_TO_STRING =
		new AbstractInvertibleFunction<Entity, String>("EntityToString")
		{
			@Override
			public String evaluate(Entity rEntity)
			{
				return EntityManager.getGlobalEntityId(rEntity);
			}

			@Override
			public Entity invert(String sEntityId)
			{
				try
				{
					return EntityManager.queryEntity(sEntityId);
				}
				catch (StorageException e)
				{
					throw new FunctionException(this, e);
				}
			}
		};

	private static final Function<Entity, String> FORMAT_ENTITY =
		formatEntity(null);

	private static final Action<Entity> STORE_ENTITY =
		e -> EntityManager.store(e);

	private static final Function<Entity, String> GET_GLOBAL_ENTITY_ID =
		e -> e != null ? EntityManager.getGlobalEntityId(e) : "";

	private static final Function<Entity, Number> GET_ENTITY_ID =
		e -> e != null ? e.get(e.getDefinition().getIdAttribute()) : null;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private EntityFunctions()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns an invertible function that can convert an entity into a string
	 * and vice versa. The string representation of a entity will be it's global
	 * entity ID as returned by {@link EntityManager#getGlobalEntityId(Entity)}.
	 * When the function is inverted the entity will be queried through the
	 * method {@link EntityManager#queryEntity(String)}. If that causes an error
	 * a {@link FunctionException} will be thrown.
	 *
	 * @return The entity string conversion function
	 */
	public static InvertibleFunction<Entity, String> entityToString()
	{
		return ENTITY_TO_STRING;
	}

	/***************************************
	 * Format an entity with a default instance of {@link EntityFormat}.
	 *
	 * @param  rEntity The entity to format
	 *
	 * @return The resulting string or NULL, if the input entity is NULL
	 */
	public static String format(Entity rEntity)
	{
		return FORMAT_ENTITY.evaluate(rEntity);
	}

	/***************************************
	 * Returns a function that formats an entity into a readable string.
	 *
	 * @param  sNullString The string to be displayed if the input entity is
	 *                     NULL
	 *
	 * @return The function for the string formatting
	 */
	public static <E extends Entity> Function<E, String> formatEntity(
		String sNullString)
	{
		return new EntityFormat<E>(sNullString, true);
	}

	/***************************************
	 * Creates a string for a formatted display of an entity's extra attributes.
	 *
	 * @param  rEntity The entity
	 *
	 * @return The resulting string
	 *
	 * @throws StorageException If accessing the extra attributes fails
	 */
	public static String formatExtraAttributes(Entity rEntity)
		throws StorageException
	{
		StringBuilder aResult = new StringBuilder();

		for (RelationType<?> rExtraAttr : rEntity.getExtraAttributes())
		{
			String sName  = rExtraAttr.getSimpleName();
			Object rValue = rEntity.getExtraAttribute(rExtraAttr, null);

			aResult.append(TextConvert.capitalize(sName, " "));
			aResult.append(": ").append(rValue).append("\n");
		}

		return aResult.toString();
	}

	/***************************************
	 * Returns a static function that returns the ID of a certain entity.
	 *
	 * @return A static function instance
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> Function<E, Number> getEntityId()
	{
		return (Function<E, Number>) GET_ENTITY_ID;
	}

	/***************************************
	 * Returns a new function that retrieves a certain extra attribute from it's
	 * input entity.
	 *
	 * @param  rKey          The extra attribute key
	 * @param  rDefaultValue The default value if the attribute doesn't exist
	 *
	 * @return A new binary function instance
	 */
	public static <E extends Entity, V> GetExtraAttribute<E, V>
	getExtraAttribute(RelationType<V> rKey, V rDefaultValue)
	{
		return new GetExtraAttribute<E, V>(rKey, rDefaultValue);
	}

	/***************************************
	 * Returns a new function that returns the correctly typed value of an extra
	 * attribute with a certain key. If the key of the evaluated extra attribute
	 * doesn't match the argument key an {@link IllegalArgumentException} will
	 * be thrown from the function evaluation.
	 *
	 * @param  rExtraAttributeKey The extra attribute key
	 *
	 * @return A new binary function instance
	 */
	public static <T> BinaryFunction<ExtraAttribute, RelationType<T>, T>
	getExtraAttributeValue(RelationType<T> rExtraAttributeKey)
	{
		return new AbstractBinaryFunction<ExtraAttribute, RelationType<T>, T>(rExtraAttributeKey,
																			  "getExtraAttributeValue")
		{
			@Override
			@SuppressWarnings("unchecked")
			public T evaluate(
				ExtraAttribute  rExtraAttribute,
				RelationType<T> rKey)
			{
				if (rKey == rExtraAttribute.get(ExtraAttribute.KEY))
				{
					return (T) rExtraAttribute.get(ExtraAttribute.VALUE);
				}
				else
				{
					String sMessage =
						String.format("Invalid key %s for %s",
									  rKey,
									  rExtraAttribute);

					throw new IllegalArgumentException(sMessage);
				}
			}
		};
	}

	/***************************************
	 * Returns a static function that returns the global ID of a certain entity.
	 *
	 * @return A static function instance
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> Function<E, String> getGlobalEntityId()
	{
		return (Function<E, String>) GET_GLOBAL_ENTITY_ID;
	}

	/***************************************
	 * Returns a function that returns an entity attribute from the entity's
	 * hierarchy.
	 *
	 * @see Entity#getUpwards(RelationType)
	 */
	public static <E extends Entity, T> BinaryFunction<E, RelationType<T>, T>
	getUpwards(RelationType<T> rAttribute)
	{
		return new AbstractBinaryFunction<E, RelationType<T>, T>(rAttribute,
																 "getUpwards(%s)")
		{
			@Override
			public T evaluate(E rEntity, RelationType<T> rAttribute)
			{
				return rEntity.getUpwards(rAttribute);
			}
		};
	}

	/***************************************
	 * Returns a new function that queries an entity with a certain global ID.
	 *
	 * @see EntityManager#queryEntity(String)
	 */
	public static Function<String, Entity> queryEntity()
	{
		return new ExceptionMappingFunction<String, Entity>("queryEntity(GlobalID)")
		{
			@Override
			public Entity evaluateWithException(String sGlobalId)
				throws StorageException
			{
				return EntityManager.queryEntity(sGlobalId);
			}
		};
	}

	/***************************************
	 * Returns a new function that queries an entity of a certain type with a
	 * particular ID.
	 *
	 * @see EntityManager#queryEntity(Class, int)
	 */
	public static <E extends Entity> Function<Long, E> queryEntity(
		final Class<E> rEntityClass)
	{
		return new ExceptionMappingFunction<Long, E>("queryEntity(ID)")
		{
			@Override
			@SuppressWarnings("boxing")
			public E evaluateWithException(Long rId) throws StorageException
			{
				return EntityManager.queryEntity(rEntityClass, rId);
			}
		};
	}

	/***************************************
	 * Returns a new function which queries an entity that matches certain
	 * criteria. The input value of the function will be ignored.
	 *
	 * @see EntityManager#queryEntity(Class, Predicate, boolean)
	 */
	public static <E extends Entity> Function<Object, E> queryEntity(
		final Class<E>			   rEntityClass,
		final Predicate<? super E> rCriteria,
		final boolean			   bFailOnMultiple)
	{
		return new ExceptionMappingFunction<Object, E>("queryEntity(" +
													   rEntityClass
													   .getSimpleName() +
													   "[" + rCriteria +
													   "]" + ")")
		{
			@Override
			public E evaluateWithException(Object rIgnored)
				throws StorageException
			{
				return EntityManager.queryEntity(rEntityClass,
												 rCriteria,
												 bFailOnMultiple);
			}
		};
	}

	/***************************************
	 * Returns a new function that queries an entity with a certain attribute
	 * value.
	 *
	 * @param  rEntityClass        The entity type to query
	 * @param  rAttribute          The attribute to search for
	 * @param  rAdditionalCriteria Optional additional criteria or NULL for none
	 * @param  bFailOnMultiple     If TRUE the call fails if multiple entities
	 *                             are found for the given criteria
	 *
	 * @return A new function instance
	 */
	public static <T, E extends Entity> Function<T, E> queryEntity(
		final Class<E>			   rEntityClass,
		final RelationType<T>	   rAttribute,
		final Predicate<? super E> rAdditionalCriteria,
		final boolean			   bFailOnMultiple)
	{
		return new AttributeQueryFunction<T, E>(rEntityClass,
												rAttribute,
												rAdditionalCriteria,
												bFailOnMultiple);
	}

	/***************************************
	 * Returns a static function instance that stores the entity it receives as
	 * the input value. The input entity will then be returned as the output
	 * value to allow function chaining.
	 *
	 * @return A static function instance that stores an entity
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> Action<E> storeEntity()
	{
		return (Action<E>) STORE_ENTITY;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An element accessor that returns the value of a certain extra attribute
	 * from an entity input value.
	 */
	public static class GetExtraAttribute<E extends Entity, V>
		extends GetElement<E, RelationType<V>, V>
	{
		//~ Instance fields ----------------------------------------------------

		V rDefaultValue;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that accesses a particular single-type
		 * relation.
		 *
		 * @param rKey          The typed key of the extra attribute
		 * @param rDefaultValue The default value if the attribute doesn't exist
		 */
		public GetExtraAttribute(RelationType<V> rKey, V rDefaultValue)
		{
			super(rKey, "GetExtraAttribute[%s]");
			this.rDefaultValue = rDefaultValue;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		protected V getElementValue(E rEntity, RelationType<V> rKey)
		{
			try
			{
				return rEntity.getExtraAttribute(rKey, rDefaultValue);
			}
			catch (StorageException e)
			{
				throw new FunctionException(this, e);
			}
		}
	}

	/********************************************************************
	 * An internal function implementation that performs an entity query for a
	 * certain attribute value.
	 *
	 * @author eso
	 */
	private static final class AttributeQueryFunction<T, E extends Entity>
		extends ExceptionMappingFunction<T, E>
	{
		//~ Instance fields ----------------------------------------------------

		private final Class<E>		 rEntityClass;
		private RelationType<T>		 rAttribute;
		private Predicate<? super E> pAdditionalCriteria;
		private final boolean		 bFailOnMultiple;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rEntityClass        The entity type to query
		 * @param rAttribute          The attribute to search for
		 * @param pAdditionalCriteria Optional additional criteria or NULL for
		 *                            none
		 * @param bFailOnMultiple     If TRUE the call fails if multiple
		 *                            entities are found for the given criteria
		 */
		private AttributeQueryFunction(Class<E>				rEntityClass,
									   RelationType<T>		rAttribute,
									   Predicate<? super E> pAdditionalCriteria,
									   boolean				bFailOnMultiple)
		{
			super("queryEntity(" + rEntityClass.getSimpleName() +
				  "[" + rAttribute.getSimpleName() + "])");

			this.rEntityClass		 = rEntityClass;
			this.rAttribute			 = rAttribute;
			this.pAdditionalCriteria = pAdditionalCriteria;
			this.bFailOnMultiple     = bFailOnMultiple;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see ExceptionMappingFunction#evaluateWithException(Object)
		 */
		@Override
		public E evaluateWithException(T rValue) throws StorageException
		{
			Predicate<E> pCriteria =
				ifAttribute(rAttribute, Predicates.equalTo(rValue));

			if (pAdditionalCriteria != null)
			{
				pCriteria = pCriteria.and(pAdditionalCriteria);
			}

			return EntityManager.queryEntity(rEntityClass,
											 pCriteria,
											 bFailOnMultiple);
		}
	}
}
