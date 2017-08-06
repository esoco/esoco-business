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

import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.SingleSelection;

import de.esoco.process.ValueEventHandler;
import de.esoco.process.ui.container.UiLayoutPanel;

import static de.esoco.lib.property.ContentProperties.LABEL;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;


/********************************************************************
 * A panel that contains multiple children of which only one is visible at a
 * time. The visible component can then be selected through the methods of the
 * implemented {@link SingleSelection} interface.
 *
 * @author eso
 */
public class UiSwitchPanel<P extends UiSwitchPanel<P>>
	extends UiLayoutContainer<P> implements SingleSelection
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param rLayout The panel layout
	 */
	public UiSwitchPanel(UiContainer<?> rParent, UiLayout rLayout)
	{
		super(rParent, rLayout);
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
	public UiLayoutPanel addPage(String sTitle, UiLayout eLayout)
	{
		return addPage(sTitle, builder().addPanel(eLayout));
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
	public <C extends UiComponent<?, C>> C addPage(
		String sTitle,
		C	   rPageComponent)
	{
		if (rPageComponent.getParent() != this)
		{
			throw new IllegalArgumentException("Component " + rPageComponent +
											   " has other parent: " +
											   rPageComponent.getParent());
		}

		rPageComponent.set(LABEL, sTitle);

		return rPageComponent;
	}

	/***************************************
	 * Returns the component of the currently selected (visible) page.
	 *
	 * @return The selected component
	 */
	public UiComponent<?, ?> getSelection()
	{
		return getComponents().get(getSelectionIndex());
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public int getSelectionIndex()
	{
		Integer rSelectionIndex = get(CURRENT_SELECTION);

		return rSelectionIndex != null ? rSelectionIndex.intValue() : 0;
	}

	/***************************************
	 * Sets the event handler for selection events of this panel. The event
	 * handler will receive this panel as it's argument so that it can query the
	 * current selection index or the selected page component.
	 *
	 * @param  rEventHandler The event handler
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P onSelection(ValueEventHandler<P> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.UPDATE,
										v -> rEventHandler.handleValueUpdate((P)
																			 this));
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
