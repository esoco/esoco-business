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
	 * @param parent The parent view
	 * @param title  The dialog title
	 * @param layout The layout of the dialog content
	 * @param modal  TRUE to block any input outside of the view
	 */
	public UiDialog(UiView<?> parent, String title, UiLayout layout,
		boolean modal) {
		super(parent, title, layout, modal);
	}
}
