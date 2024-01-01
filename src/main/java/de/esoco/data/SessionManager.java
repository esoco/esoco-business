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

import de.esoco.data.element.DataElementList;
import de.esoco.data.element.StringDataElement;

import java.util.Collection;

/**
 * Interface for objects that manage sessions and provide informations about
 * them.
 *
 * @author eso
 */
public interface SessionManager {

	/**
	 * Returns the absolute path name for a relative path.
	 *
	 * @param fileName The relative file name
	 * @return The absolute file name for the given path
	 */
	String getAbsoluteFileName(String fileName);

	/**
	 * Returns an object containing information about the context of the
	 * managed
	 * sessions if available.
	 *
	 * @return The session context (NULL if not available)
	 * @throws Exception If the session context cannot be accessed
	 */
	SessionContext getSessionContext() throws Exception;

	/**
	 * Returns the data for the current session. How exactly that will be
	 * determined depends on the implementation.
	 *
	 * @return The session data for the current session
	 * @throws Exception If the session data cannot be accessed
	 */
	SessionData getSessionData() throws Exception;

	/**
	 * Returns a unique ID string that identifies the current session.
	 *
	 * @return The session ID
	 */
	String getSessionId();

	/**
	 * Returns a collection with all active sessions if available.
	 *
	 * @return The active sessions or NULL if not available
	 * @throws Exception If the session data cannot be accessed
	 */
	Collection<SessionData> getSessions() throws Exception;

	/**
	 * Performs the login of a certain user. The data element parameter must
	 * contain the login name as it's name and the password as it's value. It
	 * may also carry application-specific properties that are needed by the
	 * respective implementation.
	 *
	 * @param loginData  A data element containing the login credentials
	 * @param clientInfo Information about the connecting client
	 * @return A data element list containing the user data if the
	 * authentication was successful
	 * @throws Exception May throw any kind of exception if the authentication
	 *                   fails
	 */
	DataElementList loginUser(StringDataElement loginData, String clientInfo) throws Exception;

	/**
	 * Terminates the current session to logout the associated user.
	 */
	void logoutCurrentUser();

	/**
	 * Prepares the download of a certain data object. Each prepared download
	 * must be removed if it is no longer needed by invoking the method
	 * {@link #removeDownload(String)} with the URL returned by this method.
	 *
	 * @param data The download data object
	 * @return The download URL
	 * @throws Exception If the download cannot be prepared
	 */
	String prepareDownload(DownloadData data) throws Exception;

	/**
	 * Prepares the upload of data from the client. The returned value is an
	 * application-relative URL that must be used by the client as the upload
	 * target and to remove the upload when it is no longer needed with a call
	 * to {@link #removeUpload(String)}.
	 *
	 * @param uploadHandler The handler to process the uploaded data
	 * @return An application-relative upload URL
	 * @throws Exception If the preparation fails
	 */
	String prepareUpload(UploadHandler uploadHandler) throws Exception;

	/**
	 * Removes the download data that has previously been registered by a call
	 * to {@link #prepareDownload(DownloadData)}.
	 *
	 * @param url The URL the download data had been registered for
	 */
	void removeDownload(String url);

	/**
	 * Removes an upload handler that has previously been registered by a call
	 * to {@link #prepareUpload(UploadHandler)}. It is recommended that
	 * implementations handle failures in the removal process without throwing
	 * an exception.
	 *
	 * @param url The URL the upload handler had been registered with
	 */
	void removeUpload(String url);
}
