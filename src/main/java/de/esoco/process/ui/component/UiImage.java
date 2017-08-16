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
package de.esoco.process.ui.component;

import de.esoco.lib.property.LabelStyle;

import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiImageDefinition;

import static de.esoco.lib.property.StyleProperties.HAS_IMAGES;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.LABEL_STYLE;


/********************************************************************
 * A component that displays an image.
 *
 * @author eso
 */
public class UiImage extends UiComponent<String, UiImage>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param rImage  The initial image
	 */
	public UiImage(UiContainer<?> rParent, UiImageDefinition<?> rImage)
	{
		super(rParent, String.class);

		setImage(rImage);
		set(HIDE_LABEL);
		set(LABEL_STYLE, LabelStyle.IMAGE);
		set(HAS_IMAGES);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the image to be displayed.
	 *
	 * @param rImage The component's image.
	 */
	public void setImage(UiImageDefinition<?> rImage)
	{
		super.image(rImage);
	}
}
