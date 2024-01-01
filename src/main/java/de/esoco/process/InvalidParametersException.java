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

import org.obrel.core.RelationType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A process exception subclass that is thrown when a process step encounters
 * invalid parameters.
 *
 * @author eso
 */
public class InvalidParametersException extends ProcessException {

	private static final long serialVersionUID = 1L;

	private final Map<RelationType<?>, String> invalidParams;

	/**
	 * Creates a new instance.
	 *
	 * @param step                 The process step in which the error occurred
	 * @param invalidParamMessages A mapping from the invalid parameter
	 *                                types to
	 *                             strings that describe the problem
	 */
	public InvalidParametersException(ProcessFragment step,
		Map<RelationType<?>, String> invalidParamMessages) {
		super(step, "InvalidParams");

		this.invalidParams = Collections.unmodifiableMap(invalidParamMessages);
	}

	/**
	 * Creates a new instance for one or more invalid parameters with the same
	 * error message.
	 *
	 * @param step          The process step in which the error occurred
	 * @param message       The error message
	 * @param invalidParams The invalid parameter types
	 */
	public InvalidParametersException(ProcessFragment step, String message,
		RelationType<?>... invalidParams) {
		this(step, message, Arrays.asList(invalidParams));
	}

	/**
	 * Creates a new instance for one or more invalid parameters with the same
	 * error message.
	 *
	 * @param step          The process step in which the error occurred
	 * @param message       The error message
	 * @param invalidParams The invalid parameter types
	 */
	public InvalidParametersException(ProcessFragment step, String message,
		Collection<RelationType<?>> invalidParams) {
		super(step, "InvalidParams");

		assert !invalidParams.isEmpty();

		Map<RelationType<?>, String> params =
			new HashMap<>(invalidParams.size());

		for (RelationType<?> param : invalidParams) {
			params.put(param, message);
		}

		this.invalidParams = Collections.unmodifiableMap(params);
	}

	/**
	 * Returns the mapping from the invalid parameter types to strings that
	 * describe the problem (typically resource IDs).
	 *
	 * @return The mapping from invalid parameters to error messages
	 */
	public final Map<RelationType<?>, String> getInvalidParams() {
		return invalidParams;
	}
}
