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

import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.TextAttribute;

import de.esoco.process.ui.UiButtonControl;
import de.esoco.process.ui.UiContainer;

import static de.esoco.lib.property.StyleProperties.BUTTON_STYLE;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;

/**
 * An interactive button control that represents a clickable link.
 *
 * @author eso
 */
public class UiLink extends UiButtonControl<String, UiLink>
	implements TextAttribute {

	/**
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param sLabel  The link label
	 */
	public UiLink(UiContainer<?> rParent, String sLabel) {
		super(rParent, String.class);

		setText(sLabel);
		set(HIDE_LABEL);
		set(BUTTON_STYLE, ButtonStyle.LINK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return getValueImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setText(String sText) {
		setValueImpl(sText);
	}
}
