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

import de.esoco.lib.property.LayoutType;

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.UiSwitchPanel;


/********************************************************************
 * A panel that arranges components in selectable tab pages.
 *
 * @author eso
 */
public class UiTabPanel extends UiSwitchPanel<UiTabPanel>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 */
	public UiTabPanel(UiContainer<?> rParent)
	{
		super(rParent, new TabLayout());
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * The internal tab panel layout.
	 *
	 * @author eso
	 */
	static class TabLayout extends UiLayout
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 */
		public TabLayout()
		{
			super(LayoutType.TABS);
		}
	}
}
