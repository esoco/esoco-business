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

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.LayoutProperties;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.property.ViewDisplayType;

import java.util.List;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.StyleProperties.AUTO_HIDE;

import static de.esoco.process.ProcessRelationTypes.VIEW_PARAMS;


/********************************************************************
 * A view that is displayed as a child of another view.
 *
 * @author eso
 */
public abstract class UiChildView<V extends UiChildView<V>> extends UiView<V>
{
	//~ Instance fields --------------------------------------------------------

	private Runnable rCloseHandler;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance and shows it.
	 *
	 * @see UiView#UiView(UiView, UiLayout)
	 */
	public UiChildView(UiView<?>	   rParent,
					   UiLayout		   rLayout,
					   ViewDisplayType eViewType)
	{
		super(rParent, rLayout);

		getParent().fragment().addViewFragment(type(), fragment());
		fragment().get(VIEW_PARAMS).remove(type());

		setViewType(eViewType);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Enables automatic hiding of this view if the user clicks outside.
	 *
	 * @return This instance
	 */
	public V autoHide()
	{
		setParameterEventHandler(InteractionEventType.UPDATE,
								 v -> handleCloseView());

		return set(AUTO_HIDE);
	}

	/***************************************
	 * Indicates that this view should be centered on the screen.
	 *
	 * @return This instance
	 */
	public V center()
	{
		return set(UserInterfaceProperties.VERTICAL_ALIGN, Alignment.CENTER);
	}

	/***************************************
	 * Adds a handler that will be invoked when this view is closed.
	 *
	 * @param  rCloseHandler The handler to be invoked if the view is closed
	 *
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public V onClose(Runnable rCloseHandler)
	{
		this.rCloseHandler = rCloseHandler;

		return (V) this;
	}

	/***************************************
	 * Overridden to show or hide this view.
	 *
	 * @see UiContainer#setVisible(boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public V setVisible(boolean bVisible)
	{
		RelationType<List<RelationType<?>>> rViewParam = type();

		if (bVisible)
		{
			fragment().get(VIEW_PARAMS).add(rViewParam);
			applyProperties();
		}
		else
		{
			getParent().fragment().removeViewFragment(rViewParam);
		}

		return (V) this;
	}

	/***************************************
	 * Overridden to do nothing because child views are managed separately from
	 * components.
	 *
	 * @see UiContainer#attachTo(UiContainer)
	 */
	@Override
	protected void attachTo(UiContainer<?> rParent)
	{
	}

	/***************************************
	 * Set the type of this view
	 *
	 * @param eViewType The view type
	 */
	protected void setViewType(ViewDisplayType eViewType)
	{
		set(LayoutProperties.VIEW_DISPLAY_TYPE, eViewType);
	}

	/***************************************
	 * Handles close events for this view.
	 */
	private void handleCloseView()
	{
		if (rCloseHandler != null)
		{
			rCloseHandler.run();
		}

		Boolean rAutoHide = get(AUTO_HIDE);

		if (rAutoHide != null && rAutoHide.booleanValue())
		{
			hide();
		}
	}
}
