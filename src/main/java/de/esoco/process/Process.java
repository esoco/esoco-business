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
package de.esoco.process;

import de.esoco.data.process.ProcessState.ProcessExecutionMode;
import de.esoco.entity.ConcurrentEntityModificationException;
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
import org.obrel.core.RelatedObject;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;
import org.obrel.core.SerializableRelatedObject;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static de.esoco.history.HistoryManager.HISTORIZED;
import static de.esoco.process.ProcessRelationTypes.AUTO_CONTINUE;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_CLEANUP_ACTIONS;
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

/**
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
 * <p>The state of a process and it's steps is defined with parameters that
 * must be set on the process instance. These parameters are stored as relations
 * of the process context and the types of the process parameter are therefore
 * defined as relation types. The process parameters can either be accessed
 * through the standard relation access methods or, for better readability,
 * through corresponding methods like {@link #getParameter(RelationType)}.</p>
 *
 * <p>Processes can be hierarchical which means that a process step can invoke
 * a sub-process (see {@link SubProcessStep} for details). A sub-process will
 * run in the context of the parent process, i.e. it will automatically access
 * the parameters of the process it had been invoked from. The own parameters of
 * a sub-process will be ignored in such a case.</p>
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
 * <p>Processes are serializable. This is not intended for long-term
 * persistence of processes but only for the temporary storage of processes,
 * e.g. during the deactivation of a server session that uses a process. The
 * serialization format is not guaranteed and may be different in future
 * versions.</p>
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
 * listener relation with the type
 * {@link ProcessRelationTypes#PROCESS_LISTENERS}. Calls to the listener
 * interface occur inside the process transaction so that any transactional code
 * inside the listener will be part of the transaction unless the listener
 * explicitly avoids the process transaction. Only if the process is canceled or
 * fails the calls to the corresponding listener methods will occur after the
 * transaction has been rolled back to allow the listener to record information
 * about the process termination.</p>
 *
 * @author eso
 */
public class Process extends SerializableRelatedObject {

	/**
	 * Internal enumeration for the distribution of process events.
	 */
	enum ProcessEventType implements BiConsumer<ProcessListener, Process> {
		CANCELED {
			@Override
			public void accept(ProcessListener listener, Process process) {
				listener.processCanceled(process);
			}
		}, FAILED {
			@Override
			public void accept(ProcessListener listener, Process process) {
				listener.processFailed(process);
			}
		}, FINISHED {
			@Override
			public void accept(ProcessListener listener, Process process) {
				listener.processFinished(process);
			}
		}, RESUMED {
			@Override
			public void accept(ProcessListener listener, Process process) {
				listener.processResumed(process);
			}
		}, STARTED {
			@Override
			public void accept(ProcessListener listener, Process process) {
				listener.processStarted(process);
			}
		}, SUSPENDED {
			@Override
			public void accept(ProcessListener listener, Process process) {
				listener.processSuspended(process);
			}
		}
	}

	/**
	 * Next step signal value for a process end-point
	 */
	public static final String PROCESS_END = "_PROCESS_END";

	static final String CLEANUP_KEY_UNLOCK_ENTITY = "UnlockEntity-";

	private static final long serialVersionUID = 1L;

	/**
	 * An internal process parameter that marks process steps that have
	 * signaled
	 * the actual need for an interaction.
	 */
	private static final RelationType<Boolean> STEP_WAS_INTERACTIVE =
		RelationTypes.newFlagType(RelationTypeModifier.PRIVATE);

	//- Relation types

	private static int nextProcessId = 1;

	static {
		RelationTypes.init(Process.class);
	}

	private final String processName;

	private final String uniqueProcessName;

	private Process context = null;

	private boolean initialized = false;

	private boolean suspended = false;

	private boolean rollbackRestart = false;

	private int historyLevel = 0;

	private int transactionLevel = 0;

	private int nextFragmentId = 0;

	private int nextParameterId = 0;

	private transient HashMap<String, ProcessStep> processSteps;

	private transient Stack<ProcessStep> executionStack;

	private transient ProcessStep currentStep;

	private ProcessInteractionHandler interactionHandler = null;

	private final Map<String, Consumer<Process>> cleanupActions =
		new LinkedHashMap<>();

	/**
	 * Package internal constructor, processes will only be created by
	 * ProcessDefinitions.
	 *
	 * @param name The name of the process
	 * @throws IllegalArgumentException If the name argument is NULL
	 */
	@SuppressWarnings("boxing")
	Process(String name) {
		if (name == null) {
			throw new IllegalArgumentException(
				"Process name must not be " + "NULL");
		}

		int id = nextProcessId++;

		processName = name;
		uniqueProcessName = name + "-" + id;

		setParameter(PROCESS, this);
		setParameter(PROCESS_ID, id);
		setParameter(PROCESS_NAME, processName);

		initFields();
	}

