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

import de.esoco.process.ui.UiContainer;


/********************************************************************
 * A composite that adds paging navigation to a {@link UiTableList}.
 *
 * @author eso
 */
public class UiPagingTableList<T> extends UiTableList<T>
{
	//~ Instance fields --------------------------------------------------------

	private UiPagingNavigation aNavigation;

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
		super(rParent, eExpandStyle);

		aNavigation = new UiPagingNavigation(this, this::update);
		aNavigation.setPageSizes(UiPagingNavigation.DEFAULT_PAGE_SIZES);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Displays the rows of the current page.
	 */
	@Override
	protected void displayData()
	{
		displayRows(aNavigation.getPageStart(), aNavigation.getPageSize());
	}

	/***************************************
	 * Updates this list on events.
	 */
	private void update()
	{
		aNavigation.setTotalSize(getData().size());
		displayData();
	}
}
