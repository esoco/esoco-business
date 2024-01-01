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
package de.esoco.process.ui;

import de.esoco.lib.property.MutableProperties;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.StringProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * A common base class for elements in the process UI framework.
 *
 * @author eso
 */
public class UiElement<E extends UiElement<E>> {

	// initially true to make sure it is applied on the first time
	private boolean modified = true;

	private MutableProperties properties = new StringProperties();

	private final Set<UiComponent<?, ?>> componentsAppliedTo = new HashSet<>();

	/**
	 * Creates a new instance.
	 */
	public UiElement() {
	}

	/**
	 * A copy constructor for subclasses.
	 *
	 * @param other The other element to copy the properties from
	 */
	protected UiElement(UiElement<?> other) {
		properties.setProperties(other.properties, true);
	}

	/**
	 * Applies this element to the given component.
	 *
	 * @param component The target component
	 */
	public void applyTo(UiComponent<?, ?> component) {
		if (modified || !componentsAppliedTo.contains(component)) {
			applyPropertiesTo(component);
			componentsAppliedTo.add(component);
			modified = false;
		}
	}

	/**
	 * Queries an integer property.
	 *
	 * @param integerProperty The property name
	 * @param defaultValue    The default value to return if the property
	 *                        doesn't exist
	 * @return The property value or the default value if it isn't set
	 */
	public final int get(PropertyName<Integer> integerProperty,
		int defaultValue) {
		return properties.getIntProperty(integerProperty, defaultValue);
	}

	/**
	 * Queries a certain property.
	 *
	 * @param propertyName The property name
	 * @param defaultValue The default value to return if the property doesn't
	 *                     exist
	 * @return The property value or the default value if it isn't set
	 */
	public final <T> T get(PropertyName<T> propertyName, T defaultValue) {
		return properties.getProperty(propertyName, defaultValue);
	}

	/**
	 * Checks whether a certain property has been set.
	 *
	 * @param propertyName The property name
	 * @return TRUE if the property exists
	 */
	public boolean hasProperty(PropertyName<?> propertyName) {
		return properties.hasProperty(propertyName);
	}

	/**
	 * Checks the modification state of this element.
	 *
	 * @return TRUE if the element has been modified since it has last been
	 * applied to the component
	 */
	public final boolean isModified() {
		return modified;
	}

	/**
	 * Set this element's modified state. If TRUE it will be applied to the
	 * component on the next call to {@link #applyTo(UiComponent)}.
	 *
	 * @param modified The new modified state
	 */
	public final void setModified(boolean modified) {
		this.modified = modified;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * Applies the properties of this element to the given component. Will only
	 * be invoked if the properties have changed.
	 *
	 * @param component The target component
	 */
	protected void applyPropertiesTo(UiComponent<?, ?> component) {
		component.setProperties(properties, true);
	}

	/**
	 * Clears all properties in this element.
	 */
	protected void clearProperties() {
		properties.clearProperties();
		setModified(true);
	}

	/**
	 * Copies the properties from another UI element to this instance.
	 *
	 * @param other   The other element
	 * @param replace TRUE to replace existing properties in this instance
	 */
	protected void copyPropertiesFrom(UiElement<?> other, boolean replace) {
		properties.setProperties(other.properties, replace);
		setModified(true);
	}

	/**
	 * Sets a boolean property.
	 *
	 * @see #set(PropertyName, Object)
	 */
	protected final E set(PropertyName<Boolean> flag) {
		return set(flag, Boolean.TRUE);
	}

	/**
	 * Sets a certain property of this element. All other property set methods
	 * redirect to this method so that subclasses only need to override this
	 * method if they want to intercept property updates.
	 *
	 * @param property The property name
	 * @param value    The property value
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	protected <V> E set(PropertyName<V> property, V value) {
		properties.setProperty(property, value);
		setModified(true);

		return (E) this;
	}

	/**
	 * Sets an integer property.
	 *
	 * @see #set(PropertyName, Object)
	 */
	protected final E set(PropertyName<Integer> property, int value) {
		return set(property, Integer.valueOf(value));
	}

	/**
	 * Internal method that returns the properties of this instance.
	 *
	 * @return The properties value
	 */
	final MutableProperties getProperties() {
		return properties;
	}

	/**
	 * Internal method to replace the properties of this instance.
	 *
	 * @param newProperties The new properties
	 */
	final void setProperties(MutableProperties newProperties) {
		properties = newProperties;
		setModified(true);
	}
}
