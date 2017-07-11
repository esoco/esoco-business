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
package de.esoco.process.ui;

import de.esoco.lib.property.Alignment;

import static de.esoco.lib.property.LayoutProperties.HORIZONTAL_ALIGN;
import static de.esoco.lib.property.LayoutProperties.VERTICAL_ALIGN;


/********************************************************************
 * The abstract base class for the elements of layouts.
 *
 * @author eso
 */
public abstract class UiLayoutElement<E extends UiLayoutElement<E>>
{
	//~ Instance fields --------------------------------------------------------

	private Alignment eHorizontalAlignment = null;
	private Alignment eVerticalAlignment   = null;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the horizontal alignment of layout cells.
	 *
	 * @param  eAlignment The horizontal alignment
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final E alignHorizontal(Alignment eAlignment)
	{
		eHorizontalAlignment = eAlignment;

		return (E) this;
	}

	/***************************************
	 * Sets the vertical alignment value of layout cells.
	 *
	 * @param  eAlignment The vertical alignment
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final E alignVertical(Alignment eAlignment)
	{
		eVerticalAlignment = eAlignment;

		return (E) this;
	}

	/***************************************
	 * Returns the horizontal alignment of layout cells.
	 *
	 * @return The horizontal alignment
	 */
	public final Alignment getHorizontalAlignment()
	{
		return eHorizontalAlignment;
	}

	/***************************************
	 * Returns the vertical alignment of layout cells.
	 *
	 * @return The vertical alignment
	 */
	public final Alignment getVerticalAlignment()
	{
		return eVerticalAlignment;
	}

	/***************************************
	 * Applies the layout parameters to the given component.
	 *
	 * @param rComponent The target component
	 */
	void applyDefaults(UiComponent<?, ?> rComponent)
	{
		if (eHorizontalAlignment != null)
		{
			rComponent.set(HORIZONTAL_ALIGN, eHorizontalAlignment);
		}

		if (eVerticalAlignment != null)
		{
			rComponent.set(VERTICAL_ALIGN, eVerticalAlignment);
		}
	}
}
