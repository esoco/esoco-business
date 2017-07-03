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

import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.TextAttribute;

import de.esoco.process.ui.Container;
import de.esoco.process.ui.ListComponent;


/********************************************************************
 * A combination of a single-line text field with a drop-down list of selectable
 * values. The datatype of of the list values can be defined on creation.
 * Typically only string and enum values are supported.
 *
 * @author eso
 */
public class ComboBox extends ListComponent<String, ComboBox>
	implements TextAttribute
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance. If the datatype is an enum all enum values will
	 * be pre-set as the list values.
	 *
	 * @param rParent The parent container
	 * @param sText   The initial text
	 */
	public ComboBox(Container<?> rParent, String sText)
	{
		super(rParent, String.class, ListStyle.EDITABLE);

		setText(sText);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String getText()
	{
		return value();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void setText(String sText)
	{
		value(sText);
	}
}
