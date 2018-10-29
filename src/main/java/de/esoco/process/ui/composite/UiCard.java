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
package de.esoco.process.ui.composite;

import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.LayoutType;

import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiImageDefinition;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.component.UiTitle;
import de.esoco.process.ui.container.UiBuilder;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.event.UiHasActionEvents;
import de.esoco.process.ui.graphics.UiIconSupplier;
import de.esoco.process.ui.graphics.UiStandardIcon;
import de.esoco.process.ui.layout.UiContentLayout;
import de.esoco.process.ui.layout.UiFooterLayout;
import de.esoco.process.ui.layout.UiSecondaryContentLayout;

import java.util.function.Consumer;


/********************************************************************
 * A composite that is rendered with a card style, i.e. as a shadowed rectangle
 * with a title and some content. The content should be added through the
 * builder returned from {@link #builder()}. The back side and card action area
 * can be built with {@link #backSideBuilder()} and {@link
 * #cardActionBuilder()}.
 *
 * @author eso
 */
public class UiCard extends UiComposite<UiCard>
{
	//~ Instance fields --------------------------------------------------------

	private UiCardTitle   aCardTitle;
	private UiCardTitle   aBackSideTitle;
	private UiLayoutPanel aBackSide;
	private UiLayoutPanel aCardActions;
	private Runnable	  fBackSideOpenHandler  = () ->{};
	private Runnable	  fBackSideCloseHandler = () ->{};

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance without a title.
	 *
	 * @param rParent The parent container
	 */
	public UiCard(UiContainer<?> rParent)
	{
		this(rParent, (String) null);
	}

	/***************************************
	 * Creates a new instance with a title text but no icon.
	 *
	 * @param rParent The parent container
	 * @param sTitle  The card title
	 */
	public UiCard(UiContainer<?> rParent, String sTitle)
	{
		this(rParent, sTitle, null);
	}

	/***************************************
	 * Creates a new instance with a title image. If a title is set later with
	 * {@link #setTitle(String, UiIconSupplier)} it will be placed in the card's
	 * content area. To place it inside the image use the constructor {@link
	 * #UiCard(UiContainer, UiImageDefinition, String)} instead.
	 *
	 * @param rParent     The parent container
	 * @param rTitleImage The image to display as the card title
	 */
	public UiCard(UiContainer<?> rParent, UiImageDefinition<?> rTitleImage)
	{
		super(rParent, new CardLayout());

		builder().addImage(rTitleImage);

		// explicitly add card content panel so that a subsequently added title
		// is placed in the content
		builder().addPanel(new UiContentLayout());
	}

	/***************************************
	 * Creates a new instance with a title image and text.
	 *
	 * @param rParent     The parent container
	 * @param rTitleImage The image to display as the card title
	 * @param sTitle      The card title to be displayed inside the title image
	 */
	public UiCard(UiContainer<?>	   rParent,
				  UiImageDefinition<?> rTitleImage,
				  String			   sTitle)
	{
		super(rParent, new CardLayout());

		builder().addImage(rTitleImage);
		setTitle(sTitle, null);
	}

