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
package de.esoco.process.ui.composite;

import de.esoco.lib.expression.monad.Option;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListLayoutStyle;
import de.esoco.process.ui.UiBuilder;
import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.layout.UiHeaderLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static de.esoco.lib.property.StateProperties.ACTION_EVENT_ON_ACTIVATION_ONLY;
import static de.esoco.lib.property.StyleProperties.LIST_LAYOUT_STYLE;

/**
 * A panel that contains a vertical list of items where each item is a container
 * for arbitrary components.
 *
 * @author eso
 */
public class UiListPanel extends UiComposite<UiListPanel> {

	/**
	 * The available styles for expandable lists:
	 *
	 * <ul>
	 *   <li>{@link #EXPAND}: expands an item inside the list.</li>
	 *   <li>{@link #POPOUT}: expands an item and separates it from the other
	 *     list items.</li>
	 * </ul>
	 */
	public enum ExpandableListStyle {
		EXPAND, POPOUT
	}

	boolean selectable = false;

	private Option<ExpandableListStyle> expandStyle;

	private final List<Item> items = new ArrayList<>();

	private Item selectedItem = null;

	private Consumer<Item> selectionHandler;

	/**
	 * Creates a new instance with simple items that are not expandable.
	 *
	 * @param parent The parent container
	 */
	public UiListPanel(UiContainer<?> parent) {
		this(parent, Option.none());
	}

	/**
	 * Creates a new instance with items that can be expanded by selecting
	 * their
	 * header area. Expanding an item will reveal the item content and hide any
	 * other previously expanded item content.
	 *
	 * @param parent      The parent container
	 * @param expandStyle The expand style
	 */
	public UiListPanel(UiContainer<?> parent,
		Option<ExpandableListStyle> expandStyle) {
		super(parent, new ListLayout());

		expandStyle(expandStyle);
	}

	/**
	 * Adds a new item to this list.
	 *
	 * @return The new item
	 */
	public Item addItem() {
		Item item = new Item(this);

		items.add(item);

		return item;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		items.clear();
		super.clear();
	}

	/**
	 * Sets the optional expand style of this list. This can typically only be
	 * changed before the list is first rendered. Use {@link Option#none()} for
	 * a non-expandable list.
	 *
	 * @param expandStyle The optional new expandable list style
	 * @return This instance for fluent invocation
	 */
	public UiListPanel expandStyle(Option<ExpandableListStyle> expandStyle) {
		this.expandStyle = expandStyle;

		if (expandStyle.exists()) {
			set(LIST_LAYOUT_STYLE,
				ListLayoutStyle.valueOf(expandStyle.orFail().name()));
		}

		return this;
	}

	/**
	 * Returns the items in this list.
	 *
	 * @return The list items
	 */
	public List<Item> getItems() {
		return items;
	}

	/**
	 * Sets the handler for selection events.
	 *
	 * @param selectionHandler The selection handler
	 * @return This instance for fluent invocations
	 */
	public UiListPanel onSelection(Consumer<Item> selectionHandler) {
		this.selectionHandler = selectionHandler;

		return this;
	}

	/**
	 * Removes an item from this list.
	 *
	 * @param item The item to remove
	 */
	public void removeItem(Item item) {
		items.remove(item);
		remove(item);
	}

	/**
	 * Sets the selectable state o this list. The default is FALSE.
	 *
	 * @param selectable The new selectable state
	 * @return This instance for fluent invocations
	 */
	public UiListPanel selectable(boolean selectable) {
		this.selectable = selectable;

		return this;
	}

	/**
	 * Returns the optional expand style of this instance.
	 *
	 * @return The optional expandable list style
	 */
	protected final Option<ExpandableListStyle> getExpandStyle() {
		return expandStyle;
	}

	/**
	 * Handles item selection events.
	 *
	 * @param item The item that has been selected
	 */
	void changeSelection(Item item) {
		if (selectable && item != selectedItem) {
			if (selectedItem != null) {
				selectedItem.setSelected(false);
			}

			item.setSelected(true);
			selectedItem = item;

			if (selectionHandler != null) {
				selectionHandler.accept(item);
			}
		}
	}

