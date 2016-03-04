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
package de.esoco.data;

/********************************************************************
 * The available file types.
 */
public enum FileType
{
	CSV("text/csv", ".csv"), PDF("application/x-pdf", ".pdf"),
	XLS("application/vnd.ms-excel", ".xls"),
	XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
		 ".xlsx");

	//~ Instance fields --------------------------------------------------------

	private String sMimeType;
	private String sFileExtension;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sMimeType      The MIME type string
	 * @param sFileExtension The file extension string (including the .)
	 */
	FileType(String sMimeType, String sFileExtension)
	{
		this.sMimeType	    = sMimeType;
		this.sFileExtension = sFileExtension;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the fileExtension value.
	 *
	 * @return The fileExtension value
	 */
	public String getFileExtension()
	{
		return sFileExtension;
	}

	/***************************************
	 * Returns the MIME type.
	 *
	 * @return The MIME type
	 */
	public String getMimeType()
	{
		return sMimeType;
	}
}
