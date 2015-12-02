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

import org.obrel.type.MetaTypes;


/********************************************************************
 * An interface that must be implemented to interact with a running process.
 *
 * @author eso
 */
public interface ProcessInteractionHandler
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * This method must be implemented to perform the interaction for the given
	 * process step. The process parameters for the interaction can be queried
	 * from the process step. The types of the parameters that must be queried
	 * are stored in a relation with the type {@link MetaTypes#INPUT_PARAMS}
	 * while {@link MetaTypes#INTERACTION_PARAMS} contains the parameters that
	 * should be displayed only.
	 *
	 * @param  rProcessStep The process step to perform the interaction for
	 *
	 * @throws Exception Any kind of exception may be thrown if the interaction
	 *                   fails
	 */
	public void performInteraction(ProcessFragment rProcessStep)
		throws Exception;
}
