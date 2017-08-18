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

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiTextComponent;

import static de.esoco.lib.property.StyleProperties.LABEL_STYLE;


/********************************************************************
 * A read-only UI text label.
 *
 * @author eso
 */
public class UiLabel extends UiTextComponent<UiLabel>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param sText   The label text
	 */
	public UiLabel(UiContainer<?> rParent, String sText)
	{
		super(rParent, sText);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Fluent variant of {@link #setCaption(String)}.
	 *
	 * @param  sCaption The caption label
	 *
	 * @return This instance
	 */
	public UiLabel caption(String sCaption)
	{
		return applyComponentLabel(sCaption);
	}

	/***************************************
	 * Sets a caption label to be displayed over the label text (if supported by
	 * the container layout).
	 *
	 * @param sCaption The caption label
	 */
	public void setCaption(String sCaption)
	{
		caption(sCaption);
	}

	/***************************************
	 * Sets the style of this label.
	 *
	 * @param eStyle The label style
	 */
	public void setLabelStyle(LabelStyle eStyle)
	{
		set(LABEL_STYLE, eStyle);
	}
}
