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
import org.obrel.core.RelatedObject;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.esoco.data.element.DataElement.HIDDEN_URL;
import static de.esoco.data.element.DataElement.INTERACTION_URL;
import static de.esoco.lib.property.ContentProperties.ELEMENT_ID;
import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.StateProperties.DISABLED;
import static de.esoco.lib.property.StateProperties.HIDDEN;
import static de.esoco.lib.property.StateProperties.INTERACTION_EVENT_TYPES;

/**
 * A common base class for objects that wrap the data that describes an
 * interactive process parameter (fragment and relation type).
 *
 * @author eso
 */
public class ParameterWrapper<T, P extends ParameterWrapper<T, P>>
	extends RelatedObject {

	InteractionFragment fragment;

	RelationType<T> paramType;

	/**
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
	 * @param fragment  The fragment to wrap the parameter for
	 * @param paramType The parameter relation type to wrap
	 */
	public ParameterWrapper(InteractionFragment fragment,
		RelationType<T> paramType) {
		this.fragment = fragment;
		this.paramType = paramType;
	}

	/**
	 * Initiates a download for a parameter. This method must be invoked during
	 * the handling of an event and the download will then be executed as the
	 * result of the event. After being processed by the process interaction
	 * the
	 * generated download URL will be removed from the process parameter.
	 *
	 * <p>This method is protected because it's functionality should only be
	 * exposed by the API of certain parameter wrappers (e.g. buttons).</p>
	 *
	 * @param param             The parameter to initiate the download for
	 * @param fileName          The file name of the download
	 * @param fileType          The file type of the download
	 * @param downloadGenerator The function that generates the download data
	 * @throws RuntimeProcessException If the download preparation fails
	 */
	@SuppressWarnings("unchecked")
	public static void initiateDownload(ParameterWrapper<?, ?> param,
		String fileName, FileType fileType,
		Function<FileType, ?> downloadGenerator) {
		InteractionFragment fragment = param.fragment();
		String downloadUrl;

		try {
			downloadUrl =
				fragment.prepareDownload(fileName, fileType,
					downloadGenerator);

			param.set(HIDDEN_URL);
			param.set(INTERACTION_URL, downloadUrl);
			fragment
				.getProcess()
				.addInteractionCleanupAction(
					() -> param.remove(INTERACTION_URL).remove(HIDDEN_URL));
		} catch (Exception e) {
			throw new RuntimeProcessException(fragment, e);
		}
	}

	/**
	 * Clear a certain property flag.
	 *
	 * @see ProcessFragment#clearUIFlag(PropertyName, RelationType...)
	 */
	@SuppressWarnings("unchecked")
	public final P clear(PropertyName<Boolean> property) {
		fragment.clearUIFlag(property, paramType);

		return (P) this;
	}

	/**
	 * Marks this parameter to be disabled in the user interface.
	 *
	 * @return This instance for concatenation
	 * @see #setEnabled(boolean)
	 */
	public final P disable() {
		return setEnabled(false);
	}

	/**
	 * Marks this parameter to be disabled in the user interface.
	 *
	 * @return This instance for concatenation
	 * @see #setEnabled(boolean)
	 */
	public final P enable() {
		return setEnabled(true);
	}

	/**
	 * Returns the fragment this parameter belongs to (i.e. where it is
	 * displayed).
	 *
	 * @return The fragment
	 */
	public InteractionFragment fragment() {
		return fragment;
	}

	/**
	 * Returns the value of a certain property for the wrapped parameter.
	 *
	 * @see ProcessFragment#getUIProperty(PropertyName, RelationType)
	 */
	public final <V> V get(PropertyName<V> property) {
		return fragment.getUIProperty(property, paramType);
	}

	/**
	 * Returns the value of a certain parameter in the current process.
	 *
	 * @see ProcessFragment#getParameter(RelationType)
	 */
	public final <V> V getParam(RelationType<V> type) {
		return fragment.getParameter(type);
	}

	/**
	 * Checks whether a certain property has been set for the wrapped
	 * parameter.
	 *
	 * @param property The property name
	 * @return TRUE if the property exists (with any value)
	 */
	public final boolean has(PropertyName<?> property) {
		HasProperties properties = fragment.getUIProperties(paramType);

		return properties != null && properties.hasProperty(property);
	}

	/**
	 * Marks this parameter to be hidden in the user interface.
	 *
	 * @return This instance for concatenation
	 * @see #setVisible(boolean)
	 */
	public final P hide() {
		return setVisible(false);
	}

	/**
	 * Sets a string ID for this instance by setting the UI property
	 * {@link ContentProperties#ELEMENT_ID}.
	 *
	 * @param id The ID string
	 * @return This instance for concatenation
	 */
	public P id(String id) {
		return set(ELEMENT_ID, id);
	}

	/**
	 * Checks the enabled/disabled state.
	 *
	 * @see #setEnabled(boolean)
	 */
	public boolean isEnabled() {
		return !fragment.hasUIFlag(DISABLED, paramType);
	}

	/**
	 * Checks the visibility.
	 *
	 * @see #setVisible(boolean)
	 */
	public boolean isVisible() {
		return !fragment.hasUIFlag(HIDDEN, paramType);
	}

	/**
	 * Removes certain properties from the wrapped parameter.
	 *
	 * @param properties The names of the properties to remove
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P remove(PropertyName<?>... properties) {
		fragment.removeUIProperties(paramType, properties);

		return (P) this;
	}

	/**
	 * Sets a resource ID for this instance by setting the UI property
	 * {@link ContentProperties#RESOURCE_ID}.
	 *
	 * @param resourceId The resource ID string
	 * @return This instance for concatenation
	 */
	public P resid(String resourceId) {
		return set(RESOURCE_ID, resourceId);
	}

	/**
	 * Sets a one or more property flags.
	 *
	 * @see ProcessFragment#setUIFlag(PropertyName, RelationType...)
	 */
	@SuppressWarnings("unchecked")
	public final P set(PropertyName<Boolean> flagProperty) {
		fragment.setUIFlag(flagProperty, paramType);

		return (P) this;
	}

	/**
	 * Sets a certain property.
	 *
	 * @see ProcessFragment#setUIProperty(PropertyName, Object, RelationType...)
	 */
	@SuppressWarnings("unchecked")
	public final <V> P set(PropertyName<V> property, V value) {
		fragment.setUIProperty(property, value, paramType);

		return (P) this;
	}

	/**
	 * Sets a certain integer property.
	 *
	 * @see ProcessFragment#setUIProperty(int, PropertyName, RelationType...)
	 */
	@SuppressWarnings("unchecked")
	public final P set(int value, PropertyName<Integer> property) {
		fragment.setUIProperty(value, property, paramType);

		return (P) this;
	}

	/**
	 * Enables or disables this parameter based on the boolean parameter.
	 *
	 * @param enabled TRUE to enable the parameter, FALSE to disable it
	 * @return This instance for concatenation
	 */
	public P setEnabled(boolean enabled) {
		return enabled ? clear(DISABLED) : set(DISABLED);
	}

	/**
	 * Sets the value of a certain parameter in the current process.
	 *
	 * @see ProcessFragment#setParameter(RelationType, Object)
	 */
	public final <V> Relation<V> setParam(RelationType<V> type, V value) {
		return fragment.setParameter(type, value);
	}

	/**
	 * Sets the visibility of this parameter based on the boolean parameter.
	 *
	 * @param visible TRUE to show the parameter, FALSE to hide it
	 * @return This instance for concatenation
	 */
	public P setVisible(boolean visible) {
		return visible ? clear(HIDDEN) : set(HIDDEN);
	}

	/**
	 * Marks this parameter to be visible in the user interface.
	 *
	 * @return This instance for concatenation
	 * @see #setVisible(boolean)
	 */
	public final P show() {
		return setVisible(true);
	}

	/**
	 * Returns the parameter relation type wrapped by this instance.
	 *
	 * @return The parameter relation type
	 */
	public final RelationType<T> type() {
		return paramType;
	}

	/**
	 * Allows subclasses to set the fragment of this parameter. This should
	 * only
	 * be used if subclasses are not able to provide the fragment at
	 * construction time. In that case this method must be invoked before any
	 * other method is invoked or else an exception will be thrown.
	 *
	 * @param fragment The new fragment
	 */
	protected final void setFragment(InteractionFragment fragment) {
		this.fragment = fragment;
	}

	/**
	 * Helper method to set a parameter event handler that forwards interaction
	 * events to a runnable object.
	 *
	 * @param eventType    The event type to set the event handler for
	 * @param eventHandler runnable The runnable to be invoked on interaction
	 *                     events
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	protected P setParameterEventHandler(InteractionEventType eventType,
		final ValueEventHandler<T> eventHandler) {
		InteractionHandler interactionHandler =
			fragment.getParameterInteractionHandler(paramType);

		if (interactionHandler instanceof ParameterWrapper.ParameterInteractionHandler) {
			((ParameterInteractionHandler) interactionHandler).setEventTypeHandler(
				eventType, eventHandler);
		} else {
			ParameterInteractionHandler handler =
				new ParameterInteractionHandler();

			handler.setEventTypeHandler(eventType, eventHandler);

			fragment.setParameterInteractionHandler(paramType, handler);
		}

		Set<InteractionEventType> interactionEventTypes =
			get(INTERACTION_EVENT_TYPES);

		if (interactionEventTypes == null) {
			interactionEventTypes = EnumSet.noneOf(InteractionEventType.class);
		}

		interactionEventTypes.add(eventType);
		input();

		return set(INTERACTION_EVENT_TYPES, interactionEventTypes);
	}

	/**
	 * Allows subclasses to set the parameter relation type. This should
	 * only be
	 * used if subclasses are not able to provide the parameter type at
	 * construction time. In that case this method must be invoked before any
	 * other method is invoked or else an exception will be thrown.
	 *
	 * @param paramType The new parameter relation type
	 */
	protected final void setParameterType(RelationType<T> paramType) {
		this.paramType = paramType;
	}

	/**
	 * Internal method to mark the wrapped relation type to be displayed as
	 * editable in the fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	P input() {
		// ignore the fragment parameter itself (e.g. when registering click
		// handlers on a container) to prevent recursion
		if (paramType != fragment.getFragmentParameter()) {
			fragment.addInputParameters(paramType);
		}

		return (P) this;
	}

	/**
	 * An interaction handler implementation for parameter-related events.
	 *
	 * @author eso
	 */
	class ParameterInteractionHandler implements InteractionHandler {

		private final Map<InteractionEventType, ValueEventHandler<T>>
			eventTypeHandlers = new HashMap<>();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleInteraction(InteractionEvent event) throws Exception {
			ValueEventHandler<T> eventHandler =
				eventTypeHandlers.get(event.getType());

			if (eventHandler != null && fragment.isAttached()) {
				eventHandler.handleValueUpdate(
					fragment.getParameter(paramType));
			}
		}

		/**
		 * Remove the event handler for a certain event type.
		 *
		 * @param eventType The event type
		 */
		void removeEventTypeHandler(InteractionEventType eventType) {
			eventTypeHandlers.remove(eventType);
		}

		/**
		 * Sets or replaces an event handler for a certain event type.
		 *
		 * @param eventType The event type
		 * @param handler   The event handler
		 */
		void setEventTypeHandler(InteractionEventType eventType,
			ValueEventHandler<T> handler) {
			eventTypeHandlers.put(eventType, handler);
		}
	}
}
