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

import static de.esoco.process.ProcessRelationTypes.SUB_PROCESS_DEFINITION;
import static de.esoco.process.ProcessRelationTypes.SUB_PROCESS_SEPARATE_CONTEXT;


/********************************************************************
 * A process step implementation that invokes another process.
 *
 * @author eso
 */
public class SubProcessStep extends ProcessStep
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Process aSubProcess			   = null;
	private boolean bCanRollbackSubProcess = true;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public SubProcessStep()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Performs a rollback of the sub-process.
	 *
	 * @see ProcessStep#abort()
	 */
	@Override
	protected void abort() throws Exception
	{
		rollback();
	}

	/***************************************
	 * Cancels the sub-process.
	 *
	 * @throws ProcessException If canceling the sub-process fails
	 *
	 * @see    ProcessStep#cancel()
	 */
	@Override
	protected void cancel() throws ProcessException
	{
		if (aSubProcess != null)
		{
			aSubProcess.cancel();
		}
	}

	/***************************************
	 * Checks whether the sub-process can be rolled back completely.
	 *
	 * @return TRUE if a rollback of the sub-process is possible
	 *
	 * @see    ProcessStep#canRollback()
	 */
	@Override
	protected boolean canRollback()
	{
		boolean bCanRollback = bCanRollbackSubProcess && aSubProcess != null;

		if (bCanRollback)
		{
			ProcessStep rFirstStep = aSubProcess.getFirstStep();

			bCanRollback =
				rFirstStep != aSubProcess.getCurrentStep() &&
				aSubProcess.canRollbackTo(rFirstStep);
		}

		return bCanRollback;
	}

	/***************************************
	 * Empty implementation because the sub-process execution already takes
	 * place in the {@link #prepareParameters()} method. This is necessary to
	 * check for interactions in the sub-process.
	 *
	 * @see ProcessStep#execute()
	 */
	@Override
	protected void execute() throws ProcessException
	{
	}

	/***************************************
	 * Overridden to create the sub-process if necessary and to execute it.
	 *
	 * @see ProcessStep#needsInteraction()
	 */
	@Override
	protected boolean needsInteraction() throws Exception
	{
		if (aSubProcess == null || aSubProcess.isFinished())
		{
			ProcessDefinition rSubProcessDefinition =
				checkParameter(SUB_PROCESS_DEFINITION);

			aSubProcess = rSubProcessDefinition.createProcess();

			if (!hasFlagParameter(SUB_PROCESS_SEPARATE_CONTEXT))
			{
				aSubProcess.setContext(getProcess());
			}
		}

		return !executeSubProcess();
	}

	/***************************************
	 * Executes the sub-process again after a process suspension that had been
	 * caused by the {@link #prepareParameters()} method.
	 *
	 * @see ProcessStep#resume()
	 */
	@Override
	protected boolean resume() throws ProcessException
	{
		return executeSubProcess();
	}

	/***************************************
	 * Overridden to rollback the sub-process.
	 *
	 * @throws ProcessException If the rollback fails
	 */
	@Override
	protected void rollback() throws ProcessException
	{
		ProcessStep rFirstStep = aSubProcess.getFirstStep();

		if (rFirstStep != aSubProcess.getCurrentStep())
		{
			aSubProcess.rollbackTo(rFirstStep);
		}

		aSubProcess = null;
	}

	/***************************************
	 * Checks whether the sub-process can be rolled back to a previous
	 * interaction.
	 *
	 * @see ProcessStep#canRollbackToPreviousInteraction()
	 */
	@Override
	boolean canRollbackToPreviousInteraction()
	{
		return aSubProcess != null &&
			   aSubProcess.canRollbackToPreviousInteraction();
	}

	/***************************************
	 * Returns the current interaction step from the sub-process.
	 *
	 * @see ProcessStep#getInteractionStep()
	 */
	@Override
	ProcessStep getInteractionStep()
	{
		if (aSubProcess == null)
		{
			throw new IllegalStateException("No current interaction");
		}

		return aSubProcess.getInteractionStep();
	}

	/***************************************
	 * Performs a rollback to the sub-process' previous interaction.
	 *
	 * @see ProcessStep#rollbackToPreviousInteraction()
	 */
	@Override
	void rollbackToPreviousInteraction() throws ProcessException
	{
		aSubProcess.rollbackToPreviousInteraction();
	}

	/***************************************
	 * Internal method to execute the sub-process that is managed by this
	 * instance.
	 *
	 * @return TRUE if the process should continue with the execution, FALSE if
	 *         an interaction is required
	 *
	 * @throws ProcessException If the execution fails
	 */
	private boolean executeSubProcess() throws ProcessException
	{
		if (!aSubProcess.getCurrentStep().canRollback())
		{
			// if a step cannot be rolled back the whole sub-process cannot
			// be rolled back subsequently; therefore assign only if FALSE
			bCanRollbackSubProcess = false;
		}

		aSubProcess.execute();

		boolean bFinished = aSubProcess.isFinished();

		if (bFinished)
		{
			aSubProcess.setContext(null);
			aSubProcess = null;
		}

		// If the sub-process stops but has not finished it has stopped at an
		// intermediate step which requires an interaction
		return bFinished;
	}
}
