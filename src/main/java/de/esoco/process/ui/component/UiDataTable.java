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

	private List<ColumnDefinition> aColumns;

	private List<HierarchicalDataObject> aTableData;

	/**
	 * Creates a new instance.
	 *
	 * @param rContainer The parent container
	 */
	public UiDataTable(UiContainer<?> rContainer) {
		super(rContainer, SelectionDataElement.class);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param rContainer The parent container
	 * @param rData      The table data
	 * @param rColumns   The table columns
	 */
	public UiDataTable(UiContainer<?> rContainer,
		Collection<HierarchicalDataObject> rData,
		Collection<ColumnDefinition> rColumns) {
		this(rContainer);

		setData(rData);
		setColumns(rColumns);
	}

	/**
	 * Returns the currently selected data object or NULL for none.
	 *
	 * @return The current selection (NULL for none)
	 */
	public HierarchicalDataObject getSelection() {
		int nSelection = getValueImpl().getSelectionIndex();

		return nSelection >= 0 ? aTableData.get(nSelection) : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UiDataTable onAction(Consumer<SelectionDataElement> rEventHandler) {
		return setParameterEventHandler(InteractionEventType.ACTION,
			v -> rEventHandler.accept(v));
	}

	/**
	 * Sets the event handler for selection events of this table. The handler
	 * will receive the currently selected data object or NULL if no object is
	 * selected.
	 *
	 * @param rEventHandler The event handler
	 * @return This instance for concatenation
	 */
	public final UiDataTable onSelection(
		Consumer<HierarchicalDataObject> rEventHandler) {
		return setParameterEventHandler(InteractionEventType.UPDATE,
			v -> rEventHandler.accept(getSelection()));
	}

	/**
	 * Sets the event handler for selection confirmed events (e.g. by double
	 * click) of this table. The handler will receive the currently selected
	 * data object or NULL if no object is selected.
	 *
	 * @param rEventHandler The event handler
	 * @return This instance for concatenation
	 */
	public final UiDataTable onSelectionConfirmed(
		Consumer<HierarchicalDataObject> rEventHandler) {
		return setParameterEventHandler(InteractionEventType.ACTION,
			v -> rEventHandler.accept(getSelection()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UiDataTable onUpdate(Consumer<SelectionDataElement> rEventHandler) {
		return setParameterEventHandler(InteractionEventType.UPDATE,
			v -> rEventHandler.accept(v));
	}

	/**
	 * Sets the table columns.
	 *
	 * @param rColumns The table column definitions
	 */
	public void setColumns(Collection<ColumnDefinition> rColumns) {
		aColumns = rColumns != null ? new ArrayList<>(rColumns) : null;
		setValueImpl(null);
	}

	/**
	 * Sets the table data as a list of hierarchical data objects that will be
	 * rendered as the table rows.
	 *
	 * @param rData A list of table data objects
	 */
	public void setData(Collection<HierarchicalDataObject> rData) {
		aTableData = rData != null ? new ArrayList<>(rData) : null;
		setValueImpl(null);
	}

	/**
	 * @see UiTableControl#applyProperties()
	 */
	@Override
	protected void applyProperties() {
		if (getValueImpl() == null) {
			if (aTableData == null) {
				throw new IllegalStateException("No table data");
			}

			if (aColumns == null) {
				throw new IllegalStateException("No colums");
			}

			setValueImpl(new SelectionDataElement(type().getName(), aTableData,
				aColumns));
		}

		super.applyProperties();
	}
}
