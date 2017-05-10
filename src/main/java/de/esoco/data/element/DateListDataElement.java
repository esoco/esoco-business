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

import de.esoco.lib.property.HasProperties;
import de.esoco.lib.property.PropertyName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/********************************************************************
 * A data element that holds a list of date values and associated data in
 * instances of {@link DateDataElement}. The properties of the data elements can
 * be used to transfer additional
 *
 * @author eso
 */
public class DateListDataElement extends ListDataElement<DateDataElement>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the display types for time the rendering in a Timetable
	 * component.
	 */
	public enum TimetableDisplayStyle
	{
		DAY, MONTH, AGENDA;
	}

	/********************************************************************
	 * Enumeration of the possible edit types that can occur in interactions for
	 * the child elements.
	 */
	public enum InteractionType
	{
		OPEN, DATE_OPEN, SELECT, UPDATE, DELETE, CREATE
	}

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The type of the interaction event that occurred on a child. */
	public static final PropertyName<InteractionType> INTERACTION_TYPE =
		PropertyName.newEnumName("INTERACTION_TYPE", InteractionType.class);

	/** UI property: the style in which to render a Timetable display. */
	public static final PropertyName<TimetableDisplayStyle> TIMETABLE_DISPLAY_STYLE =
		PropertyName.newEnumName("TIMETABLE_DISPLAY_STYLE",
								 TimetableDisplayStyle.class);

	/**
	 * UI property: the number of days to display with {@link
	 * TimetableDisplayStyle#DAY}.
	 */
	public static final PropertyName<Integer> TIMETABLE_DAYS =
		PropertyName.newIntegerName("TIMETABLE_DAYS");

	/** UI property: the first hour to display in DAY views. */
	public static final PropertyName<Integer> TIMETABLE_DAY_START =
		PropertyName.newIntegerName("TIMETABLE_DAY_START");

	/** UI property: the first working hour in DAY views. */
	public static final PropertyName<Integer> TIMETABLE_FIRST_WORKING_HOUR =
		PropertyName.newIntegerName("TIMETABLE_FIRST_WORKING_HOUR");

	/** UI property: the last working hour in DAY views. */
	public static final PropertyName<Integer> TIMETABLE_LAST_WORKING_HOUR =
		PropertyName.newIntegerName("TIMETABLE_LAST_WORKING_HOUR");

	/** UI property: the first hour to display in DAY or AGENDA views. */
	public static final PropertyName<Integer> TIMETABLE_SCROLL_TO_HOUR =
		PropertyName.newIntegerName("TIMETABLE_SCROLL_TO_HOUR");

	/** UI property: the number of hour subdivisions in DAY views. */
	public static final PropertyName<Integer> TIMETABLE_HOUR_SUBDIVISIONS =
		PropertyName.newIntegerName("TIMETABLE_HOUR_SUBDIVISIONS");

	/** UI property: the size in pixels of hour subdivisions in DAY views. */
	public static final PropertyName<Integer> TIMETABLE_HOUR_SUBDIVISION_HEIGHT =
		PropertyName.newIntegerName("TIMETABLE_HOUR_SUBDIVISION_HEIGHT");

	/** UI property: TRUE to display week numbers. */
	public static final PropertyName<Boolean> TIMETABLE_SHOW_WEEK_NUMBERS =
		PropertyName.newBooleanName("TIMETABLE_SHOW_WEEK_NUMBERS");

	//~ Instance fields --------------------------------------------------------

	private List<DateDataElement> aDataElements;

	private InteractionType eInteractionType;
	private HasProperties   rSelection;
	private HasProperties   rInteractionData;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sName     The name of this element
	 * @param rElements The date data
	 */
	public DateListDataElement(
		String						sName,
		Collection<DateDataElement> rElements)
	{
		super(sName, null, null);

		aDataElements = new ArrayList<>(rElements);
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	protected DateListDataElement()
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
	 * Returns the properties containing the data resulting from an interaction.
	 * If the interaction is caused by the editing of an existing child of this
	 * instance the returned object will be the corresponding child element.
	 * Otherwise the returned object will contain the respective data for the
	 * interaction type.
	 *
	 * @return The interaction data
	 *
	 * @see    #getInteractionType()
	 */
	public final HasProperties getInteractionData()
	{
		return rInteractionData;
	}

	/***************************************
	 * Returns the type of an interaction that occurred. Depending on the
	 * interaction type the method {@link #getInteractionData()} will return a
	 * different kind of data object:
	 *
	 * <ul>
	 *   <li>{@link InteractionType#SELECT SELECT}, {@link InteractionType#OPEN
	 *     OPEN}, {@link InteractionType#UPDATE UPDATE}, {@link
	 *     InteractionType#DELETE DELETE}: the affected child element.</li>
	 *   <li>{@link InteractionType#DATE_OPEN DATE_OPEN}: a properties object
	 *     containing the opened date in the property {@link
	 *     StandardProperties#START_DATE}</li>
	 *   <li>{@link InteractionType#CREATE CREATE}: a properties object
	 *     containing the value of the created event in it's properties</li>
	 * </ul>
	 *
	 * @return The interaction type
	 */
	public final InteractionType getInteractionType()
	{
		return eInteractionType;
	}

	/***************************************
	 * Returns the properties object for the currently selected element.
	 *
	 * @return The selection properties or NULL for none
	 */
	public final HasProperties getSelection()
	{
		return rSelection;
	}

	/***************************************
	 * Sets the type of an interaction that occurred and the associated data.
	 *
	 * @param eType The type of interaction that occurred
	 * @param rData The properties containing the interaction-specific data
	 */
	public final void setInteraction(InteractionType eType, HasProperties rData)
	{
		eInteractionType = eType;
		rInteractionData = rData;
	}

	/***************************************
	 * Returns the properties object for the currently selected element.
	 *
	 * @param rSelection The selection properties or NULL for none
	 */
	public final void setSelection(HasProperties rSelection)
	{
		this.rSelection = rSelection;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected List<DateDataElement> getList()
	{
		return aDataElements;
	}
}
