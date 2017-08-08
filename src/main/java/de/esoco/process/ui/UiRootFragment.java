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

import de.esoco.process.step.InteractionFragment;
import de.esoco.process.ui.container.UiRootView;
import de.esoco.process.ui.layout.UiFillLayout;


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

	//~ Instance fields --------------------------------------------------------

	private UiRootView aRootView;

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
	 * {@inheritDoc}
	 */
	@Override
	public void prepareInteraction() throws Exception
	{
		aRootView.applyProperties();
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
	 * Creates the root view of this fragment. This method can be overridden by
	 * subclasses to return a different view than the default instance: a {@link
	 * UiRootView} with the layout returned by {@link #getRootViewLayout()} that
	 * invokes {@link #buildUserInterface(UiRootView)}.
	 *
	 * @return The fragment's root view
	 */
	protected UiRootView createRootView()
	{
		return new UiRootView(this, getRootViewLayout())
		{
			@Override
			protected void build()
			{
				buildUserInterface(this);
			}
		};
	}

	/***************************************
	 * Returns the layout to be used by the root view created by the method
	 * {@link #createRootView()}. The default implementation returns an instance
	 * of {@link UiFillLayout}.
	 *
	 * @return The root view layout
	 */
	protected UiLayout getRootViewLayout()
	{
		return new UiFillLayout();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void initComplete() throws Exception
	{
		aRootView = createRootView().show();
	}
}
