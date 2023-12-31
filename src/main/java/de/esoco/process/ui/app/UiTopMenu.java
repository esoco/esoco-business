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
package de.esoco.process.ui.app;

import de.esoco.lib.property.LabelStyle;

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.style.DefaultStyleNames;

import java.util.Collection;

/**
 * The top menu of a web page.
 *
 * @author eso
 */
public class UiTopMenu<T> extends UiNavMenu<T> {

	/**
	 * Creates a new instance without preset menu items.
	 *
	 * @param rParent The parent container
	 */
	public UiTopMenu(UiContainer<?> rParent) {
		this(rParent, null);
	}

	/**
	 * Creates a new instance with preset menu items.
	 *
	 * @param rParent    The parent container
	 * @param rMenuItems The initial menu items
	 */
	public UiTopMenu(UiContainer<?> rParent, Collection<T> rMenuItems) {
		super(rParent, rMenuItems);
	}

	/**
	 * Adds a label that is displayed prominently to identify the website. It
	 * has the style name {@link DefaultStyleNames#TOP_MENU_BRAND_LABEL}.
	 *
	 * @param sText The text to display as the site label
	 */
	public void addBrandLabel(String sText) {
		builder()
			.addLabel(sText)
			.labelStyle(LabelStyle.BRAND)
			.styleName(DefaultStyleNames.TOP_MENU_BRAND_LABEL);
	}
}
