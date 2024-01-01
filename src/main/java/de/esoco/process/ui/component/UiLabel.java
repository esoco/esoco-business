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

import de.esoco.lib.property.LabelStyle;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiTextComponent;

import static de.esoco.lib.property.StyleProperties.LABEL_STYLE;

/**
 * A read-only UI text label.
 *
 * @author eso
 */
public class UiLabel extends UiTextComponent<UiLabel> {

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent container
	 * @param text   The label text
	 */
	public UiLabel(UiContainer<?> parent, String text) {
		super(parent, text);
	}

	/**
	 * Fluent variant of {@link #setCaption(String)}.
	 *
	 * @param caption The caption label
	 * @return This instance
	 */
	public UiLabel caption(String caption) {
		return label(caption);
	}

	/**
	 * Fluent variant of {@link #setLabelStyle(LabelStyle)}.
	 *
	 * @param style The label style
	 * @return This instance
	 */
	public UiLabel labelStyle(LabelStyle style) {
		set(LABEL_STYLE, style);

		return this;
	}

	/**
	 * Sets a caption label to be displayed over the label text (if
	 * supported by
	 * the container layout).
	 *
	 * @param caption The caption label
	 */
	public void setCaption(String caption) {
		caption(caption);
	}

	/**
	 * Sets the style of this label.
	 *
	 * @param style The label style
	 */
	public void setLabelStyle(LabelStyle style) {
		labelStyle(style);
	}
}
