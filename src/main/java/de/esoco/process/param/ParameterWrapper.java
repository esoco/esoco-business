//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.process.param;

import de.esoco.data.FileType;

import de.esoco.lib.expression.Function;
import de.esoco.lib.property.ContentProperties;
import de.esoco.lib.property.HasProperties;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.PropertyName;

import de.esoco.process.ProcessFragment;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.ValueEventHandler;
import de.esoco.process.step.Interaction.InteractionHandler;
import de.esoco.process.step.InteractionEvent;
import de.esoco.process.step.InteractionFragment;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obrel.core.RelatedObject;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;

import static de.esoco.data.element.DataElement.HIDDEN_URL;
import static de.esoco.data.element.DataElement.INTERACTION_URL;

import static de.esoco.lib.property.ContentProperties.ELEMENT_ID;
import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.StateProperties.DISABLED;
import static de.esoco.lib.property.StateProperties.HIDDEN;
import static de.esoco.lib.property.StateProperties.INTERACTION_EVENT_TYPES;


/********************************************************************
 * A common base class for objects that wrap the data that describes an
 * interactive process parameter (fragment and relation type).
 *
 * @author eso
 */
public class ParameterWrapper<T, P extends ParameterWrapper<T, P>>
	extends RelatedObject
{
	//~ Instance fields --------------------------------------------------------

	InteractionFragment rFragment;
	RelationType<T>     rParamType;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain fragment and parameter relation
	 * type.
	 *
	 * <p>If a subclass cannot provide the fragment or the relation type at
	 * creation time it may use NULL when invoking the super constructor. These
	 * values must then be set as soon as possible by invoking the respective
	 * setter method(s) (see {@link #setFragment(InteractionFragment)} and
	 * {@link #setParameterType(RelationType)}). This must happen before any
	 * other method on this instance is called or else a null pointer exception
	 * will occur. Typically this should only be used in the constructor of a
	 * subclass, e.g. to include some kind of self-reference (which is not
	 * possible while invoking the super constructor).</p>
	 *
	 * @param rFragment  The fragment to wrap the parameter for
	 * @param rParamType The parameter relation type to wrap
	 */
	public ParameterWrapper(
		InteractionFragment rFragment,
		RelationType<T>		rParamType)
	{
		this.rFragment  = rFragment;
		this.rParamType = rParamType;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Initiates a download for a parameter. This method must be invoked during
	 * the handling of an event and the download will then be executed as the
	 * result of the event. After being processed by the process interaction the
	 * generated download URL will be removed from the process parameter.
	 *
	 * <p>This method is protected because it's functionality should only be
	 * exposed by the API of certain parameter wrappers (e.g. buttons).</p>
	 *
	 * @param  rParam             The parameter to initiate the download for
	 * @param  sFileName          The file name of the download
	 * @param  eFileType          The file type of the download
	 * @param  fDownloadGenerator The function that generates the download data
	 *
	 * @throws RuntimeProcessException If the download preparation fails
	 */
	@SuppressWarnings("unchecked")
	public static void initiateDownload(
		ParameterWrapper<?, ?> rParam,
		String				   sFileName,
		FileType			   eFileType,
		Function<FileType, ?>  fDownloadGenerator)
	{
		InteractionFragment rFragment    = rParam.fragment();
		String			    sDownloadUrl;

		try
		{
			sDownloadUrl =
				rFragment.prepareDownload(
					sFileName,
					eFileType,
					fDownloadGenerator);

			rParam.set(HIDDEN_URL);
			rParam.set(INTERACTION_URL, sDownloadUrl);
			rFragment.getProcess()
					 .addInteractionCleanupAction(
		 				() ->
		 					rParam.remove(INTERACTION_URL)
		 					.remove(HIDDEN_URL));
		}
		catch (Exception e)
		{
			throw new RuntimeProcessException(rFragment, e);
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Clear a certain property flag.
	 *
	 * @see ProcessFragment#clearUIFlag(PropertyName, RelationType...)
	 */
	@SuppressWarnings("unchecked")
	public final P clear(PropertyName<Boolean> rProperty)
	{
		rFragment.clearUIFlag(rProperty, rParamType);

		return (P) this;
	}

	/***************************************
	 * Marks this parameter to be disabled in the user interface.
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #setEnabled(boolean)
	 */
	public final P disable()
	{
		return setEnabled(false);
	}

	/***************************************
	 * Marks this parameter to be disabled in the user interface.
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #setEnabled(boolean)
	 */
	public final P enable()
	{
		return setEnabled(true);
	}

	/***************************************
	 * Returns the fragment this parameter belongs to (i.e. where it is
	 * displayed).
	 *
	 * @return The fragment
	 */
	public InteractionFragment fragment()
	{
		return rFragment;
	}

	/***************************************
	 * Returns the value of a certain property for the wrapped parameter.
	 *
	 * @see ProcessFragment#getUIProperty(PropertyName, RelationType)
	 */
	public final <V> V get(PropertyName<V> rProperty)
	{
		return rFragment.getUIProperty(rProperty, rParamType);
	}

	/***************************************
	 * Returns the value of a certain parameter in the current process.
	 *
	 * @see ProcessFragment#getParameter(RelationType)
	 */
	public final <V> V getParam(RelationType<V> rType)
	{
		return rFragment.getParameter(rType);
	}

	/***************************************
	 * Checks whether a certain property has been set for the wrapped parameter.
	 *
	 * @param  rProperty The property name
	 *
	 * @return TRUE if the property exists (with any value)
	 */
	public final boolean has(PropertyName<?> rProperty)
	{
		HasProperties rProperties = rFragment.getUIProperties(rParamType);

		return rProperties != null && rProperties.hasProperty(rProperty);
	}

	/***************************************
	 * Marks this parameter to be hidden in the user interface.
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #setVisible(boolean)
	 */
	public final P hide()
	{
		return setVisible(false);
	}

	/***************************************
	 * Sets a string ID for this instance by setting the UI property {@link
	 * ContentProperties#ELEMENT_ID}.
	 *
	 * @param  sId The ID string
	 *
	 * @return This instance for concatenation
	 */
	public P id(String sId)
	{
		return set(ELEMENT_ID, sId);
	}

	/***************************************
	 * Checks the enabled/disabled state.
	 *
	 * @see #setEnabled(boolean)
	 */
	public boolean isEnabled()
	{
		return !rFragment.hasUIFlag(DISABLED, rParamType);
	}

	/***************************************
	 * Checks the visibility.
	 *
	 * @see #setVisible(boolean)
	 */
	public boolean isVisible()
	{
		return !rFragment.hasUIFlag(HIDDEN, rParamType);
	}

	/***************************************
	 * Removes certain properties from the wrapped parameter.
	 *
	 * @param  rProperties The names of the properties to remove
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P remove(PropertyName<?>... rProperties)
	{
		rFragment.removeUIProperties(rParamType, rProperties);

		return (P) this;
	}

	/***************************************
	 * Sets a resource ID for this instance by setting the UI property {@link
	 * ContentProperties#RESOURCE_ID}.
	 *
	 * @param  sResourceId The resource ID string
	 *
	 * @return This instance for concatenation
	 */
	public P resid(String sResourceId)
	{
		return set(RESOURCE_ID, sResourceId);
	}

	/***************************************
	 * Sets a one or more property flags.
	 *
	 * @see ProcessFragment#setUIFlag(PropertyName, RelationType...)
	 */
	@SuppressWarnings("unchecked")
	public final P set(PropertyName<Boolean> rFlagProperty)
	{
		rFragment.setUIFlag(rFlagProperty, rParamType);

		return (P) this;
	}

	/***************************************
	 * Sets a certain property.
	 *
	 * @see ProcessFragment#setUIProperty(PropertyName, Object, RelationType...)
	 */
	@SuppressWarnings("unchecked")
	public final <V> P set(PropertyName<V> rProperty, V rValue)
	{
		rFragment.setUIProperty(rProperty, rValue, rParamType);

		return (P) this;
	}

	/***************************************
	 * Sets a certain integer property.
	 *
	 * @see ProcessFragment#setUIProperty(int, PropertyName, RelationType...)
	 */
	@SuppressWarnings("unchecked")
	public final P set(int nValue, PropertyName<Integer> rProperty)
	{
		rFragment.setUIProperty(nValue, rProperty, rParamType);

		return (P) this;
	}

	/***************************************
	 * Enables or disables this parameter based on the boolean parameter.
	 *
	 * @param  bEnabled TRUE to enable the parameter, FALSE to disable it
	 *
	 * @return This instance for concatenation
	 */
	public P setEnabled(boolean bEnabled)
	{
		return bEnabled ? clear(DISABLED) : set(DISABLED);
	}

	/***************************************
	 * Sets the value of a certain parameter in the current process.
	 *
	 * @see ProcessFragment#setParameter(RelationType, Object)
	 */
	public final <V> Relation<V> setParam(RelationType<V> rType, V rValue)
	{
		return rFragment.setParameter(rType, rValue);
	}

	/***************************************
	 * Sets the visibility of this parameter based on the boolean parameter.
	 *
	 * @param  bVisible TRUE to show the parameter, FALSE to hide it
	 *
	 * @return This instance for concatenation
	 */
	public P setVisible(boolean bVisible)
	{
		return bVisible ? clear(HIDDEN) : set(HIDDEN);
	}

	/***************************************
	 * Marks this parameter to be visible in the user interface.
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #setVisible(boolean)
	 */
	public final P show()
	{
		return setVisible(true);
	}

	/***************************************
	 * Returns the parameter relation type wrapped by this instance.
	 *
	 * @return The parameter relation type
	 */
	public final RelationType<T> type()
	{
		return rParamType;
	}

	/***************************************
	 * Allows subclasses to set the fragment of this parameter. This should only
	 * be used if subclasses are not able to provide the fragment at
	 * construction time. In that case this method must be invoked before any
	 * other method is invoked or else an exception will be thrown.
	 *
	 * @param rFragment The new fragment
	 */
	protected final void setFragment(InteractionFragment rFragment)
	{
		this.rFragment = rFragment;
	}

	/***************************************
	 * Helper method to set a parameter event handler that forwards interaction
	 * events to a runnable object.
	 *
	 * @param  eEventType    The event type to set the event handler for
	 * @param  rEventHandler rRunnable The runnable to be invoked on interaction
	 *                       events
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	protected P setParameterEventHandler(
		InteractionEventType	   eEventType,
		final ValueEventHandler<T> rEventHandler)
	{
		InteractionHandler rInteractionHandler =
			rFragment.getParameterInteractionHandler(rParamType);

		if (rInteractionHandler instanceof
			ParameterWrapper.ParameterInteractionHandler)
		{
			((ParameterInteractionHandler) rInteractionHandler)
			.setEventTypeHandler(eEventType, rEventHandler);
		}
		else
		{
			ParameterInteractionHandler rHandler =
				new ParameterInteractionHandler();

			rHandler.setEventTypeHandler(eEventType, rEventHandler);

			rFragment.setParameterInteractionHandler(rParamType, rHandler);
		}

		Set<InteractionEventType> rInteractionEventTypes =
			get(INTERACTION_EVENT_TYPES);

		if (rInteractionEventTypes == null)
		{
			rInteractionEventTypes = EnumSet.noneOf(InteractionEventType.class);
		}

		rInteractionEventTypes.add(eEventType);
		input();

		return set(INTERACTION_EVENT_TYPES, rInteractionEventTypes);
	}

	/***************************************
	 * Allows subclasses to set the parameter relation type. This should only be
	 * used if subclasses are not able to provide the parameter type at
	 * construction time. In that case this method must be invoked before any
	 * other method is invoked or else an exception will be thrown.
	 *
	 * @param rParamType The new parameter relation type
	 */
	protected final void setParameterType(RelationType<T> rParamType)
	{
		this.rParamType = rParamType;
	}

	/***************************************
	 * Internal method to mark the wrapped relation type to be displayed as
	 * editable in the fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	P input()
	{
		// ignore the fragment parameter itself (e.g. when registering click
		// handlers on a container) to prevent recursion
		if (rParamType != rFragment.getFragmentParameter())
		{
			rFragment.addInputParameters(rParamType);
		}

		return (P) this;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An interaction handler implementation for parameter-related events.
	 *
	 * @author eso
	 */
	class ParameterInteractionHandler implements InteractionHandler
	{
		//~ Instance fields ----------------------------------------------------

		private Map<InteractionEventType, ValueEventHandler<T>> aEventTypeHandlers =
			new HashMap<>();

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void handleInteraction(InteractionEvent rEvent) throws Exception
		{
			ValueEventHandler<T> rEventHandler =
				aEventTypeHandlers.get(rEvent.getType());

			if (rEventHandler != null && rFragment.isAttached())
			{
				rEventHandler.handleValueUpdate(
					rFragment.getParameter(rParamType));
			}
		}

		/***************************************
		 * Remove the event handler for a certain event type.
		 *
		 * @param eEventType The event type
		 */
		void removeEventTypeHandler(InteractionEventType eEventType)
		{
			aEventTypeHandlers.remove(eEventType);
		}

		/***************************************
		 * Sets or replaces an event handler for a certain event type.
		 *
		 * @param eEventType The event type
		 * @param rHandler   The event handler
		 */
		void setEventTypeHandler(
			InteractionEventType eEventType,
			ValueEventHandler<T> rHandler)
		{
			aEventTypeHandlers.put(eEventType, rHandler);
		}
	}
}
