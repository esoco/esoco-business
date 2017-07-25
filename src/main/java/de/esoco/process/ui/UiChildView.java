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

import de.esoco.process.ViewFragment;


/********************************************************************
 * A view that is displayed as a child of another view.
 *
 * @author eso
 */
public abstract class UiChildView<V extends UiChildView<V>> extends UiView<V>
{
	//~ Instance fields --------------------------------------------------------

	private ViewFragment rViewFragment;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance and shows it.
	 *
	 * @see UiView#UiView(UiView, UiLayout)
	 */
	public UiChildView(UiView<?> rParent, UiLayout rLayout)
	{
		super(rParent, rLayout);

		rViewFragment = getParent().fragment().showView("", fragment(), false);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to show or hide this view.
	 *
	 * @see UiContainer#setVisible(boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public V setVisible(boolean bVisible)
	{
		if (bVisible)
		{
			applyProperties();
		}
		else if (rViewFragment != null)
		{
			rViewFragment.hide();
			rViewFragment = null;
		}

		return (V) this;
	}

	/***************************************
	 * Overridden to setup the container fragment and to attach it to the parent
	 * fragment.
	 *
	 * @see UiComponent#attachTo(UiContainer)
	 */
	@Override
	protected void attachTo(UiContainer<?> rParent)
	{
		setupContainerFragment(rParent);
	}
}
