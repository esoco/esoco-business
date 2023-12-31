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

	private Integer aValue;

	/**
	 * Creates a new read-only instance with a certain name and value.
	 *
	 * @param sName  The name of this element
	 * @param nValue The initial value
	 */
	public IntegerDataElement(String sName, int nValue) {
		this(sName, nValue, null, null);
	}

	/**
	 * Creates a new instance with a certain initial value and validator.
	 *
	 * @param sName      The name of this element
	 * @param nValue     The initial value
	 * @param rValidator The validator for the value or NULL for none
	 * @param rFlags     The optional flags for this data element
	 */
	public IntegerDataElement(String sName, int nValue,
		Validator<? super Integer> rValidator, Set<Flag> rFlags) {
		this(sName, Integer.valueOf(nValue), rValidator, rFlags);
	}

	/**
	 * Creates a new instance with a certain initial value and validator.
	 *
	 * @param sName      The name of this element
	 * @param rValue     The initial value
	 * @param rValidator The validator for the value or NULL for none
	 * @param rFlags     The optional flags for this data element
	 */
	public IntegerDataElement(String sName, Integer rValue,
		Validator<? super Integer> rValidator, Set<Flag> rFlags) {
		super(sName, rValidator, rFlags);

		this.aValue = rValue;
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
	public IntegerDataElement copy(CopyMode eMode,
		PropertyName<?>... rCopyProperties) {
		return (IntegerDataElement) super.copy(eMode, rCopyProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Integer getValue() {
		return aValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStringValue(String sValue) {
		if (sValue == null || sValue.isEmpty()) {
			setValue(null);
		} else {
			setValue(Integer.valueOf(sValue));
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
	protected void updateValue(Integer rNewValue) {
		aValue = rNewValue;
	}
}
