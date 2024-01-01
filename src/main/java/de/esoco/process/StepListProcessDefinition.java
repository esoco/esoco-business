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

import de.esoco.lib.datatype.Pair;
import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.CollectionFunctions;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Functions;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextUtil;
import de.esoco.process.step.BranchStep;
import de.esoco.process.step.DisplayMessage;
import de.esoco.process.step.FunctionStep;
import de.esoco.process.step.Interaction;
import de.esoco.process.step.InteractionFragment;
import de.esoco.process.step.SwitchStep;
import de.esoco.process.step.TransferParam;
import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.esoco.process.ProcessRelationTypes.AUTO_CONTINUE;
import static de.esoco.process.ProcessRelationTypes.CONTINUATION_FRAGMENT_CLASS;
import static org.obrel.filter.RelationFilters.ALL_RELATIONS;
import static org.obrel.type.MetaTypes.ELEMENT_DATATYPE;
import static org.obrel.type.MetaTypes.KEY_DATATYPE;
import static org.obrel.type.MetaTypes.VALUE_DATATYPE;
import static org.obrel.type.StandardTypes.NAME;

/**
 * Defines a process from a sequential list of process steps. The order in the
 * list will be considered as the general order in which the process steps will
 * be performed as long as no other order is defined in the step instances.
 *
 * <p>Some of the methods that add process steps (e.g. {@link #branchTo(Class,
 * RelationType, Predicate) branchTo()}) will enumerate the step name if it
 * occurs multiple times to prevent name clashes. If a process definition needs
 * to refer to such a step's name (e.g. to add a branch to it) it must query the
 * name of the returned step list entry.</p>
 *
 * @author eso
 */
public class StepListProcessDefinition extends ProcessDefinition {

	/**
	 * The prefix for the automatic generation of goto step names
	 */
	public static final String DEFAULT_GOTO_PREFIX = "GoTo";

	/**
	 * The prefix for the automatic generation of branch step names
	 */
	public static final String DEFAULT_BRANCH_PREFIX = "BranchTo";

	private static final long serialVersionUID = 1L;

	private static final Pattern configEntryPattern =
		Pattern.compile("\\s*(.+?)\\s*[:=]\\s*(.*)\\s*");

	/**
	 *
	 */
	private final List<StepListEntry> steps = new ArrayList<>();

	/**
	 *
	 */
	private final Map<String, Integer> stepNameCounts = new HashMap<>();

	/**
	 * Convenience constructor with a variable argument list.
	 *
	 * @see StepListProcessDefinition#StepListProcessDefinition(String, List)
	 */
	public StepListProcessDefinition(String processName,
		StepListEntry... stepList) {
		this(processName, Arrays.asList(stepList));
	}

	/**
	 * Creates a new process definition for a specific process name and a list
	 * of process steps. The sequential order of the steps in the list will be
	 * used as the order in which the steps will be performed, unless a step
	 * already contains a different next step reference. Especially the first
	 * and the last steps in the list will be used as the starting and ending
	 * steps of the process, respectively.
	 *
	 * @param processName The name of the process described by this definition
	 * @param stepList    The sequential list of process steps
	 * @throws IllegalArgumentException If the process step list is empty
	 */
	public StepListProcessDefinition(String processName,
		List<StepListEntry> stepList) {
		this(processName);

		if (stepList == null || stepList.isEmpty()) {
			throw new IllegalArgumentException("Empty process step list");
		}

		steps.addAll(stepList);
	}

	/**
	 * A subclass constructor that creates a process definition that has the
	 * same name as the subclass.
	 */
	protected StepListProcessDefinition() {
		this(null);
	}

	/**
	 * A subclass constructor that creates a new process definition with a
	 * certain name. The process steps must be added later through the method
	 * {@link #invoke(String, Class)}.
	 *
	 * @param processName The name of the process created by this definition or
	 *                    NULL for the class name
	 */
	protected StepListProcessDefinition(String processName) {
		set(NAME,
			processName != null ? processName : getClass().getSimpleName());
	}

	/**
	 * A factory method for a function that throws a {@link ProcessException}.
	 *
	 * @see Functions#error(String, Class)
	 */
	public static <I, O> BinaryFunction<I, String, O> throwProcessException(
		String message) {
		return Functions.error(message, ProcessException.class);
	}

	/**
	 * Returns the process name.
	 *
	 * @return The process
	 */
	@Override
	public String toString() {
		return get(NAME);
	}

	/**
	 * Adds a step for an interactive process step that is based on the step
	 * class {@link Interaction}.
	 *
	 * @param name The name of the interactive step
	 * @return The new step list entry
	 */
	protected StepListEntry addInteraction(String name) {
		return invoke(name, Interaction.class);
	}

