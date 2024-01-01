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

import de.esoco.data.validate.StringListValidator;
import de.esoco.lib.property.PropertyName;

import java.util.Set;

/**
 * A data element implementation for period values. Because the Period class is
 * not serializable by GWT the datatype of this element is string. Periods must
 * be mapped from and to strings outside of this class.
 *
 * @author eso
 */
public class PeriodDataElement extends DataElement<String> {

	private static final long serialVersionUID = 1L;

	private int periodCount;

	private String periodUnit;

	/**
	 * Creates a new instance with a certain initial value and read-only state.
	 *
	 * @param name         The name of this element
	 * @param periodCount  The period count
	 * @param periodUnit   The period unit
	 * @param allowedUnits The period units that can be selected
	 * @param flags        The optional flags for this data element
	 */
	public PeriodDataElement(String name, int periodCount, String periodUnit,
		StringListValidator allowedUnits, Set<Flag> flags) {
		super(name, allowedUnits, flags);

		this.periodCount = periodCount;
		this.periodUnit = periodUnit;
	}

	/**
	 * Default constructor for serialization.
	 */
	PeriodDataElement() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PeriodDataElement copy(CopyMode mode,
		PropertyName<?>... copyProperties) {
		return (PeriodDataElement) super.copy(mode, copyProperties);
	}

	/**
	 * Returns the period count.
	 *
	 * @return The period count
	 */
	public final int getPeriodCount() {
		return periodCount;
	}

	/**
	 * Returns the period unit.
	 *
	 * @return The period unit
	 */
	public final String getPeriodUnit() {
		return periodUnit;
	}

	/**
	 * @see DataElement#getValue()
	 */
	@Override
	public final String getValue() {
		return periodCount + "." + periodUnit;
	}

	/**
	 * Sets the period count.
	 *
	 * @param count The period count
	 */
	public final void setPeriodCount(int count) {
		periodCount = count;
	}

	/**
	 * Sets the period unit.
	 *
	 * @param unit The period unit
	 */
	public final void setPeriodUnit(String unit) {
		periodUnit = unit;
	}

	/**
	 * Sets the string value.
	 *
	 * @param value The new string value
	 */
	@Override
	public void setStringValue(String value) {
		setValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PeriodDataElement newInstance() {
		return new PeriodDataElement();
	}

	/**
	 * @see DataElement#updateValue(Object)
	 */
	@Override
	protected final void updateValue(String newValue) {
		if ("NONE".equals(newValue)) {
			periodCount = 0;
			periodUnit = newValue;
		} else {
			String[] parts = newValue.split("\\.");

			periodCount = Integer.parseInt(parts[0]);
			periodUnit = parts[1];
		}
	}
}
