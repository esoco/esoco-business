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

import de.esoco.entity.Entity;

import de.esoco.lib.property.Orientation;

import de.esoco.process.ui.component.UiCalendar;
import de.esoco.process.ui.component.UiCheckBox;
import de.esoco.process.ui.component.UiCheckBoxes;
import de.esoco.process.ui.component.UiComboBox;
import de.esoco.process.ui.component.UiDateField;
import de.esoco.process.ui.component.UiDecimalField;
import de.esoco.process.ui.component.UiDropDown;
import de.esoco.process.ui.component.UiIntegerField;
import de.esoco.process.ui.component.UiLabel;
import de.esoco.process.ui.component.UiList;
import de.esoco.process.ui.component.UiMultiSelectionList;
import de.esoco.process.ui.component.UiProgressBar;
import de.esoco.process.ui.component.UiPushButtons;
import de.esoco.process.ui.component.UiQueryTable;
import de.esoco.process.ui.component.UiRadioButtons;
import de.esoco.process.ui.component.UiTextArea;
import de.esoco.process.ui.component.UiTextField;
import de.esoco.process.ui.component.UiToggleButtons;
import de.esoco.process.ui.container.UiDeckPanel;
import de.esoco.process.ui.container.UiDockPanel;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.container.UiSplitPanel;
import de.esoco.process.ui.container.UiStackPanel;
import de.esoco.process.ui.container.UiTabPanel;

import java.math.BigDecimal;

import java.util.Date;


/********************************************************************
 * Provides factory methods that build UI components in a container.
 *
 * @author eso
 */
public class UiBuilder<C extends UiContainer<C>>
{
	//~ Instance fields --------------------------------------------------------

