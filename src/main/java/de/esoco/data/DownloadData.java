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

	private final String fileName;

	private final FileType fileType;

	private final Function<FileType, ?> dataGenerator;

	private final boolean removeAfterDownload;

	/**
	 * Creates a new instance.
	 *
	 * @param fileName            The name of the download to return to the
	 *                            client
	 * @param fileType            The content MIME type of the data
	 * @param dataGenerator       A function that will create or return the
	 *                            download data upon evaluation
	 * @param removeAfterDownload If TRUE the download data should be removed
	 *                            directly after it has been downloaded
	 */
	public DownloadData(String fileName, FileType fileType,
		Function<FileType, ?> dataGenerator, boolean removeAfterDownload) {
		this.fileName = fileName;
		this.fileType = fileType;
		this.dataGenerator = dataGenerator;
		this.removeAfterDownload = removeAfterDownload;
	}

	/**
	 * Creates the download data by evaluating the generator function.
	 *
	 * @return The download data
	 */
	public final Object createData() {
		return dataGenerator.evaluate(fileType);
	}

	/**
	 * Returns the file name of the data to download.
	 *
	 * @return The file name
	 */
	public final String getFileName() {
		return fileName;
	}

	/**
	 * Returns the file and content type of the data.
	 *
	 * @return The file type
	 */
	public final FileType getFileType() {
		return fileType;
	}

	/**
	 * Checks whether this instance should be invalidated after the data has
	 * been downloaded.
	 *
	 * @return TRUE if the data should be removed after download
	 */
	public final boolean isRemoveAfterDownload() {
		return removeAfterDownload;
	}
}
