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

import java.util.Set;


/********************************************************************
 * A data element implementation for boolean values.
 *
 * @author eso
 */
public class BooleanDataElement extends DataElement<Boolean>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Boolean rValue;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain initial value and read-only state.
	 *
	 * @param sName  The name of this element
	 * @param rValue The initial value
	 * @param rFlags The optional flags for this data element
	 */
	public BooleanDataElement(String sName, Boolean rValue, Set<Flag> rFlags)
	{
		super(sName, null, rFlags);

		this.rValue = rValue;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	BooleanDataElement()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public BooleanDataElement copy(CopyMode eMode)
	{
		return (BooleanDataElement) super.copy(eMode);
	}

	/***************************************
	 * @see DataElement#getValue()
	 */
	@Override
	public final Boolean getValue()
	{
		return rValue;
	}

	/***************************************
	 * Sets the string value.
	 *
	 * @param sValue The new string value
	 */
	@Override
	public void setStringValue(String sValue)
	{
		setValue(Boolean.valueOf(sValue));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected BooleanDataElement newInstance()
	{
		return new BooleanDataElement();
	}

	/***************************************
	 * @see DataElement#updateValue(Object)
	 */
	@Override
	protected final void updateValue(Boolean rNewValue)
	{
		rValue = rNewValue;
	}
}
