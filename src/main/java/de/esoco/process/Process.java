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
import de.esoco.entity.EntityManager;

import de.esoco.history.HistoryManager;
import de.esoco.history.HistoryRecord;

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.logging.Log;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.manage.TransactionManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;
import org.obrel.core.SerializableRelatedObject;
import org.obrel.type.ListenerType.NotificationHandler;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import static de.esoco.history.HistoryManager.HISTORIZED;

import static de.esoco.process.ProcessRelationTypes.AUTO_CONTINUE;
import static de.esoco.process.ProcessRelationTypes.PARAM_INITIALIZATIONS;
import static de.esoco.process.ProcessRelationTypes.PARAM_USAGE_COUNT;
import static de.esoco.process.ProcessRelationTypes.PROCESS;
import static de.esoco.process.ProcessRelationTypes.PROCESS_EXCEPTION;
import static de.esoco.process.ProcessRelationTypes.PROCESS_ID;
import static de.esoco.process.ProcessRelationTypes.PROCESS_LISTENERS;
import static de.esoco.process.ProcessRelationTypes.PROCESS_NAME;
import static de.esoco.process.ProcessRelationTypes.PROCESS_SCHEDULER;
import static de.esoco.process.ProcessRelationTypes.PROCESS_START_TIME;
import static de.esoco.process.ProcessRelationTypes.PROCESS_SUSPEND_TIME;
import static de.esoco.process.ProcessRelationTypes.PROCESS_USER;
import static de.esoco.process.ProcessRelationTypes.RESUME_PROCESSES;
import static de.esoco.process.ProcessRelationTypes.TEMPORARY_PARAM_TYPES;

import static org.obrel.type.MetaTypes.TRANSACTIONAL;


/********************************************************************
 * Models processes that consist of {@link ProcessStep} instances. Process
 * instances are created from a {@link ProcessDefinition} instance through the
 * {@link ProcessManager} class. To run a process it's {@link #execute()} method
 * must be invoked. A process can only be run once. After it's execution has
 * either finished completely or after it has been canceled it cannot be
 * executed again. To re-execute a process a new process instance needs to be
 * created first.
 *
 * <p>Processes can be identified by their name which may be identical for
 * multiple instances (stored in {@link StandardTypes#NAME}) and by a unique ID
 * (stored in {@link ProcessRelationTypes#PROCESS_ID}.</p>
 *
 * <p>The state of a process and it's steps is defined with parameters that must
 * be set on the process instance. These parameters are stored as relations of
 * the process context and the types of the process parameter are therefore
 * defined as relation types. The process parameters can either be accessed
 * through the standard relation access methods or, for better readability,
 * through corresponding methods like {@link #getParameter(RelationType)}.</p>
 *
 * <p>Processes can be hierarchical which means that a process step can invoke a
 * sub-process (see {@link SubProcessStep} for details). A sub-process will run
 * in the context of the parent process, i.e. it will automatically access the
 * parameters of the process it had been invoked from. The own parameters of a
 * sub-process will be ignored in such a case.</p>
 *
 * <p>Processes that require additional input may contain interactive process
 * steps. Such steps have a relation of the type {@link MetaTypes#INTERACTIVE}.
 * For such steps two different interaction models are supported. The first of
 * these requires an instance of the {@link ProcessInteractionHandler} interface
 * to be set on the process which will then be invoked for each interactive
 * step.</p>
 *
 * <p>Alternatively, if no interaction handler is set, the process execution
 * will terminate after invoking {@link ProcessStep#prepareParameters()} but
 * before the execution of each interactive step and return the step. The
 * calling context must then handle the necessary interaction and call the
 * {@link #execute()} method of the process again after providing the necessary
 * parameters to the returned step. This must be repeated until the process
 * execution returns NULL.</p>
 *
 * <p>Processes are serializable. This is not intended for long-term persistence
 * of processes but only for the temporary storage of processes, e.g. during the
 * deactivation of a server session that uses a process. The serialization
 * format is not guaranteed and may be different in future versions.</p>
 *
 * <p>If an interaction handler is set on a process that is to be serialized it
 * must be serializable too, else an exception will occur on serialization. All
 * process steps are serializable by default, so {@link ProcessStep} subclasses
 * must implement proper serialization for their own fields. Serialization is
 * inherited from the base class {@link SerializableRelatedObject}, so all
 * relations that are set on a process to be serialized must either have a
 * target object that is serializable too or a type that is declared as
 * transient (with the flag {@link RelationTypeModifier#TRANSIENT}).</p>
 *
 * <p>Setting the flag {@link MetaTypes#TRANSACTIONAL} on a process will cause
 * the process execution to be wrapped in a transaction that will be committed
 * if the process is finished successfully. If the process execution fails or is
 * canceled the transaction will be rolled back.</p>
 *
 * <p>If the flag {@link HistoryManager#HISTORIZED} is set on a process it will
 * create a history group for the process execution so that all history records
 * created by process steps will be added to that group. If the process has an
 * entity parameter of type {@link HistoryRecord#TARGET} it will be used as the
 * target for the history group. A historized process is always also
 * transactional.</p>
 *
 * <p>To be notified of process state changes an application can register an
 * instance of a {@link ProcessListener} implementation by adding it to the
 * listener relation with the type {@link
 * ProcessRelationTypes#PROCESS_LISTENERS}. Calls to the listener interface
 * occur inside the process transaction so that any transactional code inside
 * the listener will be part of the transaction unless the listener explicitly
 * avoids the process transaction. Only if the process is canceled or fails the
 * calls to the corresponding listener methods will occur after the transaction
 * has been rolled back to allow the listener to record information about the
 * process termination.</p>
 *
 * @author eso
 */
