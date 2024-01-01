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
import de.esoco.process.ui.UiBuilder;
import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiImageDefinition;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.component.UiTitle;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.event.UiHasActionEvents;
import de.esoco.process.ui.graphics.UiIconSupplier;
import de.esoco.process.ui.graphics.UiStandardIcon;
import de.esoco.process.ui.layout.UiContentLayout;
import de.esoco.process.ui.layout.UiFooterLayout;
import de.esoco.process.ui.layout.UiSecondaryContentLayout;

import java.util.function.Consumer;

/**
 * A composite that is rendered with a card style, i.e. as a shadowed rectangle
 * with a title and some content. The content should be added through the
 * builder returned from {@link #builder()}. The back side and card action area
 * can be built with {@link #backSideBuilder()} and
 * {@link #cardActionBuilder()}.
 *
 * @author eso
 */
public class UiCard extends UiComposite<UiCard> {

	private UiCardTitle cardTitle;

	private UiCardTitle backSideTitle;

	private UiLayoutPanel backSide;

	private UiLayoutPanel cardActions;

	private Runnable backSideOpenHandler = () -> {
	};

	private Runnable backSideCloseHandler = () -> {
	};

	/**
	 * Creates a new instance without a title.
	 *
	 * @param parent The parent container
	 */
	public UiCard(UiContainer<?> parent) {
		this(parent, (String) null);
	}

	/**
	 * Creates a new instance with a title text but no icon.
	 *
	 * @param parent The parent container
	 * @param title  The card title
	 */
	public UiCard(UiContainer<?> parent, String title) {
		this(parent, title, null);
	}

	/**
	 * Creates a new instance with a title image. If a title is set later with
	 * {@link #setTitle(String, UiIconSupplier)} it will be placed in the
	 * card's
	 * content area. To place it inside the image use the constructor
	 * {@link #UiCard(UiContainer, UiImageDefinition, String)} instead.
	 *
	 * @param parent     The parent container
	 * @param titleImage The image to display as the card title
	 */
	public UiCard(UiContainer<?> parent, UiImageDefinition<?> titleImage) {
		super(parent, new CardLayout());

		builder().addImage(titleImage);

		// explicitly add card content panel so that a subsequently added title
		// is placed in the content
		builder().addPanel(new UiContentLayout());
	}

	/**
	 * Creates a new instance with a title image and text.
	 *
	 * @param parent     The parent container
	 * @param titleImage The image to display as the card title
	 * @param title      The card title to be displayed inside the title image
	 */
	public UiCard(UiContainer<?> parent, UiImageDefinition<?> titleImage,
		String title) {
		super(parent, new CardLayout());

		builder().addImage(titleImage);
		setTitle(title, null);
	}

	/**
	 * Creates a new instance with a title and an icon.
	 *
	 * @param parent The parent container
	 * @param title  The card title
	 * @param icon   The card icon
	 */
	public UiCard(UiContainer<?> parent, String title, UiIconSupplier icon) {
		super(parent, new CardLayout());

		setTitle(title, icon);
	}

	/**
	 * Returns a builder for the optional back side of a card. If set the back
	 * side will be displayed when the user clicks on the card icon (which must
	 * therefore be set). If invoked it also adds a back side title and close
	 * button. Back-side events can be handled by registering listeners with
	 * {@link #onBackSideOpen(Runnable)} and
	 * {@link #onBackSideClose(Runnable)}.
	 *
	 * @return The back side builder
	 */
	public UiBuilder<?> backSideBuilder() {
		if (backSide == null) {
			backSide = builder().addPanel(new UiSecondaryContentLayout());
		}

		if (backSideTitle == null) {
			// add empty title and close button
			backSideTitle =
				new UiCardTitle(backSide, "", UiStandardIcon.CLOSE).onAction(
					v -> backSideCloseHandler.run());
		}

		return backSide.builder();
	}

	/**
	 * Returns the builder for the actions area at the bottom of this card.
	 * This
	 * are typically contains buttons that are used to confirm or cancel input
	 * that has been performed in the content are.
	 *
	 * @return The panel content builder
	 */
	public UiBuilder<?> cardActionBuilder() {
		if (cardActions == null) {
			cardActions = builder().addPanel(new UiFooterLayout());
		}

		return cardActions.builder();
	}