	/**
	 * Registers an cleanup action that will be executed when this process is
	 * finished, either by completing regularly (including cancelation) or by
	 * handling an error. If a different finish action is already registered
	 * under a particular key it will be replaced. Therefore invoking code must
	 * make sure to use unique keys or handle the replacement of actions in
	 * appropriate ways.
	 *
	 * <p>When invoked a cleanup action receives the associated process
	 * instance as it's argument to provide access to process parameters.
	 * Registered Actions can be removed with
	 * {@link #removeCleanupAction(String)}.</p>
	 *
	 * @param key    A key that identifies the action for later removal
	 * @param action The function to invoke on cleanup
	 */
	public void addCleanupAction(String key, Consumer<Process> action) {
		cleanupActions.put(key, action);
	}

	/**
	 * Adds an action that will be performed once before the next process
	 * execution after an interaction. After execution it will be removed from
	 * the list of cleanup actions.
	 *
	 * @param action A function performing the cleanup action
	 * @see #executeInteractionCleanupActions()
	 */
	public void addInteractionCleanupAction(Runnable action) {
		getParameter(INTERACTION_CLEANUP_ACTIONS).add(action);
	}

	/**
	 * Checks whether this process can be rolled back to a certain step. An
	 * application should invoke the method {@link #rollbackTo(ProcessStep)}
	 * for
	 * a certain step only if this method returns TRUE for that step. Else the
	 * rollback invocation will throw an exception.
	 *
	 * <p>This method is intended to be used with interactive processes that
	 * suspend execution when reaching interactive process steps. If the
	 * process
	 * has already finished execution when this method is invoked it returns
	 * FALSE.</p>
	 *
	 * @param step The step to check
	 * @return TRUE if a rollback to the given step is possible, FALSE if not
	 */
	public boolean canRollbackTo(ProcessStep step) {
		checkValidRollbackStep(step);

		ProcessStep checkStep = null;
		int steps = executionStack.size();
		boolean canRollback = true;

		while (canRollback && steps > 0 &&
			(checkStep = executionStack.get(--steps)) != step) {
			canRollback = checkStep.canRollback();
		}

		return canRollback;
	}

	/**
	 * Checks whether this process can be rolled back to a previously executed
	 * interactive step. Invokes the method {@link #canRollbackTo(ProcessStep)}
	 * if a previously executed interactive step could be found on the
	 * execution
	 * stack.
	 *
	 * <p>The method {@link #rollbackToPreviousInteraction()} should only be
	 * invoked by an application after checking that this method returns TRUE.
	 * Otherwise the rollback method may throw an exception.</p>
	 *
	 * @return TRUE if a rollback to a previously executed interactive step is
	 * possible, FALSE if not
	 */
	public boolean canRollbackToPreviousInteraction() {
		boolean canRollback = currentStep != null &&
			currentStep.canRollbackToPreviousInteraction();

		if (!canRollback) {
			ProcessStep previousStep = findPreviousInteractiveStep();

			canRollback = previousStep != null && canRollbackTo(previousStep);
		}

		return canRollback;
	}

	/**
	 * Overridden to forward the call to the process context.
	 *
	 * @see RelatedObject#deleteRelation(Relation)
	 */
	@Override
	public void deleteRelation(Relation<?> relation) {
		if (context != null) {
			context.deleteRelation(relation);
		} else {
			super.deleteRelation(relation);
		}
	}

	/**
	 * Executes this process according to a certain process execution mode.
	 *
	 * @param mode The execution mode
	 * @throws ProcessException If the process execution fails
	 */
	public void execute(ProcessExecutionMode mode) throws ProcessException {
		if (context == null) {
			// Set (only) the root process as the entity modification context.
			// If a process is spawned from another the first execution will
			// be in the same thread. Therefore keepExisting is set to TRUE to
			// not override the context of the starting process. On the next
			// interaction (on a separate thread) the context will be set to
			// the new process.
			EntityManager.setEntityModificationContext(uniqueProcessName, this,
				true);
		}

		try {
			switch (mode) {
				case RELOAD:
				case EXECUTE:
					execute();
					break;

				case ROLLBACK:
					rollbackToPreviousInteraction();
					execute();
					break;

				case CANCEL:
					cancel();
					break;
			}
		} finally {
			if (context == null) {
				// remove but ignore error of newly spawned process
				EntityManager.removeEntityModificationContext(uniqueProcessName,
					true);
			}
		}
	}

	/**
	 * Evaluates all cleanup action predicates and then removes them.
	 *
	 * @see #addInteractionCleanupAction(Runnable)
	 */
	public void executeInteractionCleanupActions() {
		List<Runnable> actions = getParameter(INTERACTION_CLEANUP_ACTIONS);

		actions.forEach(action -> action.run());
		actions.clear();
	}

	/**
	 * Overridden to forward the call to the process context.
	 *
	 * @see RelatedObject#get(RelationType)
	 */
	@Override
	public <T> T get(RelationType<T> type) {
		return context != null ? context.get(type) : super.get(type);
	}

