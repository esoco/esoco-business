//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.UiLayoutContainer;

/**
 * A generic panel container that arranges it's child containers according to a
 * particular layout.
 *
 * @author eso
 */
public class UiLayoutPanel extends UiLayoutContainer<UiLayoutPanel> {

	/**
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param rLayout The layout of this panel
	 */
	public UiLayoutPanel(UiContainer<?> rParent, UiLayout rLayout) {
		super(rParent, rLayout);
	}

	/**
	 * A shortcut to invoke {@link UiLayout#nextRow()}. This call will only
	 * have
	 * an effect for layouts that support multiple rows of components.
	 */
	public void nextRow() {
		getLayout().nextRow();
	}
}
