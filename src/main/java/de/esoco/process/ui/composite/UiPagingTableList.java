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
package de.esoco.process.ui.composite;

import de.esoco.lib.model.DataProvider;

import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.composite.UiTableList.ExpandableTableStyle;
import de.esoco.process.ui.layout.UiFlowLayout;

import java.util.Collection;


/********************************************************************
 * A composite that adds paging navigation to a {@link UiTableList}.
 *
 * @author eso
 */
public class UiPagingTableList<T> extends UiComposite<UiPagingTableList<T>>
{
	//~ Instance fields --------------------------------------------------------

	private UiTableList<T>     aTable;
	private UiPagingNavigation aNavigation;

	private DataProvider<T> rDataProvider;
	int					    nStartRow;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a simple table style.
	 *
	 * @param rParent The parent container
	 */
	public UiPagingTableList(UiContainer<?> rParent)
	{
		this(rParent, null);
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent      The parent container
	 * @param eExpandStyle The expand style
	 */
	public UiPagingTableList(
		UiContainer<?>		 rParent,
		ExpandableTableStyle eExpandStyle)
	{
		super(rParent, new UiFlowLayout());

		aTable	    = new UiTableList<>(this, eExpandStyle);
		aNavigation = new UiPagingNavigation(rParent, this::handlePageChange);

		aNavigation.setPageSizes(this::handlePageSizeChange,
								 UiPagingNavigation.DEFAULT_PAGE_SIZES);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the data provider for the table rows.
	 *
	 * @param rDataProvider The new data
	 */
	public void setData(DataProvider<T> rDataProvider)
	{
		this.rDataProvider = rDataProvider;

		displayData(aNavigation.getPageStart(), aNavigation.getPageSize());
	}

	/***************************************
	 * Displays data from the data provider in table rows.
	 *
	 * @param nStart nPageSize
	 * @param nCount rPageStart TODO: DOCUMENT ME!
	 */
	protected void displayData(int nStart, int nCount)
	{
		Collection<T> rData = rDataProvider.getData(nStart, nCount);

		aTable.updateRows(rData);
	}

	/***************************************
	 * Handles changes of the currently displayed page.
	 *
	 * @param nNewPageStart The starting index of the current page
	 */
	private void handlePageChange(int nNewPageStart)
	{
	}

	/***************************************
	 * Handles changes of the displayed page size.
	 *
	 * @param nNewPageStart The new page size
	 */
	private void handlePageSizeChange(int nNewPageStart)
	{
	}
}
