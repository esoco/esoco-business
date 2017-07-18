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

import de.esoco.process.step.InteractionFragment;
import de.esoco.process.ui.component.UiCheckBox;
import de.esoco.process.ui.component.UiCheckBoxes;
import de.esoco.process.ui.component.UiComboBox;
import de.esoco.process.ui.component.UiDropDown;
import de.esoco.process.ui.component.UiLabel;
import de.esoco.process.ui.component.UiList;
import de.esoco.process.ui.component.UiMultiSelectionList;
import de.esoco.process.ui.component.UiPushButtons;
import de.esoco.process.ui.component.UiQueryTable;
import de.esoco.process.ui.component.UiRadioButtons;
import de.esoco.process.ui.component.UiTextArea;
import de.esoco.process.ui.component.UiTextField;
import de.esoco.process.ui.component.UiToggleButtons;
import de.esoco.process.ui.container.UiDeckPanel;
import de.esoco.process.ui.container.UiDockPanel;
import de.esoco.process.ui.container.UiPanel;
import de.esoco.process.ui.container.UiSplitPanel;
import de.esoco.process.ui.container.UiStackPanel;
import de.esoco.process.ui.container.UiTabPanel;

import java.util.ArrayList;

import org.obrel.core.RelationType;


/********************************************************************
 * The base class for UI containers.
 *
 * @author eso
 */
public abstract class UiContainer<C extends UiContainer<C>>
	extends UiComponent<java.util.List<RelationType<?>>, C>
{
	//~ Instance fields --------------------------------------------------------

	private java.util.List<UiComponent<?, ?>> rComponents = new ArrayList<>();

	private UiLayout rLayout;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container or NULL for a root container
	 * @param rLayout The layout of the container
	 */
	public UiContainer(UiContainer<?> rParent, UiLayout rLayout)
	{
		super(rParent, null);

		this.rLayout = rLayout;

		if (rParent != null)
		{
			InteractionFragment rParentFragment = rParent.fragment();

			@SuppressWarnings("serial")
			InteractionFragment aContainerFragment =
				new InteractionFragment()
				{
					@Override
					public void init() throws Exception
					{
						build();
					}
				};

			RelationType<java.util.List<RelationType<?>>> rContainerParamType =
				rParentFragment.getTemporaryListType(null, RelationType.class);

			setFragment(aContainerFragment);
			setParameterType(rContainerParamType);
			rParentFragment.addInputParameters(rContainerParamType);

			rParentFragment.addSubFragment(rContainerParamType,
										   aContainerFragment);

			rParent.addComponent(this);
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a group of check boxes with string labels.
	 *
	 * @param  sLabel The check box label
	 *
	 * @return The new component
	 */
	public UiCheckBox addCheckBox(String sLabel)
	{
		return new UiCheckBox(this, sLabel);
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
			new UiCheckBoxes<>(this, String.class);

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
		return new UiCheckBoxes<>(this, rEnumType);
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
		return new UiComboBox(this, sText);
	}

	/***************************************
	 * Adds a deck panel.
	 *
	 * @return The new panel
	 */
	public UiDeckPanel addDeckPanel()
	{
		return new UiDeckPanel(this);
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
		return new UiDockPanel(this, eOrientation);
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
		return new UiDropDown<>(this, rDatatype);
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
		return new UiLabel(this, sText);
	}

	/***************************************
	 * Adds a list of selectable string.
	 *
	 * @return The new component
	 */
	public UiList<String> addList()
	{
		return new UiList<>(this, String.class);
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
		return new UiList<>(this, rEnumType);
	}

	/***************************************
	 * Adds a list of strings that allows to select multiple values.
	 *
	 * @return The new component
	 */
	public UiMultiSelectionList<String> addMultiSelectionList()
	{
		return new UiMultiSelectionList<>(this, String.class);
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
		return new UiMultiSelectionList<>(this, rEnumType);
	}

	/***************************************
	 * Adds a panel with a certain layout.
	 *
	 * @param  eLayout The panel layout
	 *
	 * @return The new panel
	 */
	public UiPanel addPanel(UiLayout eLayout)
	{
		return new UiPanel(this, eLayout);
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
			new UiPushButtons<>(this, String.class);

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
		return new UiPushButtons<>(this, rEnumType);
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
		return new UiQueryTable<>(this, rEntityType);
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
			new UiRadioButtons<>(this, String.class);

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
		return new UiRadioButtons<>(this, rEnumType);
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
		return new UiSplitPanel(this, eOrientation);
	}

	/***************************************
	 * Adds a stack panel.
	 *
	 * @return The new panel
	 */
	public UiStackPanel addStackPanel()
	{
		return new UiStackPanel(this);
	}

	/***************************************
	 * Adds a tab panel.
	 *
	 * @return The new panel
	 */
	public UiTabPanel addTabPanel()
	{
		return new UiTabPanel(this);
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
		return new UiTextArea(this, sText);
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
		return new UiTextField(this, sText);
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
		return new UiToggleButtons<>(this, rEnumType);
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
			new UiToggleButtons<>(this, String.class);

		aToggleButtons.addButtons(rButtonLabels);

		return aToggleButtons;
	}

	/***************************************
	 * Returns the components of this container in the order in which they have
	 * been added.
	 *
	 * @return The collection of components
	 */
	public java.util.List<UiComponent<?, ?>> getComponents()
	{
		return new ArrayList<>(rComponents);
	}

	/***************************************
	 * Returns the layout of this container.
	 *
	 * @return The layout
	 */
	public final UiLayout getLayout()
	{
		return rLayout;
	}

	/***************************************
	 * A shortcut to invoke {@link UiLayout#nextRow()}. This call will only work
	 * for layouts that support multiple rows of components.
	 */
	public void nextRow()
	{
		getLayout().nextRow();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return String.format("%s%s",
							 getClass().getSimpleName(),
							 getComponents());
	}

	/***************************************
	 * Overridden to apply the container layout and to invoke this method
	 * recursively on all child components.
	 *
	 * @see UiComponent#applyProperties()
	 */
	@Override
	protected void applyProperties()
	{
		// apply layout first so it can add styles to the container before
		// applying them
		rLayout.applyTo(this);

		super.applyProperties();

		for (UiComponent<?, ?> rChild : rComponents)
		{
			rChild.applyProperties();
		}
	}

	/***************************************
	 * Can be overridden by subclasses to build the contents of this container.
	 * Alternatively the contents can also be built by adding components to it
	 * after creation. This may also be used in combination. In that case this
	 * {@link #build()} method has already been invoked because that happens
	 * upon initialization.
	 *
	 * <p>The default implementation of this method does nothing.</p>
	 */
	protected void build()
	{
	}

	/***************************************
	 * Will be invoked if a new component has been added to this container. Can
	 * be overridden by subclasses to handle component additions. The complete
	 * list of child components (including the new one at the end) can be
	 * queried with {@link #getComponents()}.
	 *
	 * <p>The default implementation does nothing.</p>
	 *
	 * @param rComponent The component that has been added
	 */
	protected void componentAdded(UiComponent<?, ?> rComponent)
	{
	}

	/***************************************
	 * Internal method to add a component to this container.
	 *
	 * @param rComponent The component to add
	 */
	void addComponent(UiComponent<?, ?> rComponent)
	{
		rComponents.add(rComponent);
		rLayout.layoutComponent(rComponent);

		componentAdded(rComponent);
	}
}
