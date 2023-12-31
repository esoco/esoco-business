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
package de.esoco.process.ui.component;

import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.LabelStyle;

import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiImageDefinition;

import static de.esoco.lib.property.StyleProperties.HAS_IMAGES;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.LABEL_STYLE;

/**
 * A component that displays an image and can create click events.
 *
 * @author eso
 */
public class UiImage extends UiComponent<String, UiImage> {

	/**
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param rImage  The initial image
	 */
	public UiImage(UiContainer<?> rParent, UiImageDefinition<?> rImage) {
		super(rParent, String.class);

		setImage(rImage);
		set(HIDE_LABEL);
		set(LABEL_STYLE, LabelStyle.IMAGE);
		set(HAS_IMAGES);

		// set value to empty string (instead of NULL default) to prevent an
		// unnecessary change detection when an empty string is returned from
		// the UI (because text components use no NULL values)
		setValueImpl("");
	}

	/**
	 * Fluent variant of {@link #setCaption(String)}.
	 *
	 * @param sCaption The image caption
	 * @return This instance
	 */
	public UiImage caption(String sCaption) {
		return label(sCaption);
	}

	/**
	 * Returns the image that is displayed by this image component.
	 *
	 * @return The image definition
	 */
	@Override
	public UiImageDefinition<?> getImage() {
		return super.getImage();
	}

	/**
	 * Sets the event handler for click events on images.
	 *
	 * @param rEventHandler The event handler
	 * @return This instance for concatenation
	 */
	public final UiImage onClick(Runnable rEventHandler) {
		return setParameterEventHandler(InteractionEventType.ACTION,
			v -> rEventHandler.run());
	}

	/**
	 * Sets a caption label to be displayed over the image (if supported by the
	 * container layout).
	 *
	 * @param sCaption The caption label or null for none
	 */
	public void setCaption(String sCaption) {
		caption(sCaption);
	}

	/**
	 * Sets the image to be displayed.
	 *
	 * @param rImage The image
	 */
	public void setImage(UiImageDefinition<?> rImage) {
		super.image(rImage);
	}
}
