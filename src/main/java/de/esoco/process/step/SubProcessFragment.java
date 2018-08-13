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
package de.esoco.process.step;

import de.esoco.data.process.ProcessState.ProcessExecutionMode;

import de.esoco.lib.property.LayoutType;

import de.esoco.process.FragmentInteraction;
import de.esoco.process.Process;
import de.esoco.process.ProcessDefinition;
import de.esoco.process.ProcessException;
import de.esoco.process.ProcessManager;
import de.esoco.process.ProcessStep;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.step.ProcessControlFragment.ProcessExecutionHandler;

import java.util.Collection;
import java.util.List;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.StateProperties.STRUCTURE_CHANGED;

import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTIVE_INPUT_PARAM;
import static de.esoco.process.ProcessRelationTypes.PROCESS;


/********************************************************************
 * An interaction fragment that executes a sub-process in the current process
 * and renders it's interactions. This allows to use arbitrary interactive
 * processes inside of other interactions. If the fragment's process consists of
 * multiple steps it needs to handle the transitions between steps by itself
 * because there is no standard process navigation UI for sub-process
 * interactions.
 *
 * <p>This fragment set's it's layout to {@link LayoutType#INLINE INLINE} so
 * that it's interaction parameters will be inserted directly into the parent
 * fragment. If that is not appropriate, e.g. if a single component is required
 * for the parent content, this fragment should be wrapped in another fragment
 * with the corresponding layout.</p>
 *
 * @author eso
 */
public class SubProcessFragment extends InteractionFragment
	implements ProcessExecutionHandler
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Class<? extends ProcessDefinition> rProcessClass;

	private Process rProcess;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rSubProcessClass The class of the sub-process to execute in this
	 *                         fragment
	 */
	public SubProcessFragment(
		Class<? extends ProcessDefinition> rSubProcessClass)
	{
		displayProcess(rSubProcessClass);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void cleanup()
	{
		if (rProcess != null)
		{
			rProcess.execute(ProcessExecutionMode.CANCEL);
			setProcess(null);
		}
	}

	/***************************************
	 * Sets the process definition of this fragment. If a process is already
	 * running it will be cancelled first. If the fragment has already been
	 * initialized a new process based on the new definition will be executed
	 * and the UI will be initialized accordingly.
	 *
	 * @param  rSubProcessClass The process definition class
	 *
	 * @throws ProcessException If executing the new process fails
	 */
	public void displayProcess(
		Class<? extends ProcessDefinition> rSubProcessClass)
	{
		this.rProcessClass = rSubProcessClass;

		cleanup();

		if (isInitialized())
		{
			try
			{
				init();
			}
			catch (ProcessException e)
			{
				throw new RuntimeProcessException(this, e);
			}
		}
	}

	/***************************************
	 * Executes the process of this fragment with a particular execution mode.
	 *
	 * @param  eMode The process execution mode
	 *
	 * @throws ProcessException If the execution fails
	 */
	@Override
	public void executeProcess(ProcessExecutionMode eMode)
		throws ProcessException
	{
		if (rProcess != null)
		{
			List<RelationType<?>> rInteractionParams =
				getInteractionParameters();

			Collection<RelationType<?>> rInputParams = getInputParameters();

			rProcess.execute(eMode);

			rInteractionParams.clear();
			rInputParams.clear();

			if (rProcess.isFinished())
			{
				setProcess(null);
			}
			else
			{
				ProcessStep rInteractionStep = rProcess.getInteractionStep();

				if (rInteractionStep instanceof FragmentInteraction)
				{
					if (((FragmentInteraction) rInteractionStep)
						.getRootFragmentParam().has(STRUCTURE_CHANGED))
					{
						structureModified();
					}
				}

				rInteractionParams.addAll(rInteractionStep.get(INTERACTION_PARAMS));
				rInputParams.addAll(rInteractionStep.get(INPUT_PARAMS));
			}
		}
	}

	/***************************************
	 * Returns the current fragment process. This will only return a value after
	 * the fragment has been initialized.
	 *
	 * @return The fragment process (NULL if not yet initialized)
	 */
	public final Process getFragmentProcess()
	{
		return rProcess;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		rProcess.setParameter(INTERACTIVE_INPUT_PARAM, rInteractionParam);
		executeProcess(ProcessExecutionMode.EXECUTE);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasInteraction(RelationType<?> rInteractionParam)
	{
		return true;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws ProcessException
	{
		// simply render the process inline to it's parent fragment
		layout(LayoutType.INLINE);

		if (rProcessClass != null)
		{
			setProcess(ProcessManager.getProcess(rProcessClass));

			executeProcess(ProcessExecutionMode.EXECUTE);
		}
		else
		{
			getInteractionParameters().clear();
			getInputParameters().clear();

			label("lblNoProcessSet");
		}
	}

	/***************************************
	 * @see InteractionFragment#rollback()
	 */
	@Override
	protected void rollback() throws Exception
	{
		cleanup();
	}

	/***************************************
	 * Sets a new process for this instance.
	 *
	 * @param rNewProcess The new process or NULL for none
	 */
	protected void setProcess(Process rNewProcess)
	{
		rProcess = rNewProcess;
		fragmentParam().annotate(PROCESS, rProcess);
	}
}
