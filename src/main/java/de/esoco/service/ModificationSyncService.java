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

import de.esoco.lib.app.RestService;
import de.esoco.lib.app.Service;
import de.esoco.lib.comm.http.HttpRequestHandler;
import de.esoco.lib.comm.http.HttpStatusCode;
import de.esoco.lib.comm.http.HttpStatusException;
import de.esoco.lib.logging.Log;
import de.esoco.lib.logging.LogLevel;
import de.esoco.lib.security.AuthenticationService;

import java.util.LinkedHashMap;
import java.util.Map;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.space.ObjectSpace;
import org.obrel.space.RelationSpace;

import static org.obrel.core.RelationTypes.newType;
import static org.obrel.type.StandardTypes.IP_ADDRESS;
import static org.obrel.type.StandardTypes.NAME;


/********************************************************************
 * A service that implements the monitoring and synchronization of data
 * modifications across multiple applications.
 *
 * @author eso
 */
public class ModificationSyncService extends RestService
	implements AuthenticationService
{
	//~ Static fields/initializers ---------------------------------------------

	/** The name of the JSON attribute with the request context. */
	public static final String JSON_REQUEST_CONTEXT = "context";

	/** The name of the JSON attribute with the target ID. */
	public static final String JSON_REQUEST_TARGET_ID = "target";

	/**
	 * The name of the JSON attribute with a flag to force a certain request.
	 */
	public static final String JSON_REQUEST_FORCE_FLAG = "force";

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
		RelationTypes.init(ModificationSyncService.class);
	}

	//~ Instance fields --------------------------------------------------------

	private Map<String, Map<String, String>> aContextLocks =
		new LinkedHashMap<>();

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Runs this service.
	 *
	 * @param rArgs The invocation arguments
	 */
	public static void main(String[] rArgs)
	{
		new ModificationSyncService().run(rArgs);
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

		rApiSpace.set(SYNC, aSyncSpace);
		rApiSpace.get(STATUS).set(CURRENT_LOCKS, aContextLocks);

		aSyncSpace.set(NAME, getServiceName() + " Sync API");

		aSyncSpace.init(CHECK_LOCK).onUpdate(this::checkLock);
		aSyncSpace.init(REQUEST_LOCK).onUpdate(this::requestLock);
		aSyncSpace.init(RELEASE_LOCK).onUpdate(this::releaseLock);
		aSyncSpace.set(CURRENT_LOCKS, aContextLocks)
				  .onUpdate(this::updateLocks);

		return rRootSpace;
	}

	/***************************************
	 * Tries to acquire a lock for a certain request.
	 *
	 * @param rRequest The lock request
	 */
	private void checkLock(Map<String, String> rRequest)
	{
		processSyncRequest(rRequest, this::handleCheckLock);
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
	 * Handles a request to check for a lock.
	 *
	 * @param sContext      The lock context
	 * @param sTargetId     The target ID
	 * @param bForceRequest Ignored in this context
	 */
	private void handleCheckLock(String  sContext,
								 String  sTargetId,
								 boolean bForceRequest)
	{
		boolean bHasLock =
			aContextLocks.containsKey(sContext) &&
			aContextLocks.get(sContext).containsKey(sTargetId);

		throw new HttpStatusException(HttpStatusCode.OK,
									  Boolean.toString(bHasLock));
	}

	/***************************************
	 * Handles a request to release an entity lock.
	 *
	 * @param sContext      The lock context
	 * @param sTargetId     The target ID
	 * @param bForceRequest TRUE to force the release even if the lock has been
	 *                      acquired by a different client
	 */
	private void handleReleaseLock(String  sContext,
								   String  sTargetId,
								   boolean bForceRequest)
	{
		Map<String, String> aLocks		 = aContextLocks.get(sContext);
		String			    sCurrentLock = null;

		if (aLocks != null)
		{
			sCurrentLock = aLocks.get(sTargetId);
		}
		else
		{
			respond(HttpStatusCode.NOT_FOUND, "Unknown context " + sContext);
		}

		if (sCurrentLock != null)
		{
			String  sClientAddress  = getClientAddress();
			boolean bLockedByClient = sCurrentLock.equals(sClientAddress);

			if (bLockedByClient || bForceRequest)
			{
				if (bForceRequest && !bLockedByClient)
				{
					Log.warnf("Locked by %s, release forced by %s",
							  sCurrentLock,
							  sClientAddress);
				}

				aLocks.remove(sTargetId);

				if (Log.isLevelEnabled(LogLevel.DEBUG))
				{
					Log.debugf("Current locks: %s", aContextLocks);
				}
			}
			else
			{
				respond(HttpStatusCode.CONFLICT, sClientAddress);
			}
		}
		else
		{
			respond(HttpStatusCode.NOT_FOUND, sContext + ":" + sTargetId);
		}
	}

	/***************************************
	 * Handles a request to set a lock.
	 *
	 * @param sContext      The lock context
	 * @param sTargetId     The target ID
	 * @param bForceRequest TRUE to force the lock even if the same lock has
	 *                      already been acquired by a different client
	 */
	private void handleRequestLock(String  sContext,
								   String  sTargetId,
								   boolean bForceRequest)
	{
		Map<String, String> aLocks = aContextLocks.get(sContext);

		if (aLocks == null)
		{
			aLocks = new LinkedHashMap<>();
			aContextLocks.put(sContext, aLocks);
		}

		String sCurrentLock   = aLocks.get(sTargetId);
		String sClientAddress = getClientAddress();

		if (sCurrentLock == null || bForceRequest)
		{
			if (bForceRequest && sCurrentLock != null)
			{
				Log.warnf("Locked by %s, forcing lock to %s",
						  sCurrentLock,
						  sCurrentLock);
			}

			aLocks.put(sTargetId, sClientAddress);

			if (Log.isLevelEnabled(LogLevel.DEBUG))
			{
				Log.debug("Current locks: " + aContextLocks);
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
	 * @param rRequest        The sync request
	 * @param rRequestHandler The request handler
	 */
	private void processSyncRequest(
		Map<String, String> rRequest,
		SyncRequestHandler  rRequestHandler)
	{
		try
		{
			Object sContext   = rRequest.get(JSON_REQUEST_CONTEXT);
			Object sGlobalId  = rRequest.get(JSON_REQUEST_TARGET_ID);
			Object rForceFlag = rRequest.get(JSON_REQUEST_FORCE_FLAG);

			if (sContext == null ||
				sGlobalId == null ||
				(rForceFlag != null && !(rForceFlag instanceof Boolean)))
			{
				respond(HttpStatusCode.BAD_REQUEST, rRequest.toString());
			}

			boolean bForceRequest =
				rForceFlag != null ? ((Boolean) rForceFlag).booleanValue()
								   : false;

			rRequestHandler.handleRequest(sContext.toString(),
										  sGlobalId.toString(),
										  bForceRequest);
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
	}

	/***************************************
	 * Releases a lock in a certain context.
	 *
	 * @param rRequest The lock release request
	 */
	private void releaseLock(Map<String, String> rRequest)
	{
		processSyncRequest(rRequest, this::handleReleaseLock);
	}

	/***************************************
	 * Tries to acquire a lock on a target in a certain context.
	 *
	 * @param rRequest The lock request
	 */
	private void requestLock(Map<String, String> rRequest)
	{
		processSyncRequest(rRequest, this::handleRequestLock);
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

	/***************************************
	 * Invoked upon tries to set all locks by writing to {@link #CURRENT_LOCKS}.
	 * Currently not supported, therefore throws an exception.
	 *
	 * @param aNewLocks The new lock mapping
	 */
	private void updateLocks(Map<?, ?> aNewLocks)
	{
		respond(HttpStatusCode.METHOD_NOT_ALLOWED,
				"Setting all locks is not supported");
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
		 * @param sContext      The target context of the request
		 * @param sTargetId     The unique ID of the request target
		 * @param bForceRequest TRUE to force the request execution
		 */
		public void handleRequest(String  sContext,
								  String  sTargetId,
								  boolean bForceRequest);
	}
}
