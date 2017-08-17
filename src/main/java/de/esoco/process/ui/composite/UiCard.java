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

import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.TitleAttribute;

import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.component.UiTitle;
import de.esoco.process.ui.container.UiBuilder;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.graphics.UiIconSupplier;
import de.esoco.process.ui.graphics.UiStandardIcon;
import de.esoco.process.ui.layout.UiFooterLayout;
import de.esoco.process.ui.layout.UiSecondaryContentLayout;


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

	private UiCardTitle   aCardTitle;
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

		aCardTitle = new UiCardTitle(this, sTitle);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a close icon button to the back side of this card. If the back side
	 * has not been added yet it will be by this call. If the back side is
	 * closed the method {@link #handleBackSideClose()} will be invoked.
	 */
	public void addBackSideCloser()
	{
		backSideBuilder().addIconButton(UiStandardIcon.CLOSE.getIcon()
										.alignRight())
						 .onClick(v -> handleBackSideClose());
	}

	/***************************************
	 * Returns a builder for the optional back side of a card. If set the back
	 * side will be displayed when the user clicks on the card icon (which must
	 * therefore be set). Furthermore the back side must contain an interactive
	 * component (typically a button) with a registered action listener so that
	 * it can be closed and the display switched back to the front side. This is
	 * not done by the framework to not impose a fixed layout. But a standard
	 * close button can be added by invoking {@link #addBackSideCloser()}. To
	 * achieve a standard layout this should then be the first call before
	 * adding further back side content.
	 *
	 * @return The back side builder
	 */
	public UiBuilder<?> backSideBuilder()
	{
		if (aBackSide == null)
		{
			aBackSide = builder().addPanel(new UiSecondaryContentLayout());
		}

		return aBackSide.builder();
	}

	/***************************************
	 * Returns the builder for the actions area at the bottom of this card. This
	 * are typically contains buttons that are used to confirm or cancel input
	 * that has been performed in the content are.
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
	 * Sets the icon to be displayed in the upper right corner of the card.
	 *
	 * @param  rIconSupplier A icon supplier that returns the card icon
	 *
	 * @return This instance
	 */
	public UiCard icon(UiIconSupplier rIconSupplier)
	{
		aCardTitle.icon(rIconSupplier);

		return this;
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
	 * Will be invoked if the card's back side has been opened and is closed by
	 * the user through the button added with {@link #addBackSideCloser()}. The
	 * default implementation does nothing.
	 */
	protected void handleBackSideClose()
	{
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * The card title component.
	 *
	 * @author eso
	 */
	public static class UiCardTitle extends UiTitle
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rParent The parent container
		 * @param sText   The title text
		 */
		public UiCardTitle(UiContainer<?> rParent, String sText)
		{
			super(rParent, sText);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Sets the title icon.
		 *
		 * @param  rIconSupplier The title icon
		 *
		 * @return This instance
		 */
		public UiCardTitle icon(UiIconSupplier rIconSupplier)
		{
			return (UiCardTitle) image(rIconSupplier.getIcon().alignRight());
		}
	}

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
