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

import de.esoco.process.ui.Container;
import de.esoco.process.ui.TextInputField;


/********************************************************************
 * A multi-line text input field.
 *
 * @author eso
 */
public class TextArea extends TextInputField<TextArea>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @see TextInputField#TextInput(Container, String)
	 */
	public TextArea(Container<?> rContainer, String sText)
	{
		super(rContainer, sText);

		rows(-1);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the number of rows that should be displayed.
	 *
	 * @param nRows The visible rows
	 */
	public void setVisibleRows(int nRows)
	{
		rows(nRows);
	}
}
