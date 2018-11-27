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
package de.esoco.data.element;

import de.esoco.data.validate.Validator;

import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.UserInterfaceProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static de.esoco.lib.property.StyleProperties.LIST_STYLE;


/********************************************************************
 * A data element implementation that contains a string value.
 *
 * @author eso
 */
public class StringDataElement extends DataElement<String>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private String sValue;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new modifiable instance without a validator.
	 *
	 * @param sName  The name of this element
	 * @param sValue The value of this element
	 */
	public StringDataElement(String sName, String sValue)
	{
		this(sName, sValue, null, null);
	}

	/***************************************
	 * Creates a new instance with a constraint for the possible element values.
	 * The constraint is defined in form of a regular expression pattern against
	 * which new element values will be matched.
	 *
	 * @param sName      The name of this element
	 * @param sValue     The value of this element
	 * @param rValidator The validator for the value or NULL for none
	 * @param rFlags     The optional flags for this data element
	 */
	public StringDataElement(String					   sName,
							 String					   sValue,
							 Validator<? super String> rValidator,
							 Set<Flag>				   rFlags)
	{
		super(sName, rValidator, rFlags);

		updateValue(sValue);
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	protected StringDataElement()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Splits a phone number string into it's distinct parts and returns a list
	 * containing the parts. The list will contain 4 elements: Country code,
	 * area code, phone number, and extension. Empty parts will be returned as
	 * empty strings but will never be NULL.
	 *
	 * @param  sNumber The phone number string
	 *
	 * @return A list containing the 4 number parts
	 */
	public static List<String> getPhoneNumberParts(String sNumber)
	{
		List<String> aNumberParts;

		if (sNumber != null)
		{
			boolean bHasCountryCode = sNumber.startsWith("+");

			if (bHasCountryCode)
			{
				sNumber = sNumber.substring(1);
			}

			aNumberParts =
				new ArrayList<String>(Arrays.asList(sNumber.split("\\D")));

			if (!bHasCountryCode)
			{
				aNumberParts.add(0, "");
			}

			while (aNumberParts.size() > 4)
			{
				aNumberParts.set(
					2,
					aNumberParts.get(2) + aNumberParts.remove(3));
			}
		}
		else
		{
			aNumberParts = new ArrayList<String>(4);
		}

		for (int i = aNumberParts.size(); i < 4; i++)
		{
			aNumberParts.add("");
		}

		return aNumberParts;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public StringDataElement copy(
		CopyMode		   eMode,
		PropertyName<?>... rCopyProperties)
	{
		return (StringDataElement) super.copy(eMode, rCopyProperties);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public final String getValue()
	{
		return sValue;
	}

	/***************************************
	 * Overridden to allow any value if this instance has the user interface
	 * property {@link UserInterfaceProperties#LIST_STYLE LIST_STYLE} with a
	 * value of {@link ListStyle#EDITABLE EDITABLE}.
	 *
	 * @see DataElement#isValidValue(Validator, Object)
	 */
	@Override
	public <T> boolean isValidValue(Validator<? super T> rValidator, T rValue)
	{
		return super.isValidValue(rValidator, rValue) ||
			   (rValidator != null &&
				getProperty(LIST_STYLE, null) == ListStyle.EDITABLE);
	}

	/***************************************
	 * {@inheritDoc}
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
	protected StringDataElement newInstance()
	{
		return new StringDataElement();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected final void updateValue(String sNewValue)
	{
		sValue = sNewValue;
	}
}
