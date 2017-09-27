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

import de.esoco.lib.comm.Endpoint;
import de.esoco.lib.comm.HttpEndpoint;
import de.esoco.lib.comm.http.HttpRequestMethod;
import de.esoco.lib.json.JsonObject;
import de.esoco.lib.reflect.ReflectUtil;


/********************************************************************
 * The HTTP endpoint for interaction with an {@link InteractiveProcessExecutor}
 * or {@link InteractiveProcessRenderer} REST service. The endpoint itself uses
 * the standard HTTP endpoint implementation. An instance can be created by
 * invoking the standard method {@link Endpoint#at(String)} with the service
 * URL. This class only serves as a holder for the static REST method
 * definitions that are specific for the process services.
 *
 * @author eso
 */
public class InteractiveProcessEndpoint extends HttpEndpoint
{
	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a request method that will release a lock on an certain target.
	 *
	 * @return The request method
	 */
	public static ProcessRequest<?, ?> executeProcess()
	{
		return null;
	}

	/***************************************
	 * Returns a request method that will query the current locks. The result is
	 * a map that contains entries for the lock contexts. Each context entry
	 * contains another mapping from target IDs to the addresses of the clients
	 * that hold the locks.
	 *
	 * @return The request method
	 */
	public static ProcessRequest<?, ?> registerProcesses()
	{
		return null;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A container for the JSON serialization of process request data. The
	 * actual request data is contained in the relations of an instance.
	 *
	 * @author eso
	 */
	public static abstract class RequestData<T extends RequestData<T>>
		extends JsonObject<T>
	{
	}

	/********************************************************************
	 * The base class for request methods to the {@link
	 * InteractiveProcessEndpoint}.
	 *
	 * @author eso
	 */
	public static class ProcessRequest<D extends RequestData<D>,
									   R extends RequestData<R>>
		extends HttpRequest<D, R>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param eMethod       The request method
		 * @param sRequestUrl   The request URL
		 * @param rResponseType The datatype of the request response
		 */
		ProcessRequest(HttpRequestMethod eMethod,
					   String			 sRequestUrl,
					   Class<R>			 rResponseType)
		{
			super(sRequestUrl,
				  null,
				  eMethod,
				  "/api/" + sRequestUrl,
				  data -> data != null ? data.toJson() : "",
				  sResponse ->
				  ReflectUtil.newInstance(rResponseType)
				  .fromJson(sResponse));
		}
	}
}
