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

import de.esoco.process.ProcessException;


/********************************************************************
 * Provides a UI to control the execution of a process and display information
 * about the current execution state.
 *
 * @author eso
 */
public class ProcessControlFragment extends InteractionFragment
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * TODO: DOCUMENT ME!
	 */
	public enum ProcessControlAction
	{
		PREVIOUS(ProcessExecutionMode.ROLLBACK),
		NEXT(ProcessExecutionMode.EXECUTE), CANCEL(ProcessExecutionMode.CANCEL),
		RELOAD(ProcessExecutionMode.RELOAD);

		//~ Instance fields ----------------------------------------------------

		private final ProcessExecutionMode eExecutionMode;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param eExecutionMode The associated execution mode
		 */
		private ProcessControlAction(ProcessExecutionMode eExecutionMode)
		{
			this.eExecutionMode = eExecutionMode;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the associated execution mode.
		 *
		 * @return The execution mode
		 */
		public final ProcessExecutionMode getExecutionMode()
		{
			return eExecutionMode;
		}
	}

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private ProcessExecutionHandler rExecutionHandler;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rExecutionHandler The handler to be notified of process execution
	 *                          events
	 */
	public ProcessControlFragment(ProcessExecutionHandler rExecutionHandler)
	{
		this.rExecutionHandler = rExecutionHandler;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		buttons(ProcessControlAction.class).onAction(a ->
													 rExecutionHandler
													 .executeProcess(a.getExecutionMode()));
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * An interface that defines how a process can be executed.
	 *
	 * @author eso
	 */
	@FunctionalInterface
	public static interface ProcessExecutionHandler
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Executes the process with the given execution mode.
		 *
		 * @param  eMode The process execution mode
		 *
		 * @throws ProcessException If the process execution fails
		 */
		public void executeProcess(ProcessExecutionMode eMode)
			throws ProcessException;
	}
}
