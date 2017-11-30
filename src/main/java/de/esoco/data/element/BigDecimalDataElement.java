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

import java.math.BigDecimal;

import java.util.Set;


/********************************************************************
 * A data element implementation for {@link BigDecimal} values.
 *
 * @author eso
 */
public class BigDecimalDataElement extends DataElement<BigDecimal>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The placeholder string for the decimal grouping character. */
	public static final String DECIMAL_GROUP_CHAR = "<dec:grp>";

	/** The placeholder string for the decimal separator character. */
	public static final String DECIMAL_SEPARATOR_CHAR = "<dec:sep>";

	/**
	 * Default input constraint for positive or negative decimal values with
	 * optional grouping characters. Contains the input character placeholders
	 * {@link #DECIMAL_GROUP_CHAR} and {@link #DECIMAL_SEPARATOR_CHAR} which
	 * must be replaced with the actual locale-specific pattern before using the
	 * string as a regular expression.
	 */
	public static final String DEFAULT_CONSTRAINT =
		"-?\\d+(" + DECIMAL_SEPARATOR_CHAR + "\\d*)?";

	/**
	 * Input constraint for positive currency values. To also allow negative
	 * values use {@link #SIGNED_CURRENCY_CONSTRAINT}. See the remarks for
	 * {@link #DEFAULT_CONSTRAINT}.
	 */
	public static final String CURRENCY_CONSTRAINT =
		"\\d+(" + DECIMAL_SEPARATOR_CHAR + "\\d{0,2})?";

	/**
	 * A variant of {@link #CURRENCY_CONSTRAINT} that allows both positive and
	 * negative valuess.
	 */
	public static final String SIGNED_CURRENCY_CONSTRAINT =
		"-?" + CURRENCY_CONSTRAINT;

	//~ Instance fields --------------------------------------------------------

	private BigDecimal aValue;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new read-only instance with a certain name and value.
	 *
	 * @param sName  The name of this element
	 * @param nValue The initial value
	 */
	public BigDecimalDataElement(String sName, BigDecimal nValue)
	{
		this(sName, nValue, null, null);
	}

	/***************************************
	 * Creates a new instance with a certain initial value and validator.
	 *
	 * @param sName      The name of this element
	 * @param rValue     The initial value
	 * @param rValidator The validator for the value or NULL for none
	 * @param rFlags     The optional flags for this data element
	 */
	public BigDecimalDataElement(String						   sName,
								 BigDecimal					   rValue,
								 Validator<? super BigDecimal> rValidator,
								 Set<Flag>					   rFlags)
	{
		super(sName, rValidator, rFlags);

		this.aValue = rValue;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	protected BigDecimalDataElement()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public BigDecimalDataElement copy(CopyMode eMode)
	{
		return (BigDecimalDataElement) super.copy(eMode);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public final BigDecimal getValue()
	{
		return aValue;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void setStringValue(String sValue)
	{
		setValue(new BigDecimal(sValue));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected BigDecimalDataElement newInstance()
	{
		return new BigDecimalDataElement();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void updateValue(BigDecimal rNewValue)
	{
		aValue = rNewValue;
	}
}
