//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.event.GenericEvent;
import de.esoco.lib.property.InteractionEventType;

import org.obrel.core.RelationType;


/********************************************************************
 * An interaction event that hold information about the event source and the
 * type of event that occurred.
 *
 * @author eso
 */
public final class InteractionEvent extends GenericEvent<RelationType<?>>
{
	//~ Instance fields --------------------------------------------------------

	private Interaction			 rInteraction;
	private InteractionEventType eEventType;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rInteraction          The Interaction. The context of the event.
	 * @param rInteractionParameter The interaction parameter on which this
	 *                              event occurred.
	 * @param eEventType            The type of event that occurred.
	 */
	public InteractionEvent(Interaction			 rInteraction,
							RelationType<?>		 rInteractionParameter,
							InteractionEventType eEventType)
	{
		super(rInteractionParameter);

		this.rInteraction = rInteraction;
		this.eEventType   = eEventType;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the context in which the event occurred i.e. the context in which
	 * the event source lived when the event initially occurred on it.
	 *
	 * @return The context in which the event occurred i.e. the context in which
	 *         the event source lived when the event initially occurred on it.
	 */
	public Interaction getContext()
	{
		return rInteraction;
	}

	/***************************************
	 * Returns the type of event that occurred.
	 *
	 * @return The type of event that occurred.
	 */
	public InteractionEventType getType()
	{
		return eEventType;
	}
}
