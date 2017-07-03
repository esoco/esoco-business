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

import de.esoco.lib.property.LabelStyle;
import de.esoco.lib.property.TextAttribute;

import de.esoco.process.ui.Component;
import de.esoco.process.ui.Container;

import static de.esoco.lib.property.StyleProperties.LABEL_STYLE;


/********************************************************************
 * A UI label.
 *
 * @author eso
 */
public class Label extends Component<String, Label> implements TextAttribute
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent rContainer rFragment The fragment
	 * @param sText   The label text
	 */
	public Label(Container<?> rParent, String sText)
	{
		super(rParent,
			  rParent.fragment().getTemporaryParameterType(String.class));

		value(sText);
		hideLabel();
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
	 * Sets the style of this label.
	 *
	 * @param eStyle The label style
	 */
	public void labelStyle(LabelStyle eStyle)
	{
		set(LABEL_STYLE, eStyle);
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
