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

import de.esoco.entity.Entity;
import de.esoco.entity.EntityDataProvider;

import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;

import de.esoco.process.ui.UiContainer;

import de.esoco.storage.StoragePredicates;

import org.obrel.core.RelationType;


/********************************************************************
 * A paging table list that displays the
 *
 * @author eso
 */
public class UiEntityTableList<E extends Entity> extends UiPagingTableList<E>
{
	//~ Instance fields --------------------------------------------------------

	private EntityDataProvider<E> aEntityProvider;

	private String				   sGlobalFilter		   = null;
	private RelationType<String>[] aGlobalFilterAttributes;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new simple, non-expanding entity list.
	 *
	 * @param rParent          The parent container
	 * @param rEntityType      The class of the entity type to display
	 * @param pDefaultCriteria The default criteria or NULL for none
	 */
	public UiEntityTableList(UiContainer<?>		  rParent,
							 Class<E>			  rEntityType,
							 Predicate<? super E> pDefaultCriteria)
	{
		this(rParent, rEntityType, pDefaultCriteria, null);
	}

	/***************************************
	 * Creates a new expanding entity list.
	 *
	 * @param rParent          The parent container
	 * @param rEntityType      The class of the entity type to display
	 * @param pDefaultCriteria Default criteria that are always applied or NULL
	 *                         for none
	 * @param eExpandStyle     The expand style
	 */
	public UiEntityTableList(UiContainer<?>		  rParent,
							 Class<E>			  rEntityType,
							 Predicate<? super E> pDefaultCriteria,
							 ExpandableTableStyle eExpandStyle)
	{
		super(rParent, eExpandStyle);

		aEntityProvider =
			new EntityDataProvider<>(rEntityType, pDefaultCriteria);

		setData(aEntityProvider);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Changes the default criteria and updates the displayed data.
	 *
	 * @param pCriteria The new default criteria
	 */
	public void changeDefaultCriteria(Predicate<? super E> pCriteria)
	{
		aEntityProvider.setDefaultCriteria(pCriteria);
		update();
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
	 * Sets a global filter string for this list. This will apply the filter to
	 * all attributes set with {@link
	 * #setGlobalFilterAttributes(RelationType...)}.
	 *
	 * @param sFilter The filter string or NULL or empty for no filter
	 */
	public void setGlobalFilter(String sFilter)
	{
		sGlobalFilter = sFilter.length() > 0 ? sFilter : null;
		update();
	}

	/***************************************
	 * Sets the attributes to be considered by the global filter.
	 *
	 * @param rAttributes The new filter attributes
	 */
	@SafeVarargs
	public final void setGlobalFilterAttributes(
		RelationType<String>... rAttributes)
	{
		this.aGlobalFilterAttributes = rAttributes;
	}

	/***************************************
	 * Adds the global filter criteria (if such exist) to the given predicate.
	 *
	 * @param  pToCriteria The predicate to add the criteria to
	 *
	 * @return The resulting predicate
	 */
	private Predicate<? super E> addGlobalFilter(
		Predicate<? super E> pToCriteria)
	{
		if (sGlobalFilter != null &&
			!sGlobalFilter.isEmpty() &&
			aGlobalFilterAttributes != null)
		{
			Predicate<? super E> pAttrCriteria = null;

			Predicate<Object> pLikeFilter =
				StoragePredicates.createLikeFilter(sGlobalFilter);

			for (RelationType<String> rFilterAttr : aGlobalFilterAttributes)
			{
				pAttrCriteria =
					Predicates.or(pAttrCriteria, rFilterAttr.is(pLikeFilter));
			}

			pToCriteria = Predicates.and(pToCriteria, pAttrCriteria);
		}

		return pToCriteria;
	}
}
