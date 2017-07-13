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
package de.esoco.process.ui;

import de.esoco.lib.property.StyleProperties;

import de.esoco.process.ui.style.SizeUnit;

import java.util.HashMap;
import java.util.Map;


/********************************************************************
 * Contains properties that define the style of a {@link UiComponent}.
 *
 * @author eso
 */
public class UiStyle extends UiElement<UiStyle>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates an empty instance.
	 */
	public UiStyle()
	{
	}

	/***************************************
	 * Creates a new instance from an existing style.
	 *
	 * @param rStyle The style to copy the properties from
	 */
	public UiStyle(UiStyle rStyle)
	{
		super(rStyle);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the background color.
	 *
	 * @param  sColor The HTML color string
	 *
	 * @return This instance for concatenation
	 */
	public UiStyle backgroundColor(String sColor)
	{
		return css("backgroundColor", sColor);
	}

	/***************************************
	 * Sets the component border.
	 *
	 * @param  sBorder The HTML border definition string
	 *
	 * @return This instance for concatenation
	 */
	public UiStyle border(String sBorder)
	{
		return css("border", sBorder);
	}

	/***************************************
	 * Sets a CSS style property for the parameter. The names of multi-word CSS
	 * properties must be given in CamelCase form without hyphens, starting with
	 * a lower case letter (e.g. 'font-size' must be set as 'fontSize').
	 *
	 * @param  sCssProperty The name of the CSS property
	 * @param  sValue       The value of the CSS property or NULL to clear
	 *
	 * @return This instance for concatenation
	 */
	public final UiStyle css(String sCssProperty, String sValue)
	{
		Map<String, String> rCssStyles = get(StyleProperties.CSS_STYLES, null);

		if (rCssStyles == null)
		{
			rCssStyles = new HashMap<>();
		}

		rCssStyles.put(sCssProperty, sValue != null ? sValue : "");
		set(StyleProperties.CSS_STYLES, rCssStyles);

		return this;
	}

	/***************************************
	 * Sets the font size.
	 *
	 * @param  nSize The size value
	 * @param  eUnit The size unit
	 *
	 * @return This instance for concatenation
	 */
	public UiStyle fontSize(int nSize, SizeUnit eUnit)
	{
		return css("fontSize", eUnit.getHtmlSize(nSize));
	}

	/***************************************
	 * Sets the font weight.
	 *
	 * @param  sWeight The font weight HTML value
	 *
	 * @return This instance for concatenation
	 */
	public UiStyle fontWeight(String sWeight)
	{
		return css("fontWeight", sWeight);
	}

	/***************************************
	 * Sets the foreground color.
	 *
	 * @param  sColor The HTML color string
	 *
	 * @return This instance for concatenation
	 */
	public UiStyle foregroundColor(String sColor)
	{
		return css("color", sColor);
	}

	/***************************************
	 * Sets the component margin.
	 *
	 * @param  sMargin sPadding The HTML margin definition string
	 *
	 * @return This instance for concatenation
	 */
	public UiStyle margin(String sMargin)
	{
		return css("margin", sMargin);
	}

	/***************************************
	 * Sets the component padding.
	 *
	 * @param  sPadding The HTML padding definition string
	 *
	 * @return This instance for concatenation
	 */
	public UiStyle padding(String sPadding)
	{
		return css("padding", sPadding);
	}

	/***************************************
	 * Sets the style name(s) of a component.
	 *
	 * @param  sStyleName The style name(s)
	 *
	 * @return This instance for concatenation
	 */
	public final UiStyle styleName(String sStyleName)
	{
		return set(StyleProperties.STYLE, sStyleName);
	}
}
