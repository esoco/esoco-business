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

	private final MimeType mimeType;

	private final String fileExtension;

	/**
	 * Creates a new instance.
	 *
	 * @param mimeType      The MIME type string
	 * @param fileExtension The file extension string (including the .)
	 */
	FileType(MimeType mimeType, String fileExtension) {
		this.mimeType = mimeType;
		this.fileExtension = fileExtension;
	}

	/**
	 * Returns the file type for a certain file extension.
	 *
	 * @param extension The file extension to search for
	 * @return The file type for the given extension or NULL if no match could
	 * be found
	 */
	public static FileType forFileExtension(String extension) {
		for (FileType fileType : values()) {
			if (fileType.fileExtension.equalsIgnoreCase(extension)) {
				return fileType;
			}
		}

		return null;
	}

	/**
	 * Returns the first file type for a certain MIME type.
	 *
	 * @param mimeType The MIME type to search the file type for
	 * @return The file type for the given extension or NULL if no match could
	 * be found
	 */
	public static FileType forMimeType(MimeType mimeType) {
		for (FileType fileType : values()) {
			if (fileType.mimeType == mimeType) {
				return fileType;
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
		return fileExtension;
	}

	/**
	 * Returns the MIME type.
	 *
	 * @return The MIME type
	 */
	public MimeType getMimeType() {
		return mimeType;
	}
}
