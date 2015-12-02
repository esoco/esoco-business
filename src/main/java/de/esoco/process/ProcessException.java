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

/********************************************************************
 * Base class for all process-specific exceptions.
 *
 * @author eso
 */
public class ProcessException extends Exception
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private final ProcessFragment rStep;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rStep    The step in which the exception occurred
	 * @param sMessage The error message
	 */
	public ProcessException(ProcessFragment rStep, String sMessage)
	{
		this(rStep, sMessage, null);
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rStep  The step in which the exception occurred
	 * @param aCause The causing exception (may be NULL)
	 */
	public ProcessException(ProcessFragment rStep, Throwable aCause)
	{
		this(rStep, null, aCause);
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rStep    The step in which the exception occurred
	 * @param sMessage The error message
	 * @param aCause   The causing exception (may be NULL)
	 */
	public ProcessException(ProcessFragment rStep,
							String			sMessage,
							Throwable		aCause)
	{
		super(sMessage, aCause);

		this.rStep = rStep;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the process step that caused this exception.
	 *
	 * @return The process step
	 */
	public final ProcessFragment getProcessStep()
	{
		return rStep;
	}

	/***************************************
	 * Returns the root exception of this instance, i.e. the topmost causing
	 * exception or this if no cause exists.
	 *
	 * @return The root exception
	 */
	public Throwable getRootException()
	{
		Throwable eRoot = this;

		while (eRoot.getCause() != null)
		{
			eRoot = eRoot.getCause();
		}

		return eRoot;
	}
}
