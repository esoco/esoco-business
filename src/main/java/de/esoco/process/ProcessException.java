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

/**
 * Base class for all process-specific exceptions.
 *
 * @author eso
 */
public class ProcessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final ProcessFragment step;

	/**
	 * Creates a new instance.
	 *
	 * @param step    The step in which the exception occurred
	 * @param message The error message
	 */
	public ProcessException(ProcessFragment step, String message) {
		this(step, message, null);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param step  The step in which the exception occurred
	 * @param cause The causing exception (may be NULL)
	 */
	public ProcessException(ProcessFragment step, Throwable cause) {
		this(step, null, cause);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param step    The step in which the exception occurred
	 * @param message The error message
	 * @param cause   The causing exception (may be NULL)
	 */
	public ProcessException(ProcessFragment step, String message,
		Throwable cause) {
		super(message, cause);

		this.step = step;
	}

	/**
	 * Returns the process step that caused this exception.
	 *
	 * @return The process step
	 */
	public final ProcessFragment getProcessStep() {
		return step;
	}

	/**
	 * Returns the root exception of this instance, i.e. the topmost causing
	 * exception or this if no cause exists.
	 *
	 * @return The root exception
	 */
	public Throwable getRootException() {
		Throwable root = this;

		while (root.getCause() != null) {
			root = root.getCause();
		}

		return root;
	}
}
