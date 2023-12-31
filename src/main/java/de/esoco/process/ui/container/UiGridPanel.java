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

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayoutContainer;
import de.esoco.process.ui.layout.UiGridLayout;

/**
 * A panel that arranges it's children with a {@link UiGridLayout} in a
 * two-dimensional grid.
 *
 * @author eso
 */
public class UiGridPanel extends UiLayoutContainer<UiGridPanel> {

	/**
	 * Creates a new instance.
	 *
	 * @param rParent  The parent container
	 * @param nRows    The number of layout rows
	 * @param nColumns The number of layout columns
	 */
	public UiGridPanel(UiContainer<?> rParent, int nRows, int nColumns) {
		super(rParent, new UiGridLayout(nRows, nColumns));
	}
}
