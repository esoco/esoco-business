//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.StandardTypes;

import static de.esoco.process.ProcessRelationTypes.AUTO_CONTINUE;
import static de.esoco.process.ProcessRelationTypes.CONTINUATION_FRAGMENT_CLASS;

import static org.obrel.filter.RelationFilters.ALL_RELATIONS;
import static org.obrel.type.MetaTypes.ELEMENT_DATATYPE;
import static org.obrel.type.MetaTypes.KEY_DATATYPE;
import static org.obrel.type.MetaTypes.VALUE_DATATYPE;
import static org.obrel.type.StandardTypes.NAME;


/********************************************************************
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
public class StepListProcessDefinition extends ProcessDefinition
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	private static Pattern aConfigEntryPattern =
		Pattern.compile("\\s*(.+?)\\s*[:=]\\s*(.*)\\s*");

	/** The prefix for the automatic generation of goto step names */
	public static final String DEFAULT_GOTO_PREFIX = "GoTo";

	/** The prefix for the automatic generation of branch step names */
	public static final String DEFAULT_BRANCH_PREFIX = "BranchTo";

	//~ Instance fields --------------------------------------------------------

	/** @serial The list of process steps */
	private List<StepListEntry> aSteps = new ArrayList<StepListEntry>();

	/** @serial The set of unique process step names */
	private Map<String, Integer> aStepNameCounts =
		new HashMap<String, Integer>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Convenience constructor with a variable argument list.
	 *
	 * @see StepListProcessDefinition#StepListProcessDefinition(String, List)
	 */
	public StepListProcessDefinition(
		String			 sProcessName,
		StepListEntry... rStepList)
	{
		this(sProcessName, Arrays.asList(rStepList));
	}

	/***************************************
	 * Creates a new process definition for a specific process name and a list
	 * of process steps. The sequential order of the steps in the list will be
	 * used as the order in which the steps will be performed, unless a step
	 * already contains a different next step reference. Especially the first
	 * and the last steps in the list will be used as the starting and ending
	 * steps of the process, respectively.
	 *
	 * @param  sProcessName The name of the process described by this definition
	 * @param  rStepList    The sequential list of process steps
	 *
	 * @throws IllegalArgumentException If the process step list is empty
	 */
	public StepListProcessDefinition(
		String				sProcessName,
		List<StepListEntry> rStepList)
	{
		this(sProcessName);

		if ((rStepList == null) || (rStepList.size() == 0))
		{
			throw new IllegalArgumentException("Empty process step list");
		}

		aSteps.addAll(rStepList);
	}

	/***************************************
	 * A subclass constructor that creates a process definition that has the
	 * same name as the subclass.
	 */
	protected StepListProcessDefinition()
	{
		this(null);
	}

	/***************************************
	 * A subclass constructor that creates a new process definition with a
	 * certain name. The process steps must be added later through the method
	 * {@link #invoke(String, Class)}.
	 *
	 * @param sProcessName The name of the process created by this definition or
	 *                     NULL for the class name
	 */
	protected StepListProcessDefinition(String sProcessName)
	{
		set(NAME,
			sProcessName != null ? sProcessName : getClass().getSimpleName());
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * A factory method for a function that throws a {@link ProcessException}.
	 *
	 * @see Functions#error(String, Class)
	 */
	public static <I, O> BinaryFunction<I, String, O> throwProcessException(
		String sMessage)
	{
		return Functions.error(sMessage, ProcessException.class);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the process name.
	 *
	 * @return The process
	 */
	@Override
	public String toString()
	{
		return get(StandardTypes.NAME);
	}

	/***************************************
	 * Adds a step for an interactive process step that is based on the step
	 * class {@link Interaction}.
	 *
	 * @param  sName The name of the interactive step
	 *
	 * @return The new step list entry
	 */
	protected StepListEntry addInteraction(String sName)
	{
		return invoke(sName, Interaction.class);
	}

	/***************************************
	 * Adds a step for an interactive process step that is based on the step
	 * class {@link Interaction} and that has display parameters as well as
	 * input parameters.
	 *
	 * @param  sName          The name of the interactive step
	 * @param  rDisplayParams The list of display parameter types
	 * @param  rInputParams   The list of input parameter types
	 *
	 * @return The new step list entry
	 */
	protected StepListEntry addInteraction(String				 sName,
										   List<RelationType<?>> rDisplayParams,
										   List<RelationType<?>> rInputParams)
	{
		StepListEntry aStep = invoke(sName, Interaction.class);

		if (rDisplayParams != null)
		{
			aStep.addDisplayParameters(rDisplayParams);
		}

		if (rInputParams != null)
		{
			aStep.addInputParameters(rInputParams);
		}

		return aStep;
	}

	/***************************************
	 * Convenience method to add a branch to a certain process step that is
	 * identified by it's class.
	 *
	 * @see #branchTo(String, RelationType, Predicate)
	 */
	protected <T> StepListEntry branchTo(
		Class<? extends ProcessFragment> rTargetStepClass,
		RelationType<T>					 rBranchParam,
		Predicate<? super T>			 pBranchCondition)
	{
		return branchTo(rTargetStepClass.getSimpleName(),
						rBranchParam,
						pBranchCondition);
	}

	/***************************************
	 * Adds a branch step with a name that is derived from the target step name.
	 *
	 * @see #branchTo(String, RelationType, Predicate, String)
	 */
	protected <T> StepListEntry branchTo(String				  sTargetStep,
										 RelationType<T>	  rBranchParam,
										 Predicate<? super T> pBranchCondition)
	{
		return branchTo(sTargetStep,
						rBranchParam,
						pBranchCondition,
						DEFAULT_BRANCH_PREFIX + sTargetStep);
	}

	/***************************************
	 * Convenience method to add a named branch to a certain process step that
	 * is identified by it's class.
	 *
	 * @see #branchTo(String, RelationType, Predicate, String)
	 */
	protected <T> StepListEntry branchTo(
		Class<? extends ProcessFragment> rTargetStepClass,
		RelationType<T>					 rBranchParam,
		Predicate<? super T>			 pBranchCondition,
		String							 sName)
	{
		return branchTo(rTargetStepClass.getSimpleName(),
						rBranchParam,
						pBranchCondition,
						sName);
	}

	/***************************************
	 * Adds a step that branches the process flow. If a process contains
	 * multiple branches to the same target step the names of the branch steps
	 * will be enumerated to ensure uniqueness. If a process definition needs to
	 * refer to a branch step's name (e.g. for another branch to the step
	 * itself) it should query the name of the returned step list entry.
	 *
	 * @param  sTargetStep      The name of the target step to be executed if
	 *                          the branch condition is TRUE; to end the process
	 *                          it must be {@link Process#PROCESS_END}
	 * @param  rBranchParam     The parameter to be evaluated for the branch
	 * @param  pBranchCondition The predicate to evaluate the branch parameter
	 *                          with
	 * @param  sName            The name of the step
	 *
	 * @return The new step list entry
	 */
	protected <T> StepListEntry branchTo(String				  sTargetStep,
										 RelationType<T>	  rBranchParam,
										 Predicate<? super T> pBranchCondition,
										 String				  sName)
	{
		return addBranchStep(sName,
							 sTargetStep,
							 rBranchParam,
							 pBranchCondition);
	}

	/***************************************
	 * Adds a step to that copies a process parameter to another parameter. A
	 * unique step name will be generated automatically. If an application needs
	 * to refer to this step by it's name it must query the name of the returned
	 * step list entry.
	 *
	 * @param  rSourceParam The source parameter to copy
	 * @param  rTargetParam The target parameter
	 *
	 * @return The new step list entry
	 */
	protected <T> StepListEntry copyParam(
		RelationType<T>			rSourceParam,
		RelationType<? super T> rTargetParam)
	{
		return transferParam(rSourceParam, rTargetParam, false);
	}

	/***************************************
	 * Creates a new process instance that contains the process steps defined in
	 * the process step list of this process definition.
	 *
	 * @return The new process instance
	 *
	 * @throws ProcessException If creating the process fails
	 */
	@Override
	protected Process createProcess() throws ProcessException
	{
		Process aProcess = new Process(get(StandardTypes.NAME));
		int     nMax     = aSteps.size() - 1;

		assert nMax >= 0 : "Empty process definition";

		ObjectRelations.copyRelations(this, aProcess, true);

		for (int i = 0; i <= nMax; i++)
		{
			StepListEntry rEntry    = aSteps.get(i);
			ProcessStep   aStep     = rEntry.createStep();
			String		  sNextStep = rEntry.sNextStep;

			if (sNextStep == null)
			{
				if (i < nMax)
				{
					// else use next step in list; if last entry leave it as NULL
					sNextStep = aSteps.get(i + 1).sStepName;
				}
				else
				{
					sNextStep = Process.PROCESS_END;
				}
			}

			aStep.setNextStep(sNextStep);

			// automatically sets the first step as the starting step
			aProcess.addStep(aStep);
			rEntry.initStep(aStep);
		}

		return aProcess;
	}

	/***************************************
	 * Adds a step for an interactive process step that is based on the step
	 * class {@link Interaction} and which has several display parameters.
	 *
	 * @param  sName          The name of the interactive step
	 * @param  rDisplayParams The display parameter types
	 *
	 * @return The new step list entry
	 */
	protected StepListEntry display(
		String			   sName,
		RelationType<?>... rDisplayParams)
	{
		return addInteraction(sName, Arrays.asList(rDisplayParams), null);
	}

	/***************************************
	 * Adds a process step of type {@link DisplayMessage} that displays a
	 * message and an option information string. If the boolean parameter is
	 * true the step will automatically continue to the next step. This can be
	 * used to display a wait message for subsequent steps that take long time.
	 * The message will then be displayed until the next interaction occurs in
	 * the process.
	 *
	 * @param  sMessage      The message to display
	 * @param  sInfo         The information string or NULL for none
	 * @param  bAutoContinue TRUE to automatically continue to the next step
	 *
	 * @return The new step list entry
	 */
	protected StepListEntry displayMessage(String  sMessage,
										   String  sInfo,
										   boolean bAutoContinue)
	{
		StepListEntry rEntry = invoke(DisplayMessage.class);

		rEntry.set(ProcessRelationTypes.PROCESS_STEP_MESSAGE, sMessage);

		if (sInfo != null)
		{
			rEntry.set(ProcessRelationTypes.PROCESS_STEP_INFO, sInfo);
		}

		if (bAutoContinue)
		{
			rEntry.set(AUTO_CONTINUE);
		}

		return rEntry;
	}

	/***************************************
	 * A variant of {@link #branchTo(String, RelationType, Predicate, String)}
	 * that jumps to the end of the contract if the given condition is true for
	 * the argument parameter.
	 *
	 * @param  rParam     The parameter to be evaluated for the branch
	 * @param  pCondition The predicate to evaluate the branch parameter with
	 *
	 * @return The new step list entry
	 */
	protected <T> StepListEntry endProcessIf(
		RelationType<T>		 rParam,
		Predicate<? super T> pCondition)
	{
		return branchTo(Process.PROCESS_END, rParam, pCondition);
	}

	/***************************************
	 * Convenience method to add a goto to a certain process step that is
	 * identified by it's class.
	 *
	 * @see #goTo(String)
	 */
	protected StepListEntry goTo(
		Class<? extends ProcessFragment> rTargetStepClass)
	{
		return goTo(rTargetStepClass.getSimpleName());
	}

	/***************************************
	 * Adds a step that jumps directly to an arbitrary other process step in the
	 * same process.
	 *
	 * @param  sTargetStep The target step to branch to
	 *
	 * @return The new step list entry
	 */
	protected StepListEntry goTo(String sTargetStep)
	{
		return addBranchStep(DEFAULT_GOTO_PREFIX + sTargetStep,
							 sTargetStep,
							 null,
							 Predicates.alwaysTrue());
	}

	/***************************************
	 * Inserts the steps that are needed to handle the continuation parameters
	 * of interaction fragments. If a continuation occurred and it can be
	 * associated with an interaction fragment the corresponding handling
	 * processes will be invoked. Afterwards the process will jump to the given
	 * after-handling process step. If no continuation fragment can be found the
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
	 * @param  rAfterHandlingStep         The process step to be invoked after a
	 *                                    fragment handling process
	 * @param  rNoHandlingStep            The process step to be invoked if no
	 *                                    fragment handling has occurred
	 * @param  rFragmentHandlingProcesses The list of fragment continuation
	 *                                    handling processes
	 *
	 * @return The {@link StepListEntry} of the first continuation handling step
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	protected final StepListEntry handleFragmentContinuation(
		Class<? extends ProcessFragment>	  rAfterHandlingStep,
		Class<? extends ProcessFragment>	  rNoHandlingStep,
		Class<? extends ProcessDefinition>... rFragmentHandlingProcesses)
	{
		Map<Class<? extends ProcessFragment>, Object> aSwitchMap =
			new HashMap<Class<? extends ProcessFragment>, Object>();

		StepListEntry aSwitchStep =
			switchOnParam(CONTINUATION_FRAGMENT_CLASS, aSwitchMap);

		if (rNoHandlingStep != null)
		{
			goTo(rNoHandlingStep);
		}
		else
		{
			goTo(Process.PROCESS_END);
		}

		for (Class<? extends ProcessDefinition> rFragmentHandlingProcess :
			 rFragmentHandlingProcesses)
		{
			StepListEntry aSubProcess =
				invokeSubProcess(rFragmentHandlingProcess);

			goTo(rAfterHandlingStep);

			Class<?> rEnclosingClass =
				rFragmentHandlingProcess.getEnclosingClass();

			if (ProcessFragment.class.isAssignableFrom(rEnclosingClass))
			{
				Class<? extends ProcessFragment> rFragmentClass =
					(Class<? extends ProcessFragment>) rEnclosingClass;

				aSwitchMap.put(rFragmentClass, aSubProcess.getStepName());
			}
			else
			{
				throw new IllegalArgumentException("Not a process fragment: " +
												   rEnclosingClass);
			}
		}

		return aSwitchStep;
	}

	/***************************************
	 * Adds a conditional parameter initialization function that produces the
	 * value of a certain parameter from the process. The function will only be
	 * evaluated if the given source parameter is not NULL. Therefore the
	 * argument function doesn't need to check the parameter existence by
	 * itself.
	 *
	 * @param rSourceParam      The process parameter to evaluate with the
	 *                          initialization function
	 * @param rDestinationParam The process parameter to store the function
	 *                          result in
	 * @param fInit             A function that produces the parameter value
	 *                          from the process
	 */
	protected <S, D> void initParam(RelationType<S>		   rSourceParam,
									RelationType<D>		   rDestinationParam,
									Function<? super S, D> fInit)
	{
		Function<Relatable, D> fInitParam =
			Functions.doIf(Predicates.notNull().from(rSourceParam),
						   fInit.from(rSourceParam));

		get(ProcessRelationTypes.PARAM_INITIALIZATIONS).put(rDestinationParam,
															fInitParam);
	}

	/***************************************
	 * Adds an interactive process step that will query the value of certain
	 * parameters with a particular input policy.
	 *
	 * @param  sName        The name of the interactive step
	 * @param  rInputParams The input parameter types
	 *
	 * @return The new step list entry
	 */
	protected StepListEntry input(String			 sName,
								  RelationType<?>... rInputParams)
	{
		return addInteraction(sName, null, Arrays.asList(rInputParams));
	}

	/***************************************
	 * Adds a step to this process definition that has the same name as the
	 * simple name of the step class.
	 *
	 * @see #invoke(String, Class)
	 */
	protected StepListEntry invoke(Class<? extends ProcessFragment> rClass)
	{
		return invoke(rClass.getSimpleName(), rClass);
	}

	/***************************************
	 * Adds a process step with a specific name to this definition.
	 *
	 * @param  sName  The name of the step
	 * @param  rClass The class to create the step from
	 *
	 * @return The new step list entry
	 *
	 * @throws IllegalArgumentException If the given name has already been used
	 */
	protected StepListEntry invoke(
		String							 sName,
		Class<? extends ProcessFragment> rClass)
	{
		return addStep(sName, rClass, false);
	}

	/***************************************
	 * Adds an instance of {@link FunctionStep} for a step that evaluates a
	 * binary function on certain parameters. If multiple steps with the same
	 * name are added the names of the additional steps will be enumerated to
	 * ensure uniqueness.
	 *
	 * @param  sName        The name of the interactive step
	 * @param  rLeftParam   The type of the parameter that contains the main
	 *                      (left-side) input value for the function evaluation
	 * @param  rRightParam  The type of the parameter that contains the
	 *                      secondary (right-side) input value for the
	 *                      evaluation of the binary function
	 * @param  rOutputParam The type of the parameter to store the output value
	 *                      of the function evaluation in or NULL to ignore the
	 *                      function output
	 * @param  rFunction    The binary function that the step shall evaluate
	 *
	 * @return The new step list entry
	 */
	protected <L, R, O> StepListEntry invokeBinaryFunction(
		String											  sName,
		RelationType<L>									  rLeftParam,
		RelationType<R>									  rRightParam,
		RelationType<O>									  rOutputParam,
		BinaryFunction<? super L, ? super R, ? extends O> rFunction)
	{
		assert rLeftParam != null || rOutputParam != null;

		StepListEntry aStep = addStep(sName, FunctionStep.class, true);

		if (rLeftParam != null)
		{
			aStep.set(FunctionStep.FUNCTION_MAIN_INPUT, rLeftParam);
		}

		if (rRightParam != null)
		{
			aStep.set(FunctionStep.FUNCTION_SECONDARY_INPUT, rRightParam);
		}

		if (rOutputParam != null)
		{
			aStep.set(FunctionStep.FUNCTION_OUTPUT, rOutputParam);
		}

		aStep.set(FunctionStep.FUNCTION, rFunction);

		return aStep;
	}

	/***************************************
	 * Adds an instance of {@link FunctionStep} for a step that evaluates a
	 * function on a certain parameter. If multiple steps with the same name are
	 * added the names of the additional steps will be enumerated to ensure
	 * uniqueness.
	 *
	 * @param  sName        The name of the interactive step
	 * @param  rInputParam  The type of the parameter that contains the input
	 *                      value for the function evaluation or NULL for no
	 *                      input value (for functions that ignore their input)
	 * @param  rOutputParam The type of the parameter to store the output value
	 *                      of the function evaluation in or NULL to ignore the
	 *                      function output
	 * @param  rFunction    The function that the step shall evaluate
	 *
	 * @return The new step list entry
	 */
	protected <I, O> StepListEntry invokeFunction(
		String							 sName,
		RelationType<I>					 rInputParam,
		RelationType<O>					 rOutputParam,
		Function<? super I, ? extends O> rFunction)
	{
		assert rInputParam != null || rOutputParam != null;

		StepListEntry aStep = addStep(sName, FunctionStep.class, true);

		if (rInputParam != null)
		{
			aStep.set(FunctionStep.FUNCTION_MAIN_INPUT, rInputParam);
		}

		if (rOutputParam != null)
		{
			aStep.set(FunctionStep.FUNCTION_OUTPUT, rOutputParam);
		}

		aStep.set(FunctionStep.FUNCTION, rFunction);

		return aStep;
	}

	/***************************************
	 * Adds a step for a sub-process invocation. The name of the step will be
	 * the same as that of the the sub-process.
	 *
	 * @param  rSubProcessClass The class of the sub-process definition
	 *
	 * @return The new step list entry
	 */
	protected StepListEntry invokeSubProcess(
		Class<? extends ProcessDefinition> rSubProcessClass)
	{
		return invokeSubProcess(null, rSubProcessClass);
	}

	/***************************************
	 * Adds a step for a sub-process invocation which is based on the class
	 * {@link SubProcessStep}.
	 *
	 * @param  sName                 The step name or NULL to use the name of
	 *                               the sub-process
	 * @param  rSubProcessDefinition The definition of the sub-process
	 *
	 * @return The new step list entry
	 */
	protected StepListEntry invokeSubProcess(
		String			  sName,
		ProcessDefinition rSubProcessDefinition)
	{
		if (sName == null)
		{
			sName = rSubProcessDefinition.get(NAME);
		}

		StepListEntry aStep = addStep(sName, SubProcessStep.class, true);

		aStep.set(ProcessRelationTypes.SUB_PROCESS_DEFINITION,
				  rSubProcessDefinition);

		return aStep;
	}

	/***************************************
	 * Adds a step for a sub-process invocation with a specific name. This
	 * method should only be used for sub-process that need a different name
	 * than their definition (e.g. for multiple invocations from the same parent
	 * process). If this is not the case the {@link #invokeSubProcess(Class)}
	 * method should be used instead.
	 *
	 * @param  sName            The step name or NULL to use the name of the
	 *                          sub-process
	 * @param  rSubProcessClass The class of the sub-process definition
	 *
	 * @return The new step list entry
	 */
	protected StepListEntry invokeSubProcess(
		String							   sName,
		Class<? extends ProcessDefinition> rSubProcessClass)
	{
		return invokeSubProcess(sName,
								ProcessManager.getProcessDefinition(rSubProcessClass));
	}

	/***************************************
	 * Adds a step to that moves a process parameter to another parameter. A
	 * unique step name will be generated automatically. If an application needs
	 * to refer to this step by it's name it must query the name of the returned
	 * step list entry.
	 *
	 * @param  rSourceParam The source parameter to move
	 * @param  rTargetParam The target parameter
	 *
	 * @return The new step list entry
	 */
	protected <T> StepListEntry moveParam(
		RelationType<T>			rSourceParam,
		RelationType<? super T> rTargetParam)
	{
		return transferParam(rSourceParam, rTargetParam, true);
	}

	/***************************************
	 * Adds a function step that sets a parameter to a certain value.
	 *
	 * @param  rParam The parameter to set
	 * @param  rValue The value of the parameter
	 *
	 * @return The new step list entry
	 */
	protected <T> StepListEntry setParameter(RelationType<T> rParam, T rValue)
	{
		return invokeFunction("set" + rParam.getSimpleName(),
							  ProcessRelationTypes.PROCESS,
							  null,
							  ProcessFunctions.setParameter(rParam, rValue));
	}

	/***************************************
	 * Adds a function step that sets a list parameter to certain values.
	 *
	 * @param  rParam  The list parameter to set
	 * @param  rValues The values of the parameter
	 *
	 * @return The new step list entry
	 */
	@SuppressWarnings("unchecked")
	protected <T> StepListEntry setParameter(
		RelationType<List<T>> rParam,
		T... 				  rValues)
	{
		return setParameter(rParam, Arrays.asList(rValues));
	}

	/***************************************
	 * Convenience method for a switch that queries the switch targets from a
	 * map based on a key stored in a parameter.
	 *
	 * @see #switchOnParam(RelationType, Function)
	 */
	protected <T> StepListEntry switchOnParam(
		RelationType<T> rSwitchParam,
		Map<T, Object>  rSwitchMap)
	{
		return switchOnParam(rSwitchParam,
							 CollectionFunctions.getMapValueFrom(rSwitchMap));
	}

	/***************************************
	 * Adds a step that switches between different process flows according to
	 * the state of a certain process parameter. See {@link SwitchStep} for
	 * details.
	 *
	 * @param  rSwitchParam    The parameter to be evaluated for the switch
	 * @param  fTargetSelector A function that evaluates the value of the switch
	 *                         parameter and returns the name corresponding
	 *                         target step
	 *
	 * @return The new step list entry
	 */
	protected <T> StepListEntry switchOnParam(
		RelationType<T>				rSwitchParam,
		Function<? super T, Object> fTargetSelector)
	{
		StepListEntry aStep =
			addStep("SwitchOn" +
					TextConvert.capitalizedIdentifier(rSwitchParam
													  .getSimpleName()),
					SwitchStep.class,
					true);

		aStep.set(SwitchStep.SWITCH_PARAM, rSwitchParam);
		aStep.set(SwitchStep.SWITCH_TARGET_SELECTOR, fTargetSelector);

		return aStep;
	}

	/***************************************
	 * Adds a step that branches the process flow. If a process contains
	 * multiple branches to the same target step the names of the branch steps
	 * will be enumerated to ensure uniqueness.
	 *
	 * @param  sName            The name of the step
	 * @param  sTargetStep      The name of the target step to be executed if
	 *                          the branch condition is TRUE
	 * @param  rBranchParam     The parameter to be evaluated for the branch
	 * @param  pBranchCondition The predicate to evaluate the branch parameter
	 *                          with
	 *
	 * @return The new step list entry
	 */
	private <T> StepListEntry addBranchStep(
		String				 sName,
		String				 sTargetStep,
		RelationType<T>		 rBranchParam,
		Predicate<? super T> pBranchCondition)
	{
		StepListEntry aStep = addStep(sName, BranchStep.class, true);

		aStep.set(BranchStep.BRANCH_PARAM, rBranchParam);
		aStep.set(BranchStep.BRANCH_CONDITION, pBranchCondition);
		aStep.set(BranchStep.BRANCH_TARGET, sTargetStep);

		return aStep;
	}

	/***************************************
	 * Adds a process step with a specific name to this definition. If the given
	 * name is not unique in the process and the boolean parameter is TRUE an
	 * increasing number will be appended to it each time it is used to ensure
	 * uniqueness.
	 *
	 * @param  sName             The name of the step
	 * @param  rClass            The class to create the step from
	 * @param  bCreateUniqueName TRUE to create a unique step name if the given
	 *                           name has already been used
	 *
	 * @return The new step list entry
	 */
	@SuppressWarnings("boxing")
	private StepListEntry addStep(
		String							 sName,
		Class<? extends ProcessFragment> rClass,
		boolean							 bCreateUniqueName)
	{
		int nStepCount = 1;

		if (aStepNameCounts.containsKey(sName))
		{
			if (bCreateUniqueName)
			{
				nStepCount = aStepNameCounts.get(sName).intValue() + 1;
			}
			else
			{
				throw new IllegalArgumentException("Duplicate process step name: " +
												   sName);
			}
		}

		aStepNameCounts.put(sName, nStepCount);

		if (nStepCount > 1)
		{
			sName = sName + nStepCount;
		}

		StepListEntry aStep = new StepListEntry(sName, rClass);

		aSteps.add(aStep);

		return aStep;
	}

	/***************************************
	 * Internal method to add a step to that copies or moves a process
	 * parameter. See {@link TransferParam} for details.
	 *
	 * @param  rSourceParam The source parameter to transfer
	 * @param  rTargetParam The target parameter
	 * @param  bMove        TRUE to move, FALSE to copy
	 *
	 * @return The new step list entry
	 */
	@SuppressWarnings("boxing")
	private <T> StepListEntry transferParam(
		RelationType<T>			rSourceParam,
		RelationType<? super T> rTargetParam,
		boolean					bMove)
	{
		String sName = bMove ? "MOVE_" : "COPY_";

		sName +=
			rSourceParam.getSimpleName() + "_TO_" +
			rTargetParam.getSimpleName();

		StepListEntry aTransferStep = addStep(sName, TransferParam.class, true);

		aTransferStep.set(ProcessRelationTypes.SOURCE_PARAM, rSourceParam);
		aTransferStep.set(ProcessRelationTypes.TARGET_PARAM, rTargetParam);
		aTransferStep.set(TransferParam.TRANSFER_PARAM_MOVE, bMove);

		return aTransferStep;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * Inner class that defines the structure of the step list elements.
	 */
	public static class StepListEntry extends ProcessElement
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Instance fields ----------------------------------------------------

		/** @serial The class that provides the step's functionality */
		private final Class<? extends ProcessFragment> rStepClass;

		/** @serial The name of the step */
		private final String sStepName;

		/** @serial The name of the next step (may be NULL) */
		private String sNextStep = null;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new StepListEntry for a named step with a specific
		 * follow-up step.
		 *
		 * @param sStepName  The name of the step
		 * @param rStepClass The class to create the step from
		 */
		public StepListEntry(
			String							 sStepName,
			Class<? extends ProcessFragment> rStepClass)
		{
			this(sStepName, rStepClass, (Map<?, ?>) null);
		}

		/***************************************
		 * Creates a new StepListEntry for a named step with a specific
		 * follow-up step and a configuration map for the step.
		 *
		 * @param sStepName      The name of the step
		 * @param rStepClass     The class to create the step from
		 * @param rConfigEntries The configuration for the step
		 */
		public StepListEntry(String							  sStepName,
							 Class<? extends ProcessFragment> rStepClass,
							 String... 						  rConfigEntries)
		{
			this(sStepName, rStepClass, (Map<?, ?>) null);

			if (rConfigEntries != null)
			{
				parseConfiguration(rConfigEntries);
			}
		}

		/***************************************
		 * Creates a new StepListEntry for a named step with a specific
		 * follow-up step and a configuration map for the step.
		 *
		 * <p>If the step class has been defined by a class literal (i.e. as
		 * <StepName>.class) in the call to this constructor the class may not
		 * have been loaded yet and any parameter types defined in it haven't
		 * been created yet too. Therefore this constructor loads the step class
		 * before parsing the configuration so that any parameter relation types
		 * referenced there are available. There's still a chance that the
		 * parsing fails if the configuration references a parameter type that
		 * is defined in a step referenced only by a step list entry that is
		 * created later.</p>
		 *
		 * @param sStepName  The name of the step
		 * @param rStepClass The class to create the step from
		 * @param rConfig    The configuration for the step
		 */
		public StepListEntry(String							  sStepName,
							 Class<? extends ProcessFragment> rStepClass,
							 Map<?, ?>						  rConfig)
		{
			this.sStepName  = sStepName;
			this.rStepClass = rStepClass;

			RelationTypes.init(rStepClass);

			// CHECK: check if lazy parsing is needed to avoid missing parameter
			// types because of not yet loaded step classes
			if (rConfig != null)
			{
				parseConfiguration(rConfig);
			}
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the name of the next step that will be executed after this
		 * one.
		 *
		 * @return The name of the next step
		 */
		public final String getNextStep()
		{
			return sNextStep;
		}

		/***************************************
		 * Returns the name of the process step.
		 *
		 * @return The stepName value
		 */
		public final String getStepName()
		{
			return sStepName;
		}

		/***************************************
		 * Convenience method to end the process after the current step has been
		 * executed.
		 *
		 * @return This entry to allow further method invocations
		 */
		public StepListEntry thenEndProcess()
		{
			thenGoTo(Process.PROCESS_END);

			return this;
		}

		/***************************************
		 * Sets the name of the next step to be executed after this one.
		 *
		 * @param sNextStep The name of the next step
		 */
		public final void thenGoTo(String sNextStep)
		{
			this.sNextStep = sNextStep;
		}

		/***************************************
		 * Sets the class of the next step to be executed after this one.
		 *
		 * @param rNextStep The class of the next step
		 */
		public final void thenGoTo(Class<? extends ProcessFragment> rNextStep)
		{
			thenGoTo(rNextStep.getSimpleName());
		}

		/***************************************
		 * Adds a configuration entry from the corresponding key and value
		 * elements.
		 *
		 * @param rKey   The key
		 * @param rValue The value
		 */
		@SuppressWarnings("unchecked")
		void addConfigurationEntry(Object rKey, Object rValue)
		{
			RelationType<?> rParamType = null;

			if (rKey instanceof RelationType<?>)
			{
				rParamType = (RelationType<?>) rKey;
			}
			else if (rKey instanceof String)
			{
				rParamType = RelationType.valueOf((String) rKey);
			}

			if (rParamType == null)
			{
				throw new IllegalArgumentException("No relation type: " + rKey);
			}

			Class<?> rTargetType = rParamType.getTargetType();

			if (Collection.class.isAssignableFrom(rTargetType))
			{
				setCollectionParam(rParamType, rValue);
			}
			else if (Map.class.isAssignableFrom(rTargetType))
			{
				setMapParam(rParamType, rValue);
			}
			else
			{
				rValue = convertParamValue(rParamType.getTargetType(), rValue);
				set((RelationType<Object>) rParamType, rValue);
			}
		}

		/***************************************
		 * Applies the configuration to a new process step instance. The
		 * configuration consists of all relations that are set on this entry.
		 *
		 * @param rStep The step to apply the configuration to
		 */
		void applyConfiguration(ProcessElement rStep)
		{
			// copy single relations to preserve existing annotations set by
			// the process step
			for (Relation<?> rSource : getRelations(ALL_RELATIONS))
			{
				@SuppressWarnings("unchecked")
				RelationType<Object> rType =
					(RelationType<Object>) rSource.getType();

				// first set value which creates the relation if necessary
				Relation<?> rTarget = rStep.set(rType, rSource.getTarget());

				// then copy the annotations (meta-relations)
				ObjectRelations.copyRelations(rSource, rTarget, true);
			}
		}

		/***************************************
		 * Checks and if necessary converts the raw configuration value for a
		 * parameter according to the given target datatype. If the value can
		 * neither be assigned nor converted to the target datatype an exception
		 * will be thrown.
		 *
		 * @param  rTargetType The target datatype
		 * @param  rValue      The value to be checked and converted
		 *
		 * @return The parameter value, converted if necessary
		 *
		 * @throws IllegalArgumentException If the value can neither be assigned
		 *                                  nor converted to the target datatype
		 */
		Object convertParamValue(Class<?> rTargetType, Object rValue)
		{
			if (RelationType.class.isAssignableFrom(rTargetType) &&
				rValue instanceof String)
			{
				rValue = RelationType.valueOf((String) rValue);
			}
			else if (!rTargetType.isAssignableFrom(rValue.getClass()))
			{
				throw new IllegalArgumentException("Incompatible value datatype " +
												   rValue.getClass() +
												   " for parameter " +
												   rTargetType);
			}

			return rValue;
		}

		/***************************************
		 * Internal method to create a new ProcessStep instance from a
		 * StepListEntry.
		 *
		 * @return The new process step
		 *
		 * @throws ProcessException If creating the instance fails
		 */
		ProcessStep createStep() throws ProcessException
		{
			try
			{
				ProcessFragment aFragment = rStepClass.newInstance();
				ProcessStep     aStep;

				if (aFragment instanceof InteractionFragment)
				{
					aStep =
						new FragmentInteraction((InteractionFragment)
												aFragment);
				}
				else
				{
					aStep = (ProcessStep) aFragment;
				}

				aStep.setName(sStepName);

				return aStep;
			}
			catch (Exception e)
			{
				throw new ProcessException(null,
										   String.format("Creation of process step %s failed",
														 rStepClass
														 .getSimpleName()),
										   e);
			}
		}

		/***************************************
		 * Initializes a process step. The step instance must have been
		 * previously created by this entry.
		 *
		 * @param  rStep The step to be initialized
		 *
		 * @throws ProcessException If initializing the step fails
		 */
		void initStep(ProcessStep rStep) throws ProcessException
		{
			assert sStepName == rStep.getName();

			applyConfiguration(rStep);
			rStep.setup();
		}

		/***************************************
		 * Creates a new step configuration from a map containing the
		 * configuration entries.
		 *
		 * @param rConfig A map containing the configuration entries
		 */
		void parseConfiguration(Map<?, ?> rConfig)
		{
			for (Map.Entry<?, ?> rEntry : rConfig.entrySet())
			{
				addConfigurationEntry(rEntry.getKey(), rEntry.getValue());
			}
		}

		/***************************************
		 * Creates a new step configuration from a list of map entry strings.
		 *
		 * @param rConfig A string array containing the raw configuration
		 *                entries
		 */
		void parseConfiguration(String[] rConfig)
		{
			for (String sConfigEntry : rConfig)
			{
				Pair<Object, Object> aEntry =
					parseConfigurationEntry(sConfigEntry);

				addConfigurationEntry(aEntry.first(), aEntry.second());
			}
		}

		/***************************************
		 * Parses a configuration entry from a string.
		 *
		 * @param  sEntry The string defining the configuration entry
		 *
		 * @return A pair containing the key and value parts of the entry
		 *
		 * @throws IllegalArgumentException If the argument is not a valid
		 *                                  configuration entry
		 */
		Pair<Object, Object> parseConfigurationEntry(String sEntry)
		{
			Matcher aMatcher = aConfigEntryPattern.matcher(sEntry);

			if (!aMatcher.matches())
			{
				throw new IllegalArgumentException("Invalid configuration entry: " +
												   sEntry);
			}

			return new Pair<Object, Object>(TextUtil.parseObject(aMatcher.group(1)),
											TextUtil.parseObject(aMatcher.group(2)));
		}

		/***************************************
		 * Sets a parameter that resolves to a collection instance. The value
		 * argument can either be a collection or a single value. In the first
		 * case all elements of the value collection will be added to the
		 * parameter collection. A single value will be converted to the correct
		 * element datatype with {@link #convertParamValue(Class, Object)} and
		 * then added to the parameter collection.
		 *
		 * @param  rType  The parameter type to set the collection for
		 * @param  rValue The parameter value
		 *
		 * @throws IllegalArgumentException If the value argument is not valid
		 *                                  for the parameter's collection
		 */
		@SuppressWarnings("unchecked")
		void setCollectionParam(RelationType<?> rType, Object rValue)
		{
			Collection<Object> rCollection = (Collection<Object>) get(rType);

			if (Collection.class.isAssignableFrom(rValue.getClass()))
			{
				rCollection.addAll((Collection<Object>) rValue);
			}
			else
			{
				rCollection.add(convertParamValue(rType.get(ELEMENT_DATATYPE),
												  rValue));
			}
		}

		/***************************************
		 * Sets a parameter that resolves to a map instance. The value argument
		 * can either be a map or a single value. In the first case all elements
		 * of the value map will be added to the parameter map. A single value
		 * can be either a {@link Pair} containing key and value or a string.
		 * Strings will be parsed with {@link #parseConfigurationEntry(String)}
		 * to split them into a key/value pair.
		 *
		 * @param  rType  The parameter type to set the map for
		 * @param  rValue The parameter value
		 *
		 * @throws IllegalArgumentException If the value argument is not valid
		 *                                  for the parameter's map
		 */
		@SuppressWarnings("unchecked")
		void setMapParam(RelationType<?> rType, Object rValue)
		{
			Map<Object, Object> rMap = (Map<Object, Object>) get(rType);

			if (Map.class.isAssignableFrom(rValue.getClass()))
			{
				rMap.putAll((Map<?, ?>) rValue);
			}
			else
			{
				Pair<Object, Object> aMapEntry;

				if (rValue instanceof Pair<?, ?>)
				{
					aMapEntry = (Pair<Object, Object>) rValue;
				}
				else if (rValue instanceof String)
				{
					aMapEntry = parseConfigurationEntry((String) rValue);
				}
				else
				{
					throw new IllegalArgumentException("Invalid map type config entry: " +
													   rValue);
				}

				Object rKey =
					convertParamValue(rType.get(KEY_DATATYPE),
									  aMapEntry.first());

				rValue =
					convertParamValue(rType.get(VALUE_DATATYPE),
									  aMapEntry.second());

				rMap.put(rKey, rValue);
			}
		}
	}
}
