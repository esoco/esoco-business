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
package de.esoco.data.document;

import de.esoco.data.FileType;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A {@link TabularDocumentWriter} implementation for the CSV file format. The
 * resulting document is a text string.
 *
 * @author u.eggers
 */
public class CsvDocumentWriter implements TabularDocumentWriter<String> {

	private static final String DATE_FORMAT = "dd.MM.yyyy";

	private static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";

	private static final String DECIMAL_FORMAT = "#,##0.00";

	private final StringBuilder document = new StringBuilder();

	private final String valueSeparator;

	private final DecimalFormat decimalFormat =
		new DecimalFormat(DECIMAL_FORMAT);

	private boolean newRow = true;

	private SimpleDateFormat dateFormat;

	/**
	 * Creates a new instance.
	 *
	 * @param valueSeparator The list value separator to use.
	 */
	public CsvDocumentWriter(String valueSeparator) {
		this(valueSeparator, DateFormat.DATE);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param valueSeparator The list value separator to use.
	 * @param dateFormat     The date format for date fields.
	 */
	public CsvDocumentWriter(String valueSeparator, DateFormat dateFormat) {
		this.valueSeparator = valueSeparator;

		switch (dateFormat) {
			case DATE:
				this.dateFormat = new SimpleDateFormat(DATE_FORMAT);
				break;

			case DATE_TIME:
				this.dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
				break;
		}
	}

	/**
	 * @see TabularDocumentWriter#addValue(Object)
	 */
	@Override
	public void addValue(Object value) {
		String valueText = null;

		if (!newRow) {
			document.append(valueSeparator);
		}

		if (value instanceof Date) {
			valueText = dateFormat.format((Date) value);
		} else if (value instanceof BigDecimal) {
			valueText = decimalFormat.format(value);
		} else {
			valueText = value != null ? value.toString() : "";
		}

		document.append("\"");
		document.append(valueText);
		document.append("\"");
		newRow = false;
	}

	/**
	 * @see TabularDocumentWriter#createDocument()
	 */
	@Override
	public String createDocument() {
		return document.toString();
	}

	/**
	 * @see TabularDocumentWriter#getFileType()
	 */
	@Override
	public FileType getFileType() {
		return FileType.CSV;
	}

	/**
	 * @see TabularDocumentWriter#newRow()
	 */
	@Override
	public void newRow() {
		document.append("\n");
		newRow = true;
	}
}
