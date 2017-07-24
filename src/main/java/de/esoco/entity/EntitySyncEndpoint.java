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
package de.esoco.entity;

import de.esoco.lib.comm.HttpEndpoint;
import de.esoco.lib.comm.http.HttpRequestMethod;

import static de.esoco.entity.EntitySyncService.JSON_REQUEST_CONTEXT;
import static de.esoco.entity.EntitySyncService.JSON_REQUEST_GLOBAL_ID;

import static de.esoco.lib.comm.CommunicationRelationTypes.ENDPOINT_ADDRESS;
import static de.esoco.lib.expression.Functions.identity;


/********************************************************************
 * The HTTP endpoint for interaction with the {@link EntitySyncService} REST
 * service. The endpoint itself uses the standard HTTP endpoint implementation.
 * This class only serves as a holder for the REST methods that are specific for
 * the sync service.
 *
 * @author eso
 */
public class EntitySyncEndpoint extends HttpEndpoint
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain endpoint URL.
	 *
	 * @param sEndpointUrl The endpoint URL
	 */
	public EntitySyncEndpoint(String sEndpointUrl)
	{
		set(ENDPOINT_ADDRESS, sEndpointUrl);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Static helper method that creates a data record for an entity
	 * synchronization request.
	 *
	 * @param  sContext  The name of the synchronization context
	 * @param  sGlobalId The global ID of the entity to synchronize
	 *
	 * @return The data record for use with {@link EntitySyncRequest}
	 */
	public static EntitySyncData syncRequest(String sContext, String sGlobalId)
	{
		return new EntitySyncData(sContext, sGlobalId);
	}

	/***************************************
	 * Returns a request method that will lock an entity with a certain global
	 * ID.
	 *
	 * @return The request method
	 */
	public static EntitySyncRequest lockEntity()
	{
		return new EntitySyncRequest("request_lock");
	}

	/***************************************
	 * Returns a request method that will release a lock on an entity with a
	 * certain global ID.
	 *
	 * @return The request method
	 */
	public static EntitySyncRequest releaseEntityLock()
	{
		return new EntitySyncRequest("release_lock");
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A simple data record that contains the data needed for an entity
	 * synchronization request and a method to convert it into JSON.
	 *
	 * @author eso
	 */
	public static class EntitySyncData
	{
		//~ Instance fields ----------------------------------------------------

		private final String sContext;
		private final String sGlobalId;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param sContext  The synchronization context
		 * @param sGlobalId The global ID of the affected entity
		 */
		public EntitySyncData(String sContext, String sGlobalId)
		{
			this.sContext  = sContext;
			this.sGlobalId = sGlobalId;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Formats this data record into a JSON string in the entity sync
		 * service format.
		 *
		 * @return
		 */
		public String toJson()
		{
			return String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}",
								 JSON_REQUEST_CONTEXT,
								 sContext,
								 JSON_REQUEST_GLOBAL_ID,
								 sGlobalId);
		}
	}

	/********************************************************************
	 * The base class for request methods to the {@link EntitySyncEndpoint}.
	 *
	 * @author eso
	 */
	public static class EntitySyncRequest
		extends HttpRequest<EntitySyncData, String>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param sRequestUrl sMethodName The name of the request method
		 */
		EntitySyncRequest(String sRequestUrl)
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