	/**
	 * Adds a step for an interactive process step that is based on the step
	 * class {@link Interaction} and that has display parameters as well as
	 * input parameters.
	 *
	 * @param name          The name of the interactive step
	 * @param displayParams The list of display parameter types
	 * @param inputParams   The list of input parameter types
	 * @return The new step list entry
	 */
	protected StepListEntry addInteraction(String name,
		List<RelationType<?>> displayParams,
		List<RelationType<?>> inputParams) {
		StepListEntry step = invoke(name, Interaction.class);

		if (displayParams != null) {
			step.addDisplayParameters(displayParams);
		}

		if (inputParams != null) {
			step.addInputParameters(inputParams);
		}

		return step;
	}

	/**
	 * Convenience method to add a branch to a certain process step that is
	 * identified by it's class.
	 *
	 * @see #branchTo(String, RelationType, Predicate)
	 */
	protected <T> StepListEntry branchTo(
		Class<? extends ProcessFragment> targetStepClass,
		RelationType<T> branchParam, Predicate<? super T> branchCondition) {
		return branchTo(targetStepClass.getSimpleName(), branchParam,
			branchCondition);
	}

	/**
	 * Adds a branch step with a name that is derived from the target step
	 * name.
	 *
	 * @see #branchTo(String, RelationType, Predicate, String)
	 */
	protected <T> StepListEntry branchTo(String targetStep,
		RelationType<T> branchParam, Predicate<? super T> branchCondition) {
		return branchTo(targetStep, branchParam, branchCondition,
			DEFAULT_BRANCH_PREFIX + targetStep);
	}

	/**
	 * Convenience method to add a named branch to a certain process step that
	 * is identified by it's class.
	 *
	 * @see #branchTo(String, RelationType, Predicate, String)
	 */
	protected <T> StepListEntry branchTo(
		Class<? extends ProcessFragment> targetStepClass,
		RelationType<T> branchParam, Predicate<? super T> branchCondition,
		String name) {
		return branchTo(targetStepClass.getSimpleName(), branchParam,
			branchCondition, name);
	}

	/**
	 * Adds a step that branches the process flow. If a process contains
	 * multiple branches to the same target step the names of the branch steps
	 * will be enumerated to ensure uniqueness. If a process definition
	 * needs to
	 * refer to a branch step's name (e.g. for another branch to the step
	 * itself) it should query the name of the returned step list entry.
	 *
	 * @param targetStep      The name of the target step to be executed if the
	 *                        branch condition is TRUE; to end the process it
	 *                        must be {@link Process#PROCESS_END}
	 * @param branchParam     The parameter to be evaluated for the branch
	 * @param branchCondition The predicate to evaluate the branch parameter
	 *                        with
	 * @param name            The name of the step
	 * @return The new step list entry
	 */
	protected <T> StepListEntry branchTo(String targetStep,
		RelationType<T> branchParam, Predicate<? super T> branchCondition,
		String name) {
		return addBranchStep(name, targetStep, branchParam, branchCondition);
	}

	/**
	 * Adds a step to that copies a process parameter to another parameter. A
	 * unique step name will be generated automatically. If an application
	 * needs
	 * to refer to this step by it's name it must query the name of the
	 * returned
	 * step list entry.
	 *
	 * @param sourceParam The source parameter to copy
	 * @param targetParam The target parameter
	 * @return The new step list entry
	 */
	protected <T> StepListEntry copyParam(RelationType<T> sourceParam,
		RelationType<? super T> targetParam) {
		return transferParam(sourceParam, targetParam, false);
	}

	/**
	 * Creates a new process instance that contains the process steps
	 * defined in
	 * the process step list of this process definition.
	 *
	 * @return The new process instance
	 * @throws ProcessException If creating the process fails
	 */
	@Override
	protected Process createProcess() {
		Process process = new Process(get(NAME));
		int max = steps.size() - 1;

		assert max >= 0 : "Empty process definition";

		ObjectRelations.copyRelations(this, process, true);

		for (int i = 0; i <= max; i++) {
			StepListEntry entry = steps.get(i);
			ProcessStep step = entry.createStep();
			String nextStep = entry.nextStep;

			if (nextStep == null) {
				if (i < max) {
					// else use next step in list; if last entry leave it as
					// NULL
					nextStep = steps.get(i + 1).stepName;
				} else {
					nextStep = Process.PROCESS_END;
				}
			}

			step.setNextStep(nextStep);

			// automatically sets the first step as the starting step
			process.addStep(step);
			entry.initStep(step);
		}

		return process;
	}

