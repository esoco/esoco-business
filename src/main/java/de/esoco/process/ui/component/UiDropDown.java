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

import de.esoco.lib.property.ListStyle;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiSingleSelectionList;

/**
 * A single-line field with a drop-down list of selectable values. The datatype
 * of of the list values can be defined on creation. Typically only string and
 * enum values are supported.
 *
 * @author eso
 */
public class UiDropDown<T> extends UiSingleSelectionList<T, UiDropDown<T>> {

	/**
	 * Creates a new instance. If the datatype is an enum all enum values will
	 * be pre-set as the list values.
	 *
	 * @param parent   The parent container
	 * @param datatype The datatype of the list values
	 */
	public UiDropDown(UiContainer<?> parent, Class<T> datatype) {
		super(parent, datatype, ListStyle.DROP_DOWN);
	}
}
