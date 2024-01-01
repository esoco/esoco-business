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

import de.esoco.entity.Entity;
import de.esoco.entity.EntityDataProvider;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.monad.Option;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.composite.UiListPanel.ExpandableListStyle;
import org.obrel.core.RelationType;

/**
 * A paging table list that displays the
 *
 * @author eso
 */
public class UiEntityTableList<E extends Entity> extends UiPagingTableList<E> {

	private final EntityDataProvider<E> entityProvider;

	private String globalFilter = null;

	private RelationType<String>[] globalFilterAttributes;

	/**
	 * Creates a new simple, non-expanding entity list.
	 *
	 * @param parent          The parent container
	 * @param entityType      The class of the entity type to display
	 * @param defaultCriteria The default criteria or NULL for none
	 */
	public UiEntityTableList(UiContainer<?> parent, Class<E> entityType,
		Predicate<? super E> defaultCriteria) {
		this(parent, entityType, defaultCriteria, null);
	}

	/**
	 * Creates a new expanding entity list.
	 *
	 * @param parent          The parent container
	 * @param entityType      The class of the entity type to display
	 * @param defaultCriteria Default criteria that are always applied or NULL
	 *                        for none
	 * @param expandStyle     The expand style
	 */
	public UiEntityTableList(UiContainer<?> parent, Class<E> entityType,
		Predicate<? super E> defaultCriteria,
		Option<ExpandableListStyle> expandStyle) {
		super(parent, expandStyle);

		entityProvider = new EntityDataProvider<>(entityType, defaultCriteria);

		setData(entityProvider);
	}

	/**
	 * Changes the default criteria and updates the displayed data.
	 *
	 * @param criteria The new default criteria
	 */
	public void changeDefaultCriteria(Predicate<? super E> criteria) {
		entityProvider.setDefaultCriteria(criteria);
		update();
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
	 * Sets a global filter string for this list. This will apply the filter to
	 * all attributes set with
	 * {@link #setGlobalFilterAttributes(RelationType...)}.
	 *
	 * @param filter The filter string or NULL or empty for no filter
	 */
	public void setGlobalFilter(String filter) {
		globalFilter = filter.length() > 0 ? filter : null;

		entityProvider.setWildcardFilter(globalFilter, globalFilterAttributes);
		update();
	}

	/**
	 * Sets the attributes to be considered by the global filter.
	 *
	 * @param attributes The new filter attributes
	 */
	@SafeVarargs
	public final void setGlobalFilterAttributes(
		RelationType<String>... attributes) {
		this.globalFilterAttributes = attributes;

		entityProvider.setWildcardFilter(globalFilter, globalFilterAttributes);
	}
}
