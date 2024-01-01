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

	private Collection<T> menuItems;

	private UiPushButtons<?> menuLinks;

	private UiTextField searchField;

	private Consumer<T> menuSelectionHandler;

	/**
	 * Creates a new instance without preset menu items.
	 *
	 * @param parent The parent container
	 */
	protected UiNavMenu(UiContainer<?> parent) {
		this(parent, null);
	}

	/**
	 * Creates a new instance with preset menu items.
	 *
	 * @param parent    The parent container
	 * @param menuItems The initial menu items
	 */
	protected UiNavMenu(UiContainer<?> parent, Collection<T> menuItems) {
		super(parent, new UiLayout(LayoutType.MENU));

		setMenuItems(menuItems);
	}

	/**
	 * Returns the menu items of this instance.
	 *
	 * @return The collection of menu item enum values
	 */
	public final Collection<T> getMenuItems() {
		return menuItems;
	}

	/**
	 * Returns the component containing the menu link buttons.
	 *
	 * @return The menu links parameter
	 */
	public final UiPushButtons<?> getMenuLinks() {
		return menuLinks;
	}

	/**
	 * Sets the handler for menu actions. This will override any previously set
	 * menu selection handler.
	 *
	 * @param handler The handler to be invoked on menu selection
	 */
	public void onMenuSelection(Consumer<T> handler) {
		menuSelectionHandler = handler;
	}

	/**
	 * Marks a certain menu item as active.
	 *
	 * @param menuItem The new active item or NULL for none
	 */
	@SuppressWarnings("unchecked")
	public void setActive(T menuItem) {
		if (menuLinks != null) {
			menuItem = menuItems.contains(menuItem) ? menuItem : null;
			((UiPushButtons<Object>) menuLinks).select(
				menuItem instanceof Enum ?
				menuItem :
				menuItem != null ? menuItem.toString() : null);
		}
	}

	/**
	 * Sets the menu items to be displayed.
	 *
	 * @param menuItems The new menu items (NULL or empty for none)
	 */
	public void setMenuItems(Collection<T> menuItems) {
		this.menuItems =
			menuItems != null ? menuItems : Collections.emptyList();
	}

	/**
	 * Sets the text to be displayed in the search field (if such exists) or
	 * hides the field if the text is NULL.
	 *
	 * @param text The search text to show (NULL to hide the search field)
	 */
	public void setSearchText(String text) {
		if (searchField != null) {
			if (text != null) {
				searchField.setText(text);
				searchField.show();
			} else {
				searchField.hide();
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
	 * @param builder The builder to add the menu components with
	 */
	protected void addMenuComponents(UiBuilder<?> builder) {
	}

	/**
	 * Adds the menu item components of this menu. May be overridden by
	 * subclasses to modify or extend the menu presentation. The default
	 * implementation adds menu buttons for each menu item value.
	 *
	 * @param builder The builder to add the menu items with
	 */
	@SuppressWarnings("unchecked")
	protected <E extends Enum<E>> void addMenuItems(UiBuilder<?> builder) {
		if (!menuItems.isEmpty()) {
			T firstItem = CollectionUtil.firstElementOf(menuItems);

			if (firstItem instanceof Enum) {
				menuLinks = createEnumMenuLinks(builder, firstItem);
			} else {
				UiPushButtons<String> links = builder.addPushButtons();

				links
					.setButtons(menuItems.stream().map(i -> i.toString()))
					.select(firstItem.toString());
				menuLinks = links;
			}

			menuLinks
				.withImages()
				.buttonStyle(ButtonStyle.LINK)
				.onAction(this::handleItemSelection);

			if (get(ORIENTATION) == Orientation.VERTICAL) {
				menuLinks.set(LAYOUT, LayoutType.INLINE);
			}
		}
	}

	/**
	 * Adds a search input field with the given builder (which should be a
	 * builder from this menu).
	 *
	 * @param builder       The builder to add the field with
	 * @param placeholder   The text to be displayed if the field is empty
	 * @param searchHandler handler The handler to be invoked with the input
	 *                      value if a search is to be performed
	 * @return The search input field component
	 */
	@SuppressWarnings("boxing")
	protected UiTextField addSearchField(UiBuilder<?> builder,
		String placeholder, Consumer<String> searchHandler) {
		searchField = builder
			.addTextField("")
			.set(PLACEHOLDER, placeholder)
			.set(EVENT_HANDLING_DELAY, 1200)
			.onUpdate(searchHandler)
			.onAction(searchHandler);

		return searchField;
	}

	/**
	 * Builds this menu by first invoking {@link #addMenuComponents(UiBuilder)}
	 * and then adds the menu items if such are available.
	 *
	 * @see UiComposite#buildContent(UiBuilder)
	 */
	@Override
	protected void buildContent(UiBuilder<?> builder) {
		addMenuComponents(builder);

		if (!menuItems.isEmpty()) {
			UiLayoutPanel itemPanel =
				builder.addPanel(new UiSecondaryContentLayout());

			itemPanel.set(FLOAT, Alignment.END);
			addMenuItems(itemPanel.builder());
		}
	}

	/**
	 * Handles the selection of a menu item by forwarding it to an event
	 * handler
	 * registered through {@link #onMenuSelection(Consumer)}.
	 *
	 * @param item The selected menu item
	 */
	@SuppressWarnings("unchecked")
	protected void handleItemSelection(Object item) {
		if (menuSelectionHandler != null) {
			menuSelectionHandler.accept((T) item);
		}
	}

	/**
	 * Generically typed helper method to create the menu links component
	 * for an
	 * enum datatype.
	 *
	 * @param builder   The container builder
	 * @param firstItem The first menu item
	 * @return The menu links component
	 */
	@SuppressWarnings("unchecked")
	private <E extends Enum<E>> UiPushButtons<E> createEnumMenuLinks(
		UiBuilder<?> builder, T firstItem) {
		return builder
			.addPushButtons((Class<E>) firstItem.getClass())
			.select((E) firstItem);
	}
}
