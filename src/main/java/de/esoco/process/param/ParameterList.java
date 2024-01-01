//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.process.param;

import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.process.step.InteractionFragment;
import org.obrel.core.RelationType;

import java.util.List;

/**
 * A {@link ParameterBase} subclasses for that manages a parameter that refers
 * to a list of other process parameters.
 *
 * @author eso
 */
public class ParameterList
	extends ParameterBase<List<RelationType<?>>, ParameterList> {

	private final boolean isFragment;

	/**
	 * Creates a new instance.
	 *
	 * @param fragment   The fragment to handle the parameter for
	 * @param paramType  The parameter relation type to handle
	 * @param isFragment TRUE if this parameter represents a distinct fragment
	 *                   and FALSE if it is a subordinate panel of a fragment
	 */
	public ParameterList(InteractionFragment fragment,
		RelationType<List<RelationType<?>>> paramType, boolean isFragment) {
		super(fragment, paramType);

		this.isFragment = isFragment;
	}

	/**
	 * Sets the list display mode to display this parameter list with.
	 *
	 * @param mode The list display mode
	 * @return This instance for concatenation
	 */
	public ParameterList as(LayoutType mode) {
		set(UserInterfaceProperties.LAYOUT, mode);

		return this;
	}

	/**
	 * Marks the wrapped relation type to be displayed as readonly in the
	 * fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	@Override
	public ParameterList display() {
		if (isFragment) {
			fragment().getParent().addDisplayParameters(type());
		} else {
			super.display();
		}

		return this;
	}

	/**
	 * Enables or disables the editing of this fragment and of all it's
	 * children.
	 *
	 * @param enable TRUE to enable editing, FALSE to disable
	 * @return This instance for concatenation
	 */
	public ParameterList enableEdit(boolean enable) {
		InteractionFragment fragment = fragment();

		if (!isFragment) {
			fragment = fragment().getSubFragment(type());
		}

		fragment.enableEdit(enable);

		return this;
	}

	/**
	 * Marks the wrapped relation type to be displayed as editable in the
	 * fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	@Override
	public ParameterList input() {
		if (isFragment) {
			fragment().getParent().addInputParameters(type());
		} else {
			super.display();
		}

		return this;
	}
}
