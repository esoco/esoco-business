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

import de.esoco.lib.property.SingleSelection;

import de.esoco.process.ui.container.Panel;
import de.esoco.process.ui.event.HasUpdateEvents;

import java.util.List;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;


/********************************************************************
 * A panel that contains multiple children of which only one is visible at a
 * time. The visible component can then be selected through the methods of the
 * implemented {@link SingleSelection} interface.
 *
 * @author eso
 */
public class SwitchPanel<P extends SwitchPanel<P>> extends Container<P>
	implements SingleSelection, HasUpdateEvents<List<RelationType<?>>, P>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param eLayout The panel layout
	 */
	public SwitchPanel(Container<?> rParent, Layout eLayout)
	{
		super(rParent, eLayout);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a panel as a new page of this switch panel.
	 *
	 * @param  sTitle  The page title
	 * @param  eLayout The panel layout
	 *
	 * @return The new panel
	 */
	public Panel addPage(String sTitle, Layout eLayout)
	{
		return addPage(sTitle, addPanel(eLayout));
	}

	/***************************************
	 * Adds a certain component as a new page of this switch panel. The
	 * component must be a child of this container or else an exception will be
	 * thrown.
	 *
	 * @param  sTitle         The page title
	 * @param  rPageComponent The component to display on the page
	 *
	 * @return The page component to allow further invocations
	 *
	 * @throws IllegalArgumentException If the given component has a different
	 *                                  parent than this container
	 */
	public <C extends Component<?, C>> C addPage(
		String sTitle,
		C	   rPageComponent)
	{
		if (rPageComponent.getParent() != this)
		{
			throw new IllegalArgumentException("Component " + rPageComponent +
											   " has other parent: " +
											   rPageComponent.getParent());
		}

		rPageComponent.label(sTitle);

		return rPageComponent;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("boxing")
	public int getSelectionIndex()
	{
		return get(CURRENT_SELECTION);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("boxing")
	public void setSelection(int nIndex)
	{
		set(CURRENT_SELECTION, nIndex);
	}
}
