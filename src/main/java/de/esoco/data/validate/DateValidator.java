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
package de.esoco.data.validate;

import java.util.Date;
import java.util.Objects;

/**
 * A validator for date values. The date can be constrained by a start and an
 * end value if required.
 *
 * @author eso
 */
public class DateValidator implements Validator<Date> {

	private static final long serialVersionUID = 1L;

	private Date startDate;

	private Date endDate;

	/**
	 * Creates a new instance that tests against a certain date range.
	 *
	 * @param startDate The start date to test against or NULL for no
	 *                  limitation
	 * @param endDate   The end date to test against or NULL for no limitation
	 */
	public DateValidator(Date startDate, Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	/**
	 * Default constructor for serialization.
	 */
	DateValidator() {
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

		DateValidator other = (DateValidator) obj;

		return Objects.equals(startDate, other.startDate) &&
			Objects.equals(endDate, other.endDate);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 37 * (startDate != null ? startDate.hashCode() : 1) +
			(endDate != null ? endDate.hashCode() : 0);
	}

	/**
	 * @see Validator#isValid(Object)
	 */
	@Override
	public boolean isValid(Date date) {
		return (startDate == null || date.compareTo(startDate) >= 0) &&
			(endDate == null || date.compareTo(endDate) <= 0);
	}
}
