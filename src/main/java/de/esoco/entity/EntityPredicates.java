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

import de.esoco.entity.EntityFunctions.GetExtraAttribute;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.expression.function.GetElement.GetRelationValue;
import de.esoco.lib.expression.predicate.ElementPredicate;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StoragePredicates;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;

import static de.esoco.entity.EntityFunctions.getExtraAttribute;
import static de.esoco.lib.expression.Functions.coerce;
import static de.esoco.lib.expression.ReflectionFuntions.cast;
import static de.esoco.lib.expression.StringFunctions.substring;
import static de.esoco.storage.StoragePredicates.like;
import static de.esoco.storage.StoragePredicates.refersTo;

/**
 * This class defines several factory methods that create entity-specific
 * predicates. These methods provide a way to create predicates in a better
 * readable way, especially when concatenating predicates.
 *
 * @author eso
 */
public class EntityPredicates {

	/**
	 * Private, only static use.
	 */
	private EntityPredicates() {
	}

	/**
	 * Creates a wildcard filter predicate for certain text attributes of an
	 * entity type. The search string may contain wildcards as defined for
	 * {@link StoragePredicates#createWildcardFilter(String)}. This method
	 * accepts NULL and empty arguments so that it can be invoked without the
	 * need to check the arguments. Such empty arguments will result in NULL
	 * being returned (which can then for example be fed into
	 * {@link Predicates#and(Predicate, Predicate)}).
	 *
	 * @param wildcard           The wildcard filter string (can be NULL or
	 *                           empty)
	 * @param filteredAttributes The text attributes to filter (can be NULL or
	 *                           empty)
	 * @return The resulting predicate (may be NULL if arguments are NULL or
	 * empty)
	 */
	@SuppressWarnings("unchecked")
	public static <E> Predicate<E> createWildcardFilter(String wildcard,
		RelationType<String>... filteredAttributes) {
		Predicate<E> wildcardFilter = null;

		if (wildcard != null && !wildcard.isEmpty() &&
			filteredAttributes != null) {
			Predicate<String> wildcardMatch =
				StoragePredicates.createWildcardFilter(wildcard);

			for (RelationType<String> attr : filteredAttributes) {
				wildcardFilter = (Predicate<E>) Predicates.or(wildcardFilter,
					attr.is(wildcardMatch));
			}
		}

		return wildcardFilter;
	}

	/**
	 * A variant of {@link StoragePredicates#forType(Class, Predicate)} for
	 * entity queries.
	 *
	 * @see StoragePredicates#forType(Class, Predicate)
	 */
	public static <E extends Entity> QueryPredicate<E> forEntity(
		Class<E> entityClass, Predicate<? super E> criteria) {
		return StoragePredicates.forType(entityClass, criteria);
	}

	/**
	 * Creates an entity predicate to query for entities that have an extra
	 * attribute that fulfill certain criteria.
	 *
	 * @param entityType The entity type to query for
	 * @param criteria   The extra attribute criteria
	 * @return A new predicate for the given extra attribute
	 */
	public static <E extends Entity, V> Predicate<E> hasExtraAttribute(
		Class<E> entityType, Predicate<? super ExtraAttribute> criteria) {
		EntityDefinition<? extends Entity> def =
			EntityManager.getEntityDefinition(entityType);

		RelationType<Number> idAttr = def.getIdAttribute();

		String idPrefix = def.getIdPrefix();
		int idStart = idPrefix.length() + 1;

		// coerce attribute into a function that can be chained with substring;
		// this and the subsequent functions can only be used for storage
		// parsing
		Function<Relatable, String> attr = coerce(ExtraAttribute.ENTITY);

		Function<Relatable, ? super Number> extraAttributeEntityId =
			cast(idAttr.getTargetType()).from(
				substring(idStart, -1).from(attr));

		criteria = Predicates.and(criteria,
			ifAttribute(ExtraAttribute.ENTITY, like(idPrefix + "%")));

		Predicate<? super Number> hasExtraAttr =
			refersTo(ExtraAttribute.class, extraAttributeEntityId, criteria);

		return ifAttribute(idAttr, hasExtraAttr);
	}

	/**
	 * A {@link Predicates#ifRelation(RelationType, Predicate)} variant that
	 * provides better readability for entity attributes.
	 *
	 * @see Predicates#ifRelation(RelationType, Predicate)
	 */
	public static <E extends Entity, V> ElementPredicate<E, V> ifAttribute(
		RelationType<V> type, Predicate<? super V> predicate) {
		return Predicates.ifRelation(type, predicate);
	}

	/**
	 * Creates an {@link ElementPredicate} to evaluate a predicate on a certain
	 * extra attribute in entities. It creates an instance of the function
	 * {@link GetRelationValue} to retrieve the relation target from objects.
	 *
	 * <p>This predicate cannot be used for entity queries. It is only intended
	 * for the use in predicate evaluation on entities that have already been
	 * loaded. For queries the {@link #hasExtraAttribute(Class, Predicate)}
	 * method should be used instead.</p>
	 *
	 * @param extraAttribute The relation type of the extra attribute
	 * @param defaultValue   The default value if the attribute doesn't exist
	 * @param predicate      The predicate to evaluate the extra attribute with
	 * @return A new predicate for the given extra attribute
	 */
	public static <E extends Entity, V> Predicate<E> ifExtraAttribute(
		RelationType<V> extraAttribute, V defaultValue,
		Predicate<? super V> predicate) {
		GetExtraAttribute<E, V> getExtraAttribute =
			getExtraAttribute(extraAttribute, defaultValue);

		return new ElementPredicate<E, V>(getExtraAttribute, predicate);
	}
}
