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

import de.esoco.process.ui.UiBuilder;
import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.layout.UiHeaderLayout;

import java.util.List;

import static de.esoco.lib.property.StyleProperties.LIST_LAYOUT_STYLE;


/********************************************************************
 * A panel that contains a vertical list of items where each item is a container
 * for arbitrary components.
 *
 * @author eso
 */
public class UiListPanel extends UiComposite<UiListPanel>
{
	//~ Instance fields --------------------------------------------------------

	private List<Item>			  aItems;
	private final ListLayoutStyle eListStyle;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent    The parent container
	 * @param eListStyle The list style
	 */
	public UiListPanel(UiContainer<?> rParent, ListLayoutStyle eListStyle)
	{
		super(rParent, new ListLayout());

		this.eListStyle = eListStyle;

		set(LIST_LAYOUT_STYLE, eListStyle);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a new item to this list.
	 *
	 * @param  rItemLayout The layout of the item content
	 *
	 * @return The new item
	 */
	public Item addItem(UiLayout rItemLayout)
	{
		Item aItem = new Item(this, rItemLayout);

		aItems.add(aItem);

		return aItem;
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
	 * Returns the listStyle value.
	 *
	 * @return The listStyle value
	 */
	public final ListLayoutStyle getListStyle()
	{
		return eListStyle;
	}

	/***************************************
	 * Removes an item from this list.
	 *
	 * @param rItem The item to remove
	 */
	public void removeItem(Item rItem)
	{
		removeComponent(rItem);
		aItems.remove(rItem);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * The implementation of the list item container.
	 *
	 * @author eso
	 */
	public class Item extends UiComposite<Item>
	{
		//~ Instance fields ----------------------------------------------------

		private UiLayoutPanel aItemHeader  = null;
		private UiLayoutPanel aItemContent;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rParent     The list panel this item belongs to
		 * @param rItemLayout The layout of the panel inside the item
		 */
		Item(UiListPanel rParent, UiLayout rItemLayout)
		{
			super(rParent, new ListItemLayout());

			if (eListStyle != ListLayoutStyle.SIMPLE)
			{
				aItemHeader = builder().addPanel(new UiHeaderLayout());
			}

			aItemContent = builder().addPanel(rItemLayout);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the builder for the content panel of this item.
		 *
		 * @return The item content builder
		 */
		public final UiBuilder<?> getContentBuilder()
		{
			return aItemContent.builder();
		}

		/***************************************
		 * Returns the builder for the header panel of this item or NULL if the
		 * list has a simple style with items that have only content but no
		 * header.
		 *
		 * <p>As the header uses a special layout it is recommended to add only
		 * a single component with this builder (typically some panel with it's
		 * own layout) or else the resulting rendering can be unexpected.</p>
		 *
		 * @return The item header builder or NULL for none
		 */
		public final UiBuilder<?> getHeaderBuilder()
		{
			return aItemHeader != null ? aItemHeader.builder() : null;
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
