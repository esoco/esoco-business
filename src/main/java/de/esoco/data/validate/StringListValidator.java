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

import java.util.List;


/********************************************************************
 * A {@link ListValidator} implementation that checks whether a value is an
 * element of a list of strings.
 *
 * @author eso
 */
public class StringListValidator extends ListValidator<String>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private boolean bResourceIds;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that validates against a certain list.
	 *
	 * @param rStrings     The list of strings to be validated against or an
	 *                     empty list to allow any value
	 * @param bResourceIds TRUE to interpret the string values as resource IDs
	 */
	public StringListValidator(List<String> rStrings, boolean bResourceIds)
	{
		super(rStrings);

		this.bResourceIds = bResourceIds;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	StringListValidator()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns TRUE if the string values of this instance should be interpreted
	 * as resource IDs.
	 *
	 * @return TRUE to interpret the string values as resource IDs
	 */
	public final boolean isResourceIds()
	{
		return bResourceIds;
	}
}
