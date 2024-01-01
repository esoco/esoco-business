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

import de.esoco.lib.property.TextAttribute;

import java.util.Arrays;

import static de.esoco.lib.property.ContentProperties.FORMAT_ARGUMENTS;
import static de.esoco.lib.property.StyleProperties.EDITABLE;

/**
 * A text input field.
 *
 * @author eso
 */
public abstract class UiTextInputField<T extends UiTextInputField<T>>
	extends UiInputField<String, T> implements TextAttribute {

	/**
	 * Creates a new instance.
	 *
	 * @param parent container The parent container
	 * @param text   The initial text
	 */
	public UiTextInputField(UiContainer<?> parent, String text) {
		super(parent, String.class, text);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return getValueImpl();
	}

	/**
	 * Sets the editable state of this text field. If set to FALSE no input
	 * will
	 * be possible but the field will not be rendered as disabled.
	 *
	 * @param editable The new editable
	 */
	@SuppressWarnings("boxing")
	public void setEditable(boolean editable) {
		set(EDITABLE, editable);
	}

	/**
	 * Sets a text that will be formatted by inserting values into a template,
	 * similar to {@link String#format(String, Object...)}. Depending on the
	 * underlying client UI implementation the formatting options may be
	 * limited. Therefore the format arguments must already be in string format
	 * and advanced features like argument reordering may not be available.
	 *
	 * @param template        The format template
	 * @param formatArguments The format arguments
	 */
	public void setFormattedText(String template, String... formatArguments) {
		setText(template);
		set(FORMAT_ARGUMENTS, Arrays.asList(formatArguments));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setText(String text) {
		setValueImpl(text);
	}

	/**
	 * Fluent method to set the text of this field.
	 *
	 * @param text The new text
	 * @return This instance
	 */
	public T text(String text) {
		return setValueImpl(text);
	}
}
