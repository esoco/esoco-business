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
package de.esoco.process.ui;

import de.esoco.lib.property.ListStyle;

import java.util.ArrayList;
import java.util.Collection;

import static de.esoco.lib.property.ContentProperties.NULL_VALUE;

/**
 * The base class for lists with a single selectable value. The generic datatype
 * defines the type of the list values. Typically string and enum values are
 * supported.
 *
 * @author eso
 */
public abstract class UiSingleSelectionList<T,
	C extends UiSingleSelectionList<T, C>>
	extends UiListControl<T, C> {

	/**
	 * Creates a new instance. If the datatype is an enum all enum values will
	 * be pre-set as the list values. This can be changed after construction
	 * through the {@link #setListValues(java.util.Collection) setListValues}
	 * methods.
	 *
	 * @param parent   The parent container
	 * @param datatype The datatype of the list values
	 * @param style    The list style
	 */
	public UiSingleSelectionList(UiContainer<?> parent, Class<T> datatype,
		ListStyle style) {
		super(parent, datatype, style);

		if (datatype.isEnum()) {
			setListValues(datatype.getEnumConstants());
		} else {
			// set empty list to force rendering as a list
			setListValues(new ArrayList<>());
		}
	}

	/**
	 * Returns the collection of values that is displayed in this list.
	 *
	 * @return The list values
	 */
	public Collection<T> getListValues() {
		return fragment().getAllowedValues(type());
	}

	/**
	 * Sets the values to be displayed in this list.
	 *
	 * @param values The list values
	 */
	@SuppressWarnings("unchecked")
	public void setListValues(T... values) {
		fragment().setAllowedValues(type(), values);
	}

	/**
	 * Sets a collection of values to be displayed in this list.
	 *
	 * @param values The list values
	 */
	public void setListValues(Collection<T> values) {
		fragment().setAllowedValues(type(), values);
	}

	/**
	 * Sets a value that should be displayed as a placeholder for the choice of
	 * no list selection. This represents a NULL value of the component.
	 *
	 * @param value The string value to display as the "no selection" choice or
	 *              NULL to not provide this choice
	 */
	public void setNoSelectionValue(String value) {
		set(NULL_VALUE, value);
	}
}
