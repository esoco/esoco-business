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

import de.esoco.lib.property.InteractionEventType;

import de.esoco.process.ValueEventHandler;
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
	 * A shortcut to invoke {@link UiLayout#nextRow()}. This call will only work
	 * for layouts that support multiple rows of components.
	 */
	public void nextRow()
	{
		getLayout().nextRow();
	}

	/***************************************
	 * Sets the event handler for click events on this container's visible area
	 * that is not occupied by components. The handler will receive the
	 * container instance as it's argument.
	 *
	 * @param  rEventHandler The event handler
	 *
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final C onClick(ValueEventHandler<C> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.ACTION,
										v -> rEventHandler.handleValueUpdate((C)
																			 this));
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
