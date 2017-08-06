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

/********************************************************************
 * A base class for components that are build from the combination of other
 * components. A composite is always a container with a certain layout in which
 * it's
 *
 * @author eso
 */
public abstract class UiComposite<C extends UiComposite<C>>
	extends UiContainer<C>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param eLayout The layout of this panel
	 */
	public UiComposite(UiContainer<?> rParent, UiLayout eLayout)
	{
		super(rParent, eLayout);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to be abstract because it must always be implemented to create
	 * the content of this composite.
	 *
	 * @see UiContainer#build()
	 */
	@Override
	protected abstract void build();
}
