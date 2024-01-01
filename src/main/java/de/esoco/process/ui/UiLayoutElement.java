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

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.PropertyName;

import static de.esoco.lib.property.LayoutProperties.HORIZONTAL_ALIGN;
import static de.esoco.lib.property.LayoutProperties.VERTICAL_ALIGN;

/**
 * The abstract base class for the elements of layouts.
 *
 * @author eso
 */
public abstract class UiLayoutElement<E extends UiLayoutElement<E>>
	extends UiElement<E> {

	/**
	 * Sets the horizontal alignment of this element.
	 *
	 * @param alignment The horizontal alignment
	 * @return This instance for concatenation
	 */
	public E alignHorizontal(Alignment alignment) {
		return set(HORIZONTAL_ALIGN, alignment);
	}

	/**
	 * Sets the vertical alignment value of this element.
	 *
	 * @param alignment The vertical alignment
	 * @return This instance for concatenation
	 */
	public E alignVertical(Alignment alignment) {
		return set(VERTICAL_ALIGN, alignment);
	}

	/**
	 * Overridden to skip properties that are already set.
	 *
	 * @see UiElement#applyPropertiesTo(UiComponent)
	 * @see UiLayout#ignoreProperties(PropertyName...)
	 */
	@Override
	public void applyPropertiesTo(UiComponent<?, ?> component) {
		for (PropertyName<?> property : getProperties().getPropertyNames()) {
			applyProperty(component, property);
		}
	}

	/**
	 * Applies a single property to a component if it doesn't exist already.
	 * This is in a separate method to allow type-safe invocation.
	 *
	 * @param component The component to apply the property to
	 * @param property  The name of the property to apply
	 */
	protected <T> void applyProperty(UiComponent<?, ?> component,
		PropertyName<T> property) {
		if (!component.has(property)) {
			component.set(property, get(property, null));
		}
	}
}
