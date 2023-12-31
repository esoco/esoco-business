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
package de.esoco.process.ui.app;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.Orientation;

import de.esoco.process.ui.UiBuilder;
import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.component.UiPushButtons;
import de.esoco.process.ui.component.UiTextField;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.layout.UiSecondaryContentLayout;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

import static de.esoco.lib.property.ContentProperties.PLACEHOLDER;
import static de.esoco.lib.property.LayoutProperties.FLOAT;
import static de.esoco.lib.property.LayoutProperties.LAYOUT;
import static de.esoco.lib.property.StateProperties.EVENT_HANDLING_DELAY;
import static de.esoco.lib.property.StyleProperties.ORIENTATION;

/**
 * A composite that contains a navigation menu for an application.
 *
 * @author eso
 */
public abstract class UiNavMenu<T> extends UiComposite<UiNavMenu<T>> {

	/**
	 * Enumeration of the different menu types.
	 */
	public enum NavMenuType {TOP, SIDE}

	private Collection<T> rMenuItems;

	private UiPushButtons<?> aMenuLinks;

	private UiTextField aSearchField;

	private Consumer<T> fMenuSelectionHandler;

	/**
	 * Creates a new instance without preset menu items.
	 *
	 * @param rParent The parent container
	 */
	protected UiNavMenu(UiContainer<?> rParent) {
		this(rParent, null);
	}

	/**
	 * Creates a new instance with preset menu items.
	 *
	 * @param rParent    The parent container
	 * @param rMenuItems The initial menu items
	 */
	protected UiNavMenu(UiContainer<?> rParent, Collection<T> rMenuItems) {
		super(rParent, new UiLayout(LayoutType.MENU));

		setMenuItems(rMenuItems);
	}

	/**
	 * Returns the menu items of this instance.
	 *
	 * @return The collection of menu item enum values
	 */
	public final Collection<T> getMenuItems() {
		return rMenuItems;
	}

	/**
	 * Returns the component containing the menu link buttons.
	 *
	 * @return The menu links parameter
	 */
	public final UiPushButtons<?> getMenuLinks() {
		return aMenuLinks;
	}

	/**
	 * Sets the handler for menu actions. This will override any previously set
	 * menu selection handler.
	 *
	 * @param fHandler The handler to be invoked on menu selection
	 */
	public void onMenuSelection(Consumer<T> fHandler) {
		fMenuSelectionHandler = fHandler;
	}

	/**
	 * Marks a certain menu item as active.
	 *
	 * @param rMenuItem The new active item or NULL for none
	 */
	@SuppressWarnings("unchecked")
	public void setActive(T rMenuItem) {
		if (aMenuLinks != null) {
			rMenuItem = rMenuItems.contains(rMenuItem) ? rMenuItem : null;
			((UiPushButtons<Object>) aMenuLinks).select(
				rMenuItem instanceof Enum ?
				rMenuItem :
				rMenuItem != null ? rMenuItem.toString() : null);
		}
	}

	/**
	 * Sets the menu items to be displayed.
	 *
	 * @param rMenuItems The new menu items (NULL or empty for none)
	 */
	public void setMenuItems(Collection<T> rMenuItems) {
		this.rMenuItems =
			rMenuItems != null ? rMenuItems : Collections.emptyList();
	}

	/**
	 * Sets the text to be displayed in the search field (if such exists) or
	 * hides the field if the text is NULL.
	 *
	 * @param sText The search text to show (NULL to hide the search field)
	 */
	public void setSearchText(String sText) {
		if (aSearchField != null) {
			if (sText != null) {
				aSearchField.setText(sText);
				aSearchField.show();
			} else {
				aSearchField.hide();
			}
		}
	}

