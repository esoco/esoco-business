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

import java.util.function.Consumer;

import org.obrel.core.RelationType;

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
	 * @param rParent    The parent container
	 * @param rParamType The parameter relation type
	 * @param eListStyle The list style
	 */
	public UiListControl(UiContainer<?> rParent, RelationType<T> rParamType,
		ListStyle eListStyle) {
		super(rParent, rParamType);

		set(LIST_STYLE, eListStyle);
	}

	/**
	 * Creates a new instance. If the datatype is an enum all enum values will
	 * be pre-set as the list values.
	 *
	 * @param rParent    The parent container
	 * @param rDatatype  The datatype of the list values
	 * @param eListStyle The list style
	 */
	public UiListControl(UiContainer<?> rParent, Class<? super T> rDatatype,
		ListStyle eListStyle) {
		super(rParent, rDatatype);

		set(LIST_STYLE, eListStyle);
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
	 * @param rEventHandler The event handler
	 * @return This instance for concatenation
	 */
	public final C onSelection(Consumer<T> rEventHandler) {
		return setParameterEventHandler(InteractionEventType.UPDATE,
			v -> rEventHandler.accept(v));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public C onUpdate(Consumer<T> rEventHandler) {
		return onSelection(rEventHandler);
	}

	/**
	 * Sets the selected value.
	 *
	 * @param rValue The new selection or NULL for none
	 * @return This instance
	 */
	public C select(T rValue) {
		return setValueImpl(rValue);
	}

	/**
	 * Sets the selected value.
	 *
	 * @param rValue The new selection or NULL for none
	 */
	public void setSelection(T rValue) {
		select(rValue);
	}
}
