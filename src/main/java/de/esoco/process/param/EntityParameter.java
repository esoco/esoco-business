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
package de.esoco.process.param;

import de.esoco.entity.Entity;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.process.step.InteractionFragment;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StoragePredicates;
import org.obrel.core.RelationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static de.esoco.entity.EntityRelationTypes.ENTITY_ATTRIBUTES;
import static de.esoco.entity.EntityRelationTypes.ENTITY_QUERY_PREDICATE;
import static de.esoco.entity.EntityRelationTypes.ENTITY_SORT_PREDICATE;

/**
 * A parameter subclass that contains additional methods for accessing process
 * parameters with an {@link Entity} datatype.
 *
 * @author eso
 * @see ParameterBase
 */
public class EntityParameter<E extends Entity> extends Parameter<E> {

	/**
	 * @see Parameter#Parameter(InteractionFragment, RelationType) .
	 */
	public EntityParameter(InteractionFragment fragment,
		RelationType<E> paramType) {
		super(fragment, paramType);
	}

	/**
	 * Sets the attributes to be displayed for entity queries.
	 *
	 * @see #attributes(Collection)
	 */
	@SafeVarargs
	public final EntityParameter<E> attributes(
		Function<? super E, ?>... attributes) {
		return attributes(Arrays.asList(attributes));
	}

	/**
	 * Sets the attributes to be displayed for entity queries. The datatype of
	 * an attribute is defined as a function that queries the attributes
	 * from an
	 * entity. This applies to standard relation types as these are also
	 * functions that can be applied to relatable objects (like entities). But
	 * that can also be compound functions that generate the attribute value to
	 * displayed in a result table on access. An example could be a function
	 * that extracts the name from an entity reference (like
	 * NAME.from(OTHER_ENTITY)).
	 *
	 * @param attributes The entity attribute access functions
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final EntityParameter<E> attributes(
		Collection<Function<? super E, ?>> attributes) {
		List<Function<? super Entity, ?>> genericAttributes = null;

		if (attributes != null && attributes.size() > 0) {
			genericAttributes = new ArrayList<Function<? super Entity, ?>>();

			for (Function<? super E, ?> function : attributes) {
				genericAttributes.add((Function<? super Entity, ?>) function);
			}
		}

		fragment().annotateParameter(type(), null, ENTITY_ATTRIBUTES,
			genericAttributes);

		return this;
	}

	/**
	 * Returns the current storage query for this parameter, including the
	 * filter criteria that have been set by the user.
	 *
	 * @return The current query
	 */
	public final QueryPredicate<E> getCurrentQuery() {
		return fragment().getCurrentQuery(type());
	}

	/**
	 * Defines the ordering for queries on the entity type of this parameter.
	 *
	 * @param order The sort order criteria (NULL for none)
	 * @return This instance for concatenation
	 */
	public final EntityParameter<E> order(Predicate<? super Entity> order) {
		fragment().annotateParameter(type(), null, ENTITY_SORT_PREDICATE,
			order);

		return this;
	}

	/**
	 * Sets an ordering attribute for queries on the entity type of this
	 * parameter.
	 *
	 * @param orderAttribute The ordering attribute
	 * @param ascending      TRUE for ascending order, FALSE for descending
	 * @see #order(Predicate)
	 */
	public final EntityParameter<E> orderBy(RelationType<?> orderAttribute,
		boolean ascending) {
		return order(StoragePredicates.sortBy(orderAttribute, ascending));
	}

	/**
	 * Defines a general storage query without criteria for this parameter.
	 *
	 * @see #query(Predicate)
	 */
	public final EntityParameter<E> query() {
		return query(null);
	}

	/**
	 * Defines the storage query to be executed for this parameter.
	 *
	 * @param criteria The query criteria to apply (NULL for none)
	 * @return This instance for concatenation
	 */
	public final EntityParameter<E> query(Predicate<? super E> criteria) {
		@SuppressWarnings("unchecked")
		QueryPredicate<E> query =
			new QueryPredicate<E>((Class<E>) type().getTargetType(), criteria);

		fragment().annotateParameter(type(), null, ENTITY_QUERY_PREDICATE,
			query);

		return this;
	}

	/**
	 * Reloads the entity that is stored in this parameter. NULL values are
	 * ignored.
	 *
	 * @return This instance
	 */
	public EntityParameter<E> reloadEntity() {
		fragment().reloadEntity(paramType);

		return this;
	}
}
