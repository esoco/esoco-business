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
import de.esoco.process.InvalidParametersException;
import de.esoco.process.ProcessFragment;
import de.esoco.process.ValueEventHandler;
import de.esoco.process.step.Interaction.InteractionHandler;
import de.esoco.process.step.InteractionFragment;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationEvent;
import org.obrel.core.RelationType;
import org.obrel.filter.RelationCoupling;
import org.obrel.type.ListenerTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

/**
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
	extends ParameterWrapper<T, P> {

	private static int nextFinishActionId = 0;

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
	 * @param fragment  The fragment to handle the parameter for
	 * @param paramType The parameter relation type to handle
	 */
	public ParameterBase(InteractionFragment fragment,
		RelationType<T> paramType) {
		super(fragment, paramType);
	}

	/**
	 * Enables action events without setting an event handler. The event
	 * handler
	 * must either be set later or the containing fragment must implement
	 * {@link InteractionFragment#handleInteraction(RelationType)}.
	 *
	 * @return This instance for concatenation
	 */
	public final P actionEvents() {
		return interactive(InteractiveInputMode.ACTION);
	}

	/**
	 * Sets the property {@link LayoutProperties#HORIZONTAL_ALIGN}.
	 *
	 * @param alignment The alignment
	 * @return This instance for concatenation
	 */
	public final P alignHorizontal(Alignment alignment) {
		return set(HORIZONTAL_ALIGN, alignment);
	}

	/**
	 * Sets the property {@link LayoutProperties#ICON_ALIGN}.
	 *
	 * @param alignment The alignment of the icon
	 * @return This instance for concatenation
	 */
	public final P alignIcon(Alignment alignment) {
		return set(ICON_ALIGN, alignment);
	}

	/**
	 * Sets the property {@link LayoutProperties#TEXT_ALIGN}.
	 *
	 * @param alignment The alignment
	 * @return This instance for concatenation
	 */
	public final P alignText(Alignment alignment) {
		return set(TEXT_ALIGN, alignment);
	}

	/**
	 * Sets the property {@link LayoutProperties#VERTICAL_ALIGN}.
	 *
	 * @param alignment The alignment
	 * @return This instance for concatenation
	 */
	public final P alignVertical(Alignment alignment) {
		return set(VERTICAL_ALIGN, alignment);
	}

	/**
	 * Enables all events without setting an event handler. The event handler
	 * must either be set later or the containing fragment must implement
	 * {@link InteractionFragment#handleInteraction(RelationType)}.
	 *
	 * @return This instance for concatenation
	 */
	public final P allEvents() {
		return interactive(InteractiveInputMode.BOTH);
	}

	/**
	 * Sets the allowed values for the parameter.
	 *
	 * @see ProcessFragment#setAllowedValues(RelationType, Object...)
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final P allow(T... values) {
		fragment.setAllowedValues(paramType, values);

		return (P) this;
	}

	/**
	 * Sets the allowed values for the parameter.
	 *
	 * @see ProcessFragment#setAllowedValues(RelationType, Object...)
	 */
	@SuppressWarnings("unchecked")
	public final P allow(Collection<T> values) {
		fragment.setAllowedValues(paramType, values);

		return (P) this;
	}

	/**
	 * Allows the value NULL for a parameter that has constrained values as set
	 * with {@link #allow(Collection)}. If a NULL value item string is set it
	 * will be displayed as an additional selectable value along with the
	 * allowed values. This sets the {@link ContentProperties#NULL_VALUE}.
	 *
	 * @param nullValueItem The descriptive string for the NULL value item
	 * @return This instance for concatenation
	 */
	public final P allowNull(String nullValueItem) {
		return set(NULL_VALUE, nullValueItem);
	}

	/**
	 * Returns the values that this parameter is allowed to contain.
	 *
	 * @return The allowed values (can be NULL)
	 */
	public Collection<T> allowedValues() {
		return fragment.getAllowedValues(paramType);
	}

	/**
	 * Sets an annotation on the relation of this parameter. The relation must
	 * exist already or else a {@link NullPointerException} will occur.
	 *
	 * @param annotationType The relation type of the annotation
	 * @param value          The annotation value
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final <A> P annotate(RelationType<A> annotationType, A value) {
		Relation<T> paramRelation =
			fragment.getProcess().getRelation(paramType);

		paramRelation.annotate(annotationType, value);

		return (P) this;
	}

	/**
	 * Mark this parameter to be displayed with a certain button style.
	 *
	 * @param buttonStyle The button style
	 * @return This instance for concatenation
	 * @see #buttons(Object...)
	 */
	public final P buttonStyle(ButtonStyle buttonStyle) {
		return set(BUTTON_STYLE, buttonStyle);
	}

	/**
	 * Marks this parameter to be displayed as interactive buttons. It's list
	 * style will be set to {@link ListStyle#IMMEDIATE}, it will have the flag
	 * {@link UserInterfaceProperties#HIDE_LABEL} set, and the property
	 * {@link UserInterfaceProperties#COLUMNS} will be set to the number of
	 * allowed values. This will also add this parameter as an input parameter
	 * to the fragment.
	 *
	 * @param allowedValues Optionally the allowed values for this parameter
	 *                      (NULL or empty for the default)
	 * @return This instance for concatenation
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final P buttons(T... allowedValues) {
		interactive(ListStyle.IMMEDIATE);
		hideLabel();

		if (allowedValues != null && allowedValues.length > 0) {
			allow(allowedValues);
			set(allowedValues.length, COLUMNS);
		} else {
			int valueCount = fragment.getAllowedValues(paramType).size();

			if (valueCount > 0) {
				set(valueCount, COLUMNS);
			}
		}

		return (P) this;
	}

	/**
	 * Checks whether the value of this parameter fulfills a certain condition.
	 *
	 * @param valueCondition The predicate that checks the condition
	 * @return TRUE if the condition is fulfilled
	 */
	@SuppressWarnings("boxing")
	public boolean check(Predicate<? super T> valueCondition) {
		return valueCondition.evaluate(value());
	}

	/**
	 * Sets the UI property {@link UserInterfaceProperties#COLUMN_SPAN}.
	 *
	 * @param columns the number of columns to span.
	 * @return This instance for concatenation
	 */
	public final P colSpan(int columns) {
		return set(columns, COLUMN_SPAN);
	}

	/**
	 * Sets the UI property {@link UserInterfaceProperties#COLUMNS}.
	 *
	 * @param columns the number of columns.
	 * @return This instance for concatenation
	 */
	public final P columns(int columns) {
		return set(columns, COLUMNS);
	}

	/**
	 * Sets the content type of this parameter.
	 *
	 * @param contentType The content type
	 * @return This instance for concatenation
	 */
	public final P content(ContentType contentType) {
		return set(CONTENT_TYPE, contentType);
	}

	/**
	 * Sets whether the process should continue on an interaction with this
	 * parameter or not. If an application needs to change this state during an
	 * interaction it should first be set to TRUE so that the (final) process
	 * step validations that are performed on transitions to another step work
	 * correctly.
	 *
	 * @param continueOnInterations TRUE to continue process execution,
	 *                                 FALSE to
	 *                              stay in the current step and wait for
	 *                              further interactions
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P continueOnInteraction(boolean continueOnInterations) {
		fragment().setContinueOnInteraction(continueOnInterations, type());

		return (P) this;
	}

	/**
	 * Enables continuous events without setting an event handler. The event
	 * handler must either be set later or the containing fragment must
	 * implement {@link InteractionFragment#handleInteraction(RelationType)}.
	 *
	 * @return This instance for concatenation
	 */
	public final P continuousEvents() {
		return interactive(InteractiveInputMode.CONTINUOUS);
	}

	/**
	 * Couples the relation of this parameter with a certain target and/or
	 * source.
	 *
	 * @return This instance for concatenation
	 * @see RelationCoupling#couple(org.obrel.core.Relatable, RelationType,
	 * Consumer, Supplier)
	 */
	@SuppressWarnings("unchecked")
	public P couple(Consumer<T> updateTarget, Supplier<T> querySource) {
		RelationCoupling<T> coupling =
			RelationCoupling.couple(fragment.getProcess(), paramType,
				updateTarget, querySource);

		fragment.addCleanupAction("RemoveCoupling_" + nextFinishActionId++,
			f -> coupling.remove());

		return (P) this;
	}

	/**
	 * Couples the relation of this parameter with another relation in a
	 * relatable object.
	 *
	 * @param coupledRelatable The coupled relatable
	 * @param coupledType      The relation type to couple this parameter with
	 * @return This instance for concatenation
	 */
	public P couple(Relatable coupledRelatable, RelationType<T> coupledType) {
		RelationAccessor<T> accessor =
			new RelationAccessor<>(coupledRelatable, coupledType);

		return couple(accessor, accessor);
	}

	/**
	 * Sets a CSS style property for the parameter.
	 *
	 * @param cssProperty The name of the CSS property
	 * @param value       The value of the CSS property or NULL to clear
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P css(String cssProperty, String value) {
		Map<String, String> cssStyles =
			fragment.getUIProperty(CSS_STYLES, paramType);

		if (cssStyles == null) {
			cssStyles = new HashMap<>();
		}

		cssStyles.put(cssProperty, value != null ? value : "");
		set(CSS_STYLES, cssStyles);

		return (P) this;
	}

	/**
	 * Marks the wrapped relation type to be displayed as readonly in the
	 * fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public P display() {
		fragment.addDisplayParameters(paramType);

		return (P) this;
	}

	/**
	 * Sets a general validation for this parameter.
	 *
	 * @param valueConstraint The constraint that must be valid
	 * @param errorMessage    The error message to be displayed for the
	 *                        parameter in the case of a constraint violation
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public P ensure(Predicate<? super T> valueConstraint,
		String errorMessage) {
		fragment.setParameterValidation(type(), errorMessage,
			not(valueConstraint));

		return (P) this;
	}

	/**
	 * Sets a not empty validation for this parameter.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public P ensureNotEmpty() {
		fragment.setParameterNotEmptyValidations(type());

		return (P) this;
	}

	/**
	 * Sets the pixel width of an element in the UI property
	 * {@link LayoutProperties#WIDTH}.
	 *
	 * @param height width The width
	 * @return This instance for concatenation
	 */
	public final P height(int height) {
		return set(height, HEIGHT);
	}

	/**
	 * Sets the UI property {@link LayoutProperties#RELATIVE_WIDTH}.
	 *
	 * @param height width The relative width constant
	 * @return This instance for concatenation
	 */
	public final P height(RelativeSize height) {
		return set(RELATIVE_HEIGHT, height);
	}

	/**
	 * Sets the UI property {@link UserInterfaceProperties#HTML_HEIGHT}.
	 *
	 * @param height The HTML height string
	 * @return This instance for concatenation
	 */
	public final P height(String height) {
		return set(HTML_HEIGHT, height);
	}

	/**
	 * Hides the label of this parameter.
	 *
	 * @return This instance for concatenation
	 */
	public final P hideLabel() {
		return set(HIDE_LABEL);
	}

	/**
	 * Sets the UI property {@link ContentProperties#ICON}.
	 *
	 * @param iconIdentifier An identifier that describes the icon to display;
	 *                       will be converted to a string and should typically
	 *                       either be a string of an enum constant
	 * @return This instance for concatenation
	 */
	public final P icon(Object iconIdentifier) {
		return set(ICON, iconIdentifier.toString());
	}

	/**
	 * Sets both UI properties {@link ContentProperties#ICON} and
	 * {@link LayoutProperties#ICON_SIZE}.
	 *
	 * @param iconIdentifier The icon identifier ({@link #icon(Object)})
	 * @param size           The relative size of the icon
	 * @return This instance for concatenation
	 */
	public final P icon(Object iconIdentifier, RelativeScale size) {
		return icon(iconIdentifier).iconSize(size);
	}

	/**
	 * Sets both UI properties {@link ContentProperties#ICON} and
	 * {@link LayoutProperties#ICON_ALIGN}. Not all types of {@link Alignment}
	 * may be supported in an UI implementation.
	 *
	 * @param iconIdentifier The icon identifier ({@link #icon(Object)})
	 * @param alignment      The position alignment of the icon
	 * @return This instance for concatenation
	 */
	public final P icon(Object iconIdentifier, Alignment alignment) {
		return icon(iconIdentifier).alignIcon(alignment);
	}

	/**
	 * Sets the property {@link LayoutProperties#ICON_SIZE}.
	 *
	 * @param size The relative size of the icon
	 * @return This instance for concatenation
	 */
	public final P iconSize(RelativeScale size) {
		return set(ICON_SIZE, size);
	}

	/**
	 * Sets the flag property {@link StyleProperties#HAS_IMAGES}.
	 *
	 * @return This instance for concatenation
	 */
	public final P images() {
		return set(HAS_IMAGES);
	}

	/**
	 * Transfers certain properties from the parent fragment to this parameter.
	 *
	 * @param properties The properties to transfer
	 * @return This instance for concatenation
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final P inherit(PropertyName<?>... properties) {
		for (PropertyName property : properties) {
			set(property, fragment.fragmentParam().get(property));
		}

		return (P) this;
	}

	/**
	 * Overridden to be public.
	 *
	 * @see ParameterWrapper#input()
	 */
	@Override
	public P input() {
		return super.input();
	}

	/**
	 * Sets the interactive input mode for this parameter.
	 *
	 * @param inputMode The interactive input mode
	 * @return This instance for concatenation
	 */
	public final P interactive(InteractiveInputMode inputMode) {
		fragment.setInteractive(inputMode, paramType);

		return input();
	}

	/**
	 * Sets a parameter with a list of allowed values to be displayed in a
	 * certain interactive list style.
	 *
	 * @param listStyle The style in which to display the allowed values
	 * @return This instance for concatenation
	 */
	public final P interactive(ListStyle listStyle) {
		fragment.setInteractive(paramType, null, listStyle);

		return input();
	}

	/**
	 * Checks the value of a boolean property.
	 *
	 * @see #get(PropertyName)
	 */
	public final boolean is(PropertyName<Boolean> flagProperty) {
		return get(flagProperty) == Boolean.TRUE;
	}

	/**
	 * Sets the UI property {@link UserInterfaceProperties#LABEL}.
	 *
	 * @param label width The label string
	 * @return This instance for concatenation
	 */
	public final P label(String label) {
		return set(LABEL, label);
	}

	/**
	 * Sets the layout for the panel of a parameter. This will only be valid if
	 * the given parameter is rendered in a panel (like {@link DataElementList}
	 * or buttons).
	 *
	 * @param layout The panel layout
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public P layout(LayoutType layout) {
		set(UserInterfaceProperties.LAYOUT, layout);

		return (P) this;
	}

	/**
	 * Marks this parameter as modified.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P modified() {
		fragment.markParameterAsModified(paramType);

		return (P) this;
	}

	/**
	 * Notifies all listeners that have been registered to listen for parameter
	 * updates with {@link #onChange(EventHandler)}.
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P notifyChangeListeners() {
		value(value());

		return (P) this;
	}

	/**
	 * Sets a simple event handler for action events of this parameter.
	 *
	 * @param eventHandler The event handler to be invoked on an event
	 * @return This instance for concatenation
	 */
	public final P onAction(ValueEventHandler<T> eventHandler) {
		return setParameterEventHandler(InteractionEventType.ACTION,
			eventHandler);
	}

	/**
	 * Registers an event handler that will be notified of changes of this
	 * parameter's relation.
	 *
	 * @param eventHandler The event handler to register
	 * @return This instance for concatenation
	 * @see Relation#addUpdateListener(EventHandler)
	 */
	@SuppressWarnings("unchecked")
	public final P onChange(final EventHandler<RelationEvent<T>> eventHandler) {
		Relation<T> relation = fragment.getParameterRelation(paramType);

		if (relation == null) {
			relation = fragment.setParameter(paramType, null);
		}

		relation.addUpdateListener(eventHandler);

		// cleanup action: remove parameter change listener if step is left
		fragment.addCleanupAction(
			"RemoveChangeListener_" + nextFinishActionId++,
			f -> removeChangeListener(eventHandler));

		return (P) this;
	}

	/**
	 * Sets the event handler for this parameter. The handler will only be
	 * invoked if events have been enabled for this parameter with one of the
	 * corresponding methods.
	 *
	 * @param eventHandler The event handler to register
	 * @return This instance for concatenation
	 * @see #interactive(InteractiveInputMode)
	 * @see #interactive(ListStyle)
	 * @see #continuousEvents()
	 * @see #actionEvents()
	 * @see #allEvents()
	 */
	@SuppressWarnings("unchecked")
	public final P onEvent(InteractionHandler eventHandler) {
		fragment.setParameterInteractionHandler(paramType, eventHandler);

		return (P) this;
	}

	/**
	 * Sets a simple event handler for action events of this parameter.
	 *
	 * @param eventHandler The event handler to be invoked on an event
	 * @return This instance for concatenation
	 */
	public final P onFocusLost(ValueEventHandler<T> eventHandler) {
		return setParameterEventHandler(InteractionEventType.FOCUS_LOST,
			eventHandler);
	}

	/**
	 * Sets an event handler for update events of this parameter.
	 *
	 * @param eventHandler The event handler to be invoked on an event
	 * @return This instance for concatenation
	 */
	public final P onUpdate(ValueEventHandler<T> eventHandler) {
		return setParameterEventHandler(InteractionEventType.UPDATE,
			eventHandler);
	}

	/**
	 * Prepares a download that is associated with an event on this parameter.
	 * This method must be invoked during the handling of the event and the
	 * download will then be executed as the result of the event. After being
	 * processed by the process interaction the generated download URL will be
	 * removed from the parameter.
	 *
	 * @param fileName          The file name of the download
	 * @param fileType          The file type of the download
	 * @param downloadGenerator The function that generates the download data
	 * @throws Exception If the download preparation fails
	 */
	public void prepareDownload(String fileName, FileType fileType,
		Function<FileType, ?> downloadGenerator) {
		initiateDownload(this, fileName, fileType, downloadGenerator);
	}

	/**
	 * Sets the UI properties {@link LayoutProperties#SMALL_COLUMN_SPAN} and
	 * {@link LayoutProperties#MEDIUM_COLUMN_SPAN}.
	 *
	 * @param small  the number of columns to span in small-size layouts
	 * @param medium the number of columns to span in medium-size layouts
	 * @return This instance for concatenation
	 */
	public final P responsiveColSpans(int small, int medium) {
		return set(small, SMALL_COLUMN_SPAN).set(medium, MEDIUM_COLUMN_SPAN);
	}

	/**
	 * Sets the UI property {@link UserInterfaceProperties#ROW_SPAN}.
	 *
	 * @param rows the number of rows to span.
	 * @return This instance for concatenation
	 */
	public final P rowSpan(int rows) {
		return set(rows, ROW_SPAN);
	}

	/**
	 * Sets the UI property {@link UserInterfaceProperties#ROWS}.
	 *
	 * @param rows the number of rows.
	 * @return This instance for concatenation
	 */
	public final P rows(int rows) {
		return set(rows, ROWS);
	}

	/**
	 * Marks this parameter to be displayed in the same row as the previous
	 * parameter (in grid and table layouts).
	 *
	 * @return This instance for concatenation
	 */
	public final P sameRow() {
		return set(SAME_ROW);
	}

	/**
	 * Marks this parameter to be displayed in the same row as the previous
	 * parameter with a certain column span in a grid or table layout.
	 *
	 * @param columnSpan The number of columns that the parameter UI should
	 *                   span
	 * @return This instance for concatenation
	 */
	public final P sameRow(int columnSpan) {
		return sameRow().colSpan(columnSpan);
	}

	/**
	 * Marks this parameter to be displayed in the same row as the previous
	 * parameter with a certain width in a grid layout.
	 *
	 * @param columnWidth The relative width of the parameter UI in a grid
	 *                    layout
	 * @return This instance for concatenation
	 */
	public final P sameRow(RelativeSize columnWidth) {
		return sameRow().width(columnWidth);
	}

	/**
	 * Invokes {@link #width(String)} and {@link #height(String)}.
	 *
	 * @param width  The HTML width string
	 * @param height The HTML height string
	 * @return This instance for concatenation
	 */
	public final P size(String width, String height) {
		return width(width).height(height);
	}

	/**
	 * Queries the UI property {@link UserInterfaceProperties#STYLE}.
	 *
	 * @return The style name (NULL for none)
	 */
	public final String style() {
		return get(STYLE);
	}

	/**
	 * Sets the UI property {@link UserInterfaceProperties#STYLE}.
	 *
	 * @param style The style name(s)
	 * @return This instance for concatenation
	 */
	public final P style(String style) {
		return set(STYLE, style);
	}

	/**
	 * Appends a parameter to the row of this one. This is the same as invoking
	 * the method {@link #sameRow()} on the argument parameter.
	 *
	 * @param parameter The parameter to add to the current row
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public P then(ParameterBase<?, ?> parameter) {
		parameter.sameRow();

		return (P) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("%s(%s)", paramType.getSimpleName(), value());
	}

	/**
	 * Sets the UI property {@link UserInterfaceProperties#TOOLTIP}.
	 *
	 * @param tooltip width The tooltip string
	 * @return This instance for concatenation
	 */
	public final P tooltip(String tooltip) {
		return set(TOOLTIP, tooltip);
	}

	/**
	 * Validates that the value of this parameter fulfills a certain
	 * constraint.
	 * If not an exception will be thrown.
	 *
	 * @param valueConstraint The constraint to be validated
	 * @param errorMessage    The error message to be displayed for the
	 *                        parameter in the case of a constraint violation
	 * @return This instance for concatenation
	 * @throws InvalidParametersException If the constraint is violated
	 */
	public P validate(Predicate<? super T> valueConstraint,
		String errorMessage)
		throws InvalidParametersException {
		return validate(valueConstraint, errorMessage, null);
	}

	/**
	 * Validates that the value of this parameter fulfills a certain
	 * constraint.
	 * If not an exception will be thrown.
	 *
	 * @param valueConstraint The constraint to be validated
	 * @param errorMessage    The error message to be displayed for the
	 *                        parameter in the case of a constraint violation
	 * @param runOnViolation  A runnable to be executed if the constraint is
	 *                        violated
	 * @return This instance for concatenation
	 * @throws InvalidParametersException If the constraint is violated
	 */
	@SuppressWarnings("unchecked")
	public P validate(Predicate<? super T> valueConstraint,
		String errorMessage,
		Runnable runOnViolation) throws InvalidParametersException {
		if (!check(valueConstraint)) {
			if (runOnViolation != null) {
				runOnViolation.run();
			}

			fragment().validationError(
				Collections.singletonMap(type(), errorMessage));

			throw new InvalidParametersException(fragment(), errorMessage,
				type());
		}

		return (P) this;
	}

	/**
	 * Returns the value of the wrapped parameter.
	 *
	 * @see ProcessFragment#getParameter(RelationType)
	 */
	public final T value() {
		return fragment.getParameter(paramType);
	}

	/**
	 * Sets the parameter value.
	 *
	 * @see ProcessFragment#setParameter(RelationType, Object)
	 */
	@SuppressWarnings("unchecked")
	public final P value(T value) {
		fragment.setParameter(paramType, value);

		return (P) this;
	}

	/**
	 * Sets the UI property {@link LayoutProperties#HTML_WIDTH}.
	 *
	 * @param width The HTML width string
	 * @return This instance for concatenation
	 */
	public final P width(String width) {
		return set(HTML_WIDTH, width);
	}

	/**
	 * Sets the UI property {@link LayoutProperties#RELATIVE_WIDTH}.
	 *
	 * @param width The relative width constant
	 * @return This instance for concatenation
	 */
	public final P width(RelativeSize width) {
		return set(RELATIVE_WIDTH, width);
	}

	/**
	 * Sets the pixel width of an element in the UI property
	 * {@link LayoutProperties#WIDTH}.
	 *
	 * @param width The width
	 * @return This instance for concatenation
	 */
	public final P width(int width) {
		return set(width, WIDTH);
	}

	/**
	 * Removes an parameter update listener that had been set with
	 * {@link #onChange(EventHandler)}.
	 *
	 * @param eventHandler The event listener to remove
	 * @return This instance for concatenation
	 * @see #onChange(EventHandler)
	 */
	@SuppressWarnings("unchecked")
	private final P removeChangeListener(
		EventHandler<RelationEvent<T>> eventHandler) {
		Relation<T> relation = fragment.getParameterRelation(paramType);

		if (relation != null) {
			relation
				.get(ListenerTypes.RELATION_UPDATE_LISTENERS)
				.remove(eventHandler);
		}

		return (P) this;
	}
}
