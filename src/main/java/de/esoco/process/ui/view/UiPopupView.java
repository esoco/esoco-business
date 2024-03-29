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
package de.esoco.process.ui.view;

import de.esoco.lib.property.ViewDisplayType;
import de.esoco.process.ui.UiChildView;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.UiView;

/**
 * A view that is displayed as a pop-up windows over a parent view.
 *
 * @author eso
 */
public class UiPopupView extends UiChildView<UiPopupView> {

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent view
	 * @param layout The dialog layout
	 * @param modal  TRUE to block any input outside of the view
	 */
	public UiPopupView(UiView<?> parent, UiLayout layout, boolean modal) {
		super(parent, layout,
			modal ? ViewDisplayType.MODAL_VIEW : ViewDisplayType.VIEW);
	}
}