	/**
	 * Adds a step for an interactive process step that is based on the step
	 * class {@link Interaction} and which has several display parameters.
	 *
	 * @param name          The name of the interactive step
	 * @param displayParams The display parameter types
	 * @return The new step list entry
	 */
	protected StepListEntry display(String name,
		RelationType<?>... displayParams) {
		return addInteraction(name, Arrays.asList(displayParams), null);
	}

	/**
	 * Adds a process step of type {@link DisplayMessage} that displays a
	 * message and an option information string. If the boolean parameter is
	 * true the step will automatically continue to the next step. This can be
	 * used to display a wait message for subsequent steps that take long time.
	 * The message will then be displayed until the next interaction occurs in
	 * the process.
	 *
	 * @param message      The message to display
	 * @param info         The information string or NULL for none
	 * @param autoContinue TRUE to automatically continue to the next step
	 * @return The new step list entry
	 */
	protected StepListEntry displayMessage(String message, String info,
		boolean autoContinue) {
		StepListEntry entry = invoke(DisplayMessage.class);

		entry.set(ProcessRelationTypes.PROCESS_STEP_MESSAGE, message);

		if (info != null) {
			entry.set(ProcessRelationTypes.PROCESS_STEP_INFO, info);
		}

		if (autoContinue) {
			entry.set(AUTO_CONTINUE);
		}

		return entry;
	}

	/**
	 * A variant of {@link #branchTo(String, RelationType, Predicate, String)}
	 * that jumps to the end of the contract if the given condition is true for
	 * the argument parameter.
	 *
	 * @param param     The parameter to be evaluated for the branch
	 * @param condition The predicate to evaluate the branch parameter with
	 * @return The new step list entry
	 */
	protected <T> StepListEntry endProcessIf(RelationType<T> param,
		Predicate<? super T> condition) {
		return branchTo(Process.PROCESS_END, param, condition);
	}

	/**
	 * Convenience method to add a goto to a certain process step that is
	 * identified by it's class.
	 *
	 * @see #goTo(String)
	 */
	protected StepListEntry goTo(
		Class<? extends ProcessFragment> targetStepClass) {
		return goTo(targetStepClass.getSimpleName());
	}

	/**
	 * Adds a step that jumps directly to an arbitrary other process step in
	 * the
	 * same process.
	 *
	 * @param targetStep The target step to branch to
	 * @return The new step list entry
	 */
	protected StepListEntry goTo(String targetStep) {
		return addBranchStep(DEFAULT_GOTO_PREFIX + targetStep, targetStep,
			null,
			Predicates.alwaysTrue());
	}

	/**
	 * Inserts the steps that are needed to handle the continuation parameters
	 * of interaction fragments. If a continuation occurred and it can be
	 * associated with an interaction fragment the corresponding handling
	 * processes will be invoked. Afterwards the process will jump to the given
	 * after-handling process step. If no continuation fragment can be found
	 * the
	 * process will jump to the given no-handling step or, if that is NULL, to
	 * the process end.
	 *
	 * <p>To determine the fragment classes that must be checked against the
	 * current continuation fragment class (which is stored in the parameter
	 * {@link ProcessRelationTypes#CONTINUATION_FRAGMENT_CLASS}) the enclosing
	 * classes of the fragment handling processes will be used. That means that
	 * fragment handling processes must always be defined as a direct inner
	 * class of the fragment they define the continuation handling of.</p>
	 *
	 * @param afterHandlingStep         The process step to be invoked after a
	 *                                  fragment handling process
	 * @param noHandlingStep            The process step to be invoked if no
	 *                                  fragment handling has occurred
	 * @param fragmentHandlingProcesses The list of fragment continuation
	 *                                  handling processes
	 * @return The {@link StepListEntry} of the first continuation handling
	 * step
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	protected final StepListEntry handleFragmentContinuation(
		Class<? extends ProcessFragment> afterHandlingStep,
		Class<? extends ProcessFragment> noHandlingStep,
		Class<? extends ProcessDefinition>... fragmentHandlingProcesses) {
		Map<Class<? extends ProcessFragment>, Object> switchMap =
			new HashMap<>();

		StepListEntry switchStep =
			switchOnParam(CONTINUATION_FRAGMENT_CLASS, switchMap);

		if (noHandlingStep != null) {
			goTo(noHandlingStep);
		} else {
			goTo(Process.PROCESS_END);
		}

		for (Class<? extends ProcessDefinition> fragmentHandlingProcess :
			fragmentHandlingProcesses) {
			StepListEntry subProcess =
				invokeSubProcess(fragmentHandlingProcess);

			goTo(afterHandlingStep);

			Class<?> enclosingClass =
				fragmentHandlingProcess.getEnclosingClass();

			if (ProcessFragment.class.isAssignableFrom(enclosingClass)) {
				Class<? extends ProcessFragment> fragmentClass =
					(Class<? extends ProcessFragment>) enclosingClass;

				switchMap.put(fragmentClass, subProcess.getStepName());
			} else {
				throw new IllegalArgumentException(
					"Not a process fragment: " + enclosingClass);
			}
		}

		return switchStep;
	}

	/**
	 * Adds a conditional parameter initialization function that produces the
	 * value of a certain parameter from the process. The function will only be
	 * evaluated if the given source parameter is not NULL. Therefore the
	 * argument function doesn't need to check the parameter existence by
	 * itself.
	 *
	 * @param sourceParam      The process parameter to evaluate with the
	 *                         initialization function
	 * @param destinationParam The process parameter to store the function
	 *                         result in
	 * @param init             A function that produces the parameter value
	 *                            from
	 *                         the process
	 */
	protected <S, D> void initParam(RelationType<S> sourceParam,
		RelationType<D> destinationParam, Function<? super S, D> init) {
		Function<Relatable, D> initParam =
			Functions.doIf(Predicates.notNull().from(sourceParam),
				init.from(sourceParam));

		get(ProcessRelationTypes.PARAM_INITIALIZATIONS).put(destinationParam,
			initParam);
	}

