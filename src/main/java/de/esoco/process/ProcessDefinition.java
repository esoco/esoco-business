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

import de.esoco.lib.manage.ElementDefinition;

import org.obrel.core.SerializableRelatedObject;

/**
 * The base class for process definitions.
 *
 * @author eso
 */
public abstract class ProcessDefinition extends SerializableRelatedObject
	implements ElementDefinition<Process> {

	private static final long serialVersionUID = 1L;

	/**
	 * Abstract method that must be provided by implementations to create a
	 * process instance from the definition. This method is declared protected
	 * because it should only be invoked by the process manager. Therefore
	 * subclasses should leave the access modifier as "protected".
	 *
	 * @return The new process instance
	 * @throws ProcessException If creating the new instance fails
	 */
	protected abstract Process createProcess() throws ProcessException;
}
