//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Functions;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.StringFunctions;
import org.obrel.core.RelationType;
import org.obrel.core.SerializableRelatedObject;
import org.obrel.type.MetaTypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.esoco.lib.expression.Functions.value;
import static de.esoco.lib.expression.Predicates.isNull;
import static de.esoco.process.ProcessRelationTypes.CONTINUATION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAM_VALIDATIONS;
import static de.esoco.process.ProcessRelationTypes.PARAM_VALIDATIONS;
import static org.obrel.type.MetaTypes.MANDATORY;

/**
 * The abstract base class for objects that represent or configure process
 * steps.
 *
 * @author eso
 */
public abstract class ProcessElement extends SerializableRelatedObject {

	/**
	 * Predefined message resource key to signal a missing process parameter
	 */
	public static final String MSG_PARAM_NOT_SET = "ProcessParamNotSet";

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 */
	public ProcessElement() {
	}

	/**
	 * A convenience method that creates a mutable list of process parameters
	 * without the need to explicitly specify 'RelationType&lt;?&gt;' as would
	 * be required with the generic methods {@link Arrays#asList(Object...)} or
	 * {@link CollectionUtil#listOf(Object...)}. For an immutable list the
	 * method {@link #staticParams(RelationType...)} can be used.
	 *
	 * @param params The parameters to place in the list
	 * @return The mutable parameter list
	 */
	public static List<RelationType<?>> params(RelationType<?>... params) {
		return CollectionUtil.listOf(params);
	}

	/**
	 * A convenience method that creates a immutable list of process parameters
	 * without the need to explicitly specify 'RelationType&lt;?&gt;' as would
	 * be required with the generic methods {@link Arrays#asList(Object...)} or
	 * {@link CollectionUtil#listOf(Object...)}. For a mutable list the method
	 * {@link #params(RelationType...)} can be used.
	 *
	 * @param params The parameters to place in the list
	 * @return The mutable parameter list
	 */
	public static List<RelationType<?>> staticParams(
		RelationType<?>... params) {
		return Arrays.asList(params);
	}

	/**
	 * @see #addDisplayParameters(Collection)
	 */
	public void addDisplayParameters(RelationType<?>... params) {
		addDisplayParameters(Arrays.asList(params));
	}

	/**
	 * Adds parameter types into the list of this step's interaction parameters
	 * for display only.
	 *
	 * @param params The parameter types to be displayed
	 */
	public void addDisplayParameters(
		Collection<? extends RelationType<?>> params) {
		List<RelationType<?>> interactionParams = get(INTERACTION_PARAMS);

		for (RelationType<?> param : params) {
			if (!interactionParams.contains(param)) {
				interactionParams.add(param);
			}
		}
	}

	/**
	 * @see #addInputParameters(Collection)
	 */
	public void addInputParameters(RelationType<?>... params) {
		addInputParameters(Arrays.asList(params));
	}

	/**
	 * Adds parameter types into the list of this step's interaction parameters
	 * and marks them for input with
	 * {@link #markInputParams(boolean, Collection)}. If parameters should not
	 * be displayed on the top level during an interaction (e.g. if they are
	 * members of a data element hierarchy) they should only be marked for
	 * input
	 * and not added with this method.
	 *
	 * @param params The parameter types to be displayed
	 */
	public void addInputParameters(
		Collection<? extends RelationType<?>> params) {
		addDisplayParameters(params);
		markInputParams(true, params);
	}

	/**
	 * Clears this step's interaction parameters.
	 */
	public void clearInteractionParameters() {
		get(INTERACTION_PARAMS).clear();
		get(INPUT_PARAMS).clear();
	}

	/**
	 * Marks interaction parameters types as continuation parameters.
	 *
	 * @param params The parameters to be marked for process continuation
	 * @see #setContinueOnInteraction(boolean, RelationType...)
	 */
	public void continueOnInteraction(RelationType<?>... params) {
		setContinueOnInteraction(true, params);
	}

