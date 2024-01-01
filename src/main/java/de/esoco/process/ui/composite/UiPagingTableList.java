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
package de.esoco.process.ui.composite;

import de.esoco.lib.expression.monad.Option;
import de.esoco.lib.model.DataProvider;
import de.esoco.lib.property.Alignment;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.composite.UiListPanel.ExpandableListStyle;
import de.esoco.process.ui.container.UiColumnGridPanel;

/**
 * A composite that adds paging navigation to a {@link UiTableList}.
 *
 * @author eso
 */
public class UiPagingTableList<T> extends UiTableList<T> {

	private final String emptyPageLabel;

	private final UiColumnGridPanel toolPanel;

	private final UiPagingNavigation navigation;

	/**
	 * Creates a new instance with a simple table style.
	 *
	 * @param parent The parent container
	 */
	public UiPagingTableList(UiContainer<?> parent) {
		this(parent, Option.none());
	}

	/**
	 * Creates a new instance.
	 *
	 * @param parent      The parent container
	 * @param expandStyle The expand style
	 */
	public UiPagingTableList(UiContainer<?> parent,
		Option<ExpandableListStyle> expandStyle) {
		super(parent, expandStyle);

		toolPanel = new UiColumnGridPanel(this);
		navigation = new UiPagingNavigation(toolPanel, this::update, 10);

		navigation.cell().alignHorizontal(Alignment.END);
		navigation.setPageSizes(UiPagingNavigation.DEFAULT_PAGE_SIZES);

		emptyPageLabel = "$lbl" + getComponentStyleName() + "Empty";
	}

	/**
	 * Returns the text to be displayed in the navigation area if no data is
	 * available. By default this returns a static label generated from the
	 * component name which can be set with {@link #setEmptyPageLabel(String)}.
	 * But subclasses can also choose to override this method to return a label
	 * that has been generated from the current state.
	 *
	 * @return The empty page label
	 */
	public String getEmptyPagelLabel() {
		return emptyPageLabel;
	}

	/**
	 * Overridden to reset the navigation start position to zero.
	 *
	 * @see UiTableList#setData(DataProvider)
	 */
	@Override
	public void setData(DataProvider<T> rowDataProvider) {
		navigation.setPageStart(0);

		super.setData(rowDataProvider);
	}

	/**
	 * Sets the text to be displayed in the navigation area if no data is
	 * available so that the current page is empty.
	 *
	 * @param emptyPageLabel The emptyPageLabel value
	 */
	public final void setEmptyPageLabel(String emptyPageLabel) {
		emptyPageLabel = emptyPageLabel;
	}

	/**
	 * Displays the rows of the current page.
	 */
	@Override
	protected void update() {
		setSelection(null);
		displayRows(navigation.getPageStart(), navigation.getPageSize());
		navigation.setEmptyPageLabel(getEmptyPagelLabel());
		navigation.setTotalSize(getData().size());
	}
}
