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

import de.esoco.lib.property.Layout;
import de.esoco.lib.property.UserInterfaceProperties;

import de.esoco.process.step.InteractionFragment;

import java.util.List;

import org.obrel.core.RelationType;


/********************************************************************
 * A {@link Parameter} subclasses for that manages a parameter that refers to a
 * list of other process parameters.
 *
 * @author eso
 */
public class ParameterList
	extends ParameterBase<List<RelationType<?>>, ParameterList>
{
	//~ Instance fields --------------------------------------------------------

	private boolean				  bIsPanel;
	private ParameterBase<?, ?>[] rLastAddedParams;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rFragment  The fragment to handle the parameter for
	 * @param rParamType The parameter relation type to handle
	 * @param bIsPanel   TRUE if this parameter represents a subordinate panel
	 *                   of it's fragment
	 */
	public ParameterList(InteractionFragment				 rFragment,
						 RelationType<List<RelationType<?>>> rParamType,
						 boolean							 bIsPanel)
	{
		super(rFragment, rParamType);

		this.bIsPanel = bIsPanel;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds certain parameters to this list.
	 *
	 * @param  rParams The parameters to add
	 *
	 * @return This instance for concatenation
	 */
	public ParameterList add(ParameterBase<?, ?>... rParams)
	{
		rLastAddedParams = rParams;

		for (ParameterBase<?, ?> rParam : rParams)
		{
			RelationType<?> rParamType = rParam.type();

			value().add(rParamType);

			if (bIsPanel)
			{
				fragment().addPanelParameters(ProcessElement.params(rParamType));
			}
		}

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
		return add(fragment().addSubFragment(rSubFragment.getClass()
											 .getSimpleName(),
											 rSubFragment));
	}

	/***************************************
	 * Sets the list display mode to display this parameter list with.
	 *
	 * @param  eMode The list display mode
	 *
	 * @return This instance for concatenation
	 */
	public ParameterList as(Layout eMode)
	{
		set(UserInterfaceProperties.LAYOUT, eMode);

		return this;
	}

	/***************************************
	 * Marks the parameters that have been added to this parameter's fragment
	 * with the last call to {@link #add(Parameter...)} for input.
	 *
	 * @return This instance for concatenation
	 */
	public ParameterList forInput()
	{
		if (rLastAddedParams != null)
		{
			for (ParameterBase<?, ?> rParam : rLastAddedParams)
			{
				rParam.input();
			}
		}

		return this;
	}
}
