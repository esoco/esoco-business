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

/**
 * An service implementation that executes a {@link Process}.
 *
 * @author eso
 */
public class ProcessService extends Service {

	private static final String ARG_PROCESS = "process";

	private ProcessRunner processRunner;

	/**
	 * The main method of this application.
	 *
	 * @param args The application arguments
	 */
	public static void main(String[] args) {
		new ProcessService().run(args);
	}

	/**
	 * @see de.esoco.lib.manage.Stoppable#stop()
	 */
	@Override
	public void stop() {
		processRunner.stop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure(CommandLine commandLine) throws Exception {
		processRunner.setLogLevel(LogLevel.INFO);
	}

	/**
	 * Creates the {@link ProcessRunner} instance that will be used to run the
	 * process of this application. Can be overridden by subclasses that
	 * need to
	 * run a sub-class of {@link ProcessRunner}.
	 *
	 * @param commandLine The application command line
	 * @return A process runner instance
	 * @throws Exception If creating the runner fails
	 */
	@SuppressWarnings("unchecked")
	protected ProcessRunner createProcessRunner(CommandLine commandLine)
		throws Exception {
		ProcessRunner runner = null;

		String process = commandLine.requireOption(ARG_PROCESS).toString();

		try {
			Class<?> processClass = Class.forName(process);

			if (ProcessDefinition.class.isAssignableFrom(processClass)) {
				ProcessDefinition processDef =
					ProcessManager.getProcessDefinition(
						(Class<? extends ProcessDefinition>) processClass);

				runner = new ProcessRunner(processDef);
			}
		} catch (ClassNotFoundException e) {
			throw new CommandLineException(
				String.format("Could not find process class '%s'", process),
				ARG_PROCESS, e);
		}

		if (runner != null) {
			return runner;
		} else {
			throw new CommandLineException("Missing argument %s", ARG_PROCESS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getServiceName() {
		return processRunner.getProcessDefinition().getClass().getSimpleName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialize(CommandLine commandLine) throws Exception {
		processRunner = createProcessRunner(commandLine);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runService() throws Exception {
		processRunner.run();
	}
}
