//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.property.HasSelection;
import de.esoco.lib.property.ListStyle;
import org.obrel.core.RelationType;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.esoco.lib.property.StyleProperties.DISABLED_ELEMENTS;
import static de.esoco.lib.property.StyleProperties.LIST_STYLE;

/**
 * A base class for groups of buttons.
 *
 * @author eso
 */
public abstract class UiButtonGroup<T, C extends UiButtonGroup<T, C>>
	extends UiButtonControl<T, C> implements HasSelection<T> {

	/**
	 * Creates a new instance.
	 *
	 * @param parent    The parent container
	 * @param datatype  The datatype of the component value
	 * @param listStyle The style for the rendering of the buttons
	 */
	@SuppressWarnings("unchecked")
	public UiButtonGroup(UiContainer<?> parent, Class<? super T> datatype,
		ListStyle listStyle) {
		super(parent, datatype);

		set(LIST_STYLE, listStyle);

		if (datatype.isEnum()) {
			addButtons((T[]) datatype.getEnumConstants());
		}
	}

	/**
	 * Adds certain buttons.
	 *
	 * @param buttonLabels The labels of the buttons to add
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public C addButtons(T... buttonLabels) {
		return addButtons(Arrays.asList(buttonLabels));
	}

	/**
	 * Adds a collection of buttons.
	 *
	 * @param buttonLabels The collection of button labels to add
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public C addButtons(Collection<T> buttonLabels) {
		Collection<T> allowedValues = fragment().getAllowedValues(type());

		if (allowedValues != null) {
			allowedValues.addAll(buttonLabels);
			checkSetColumns(allowedValues);
		} else {
			fragment().setAllowedValues(type(), buttonLabels);
			checkSetColumns(buttonLabels);
		}

		return (C) this;
	}

	/**
	 * Disables certain values of the parameter enum.
	 *
	 * @param disabledButtons disabledElements A collection of the elements to
	 *                        disable
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public C disableButtons(Collection<T> disabledButtons) {
		RelationType<T> paramType = type();

		fragment().disableElements(paramType,
			(Class<T>) paramType.getTargetType(),
			fragment().getAllowedValues(type()), disabledButtons);

		return (C) this;
	}

	/**
	 * Disables certain values of the parameter enum.
	 *
	 * @param disabledButtons disabledElements The elements to disable
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public C disableButtons(T... disabledButtons) {
		return disableButtons(Arrays.asList(disabledButtons));
	}

	/**
	 * Enables all Buttons.
	 *
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public C enableAllButtons() {
		fragment().removeUIProperties(type(), DISABLED_ELEMENTS);

		return (C) this;
	}

	/**
	 * Returns the current selection. Depending on the datatype of this
	 * instance
	 * it will either be a single value or a collection of values.
	 *
	 * @return The current selection
	 */
	@Override
	public T getSelection() {
		return getValueImpl();
	}

	/**
	 * Selects a certain button.
	 *
	 * @param value The enum value of the button to select
	 * @return This instance
	 */
	public C select(T value) {
		return setValueImpl(value);
	}

	/**
	 * Sets the buttons to be displayed.
	 *
	 * @param buttonLabels The labels of the displayed buttons
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public C setButtons(T... buttonLabels) {
		return setButtons(Arrays.asList(buttonLabels));
	}

	/**
	 * Sets the buttons to be displayed.
	 *
	 * @param buttonLabels A stream containing the labels of the displayed
	 *                     buttons
	 * @return This instance
	 */
	public C setButtons(Stream<T> buttonLabels) {
		return setButtons(buttonLabels.collect(Collectors.toList()));
	}

	/**
	 * Sets the buttons of this instance, overriding any previously set
	 * buttons.
	 * This can also be used to remove the current buttons by setting an empty
	 * collection.
	 *
	 * @param newButtons The new collection of button labels
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public C setButtons(Collection<T> newButtons) {
		fragment().setAllowedValues(type(), newButtons);
		checkSetColumns(newButtons);

		return (C) this;
	}

	/**
	 * Sets the selection of this button group. Depending on the datatype of
	 * this instance this either needs to be a single value or a collection of
	 * values.
	 *
	 * @param newSelection The new selection
	 */
	@Override
	public void setSelection(T newSelection) {
		select(newSelection);
	}
}
