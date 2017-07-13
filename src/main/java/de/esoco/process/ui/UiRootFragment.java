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
package de.esoco.process.ui;

import de.esoco.lib.property.LayoutType;

import de.esoco.process.step.InteractionFragment;
import de.esoco.process.ui.container.UiRootView;


/********************************************************************
 * An interactive process fragment that renders UI components (subclasses of
 * {@link UiComponent}).
 *
 * @author eso
 */
public abstract class UiRootFragment extends InteractionFragment
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public UiRootFragment()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		// not needed
	}

	/***************************************
	 * Can be implemented by subclasses to build the application UI in the given
	 * root view after it has been created by {@link #createRootView()}.
	 *
	 * @param rRootView The root view
	 */
	protected void buildUserInterface(UiRootView rRootView)
	{
	}

	/***************************************
	 * Creates the root view of this fragment. Can be overridden by subclasses
	 * to return a different view than the default instance (a {@link
	 * UiRootView} with a fill layout that invokes {@link
	 * #buildUserInterface(UiRootView)}).
	 *
	 * @return The fragment's root view
	 */
	protected UiRootView createRootView()
	{
		return new UiRootView(this, new UiLayout(LayoutType.FILL))
		{
			@Override
			protected void build()
			{
				buildUserInterface(this);
			}
		};
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void initComplete() throws Exception
	{
		createRootView();
	}
}
