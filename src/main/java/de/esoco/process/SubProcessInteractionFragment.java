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
package de.esoco.process;

import de.esoco.process.step.InteractionFragment;


/********************************************************************
 * An interaction fragment that executes a sub-process in the current process
 * and renders it's interactions
 *
 * @author eso
 */
public class SubProcessInteractionFragment extends InteractionFragment
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
		this.rProcessClass = rSubProcessClass;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see de.esoco.process.step.InteractionFragment#cleanup()
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
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		rProcess = ProcessManager.getProcess(rProcessClass);
		rProcess.execute();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void prepareInteraction() throws Exception
	{
		super.prepareInteraction();
	}

	/***************************************
	 * @see de.esoco.process.step.InteractionFragment#canRollback()
	 */
	@Override
	protected boolean canRollback()
	{
		return rProcess.canRollbackToPreviousInteraction();
	}

	/***************************************
	 * @see de.esoco.process.step.InteractionFragment#rollback()
	 */
	@Override
	protected void rollback() throws Exception
	{
		rProcess.rollbackToPreviousInteraction();
	}
}
