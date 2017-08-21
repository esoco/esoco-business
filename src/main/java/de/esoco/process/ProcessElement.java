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

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.CollectionPredicates;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Functions;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.StringFunctions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obrel.core.RelationType;
import org.obrel.core.SerializableRelatedObject;
import org.obrel.type.MetaTypes;

import static de.esoco.lib.expression.Functions.value;
import static de.esoco.lib.expression.Predicates.isNull;

import static de.esoco.process.ProcessRelationTypes.CONTINUATION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAM_VALIDATIONS;
import static de.esoco.process.ProcessRelationTypes.PARAM_VALIDATIONS;

import static org.obrel.type.MetaTypes.MANDATORY;


/********************************************************************
 * The abstract base class for objects that represent or configure process
 * steps.
 *
 * @author eso
 */
public abstract class ProcessElement extends SerializableRelatedObject
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** Predefined message resource key to signal a missing process parameter */
	public static final String MSG_PARAM_NOT_SET = "ProcessParamNotSet";

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public ProcessElement()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * A convenience method that creates a mutable list of process parameters
	 * without the need to explicitly specify 'RelationType&lt;?&gt;' as would
	 * be required with the generic methods {@link Arrays#asList(Object...)} or
	 * {@link CollectionUtil#listOf(Object...)}. For an immutable list the
	 * method {@link #staticParams(RelationType...)} can be used.
	 *
	 * @param  rParams The parameters to place in the list
	 *
	 * @return The mutable parameter list
	 */
	public static List<RelationType<?>> params(RelationType<?>... rParams)
	{
		return CollectionUtil.<RelationType<?>>listOf(rParams);
	}

	/***************************************
	 * A convenience method that creates a immutable list of process parameters
	 * without the need to explicitly specify 'RelationType&lt;?&gt;' as would
	 * be required with the generic methods {@link Arrays#asList(Object...)} or
	 * {@link CollectionUtil#listOf(Object...)}. For a mutable list the method
	 * {@link #params(RelationType...)} can be used.
	 *
	 * @param  rParams The parameters to place in the list
	 *
	 * @return The mutable parameter list
	 */
	public static List<RelationType<?>> staticParams(RelationType<?>... rParams)
	{
		return Arrays.<RelationType<?>>asList(rParams);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see #addDisplayParameters(Collection)
	 */
	public void addDisplayParameters(RelationType<?>... rParams)
	{
		addDisplayParameters(Arrays.asList(rParams));
	}

	/***************************************
	 * Adds parameter types into the list of this step's interaction parameters
	 * for display only.
	 *
	 * @param rParams The parameter types to be displayed
	 */
	public void addDisplayParameters(
		Collection<? extends RelationType<?>> rParams)
	{
		List<RelationType<?>> rInteractionParams = get(INTERACTION_PARAMS);

		for (RelationType<?> rParam : rParams)
		{
			if (!rInteractionParams.contains(rParam))
			{
				rInteractionParams.add(rParam);
			}
		}
	}

	/***************************************
	 * @see #addInputParameters(Collection)
	 */
	public void addInputParameters(RelationType<?>... rParams)
	{
		addInputParameters(Arrays.asList(rParams));
	}

	/***************************************
	 * Adds parameter types into the list of this step's interaction parameters
	 * and marks them for input with {@link #markInputParams(boolean,
	 * Collection)}. If parameters should not be displayed on the top level
	 * during an interaction (e.g. if they are members of a data element
	 * hierarchy) they should only be marked for input and not added with this
	 * method.
	 *
	 * @param rParams The parameter types to be displayed
	 */
	public void addInputParameters(
		Collection<? extends RelationType<?>> rParams)
	{
		addDisplayParameters(rParams);
		markInputParams(true, rParams);
	}

	/***************************************
	 * Clears this step's interaction parameters.
	 */
	public void clearInteractionParameters()
	{
		get(INTERACTION_PARAMS).clear();
		get(INPUT_PARAMS).clear();
	}

	/***************************************
	 * Marks interaction parameters types as continuation parameters.
	 *
	 * @param rParams The parameters to be marked for process continuation
	 *
	 * @see   #setContinueOnInteraction(boolean, RelationType...)
	 */
	public void continueOnInteraction(RelationType<?>... rParams)
	{
		setContinueOnInteraction(true, rParams);
	}

	/***************************************
	 * Checks whether a certain parameter is registered as an interaction
	 * parameter.
	 *
	 * @param  rParam The parameter to check
	 *
	 * @return TRUE if the parameter is an interaction parameter
	 */
	public boolean hasInteractionParameter(RelationType<?> rParam)
	{
		return get(INTERACTION_PARAMS).contains(rParam);
	}

	/***************************************
	 * @see #markInputParams(boolean, Collection)
	 */
	public void markInputParams(boolean bInput, RelationType<?>... rParams)
	{
		markInputParams(bInput, Arrays.asList(rParams));
	}

	/***************************************
	 * Marks a number of parameter types as input parameters. Such parameters
	 * will be rendered as input components during interactions.
	 *
	 * @param bInput  TRUE to add input parameters, FALSE to remove
	 * @param rParams The parameters to add or remove
	 */
	public void markInputParams(
		boolean								  bInput,
		Collection<? extends RelationType<?>> rParams)
	{
		Set<RelationType<?>> rInputParams = get(INPUT_PARAMS);

		if (bInput)
		{
			rInputParams.addAll(rParams);
		}
		else
		{
			rInputParams.removeAll(rParams);
		}
	}

	/***************************************
	 * Removes all parameter validations from this step.
	 */
	public void removeAllParameterValidations()
	{
		getParameterValidations(false).clear();
		getParameterValidations(true).clear();
	}

	/***************************************
	 * Removes certain parameter types from the list of this step's interaction
	 * parameters.
	 *
	 * @param rParams The parameter types to be displayed
	 */
	public void removeInteractionParameters(RelationType<?>... rParams)
	{
		removeInteractionParameters(Arrays.asList(rParams));
	}

	/***************************************
	 * Removes certain parameter types from the list of this step's interaction
	 * parameters.
	 *
	 * @param rParams The parameter types to be displayed
	 */
	public void removeInteractionParameters(Collection<RelationType<?>> rParams)
	{
		get(INTERACTION_PARAMS).removeAll(rParams);
		get(INPUT_PARAMS).removeAll(rParams);
	}

	/***************************************
	 * Removes certain parameter validations from this step.
	 *
	 * @param rParams The parameters to remove the validations for
	 */
	public void removeParameterValidations(
		Collection<? extends RelationType<?>> rParams)
	{
		Map<RelationType<?>, Function<?, String>> rFinishValidations	  =
			getParameterValidations(false);
		Map<RelationType<?>, Function<?, String>> rInteractionValidations =
			getParameterValidations(true);

		for (RelationType<?> rParam : rParams)
		{
			rFinishValidations.remove(rParam);
			rInteractionValidations.remove(rParam);
		}
	}

	/***************************************
	 * Removes certain parameter validations from this step.
	 *
	 * @param rParams The parameters to remove the validations for
	 */
	public void removeParameterValidations(RelationType<?>... rParams)
	{
		removeParameterValidations(Arrays.asList(rParams));
	}

	/***************************************
	 * Sets or removes the continuation parameter mark for the given interaction
	 * parameters. If an interactive input event is caused by a continuation
	 * parameter the process execution will continue instead of re-executing the
	 * current step. This is typically used for enumerated parameters with an
	 * immediate interaction flag set.
	 *
	 * @param bContinue TRUE to continue the process on interaction, FALSE to
	 *                  stay in the same step
	 * @param rParams   The parameters to set or remove the continuation mark
	 *                  for
	 */
	public void setContinueOnInteraction(
		boolean			   bContinue,
		RelationType<?>... rParams)
	{
		if (bContinue)
		{
			get(CONTINUATION_PARAMS).addAll(Arrays.asList(rParams));
		}
		else
		{
			get(CONTINUATION_PARAMS).removeAll(Arrays.asList(rParams));
		}
	}

	/***************************************
	 * Marks the mandatory parameters of this element by setting the flag
	 * relation {@link MetaTypes#MANDATORY} to TRUE on relations with the given
	 * relation types. If no relation with a certain type exists already it will
	 * be created with an initial value of NULL.
	 *
	 * @param rParams The parameter types to be marked as mandatory
	 */
	public void setMandatory(RelationType<?>... rParams)
	{
		for (RelationType<?> rParam : rParams)
		{
			set(rParam, null).annotate(MANDATORY);
		}
	}

	/***************************************
	 * @see #setParameterNotEmptyValidations(Collection)
	 */
	public void setParameterNotEmptyValidations(RelationType<?>... rParams)
	{
		setParameterNotEmptyValidations(Arrays.asList(rParams));
	}

	/***************************************
	 * A shortcut method to add a validation that checks that parameters are not
	 * empty. If the target type of the parameter is a {@link CharSequence} or
	 * {@link Collection} the parameter value will be checked to be not NULL and
	 * to have a size greater than zero. Otherwise this method has the same
	 * effect as {@link #setParameterNotNullValidations(Collection)}.
	 *
	 * @param rParams The parameters to validate
	 *
	 * @see   #setParameterValidation(RelationType, String, Predicate)
	 */
	public void setParameterNotEmptyValidations(
		Collection<? extends RelationType<?>> rParams)
	{
		for (RelationType<?> rParam : rParams)
		{
			Class<?> rDatatype = rParam.getValueType();

			if (CharSequence.class.isAssignableFrom(rDatatype))
			{
				@SuppressWarnings("unchecked")
				RelationType<CharSequence> rStringParam =
					(RelationType<CharSequence>) rParam;

				setParameterValidation(rStringParam,
									   MSG_PARAM_NOT_SET,
									   StringFunctions.isEmpty());
			}
			else if (Collection.class.isAssignableFrom(rDatatype))
			{
				@SuppressWarnings("unchecked")
				RelationType<Collection<?>> rCollectionParam =
					(RelationType<Collection<?>>) rParam;

				setParameterValidation(rCollectionParam,
									   MSG_PARAM_NOT_SET,
									   CollectionPredicates.isEmpty());
			}
			else
			{
				setParameterValidation(rParam, MSG_PARAM_NOT_SET, isNull());
			}
		}
	}

	/***************************************
	 * A shortcut method to validate that certain parameters are not NULL.
	 *
	 * @param rParams The parameters to check
	 */
	public void setParameterNotNullValidations(RelationType<?>... rParams)
	{
		setParameterNotNullValidations(Arrays.asList(rParams));
	}

	/***************************************
	 * A shortcut method to validate that certain parameters are not NULL.
	 *
	 * @param rParams The parameters to check
	 */
	public void setParameterNotNullValidations(
		Collection<? extends RelationType<?>> rParams)
	{
		for (RelationType<?> rParam : rParams)
		{
			setParameterValidation(rParam, MSG_PARAM_NOT_SET, isNull());
		}
	}

	/***************************************
	 * Sets the validation function for a certain process input parameter. The
	 * validation function must return either NULL if the value of the given
	 * parameter is valid or else a string with a validation message that
	 * describes why the value is invalid (typically a resource key). Invoking
	 * this method overrides any previous validation function.
	 *
	 * @param rParam         The parameter to check
	 * @param bOnInteraction TRUE to validate on each interaction, FALSE to
	 *                       validate only if the process step is finished
	 * @param fValidation    The validation function
	 */
	public <T> void setParameterValidation(
		RelationType<T>				rParam,
		boolean						bOnInteraction,
		Function<? super T, String> fValidation)
	{
		getParameterValidations(bOnInteraction).put(rParam, fValidation);
	}

	/***************************************
	 * A shortcut method to set an input validation that has only a single
	 * validation message. The predicate argument must check whether the
	 * parameter value is invalid, i.e. it must return TRUE if the value is
	 * invalid and FALSE if it is valid.
	 *
	 * @param rParam       The parameter to check
	 * @param sInvalidInfo The string to return if the parameter is invalid
	 * @param pIsInvalid   A predicate that checks if the parameter is invalid
	 *
	 * @see   #setParameterValidation(RelationType, boolean, Function)
	 */
	public <T> void setParameterValidation(RelationType<T>		rParam,
										   String				sInvalidInfo,
										   Predicate<? super T> pIsInvalid)
	{
		setParameterValidation(rParam,
							   false,
							   Functions.<T, String>doIf(pIsInvalid,
														 value(sInvalidInfo)));
	}

	/***************************************
	 * Returns the parameter validations for a certain validation type.
	 *
	 * @param  bOnInteraction TRUE for interaction validations, FALSE for
	 *                        finishing validations
	 *
	 * @return
	 */
	protected Map<RelationType<?>, Function<?, String>> getParameterValidations(
		boolean bOnInteraction)
	{
		Map<RelationType<?>, Function<?, String>> rParamValidations =
			bOnInteraction ? get(INTERACTION_PARAM_VALIDATIONS)
						   : get(PARAM_VALIDATIONS);

		return rParamValidations;
	}
}
