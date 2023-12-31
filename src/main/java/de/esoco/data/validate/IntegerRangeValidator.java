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
 * A validator for integer values that constrains them in a range between a
 * minimal and a maximal value.
 *
 * @author eso
 */
public class IntegerRangeValidator implements Validator<Integer> {

	private static final long serialVersionUID = 1L;

	private int nMin;

	private int nMax;

	/**
	 * Creates a new instance that tests against a certain integer range.
	 *
	 * @param nMin The minimal value (inclusive)
	 * @param nMax The maximal value (inclusive)
	 */
	public IntegerRangeValidator(int nMin, int nMax) {
		this.nMin = nMin;
		this.nMax = nMax;
	}

	/**
	 * Default constructor for serialization.
	 */
	IntegerRangeValidator() {
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object rObj) {
		if (this == rObj) {
			return true;
		}

		if (rObj == null || getClass() != rObj.getClass()) {
			return false;
		}

		IntegerRangeValidator rOther = (IntegerRangeValidator) rObj;

		return nMin == rOther.nMin && nMax == rOther.nMax;
	}

	/**
	 * Returns the maximum value.
	 *
	 * @return The maximum value
	 */
	public final int getMaximum() {
		return nMax;
	}

	/**
	 * Returns the minimum value.
	 *
	 * @return The minimum value
	 */
	public final int getMinimum() {
		return nMin;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 37 * nMin + nMax;
	}

	/**
	 * @see Validator#isValid(Object)
	 */
	@Override
	public boolean isValid(Integer rValue) {
		int nValue = rValue.intValue();

		return nMin <= nValue && nValue <= nMax;
	}
}
