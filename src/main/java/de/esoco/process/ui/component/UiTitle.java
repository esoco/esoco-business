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

import de.esoco.lib.property.LabelStyle;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiTextComponent;

import static de.esoco.lib.property.StyleProperties.LABEL_STYLE;

/**
 * A read-only text component that is rendered as a title.
 *
 * @author eso
 */
public class UiTitle extends UiTextComponent<UiTitle> {

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent container
	 * @param text   The initial title text
	 */
	public UiTitle(UiContainer<?> parent, String text) {
		super(parent, text);

		set(LABEL_STYLE, LabelStyle.TITLE);
	}
}
