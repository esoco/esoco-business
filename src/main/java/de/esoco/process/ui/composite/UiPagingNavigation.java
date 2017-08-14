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

import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.component.UiDropDown;
import de.esoco.process.ui.layout.UiTableLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


/********************************************************************
 * A composites that contains the components needed for the navigation through
 * pages of data.
 *
 * @author eso
 */
public class UiPagingNavigation extends UiComposite<UiPagingNavigation>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of paging navigation action.
	 */
	public enum NavigationAction
	{
		FIRST_PAGE, PREVIOUS_PAGE, NEXT_PAGE, LAST_PAGE
	}

	//~ Static fields/initializers ---------------------------------------------

	/**
	 * A constant array of default page sizes that can be used for {@link
	 * #setPageSizes(int...)}.
	 */
	public static final int[] DEFAULT_PAGE_SIZES =
		new int[] { 5, 10, 20, 25, 50, 100 };

	//~ Instance fields --------------------------------------------------------

	private int   nStartIndex = 0;
	private int   nPageSize;
	private int[] aPageSizes  = null;

	private UiDropDown<String> aPageSizeSelector;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent            The parent container
	 * @param fPageChangeHandler A consumer to be invoked with the start index
	 *                           of a page after a paging event
	 */
	public UiPagingNavigation(
		UiContainer<?>    rParent,
		Consumer<Integer> fPageChangeHandler)
	{
		super(rParent, new UiTableLayout(6));

		aPageSizeSelector =
			builder().addDropDown(String.class).setVisible(false);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the page sizes that can be selected by the user.
	 *
	 * @return The array list sizes or NULL for none
	 */
	public final int[] getPageSizes()
	{
		return aPageSizes;
	}

	/***************************************
	 * Sets the page sizes that can be selected by the user. If set to NULL no
	 * page size selection will be available.
	 *
	 * @param fPageSizeHandler TODO: DOCUMENT ME!
	 * @param rPageSizes       The selectable page sizes or NULL to hide the
	 *                         page size selection
	 */
	public final void setPageSizes(
		Consumer<Integer> fPageSizeHandler,
		int... 			  rPageSizes)
	{
		aPageSizes = rPageSizes;

		if (rPageSizes != null)
		{
			List<String> aPageSizeValues = new ArrayList<>(aPageSizes.length);

			for (int nPageSize : aPageSizes)
			{
				aPageSizeValues.add(Integer.toString(nPageSize));
			}

			aPageSizeSelector.setListValues(aPageSizeValues);
		}
	}
}
