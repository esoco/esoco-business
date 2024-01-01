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

import de.esoco.data.element.BigDecimalDataElement;
import de.esoco.data.element.BigDecimalDataElement.DisplayStyle;
import de.esoco.lib.property.HasValue;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiNumberInputField;

import java.math.BigDecimal;

/**
 * An input field for decimal numbers.
 *
 * @author eso
 */
public class UiDecimalField
	extends UiNumberInputField<BigDecimal, UiDecimalField>
	implements HasValue<BigDecimal> {

	/**
	 * Creates a new instance.
	 *
	 * @see UiNumberInputField#UiNumberInputField(UiContainer, Class, Number)
	 */
	public UiDecimalField(UiContainer<?> container, BigDecimal value) {
		super(container, BigDecimal.class, value);
	}

	/**
	 * Sets the display style for the big decimal value.
	 *
	 * @param displayStyle The display style
	 * @return This instance for fluent invocation
	 */
	public UiDecimalField displayAs(DisplayStyle displayStyle) {
		if (displayStyle == DisplayStyle.MULTI_FORMAT) {
			fragment().get(ProcessRelationTypes.INPUT_PARAMS).remove(type());
		}

		return set(BigDecimalDataElement.DISPLAY_STYLE, displayStyle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BigDecimal getValue() {
		return getValueImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(BigDecimal value) {
		setValueImpl(value);
	}
}
