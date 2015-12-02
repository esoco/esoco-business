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
package de.esoco.data.document;

import de.esoco.data.FileType;

import java.math.BigDecimal;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Date;


/********************************************************************
 * A {@link TabularDocumentWriter} implementation for the CSV file format. The
 * resulting document is a text string.
 *
 * @author u.eggers
 */
public class CsvDocumentWriter implements TabularDocumentWriter<String>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final String DATE_FORMAT		 = "dd.MM.yyyy";
	private static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
	private static final String DECIMAL_FORMAT   = "#,##0.00";

	//~ Instance fields --------------------------------------------------------

	private StringBuilder    aDocument		 = new StringBuilder();
	private boolean			 bNewRow		 = true;
	private String			 sValueSeparator;
	private SimpleDateFormat aDateFormat;

	private DecimalFormat aDecimalFormat = new DecimalFormat(DECIMAL_FORMAT);

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sValueSeparator The list value separator to use.
	 */
	public CsvDocumentWriter(String sValueSeparator)
	{
		this(sValueSeparator, DateFormat.DATE);
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sValueSeparator The list value separator to use.
	 * @param eDateFormat     The date format for date fields.
	 */
	public CsvDocumentWriter(String sValueSeparator, DateFormat eDateFormat)
	{
		this.sValueSeparator = sValueSeparator;

		switch (eDateFormat)
		{
			case DATE:
				aDateFormat = new SimpleDateFormat(DATE_FORMAT);
				break;

			case DATE_TIME:
				aDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
				break;
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see TabularDocumentWriter#addValue(String)
	 */
	@Override
	public void addValue(Object rValue)
	{
		String sValue = null;

		if (!bNewRow)
		{
			aDocument.append(sValueSeparator);
		}

		if (rValue instanceof Date)
		{
			sValue = aDateFormat.format((Date) rValue);
		}
		else if (rValue instanceof BigDecimal)
		{
			sValue = aDecimalFormat.format(rValue);
		}
		else
		{
			sValue = rValue != null ? rValue.toString() : "";
		}

		aDocument.append("\"");
		aDocument.append(sValue);
		aDocument.append("\"");
		bNewRow = false;
	}

	/***************************************
	 * @see TabularDocumentWriter#createDocument()
	 */
	@Override
	public String createDocument()
	{
		return aDocument.toString();
	}

	/***************************************
	 * @see TabularDocumentWriter#getFileType()
	 */
	@Override
	public FileType getFileType()
	{
		return FileType.CSV;
	}

	/***************************************
	 * @see TabularDocumentWriter#newRow()
	 */
	@Override
	public void newRow()
	{
		aDocument.append("\n");
		bNewRow = true;
	}
}