	/**
	 * Adds components to this menu that should appear before the the menu
	 * items
	 * which are added later with {@link #addMenuItems(UiBuilder)}. Can be
	 * overridden by subclasses to add additional components like a logo image
	 * and/or a search field.
	 *
	 * @param rBuilder The builder to add the menu components with
	 */
	protected void addMenuComponents(UiBuilder<?> rBuilder) {
	}

	/**
	 * Adds the menu item components of this menu. May be overridden by
	 * subclasses to modify or extend the menu presentation. The default
	 * implementation adds menu buttons for each menu item value.
	 *
	 * @param rBuilder The builder to add the menu items with
	 */
	@SuppressWarnings("unchecked")
	protected <E extends Enum<E>> void addMenuItems(UiBuilder<?> rBuilder) {
		if (!rMenuItems.isEmpty()) {
			T rFirstItem = CollectionUtil.firstElementOf(rMenuItems);

			if (rFirstItem instanceof Enum) {
				aMenuLinks = createEnumMenuLinks(rBuilder, rFirstItem);
			} else {
				UiPushButtons<String> aLinks = rBuilder.addPushButtons();

				aLinks
					.setButtons(rMenuItems.stream().map(i -> i.toString()))
					.select(rFirstItem.toString());
				aMenuLinks = aLinks;
			}

			aMenuLinks
				.withImages()
				.buttonStyle(ButtonStyle.LINK)
				.onAction(this::handleItemSelection);

			if (get(ORIENTATION) == Orientation.VERTICAL) {
				aMenuLinks.set(LAYOUT, LayoutType.INLINE);
			}
		}
	}

	/**
	 * Adds a search input field with the given builder (which should be a
	 * builder from this menu).
	 *
	 * @param rBuilder       The builder to add the field with
	 * @param sPlaceholder   The text to be displayed if the field is empty
	 * @param fSearchHandler fHandler The handler to be invoked with the input
	 *                       value if a search is to be performed
	 * @return The search input field component
	 */
	@SuppressWarnings("boxing")
	protected UiTextField addSearchField(UiBuilder<?> rBuilder,
		String sPlaceholder, Consumer<String> fSearchHandler) {
		aSearchField = rBuilder
			.addTextField("")
			.set(PLACEHOLDER, sPlaceholder)
			.set(EVENT_HANDLING_DELAY, 1200)
			.onUpdate(fSearchHandler)
			.onAction(fSearchHandler);

		return aSearchField;
	}

	/**
	 * Builds this menu by first invoking {@link #addMenuComponents(UiBuilder)}
	 * and then adds the menu items if such are available.
	 *
	 * @see UiComposite#buildContent(UiBuilder)
	 */
	@Override
	protected void buildContent(UiBuilder<?> rBuilder) {
		addMenuComponents(rBuilder);

		if (!rMenuItems.isEmpty()) {
			UiLayoutPanel aItemPanel =
				rBuilder.addPanel(new UiSecondaryContentLayout());

			aItemPanel.set(FLOAT, Alignment.END);
			addMenuItems(aItemPanel.builder());
		}
	}

	/**
	 * Handles the selection of a menu item by forwarding it to an event
	 * handler
	 * registered through {@link #onMenuSelection(Consumer)}.
	 *
	 * @param rItem The selected menu item
	 */
	@SuppressWarnings("unchecked")
	protected void handleItemSelection(Object rItem) {
		if (fMenuSelectionHandler != null) {
			fMenuSelectionHandler.accept((T) rItem);
		}
	}

	/**
	 * Generically typed helper method to create the menu links component
	 * for an
	 * enum datatype.
	 *
	 * @param rBuilder   The container builder
	 * @param rFirstItem The first menu item
	 * @return The menu links component
	 */
	@SuppressWarnings("unchecked")
	private <E extends Enum<E>> UiPushButtons<E> createEnumMenuLinks(
		UiBuilder<?> rBuilder, T rFirstItem) {
		return rBuilder
			.addPushButtons((Class<E>) rFirstItem.getClass())
			.select((E) rFirstItem);
	}
}
