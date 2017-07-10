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
package de.esoco.process.ui.container;

import de.esoco.process.step.InteractionFragment;
import de.esoco.process.ui.Layout;
import de.esoco.process.ui.View;


/********************************************************************
 * The root view of an application UI. All other components or the UI must be
 * (direct or indirect) children of a root view.
 *
 * @author eso
 */
public class RootView extends View<RootView>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rFragment The fragment this view shall be rendered in
	 * @param rLayout   The view layout
	 */
	public RootView(InteractionFragment rFragment, Layout rLayout)
	{
		super(null, rLayout);

		setFragment(rFragment);
		setParameterType(rFragment.getFragmentParameter());
		applyLayout();
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to be public so that it may be invoked externally on root
	 * views.
	 */
	@Override
	public void finishSetup()
	{
		super.finishSetup();
	}
}
