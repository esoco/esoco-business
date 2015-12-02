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

import java.util.Collection;


/********************************************************************
 * Interface for objects that manage sessions and provide informations about
 * them.
 *
 * @author eso
 */
public interface SessionManager
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the absolute path name for a relative path.
	 *
	 * @param  sFileName The relative file name
	 *
	 * @return The absolute file name for the given path
	 */
	public String getAbsoluteFileName(String sFileName);

	/***************************************
	 * Returns an object containing information about the context of the managed
	 * sessions if available.
	 *
	 * @return The session context (NULL if not available)
	 *
	 * @throws Exception If the session context cannot be accessed
	 */
	public SessionContext getSessionContext() throws Exception;

	/***************************************
	 * Returns the data for the current session. How exactly that will be
	 * determined depends on the implementation.
	 *
	 * @return The session data for the current session
	 *
	 * @throws Exception If the session data cannot be accessed
	 */
	public SessionData getSessionData() throws Exception;

	/***************************************
	 * Returns a unique ID string that identifies the current session.
	 *
	 * @return The session ID
	 */
	public String getSessionId();

	/***************************************
	 * Returns a collection with all active sessions if available.
	 *
	 * @return The active sessions or NULL if not available
	 *
	 * @throws Exception If the session data cannot be accessed
	 */
	public Collection<SessionData> getSessions() throws Exception;

	/***************************************
	 * Prepares the download of a certain data object. Each prepared download
	 * must be removed if it is no longer needed by invoking the method {@link
	 * #removeDownload(String)} with the URL returned by this method.
	 *
	 * @param  rData The download data object
	 *
	 * @return The download URL
	 *
	 * @throws Exception If the download cannot be prepared
	 */
	public String prepareDownload(DownloadData rData) throws Exception;

	/***************************************
	 * Prepares the upload of data from the client. The returned value is an
	 * application-relative URL that must be used by the client as the upload
	 * target and to remove the upload when it is no longer needed with a call
	 * to {@link #removeUpload(String)}.
	 *
	 * @param  rUploadHandler The handler to process the uploaded data
	 *
	 * @return An application-relative upload URL
	 *
	 * @throws Exception If the preparation fails
	 */
	public String prepareUpload(UploadHandler rUploadHandler) throws Exception;

	/***************************************
	 * Removes the download data that has previously been registered by a call
	 * to {@link #prepareDownload(DownloadData)}.
	 *
	 * @param sUrl The URL the download data had been registered for
	 */
	public void removeDownload(String sUrl);

	/***************************************
	 * Removes an upload handler that has previously been registered by a call
	 * to {@link #prepareUpload(UploadHandler)}. It is recommended that
	 * implementations handle failures in the removal process without throwing
	 * an exception.
	 *
	 * @param sUrl The URL the upload handler had been registered with
	 */
	public void removeUpload(String sUrl);
}
