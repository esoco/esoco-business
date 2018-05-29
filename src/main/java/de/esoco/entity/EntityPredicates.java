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


/********************************************************************
 * This class defines several factory methods that create entity-specific
 * predicates. These methods provide a way to create predicates in a better
 * readable way, especially when concatenating predicates.
 *
 * @author eso
 */
public class EntityPredicates
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private EntityPredicates()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a wildcard filter predicate for certain text attributes of an
	 * entity type. The search string may contain wildcards as defined for
	 * {@link StoragePredicates#createWildcardFilter(String)}. This method
	 * accepts NULL and empty arguments so that it can be invoked without the
	 * need to check the arguments. Such empty arguments will result in NULL
	 * being returned (which can then for example be fed into {@link
	 * Predicates#and(Predicate, Predicate)}).
	 *
	 * @param  sWildcard           The wildcard filter string (can be NULL or
	 *                             empty)
	 * @param  rFilteredAttributes The text attributes to filter (can be NULL or
	 *                             empty)
	 *
	 * @return The resulting predicate (may be NULL if arguments are NULL or
	 *         empty)
	 */
	@SuppressWarnings("unchecked")
	public static <E> Predicate<E> createWildcardFilter(
		String					sWildcard,
		RelationType<String>... rFilteredAttributes)
	{
		Predicate<E> pWildcardFilter = null;

		if (sWildcard != null &&
			!sWildcard.isEmpty() &&
			rFilteredAttributes != null)
		{
			Predicate<String> pWildcardMatch =
				StoragePredicates.createWildcardFilter(sWildcard);

			for (RelationType<String> rAttr : rFilteredAttributes)
			{
				pWildcardFilter =
					(Predicate<E>) Predicates.or(pWildcardFilter,
												 rAttr.is(pWildcardMatch));
			}
		}

		return pWildcardFilter;
	}

	/***************************************
	 * A variant of {@link StoragePredicates#forType(Class, Predicate)} for
	 * entity queries.
	 *
	 * @see StoragePredicates#forType(Class, Predicate)
	 */
	public static <E extends Entity> QueryPredicate<E> forEntity(
		Class<E>			 rEntityClass,
		Predicate<? super E> rCriteria)
	{
		return StoragePredicates.forType(rEntityClass, rCriteria);
	}

	/***************************************
	 * Creates an entity predicate to query for entities that have an extra
	 * attribute that fulfill certain criteria.
	 *
	 * @param  rEntityType The entity type to query for
	 * @param  pCriteria   The extra attribute criteria
	 *
	 * @return A new predicate for the given extra attribute
	 */
	public static <E extends Entity, V> Predicate<E> hasExtraAttribute(
		Class<E>						  rEntityType,
		Predicate<? super ExtraAttribute> pCriteria)
	{
		EntityDefinition<? extends Entity> rDef =
			EntityManager.getEntityDefinition(rEntityType);

		RelationType<Number> rIdAttr = rDef.getIdAttribute();

		String sIdPrefix = rDef.getIdPrefix();
		int    nIdStart  = sIdPrefix.length() + 1;

		// coerce attribute into a function that can be chained with substring;
		// this and the subsequent functions can only be used for storage parsing
		Function<Relatable, String> fAttr = coerce(ExtraAttribute.ENTITY);

		Function<Relatable, ? super Number> fExtraAttributeEntityId =
			cast(rIdAttr.getValueType()).from(substring(nIdStart, -1).from(fAttr));

		pCriteria =
			Predicates.and(pCriteria,
						   ifAttribute(ExtraAttribute.ENTITY,
									   like(sIdPrefix + "%")));

		Predicate<? super Number> pHasExtraAttr =
			refersTo(ExtraAttribute.class, fExtraAttributeEntityId, pCriteria);

		return ifAttribute(rIdAttr, pHasExtraAttr);
	}

	/***************************************
	 * A {@link Predicates#ifRelation(RelationType, Predicate)} variant that
	 * provides better readability for entity attributes.
	 *
	 * @see Predicates#ifRelation(RelationType, Predicate)
	 */
	public static <E extends Entity, V> ElementPredicate<E, V> ifAttribute(
		RelationType<V>		 rType,
		Predicate<? super V> rPredicate)
	{
		return Predicates.ifRelation(rType, rPredicate);
	}

	/***************************************
	 * Creates an {@link ElementPredicate} to evaluate a predicate on a certain
	 * extra attribute in entities. It creates an instance of the function
	 * {@link GetRelationValue} to retrieve the relation target from objects.
	 *
	 * <p>This predicate cannot be used for entity queries. It is only intended
	 * for the use in predicate evaluation on entities that have already been
	 * loaded. For queries the {@link #hasExtraAttribute(Class, Predicate)}
	 * method should be used instead.</p>
	 *
	 * @param  rExtraAttribute The relation type of the extra attribute
	 * @param  rDefaultValue   The default value if the attribute doesn't exist
	 * @param  rPredicate      The predicate to evaluate the extra attribute
	 *                         with
	 *
	 * @return A new predicate for the given extra attribute
	 */
	public static <E extends Entity, V> Predicate<E> ifExtraAttribute(
		RelationType<V>		 rExtraAttribute,
		V					 rDefaultValue,
		Predicate<? super V> rPredicate)
	{
		GetExtraAttribute<E, V> fGetExtraAttribute =
			getExtraAttribute(rExtraAttribute, rDefaultValue);

		return new ElementPredicate<E, V>(fGetExtraAttribute, rPredicate);
	}
}
