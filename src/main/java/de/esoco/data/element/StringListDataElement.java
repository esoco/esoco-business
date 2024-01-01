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
package de.esoco.data.element;

import de.esoco.data.validate.Validator;
import de.esoco.lib.property.PropertyName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A list data element implementation for string values.
 *
 * @author eso
 */
public class StringListDataElement extends ListDataElement<String> {

	private static final long serialVersionUID = 1L;

	private List<String> values = new ArrayList<String>();

	/**
	 * Creates a new instance with a list of values.
	 *
	 * @param name   The name of this element
	 * @param values The string values for this element
	 */
	public StringListDataElement(String name, List<String> values) {
		this(name, values, null, null);
	}

	/**
	 * Creates a new instance with a list of values.
	 *
	 * @param name      The name of this element
	 * @param values    The string values for this element or NULL for none
	 * @param validator The validator for the list elements or NULL for none
	 * @param flags     The optional flags for this data element
	 */
	public StringListDataElement(String name, List<String> values,
		Validator<? super String> validator, Set<Flag> flags) {
		super(name, validator, flags);

		if (values != null) {
			this.values.addAll(values);
		}
	}

	/**
	 * Default constructor for serialization.
	 */
	StringListDataElement() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StringListDataElement copy(CopyMode mode,
		PropertyName<?>... copyProperties) {
		return (StringListDataElement) super.copy(mode, copyProperties);
	}

	/**
	 * @see ListDataElement#getList()
	 */
	@Override
	public final List<String> getList() {
		return values;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StringListDataElement newInstance() {
		return new StringListDataElement();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateValue(List<String> newValues) {
		values = newValues;
	}
}
