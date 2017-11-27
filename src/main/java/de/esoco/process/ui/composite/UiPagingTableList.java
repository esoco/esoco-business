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
import de.esoco.lib.property.Alignment;

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.container.UiColumnGridPanel;


/********************************************************************
 * A composite that adds paging navigation to a {@link UiTableList}.
 *
 * @author eso
 */
public class UiPagingTableList<T> extends UiTableList<T>
{
	//~ Instance fields --------------------------------------------------------

	private String sEmptyPageLabel;

	private UiColumnGridPanel  aToolPanel;
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

		aToolPanel  = new UiColumnGridPanel(this);
		aNavigation = new UiPagingNavigation(aToolPanel, this::update, 10);

		aNavigation.cell().alignHorizontal(Alignment.END);
		aNavigation.setPageSizes(UiPagingNavigation.DEFAULT_PAGE_SIZES);

		sEmptyPageLabel = "$lbl" + getComponentStyleName() + "Empty";
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the text to be displayed in the navigation area if no data is
	 * available. By default this returns a static label generated from the
	 * component name which can be set with {@link #setEmptyPageLabel(String)}.
	 * But subclasses can also choose to override this method to return a label
	 * that has been generated from the current state.
	 *
	 * @return
	 */
	public String getEmptyPagelLabel()
	{
		return sEmptyPageLabel;
	}

	/***************************************
	 * Overridden to reset the navigation start position to zero.
	 *
	 * @see UiTableList#setData(DataProvider)
	 */
	@Override
	public void setData(DataProvider<T> rRowDataProvider)
	{
		aNavigation.setPageStart(0);

		super.setData(rRowDataProvider);
	}

	/***************************************
	 * Sets the text to be displayed in the navigation area if no data is
	 * available so that the current page is empty.
	 *
	 * @param rEmptyPageLabel The emptyPageLabel value
	 */
	public final void setEmptyPageLabel(String rEmptyPageLabel)
	{
		sEmptyPageLabel = rEmptyPageLabel;
	}

	/***************************************
	 * Displays the rows of the current page.
	 */
	@Override
	protected void update()
	{
		displayRows(aNavigation.getPageStart(), aNavigation.getPageSize());
		aNavigation.setEmptyPageLabel(getEmptyPagelLabel());
		aNavigation.setTotalSize(getData().size());
	}
}
