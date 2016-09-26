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
package de.esoco.process;

import de.esoco.data.element.DataElementList;

import de.esoco.lib.event.EventHandler;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.function.AbstractAction;
import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.ContentProperties;
import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.lib.property.Layout;
import de.esoco.lib.property.LayoutProperties;
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.RelativeScale;
import de.esoco.lib.property.RelativeSize;
import de.esoco.lib.property.StyleProperties;
import de.esoco.lib.property.UserInterfaceProperties;

import de.esoco.process.step.Interaction.InteractionHandler;
import de.esoco.process.step.InteractionEvent;
import de.esoco.process.step.InteractionFragment;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obrel.core.RelatedObject;
import org.obrel.core.Relation;
import org.obrel.core.RelationEvent;
import org.obrel.core.RelationType;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.expression.Predicates.not;
import static de.esoco.lib.property.ContentProperties.CONTENT_TYPE;
import static de.esoco.lib.property.ContentProperties.ICON;
import static de.esoco.lib.property.ContentProperties.LABEL;
import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.ContentProperties.TOOLTIP;
import static de.esoco.lib.property.LayoutProperties.COLUMNS;
import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HORIZONTAL_ALIGN;
import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.ICON_ALIGN;
import static de.esoco.lib.property.LayoutProperties.ICON_SIZE;
import static de.esoco.lib.property.LayoutProperties.RELATIVE_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.RELATIVE_WIDTH;
import static de.esoco.lib.property.LayoutProperties.ROWS;
import static de.esoco.lib.property.LayoutProperties.ROW_SPAN;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.LayoutProperties.TEXT_ALIGN;
import static de.esoco.lib.property.LayoutProperties.VERTICAL_ALIGN;
import static de.esoco.lib.property.LayoutProperties.WIDTH;
import static de.esoco.lib.property.StateProperties.DISABLED;
import static de.esoco.lib.property.StateProperties.HIDDEN;
import static de.esoco.lib.property.StateProperties.INTERACTION_EVENT_TYPES;
import static de.esoco.lib.property.StyleProperties.BUTTON_STYLE;
import static de.esoco.lib.property.StyleProperties.CSS_STYLES;
import static de.esoco.lib.property.StyleProperties.HAS_IMAGES;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.STYLE;
import static de.esoco.lib.property.StyleProperties.VERTICAL;


/********************************************************************
 * This is a common superclass for parameter classes that wrap a process
 * parameter relation type. It provides access to typical parameter
 * manipulations through a fluent interface by returning the current instance
 * from all methods. This allows to concatenated arbitrary methods in a single
 * statement to modify the parameter properties.
 *
 * <p>By using a generic self-reference all methods in this class return an
 * instance of the respective subclass to better support the fluent
 * concatenation of method calls. Different subclasses may add additional
 * methods for their specific datatype.</p>
 *
 * @author eso
 */
