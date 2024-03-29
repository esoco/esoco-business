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
 * A panel that arranges components in a stack of elements that can be selected
 * to display them.
 *
 * @author eso
 */
public class UiStackPanel extends UiSwitchPanel<UiStackPanel> {

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent container
	 */
	public UiStackPanel(UiContainer<?> parent) {
		super(parent, new StackLayout());
	}

	/**
	 * The internal stack panel layout.
	 *
	 * @author eso
	 */
	static class StackLayout extends UiLayout {

		/**
		 * Creates a new instance.
		 */
		public StackLayout() {
			super(LayoutType.STACK);
		}
	}
}
