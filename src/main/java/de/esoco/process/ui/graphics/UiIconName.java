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
package de.esoco.process.ui.graphics;

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.Color;
import de.esoco.lib.property.RelativeScale;

import de.esoco.process.ui.UiImageDefinition;

import static de.esoco.lib.property.ContentProperties.ICON;
import static de.esoco.lib.property.LayoutProperties.ICON_ALIGN;
import static de.esoco.lib.property.LayoutProperties.ICON_SIZE;
import static de.esoco.lib.property.StyleProperties.ICON_COLOR;

/**
 * An icon image that is identified by the icon's name.
 *
 * @author eso
 */
public class UiIconName extends UiImageDefinition<UiIconName>
	implements UiIconSupplier {

	/**
	 * Creates a new instance. The argument must be an object with a string
	 * representation that can be used to identify the icon. This is typically
	 * an enum constant or a string.
	 *
	 * @param rIconIdentifier The icon identifier
	 */
	public UiIconName(Object rIconIdentifier) {
		set(ICON, rIconIdentifier.toString());
	}

	/**
	 * Sets the icon's alignment relative to it's component. Which alignments
	 * are supported depends on the client-side UI toolkit.
	 *
	 * @return This instance
	 */
	public UiIconName alignRight() {
		set(ICON_ALIGN, Alignment.END);

		return this;
	}

	/**
	 * Sets the icon's foreground color.
	 *
	 * @param rIconColor The foreground color
	 * @return This instance
	 */
	public UiIconName color(Color rIconColor) {
		set(ICON_COLOR, rIconColor);

		return this;
	}

	/**
	 * Implemented to return THIS.
	 *
	 * @see UiIconSupplier#getIcon()
	 */
	@Override
	public UiIconName getIcon() {
		return this;
	}

	/**
	 * Sets the icon's foreground color.
	 *
	 * @param eScale rIconColor The foreground color
	 * @return This instance
	 */
	public UiIconName size(RelativeScale eScale) {
		set(ICON_SIZE, eScale);

		return this;
	}
}
