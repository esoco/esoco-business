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

import de.esoco.process.step.InteractionFragment;

import java.util.Collection;

import org.obrel.core.RelationType;

/**
 * A parameter wrapper with additional functions for enum values.
 *
 * @author eso
 */
public class EnumParameter<E extends Enum<E>>
	extends ParameterBase<E, EnumParameter<E>> {

	/**
	 * @see ParameterBase#ParameterBase(InteractionFragment, RelationType)
	 */
	public EnumParameter(InteractionFragment rFragment,
		RelationType<E> rParamType) {
		super(rFragment, rParamType);
	}

	/**
	 * Disables certain values of the parameter enum.
	 *
	 * @param rDisabledElements The elements to disable
	 * @return This parameter instance
	 */
	@SuppressWarnings("unchecked")
	public EnumParameter<E> disable(E... rDisabledElements) {
		fragment().disableElements(type(), rDisabledElements);

		return this;
	}

	/**
	 * Disables certain values of the parameter enum.
	 *
	 * @param rDisabledElements A collection of the elements to disable
	 * @return This parameter instance
	 */
	public EnumParameter<E> disable(Collection<E> rDisabledElements) {
		fragment().disableElements(type(), rDisabledElements);

		return this;
	}

	/**
	 * Enables all enum values.
	 *
	 * @return This parameter instance
	 */
	public EnumParameter<E> enableAll() {
		fragment().enableAllElements(type());

		return this;
	}
}