	private final C rContainer;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rContainer The container to build the UI in
	 */
	public UiBuilder(C rContainer)
	{
		this.rContainer = rContainer;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a date selector. By default it only provides a calendar date input
	 * but an additional time of day selector can be enabled with the method
	 * {@link UiCalendar#withTimeInput()}.
	 *
	 * @param  rDate The initial date value or NULL for the current date
	 *
	 * @return The new component
	 */
	public UiCalendar addCalendar(Date rDate)
	{
		return new UiCalendar(rContainer, rDate);
	}

	/***************************************
	 * Adds a group of check boxes with string labels.
	 *
	 * @param  sLabel The check box label
	 *
	 * @return The new component
	 */
	public UiCheckBox addCheckBox(String sLabel)
	{
		return new UiCheckBox(rContainer, sLabel);
	}

	/***************************************
	 * Adds a group of check boxes with string labels.
	 *
	 * @param  rLabels rButtonLabels The initial check box labels (may be empty)
	 *
	 * @return The new component
	 */
	public UiCheckBoxes<String> addCheckBoxes(String... rLabels)
	{
		UiCheckBoxes<String> aCheckBoxes =
			new UiCheckBoxes<>(rContainer, String.class);

		aCheckBoxes.addButtons(rLabels);

		return aCheckBoxes;
	}

	/***************************************
	 * Adds a group of check boxes with labels derived from an enum. All enum
	 * values of the given type will be pre-set as check boxes.
	 *
	 * @param  rEnumType The enum class for the check box labels
	 *
	 * @return The new component
	 */
	public <E extends Enum<E>> UiCheckBoxes<E> addCheckBoxes(Class<E> rEnumType)
	{
		return new UiCheckBoxes<>(rContainer, rEnumType);
	}

	/***************************************
	 * Adds a single-line text input field with a drop-down list of value
	 * suggestions.
	 *
	 * @param  sText The text to edit
	 *
	 * @return The new component
	 */
	public UiComboBox addComboBox(String sText)
	{
		return new UiComboBox(rContainer, sText);
	}

	/***************************************
	 * Adds a date input field with a pop-up date selector. By default it
	 * accepts only date input but additional time input can be enabled with
	 * {@link UiCalendar#withTimeInput()}.
	 *
	 * @param  rDate The initial value or NULL for the current date
	 *
	 * @return The new component
	 */
	public UiDateField addDateField(Date rDate)
	{
		return new UiDateField(rContainer, rDate);
	}

	/***************************************
	 * Adds an input field for decimal values.
	 *
	 * @param  rValue The initial value
	 *
	 * @return The new component
	 */
	public UiDecimalField addDecimalField(BigDecimal rValue)
	{
		return new UiDecimalField(rContainer, rValue);
	}

	/***************************************
	 * Adds a deck panel.
	 *
	 * @return The new panel
	 */
	public UiDeckPanel addDeckPanel()
	{
		return new UiDeckPanel(rContainer);
	}

	/***************************************
	 * Adds a dock panel.
	 *
	 * @param  eOrientation The panel orientation
	 *
	 * @return The new panel
	 */
	public UiDockPanel addDockPanel(Orientation eOrientation)
	{
		return new UiDockPanel(rContainer, eOrientation);
	}

	/***************************************
	 * Adds a single-line field with a list of selectable elements. If the
	 * datatype is an enum all enum values will be pre-set as the list values.
	 *
	 * @param  rDatatype The datatype of the list elements
	 *
	 * @return The new component
	 */
	public <T> UiDropDown<T> addDropDown(Class<T> rDatatype)
	{
		return new UiDropDown<>(rContainer, rDatatype);
	}

	/***************************************
	 * Adds an integer input field. To allow only the input of a certain value
	 * range with spinner controls {@link UiIntegerField#withBounds(int, int)}
	 * can be invoked on it.
	 *
	 * @param  nValue The initial value
	 *
	 * @return The new component
	 */
	public UiIntegerField addIntegerField(int nValue)
	{
		return new UiIntegerField(rContainer, nValue);
	}

	/***************************************
	 * Adds a non-interactive label.
	 *
	 * @param  sText The label text
	 *
	 * @return The new component
	 */
	public UiLabel addLabel(String sText)
	{
		return new UiLabel(rContainer, sText);
	}

	/***************************************
	 * Adds a list of selectable string.
	 *
	 * @return The new component
	 */
	public UiList<String> addList()
	{
		return new UiList<>(rContainer, String.class);
	}

	/***************************************
	 * Adds a list of selectable enum values. All enum values of the given type
	 * will be pre-set as the list values.
	 *
	 * @param  rEnumType rEnumClass The enum class of the list values
	 *
	 * @return The new component
	 */
	public <E extends Enum<E>> UiList<E> addList(Class<E> rEnumType)
	{
		return new UiList<>(rContainer, rEnumType);
	}

	/***************************************
	 * Adds a list of strings that allows to select multiple values.
	 *
	 * @return The new component
	 */
	public UiMultiSelectionList<String> addMultiSelectionList()
	{
		return new UiMultiSelectionList<>(rContainer, String.class);
	}

	/***************************************
	 * Adds a list of enums that allows to select multiple values. All enum
	 * values of the given type will be pre-set as the list values.
	 *
	 * @param  rEnumType rEnumClass The enum for the list values
	 *
	 * @return The new component
	 */
	public <E extends Enum<E>> UiMultiSelectionList<E> addMultiSelectionList(
		Class<E> rEnumType)
	{
		return new UiMultiSelectionList<>(rContainer, rEnumType);
	}

	/***************************************
	 * Adds a panel with a certain layout.
	 *
	 * @param  eLayout The panel layout
	 *
	 * @return The new panel
	 */
	public UiLayoutPanel addPanel(UiLayout eLayout)
	{
		return new UiLayoutPanel(rContainer, eLayout);
	}

	/***************************************
	 * Adds a progress bar. The integer value of the bar defines the current
	 * progress in relation to the progress bar bounds. These default to 0 and
	 * 100 and can be changed with {@link UiProgressBar#withBounds(int, int)}.
	 *
	 * @return The new component
	 */
	public UiProgressBar addProgressBar()
	{
		return new UiProgressBar(rContainer);
	}

	/***************************************
	 * Adds a group of push buttons with string labels.
	 *
	 * @param  rButtonLabels The initial button labels (may be empty)
	 *
	 * @return The new component
	 */
	public UiPushButtons<String> addPushButtons(String... rButtonLabels)
	{
		UiPushButtons<String> aPushButtons =
			new UiPushButtons<>(rContainer, String.class);

		aPushButtons.addButtons(rButtonLabels);

		return aPushButtons;
	}

	/***************************************
	 * Adds a group of push buttons with labels derived from an enum. All enum
	 * values of the given type will be pre-set as buttons.
	 *
	 * @param  rEnumType The enum class for the button labels
	 *
	 * @return The new component
	 */
	public <E extends Enum<E>> UiPushButtons<E> addPushButtons(
		Class<E> rEnumType)
	{
		return new UiPushButtons<>(rContainer, rEnumType);
	}

	/***************************************
	 * Adds a table that performs an entity query.
	 *
	 * @param  rEntityType The entity type to display
	 *
	 * @return The new component
	 */
	public <E extends Entity> UiQueryTable<E> addQueryTable(
		Class<E> rEntityType)
	{
		return new UiQueryTable<>(rContainer, rEntityType);
	}

	/***************************************
	 * Adds a group of radio buttons with string labels.
	 *
	 * @param  rButtonLabels The initial button labels (may be empty)
	 *
	 * @return The new component
	 */
	public UiRadioButtons<String> addRadioButtons(String... rButtonLabels)
	{
		UiRadioButtons<String> aRadioButtons =
			new UiRadioButtons<>(rContainer, String.class);

		aRadioButtons.addButtons(rButtonLabels);

		return aRadioButtons;
	}

	/***************************************
	 * Adds a group of radio buttons with labels derived from an enum. All enum
	 * values of the given type will be pre-set as buttons.
	 *
	 * @param  rEnumType The enum class for the button labels
	 *
	 * @return The new component
	 */
	public <E extends Enum<E>> UiRadioButtons<E> addRadioButtons(
		Class<E> rEnumType)
	{
		return new UiRadioButtons<>(rContainer, rEnumType);
	}

	/***************************************
	 * Adds a split panel.
	 *
	 * @param  eOrientation The panel orientation
	 *
	 * @return The new panel
	 */
	public UiSplitPanel addSplitPanel(Orientation eOrientation)
	{
		return new UiSplitPanel(rContainer, eOrientation);
	}

	/***************************************
	 * Adds a stack panel.
	 *
	 * @return The new panel
	 */
	public UiStackPanel addStackPanel()
	{
		return new UiStackPanel(rContainer);
	}

	/***************************************
	 * Adds a tab panel.
	 *
	 * @return The new panel
	 */
	public UiTabPanel addTabPanel()
	{
		return new UiTabPanel(rContainer);
	}

	/***************************************
	 * Adds a multi-line text input field.
	 *
	 * @param  sText The text to edit
	 *
	 * @return The new component
	 */
	public UiTextArea addTextArea(String sText)
	{
		return new UiTextArea(rContainer, sText);
	}

	/***************************************
	 * Adds a single-line text input field.
	 *
	 * @param  sText The text to edit
	 *
	 * @return The new component
	 */
	public UiTextField addTextField(String sText)
	{
		return new UiTextField(rContainer, sText);
	}

	/***************************************
	 * Adds a group of radio buttons with labels derived from an enum. All enum
	 * values of the given type will be pre-set as buttons.
	 *
	 * @param  rEnumType The enum class for the button labels
	 *
	 * @return The new component
	 */
	public <E extends Enum<E>> UiToggleButtons<E> addToggleButtons(
		Class<E> rEnumType)
	{
		return new UiToggleButtons<>(rContainer, rEnumType);
	}

	/***************************************
	 * Adds a group of radio buttons with string labels.
	 *
	 * @param  rButtonLabels The initial button labels (may be empty)
	 *
	 * @return The new component
	 */
	public UiToggleButtons<String> addToggleButtons(String... rButtonLabels)
	{
		UiToggleButtons<String> aToggleButtons =
			new UiToggleButtons<>(rContainer, String.class);

		aToggleButtons.addButtons(rButtonLabels);

		return aToggleButtons;
	}

	/***************************************
	 * Returns the container that is built by rContainer instance.
	 *
	 * @return The container
	 */
	public final C getContainer()
	{
		return rContainer;
	}
}
