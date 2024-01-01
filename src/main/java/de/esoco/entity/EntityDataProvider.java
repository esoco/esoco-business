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

import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.model.AbstractDataProvider;
import de.esoco.lib.model.DataProvider;
import de.esoco.lib.property.SortDirection;
import de.esoco.storage.QueryPredicate;
import org.obrel.core.RelationType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static de.esoco.storage.StoragePredicates.sortBy;

/**
 * A {@link DataProvider} implementation that is based on entity queries.
 *
 * @author eso
 */
public class EntityDataProvider<E extends Entity>
	extends AbstractDataProvider<E> {

	private QueryPredicate<E> baseQuery;

	private QueryPredicate<E> visibleEntities;

	private Predicate<E> attributeFilter;

	private Predicate<E> wildcardFilter;

	private Predicate<E> sortPredicate;

	/**
	 * Creates a new instance that queries all entities of a certain type.
	 *
	 * @param entityType The class of the entity type to be queried
	 */
	public EntityDataProvider(Class<E> entityType) {
		this(entityType, null);
	}

	/**
	 * Creates a new instance for a certain entity type with default query
	 * criteria that will always be applied.
	 *
	 * @param baseQuery The base query to be executed if no additional
	 *                  constraints are applied
	 */
	public EntityDataProvider(QueryPredicate<E> baseQuery) {
		this.baseQuery = baseQuery;
		visibleEntities = baseQuery;
	}

	/**
	 * Creates a new instance that queries the entities of a certain type the
	 * match certain criteria.
	 *
	 * @param entityType      The class of the entity type to be queried
	 * @param defaultCriteria The criteria for which to limit the queried
	 *                        entities
	 */
	public EntityDataProvider(Class<E> entityType,
		Predicate<? super E> defaultCriteria) {
		this(new QueryPredicate<>(entityType, defaultCriteria));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<E> getData(int start, int count) {
		Collection<E> result = new ArrayList<>(count);

		try (EntityIterator<E> iterator = new EntityIterator<>(
			visibleEntities)) {
			iterator.setPosition(start, false);

			while (count-- > 0 && iterator.hasNext()) {
				result.add(iterator.next());
			}
		}

		return result;
	}

	/**
	 * Sets the default criteria.
	 *
	 * @param criteria The new default criteria
	 */
	public void setDefaultCriteria(Predicate<? super E> criteria) {
		baseQuery = new QueryPredicate<>(baseQuery.getQueryType(), criteria);

		updateVisibleEntities();
	}

	/**
	 * Sets a wildcard filter for certain text attributes of the entity type.
	 * See
	 * {@link EntityPredicates#createWildcardFilter(String, RelationType...)}
	 * for details.
	 *
	 * @param filter             wildcard The wildcard filter string (can be
	 *                           NULL or empty)
	 * @param filteredAttributes The text attributes to filter (can be NULL or
	 *                           empty)
	 */
	@SuppressWarnings("unchecked")
	public void setWildcardFilter(String filter,
		RelationType<String>... filteredAttributes) {
		wildcardFilter =
			EntityPredicates.createWildcardFilter(filter, filteredAttributes);

		updateVisibleEntities();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		try (EntityIterator<E> iterator = new EntityIterator<>(
			visibleEntities)) {
			return iterator.size();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void updateFilter(
		Map<Function<? super E, ?>, java.util.function.Predicate<?>> filters) {
		attributeFilter = null;

		for (Entry<Function<? super E, ?>, java.util.function.Predicate<?>> filter : filters.entrySet()) {
			Function<? super E, ?> attribute = filter.getKey();
			java.util.function.Predicate<?> entityFilter = filter.getValue();

			if (attribute instanceof RelationType &&
				filter instanceof de.esoco.lib.expression.Predicate) {
				RelationType<Object> entityAttr =
					(RelationType<Object>) attribute;

				Predicate<Object> criterion = (Predicate<Object>) entityFilter;

				attributeFilter = Predicates.and(attributeFilter,
					EntityPredicates.ifAttribute(entityAttr, criterion));
			}
		}

		updateVisibleEntities();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateSorting(
		Map<Function<? super E, ? extends Comparable<?>>, SortDirection> sortings) {
		sortPredicate = null;

		for (Entry<Function<? super E, ? extends Comparable<?>>,
			SortDirection> ordering : sortings.entrySet()) {
			Function<? super E, ?> attribute = ordering.getKey();
			SortDirection direction = ordering.getValue();

			if (attribute instanceof RelationType) {
				sortPredicate = Predicates.and(sortPredicate,
					sortBy((RelationType<?>) attribute,
						direction == SortDirection.ASCENDING));
			}
		}

		updateVisibleEntities();
	}

	/**
	 * Updates the query of the visible entities.
	 */
	protected void updateVisibleEntities() {
		Predicate<E> criteria =
			Predicates.and(baseQuery.getCriteria(), attributeFilter);

		criteria = Predicates.and(criteria, attributeFilter);
		criteria = Predicates.and(criteria, wildcardFilter);
		criteria = Predicates.and(criteria, sortPredicate);

		visibleEntities =
			new QueryPredicate<>(baseQuery.getQueryType(), criteria);
	}
}
