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
package de.esoco.process.param;

import de.esoco.process.ProcessFragment;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.step.InteractionFragment;
import org.obrel.core.RelationType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A parameter subclass that wraps parameters with a collection datatype.
 *
 * @author eso
 */
public class CollectionParameter<T, C extends Collection<T>,
	P extends CollectionParameter<T, C, P>>
	extends ParameterBase<C, P> {

	/**
	 * @see ParameterBase#ParameterBase(InteractionFragment, RelationType)
	 */
	public CollectionParameter(InteractionFragment fragment,
		RelationType<C> paramType) {
		super(fragment, paramType);
	}

	/**
	 * Sets the elements that this collection parameter is allowed to contain.
	 *
	 * @see ProcessFragment#setAllowedElements(RelationType, Collection)
	 */
	@SuppressWarnings("unchecked")
	public P allowElements(Collection<T> values) {
		fragment().annotateParameter(type(), null,
			ProcessRelationTypes.ALLOWED_VALUES, values);

		return (P) this;
	}

	/**
	 * Returns the collection of elements that this collection parameter is
	 * allowed to contain.
	 *
	 * @return The collection of allowed elements
	 */
	public Collection<T> allowedElements() {
		return fragment().getAllowedElements(type());
	}

	/**
	 * A set-based collection parameter.
	 *
	 * @author eso
	 */
	public static class ListParameter<T>
		extends CollectionParameter<T, List<T>, ListParameter<T>> {

		/**
		 * @see CollectionParameter#CollectionParameter(InteractionFragment,
		 * RelationType)
		 */
		public ListParameter(InteractionFragment fragment,
			RelationType<List<T>> paramType) {
			super(fragment, paramType);
		}
	}

	/**
	 * A set-based collection parameter.
	 *
	 * @author eso
	 */
	public static class SetParameter<T>
		extends CollectionParameter<T, Set<T>, SetParameter<T>> {

		/**
		 * @see CollectionParameter#CollectionParameter(InteractionFragment,
		 * RelationType)
		 */
		public SetParameter(InteractionFragment fragment,
			RelationType<Set<T>> paramType) {
			super(fragment, paramType);
		}
	}
}
