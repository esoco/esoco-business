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
package de.esoco.process.ui.component;

import de.esoco.lib.property.ButtonStyle;
import de.esoco.process.ui.UiButtonControl;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.graphics.UiIconSupplier;

import static de.esoco.lib.property.StyleProperties.BUTTON_STYLE;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;

/**
 * An interactive icon button that produces click events.
 *
 * @author eso
 */
public class UiIconButton extends UiButtonControl<String, UiIconButton> {

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent container
	 * @param icon   The icon
	 */
	public UiIconButton(UiContainer<?> parent, UiIconSupplier icon) {
		super(parent, String.class);

		icon(icon);
		set(HIDE_LABEL);
		set(BUTTON_STYLE, ButtonStyle.ICON);

		// set value to empty string (instead of NULL default) to prevent an
		// unnecessary change detection when an empty string is returned from
		// the UI (because text components use no NULL values)
		setValueImpl("");
	}

	/**
	 * Sets the icon of this button.
	 *
	 * @param iconSupplier The button icon
	 * @return This instance for fluent invocation
	 */
	@Override
	public UiIconButton icon(UiIconSupplier iconSupplier) {
		return super.icon(iconSupplier);
	}
}
