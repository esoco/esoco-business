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
package de.esoco.data.element;

import de.esoco.data.validate.Validator;
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.UserInterfaceProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static de.esoco.lib.property.StyleProperties.LIST_STYLE;

/**
 * A data element implementation that contains a string value.
 *
 * @author eso
 */
public class StringDataElement extends DataElement<String> {

	private static final long serialVersionUID = 1L;

	private String value;

	/**
	 * Creates a new modifiable instance without a validator.
	 *
	 * @param name  The name of this element
	 * @param value The value of this element
	 */
	public StringDataElement(String name, String value) {
		this(name, value, null, null);
	}

	/**
	 * Creates a new instance with a constraint for the possible element
	 * values.
	 * The constraint is defined in form of a regular expression pattern
	 * against
	 * which new element values will be matched.
	 *
	 * @param name      The name of this element
	 * @param value     The value of this element
	 * @param validator The validator for the value or NULL for none
	 * @param flags     The optional flags for this data element
	 */
	public StringDataElement(String name, String value,
		Validator<? super String> validator, Set<Flag> flags) {
		super(name, validator, flags);

		updateValue(value);
	}

	/**
	 * Default constructor for serialization.
	 */
	protected StringDataElement() {
	}

	/**
	 * Splits a phone number string into it's distinct parts and returns a list
	 * containing the parts. The list will contain 4 elements: Country code,
	 * area code, phone number, and extension. Empty parts will be returned as
	 * empty strings but will never be NULL.
	 *
	 * @param number The phone number string
	 * @return A list containing the 4 number parts
	 */
	public static List<String> getPhoneNumberParts(String number) {
		List<String> numberParts;

		if (number != null) {
			boolean hasCountryCode = number.startsWith("+");

			if (hasCountryCode) {
				number = number.substring(1);
			}

			numberParts =
				new ArrayList<String>(Arrays.asList(number.split("\\D")));

			if (!hasCountryCode) {
				numberParts.add(0, "");
			}

			while (numberParts.size() > 4) {
				numberParts.set(2, numberParts.get(2) + numberParts.remove(3));
			}
		} else {
			numberParts = new ArrayList<String>(4);
		}

		for (int i = numberParts.size(); i < 4; i++) {
			numberParts.add("");
		}

		return numberParts;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StringDataElement copy(CopyMode mode,
		PropertyName<?>... copyProperties) {
		return (StringDataElement) super.copy(mode, copyProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getValue() {
		return value;
	}

	/**
	 * Overridden to allow any value if this instance has the user interface
	 * property {@link UserInterfaceProperties#LIST_STYLE LIST_STYLE} with a
	 * value of {@link ListStyle#EDITABLE EDITABLE}.
	 *
	 * @see DataElement#isValidValue(Validator, Object)
	 */
	@Override
	public <T> boolean isValidValue(Validator<? super T> validator, T value) {
		return super.isValidValue(validator, value) || (validator != null &&
			getProperty(LIST_STYLE, null) == ListStyle.EDITABLE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStringValue(String value) {
		setValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StringDataElement newInstance() {
		return new StringDataElement();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void updateValue(String newValue) {
		value = newValue;
	}
}
