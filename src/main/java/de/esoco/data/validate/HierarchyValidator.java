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

import de.esoco.data.element.HierarchicalDataObject;

import java.util.List;


/********************************************************************
 * A validator for the selection of a certain object from a hierarchy of data
 * objects.
 *
 * @author eso
 */
public class HierarchyValidator implements Validator<HierarchicalDataObject>,
										   HasValueList<HierarchicalDataObject>
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
	 * @see #SelectionValidator(List, List, boolean)
	 */
	public HierarchyValidator(List<HierarchicalDataObject> rValues)
	{
		this.rValues = rValues;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	HierarchyValidator()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
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

		HierarchyValidator rOther = (HierarchyValidator) rObj;

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
		return 37 * rValues.hashCode();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValid(HierarchicalDataObject rObject)
	{
		// always return true because the object must be from the validated set
		return true;
	}
}
