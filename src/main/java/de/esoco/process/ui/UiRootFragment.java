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
package de.esoco.process.ui;

import de.esoco.process.step.InteractionFragment;
import de.esoco.process.ui.container.UiBuilder;
import de.esoco.process.ui.layout.UiFillLayout;
import de.esoco.process.ui.view.UiRootView;

import java.util.function.Consumer;


/********************************************************************
 * An interactive process fragment that renders UI components (subclasses of
 * {@link UiComponent}). This class can be used either as a base class by
 * overriding {@link #buildUserInterface(UiRootView)} and {@link
 * #getRootViewLayout()} or {@link #createRootView()}. Or it can be used by
 * providing the root view layout and a builder function through the constructor
 * {@link #UiRootFragment(UiLayout, Consumer)}.
 *
 * @author eso
 */
public class UiRootFragment extends InteractionFragment
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private UiLayout			   rRootViewLayout;
	private Consumer<UiBuilder<?>> fRootViewBuilder;
	private UiRootView			   aRootView;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a fill layout for the root view.
	 */
	public UiRootFragment()
	{
		this(new UiFillLayout());
	}

	/***************************************
	 * Creates a new instance with a specific root view layout.
	 *
	 * @param rLayout The root view layout
	 */
	public UiRootFragment(UiLayout rLayout)
	{
		rRootViewLayout = rLayout;
	}

	/***************************************
	 * Creates a new instance with a specific root view layout and a root view
	 * builder function. The builder function will be invoked from the method
	 * {@link #buildUserInterface(UiRootView)}.
	 *
	 * @param rLayout  The root view layout
	 * @param fBuilder The function that builds the root view
	 */
	public UiRootFragment(UiLayout rLayout, Consumer<UiBuilder<?>> fBuilder)
	{
		rRootViewLayout  = rLayout;
		fRootViewBuilder = fBuilder;
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
	 * Can be overridden by subclasses to build the application UI in the given
	 * root view after it has been created by {@link #createRootView()}. The
	 * default application invokes the root view builder function if it has been
	 * set with {@link #UiRootFragment(UiLayout, Consumer)}.
	 *
	 * @param rRootView The root view
	 */
	protected void buildUserInterface(UiRootView rRootView)
	{
		if (fRootViewBuilder != null)
		{
			fRootViewBuilder.accept(rRootView.builder());
		}
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
		UiLayout rViewLayout = getRootViewLayout();

		// make sure that the layout will be applied. This needed for fragments
		// that invoked directly as process steps if process navigation occurs
		rViewLayout.setModified(true);

		UiRootView aRootView =
			new UiRootView(this, rViewLayout)
			{
				@Override
				protected void build()
				{
					buildUserInterface(this);
				}
			};

		return aRootView;
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
		return rRootViewLayout;
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