	/**
	 * Checks whether a certain parameter is registered as an interaction
	 * parameter.
	 *
	 * @param param The parameter to check
	 * @return TRUE if the parameter is an interaction parameter
	 */
	public boolean hasInteractionParameter(RelationType<?> param) {
		return get(INTERACTION_PARAMS).contains(param);
	}

	/**
	 * @see #markInputParams(boolean, Collection)
	 */
	public void markInputParams(boolean input, RelationType<?>... params) {
		markInputParams(input, Arrays.asList(params));
	}

	/**
	 * Marks a number of parameter types as input parameters. Such parameters
	 * will be rendered as input components during interactions.
	 *
	 * @param input  TRUE to add input parameters, FALSE to remove
	 * @param params The parameters to add or remove
	 */
	public void markInputParams(boolean input,
		Collection<? extends RelationType<?>> params) {
		Set<RelationType<?>> inputParams = get(INPUT_PARAMS);

		if (input) {
			inputParams.addAll(params);
		} else {
			inputParams.removeAll(params);
		}
	}

	/**
	 * Removes all parameter validations from this step.
	 */
	public void removeAllParameterValidations() {
		getParameterValidations(false).clear();
		getParameterValidations(true).clear();
	}

	/**
	 * Removes certain parameter types from the list of this step's interaction
	 * parameters.
	 *
	 * @param params The parameter types to be displayed
	 */
	public void removeInteractionParameters(RelationType<?>... params) {
		removeInteractionParameters(Arrays.asList(params));
	}

	/**
	 * Removes certain parameter types from the list of this step's interaction
	 * parameters.
	 *
	 * @param params The parameter types to be displayed
	 */
	public void removeInteractionParameters(
		Collection<RelationType<?>> params) {
		get(INTERACTION_PARAMS).removeAll(params);
		get(INPUT_PARAMS).removeAll(params);
	}

	/**
	 * Removes certain parameter validations from this step.
	 *
	 * @param params The parameters to remove the validations for
	 */
	public void removeParameterValidations(
		Collection<? extends RelationType<?>> params) {
		Map<RelationType<?>, Function<?, String>> finishValidations =
			getParameterValidations(false);
		Map<RelationType<?>, Function<?, String>> interactionValidations =
			getParameterValidations(true);

		for (RelationType<?> param : params) {
			finishValidations.remove(param);
			interactionValidations.remove(param);
		}
	}

	/**
	 * Removes certain parameter validations from this step.
	 *
	 * @param params The parameters to remove the validations for
	 */
	public void removeParameterValidations(RelationType<?>... params) {
		removeParameterValidations(Arrays.asList(params));
	}

	/**
	 * Sets or removes the continuation parameter mark for the given
	 * interaction
	 * parameters. If an interactive input event is caused by a continuation
	 * parameter the process execution will continue instead of re-executing
	 * the
	 * current step. This is typically used for enumerated parameters with an
	 * immediate interaction flag set.
	 *
	 * @param continueOnInteraction TRUE to continue the process on
	 *                                 interaction,
	 *                              FALSE to stay in the same step
	 * @param params                The parameters to set or remove the
	 *                              continuation mark for
	 */
	public void setContinueOnInteraction(boolean continueOnInteraction,
		RelationType<?>... params) {
		if (continueOnInteraction) {
			get(CONTINUATION_PARAMS).addAll(Arrays.asList(params));
		} else {
			get(CONTINUATION_PARAMS).removeAll(Arrays.asList(params));
		}
	}

	/**
	 * Marks the mandatory parameters of this element by setting the flag
	 * relation {@link MetaTypes#MANDATORY} to TRUE on relations with the given
	 * relation types. If no relation with a certain type exists already it
	 * will
	 * be created with an initial value of NULL.
	 *
	 * @param params The parameter types to be marked as mandatory
	 */
	public void setMandatory(RelationType<?>... params) {
		for (RelationType<?> param : params) {
			set(param, null).annotate(MANDATORY);
		}
	}

	/**
	 * @see #setParameterNotEmptyValidations(Collection)
	 */
	public void setParameterNotEmptyValidations(RelationType<?>... params) {
		setParameterNotEmptyValidations(Arrays.asList(params));
	}

