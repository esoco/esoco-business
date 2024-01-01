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
package de.esoco.process.ui.component;

import de.esoco.data.element.HierarchicalDataObject;
import de.esoco.data.element.SelectionDataElement;
import de.esoco.lib.model.ColumnDefinition;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiTableControl;
import de.esoco.process.ui.event.UiHasActionEvents;
import de.esoco.process.ui.event.UiHasUpdateEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * A table that displays a static collection of {@link HierarchicalDataObject}
 * that is stored in the validator of a {@link SelectionDataElement}.
 *
 * @author eso
 */
public class UiDataTable
	extends UiTableControl<SelectionDataElement, UiDataTable>
	implements UiHasUpdateEvents<SelectionDataElement, UiDataTable>,
	UiHasActionEvents<SelectionDataElement, UiDataTable> {

	private List<ColumnDefinition> columns;

	private List<HierarchicalDataObject> tableData;

	/**
	 * Creates a new instance.
	 *
	 * @param container The parent container
	 */
	public UiDataTable(UiContainer<?> container) {
		super(container, SelectionDataElement.class);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param container The parent container
	 * @param data      The table data
	 * @param columns   The table columns
	 */
	public UiDataTable(UiContainer<?> container,
		Collection<HierarchicalDataObject> data,
		Collection<ColumnDefinition> columns) {
		this(container);

		setData(data);
		setColumns(columns);
	}

	/**
	 * Returns the currently selected data object or NULL for none.
	 *
	 * @return The current selection (NULL for none)
	 */
	public HierarchicalDataObject getSelection() {
		int selection = getValueImpl().getSelectionIndex();

		return selection >= 0 ? tableData.get(selection) : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UiDataTable onAction(Consumer<SelectionDataElement> eventHandler) {
		return setParameterEventHandler(InteractionEventType.ACTION,
			v -> eventHandler.accept(v));
	}

	/**
	 * Sets the event handler for selection events of this table. The handler
	 * will receive the currently selected data object or NULL if no object is
	 * selected.
	 *
	 * @param eventHandler The event handler
	 * @return This instance for concatenation
	 */
	public final UiDataTable onSelection(
		Consumer<HierarchicalDataObject> eventHandler) {
		return setParameterEventHandler(InteractionEventType.UPDATE,
			v -> eventHandler.accept(getSelection()));
	}

	/**
	 * Sets the event handler for selection confirmed events (e.g. by double
	 * click) of this table. The handler will receive the currently selected
	 * data object or NULL if no object is selected.
	 *
	 * @param eventHandler The event handler
	 * @return This instance for concatenation
	 */
	public final UiDataTable onSelectionConfirmed(
		Consumer<HierarchicalDataObject> eventHandler) {
		return setParameterEventHandler(InteractionEventType.ACTION,
			v -> eventHandler.accept(getSelection()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UiDataTable onUpdate(Consumer<SelectionDataElement> eventHandler) {
		return setParameterEventHandler(InteractionEventType.UPDATE,
			v -> eventHandler.accept(v));
	}

	/**
	 * Sets the table columns.
	 *
	 * @param columns The table column definitions
	 */
	public void setColumns(Collection<ColumnDefinition> columns) {
		columns = columns != null ? new ArrayList<>(columns) : null;
		setValueImpl(null);
	}

	/**
	 * Sets the table data as a list of hierarchical data objects that will be
	 * rendered as the table rows.
	 *
	 * @param data A list of table data objects
	 */
	public void setData(Collection<HierarchicalDataObject> data) {
		tableData = data != null ? new ArrayList<>(data) : null;
		setValueImpl(null);
	}

	/**
	 * @see UiTableControl#applyProperties()
	 */
	@Override
	protected void applyProperties() {
		if (getValueImpl() == null) {
			if (tableData == null) {
				throw new IllegalStateException("No table data");
			}

			if (columns == null) {
				throw new IllegalStateException("No colums");
			}

			setValueImpl(
				new SelectionDataElement(type().getName(), tableData,
					columns));
		}

		super.applyProperties();
	}
}
