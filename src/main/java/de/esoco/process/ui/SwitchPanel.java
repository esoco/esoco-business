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
import de.esoco.lib.property.SingleSelection;
import de.esoco.process.ui.container.Panel;

import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;


/********************************************************************
 * A panel that arranges components in selectable tab pages.
 *
 * @author eso
 */
public class SwitchPanel extends Panel implements SingleSelection
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
