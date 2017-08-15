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
package de.esoco.process.ui.composite;

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.TitleAttribute;

import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.component.UiTitle;
import de.esoco.process.ui.container.UiBuilder;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.layout.UiFooterLayout;
import de.esoco.process.ui.layout.UiSecondaryContentLayout;

import static de.esoco.lib.property.ContentProperties.ICON;
import static de.esoco.lib.property.LayoutProperties.ICON_ALIGN;
import static de.esoco.lib.property.StyleProperties.BUTTON_STYLE;


/********************************************************************
 * A composite that is rendered with a card style, i.e. a rectangle with a title
 * and some content. Optionally the "back side" of the card can also contain a
 * component, e.g. to display help or options for the main card content.
 *
 * @author eso
 */
public class UiCard extends UiComposite<UiCard> implements TitleAttribute
{
	//~ Instance fields --------------------------------------------------------

	private UiTitle		  aCardTitle;
	private UiLayoutPanel aBackSide;
	private UiLayoutPanel aCardActions;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param sTitle  The card title
	 */
	public UiCard(UiContainer<?> rParent, String sTitle)
	{
		super(rParent, new CardLayout());

		aCardTitle =
			new UiTitle(this, sTitle).set(ICON, "HELP")
									 .set(ICON_ALIGN, Alignment.END);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns a builder for the optional back side of a card. If set the back
	 * side will be displayed when the user clicks on the card title icon.
	 *
	 * @return The back side builder
	 */
	public UiBuilder<?> backSideBuilder()
	{
		if (aBackSide == null)
		{
			aBackSide = builder().addPanel(new UiSecondaryContentLayout());

			aBackSide.builder()
					 .addButton("")
					 .set(ICON, "CLOSE")
					 .set(BUTTON_STYLE, ButtonStyle.ICON)
					 .set(ICON_ALIGN, Alignment.END)
					 .onClick(v -> handleBackSideClose());
		}

		return aBackSide.builder();
	}

	/***************************************
	 * Returns the builder for the card actions.
	 *
	 * @return The panel content builder
	 */
	public UiBuilder<?> cardActionBuilder()
	{
		if (aCardActions == null)
		{
			aCardActions = builder().addPanel(new UiFooterLayout());
		}

		return aCardActions.builder();
	}

	/***************************************
	 * Returns the builder for the panel content.
	 *
	 * @return The panel content builder
	 */
	public UiBuilder<?> contentBuilder()
	{
		return builder();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle()
	{
		return aCardTitle.getText();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void setTitle(String sTitle)
	{
		aCardTitle.setText(sTitle);
	}

	/***************************************
	 * Will be invoked if the card back side has been displayed and is closed by
	 * the user. The default implementation does nothing.
	 */
	protected void handleBackSideClose()
	{
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * The layout for UI cards.
	 *
	 * @author eso
	 */
	static class CardLayout extends UiLayout
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 */
		public CardLayout()
		{
			super(LayoutType.CARD);
		}
	}
}
