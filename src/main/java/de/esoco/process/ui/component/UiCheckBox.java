//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.lib.property.Selectable;
import de.esoco.lib.property.TextAttribute;
import de.esoco.process.ui.UiButtonControl;
import de.esoco.process.ui.UiContainer;

import static de.esoco.lib.property.ContentProperties.LABEL;
import static de.esoco.lib.property.StyleProperties.CHECK_BOX_STYLE;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;

/**
 * A check box button that has a selectable state.
 *
 * @author eso
 */
public class UiCheckBox extends UiButtonControl<Boolean, UiCheckBox>
	implements Selectable, TextAttribute {

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent container
	 * @param label  The check box label
	 */
	public UiCheckBox(UiContainer<?> parent, String label) {
		super(parent, Boolean.class);

		setText(label);
		set(HIDE_LABEL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return get(LABEL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSelected() {
		Boolean state = fragment().getParameter(type());

		return state != null && state.booleanValue();
	}

	/**
	 * Fluent variant of {@link #setSelected(boolean)}.
	 *
	 * @param selected The selection state
	 * @return This instance
	 */
	public UiCheckBox select(boolean selected) {
		setSelected(selected);

		return this;
	}

	/**
	 * Sets the style of this check box.
	 *
	 * @param style The new check box style
	 */
	public void setCheckBoxStyle(CheckBoxStyle style) {
		set(CHECK_BOX_STYLE, style);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSelected(boolean selected) {
		fragment().setParameter(type(), selected);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setText(String text) {
		set(LABEL, text);
	}

	/**
	 * Sets the style of this check box.
	 *
	 * @param style The check box style
	 * @return This instance
	 */
	public UiCheckBox style(CheckBoxStyle style) {
		return set(CHECK_BOX_STYLE, style);
	}
}
