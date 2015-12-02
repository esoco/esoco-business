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
 * A base class for {@link Validator} implementations that check whether a value
 * is an element of a list of objects.
 *
 * @author eso
 */
public abstract class ListValidator<T> implements Validator<T>, HasValueList<T>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private List<T> rValueList;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that checks if values are an element of the
	 * argument list. If the list is empty any value will be allowed.
	 *
	 * @param rValueList The list of values to be validated against or an empty
	 *                   list to allow any value
	 */
	@SuppressWarnings("unchecked")
	public ListValidator(List<? extends T> rValueList)
	{
		this.rValueList = (List<T>) rValueList;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	ListValidator()
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

		ListValidator<?> rOther = (ListValidator<?>) rObj;

		return rValueList.equals(rOther.rValueList);
	}

	/***************************************
	 * @see HasValueList#getValues()
	 */
	@Override
	public List<T> getValues()
	{
		return rValueList;
	}

	/***************************************
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return 37 * rValueList.hashCode();
	}

	/***************************************
	 * @see Validator#isValid(Object)
	 */
	@Override
	public boolean isValid(T rValue)
	{
		return rValue == null || rValueList.isEmpty() ||
			   rValueList.contains(rValue);
	}
}
