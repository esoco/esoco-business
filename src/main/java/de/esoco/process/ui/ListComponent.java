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

import de.esoco.lib.property.ListStyle;

import java.util.Collection;

import static de.esoco.lib.property.StyleProperties.LIST_STYLE;


/********************************************************************
 * Base class for interactive components that display a list of selectable
 * values.
 *
 * @author eso
 */
public abstract class ListComponent<T, C extends ListComponent<T, C>>
	extends Control<T, C>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance. If the datatype is an enum all enum values will
	 * be pre-set as the list values.
	 *
	 * @param rParent    The parent container
	 * @param rDatatype  The datatype of the list values
	 * @param eListStyle The list style
	 */
	@SuppressWarnings("unchecked")
	public ListComponent(Container<?>	  rParent,
						 Class<? super T> rDatatype,
						 ListStyle		  eListStyle)
	{
		super(rParent, rDatatype);

		set(LIST_STYLE, eListStyle);

		if (rDatatype.isEnum())
		{
			setListValues((T[]) rDatatype.getEnumConstants());
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the values to be displayed in the list.
	 *
	 * @param rValues The list values
	 */
	@SuppressWarnings("unchecked")
	public void setListValues(T... rValues)
	{
		allow(rValues);
	}

	/***************************************
	 * Sets a collection of values to be displayed in the list.
	 *
	 * @param rValues The list values
	 */
	public void setListValues(Collection<T> rValues)
	{
		allow(rValues);
	}
}
