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
package de.esoco.process.ui.container;

import de.esoco.process.step.InteractionFragment;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiRootFragment;
import de.esoco.process.ui.layout.UiInlineLayout;


/********************************************************************
 * A UI container that wraps an existing process interaction fragment. This
 * allows UI components to be combined with fragments similar to the way that
 * {@link UiRootFragment} integrates Ui component hierarchies into process
 * interactions.
 *
 * @author eso
 */
public class UiSubFragment extends UiContainer<UiSubFragment>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent   The parent container
	 * @param rFragment The fragment to wrap
	 */
	public UiSubFragment(UiContainer<?> rParent, InteractionFragment rFragment)
	{
		// UiContainer requires a layout; this is typically overwritten by the
		// fragment during it's initialization, so we use just an inline layout
		super(rParent, new UiInlineLayout());

		setFragment(rFragment);
	}
}
