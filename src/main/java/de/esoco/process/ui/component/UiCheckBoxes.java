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

import de.esoco.lib.property.CheckBoxStyle;
import de.esoco.lib.property.ListStyle;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiMultiSelectionButtonGroup;

import static de.esoco.lib.property.StyleProperties.CHECK_BOX_STYLE;

/**
 * A group of checkboxes that have a selection state that can be toggled
 * independent from each other (other than {@link UiRadioButtons} where the
 * selection is mutually exclusive). The datatype defines the type of the button
 * labels. Typically string and enum values are supported.
 *
 * @author eso
 */
public class UiCheckBoxes<T>
	extends UiMultiSelectionButtonGroup<T, UiCheckBoxes<T>> {

	/**
	 * Creates a new instance. If the datatype is an enum all enum values will
	 * be pre-set as buttons.
	 *
	 * @param parent   The parent container
	 * @param datatype The datatype of the check box labels
	 */
	public UiCheckBoxes(UiContainer<?> parent, Class<T> datatype) {
		super(parent, datatype, ListStyle.DISCRETE);
	}

	/**
	 * Sets the style of this check box.
	 *
	 * @param style The new check box style
	 */
	public void setCheckBoxStyle(CheckBoxStyle style) {
		set(CHECK_BOX_STYLE, style);
	}
}
