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
package de.esoco.process.step.entity;

import de.esoco.entity.Entity;
import de.esoco.entity.EntityIterator;
import de.esoco.entity.EntityPredicates;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListLayoutStyle;
import de.esoco.lib.property.RelativeScale;
import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.ValueEventHandler;
import de.esoco.process.param.Parameter;
import de.esoco.process.param.ParameterList;
import de.esoco.process.step.InteractionFragment;
import de.esoco.process.step.entity.EntityList.EntityListItem;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StorageException;
import de.esoco.storage.StoragePredicates.SortPredicate;
import de.esoco.storage.StorageRelationTypes;
import org.obrel.core.RelationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.property.LayoutProperties.ICON_SIZE;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.StyleProperties.LIST_LAYOUT_STYLE;
import static de.esoco.lib.property.StyleProperties.MULTI_SELECTION;

/**
 * A fragment that displays a list of entities with user-defined list item
 * fragments.
 *
 * @author eso
 */
public class EntityList<E extends Entity,
	I extends InteractionFragment & EntityListItem<E>>
	extends InteractionFragment {

	/**
	 * Enumeration of actions on the list filter.
	 */
	enum ListFilterAction {CLEAR}

	/**
	 * Enumeration of paging navigation controls.
	 */
	enum PagingNavigation {FIRST_PAGE, PREVIOUS_PAGE, NEXT_PAGE, LAST_PAGE}

	private static final long serialVersionUID = 1L;

	private static final Collection<String> DEFAULT_ALLOWED_LIST_SIZES =
		Arrays.asList("5", "10", "20", "25", "50");

	private final Class<E> entityType;

	private final Class<I> itemType;

	private final List<E> visibleEntities = new ArrayList<>();

	private final EntityListNavigation navigation;

	private final EntityListItemList itemList;

	private final Collection<EntitySelectionListener<E>> selectionListeners =
		new LinkedHashSet<>();

	private Predicate<? super E> defaultCriteria;

	private Predicate<? super E> extraCriteria = null;

	private Predicate<? super E> allCriteria = null;

	private String globalFilter = null;

	private ListLayoutStyle listLayoutStyle = null;

	private SortPredicate<? super E> sortColumn;

	private int entityCount = 0;

	private int firstEntity = 0;

	private int pageSize = 10;

	private boolean initialQuery = false;

	private Collection<String> allowedListSizes = DEFAULT_ALLOWED_LIST_SIZES;

	private I selectedItem = null;

	private EntityListHeader<E> header;

	private ParameterList itemListPanel;

	private ParameterList navigationPanel;

	private RelationType<String>[] globalFilterAttributes;

	/**
	 * Creates a new instance without criteria that doesn't perform an initial
	 * query. The user must invoke either
	 * {@link #setDefaultCriteria(Predicate)}
	 * or {@link #update()} to perform the first query.
	 *
	 * @param entityType The entity type to be displayed in this list
	 * @param itemType   The type of the list items
	 */
	public EntityList(Class<E> entityType, Class<I> itemType) {
		this(entityType, itemType, null, null, false);
	}

	/**
	 * Creates a new instance that displays entities that fulfill the given
	 * criteria (if not NULL). Depending on the boolean parameter the first
	 * query will be performed immediately after the fragment initialization
	 * has
	 * completed or, if FALSE on the first call to {@link #update()}.
	 *
	 * @param entityType      The entity type to be displayed in this list
	 * @param itemType        The type of the list items
	 * @param defaultCriteria The optional default criteria or NULL to query
	 *                          all
	 *                        entities
	 * @param sortColumn      The sort predicate for the initial sort column or
	 *                        NULL for none
	 * @param initialQuery    TRUE if a initial query should be performed
	 *                        automatically
	 */
	public EntityList(Class<E> entityType, Class<I> itemType,
		Predicate<? super E> defaultCriteria,
		SortPredicate<? super E> sortColumn, boolean initialQuery) {
		this.itemType = itemType;
		this.entityType = entityType;
		this.defaultCriteria = defaultCriteria;
		this.sortColumn = sortColumn;
		this.initialQuery = initialQuery;

		navigation = new EntityListNavigation();
		itemList = new EntityListItemList();
	}

	/**
	 * Register an entity selection listener.
	 *
	 * @param listener The listener to register
	 */
	public void addSelectionListener(EntitySelectionListener<E> listener) {
		selectionListeners.add(listener);
	}

	/**
	 * Clears this list by removing all entities and updating the display.
	 */
	public void clear() {
		visibleEntities.clear();
		itemList.update();
		navigation.update();
		navigationPanel.hide();
	}

	/**
	 * Returns the current default criteria of this instance.
	 *
	 * @return The default criteria
	 */
	public final Predicate<? super E> getDefaultCriteria() {
		return defaultCriteria;
	}

	/**
	 * Returns the type of the entities displayed in this list.
	 *
	 * @return The entityType value
	 */
	public final Class<E> getEntityType() {
		return entityType;
	}

	/**
	 * Returns the current extra criteria of this instance.
	 *
	 * @return The extra criteria
	 */
	public final Predicate<? super E> getExtraCriteria() {
		return extraCriteria;
	}

	/**
	 * Returns the global filter string.
	 *
	 * @return The global filter string or NULL for none
	 */
	public final String getGlobalFilter() {
		return globalFilter;
	}

	/**
	 * Returns the list of items for this entity list.
	 *
	 * @return The item list
	 */
	public final List<I> getItems() {
		return itemList.items;
	}

	/**
	 * Returns the list layout style.
	 *
	 * @return The list layout style
	 */
	public final ListLayoutStyle getListLayoutStyle() {
		return listLayoutStyle != null ?
		       listLayoutStyle :
		       fragmentParam().get(LIST_LAYOUT_STYLE);
	}

	/**
	 * Returns the current number of entities that are displayed in a list
	 * page.
	 *
	 * @return The current page size
	 */
	public final int getPageSize() {
		return pageSize;
	}

	/**
	 * Returns the currently selected entity.
	 *
	 * @return The selected entity or NULL for none
	 */
	public final E getSelectedEntity() {
		return selectedItem != null ? selectedItem.getEntity() : null;
	}

	/**
	 * Returns the currently selected item.
	 *
	 * @return The selected item or NULL for none
	 */
	public final I getSelectedItem() {
		return selectedItem;
	}

	/**
	 * Returns the sort predicate for the current sort column.
	 *
	 * @return The current sort column predicate (NULL for none)
	 */
	public final SortPredicate<? super E> getSortColumn() {
		return sortColumn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		layout(LayoutType.FLOW).style(EntityList.class.getSimpleName());

		if (listLayoutStyle != null) {
			fragmentParam().set(LIST_LAYOUT_STYLE, listLayoutStyle);
		}

		if (header != null) {
			panel(this::initHeaderPanel);
		}

		itemListPanel = panel(itemList);
		navigationPanel = panel(navigation).hide();

		itemListPanel.inherit(LIST_LAYOUT_STYLE, MULTI_SELECTION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initComplete() throws Exception {
		if (initialQuery) {
			if (allCriteria == null) {
				updateQuery();
			} else {
				queryEntities();
			}
		}
	}

	/**
	 * Removes an entity selection listener.
	 *
	 * @param listener The listener to remove
	 */
	public void removeSelectionListener(EntitySelectionListener<?> listener) {
		selectionListeners.remove(listener);
	}

	/**
	 * Sets the default criteria for this instance. This will also update the
	 * display.
	 *
	 * @param defaultCriteria The new default criteria
	 * @throws StorageException If updating the list fails
	 */
	public void setDefaultCriteria(Predicate<? super E> defaultCriteria) {
		this.defaultCriteria = defaultCriteria;

		updateQuery();
	}

	/**
	 * Sets a filter predicate for this list. This predicate will be considered
	 * together with the default criteria and the text filter set through
	 * {@link #setGlobalFilter(String)}.
	 *
	 * @param criteria The filter predicate or NULL to reset
	 */
	public void setExtraCriteria(Predicate<? super E> criteria) {
		this.extraCriteria = criteria;

		updateQuery();
	}

	/**
	 * Sets a global filter string for this list. This will apply the filter to
	 * all attributes set with
	 * {@link #setGlobalFilterAttributes(RelationType...)}.
	 *
	 * @param filter The filter string or NULL or empty for no filter
	 */
	public void setGlobalFilter(String filter) {
		globalFilter = !filter.isEmpty() ? filter : null;
		updateQuery();
	}

	/**
	 * Sets the attributes to be considered by the global filter.
	 *
	 * @param filterAttributes The new filter attributes
	 */
	@SafeVarargs
	public final void setGlobalFilterAttributes(
		RelationType<String>... filterAttributes) {
		this.globalFilterAttributes = filterAttributes;
	}

	/**
	 * Sets the header for to be displayed at the top of the list. This method
	 * must be invoked before this fragment is initialized or otherwise the
	 * header will not be displayed.
	 *
	 * @param header The new list header
	 */
	public void setListHeader(EntityListHeader<E> header) {
		this.header = header;
	}

	/**
	 * Sets the list layout style.
	 *
	 * @param listLayoutStyle The style
	 */
	public final void setListLayoutStyle(ListLayoutStyle listLayoutStyle) {
		this.listLayoutStyle = listLayoutStyle;
	}

	/**
	 * Sets the number of entities to be displayed in a list page. Sets the
	 * page
	 * size value.
	 *
	 * @param pageSize The new page size
	 */
	public final void setPageSize(int pageSize) {
		pageSize = pageSize;

		updateQuery();
	}

	/**
	 * Sets the currently selected list item.
	 *
	 * @param item The new selection
	 */
	@SuppressWarnings("unchecked")
	public void setSelection(EntityListItem<?> item) {
		if (selectedItem != item) {
			if (selectedItem != null) {
				selectedItem.setSelected(false);
			}

			selectedItem = (I) item;

			if (selectedItem != null) {
				selectedItem.setSelected(true);
			}

			itemListPanel.set(selectedItem != null ?
			                  itemList.items.indexOf(selectedItem) :
			                  -1, CURRENT_SELECTION);
		}

		for (EntitySelectionListener<E> listener : selectionListeners) {
			listener.onSelection(getSelectedEntity());
		}
	}

	/**
	 * Sets the sort predicate for the column to sort by.
	 *
	 * @param sortColumn The sort predicate or NULL for none
	 */
	public final void setSortColumn(SortPredicate<? super E> sortColumn) {
		this.sortColumn = sortColumn;
		updateQuery();
	}

	/**
	 * Updates this list to display the entities for the current criteria.
	 *
	 * @throws StorageException If performing the query fails
	 */
	public void update() throws StorageException {
		queryEntities();
	}

	/**
	 * Creates a new instance of the list item type. The default implementation
	 * uses reflection to create a new instance and requires the
	 * availability of
	 * a public no-arguments constructor. Subclasses can override this
	 * method to
	 * create items that need additional initialization, e.g. through
	 * constructor arguments.
	 *
	 * @return The new list item
	 */
	protected I createListItem() {
		return ReflectUtil.newInstance(itemType);
	}

	/**
	 * Initializes the header panel of this list if a header has been set with
	 * {@link #setListHeader(EntityListHeader)}.
	 *
	 * @param headerPanel The header panel fragment
	 */
	protected void initHeaderPanel(InteractionFragment headerPanel) {
		headerPanel
			.layout(LayoutType.LIST)
			.resid("EntityListHeaderPanel")
			.set(LIST_LAYOUT_STYLE, header.getHeaderType());

		headerPanel.panel(header);
	}

	/**
	 * Queries the current page of entities according to the given criteria and
	 * updates the display.
	 *
	 * @throws StorageException If the query fails
	 */
	void queryEntities() throws StorageException {
		Predicate<E> criteria = Predicates.and(allCriteria, sortColumn);

		QueryPredicate<E> queryEntities =
			new QueryPredicate<>(entityType, criteria);

		queryEntities.set(StorageRelationTypes.QUERY_OFFSET, firstEntity);
		queryEntities.set(StorageRelationTypes.QUERY_LIMIT, pageSize);

		try (EntityIterator<E> entities =
			new EntityIterator<>(queryEntities)) {
			int count = pageSize;

			entityCount = entities.size();

			if (firstEntity + pageSize > entityCount) {
				firstEntity = Math.max(0, entityCount - pageSize);
			}

			visibleEntities.clear();

			while (count-- > 0 && entities.hasNext()) {
				visibleEntities.add(entities.next());
			}
		}

		setSelection(null);
		itemList.update();
		navigation.update();
		navigationPanel.show();
	}

	/**
	 * Sets the allowed list sizes that can be selected from a drop-down.
	 *
	 * @param allowedSizes The selectable list sizes
	 */
	final void setAllowedListSizes(int[] allowedSizes) {
		allowedListSizes = new ArrayList<>(allowedSizes.length);

		for (int size : allowedSizes) {
			allowedListSizes.add(Integer.toString(size));
		}
	}

	/**
	 * Builds the full query criteria.
	 */
	private void updateQuery() {
		Predicate<? super E> newFilter;

		if (globalFilter != null && globalFilter.startsWith("=") &&
			globalFilterAttributes.length == 1) {
			newFilter = globalFilterAttributes[0].is(
				equalTo(globalFilter.substring(1)));
		} else {
			newFilter = EntityPredicates.createWildcardFilter(globalFilter,
				globalFilterAttributes);
		}

		allCriteria = Predicates.and(defaultCriteria, extraCriteria);
		allCriteria = Predicates.and(allCriteria, newFilter);

		try {
			firstEntity = 0;

			if (isInitialized()) {
				queryEntities();
			}
		} catch (StorageException e) {
			throw new RuntimeProcessException(this, e);
		}
	}

	/**
	 * The interface that needs to be implemented by item fragments in an
	 * {@link EntityList}.
	 *
	 * @author eso
	 */
	public interface EntityListItem<E extends Entity> {

		/**
		 * Returns the entity of this list item.
		 *
		 * @return The item entity
		 */
		E getEntity();

		/**
		 * Checks whether this item is currently selected.
		 *
		 * @return The selected state
		 */
		boolean isSelected();

		/**
		 * Sets the default style of this item. This style should always be
		 * applied if the style of an item needs to be reset.
		 *
		 * @param style The new default style
		 */
		void setDefaultStyle(String style);

		/**
		 * Sets the selected state of this item
		 *
		 * @param selected The new selected state
		 */
		void setSelected(boolean selected);

		/**
		 * Updates the entity that is displayed by this instance.
		 *
		 * @param entity The new Entity
		 */
		void updateEntity(E entity);
	}

	/**
	 * A listener interface that will be notified of entity selection.
	 *
	 * @author eso
	 */
	public interface EntitySelectionListener<E extends Entity> {

		/**
		 * Will be invoked if an entity is (de-) selected.
		 *
		 * @param entity The selected entity or NULL for none
		 */
		void onSelection(E entity);
	}

	/**
	 * Internal fragment that represents the list of entity list items.
	 *
	 * @author eso
	 */
	class EntityListItemList extends InteractionFragment {

		private static final long serialVersionUID = 1L;

		private final List<I> items = new ArrayList<>();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init() {
			layout(LayoutType.LIST);
			updateItemList();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void abort() {
		}

		/**
		 * Updates the currently displayed entities.
		 */
		void update() {
			updateItemList();

			int visibleItems = Math.min(visibleEntities.size(), pageSize);

			for (int i = 0; i < pageSize; i++) {
				I item = items.get(i);

				if (i < visibleItems) {
					item.updateEntity(visibleEntities.get(i));
					item.fragmentParam().show();
				} else {
					item.fragmentParam().hide();
				}
			}
		}

		/**
		 * Updates the list items by creating or removing item according to the
		 * current page size.
		 */
		void updateItemList() {
			int currentSize = items.size();

			if (currentSize < pageSize) {
				for (int i = currentSize; i < pageSize; i++) {
					I item = createListItem();

					items.add(item);
					item.setDefaultStyle(i % 2 == 1 ? "odd" : "even");
					addSubFragment("Entity" + i, item);
					item.fragmentParam().hide();
				}
			} else if (currentSize > pageSize) {
				for (int i = items.size() - 1; i >= pageSize; i--) {
					I item = items.remove(i);

					if (item == selectedItem) {
						setSelection(null);
					}

					removeSubFragment(item);
				}
			}
		}
	}

	/**
	 * The navigation panel of an entity list.
	 *
	 * @author eso
	 */
	class EntityListNavigation extends InteractionFragment {

		private static final long serialVersionUID = 1L;

		private Parameter<String> navPosition;

		private Parameter<String> listSizeDropDown;

		private Parameter<PagingNavigation> leftPagingButtons;

		private Parameter<PagingNavigation> rightPagingButtons;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception {
			layout(LayoutType.TABLE);

			if (allowedListSizes.size() > 1) {
				String pageSizeText = Integer.toString(pageSize);

				listSizeDropDown =
					dropDown("EntityListPageSize", allowedListSizes)
						.value(pageSizeText)
						.input()
						.onUpdate(new ValueEventHandler<String>() {
							@Override
							public void handleValueUpdate(String pageSize)
								throws Exception {
								changePageSize(pageSize);
							}
						});
			}

			leftPagingButtons =
				pagingButtons("LeftPaging", PagingNavigation.FIRST_PAGE,
					PagingNavigation.PREVIOUS_PAGE).sameRow();

			navPosition = label("").resid("EntityListPosition").sameRow();

			rightPagingButtons =
				pagingButtons("RightPaging", PagingNavigation.NEXT_PAGE,
					PagingNavigation.LAST_PAGE).sameRow();

			update();
		}

		/**
		 * Changes the number of displayed list items.
		 *
		 * @param newSize The new number of list items
		 * @throws StorageException If updating the list fails
		 */
		void changePageSize(String newSize) throws StorageException {
			pageSize = Integer.parseInt(newSize);

			if (firstEntity + pageSize > entityCount) {
				firstEntity = Math.max(0, entityCount - pageSize);
			}

			queryEntities();
		}

		/**
		 * Handles the paging navigation.
		 *
		 * @param navigation The navigation action
		 * @throws StorageException If updating the list fails
		 */
		void navigate(PagingNavigation navigation) throws StorageException {
			int max = Math.max(0, entityCount - pageSize);

			switch (navigation) {
				case FIRST_PAGE:
					firstEntity = 0;
					break;

				case PREVIOUS_PAGE:
					firstEntity = Math.max(0, firstEntity - pageSize);
					break;

				case NEXT_PAGE:
					firstEntity = Math.min(max, firstEntity + pageSize);
					break;

				case LAST_PAGE:
					firstEntity = max;
					break;
			}

			setSelection(null);
			queryEntities();
		}

		/**
		 * Updates the indicator of the current navigation position.
		 */
		@SuppressWarnings("boxing")
		void update() {
			boolean showControls = entityCount > pageSize;
			String position = "";

			if (entityCount > 5) {
				int last =
					Math.min(firstEntity + itemList.items.size(), entityCount);

				position = String.format("$$%d - %d {$lblPositionOfCount} %d",
					firstEntity + 1, last, entityCount);
			} else if (entityCount == 0) {
				position = "$lbl" + EntityList.this.getClass().getSimpleName();

				if (extraCriteria != null || globalFilter != null) {
					position += "NoMatch";
				} else {
					position += "Empty";
				}
			}

			listSizeDropDown.setVisible(showControls);
			leftPagingButtons.setVisible(showControls);
			rightPagingButtons.setVisible(showControls);
			navPosition.setVisible(entityCount == 0 || entityCount > 5);

			navPosition.value(position);
		}

		/**
		 * Creates a navigation buttons parameter for the given values.
		 *
		 * @param name          The name of the parameter
		 * @param allowedValues The allowed button values
		 * @return The new parameter
		 */
		private Parameter<PagingNavigation> pagingButtons(String name,
			PagingNavigation... allowedValues) {
			Parameter<PagingNavigation> navigation =
				param(name, PagingNavigation.class);

			return navigation
				.resid(PagingNavigation.class.getSimpleName())
				.label("")
				.buttons(allowedValues)
				.buttonStyle(ButtonStyle.ICON)
				.images()
				.set(ICON_SIZE, RelativeScale.SMALL)
				.layout(LayoutType.TABLE)
				.onAction(this::navigate);
		}
	}
}
