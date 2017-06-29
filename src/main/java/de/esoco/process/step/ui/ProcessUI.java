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
package de.esoco.process.step.ui;

import de.esoco.process.step.InteractionFragment;


/********************************************************************
 * TODO: DOCUMENT ME!
 *
 * @author eso
 */
public abstract class ProcessUI
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private InteractionFragment rInteractionFragment;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rInteractionFragment The interaction fragment containing this UI
	 */
	public ProcessUI(InteractionFragment rInteractionFragment)
	{
		this.rInteractionFragment = rInteractionFragment;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Must be implemented to build the process UI by adding components.
	 *
	 * @throws Exception TODO: DOCUMENT ME!
	 */
	public abstract void build() throws Exception;
}
