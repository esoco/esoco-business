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

import de.esoco.lib.event.GenericEvent;

import org.obrel.core.RelationType;


/********************************************************************
 * An interaction event that hold information about the event source and the
 * type of event that occurred.
 *
 * @author ueggers
 */
public final class InteractionEvent extends GenericEvent<RelationType<?>>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * The available event types.<br>
	 * {@link EventType#ACTION} a single action event<br>
	 * {@link EventType#INPUT} a continuous input event
	 */
	public enum EventType { ACTION, INPUT }

	//~ Instance fields --------------------------------------------------------

	private EventType   eEventType;
	private Interaction rInteraction;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rInteraction          The Interaction. The context of the event.
	 * @param rInteractionParameter The interaction parameter on which this
	 *                              event occurred.
	 * @param eEventType            The type of event that occurred.
	 */
	public InteractionEvent(Interaction		rInteraction,
							RelationType<?> rInteractionParameter,
							EventType		eEventType)
	{
		super(rInteractionParameter);
		this.eEventType   = eEventType;
		this.rInteraction = rInteraction;
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
	public EventType getType()
	{
		return eEventType;
	}
}