	/**
	 * Returns the context this process runs in. This will either be this
	 * process itself or, in the case of sub-processes, the enclosing process
	 * context.
	 *
	 * @return The process context
	 */
	public final Process getContext() {
		return context != null ? context : this;
	}

	/**
	 * This method returns the current process step. It has been implemented
	 * for
	 * the use of interactive process steps. Returns NULL when no further steps
	 * will follow.
	 *
	 * @return The current step
	 */
	public ProcessStep getCurrentStep() {
		return currentStep;
	}

	/**
	 * Returns the first step of this process.
	 *
	 * @return The first process step
	 */
	public ProcessStep getFirstStep() {
		return executionStack.isEmpty() ?
		       currentStep :
		       executionStack.firstElement();
	}

	/**
	 * Returns the hierarchical name of this process, including any parent
	 * processes.
	 *
	 * @return The hierarchical process name
	 */
	public String getFullName() {
		String fullName = processName;

		if (context != null) {
			fullName = context.getFullName() + '.' + fullName;
		}

		return fullName;
	}

	/**
	 * Returns a unique integer ID for this process instance as stored in the
	 * process parameter {@link ProcessRelationTypes#PROCESS_ID}.
	 *
	 * @return The process ID
	 */
	@SuppressWarnings("boxing")
	public final int getId() {
		return get(PROCESS_ID);
	}

	/**
	 * This method returns the process step for the latest interaction of this
	 * process. It will only return a valid result if the process has
	 * previously
	 * been executed but the method {@link #isFinished()} still returns FALSE.
	 *
	 * @return The interaction step or NULL for none
	 */
	public ProcessStep getInteractionStep() {
		if (currentStep == null) {
			throw new IllegalStateException("No current interaction");
		}

		return currentStep.getInteractionStep();
	}

	/**
	 * Returns the name of this process as stored in the process parameter
	 * {@link StandardTypes#NAME}. This name represents the process class and
	 * may be the same for different process instances. For a unique process
	 * name see the method {@link #getUniqueProcessName()}.
	 *
	 * @return The process name
	 */
	public final String getName() {
		return processName;
	}

	/**
	 * Returns an integer ID for the automatic naming of process fragments.
	 * This
	 * method is intended to be used internally by the framework.
	 *
	 * @return The next generated fragment ID
	 */
	public int getNextFragmentId() {
		return getContext().nextFragmentId++;
	}

	/**
	 * Returns a certain process parameter. If no parameter with the given
	 * ID is
	 * set NULL will be returned.
	 *
	 * @param paramType The type of the parameter
	 * @return The parameter value or NULL
	 */
	public <T> T getParameter(RelationType<T> paramType) {
		return get(paramType);
	}

	/**
	 * Returns the process user entity that is stored in the process parameter
	 * with the type {@link ProcessRelationTypes#PROCESS_USER}.
	 *
	 * @return The process user or NULL for none
	 */
	public Entity getProcessUser() {
		return getParameter(PROCESS_USER);
	}

	/**
	 * Overridden to forward the call to the process context.
	 *
	 * @see RelatedObject#getRelation(RelationType)
	 */
	@Override
	public <T> Relation<T> getRelation(RelationType<T> type) {
		return context != null ?
		       context.getRelation(type) :
		       super.getRelation(type);
	}

