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

import de.esoco.data.validate.Validator;
import de.esoco.lib.property.PropertyName;

import java.util.Set;

/**
 * A data element implementation for integer values.
 *
 * @author eso
 */
public class IntegerDataElement extends DataElement<Integer> {

	private static final long serialVersionUID = 1L;

	private Integer value;

	/**
	 * Creates a new read-only instance with a certain name and value.
	 *
	 * @param name  The name of this element
	 * @param value The initial value
	 */
	public IntegerDataElement(String name, int value) {
		this(name, value, null, null);
	}

	/**
	 * Creates a new instance with a certain initial value and validator.
	 *
	 * @param name      The name of this element
	 * @param value     The initial value
	 * @param validator The validator for the value or NULL for none
	 * @param flags     The optional flags for this data element
	 */
	public IntegerDataElement(String name, int value,
		Validator<? super Integer> validator, Set<Flag> flags) {
		this(name, Integer.valueOf(value), validator, flags);
	}

	/**
	 * Creates a new instance with a certain initial value and validator.
	 *
	 * @param name      The name of this element
	 * @param value     The initial value
	 * @param validator The validator for the value or NULL for none
	 * @param flags     The optional flags for this data element
	 */
	public IntegerDataElement(String name, Integer value,
		Validator<? super Integer> validator, Set<Flag> flags) {
		super(name, validator, flags);

		this.value = value;
	}

	/**
	 * Default constructor for serialization.
	 */
	protected IntegerDataElement() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IntegerDataElement copy(CopyMode mode,
		PropertyName<?>... copyProperties) {
		return (IntegerDataElement) super.copy(mode, copyProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Integer getValue() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStringValue(String value) {
		if (value == null || value.isEmpty()) {
			setValue(null);
		} else {
			setValue(Integer.valueOf(value));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IntegerDataElement newInstance() {
		return new IntegerDataElement();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateValue(Integer newValue) {
		value = newValue;
	}
}
