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
package de.esoco.process.ui.component;

import de.esoco.entity.Entity;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiTableControl;
import de.esoco.process.ui.UiTextInputField;
import de.esoco.process.ui.event.UiHasActionEvents;
import de.esoco.process.ui.event.UiHasUpdateEvents;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StoragePredicates;
import org.obrel.core.RelationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static de.esoco.entity.EntityRelationTypes.ENTITY_ATTRIBUTES;
import static de.esoco.entity.EntityRelationTypes.ENTITY_QUERY_PREDICATE;
import static de.esoco.entity.EntityRelationTypes.ENTITY_SORT_PREDICATE;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;

/**
 * A table that displays the result of a database query.
 *
 * @author eso
 */
public class UiQueryTable<E extends Entity>
	extends UiTableControl<E, UiQueryTable<E>>
	implements UiHasUpdateEvents<E, UiQueryTable<E>>,
	UiHasActionEvents<E, UiQueryTable<E>> {

	/**
	 * Creates a new instance.
	 *
	 * @see UiTextInputField#UiTextInputField(UiContainer, String)
	 */
	public UiQueryTable(UiContainer<?> container, Class<E> entityType) {
		super(container, entityType);

		// the existence of a query predicate initiates the table rendering
		setQuery(null);
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
	 * Returns the currently selected entity.
	 *
	 * @return The selected entity (NULL for none)
	 */
	public E getSelection() {
		return getValueImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UiQueryTable<E> onAction(Consumer<E> eventHandler) {
		return onSelectionConfirmed(eventHandler);
	}

	/**
	 * Sets the event handler for selection events of this table.
	 *
	 * @param eventHandler The event handler
	 * @return This instance for concatenation
	 */
	public final UiQueryTable<E> onSelection(Consumer<E> eventHandler) {
		return setParameterEventHandler(InteractionEventType.UPDATE,
			v -> eventHandler.accept(v));
	}

	/**
	 * Sets the event handler for selection confirmed events (e.g. by double
	 * click) of this table.
	 *
	 * @param eventHandler The event handler
	 * @return This instance for concatenation
	 */
	public final UiQueryTable<E> onSelectionConfirmed(
		Consumer<E> eventHandler) {
		return setParameterEventHandler(InteractionEventType.ACTION,
			v -> eventHandler.accept(v));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UiQueryTable<E> onUpdate(Consumer<E> eventHandler) {
		return onSelection(eventHandler);
	}

	/**
	 * Sets the default query order for a single attribute. For complex order
	 * criteria see {@link #setOrderBy(Predicate)}.
	 *
	 * @param orderAttribute The entity attribute to order the query by
	 * @param ascending      TRUE for ascending, FALSE for descending ordering
	 * @return This instance for fluent invocation
	 */
	public UiQueryTable<E> orderBy(RelationType<?> orderAttribute,
		boolean ascending) {
		setOrderBy(StoragePredicates.sortBy(orderAttribute, ascending));

		return this;
	}

	/**
	 * Sets the storage query criteria of this table.
	 *
	 * @param criteria The query criteria to apply (NULL for none)
	 * @return This instance for fluent invocation
	 */
	public UiQueryTable<E> query(Predicate<? super E> criteria) {
		@SuppressWarnings("unchecked")
		QueryPredicate<E> query =
			new QueryPredicate<E>((Class<E>) type().getTargetType(), criteria);

		fragment().annotateParameter(type(), null, ENTITY_QUERY_PREDICATE,
			query);

		return this;
	}

	/**
	 * Sets the attributes to be displayed for entity queries.
	 *
	 * @see #setColumns(Collection)
	 */
	@SafeVarargs
	public final void setColumns(Function<? super E, ?>... attributes) {
		setColumns(Arrays.asList(attributes));
	}

	/**
	 * Sets the entity attributes to be displayed as the table columns. The
	 * datatype of a column is a function that queries the attributes from an
	 * entity. This applies to standard relation types as these are also
	 * functions that can be applied to relatable objects (like entities). But
	 * that can also be compound functions that generate the attribute value to
	 * displayed in a result table on access. An example could be a function
	 * that extracts the name from an entity reference (e.g. <code>
	 * NAME.from(OTHER_ENTITY)</code>).
	 *
	 * @param columnAttributes The entity attribute access functions
	 */
	@SuppressWarnings("unchecked")
	public void setColumns(
		Collection<Function<? super E, ?>> columnAttributes) {
		List<Function<? super Entity, ?>> genericAttributes = null;

		if (columnAttributes != null && columnAttributes.size() > 0) {
			genericAttributes = new ArrayList<Function<? super Entity, ?>>();

			for (Function<? super E, ?> function : columnAttributes) {
				genericAttributes.add((Function<? super Entity, ?>) function);
			}
		}

		fragment().annotateParameter(type(), null, ENTITY_ATTRIBUTES,
			genericAttributes);
	}

	/**
	 * Sets the default query order with a predicate that may contain multiple
	 * order criteria. For ordering by a single entity attribute see method
	 * {@link #setOrderBy(RelationType, boolean)}.
	 *
	 * @param order The sort order criteria (NULL for none)
	 */
	public void setOrderBy(Predicate<? super Entity> order) {
		fragment().annotateParameter(type(), null, ENTITY_SORT_PREDICATE,
			order);
	}

	/**
	 * Sets the default query order for a single attribute. For complex order
	 * criteria see {@link #setOrderBy(Predicate)}.
	 *
	 * @param orderAttribute The entity attribute to order the query by
	 * @param ascending      TRUE for ascending, FALSE for descending ordering
	 */
	public void setOrderBy(RelationType<?> orderAttribute, boolean ascending) {
		orderBy(orderAttribute, ascending);
	}

	/**
	 * Sets the storage query criteria of this table.
	 *
	 * @param criteria The query criteria to apply (NULL for none)
	 */
	public void setQuery(Predicate<? super E> criteria) {
		query(criteria);
	}

	/**
	 * Sets (or clears) the currently selected entity.
	 *
	 * @param value The new selection or NULL for none
	 */
	@SuppressWarnings("boxing")
	public void setSelection(E value) {
		setValueImpl(value);

		// reset selection index for recalculation
		set(CURRENT_SELECTION, -1);
	}
}
