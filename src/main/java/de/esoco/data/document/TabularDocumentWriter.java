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

/**
 * An interface that defines the creation of tabular data documents. The column
 * values can be added by means of {@link #addValue(Object)} and new table rows
 * can be created with {@link #newRow()}. After completion the resulting
 * document can be generated and queried with {@link #createDocument()}.
 *
 * @author eso
 */
public interface TabularDocumentWriter<T> {

	/**
	 * The available date type formats
	 */
	public enum DateFormat {DATE, DATE_TIME}

	/**
	 * Adds a value object to the current position in the document. NULL values
	 * will be handled as an empty celll.
	 *
	 * @param rItem The value to add
	 */
	public void addValue(Object rItem);

	/**
	 * Generates and/or returns the resulting document.
	 *
	 * @return The resulting document
	 * @throws Exception if the document creation fails
	 */
	public T createDocument() throws Exception;

	/**
	 * Returns the file type of the generated document.
	 *
	 * @return The file type
	 */
	public FileType getFileType();

	/**
	 * Indicates that a new row should be started at the current position of
	 * the
	 * document.
	 */
	public void newRow();
}
