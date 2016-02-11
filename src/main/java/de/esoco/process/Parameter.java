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

import de.esoco.data.element.DataElementList.ListDisplayMode;

import de.esoco.lib.event.EventHandler;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.property.UserInterfaceProperties.ContentType;
import de.esoco.lib.property.UserInterfaceProperties.InteractiveInputMode;
import de.esoco.lib.property.UserInterfaceProperties.ListStyle;

import de.esoco.process.step.Interaction.InteractionHandler;
import de.esoco.process.step.InteractionFragment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.obrel.core.Relation;
import org.obrel.core.RelationEvent;
import org.obrel.core.RelationType;

import static de.esoco.lib.property.UserInterfaceProperties.COLUMNS;
import static de.esoco.lib.property.UserInterfaceProperties.COLUMN_SPAN;
import static de.esoco.lib.property.UserInterfaceProperties.CONTENT_TYPE;
import static de.esoco.lib.property.UserInterfaceProperties.CSS_STYLES;
import static de.esoco.lib.property.UserInterfaceProperties.DISABLED;
import static de.esoco.lib.property.UserInterfaceProperties.HEIGHT;
import static de.esoco.lib.property.UserInterfaceProperties.HIDDEN;
import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.HTML_HEIGHT;
import static de.esoco.lib.property.UserInterfaceProperties.HTML_WIDTH;
import static de.esoco.lib.property.UserInterfaceProperties.LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.RESOURCE_ID;
import static de.esoco.lib.property.UserInterfaceProperties.ROWS;
import static de.esoco.lib.property.UserInterfaceProperties.ROW_SPAN;
import static de.esoco.lib.property.UserInterfaceProperties.SAME_ROW;
import static de.esoco.lib.property.UserInterfaceProperties.STYLE;
import static de.esoco.lib.property.UserInterfaceProperties.TOOLTIP;
import static de.esoco.lib.property.UserInterfaceProperties.VERTICAL;
import static de.esoco.lib.property.UserInterfaceProperties.WIDTH;


/********************************************************************
 * A class that wraps a process parameter relation types and provides access to
 * typical parameter manipulations through a fluent interface.
 *
 * @author eso
 */
public class Parameter<T>
{
	//~ Instance fields --------------------------------------------------------

	private final InteractionFragment rFragment;

	private final RelationType<T> rParamType;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain fragment and parameter relation
	 * type.
	 *
	 * @param rFragment  The fragment to handle the parameter for
	 * @param rParamType The parameter relation type to handle
	 */
	public Parameter(InteractionFragment rFragment, RelationType<T> rParamType)
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
	public final Parameter<T> actionEvents()
	{
		return interactive(InteractiveInputMode.ACTION);
	}

	/***************************************
	 * Enables all events without setting an event handler. The event handler
	 * must either be set later or the containing fragment must implement {@link
	 * InteractionFragment#handleInteraction(RelationType)}.
	 *
	 * @see #actionEvents(InteractionHandler)
	 */
	public final Parameter<T> allEvents()
	{
		return interactive(InteractiveInputMode.BOTH);
	}

	/***************************************
	 * Sets the allowed values for the parameter.
	 *
	 * @see ProcessFragment#setAllowedValues(RelationType, Object...)
	 */
	@SafeVarargs
	public final Parameter<T> allow(T... rValues)
	{
		rFragment.setAllowedValues(rParamType, rValues);

		return this;
	}

	/***************************************
	 * Sets the allowed values for the parameter.
	 *
	 * @see ProcessFragment#setAllowedValues(RelationType, Object...)
	 */
	public final <C extends Collection<T>> Parameter<T> allow(C rValues)
	{
		rFragment.setAllowedValues(rParamType, rValues);

		return this;
	}

