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
package de.esoco.data.storage;

import de.esoco.lib.model.FilterableDataModel;
import org.obrel.core.SerializableRelatedObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A base class for {@link StorageAdapter} implementations.
 *
 * @author eso
 */
public abstract class AbstractStorageAdapter extends SerializableRelatedObject
	implements StorageAdapter {

	private static final long serialVersionUID = 1L;

	private final SimpleDateFormat constraintDateFormat = new SimpleDateFormat(
		FilterableDataModel.CONSTRAINT_DATE_FORMAT_PATTERN);

	/**
	 * Creates a new instance.
	 */
	public AbstractStorageAdapter() {
		constraintDateFormat.setLenient(true);
	}

	/**
	 * ö Must be implemented by subclasses to return a short string that
	 * describes this instance. Used by {@link #toString()}.
	 *
	 * @return The storage description
	 */
	public abstract String getStorageDescription();

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(),
			getStorageDescription());
	}

	/**
	 * Parses a string constraint value into a certain datatype.
	 *
	 * @param constraint The constraint value
	 * @param dataType   The datatype to parse the constraint into
	 * @return The parsed value or NULL if it could not be parsed
	 * @throws IllegalArgumentException If the constraint could not be parsed
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object parseConstraintValue(String constraint,
		Class<?> dataType) {
		Object value = constraint;

		try {
			if (!FilterableDataModel.NULL_CONSTRAINT_VALUE.equals(value)) {
				if (dataType.isEnum()) {
					try {
						value =
							Enum.valueOf((Class<Enum>) dataType, constraint);
					} catch (Exception e) {
						value = constraint;
					}
				} else if (Number.class.isAssignableFrom(dataType)) {
					try {
						value = Integer.valueOf(constraint);
					} catch (NumberFormatException e) {
						value = null;
					}
				} else if (dataType == Boolean.class) {
					value =
						constraint != null ? Boolean.valueOf(constraint) :
						null;
				} else if (Date.class.isAssignableFrom(dataType)) {
					value = constraintDateFormat.parse(constraint);
				}
			}
		} catch (Exception e) {
			String message =
				String.format("Could not parse constraint %s as type %s",
					constraint, dataType);

			throw new IllegalArgumentException(message, e);
		}

		return value;
	}
}