	/**
	 * Adds an interactive process step that will query the value of certain
	 * parameters with a particular input policy.
	 *
	 * @param name        The name of the interactive step
	 * @param inputParams The input parameter types
	 * @return The new step list entry
	 */
	protected StepListEntry input(String name,
		RelationType<?>... inputParams) {
		return addInteraction(name, null, Arrays.asList(inputParams));
	}

	/**
	 * Adds a step to this process definition that has the same name as the
	 * simple name of the step class.
	 *
	 * @see #invoke(String, Class)
	 */
	protected StepListEntry invoke(
		Class<? extends ProcessFragment> fragmentType) {
		return invoke(fragmentType.getSimpleName(), fragmentType);
	}

	/**
	 * Adds a process step with a specific name to this definition.
	 *
	 * @param name         The name of the step
	 * @param fragmentType The class to create the step from
	 * @return The new step list entry
	 * @throws IllegalArgumentException If the given name has already been used
	 */
	protected StepListEntry invoke(String name,
		Class<? extends ProcessFragment> fragmentType) {
		return addStep(name, fragmentType, false);
	}

	/**
	 * Adds an instance of {@link FunctionStep} for a step that evaluates a
	 * binary function on certain parameters. If multiple steps with the same
	 * name are added the names of the additional steps will be enumerated to
	 * ensure uniqueness.
	 *
	 * @param name        The name of the interactive step
	 * @param leftParam   The type of the parameter that contains the main
	 *                    (left-side) input value for the function evaluation
	 * @param rightParam  The type of the parameter that contains the secondary
	 *                    (right-side) input value for the evaluation of the
	 *                    binary function
	 * @param outputParam The type of the parameter to store the output
	 *                       value of
	 *                    the function evaluation in or NULL to ignore the
	 *                    function output
	 * @param function    The binary function that the step shall evaluate
	 * @return The new step list entry
	 */
	protected <L, R, O> StepListEntry invokeBinaryFunction(String name,
		RelationType<L> leftParam, RelationType<R> rightParam,
		RelationType<O> outputParam,
		BinaryFunction<? super L, ? super R, ? extends O> function) {
		assert leftParam != null || outputParam != null;

		StepListEntry step = addStep(name, FunctionStep.class, true);

		if (leftParam != null) {
			step.set(FunctionStep.FUNCTION_MAIN_INPUT, leftParam);
		}

		if (rightParam != null) {
			step.set(FunctionStep.FUNCTION_SECONDARY_INPUT, rightParam);
		}

		if (outputParam != null) {
			step.set(FunctionStep.FUNCTION_OUTPUT, outputParam);
		}

		step.set(FunctionStep.FUNCTION, function);

		return step;
	}

	/**
	 * Adds an instance of {@link FunctionStep} for a step that evaluates a
	 * function on a certain parameter. If multiple steps with the same name
	 * are
	 * added the names of the additional steps will be enumerated to ensure
	 * uniqueness.
	 *
	 * @param name        The name of the interactive step
	 * @param inputParam  The type of the parameter that contains the input
	 *                    value for the function evaluation or NULL for no
	 *                    input
	 *                    value (for functions that ignore their input)
	 * @param outputParam The type of the parameter to store the output
	 *                       value of
	 *                    the function evaluation in or NULL to ignore the
	 *                    function output
	 * @param function    The function that the step shall evaluate
	 * @return The new step list entry
	 */
	protected <I, O> StepListEntry invokeFunction(String name,
		RelationType<I> inputParam, RelationType<O> outputParam,
		Function<? super I, ? extends O> function) {
		assert inputParam != null || outputParam != null;

		StepListEntry step = addStep(name, FunctionStep.class, true);

		if (inputParam != null) {
			step.set(FunctionStep.FUNCTION_MAIN_INPUT, inputParam);
		}

		if (outputParam != null) {
			step.set(FunctionStep.FUNCTION_OUTPUT, outputParam);
		}

		step.set(FunctionStep.FUNCTION, function);

		return step;
	}

