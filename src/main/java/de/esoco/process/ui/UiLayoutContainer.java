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

import de.esoco.process.ui.container.UiBuilder;


/********************************************************************
 * The base class for containers that expose layout functionality through
 * generic layout methods.
 *
 * @author eso
 */
public abstract class UiLayoutContainer<C extends UiLayoutContainer<C>>
	extends UiContainer<C>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param eLayout The layout of this panel
	 */
	public UiLayoutContainer(UiContainer<?> rParent, UiLayout eLayout)
	{
		super(rParent, eLayout);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to be public.
	 *
	 * @see UiContainer#builder()
	 */
	@Override
	public UiBuilder<C> builder()
	{
		return super.builder();
	}

	/***************************************
	 * Overridden to be public.
	 *
	 * @see UiContainer#getLayout()
	 */
	@Override
	public final UiLayout getLayout()
	{
		return super.getLayout();
	}

	/***************************************
	 * Overridden to be public.
	 *
	 * @see UiContainer#removeComponent(UiComponent)
	 */
	@Override
	public void removeComponent(UiComponent<?, ?> rComponent)
	{
		super.removeComponent(rComponent);
	}
}
