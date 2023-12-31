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
package de.esoco.process.ui.container;

import de.esoco.lib.property.LayoutType;

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.UiSwitchPanel;

/**
 * Contains multiple components arranged as a deck of pages of which only one is
 * visible at a time. The visible component can be selected or queried through
 * the selection methods of {@link UiSwitchPanel}.
 *
 * @author eso
 */
public class UiDeckPanel extends UiSwitchPanel<UiDeckPanel> {

	/**
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 */
	public UiDeckPanel(UiContainer<?> rParent) {
		super(rParent, new DeckLayout());
	}

	/**
	 * Adds a new panel as a deck page. This is a variant of the base class
	 * method {@link #addPage(String, UiLayout)} because the page title is
	 * ignored in deck panels.
	 *
	 * @see #addPage(String, UiLayout)
	 */
	public UiLayoutPanel addPage(UiLayout eLayout) {
		return addPage("", eLayout);
	}

	/**
	 * The internal deck panel layout.
	 *
	 * @author eso
	 */
	static class DeckLayout extends UiLayout {

		/**
		 * Creates a new instance.
		 */
		public DeckLayout() {
			super(LayoutType.DECK, 1);
		}
	}
}
