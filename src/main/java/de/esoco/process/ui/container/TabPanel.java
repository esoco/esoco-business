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

import de.esoco.lib.property.Layout;

import de.esoco.process.ui.Component;
import de.esoco.process.ui.Container;
import de.esoco.process.ui.SwitchPanel;


/********************************************************************
 * A panel that arranges components in selectable tab pages.
 *
 * @author eso
 */
public class TabPanel extends SwitchPanel
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 */
	public TabPanel(Container<?> rParent)
	{
		super(rParent, Layout.TABS);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a new tab page.
	 *
	 * @param rComponent The component to be displayed on the tab page
	 */
	public void addTab(Component<?, ?> rComponent)
	{
		value().add(rComponent.type());
	}
}
