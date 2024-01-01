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
package de.esoco.process.ui;

import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.TextAttribute;

import java.util.Arrays;

import static de.esoco.lib.property.ContentProperties.CONTENT_TYPE;
import static de.esoco.lib.property.ContentProperties.FORMAT_ARGUMENTS;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;

/**
 * Base class for read-only text components.
 *
 * @author eso
 */
public abstract class UiTextComponent<C extends UiTextComponent<C>>
	extends UiComponent<String, C> implements TextAttribute {

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent container
	 * @param text   The initial component text
	 */
	public UiTextComponent(UiContainer<?> parent, String text) {
		super(parent, String.class);

		setText(text);
		set(HIDE_LABEL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return getValueImpl();
	}

	/**
	 * Sets the text value of this component so that it will be rendered as
	 * HTML. The value can be queried with {@link #getText()}.
	 *
	 * @param html The HTML text
	 * @return This instance for fluent invocation
	 */
	public C html(String html) {
		set(CONTENT_TYPE, ContentType.HTML);

		return setValueImpl(html);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public C resid(String resourceId) {
		if (resourceId != null) {
			label("$lbl" + resourceId);
		}

		return super.resid(resourceId);
	}

	/**
	 * Sets an HTML text value, formatted with arguments.
	 *
	 * @see #setHtml(String)
	 * @see #setFormattedText(String, String...)
	 */
	public void setFormattedHtml(String template, String... formatArguments) {
		setHtml(template);
		set(FORMAT_ARGUMENTS, Arrays.asList(formatArguments));
	}

	/**
	 * Sets a text that will be formatted by inserting values into a template,
	 * similar to {@link String#format(String, Object...)} but only with
	 * strings
	 * allowed as format arguments.
	 *
	 * @param template        The format template
	 * @param formatArguments The format arguments
	 */
	public void setFormattedText(String template, String... formatArguments) {
		setText(template);
		set(FORMAT_ARGUMENTS, Arrays.asList(formatArguments));
	}

	/**
	 * Sets the text value of this component so that it will be rendered as
	 * HTML. The value can be queried with {@link #getText()}.
	 *
	 * @param html The HTML text
	 */
	public void setHtml(String html) {
		html(html);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setText(String text) {
		text(text);
	}

	/**
	 * Sets the text of this component.
	 *
	 * @param text The new text
	 * @return This instance for fluent invocation
	 */
	public C text(String text) {
		remove(CONTENT_TYPE);

		return setValueImpl(text);
	}
}
