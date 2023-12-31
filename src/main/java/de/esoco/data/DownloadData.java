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
package de.esoco.data;

import de.esoco.lib.expression.Function;

import org.obrel.core.RelatedObject;

/**
 * This class encapsulates the data and parameters for an HTTP file download.
 * The actual download data must be provided by a function that generates or
 * returns the data object on evaluation.
 *
 * @author eso
 */
public class DownloadData extends RelatedObject {

	private final String sFileName;

	private final FileType eFileType;

	private final Function<FileType, ?> fDataGenerator;

	private final boolean bRemoveAfterDownload;

	/**
	 * Creates a new instance.
	 *
	 * @param sFileName            The name of the download to return to the
	 *                             client
	 * @param eFileType            The content MIME type of the data
	 * @param fDataGenerator       A function that will create or return the
	 *                             download data upon evaluation
	 * @param bRemoveAfterDownload If TRUE the download data should be removed
	 *                             directly after it has been downloaded
	 */
	public DownloadData(String sFileName, FileType eFileType,
		Function<FileType, ?> fDataGenerator, boolean bRemoveAfterDownload) {
		this.sFileName = sFileName;
		this.eFileType = eFileType;
		this.fDataGenerator = fDataGenerator;
		this.bRemoveAfterDownload = bRemoveAfterDownload;
	}

	/**
	 * Creates the download data by evaluating the generator function.
	 *
	 * @return The download data
	 */
	public final Object createData() {
		return fDataGenerator.evaluate(eFileType);
	}

	/**
	 * Returns the file name of the data to download.
	 *
	 * @return The file name
	 */
	public final String getFileName() {
		return sFileName;
	}

	/**
	 * Returns the file and content type of the data.
	 *
	 * @return The file type
	 */
	public final FileType getFileType() {
		return eFileType;
	}

	/**
	 * Checks whether this instance should be invalidated after the data has
	 * been downloaded.
	 *
	 * @return TRUE if the data should be removed after download
	 */
	public final boolean isRemoveAfterDownload() {
		return bRemoveAfterDownload;
	}
}
