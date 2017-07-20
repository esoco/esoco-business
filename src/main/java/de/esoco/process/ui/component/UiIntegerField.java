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

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiNumberInputField;


/********************************************************************
 * An input field for integer numbers with the option to display spinner
 * buttons.
 *
 * @author eso
 */
public class UiIntegerField extends UiNumberInputField<Integer, UiIntegerField>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @see UiNumberInputField#UiNumberInputField(UiContainer, Class, Number)
	 */
	public UiIntegerField(UiContainer<?> rContainer, int nValue)
	{
		super(rContainer, Integer.class, Integer.valueOf(nValue));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the minimum and maximum values for the integer input. If these
	 * limits are set the component will display additional button to increment
	 * or decrement the displayed value.
	 *
	 * @param  nMinimum The minimum integer value
	 * @param  nMaximum The maximum integer value
	 *
	 * @return This instance
	 */
	public UiIntegerField withBounds(int nMinimum, int nMaximum)
	{
		fragment().setParameterBounds(type(), nMinimum, nMaximum);

		return this;
	}
}
