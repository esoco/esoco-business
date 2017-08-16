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
package de.esoco.process.ui.graphics;

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.Color;
import de.esoco.lib.property.RelativeScale;

import de.esoco.process.ui.UiImageDefinition;

import static de.esoco.lib.property.ContentProperties.ICON;
import static de.esoco.lib.property.LayoutProperties.ICON_ALIGN;
import static de.esoco.lib.property.LayoutProperties.ICON_SIZE;
import static de.esoco.lib.property.StyleProperties.ICON_COLOR;


/********************************************************************
 * An icon that is identified by it's name.
 *
 * @author eso
 */
public class UiIconDefinition extends UiImageDefinition<UiIconDefinition>
	implements UiIconSupplier
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance. The argument must be an object of which the
	 * string representation can be used to identify the icon. This is typically
	 * an enum constant or a string.
	 *
	 * @param rIconIdentifier The icon identifier
	 */
	public UiIconDefinition(Object rIconIdentifier)
	{
		set(ICON, rIconIdentifier.toString());
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the icon's alignment relative to it's component. Which alignments
	 * are supported depends on the client-side UI toolkit.
	 *
	 * @return This instance
	 */
	public UiIconDefinition alignRight()
	{
		set(ICON_ALIGN, Alignment.END);

		return this;
	}

	/***************************************
	 * Sets the icon's foreground color.
	 *
	 * @param  rIconColor The foreground color
	 *
	 * @return This instance
	 */
	public UiIconDefinition color(Color rIconColor)
	{
		set(ICON_COLOR, rIconColor);

		return this;
	}

	/***************************************
	 * Implemented to return THIS.
	 *
	 * @see UiIconSupplier#getIcon()
	 */
	@Override
	public UiIconDefinition getIcon()
	{
		return this;
	}

	/***************************************
	 * Sets the icon's foreground color.
	 *
	 * @param  eScale rIconColor The foreground color
	 *
	 * @return This instance
	 */
	public UiIconDefinition size(RelativeScale eScale)
	{
		set(ICON_SIZE, eScale);

		return this;
	}
}
