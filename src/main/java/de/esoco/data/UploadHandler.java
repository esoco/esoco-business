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

import java.io.InputStream;

/**
 * An interface that defines the handling of an HTTP file upload.
 *
 * @author eso
 */
public interface UploadHandler {

	/**
	 * Must be implemented to process the uploaded data. The actual data
	 * must be
	 * read from the given input stream and stored as needed by the
	 * implementation.
	 *
	 * @param filename    The file name of the upload
	 * @param contentType The MIME type of the uploaded data
	 * @param dataStream  The input stream that provides the upload data
	 * @throws Exception Any kind of exception may be thrown if the data
	 *                   processing fails
	 */
	void processUploadData(String filename, String contentType,
		InputStream dataStream) throws Exception;
}
