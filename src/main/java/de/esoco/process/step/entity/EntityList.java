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

import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.Layout;
import de.esoco.lib.property.ListLayoutStyle;
import de.esoco.lib.property.RelativeScale;
import de.esoco.lib.reflect.ReflectUtil;

import de.esoco.process.Parameter;
import de.esoco.process.ParameterEventHandler;
import de.esoco.process.ParameterList;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.step.InteractionFragment;
import de.esoco.process.step.entity.EntityList.EntityListItem;

import de.esoco.storage.Query;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.QueryResult;
import de.esoco.storage.Storage;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;
import de.esoco.storage.StoragePredicates.SortPredicate;
import de.esoco.storage.StorageRelationTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.LayoutProperties.ICON_SIZE;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.StyleProperties.LIST_LAYOUT_STYLE;
import static de.esoco.lib.property.StyleProperties.MULTI_SELECTION;

import static de.esoco.storage.StoragePredicates.like;


/********************************************************************
 * A fragment that displays a list of entities with user-defined list item
 * fragments.
 *
 * @author eso
 */
public class EntityList<E extends Entity,
						I extends InteractionFragment & EntityListItem<E>>
	extends InteractionFragment
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of paging navigation controls.
	 */
	enum PagingNavigation { FIRST_PAGE, PREVIOUS_PAGE, NEXT_PAGE, LAST_PAGE }

	/********************************************************************
	 * Enumeration of actions on the list filter.
	 */
	enum ListFilterAction { CLEAR }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	private static final Collection<String> DEFAULT_ALLOWED_LIST_SIZES =
		Arrays.asList("5", "10", "25", "50", "100");

	//~ Instance fields --------------------------------------------------------

	private final Class<E> rEntityType;
	private final Class<I> rItemType;

	private Predicate<? super E> pDefaultCriteria;
	private Predicate<? super E> pExtraCriteria = null;
	private Predicate<? super E> pAllCriteria   = null;
	private String				 sGlobalFilter  = null;

	private SortPredicate<? super E> pSortColumn;

	private List<E> aVisibleEntities = new ArrayList<>();
	private int     nEntityCount     = 0;
	private int     nFirstEntity     = 0;
	private int     nPageSize		 = 10;
	private boolean bInitialQuery    = true;

	private Collection<String> aAllowedListSizes = DEFAULT_ALLOWED_LIST_SIZES;

	private EntityListNavigation aNavigation;
	private EntityListItemList   aItemList;
	private I					 rSelectedItem = null;

	private InteractionFragment rHeader;
	private ParameterList	    aItemListPanel;

	private RelationType<String>[] rFilterAttributes;

	private Collection<EntitySelectionListener<E>> aSelectionListeners =
		new LinkedHashSet<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that doesn't perform an initial query. The user
	 * must invoke {@link #setDefaultCriteria(Predicate)} or {@link #update()}
	 * to perform the first query.
	 *
	 * @param rEntityType The entity type to be displayed in this list
	 * @param rItemType   The type of the list items
	 */
	public EntityList(Class<E> rEntityType, Class<I> rItemType)
	{
		this(rEntityType, rItemType, null, null);

		bInitialQuery = false;
	}

	/***************************************
	 * Creates a new instance that displays entities that fulfill the given
	 * criteria (if not NULL). The first query will be performed immediately
	 * after the fragment initialization has completed. If this is not desired
	 * the constructor {@link #EntityList(Class, Class)} should be used instead.
	 *
	 * @param rEntityType      The entity type to be displayed in this list
	 * @param rItemType        The type of the list items
	 * @param pDefaultCriteria The optional default criteria or NULL to query
	 *                         all entities
	 * @param pSortColumn      The sort predicate for the initial sort column or
	 *                         NULL for none
	 */
	public EntityList(Class<E>				   rEntityType,
					  Class<I>				   rItemType,
					  Predicate<? super E>	   pDefaultCriteria,
					  SortPredicate<? super E> pSortColumn)
	{
		this.rItemType		  = rItemType;
		this.rEntityType	  = rEntityType;
		this.pDefaultCriteria = pDefaultCriteria;
		this.pSortColumn	  = pSortColumn;

		aNavigation = new EntityListNavigation();
		aItemList   = new EntityListItemList();
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a filter value for a SQL like expression.
	 *
	 * @param  sFilter The original filter value
	 *
	 * @return The converted filter value
	 */
	static Predicate<Object> createLikeFilter(String sFilter)
	{
		if (sFilter.indexOf('*') == -1)
		{
			sFilter += "*";
		}

		sFilter = StorageManager.convertToSqlConstraint(sFilter);

		Predicate<Object> pLikeFilter = like(sFilter);

		return pLikeFilter;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Register an entity selection listener.
	 *
	 * @param rListener The listener to register
	 */
	public void addSelectionListener(EntitySelectionListener<E> rListener)
	{
		aSelectionListeners.add(rListener);
	}

	/***************************************
	 * Clears this list by removing all entities and updating the display.
	 */
	public void clear()
	{
		aVisibleEntities.clear();
		aItemList.update();
		aNavigation.update();
	}

	/***************************************
	 * Returns the current default criteria of this instance.
	 *
	 * @return The default criteria
	 */
	public final Predicate<? super E> getDefaultCriteria()
	{
		return pDefaultCriteria;
	}

	/***************************************
	 * Returns the type of the entities displayed in this list.
	 *
	 * @return The entityType value
	 */
	public final Class<E> getEntityType()
	{
		return rEntityType;
	}

	/***************************************
	 * Returns the current extra criteria of this instance.
	 *
	 * @return The extra criteria
	 */
	public final Predicate<? super E> getExtraCriteria()
	{
		return pExtraCriteria;
	}

	/***************************************
	 * Returns the global filter string.
	 *
	 * @return The global filter string or NULL for none
	 */
	public final String getGlobalFilter()
	{
		return sGlobalFilter;
	}

	/***************************************
	 * Returns the current number of entities that are displayed in a list page.
	 *
	 * @return The current page size
	 */
	public final int getPageSize()
	{
		return nPageSize;
	}

	/***************************************
	 * Returns the currently selected entity.
	 *
	 * @return The selected entity or NULL for none
	 */
	public final E getSelectedEntity()
	{
		return rSelectedItem != null ? rSelectedItem.getEntity() : null;
	}

	/***************************************
	 * Returns the currently selected item.
	 *
	 * @return The selected item or NULL for none
	 */
	public final I getSelectedItem()
	{
		return rSelectedItem;
	}

	/***************************************
	 * Returns the sort predicate for the current sort column.
	 *
	 * @return The current sort column predicate (NULL for none)
	 */
	public final SortPredicate<? super E> getSortColumn()
	{
		return pSortColumn;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init()
	{
		layout(Layout.FLOW).style(EntityList.class.getSimpleName());

		panel(this::initHeaderPanel);
		aItemListPanel =
			panel(aItemList).inherit(LIST_LAYOUT_STYLE, MULTI_SELECTION);
		panel(aNavigation).hide();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void initComplete() throws Exception
	{
		if (bInitialQuery)
		{
			if (pAllCriteria == null)
			{
				updateQuery();
			}
			else
			{
				queryEntities();
			}
		}
	}

	/***************************************
	 * Removes an entity selection listener.
	 *
	 * @param rListener The listener to remove
	 */
	public void removeSelectionListener(EntitySelectionListener<?> rListener)
	{
		aSelectionListeners.remove(rListener);
	}

	/***************************************
	 * Sets the default criteria for this instance. This will also update the
	 * display.
	 *
	 * @param  pDefaultCriteria The new default criteria
	 *
	 * @throws StorageException If updating the list fails
	 */
	public void setDefaultCriteria(Predicate<? super E> pDefaultCriteria)
		throws StorageException
	{
		this.pDefaultCriteria = pDefaultCriteria;

		updateQuery();
	}

	/***************************************
	 * Sets a filter predicate for this list. This predicate will be considered
	 * together with the default criteria and the text filter set through {@link
	 * #setGlobalFilter(String)}.
	 *
	 * @param pCriteria sThe filter predicate or NULL to reset
	 */
	public void setExtraCriteria(Predicate<? super E> pCriteria)
	{
		this.pExtraCriteria = pCriteria;

		updateQuery();
	}

	/***************************************
	 * Sets a global filter string for this list. This will apply the filter to
	 * all attributes set with {@link
	 * #setGlobalFilterAttributes(RelationType...)}.
	 *
	 * @param sFilter The filter string or NULL for no filter
	 */
	public void setGlobalFilter(String sFilter)
	{
		sGlobalFilter = sFilter;
		updateQuery();
	}

	/***************************************
	 * Sets the attributes to be considered by the global filter.
	 *
	 * @param rFilterAttributes The new filter attributes
	 */
	@SafeVarargs
	public final void setGlobalFilterAttributes(
		RelationType<String>... rFilterAttributes)
	{
		this.rFilterAttributes = rFilterAttributes;
	}

	/***************************************
	 * Sets the header for to be displayed at the top of the list. This method
	 * must be invoked before this fragment is initialized or otherwise the
	 * header will not be displayed.
	 *
	 * @param rHeader The new list header
	 */
	public void setListHeader(InteractionFragment rHeader)
	{
		this.rHeader = rHeader;
	}

	/***************************************
	 * Sets the number of entities to be displayed in a list page. Sets the page
	 * size value.
	 *
	 * @param rPageSize The new page size
	 */
	public final void setPageSize(int rPageSize)
	{
		nPageSize = rPageSize;

		updateQuery();
	}

	/***************************************
	 * Sets the currently selected list item.
	 *
	 * @param rItem The new selection
	 */
	@SuppressWarnings("unchecked")
	public void setSelection(EntityListItem<?> rItem)
	{
		if (rSelectedItem != rItem)
		{
			if (rSelectedItem != null)
			{
				rSelectedItem.setSelected(false);
			}

			rSelectedItem = (I) rItem;

			if (rSelectedItem != null)
			{
				rSelectedItem.setSelected(true);
			}
		}

		for (EntitySelectionListener<E> rListener : aSelectionListeners)
		{
			rListener.onSelection(getSelectedEntity());
		}
	}

	/***************************************
	 * Sets the sort predicate for the column to sort by.
	 *
	 * @param pSortColumn The sort predicate or NULL for none
	 */
	public final void setSortColumn(SortPredicate<? super E> pSortColumn)
	{
		this.pSortColumn = pSortColumn;
		updateQuery();
	}

	/***************************************
	 * Updates this list to display to display the entities for the current
	 * criteria.
	 *
	 * @throws StorageException If performing the query fails
	 */
	public void update() throws StorageException
	{
		queryEntities();
	}

	/***************************************
	 * Creates a new instance of the list item type. The default implementation
	 * uses reflection to create a new instance and requires the availability of
	 * a public no-arguments constructor. Subclasses can override this method to
	 * create items that need additional initialization, e.g. through
	 * constructor arguments.
	 *
	 * @return The new list item
	 */
	protected I createListItem()
	{
		return ReflectUtil.newInstance(rItemType);
	}

	/***************************************
	 * Initializes the header panel of this list if a header has been set with
	 * {@link #setListHeader(InteractionFragment)}.
	 *
	 * @param rHeaderPanel The header panel fragment
	 */
	protected void initHeaderPanel(InteractionFragment rHeaderPanel)
	{
		if (rHeader != null)
		{
			rHeaderPanel.layout(Layout.LIST).resid("EntityListHeaderPanel")
						.set(LIST_LAYOUT_STYLE, ListLayoutStyle.SIMPLE);

			rHeaderPanel.panel(rHeader);
		}
	}

	/***************************************
	 * Queries the current page of entities according to the given criteria and
	 * updates the display.
	 *
	 * @throws StorageException If the query fails
	 */
	void queryEntities() throws StorageException
	{
		Storage rStorage = StorageManager.getStorage(rEntityType);

		Predicate<E> pCriteria = Predicates.and(pAllCriteria, pSortColumn);

		QueryPredicate<E> qEntities =
			new QueryPredicate<>(rEntityType, pCriteria);

		try (Query<E> aQuery = rStorage.query(qEntities))
		{
			int nCount = nPageSize;

			aQuery.set(StorageRelationTypes.QUERY_OFFSET, nFirstEntity);
			aQuery.set(StorageRelationTypes.QUERY_LIMIT, nPageSize);

			QueryResult<E> aEntities = aQuery.execute();

			nEntityCount = aQuery.size();

			if (nFirstEntity + nPageSize > nEntityCount)
			{
				nFirstEntity = Math.max(0, nEntityCount - nPageSize);
			}

			aVisibleEntities.clear();

			while (nCount-- > 0 && aEntities.hasNext())
			{
				aVisibleEntities.add(aEntities.next());
			}
		}
		finally
		{
			rStorage.release();
		}

		aItemList.update();
		aNavigation.update();
	}

	/***************************************
	 * Sets the allowed list sizes that can be selected from a drop-down.
	 *
	 * @param rAllowedSizes The selectable list sizes
	 */
	final void setAllowedListSizes(int[] rAllowedSizes)
	{
		aAllowedListSizes = new ArrayList<>(rAllowedSizes.length);

		for (int nSize : rAllowedSizes)
		{
			aAllowedListSizes.add(Integer.toString(nSize));
		}
	}

	/***************************************
	 * Builds the full query criteria.
	 */
	private void updateQuery()
	{
		pAllCriteria = Predicates.and(pDefaultCriteria, pExtraCriteria);

		if (sGlobalFilter != null && !sGlobalFilter.isEmpty())
		{
			Predicate<? super E> pAttrCriteria = null;

			if (rFilterAttributes != null)
			{
				Predicate<Object> pLikeFilter = createLikeFilter(sGlobalFilter);

				for (RelationType<String> rFilterAttr : rFilterAttributes)
				{
					pAttrCriteria =
						Predicates.or(pAttrCriteria,
									  rFilterAttr.is(pLikeFilter));
				}
			}

			pAllCriteria = Predicates.and(pAllCriteria, pAttrCriteria);
		}

		try
		{
			nFirstEntity = 0;

			if (isInitialized())
			{
				queryEntities();
			}
		}
		catch (StorageException e)
		{
			throw new RuntimeProcessException(this, e);
		}
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * The interface that needs to be implemented by item fragments in an {@link
	 * EntityList}.
	 *
	 * @author eso
	 */
	public static interface EntityListItem<E extends Entity>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the entity of this list item.
		 *
		 * @return The item entity
		 */
		public E getEntity();

		/***************************************
		 * Checks whether this item is currently selected.
		 *
		 * @return The selected state
		 */
		public boolean isSelected();

		/***************************************
		 * Sets the default style of this item. This style should always be
		 * applied if the style of an item needs to be reset.
		 *
		 * @param sStyle The new default style
		 */
		public void setDefaultStyle(String sStyle);

		/***************************************
		 * Sets the selected state of this item
		 *
		 * @param bSelected The new selected state
		 */
		public void setSelected(boolean bSelected);

		/***************************************
		 * Updates the entity that is displayed by this instance.
		 *
		 * @param rEntity The new Entity
		 */
		public void updateEntity(E rEntity);
	}

	/********************************************************************
	 * A listener interface that will be notified of entity selection.
	 *
	 * @author eso
	 */
	public static interface EntitySelectionListener<E extends Entity>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Will be invoked if an entity is (de-) selected.
		 *
		 * @param rEntity The selected entity or NULL for none
		 */
		public void onSelection(E rEntity);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * Internal fragment that represents the list of entity list items.
	 *
	 * @author eso
	 */
	class EntityListItemList extends InteractionFragment
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Instance fields ----------------------------------------------------

		private List<I> aItems = new ArrayList<>();

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void init()
		{
			layout(Layout.LIST);
			updateItemList();
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected void abort()
		{
		}

		/***************************************
		 * Updates the currently displayed entities.
		 */
		void update()
		{
			updateItemList();

			int nVisibleItems = Math.min(aVisibleEntities.size(), nPageSize);

			for (int i = 0; i < nPageSize; i++)
			{
				I rItem = aItems.get(i);

				if (i < nVisibleItems)
				{
					rItem.updateEntity(aVisibleEntities.get(i));
					rItem.fragmentParam().show();
				}
				else
				{
					rItem.fragmentParam().hide();
				}
			}

			aNavigation.fragmentParam().setVisible(nEntityCount > nPageSize);
		}

		/***************************************
		 * Updates the list items by creating or removing item according to the
		 * current page size.
		 */
		void updateItemList()
		{
			int nCurrentSize = aItems.size();

			if (nCurrentSize < nPageSize)
			{
				for (int i = nCurrentSize; i < nPageSize; i++)
				{
					I aItem = createListItem();

					aItems.add(aItem);
					aItem.setDefaultStyle(i % 2 == 1 ? "odd" : "even");
					addSubFragment("Entity" + i, aItem).resid("Entity");
					aItem.fragmentParam().hide();
				}
			}
			else if (nCurrentSize > nPageSize)
			{
				for (int i = aItems.size() - 1; i >= nPageSize; i--)
				{
					I rItem = aItems.remove(i);

					if (rItem == rSelectedItem)
					{
						setSelection(null);
					}

					removeSubFragment(rItem);
				}
			}
		}
	}

	/********************************************************************
	 * The navigation panel of an entity list.
	 *
	 * @author eso
	 */
	class EntityListNavigation extends InteractionFragment
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Instance fields ----------------------------------------------------

		private Parameter<String> aNavPosition;

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception
		{
			layout(Layout.TABLE);

			if (aAllowedListSizes.size() > 1)
			{
				String sPageSize = Integer.toString(nPageSize);

				dropDown("EntityListPageSize", aAllowedListSizes).value(sPageSize)
																 .input()
																 .onUpdate(new ParameterEventHandler<String>()
					{
						@Override
						public void handleParameterUpdate(String sPageSize)
							throws Exception
						{
							changePageSize(sPageSize);
						}
					});
			}

			pagingButtons("LeftPaging",
						  PagingNavigation.FIRST_PAGE,
						  PagingNavigation.PREVIOUS_PAGE).sameRow();

			aNavPosition = label("").resid("EntityListPosition").sameRow();

			pagingButtons("RightPaging",
						  PagingNavigation.NEXT_PAGE,
						  PagingNavigation.LAST_PAGE).sameRow();

			update();
		}

		/***************************************
		 * Changes the number of displayed list items.
		 *
		 * @param  sNewSize The new number of list items
		 *
		 * @throws StorageException If updating the list fails
		 */
		void changePageSize(String sNewSize) throws StorageException
		{
			nPageSize = Integer.parseInt(sNewSize);
			queryEntities();
		}

		/***************************************
		 * Handles the paging navigation.
		 *
		 * @param  eNavigation The navigation action
		 *
		 * @throws StorageException If updating the list fails
		 */
		void navigate(PagingNavigation eNavigation) throws StorageException
		{
			int nMax = Math.max(0, nEntityCount - nPageSize);

			switch (eNavigation)
			{
				case FIRST_PAGE:
					nFirstEntity = 0;
					break;

				case PREVIOUS_PAGE:
					nFirstEntity = Math.max(0, nFirstEntity - nPageSize);
					break;

				case NEXT_PAGE:
					nFirstEntity = Math.min(nMax, nFirstEntity + nPageSize);
					break;

				case LAST_PAGE:
					nFirstEntity = nMax;
					break;
			}

			setSelection(null);
			aItemListPanel.set(-1, CURRENT_SELECTION);
			queryEntities();
		}

		/***************************************
		 * Updates the indicator of the current navigation position.
		 */
		@SuppressWarnings("boxing")
		void update()
		{
			String sPosition =
				String.format("$$%d - %d {$lblPositionOfCount} %d",
							  nFirstEntity + 1,
							  nFirstEntity + aItemList.aItems.size(),
							  nEntityCount);

			aNavPosition.value(sPosition);
		}

		/***************************************
		 * Creates a navigation buttons parameter for the given values.
		 *
		 * @param  sName          The name of the parameter
		 * @param  rAllowedValues The allowed button values
		 *
		 * @return The new parameter
		 */
		private Parameter<PagingNavigation> pagingButtons(
			String				sName,
			PagingNavigation... rAllowedValues)
		{
			Parameter<PagingNavigation> aNavigation =
				param(sName, PagingNavigation.class);

			return aNavigation.resid(PagingNavigation.class.getSimpleName())
							  .label("")
							  .buttons(rAllowedValues)
							  .buttonStyle(ButtonStyle.ICON)
							  .images()
							  .set(ICON_SIZE, RelativeScale.SMALL)
							  .layout(Layout.TABLE)
							  .onAction(new ParameterEventHandler<PagingNavigation>()
				{
					@Override
					public void handleParameterUpdate(
						PagingNavigation eNavigation) throws Exception
					{
						navigate(eNavigation);
					}
				});
		}
	}
}
