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

import de.esoco.lib.property.ListStyle;

import de.esoco.process.ui.Container;
import de.esoco.process.ui.ListControl;

import java.util.Collection;


/********************************************************************
 * A list of selectable values. The datatype of of the list values can be
 * defined on creation. Typically string and enum values are supported.
 *
 * @author eso
 */
public class List<T> extends ListControl<T, List<T>>
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
	public List(Container<?> rParent, Class<T> rDatatype)
	{
		super(rParent, rDatatype, ListStyle.LIST);

		if (rDatatype.isEnum())
		{
			setListValues(rDatatype.getEnumConstants());
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the values to be displayed in this list.
	 *
	 * @param rValues The list values
	 */
	@SuppressWarnings("unchecked")
	public void setListValues(T... rValues)
	{
		allow(rValues);
	}

	/***************************************
	 * Sets a collection of values to be displayed in this list.
	 *
	 * @param rValues The list values
	 */
	public void setListValues(Collection<T> rValues)
	{
		allow(rValues);
	}
}
