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

import de.esoco.data.validate.StringListValidator;

import java.util.Set;


/********************************************************************
 * A data element implementation for period values. Because the Period class is
 * not serializable by GWT the datatype of this element is string. Periods must
 * be mapped from and to strings outside of this class.
 *
 * @author eso
 */
public class PeriodDataElement extends DataElement<String>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private int nPeriodCount;

	private String sPeriodUnit;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain initial value and read-only state.
	 *
	 * @param sName         The name of this element
	 * @param nPeriodCount  The period count
	 * @param sPeriodUnit   The period unit
	 * @param rAllowedUnits The period units that can be selected
	 * @param rFlags        The optional flags for this data element
	 */
	public PeriodDataElement(String				 sName,
							 int				 nPeriodCount,
							 String				 sPeriodUnit,
							 StringListValidator rAllowedUnits,
							 Set<Flag>			 rFlags)
	{
		super(sName, rAllowedUnits, rFlags);

		this.nPeriodCount = nPeriodCount;
		this.sPeriodUnit  = sPeriodUnit;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	PeriodDataElement()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public PeriodDataElement copy(CopyMode eMode)
	{
		return (PeriodDataElement) super.copy(eMode);
	}

	/***************************************
	 * Returns the period count.
	 *
	 * @return The period count
	 */
	public final int getPeriodCount()
	{
		return nPeriodCount;
	}

	/***************************************
	 * Returns the period unit.
	 *
	 * @return The period unit
	 */
	public final String getPeriodUnit()
	{
		return sPeriodUnit;
	}

	/***************************************
	 * @see DataElement#getValue()
	 */
	@Override
	public final String getValue()
	{
		return nPeriodCount + "." + sPeriodUnit;
	}

	/***************************************
	 * Sets the period count.
	 *
	 * @param nCount The period count
	 */
	public final void setPeriodCount(int nCount)
	{
		nPeriodCount = nCount;
	}

	/***************************************
	 * Sets the period unit.
	 *
	 * @param sUnit The period unit
	 */
	public final void setPeriodUnit(String sUnit)
	{
		sPeriodUnit = sUnit;
	}

	/***************************************
	 * Sets the string value.
	 *
	 * @param sValue The new string value
	 */
	@Override
	public void setStringValue(String sValue)
	{
		setValue(sValue);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected PeriodDataElement newInstance()
	{
		return new PeriodDataElement();
	}

	/***************************************
	 * @see DataElement#updateValue(Object)
	 */
	@Override
	protected final void updateValue(String sNewValue)
	{
		if ("NONE".equals(sNewValue))
		{
			nPeriodCount = 0;
			sPeriodUnit  = sNewValue;
		}
		else
		{
			String[] rParts = sNewValue.split("\\.");

			nPeriodCount = Integer.parseInt(rParts[0]);
			sPeriodUnit  = rParts[1];
		}
	}
}