public class Process extends SerializableRelatedObject
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Internal enumeration for the distribution of process events.
	 */
	enum ProcessEventType
		implements NotificationHandler<ProcessListener, Process>
	{
		CANCELED
		{
			@Override
			public void notifyListener(
				ProcessListener rListener,
				Process			rProcess)
			{
				rListener.processCanceled(rProcess);
			}
		},
		FAILED
		{
			@Override
			public void notifyListener(
				ProcessListener rListener,
				Process			rProcess)
			{
				rListener.processFailed(rProcess);
			}
		},
		FINISHED
		{
			@Override
			public void notifyListener(
				ProcessListener rListener,
				Process			rProcess)
			{
				rListener.processFinished(rProcess);
			}
		},
		RESUMED
		{
			@Override
			public void notifyListener(
				ProcessListener rListener,
				Process			rProcess)
			{
				rListener.processResumed(rProcess);
			}
		},
		STARTED
		{
			@Override
			public void notifyListener(
				ProcessListener rListener,
				Process			rProcess)
			{
				rListener.processStarted(rProcess);
			}
		},
		SUSPENDED
		{
			@Override
			public void notifyListener(
				ProcessListener rListener,
				Process			rProcess)
			{
				rListener.processSuspended(rProcess);
			}
		};
	}

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	private static int nNextProcessId = 1;

	/** Next step signal value for a process end-point */
	public static final String PROCESS_END = "_PROCESS_END";

	/**
	 * An internal process parameter that marks process steps that have signaled
	 * the actual need for an interaction.
	 */
	private static final RelationType<Boolean> STEP_WAS_INTERACTIVE =
		RelationTypes.newFlagType(RelationTypeModifier.PRIVATE);

	//- Relation types ---------------------------------------------------------

	static
	{
		RelationTypes.init(Process.class);
	}

	//~ Instance fields --------------------------------------------------------

	private final String sProcessName;
	private final String sUniqueProcessName;

	private Process rContext		  = null;
	private boolean bInitialized	  = false;
	private boolean bSuspended		  = false;
	private boolean bRollbackRestart  = false;
	private int     nHistoryLevel     = 0;
	private int     nTransactionLevel = 0;
	private int     nNextFragmentId   = 0;
	private int     nNextParameterId  = 0;

	private transient HashMap<String, ProcessStep> aProcessSteps;
	private transient Stack<ProcessStep>		   aExecutionStack;
	private transient ProcessStep				   rCurrentStep;

	private ProcessInteractionHandler rInteractionHandler = null;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Package internal constructor, processes will only be created by
	 * ProcessDefinitions.
	 *
	 * @param  sName The name of the process
	 *
	 * @throws IllegalArgumentException If the name argument is NULL
	 */
	@SuppressWarnings("boxing")
	Process(String sName)
	{
		if (sName == null)
		{
			throw new IllegalArgumentException("Process name must not be NULL");
		}

		int nId = nNextProcessId++;

		sProcessName	   = sName;
		sUniqueProcessName = sName + "-" + nId;

		setParameter(PROCESS, this);
		setParameter(PROCESS_ID, nId);
		setParameter(PROCESS_NAME, sProcessName);

		initFields();
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Allows to cancel this process. This method can only be invoked on an
	 * interactive process that hasn't finished execution already. It will
	 * invoke the method {@link ProcessStep#cancel()} on all executed steps, by
	 * this undoing all persistent changes made so far. Finally, an active
	 * history group will be canceled and an open transaction will be rolled
	 * back.
	 *
	 * @throws ProcessException If an error occurs during canceling or if this
	 *                          process has finished execution already
	 */
	public void cancel() throws ProcessException
	{
		try
		{
			for (int i = aExecutionStack.size() - 1; i >= 0; i--)
			{
				aExecutionStack.get(i).cancel();
			}

			rCurrentStep = null;
			bSuspended   = false;

			notifyListeners(ProcessEventType.CANCELED);
			cleanup();
		}
		catch (Exception e)
		{
			wrapException(e, "Cancelling %s failed", this);
		}
	}

	/***************************************
	 * Checks whether this process can be rolled back to a certain step. An
	 * application should invoke the method {@link #rollbackTo(ProcessStep)} for
	 * a certain step only if this method returns TRUE for that step. Else the
	 * rollback invocation will throw an exception.
	 *
	 * <p>This method is intended to be used with interactive processes that
	 * suspend execution when reaching interactive process steps. If the process
	 * has already finished execution when this method is invoked it returns
	 * FALSE.</p>
	 *
	 * @param  rStep The step to check
	 *
	 * @return TRUE if a rollback to the given step is possible, FALSE if not
	 */
	public boolean canRollbackTo(ProcessStep rStep)
	{
		checkValidRollbackStep(rStep);

		ProcessStep rCheckStep   = null;
		int		    nSteps		 = aExecutionStack.size();
		boolean     bCanRollback = true;

		while (bCanRollback &&
			   nSteps > 0 &&
			   (rCheckStep = aExecutionStack.get(--nSteps)) != rStep)
		{
			bCanRollback = rCheckStep.canRollback();
		}

		return bCanRollback;
	}

	/***************************************
	 * Checks whether this process can be rolled back to a previously executed
	 * interactive step. Invokes the method {@link #canRollbackTo(ProcessStep)}
	 * if a previously executed interactive step could be found on the execution
	 * stack.
	 *
	 * <p>The method {@link #rollbackToPreviousInteraction()} should only be
	 * invoked by an application after checking that this method returns TRUE.
	 * Otherwise the rollback method may throw an exception.</p>
	 *
	 * @return TRUE if a rollback to a previously executed interactive step is
	 *         possible, FALSE if not
	 */
	public boolean canRollbackToPreviousInteraction()
	{
		boolean bCanRollback =
			rCurrentStep != null &&
			rCurrentStep.canRollbackToPreviousInteraction();

		if (!bCanRollback)
		{
			ProcessStep rPreviousStep = findPreviousInteractiveStep();

			bCanRollback =
				rPreviousStep != null ? canRollbackTo(rPreviousStep) : false;
		}

		return bCanRollback;
	}

	/***************************************
	 * Overridden to forward the call to the process context.
	 *
	 * @see {@link Relatable#deleteRelation(RelationType)}
	 */
	@Override
	public void deleteRelation(Relation<?> rRelation)
	{
		if (rContext != null)
		{
			rContext.deleteRelation(rRelation);
		}
		else
		{
			super.deleteRelation(rRelation);
		}
	}

	/***************************************
	 * Executes the process by executing all the contained process steps in the
	 * order that has been defined by the process definition.
	 *
	 * @throws ProcessException If the execution fails
	 */
	public void execute() throws ProcessException
	{
		if (rCurrentStep == null)
		{
			throw new ProcessException(null, "ProcessFinished");
		}

		if (!bInitialized)
		{
			init();
			setParameter(PROCESS_START_TIME, new Date());
			notifyListeners(ProcessEventType.STARTED);
			bInitialized = true;
		}

		if (rContext == null)
		{
			// set (only) the root process as the entity modification context
			EntityManager.setEntityModificationContext(sUniqueProcessName,
													   this);
		}

		try
		{
			executeSteps();

			if (rCurrentStep == null)
			{
				finish();
			}
			else
			{
				notifyListeners(ProcessEventType.SUSPENDED);
			}
		}
		catch (Exception e)
		{
			bSuspended = true;
			handleException(e);
		}
		finally
		{
			if (rContext == null)
			{
				EntityManager.removeEntityModificationContext(sUniqueProcessName);
			}
		}
	}

	/***************************************
	 * Overridden to forward the call to the process context.
	 *
	 * @see {@link Relatable#get(RelationType)}
	 */
	@Override
	public <T> T get(RelationType<T> rType)
	{
		return rContext != null ? rContext.get(rType) : super.get(rType);
	}

	/***************************************
	 * Returns the context this process runs in. This will either be this
	 * process itself or, in the case of sub-processes, the enclosing process
	 * context.
	 *
	 * @return The process context
	 */
	public final Process getContext()
	{
		return rContext != null ? rContext : this;
	}

	/***************************************
	 * This method returns the current process step. It has been implemented for
	 * the use of interactive process steps. Returns NULL when no further steps
	 * will follow.
	 *
	 * @return The current step
	 */
	public ProcessStep getCurrentStep()
	{
		return rCurrentStep;
	}

	/***************************************
	 * Returns the first step of this process.
	 *
	 * @return The first process step
	 */
	public ProcessStep getFirstStep()
	{
		return aExecutionStack.isEmpty() ? rCurrentStep
										 : aExecutionStack.firstElement();
	}

	/***************************************
	 * Returns the hierarchical name of this process, including any parent
	 * processes.
	 *
	 * @return The hierarchical process name
	 */
	public String getFullName()
	{
		String sFullName = sProcessName;

		if (rContext != null)
		{
			sFullName = rContext.getFullName() + '.' + sFullName;
		}

		return sFullName;
	}

	/***************************************
	 * Returns a unique integer ID for this process instance as stored in the
	 * process parameter {@link ProcessRelationTypes#PROCESS_ID}.
	 *
	 * @return The process ID
	 */
	@SuppressWarnings("boxing")
	public final int getId()
	{
		return get(PROCESS_ID);
	}

	/***************************************
	 * This method returns the process step for the latest interaction of this
	 * process. It will only return a valid result if the process has previously
	 * been executed but the method {@link #isFinished()} still returns FALSE.
	 *
	 * @return The interaction step or NULL for none
	 */
	public ProcessStep getInteractionStep()
	{
		if (rCurrentStep == null)
		{
			throw new IllegalStateException("No current interaction");
		}

		return rCurrentStep.getInteractionStep();
	}

	/***************************************
	 * Returns the name of this process as stored in the process parameter
	 * {@link StandardTypes#NAME}. This name represents the process class and
	 * may be the same for different process instances. For a unique process
	 * name see the method {@link #getUniqueProcessName()}.
	 *
	 * @return The process name
	 */
	public final String getName()
	{
		return sProcessName;
	}

	/***************************************
	 * Returns an integer ID for the automatic naming of process fragments. This
	 * method is intended to be used internally by the framework.
	 *
	 * @return   The next generated fragment ID
	 *
	 * @category internal
	 */
	public int getNextFragmentId()
	{
		return getContext().nNextFragmentId++;
	}

	/***************************************
	 * Returns a certain process parameter. If no parameter with the given ID is
	 * set NULL will be returned.
	 *
	 * @param  rParamType The type of the parameter
	 *
	 * @return The parameter value or NULL
	 */
	public <T> T getParameter(RelationType<T> rParamType)
	{
		return get(rParamType);
	}

	/***************************************
	 * Returns the process user entity that is stored in the process parameter
	 * with the type {@link ProcessRelationTypes#PROCESS_USER}.
	 *
	 * @return The process user or NULL for none
	 */
	public Entity getProcessUser()
	{
		return getParameter(PROCESS_USER);
	}

	/***************************************
	 * Overridden to forward the call to the process context.
	 *
	 * @see {@link Relatable#getRelation(RelationType)}
	 */
	@Override
	public <T> Relation<T> getRelation(RelationType<T> rType)
	{
		return rContext != null ? rContext.getRelation(rType)
								: super.getRelation(rType);
	}

	/***************************************
	 * Overridden to forward the call to the process context.
	 *
	 * @see {@link Relatable#getRelations(Predicate)}
	 */
	@Override
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> rFilter)
	{
		return rContext != null ? rContext.getRelations(rFilter)
								: super.getRelations(rFilter);
	}

	/***************************************
	 * Returns a process step with a certain name.
	 *
	 * @param  sName The name of the process step to be queried
	 *
	 * @return The corresponding process step instance or NULL if no matching
	 *         step could be found or if the step name is {@link #PROCESS_END}
	 */
	public ProcessStep getStep(String sName)
	{
		return sName.equals(PROCESS_END) ? null : aProcessSteps.get(sName);
	}

	/***************************************
	 * Returns a unique name for this process instance.
	 *
	 * @return The unique process name
	 */
	public final String getUniqueProcessName()
	{
		return sUniqueProcessName;
	}

	/***************************************
	 * A convenience method to check the existence and value of a parameter with
	 * a boolean datatype.
	 *
	 * @param  rFlagType The flag parameter type
	 *
	 * @return TRUE if the flag exists and is set to TRUE
	 */
	public final boolean hasFlagParameter(RelationType<Boolean> rFlagType)
	{
		return hasFlag(rFlagType);
	}

	/***************************************
	 * Queries if a certain parameter is stored in the process.
	 *
	 * @param  rParamType The type of the parameter
	 *
	 * @return TRUE if the parameter exists
	 */
	public boolean hasParameter(RelationType<?> rParamType)
	{
		return hasRelation(rParamType);
	}

	/***************************************
	 * Indicates whether the process has finished execution or not.
	 *
	 * @return TRUE if the process has finished execution
	 */
	public boolean isFinished()
	{
		return rCurrentStep == null;
	}

	/***************************************
	 * Checks whether this process is running in the context of another process
	 * or if is a root process.
	 *
	 * @return TRUE if this process runs in the context of another process
	 */
	public final boolean isSubProcess()
	{
		return rContext != null;
	}

	/***************************************
	 * Removes the current entity modification lock rule by invoking the method
	 * {@link EntityManager#removeEntityModificationLock(String)} with the ID of
	 * this process.
	 *
	 * @return The removed lock rule or NULL for none
	 */
	public Predicate<? super Entity> removeEntityModificationLock()
	{
		return EntityManager.removeEntityModificationLock(getContext()
														  .getUniqueProcessName());
	}

	/***************************************
	 * Removes a certain parameter from the process. If the parameter doesn't
	 * exist the call is ignored.
	 *
	 * @param rParamType The type of the parameter
	 */
	public void removeParameter(RelationType<?> rParamType)
	{
		if (rContext != null)
		{
			rContext.deleteRelation(rParamType);
		}
		else
		{
			super.deleteRelation(rParamType);
		}
	}

	/***************************************
	 * Performs a rollback of all steps up to and including a certain step. This
	 * method is intended to be used with interactive processes that suspend
	 * execution when reaching interactive process steps. The process must not
	 * have finished execution completely when this method is invoked, else an
	 * exception will be thrown. The given process step must exist in this
	 * process and it must have been executed already. All process steps that
	 * need to be rolled back must support the rollback functionality, or else
	 * an exception will be thrown.
	 *
	 * <p>When this method finishes, the process execution stack has been reset
	 * so that the argument step will be executed next if the {@link #execute()}
	 * method is invoked again.</p>
	 *
	 * @param  rStep The step to roll the process back to
	 *
	 * @throws ProcessException If this process has already finished execution
	 *                          or if the given process step is invalid or if
	 *                          any affected step doesn't support a rollback
	 */
	public void rollbackTo(ProcessStep rStep) throws ProcessException
	{
		checkValidRollbackStep(rStep);

		ProcessStep rRollbackStep;

		try
		{
			rCurrentStep.resetParameters();
			rCurrentStep.abort();

			do
			{
				rRollbackStep = aExecutionStack.pop();

				rRollbackStep.resetParameters();
				rRollbackStep.rollback();
			}
			while (rStep != rRollbackStep);
		}
		catch (Exception e)
		{
			wrapException(e, "Rollback of %s failed", this);
		}

		rCurrentStep = rStep;
		bSuspended   = false;

		// if invoked from the interaction handler, the execution loop needs to
		// be restarted with the changed current step
		if (getInteractionHandler() != null)
		{
			bRollbackRestart = true;
		}
	}

	/***************************************
	 * Performs a rollback to the previous interactive step.
	 *
	 * @throws ProcessException If the rollback fails
	 */
	public void rollbackToPreviousInteraction() throws ProcessException
	{
		if (rCurrentStep != null &&
			rCurrentStep.canRollbackToPreviousInteraction())
		{
			rCurrentStep.rollbackToPreviousInteraction();
		}
		else
		{
			ProcessStep rRollbackStep = findPreviousInteractiveStep();

			if (rRollbackStep != null)
			{
				rollbackTo(rRollbackStep);
			}
			else
			{
				throw new ProcessException(rCurrentStep,
										   "No previous interactive step");
			}
		}
	}

	/***************************************
	 * Overridden to forward the call to the process context.
	 *
	 * @see {@link Relatable#set(RelationType, Object)}
	 */
	@Override
	public <T> Relation<T> set(RelationType<T> rType, T rTarget)
	{
		return rContext != null ? rContext.set(rType, rTarget)
								: super.set(rType, rTarget);
	}

	/***************************************
	 * Sets an entity modification lock rule through the entity manager method
	 * {@link EntityManager#setEntityModificationLock(String, Predicate)}. If a
	 * lock rule already exists it will be combined with the new rule with an or
	 * join. If that is not desired the current rule must be removed by invoking
	 * {@link #removeEntityModificationLock()}.
	 *
	 * <p>All rules set from a process instance will be automatically removed if
	 * a process execution ends in any way, i.e. successfully, by cancellation,
	 * or with an error.</p>
	 *
	 * @param pLockRule The entity modification lock rule
	 */
	public void setEntityModificationLock(Predicate<? super Entity> pLockRule)
	{
		String sContextId = getContext().getUniqueProcessName();

		Predicate<? super Entity> pCurrentRule =
			EntityManager.removeEntityModificationLock(sContextId);

		pLockRule = Predicates.or(pCurrentRule, pLockRule);

		EntityManager.setEntityModificationLock(sContextId, pLockRule);
	}

	/***************************************
	 * Sets the process interaction handler for this context.
	 *
	 * @param rHandler The new interaction handler
	 */
	public void setInteractionHandler(ProcessInteractionHandler rHandler)
	{
		rInteractionHandler = rHandler;
	}

	/***************************************
	 * Sets a process parameter. If the parameter already exists it's value will
	 * be replaced.
	 *
	 * @param  rParam The parameter
	 * @param  rValue The parameter value
	 *
	 * @return The relation of the parameter
	 */
	public <T> Relation<T> setParameter(RelationType<T> rParam, T rValue)
	{
		return set(rParam, rValue);
	}

	/***************************************
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		return String.format("Process %s (current step: %s)",
							 getName(),
							 rCurrentStep);
	}

	/***************************************
	 * Adds a process step to the internal list. The step is stored by it's name
	 * which identifies it only in the context of this particular process
	 * instance. Only one step with a certain name is allowed in a process
	 * instance.
	 *
	 * <p>If no starting step has been set the argument will be set as the
	 * process step through the method {@link #setStart(ProcessStep)}. This
	 * means that the first step added to a process will become it's starting
	 * step unless the start is changed later.</p>
	 *
	 * @param  rStep The process step to add
	 *
	 * @throws IllegalArgumentException If a step with the same name exists
	 */
	void addStep(ProcessStep rStep)
	{
		if (aProcessSteps.containsKey(rStep.getName()))
		{
			throw new IllegalArgumentException("Duplicate process step name: " +
											   rStep.getName());
		}

		aProcessSteps.put(rStep.getName(), rStep);
		rStep.setProcess(this);

		if (rCurrentStep == null)
		{
			setStart(rStep);
		}
	}

	/***************************************
	 * Starts a transaction and optionally also a history group.
	 *
	 * @param  bWithHistory TRUE to also start a history group inside the
	 *                      transaction
	 * @param  rTarget      The target entity for the history or NULL for none
	 * @param  sValue       The history value or NULL
	 *
	 * @throws ProcessException If the boolean flag is TRUE but the history
	 *                          target is NULL
	 */
	void beginTransaction(boolean bWithHistory, Entity rTarget, String sValue)
		throws ProcessException
	{
		TransactionManager.begin();
		nTransactionLevel++;

		if (bWithHistory)
		{
			if (rTarget == null)
			{
				Log.warn("Missing history target for " + this);
			}

			HistoryManager.begin(getParameter(PROCESS_USER), rTarget, sValue);
			nHistoryLevel++;
		}
	}

	/***************************************
	 * Commits a currently open transaction and optionally an open history
	 * group.
	 *
	 * @param  bWithHistory TRUE to also commit a history group before the
	 *                      transaction
	 *
	 * @throws TransactionException If committing fails
	 * @throws ProcessException     If the transaction or history state is
	 *                              invalid
	 */
	void commitTransaction(boolean bWithHistory) throws TransactionException,
														ProcessException
	{
		if (nTransactionLevel > 0)
		{
			if (bWithHistory)
			{
				if (nHistoryLevel > 0)
				{
					HistoryManager.commit(false);
					nHistoryLevel--;
				}
				else
				{
					throw new ProcessException(rCurrentStep,
											   "No open history group");
				}
			}

			TransactionManager.commit();
			nTransactionLevel--;
		}
		else
		{
			throw new ProcessException(rCurrentStep, "No open transaction");
		}
	}

	/***************************************
	 * Returns the process interaction handler for this process.
	 *
	 * @return The interaction handler
	 */
	final ProcessInteractionHandler getInteractionHandler()
	{
		return getContext().rInteractionHandler;
	}

	/***************************************
	 * Returns an integer ID for the automatic naming of process parameters.
	 *
	 * @return The next generated parameter ID
	 */
	final int getNextParameterId()
	{
		return getContext().nNextParameterId++;
	}

	/***************************************
	 * Registers a temporary parameter type for this process.
	 *
	 * @param rTempParam The parameter relation type+
	 *
	 * @see   #unregisterTemporaryParameterType(RelationType, boolean)
	 */
	@SuppressWarnings("boxing")
	void registerTemporaryParameterType(RelationType<?> rTempParam)
	{
		Set<RelationType<?>> rTemporaryParamTypes =
			getParameter(TEMPORARY_PARAM_TYPES);

		if (!rTemporaryParamTypes.contains(rTempParam))
		{
			rTemporaryParamTypes.add(rTempParam);

			// ensure that usage count update is atomic
			synchronized (rTempParam)
			{
				rTempParam.set(PARAM_USAGE_COUNT,
							   rTempParam.get(PARAM_USAGE_COUNT) + 1);
			}
		}
	}

	/***************************************
	 * Unregisters all temporary parameter types that have been used in this
	 * process.
	 *
	 * @see #unregisterTemporaryParameterType(RelationType, boolean)
	 */
	final void removeTemporaryParameterTypes()
	{
		Set<RelationType<?>> rTemporaryParamTypes =
			getParameter(TEMPORARY_PARAM_TYPES);

		for (RelationType<?> rTempParam : rTemporaryParamTypes)
		{
			unregisterTemporaryParameterType(rTempParam, false);
		}

		rTemporaryParamTypes.clear();
	}

	/***************************************
	 * Internal method to set the context in which this process is executed.
	 * This is used for the execution of sub-processes in the context of a
	 * parent process.
	 *
	 * @param rContext The new process context
	 */
	final void setContext(Process rContext)
	{
		// invoke getContext() so that all sub-process contexts point to the
		// root, even in process hierarchies
		this.rContext = rContext != null ? rContext.getContext() : null;
	}

	/***************************************
	 * Removes a temporary parameter relation type that had been used in this
	 * process.
	 *
	 * @param rTempParam The temporary parameter relation type to remove
	 * @param bRemove    TRUE if it should also be removed from this process
	 *
	 * @see   #registerTemporaryParameterType(RelationType)
	 */
	@SuppressWarnings("boxing")
	void unregisterTemporaryParameterType(
		RelationType<?> rTempParam,
		boolean			bRemove)
	{
		// ensure that usage count update is atomic
		synchronized (rTempParam)
		{
			int nUsageCount = rTempParam.get(PARAM_USAGE_COUNT);

			if (nUsageCount == 1)
			{
				RelationType.unregisterRelationType(rTempParam);
				rTempParam.deleteRelation(PARAM_USAGE_COUNT);
			}
			else
			{
				rTempParam.set(PARAM_USAGE_COUNT, nUsageCount - 1);
			}
		}

		if (bRemove)
		{
			getParameter(TEMPORARY_PARAM_TYPES).remove(rTempParam);
		}
	}

	/***************************************
	 * Checks whether this process and the given process step are valid for a
	 * rollback. If not, an exception will be thrown.
	 *
	 * @param  rStep The step to check
	 *
	 * @throws IllegalArgumentException If either this process or the given step
	 *                                  are not valid for a rollback
	 */
	private void checkValidRollbackStep(ProcessStep rStep)
	{
		if (rStep == null || !aExecutionStack.contains(rStep))
		{
			throw new IllegalArgumentException("InvalidRollbackStep: " + rStep);
		}
	}

	/***************************************
	 * Performs a final cleanup of process resources when the process is
	 * terminated.
	 *
	 * @throws TransactionException If performing a transaction rollback fails
	 */
	private void cleanup() throws TransactionException
	{
		try
		{
			for (ProcessStep rStep : aProcessSteps.values())
			{
				if (aExecutionStack.contains(rStep))
				{
					rStep.executeFinishActions();
					rStep.cleanup();
				}
			}

			// only remove temporary types if root process is terminated
			if (rContext == null)
			{
				removeTemporaryParameterTypes();
			}

			if (nHistoryLevel > 0)
			{
				HistoryManager.rollback();
				nHistoryLevel = 0;
			}
		}
		finally
		{
			if (nTransactionLevel > 0)
			{
				TransactionManager.rollback();
				nTransactionLevel = 0;

				EntityManager.resetEntityModifications(this);
			}

			if (rContext == null)
			{
				EntityManager.removeEntityModificationLock(sUniqueProcessName);
				EntityManager.checkUnsavedEntityModifications(sUniqueProcessName,
															  this);
			}
		}
	}

	/***************************************
	 * Executes the process steps in a loop until the process is either
	 * suspended or finished.
	 *
	 * @throws Exception If the execution of a step fails
	 */
	private void executeSteps() throws Exception
	{
		do
		{
			boolean bExecute = true;

			if (bSuspended)
			{
				rCurrentStep.validate();

				bSuspended = false;
				bExecute   = rCurrentStep.resume();

				notifyListeners(ProcessEventType.RESUMED);
			}
			else
			{
				bExecute = prepareStep(rCurrentStep);

				if (bRollbackRestart)
				{
					// flag will be set to TRUE by the rollbackTo method if
					// an interaction handler has caused the rollback; in
					// that case the execution loop must be restarted with
					// the modified current step from the rollback
					bRollbackRestart = false;

					continue;
				}
			}

			if (bExecute)
			{
				ProcessStep rNextStep = getStep(rCurrentStep.perform());

				// only add step if progressing to next step, not on interaction
				if (rCurrentStep != null && rNextStep != rCurrentStep)
				{
					if (rCurrentStep.canRollback())
					{
						aExecutionStack.push(rCurrentStep);
					}
					else
					{
						// if no rollback possible remove unreachable entries
						// from stack; this will prevent the storing of
						// unnecessary object references, especially during
						// bulk processing or background processes
						aExecutionStack.clear();
					}

					rCurrentStep = rNextStep;
				}
			}
			else
			{
				setParameter(PROCESS_SUSPEND_TIME, new Date());
				bSuspended = true;
			}
		}
		while (!bSuspended && rCurrentStep != null);
	}

	/***************************************
	 * Internal method to search the previous interactive step from the top of
	 * the execution stack.
	 *
	 * @return The previous interactive step or NULL if no such step exists on
	 *         the execution stack
	 */
	private ProcessStep findPreviousInteractiveStep()
	{
		// start at last stack element; the current interactive step is not yet
		// on the stack because it will only be executed after the interaction
		int		    nStep		  = aExecutionStack.size();
		ProcessStep rRollbackStep = null;

		while (nStep > 0 && rRollbackStep == null)
		{
			ProcessStep rStep = aExecutionStack.get(--nStep);

			if (rStep.hasFlag(STEP_WAS_INTERACTIVE) &&
				!rStep.hasFlag(AUTO_CONTINUE))
			{
				rRollbackStep = rStep;
			}
		}

		return rRollbackStep;
	}

	/***************************************
	 * Finishes the successful execution of this process.
	 *
	 * @throws TransactionException If committing an open transaction fails
	 * @throws ProcessException     If the transaction or history state is
	 *                              invalid
	 */
	private void finish() throws TransactionException, ProcessException
	{
		if (hasFlag(TRANSACTIONAL))
		{
			commitTransaction(hasFlag(HISTORIZED));
		}

		if (nHistoryLevel > 0)
		{
			@SuppressWarnings("boxing")
			String sMessage =
				String.format("Uncommitted history levels(%d) in process %s",
							  nHistoryLevel,
							  this);

			throw new ProcessException(rCurrentStep, sMessage);
		}

		if (nTransactionLevel > 0)
		{
			@SuppressWarnings("boxing")
			String sMessage =
				String.format("Uncommitted transaction levels (%d) in process %s",
							  nTransactionLevel,
							  this);

			throw new ProcessException(rCurrentStep, sMessage);
		}

		notifyListeners(ProcessEventType.FINISHED);

		ProcessScheduler rProcessScheduler = getParameter(PROCESS_SCHEDULER);

		if (rProcessScheduler != null)
		{
			for (Class<? extends ProcessDefinition> rProcess :
				 getParameter(RESUME_PROCESSES))
			{
				rProcessScheduler.resumeProcess(rProcess);
			}
		}

		cleanup();
	}

	/***************************************
	 * Handles exceptions that may occur during process execution.
	 *
	 * @param  e The exception that occurred
	 *
	 * @throws ProcessException Always throws a process exception that has been
	 *                          created from the original exception
	 */
	private void handleException(Exception e) throws ProcessException
	{
		if (!(e instanceof InvalidParametersException))
		{
			try
			{
				cleanup();
			}
			catch (Exception eCleanup)
			{
				// only log and then throw original exception
				Log.errorf(eCleanup, "Error rollback failed in %s", this);
			}
		}

		// TODO: introduce special error parameters and perform cleanup
		// of disposable/closeable parameters
		throw wrapException(e, "Execution of %s failed", this);
	}

	/***************************************
	 * Performs the initialization of this process before execution.
	 *
	 * @throws ProcessException
	 */
	private void init() throws ProcessException
	{
		boolean bHistory = hasFlag(HISTORIZED);

		initParams();

		if (bHistory || hasFlag(TRANSACTIONAL))
		{
			beginTransaction(bHistory,
							 getParameter(HistoryRecord.TARGET),
							 getName());
		}
	}

	/***************************************
	 * Initializes the transient fields. Invoked from the constructors and by
	 * deserialization.
	 */
	private void initFields()
	{
		aProcessSteps   = new HashMap<String, ProcessStep>();
		aExecutionStack = new Stack<ProcessStep>();
	}

	/***************************************
	 * Initializes this process by evaluating it with the functions stored in
	 * the map{@link ProcessRelationTypes#PARAM_INITIALIZATIONS}. The resulting
	 * values are assigned to the process parameters stored in the map.
	 */
	@SuppressWarnings("unchecked")
	private void initParams()
	{
		Map<RelationType<?>, Function<? super Process, ?>> rTaskAttributeMap =
			getParameter(PARAM_INITIALIZATIONS);

		for (Entry<RelationType<?>, Function<? super Process, ?>> rEntry :
			 rTaskAttributeMap.entrySet())
		{
			RelationType<Object>     rParam    =
				(RelationType<Object>) rEntry.getKey();
			Function<Object, Object> rFunction =
				(Function<Object, Object>) rEntry.getValue();

			Object rValue = rFunction.evaluate(this);

			if (rValue != null)
			{
				setParameter(rParam, rValue);
			}
		}
	}

	/***************************************
	 * Notifies registered listeners of a certain process event.
	 *
	 * @param rEventType The type of event that occurred
	 */
	private void notifyListeners(ProcessEventType rEventType)
	{
		if (rContext == null)
		{
			PROCESS_LISTENERS.notifyListeners(this, this, rEventType);
		}
	}

	/***************************************
	 * Internal method to prepare a process step for execution and to invoke the
	 * interaction handler if necessary. If no handler is available and
	 * interaction is required this method returns false to suspend the
	 * execution of this process.
	 *
	 * @param  rStep The process step to prepare
	 *
	 * @return TRUE if the process can continue with the step execution, FALSE
	 *         if it requires an external interaction
	 *
	 * @throws Exception If either preparing the step or invoking the
	 *                   interaction handler fails
	 */
	private boolean prepareStep(ProcessStep rStep) throws Exception
	{
		boolean bContinue = rStep.prepareStep();

		if (!bContinue)
		{
			rStep.set(STEP_WAS_INTERACTIVE);

			ProcessInteractionHandler rInteractionHandler =
				getInteractionHandler();

			if (rInteractionHandler != null)
			{
				rInteractionHandler.performInteraction(rStep);
				bContinue = true;
			}
		}

		return bContinue;
	}

	/***************************************
	 * Reads this instance from the given stream.
	 *
	 * @param  rIn The stream to read this process' state from
	 *
	 * @throws IOException            In case of I/O errors
	 * @throws ClassNotFoundException If a class could not be deserialized
	 */
	private void readObject(ObjectInputStream rIn) throws IOException,
														  ClassNotFoundException
	{
		// read non-transient fields: interaction handler and flags
		rIn.defaultReadObject();

		initFields();

		// read all steps and put them in the process step map:
		int nCount = rIn.readInt();

		while (nCount-- > 0)
		{
			addStep((ProcessStep) rIn.readObject());
		}

		// read names of the steps on the execution stack and restore the stack
		nCount = rIn.readInt();

		while (nCount-- > 0)
		{
			aExecutionStack.push(getStep((String) rIn.readObject()));
		}

		// finally read the name of the current step and restore it
		rCurrentStep = getStep((String) rIn.readObject());
	}

	/***************************************
	 * Defines the starting point (i.e., the first step) of the process. Only
	 * one start point can exist in a process. The step must have been added to
	 * this process already, else an exception will be thrown.
	 *
	 * @param  rStartStep The step to set as the process start
	 *
	 * @throws IllegalArgumentException If the given step does not exist in this
	 *                                  process
	 */
	private void setStart(ProcessStep rStartStep)
	{
		if (aProcessSteps.containsKey(rStartStep.getName()))
		{
			rCurrentStep = rStartStep;
		}
		else
		{
			throw new IllegalArgumentException("Starting step not found: " +
											   rStartStep);
		}
	}

	/***************************************
	 * Internal method to check whether an exception is already an instance of
	 * {@link ProcessException} and can simply be re-thrown or needs to wrapped
	 * into one.
	 *
	 * @param  e            The exception to wrap or type-cast
	 * @param  sMessage     The error message
	 * @param  rMessageArgs An optional list of message arguments to format into
	 *                      the error message
	 *
	 * @return The original or wrapped exception
	 */
	private ProcessException wrapException(Exception e,
										   String    sMessage,
										   Object... rMessageArgs)
	{
		ProcessException eResult;

		if (e instanceof ProcessException)
		{
			eResult = (ProcessException) e;
		}
		else if (e instanceof RuntimeProcessException)
		{
			eResult =
				new ProcessException(((RuntimeProcessException) e)
									 .getProcessStep(),
									 String.format(sMessage, rMessageArgs),
									 e);
		}
		else
		{
			eResult =
				new ProcessException(rCurrentStep,
									 String.format(sMessage, rMessageArgs),
									 e);
		}

		setParameter(PROCESS_EXCEPTION, eResult);
		notifyListeners(ProcessEventType.FAILED);

		return eResult;
	}

	/***************************************
	 * Serializes this instance into the given stream.
	 *
	 * @param      rOut The stream to store this process' state into
	 *
	 * @throws     IOException In case of I/O errors
	 *
	 * @serialData First writes out the default form of the non-transient
	 *             fields, followed by the count of all steps in this process
	 *             and the steps themselves. After this the size of the
	 *             execution stack, the names of the steps on it (from bottom to
	 *             top) and finally the name of the current step is written.
	 */
	private void writeObject(ObjectOutputStream rOut) throws IOException
	{
		// write non-transient fields: interaction handler and flags
		rOut.defaultWriteObject();

		// write all steps from the process step map:
		rOut.writeInt(aProcessSteps.size());

		for (ProcessStep rStep : aProcessSteps.values())
		{
			rOut.writeObject(rStep);
		}

		// write the names of all steps on the execution stack
		rOut.writeInt(aExecutionStack.size());

		for (ProcessStep rStep : aExecutionStack)
		{
			rOut.writeObject(rStep.getName());
		}

		if (!(rCurrentStep == null))
		{
			// finally write the name of the current step
			rOut.writeObject(rCurrentStep.getName());
		}
	}
}
