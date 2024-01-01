//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.data.validate;

/**
 * A validator that validates string values against a regular expression
 * pattern.
 *
 * @author eso
 */
public class RegExValidator implements Validator<String> {

	private static final long serialVersionUID = 1L;

	private String pattern;

	/**
	 * Creates a new instance.
	 *
	 * @param pattern The regular expression pattern to validate against
	 */
	public RegExValidator(String pattern) {
		assert pattern != null;

		this.pattern = pattern;
	}

	/**
	 * Default constructor for serialization.
	 */
	RegExValidator() {
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		RegExValidator other = (RegExValidator) obj;

		return pattern.equals(other.pattern);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 37 * pattern.hashCode();
	}

	/**
	 * Validates the string value by matching it against the regular expression
	 * pattern of this instance.
	 *
	 * @see Validator#isValid(Object)
	 */
	@Override
	public boolean isValid(String value) {
		return value.matches(pattern);
	}

	/**
	 * Returns the regular expression pattern of this validator.
	 *
	 * @return The regular expression pattern
	 */
	protected final String getPattern() {
		return pattern;
	}
}