	/**
	 * The internal layout of list items.
	 *
	 * @author eso
	 */
	static class ListItemLayout extends UiLayout {

		/**
		 * Creates a new instance.
		 */
		public ListItemLayout() {
			super(LayoutType.LIST_ITEM);
		}
	}

	/**
	 * The internal layout of list panels.
	 *
	 * @author eso
	 */
	static class ListLayout extends UiLayout {

		/**
		 * Creates a new instance.
		 */
		public ListLayout() {
			super(LayoutType.LIST);
		}
	}

	/**
	 * The container for a single list item. The builder returned from the item
	 * is used to create the item content. In the case of expandable lists the
	 * item header can be created with the builder of the header panel returned
	 * by {@link #getHeader()}.
	 *
	 * @author eso
	 */
	public class Item extends UiComposite<Item> {

		private Option<UiContainer<?>> itemHeader = Option.none();

		private boolean selected = false;

		private Consumer<Item> selectionHandler;

		/**
		 * Creates a new instance.
		 *
		 * @param parent The list panel this item belongs to
		 */
		Item(UiListPanel parent) {
			super(parent, new ListItemLayout());

			if (expandStyle.exists()) {
				itemHeader = Option.of(builder()
					.addPanel(new UiHeaderLayout())
					.set(ACTION_EVENT_ON_ACTIVATION_ONLY));
			}

			getHeader()
				.getContainer()
				.onClickInContainerArea(c -> parent.changeSelection(this));
		}

		/**
		 * Removes the content components from this item.
		 */
		public void clearContent() {
			List<UiComponent<?, ?>> components =
				new ArrayList<>(getComponents());

			UiContainer<?> header = itemHeader.orUse(null);

			components.stream().filter(c -> c != header).forEach(this::remove);
		}

		/**
		 * Helper method to create a new header panel with a certain layout in
		 * the item header returned by {@link #getHeader()}. This method should
		 * only be invoked once or else additional panels will be added to the
		 * header. Invoking this method mainly makes sense if an expandable
		 * list
		 * style is used. Otherwise it will just add an additional panel to the
		 * item.
		 *
		 * @param layout The header panel layout
		 * @return The builder of the new header panel with the given layout
		 */
		public final UiBuilder<?> createHeaderPanel(UiLayout layout) {
			return new UiLayoutPanel(getHeader().getContainer(),
				layout).builder();
		}

		/**
		 * Returns the builder of this item's header panel. If the list has a
		 * simple style this method returns the item's builder so that it
		 * can be
		 * used to build the content in it.
		 *
		 * <p>Depending on the underlying implementation the item header may
		 * use a special layout. Therefore it is recommended to add only a
		 * single component with this builder (typically some panel with it's
		 * own layout). Otherwise the resulting rendering can be
		 * unexpected.</p>
		 *
		 * @return The item header container
		 */
		public final UiBuilder<?> getHeader() {
			return itemHeader.orUse(this).builder();
		}

		/**
		 * Returns the selected state of this item.
		 *
		 * @return The selected state
		 */
		public boolean isSelected() {
			return selected;
		}

		/**
		 * Sets the handler for selection events of this item.
		 *
		 * @param selectionHandler The selection handler
		 * @return This instance for fluent invocations
		 */
		public Item onSelection(Consumer<Item> selectionHandler) {
			this.selectionHandler = selectionHandler;

			return this;
		}

		/**
		 * Overridden to be public
		 *
		 * @see UiComposite#remove(UiComponent)
		 */
		@Override
		public void remove(UiComponent<?, ?> component) {
			super.remove(component);
		}

		/**
		 * Sets the selected state of this item.
		 *
		 * @param selected The new selected state
		 */
		public void setSelected(boolean selected) {
			this.selected = selected;

			style().styleName(selected ? "selected" : "");

			if (selected && selectionHandler != null) {
				selectionHandler.accept(this);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getComponentStyleName() {
			return UiListPanel.this.getComponentStyleName() +
				super.getComponentStyleName();
		}
	}
}
