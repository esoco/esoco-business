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
	public final E alignVertical(Alignment eAlignment)
	{
		return set(VERTICAL_ALIGN, eAlignment);
	}

	/***************************************
	 * Applies the properties of this layout element to the given component if
	 * they are not already set.
	 *
	 * @param rComponent The target component
	 */
	void applyPropertiesTo(UiComponent<?, ?> rComponent)
	{
		MutableProperties rComponentProperties = rComponent.getUiProperties();

		rComponentProperties.setProperties(aProperties, false);
	}

	/***************************************
	 * Sets a certain boolean property of this element.
	 *
	 * @param  rFlag rProperty The property name
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	final E set(PropertyName<Boolean> rFlag)
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
	final <V> E set(PropertyName<V> rProperty, V rValue)
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
	final E set(PropertyName<Integer> rProperty, int nValue)
	{
		aProperties.setProperty(rProperty, nValue);

		return (E) this;
	}
}
