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
package de.esoco.process.ui.component;

import de.esoco.lib.property.Selectable;

import de.esoco.process.ui.UiButtonControl;
import de.esoco.process.ui.UiContainer;


/********************************************************************
 * A check box button that has a selectable state.
 *
 * @author eso
 */
public class UiCheckBox extends UiButtonControl<Boolean, UiCheckBox>
	implements Selectable
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param sLabel  The check box label
	 */
	public UiCheckBox(UiContainer<?> rParent, String sLabel)
	{
		super(rParent, Boolean.class);

		label(sLabel);
		hideLabel();
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("boxing")
	public boolean isSelected()
	{
		return value();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("boxing")
	public void setSelected(boolean bSelected)
	{
		value(bSelected);
	}
}
