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
package de.esoco.data.element;

import de.esoco.lib.property.PropertyName;

import java.util.ArrayList;
import java.util.List;

/**
 * A data element that contains the result of the execution of a storage query.
 * The resulting query rows are encoded in a list of strings which are in turn
 * contained in a list of all rows for a certain query result. The easiest way
 * to process the result rows is by iterating over the result because it
 * implements the {@link Iterable} interface. Besides the query rows this class
 * also allows to query the full size of the executed query by invoking the
 * method {@link #getQuerySize()}.
 *
 * <p>Query results are always readonly.</p>
 *
 * @author eso
 */
public class QueryResultElement<T> extends ListDataElement<T> {

	private static final long serialVersionUID = 1L;

	private List<T> rows;

	private int querySize;

	/**
	 * Creates a new instance for a certain query result.
	 *
	 * @param name      The element name
	 * @param rows      A list containing lists of strings that contains the
	 *                    the
	 *                  query rows
	 * @param querySize The full size of the query represented by this result
	 */
	public QueryResultElement(String name, List<T> rows, int querySize) {
		super(name, null, null);

		this.rows = rows;
		this.querySize = querySize;
	}

	/**
	 * Default constructor for serialization.
	 */
	QueryResultElement() {
		rows = new ArrayList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryResultElement<T> copy(CopyMode mode,
		PropertyName<?>... copyProperties) {
		return (QueryResultElement<T>) super.copy(mode, copyProperties);
	}

	/**
	 * Returns the full query size for the query represented by this result.
	 *
	 * @return The full query size
	 */
	public final int getQuerySize() {
		return querySize;
	}

	/**
	 * Returns the list.
	 *
	 * @return The list
	 */
	@Override
	protected List<T> getList() {
		return rows;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected QueryResultElement<T> newInstance() {
		return new QueryResultElement<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateValue(List<T> newRows) {
		rows = newRows;
	}
}
