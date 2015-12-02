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

/********************************************************************
 * A validator that validates string values against a regular expression
 * pattern.
 *
 * @author eso
 */
public class RegExValidator implements Validator<String>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private String sPattern;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sPattern The regular expression pattern to validate against
	 */
	public RegExValidator(String sPattern)
	{
		assert sPattern != null;

		this.sPattern = sPattern;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	RegExValidator()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object rObj)
	{
		if (this == rObj)
		{
			return true;
		}

		if (rObj == null || getClass() != rObj.getClass())
		{
			return false;
		}

		RegExValidator rOther = (RegExValidator) rObj;

		return sPattern.equals(rOther.sPattern);
	}

	/***************************************
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return 37 * sPattern.hashCode();
	}

	/***************************************
	 * Validates the string value by matching it against the regular expression
	 * pattern of this instance.
	 *
	 * @see Validator#isValid(Object)
	 */
	@Override
	public boolean isValid(String sValue)
	{
		return sValue.matches(sPattern);
	}

	/***************************************
	 * Returns the regular expression pattern of this validator.
	 *
	 * @return The regular expression pattern
	 */
	protected final String getPattern()
	{
		return sPattern;
	}
}
