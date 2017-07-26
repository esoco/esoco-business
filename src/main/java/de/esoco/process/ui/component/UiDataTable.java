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
package de.esoco.process.ui.component;

import de.esoco.data.element.SelectionDataElement;

import de.esoco.lib.model.ColumnDefinition;
import de.esoco.lib.property.InteractionEventType;

import de.esoco.process.ValueEventHandler;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiTableControl;
import de.esoco.process.ui.UiTextInputField;

import java.util.Arrays;
import java.util.Collection;


/********************************************************************
 * A single-line text input field.
 *
 * @author eso
 */
public class UiDataTable
	extends UiTableControl<SelectionDataElement, UiDataTable>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @see UiTextInputField#TextInput(UiContainer, String)
	 */
	public UiDataTable(UiContainer<?> rContainer)
	{
		super(rContainer, SelectionDataElement.class);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the event handler for selection events of this table.
	 *
	 * @param  rEventHandler The event handler
	 *
	 * @return This instance for concatenation
	 */
	public final UiDataTable onSelection(
		ValueEventHandler<String> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.UPDATE,
										sd ->
										rEventHandler.handleValueUpdate(sd.getValue()));
	}

	/***************************************
	 * Sets the event handler for selection confirmed events (e.g. by double
	 * click) of this table.
	 *
	 * @param  rEventHandler The event handler
	 *
	 * @return This instance for concatenation
	 */
	public final UiDataTable onSelectionConfirmed(
		ValueEventHandler<String> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.ACTION,
										sd ->
										rEventHandler.handleValueUpdate(sd.getValue()));
	}

	/***************************************
	 * Sets the attributes to be displayed for entity queries.
	 *
	 * @see #setColumns(Collection)
	 */
	@SuppressWarnings("unchecked")
	public void setColumns(ColumnDefinition... rColumns)
	{
		setColumns(Arrays.asList(rColumns));
	}

	/***************************************
	 * Sets the entity attributes to be displayed as the table columns. The
	 * datatype of a column is a function that queries the attributes from an
	 * entity. This applies to standard relation types as these are also
	 * functions that can be applied to relatable objects (like entities). But
	 * that can also be compound functions that generate the attribute value to
	 * displayed in a result table on access. An example could be a function
	 * that extracts the name from an entity reference (e.g. <code>
	 * NAME.from(OTHER_ENTITY)</code>).
	 *
	 * @param rColumns rColumnAttributes The entity attribute access functions
	 */
	@SuppressWarnings("unchecked")
	public void setColumns(Collection<ColumnDefinition> rColumns)
	{
	}
}
