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

import de.esoco.lib.property.MutableProperties;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.StringProperties;


/********************************************************************
 * A common base class for elements in the process UI framework.
 *
 * @author eso
 */
public class UiElement<E extends UiElement<E>>
{
	//~ Instance fields --------------------------------------------------------

	private MutableProperties aProperties = new StringProperties();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public UiElement()
	{
	}

	/***************************************
	 * A copy constructor for subclasses.
	 *
	 * @param rOther The other element to copy the properties from
	 */
	protected UiElement(UiElement<?> rOther)
	{
		aProperties.setProperties(rOther.aProperties, true);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Queries a certain property.
	 *
	 * @param  rPropertyName The property name
	 * @param  rDefault      The default value to return if the property doesn't
	 *                       exist
	 *
	 * @return The property value or the default value if it isn't set
	 */
	public final <T> T get(PropertyName<T> rPropertyName, T rDefault)
	{
		return aProperties.getProperty(rPropertyName, rDefault);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return String.format("%s%s", getClass().getSimpleName(), aProperties);
	}

	/***************************************
	 * Applies the properties of this layout element to the given component if
	 * they are not already set.
	 *
	 * @param rComponent The target component
	 */
	void applyPropertiesTo(UiComponent<?, ?> rComponent)
	{
		rComponent.setProperties(aProperties, false);
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
