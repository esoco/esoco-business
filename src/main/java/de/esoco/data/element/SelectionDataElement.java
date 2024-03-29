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
package de.esoco.data.element;

import de.esoco.data.validate.SelectionValidator;
import de.esoco.data.validate.Validator;
import de.esoco.lib.model.ColumnDefinition;
import de.esoco.lib.property.PropertyName;

import java.util.List;
import java.util.Set;

/**
 * A data element that stores information about a selection. The element value
 * is the identifier of the selected object.
 *
 * @author eso
 */
public class SelectionDataElement extends StringDataElement {

	/**
	 * A constant for the value of this element if no value is currently
	 * selected.
	 */
	public static final String NO_SELECTION = "-1";

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance with no initial selection and a
	 * {@link SelectionValidator} that is initialized with the given data and
	 * columns.
	 *
	 * @param name    The name of this element
	 * @param data    The data objects to be displayed and selected from
	 * @param columns The data columns to be displayed
	 */
	public SelectionDataElement(String name, List<HierarchicalDataObject> data,
		List<ColumnDefinition> columns) {
		this(name, NO_SELECTION, new SelectionValidator(data, columns), null);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param name             The name of this element
	 * @param initialSelection The initial selection ID
	 * @param validator        The validator defining the selectable elements
	 * @param flags            The optional flags for this data element
	 */
	public SelectionDataElement(String name, String initialSelection,
		Validator<? super String> validator, Set<Flag> flags) {
		super(name, initialSelection, validator, flags);
	}

	/**
	 * Default constructor for serialization.
	 */
	SelectionDataElement() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SelectionDataElement copy(CopyMode mode,
		PropertyName<?>... copyProperties) {
		return (SelectionDataElement) super.copy(mode, copyProperties);
	}

	/**
	 * Returns the integer index of the current selection.
	 *
	 * @return The selection index (-1 for no selection)
	 */
	public int getSelectionIndex() {
		return Integer.parseInt(getValue());
	}

	/**
	 * Checks whether this instance has a selection by comparing the value with
	 * the {@link #NO_SELECTION} constant.
	 *
	 * @return TRUE if this instance has a valid selection
	 */
	public boolean hasSelection() {
		return !NO_SELECTION.equals(getValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SelectionDataElement newInstance() {
		return new SelectionDataElement();
	}
}
