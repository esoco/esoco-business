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
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.step.InteractionFragment;
import org.obrel.core.RelationType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static de.esoco.lib.property.StyleProperties.DISABLED_ELEMENTS;
import static de.esoco.lib.property.StyleProperties.LIST_STYLE;

/**
 * A base class for groups of buttons that allow to select multiple buttons.
 *
 * @author eso
 */
public abstract class UiMultiSelectionButtonGroup<T,
	B extends UiMultiSelectionButtonGroup<T, B>>
	extends UiButtonControl<List<T>, B> implements HasSelection<List<T>> {

	private final Class<T> elementType;

	/**
	 * Creates a new instance.
	 *
	 * @param parent      The parent container
	 * @param elementType The datatype of the button labels
	 * @param listStyle   The style for the button rendering
	 */
	public UiMultiSelectionButtonGroup(UiContainer<?> parent,
		Class<T> elementType, ListStyle listStyle) {
		super(parent, parent.fragment().getTemporaryListType(elementType));

		this.elementType = elementType;

		set(LIST_STYLE, listStyle);

		if (elementType.isEnum()) {
			addButtons(elementType.getEnumConstants());
			resid(elementType.getSimpleName());
		}
	}

	/**
	 * Adds certain buttons.
	 *
	 * @param buttonLabels The labels of the buttons to add
	 */
	@SuppressWarnings("unchecked")
	public void addButtons(T... buttonLabels) {
		addButtons(Arrays.asList(buttonLabels));
	}

	/**
	 * Adds a collection of buttons.
	 *
	 * @param buttonLabels The collection of button labels to add
	 */
	public void addButtons(Collection<T> buttonLabels) {
		InteractionFragment fragment = fragment();
		RelationType<List<T>> paramType = type();

		Collection<T> allowedValues = fragment.getAllowedElements(paramType);

		if (allowedValues != null) {
			allowedValues.addAll(buttonLabels);
			checkSetColumns(allowedValues);
		} else {
			fragment.annotateParameter(paramType, null,
				ProcessRelationTypes.ALLOWED_VALUES, buttonLabels);
			checkSetColumns(buttonLabels);
		}
	}

	/**
	 * Disables certain values of the parameter enum.
	 *
	 * @param disabledButtons disabledElements A collection of the elements to
	 *                        disable
	 */
	public void disableButtons(Collection<T> disabledButtons) {
		InteractionFragment fragment = fragment();
		RelationType<List<T>> paramType = type();

		fragment.disableElements(paramType, elementType,
			fragment.getAllowedElements(paramType), disabledButtons);
	}

	/**
	 * Disables certain values of the parameter enum.
	 *
	 * @param disabledButtons disabledElements The elements to disable
	 */
	@SuppressWarnings("unchecked")
	public void disableButtons(T... disabledButtons) {
		disableButtons(Arrays.asList(disabledButtons));
	}

	/**
	 * Enables all Buttons.
	 */
	public void enableAllButtons() {
		fragment().removeUIProperties(type(), DISABLED_ELEMENTS);
	}

	/**
	 * Returns the current selection. Depending on the datatype of this
	 * instance
	 * it will either be a single value or a collection of values.
	 *
	 * @return The current selection
	 */
	@Override
	public List<T> getSelection() {
		return fragment().getParameter(type());
	}

	/**
	 * Sets the buttons of this instance, overriding any previously set
	 * buttons.
	 * This can also be used to remove the current buttons by setting an empty
	 * collection.
	 *
	 * @param newLabels The new collection of button labels
	 */
	public void setButtons(Collection<T> newLabels) {
		fragment().annotateParameter(type(), null,
			ProcessRelationTypes.ALLOWED_VALUES, newLabels);
		checkSetColumns(newLabels);
	}

	/**
	 * Sets the selection of this button group. Depending on the datatype of
	 * this instance this either needs to be a single value or a collection of
	 * values.
	 *
	 * @param newSelection The new selection
	 */
	@Override
	public void setSelection(List<T> newSelection) {
		fragment().setParameter(type(), newSelection);
	}
}
