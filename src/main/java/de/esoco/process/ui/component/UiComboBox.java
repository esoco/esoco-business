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

import de.esoco.lib.property.TextAttribute;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiTextInputField;

import java.util.Collection;

/**
 * A combination of a single-line text field with a drop-down list of selectable
 * value suggestions.
 *
 * @author eso
 */
public class UiComboBox extends UiTextInputField<UiComboBox>
	implements TextAttribute {

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent container
	 * @param text   The initial text
	 */
	public UiComboBox(UiContainer<?> parent, String text) {
		super(parent, text);
	}

	/**
	 * Sets the values to be displayed as suggestions for the input field.
	 *
	 * @param values The suggested values
	 */
	public void setSuggestions(String... values) {
		fragment().setAllowedValues(type(), values);
	}

	/**
	 * Sets a collection of values to be displayed as suggestions for the input
	 * field.
	 *
	 * @param values The suggested values
	 */
	public void setSuggestions(Collection<String> values) {
		fragment().setAllowedValues(type(), values);
	}
}
