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
import de.esoco.data.element.SelectionDataElement;

import de.esoco.lib.model.ColumnDefinition;

import java.util.List;


/********************************************************************
 * A validator for the selection of a certain object from a list of data
 * objects. The value of the {@link SelectionDataElement} will be the index of
 * the selected data element, therefore this class validates the integer value
 * of the selection index.
 *
 * @author eso
 */
public class SelectionValidator extends TabularDataValidator
	implements HasValueList<HierarchicalDataObject>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private List<HierarchicalDataObject> rValues;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that uses zero-based continuous integer values for
	 * the identification of the data objects.
	 *
	 * @param rValues  The hierarchical data objects allowed by this instance
	 * @param rColumns The table columns
	 */
	public SelectionValidator(
		List<HierarchicalDataObject> rValues,
		List<ColumnDefinition>		 rColumns)
	{
		super(rColumns);

		this.rValues = rValues;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	SelectionValidator()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object rObj)
	{
		if (!super.equals(rObj))
		{
			return false;
		}

		SelectionValidator rOther = (SelectionValidator) rObj;

		return rValues.equals(rOther.rValues);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public final List<HierarchicalDataObject> getValues()
	{
		return rValues;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return 37 * super.hashCode() + rValues.hashCode();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValid(String sId)
	{
		return true;
	}
}
