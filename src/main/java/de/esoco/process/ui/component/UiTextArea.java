//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.process.ui.UiTextInputField;

import static de.esoco.lib.property.LayoutProperties.ROWS;


/********************************************************************
 * A multi-line text input field.
 *
 * @author eso
 */
public class UiTextArea extends UiTextInputField<UiTextArea>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @see UiTextInputField#UiTextInputField(UiContainer, String)
	 */
	public UiTextArea(UiContainer<?> rContainer, String sText)
	{
		super(rContainer, sText);

		set(-1, ROWS);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the number of rows that should be displayed.
	 *
	 * @param  nRows The visible rows
	 *
	 * @return This instance for concatenation
	 */
	public UiTextArea visibleRows(int nRows)
	{
		return set(nRows, ROWS);
	}
}
