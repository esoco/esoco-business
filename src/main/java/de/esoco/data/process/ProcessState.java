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
package de.esoco.data.process;

import de.esoco.data.element.DataElement;
import de.esoco.data.element.DataElementList;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.text.TextConvert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A process description subclass that provides additional information about the
 * current state of a process during it's execution. It also overrides the
 * methods {@link DataElement#equals(Object)} and {@link DataElement#hashCode()}
 * to only check an internal, unique and immutable ID that is assigned during
 * instance creation. This allows to use process state objects as map keys.
 *
 * @author eso
 */
public class ProcessState extends ProcessDescription {

	/**
	 * An enumeration of the process execution modes.
	 */
	public enum ProcessExecutionMode {
		EXECUTE, RELOAD, ROLLBACK, CANCEL
	}

	/**
	 * An enumeration of flags that describe the current state of the process
	 * and the interactive step at which the process paused. The possible
	 * values
	 * are:
	 *
	 * <ul>
	 *   <li>{@link #ROLLBACK}: The process can perform a rollback to the
	 *     previous interactive step.</li>
	 *   <li>{@link #AUTO_CONTINUE}: The step only displays data and expects
	 *     automatic re-execution of the process.</li>
	 *   <li>{@link #FINAL_STEP}: The step is the last interaction and the
	 *     process will be finished after the next re-execution.</li>
	 *   <li>{@link #HAS_IMMEDIATE_INTERACTION}: The step contains parameters
	 *     that will cause an immediate re-execution of the process. This
	 *     can be
	 *     used as a hint to the user interface that no separate execution
	 *     control needs to be displayed.</li>
	 * </ul>
	 */
	public enum ProcessStateFlag {
		ROLLBACK, AUTO_CONTINUE, FINAL_STEP, HAS_IMMEDIATE_INTERACTION
	}

	/**
	 * The name of the data element list that contains the interaction
	 * parameter
	 * data elements.
	 */
	public static final String INTERACTION_PARAMS_LIST = "InteractionParams";

	/**
	 * The name of the data element list that contains the data elements for
	 * additional views.
	 */
	public static final String VIEW_PARAMS_LIST = "InteractionParams";

	private static final long serialVersionUID = 1L;

	// fields are package protected to be accessible by custom field serializer
	int processId;

	String processInfo;

	String currentStep;

	List<DataElementList> viewParams;

	List<ProcessState> spawnProcesses;

	ProcessExecutionMode executionMode;

	InteractionEventType interactionEventType;

	DataElement<?> interactionElement;

	List<DataElement<?>> interactionParams;

	Set<ProcessStateFlag> currentStepFlags = Collections.emptySet();

	/**
	 * Creates a new instance for a finished process without any further
	 * interaction.
	 *
	 * @see #ProcessState(ProcessDescription, int, String, String, List, List,
	 * List, Set)
	 */
	public ProcessState(ProcessDescription description, int processId,
		String processInfo) {
		this(description, processId, processInfo, null, null, null, null,
			Collections.emptySet());
	}

	/**
	 * Creates an copy instance for process interactions.
	 *
	 * @param originalState      The original process state
	 * @param eventType          The interaction event type
	 * @param interactionElement The data element that caused the interaction
	 * @param modifiedParams     The parameters that have been modified since
	 *                           the last interaction
	 */
	public ProcessState(ProcessState originalState,
		InteractionEventType eventType, DataElement<?> interactionElement,
		List<DataElement<?>> modifiedParams) {
		super(originalState.getName(), null, originalState.getDescriptionId(),
			false);

		this.processId = originalState.processId;
		this.interactionEventType = eventType;
		this.interactionParams = new ArrayList<>(modifiedParams.size());
		this.interactionElement = interactionElement != null ?
		                          interactionElement.copy(CopyMode.FLAT) :
		                          null;

		for (DataElement<?> element : modifiedParams) {
			interactionParams.add(element.copy(CopyMode.FLAT));
		}
	}

	/**
	 * Creates a new instance by copying the data from another process
	 * description (or state) and specific process state attributes.
	 *
	 * @param description       The process description or state
	 * @param processId         The ID of the described process
	 * @param processInfo       An information string describing the process
	 * @param currentStep       The name of the currently executed process step
	 * @param interactionParams The interaction parameter data elements
	 * @param viewParams        The optional view parameter data elements (NULL
	 *                          for none)
	 * @param spawnProcesses    An optional list of new processes to be
	 *                          displayed by the client (NULL for none)
	 * @param currentStepFlags  The flags for the current step
	 */
	public ProcessState(ProcessDescription description, int processId,
		String processInfo, String currentStep,
		List<DataElement<?>> interactionParams,
		List<DataElementList> viewParams, List<ProcessState> spawnProcesses,
		Set<ProcessStateFlag> currentStepFlags) {
		super(description);

		this.processId = processId;
		this.currentStep = currentStep;
		this.processInfo = processInfo;
		this.interactionParams = interactionParams;
		this.viewParams = viewParams;
		this.spawnProcesses = spawnProcesses;
		this.currentStepFlags = currentStepFlags;
	}

	/**
	 * Default constructor for serialization.
	 */
	ProcessState() {
	}

	/**
	 * Returns the rollback state of the current process.
	 *
	 * @return TRUE if the current process can be rolled back to a previous
	 * interaction
	 */
	public final boolean canRollback() {
		return currentStepFlags.contains(ProcessStateFlag.ROLLBACK);
	}

	/**
	 * Returns the name of the currently executed process step.
	 *
	 * @return The currently executed process step
	 */
	public final String getCurrentStep() {
		return currentStep;
	}

	/**
	 * Returns the process execution mode.
	 *
	 * @return The process execution mode
	 */
	public final ProcessState.ProcessExecutionMode getExecutionMode() {
		return executionMode;
	}

	/**
	 * Returns the data element that caused the re-execution of the process
	 * after during an interaction because of an interactive input event. In
	 * all
	 * other cases NULL will be returned.
	 *
	 * @return The data element that caused an interactive input event or NULL
	 * for a non-interactive re-execution of the process
	 */
	public final DataElement<?> getInteractionElement() {
		return interactionElement;
	}

	/**
	 * Returns TRUE if an interaction event has been caused by an action event
	 * or FALSE for a continuous selection event.
	 *
	 * @return TRUE for an action event, FALSE for a continuous selection event
	 */
	public final InteractionEventType getInteractionEventType() {
		return interactionEventType;
	}

	/**
	 * Returns the data elements that represent the interaction parameters of
	 * the current process step.
	 *
	 * @return A list containing the interaction parameter data elements (empty
	 * for none)
	 */
	public List<DataElement<?>> getInteractionParams() {
		return interactionParams != null ?
		       interactionParams :
		       Collections.emptyList();
	}

	/**
	 * Returns the ID of the process.
	 *
	 * @return The process ID
	 */
	public final int getProcessId() {
		return processId;
	}

	/**
	 * Returns an information string for the process or it's current state.
	 *
	 * @return The process information
	 */
	public final String getProcessInfo() {
		return processInfo;
	}

	/**
	 * Returns a list of process states for processes that need to be spawned
	 * indepently from the current process context.
	 *
	 * @return A list containing the process states of the processes to spawn
	 * (empty for none)
	 */
	public List<ProcessState> getSpawnProcesses() {
		return spawnProcesses != null ?
		       spawnProcesses :
		       Collections.emptyList();
	}

	/**
	 * Returns the data elements that represent the additional views in an
	 * interactive process step.
	 *
	 * @return A list containing the view parameter data elements
	 */
	public List<DataElementList> getViewParams() {
		return viewParams != null ? viewParams : Collections.emptyList();
	}

	/**
	 * Returns TRUE if the current step contains interaction parameters that
	 * will cause an immediate re-execution of the process and therefore
	 * doesn't
	 * need a special interaction control ("next button") to proceed.
	 *
	 * @return TRUE if immediate execution parameters are present
	 */
	public final boolean hasImmedidateInteraction() {
		return currentStepFlags.contains(
			ProcessStateFlag.HAS_IMMEDIATE_INTERACTION);
	}

	/**
	 * Returns TRUE if the process execution should be continued automatically
	 * after an interaction.
	 *
	 * @return The auto continue flag
	 */
	public final boolean isAutoContinue() {
		return currentStepFlags.contains(ProcessStateFlag.AUTO_CONTINUE);
	}

	/**
	 * Returns TRUE if the current step is the final interactive step in the
	 * process.
	 *
	 * @return TRUE for the final interactive step
	 */
	public final boolean isFinalStep() {
		return currentStepFlags.contains(ProcessStateFlag.FINAL_STEP);
	}

	/**
	 * Checks whether the process has finished execution. This will be the case
	 * if the name of the current step as returned by {@link #getCurrentStep()}
	 * is NULL.
	 *
	 * @return TRUE if the process is finished
	 */
	public boolean isFinished() {
		return currentStep == null;
	}

	/**
	 * Sets the execution mode. This method is intended to be used by
	 * clients to
	 * send the process execution mode to the server.
	 *
	 * @param executionMode The new execution mode
	 */
	public final void setExecutionMode(ProcessExecutionMode executionMode) {
		this.executionMode = executionMode;
	}

	/**
	 * Updates the ID of the process.
	 *
	 * @param processId The new process id
	 */
	public final void setProcessId(int processId) {
		this.processId = processId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("boxing")
	public String toString() {
		return TextConvert.format("%s-%s[%s]", getName(), processId,
			currentStep);
	}
}
