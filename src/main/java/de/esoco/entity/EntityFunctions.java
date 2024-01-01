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

import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.FunctionException;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.expression.ThrowingFunction;
import de.esoco.lib.expression.ThrowingSupplier;
import de.esoco.lib.expression.function.AbstractBinaryFunction;
import de.esoco.lib.expression.function.GetElement;
import de.esoco.lib.text.TextConvert;
import de.esoco.storage.StorageException;
import org.obrel.core.RelationType;

import java.util.function.Supplier;

import static de.esoco.entity.EntityPredicates.ifAttribute;

/**
 * Contains factory method for entity-specific instances of {@link Function}.
 *
 * @author eso
 */
public class EntityFunctions {

	private static final InvertibleFunction<Entity, String> ENTITY_TO_STRING =
		InvertibleFunction.of(EntityManager::getGlobalEntityId,
			EntityManager::queryEntity);

	private static final Function<Entity, String> FORMAT_ENTITY =
		formatEntity(null);

	private static final Action<Entity> STORE_ENTITY = EntityManager::store;

	private static final Function<Entity, String> GET_GLOBAL_ENTITY_ID =
		e -> e != null ? EntityManager.getGlobalEntityId(e) : "";

	private static final Function<Entity, Number> GET_ENTITY_ID =
		e -> e != null ? e.get(e.getDefinition().getIdAttribute()) : null;

	/**
	 * Private, only static use.
	 */
	private EntityFunctions() {
	}

	/**
	 * Returns an invertible function that can convert an entity into a string
	 * and vice versa. The string representation of a entity will be it's
	 * global
	 * entity ID as returned by
	 * {@link EntityManager#getGlobalEntityId(Entity)}.
	 * When the function is inverted the entity will be queried through the
	 * method {@link EntityManager#queryEntity(String)}. If that causes an
	 * error
	 * a {@link FunctionException} will be thrown.
	 *
	 * @return The entity string conversion function
	 */
	public static InvertibleFunction<Entity, String> entityToString() {
		return ENTITY_TO_STRING;
	}

	/**
	 * Format an entity with a default instance of {@link EntityFormat}.
	 *
	 * @param entity The entity to format
	 * @return The resulting string or NULL, if the input entity is NULL
	 */
	public static String format(Entity entity) {
		return FORMAT_ENTITY.evaluate(entity);
	}

	/**
	 * Returns a function that formats an entity into a readable string.
	 *
	 * @param nullString The string to be displayed if the input entity is NULL
	 * @return The function for the string formatting
	 */
	public static <E extends Entity> Function<E, String> formatEntity(
		String nullString) {
		return new EntityFormat<>(nullString);
	}

	/**
	 * Creates a string for a formatted display of an entity's extra
	 * attributes.
	 *
	 * @param entity The entity
	 * @return The resulting string
	 * @throws StorageException If accessing the extra attributes fails
	 */
	public static String formatExtraAttributes(Entity entity) {
		StringBuilder result = new StringBuilder();

		for (RelationType<?> extraAttr : entity.getExtraAttributes()) {
			String name = extraAttr.getSimpleName();
			Object value = entity.getExtraAttribute(extraAttr, null);

			result.append(TextConvert.capitalize(name, " "));
			result.append(": ").append(value).append("\n");
		}

		return result.toString();
	}

	/**
	 * Returns a static function that returns the ID of a certain entity.
	 *
	 * @return A static function instance
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> Function<E, Number> getEntityId() {
		return (Function<E, Number>) GET_ENTITY_ID;
	}

	/**
	 * Returns a new function that retrieves a certain extra attribute from
	 * it's
	 * input entity.
	 *
	 * @param key          The extra attribute key
	 * @param defaultValue The default value if the attribute doesn't exist
	 * @return A new binary function instance
	 */
	public static <E extends Entity, V> GetExtraAttribute<E, V> getExtraAttribute(
		RelationType<V> key, V defaultValue) {
		return new GetExtraAttribute<>(key, defaultValue);
	}

	/**
	 * Returns a new function that returns the correctly typed value of an
	 * extra
	 * attribute with a certain key. If the key of the evaluated extra
	 * attribute
	 * doesn't match the argument key an {@link IllegalArgumentException} will
	 * be thrown from the function evaluation.
	 *
	 * @param extraAttributeKey The extra attribute key
	 * @return A new binary function instance
	 */
	public static <T> BinaryFunction<ExtraAttribute, RelationType<T>, T> getExtraAttributeValue(
		RelationType<T> extraAttributeKey) {
		return new AbstractBinaryFunction<ExtraAttribute, RelationType<T>, T>(
			extraAttributeKey, "getExtraAttributeValue") {
			@Override
			@SuppressWarnings("unchecked")
			public T evaluate(ExtraAttribute extraAttribute,
				RelationType<T> key) {
				if (key == extraAttribute.get(ExtraAttribute.KEY)) {
					return (T) extraAttribute.get(ExtraAttribute.VALUE);
				} else {
					String message = String.format("Invalid key %s for %s",
						key,
						extraAttribute);

					throw new IllegalArgumentException(message);
				}
			}
		};
	}

