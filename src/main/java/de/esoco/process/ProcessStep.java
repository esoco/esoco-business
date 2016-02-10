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

import de.esoco.entity.Entity;

import de.esoco.history.HistoryManager;
import de.esoco.history.HistoryRecord;

import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.Function;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import static de.esoco.history.HistoryManager.HISTORIZED;

import static de.esoco.process.ProcessRelationTypes.CONTINUATION_PARAM;
import static de.esoco.process.ProcessRelationTypes.CONTINUATION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.HISTORY_END;
import static de.esoco.process.ProcessRelationTypes.HISTORY_START;
import static de.esoco.process.ProcessRelationTypes.HISTORY_TARGET_PARAM;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAM_VALIDATIONS;
import static de.esoco.process.ProcessRelationTypes.INTERACTIVE_INPUT_PARAM;
import static de.esoco.process.ProcessRelationTypes.PARAM_VALIDATIONS;
import static de.esoco.process.ProcessRelationTypes.TRANSACTION_END;
import static de.esoco.process.ProcessRelationTypes.TRANSACTION_START;

import static org.obrel.core.RelationTypes.newType;
import static org.obrel.type.MetaTypes.TRANSACTIONAL;
import static org.obrel.type.StandardTypes.NAME;


/********************************************************************
 * Base class for all kinds of process steps. This class is serializable and
 * uses the default serialized form. Subclasses must declare a serialVersionUID
 * field and implement correct serialization of any fields that they define. In
 * general it is recommended that subclasses don't define fields but use
 * relations instead. The types of all relations that shall not be serialized
 * must have the flag {@link RelationTypeModifier#TRANSIENT} set. Further
 * information about the serialization of processes can be found in the
 * documentation of class {@link Process}.
 *
 * <p>A process step can be set to be wrapped inside a history group or a
 * transaction by setting one of the flags {@link HistoryManager#HISTORIZED} or
 * {@link MetaTypes#TRANSACTIONAL}, respectively. The {@link #execute()} method
 * will then be invoked after a transaction and a history group have been
 * started. History always includes a transaction to combine the writing of the
 * history and persistent changes by the step.</p>
 *
 * @author eso
 */
