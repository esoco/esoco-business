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

import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListLayoutStyle;
import de.esoco.lib.property.SortDirection;
import de.esoco.lib.text.TextConvert;

import de.esoco.process.Parameter;
import de.esoco.process.step.InteractionFragment;

import de.esoco.storage.StoragePredicates;
import de.esoco.storage.StoragePredicates.SortPredicate;

import java.util.HashMap;
import java.util.Map;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.StateProperties.NO_EVENT_PROPAGATION;


/********************************************************************
 * The base class for a header in a {@link EntityList}.
 *
 * @author eso
 */
public abstract class EntityListHeader<E extends Entity>
	extends InteractionFragment
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	private static final String COLUMN_BASE_STYLE = "EntityListColumn";

	//~ Instance fields --------------------------------------------------------

	private final EntityList<E, ?> rEntityList;

	private RelationType<?> rCurrentSortColumn = null;

	private Map<RelationType<?>, Parameter<String>> aColumnParams =
		new HashMap<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rEntityList The entity list this header belongs to
	 */
	public EntityListHeader(EntityList<E, ?> rEntityList)
	{
		this.rEntityList = rEntityList;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public final EntityList<E, ?> getEntityList()
	{
		return rEntityList;
	}

	/***************************************
	 * Returns the expand style of this header. The default implementation
	 * returns {@link ListLayoutStyle#SIMPLE} which will display a
	 * non-expandable header and the method {@link
	 * #initDataPanel(InteractionFragment)} will not be invoked. Subclasses that
	 * want to use an expandable header panel must override this method and
	 * return a different style.
	 *
	 * @return The header layout style
	 */
	public ListLayoutStyle getHeaderType()
	{
		return ListLayoutStyle.SIMPLE;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		layout(LayoutType.LIST_ITEM);

		panel(p ->
  			{
  				initTitlePanel(p);

  				SortPredicate<? super E> pSortColumn =
  					getEntityList().getSortColumn();

  				if (pSortColumn != null)
  				{
  					toggleSorting((RelationType<?>) pSortColumn
  								  .getElementDescriptor());
  				}
			  });

		if (getHeaderType() != ListLayoutStyle.SIMPLE)
		{
			panel(p -> initDataPanel(p));
		}
	}

	/***************************************
	 * Must be implemented to initialize the fragment containing the title panel
	 * of this instance. The panel layout is set to {@link LayoutType#GRID}
	 * which can be overridden.
	 *
	 * @param rHeaderPanel The header panel fragment
	 */
	protected abstract void initTitlePanel(InteractionFragment rHeaderPanel);

	/***************************************
	 * Creates a parameter for a column title from a relation type.
	 *
	 * @param  rPanel The fragment of the title panel
	 * @param  rAttr  The relation type to create the column title for
	 *
	 * @return A parameter instance for the column title
	 */
	protected Parameter<String> createColumnTitle(
		InteractionFragment   rPanel,
		final RelationType<?> rAttr)
	{
		String sAttrName = rAttr.getSimpleName();

		String sColumnTitle =
			"$lbl" + getEntityList().getEntityType().getSimpleName() +
			TextConvert.capitalizedIdentifier(sAttrName);

		Parameter<String> aColumnTitle =
			rPanel.label(sColumnTitle)
				  .style(COLUMN_BASE_STYLE)
				  .buttonStyle(ButtonStyle.LINK)
				  .set(NO_EVENT_PROPAGATION)
				  .onAction(v -> changeSortColumn(rAttr));

		aColumnParams.put(rAttr, aColumnTitle);

		return aColumnTitle;
	}

	/***************************************
	 * Must be overridden to initialize the the expanded header content if the
	 * method {@link #getHeaderType()} returns an expanding header style. The
	 * data panel layout is pre-set to {@link LayoutType#GRID}.
	 *
	 * @param rContentPanel rHeaderPanel The content panel fragment
	 */
	protected void initDataPanel(InteractionFragment rContentPanel)
	{
	}

	/***************************************
	 * Toggles the sorting of a certain column attribute by switching through
	 * the states ascending, descending, and no sorting. This will change the
	 * style of the sort column header accordingly.
	 *
	 * @param  rSortColumn The new sort column or NULL for no sorting
	 *
	 * @return The current style of the sorted column (NULL for not sorted)
	 */
	protected SortDirection toggleSorting(RelationType<?> rSortColumn)
	{
		if (rCurrentSortColumn != null && rCurrentSortColumn != rSortColumn)
		{
			aColumnParams.get(rCurrentSortColumn).style(COLUMN_BASE_STYLE);
		}

		rCurrentSortColumn = rSortColumn;

		Parameter<String> rColumnParam = aColumnParams.get(rCurrentSortColumn);
		String			  sStyle	   = rColumnParam.style();
		SortDirection     eDirection   = null;

		if (COLUMN_BASE_STYLE.equals(sStyle))
		{
			eDirection = SortDirection.ASCENDING;
			sStyle     = COLUMN_BASE_STYLE + " sort ascending";
		}
		else if (sStyle.endsWith("ascending"))
		{
			eDirection = SortDirection.DESCENDING;
			sStyle     = COLUMN_BASE_STYLE + " sort descending";
		}
		else
		{
			sStyle = COLUMN_BASE_STYLE;
		}

		rColumnParam.style(sStyle);

		return eDirection;
	}

	/***************************************
	 * Changes the active sorting column.
	 *
	 * @param rSortColumn The column attribute relation type to sort on
	 */
	private void changeSortColumn(RelationType<?> rSortColumn)
	{
		SortDirection    eDirection = toggleSorting(rSortColumn);
		SortPredicate<E> pSort	    = null;

		if (eDirection != null)
		{
			pSort =
				StoragePredicates.sortBy(rCurrentSortColumn,
										 eDirection == SortDirection.ASCENDING);
		}

		rEntityList.setSortColumn(pSort);
	}
}
