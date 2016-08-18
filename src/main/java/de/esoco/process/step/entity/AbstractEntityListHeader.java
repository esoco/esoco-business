//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.expression.function.Initializer;
import de.esoco.lib.property.Layout;

import de.esoco.process.step.InteractionFragment;


/********************************************************************
 * A single item in the list of assets.
 *
 * @author eso
 */
public abstract class AbstractEntityListHeader<E extends Entity>
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
	public AbstractEntityListHeader(EntityList<E, ?> rEntityList)
	{
		this.rEntityList = rEntityList;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		layout(Layout.LIST_ITEM);

		panel(new Initializer<InteractionFragment>()
			{
				@Override
				public void init(InteractionFragment rFragment) throws Exception
				{
					initTitlePanel(rFragment);
				}
			});
		panel(new Initializer<InteractionFragment>()
			{
				@Override
				public void init(InteractionFragment rFragment) throws Exception
				{
					initDataPanel(rFragment);
				}
			});
	}

	/***************************************
	 * Must be implemented to initialize the fragment containing the data panel
	 * of this instance. The panel layout is set to {@link Layout#GRID} which
	 * can be overridden.
	 *
	 * @param rContentPanel rHeaderPanel The content panel fragment
	 */
	protected abstract void initDataPanel(InteractionFragment rContentPanel);

	/***************************************
	 * Must be implemented to initialize the fragment containing the title panel
	 * of this instance. The panel layout is set to {@link Layout#GRID} which
	 * can be overridden.
	 *
	 * @param rHeaderPanel The header panel fragment
	 */
	protected abstract void initTitlePanel(InteractionFragment rHeaderPanel);

	/***************************************
	 * Returns the entity list this header belongs to.
	 *
	 * @return The header's entity list
	 */
	protected final EntityList<E, ?> getEntityList()
	{
		return rEntityList;
	}
}
