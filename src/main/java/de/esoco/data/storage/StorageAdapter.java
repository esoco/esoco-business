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
package de.esoco.data.storage;

import de.esoco.data.element.QueryResultElement;
import de.esoco.data.element.StringDataElement;

import de.esoco.lib.model.ColumnDefinition;
import de.esoco.lib.model.DataModel;
import de.esoco.lib.property.StorageProperties;

import java.util.List;

/**
 * The interface for storage adapters that perform storage access based on data
 * elements.
 *
 * @author eso
 */
public interface StorageAdapter {

	/**
	 * Returns the definitions of the columns of the storage query.
	 *
	 * @return A list of column definitions (may be NULL)
	 */
	public List<ColumnDefinition> getColumns();

	/**
	 * An optional operation that returns the current query criteria of this
	 * instance. The result of this method may only be valid after a call to
	 * {@link #performQuery(StringDataElement)}. What kind of object is
	 * returned
	 * depends on the implementation.
	 *
	 * @return The current query predicate or NULL if no query has been
	 * executed
	 * yet
	 */
	public Object getCurrentQueryCriteria();

	/**
	 * Must be implemented by subclasses to perform a query. The query
	 * parameters data element must contain the mandatory query properties from
	 * {@link StorageProperties}.
	 *
	 * @param rQueryParams A data element containing the query parameters
	 * @return A query result data element
	 * @throws Exception If executing the query fails
	 */
	public QueryResultElement<DataModel<String>> performQuery(
		StringDataElement rQueryParams) throws Exception;
}
