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
package de.esoco.process.ui.layout;

import de.esoco.lib.property.Alignment;


/********************************************************************
 * The abstract base class for the elements of layouts.
 *
 * @author eso
 */
public abstract class LayoutElement<E extends LayoutElement<E>>
{
	//~ Instance fields --------------------------------------------------------

	private Alignment eHorizontalAlignment = Alignment.FILL;
	private Alignment eVerticalAlignment   = Alignment.FILL;

	//~ Methods ----------------------------------------------------------------

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
	 * Sets the horizontal alignment of layout cells.
	 *
	 * @param  eAlignment The horizontal alignment
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final E hAlign(Alignment eAlignment)
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
	public final E vAlign(Alignment eAlignment)
	{
		eVerticalAlignment = eAlignment;

		return (E) this;
	}
}
