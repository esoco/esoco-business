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

import de.esoco.lib.property.Layout;

import de.esoco.process.step.InteractionFragment;
import de.esoco.process.ui.component.CheckBox;
import de.esoco.process.ui.component.CheckBoxes;
import de.esoco.process.ui.component.ComboBox;
import de.esoco.process.ui.component.DropDown;
import de.esoco.process.ui.component.Label;
import de.esoco.process.ui.component.List;
import de.esoco.process.ui.component.MultiSelectionList;
import de.esoco.process.ui.component.PushButtons;
import de.esoco.process.ui.component.RadioButtons;
import de.esoco.process.ui.component.TextArea;
import de.esoco.process.ui.component.TextField;
import de.esoco.process.ui.component.ToggleButtons;
import de.esoco.process.ui.container.DeckPanel;
import de.esoco.process.ui.container.Panel;
import de.esoco.process.ui.container.StackPanel;
import de.esoco.process.ui.container.TabPanel;

import java.util.ArrayList;

import org.obrel.core.RelationType;


/********************************************************************
 * The base class for UI containers.
 *
 * @author eso
 */
public abstract class Container<C extends Container<C>>
	extends Component<java.util.List<RelationType<?>>, C>
{
	//~ Instance fields --------------------------------------------------------

	private java.util.List<Component<?, ?>> rComponents = new ArrayList<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container or NULL for a root container
	 */
	public Container(Container<?> rParent)
	{
		super(rParent, null);

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

			rParentFragment.addSubFragment(rContainerParamType,
										   aContainerFragment);

			setFragment(aContainerFragment);
			setParameterType(rContainerParamType);
			rParentFragment.addInputParameters(rContainerParamType);
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
	public CheckBox addCheckBox(String sLabel)
	{
		return new CheckBox(this, sLabel);
	}

	/***************************************
	 * Adds a group of check boxes with string labels.
	 *
	 * @param  rLabels rButtonLabels The initial check box labels (may be empty)
	 *
	 * @return The new component
	 */
	public CheckBoxes<String> addCheckBoxes(String... rLabels)
	{
		CheckBoxes<String> aCheckBoxes = new CheckBoxes<>(this, String.class);

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
	public <E extends Enum<E>> CheckBoxes<E> addCheckBoxes(Class<E> rEnumType)
	{
		return new CheckBoxes<>(this, rEnumType);
	}

	/***************************************
	 * Adds a single-line text input field with a drop-down list of value
	 * suggestions.
	 *
	 * @param  sText The text to edit
	 *
	 * @return The new component
	 */
	public ComboBox addComboBox(String sText)
	{
		return new ComboBox(this, sText);
	}

	/***************************************
	 * Adds a deck panel.
	 *
	 * @return The new panel
	 */
	public DeckPanel addDeckPanel()
	{
		return new DeckPanel(this);
	}

	/***************************************
	 * Adds a single-line field with a list of selectable elements. If the
	 * datatype is an enum all enum values will be pre-set as the list values.
	 *
	 * @param  rDatatype The datatype of the list elements
	 *
	 * @return The new component
	 */
	public <T> DropDown<T> addDropDown(Class<T> rDatatype)
	{
		return new DropDown<>(this, rDatatype);
	}

	/***************************************
	 * Adds a non-interactive label.
	 *
	 * @param  sText The label text
	 *
	 * @return The new component
	 */
	public Label addLabel(String sText)
	{
		return new Label(this, sText);
	}

	/***************************************
	 * Adds a list of selectable string.
	 *
	 * @return The new component
	 */
	public List<String> addList()
	{
		return new List<>(this, String.class);
	}

	/***************************************
	 * Adds a list of selectable enum values. All enum values of the given type
	 * will be pre-set as the list values.
	 *
	 * @param  rEnumType rEnumClass The enum class of the list values
	 *
	 * @return The new component
	 */
	public <E extends Enum<E>> List<E> addList(Class<E> rEnumType)
	{
		return new List<>(this, rEnumType);
	}

	/***************************************
	 * Adds a list of strings that allows to select multiple values.
	 *
	 * @return The new component
	 */
	public MultiSelectionList<String> addMultiSelectionList()
	{
		return new MultiSelectionList<>(this, String.class);
	}

	/***************************************
	 * Adds a list of enums that allows to select multiple values. All enum
	 * values of the given type will be pre-set as the list values.
	 *
	 * @param  rEnumType rEnumClass The enum for the list values
	 *
	 * @return The new component
	 */
	public <E extends Enum<E>> MultiSelectionList<E> addMultiSelectionList(
		Class<E> rEnumType)
	{
		return new MultiSelectionList<>(this, rEnumType);
	}

	/***************************************
	 * Adds a panel with a certain layout.
	 *
	 * @param  eLayout The panel layout
	 *
	 * @return The new panel
	 */
	public Panel addPanel(Layout eLayout)
	{
		return new Panel(this, eLayout);
	}

	/***************************************
	 * Adds a group of push buttons with string labels.
	 *
	 * @param  rButtonLabels The initial button labels (may be empty)
	 *
	 * @return The new component
	 */
	public PushButtons<String> addPushButtons(String... rButtonLabels)
	{
		PushButtons<String> aPushButtons =
			new PushButtons<>(this, String.class);

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
	public <E extends Enum<E>> PushButtons<E> addPushButtons(Class<E> rEnumType)
	{
		return new PushButtons<>(this, rEnumType);
	}

	/***************************************
	 * Adds a group of radio buttons with string labels.
	 *
	 * @param  rButtonLabels The initial button labels (may be empty)
	 *
	 * @return The new component
	 */
	public RadioButtons<String> addRadioButtons(String... rButtonLabels)
	{
		RadioButtons<String> aRadioButtons =
			new RadioButtons<>(this, String.class);

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
	public <E extends Enum<E>> RadioButtons<E> addRadioButtons(
		Class<E> rEnumType)
	{
		return new RadioButtons<>(this, rEnumType);
	}

	/***************************************
	 * Adds a stack panel.
	 *
	 * @return The new panel
	 */
	public StackPanel addStackPanel()
	{
		return new StackPanel(this);
	}

	/***************************************
	 * Adds a tab panel.
	 *
	 * @return The new panel
	 */
	public TabPanel addTabPanel()
	{
		return new TabPanel(this);
	}

	/***************************************
	 * Adds a multi-line text input field.
	 *
	 * @param  sText The text to edit
	 *
	 * @return The new component
	 */
	public TextArea addTextArea(String sText)
	{
		return new TextArea(this, sText);
	}

	/***************************************
	 * Adds a single-line text input field.
	 *
	 * @param  sText The text to edit
	 *
	 * @return The new component
	 */
	public TextField addTextField(String sText)
	{
		return new TextField(this, sText);
	}

	/***************************************
	 * Adds a group of radio buttons with labels derived from an enum. All enum
	 * values of the given type will be pre-set as buttons.
	 *
	 * @param  rEnumType The enum class for the button labels
	 *
	 * @return The new component
	 */
	public <E extends Enum<E>> ToggleButtons<E> addToggleButtons(
		Class<E> rEnumType)
	{
		return new ToggleButtons<>(this, rEnumType);
	}

	/***************************************
	 * Adds a group of radio buttons with string labels.
	 *
	 * @param  rButtonLabels The initial button labels (may be empty)
	 *
	 * @return The new component
	 */
	public ToggleButtons<String> addToggleButtons(String... rButtonLabels)
	{
		ToggleButtons<String> aToggleButtons =
			new ToggleButtons<>(this, String.class);

		aToggleButtons.addButtons(rButtonLabels);

		return aToggleButtons;
	}

	/***************************************
	 * Returns the components of this container in the order in which they have
	 * been added.
	 *
	 * @return The collection of components
	 */
	public java.util.List<Component<?, ?>> getComponents()
	{
		return new ArrayList<>(rComponents);
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
	 * Internal method to add a component to this container.
	 *
	 * @param rComponent The component to add
	 */
	void addComponent(Component<?, ?> rComponent)
	{
		rComponents.add(rComponent);
	}
}
