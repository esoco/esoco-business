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
package de.esoco.process.ui.component;

import de.esoco.entity.Entity;

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;

import de.esoco.process.ui.Container;
import de.esoco.process.ui.TableControl;
import de.esoco.process.ui.TextInputField;

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
 * A single-line text input field.
 *
 * @author eso
 */
public class QueryTable<E extends Entity> extends TableControl<E, QueryTable<E>>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @see TextInputField#TextInput(Container, String)
	 */
	public QueryTable(Container<?> rContainer, Class<E> rEntityType)
	{
		super(rContainer, rEntityType);

		setQuery(null);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the current storage query for this parameter, including the
	 * filter criteria that have been set by the user.
	 *
	 * @return The current query
	 */
	public final QueryPredicate<E> getCurrentQuery()
	{
		return fragment().getCurrentQuery(type());
	}

	/***************************************
	 * Sets the attributes to be displayed for entity queries.
	 *
	 * @see #setColumns(Collection)
	 */
	@SafeVarargs
	public final void setColumns(Function<? super E, ?>... rAttributes)
	{
		setColumns(Arrays.asList(rAttributes));
	}

	/***************************************
	 * Sets the entity attributes to be displayed as the table columns. The
	 * datatype of a column is a function that queries the attributes from an
	 * entity. This applies to standard relation types as these are also
	 * functions that can be applied to relatable objects (like entities). But
	 * that can also be compound functions that generate the attribute value to
	 * displayed in a result table on access. An example could be a function
	 * that extracts the name from an entity reference (e.g. <code>
	 * NAME.from(OTHER_ENTITY)</code>).
	 *
	 * @param rColumnAttributes The entity attribute access functions
	 */
	@SuppressWarnings("unchecked")
	public void setColumns(Collection<Function<? super E, ?>> rColumnAttributes)
	{
		List<Function<? super Entity, ?>> rGenericAttributes = null;

		if (rColumnAttributes != null && rColumnAttributes.size() > 0)
		{
			rGenericAttributes = new ArrayList<Function<? super Entity, ?>>();

			for (Function<? super E, ?> rFunction : rColumnAttributes)
			{
				rGenericAttributes.add((Function<? super Entity, ?>) rFunction);
			}
		}

		fragment().annotateParameter(type(),
									 null,
									 ENTITY_ATTRIBUTES,
									 rGenericAttributes);
	}

	/***************************************
	 * Sets the default query order with a predicate that may contain multiple
	 * order criteria. For ordering by a single entity attribute see method
	 * {@link #setOrderBy(RelationType, boolean)}.
	 *
	 * @param pOrder The sort order criteria (NULL for none)
	 */
	public void setOrderBy(Predicate<? super Entity> pOrder)
	{
		fragment().annotateParameter(type(),
									 null,
									 ENTITY_SORT_PREDICATE,
									 pOrder);
	}

	/***************************************
	 * Sets the default query order for a single attribute. For complex order
	 * criteria see {@link #setOrderBy(Predicate)}.
	 *
	 * @param rOrderAttribute The entity attribute to order the query by
	 * @param bAscending      TRUE for ascending, FALSE for descending ordering
	 */
	public void setOrderBy(RelationType<?> rOrderAttribute, boolean bAscending)
	{
		setOrderBy(StoragePredicates.sortBy(rOrderAttribute, bAscending));
	}

	/***************************************
	 * Sets the storage query criteria of this table.
	 *
	 * @param pCriteria The query criteria to apply (NULL for none)
	 */
	public void setQuery(Predicate<? super E> pCriteria)
	{
		@SuppressWarnings("unchecked")
		QueryPredicate<E> pQuery =
			new QueryPredicate<E>((Class<E>) type().getTargetType(), pCriteria);

		fragment().annotateParameter(type(),
									 null,
									 ENTITY_QUERY_PREDICATE,
									 pQuery);
	}
}
