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

import de.esoco.data.validate.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/********************************************************************
 * A list data element implementation for string values.
 *
 * @author eso
 */
public class StringListDataElement extends ListDataElement<String>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private List<String> rValues = new ArrayList<String>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a list of values.
	 *
	 * @param sName   The name of this element
	 * @param rValues The string values for this element
	 */
	public StringListDataElement(String sName, List<String> rValues)
	{
		this(sName, rValues, null, null);
	}

	/***************************************
	 * Creates a new instance with a list of values.
	 *
	 * @param sName      The name of this element
	 * @param rValues    The string values for this element or NULL for none
	 * @param rValidator The validator for the list elements or NULL for none
	 * @param rFlags     The optional flags for this data element
	 */
	public StringListDataElement(String					   sName,
								 List<String>			   rValues,
								 Validator<? super String> rValidator,
								 Set<Flag>				   rFlags)
	{
		super(sName, rValidator, rFlags);

		if (rValues != null)
		{
			this.rValues.addAll(rValues);
		}
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	StringListDataElement()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public StringListDataElement copy(CopyMode eMode)
	{
		return (StringListDataElement) super.copy(eMode);
	}

	/***************************************
	 * @see ListDataElement#getList()
	 */
	@Override
	public final List<String> getList()
	{
		return rValues;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected StringListDataElement newInstance()
	{
		return new StringListDataElement();
	}
}