	/**
	 * Returns the card title text.
	 *
	 * @return The title text
	 */
	public String getTitle() {
		return cardTitle.getText();
	}

	/**
	 * The argument function will be invoked when the card's back side has been
	 * closed. This will only have an effect after a back side title has been
	 * set with {@link #setBackSideTitle(String)} because the title contains
	 * the
	 * close button. Only one handler function can be active. Invoking this
	 * method again overrides any previous close handler.
	 *
	 * @param closeHandler The back side close handler function
	 * @return This instance for fluent invocations
	 */
	public UiCard onBackSideClose(Runnable closeHandler) {
		backSideCloseHandler = closeHandler;

		return this;
	}

	/**
	 * The argument function will be invoked when the card's back side is
	 * opened. This will only have an effect if a back side has been created
	 * with the {@link #backSideBuilder()} or by setting the back side title
	 * with {@link #setBackSideTitle(String)}. Invoking this method again
	 * overrides any previous open handler.
	 *
	 * @param openHandler The back side open handler function
	 * @return This instance for fluent invocations
	 */
	public UiCard onBackSideOpen(Runnable openHandler) {
		backSideOpenHandler = openHandler;

		return this;
	}

	/**
	 * Sets the back side title with the standard close icon.
	 *
	 * @param title The new back side title
	 * @return This instance for fluent invocations
	 */
	public UiCard setBackSideTitle(String title) {
		return setBackSideTitle(title, UiStandardIcon.CLOSE);
	}

	/**
	 * Adds the title and icon of the back side of this card. If the back side
	 * has not been added yet it will be by this call. If the user closes the
	 * back side a hander that has been registered by invoking the method
	 * {@link #onBackSideClose(Runnable)} will be invoked.
	 *
	 * <p>The back side icon will also serve to close the back side of the
	 * card. Therefore it should by a symbol like {@link UiStandardIcon#CLOSE}
	 * that is recognizable by the user. That icon is preset when using the
	 * method {@link #setBackSideTitle(String)}.</p>
	 *
	 * @param title The back side title text
	 * @param icon  The back side title icon (used to close the back side)
	 * @return This instance for fluent invocations
	 */
	public UiCard setBackSideTitle(String title, UiIconSupplier icon) {
		if (backSideTitle == null) {
			// also creates the back side title
			backSideBuilder();
		}

		backSideTitle.icon(icon).text(title);

		return this;
	}

	/**
	 * Sets or removes the card title. If the title string is NULL the title
	 * will be removed completely and the icon argument will be ignored.
	 *
	 * @param title The card title string or NULL to remove
	 * @param icon  The title icon
	 */
	public void setTitle(String title, UiIconSupplier icon) {
		if (title != null) {
			if (cardTitle == null) {
				cardTitle = new UiCardTitle(this, title, icon).onAction(
					v -> backSideOpenHandler.run());
			}

			cardTitle.setText(title);
		} else if (cardTitle != null) {
			remove(cardTitle);
		}
	}

	/**
	 * The card title component.
	 *
	 * @author eso
	 */
	public static class UiCardTitle extends UiTitle
		implements UiHasActionEvents<String, UiCardTitle> {

		/**
		 * Creates a new instance.
		 *
		 * @param parent The parent container
		 * @param text   The title text
		 * @param icon   The title icon
		 */
		public UiCardTitle(UiContainer<?> parent, String text,
			UiIconSupplier icon) {
			super(parent, text);

			icon(icon);
		}

		/**
		 * Sets the title icon.
		 *
		 * @param iconSupplier The title icon
		 * @return This instance
		 */
		@Override
		public UiCardTitle icon(UiIconSupplier iconSupplier) {
			return (UiCardTitle) super.icon(iconSupplier);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public UiCardTitle onAction(Consumer<String> eventHandler) {
			return (UiCardTitle) setParameterEventHandler(
				InteractionEventType.ACTION, v -> eventHandler.accept(v));
		}
	}

	/**
	 * The layout for UI cards.
	 *
	 * @author eso
	 */
	static class CardLayout extends UiLayout {

		/**
		 * Creates a new instance.
		 */
		public CardLayout() {
			super(LayoutType.CARD);
		}
	}
}
