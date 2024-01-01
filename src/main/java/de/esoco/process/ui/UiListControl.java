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

import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.ListStyle;
import de.esoco.process.ui.event.UiHasUpdateEvents;
import org.obrel.core.RelationType;

import java.util.function.Consumer;

import static de.esoco.lib.property.StyleProperties.LIST_STYLE;

/**
 * Base class for interactive components that display a list of selectable
 * values.
 *
 * @author eso
 */
public abstract class UiListControl<T, C extends UiListControl<T, C>>
	extends UiControl<T, C> implements UiHasUpdateEvents<T, C> {

	/**
	 * Creates a new instance for an existing parameter type.
	 *
	 * @param parent    The parent container
	 * @param paramType The parameter relation type
	 * @param listStyle The list style
	 */
	public UiListControl(UiContainer<?> parent, RelationType<T> paramType,
		ListStyle listStyle) {
		super(parent, paramType);

		set(LIST_STYLE, listStyle);
	}

	/**
	 * Creates a new instance. If the datatype is an enum all enum values will
	 * be pre-set as the list values.
	 *
	 * @param parent    The parent container
	 * @param datatype  The datatype of the list values
	 * @param listStyle The list style
	 */
	public UiListControl(UiContainer<?> parent, Class<? super T> datatype,
		ListStyle listStyle) {
		super(parent, datatype);

		set(LIST_STYLE, listStyle);
	}

	/**
	 * Returns the currently selected list value.
	 *
	 * @return The selected value (NULL for none)
	 */
	public T getSelection() {
		return getValueImpl();
	}

	/**
	 * Sets the event handler for selection events of this table.
	 *
	 * @param eventHandler The event handler
	 * @return This instance for concatenation
	 */
	public final C onSelection(Consumer<T> eventHandler) {
		return setParameterEventHandler(InteractionEventType.UPDATE,
			v -> eventHandler.accept(v));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public C onUpdate(Consumer<T> eventHandler) {
		return onSelection(eventHandler);
	}

	/**
	 * Sets the selected value.
	 *
	 * @param value The new selection or NULL for none
	 * @return This instance
	 */
	public C select(T value) {
		return setValueImpl(value);
	}

	/**
	 * Sets the selected value.
	 *
	 * @param value The new selection or NULL for none
	 */
	public void setSelection(T value) {
		select(value);
	}
}
