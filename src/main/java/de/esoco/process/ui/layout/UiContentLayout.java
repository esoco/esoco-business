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
package de.esoco.process.ui.layout;

import de.esoco.lib.property.LayoutType;

import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.composite.UiCard;

/**
 * A layout for the content area of a page or of some UI containers (e.g.
 * {@link UiCard}). Many client-side container implementations will create
 * content structure areas (header, content, footer) automatically but for more
 * complex layouts it can be helpful to define these areas explicitly in
 * separate panels (which are then added to the container).
 *
 * @author eso
 */
public class UiContentLayout extends UiLayout {

	/**
	 * Creates a new instance.
	 */
	public UiContentLayout() {
		super(LayoutType.CONTENT);
	}
}
