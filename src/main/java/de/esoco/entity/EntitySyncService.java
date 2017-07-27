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

import de.esoco.lib.app.Service;
import de.esoco.lib.comm.http.HttpRequestHandler;
import de.esoco.lib.comm.http.HttpStatusCode;
import de.esoco.lib.comm.http.HttpStatusException;
import de.esoco.lib.logging.Log;
import de.esoco.lib.logging.LogLevel;
import de.esoco.lib.security.AuthenticationService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.space.ObjectSpace;
import org.obrel.space.RelationSpace;

import static org.obrel.core.RelationTypes.newType;
import static org.obrel.type.StandardTypes.IP_ADDRESS;
import static org.obrel.type.StandardTypes.NAME;


/********************************************************************
 * A service that implements the monitoring and synchronization of entity
 * modifications across multiple applications.
 *
 * @author eso
 */
public class EntitySyncService extends Service implements AuthenticationService
{
	//~ Static fields/initializers ---------------------------------------------

	/** The name of the JSON attribute that contains the request context. */
	public static final String JSON_REQUEST_CONTEXT = "context";

	/** The name of the JSON attribute that contains the global entity ID. */
	public static final String JSON_REQUEST_GLOBAL_ID = "entity";

	/** The part of the API providing access to server control. */
	public static final RelationType<ObjectSpace<Object>> SYNC = newType();

	private static final RelationType<Map<String, String>> CHECK_LOCK   =
		newType();
	private static final RelationType<Map<String, String>> REQUEST_LOCK =
		newType();
	private static final RelationType<Map<String, String>> RELEASE_LOCK =
		newType();

	private static final RelationType<Map<String, Map<String, String>>> CURRENT_LOCKS =
		newType();

	static
	{
		RelationTypes.init(EntitySyncService.class);
	}

	//~ Instance fields --------------------------------------------------------

	private Map<String, Map<String, String>> aEntityContextLocks =
		new LinkedHashMap<>();

