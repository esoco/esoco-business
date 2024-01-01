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
import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.HasProperties;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListLayoutStyle;
import de.esoco.lib.property.SortDirection;
import de.esoco.lib.text.TextConvert;
import de.esoco.process.param.Parameter;
import de.esoco.process.step.InteractionFragment;
import de.esoco.process.ui.graphics.UiMaterialIcon;
import de.esoco.storage.StoragePredicates;
import de.esoco.storage.StoragePredicates.SortPredicate;
import org.obrel.core.RelationType;

import java.util.HashMap;
import java.util.Map;

import static de.esoco.lib.property.ContentProperties.ICON;
import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.StateProperties.NO_EVENT_PROPAGATION;
import static de.esoco.lib.property.StyleProperties.BUTTON_STYLE;
import static org.obrel.type.MetaTypes.SORT_DIRECTION;

/**
 * The base class for a header in a {@link EntityList}.
 *
 * @author eso
 */
public abstract class EntityListHeader<E extends Entity>
	extends InteractionFragment {

	private static final long serialVersionUID = 1L;

	private static final String COLUMN_BASE_STYLE = "EntityListColumn";

	private final EntityList<E, ?> entityList;

	private final Map<RelationType<?>, Parameter<String>> columnParams =
		new HashMap<>();

	private RelationType<?> currentSortColumn = null;

	/**
	 * Creates a new instance.
	 *
	 * @param entityList The entity list this header belongs to
	 */
	public EntityListHeader(EntityList<E, ?> entityList) {
		this.entityList = entityList;
	}

	/**
	 * Returns the entity list this header belongs to.
	 *
	 * @return The parent entity list
	 */
	public final EntityList<E, ?> getEntityList() {
		return entityList;
	}

	/**
	 * Returns the expand style of this header. The default implementation
	 * returns {@link ListLayoutStyle#SIMPLE} which will display a
	 * non-expandable header and the method
	 * {@link #initDataPanel(InteractionFragment)} will not be invoked.
	 * Subclasses that want to use an expandable header panel must override
	 * this
	 * method and return a different style.
	 *
	 * @return The header layout style
	 */
	public ListLayoutStyle getHeaderType() {
		return ListLayoutStyle.SIMPLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		boolean expandable = getHeaderType() != ListLayoutStyle.SIMPLE;

		layout(LayoutType.LIST_ITEM);

		panel(header -> {
			if (expandable) {
				initExpandableHeaderPanel(header);
			} else {
				buildTitlePanel(header);
			}
		});

		if (expandable) {
			panel(p -> initDataPanel(p));
		}
	}

	/**
	 * Adds a component that indicates that this header is expandable. The
	 * panel
	 * is expected to have a column grid layout and should typically be the
	 * same
	 * as the argument to {@link #initTitlePanel(InteractionFragment)}.
	 *
	 * @param headerPanel The header panel
	 * @return The parameter representing the indicator
	 */
	protected Parameter<String> addExpandableHeaderIndicator(
		InteractionFragment headerPanel) {
		return headerPanel
			.label("")
			.input()
			.sameRow(1)
			.tooltip("$ttExpandedListHeader")
			.style("ExpandableHeaderIndicator")
			.set(ICON, UiMaterialIcon.MORE_VERT.name())
			.set(BUTTON_STYLE, ButtonStyle.ICON);
	}

	/**
	 * Builds and initializes the title panel of this header.
	 *
	 * @param panel The title panel
	 */
	protected void buildTitlePanel(InteractionFragment panel) {
		initTitlePanel(panel);

		SortPredicate<? super E> sortColumn = getEntityList().getSortColumn();

		if (sortColumn != null) {
			toggleSorting((RelationType<?>) sortColumn.getElementDescriptor(),
				sortColumn.get(SORT_DIRECTION));
		}
	}

	/**
	 * Changes the active sorting column.
	 *
	 * @param sortColumn The column attribute relation type to sort on
	 */
	protected void changeSortColumn(RelationType<?> sortColumn) {
		SortDirection direction = toggleSorting(sortColumn, null);
		SortPredicate<E> sort = null;

		if (direction != null) {
			sort = StoragePredicates.sortBy(currentSortColumn, direction);
		}

		entityList.setSortColumn(sort);
	}

	/**
	 * Creates a parameter for a column title from a relation type.
	 *
	 * @param panel            The fragment of the title panel
	 * @param attr             The relation type to create the column title for
	 * @param columnProperties The column properties
	 * @return A parameter instance for the column title
	 */
	protected Parameter<String> createColumnTitle(InteractionFragment panel,
		final RelationType<?> attr, HasProperties columnProperties) {
		String columnTitle = columnProperties.getProperty(RESOURCE_ID, null);

		if (columnTitle == null) {
			columnTitle = getEntityList().getEntityType().getSimpleName() +
				TextConvert.capitalizedIdentifier(attr.getSimpleName());
		}

		columnTitle = "$lbl" + columnTitle;

		Parameter<String> titleParam = panel
			.label(columnTitle)
			.style(COLUMN_BASE_STYLE)
			.buttonStyle(ButtonStyle.LINK)
			.set(NO_EVENT_PROPAGATION)
			.onAction(v -> changeSortColumn(attr));

		columnParams.put(attr, titleParam);

		return titleParam;
	}

	/**
	 * Must be overridden to initialize the the expanded header content if the
	 * method {@link #getHeaderType()} returns an expanding header style. The
	 * data panel layout is pre-set to {@link LayoutType#GRID}.
	 *
	 * @param contentPanel headerPanel The content panel fragment
	 */
	protected void initDataPanel(InteractionFragment contentPanel) {
	}

	/**
	 * Will be invoked to init the wrapping header panel if this header is
	 * expandable.
	 *
	 * @param header The header panel
	 */
	protected void initExpandableHeaderPanel(InteractionFragment header) {
		header.layout(LayoutType.HEADER);
		header.panel(p -> buildTitlePanel(p));
	}

	/**
	 * Must be implemented to initialize the fragment containing the title
	 * panel
	 * of this instance. The panel layout is set to {@link LayoutType#GRID}
	 * which can be overridden.
	 *
	 * @param headerPanel The header panel fragment
	 */
	protected abstract void initTitlePanel(InteractionFragment headerPanel);

	/**
	 * Toggles the sorting of a certain column attribute by switching through
	 * the states ascending, descending, and no sorting. This will change the
	 * style of the sort column header accordingly.
	 *
	 * @param sortColumn The new sort column or NULL for no sorting
	 * @param direction  The sort direction to set or NULL to switch an
	 *                      existing
	 *                   direction
	 * @return The current style of the sorted column (NULL for not sorted)
	 */
	protected SortDirection toggleSorting(RelationType<?> sortColumn,
		SortDirection direction) {
		if (currentSortColumn != null && currentSortColumn != sortColumn) {
			Parameter<String> currentColumnParam =
				columnParams.get(currentSortColumn);

			currentColumnParam.style(COLUMN_BASE_STYLE);
			currentColumnParam.set(SORT_DIRECTION, null);
		}

		currentSortColumn = sortColumn;

		Parameter<String> columnParam = columnParams.get(currentSortColumn);
		String style = COLUMN_BASE_STYLE;

		if (direction == null) {
			direction = columnParam.get(SORT_DIRECTION);

			if (direction == null) {
				direction = SortDirection.ASCENDING;
			} else if (direction == SortDirection.ASCENDING) {
				direction = SortDirection.DESCENDING;
			} else {
				direction = null;
			}
		}

		if (direction != null) {
			style += " sort " + direction.name().toLowerCase();
		}

		columnParam.style(style);
		columnParam.set(SORT_DIRECTION, direction);

		return direction;
	}
}
