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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.obrel.core.RelationType;


/********************************************************************
 * A process exception subclass that is thrown when a process step encounters
 * invalid parameters.
 *
 * @author eso
 */
public class InvalidParametersException extends ProcessException
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private final Map<RelationType<?>, String> aInvalidParams;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rStep                 The process step in which the error occurred
	 * @param rInvalidParamMessages A mapping from the invalid parameter types
	 *                              to strings that describe the problem
	 */
	public InvalidParametersException(
		ProcessFragment				 rStep,
		Map<RelationType<?>, String> rInvalidParamMessages)
	{
		super(rStep, "InvalidParams");

		this.aInvalidParams =
			Collections.unmodifiableMap(rInvalidParamMessages);
	}

	/***************************************
	 * Creates a new instance for one or more invalid parameters with the same
	 * error message.
	 *
	 * @param rStep          The process step in which the error occurred
	 * @param sMessage       The error message
	 * @param rInvalidParams The invalid parameter types
	 */
	public InvalidParametersException(ProcessFragment    rStep,
									  String			 sMessage,
									  RelationType<?>... rInvalidParams)
	{
		this(rStep, sMessage, Arrays.asList(rInvalidParams));
	}

	/***************************************
	 * Creates a new instance for one or more invalid parameters with the same
	 * error message.
	 *
	 * @param rStep          The process step in which the error occurred
	 * @param sMessage       The error message
	 * @param rInvalidParams The invalid parameter types
	 */
	public InvalidParametersException(
		ProcessFragment				rStep,
		String						sMessage,
		Collection<RelationType<?>> rInvalidParams)
	{
		super(rStep, "InvalidParams");

		assert rInvalidParams.size() > 0;

		Map<RelationType<?>, String> aParams =
			new HashMap<RelationType<?>, String>(rInvalidParams.size());

		for (RelationType<?> rParam : rInvalidParams)
		{
			aParams.put(rParam, sMessage);
		}

		aInvalidParams = Collections.unmodifiableMap(aParams);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the mapping from the invalid parameter types to strings that
	 * describe the problem (typically resource IDs).
	 *
	 * @return The mapping from invalid parameters to error messages
	 */
	public final Map<RelationType<?>, String> getInvalidParams()
	{
		return aInvalidParams;
	}
}
