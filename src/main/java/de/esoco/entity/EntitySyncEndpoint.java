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

import de.esoco.lib.comm.Connection;
import de.esoco.lib.comm.HttpEndpoint;
import de.esoco.lib.comm.http.HttpRequestMethod;

import static de.esoco.lib.comm.CommunicationRelationTypes.HTTP_STATUS_CODE;
import static de.esoco.lib.expression.Functions.identity;


/********************************************************************
 * The HTTP endpoint for interaction with the {@link EntitySyncService} REST
 * service.
 *
 * @author eso
 */
public class EntitySyncEndpoint extends HttpEndpoint
{
	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * The base class for request methods to the {@link EntitySyncEndpoint}.
	 *
	 * @author eso
	 */
	public static class EntitySyncRequest extends HttpRequest<String, String>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param sMethodName The name of the request method
		 */
		public EntitySyncRequest(String sMethodName)
		{
			super(sMethodName,
				  null,
				  HttpRequestMethod.POST,
				  "/api/sync/",
				  identity(),
				  identity());
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see HttpRequest#processResponse(Connection, String)
		 */
		@Override
		protected String processResponse(
			Connection rConnection,
			String	   sRawResponse)
		{
			return rConnection.get(HTTP_STATUS_CODE).toString();
		}
	}
}
