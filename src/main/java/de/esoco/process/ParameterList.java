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
package de.esoco.process;

import de.esoco.lib.property.LayoutType;
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

	private boolean				  bIsFragment;
	private ParameterBase<?, ?>[] rLastAddedParams;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rFragment   The fragment to handle the parameter for
	 * @param rParamType  The parameter relation type to handle
	 * @param bIsFragment TRUE if this parameter represents a distinct fragment
	 *                    and FALSE if it is a subordinate panel of a fragment
	 */
	public ParameterList(InteractionFragment				 rFragment,
						 RelationType<List<RelationType<?>>> rParamType,
						 boolean							 bIsFragment)
	{
		super(rFragment, rParamType);

		this.bIsFragment = bIsFragment;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the list display mode to display this parameter list with.
	 *
	 * @param  eMode The list display mode
	 *
	 * @return This instance for concatenation
	 */
	public ParameterList as(LayoutType eMode)
	{
		set(UserInterfaceProperties.LAYOUT, eMode);

		return this;
	}

	/***************************************
	 * Marks the wrapped relation type to be displayed as readonly in the
	 * fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	@Override
	public ParameterList display()
	{
		if (bIsFragment)
		{
			fragment().getParent().addDisplayParameters(type());
		}
		else
		{
			super.display();
		}

		return this;
	}

	/***************************************
	 * Enables or disables the editing of this fragment and of all it's
	 * children.
	 *
	 * @param  bEnable TRUE to enable editing, FALSE to disable
	 *
	 * @return This instance for concatenation
	 */
	public ParameterList enableEdit(boolean bEnable)
	{
		InteractionFragment rFragment = fragment();

		if (!bIsFragment)
		{
			rFragment = fragment().getSubFragment(type());
		}

		rFragment.enableEdit(bEnable);

		return this;
	}

	/***************************************
	 * Marks the parameters that have been added to this parameter's fragment
	 * with the last call to {@link #add(ParameterBase...)} for input.
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

	/***************************************
	 * Marks the wrapped relation type to be displayed as editable in the
	 * fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	@Override
	public ParameterList input()
	{
		if (bIsFragment)
		{
			fragment().getParent().addInputParameters(type());
		}
		else
		{
			super.display();
		}

		return this;
	}
}
