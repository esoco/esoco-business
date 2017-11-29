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


/********************************************************************
 * A validator for date values. The date can be constrained by a start and an
 * end value if required.
 *
 * @author eso
 */
public class DateValidator implements Validator<Date>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Date rStartDate;
	private Date rEndDate;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that tests against a certain date range.
	 *
	 * @param rStartDate The start date to test against or NULL for no
	 *                   limitation
	 * @param rEndDate   The end date to test against or NULL for no limitation
	 */
	public DateValidator(Date rStartDate, Date rEndDate)
	{
		this.rStartDate = rStartDate;
		this.rEndDate   = rEndDate;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	DateValidator()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object rObj)
	{
		if (this == rObj)
		{
			return true;
		}

		if (rObj == null || getClass() != rObj.getClass())
		{
			return false;
		}

		DateValidator rOther = (DateValidator) rObj;

		return Objects.equals(rStartDate, rOther.rStartDate) &&
			   Objects.equals(rEndDate, rOther.rEndDate);
	}

	/***************************************
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return 37 * (rStartDate != null ? rStartDate.hashCode() : 1) +
			   (rEndDate != null ? rEndDate.hashCode() : 0);
	}

	/***************************************
	 * @see Validator#isValid(Object)
	 */
	@Override
	public boolean isValid(Date rDate)
	{
		return (rStartDate == null || rDate.compareTo(rStartDate) >= 0) &&
			   (rEndDate == null || rDate.compareTo(rEndDate) <= 0);
	}
}