	/**
	 * A shortcut method to add a validation that checks that parameters are
	 * not
	 * empty. If the target type of the parameter is a {@link CharSequence} or
	 * {@link Collection} the parameter value will be checked to be not NULL
	 * and
	 * to have a size greater than zero. Otherwise this method has the same
	 * effect as {@link #setParameterNotNullValidations(Collection)}.
	 *
	 * @param params The parameters to validate
	 * @see #setParameterValidation(RelationType, String, Predicate)
	 */
	public void setParameterNotEmptyValidations(
		Collection<? extends RelationType<?>> params) {
		for (RelationType<?> param : params) {
			Class<?> datatype = param.getTargetType();

			if (CharSequence.class.isAssignableFrom(datatype)) {
				@SuppressWarnings("unchecked")
				RelationType<CharSequence> stringParam =
					(RelationType<CharSequence>) param;

				setParameterValidation(stringParam, MSG_PARAM_NOT_SET,
					StringFunctions.isEmpty());
			} else if (Collection.class.isAssignableFrom(datatype)) {
				@SuppressWarnings("unchecked")
				RelationType<Collection<?>> collectionParam =
					(RelationType<Collection<?>>) param;

				setParameterValidation(collectionParam, MSG_PARAM_NOT_SET,
					Collection::isEmpty);
			} else {
				setParameterValidation(param, MSG_PARAM_NOT_SET, isNull());
			}
		}
	}

	/**
	 * A shortcut method to validate that certain parameters are not NULL.
	 *
	 * @param params The parameters to check
	 */
	public void setParameterNotNullValidations(RelationType<?>... params) {
		setParameterNotNullValidations(Arrays.asList(params));
	}

	/**
	 * A shortcut method to validate that certain parameters are not NULL.
	 *
	 * @param params The parameters to check
	 */
	public void setParameterNotNullValidations(
		Collection<? extends RelationType<?>> params) {
		for (RelationType<?> param : params) {
			setParameterValidation(param, MSG_PARAM_NOT_SET, isNull());
		}
	}

	/**
	 * Sets the validation function for a certain process input parameter. The
	 * validation function must return either NULL if the value of the given
	 * parameter is valid or else a string with a validation message that
	 * describes why the value is invalid (typically a resource key). Invoking
	 * this method overrides any previous validation function.
	 *
	 * @param param         The parameter to check
	 * @param onInteraction TRUE to validate on each interaction, FALSE to
	 *                      validate only if the process step is finished
	 * @param validation    The validation function
	 */
	public <T> void setParameterValidation(RelationType<T> param,
		boolean onInteraction, Function<? super T, String> validation) {
		getParameterValidations(onInteraction).put(param, validation);
	}

	/**
	 * A shortcut method to set an input validation that has only a single
	 * validation message. The predicate argument must check whether the
	 * parameter value is invalid, i.e. it must return TRUE if the value is
	 * invalid and FALSE if it is valid.
	 *
	 * @param param       The parameter to check
	 * @param invalidInfo The string to return if the parameter is invalid
	 * @param isInvalid   A predicate that checks if the parameter is invalid
	 * @see #setParameterValidation(RelationType, boolean, Function)
	 */
	public <T> void setParameterValidation(RelationType<T> param,
		String invalidInfo, Predicate<? super T> isInvalid) {
		setParameterValidation(param, false,
			Functions.doIf(isInvalid, value(invalidInfo)));
	}

	/**
	 * Returns the parameter validations for a certain validation type.
	 *
	 * @param onInteraction TRUE for interaction validations, FALSE for
	 *                      finishing validations
	 * @return The mapping from parameter types to validation functions
	 */
	protected Map<RelationType<?>, Function<?, String>> getParameterValidations(
		boolean onInteraction) {
		Map<RelationType<?>, Function<?, String>> paramValidations =
			onInteraction ?
			get(INTERACTION_PARAM_VALIDATIONS) :
			get(PARAM_VALIDATIONS);

		return paramValidations;
	}
}
