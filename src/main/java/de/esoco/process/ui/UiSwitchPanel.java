//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.lib.property.SingleSelection;
import de.esoco.process.ui.container.UiLayoutPanel;

import java.util.function.Consumer;
import java.util.function.Function;

import static de.esoco.lib.property.ContentProperties.LABEL;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;

/**
 * A panel that contains multiple children of which only one is visible at a
 * time. The visible component can then be selected through the methods of the
 * implemented {@link SingleSelection} interface.
 *
 * @author eso
 */
public class UiSwitchPanel<P extends UiSwitchPanel<P>>
	extends UiLayoutContainer<P> implements SingleSelection {

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent container
	 * @param layout The panel layout
	 */
	public UiSwitchPanel(UiContainer<?> parent, UiLayout layout) {
		super(parent, layout);
	}

	/**
	 * Adds a panel with a particular layout as a new page of this switch
	 * panel.
	 *
	 * @param title  The page title
	 * @param layout The panel layout
	 * @return The page panel to allow further invocations
	 */
	public UiLayoutPanel addPage(String title, UiLayout layout) {
		return addPage(title, c -> c.builder().addPanel(layout));
	}

	/**
	 * Adds a component that is created by a factory function as a new page of
	 * this switch panel. The component must be a child of this container or
	 * else an exception will be thrown.
	 *
	 * @param title  The page title
	 * @param create A factory function that receives this panel as it's input
	 *               and returns a child component for the new page
	 * @return The page component to allow further invocations
	 * @throws IllegalArgumentException If the given component has a different
	 *                                  parent than this container
	 */
	public <T, V extends UiComponent<T, V>> V addPage(String title,
		Function<UiContainer<?>, ? extends V> create) {
		V pageComponent = create.apply(this).set(LABEL, title);

		assert pageComponent.getParent() == this :
			String.format("Component %s has other parent: ", pageComponent,
				pageComponent.getParent());

		return pageComponent;
	}

	/**
	 * Returns the component of the currently selected (visible) page.
	 *
	 * @return The selected component
	 */
	public UiComponent<?, ?> getSelection() {
		return getComponents().get(getSelectionIndex());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSelectionIndex() {
		Integer selectionIndex = get(CURRENT_SELECTION);

		return selectionIndex != null ? selectionIndex.intValue() : 0;
	}

	/**
	 * Sets the event handler for selection events of this panel. The event
	 * handler will receive this panel as it's argument so that it can query
	 * the
	 * current selection index or the selected page component.
	 *
	 * @param eventHandler The event handler
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("unchecked")
	public final P onSelection(Consumer<P> eventHandler) {
		return setParameterEventHandler(InteractionEventType.UPDATE,
			v -> eventHandler.accept((P) this));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("boxing")
	public void setSelection(int index) {
		set(CURRENT_SELECTION, index);
	}
}
