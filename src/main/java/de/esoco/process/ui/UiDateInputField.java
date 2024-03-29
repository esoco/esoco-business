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
package de.esoco.process.ui;

import de.esoco.data.element.DateDataElement.DateInputType;
import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.DateAttribute;

import java.util.Date;

import static de.esoco.data.element.DateDataElement.DATE_INPUT_TYPE;
import static de.esoco.lib.property.ContentProperties.CONTENT_TYPE;

/**
 * A date input field.
 *
 * @author eso
 */
public abstract class UiDateInputField<C extends UiDateInputField<C>>
	extends UiInputField<Date, C> implements DateAttribute {

	/**
	 * Creates a new instance.
	 *
	 * @param parent container The parent fragment
	 * @param date   The initial date value
	 * @param type   The type of date input
	 */
	protected UiDateInputField(UiContainer<?> parent, Date date,
		DateInputType type) {
		super(parent, Date.class, date);

		set(DATE_INPUT_TYPE, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getDate() {
		return fragment().getParameter(type());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDate(Date date) {
		fragment().setParameter(type(), date);
	}

	/**
	 * Enables the input of a time value besides the calendar date.
	 *
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public C withTimeInput() {
		set(CONTENT_TYPE, ContentType.DATE_TIME);

		return (C) this;
	}
}