	/**
	 * Adds a step for a sub-process invocation. The name of the step will be
	 * the same as that of the the sub-process.
	 *
	 * @param subProcessClass The class of the sub-process definition
	 * @return The new step list entry
	 */
	protected StepListEntry invokeSubProcess(
		Class<? extends ProcessDefinition> subProcessClass) {
		return invokeSubProcess(null, subProcessClass);
	}

	/**
	 * Adds a step for a sub-process invocation which is based on the class
	 * {@link SubProcessStep}.
	 *
	 * @param name                 The step name or NULL to use the name of the
	 *                             sub-process
	 * @param subProcessDefinition The definition of the sub-process
	 * @return The new step list entry
	 */
	protected StepListEntry invokeSubProcess(String name,
		ProcessDefinition subProcessDefinition) {
		if (name == null) {
			name = subProcessDefinition.get(NAME);
		}

		StepListEntry step = addStep(name, SubProcessStep.class, true);

		step.set(ProcessRelationTypes.SUB_PROCESS_DEFINITION,
			subProcessDefinition);

		return step;
	}

	/**
	 * Adds a step for a sub-process invocation with a specific name. This
	 * method should only be used for sub-process that need a different name
	 * than their definition (e.g. for multiple invocations from the same
	 * parent
	 * process). If this is not the case the {@link #invokeSubProcess(Class)}
	 * method should be used instead.
	 *
	 * @param name            The step name or NULL to use the name of the
	 *                        sub-process
	 * @param subProcessClass The class of the sub-process definition
	 * @return The new step list entry
	 */
	protected StepListEntry invokeSubProcess(String name,
		Class<? extends ProcessDefinition> subProcessClass) {
		return invokeSubProcess(name,
			ProcessManager.getProcessDefinition(subProcessClass));
	}

	/**
	 * Adds a step to that moves a process parameter to another parameter. A
	 * unique step name will be generated automatically. If an application
	 * needs
	 * to refer to this step by it's name it must query the name of the
	 * returned
	 * step list entry.
	 *
	 * @param sourceParam The source parameter to move
	 * @param targetParam The target parameter
	 * @return The new step list entry
	 */
	protected <T> StepListEntry moveParam(RelationType<T> sourceParam,
		RelationType<? super T> targetParam) {
		return transferParam(sourceParam, targetParam, true);
	}

	/**
	 * Adds a function step that sets a parameter to a certain value.
	 *
	 * @param param The parameter to set
	 * @param value The value of the parameter
	 * @return The new step list entry
	 */
	protected <T> StepListEntry setParameter(RelationType<T> param, T value) {
		return invokeFunction("set" + param.getSimpleName(),
			ProcessRelationTypes.PROCESS, null,
			ProcessFunctions.setParameter(param, value));
	}

	/**
	 * Adds a function step that sets a list parameter to certain values.
	 *
	 * @param param  The list parameter to set
	 * @param values The values of the parameter
	 * @return The new step list entry
	 */
	@SuppressWarnings("unchecked")
	protected <T> StepListEntry setParameter(RelationType<List<T>> param,
		T... values) {
		return setParameter(param, Arrays.asList(values));
	}

	/**
	 * Convenience method for a switch that queries the switch targets from a
	 * map based on a key stored in a parameter.
	 *
	 * @see #switchOnParam(RelationType, Function)
	 */
	protected <T> StepListEntry switchOnParam(RelationType<T> switchParam,
		Map<T, Object> switchMap) {
		return switchOnParam(switchParam,
			CollectionFunctions.getMapValueFrom(switchMap));
	}

	/**
	 * Adds a step that switches between different process flows according to
	 * the state of a certain process parameter. See {@link SwitchStep} for
	 * details.
	 *
	 * @param switchParam    The parameter to be evaluated for the switch
	 * @param targetSelector A function that evaluates the value of the switch
	 *                       parameter and returns the name corresponding
	 *                       target
	 *                       step
	 * @return The new step list entry
	 */
	protected <T> StepListEntry switchOnParam(RelationType<T> switchParam,
		Function<? super T, Object> targetSelector) {
		StepListEntry step = addStep("SwitchOn" +
				TextConvert.capitalizedIdentifier(switchParam.getSimpleName()),
			SwitchStep.class, true);

		step.set(SwitchStep.SWITCH_PARAM, switchParam);
		step.set(SwitchStep.SWITCH_TARGET_SELECTOR, targetSelector);

		return step;
	}