	/***************************************
	 * Creates a new instance with a title and an icon.
	 *
	 * @param rParent The parent container
	 * @param sTitle  The card title
	 * @param rIcon   The card icon
	 */
	public UiCard(UiContainer<?> rParent, String sTitle, UiIconSupplier rIcon)
	{
		super(rParent, new CardLayout());

		setTitle(sTitle, rIcon);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns a builder for the optional back side of a card. If set the back
	 * side will be displayed when the user clicks on the card icon (which must
	 * therefore be set). If invoked it also adds a back side title and close
	 * button. Back-side events can be handled by registering listeners with
	 * {@link #onBackSideOpen(Runnable)} and {@link #onBackSideClose(Runnable)}.
	 *
	 * @return The back side builder
	 */
	public UiBuilder<?> backSideBuilder()
	{
		if (aBackSide == null)
		{
			aBackSide = builder().addPanel(new UiSecondaryContentLayout());
		}

		if (aBackSideTitle == null)
		{
			// add empty title and close button
			aBackSideTitle =
				new UiCardTitle(aBackSide, "", UiStandardIcon.CLOSE).onAction(
					v -> fBackSideCloseHandler.run());
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
	 * Returns the card title text.
	 *
	 * @return The title text
	 */
	public String getTitle()
	{
		return aCardTitle.getText();
	}

	/***************************************
	 * The argument function will be invoked when the card's back side has been
	 * closed. This will only have an effect after a back side title has been
	 * set with {@link #setBackSideTitle(String)} because the title contains the
	 * close button. Only one handler function can be active. Invoking this
	 * method again overrides any previous close handler.
	 *
	 * @param  fCloseHandler The back side close handler function
	 *
	 * @return This instance for fluent invocations
	 */
	public UiCard onBackSideClose(Runnable fCloseHandler)
	{
		fBackSideCloseHandler = fCloseHandler;

		return this;
	}

	/***************************************
	 * The argument function will be invoked when the card's back side is
	 * opened. This will only have an effect if a back side has been created
	 * with the {@link #backSideBuilder()} or by setting the back side title
	 * with {@link #setBackSideTitle(String)}. Invoking this method again
	 * overrides any previous open handler.
	 *
	 * @param  fOpenHandler The back side open handler function
	 *
	 * @return This instance for fluent invocations
	 */
	public UiCard onBackSideOpen(Runnable fOpenHandler)
	{
		fBackSideOpenHandler = fOpenHandler;

		return this;
	}

	/***************************************
	 * Sets the back side title with the standard close icon.
	 *
	 * @param  sTitle The new back side title
	 *
	 * @return This instance for fluent invocations
	 */
	public UiCard setBackSideTitle(String sTitle)
	{
		return setBackSideTitle(sTitle, UiStandardIcon.CLOSE);
	}

	/***************************************
	 * Adds the title and icon of the back side of this card. If the back side
	 * has not been added yet it will be by this call. If the user closes the
	 * back side a hander that has been registered by invoking the method {@link
	 * #onBackSideClose(Runnable)} will be invoked.
	 *
	 * <p>The back side icon will also serve to close the back side of the card.
	 * Therefore it should by a symbol like {@link UiStandardIcon#CLOSE} that is
	 * recognizable by the user. That icon is preset when using the method
	 * {@link #setBackSideTitle(String)}.</p>
	 *
	 * @param  sTitle The back side title text
	 * @param  rIcon  The back side title icon (used to close the back side)
	 *
	 * @return This instance for fluent invocations
	 */
	public UiCard setBackSideTitle(String sTitle, UiIconSupplier rIcon)
	{
		if (aBackSideTitle == null)
		{
			// also creates the back side title
			backSideBuilder();
		}

		aBackSideTitle.icon(rIcon).text(sTitle);

		return this;
	}

	/***************************************
	 * Sets or removes the card title. If the title string is NULL the title
	 * will be removed completely and the icon argument will be ignored.
	 *
	 * @param sTitle The card title string or NULL to remove
	 * @param rIcon  The title icon
	 */
	public void setTitle(String sTitle, UiIconSupplier rIcon)
	{
		if (sTitle != null)
		{
			if (aCardTitle == null)
			{
				aCardTitle =
					new UiCardTitle(this, sTitle, rIcon).onAction(
						v -> fBackSideOpenHandler.run());
			}

			aCardTitle.setText(sTitle);
		}
		else if (aCardTitle != null)
		{
			remove(aCardTitle);
		}
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * The card title component.
	 *
	 * @author eso
	 */
	public static class UiCardTitle extends UiTitle
		implements UiHasActionEvents<String, UiCardTitle>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rParent The parent container
		 * @param sText   The title text
		 * @param rIcon   The title icon
		 */
		public UiCardTitle(UiContainer<?> rParent,
						   String		  sText,
						   UiIconSupplier rIcon)
		{
			super(rParent, sText);

			icon(rIcon);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Sets the title icon.
		 *
		 * @param  rIconSupplier The title icon
		 *
		 * @return This instance
		 */
		@Override
		public UiCardTitle icon(UiIconSupplier rIconSupplier)
		{
			return (UiCardTitle) super.icon(rIconSupplier);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public UiCardTitle onAction(Consumer<String> rEventHandler)
		{
			return (UiCardTitle) setParameterEventHandler(
				InteractionEventType.ACTION,
				v -> rEventHandler.accept(v));
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
