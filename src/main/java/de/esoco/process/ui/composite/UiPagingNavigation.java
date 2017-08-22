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

import de.esoco.lib.property.Updatable;

import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.component.UiDropDown;
import de.esoco.process.ui.component.UiIconButton;
import de.esoco.process.ui.component.UiLabel;
import de.esoco.process.ui.graphics.UiIconSupplier;
import de.esoco.process.ui.graphics.UiStandardIcon;
import de.esoco.process.ui.layout.UiTableLayout;

import java.util.ArrayList;
import java.util.List;


/********************************************************************
 * A composites that contains the components needed for the navigation through
 * pages of data.
 *
 * @author eso
 */
public class UiPagingNavigation extends UiComposite<UiPagingNavigation>
{
	//~ Static fields/initializers ---------------------------------------------

	/**
	 * A constant array of default page sizes that can be used for {@link
	 * #setPageSizes(int...)}.
	 */
	public static final int[] DEFAULT_PAGE_SIZES =
		new int[] { 5, 10, 20, 25, 50, 100 };

	//~ Instance fields --------------------------------------------------------

	private Updatable rNavigationListener;

	private int   nPageStart = 0;
	private int   nPageSize  = 0;
	private int   nTotalSize = 0;
	private int[] aPageSizes = null;

	private UiDropDown<String> aPageSizeSelector;
	private UiIconButton	   aFirstPageButton;
	private UiIconButton	   aPreviousButton;
	private UiLabel			   aNavPosition;
	private UiIconButton	   aNextButton;
	private UiIconButton	   aLastPageButton;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent             The parent container
	 * @param rNavigationListener A listener for navigation events
	 * @param nPageSize           The initial page size
	 */
	public UiPagingNavigation(UiContainer<?> rParent,
							  Updatable		 rNavigationListener,
							  int			 nPageSize)
	{
		super(rParent, new UiTableLayout(6));

		this.rNavigationListener = rNavigationListener;
		this.nPageSize			 = nPageSize;

		aFirstPageButton =
			builder().addIconButton(UiStandardIcon.FIRST_PAGE)
					 .onClick(v -> handleNavigation(UiStandardIcon.FIRST_PAGE));
		aPreviousButton  =
			builder().addIconButton(UiStandardIcon.PREVIOUS)
					 .onClick(v -> handleNavigation(UiStandardIcon.PREVIOUS));
		aNavPosition     = builder().addLabel("");
		aNextButton		 =
			builder().addIconButton(UiStandardIcon.NEXT)
					 .onClick(v -> handleNavigation(UiStandardIcon.NEXT));
		aLastPageButton  =
			builder().addIconButton(UiStandardIcon.LAST_PAGE)
					 .onClick(v -> handleNavigation(UiStandardIcon.LAST_PAGE));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the currently selected page size.
	 *
	 * @return The current page size
	 */
	public final int getPageSize()
	{
		return nPageSize;
	}

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
	 * Returns the index of the first element on the current page.
	 *
	 * @return The starting index of the current page
	 */
	public final int getPageStart()
	{
		return nPageStart;
	}

	/***************************************
	 * Sets the current page size and updates the display. This will not fire an
	 * event to the page size change handler.
	 *
	 * @param nPageSize The new page size
	 */
	public final void setPageSize(int nPageSize)
	{
		this.nPageSize = nPageSize;

		if (aPageSizeSelector != null)
		{
			aPageSizeSelector.setSelection(Integer.toString(nPageSize));
		}

		update();
	}

	/***************************************
	 * Sets the page sizes that can be selected by the user. If set to NULL no
	 * page size selection will be available.
	 *
	 * @param rPageSizes The selectable page sizes or NULL to hide the page size
	 *                   selection
	 */
	public final void setPageSizes(int... rPageSizes)
	{
		this.aPageSizes = rPageSizes;

		if (rPageSizes != null)
		{
			if (aPageSizeSelector == null)
			{
				aPageSizeSelector =
					builder().addDropDown(String.class)
							 .setVisible(false)
							 .onSelection(this::updatePageSize);
				aPageSizeSelector.placeBefore(aFirstPageButton);
				aPageSizeSelector.setSelection(Integer.toString(nPageSize));
			}

			List<String> aPageSizeValues = new ArrayList<>(aPageSizes.length);

			for (int nPageSize : aPageSizes)
			{
				aPageSizeValues.add(Integer.toString(nPageSize));
			}

			aPageSizeSelector.setListValues(aPageSizeValues);
		}
		else if (aPageSizeSelector != null)
		{
			removeComponent(aPageSizeSelector);
			aPageSizeSelector = null;
		}
	}

	/***************************************
	 * Sets the index of the first element to display and updates the navigation
	 * display. This will not fire an event to the page change handler.
	 *
	 * @param nPageStart The new starting index of the current page
	 */
	public final void setPageStart(int nPageStart)
	{
		this.nPageStart = nPageStart;
		update();
	}

	/***************************************
	 * Sets the total size of data available and updates the navigation display.
	 *
	 * @param nSize The new total data size
	 */
	public final void setTotalSize(int nSize)
	{
		this.nTotalSize = nSize;
		update();
	}

	/***************************************
	 * Updates the navigation display according to the current navigation
	 * position.
	 */
	@SuppressWarnings("boxing")
	public void update()
	{
		boolean bShowControls = nTotalSize > nPageSize;
		String  sPosition     = "";

		if (nTotalSize > 5)
		{
			int nLast = Math.min(nPageStart + nPageSize, nTotalSize);

			sPosition =
				String.format("$$%d - %d {$lblPositionOfCount} %d",
							  nPageStart + 1,
							  nLast,
							  nTotalSize);
		}
		else if (nTotalSize == 0)
		{
			sPosition = "$lblEmpty";
		}

		aPageSizeSelector.setVisible(bShowControls);
		aFirstPageButton.setVisible(bShowControls);
		aLastPageButton.setVisible(bShowControls);
		aPreviousButton.setVisible(bShowControls);
		aNextButton.setVisible(bShowControls);

		aNavPosition.setVisible(nTotalSize == 0 || nTotalSize > 5);
		aNavPosition.setText(sPosition);
	}

	/***************************************
	 * Performs navigation and notifies the navigation listener.
	 *
	 * @param eAction The navigation action icon
	 */
	protected void handleNavigation(UiIconSupplier eAction)
	{
		int nMax = Math.max(0, nTotalSize - nPageSize);

		switch ((UiStandardIcon) eAction)
		{
			case FIRST_PAGE:
				nPageStart = 0;
				break;

			case PREVIOUS:
				nPageStart = Math.max(0, nPageStart - nPageSize);
				break;

			case NEXT:
				nPageStart = Math.min(nMax, nPageStart + nPageSize);
				break;

			case LAST_PAGE:
				nPageStart = nMax;
				break;

			default:
				assert false;
		}

		rNavigationListener.update();
	}

	/***************************************
	 * Updates the current page size and notifies the update listener.
	 *
	 * @param sPageSize The new page size as a string containing an integer
	 *                  value
	 */
	private void updatePageSize(String sPageSize)
	{
		int nNewPageSize = Integer.parseInt(sPageSize);

		if (nNewPageSize != nPageSize)
		{
			nPageSize = nNewPageSize;
			rNavigationListener.update();
		}
	}
}
