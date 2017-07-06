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

import org.obrel.core.RelationType;


/********************************************************************
 * The base class for interactive components.
 *
 * @author eso
 */
public class Control<T, C extends Control<T, C>> extends Component<T, C>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @see Component#Component(Container, Class)
	 */
	public Control(Container<?> rParent, Class<? super T> rDatatype)
	{
		super(rParent, rDatatype);

		if (rDatatype != null)
		{
			input();
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Initializes this control with a list parameter type. This is intended for
	 * subclasses that have a list parameter type which must be initialized with
	 * an additional element datatype. In that case the subclass should use NULL
	 * for the datatype parameter to the superclass constructor and instead
	 * invoke this method afterwards.
	 *
	 * @param rElementDatatype The element datatype of the list parameter type
	 */
	@SuppressWarnings("unchecked")
	protected void initListParameterType(Class<?> rElementDatatype)
	{
		RelationType<?> rListType =
			fragment().getTemporaryListType(null, rElementDatatype);

		setParameterType((RelationType<T>) rListType);
		input();
	}
}
