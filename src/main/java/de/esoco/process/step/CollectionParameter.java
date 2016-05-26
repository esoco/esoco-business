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
package de.esoco.process.step;

import de.esoco.process.ParameterBase;
import de.esoco.process.ProcessFragment;
import de.esoco.process.ProcessRelationTypes;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.obrel.core.RelationType;


/********************************************************************
 * A parameter subclass that wraps parameters with a collection datatype.
 *
 * @author eso
 */
public class CollectionParameter<T, C extends Collection<T>,
								 P extends CollectionParameter<T, C, P>>
	extends ParameterBase<C, P>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public CollectionParameter(
		InteractionFragment rFragment,
		RelationType<C>		rParamType)
	{
		super(rFragment, rParamType);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the allowed values for a collection parameter.
	 *
	 * @see ProcessFragment#setAllowedElements(RelationType, Collection)
	 */
	@SuppressWarnings("unchecked")
	public P allowElements(Collection<T> rValues)
	{
		fragment().annotateParameter(type(),
									 null,
									 ProcessRelationTypes.ALLOWED_VALUES,
									 rValues);

		return (P) this;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A set-based collection parameter.
	 *
	 * @author eso
	 */
	public static class ListParameter<T>
		extends CollectionParameter<T, List<T>, ListParameter<T>>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		public ListParameter(
			InteractionFragment   rFragment,
			RelationType<List<T>> rParamType)
		{
			super(rFragment, rParamType);
		}
	}

	/********************************************************************
	 * A set-based collection parameter.
	 *
	 * @author eso
	 */
	public static class SetParameter<T>
		extends CollectionParameter<T, Set<T>, SetParameter<T>>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		public SetParameter(
			InteractionFragment  rFragment,
			RelationType<Set<T>> rParamType)
		{
			super(rFragment, rParamType);
		}
	}
}
