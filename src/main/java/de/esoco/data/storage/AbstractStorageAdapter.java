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

import java.text.SimpleDateFormat;

import java.util.Date;

import org.obrel.core.SerializableRelatedObject;

/**
 * A base class for {@link StorageAdapter} implementations.
 *
 * @author eso
 */
public abstract class AbstractStorageAdapter extends SerializableRelatedObject
	implements StorageAdapter {

	private static final long serialVersionUID = 1L;

	private final SimpleDateFormat aConstraintDateFormat =
		new SimpleDateFormat(
		FilterableDataModel.CONSTRAINT_DATE_FORMAT_PATTERN);

	/**
	 * Creates a new instance.
	 */
	public AbstractStorageAdapter() {
		aConstraintDateFormat.setLenient(true);
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
	 * @param sConstraint The constraint value
	 * @param rDataType   The datatype to parse the constraint into
	 * @return The parsed value or NULL if it could not be parsed
	 * @throws IllegalArgumentException If the constraint could not be parsed
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object parseConstraintValue(String sConstraint,
		Class<?> rDataType) {
		Object rValue = sConstraint;

		try {
			if (!FilterableDataModel.NULL_CONSTRAINT_VALUE.equals(rValue)) {
				if (rDataType.isEnum()) {
					try {
						rValue =
							Enum.valueOf((Class<Enum>) rDataType, sConstraint);
					} catch (Exception e) {
						rValue = sConstraint;
					}
				} else if (Number.class.isAssignableFrom(rDataType)) {
					try {
						rValue = Integer.valueOf(sConstraint);
					} catch (NumberFormatException e) {
						rValue = null;
					}
				} else if (rDataType == Boolean.class) {
					rValue = sConstraint != null ?
					         Boolean.valueOf(sConstraint) :
					         null;
				} else if (Date.class.isAssignableFrom(rDataType)) {
					rValue = aConstraintDateFormat.parse(sConstraint);
				}
			}
		} catch (Exception e) {
			String sMessage =
				String.format("Could not parse constraint %s as type %s",
					sConstraint, rDataType);

			throw new IllegalArgumentException(sMessage, e);
		}

		return rValue;
	}
}