	private Lock aLock = new ReentrantLock();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public EntitySyncService()
	{
		super(true);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Runs this service.
	 *
	 * @param rArgs The invocation arguments
	 */
	public static void main(String[] rArgs)
	{
		new EntitySyncService().run(rArgs);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean authenticate(Relatable rAuthData)
	{
		return true;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void stop()
	{
	}

	/***************************************
	 * Overridden to add the sync service endpoints to the REST server object
	 * space.
	 *
	 * @see Service#buildRestServerSpace()
	 */
	@Override
	protected ObjectSpace<Object> buildRestServerSpace()
	{
		ObjectSpace<Object> rRootSpace = super.buildRestServerSpace();
		ObjectSpace<String> rApiSpace  = rRootSpace.get(API);
		ObjectSpace<Object> aSyncSpace = new RelationSpace<>(true);

		rApiSpace.get(STATUS).set(CURRENT_LOCKS, aEntityContextLocks);
		rApiSpace.set(SYNC, aSyncSpace);

		aSyncSpace.set(NAME, getServiceName() + " Sync API");

		aSyncSpace.init(CHECK_LOCK).onUpdate(this::checkEntityLock);
		aSyncSpace.init(REQUEST_LOCK).onUpdate(this::requestEntityLock);
		aSyncSpace.init(RELEASE_LOCK).onUpdate(this::releaseEntityLock);

		return rRootSpace;
	}

	/***************************************
	 * @see de.esoco.lib.app.Service#runService()
	 */
	@Override
	protected void runService() throws Exception
	{
	}

	/***************************************
	 * Tries to acquire an entity lock for an entity with a certain global ID.
	 *
	 * @param rRequest sGlobalId The global ID of the entity to lock
	 */
	private void checkEntityLock(Map<String, String> rRequest)
	{
		processEntitySyncRequest(rRequest, this::handleCheckLock);
	}

	/***************************************
	 * Returns the IP address of the client that performs the current request.
	 *
	 * @return The IP address string
	 */
	private String getClientAddress()
	{
		return HttpRequestHandler.getThreadLocalRequest()
								 .get(IP_ADDRESS)
								 .getHostAddress();
	}

	/***************************************
	 * Handles a request to check for an entity lock.
	 *
	 * @param sContext  The lock context
	 * @param sGlobalId The global entity ID
	 */
	private void handleCheckLock(String sContext, String sGlobalId)
	{
		boolean bHasLock =
			aEntityContextLocks.containsKey(sContext) &&
			aEntityContextLocks.get(sContext).containsKey(sGlobalId);

		throw new HttpStatusException(HttpStatusCode.OK,
									  Boolean.toString(bHasLock));
	}

	/***************************************
	 * Handles a request to release an entity lock.
	 *
	 * @param sContext  The lock context
	 * @param sGlobalId The global entity ID
	 */
	private void handleReleaseLock(String sContext, String sGlobalId)
	{
		Map<String, String> aLocks		 = aEntityContextLocks.get(sContext);
		String			    sCurrentLock = null;

		if (aLocks != null)
		{
			sCurrentLock = aLocks.get(sGlobalId);
		}
		else
		{
			respond(HttpStatusCode.NOT_FOUND, "Unknown context " + sContext);
		}

		if (sCurrentLock != null)
		{
			String sClientAddress = getClientAddress();

			if (sCurrentLock.equals(sClientAddress))
			{
				aLocks.remove(sGlobalId);

				if (Log.isLevelEnabled(LogLevel.DEBUG))
				{
					Log.debugf("Current locks: %s", aEntityContextLocks);
				}
			}
			else
			{
				respond(HttpStatusCode.CONFLICT, sClientAddress);
			}
		}
		else
		{
			respond(HttpStatusCode.NOT_FOUND, sContext + ":" + sGlobalId);
		}
	}

	/***************************************
	 * Handles a request to set an entity lock.
	 *
	 * @param sContext  The lock context
	 * @param sGlobalId The global entity ID
	 */
	private void handleRequestLock(String sContext, String sGlobalId)
	{
		Map<String, String> aLocks = aEntityContextLocks.get(sContext);

		if (aLocks == null)
		{
			aLocks = new HashMap<>();
			aEntityContextLocks.put(sContext, aLocks);
		}

		String sCurrentLock   = aLocks.get(sGlobalId);
		String sClientAddress = getClientAddress();

		if (sCurrentLock == null)
		{
			aLocks.put(sGlobalId, sClientAddress);

			if (Log.isLevelEnabled(LogLevel.DEBUG))
			{
				Log.debug("Current locks: " + aEntityContextLocks);
			}
		}
		else
		{
			if (sCurrentLock.equals(sClientAddress))
			{
				respond(HttpStatusCode.ALREADY_REPORTED, "");
			}
			else
			{
				respond(HttpStatusCode.LOCKED, sClientAddress);
			}
		}
	}

	/***************************************
	 * The main method to process requests. It delegates the actual request
	 * handling to methods that implement the {@link SyncRequestHandler}
	 * interface.
	 *
	 * @param rRequest        The entity sync request
	 * @param rRequestHandler The request handler
	 */
	private void processEntitySyncRequest(
		Map<String, String> rRequest,
		SyncRequestHandler  rRequestHandler)
	{
		aLock.lock();

		try
		{
			String sContext  = rRequest.get(JSON_REQUEST_CONTEXT).toString();
			String sGlobalId = rRequest.get(JSON_REQUEST_GLOBAL_ID).toString();

			if (sContext == null || sGlobalId == null)
			{
				respond(HttpStatusCode.BAD_REQUEST, rRequest.toString());
			}

			rRequestHandler.handleRequest(sContext, sGlobalId);
		}
		catch (HttpStatusException e)
		{
			// just re-throw as this has already been handled
			throw e;
		}
		catch (Exception e)
		{
			respond(HttpStatusCode.BAD_REQUEST, rRequest.toString());
		}
		finally
		{
			aLock.unlock();
		}
	}

	/***************************************
	 * Releases an entity lock for an entity in a certain context.
	 *
	 * @param rRequest The entity lock release request
	 */
	private void releaseEntityLock(Map<String, String> rRequest)
	{
		processEntitySyncRequest(rRequest, this::handleReleaseLock);
	}

	/***************************************
	 * Tries to acquire an entity lock for an entity in a certain context.
	 *
	 * @param rRequest The entity lock request
	 */
	private void requestEntityLock(Map<String, String> rRequest)
	{
		processEntitySyncRequest(rRequest, this::handleRequestLock);
	}

	/***************************************
	 * Signals a failed request by throwing an {@link HttpStatusException}.
	 *
	 * @param  eStatus  The status code for the failure
	 * @param  sMessage The failure message
	 *
	 * @throws HttpStatusException Always throws this exception with the given
	 *                             parameters
	 */
	private void respond(HttpStatusCode eStatus, String sMessage)
	{
		throw new HttpStatusException(eStatus, sMessage);
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * A simple interface that is used internally to delegate request handling
	 * to methods.
	 *
	 * @author eso
	 */
	@FunctionalInterface
	private static interface SyncRequestHandler
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Handles a request.
		 *
		 * @param sContext  The target context of the request
		 * @param sGlobalId sContext The global ID of the target entity of the
		 *                  request
		 */
		public void handleRequest(String sContext, String sGlobalId);
	}
}
