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
package de.esoco.data.element;

import java.util.ArrayList;
import java.util.List;


/********************************************************************
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
public class QueryResultElement<T> extends ListDataElement<T>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private List<T> rRows;
	private int     nQuerySize;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain query result.
	 *
	 * @param sName      The element name
	 * @param rRows      A list containing lists of strings that contains the
	 *                   the query rows
	 * @param nQuerySize The full size of the query represented by this result
	 */
	public QueryResultElement(String sName, List<T> rRows, int nQuerySize)
	{
		super(sName, null, null);

		this.rRows	    = rRows;
		this.nQuerySize = nQuerySize;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	QueryResultElement()
	{
		rRows = new ArrayList<>();
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public QueryResultElement<T> copy(CopyMode eMode)
	{
		return (QueryResultElement<T>) super.copy(eMode);
	}

	/***************************************
	 * Returns the full query size for the query represented by this result.
	 *
	 * @return The full query size
	 */
	public final int getQuerySize()
	{
		return nQuerySize;
	}

	/***************************************
	 * Returns the list.
	 *
	 * @return The list
	 */
	@Override
	protected List<T> getList()
	{
		return rRows;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected QueryResultElement<T> newInstance()
	{
		return new QueryResultElement<>();
	}
}
