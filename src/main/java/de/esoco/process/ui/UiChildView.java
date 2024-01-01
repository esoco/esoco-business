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
import org.obrel.core.RelationType;

import java.util.List;

import static de.esoco.lib.property.StyleProperties.AUTO_HIDE;
import static de.esoco.process.ProcessRelationTypes.VIEW_PARAMS;

/**
 * A view that is displayed as a child of another view.
 *
 * @author eso
 */
public abstract class UiChildView<V extends UiChildView<V>> extends UiView<V> {

	private Runnable closeHandler;

	/**
	 * Creates a new instance and shows it.
	 *
	 * @see UiView#UiView(UiView, UiLayout)
	 */
	public UiChildView(UiView<?> parent, UiLayout layout,
		ViewDisplayType viewType) {
		super(parent, layout);

		getParent().fragment().addViewFragment(type(), fragment());
		fragment().get(VIEW_PARAMS).remove(type());

		setViewType(viewType);
	}

	/**
	 * Enables automatic hiding of this view if the user clicks outside.
	 *
	 * @return This instance
	 */
	public V autoHide() {
		setParameterEventHandler(InteractionEventType.UPDATE,
			v -> handleCloseView());

		return set(AUTO_HIDE);
	}

	/**
	 * Indicates that this view should be centered on the screen.
	 *
	 * @return This instance
	 */
	public V center() {
		return set(UserInterfaceProperties.VERTICAL_ALIGN, Alignment.CENTER);
	}

	/**
	 * Adds a handler that will be invoked when this view is closed.
	 *
	 * @param closeHandler The handler to be invoked if the view is closed
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public V onClose(Runnable closeHandler) {
		this.closeHandler = closeHandler;

		return (V) this;
	}

	/**
	 * Overridden to show or hide this view.
	 *
	 * @see UiContainer#setVisible(boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public V setVisible(boolean visible) {
		RelationType<List<RelationType<?>>> viewParam = type();

		if (visible) {
			fragment().get(VIEW_PARAMS).add(viewParam);
			applyProperties();
		} else {
			getParent().fragment().removeViewFragment(viewParam);
		}

		return (V) this;
	}

	/**
	 * Overridden to do nothing because child views are managed separately from
	 * components.
	 *
	 * @see UiContainer#attachTo(UiContainer)
	 */
	@Override
	protected void attachTo(UiContainer<?> parent) {
	}

	/**
	 * Set the type of this view
	 *
	 * @param viewType The view type
	 */
	protected void setViewType(ViewDisplayType viewType) {
		set(LayoutProperties.VIEW_DISPLAY_TYPE, viewType);
	}

	/**
	 * Handles close events for this view.
	 */
	private void handleCloseView() {
		if (closeHandler != null) {
			closeHandler.run();
		}

		Boolean autoHide = get(AUTO_HIDE);

		if (autoHide != null && autoHide.booleanValue()) {
			hide();
		}
	}
}
