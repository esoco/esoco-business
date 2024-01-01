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
package de.esoco.process.ui.style;

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.Color;
import de.esoco.lib.property.HasCssName;
import de.esoco.lib.property.StyleProperties;
import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiElement;

import java.util.HashMap;
import java.util.Map;

import static de.esoco.lib.property.StyleProperties.STYLE;

/**
 * Contains properties that define the style of a {@link UiComponent}.
 *
 * @author eso
 */
public class UiStyle extends UiElement<UiStyle> {

	private String defaultStyleName;

	/**
	 * Creates an empty instance.
	 */
	public UiStyle() {
	}

	/**
	 * Creates a new instance from an existing style.
	 *
	 * @param style The style to copy the properties from
	 */
	public UiStyle(UiStyle style) {
		super(style);
	}

	/**
	 * Adds an additional style name to an existing style name and returns the
	 * corresponding string.
	 *
	 * @param styleName      The existing style name (NULL or empty for none)
	 * @param additionalName The style name to add
	 * @return The resulting style name (may be empty but will never be NULL)
	 */
	public static String addStyleName(String styleName,
		String additionalName) {
		if (styleName != null && styleName.length() > 0) {
			if (additionalName.length() > 0) {
				styleName += " " + additionalName;
			}
		} else {
			styleName = additionalName;
		}

		return styleName;
	}

	/**
	 * Adds a style name to the current style name.
	 *
	 * @param additionalName The style name to add
	 * @return This instance for fluent invocation
	 */
	public final UiStyle addStyleName(String additionalName) {
		return styleName(addStyleName(get(STYLE, ""), additionalName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applyPropertiesTo(UiComponent<?, ?> component) {
		super.applyPropertiesTo(component);

		String fullStyle = addStyleName(defaultStyleName, get(STYLE, ""));

		if (fullStyle.length() > 0) {
			component.set(STYLE, fullStyle);
		}
	}

	/**
	 * Sets the background color.
	 *
	 * @param color The HTML color string
	 * @return This instance for fluent invocation
	 */
	public final UiStyle backgroundColor(Color color) {
		return css("backgroundColor", color.toHtml());
	}

	/**
	 * Sets the component border.
	 *
	 * @param border The HTML border definition string
	 * @return This instance for fluent invocation
	 */
	public final UiStyle border(String border) {
		return css("border", border);
	}

	/**
	 * Sets a CSS style property for the parameter. The names of multi-word CSS
	 * properties must be given in CamelCase form without hyphens, starting
	 * with
	 * a lower case letter (e.g. 'font-size' must be set as 'fontSize').
	 *
	 * <p>This method is optimized to ignore NULL values if the property
	 * doesn't exist already. Therefore invoking code doesn't need to perform
	 * null checks to prevent adding empty CSS values (e.g. from
	 * NULL-initialized variables).</p>
	 *
	 * @param propertyName The name of the CSS property
	 * @param value        The value of the CSS property or NULL to clear
	 * @return This instance for fluent invocation
	 */
	public final UiStyle css(String propertyName, String value) {
		Map<String, String> cssStyles = get(StyleProperties.CSS_STYLES, null);

		if (cssStyles != null) {
			// prevent adding unnecessary empty values
			if (value == null && !cssStyles.containsKey(propertyName)) {
				return this;
			}
		} else {
			cssStyles = new HashMap<>();
		}

		// if value is NULL (= clear property) put an empty string instead
		// of removing or else existing values will not be overwritten on
		// the client side
		cssStyles.put(propertyName, value != null ? value : "");
		set(StyleProperties.CSS_STYLES, cssStyles);

		return this;
	}

	/**
	 * Applies a CSS property.
	 *
	 * @param propertyName The name of the CSS property to apply
	 * @param value        The property value to apply or NULL to clear
	 * @return This instance for fluent invocation
	 */
	public final UiStyle css(String propertyName, HasCssName value) {
		return css(propertyName, value != null ? value.getCssName() : null);
	}

	/**
	 * Sets the default style name that should always be applied. The style
	 * name
	 * can consist of multiple words that are separated by spaces.
	 *
	 * @param styleName The new default style name
	 * @return This instance for fluent invocation
	 */
	public final UiStyle defaultStyleName(String styleName) {
		defaultStyleName = styleName;

		return this;
	}

	/**
	 * Sets the font size.
	 *
	 * @param size The size value
	 * @param unit The size unit
	 * @return This instance for fluent invocation
	 */
	public final UiStyle fontSize(int size, SizeUnit unit) {
		return css("fontSize", unit.getHtmlSize(size));
	}

	/**
	 * Sets the font weight.
	 *
	 * @param weight The font weight HTML value
	 * @return This instance for fluent invocation
	 */
	public final UiStyle fontWeight(String weight) {
		return css("fontWeight", weight);
	}

	/**
	 * Sets the foreground color.
	 *
	 * @param color The HTML color string
	 * @return This instance for fluent invocation
	 */
	public UiStyle foregroundColor(Color color) {
		return css("color", color.toHtml());
	}

	/**
	 * Returns the style name(s) of a component.
	 *
	 * @return The style name (empty string for none)
	 */
	public final String getStyleName() {
		String style = get(STYLE, "");

		return style != null ? style : "";
	}

	/**
	 * Sets the height of a text line in the component.
	 *
	 * @param height The height value
	 * @param unit   The height unit
	 * @return This instance for fluent invocation
	 */
	public final UiStyle lineHeight(int height, SizeUnit unit) {
		return css("lineHeight", unit.getHtmlSize(height));
	}

	/**
	 * Sets the component margin.
	 *
	 * @param margin padding The HTML margin definition string
	 * @return This instance for fluent invocation
	 */
	public final UiStyle margin(String margin) {
		return css("margin", margin);
	}

	/**
	 * Sets the component padding.
	 *
	 * @param padding The HTML padding definition string
	 * @return This instance for fluent invocation
	 */
	public final UiStyle padding(String padding) {
		return css("padding", padding);
	}

	/**
	 * Sets the style name. The style name can consist of multiple words that
	 * are separated by spaces.
	 *
	 * @param styleName The style name
	 * @return This instance for fluent invocation
	 */
	public final UiStyle styleName(String styleName) {
		return set(STYLE, styleName);
	}

	/**
	 * Sets the horizontal alignment of text in the component.
	 *
	 * @param textAlignment The horizontal text alignment
	 * @return This instance for fluent invocation
	 */
	public final UiStyle textAlign(Alignment textAlignment) {
		return css("textAlign", mapTextAlignment(textAlignment));
	}

	/**
	 * Maps an alignment value to the corresponding CSS text alignment value.
	 *
	 * @param alignment The alignment to map
	 * @return The CSS text alignment value
	 */
	private String mapTextAlignment(Alignment alignment) {
		switch (alignment) {
			case BEGIN:
				return "start";

			case CENTER:
				return "center";

			case END:
				return "end";

			case FILL:
				return "justify";

			default:
				throw new AssertionError("Undefined: " + alignment);
		}
	}
}
