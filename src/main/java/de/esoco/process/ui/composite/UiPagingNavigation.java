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

/**
 * A composites that contains the components needed for the navigation through
 * pages of data.
 *
 * @author eso
 */
public class UiPagingNavigation extends UiComposite<UiPagingNavigation> {

	/**
	 * A constant array of default page sizes that can be used for
	 * {@link #setPageSizes(int...)}.
	 */
	public static final int[] DEFAULT_PAGE_SIZES =
		new int[] { 5, 10, 20, 25, 50, 100 };

	private final Updatable navigationListener;

	private final String emptyPageLabel = "$lblPageEmpty";

	private final UiIconButton firstPageButton;

	private final UiIconButton previousButton;

	private final UiLabel navPosition;

	private final UiIconButton nextButton;

	private final UiIconButton lastPageButton;

	private int pageStart = 0;

	private int pageSize = 0;

	private int totalSize = 0;

	private int[] pageSizes = null;

	private UiDropDown<String> pageSizeSelector;

	/**
	 * Creates a new instance.
	 *
	 * @param parent             The parent container
	 * @param navigationListener A listener for navigation events
	 * @param pageSize           The initial page size
	 */
	public UiPagingNavigation(UiContainer<?> parent,
		Updatable navigationListener, int pageSize) {
		super(parent, new UiTableLayout(6));

		this.navigationListener = navigationListener;
		this.pageSize = pageSize;

		firstPageButton = builder()
			.addIconButton(UiStandardIcon.FIRST_PAGE)
			.onClick(v -> handleNavigation(UiStandardIcon.FIRST_PAGE));
		previousButton = builder()
			.addIconButton(UiStandardIcon.PREVIOUS)
			.onClick(v -> handleNavigation(UiStandardIcon.PREVIOUS));
		navPosition = builder().addLabel("");
		nextButton = builder()
			.addIconButton(UiStandardIcon.NEXT)
			.onClick(v -> handleNavigation(UiStandardIcon.NEXT));
		lastPageButton = builder()
			.addIconButton(UiStandardIcon.LAST_PAGE)
			.onClick(v -> handleNavigation(UiStandardIcon.LAST_PAGE));
	}

	/**
	 * Returns the currently selected page size.
	 *
	 * @return The current page size
	 */
	public final int getPageSize() {
		return pageSize;
	}

	/**
	 * Returns the page sizes that can be selected by the user.
	 *
	 * @return The array list sizes or NULL for none
	 */
	public final int[] getPageSizes() {
		return pageSizes;
	}

	/**
	 * Returns the index of the first element on the current page.
	 *
	 * @return The starting index of the current page
	 */
	public final int getPageStart() {
		return pageStart;
	}

	/**
	 * Sets the label to be displayed if the current page is empty.
	 *
	 * @param emptyPageLabel The new empty page label
	 */
	public final void setEmptyPageLabel(String emptyPageLabel) {
		emptyPageLabel = emptyPageLabel;
	}

	/**
	 * Sets the current page size and updates the display. This will not
	 * fire an
	 * event to the page size change handler.
	 *
	 * @param pageSize The new page size
	 */
	public final void setPageSize(int pageSize) {
		this.pageSize = pageSize;

		if (pageSizeSelector != null) {
			pageSizeSelector.setSelection(Integer.toString(pageSize));
		}

		update();
	}

	/**
	 * Sets the page sizes that can be selected by the user. If set to NULL no
	 * page size selection will be available.
	 *
	 * @param pageSizes The selectable page sizes or NULL to hide the page size
	 *                  selection
	 */
	public final void setPageSizes(int... pageSizes) {
		this.pageSizes = pageSizes;

		if (pageSizes != null) {
			if (pageSizeSelector == null) {
				pageSizeSelector = builder()
					.addDropDown(String.class)
					.setVisible(false)
					.onSelection(this::updatePageSize);
				pageSizeSelector.placeBefore(firstPageButton);
				pageSizeSelector.setSelection(Integer.toString(pageSize));
			}

			List<String> pageSizeValues = new ArrayList<>(pageSizes.length);

			for (int pageSize : pageSizes) {
				pageSizeValues.add(Integer.toString(pageSize));
			}

			pageSizeSelector.setListValues(pageSizeValues);
		} else if (pageSizeSelector != null) {
			remove(pageSizeSelector);
			pageSizeSelector = null;
		}
	}

	/**
	 * Sets the index of the first element to display and updates the
	 * navigation
	 * display. This will not fire an event to the page change handler.
	 *
	 * @param pageStart The new starting index of the current page
	 */
	public final void setPageStart(int pageStart) {
		this.pageStart = pageStart;
		update();
	}

	/**
	 * Sets the total size of data available and updates the navigation
	 * display.
	 *
	 * @param size The new total data size
	 */
	public final void setTotalSize(int size) {
		this.totalSize = size;
		update();
	}

	/**
	 * Updates the navigation display according to the current navigation
	 * position.
	 */
	@Override
	@SuppressWarnings("boxing")
	public void update() {
		boolean showControls = totalSize > pageSize;
		String position = "";

		if (totalSize > 5) {
			int last = Math.min(pageStart + pageSize, totalSize);

			position = String.format("$$%d - %d {$lblPositionOfCount} %d",
				pageStart + 1, last, totalSize);
		} else if (totalSize == 0) {
			position = emptyPageLabel;
		}

		pageSizeSelector.setVisible(showControls);
		firstPageButton.setVisible(showControls);
		lastPageButton.setVisible(showControls);
		previousButton.setVisible(showControls);
		nextButton.setVisible(showControls);

		navPosition.setVisible(!position.isEmpty());
		navPosition.setText(position);
	}

	/**
	 * Performs navigation and notifies the navigation listener.
	 *
	 * @param action The navigation action icon
	 */
	protected void handleNavigation(UiIconSupplier action) {
		int max = Math.max(0, totalSize - pageSize);

		switch ((UiStandardIcon) action) {
			case FIRST_PAGE:
				pageStart = 0;
				break;

			case PREVIOUS:
				pageStart = Math.max(0, pageStart - pageSize);
				break;

			case NEXT:
				pageStart = Math.min(max, pageStart + pageSize);
				break;

			case LAST_PAGE:
				pageStart = max;
				break;

			default:
				assert false;
		}

		navigationListener.update();
	}

	/**
	 * Updates the current page size and notifies the update listener.
	 *
	 * @param size The new page size as a string containing an integer value
	 */
	private void updatePageSize(String size) {
		int newPageSize = Integer.parseInt(size);

		if (newPageSize != pageSize) {
			pageSize = newPageSize;
			navigationListener.update();
		}
	}
}
