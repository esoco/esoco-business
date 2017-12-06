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

import de.esoco.process.step.InteractionFragment;


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

	//~ Instance fields --------------------------------------------------------

	private final EntityList<E, ?> rEntityList;

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

		panel(p -> initTitlePanel(p));

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
	 * Must be overridden to initialize the the expanded header content if the
	 * method {@link #getHeaderType()} returns an expanding header style. The
	 * data panel layout is pre-set to {@link LayoutType#GRID}.
	 *
	 * @param rContentPanel rHeaderPanel The content panel fragment
	 */
	protected void initDataPanel(InteractionFragment rContentPanel)
	{
	}
}
