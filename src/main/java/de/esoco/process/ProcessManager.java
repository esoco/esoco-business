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

import de.esoco.data.element.DataElement;

import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.lib.text.TextConvert;

import java.util.HashMap;
import java.util.Map;

import org.obrel.core.RelationTypes;

import static org.obrel.type.StandardTypes.NAME;

/**
 * Provides static methods to access and handle processes.
 *
 * @author eso
 */
public final class ProcessManager {

	private static final Map<Class<? extends ProcessDefinition>,
		ProcessDefinition>
		aProcessDefRegistry =
		new HashMap<Class<? extends ProcessDefinition>, ProcessDefinition>();

	/**
	 * Private, only static use.
	 */
	private ProcessManager() {
	}

	/**
	 * Returns a process instance for a certain process definition class.
	 *
	 * @param rDefinitionClass The process definition class
	 * @return The corresponding process instance
	 * @throws ProcessException If creating or retrieving the process instance
	 *                          fails
	 */
	public static Process getProcess(
		Class<? extends ProcessDefinition> rDefinitionClass)
		throws ProcessException {
		ProcessDefinition rDef = getProcessDefinition(rDefinitionClass);

		return getProcess(rDef);
	}

	/**
	 * Returns a process instance for a certain process definition.
	 *
	 * @param rDefinition The process definition
	 * @return The corresponding process instance
	 * @throws ProcessException If creating or retrieving the process instance
	 *                          fails
	 */
	public static Process getProcess(ProcessDefinition rDefinition)
		throws ProcessException {
		RelationTypes.init(rDefinition.getClass());

		return rDefinition.createProcess();
	}

	/**
	 * Returns the process definition instance for a certain definition class.
	 *
	 * @param rDefinitionClass The process definition class
	 * @return The corresponding process definition
	 */
	public static <T extends ProcessDefinition> T getProcessDefinition(
		Class<T> rDefinitionClass) {
		@SuppressWarnings("unchecked")
		T rDef = (T) aProcessDefRegistry.get(rDefinitionClass);

		if (rDef == null) {
			rDef = ReflectUtil.newInstance(rDefinitionClass);

			aProcessDefRegistry.put(rDefinitionClass, rDef);
		}

		return rDef;
	}

	/**
	 * Returns a resource string for a certain process class. The returned
	 * string will be prefixed with {@link DataElement#ITEM_RESOURCE_PREFIX}.
	 *
	 * @param rClass The process class
	 * @return The resulting resource string
	 */
	public static String getProcessResource(
		Class<? extends ProcessDefinition> rClass) {
		String sName = getProcessDefinition(rClass).get(NAME);

		return DataElement.ITEM_RESOURCE_PREFIX +
			TextConvert.capitalizedIdentifier(sName);
	}
}
