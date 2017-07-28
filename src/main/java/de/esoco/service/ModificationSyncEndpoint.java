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
package de.esoco.service;

import de.esoco.lib.comm.HttpEndpoint;
import de.esoco.lib.comm.http.HttpRequestMethod;

import static de.esoco.lib.comm.CommunicationRelationTypes.ENDPOINT_ADDRESS;
import static de.esoco.lib.expression.Functions.identity;

import static de.esoco.service.ModificationSyncService.JSON_REQUEST_CONTEXT;
import static de.esoco.service.ModificationSyncService.JSON_REQUEST_TARGET_ID;


/********************************************************************
 * The HTTP endpoint for interaction with the {@link ModificationSyncService}
 * REST service. The endpoint itself uses the standard HTTP endpoint
 * implementation. This class only serves as a holder for the REST methods that
 * are specific for the sync service.
 *
 * @author eso
 */
public class ModificationSyncEndpoint extends HttpEndpoint
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain endpoint URL.
	 *
	 * @param sEndpointUrl The endpoint URL
	 */
	public ModificationSyncEndpoint(String sEndpointUrl)
	{
		set(ENDPOINT_ADDRESS, sEndpointUrl);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a request method that will release a lock on an certain target.
	 *
	 * @return The request method
	 */
	public static SyncRequest releaseLock()
	{
		return new SyncRequest("release_lock");
	}

	/***************************************
	 * Returns a request method that will lock a certain target.
	 *
	 * @return The request method
	 */
	public static SyncRequest requestLock()
	{
		return new SyncRequest("request_lock");
	}

	/***************************************
	 * Static helper method that creates a data record for a synchronization
	 * request.
	 *
	 * @param  sContext  The name of the synchronization context
	 * @param  sTargetId The unique ID of the target to synchronize
	 *
	 * @return The data record for use with {@link SyncRequest}
	 */
	public static SyncData syncRequest(String sContext, String sTargetId)
	{
		return new SyncData(sContext, sTargetId);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A simple data record that contains the data needed for a synchronization
	 * request and a method to convert it into JSON.
	 *
	 * @author eso
	 */
	public static class SyncData
	{
		//~ Instance fields ----------------------------------------------------

		private final String sContext;
		private final String sTargetId;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param sContext  The synchronization context
		 * @param sTargetId The unique ID of the target
		 */
		public SyncData(String sContext, String sTargetId)
		{
			this.sContext  = sContext;
			this.sTargetId = sTargetId;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Formats this data record into a JSON string in the required sync
		 * service format.
		 *
		 * @return
		 */
		public String toJson()
		{
			return String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}",
								 JSON_REQUEST_CONTEXT,
								 sContext,
								 JSON_REQUEST_TARGET_ID,
								 sTargetId);
		}
	}

	/********************************************************************
	 * The base class for request methods to the {@link
	 * ModificationSyncEndpoint}.
	 *
	 * @author eso
	 */
	public static class SyncRequest extends HttpRequest<SyncData, String>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param sRequestUrl sMethodName The name of the request method
		 */
		SyncRequest(String sRequestUrl)
		{
			super(sRequestUrl,
				  null,
				  HttpRequestMethod.POST,
				  "/api/sync/" + sRequestUrl,
				  data -> data.toJson(),
				  identity());
		}
	}
}
