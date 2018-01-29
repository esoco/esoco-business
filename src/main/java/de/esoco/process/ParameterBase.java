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
package de.esoco.process;

import de.esoco.data.FileType;
import de.esoco.data.element.DataElementList;

import de.esoco.lib.event.EventHandler;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.function.RelationAccessor;
import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.ContentProperties;
import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.lib.property.LayoutProperties;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.RelativeScale;
import de.esoco.lib.property.RelativeSize;
import de.esoco.lib.property.StyleProperties;
import de.esoco.lib.property.UserInterfaceProperties;

import de.esoco.process.step.Interaction.InteractionHandler;
import de.esoco.process.step.InteractionFragment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationEvent;
import org.obrel.core.RelationType;
import org.obrel.filter.RelationCoupling;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.expression.Predicates.not;
import static de.esoco.lib.property.ContentProperties.CONTENT_TYPE;
import static de.esoco.lib.property.ContentProperties.ICON;
import static de.esoco.lib.property.ContentProperties.LABEL;
import static de.esoco.lib.property.ContentProperties.NULL_VALUE;
import static de.esoco.lib.property.ContentProperties.TOOLTIP;
import static de.esoco.lib.property.LayoutProperties.COLUMNS;
import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HORIZONTAL_ALIGN;
import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.ICON_ALIGN;
import static de.esoco.lib.property.LayoutProperties.ICON_SIZE;
import static de.esoco.lib.property.LayoutProperties.MEDIUM_COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.RELATIVE_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.RELATIVE_WIDTH;
import static de.esoco.lib.property.LayoutProperties.ROWS;
import static de.esoco.lib.property.LayoutProperties.ROW_SPAN;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.LayoutProperties.SMALL_COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.TEXT_ALIGN;
import static de.esoco.lib.property.LayoutProperties.VERTICAL_ALIGN;
import static de.esoco.lib.property.LayoutProperties.WIDTH;
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
	extends ParameterWrapper<T, P>
{
	//~ Static fields/initializers ---------------------------------------------

	private static int nNextFinishActionId = 0;

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
	 * @param rFragment  The fragment to handle the parameter for
	 * @param rParamType The parameter relation type to handle
	 */
	public ParameterBase(
		InteractionFragment rFragment,
		RelationType<T>		rParamType)
	{
		super(rFragment, rParamType);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Enables action events without setting an event handler. The event handler
	 * must either be set later or the containing fragment must implement {@link
	 * InteractionFragment#handleInteraction(RelationType)}.
	 *
	 * @return This instance for concatenation
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
	 * Sets the property {@link LayoutProperties#TEXT_ALIGN}.
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
	 * @return This instance for concatenation
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
	 * Allows the value NULL for a parameter that has constrained values as set
	 * with {@link #allow(Collection)}. If a NULL value item string is set it
	 * will be displayed as an additional selectable value along with the
	 * allowed values. This sets the {@link ContentProperties#NULL_VALUE}.
	 *
	 * @param  sNullValueItem The descriptive string for the NULL value item
	 *
	 * @return This instance for concatenation
	 */
	public final P allowNull(String sNullValueItem)
	{
		return set(NULL_VALUE, sNullValueItem);
	}

	/***************************************
	 * Sets an annotation on the relation of this parameter. The relation must
	 * exist already or else a {@link NullPointerException} will occur.
	 *
	 * @param  rAnnotationType The relation type of the annotation
	 * @param  rValue          The annotation value
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final <A> P annotate(RelationType<A> rAnnotationType, A rValue)
	{
		Relation<T> rParamRelation =
			rFragment.getProcess().getRelation(rParamType);

		rParamRelation.annotate(rAnnotationType, rValue);

		return (P) this;
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
	 * @return This instance for concatenation
	 */
	public final P continuousEvents()
	{
		return interactive(InteractiveInputMode.CONTINUOUS);
	}

	/***************************************
	 * Couples the relation of this parameter with a certain target and/or
	 * source.
	 *
	 * @return This instance for concatenation
	 *
	 * @see    RelationCoupling#couple(org.obrel.core.Relatable, RelationType,
	 *         Consumer, Supplier)
	 */
	@SuppressWarnings("unchecked")
	public P couple(Consumer<T> fUpdateTarget, Supplier<T> fQuerySource)
	{
		RelationCoupling<T> aCoupling =
			RelationCoupling.couple(rFragment.getProcess(),
									rParamType,
									fUpdateTarget,
									fQuerySource);

		rFragment.addCleanupAction("RemoveCoupling_" + nNextFinishActionId++,
								   f -> aCoupling.remove());

		return (P) this;
	}

	/***************************************
	 * Couples the relation of this parameter with another relation in a
	 * relatable object.
	 *
	 * @param  rCoupledRelatable The coupled relatable
	 * @param  rCoupledType      The relation type to couple this parameter with
	 *
	 * @return This instance for concatenation
	 */
	public P couple(Relatable rCoupledRelatable, RelationType<T> rCoupledType)
	{
		RelationAccessor<T> aAccessor =
			new RelationAccessor<>(rCoupledRelatable, rCoupledType);

		return couple(aAccessor, aAccessor);
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
	 * Marks the wrapped relation type to be displayed as readonly in the
	 * fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public P display()
	{
		rFragment.addDisplayParameters(rParamType);

		return (P) this;
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
		rFragment.setParameterValidation(type(),
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
		rFragment.setParameterNotEmptyValidations(type());

		return (P) this;
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
	 * LayoutProperties#ICON_SIZE}.
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
	 * LayoutProperties#ICON_ALIGN}. Not all types of {@link Alignment} may be
	 * supported in an UI implementation.
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
	 * Sets the property {@link LayoutProperties#ICON_SIZE}.
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
	 * Overridden to be public.
	 *
	 * @see ParameterWrapper#input()
	 */
	@Override
	public P input()
	{
		return super.input();
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
	public P layout(LayoutType eLayout)
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
	public final P onAction(ValueEventHandler<T> rEventHandler)
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

		// cleanup action: remove parameter change listener if step is left
		rFragment.addCleanupAction("RemoveChangeListener_" +
								   nNextFinishActionId++,
								   f -> removeChangeListener(rEventHandler));

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
	public final P onFocusLost(ValueEventHandler<T> rEventHandler)
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
	public final P onUpdate(ValueEventHandler<T> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.UPDATE,
										rEventHandler);
	}

	/***************************************
	 * Prepares a download that is associated with an event on this parameter.
	 * This method must be invoked during the handling of the event and the
	 * download will then be executed as the result of the event. After being
	 * processed by the process interaction the generated download URL will be
	 * removed from the parameter.
	 *
	 * @param  sFileName          The file name of the download
	 * @param  eFileType          The file type of the download
	 * @param  fDownloadGenerator The function that generates the download data
	 *
	 * @throws Exception If the download preparation fails
	 */
	public void prepareDownload(String				  sFileName,
								FileType			  eFileType,
								Function<FileType, ?> fDownloadGenerator)
	{
		initiateDownload(this, sFileName, eFileType, fDownloadGenerator);
	}

	/***************************************
	 * Sets the UI properties {@link LayoutProperties#SMALL_COLUMN_SPAN} and
	 * {@link LayoutProperties#MEDIUM_COLUMN_SPAN}.
	 *
	 * @param  nSmall  the number of columns to span in small-size layouts
	 * @param  nMedium the number of columns to span in medium-size layouts
	 *
	 * @return This instance for concatenation
	 */
	public final P responsiveColSpans(int nSmall, int nMedium)
	{
		return set(nSmall, SMALL_COLUMN_SPAN).set(nMedium, MEDIUM_COLUMN_SPAN);
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
		return String.format("%s(%s)", rParamType.getSimpleName(), value());
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
	private final P removeChangeListener(
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
}
