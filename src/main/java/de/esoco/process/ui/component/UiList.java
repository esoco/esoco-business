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

import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.ListStyle;

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiSingleSelectionList;
import de.esoco.process.ui.event.UiHasActionEvents;

import java.util.function.Consumer;


/********************************************************************
 * A list of selectable values. The datatype of of the list values can be
 * defined on creation. Typically string and enum values are supported.
 *
 * @author eso
 */
public class UiList<T> extends UiSingleSelectionList<T, UiList<T>>
	implements UiHasActionEvents<T, UiList<T>>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance. If the datatype is an enum all enum values will
	 * be pre-set as the list values. This can be changed after construction
	 * through the {@link #setListValues(java.util.Collection) setListValues}
	 * methods.
	 *
	 * @param rParent   The parent container
	 * @param rDatatype The datatype of the list values
	 */
	public UiList(UiContainer<?> rParent, Class<T> rDatatype)
	{
		super(rParent, rDatatype, ListStyle.LIST);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public UiList<T> onAction(Consumer<T> rEventHandler)
	{
		return onSelectionConfirmed(rEventHandler);
	}

	/***************************************
	 * Sets the event handler for selection confirmed events (e.g. by double
	 * click) of this list.
	 *
	 * @param  rEventHandler The event handler
	 *
	 * @return This instance for concatenation
	 */
	public final UiList<T> onSelectionConfirmed(Consumer<T> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.ACTION,
										v -> rEventHandler.accept(v));
	}
}
