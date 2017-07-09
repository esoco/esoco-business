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
package de.esoco.process.ui.container;

import de.esoco.process.ui.Container;
import de.esoco.process.ui.SwitchPanel;
import de.esoco.process.ui.layout.Layout;


/********************************************************************
 * Contains multiple components arranged as a deck of pages of which only one is
 * visible at a time. The visible component can be selected or queried through
 * the selection methods of {@link SwitchPanel}.
 *
 * @author eso
 */
public class DeckPanel extends SwitchPanel<DeckPanel>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 */
	public DeckPanel(Container<?> rParent)
	{
		super(rParent, new Layout(de.esoco.lib.property.Layout.TABS));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a new panel as a deck page. This is a variant of the base class
	 * method {@link #addPage(String, Layout)} because the page title is ignored
	 * in deck panels.
	 *
	 * @see #addPage(String, Layout)
	 */
	public Panel addPage(Layout eLayout)
	{
		return addPage("", eLayout);
	}
}
