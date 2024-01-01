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
package de.esoco.data.element;

import de.esoco.lib.property.PropertyName;

import java.util.Set;

/**
 * A data element implementation for boolean values.
 *
 * @author eso
 */
public class BooleanDataElement extends DataElement<Boolean> {

	private static final long serialVersionUID = 1L;

	private Boolean value;

	/**
	 * Creates a new instance with a certain initial value and read-only state.
	 *
	 * @param name  The name of this element
	 * @param value The initial value
	 * @param flags The optional flags for this data element
	 */
	public BooleanDataElement(String name, Boolean value, Set<Flag> flags) {
		super(name, null, flags);

		this.value = value;
	}

	/**
	 * Default constructor for serialization.
	 */
	BooleanDataElement() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BooleanDataElement copy(CopyMode mode,
		PropertyName<?>... copyProperties) {
		return (BooleanDataElement) super.copy(mode, copyProperties);
	}

	/**
	 * @see DataElement#getValue()
	 */
	@Override
	public final Boolean getValue() {
		return value;
	}

	/**
	 * Sets the string value.
	 *
	 * @param value The new string value
	 */
	@Override
	public void setStringValue(String value) {
		setValue(Boolean.valueOf(value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BooleanDataElement newInstance() {
		return new BooleanDataElement();
	}

	/**
	 * @see DataElement#updateValue(Object)
	 */
	@Override
	protected final void updateValue(Boolean newValue) {
		value = newValue;
	}
}
