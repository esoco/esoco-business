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
package de.esoco.process.ui.view;

import de.esoco.process.ui.UiDialogView;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.UiView;

/**
 * A view implementing dialogs.
 *
 * @author eso
 */
public class UiDialog extends UiDialogView<UiDialog> {

	/**
	 * Creates a new instance.
	 *
	 * @param rParent The parent view
	 * @param sTitle  The dialog title
	 * @param rLayout The layout of the dialog content
	 * @param bModal  TRUE to block any input outside of the view
	 */
	public UiDialog(UiView<?> rParent, String sTitle, UiLayout rLayout,
		boolean bModal) {
		super(rParent, sTitle, rLayout, bModal);
	}
}
