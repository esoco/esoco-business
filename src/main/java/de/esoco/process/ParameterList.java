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
package de.esoco.process;

import de.esoco.data.element.DataElementList;
import de.esoco.data.element.DataElementList.ListDisplayMode;

import de.esoco.process.step.InteractionFragment;

import java.util.List;

import org.obrel.core.RelationType;


/********************************************************************
 * A {@link Parameter} subclasses for that manages a parameter that refers to a
 * list of other process parameters.
 *
 * @author eso
 */
public class ParameterList extends Parameter<List<RelationType<?>>>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public ParameterList(
		InteractionFragment					rFragment,
		RelationType<List<RelationType<?>>> rParamType)
	{
		super(rFragment, rParamType);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a certain parameter to this list.
	 *
	 * @param  rParam The parameter to add
	 *
	 * @return This instance for concatenation
	 */
	public ParameterList add(Parameter<?> rParam)
	{
		fragment().getParameter(type()).add(rParam.type());

		return this;
	}

	/***************************************
	 * Adds a certain sub-fragment parameter to this list.
	 *
	 * @param  rSubFragment The sub-fragment to add
	 *
	 * @return This instance for concatenation
	 */
	public ParameterList add(InteractionFragment rSubFragment)
	{
		return add(fragment().addSubFragment(rSubFragment));
	}

	/***************************************
	 * Overridden to return a {@link ParameterList}.
	 *
	 * @see Parameter#display()
	 */
	@Override
	public ParameterList display()
	{
		super.display();

		return this;
	}

	/***************************************
	 * Sets the list display mode to display this parameter list with.
	 *
	 * @param  eMode The list display mode
	 *
	 * @return This instance for concatenation
	 */
	public ParameterList as(ListDisplayMode eMode)
	{
		set(DataElementList.LIST_DISPLAY_MODE, eMode);

		return this;
	}
}
