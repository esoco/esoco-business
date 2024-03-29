//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.data.element.HierarchicalDataObject;

import java.util.List;

/**
 * A validator for the selection of a certain object from a hierarchy of data
 * objects.
 *
 * @author eso
 */
public class HierarchyValidator implements Validator<HierarchicalDataObject>,
	HasValueList<HierarchicalDataObject> {

	private static final long serialVersionUID = 1L;

	private List<HierarchicalDataObject> values;

	/**
	 * Creates a new instance that uses zero-based continuous integer values
	 * for
	 * the identification of the data objects.
	 *
	 * @param values The hierarchical data objects allowed by this instance
	 */
	public HierarchyValidator(List<HierarchicalDataObject> values) {
		this.values = values;
	}

	/**
	 * Default constructor for serialization.
	 */
	HierarchyValidator() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		HierarchyValidator other = (HierarchyValidator) obj;

		return values.equals(other.values);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<HierarchicalDataObject> getValues() {
		return values;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 37 * values.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValid(HierarchicalDataObject object) {
		// always return true because the object must be from the validated set
		return true;
	}
}