	/***************************************
	 * Marks this parameter to be displayed as interactive buttons. It's list
	 * style will be set to {@link ListStyle#IMMEDIATE}, it will have the flag
	 * {@link UserInterfaceProperties#HIDE_LABEL} set, and the property {@link
	 * UserInterfaceProperties#COLUMNS} will be set to the number of allowed
	 * values. This will also add this parameter as an input parameter to the
	 * fragment.
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> buttons()
	{
		interactive(ListStyle.IMMEDIATE);
		hideLabel();

		set(rFragment.getAllowedValues(rParamType).size(),
			UserInterfaceProperties.COLUMNS);

		return this;
	}

	/***************************************
	 * Clear a certain property flag.
	 *
	 * @see ProcessFragment#clearUIFlag(PropertyName, RelationType...)
	 */
	public final Parameter<T> clear(PropertyName<Boolean> rProperty)
	{
		rFragment.clearUIFlag(rProperty, rParamType);

		return this;
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#COLUMN_SPAN}.
	 *
	 * @param  nColumns the number of columns to span.
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> colSpan(int nColumns)
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
	public final Parameter<T> columns(int nColumns)
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
	public final Parameter<T> content(ContentType eContentType)
	{
		return set(CONTENT_TYPE, eContentType);
	}

	/***************************************
	 * Enables continuous events without setting an event handler. The event
	 * handler must either be set later or the containing fragment must
	 * implement {@link InteractionFragment#handleInteraction(RelationType)}.
	 *
	 * @see #actionEvents(InteractionHandler)
	 */
	public final Parameter<T> continuousEvents()
	{
		return interactive(InteractiveInputMode.CONTINUOUS);
	}

	/***************************************
	 * Sets a CSS style property for the parameter. The style values must be
	 * instances of {@link HasCssName} as defined in the GWT class {@link
	 * Style}.
	 *
	 * @param  sCssProperty The name of the CSS property
	 * @param  sValue       The value of the CSS property or NULL to clear
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> css(String sCssProperty, String sValue)
	{
		Map<String, String> rCssStyles =
			rFragment.getUIProperty(CSS_STYLES, rParamType);

		if (rCssStyles == null)
		{
			rCssStyles = new HashMap<>();
		}

		rCssStyles.put(sCssProperty, sValue != null ? sValue : "");
		set(CSS_STYLES, rCssStyles);

		return this;
	}

	/***************************************
	 * Marks this parameter to be disabled in the user interface.
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> disable()
	{
		return set(DISABLED);
	}

	/***************************************
	 * Marks the wrapped relation type to be displayed as readonly in the
	 * fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	public Parameter<T> display()
	{
		rFragment.addDisplayParameters(rParamType);

		return this;
	}

	/***************************************
	 * Marks this parameter to be disabled in the user interface.
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> enable()
	{
		return clear(DISABLED);
	}

	/***************************************
	 * Returns the fragment this parameter belongs to.
	 *
	 * @return The fragment
	 */
	public final InteractionFragment fragment()
	{
		return rFragment;
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#HTML_HEIGHT} which
	 * defines the table cell height in a panel with a {@link
	 * ListDisplayMode#GRID GRID} layout.
	 *
	 * @param  sHeight The HTML height string
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> gridHeight(String sHeight)
	{
		return set(HTML_HEIGHT, sHeight);
	}

	/***************************************
	 * Sets the UI properties {@link UserInterfaceProperties#HTML_WIDTH} and
	 * {@link UserInterfaceProperties#HTML_HEIGHT} which defines the table cell
	 * size in a panel with a {@link ListDisplayMode#GRID GRID} layout.
	 *
	 * @param  sWidth  The HTML width string
	 * @param  sHeight The HTML height string
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> gridSize(String sWidth, String sHeight)
	{
		return gridWidth(sWidth).gridHeight(sHeight);
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#HTML_WIDTH} which
	 * defines the table cell width in a panel with a {@link
	 * ListDisplayMode#GRID GRID} layout.
	 *
	 * @param  sWidth The HTML width string
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> gridWidth(String sWidth)
	{
		return set(HTML_WIDTH, sWidth);
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#HEIGHT} which defines
	 * the height of the parameter component in panels with a layout that
	 * requires a size value (like {@link ListDisplayMode#DOCK} and {@link
	 * ListDisplayMode#SPLIT}).
	 *
	 * @param  nHeight nWidth The width
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> height(int nHeight)
	{
		return set(nHeight, HEIGHT);
	}

	/***************************************
	 * Marks this parameter to be hidden in the user interface.
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> hide()
	{
		return set(HIDDEN);
	}

	/***************************************
	 * Hides the label of this parameter.
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> hideLabel()
	{
		return set(HIDE_LABEL);
	}

	/***************************************
	 * Marks the wrapped relation type to be displayed as editable in the
	 * fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> input()
	{
		rFragment.addInputParameters(rParamType);

		return this;
	}

	/***************************************
	 * Sets the interactive input mode for this parameter. If an event handler
	 * is given it will be registered with {@link
	 * InteractionFragment#setParameterInteractionHandler(RelationType,
	 * InteractionHandler)}.
	 *
	 * @param  eInputMode The interactive input mode
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> interactive(InteractiveInputMode eInputMode)
	{
		input();
		rFragment.setInteractive(eInputMode, rParamType);

		return this;
	}

	/***************************************
	 * Sets a parameter with a list of allowed values to be displayed in a
	 * certain interactive list style.
	 *
	 * @param  eListStyle The style in which to display the allowed values
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> interactive(ListStyle eListStyle)
	{
		input();
		rFragment.setInteractive(rParamType, null, eListStyle);

		return this;
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#LABEL}.
	 *
	 * @param  sLabel sWidth The label string
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> label(String sLabel)
	{
		return set(LABEL, sLabel);
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
	public final Parameter<T> onChange(
		EventHandler<RelationEvent<T>> rEventHandler)
	{
		Relation<T> rRelation = rFragment.getParameterRelation(rParamType);

		if (rRelation == null)
		{
			rRelation = rFragment.setParameter(rParamType, null);
		}

		rRelation.addUpdateListener(rEventHandler);

		return this;
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
	public final Parameter<T> onEvent(InteractionHandler rEventHandler)
	{
		rFragment.setParameterInteractionHandler(rParamType, rEventHandler);

		return this;
	}

	/***************************************
	 * Sets a certain property flag.
	 *
	 * @see ProcessFragment#setUIFlag(PropertyName, RelationType...)
	 */
	public final Parameter<T> remove(PropertyName<?> rProperty)
	{
		rFragment.removeUIProperties(rParamType);

		return this;
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#RESOURCE_ID}.
	 *
	 * @param  sResourceId sWidth The resource ID string
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> resid(String sResourceId)
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
	public final Parameter<T> rows(int nRows)
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
	public final Parameter<T> rowSpan(int nRows)
	{
		return set(nRows, ROW_SPAN);
	}

	/***************************************
	 * Marks this parameter to be displayed in the same row as the previous
	 * parameter (in table-based layouts).
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> sameRow()
	{
		return set(SAME_ROW);
	}

	/***************************************
	 * Sets a certain property flag.
	 *
	 * @see ProcessFragment#setUIFlag(PropertyName, RelationType...)
	 */
	public final Parameter<T> set(PropertyName<Boolean> rProperty)
	{
		rFragment.setUIFlag(rProperty, rParamType);

		return this;
	}

	/***************************************
	 * Sets a certain property.
	 *
	 * @see ProcessFragment#setUIProperty(PropertyName, Object, RelationType...)
	 */
	public final <P> Parameter<T> set(PropertyName<P> rProperty, P rValue)
	{
		rFragment.setUIProperty(rProperty, rValue, rParamType);

		return this;
	}

	/***************************************
	 * Sets a certain integer property.
	 *
	 * @see ProcessFragment#setUIProperty(int, PropertyName, RelationType...)
	 */
	public final Parameter<T> set(int nValue, PropertyName<Integer> rProperty)
	{
		rFragment.setUIProperty(nValue, rProperty, rParamType);

		return this;
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
	public final Parameter<T> setEnabled(boolean bEnabled)
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
	public final Parameter<T> setVisible(boolean bVisible)
	{
		return bVisible ? show() : hide();
	}

	/***************************************
	 * Marks this parameter to be visible in the user interface.
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> show()
	{
		return clear(HIDDEN);
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#STYLE}.
	 *
	 * @param  sStyle The style name(s)
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> style(String sStyle)
	{
		return set(STYLE, sStyle);
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#TOOLTIP}.
	 *
	 * @param  sTooltip sWidth The tooltip string
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> tooltip(String sTooltip)
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
	 * Returns the parameter value.
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
	public final Parameter<T> value(T rValue)
	{
		rFragment.setParameter(rParamType, rValue);

		return this;
	}

	/***************************************
	 * Marks this parameter to have a vertical orientation (instead if the
	 * horizontal default).
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> vertical()
	{
		return set(VERTICAL);
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#WIDTH} which defines
	 * the width of the parameter component in panels with a layout that
	 * requires a size value (like {@link ListDisplayMode#DOCK} and {@link
	 * ListDisplayMode#SPLIT}).
	 *
	 * @param  nWidth The width
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> width(int nWidth)
	{
		return set(nWidth, WIDTH);
	}
}