	/**
	 * Returns a static function that returns the global ID of a certain
	 * entity.
	 *
	 * @return A static function instance
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> Function<E, String> getGlobalEntityId() {
		return (Function<E, String>) GET_GLOBAL_ENTITY_ID;
	}

	/**
	 * Returns a function that returns an entity attribute from the entity's
	 * hierarchy.
	 *
	 * @see Entity#getUpwards(RelationType)
	 */
	public static <E extends Entity, T> BinaryFunction<E, RelationType<T>, T> getUpwards(
		RelationType<T> attribute) {
		return new AbstractBinaryFunction<E, RelationType<T>, T>(attribute,
			"getUpwards(%s)") {
			@Override
			public T evaluate(E entity, RelationType<T> attribute) {
				return entity.getUpwards(attribute);
			}
		};
	}

	/**
	 * Returns a new function that queries an entity with a certain global ID.
	 *
	 * @see EntityManager#queryEntity(String)
	 */
	public static Function<String, Entity> queryEntity() {
		return EntityManager::queryEntity;
	}

	/**
	 * Returns a new function that queries an entity of a certain type with a
	 * particular ID.
	 *
	 * @see EntityManager#queryEntity(Class, long)
	 */
	public static <E extends Entity> Function<Number, E> queryEntity(
		final Class<E> entityClass) {
		return id -> EntityManager.queryEntity(entityClass, id.longValue());
	}

	/**
	 * Returns a supplier which queries an entity that matches certain
	 * criteria.
	 *
	 * @see EntityManager#queryEntity(Class, Predicate, boolean)
	 */
	public static <E extends Entity> Supplier<E> queryEntity(
		final Class<E> entityClass, final Predicate<? super E> criteria,
		final boolean failOnMultiple) {
		return ThrowingSupplier.of(
			() -> EntityManager.queryEntity(entityClass, criteria,
				failOnMultiple));
	}

	/**
	 * Returns a new function that queries an entity with a certain attribute
	 * value.
	 *
	 * @param entityClass        The entity type to query
	 * @param attribute          The attribute to search for
	 * @param additionalCriteria Optional additional criteria or NULL for none
	 * @param failOnMultiple     If TRUE the call fails if multiple entities
	 *                             are
	 *                           found for the given criteria
	 * @return A new function instance
	 */
	public static <T, E extends Entity> java.util.function.Function<T, E> queryEntity(
		final Class<E> entityClass, final RelationType<T> attribute,
		final Predicate<? super E> additionalCriteria,
		final boolean failOnMultiple) {
		return new AttributeQueryFunction<T, E>(entityClass, attribute,
			additionalCriteria, failOnMultiple);
	}

	/**
	 * Returns a static function instance that stores the entity it receives as
	 * the input value. The input entity will then be returned as the output
	 * value to allow function chaining.
	 *
	 * @return A static function instance that stores an entity
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> Action<E> storeEntity() {
		return (Action<E>) STORE_ENTITY;
	}

	/**
	 * An element accessor that returns the value of a certain extra attribute
	 * from an entity input value.
	 */
	public static class GetExtraAttribute<E extends Entity, V>
		extends GetElement<E, RelationType<V>, V> {

		V defaultValue;

		/**
		 * Creates a new instance that accesses a particular single-type
		 * relation.
		 *
		 * @param key          The typed key of the extra attribute
		 * @param defaultValue The default value if the attribute doesn't exist
		 */
		public GetExtraAttribute(RelationType<V> key, V defaultValue) {
			super(key, "GetExtraAttribute[%s]");
			this.defaultValue = defaultValue;
		}

		/**
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		protected V getElementValue(E entity, RelationType<V> key) {
			try {
				return entity.getExtraAttribute(key, defaultValue);
			} catch (StorageException e) {
				throw new FunctionException(this, e);
			}
		}
	}

	/**
	 * An internal function implementation that performs an entity query for a
	 * certain attribute value.
	 *
	 * @author eso
	 */
	private static final class AttributeQueryFunction<T, E extends Entity>
		implements ThrowingFunction<T, E> {

		private final Class<E> entityClass;

		private final boolean failOnMultiple;

		private final RelationType<T> attribute;

		private final Predicate<? super E> additionalCriteria;

		/**
		 * Creates a new instance.
		 *
		 * @param entityClass        The entity type to query
		 * @param attribute          The attribute to search for
		 * @param additionalCriteria Optional additional criteria or NULL for
		 *                           none
		 * @param failOnMultiple     If TRUE the call fails if multiple
		 *                                 entities
		 *                           are found for the given criteria
		 */
		private AttributeQueryFunction(Class<E> entityClass,
			RelationType<T> attribute, Predicate<? super E> additionalCriteria,
			boolean failOnMultiple) {
			this.entityClass = entityClass;
			this.attribute = attribute;
			this.additionalCriteria = additionalCriteria;
			this.failOnMultiple = failOnMultiple;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public E tryApply(T value) throws StorageException {
			Predicate<E> criteria =
				ifAttribute(attribute, Predicates.equalTo(value));

			if (additionalCriteria != null) {
				criteria = criteria.and(additionalCriteria);
			}

			return EntityManager.queryEntity(entityClass, criteria,
				failOnMultiple);
		}
	}
}
