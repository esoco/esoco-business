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

import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.model.AbstractDataProvider;
import de.esoco.lib.model.AttributeBinding;
import de.esoco.lib.model.DataProvider;

import de.esoco.storage.QueryPredicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import org.obrel.core.RelationType;

import static de.esoco.storage.StoragePredicates.sortBy;


/********************************************************************
 * A {@link DataProvider} implementation that is based on entity queries.
 *
 * @author eso
 */
public class EntityDataProvider<E extends Entity>
	extends AbstractDataProvider<E>
{
	//~ Instance fields --------------------------------------------------------

	private QueryPredicate<E> qBaseQuery;
	private QueryPredicate<E> qVisibleEntities;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that queries all entities of a certain type.
	 *
	 * @param rEntityType The class of the entity type to be queried
	 */
	public EntityDataProvider(Class<E> rEntityType)
	{
		this(rEntityType, null);
	}

	/***************************************
	 * Creates a new instance for a certain entity type with default query
	 * criteria that will always be applied.
	 *
	 * @param qBaseQuery The base query to be executed if no additional
	 *                   constraints are applied
	 */
	public EntityDataProvider(QueryPredicate<E> qBaseQuery)
	{
		this.qBaseQuery  = qBaseQuery;
		qVisibleEntities = qBaseQuery;
	}

	/***************************************
	 * Creates a new instance that queries the entities of a certain type the
	 * match certain criteria.
	 *
	 * @param rEntityType The class of the entity type to be queried
	 * @param pCriteria   The criteria for which to limit the queried entities
	 */
	public EntityDataProvider(
		Class<E>			 rEntityType,
		Predicate<? super E> pCriteria)
	{
		this(new QueryPredicate<>(rEntityType, pCriteria));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Collection<E> getData(int nStart, int nCount)
	{
		Collection<E> aResult = new ArrayList<>(nCount);

		try (EntityIterator<E> aIterator =
			 new EntityIterator<>(qVisibleEntities))
		{
			aIterator.setPosition(nStart, false);

			while (nCount-- > 0 && aIterator.hasNext())
			{
				aResult.add(aIterator.next());
			}
		}

		return aResult;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		try (EntityIterator<E> aIterator =
			 new EntityIterator<>(qVisibleEntities))
		{
			return aIterator.size();
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void applyConstraints()
	{
		de.esoco.lib.expression.Predicate<E> pCriteria = null;

		for (Entry<AttributeBinding<E, ?>, java.util.function.Predicate<?>> rFilter :
			 getAttributeFilters().entrySet())
		{
			AttributeBinding<E, ?>		    rAttribute = rFilter.getKey();
			java.util.function.Predicate<?> pFilter    = rFilter.getValue();

			if (rAttribute instanceof RelationType &&
				pFilter instanceof de.esoco.lib.expression.Predicate)
			{
				RelationType<Object>					  rEntityAttr =
					(RelationType<Object>) rAttribute;
				de.esoco.lib.expression.Predicate<Object> pCriterion  =
					(de.esoco.lib.expression.Predicate<Object>) pFilter;

				pCriteria =
					Predicates.and(pCriteria,
								   EntityPredicates.ifAttribute(rEntityAttr,
																pCriterion));
			}
		}

		Predicate<E> pOrdering = null;

		for (Entry<AttributeBinding<E, ? extends Comparable<?>>, OrderDirection> rOrdering :
			 getAttributeOrders().entrySet())
		{
			AttributeBinding<E, ?> rAttribute = rOrdering.getKey();
			OrderDirection		   eDirection = rOrdering.getValue();

			if (rAttribute instanceof RelationType)
			{
				pOrdering =
					Predicates.and(pOrdering,
								   sortBy((RelationType<?>) rAttribute,
										  eDirection ==
										  OrderDirection.ASCENDING));
			}
		}

		pCriteria = Predicates.and(pCriteria, pOrdering);

		qVisibleEntities =
			new QueryPredicate<>(qBaseQuery.getQueryType(),
								 Predicates.and(qBaseQuery.getCriteria(),
												pCriteria));
	}
}
