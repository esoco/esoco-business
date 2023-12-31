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
package de.esoco.data;

/**
 * The available file types.
 */
public enum FileType {
	ANY(MimeType.OCTET_STREAM, ".*"), CSV(MimeType.TEXT_CSV, ".csv"),
	HTML(MimeType.TEXT_HTML, ".html"), PDF(MimeType.PDF, ".pdf"),
	TXT(MimeType.TEXT_PLAIN, ".txt"), XLS(MimeType.MS_EXCEL, ".xls"),
	XLSX(MimeType.MS_XLSX, ".xlsx"), ZIP(MimeType.ZIP, ".zip");

	private MimeType eMimeType;

	private String sFileExtension;

	/**
	 * Creates a new instance.
	 *
	 * @param eMimeType      The MIME type string
	 * @param sFileExtension The file extension string (including the .)
	 */
	private FileType(MimeType eMimeType, String sFileExtension) {
		this.eMimeType = eMimeType;
		this.sFileExtension = sFileExtension;
	}

	/**
	 * Returns the file type for a certain file extension.
	 *
	 * @param sExtension The file extension to search for
	 * @return The file type for the given extension or NULL if no match could
	 * be found
	 */
	public static FileType forFileExtension(String sExtension) {
		for (FileType eFileType : values()) {
			if (eFileType.sFileExtension.equalsIgnoreCase(sExtension)) {
				return eFileType;
			}
		}

		return null;
	}

	/**
	 * Returns the first file type for a certain MIME type.
	 *
	 * @param eMimeType The MIME type to search the file type for
	 * @return The file type for the given extension or NULL if no match could
	 * be found
	 */
	public static FileType forMimeType(MimeType eMimeType) {
		for (FileType eFileType : values()) {
			if (eFileType.eMimeType == eMimeType) {
				return eFileType;
			}
		}

		return null;
	}

	/**
	 * Returns the fileExtension value.
	 *
	 * @return The fileExtension value
	 */
	public String getFileExtension() {
		return sFileExtension;
	}

	/**
	 * Returns the MIME type.
	 *
	 * @return The MIME type
	 */
	public MimeType getMimeType() {
		return eMimeType;
	}
}
