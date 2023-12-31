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
package de.esoco.process.ui.component;

import de.esoco.data.element.DateDataElement.DateInputType;

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiDateInputField;

import java.util.Date;

/**
 * A component that allows to select a date from a calendar display.
 *
 * @author eso
 */
public class UiCalendar extends UiDateInputField<UiCalendar> {

	/**
	 * Creates a new instance with the current date as it's value.
	 *
	 * @param rParent rContainer The parent container
	 */
	public UiCalendar(UiContainer<?> rParent) {
		this(rParent, null);
	}

	/**
	 * Creates a new instance for a particular date.
	 *
	 * @param rContainer The parent container
	 * @param rDate      The initial date to display
	 */
	public UiCalendar(UiContainer<?> rContainer, Date rDate) {
		super(rContainer, rDate, DateInputType.CALENDAR);
	}
}