	/**
	 * Adds a step that branches the process flow. If a process contains
	 * multiple branches to the same target step the names of the branch steps
	 * will be enumerated to ensure uniqueness.
	 *
	 * @param name            The name of the step
	 * @param targetStep      The name of the target step to be executed if the
	 *                        branch condition is TRUE
	 * @param branchParam     The parameter to be evaluated for the branch
	 * @param branchCondition The predicate to evaluate the branch parameter
	 *                        with
	 * @return The new step list entry
	 */
	private <T> StepListEntry addBranchStep(String name, String targetStep,
		RelationType<T> branchParam, Predicate<? super T> branchCondition) {
		StepListEntry step = addStep(name, BranchStep.class, true);

		step.set(BranchStep.BRANCH_PARAM, branchParam);
		step.set(BranchStep.BRANCH_CONDITION, branchCondition);
		step.set(BranchStep.BRANCH_TARGET, targetStep);

		return step;
	}

	/**
	 * Adds a process step with a specific name to this definition. If the
	 * given
	 * name is not unique in the process and the boolean parameter is TRUE an
	 * increasing number will be appended to it each time it is used to ensure
	 * uniqueness.
	 *
	 * @param name             The name of the step
	 * @param framgmentType    The class to create the step from
	 * @param createUniqueName TRUE to create a unique step name if the given
	 *                         name has already been used
	 * @return The new step list entry
	 */
	@SuppressWarnings("boxing")
	private StepListEntry addStep(String name,
		Class<? extends ProcessFragment> framgmentType,
		boolean createUniqueName) {
		int stepCount = 1;

		if (stepNameCounts.containsKey(name)) {
			if (createUniqueName) {
				stepCount = stepNameCounts.get(name).intValue() + 1;
			} else {
				throw new IllegalArgumentException(
					"Duplicate process step name: " + name);
			}
		}

		stepNameCounts.put(name, stepCount);

		if (stepCount > 1) {
			name = name + stepCount;
		}

		StepListEntry step = new StepListEntry(name, framgmentType);

		steps.add(step);

		return step;
	}

	/**
	 * Internal method to add a step to that copies or moves a process
	 * parameter. See {@link TransferParam} for details.
	 *
	 * @param sourceParam The source parameter to transfer
	 * @param targetParam The target parameter
	 * @param move        TRUE to move, FALSE to copy
	 * @return The new step list entry
	 */
	@SuppressWarnings("boxing")
	private <T> StepListEntry transferParam(RelationType<T> sourceParam,
		RelationType<? super T> targetParam, boolean move) {
		String name = move ? "MOVE_" : "COPY_";

		name +=
			sourceParam.getSimpleName() + "_TO_" + targetParam.getSimpleName();

		StepListEntry transferStep = addStep(name, TransferParam.class, true);

		transferStep.set(ProcessRelationTypes.SOURCE_PARAM, sourceParam);
		transferStep.set(ProcessRelationTypes.TARGET_PARAM, targetParam);
		transferStep.set(TransferParam.TRANSFER_PARAM_MOVE, move);

		return transferStep;
	}

	/**
	 * Inner class that defines the structure of the step list elements.
	 */
	public static class StepListEntry extends ProcessElement {

		private static final long serialVersionUID = 1L;

		/**
		 *
		 */
		private final Class<? extends ProcessFragment> stepClass;

		/**
		 *
		 */
		private final String stepName;

		/**
		 *
		 */
		private String nextStep = null;

		/**
		 * Creates a new StepListEntry for a named step with a specific
		 * follow-up step.
		 *
		 * @param stepName  The name of the step
		 * @param stepClass The class to create the step from
		 */
		public StepListEntry(String stepName,
			Class<? extends ProcessFragment> stepClass) {
			this(stepName, stepClass, (Map<?, ?>) null);
		}

		/**
		 * Creates a new StepListEntry for a named step with a specific
		 * follow-up step and a configuration map for the step.
		 *
		 * @param stepName      The name of the step
		 * @param stepClass     The class to create the step from
		 * @param configEntries The configuration for the step
		 */
		public StepListEntry(String stepName,
			Class<? extends ProcessFragment> stepClass,
			String... configEntries) {
			this(stepName, stepClass, (Map<?, ?>) null);

			if (configEntries != null) {
				parseConfiguration(configEntries);
			}
		}

