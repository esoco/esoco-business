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
package de.esoco.process.step;

import de.esoco.data.process.ProcessState.ProcessExecutionMode;

import de.esoco.lib.property.Layout;

import de.esoco.process.Process;
import de.esoco.process.ProcessDefinition;
import de.esoco.process.ProcessException;
import de.esoco.process.ProcessManager;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.step.ProcessControlFragment.ProcessExecutionHandler;

import java.util.Collection;
import java.util.List;

import org.obrel.core.RelationType;

import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAMS;


/********************************************************************
 * An interaction fragment that executes a sub-process in the current process
 * and renders it's interactions
 *
 * @author eso
 */
public class SubProcessInteractionFragment extends InteractionFragment
	implements ProcessExecutionHandler
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Class<? extends ProcessDefinition> rProcessClass;
	private Process							   rProcess;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rSubProcessClass The class of the sub-process to execute in this
	 *                         fragment
	 */
	public SubProcessInteractionFragment(
		Class<? extends ProcessDefinition> rSubProcessClass)
	{
		setProcessDefinition(rSubProcessClass);
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
			rProcess.cancel();
			rProcess = null;
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
		List<RelationType<?>> rInteractionParams = getInteractionParameters();

		Collection<RelationType<?>> rInputParams = getInputParameters();

		rInteractionParams.clear();
		rInputParams.clear();

		rProcess.execute(eMode);

		if (rProcess.isFinished())
		{
			rProcess = null;
		}
		else
		{
			rInteractionParams.addAll(rProcess.getInteractionStep()
									  .get(INTERACTION_PARAMS));
			rInputParams.addAll(rProcess.getInteractionStep()
								.get(INPUT_PARAMS));
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
	public void init() throws ProcessException
	{
		layout(Layout.FILL);

		if (rProcessClass != null)
		{
			rProcess = ProcessManager.getProcess(rProcessClass);

			executeProcess(ProcessExecutionMode.EXECUTE);
		}
		else
		{
			getInteractionParameters().clear();
			getInputParameters().clear();

			label("No Process");
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
	public void setProcessDefinition(
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
	 * @see InteractionFragment#rollback()
	 */
	@Override
	protected void rollback() throws Exception
	{
		cleanup();
	}
}