	/**
	 * Overridden to forward the call to the process context.
	 *
	 * @see RelatedObject#getRelations(Predicate)
	 */
	@Override
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> filter) {
		return context != null ?
		       context.getRelations(filter) :
		       super.getRelations(filter);
	}

	/**
	 * Returns a process step with a certain name.
	 *
	 * @param name The name of the process step to be queried
	 * @return The corresponding process step instance or NULL if no matching
	 * step could be found or if the step name is {@link #PROCESS_END}
	 */
	public ProcessStep getStep(String name) {
		return PROCESS_END.equals(name) ? null : processSteps.get(name);
	}

	/**
	 * Returns a unique name for this process instance.
	 *
	 * @return The unique process name
	 */
	public final String getUniqueProcessName() {
		return uniqueProcessName;
	}

	/**
	 * A convenience method to check the existence and value of a parameter
	 * with
	 * a boolean datatype.
	 *
	 * @param flagType The flag parameter type
	 * @return TRUE if the flag exists and is set to TRUE
	 */
	public final boolean hasFlagParameter(RelationType<Boolean> flagType) {
		return hasFlag(flagType);
	}

	/**
	 * Queries if a certain parameter is stored in the process.
	 *
	 * @param paramType The type of the parameter
	 * @return TRUE if the parameter exists
	 */
	public boolean hasParameter(RelationType<?> paramType) {
		return hasRelation(paramType);
	}

	/**
	 * Indicates whether the process has finished execution or not.
	 *
	 * @return TRUE if the process has finished execution
	 */
	public boolean isFinished() {
		return currentStep == null;
	}

	/**
	 * Checks whether this process is running in the context of another process
	 * or if is a root process.
	 *
	 * @return TRUE if this process runs in the context of another process
	 */
	public final boolean isSubProcess() {
		return context != null;
	}

	/**
	 * Tries to acquire a modification lock on an entity for the remaining
	 * execution of this process. If successful, a cleanup action will be
	 * registered with {@link #addCleanupAction(String, Consumer)} that removes
	 * the lock if the process is finished.
	 *
	 * @param entity The entity to lock
	 * @return TRUE if the lock could be acquired, FALSE if the entity is
	 * already locked
	 */
	public final boolean lockEntity(Entity entity) {
		assert entity.isPersistent();

		boolean success = entity.lock();

		if (success) {
			addCleanupAction(CLEANUP_KEY_UNLOCK_ENTITY + entity.getGlobalId(),
				p -> entity.unlock());
		}

		return success;
	}

	/**
	 * Removes a cleanup action that has previously been registered through the
	 * method {@link #addCleanupAction(String, Consumer)}.
	 *
	 * @param key The key that identifies the action to remove
	 * @return The registered action or NULL for none
	 */
	public Consumer<Process> removeCleanupAction(String key) {
		return cleanupActions.remove(key);
	}

	/**
	 * Removes the current entity modification lock rule by invoking the method
	 * {@link EntityManager#removeEntityModificationLock(String)} with the
	 * ID of
	 * this process.
	 *
	 * @return The removed lock rule or NULL for none
	 */
	public Predicate<? super Entity> removeEntityModificationLock() {
		return EntityManager.removeEntityModificationLock(
			getContext().getUniqueProcessName());
	}

	/**
	 * Removes a certain parameter from the process. If the parameter doesn't
	 * exist the call is ignored.
	 *
	 * @param paramType The type of the parameter
	 */
	public void removeParameter(RelationType<?> paramType) {
		if (context != null) {
			context.deleteRelation(paramType);
		} else {
			super.deleteRelation(paramType);
		}
	}

	/**
	 * Overridden to forward the call to the process context.
	 *
	 * @see RelatedObject#set(RelationType, Object)
	 */
	@Override
	public <T> Relation<T> set(RelationType<T> type, T target) {
		return context != null ?
		       context.set(type, target) :
		       super.set(type, target);
	}

	/**
	 * Sets an entity modification lock rule through the entity manager method
	 * {@link EntityManager#setEntityModificationLock(String, Predicate)}. If a
	 * lock rule already exists it will be combined with the new rule with
	 * an or
	 * join. If that is not desired the current rule must be removed by
	 * invoking
	 * {@link #removeEntityModificationLock()}.
	 *
	 * <p>All rules set from a process instance will be automatically removed
	 * if a process execution ends in any way, i.e. successfully, by
	 * cancellation, or with an error.</p>
	 *
	 * @param lockRule The entity modification lock rule
	 */
	public void setEntityModificationLock(Predicate<? super Entity> lockRule) {
		String contextId = getContext().getUniqueProcessName();

		Predicate<? super Entity> currentRule =
			EntityManager.removeEntityModificationLock(contextId);

		lockRule = Predicates.or(currentRule, lockRule);

		EntityManager.setEntityModificationLock(contextId, lockRule);
	}

	/**
	 * Sets the process interaction handler for this context.
	 *
	 * @param handler The new interaction handler
	 */
	public void setInteractionHandler(ProcessInteractionHandler handler) {
		interactionHandler = handler;
	}

	/**
	 * Sets a process parameter. If the parameter already exists it's value
	 * will
	 * be replaced.
	 *
	 * @param param The parameter
	 * @param value The parameter value
	 * @return The relation of the parameter
	 */
	public <T> Relation<T> setParameter(RelationType<T> param, T value) {
		return set(param, value);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Process %s (current step: %s)", getName(),
			currentStep);
	}

	/**
	 * Removes an entity lock that had been acquired by a successful call to
	 * {@link #lockEntity(Entity)}. This will also remove the associated
	 * cleanup
	 * action.
	 *
	 * @param entity The entity to unlock
	 */
	public final void unlockEntity(Entity entity) {
		removeCleanupAction(CLEANUP_KEY_UNLOCK_ENTITY + entity.getGlobalId());
		entity.unlock();
	}

	/**
	 * Adds a process step to the internal list. The step is stored by it's
	 * name
	 * which identifies it only in the context of this particular process
	 * instance. Only one step with a certain name is allowed in a process
	 * instance.
	 *
	 * <p>If no starting step has been set the argument will be set as the
	 * first process step. This means that the first step added to a process
	 * will become it's starting step unless the start is changed later.</p>
	 *
	 * @param step The process step to add
	 * @throws IllegalArgumentException If a step with the same name exists
	 */
	void addStep(ProcessStep step) {
		if (processSteps.containsKey(step.getName())) {
			throw new IllegalArgumentException(
				"Duplicate process step name: " + step.getName());
		}

		processSteps.put(step.getName(), step);
		step.setProcess(this);

		if (currentStep == null) {
			setStart(step);
		}
	}

	/**
	 * Starts a transaction and optionally also a history group.
	 *
	 * @param withHistory TRUE to also start a history group inside the
	 *                    transaction
	 * @param target      The target entity for the history or NULL for none
	 * @param value       The history value or NULL
	 */
	void beginTransaction(boolean withHistory, Entity target, String value) {
		TransactionManager.begin();
		transactionLevel++;

		if (withHistory) {
			if (target == null) {
				Log.warn("Missing history target for " + this);
			}

			HistoryManager.begin(getParameter(PROCESS_USER), target, value);
			historyLevel++;
		}
	}

	/**
	 * Allows to cancel this process. This method can only be invoked on an
	 * interactive process that hasn't finished execution already. It will
	 * invoke the method {@link ProcessStep#cancel()} on all executed steps, by
	 * this undoing all persistent changes made so far. Finally, an active
	 * history group will be canceled and an open transaction will be rolled
	 * back.
	 */
	void cancel() {
		try {
			if (currentStep != null) {
				currentStep.cancel();
				currentStep.executeCleanupActions();
				currentStep.cleanup();
				currentStep = null;
			}

			for (int i = executionStack.size() - 1; i >= 0; i--) {
				executionStack.get(i).cancel();
			}

			suspended = false;

			notifyListeners(ProcessEventType.CANCELED);
			cleanup();
		} catch (Exception e) {
			handleException(ProcessExecutionMode.CANCEL, e);
		}
	}

	/**
	 * Commits a currently open transaction and optionally an open history
	 * group.
	 *
	 * @param withHistory TRUE to also commit a history group before the
	 *                    transaction
	 * @throws TransactionException If committing fails
	 * @throws ProcessException     If the transaction or history state is
	 *                              invalid
	 */
	void commitTransaction(boolean withHistory)
		throws TransactionException, ProcessException {
		if (transactionLevel > 0) {
			if (withHistory) {
				if (historyLevel > 0) {
					HistoryManager.commit(false);
					historyLevel--;
				} else {
					throw new ProcessException(currentStep,
						"No open history group");
				}
			}

			TransactionManager.commit();
			transactionLevel--;
		} else {
			throw new ProcessException(currentStep, "No open transaction");
		}
	}

	/**
	 * Executes the process by executing all the contained process steps in the
	 * order that has been defined by the process definition.
	 *
	 * @throws ProcessException If the execution fails
	 */
	void execute() throws ProcessException {
		if (currentStep == null) {
			throw new ProcessException(null, "ProcessFinished");
		}

		if (!initialized) {
			init();
			setParameter(PROCESS_START_TIME, new Date());
			notifyListeners(ProcessEventType.STARTED);
			initialized = true;
		}

		try {
			executeSteps();

			if (currentStep == null) {
				finish();
			} else {
				notifyListeners(ProcessEventType.SUSPENDED);
			}
		} catch (Exception e) {
			suspended = true;
			handleException(ProcessExecutionMode.EXECUTE, e);
		}
	}

	/**
	 * Executes actions that reset process states with error handling. This
	 * method is used internally by {@link #executeCleanupActions()} but can
	 * also be used by subclasses for similar house keeping tasks.
	 *
	 * @param actions A mapping from keys to the associated action to execute
	 */
	void executeActions(Map<String, Consumer<Process>> actions) {
		for (String key : actions.keySet()) {
			try {
				actions.get(key).accept(this);
			} catch (Exception e) {
				Log.errorf(e, "Process cleanup action failed: %s", key);
			}
		}

		actions.clear();
	}

	/**
	 * Returns the process interaction handler for this process.
	 *
	 * @return The interaction handler
	 */
	final ProcessInteractionHandler getInteractionHandler() {
		return getContext().interactionHandler;
	}

	/**
	 * Returns an integer ID for the automatic naming of process parameters.
	 *
	 * @return The next generated parameter ID
	 */
	final int getNextParameterId() {
		return getContext().nextParameterId++;
	}

	/**
	 * Registers a temporary parameter type for this process.
	 *
	 * @param tempParam The parameter relation type+
	 * @see #unregisterTemporaryParameterType(RelationType, boolean)
	 */
	@SuppressWarnings("boxing")
	void registerTemporaryParameterType(RelationType<?> tempParam) {
		Set<RelationType<?>> temporaryParamTypes =
			getParameter(TEMPORARY_PARAM_TYPES);

		if (!temporaryParamTypes.contains(tempParam)) {
			temporaryParamTypes.add(tempParam);

			// ensure that usage count update is atomic
			synchronized (tempParam) {
				tempParam.set(PARAM_USAGE_COUNT,
					tempParam.get(PARAM_USAGE_COUNT) + 1);
			}
		}
	}

	/**
	 * Unregisters all temporary parameter types that have been used in this
	 * process.
	 *
	 * @see #unregisterTemporaryParameterType(RelationType, boolean)
	 */
	final void removeTemporaryParameterTypes() {
		Set<RelationType<?>> temporaryParamTypes =
			getParameter(TEMPORARY_PARAM_TYPES);

		for (RelationType<?> tempParam : temporaryParamTypes) {
			unregisterTemporaryParameterType(tempParam, false);
		}

		temporaryParamTypes.clear();
	}

	/**
	 * Performs a rollback of all steps up to and including a certain step.
	 * This
	 * method is intended to be used with interactive processes that suspend
	 * execution when reaching interactive process steps. The process must not
	 * have finished execution completely when this method is invoked, else an
	 * exception will be thrown. The given process step must exist in this
	 * process and it must have been executed already. All process steps that
	 * need to be rolled back must support the rollback functionality, or else
	 * an exception will be thrown.
	 *
	 * <p>When this method finishes, the process execution stack has been reset
	 * so that the argument step will be executed next if the
	 * {@link #execute()}
	 * method is invoked again.</p>
	 *
	 * @param step The step to roll the process back to
	 */
	void rollbackTo(ProcessStep step) {
		checkValidRollbackStep(step);

		ProcessStep rollbackStep;

		try {
			currentStep.resetParameters();
			currentStep.abort();

			do {
				rollbackStep = executionStack.pop();

				rollbackStep.resetParameters();
				rollbackStep.rollback();
			} while (step != rollbackStep);
		} catch (Exception e) {
			handleException(ProcessExecutionMode.ROLLBACK, e);
		}

		currentStep = step;
		suspended = false;

		// if invoked from the interaction handler, the execution loop needs to
		// be restarted with the changed current step
		if (getInteractionHandler() != null) {
			rollbackRestart = true;
		}
	}

	/**
	 * Performs a rollback to the previous interactive step.
	 *
	 * @throws ProcessException If the rollback fails
	 */
	void rollbackToPreviousInteraction() throws ProcessException {
		if (currentStep != null &&
			currentStep.canRollbackToPreviousInteraction()) {
			currentStep.rollbackToPreviousInteraction();
		} else {
			ProcessStep rollbackStep = findPreviousInteractiveStep();

			if (rollbackStep != null) {
				rollbackTo(rollbackStep);
			} else {
				throw new ProcessException(currentStep,
					"No previous interactive step");
			}
		}
	}

	/**
	 * Internal method to set the context in which this process is executed.
	 * This is used for the execution of sub-processes in the context of a
	 * parent process.
	 *
	 * @param context The new process context
	 */
	final void setContext(Process context) {
		// invoke getContext() so that all sub-process contexts point to the
		// root, even in process hierarchies
		this.context = context != null ? context.getContext() : null;
	}

	/**
	 * Removes a temporary parameter relation type that had been used in this
	 * process.
	 *
	 * @param tempParam The temporary parameter relation type to remove
	 * @param remove    TRUE if it should also be removed from this process
	 * @see #registerTemporaryParameterType(RelationType)
	 */
	@SuppressWarnings("boxing")
	void unregisterTemporaryParameterType(RelationType<?> tempParam,
		boolean remove) {
		// ensure that usage count update is atomic
		synchronized (tempParam) {
			int usageCount = tempParam.get(PARAM_USAGE_COUNT);

			if (usageCount == 1) {
				RelationType.unregisterRelationType(tempParam);
				tempParam.deleteRelation(PARAM_USAGE_COUNT);
			} else {
				tempParam.set(PARAM_USAGE_COUNT, usageCount - 1);
			}
		}

		if (remove) {
			getParameter(TEMPORARY_PARAM_TYPES).remove(tempParam);
		}
	}

	/**
	 * Checks whether this process and the given process step are valid for a
	 * rollback. If not, an exception will be thrown.
	 *
	 * @param step The step to check
	 * @throws IllegalArgumentException If either this process or the given
	 * step
	 *                                  are not valid for a rollback
	 */
	private void checkValidRollbackStep(ProcessStep step) {
		if (step == null || !executionStack.contains(step)) {
			throw new IllegalArgumentException("InvalidRollbackStep: " + step);
		}
	}

	/**
	 * Performs a final cleanup of process resources when the process is
	 * terminated.
	 *
	 * @throws TransactionException If performing a transaction rollback fails
	 */
	private void cleanup() throws TransactionException {
		try {
			for (ProcessStep step : processSteps.values()) {
				if (executionStack.contains(step)) {
					step.executeCleanupActions();
					step.cleanup();
				}
			}

			executeCleanupActions();

			// only remove temporary types if root process is terminated
			if (context == null) {
				removeTemporaryParameterTypes();
			}

			if (historyLevel > 0) {
				HistoryManager.rollback();
				historyLevel = 0;
			}
		} finally {
			if (transactionLevel > 0) {
				TransactionManager.rollback();
				transactionLevel = 0;

				EntityManager.resetEntityModifications(this);
			}

			if (context == null) {
				EntityManager.removeEntityModificationLock(uniqueProcessName);
				EntityManager.checkUnsavedEntityModifications(uniqueProcessName,
					this);
			}
		}
	}

	/**
	 * Executes all actions that have previously been registered through the
	 * method {@link #addCleanupAction(String, Action)}.
	 */
	private void executeCleanupActions() {
		executeActions(cleanupActions);
	}

	/**
	 * Executes the process steps in a loop until the process is either
	 * suspended or finished.
	 *
	 * @throws Exception If the execution of a step fails
	 */
	private void executeSteps() throws Exception {
		do {
			boolean execute = true;

			if (suspended) {
				currentStep.validate();

				suspended = false;
				execute = currentStep.resume();

				notifyListeners(ProcessEventType.RESUMED);
			} else {
				execute = prepareStep(currentStep);

				if (rollbackRestart) {
					// flag will be set to TRUE by the rollbackTo method if
					// an interaction handler has caused the rollback; in
					// that case the execution loop must be restarted with
					// the modified current step from the rollback
					rollbackRestart = false;

					continue;
				}
			}

			if (execute) {
				ProcessStep nextStep = getStep(currentStep.perform());

				// only add step if progressing to next step, not on
				// interaction
				if (currentStep != null && nextStep != currentStep) {
					if (currentStep.canRollback()) {
						executionStack.push(currentStep);
					} else {
						// if no rollback possible remove unreachable entries
						// from stack; this will prevent the storing of
						// unnecessary object references, especially during
						// bulk processing or background processes
						executionStack.clear();
					}

					currentStep = nextStep;
				}
			} else {
				setParameter(PROCESS_SUSPEND_TIME, new Date());
				suspended = true;
			}
		} while (!suspended && currentStep != null);
	}

	/**
	 * Internal method to search the previous interactive step from the top of
	 * the execution stack.
	 *
	 * @return The previous interactive step or NULL if no such step exists on
	 * the execution stack
	 */
	private ProcessStep findPreviousInteractiveStep() {
		// start at last stack element; the current interactive step is not yet
		// on the stack because it will only be executed after the interaction
		int stepNo = executionStack.size();
		ProcessStep rollbackStep = null;

		while (stepNo > 0 && rollbackStep == null) {
			ProcessStep step = executionStack.get(--stepNo);

			if (step.hasFlag(STEP_WAS_INTERACTIVE) &&
				!step.hasFlag(AUTO_CONTINUE)) {
				rollbackStep = step;
			}
		}

		return rollbackStep;
	}

	/**
	 * Finishes the successful execution of this process.
	 *
	 * @throws TransactionException If committing an open transaction fails
	 * @throws ProcessException     If the transaction or history state is
	 *                              invalid
	 */
	private void finish() throws TransactionException, ProcessException {
		if (hasFlag(TRANSACTIONAL)) {
			commitTransaction(hasFlag(HISTORIZED));
		}

		if (historyLevel > 0) {
			@SuppressWarnings("boxing")
			String message =
				String.format("Uncommitted history levels(%d) in process %s",
					historyLevel, this);

			throw new ProcessException(currentStep, message);
		}

		if (transactionLevel > 0) {
			@SuppressWarnings("boxing")
			String message = String.format(
				"Uncommitted transaction levels (%d) in process %s",
				transactionLevel, this);

			throw new ProcessException(currentStep, message);
		}

		notifyListeners(ProcessEventType.FINISHED);

		ProcessScheduler processScheduler = getParameter(PROCESS_SCHEDULER);

		if (processScheduler != null) {
			for (Class<? extends ProcessDefinition> process : getParameter(
				RESUME_PROCESSES)) {
				processScheduler.resumeProcess(process);
			}
		}

		cleanup();
	}

	/**
	 * Handles exceptions that may occur during process execution.
	 *
	 * @param mode The process execution mode of the failed invocation
	 * @param e    The exception that occurred
	 * @throws ProcessException Always throws a process exception that has been
	 *                          created from the original exception
	 */
	private void handleException(ProcessExecutionMode mode, Exception e)
		throws ProcessException {
		if (!(e instanceof InvalidParametersException ||
			e instanceof ConcurrentEntityModificationException)) {
			try {
				cleanup();
			} catch (Exception cleanup) {
				// only log and then continue with original exception below
				Log.errorf(cleanup, "Error cleanup failed in %s", this);
			}
		}

		// TODO: introduce special error parameters and perform cleanup
		// of disposable/closeable parameters
		throw wrapException(e, "%s of %s failed", mode, this);
	}

	/**
	 * Performs the initialization of this process before execution.
	 */
	private void init() {
		boolean history = hasFlag(HISTORIZED);

		initParams();

		if (history || hasFlag(TRANSACTIONAL)) {
			beginTransaction(history, getParameter(HistoryRecord.TARGET),
				getName());
		}
	}

	/**
	 * Initializes the transient fields. Invoked from the constructors and by
	 * deserialization.
	 */
	private void initFields() {
		processSteps = new HashMap<String, ProcessStep>();
		executionStack = new Stack<ProcessStep>();
	}

	/**
	 * Initializes this process by evaluating it with the functions stored in
	 * the map{@link ProcessRelationTypes#PARAM_INITIALIZATIONS}. The resulting
	 * values are assigned to the process parameters stored in the map.
	 */
	@SuppressWarnings("unchecked")
	private void initParams() {
		Map<RelationType<?>, Function<? super Process, ?>> taskAttributeMap =
			getParameter(PARAM_INITIALIZATIONS);

		for (Entry<RelationType<?>, Function<? super Process, ?>> entry :
			taskAttributeMap.entrySet()) {
			RelationType<Object> param = (RelationType<Object>) entry.getKey();
			Function<Object, Object> function =
				(Function<Object, Object>) entry.getValue();

			Object value = function.evaluate(this);

			if (value != null) {
				setParameter(param, value);
			}
		}
	}

	/**
	 * Notifies registered listeners of a certain process event.
	 *
	 * @param eventType The type of event that occurred
	 */
	private void notifyListeners(ProcessEventType eventType) {
		if (context == null) {
			PROCESS_LISTENERS.notifyListeners(this, this, eventType);
		}
	}

	/**
	 * Internal method to prepare a process step for execution and to invoke
	 * the
	 * interaction handler if necessary. If no handler is available and
	 * interaction is required this method returns false to suspend the
	 * execution of this process.
	 *
	 * @param step The process step to prepare
	 * @return TRUE if the process can continue with the step execution, FALSE
	 * if it requires an external interaction
	 * @throws Exception If either preparing the step or invoking the
	 *                   interaction handler fails
	 */
	private boolean prepareStep(ProcessStep step) throws Exception {
		boolean continueExecution = step.prepareStep();

		if (!continueExecution) {
			step.set(STEP_WAS_INTERACTIVE);

			ProcessInteractionHandler interactionHandler =
				getInteractionHandler();

			if (interactionHandler != null) {
				interactionHandler.performInteraction(step);
				continueExecution = true;
			}
		}

		return continueExecution;
	}

	/**
	 * Reads this instance from the given stream.
	 *
	 * @param in The stream to read this process' state from
	 * @throws IOException            In case of I/O errors
	 * @throws ClassNotFoundException If a class could not be deserialized
	 */
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException {
		// read non-transient fields: interaction handler and flags
		in.defaultReadObject();

		initFields();

		// read all steps and put them in the process step map:
		int count = in.readInt();

		while (count-- > 0) {
			addStep((ProcessStep) in.readObject());
		}

		// read names of the steps on the execution stack and restore the stack
		count = in.readInt();

		while (count-- > 0) {
			executionStack.push(getStep((String) in.readObject()));
		}

		// finally read the name of the current step and restore it
		currentStep = getStep((String) in.readObject());
	}

	/**
	 * Defines the starting point (i.e., the first step) of the process. Only
	 * one start point can exist in a process. The step must have been added to
	 * this process already, else an exception will be thrown.
	 *
	 * @param startStep The step to set as the process start
	 * @throws IllegalArgumentException If the given step does not exist in
	 * this
	 *                                  process
	 */
	private void setStart(ProcessStep startStep) {
		if (processSteps.containsKey(startStep.getName())) {
			currentStep = startStep;
		} else {
			throw new IllegalArgumentException(
				"Starting step not found: " + startStep);
		}
	}

	/**
	 * Internal method to check whether an exception is already an instance of
	 * {@link ProcessException} and can simply be re-thrown or needs to wrapped
	 * into one.
	 *
	 * @param e           The exception to wrap or type-cast
	 * @param message     The error message
	 * @param messageArgs An optional list of message arguments to format into
	 *                    the error message
	 * @return The original or wrapped exception
	 */
	private ProcessException wrapException(Exception e, String message,
		Object... messageArgs) {
		ProcessException result;

		if (e instanceof ProcessException) {
			result = (ProcessException) e;
		} else if (e instanceof RuntimeProcessException) {
			result = new ProcessException(
				((RuntimeProcessException) e).getProcessStep(),
				String.format(message, messageArgs), e);
		} else {
			result = new ProcessException(currentStep,
				String.format(message, messageArgs), e);
		}

		setParameter(PROCESS_EXCEPTION, result);
		notifyListeners(ProcessEventType.FAILED);

		return result;
	}

	/**
	 * Serializes this instance into the given stream.
	 *
	 * @param out The stream to store this process' state into
	 * @throws IOException In case of I/O errors
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		// write non-transient fields: interaction handler and flags
		out.defaultWriteObject();

		// write all steps from the process step map:
		out.writeInt(processSteps.size());

		for (ProcessStep step : processSteps.values()) {
			out.writeObject(step);
		}

		// write the names of all steps on the execution stack
		out.writeInt(executionStack.size());

		for (ProcessStep step : executionStack) {
			out.writeObject(step.getName());
		}

		if (!(currentStep == null)) {
			// finally write the name of the current step
			out.writeObject(currentStep.getName());
		}
	}
}
