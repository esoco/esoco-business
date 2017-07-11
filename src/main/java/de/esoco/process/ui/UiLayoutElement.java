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
import de.esoco.lib.property.MutableProperties;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.StringProperties;

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

	private MutableProperties aProperties = new StringProperties();

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
		return set(HORIZONTAL_ALIGN, eAlignment);
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
		return set(VERTICAL_ALIGN, eAlignment);
	}

	/***************************************
	 * Returns the value of a certain property of this element.
	 *
	 * @param  rProperty The property name
	 *
	 * @return The property value
	 */
	public final <V> V get(PropertyName<V> rProperty)
	{
		return aProperties.getProperty(rProperty, null);
	}

	/***************************************
	 * Returns the horizontal alignment of layout cells.
	 *
	 * @return The horizontal alignment
	 */
	public final Alignment getHorizontalAlignment()
	{
		return get(HORIZONTAL_ALIGN);
	}

	/***************************************
	 * Returns the vertical alignment of layout cells.
	 *
	 * @return The vertical alignment
	 */
	public final Alignment getVerticalAlignment()
	{
		return get(VERTICAL_ALIGN);
	}

	/***************************************
	 * Checks if a certain property exists in this element.
	 *
	 * @param  rProperty The property name
	 *
	 * @return TRUE if the property exists
	 */
	public final boolean has(PropertyName<?> rProperty)
	{
		return aProperties.hasProperty(rProperty);
	}

	/***************************************
	 * Sets a certain boolean property of this element.
	 *
	 * @param  rFlag rProperty The property name
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final E set(PropertyName<Boolean> rFlag)
	{
		aProperties.setFlag(rFlag);

		return (E) this;
	}

	/***************************************
	 * Sets a certain property of this element.
	 *
	 * @param  rProperty The property name
	 * @param  rValue    The property value
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final <V> E set(PropertyName<V> rProperty, V rValue)
	{
		aProperties.setProperty(rProperty, rValue);

		return (E) this;
	}

	/***************************************
	 * Sets a certain property of this element.
	 *
	 * @param  rProperty The property name
	 * @param  nValue    The property value
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final E set(PropertyName<Integer> rProperty, int nValue)
	{
		aProperties.setProperty(rProperty, nValue);

		return (E) this;
	}

	/***************************************
	 * Applies the layout parameters to the given component.
	 *
	 * @param rComponent The target component
	 */
	void applyDefaults(UiComponent<?, ?> rComponent)
	{
		MutableProperties rComponentProperties = rComponent.getUiProperties();
	}
}
