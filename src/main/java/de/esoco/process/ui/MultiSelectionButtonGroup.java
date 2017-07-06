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

import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.step.InteractionFragment;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.StyleProperties.DISABLED_ELEMENTS;
import static de.esoco.lib.property.StyleProperties.LIST_STYLE;


/********************************************************************
 * A base class for groups of buttons that allow to select multiple buttons.
 *
 * @author eso
 */
public abstract class MultiSelectionButtonGroup<T, B extends MultiSelectionButtonGroup<T, B>>
	extends ButtonControl<List<T>, B> implements HasSelection<List<T>>
{
	//~ Instance fields --------------------------------------------------------

	private Class<T> rDatatype;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent    The parent container
	 * @param rDatatype  The datatype of the button labels
	 * @param eListStyle The style for the rendering of the buttons
	 */
	public MultiSelectionButtonGroup(Container<?> rParent,
									 Class<T>	  rDatatype,
									 ListStyle    eListStyle)
	{
		super(rParent, null);

		this.rDatatype = rDatatype;

		initListParameterType(rDatatype);
		set(LIST_STYLE, eListStyle);

		if (rDatatype.isEnum())
		{
			addButtons(rDatatype.getEnumConstants());
			resid(rDatatype.getSimpleName());
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
		InteractionFragment   rFragment  = fragment();
		RelationType<List<T>> rParamType = type();

		Collection<T> rAllowedValues = rFragment.getAllowedElements(rParamType);

		if (rAllowedValues != null)
		{
			rAllowedValues.addAll(rButtonLabels);
			checkSetColumns(rAllowedValues);
		}
		else
		{
			rFragment.annotateParameter(rParamType,
										null,
										ProcessRelationTypes.ALLOWED_VALUES,
										rButtonLabels);
			checkSetColumns(rButtonLabels);
		}
	}

	/***************************************
	 * Disables certain values of the parameter enum.
	 *
	 * @param rDisabledButtons rDisabledElements A collection of the elements to
	 *                         disable
	 */
	public void disableButtons(Collection<T> rDisabledButtons)
	{
		InteractionFragment   rFragment  = fragment();
		RelationType<List<T>> rParamType = type();

		rFragment.disableElements(rParamType,
								  rDatatype,
								  rFragment.getAllowedElements(rParamType),
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
	public List<T> getSelection()
	{
		return value();
	}

	/***************************************
	 * Sets the buttons of this instance, overriding any previously set buttons.
	 * This can also be used to remove the current buttons by setting an empty
	 * collection.
	 *
	 * @param rNewLabels The new collection of button labels
	 */
	public void setButtons(Collection<T> rNewLabels)
	{
		fragment().annotateParameter(type(),
									 null,
									 ProcessRelationTypes.ALLOWED_VALUES,
									 rNewLabels);
		checkSetColumns(rNewLabels);
	}

	/***************************************
	 * Sets the selection of this button group. Depending on the datatype of
	 * this instance this either needs to be a single value or a collection of
	 * values.
	 *
	 * @param rNewSelection The new selection
	 */
	@Override
	public void setSelection(List<T> rNewSelection)
	{
		value(rNewSelection);
	}
}
