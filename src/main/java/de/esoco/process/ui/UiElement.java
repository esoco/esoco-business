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

import java.util.HashSet;
import java.util.Set;


/********************************************************************
 * A common base class for elements in the process UI framework.
 *
 * @author eso
 */
public class UiElement<E extends UiElement<E>>
{
	//~ Instance fields --------------------------------------------------------

	// initially true to make sure it is applied on the first time
	private boolean bModified = true;

	private MutableProperties aProperties = new StringProperties();

	private Set<UiComponent<?, ?>> aComponentsAppliedTo = new HashSet<>();

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
	 * Applies this element to the given component.
	 *
	 * @param rComponent The target component
	 */
	public void applyTo(UiComponent<?, ?> rComponent)
	{
		if (bModified || !aComponentsAppliedTo.contains(rComponent))
		{
			applyPropertiesTo(rComponent);
			aComponentsAppliedTo.add(rComponent);
			bModified = false;
		}
	}

	/***************************************
	 * Queries an integer property.
	 *
	 * @param  rIntegerProperty The property name
	 * @param  nDefault         The default value to return if the property
	 *                          doesn't exist
	 *
	 * @return The property value or the default value if it isn't set
	 */
	public final int get(PropertyName<Integer> rIntegerProperty, int nDefault)
	{
		return aProperties.getIntProperty(rIntegerProperty, nDefault);
	}

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
	 * Checks whether a certain property has been set.
	 *
	 * @param  rPropertyName The property name
	 *
	 * @return TRUE if the property exists
	 */
	public boolean hasProperty(PropertyName<?> rPropertyName)
	{
		return aProperties.hasProperty(rPropertyName);
	}

	/***************************************
	 * Checks the modification state of this element.
	 *
	 * @return TRUE if the element has been modified since it has last been
	 *         applied to the component
	 */
	public final boolean isModified()
	{
		return bModified;
	}

	/***************************************
	 * Set this element's modified state. If TRUE it will be applied to the
	 * component on the next call to {@link #applyTo(UiComponent)}.
	 *
	 * @param bModified The new modified state
	 */
	public final void setModified(boolean bModified)
	{
		this.bModified = bModified;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	/***************************************
	 * Applies the properties of this element to the given component. Will only
	 * be invoked if the properties have changed.
	 *
	 * @param rComponent The target component
	 */
	protected void applyPropertiesTo(UiComponent<?, ?> rComponent)
	{
		rComponent.setProperties(aProperties, true);
	}

	/***************************************
	 * Clears all properties in this element.
	 */
	protected void clearProperties()
	{
		aProperties.clearProperties();
		setModified(true);
	}

	/***************************************
	 * Copies the properties from another UI element to this instance.
	 *
	 * @param rOther   The other element
	 * @param bReplace TRUE to replace existing properties in this instance
	 */
	protected void copyPropertiesFrom(UiElement<?> rOther, boolean bReplace)
	{
		aProperties.setProperties(rOther.aProperties, bReplace);
		setModified(true);
	}

	/***************************************
	 * Sets a boolean property.
	 *
	 * @see #set(PropertyName, Object)
	 */
	protected final E set(PropertyName<Boolean> rFlag)
	{
		return set(rFlag, Boolean.TRUE);
	}

	/***************************************
	 * Sets a certain property of this element. All other property set methods
	 * redirect to this method so that subclasses only need to override this
	 * method if they want to intercept property updates.
	 *
	 * @param  rProperty The property name
	 * @param  rValue    The property value
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	protected <V> E set(PropertyName<V> rProperty, V rValue)
	{
		aProperties.setProperty(rProperty, rValue);
		setModified(true);

		return (E) this;
	}

	/***************************************
	 * Sets an integer property.
	 *
	 * @see #set(PropertyName, Object)
	 */
	protected final E set(PropertyName<Integer> rProperty, int nValue)
	{
		return set(rProperty, Integer.valueOf(nValue));
	}

	/***************************************
	 * Internal method that returns the properties of this instance.
	 *
	 * @return The properties value
	 */
	final MutableProperties getProperties()
	{
		return aProperties;
	}

	/***************************************
	 * Internal method to replace the properties of this instance.
	 *
	 * @param rNewProperties The new properties
	 */
	final void setProperties(MutableProperties rNewProperties)
	{
		aProperties = rNewProperties;
		bModified   = true;
	}
}
