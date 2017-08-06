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
package de.esoco.process.step.entity;

import de.esoco.entity.Entity;

import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListLayoutStyle;

import de.esoco.process.RuntimeProcessException;
import de.esoco.process.step.InteractionFragment;
import de.esoco.process.step.entity.EntityList.EntityListItem;

import static de.esoco.lib.property.StyleProperties.LIST_LAYOUT_STYLE;


/********************************************************************
 * A base class for items in an {@link EntityListItem}.
 *
 * @author eso
 */
public abstract class AbstractEntityListItem<E extends Entity>
	extends InteractionFragment implements EntityListItem<E>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private EntityList<?, ?> rEntityList;

	private E	    rEntity		  = null;
	private boolean bSelected     = false;
	private boolean bSimpleLayout;

	private String sDefaultStyle;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public AbstractEntityListItem()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the entity displayed by this item.
	 *
	 * @return The current entity
	 */
	@Override
	public final E getEntity()
	{
		return rEntity;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		layout(LayoutType.LIST_ITEM).style(sDefaultStyle);

		initItemContent();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSelected()
	{
		return bSelected;
	}

	/***************************************
	 * Sets the default style.
	 *
	 * @param sStyle The new default style
	 */
	@Override
	public void setDefaultStyle(String sStyle)
	{
		sDefaultStyle = sStyle;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void setSelected(boolean bSelected)
	{
		String sStyle = sDefaultStyle;

		this.bSelected = bSelected;

		if (bSelected)
		{
			sStyle += " selectedEntity";
		}

		fragmentParam().style(sStyle);

		if (bSelected && !bSimpleLayout)
		{
			try
			{
				updateContent(rEntity);
			}
			catch (Exception e)
			{
				throw new RuntimeProcessException(this, e);
			}
		}
	}

	/***************************************
	 * Updates the asset to be display by this instance.
	 *
	 * @param rEntity The asset for this item
	 */
	@Override
	public void updateEntity(E rEntity)
	{
		this.rEntity = rEntity;

		try
		{
			if (bSimpleLayout)
			{
				updateContent(rEntity);
			}
			else
			{
				updateHeader(rEntity);
				prepareContent(rEntity);

				if (bSelected)
				{
					updateContent(rEntity);
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeProcessException(this, e);
		}
	}

	/***************************************
	 * Must be implemented to init the fragment containing the content panel of
	 * this list item. The panel layout is pre-set to {@link LayoutType#GRID}
	 * which can be overridden.
	 *
	 * @param rContentPanel The content panel fragment
	 */
	protected abstract void initContentPanel(InteractionFragment rContentPanel);

	/***************************************
	 * Internal method to create the event handling wrapper for the header
	 * panel.
	 *
	 * @param rHeader The header panel fragment
	 */
	protected void createHeaderPanel(InteractionFragment rHeader)
	{
		rHeader.layout(LayoutType.HEADER).onAction(v -> handleItemSelection());
		rHeader.panel(p -> initHeaderPanel(p));
	}

	/***************************************
	 * Needs to be implemented to initialize the header panel of list items in
	 * list layouts that separate header and content (i.e. with a {@link
	 * ListLayoutStyle} other than {@link ListLayoutStyle#SIMPLE}). The panel
	 * layout is set to {@link LayoutType#GRID} which can be overridden. The
	 * default implementation does nothing.
	 *
	 * @param rHeaderPanel The header panel fragment
	 */
	protected void initHeaderPanel(InteractionFragment rHeaderPanel)
	{
	}

	/***************************************
	 * Initializes the content parameters that defines the UI of this list item.
	 * Can be overridden by subclasses that need to define specific UIs. The
	 * default implementation invokes {@link
	 * #initHeaderPanel(InteractionFragment)} and {@link
	 * #initContentPanel(InteractionFragment)}.
	 */
	protected void initItemContent()
	{
		if (!bSimpleLayout)
		{
			panel(p -> createHeaderPanel(p));
		}

		panel(p -> initContentPanel(p));
	}

	/***************************************
	 * Returns the simpleLayout value.
	 *
	 * @return The simpleLayout value
	 */
	protected boolean isSimpleLayout()
	{
		return bSimpleLayout;
	}

	/***************************************
	 * Will be invoked for all items in an entity list if the entity needs to be
	 * updated. For selected entities the method {@link #updateContent(Entity)}
	 * will be invoked afterwards. Normally the latter should be used to perform
	 * time-critical updates from the entity, e.g. performing queries of related
	 * data.
	 *
	 * @param  rEntity The entity to prepare the content from
	 *
	 * @throws Exception May throw any kind of exception on errors
	 */
	protected void prepareContent(E rEntity) throws Exception
	{
	}

	/***************************************
	 * Overridden to check whether the parent fragment has a simple list layout.
	 *
	 * @see InteractionFragment#setParent(InteractionFragment)
	 */
	@Override
	protected void setParent(InteractionFragment rParent)
	{
		super.setParent(rParent);

		if (rParent != null)
		{
			rEntityList = (EntityList<?, ?>) rParent.getParent();

			ListLayoutStyle eListLayout =
				rParent.fragmentParam().get(LIST_LAYOUT_STYLE);

			bSimpleLayout =
				(eListLayout == null ||
				 eListLayout == ListLayoutStyle.SIMPLE);
		}
	}

	/***************************************
	 * Will be invoked to update the content of this item when it is selected.
	 * If an update of the item content is time-consuming (e.g. because of
	 * additional storage queries) it should be performed in this method instead
	 * of in {@link #prepareContent(Entity)} so that it is not invoked for each
	 * item without the data being displayed.
	 *
	 * @param  rEntity The new entity to update the parameters from
	 *
	 * @throws Exception May throw any kind of exception on errors
	 */
	protected void updateContent(E rEntity) throws Exception
	{
	}

	/***************************************
	 * Needs be implemented to update the content of list items in list layouts
	 * that separate header and content (i.e. with a {@link ListLayoutStyle}
	 * other than {@link ListLayoutStyle#SIMPLE}). The default implementation
	 * does nothing.
	 *
	 * @param  rEntity The new entity to update the parameters from
	 *
	 * @throws Exception May throw any kind of exception on errors
	 */
	protected void updateHeader(E rEntity) throws Exception
	{
	}

	/***************************************
	 * Handles the selection event for an item.
	 */
	void handleItemSelection()
	{
		rEntityList.setSelection(this);
	}
}
