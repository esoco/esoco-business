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

import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.property.UserInterfaceProperties.InteractiveInputMode;
import de.esoco.lib.property.UserInterfaceProperties.ListStyle;

import de.esoco.process.step.InteractionFragment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.UserInterfaceProperties.CSS_STYLES;
import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.HTML_HEIGHT;
import static de.esoco.lib.property.UserInterfaceProperties.HTML_WIDTH;
import static de.esoco.lib.property.UserInterfaceProperties.STYLE;


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
	private final RelationType<T>     rParamType;

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
	 * Marks the parameter to be displayed as interactive buttons. It's list
	 * style will be set to {@link ListStyle#IMMEDIATE}, it will have the flag
	 * {@link UserInterfaceProperties#HIDE_LABEL} set, and for enums {@link
	 * UserInterfaceProperties#COLUMNS} will be set to the number of enum
	 * constants.
	 *
	 * @return This instance for concatenation
	 */
	public Parameter<T> buttons()
	{
		rFragment.setInteractive(rParamType, null, ListStyle.IMMEDIATE);
		rFragment.setUIFlag(HIDE_LABEL, rParamType);

		Class<? super T> rDatatype = rParamType.getTargetType();

		if (rDatatype.isEnum())
		{
			set(rDatatype.getEnumConstants().length,
				UserInterfaceProperties.COLUMNS);
		}

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
			rFragment.setUIProperty(CSS_STYLES, rCssStyles, rParamType);
		}

		rCssStyles.put(sCssProperty, sValue != null ? sValue : "");

		return this;
	}

	/***************************************
	 * Marks the wrapped relation type to be displayed as readonly in the
	 * fragment this parameter belongs to.
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> display()
	{
		rFragment.addDisplayParameters(rParamType);

		return this;
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#HTML_HEIGHT}.
	 *
	 * @param  sHeight The HTML height string
	 *
	 * @return This instance for concatenation
	 */
	public Parameter<T> height(String sHeight)
	{
		return set(HTML_HEIGHT, sHeight);
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
	 * Marks the wrapped relation type to be displayed as editable and
	 * initializes it to generate interaction events according to the given
	 * interactive input mode.
	 *
	 * @param  eMode The interactive input mode
	 *
	 * @return This instance for concatenation
	 */
	public final Parameter<T> input(InteractiveInputMode eMode)
	{
		rFragment.setInteractive(eMode, rParamType);

		return input();
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
	 * Sets the parameter value.
	 *
	 * @see ProcessFragment#setParameter(RelationType, Object)
	 */
	public final Parameter<T> setValue(T rValue)
	{
		rFragment.setParameter(rParamType, rValue);

		return this;
	}

	/***************************************
	 * Sets the UI properties {@link UserInterfaceProperties#HTML_WIDTH} and
	 * {@link UserInterfaceProperties#HTML_HEIGHT}.
	 *
	 * @param  sWidth  The HTML width string
	 * @param  sHeight The HTML height string
	 *
	 * @return This instance for concatenation
	 */
	public Parameter<T> size(String sWidth, String sHeight)
	{
		return width(sWidth).height(sHeight);
	}

	/***************************************
	 * Sets the UI property {@link UserInterfaceProperties#STYLE}.
	 *
	 * @param  sStyle The style name(s)
	 *
	 * @return This instance for concatenation
	 */
	public Parameter<T> style(String sStyle)
	{
		return set(STYLE, sStyle);
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
	 * Sets the UI property {@link UserInterfaceProperties#HTML_WIDTH}.
	 *
	 * @param  sWidth The HTML width string
	 *
	 * @return This instance for concatenation
	 */
	public Parameter<T> width(String sWidth)
	{
		return set(HTML_WIDTH, sWidth);
	}
}
