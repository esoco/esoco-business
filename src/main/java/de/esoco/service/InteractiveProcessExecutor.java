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
package de.esoco.service;

import de.esoco.lib.app.CommandLine;
import de.esoco.lib.app.CommandLineException;
import de.esoco.lib.app.RestService;
import de.esoco.lib.security.AuthenticationService;
import de.esoco.process.ProcessDefinition;
import de.esoco.process.ProcessManager;
import org.obrel.core.RelationType;
import org.obrel.space.ObjectSpace;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.obrel.core.RelationTypes.newType;

/**
 * A service that executes interactive processes and transfers their interaction
 * state to a remote service (typically a {@link InteractiveProcessRenderer}).
 *
 * @author eso
 */
public abstract class InteractiveProcessExecutor extends RestService
	implements AuthenticationService {

	private static final RelationType<Map<String, String>> EXECUTE_PROCESS =
		newType();

	private static final String ARG_RENDERER = "renderer";

	private static final String ARG_PROCESSES = "processes";

	private final Set<ProcessDefinition> processDefinitions = new HashSet<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ObjectSpace<Object> buildRestServerSpace() {
		ObjectSpace<Object> rootSpace = super.buildRestServerSpace();
		ObjectSpace<String> apiSpace = rootSpace.get(API);

		apiSpace.init(EXECUTE_PROCESS).onUpdate(this::executeProcess);

		return rootSpace;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getServiceName() {
		return getClass().getSimpleName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void initialize(CommandLine commandLine) throws Exception {
//		String renderService =
//			commandLine.requireOption(ARG_RENDERER).toString();

		String[] processes =
			commandLine.requireOption(ARG_PROCESSES).toString().split(",");

		for (String process : processes) {
			try {
				Class<?> processClass = Class.forName(process);

				if (ProcessDefinition.class.isAssignableFrom(processClass)) {
					ProcessDefinition definition =
						ProcessManager.getProcessDefinition(
							(Class<? extends ProcessDefinition>) processClass);

					processDefinitions.add(definition);
				} else {
					throw new CommandLineException(
						String.format("Class is not a process definition: %s",
							process), ARG_PROCESSES);
				}
			} catch (Exception e) {
				throw new CommandLineException(String.format(
					"Could not create process definition for class %s",
					process), ARG_PROCESSES, e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void printUsage(PrintStream output) {
		output.printf(
			"Usage: %s -%s <render service URL> -%s <process definition " +
				"classes> -port <listening port>\n",
			getClass().getSimpleName(),
			ARG_RENDERER, ARG_PROCESSES);
	}

	/**
	 * Executes the process in the request.
	 *
	 * @param request The execution request
	 */
	private void executeProcess(Map<String, String> request) {
	}
}
