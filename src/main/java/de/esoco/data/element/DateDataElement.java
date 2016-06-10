//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import java.util.Date;
import java.util.Map;
import java.util.Set;


/********************************************************************
 * A data element subclass for {@link Date} values.
 *
 * @author eso
 */
public class DateDataElement extends DataElement<Date>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the possible input types when presenting data elements of
	 * this type in a user interface. The following types are supported:
	 *
	 * <ul>
	 *   <li>{@link #INPUT_FIELD}: Display an input field with a calendar popup
	 *     for date selection.</li>
	 *   <li>{@link #CALENDAR}: Display the date value in a calendar component.
	 *   </li>
	 * </ul>
	 */
	public enum DateInputType { INPUT_FIELD, CALENDAR }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** UI property: the type of a date input field. */
	public static final PropertyName<DateInputType> DATE_INPUT_TYPE =
		PropertyName.newEnumName("DATE_INPUT_TYPE", DateInputType.class);

	/** UI property: events that should be visualized in a calendar UI. */
	public static final PropertyName<Map<Date, String>> DATE_HIGHLIGHTS =
		PropertyName.newMapName("DATE_HIGHLIGHTS", Date.class, String.class);

	//~ Instance fields --------------------------------------------------------

	private Date rValue;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new modifiable instance without a validator.
	 *
	 * @param sName  The name of this element
	 * @param rValue The value of this element
	 */
	public DateDataElement(String sName, Date rValue)
	{
		this(sName, rValue, null, null);
	}

	/***************************************
	 * Creates a new instance with a certain initial value.
	 *
	 * @param sName      The element name
	 * @param rValue     The initial value
	 * @param rValidator The validator for the date value or NULL for none
	 * @param rFlags     The optional flags for this data element
	 */
	public DateDataElement(String				   sName,
						   Date					   rValue,
						   Validator<? super Date> rValidator,
						   Set<Flag>			   rFlags)
	{
		super(sName, rValidator, rFlags);
		this.rValue = rValue;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	DateDataElement()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * This method should be invoked to initialize the property name constants
	 * for de-serialization.
	 */
	public static void init()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the date value.
	 *
	 * @see DataElement#getValue()
	 */
	@Override
	public final Date getValue()
	{
		return rValue;
	}

	/***************************************
	 * @see DataElement#updateValue(Object)
	 */
	@Override
	protected void updateValue(Date rNewValue)
	{
		rValue = rNewValue;
	}
}
