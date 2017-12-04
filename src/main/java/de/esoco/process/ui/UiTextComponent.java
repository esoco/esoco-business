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
package de.esoco.process.ui;

import de.esoco.lib.property.TextAttribute;

import java.util.Arrays;

import static de.esoco.lib.property.ContentProperties.FORMAT_ARGUMENTS;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;


/********************************************************************
 * Base class for read-only text components.
 *
 * @author eso
 */
public abstract class UiTextComponent<C extends UiTextComponent<C>>
	extends UiComponent<String, C> implements TextAttribute
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param sText   The initial component text
	 */
	public UiTextComponent(UiContainer<?> rParent, String sText)
	{
		super(rParent, String.class);

		setText(sText);
		set(HIDE_LABEL);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String getText()
	{
		return getValueImpl();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public C resid(String sResourceId)
	{
		if (sResourceId != null)
		{
			label("$lbl" + sResourceId);
		}

		return super.resid(sResourceId);
	}

	/***************************************
	 * Sets a text that will be formatted by inserting values into a template,
	 * similar to {@link String#format(String, Object...)}. Depending on the
	 * underlying client UI implementation the formatting options may be
	 * limited. Therefore the format arguments must already be in string format
	 * and advanced features like argument reordering may not be available.
	 *
	 * @param sTemplate        The format template
	 * @param rFormatArguments The format arguments
	 */
	public void setFormattedText(String sTemplate, String... rFormatArguments)
	{
		setValueImpl(sTemplate);
		set(FORMAT_ARGUMENTS, Arrays.asList(rFormatArguments));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void setText(String sText)
	{
		setValueImpl(sText);
	}
}
