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
 * A panel that arranges components in a stack of elements that can be selected
 * to display them.
 *
 * @author eso
 */
public class StackPanel extends SwitchPanel<StackPanel>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 */
	public StackPanel(Container<?> rParent)
	{
		super(rParent, Layout.STACK);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a new stack element.
	 *
	 * @param rComponent The component to be displayed on the stack element
	 */
	public void addStack(Component<?, ?> rComponent)
	{
		value().add(rComponent.type());
	}
}