public abstract class ProcessStep extends ProcessFragment
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The next step's name */
	public static final RelationType<String> NEXT_STEP = newType();

	static
	{
		RelationTypes.init(ProcessStep.class);
	}

	//~ Instance fields --------------------------------------------------------

	/** Will be restored by the parent process on deserialization */
	private transient Process rProcess;

	private Set<RelationType<?>>			 aModifiedParams = new HashSet<>();
	private Map<String, Action<ProcessStep>> aFinishActions  = new HashMap<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Default constructor, which must be provided by any subclass.
	 */
	public ProcessStep()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Registers an action that will be executed just before this process step
	 * is finished, i.e. the process continues to the next step or terminates.
	 * This can be used to perform cleanups of resources that have been
	 * allocated during the step initialization. It is intended mainly for
	 * framework methods to perform automatic resource cleanup. Subclasses of
	 * process steps and fragments should perform their cleanups directly in
	 * their implemented methods. Actions can be removed by their key through
	 * the method {@link #removeFinishAction(String)}.
	 *
	 * <p>A finish action will receive the associated process step instance as
	 * it's argument to allow it to modify process parameters if necessary.</p>
	 *
	 * @param sKey    A key that identifies the action for removal
	 * @param rAction The finish action to register
	 */
	public void addFinishAction(String sKey, Action<ProcessStep> rAction)
	{
		aFinishActions.put(sKey, rAction);
	}

	/***************************************
	 * Returns the step's name.
	 *
	 * @return The step's name
	 */
	public final String getName()
	{
		return get(NAME);
	}

	/***************************************
	 * @see ProcessFragment#getProcess()
	 */
	@Override
	public final Process getProcess()
	{
		return rProcess;
	}

	/***************************************
	 * Checks whether a parameter has been modified by the process during the
	 * last interaction cycle.
	 *
	 * @param  rParam The parameter to check
	 *
	 * @return TRUE if the parameter has been modified
	 */
	public boolean isParameterModified(RelationType<?> rParam)
	{
		return aModifiedParams.contains(rParam);
	}

	/***************************************
	 * Removes a step cleanup action that has previously been registered through
	 * the method {@link #addFinishAction(String, Action)}.
	 *
	 * @param sKey The key that identifies the action to remove
	 */
	public void removeFinishAction(String sKey)
	{
		aFinishActions.remove(sKey);
	}

	/***************************************
	 * Resets all parameter modification markers for this step.
	 */
	public void resetParameterModifications()
	{
		aModifiedParams.clear();
	}

	/***************************************
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		String sResult = getClass().getSimpleName();
		String sName   = getName();

		if (!sResult.equals(sName))
		{
			sResult += "[" + sName + "]";
		}

		return sResult;
	}

	/***************************************
	 * Executes the process step. This method must be implemented by subclasses
	 * to provide the functionality of the process step. in case of errors The
	 * implementation may throw any kind of exception. The Process class will
	 * catch any Throwable that is thrown from this method.
	 *
	 * @throws Exception Any kind of exception may be thrown if executing the
	 *                   step fails
	 */
	protected abstract void execute() throws Exception;

	/***************************************
	 * This method will be invoked on the currently suspended step if the parent
	 * process is rolled back. Most step implementations won't need to override
	 * this method. It is intended only for special steps that perform complex
	 * tasks like the execution of sub-processes. The default implementation
	 * does nothing.
	 *
	 * @throws Exception Any kind of exception can be thrown
	 */
	protected void abort() throws Exception
	{
	}

	/***************************************
	 * This method is similar to the {@link #rollback()} method, but it is
	 * invoked if the enclosing interactive process is canceled completely.
	 * Therefore it is not necessary to restore the process parameters in this
	 * method. It only needs to be implemented if the process step needs to undo
	 * a modification that has been performed on execution and which must not be
	 * persistent if the process is canceled. This is an infrequent case because
	 * in most cases this will be implemented differently, e.g. by making
	 * persistent changes in the final, non-interactive steps of the process.
	 *
	 * <p>The default implementation does nothing.</p>
	 *
	 * @throws Exception Any kind of exception may be thrown if canceling fails
	 */
	protected void cancel() throws Exception
	{
	}

	/***************************************
	 * This method must be overridden by subclasses that support a rollback of
	 * their processing. If it returns TRUE the step must also implement the
	 * method {@link #rollback()} with the rollback functionality.
	 *
	 * <p>This default implementation always returns FALSE.</p>
	 *
	 * @return TRUE if the step implementation support a rollback
	 */
	protected boolean canRollback()
	{
		return false;
	}

	/***************************************
	 * check whether the execution of this process should be stopped.
	 *
	 * @return TRUE if the process should be stopped, FALSE otherwise.
	 */
	protected boolean checkStopProcessExecution()
	{
		return Boolean.TRUE.equals(getParameter(ProcessRelationTypes.STOP_PROCESS_EXECUTION));
	}

	/***************************************
	 * This method will always be invoked at the end of a process (whether
	 * successful or not) for all executed steps. Process steps that allocate
	 * resources should override this method to free such resources if that
	 * hasn't already be done in a regular way. Multiple invocations of this
	 * method can occur and should be handled correctly by implementations. The
	 * default implementation does nothing.
	 */
	protected void cleanup()
	{
	}

	/***************************************
	 * Returns the name of the next process step that shall be executed after
	 * this one.
	 *
	 * @return The name of the next process step
	 */
	protected String getNextStep()
	{
		String sNextStep;

		if (getParameter(INTERACTIVE_INPUT_PARAM) == null)
		{
			sNextStep = get(NEXT_STEP);
		}
		else // re-execute this step again after an interactive input event
		{
			sNextStep = getName();
		}

		return sNextStep;
	}

	/***************************************
	 * Implemented to return this instance.
	 *
	 * @see ProcessFragment#getProcessStep()
	 */
	@Override
	protected ProcessStep getProcessStep()
	{
		return this;
	}

	/***************************************
	 * Internal method to invoke the {@link #execute()} method. Should only be
	 * invoked by framework classes.
	 *
	 * @throws Exception Any exception may be thrown by subclasses
	 */
	protected void internalExecute() throws Exception
	{
		execute();
	}

	/***************************************
	 * Checks whether this step must be interrupted to perform an interaction to
	 * query additional data. The default implementation returns TRUE if the
	 * flag {@link MetaTypes#INTERACTIVE} is set to TRUE and at least one
	 * interaction parameter is present.
	 *
	 * @return TRUE if an interaction is needed
	 *
	 * @throws Exception Any exception may be thrown by subclasses
	 */
	protected boolean needsInteraction() throws Exception
	{
		return hasFlag(MetaTypes.INTERACTIVE) &&
			   get(INTERACTION_PARAMS).size() > 0;
	}

	/***************************************
	 * This method can be overridden by (interactive) subclasses if an
	 * interaction continuation occurs. That is the case if the parameter that
	 * caused the interaction is an element of the continuation parameter list
	 * that is stored in {@link ProcessRelationTypes#CONTINUATION_PARAMS}. The
	 * framework stores the corresponding parameter into {@link
	 * ProcessRelationTypes#CONTINUATION_PARAM} before this method will be
	 * invoked.
	 *
	 * <p>The default implementation does nothing.</p>
	 *
	 * @throws Exception Any exception may be thrown
	 */
	protected void prepareContinuation() throws Exception
	{
	}

	/***************************************
	 * Prepares the execution of this step in succession of the previous step by
	 * invoking {@link #prepareParameters()} and {@link #prepareValues()}. This
	 * method should only be overridden by framework classes.
	 *
	 * @throws Exception Any exception may be thrown
	 */
	protected void prepareExecution() throws Exception
	{
		prepareParameters();
		prepareValues();
	}

	/***************************************
	 * Prepares a re-execution of this step during an interaction by invoking
	 * the method {@link #prepareValues()} and resets the interaction parameter.
	 * This method should only be overridden by framework classes.
	 *
	 * @throws Exception Any exception may be thrown
	 */
	protected void prepareInteraction() throws Exception
	{
		prepareValues();
		setParameter(INTERACTIVE_INPUT_PARAM, null);
	}

	/***************************************
	 * This method can be overridden to prepare this step's parameters for
	 * execution. The main use of this method is for steps that are interactive
	 * to prepare the interaction parameters. This method will not be invoked if
	 * this step is re-executed because of the modification of an interactive
	 * input parameter. In that case only {@link #prepareValues()} will be
	 * invoked.
	 *
	 * @throws Exception Any exception may be thrown if the preparation fails
	 */
	protected void prepareParameters() throws Exception
	{
	}

	/***************************************
	 * This method can be overridden to prepare a step's parameter values after
	 * their initialization in the method {@link #prepareParameters()} or to
	 * update the values after an interaction occurred.
	 *
	 * @throws Exception Any exception may be thrown if the preparation fails
	 */
	protected void prepareValues() throws Exception
	{
	}

	/***************************************
	 * Will be invoked by a process on a rollback to reset parameter
	 * initializations performed by interactive steps. This method will be
	 * invoked on the currently suspended step as well as on any step on which
	 * the {@link ProcessStep#rollback()} method is invoked.
	 *
	 * @throws Exception Any exception may be thrown if the reset fails
	 */
	protected void resetParameters() throws Exception
	{
	}

	/***************************************
	 * This method can be overridden to resume this step after the process had
	 * been suspended. It will be invoked before the {@link #execute()} method
	 * is called when the process had been suspended by this step after a
	 * previous call to the {@link #prepareParameters()} method. A possible
	 * application would be to collect the user input from the parameters for an
	 * interactive step.
	 *
	 * <p>If the method is invoked after an interactive input occurred the
	 * interaction parameter will still be set. It will be reset automatically
	 * before the process continues the execution.</p>
	 *
	 * @return TRUE if the process can continue with the execution of this step,
	 *         FALSE if this step requires another interaction first
	 *
	 * @throws Exception Any exception may be thrown if resuming the step fails
	 */
	protected boolean resume() throws Exception
	{
		return true;
	}

	/***************************************
	 * Must be implemented by a subclass if it can perform a rollback of a
	 * previous execution. It is guaranteed by the framework that this method
	 * will only be invoked after the step has been executed already. It is
	 * intended mainly for interactive processes that stop execution at certain
	 * (interactive) steps and can be rolled back to previous such steps. A step
	 * implementation that supports rollback must return TRUE from it's
	 * overridden {@link #canRollback()} method.
	 *
	 * <p>A successful rollback must leave this step in a state that allows it
	 * and the following steps in the enclosing process to be executed again. On
	 * re-execution, the method {@link #prepareParameters()} will be invoked
	 * again too before execution. Basically, after a rollback the parameters of
	 * the enclosing process should be in the same state as they had been before
	 * the execution of this step.</p>
	 *
	 * <p>This default implementation always throws a {@link ProcessException}
	 * stating that a rollback is not supported.</p>
	 *
	 * @throws Exception Any exception may be thrown if the rollback fails
	 */
	protected void rollback() throws Exception
	{
		throw new ProcessException(this,
								   String.format("Rollback not supported by process step %s [%s]",
												 getName(),
												 getClass().getSimpleName()));
	}

	/***************************************
	 * Sets the name of the next process step that shall be executed after this
	 * one.
	 *
	 * @param sNextStepName The name of the next process step
	 */
	protected void setNextStep(String sNextStepName)
	{
		set(NEXT_STEP, sNextStepName);
	}

	/***************************************
	 * This method must be overridden by subclasses that require initialization.
	 * It will be invoked automatically after a step instance has been created
	 * and added to it's process. Overriding classes should invoke the super
	 * method.
	 *
	 * @throws ProcessException Can be thrown by subclasses if the
	 *                          initialization fails
	 */
	protected void setup() throws ProcessException
	{
	}

	/***************************************
	 * Throws a runtime exception that signals a missing process parameter.
	 *
	 * @param rParamType The relation type of the missing parameter
	 */
	@Override
	protected <T> void throwMissingParameterException(
		RelationType<T> rParamType)
	{
		throw new IllegalStateException(String.format("Parameter %s not set",
													  rParamType));
	}

	/***************************************
	 * Will be invoked to validate the step's parameters after an interaction
	 * has occurred. This method processes all validation functions that are
	 * stored in {@link ProcessRelationTypes#PARAM_VALIDATIONS} and throws an
	 * {@link InvalidParametersException} if at least one validation fails.
	 * Subclasses can override this method to implement their own validations
	 * but should in most cases also invoke the superclass method.
	 *
	 * <p>The validation will not occur if an interactive input parameter exists
	 * and the method {@link #continueOnInteractiveInput(RelationType)} returns
	 * FALSE because then this step will be prepared and executed again to
	 * continue with the current interaction. Validation only occurs on the
	 * transition to the next step.</p>
	 *
	 * @throws InvalidParametersException If a preset parameter validation fails
	 * @throws Exception                  Any exception may be thrown if the
	 *                                    validation fails
	 */
	protected void validate() throws Exception
	{
		handleParamValidation(true);

		RelationType<?> rInteractionParam =
			getParameter(INTERACTIVE_INPUT_PARAM);

		if (rInteractionParam == null ||
			(hasRelation(CONTINUATION_PARAMS) &&
			 get(CONTINUATION_PARAMS).contains(rInteractionParam)))
		{
			handleParamValidation(false);
		}
	}

	/***************************************
	 * Invokes the validation functions that are stored in the process step
	 * relations {@link ProcessRelationTypes#INTERACTION_PARAM_VALIDATIONS} and
	 * {@link ProcessRelationTypes#PARAM_VALIDATIONS}. It can be overridden by
	 * subclasses to perform more complex parameter validations that cannot be
	 * described by a single validation function. It returns a mapping from
	 * invalid parameters to the corresponding error messages if at least one
	 * parameter validation fails. If no error occurs the returned map will be
	 * empty.
	 *
	 * <p>Subclasses should normally invoke the superclass method and add their
	 * own error message to the returned map if necessary. That allows the user
	 * interface of interactive steps to display all failures at once.</p>
	 *
	 * @param  bOnInteraction TRUE if the validation occurs during an
	 *                        interaction and FALSE if it occurs when the
	 *                        process progresses to the next step
	 *
	 * @return The mapping from invalid parameters to the corresponding error
	 *         messages
	 */
	@SuppressWarnings("unchecked")
	protected Map<RelationType<?>, String> validateParameters(
		boolean bOnInteraction)
	{
		Map<RelationType<?>, Function<?, String>> rParamValidations =
			bOnInteraction ? get(INTERACTION_PARAM_VALIDATIONS)
						   : get(PARAM_VALIDATIONS);

		Map<RelationType<?>, String> aInvalidParams =
			new HashMap<RelationType<?>, String>(rParamValidations.size());

		for (Entry<RelationType<?>, Function<?, String>> rEntry :
			 rParamValidations.entrySet())
		{
			RelationType<Object>     rParam    =
				(RelationType<Object>) rEntry.getKey();
			Function<Object, String> fValidate =
				(Function<Object, String>) rEntry.getValue();

			Object rParamValue  = getParameter(rParam);
			String sInvalidInfo;

			try
			{
				sInvalidInfo = fValidate.evaluate(rParamValue);
			}
			catch (Exception e)
			{
				sInvalidInfo = "ParamValidationFailed";
			}

			if (sInvalidInfo != null)
			{
				aInvalidParams.put(rParam, sInvalidInfo);
			}
		}

		return aInvalidParams;
	}

	/***************************************
	 * Package-internal method to be overridden by subclasses that allow
	 * multiple interactions in a single step which can be rolled back with this
	 * method. This is needed for steps that invoke sub-processes. This method
	 * should always be invoked before executing a rollback with the method
	 * {@link #rollbackToPreviousInteraction()}.
	 *
	 * <p>This default implementation always returns FALSE.</p>
	 *
	 * @return TRUE if this step support the rollback to a previous interaction
	 */
	boolean canRollbackToPreviousInteraction()
	{
		return false;
	}

	/***************************************
	 * Executes all actions that have previously been registered through the
	 * method {@link #addFinishAction(String, Action)}.
	 */
	void executeFinishActions()
	{
		for (Action<ProcessStep> rAction : aFinishActions.values())
		{
			rAction.execute(this);
		}

		aFinishActions.clear();
	}

	/***************************************
	 * Returns the interaction process step for this step. This default
	 * implementation always returns THIS but subclasses can return different
	 * step, e.g. when they execute sub-processes.
	 *
	 * @return The interaction step
	 */
	ProcessStep getInteractionStep()
	{
		return this;
	}

	/***************************************
	 * Records a parameter modification that can then later be queried with
	 * {@link #isParameterModified(RelationType)}. Parameter modifications can
	 * be value or property changes.
	 *
	 * @param rParam The relation type of the modified parameter
	 */
	void parameterModified(RelationType<?> rParam)
	{
		aModifiedParams.add(rParam);
	}

	/***************************************
	 * Performs this process step by invoking it's execute method. If either
	 * history or transactions handling are enabled for this step the execution
	 * will be wrapped inside a history group and/or a transaction.
	 *
	 * @return The name of the next process step to execute after this one
	 *
	 * @throws Exception If performing the process step fails
	 */
	final String perform() throws Exception
	{
		boolean bHistorized    = hasFlag(HISTORIZED);
		boolean bTransactional = hasFlag(TRANSACTIONAL);

		boolean bBeginHistory  = bHistorized || hasFlag(HISTORY_START);
		boolean bCommitHistory = bHistorized || hasFlag(HISTORY_END);

		boolean bBeginTransaction  =
			bBeginHistory || bTransactional || hasFlag(TRANSACTION_START);
		boolean bCommitTransaction =
			bCommitHistory || bTransactional || hasFlag(TRANSACTION_END);

		setParameter(CONTINUATION_PARAM, null);

		if (bBeginTransaction)
		{
			RelationType<? extends Entity> rTargetParam =
				get(HISTORY_TARGET_PARAM);

			String sValue  = get(HistoryRecord.VALUE);
			Entity rTarget =
				rTargetParam != null ? getParameter(rTargetParam)
									 : get(HistoryRecord.TARGET);

			if (sValue == null)
			{
				sValue = getProcess().get(StandardTypes.NAME) + "." + getName();
			}

			getProcess().beginTransaction(bBeginHistory, rTarget, sValue);
		}

		internalExecute();

		RelationType<?> rInteractionParam =
			getParameter(INTERACTIVE_INPUT_PARAM);

		if (rInteractionParam == null)
		{
			executeFinishActions();
		}
		else if (hasRelation(CONTINUATION_PARAMS) &&
				 get(CONTINUATION_PARAMS).contains(rInteractionParam))
		{
			setParameter(CONTINUATION_PARAM, rInteractionParam);
			setParameter(INTERACTIVE_INPUT_PARAM, null);
			prepareContinuation();
		}

		if (bCommitTransaction)
		{
			getProcess().commitTransaction(bCommitHistory);
		}

		return getNextStep();
	}

	/***************************************
	 * An internal method that prepares this step for execution If no
	 * interaction occurred it invokes {@link #prepareExecution()}.
	 *
	 * @return TRUE if the process can continue with the execution of this step,
	 *         FALSE if this step requires an interaction first
	 *
	 * @throws Exception Any exception may be thrown if the preparation fails
	 */
	boolean prepareStep() throws Exception
	{
		if (getParameter(INTERACTIVE_INPUT_PARAM) == null)
		{
			aFinishActions.clear();
			prepareExecution();
		}
		else
		{
			prepareInteraction();
		}

		return !needsInteraction();
	}

	/***************************************
	 * Package-internal method to be overridden by subclasses that allow
	 * multiple interactions in a single step which can be rolled back with this
	 * method. This is needed for steps that invoke sub-processes. This method
	 * should only be invoked after querying the capability of a rollback with
	 * the method {@link #canRollbackToPreviousInteraction()}.
	 *
	 * <p>This default implementation does nothing.</p>
	 *
	 * @throws ProcessException If the rollback fails
	 */
	void rollbackToPreviousInteraction() throws ProcessException
	{
	}

	/***************************************
	 * Sets the step's name. Will only be used internally for special purposes
	 *
	 * @param sName The new name of the step
	 */
	void setName(String sName)
	{
		set(NAME, sName);
	}

	/***************************************
	 * Package internal method to associate this step with a particular
	 * instance.
	 *
	 * @param rProcess The new parent process of this step
	 */
	void setProcess(Process rProcess)
	{
		this.rProcess = rProcess;
	}

	/***************************************
	 * Evaluates a list of parameter validation functions and throws an
	 * exception for all invalid parameters.
	 *
	 * @param  bOnInteraction rParamValidations The mapping from parameters to
	 *                        validation functions
	 *
	 * @throws InvalidParametersException If one or more parameters are invalid
	 */
	private void handleParamValidation(boolean bOnInteraction)
		throws InvalidParametersException
	{
		Map<RelationType<?>, String> rInvalidParams =
			validateParameters(bOnInteraction);

		if (rInvalidParams.size() > 0)
		{
			throw new InvalidParametersException(this, rInvalidParams);
		}
	}
}
