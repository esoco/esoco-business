//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.process.ui.component;

import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.IntAttribute;
import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiContainer;

import static de.esoco.lib.property.ContentProperties.CONTENT_TYPE;
import static org.obrel.type.StandardTypes.MAXIMUM;
import static org.obrel.type.StandardTypes.MINIMUM;

/**
 * A component that shows the progress of an operation. If no progress
 * parameters are set it may be rendered as an infinite animation to show an
 * indeterminable progress. But that also depends on the UI toolkit that is used
 * on the client side.
 *
 * @author eso
 */
public class UiProgressBar extends UiComponent<Integer, UiProgressBar>
	implements IntAttribute {

	/**
	 * Creates a new instance that without preset values. Depending on the UI
	 * toolkit it may be rendered as an infinite animation to show an
	 * indeterminable progress.
	 *
	 * @param parent The parent container
	 * @see #withBounds(int, int)
	 */
	public UiProgressBar(UiContainer<?> parent) {
		super(parent, Integer.class);

		set(CONTENT_TYPE, ContentType.PROGRESS);
	}

	/**
	 * Fluent variant of {@link #setCaption(String)}.
	 *
	 * @param caption The caption label
	 * @return This instance
	 */
	public UiProgressBar caption(String caption) {
		return label(caption);
	}

	/**
	 * Returns the maximum value of the progress.
	 *
	 * @return The maximum value
	 */
	public int getMaximum() {
		return fragment().getParameterRelation(type()).getAnnotation(MAXIMUM);
	}

	/**
	 * Returns the minimum value of the progress.
	 *
	 * @return The minimum value
	 */
	public int getMinimum() {
		return fragment().getParameterRelation(type()).getAnnotation(MINIMUM);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getValue() {
		return getValueImpl().intValue();
	}

	/**
	 * Sets a caption label to be displayed over the label text (if
	 * supported by
	 * the container layout).
	 *
	 * @param caption The caption label
	 */
	public void setCaption(String caption) {
		caption(caption);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(int value) {
		value(value);
	}

	/**
	 * Sets the value of this instance.
	 *
	 * @param value The value
	 * @return This instance
	 */
	public UiProgressBar value(int value) {
		return setValueImpl(value);
	}

	/**
	 * Sets the minimum and maximum values for the progress value. If not set
	 * these values are initialized to 0 and 100, respectively.
	 *
	 * @param minimum The minimum integer value
	 * @param maximum The maximum integer value
	 * @return This instance
	 */
	public UiProgressBar withBounds(int minimum, int maximum) {
		fragment().setParameterBounds(type(), minimum, maximum);

		return this;
	}
}
