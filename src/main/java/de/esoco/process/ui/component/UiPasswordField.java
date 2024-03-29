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

import de.esoco.lib.property.StyleProperties;
import de.esoco.lib.property.TextFieldStyle;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiTextInputField;

/**
 * A single-line text input field that hides the input value.
 *
 * @author eso
 */
public class UiPasswordField extends UiTextInputField<UiPasswordField> {

	/**
	 * Creates a new instance.
	 *
	 * @see UiTextInputField#UiTextInputField(UiContainer, String)
	 */
	public UiPasswordField(UiContainer<?> container, String text) {
		super(container, text);

		set(StyleProperties.TEXT_FIELD_STYLE, TextFieldStyle.PASSWORD);
	}
}