public abstract class ParameterBase<T, P extends ParameterBase<T, P>>
	extends RelatedObject
{
	//~ Instance fields --------------------------------------------------------

	private final InteractionFragment rFragment;
	private final RelationType<T>     rParamType;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain fragment and parameter relation
	 * type.
	 *
	 * @param rFragment  The fragment to handle the parameter for
	 * @param rParamType The parameter relation type to handle
	 */
	public ParameterBase(
		InteractionFragment rFragment,
		RelationType<T>		rParamType)
	{
		this.rFragment  = rFragment;
		this.rParamType = rParamType;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Enables action events without setting an event handler. The event handler
	 * must either be set later or the containing fragment must implement {@link
	 * InteractionFragment#handleInteraction(RelationType)}.
	 *
	 * @see #actionEvents(InteractionHandler)
	 */
	public final P actionEvents()
	{
		return interactive(InteractiveInputMode.ACTION);
	}

	/***************************************
	 * Sets the property {@link LayoutProperties#HORIZONTAL_ALIGN}.
	 *
	 * @param  eAlignment The alignment
	 *
	 * @return This instance for concatenation
	 */
	public final P alignHorizontal(Alignment eAlignment)
	{
		return set(HORIZONTAL_ALIGN, eAlignment);
	}

	/***************************************
	 * Sets the property {@link LayoutProperties#ICON_ALIGN}.
	 *
	 * @param  eAlignment The alignment of the icon
	 *
	 * @return This instance for concatenation
	 */
	public final P alignIcon(Alignment eAlignment)
	{
		return set(ICON_ALIGN, eAlignment);
	}

	/***************************************
	 * Sets the property {@link LayoutProperties#TEXT_ALIGNMENT}.
	 *
	 * @param  eAlignment The alignment
	 *
	 * @return This instance for concatenation
	 */
	public final P alignText(Alignment eAlignment)
	{
		return set(TEXT_ALIGN, eAlignment);
	}

	/***************************************
	 * Sets the property {@link LayoutProperties#VERTICAL_ALIGN}.
	 *
	 * @param  eAlignment The alignment
	 *
	 * @return This instance for concatenation
	 */
	public final P alignVertical(Alignment eAlignment)
	{
		return set(VERTICAL_ALIGN, eAlignment);
	}

	/***************************************
	 * Enables all events without setting an event handler. The event handler
	 * must either be set later or the containing fragment must implement {@link
	 * InteractionFragment#handleInteraction(RelationType)}.
	 *
	 * @see #actionEvents(InteractionHandler)
	 */
	public final P allEvents()
	{
		return interactive(InteractiveInputMode.BOTH);
	}

	/***************************************
	 * Sets the allowed values for the parameter.
	 *
	 * @see ProcessFragment#setAllowedValues(RelationType, Object...)
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final P allow(T... rValues)
	{
		rFragment.setAllowedValues(rParamType, rValues);

		return (P) this;
	}

	/***************************************
	 * Sets the allowed values for the parameter.
	 *
	 * @see ProcessFragment#setAllowedValues(RelationType, Object...)
	 */
	@SuppressWarnings("unchecked")
	public final P allow(Collection<T> rValues)
	{
		rFragment.setAllowedValues(rParamType, rValues);

		return (P) this;
	}

	/***************************************
	 * Returns the values that this parameter is allowed to contain.
	 *
	 * @return The allowed values (can be NULL)
	 */
	public Collection<T> allowedValues()
	{
		return rFragment.getAllowedValues(rParamType);
	}

	/***************************************
	 * Marks this parameter to be displayed as interactive buttons. It's list
	 * style will be set to {@link ListStyle#IMMEDIATE}, it will have the flag
	 * {@link UserInterfaceProperties#HIDE_LABEL} set, and the property {@link
	 * UserInterfaceProperties#COLUMNS} will be set to the number of allowed
	 * values. This will also add this parameter as an input parameter to the
	 * fragment.
	 *
	 * @param  rAllowedValues Optionally the allowed values for this parameter
	 *                        (NULL or empty for the default)
	 *
	 * @return This instance for concatenation
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final P buttons(T... rAllowedValues)
	{
		interactive(ListStyle.IMMEDIATE);
		hideLabel();

		if (rAllowedValues != null && rAllowedValues.length > 0)
		{
			allow(rAllowedValues);
			set(rAllowedValues.length, COLUMNS);
		}
		else
		{
			int nValueCount = rFragment.getAllowedValues(rParamType).size();

			if (nValueCount > 0)
			{
				set(nValueCount, COLUMNS);
			}
		}

		return (P) this;
	}

	/***************************************
	 * Mark this parameter to be displayed with a certain button style.
	 *
	 * @param  eButtonStyle The button style
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #buttons(Object...)
	 */
	public final P buttonStyle(ButtonStyle eButtonStyle)
	{
		return set(BUTTON_STYLE, eButtonStyle);
	}

	/***************************************
	 * Checks whether the value of this parameter fulfills a certain condition.
	 *
	 * @param  pValueCondition The predicate that checks the condition
	 *
	 * @return TRUE if the condition is fulfilled
	 */
	@SuppressWarnings("boxing")
	public boolean check(Predicate<? super T> pValueCondition)
	{
		return pValueCondition.evaluate(value());
	}

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
	 * Sets the UI property {@link UserInterfaceProperties#COLUMN_SPAN}.
	 *
	 * @param  nColumns the number of columns to span.
	 *
	 * @return This instance for concatenation
	 */
	public final P colSpan(int nColumns)
	{
		return set(nColumns, COLUMN_SPAN);
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#COLUMNS}.
	 *
	 * @param  nColumns the number of columns.
	 *
	 * @return This instance for concatenation
	 */
	public final P columns(int nColumns)
	{
		return set(nColumns, COLUMNS);
	}

	/***************************************
	 * Sets the content type of this parameter.
	 *
	 * @param  eContentType The content type
	 *
	 * @return This instance for concatenation
	 */
	public final P content(ContentType eContentType)
	{
		return set(CONTENT_TYPE, eContentType);
	}

	/***************************************
	 * Sets whether the process should continue on an interaction with this
	 * parameter or not. If an application needs to change this state during an
	 * interaction it should first be set to TRUE so that the (final) process
	 * step validations that are performed on transitions to another step work
	 * correctly.
	 *
	 * @param  bContinue TRUE to continue process execution, FALSE to stay in
	 *                   the current step and wait for further interactions
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P continueOnInteraction(boolean bContinue)
	{
		fragment().setContinueOnInteraction(bContinue, type());

		return (P) this;
	}

	/***************************************
	 * Enables continuous events without setting an event handler. The event
	 * handler must either be set later or the containing fragment must
	 * implement {@link InteractionFragment#handleInteraction(RelationType)}.
	 *
	 * @see #actionEvents(InteractionHandler)
	 */
	public final P continuousEvents()
	{
		return interactive(InteractiveInputMode.CONTINUOUS);
	}

	/***************************************
	 * Sets a CSS style property for the parameter.
	 *
	 * @param  sCssProperty The name of the CSS property
	 * @param  sValue       The value of the CSS property or NULL to clear
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P css(String sCssProperty, String sValue)
	{
		Map<String, String> rCssStyles =
			rFragment.getUIProperty(CSS_STYLES, rParamType);

		if (rCssStyles == null)
		{
			rCssStyles = new HashMap<>();
		}

		rCssStyles.put(sCssProperty, sValue != null ? sValue : "");
		set(CSS_STYLES, rCssStyles);

		return (P) this;
	}

	/***************************************
	 * Marks this parameter to be disabled in the user interface.
	 *
	 * @return This instance for concatenation
	 */
	public final P disable()
	{
		return set(DISABLED);
	}

	/***************************************
	 * Marks the wrapped relation type to be displayed as readonly in the
	 * fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P display()
	{
		rFragment.addDisplayParameters(rParamType);

		return (P) this;
	}

	/***************************************
	 * Marks this parameter to be disabled in the user interface.
	 *
	 * @return This instance for concatenation
	 */
	public final P enable()
	{
		return clear(DISABLED);
	}

	/***************************************
	 * Sets a general validation for this parameter.
	 *
	 * @param  pValueConstraint The constraint that must be valid
	 * @param  sErrorMessage    The error message to be displayed for the
	 *                          parameter in the case of a constraint violation
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public P ensure(Predicate<? super T> pValueConstraint, String sErrorMessage)
	{
		fragment().setParameterValidation(type(),
										  sErrorMessage,
										  not(pValueConstraint));

		return (P) this;
	}

	/***************************************
	 * Sets a not empty validation for this parameter.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public P ensureNotEmpty()
	{
		fragment().setParameterNotEmptyValidations(type());

		return (P) this;
	}

	/***************************************
	 * Returns the fragment this parameter belongs to (i.e. where it is
	 * displayed).
	 *
	 * @return The fragment
	 */
	public final InteractionFragment fragment()
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
	 * Sets the pixel width of an element in the UI property {@link
	 * LayoutProperties#WIDTH}.
	 *
	 * @param  nHeight nWidth The width
	 *
	 * @return This instance for concatenation
	 */
	public final P height(int nHeight)
	{
		return set(nHeight, HEIGHT);
	}

	/***************************************
	 * Sets the UI property {@link LayoutProperties#RELATIVE_WIDTH}.
	 *
	 * @param  eHeight sWidth The relative width constant
	 *
	 * @return This instance for concatenation
	 */
	public final P height(RelativeSize eHeight)
	{
		return set(RELATIVE_HEIGHT, eHeight);
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#HTML_HEIGHT}.
	 *
	 * @param  sHeight The HTML height string
	 *
	 * @return This instance for concatenation
	 */
	public final P height(String sHeight)
	{
		return set(HTML_HEIGHT, sHeight);
	}

	/***************************************
	 * Marks this parameter to be hidden in the user interface.
	 *
	 * @return This instance for concatenation
	 */
	public final P hide()
	{
		return set(HIDDEN);
	}

	/***************************************
	 * Hides the label of this parameter.
	 *
	 * @return This instance for concatenation
	 */
	public final P hideLabel()
	{
		return set(HIDE_LABEL);
	}

	/***************************************
	 * Sets the UI property {@link ContentProperties#ICON}.
	 *
	 * @param  rIconIdentifier An identifier that describes the icon to display;
	 *                         will be converted to a string and should
	 *                         typically either be a string of an enum constant
	 *
	 * @return This instance for concatenation
	 */
	public final P icon(Object rIconIdentifier)
	{
		return set(ICON, rIconIdentifier.toString());
	}

	/***************************************
	 * Sets both UI properties {@link ContentProperties#ICON} and {@link
	 * StyleProperties#ICON_SIZE}.
	 *
	 * @param  rIconIdentifier The icon identifier ({@link #icon(Object)})
	 * @param  eSize           The relative size of the icon
	 *
	 * @return This instance for concatenation
	 */
	public final P icon(Object rIconIdentifier, RelativeScale eSize)
	{
		return icon(rIconIdentifier).iconSize(eSize);
	}

	/***************************************
	 * Sets both UI properties {@link ContentProperties#ICON} and {@link
	 * StyleProperties#ICON_ALIGNMENT}. Not all types of {@link Alignment} may
	 * be supported in an UI implementation.
	 *
	 * @param  rIconIdentifier The icon identifier ({@link #icon(Object)})
	 * @param  eAlignment      The position alignment of the icon
	 *
	 * @return This instance for concatenation
	 */
	public final P icon(Object rIconIdentifier, Alignment eAlignment)
	{
		return icon(rIconIdentifier).alignIcon(eAlignment);
	}

	/***************************************
	 * Sets the property {@link StyleProperties#ICON_SIZE}.
	 *
	 * @param  eSize The relative size of the icon
	 *
	 * @return This instance for concatenation
	 */
	public final P iconSize(RelativeScale eSize)
	{
		return set(ICON_SIZE, eSize);
	}

	/***************************************
	 * Sets the flag property {@link StyleProperties#HAS_IMAGES}.
	 *
	 * @return This instance for concatenation
	 */
	public final P images()
	{
		return set(HAS_IMAGES);
	}

	/***************************************
	 * Transfers certain properties from the parent fragment to this parameter.
	 *
	 * @param  rProperties The properties to transfer
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final P inherit(PropertyName<?>... rProperties)
	{
		for (PropertyName rProperty : rProperties)
		{
			set(rProperty, rFragment.fragmentParam().get(rProperty));
		}

		return (P) this;
	}

	/***************************************
	 * Marks the wrapped relation type to be displayed as editable in the
	 * fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P input()
	{
		rFragment.addInputParameters(rParamType);

		return (P) this;
	}

	/***************************************
	 * Sets the interactive input mode for this parameter.
	 *
	 * @param  eEventType eInputMode The interactive input mode
	 *
	 * @return This instance for concatenation
	 */
	public final P interactive(InteractionEventType eEventType)
	{
		Set<InteractionEventType> rInteractionEventTypes =
			get(INTERACTION_EVENT_TYPES);

		if (rInteractionEventTypes == null)
		{
			rInteractionEventTypes = EnumSet.noneOf(InteractionEventType.class);
		}

		rInteractionEventTypes.add(eEventType);

		return input().set(INTERACTION_EVENT_TYPES, rInteractionEventTypes);
	}

	/***************************************
	 * Sets the interactive input mode for this parameter.
	 *
	 * @param  eInputMode The interactive input mode
	 *
	 * @return This instance for concatenation
	 */
	public final P interactive(InteractiveInputMode eInputMode)
	{
		rFragment.setInteractive(eInputMode, rParamType);

		return input();
	}

	/***************************************
	 * Sets a parameter with a list of allowed values to be displayed in a
	 * certain interactive list style.
	 *
	 * @param  eListStyle The style in which to display the allowed values
	 *
	 * @return This instance for concatenation
	 */
	public final P interactive(ListStyle eListStyle)
	{
		rFragment.setInteractive(rParamType, null, eListStyle);

		return input();
	}

	/***************************************
	 * Checks the value of a boolean property.
	 *
	 * @see #get(PropertyName)
	 */
	public final boolean is(PropertyName<Boolean> rFlagProperty)
	{
		return get(rFlagProperty) == Boolean.TRUE;
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#LABEL}.
	 *
	 * @param  sLabel sWidth The label string
	 *
	 * @return This instance for concatenation
	 */
	public final P label(String sLabel)
	{
		return set(LABEL, sLabel);
	}

	/***************************************
	 * Sets the layout for the panel of a parameter. This will only be valid if
	 * the given parameter is rendered in a panel (like {@link DataElementList}
	 * or buttons).
	 *
	 * @param  eLayout The panel layout
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public P layout(Layout eLayout)
	{
		set(UserInterfaceProperties.LAYOUT, eLayout);

		return (P) this;
	}

	/***************************************
	 * Marks this parameter as modified.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P modified()
	{
		rFragment.markParameterAsModified(rParamType);

		return (P) this;
	}

	/***************************************
	 * Notifies all listeners that have been registered to listen for parameter
	 * updates with {@link #onChange(EventHandler)}.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P notifyChangeListeners()
	{
		value(value());

		return (P) this;
	}

	/***************************************
	 * Sets a simple event handler for action events of this parameter.
	 *
	 * @param  rEventHandler The event handler to be invoked on an event
	 *
	 * @return This instance for concatenation
	 */
	public final P onAction(ParameterEventHandler<T> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.ACTION,
										rEventHandler);
	}

	/***************************************
	 * Registers an event handler that will be notified of changes of this
	 * parameter's relation.
	 *
	 * @param  rEventHandler The event handler to register
	 *
	 * @return This instance for concatenation
	 *
	 * @see    Relation#addUpdateListener(EventHandler)
	 */
	@SuppressWarnings("unchecked")
	public final P onChange(final EventHandler<RelationEvent<T>> rEventHandler)
	{
		Relation<T> rRelation = rFragment.getParameterRelation(rParamType);

		if (rRelation == null)
		{
			rRelation = rFragment.setParameter(rParamType, null);
		}

		rRelation.addUpdateListener(rEventHandler);

		// cleanup: remove parameter change listener if step is left
		rFragment.getProcessStep()
				 .addFinishAction(rParamType.getName(),
			new AbstractAction<ProcessStep>("")
			{
				@Override
				public void execute(ProcessStep rStep)
				{
					removeChangeListener(rEventHandler);
				}
			});

		return (P) this;
	}

	/***************************************
	 * Sets the event handler for this parameter. The handler will only be
	 * invoked if events have been enabled for this parameter with one of the
	 * corresponding methods.
	 *
	 * @param  rEventHandler The event handler to register
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #interactive(InteractiveInputMode)
	 * @see    #interactive(ListStyle)
	 * @see    #continuousEvents()
	 * @see    #actionEvents()
	 * @see    #allEvents()
	 */
	@SuppressWarnings("unchecked")
	public final P onEvent(InteractionHandler rEventHandler)
	{
		rFragment.setParameterInteractionHandler(rParamType, rEventHandler);

		return (P) this;
	}

	/***************************************
	 * Sets a simple event handler for action events of this parameter.
	 *
	 * @param  rEventHandler The event handler to be invoked on an event
	 *
	 * @return This instance for concatenation
	 */
	public final P onFocusLost(ParameterEventHandler<T> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.FOCUS_LOST,
										rEventHandler);
	}

	/***************************************
	 * Sets an event handler for update events of this parameter.
	 *
	 * @param  rEventHandler The event handler to be invoked on an event
	 *
	 * @return This instance for concatenation
	 */
	public final P onUpdate(ParameterEventHandler<T> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.UPDATE,
										rEventHandler);
	}

	/***************************************
	 * Sets a certain property flag.
	 *
	 * @see ProcessFragment#setUIFlag(PropertyName, RelationType...)
	 */
	@SuppressWarnings("unchecked")
	public final P remove(PropertyName<?> rProperty)
	{
		rFragment.removeUIProperties(rParamType);

		return (P) this;
	}

	/***************************************
	 * Removes an parameter update listener that had been set with {@link
	 * #onChange(EventHandler)}.
	 *
	 * @param  rEventHandler The event listener to remove
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #onChange(EventHandler)
	 */
	@SuppressWarnings("unchecked")
	public final P removeChangeListener(
		EventHandler<RelationEvent<T>> rEventHandler)
	{
		Relation<T> rRelation = rFragment.getParameterRelation(rParamType);

		if (rRelation != null)
		{
			rRelation.get(StandardTypes.RELATION_UPDATE_LISTENERS)
					 .remove(rEventHandler);
		}

		return (P) this;
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#RESOURCE_ID}.
	 *
	 * @param  sResourceId sWidth The resource ID string
	 *
	 * @return This instance for concatenation
	 */
	public final P resid(String sResourceId)
	{
		return set(RESOURCE_ID, sResourceId);
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#ROWS}.
	 *
	 * @param  nRows the number of rows.
	 *
	 * @return This instance for concatenation
	 */
	public final P rows(int nRows)
	{
		return set(nRows, ROWS);
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#ROW_SPAN}.
	 *
	 * @param  nRows the number of rows to span.
	 *
	 * @return This instance for concatenation
	 */
	public final P rowSpan(int nRows)
	{
		return set(nRows, ROW_SPAN);
	}

	/***************************************
	 * Marks this parameter to be displayed in the same row as the previous
	 * parameter (in grid and table layouts).
	 *
	 * @return This instance for concatenation
	 */
	public final P sameRow()
	{
		return set(SAME_ROW);
	}

	/***************************************
	 * Marks this parameter to be displayed in the same row as the previous
	 * parameter with a certain column span in a grid or table layout.
	 *
	 * @param  nColumnSpan The number of columns that the parameter UI should
	 *                     span
	 *
	 * @return This instance for concatenation
	 */
	public final P sameRow(int nColumnSpan)
	{
		return sameRow().colSpan(nColumnSpan);
	}

	/***************************************
	 * Marks this parameter to be displayed in the same row as the previous
	 * parameter with a certain width in a grid layout.
	 *
	 * @param  eColumnWidth The relative width of the parameter UI in a grid
	 *                      layout
	 *
	 * @return This instance for concatenation
	 */
	public final P sameRow(RelativeSize eColumnWidth)
	{
		return sameRow().width(eColumnWidth);
	}

	/***************************************
	 * Sets a one or more property flags.
	 *
	 * @see ProcessFragment#setUIFlag(PropertyName, RelationType...)
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final P set(PropertyName<Boolean>... rFlagProperties)
	{
		for (PropertyName<Boolean> rFlag : rFlagProperties)
		{
			rFragment.setUIFlag(rFlag, rParamType);
		}

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
	 *
	 * @see    #enable()
	 * @see    #disable()
	 */
	public final P setEnabled(boolean bEnabled)
	{
		return bEnabled ? enable() : disable();
	}

	/***************************************
	 * Sets the visibility of this parameter based on the boolean parameter.
	 *
	 * @param  bVisible TRUE to show the parameter, FALSE to hide it
	 *
	 * @return This instance for concatenation
	 *
	 * @see    #hide()
	 * @see    #show()
	 */
	public final P setVisible(boolean bVisible)
	{
		return bVisible ? show() : hide();
	}

	/***************************************
	 * Marks this parameter to be visible in the user interface.
	 *
	 * @return This instance for concatenation
	 */
	public final P show()
	{
		return clear(HIDDEN);
	}

	/***************************************
	 * Invokes {@link #width(String)} and {@link #height(String)}.
	 *
	 * @param  sWidth  The HTML width string
	 * @param  sHeight The HTML height string
	 *
	 * @return This instance for concatenation
	 */
	public final P size(String sWidth, String sHeight)
	{
		return width(sWidth).height(sHeight);
	}

	/***************************************
	 * Queries the UI property {@link UserInterfaceProperties#STYLE}.
	 *
	 * @return The style name (NULL for none)
	 */
	public final String style()
	{
		return get(STYLE);
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#STYLE}.
	 *
	 * @param  sStyle The style name(s)
	 *
	 * @return This instance for concatenation
	 */
	public final P style(String sStyle)
	{
		return set(STYLE, sStyle);
	}

	/***************************************
	 * Appends a parameter to the row of this one. This is the same as invoking
	 * the method {@link #sameRow()} on the argument parameter.
	 *
	 * @param  rParameter The parameter to add to the current row
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public P then(ParameterBase<?, ?> rParameter)
	{
		rParameter.sameRow();

		return (P) this;
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#TOOLTIP}.
	 *
	 * @param  sTooltip sWidth The tooltip string
	 *
	 * @return This instance for concatenation
	 */
	public final P tooltip(String sTooltip)
	{
		return set(TOOLTIP, sTooltip);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + rParamType + "]";
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
	 * Validates that the value of this parameter fulfills a certain constraint.
	 * If not an exception will be thrown.
	 *
	 * @param  pValueConstraint The constraint to be validated
	 * @param  sErrorMessage    The error message to be displayed for the
	 *                          parameter in the case of a constraint violation
	 *
	 * @return This instance for concatenation
	 *
	 * @throws InvalidParametersException If the constraint is violated
	 */
	public P validate(
		Predicate<? super T> pValueConstraint,
		String				 sErrorMessage) throws InvalidParametersException
	{
		return validate(pValueConstraint, sErrorMessage, null);
	}

	/***************************************
	 * Validates that the value of this parameter fulfills a certain constraint.
	 * If not an exception will be thrown.
	 *
	 * @param  pValueConstraint The constraint to be validated
	 * @param  sErrorMessage    The error message to be displayed for the
	 *                          parameter in the case of a constraint violation
	 * @param  rRunOnViolation  A runnable to be executed if the constraint is
	 *                          violated
	 *
	 * @return This instance for concatenation
	 *
	 * @throws InvalidParametersException If the constraint is violated
	 */
	@SuppressWarnings("unchecked")
	public P validate(Predicate<? super T> pValueConstraint,
					  String			   sErrorMessage,
					  Runnable			   rRunOnViolation)
		throws InvalidParametersException
	{
		if (!check(pValueConstraint))
		{
			if (rRunOnViolation != null)
			{
				rRunOnViolation.run();
			}

			fragment().validationError(Collections
									   .<RelationType<?>, String>singletonMap(type(),
																			  sErrorMessage));

			throw new InvalidParametersException(fragment(),
												 sErrorMessage,
												 type());
		}

		return (P) this;
	}

	/***************************************
	 * Returns the value of the wrapped parameter.
	 *
	 * @see ProcessFragment#getParameter(RelationType)
	 */
	public final T value()
	{
		return rFragment.getParameter(rParamType);
	}

	/***************************************
	 * Sets the parameter value.
	 *
	 * @see ProcessFragment#setParameter(RelationType, Object)
	 */
	@SuppressWarnings("unchecked")
	public final P value(T rValue)
	{
		rFragment.setParameter(rParamType, rValue);

		return (P) this;
	}

	/***************************************
	 * Marks this parameter to have a vertical orientation (instead if the
	 * horizontal default).
	 *
	 * @return This instance for concatenation
	 */
	public final P vertical()
	{
		return set(VERTICAL);
	}

	/***************************************
	 * Sets the UI property {@link LayoutProperties#HTML_WIDTH}.
	 *
	 * @param  sWidth The HTML width string
	 *
	 * @return This instance for concatenation
	 */
	public final P width(String sWidth)
	{
		return set(HTML_WIDTH, sWidth);
	}

	/***************************************
	 * Sets the UI property {@link LayoutProperties#RELATIVE_WIDTH}.
	 *
	 * @param  eWidth The relative width constant
	 *
	 * @return This instance for concatenation
	 */
	public final P width(RelativeSize eWidth)
	{
		return set(RELATIVE_WIDTH, eWidth);
	}

	/***************************************
	 * Sets the pixel width of an element in the UI property {@link
	 * LayoutProperties#WIDTH}.
	 *
	 * @param  nWidth The width
	 *
	 * @return This instance for concatenation
	 */
	public final P width(int nWidth)
	{
		return set(nWidth, WIDTH);
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
	private P setParameterEventHandler(
		InteractionEventType		   eEventType,
		final ParameterEventHandler<T> rEventHandler)
	{
		InteractionHandler rInteractionHandler =
			rFragment.getParameterInteractionHandler(rParamType);

		if (rInteractionHandler instanceof
			ParameterBase.ParameterInteractionHandler)
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

		return interactive(eEventType);
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

		private Map<InteractionEventType, ParameterEventHandler<T>> aEventTypeHandlers =
			new HashMap<>();

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void handleInteraction(InteractionEvent rEvent) throws Exception
		{
			ParameterEventHandler<T> rEventHandler =
				aEventTypeHandlers.get(rEvent.getType());

			if (rEventHandler != null)
			{
				rEventHandler.handleParameterUpdate(value());
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
			InteractionEventType	 eEventType,
			ParameterEventHandler<T> rHandler)
		{
			aEventTypeHandlers.put(eEventType, rHandler);
		}
	}
}
