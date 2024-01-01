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
package de.esoco.entity;

import org.obrel.core.RelationType;
import org.obrel.type.StandardTypes;

import java.util.Date;

/**
 * A base class for entities that may be valid only during a certain period of
 * time.
 *
 * @author eso
 */
public abstract class PeriodEntity extends Entity {

	/**
	 * The start date (inclusively) from which an invoicing element will be
	 * valid.
	 */
	public static final RelationType<Date> START_DATE =
		StandardTypes.START_DATE;

	/**
	 * The end date (exclusively) until an invoicing element will be valid.
	 * Exclusively means that the entity will only be valid until just before
	 * the end dates milliseconds.
	 */
	public static final RelationType<Date> END_DATE = StandardTypes.END_DATE;

	private static final long serialVersionUID = 1L;

	/**
	 * Checks whether this entity is valid on a certain date, i.e. whether it's
	 * start date is before or equal to and less than the given date.
	 *
	 * @param date The date to check
	 * @return TRUE if this entity is valid on the given date
	 */
	public boolean isValidOn(Date date) {
		Date startDate = get(START_DATE);
		Date endDate = get(END_DATE);

		return (startDate == null || date.compareTo(startDate) >= 0) &&
			(endDate == null || date.compareTo(endDate) < 0);
	}

	/**
	 * Sets a date attribute to the earliest date compared with the current
	 * attribute value.
	 *
	 * @param dateAttr The date attribute to compare and set
	 * @param date     The new date value to compare the current value with
	 */
	public void setEarliestDate(RelationType<Date> dateAttr, Date date) {
		Date currentDate = get(dateAttr);

		if (currentDate == null || currentDate.compareTo(date) > 0) {
			set(dateAttr, date);
		}
	}

	/**
	 * Sets a date attribute to the latest date compared with the current
	 * attribute value.
	 *
	 * @param dateAttr The date attribute to compare and set
	 * @param date     The new date value to compare the current value with
	 */
	public void setLatestDate(RelationType<Date> dateAttr, Date date) {
		Date currentDate = get(dateAttr);

		if (currentDate == null || currentDate.compareTo(date) < 0) {
			set(dateAttr, date);
		}
	}
}
