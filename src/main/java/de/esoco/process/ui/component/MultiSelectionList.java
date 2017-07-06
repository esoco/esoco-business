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

import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ui.Container;
import de.esoco.process.ui.ListControl;

import java.util.Arrays;
import java.util.Collection;

import static de.esoco.lib.property.StyleProperties.LIST_STYLE;


/********************************************************************
 * A list component that allows the selection of multiple values. The datatype
 * of of the list values can be defined on creation. Typically string and enum
 * values are supported.
 *
 * @author eso
 */
public class MultiSelectionList<T>
	extends ListControl<java.util.List<T>, MultiSelectionList<T>>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent   The parent container
	 * @param rDatatype The datatype of the list elements
	 */
	public MultiSelectionList(Container<?> rParent, Class<T> rDatatype)
	{
		super(rParent, null, null);

		initListParameterType(rDatatype);
		set(LIST_STYLE, ListStyle.LIST);

		if (rDatatype.isEnum())
		{
			resid(rDatatype.getSimpleName());
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
		setListValues(Arrays.asList(rValues));
	}

	/***************************************
	 * Sets a collection of values to be displayed in this list.
	 *
	 * @param rValues The list values
	 */
	public void setListValues(Collection<T> rValues)
	{
		fragment().annotateParameter(type(),
									 null,
									 ProcessRelationTypes.ALLOWED_VALUES,
									 rValues);
	}
}
