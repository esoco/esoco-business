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
import de.esoco.process.ui.component.Label;
import de.esoco.process.ui.component.List;
import de.esoco.process.ui.component.TextArea;
import de.esoco.process.ui.component.TextField;
import de.esoco.process.ui.container.Panel;

import org.obrel.core.RelationType;


/********************************************************************
 * The base class for UI containers.
 *
 * @author eso
 */
public abstract class Container<C extends Container<C>>
	extends Component<java.util.List<RelationType<?>>, C>
{
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
	 * Adds a list of selectable elements. If the datatype is an enum all enum
	 * values will be pre-set as the list values.
	 *
	 * @param  rDatatype The datatype of the list elements
	 *
	 * @return The new component
	 */
	public <T> List<T> addList(Class<T> rDatatype)
	{
		return new List<>(this, rDatatype);
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
	 * Adds a multi-line text input field.
	 *
	 * @return The new component
	 */
	public TextArea addTextArea()
	{
		return new TextArea(this);
	}

	/***************************************
	 * Adds a single-line text input field.
	 *
	 * @return The new component
	 */
	public TextField addTextField()
	{
		return new TextField(this);
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
}
