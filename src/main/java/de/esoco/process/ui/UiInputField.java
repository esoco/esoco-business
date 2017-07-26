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

import de.esoco.lib.property.InteractionEventType;

import de.esoco.process.ValueEventHandler;


/********************************************************************
 * The base class for input fields.
 *
 * @author eso
 */
public abstract class UiInputField<T, C extends UiInputField<T, C>>
	extends UiControl<T, C>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent   The parent container
	 * @param rDatatype The value datatype
	 * @param rValue    The initial value
	 */
	public UiInputField(UiContainer<?> rParent, Class<T> rDatatype, T rValue)
	{
		super(rParent, rDatatype);

		fragment().setParameter(type(), rValue);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the event handler for input confirmation events (enter key) of this
	 * input field.
	 *
	 * @param  rEventHandler The event handler
	 *
	 * @return This instance for concatenation
	 */
	public final C onEnter(ValueEventHandler<T> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.ACTION,
										rEventHandler);
	}

	/***************************************
	 * Sets the event handler for value update events of this input field.
	 *
	 * @param  rEventHandler The event handler
	 *
	 * @return This instance for concatenation
	 */
	public final C onInput(ValueEventHandler<T> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.UPDATE,
										rEventHandler);
	}
}
