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
import de.esoco.lib.property.ListLayoutStyle;

import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.layout.UiHeaderLayout;

import java.util.ArrayList;
import java.util.List;

import static de.esoco.lib.property.StateProperties.ACTION_EVENT_ON_ACTIVATION_ONLY;
import static de.esoco.lib.property.StyleProperties.LIST_LAYOUT_STYLE;


/********************************************************************
 * A panel that contains a vertical list of items where each item is a container
 * for arbitrary components.
 *
 * @author eso
 */
public class UiListPanel extends UiComposite<UiListPanel>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * The available styles for expandable lists:
	 *
	 * <ul>
	 *   <li>{@link #EXPAND}: expands an item inside the list.</li>
	 *   <li>{@link #POPOUT}: expands an item and separates it from the other
	 *     list items.</li>
	 * </ul>
	 */
	public enum ExpandableListStyle { EXPAND, POPOUT }

	//~ Instance fields --------------------------------------------------------

	private final ExpandableListStyle eExpandStyle;

	private List<Item> aItems = new ArrayList<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with simple items that are not expandable.
	 *
	 * @param rParent The parent container
	 */
	public UiListPanel(UiContainer<?> rParent)
	{
		this(rParent, null);
	}

	/***************************************
	 * Creates a new instance with items that can be expanded by selecting their
	 * header area. Expanding an item will reveal the item content and hide any
	 * other previously expanded item content.
	 *
	 * @param rParent      The parent container
	 * @param eExpandStyle The expand style
	 */
	public UiListPanel(UiContainer<?>	   rParent,
					   ExpandableListStyle eExpandStyle)
	{
		super(rParent, new ListLayout());

		this.eExpandStyle = eExpandStyle;

		ListLayoutStyle eListStyle =
			eExpandStyle == ExpandableListStyle.EXPAND
			? ListLayoutStyle.EXPAND
			: eExpandStyle == ExpandableListStyle.POPOUT
			? ListLayoutStyle.POPOUT : null;

		set(LIST_LAYOUT_STYLE, eListStyle);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a new item to this list.
	 *
	 * @return The new item
	 */
	public Item addItem()
	{
		Item aItem = new Item(this);

		aItems.add(aItem);

		return aItem;
	}

	/***************************************
	 * @see de.esoco.process.ui.UiContainer#clear()
	 */
	@Override
	public void clear()
	{
		aItems.clear();
		super.clear();
	}

	/***************************************
	 * Returns the items in this list.
	 *
	 * @return The list items
	 */
	public List<Item> getItems()
	{
		return aItems;
	}

	/***************************************
	 * Removes an item from this list.
	 *
	 * @param rItem The item to remove
	 */
	public void removeItem(Item rItem)
	{
		aItems.remove(rItem);
		removeComponent(rItem);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * The container for a single list item.
	 *
	 * @author eso
	 */
	public class Item extends UiComposite<Item>
	{
		//~ Instance fields ----------------------------------------------------

		private UiLayoutPanel aItemHeader = null;

		private String sDefaultStyle = null;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rParent The list panel this item belongs to
		 */
		Item(UiListPanel rParent)
		{
			super(rParent, new ListItemLayout());

			if (eExpandStyle != null)
			{
				aItemHeader =
					builder().addPanel(new UiHeaderLayout())
							 .set(ACTION_EVENT_ON_ACTIVATION_ONLY);
			}
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Helper method to create a new header panel with a certain layout in
		 * the item header returned by {@link #getHeader()}. This method should
		 * only be invoked once or else additional panels will be added to the
		 * header.
		 *
		 * @param  rLayout The header panel layout
		 *
		 * @return The new panel with the given layout
		 */
		public final UiLayoutPanel createHeaderPanel(UiLayout rLayout)
		{
			return new UiLayoutPanel(getHeader(), rLayout);
		}

		/***************************************
		 * Returns the header container panel of this item. If the list has a
		 * simple style this method returns the item itself so that it can be
		 * used to build the content in it.
		 *
		 * <p>The item header uses a special layout and therefore it is
		 * recommended to add only a single component with this builder
		 * (typically some panel with it's own layout) or else the resulting
		 * rendering can be unexpected.</p>
		 *
		 * @return The item header container
		 */
		public final UiContainer<?> getHeader()
		{
			return aItemHeader != null ? aItemHeader : this;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected String getComponentStyleName()
		{
			return UiListPanel.this.getComponentStyleName() +
				   super.getComponentStyleName();
		}

		/***************************************
		 * Returns the default style of this item.
		 *
		 * @return The default style
		 */
		protected final String getDefaultStyle()
		{
			return sDefaultStyle;
		}

		/***************************************
		 * Sets the default style that should always be applied to this item (in
		 * additional to any style set in {@link #style()}).
		 *
		 * @param rDefaultStyle The defaultStyle value
		 */
		protected final void setDefaultStyle(String rDefaultStyle)
		{
			sDefaultStyle = rDefaultStyle;
		}
	}

	/********************************************************************
	 * The internal layout of list items.
	 *
	 * @author eso
	 */
	static class ListItemLayout extends UiLayout
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 */
		public ListItemLayout()
		{
			super(LayoutType.LIST_ITEM);
		}
	}

	/********************************************************************
	 * The internal layout of list panels.
	 *
	 * @author eso
	 */
	static class ListLayout extends UiLayout
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 */
		public ListLayout()
		{
			super(LayoutType.LIST);
		}
	}
}
