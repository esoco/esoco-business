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

import de.esoco.data.process.ProcessDescription;
import de.esoco.data.process.ProcessState;
import org.obrel.core.Relatable;

/**
 * Defines the interface for the execution of processes in the current
 * application's context.
 *
 * @author eso
 */
public interface ProcessExecutor {

	/**
	 * Executes a process and returns the resulting state. If the process is
	 * interactive the returned state will not be finished and the state can be
	 * processed by an interactive front-end to perform user interactions.
	 *
	 * <p>If the initialization parameters are not NULL they will all be copied
	 * onto the new process, overriding any existing parameters.</p>
	 *
	 * @param description The description of the process to execute
	 * @param initParams  Optional process initialization parameters or NULL
	 *                      for
	 *                    none
	 * @return The resulting process state or NULL if the process has already
	 * been finished by a previous execution.
	 * @throws Exception Any kind of exception can occur if the process
	 *                   execution fails
	 */
	ProcessState executeProcess(ProcessDescription description,
		Relatable initParams) throws Exception;
}
