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
import de.esoco.process.ui.container.RootView;


/********************************************************************
 * An interactive process fragment that renders UI components (subclasses of
 * {@link Component}).
 *
 * @author eso
 */
public abstract class UiFragment extends InteractionFragment
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public UiFragment()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		layout(LayoutType.INLINE);
	}

	/***************************************
	 * Must be implemented by subclasses to build the application UI in the
	 * given root view.
	 *
	 * @param rRootView The root view
	 */
	protected abstract void buildUserInterface(RootView rRootView);

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void initComplete() throws Exception
	{
		buildUserInterface(new RootView(this));
	}
}
