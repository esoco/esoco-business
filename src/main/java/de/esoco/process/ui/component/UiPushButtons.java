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
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.StyleProperties;
import de.esoco.process.ui.UiButtonGroup;
import de.esoco.process.ui.UiContainer;

import static de.esoco.lib.property.StyleProperties.BUTTON_STYLE;

/**
 * A group of buttons that can be pressed to cause action events. The datatype
 * defines the type of the button labels. Typically string and enum values are
 * supported.
 *
 * @author eso
 */
public class UiPushButtons<T> extends UiButtonGroup<T, UiPushButtons<T>> {

	/**
	 * Creates a new instance. If the datatype is an enum all enum values will
	 * be pre-set as buttons.
	 *
	 * @param parent   The parent container
	 * @param datatype The datatype of the list values
	 */
	public UiPushButtons(UiContainer<?> parent, Class<T> datatype) {
		super(parent, datatype, ListStyle.IMMEDIATE);
	}

	/**
	 * Sets the button style.
	 *
	 * @param style The new style
	 * @return This instance
	 */
	public UiPushButtons<T> buttonStyle(ButtonStyle style) {
		return set(BUTTON_STYLE, style);
	}

	/**
	 * Marks this button group as having images for the button faces. The
	 * images
	 * must be defined in the application resource together with the button
	 * labels with an "im" prefix.
	 *
	 * @return This instance
	 */
	public UiPushButtons<T> withImages() {
		return set(StyleProperties.HAS_IMAGES);
	}
}
