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

import de.esoco.lib.property.HasSelection;
import de.esoco.lib.property.ListStyle;

import java.util.Arrays;
import java.util.Collection;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.LayoutProperties.COLUMNS;
import static de.esoco.lib.property.StyleProperties.DISABLED_ELEMENTS;
import static de.esoco.lib.property.StyleProperties.LIST_STYLE;


/********************************************************************
 * A base class for groups of buttons.
 *
 * @author eso
 */
public abstract class ButtonGroup<T, C extends ButtonGroup<T, C>>
	extends ButtonComponent<T, C> implements HasSelection<T>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent    The parent container
	 * @param rDatatype  The datatype of the component value
	 * @param eListStyle The style for the rendering of the buttons
	 */
	@SuppressWarnings("unchecked")
	public ButtonGroup(Container<?>		rParent,
					   Class<? super T> rDatatype,
					   ListStyle		eListStyle)
	{
		super(rParent, rDatatype);

		set(LIST_STYLE, eListStyle);

		if (rDatatype.isEnum())
		{
			addButtons((T[]) rDatatype.getEnumConstants());
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds certain buttons.
	 *
	 * @param rButtonLabels The labels of the buttons to add
	 */
	@SuppressWarnings("unchecked")
	public void addButtons(T... rButtonLabels)
	{
		addButtons(Arrays.asList(rButtonLabels));
	}

	/***************************************
	 * Adds a collection of buttons.
	 *
	 * @param rButtonLabels The collection of button labels to add
	 */
	public void addButtons(Collection<T> rButtonLabels)
	{
		Collection<T> rAllowedValues = allowedValues();

		if (rAllowedValues != null)
		{
			rAllowedValues.addAll(rButtonLabels);
			checkSetColumns(rAllowedValues);
		}
		else
		{
			allow(rButtonLabels);
			checkSetColumns(rButtonLabels);
		}
	}

	/***************************************
	 * Disables certain values of the parameter enum.
	 *
	 * @param rDisabledButtons rDisabledElements A collection of the elements to
	 *                         disable
	 */
	@SuppressWarnings("unchecked")
	public void disableButtons(Collection<T> rDisabledButtons)
	{
		RelationType<T> rParamType = type();

		fragment().disableElements(rParamType,
								   (Class<T>) rParamType.getTargetType(),
								   allowedValues(),
								   rDisabledButtons);
	}

	/***************************************
	 * Disables certain values of the parameter enum.
	 *
	 * @param rDisabledButtons rDisabledElements The elements to disable
	 */
	@SuppressWarnings("unchecked")
	public void disableButtons(T... rDisabledButtons)
	{
		disableButtons(Arrays.asList(rDisabledButtons));
	}

	/***************************************
	 * Enables all Buttons.
	 */
	public void enableAllButtons()
	{
		fragment().removeUIProperties(type(), DISABLED_ELEMENTS);
	}

	/***************************************
	 * Returns the current selection. Depending on the datatype of this instance
	 * it will either be a single value or a collection of values.
	 *
	 * @return The current selection
	 */
	@Override
	public T getSelection()
	{
		return value();
	}

	/***************************************
	 * Sets the buttons of this instance, overriding any previously set buttons.
	 * This can also be used to remove the current buttons by setting an empty
	 * collection.
	 *
	 * @param rNewButtons The new collection of button labels
	 */
	public void setButtons(Collection<T> rNewButtons)
	{
		allow(rNewButtons);
		checkSetColumns(rNewButtons);
	}

	/***************************************
	 * Sets the selection of this button group. Depending on the datatype of
	 * this instance this either needs to be a single value or a collection of
	 * values.
	 *
	 * @param rNewSelection The new selection
	 */
	@Override
	public void setSelection(T rNewSelection)
	{
		value(rNewSelection);
	}

	/***************************************
	 * Checks whether the columns should be set to the number of buttons.
	 *
	 * @param rNewButtons
	 */
	private void checkSetColumns(Collection<T> rNewButtons)
	{
		if (!has(COLUMNS))
		{
			columns(rNewButtons.size());
		}
	}
}
