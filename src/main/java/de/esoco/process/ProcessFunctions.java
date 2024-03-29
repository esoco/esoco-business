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

import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.function.GetElement;
import de.esoco.lib.expression.function.SetElement;
import org.obrel.core.RelationType;

/**
 * A class that contains process-specific functions and predicates and the
 * corresponding static access methods.
 *
 * @author eso
 */
public class ProcessFunctions {

	/**
	 * Private, only static use.
	 */
	private ProcessFunctions() {
	}

	/**
	 * Returns a new function instance which returns a certain process
	 * parameter.
	 *
	 * @param type The type of the process parameter to return
	 * @return A new function instance
	 */
	public static <T> Function<Process, T> getParameter(RelationType<T> type) {
		return new GetParameter<T>(type);
	}

	/**
	 * Returns a new function instance which sets a certain process parameter.
	 *
	 * @param type  The type of the parameter to set
	 * @param value The parameter value to set
	 * @return A new function instance
	 */
	public static <V> BinaryFunction<Process, V, Process> setParameter(
		RelationType<V> type, V value) {
		return new SetParameter<V>(type, value);
	}

	/**
	 * An element access function that returns a particular parameter from a
	 * {@link Process}.
	 */
	public static class GetParameter<T>
		extends GetElement<Process, RelationType<T>, T> {

		/**
		 * Creates a new instance that accesses a particular single-type
		 * relation.
		 *
		 * @param type The single-relation type to access
		 */
		public GetParameter(RelationType<T> type) {
			super(type, "GetParameter[%s]");
		}

		/**
		 * @see GetElement#getElementValue(Object, Object)
		 */
		@Override
		protected T getElementValue(Process process, RelationType<T> type) {
			return process.getParameter(type);
		}
	}

	/**
	 * Implementation of an element access function that sets a certain
	 * parameter in a {@link Process}.
	 */
	public static class SetParameter<T>
		extends SetElement<Process, RelationType<T>, T> {

		/**
		 * Creates a new instance that sets a particular relation.
		 *
		 * @param type  The type of the relation to set
		 * @param value The relation value to set
		 */
		public SetParameter(RelationType<T> type, T value) {
			super(type, value, "SetParameter[%s]");
		}

		/**
		 * @see SetElement#setElementValue(Object, Object, Object)
		 */
		@Override
		protected void setElementValue(RelationType<T> type, Process process,
			T value) {
			process.setParameter(type, value);
		}
	}
}