		/**
		 * Creates a new StepListEntry for a named step with a specific
		 * follow-up step and a configuration map for the step.
		 *
		 * <p>If the step class has been defined by a class literal (i.e. as
		 * &lt;StepName&gt;.class) in the call to this constructor the class
		 * may
		 * not have been loaded yet and any parameter types defined in it
		 * haven't been created yet too. Therefore this constructor loads the
		 * step class before parsing the configuration so that any parameter
		 * relation types referenced there are available. There's still a
		 * chance
		 * that the parsing fails if the configuration references a parameter
		 * type that is defined in a step referenced only by a step list entry
		 * that is created later.</p>
		 *
		 * @param stepName  The name of the step
		 * @param stepClass The class to create the step from
		 * @param config    The configuration for the step
		 */
		public StepListEntry(String stepName,
			Class<? extends ProcessFragment> stepClass, Map<?, ?> config) {
			this.stepName = stepName;
			this.stepClass = stepClass;

			RelationTypes.init(stepClass);

			// CHECK: check if lazy parsing is needed to avoid missing
			// parameter
			// types because of not yet loaded step classes
			if (config != null) {
				parseConfiguration(config);
			}
		}

		/**
		 * Returns the name of the next step that will be executed after this
		 * one.
		 *
		 * @return The name of the next step
		 */
		public final String getNextStep() {
			return nextStep;
		}

		/**
		 * Returns the name of the process step.
		 *
		 * @return The stepName value
		 */
		public final String getStepName() {
			return stepName;
		}

		/**
		 * Convenience method to end the process after the current step has
		 * been
		 * executed.
		 *
		 * @return This entry to allow further method invocations
		 */
		public StepListEntry thenEndProcess() {
			thenGoTo(Process.PROCESS_END);

			return this;
		}

		/**
		 * Sets the name of the next step to be executed after this one.
		 *
		 * @param nextStep The name of the next step
		 */
		public final void thenGoTo(String nextStep) {
			this.nextStep = nextStep;
		}

		/**
		 * Sets the class of the next step to be executed after this one.
		 *
		 * @param nextStep The class of the next step
		 */
		public final void thenGoTo(Class<? extends ProcessFragment> nextStep) {
			thenGoTo(nextStep.getSimpleName());
		}

		/**
		 * Adds a configuration entry from the corresponding key and value
		 * elements.
		 *
		 * @param key   The key
		 * @param value The value
		 */
		@SuppressWarnings("unchecked")
		void addConfigurationEntry(Object key, Object value) {
			RelationType<?> paramType = null;

			if (key instanceof RelationType<?>) {
				paramType = (RelationType<?>) key;
			} else if (key instanceof String) {
				paramType = RelationType.valueOf((String) key);
			}

			if (paramType == null) {
				throw new IllegalArgumentException("No relation type: " + key);
			}

			Class<?> targetType = paramType.getTargetType();

			if (Collection.class.isAssignableFrom(targetType)) {
				setCollectionParam(paramType, value);
			} else if (Map.class.isAssignableFrom(targetType)) {
				setMapParam(paramType, value);
			} else {
				value = convertParamValue(paramType.getTargetType(), value);
				set((RelationType<Object>) paramType, value);
			}
		}

		/**
		 * Applies the configuration to a new process step instance. The
		 * configuration consists of all relations that are set on this entry.
		 *
		 * @param step The step to apply the configuration to
		 */
		void applyConfiguration(ProcessElement step) {
			// copy single relations to preserve existing annotations set by
			// the process step
			for (Relation<?> source : getRelations(ALL_RELATIONS)) {
				@SuppressWarnings("unchecked")
				RelationType<Object> type =
					(RelationType<Object>) source.getType();

				// first set value which creates the relation if necessary
				Relation<?> target = step.set(type, source.getTarget());

				// then copy the annotations (meta-relations)
				ObjectRelations.copyRelations(source, target, true);
			}
		}

		/**
		 * Checks and if necessary converts the raw configuration value for a
		 * parameter according to the given target datatype. If the value can
		 * neither be assigned nor converted to the target datatype an
		 * exception
		 * will be thrown.
		 *
		 * @param targetType The target datatype
		 * @param value      The value to be checked and converted
		 * @return The parameter value, converted if necessary
		 * @throws IllegalArgumentException If the value can neither be
		 * assigned
		 *                                  nor converted to the target
		 *                                  datatype
		 */
		Object convertParamValue(Class<?> targetType, Object value) {
			if (RelationType.class.isAssignableFrom(targetType) &&
				value instanceof String) {
				value = RelationType.valueOf((String) value);
			} else if (!targetType.isAssignableFrom(value.getClass())) {
				throw new IllegalArgumentException(
					"Incompatible value datatype " + value.getClass() +
						" for parameter " + targetType);
			}

			return value;
		}

