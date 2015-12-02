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
package de.esoco.process.step;

import de.esoco.process.ProcessRelationTypes;

import static de.esoco.process.ProcessRelationTypes.PROCESS_STEP_INFO;
import static de.esoco.process.ProcessRelationTypes.PROCESS_STEP_MESSAGE;


/********************************************************************
 * A simple interactive process step that displays a message in a standard
 * format to the user. The message to display must be stored in a relation of
 * the type {@link ProcessRelationTypes#PROCESS_STEP_MESSAGE} either in the step
 * instance or a process parameter.
 *
 * @author eso
 */
public class DisplayMessage extends Interaction
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public DisplayMessage()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Can be overridden by subclasses to return the information string for this
	 * process step. The default implementation returns either the value of this
	 * step's relation of type {@link ProcessRelationTypes#PROCESS_STEP_INFO}
	 * or, if it doesn't exist the process parameter with that type. This method
	 * will be invoked from {@link #prepareParameters()}.
	 *
	 * @return The information string for this step
	 */
	protected String getProcessStepInfo()
	{
		String sInfo = get(PROCESS_STEP_INFO);

		if (sInfo == null)
		{
			sInfo = getParameter(PROCESS_STEP_INFO);
		}

		return sInfo;
	}

	/***************************************
	 * Can be overridden by subclasses to return the message for this process
	 * step. The default implementation returns either the value of the step
	 * relation with the type {@link ProcessRelationTypes#PROCESS_STEP_MESSAGE}
	 * or, if it doesn't exist the process parameter with that type. This method
	 * will be invoked from {@link #prepareParameters()}.
	 *
	 * @return The process step message
	 */
	protected String getProcessStepMessage()
	{
		String sMessage = get(PROCESS_STEP_MESSAGE);

		if (sMessage == null)
		{
			sMessage = getParameter(PROCESS_STEP_MESSAGE);
		}

		return sMessage;
	}

	/***************************************
	 * @see Interaction#prepareParameters()
	 */
	@Override
	protected void prepareParameters() throws Exception
	{
		setProcessStepMessage(getProcessStepMessage());
		setProcessStepInfo(getProcessStepInfo(), -1);
	}
}
