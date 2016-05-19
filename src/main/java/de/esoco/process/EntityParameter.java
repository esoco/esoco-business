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
package de.esoco.process;

import de.esoco.entity.Entity;

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;

import de.esoco.process.step.InteractionFragment;

import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StoragePredicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.obrel.core.RelationType;

import static de.esoco.entity.EntityRelationTypes.ENTITY_ATTRIBUTES;
import static de.esoco.entity.EntityRelationTypes.ENTITY_QUERY_PREDICATE;
import static de.esoco.entity.EntityRelationTypes.ENTITY_SORT_PREDICATE;


/********************************************************************
 * A parameter subclass that contains additional methods for accessing process
 * parameters with an {@link Entity} datatype.
 *
 * @author eso
 * @see    ParameterBase
 */
public class EntityParameter<E extends Entity> extends Parameter<E>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public EntityParameter(
		InteractionFragment rFragment,
		RelationType<E>		rParamType)
	{
		super(rFragment, rParamType);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the attributes to be displayed for entity queries.
	 *
	 * @see #attributes(Collection)
	 */
	@SafeVarargs
	public final EntityParameter<E> attributes(
		Function<? super E, ?>... rAttributes)
	{
		return attributes(Arrays.asList(rAttributes));
	}

	/***************************************
	 * Sets the attributes to be displayed for entity queries. The datatype of
	 * an attribute is defined as a function that queries the attributes from an
	 * entity. This applies to standard relation types as these are also
	 * functions that can be applied to relatable objects (like entities). But
	 * that can also be compound functions that generate the attribute value to
	 * displayed in a result table on access. An example could be a function
	 * that extracts the name from an entity reference (like
	 * NAME.from(OTHER_ENTITY)).
	 *
	 * @param  rAttributes The entity attribute access functions
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final EntityParameter<E> attributes(
		Collection<Function<? super E, ?>> rAttributes)
	{
		List<Function<? super Entity, ?>> rGenericAttributes = null;

		if (rAttributes != null && rAttributes.size() > 0)
		{
			rGenericAttributes = new ArrayList<Function<? super Entity, ?>>();

			for (Function<? super E, ?> rFunction : rAttributes)
			{
				rGenericAttributes.add((Function<? super Entity, ?>) rFunction);
			}
		}

		fragment().annotateParameter(type(),
									 null,
									 ENTITY_ATTRIBUTES,
									 rGenericAttributes);

		return this;
	}

	/***************************************
	 * Defines the ordering for queries on the entity type of this parameter.
	 *
	 * @param  pOrder The sort order criteria (NULL for none)
	 *
	 * @return This instance for concatenation
	 */
	public final EntityParameter<E> order(Predicate<? super Entity> pOrder)
	{
		fragment().annotateParameter(type(),
									 null,
									 ENTITY_SORT_PREDICATE,
									 pOrder);

		return this;
	}

	/***************************************
	 * Sets an ordering attribute for queries on the entity type of this
	 * parameter.
	 *
	 * @param rOrderAttribute The ordering attribute
	 * @param bAscending      TRUE for ascending order, FALSE for descending
	 *
	 * @see   #order(Predicate)
	 */
	public final EntityParameter<E> orderBy(
		RelationType<?> rOrderAttribute,
		boolean			bAscending)
	{
		return order(StoragePredicates.sortBy(rOrderAttribute, bAscending));
	}

	/***************************************
	 * Sets the ordering attributes for queries on the entity type of this
	 * parameter.
	 *
	 * @param rOrderAttribute            The primary sort attribute
	 * @param rAdditionalOrderAttributes Optional additional sort attributes
	 *
	 * @see   #order(Predicate)
	 */
	public final EntityParameter<E> orderBy(
		RelationType<?>    rOrderAttribute,
		RelationType<?>... rAdditionalOrderAttributes)
	{
		return order(StoragePredicates.sortBy(rOrderAttribute,
											  rAdditionalOrderAttributes));
	}

	/***************************************
	 * Defines a general storage query without criteria for this parameter.
	 *
	 * @see #query(Predicate)
	 */
	public final EntityParameter<E> query()
	{
		return query(null);
	}

	/***************************************
	 * Defines the storage query to be executed for this parameter.
	 *
	 * @param  pCriteria The query criteria to apply (NULL for none)
	 *
	 * @return This instance for concatenation
	 */
	public final EntityParameter<E> query(Predicate<? super E> pCriteria)
	{
		@SuppressWarnings("unchecked")
		QueryPredicate<E> pQuery =
			new QueryPredicate<E>((Class<E>) type().getTargetType(), pCriteria);

		fragment().annotateParameter(type(),
									 null,
									 ENTITY_QUERY_PREDICATE,
									 pQuery);

		return this;
	}
}
