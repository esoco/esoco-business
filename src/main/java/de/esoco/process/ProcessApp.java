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

import de.esoco.lib.app.CommandLine;
import de.esoco.lib.app.CommandLineException;
import de.esoco.lib.app.Service;
import de.esoco.lib.logging.LogLevel;


/********************************************************************
 * An application class that can be run to execute a {@link Process} with a
 * {@link ProcessRunner} instance.
 *
 * @author eso
 */
public class ProcessApp extends Service
{
	//~ Static fields/initializers ---------------------------------------------

	private static final String ARG_PROCESS = "process";

	//~ Instance fields --------------------------------------------------------

	private ProcessRunner aProcessRunner;

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * The main method of this application.
	 *
	 * @param rArgs The application arguments
	 */
	public static void main(String[] rArgs)
	{
		new ProcessApp().run(rArgs);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void configure(CommandLine rCommandLine) throws Exception
	{
		aProcessRunner.setLogLevel(LogLevel.INFO);
	}

	/***************************************
	 * Creates the {@link ProcessRunner} instance that will be used to run the
	 * process of this application. Can be overridden by subclasses that need to
	 * run a sub-class of {@link ProcessRunner}.
	 *
	 * @param  rCommandLine The application command line
	 *
	 * @return A process runner instance
	 *
	 * @throws Exception If creating the runner fails
	 */
	@SuppressWarnings("unchecked")
	protected ProcessRunner createProcessRunner(CommandLine rCommandLine)
		throws Exception
	{
		ProcessRunner aRunner = null;

		String sProcess = rCommandLine.requireOption(ARG_PROCESS).toString();

		try
		{
			Class<?> rProcessClass = Class.forName(sProcess);

			if (ProcessDefinition.class.isAssignableFrom(rProcessClass))
			{
				ProcessDefinition rProcessDef =
					ProcessManager.getProcessDefinition((Class<? extends ProcessDefinition>)
														rProcessClass);

				aRunner = new ProcessRunner(rProcessDef);
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new CommandLineException(String.format("Could not find process class '%s'",
														 sProcess),
										   ARG_PROCESS,
										   e);
		}

		if (aRunner != null)
		{
			return aRunner;
		}
		else
		{
			throw new CommandLineException("Missing argument %s", ARG_PROCESS);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void initialize(CommandLine rCommandLine) throws Exception
	{
		aProcessRunner = createProcessRunner(rCommandLine);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void runService() throws Exception
	{
		aProcessRunner.run();
	}
}
