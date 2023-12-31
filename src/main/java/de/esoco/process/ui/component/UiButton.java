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

import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.TextAttribute;

import de.esoco.process.ui.UiButtonControl;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiImageDefinition;
import de.esoco.process.ui.graphics.UiIconSupplier;

import java.util.Arrays;

import static de.esoco.lib.property.ContentProperties.FORMAT_ARGUMENTS;
import static de.esoco.lib.property.StyleProperties.BUTTON_STYLE;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;

/**
 * An interactive button control that produces click events.
 *
 * @author eso
 */
public class UiButton extends UiButtonControl<String, UiButton>
	implements TextAttribute {

	/**
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param sLabel  The link label
	 */
	public UiButton(UiContainer<?> rParent, String sLabel) {
		super(rParent, String.class);

		setText(sLabel);
		buttonStyle(ButtonStyle.DEFAULT);
		set(HIDE_LABEL);
	}

	/**
	 * Sets the button style.
	 *
	 * @param eStyle The new style
	 * @return This instance
	 */
	public UiButton buttonStyle(ButtonStyle eStyle) {
		return set(BUTTON_STYLE, eStyle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return getValueImpl();
	}

	/**
	 * Sets an icon for the button. If both image and text are set both may be
	 * displayed but that depends on the client-side UI toolkit. As icons are a
	 * subclass of {@link UiImageDefinition} setting an icon will replace any
	 * previously set image and vice versa.
	 *
	 * @param rIconSupplier The button icon
	 * @return This instance
	 */
	@Override
	public UiButton icon(UiIconSupplier rIconSupplier) {
		return super.icon(rIconSupplier);
	}

	/**
	 * Sets the button image. If both image and text are set both may be
	 * displayed but that depends on the client-side UI toolkit.
	 *
	 * @param rImage The button image
	 * @return This instance
	 */
	@Override
	public UiButton image(UiImageDefinition<?> rImage) {
		return super.image(rImage);
	}

	/**
	 * Sets a text that will be formatted by inserting values into a template,
	 * similar to {@link String#format(String, Object...)} but only with
	 * strings
	 * allowed as format arguments.
	 *
	 * @param sTemplate        The format template
	 * @param rFormatArguments The format arguments
	 */
	public void setFormattedText(String sTemplate,
		String... rFormatArguments) {
		setText(sTemplate);
		set(FORMAT_ARGUMENTS, Arrays.asList(rFormatArguments));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setText(String sText) {
		setValueImpl(sText);
	}
}