		/**
		 * Internal method to create a new ProcessStep instance from a
		 * StepListEntry.
		 *
		 * @return The new process step
		 * @throws ProcessException If creating the instance fails
		 */
		ProcessStep createStep() throws ProcessException {
			try {
				ProcessFragment fragment = stepClass.newInstance();
				ProcessStep step;

				if (fragment instanceof InteractionFragment) {
					step =
						new FragmentInteraction((InteractionFragment) fragment);
				} else {
					step = (ProcessStep) fragment;
				}

				step.setName(stepName);

				return step;
			} catch (Exception e) {
				throw new ProcessException(null,
					String.format("Creation of process step %s failed",
						stepClass.getSimpleName()), e);
			}
		}

		/**
		 * Initializes a process step. The step instance must have been
		 * previously created by this entry.
		 *
		 * @param step The step to be initialized
		 * @throws ProcessException If initializing the step fails
		 */
		void initStep(ProcessStep step) throws ProcessException {
			assert stepName == step.getName();

			applyConfiguration(step);
			step.setup();
		}

		/**
		 * Creates a new step configuration from a map containing the
		 * configuration entries.
		 *
		 * @param config A map containing the configuration entries
		 */
		void parseConfiguration(Map<?, ?> config) {
			for (Map.Entry<?, ?> entry : config.entrySet()) {
				addConfigurationEntry(entry.getKey(), entry.getValue());
			}
		}

		/**
		 * Creates a new step configuration from a list of map entry strings.
		 *
		 * @param config A string array containing the raw configuration
		 *               entries
		 */
		void parseConfiguration(String[] config) {
			for (String configEntry : config) {
				Pair<Object, Object> entry =
					parseConfigurationEntry(configEntry);

				addConfigurationEntry(entry.first(), entry.second());
			}
		}

		/**
		 * Parses a configuration entry from a string.
		 *
		 * @param entry The string defining the configuration entry
		 * @return A pair containing the key and value parts of the entry
		 * @throws IllegalArgumentException If the argument is not a valid
		 *                                  configuration entry
		 */
		Pair<Object, Object> parseConfigurationEntry(String entry) {
			Matcher matcher = configEntryPattern.matcher(entry);

			if (!matcher.matches()) {
				throw new IllegalArgumentException(
					"Invalid configuration entry: " + entry);
			}

			return new Pair<Object, Object>(
				TextUtil.parseObject(matcher.group(1)),
				TextUtil.parseObject(matcher.group(2)));
		}

		/**
		 * Sets a parameter that resolves to a collection instance. The value
		 * argument can either be a collection or a single value. In the first
		 * case all elements of the value collection will be added to the
		 * parameter collection. A single value will be converted to the
		 * correct
		 * element datatype with {@link #convertParamValue(Class, Object)} and
		 * then added to the parameter collection.
		 *
		 * @param type  The parameter type to set the collection for
		 * @param value The parameter value
		 * @throws IllegalArgumentException If the value argument is not valid
		 *                                  for the parameter's collection
		 */
		@SuppressWarnings("unchecked")
		void setCollectionParam(RelationType<?> type, Object value) {
			Collection<Object> collection = (Collection<Object>) get(type);

			if (Collection.class.isAssignableFrom(value.getClass())) {
				collection.addAll((Collection<Object>) value);
			} else {
				collection.add(
					convertParamValue(type.get(ELEMENT_DATATYPE), value));
			}
		}

		/**
		 * Sets a parameter that resolves to a map instance. The value argument
		 * can either be a map or a single value. In the first case all
		 * elements
		 * of the value map will be added to the parameter map. A single value
		 * can be either a {@link Pair} containing key and value or a string.
		 * Strings will be parsed with {@link #parseConfigurationEntry(String)}
		 * to split them into a key/value pair.
		 *
		 * @param type  The parameter type to set the map for
		 * @param value The parameter value
		 * @throws IllegalArgumentException If the value argument is not valid
		 *                                  for the parameter's map
		 */
		@SuppressWarnings("unchecked")
		void setMapParam(RelationType<?> type, Object value) {
			Map<Object, Object> map = (Map<Object, Object>) get(type);

			if (Map.class.isAssignableFrom(value.getClass())) {
				map.putAll((Map<?, ?>) value);
			} else {
				Pair<Object, Object> mapEntry;

				if (value instanceof Pair<?, ?>) {
					mapEntry = (Pair<Object, Object>) value;
				} else if (value instanceof String) {
					mapEntry = parseConfigurationEntry((String) value);
				} else {
					throw new IllegalArgumentException(
						"Invalid map type config entry: " + value);
				}

				Object key =
					convertParamValue(type.get(KEY_DATATYPE),
						mapEntry.first());

				value = convertParamValue(type.get(VALUE_DATATYPE),
					mapEntry.second());

				map.put(key, value);
			}
		}
	}
}
