//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.data.validate;

import de.esoco.lib.model.ColumnDefinition;

import java.util.List;

/**
 * A base class for the validation of tabular data. The validated type is an
 * integer that represents the selected table row. Depending on the implementing
 * subclass that can either be the list index or an ID of the selected element.
 *
 * @author eso
 */
public abstract class TabularDataValidator implements Validator<String> {

	private static final long serialVersionUID = 1L;

	private List<ColumnDefinition> columns;

	/**
	 * Creates a new instance.
	 *
	 * @param columns The table columns
	 */
	public TabularDataValidator(List<ColumnDefinition> columns) {
		this.columns = columns;
	}

	/**
	 * Default constructor for serialization.
	 */
	TabularDataValidator() {
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		TabularDataValidator other = (TabularDataValidator) obj;

		return columns.equals(other.columns);
	}

	/**
	 * Returns the definitions for the table columns.
	 *
	 * @return The table column definitions
	 */
	public final List<ColumnDefinition> getColumns() {
		return columns;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 37 * columns.hashCode();
	}
}
