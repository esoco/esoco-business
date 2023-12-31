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
 * A validator that is based on a storage query. This validator doesn't do any
 * real validation because it assumes that the client only chooses elements that
 * have been returned by the query which are always valid (although subclasses
 * could implement additional validations if necessary). This validator serves
 * mainly to hold information about a storage query which then can be used by a
 * client to access a storage service.
 *
 * @author eso
 */
public class QueryValidator extends TabularDataValidator {

	private static final long serialVersionUID = 1L;

	private String sQueryId;

	/**
	 * Creates a new instance for a certain query.
	 *
	 * @param sQueryId      The ID to identify the query
	 * @param rQueryColumns The definitions of the query columns
	 */
	public QueryValidator(String sQueryId,
		List<ColumnDefinition> rQueryColumns) {
		super(rQueryColumns);

		this.sQueryId = sQueryId;
	}

	/**
	 * Default constructor for serialization.
	 */
	QueryValidator() {
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object rObj) {
		return (rObj instanceof QueryValidator) &&
			sQueryId.equals(((QueryValidator) rObj).sQueryId);
	}

	/**
	 * Returns the query ID.
	 *
	 * @return The query ID string
	 */
	public final String getQueryId() {
		return sQueryId;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 37 * sQueryId.hashCode();
	}

	/**
	 * Only checks that the given value is not NULL and otherwise assumes that
	 * values are only selected from query results.
	 *
	 * @see Validator#isValid(Object)
	 */
	@Override
	public boolean isValid(String sEntityId) {
		return sEntityId != null;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return sQueryId;
	}
}
