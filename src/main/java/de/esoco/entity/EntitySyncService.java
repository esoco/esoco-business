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


/********************************************************************
 * A service that implements the monitoring and synchronization of entity
 * modifications across multiple applications.
 *
 * @author eso
 */
public class EntitySyncService extends Service implements AuthenticationService
{
	//~ Static fields/initializers ---------------------------------------------

	/** The part of the API providing access to server control. */
	public static final RelationType<ObjectSpace<Object>> SYNC = newType();

	private static final RelationType<String> CHECK_LOCK   = newType();
	private static final RelationType<String> REQUEST_LOCK = newType();
	private static final RelationType<String> RELEASE_LOCK = newType();

	private static final RelationType<Map<String, String>> CURRENT_LOCKS =
		newType();

	static
	{
		RelationTypes.init(EntitySyncService.class);
	}

	//~ Instance fields --------------------------------------------------------

	private Map<String, String> aEntityLocks = new LinkedHashMap<>();

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

		rApiSpace.get(STATUS).set(CURRENT_LOCKS, aEntityLocks);
		rApiSpace.set(SYNC, aSyncSpace);

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
	 * @param sGlobalId The global ID of the entity to lock
	 */
	private void checkEntityLock(String sGlobalId)
	{
		aLock.lock();

		try
		{
			boolean bHasLock = aEntityLocks.containsKey(sGlobalId);

			throw new HttpStatusException(HttpStatusCode.OK,
										  Boolean.toString(bHasLock));
		}
		finally
		{
			aLock.unlock();
		}
	}

	/***************************************
	 * Returns the IP address of the client that performs the current request.
	 *
	 * @return The IP address string
	 */
	private String getClientAddress()
	{
		return HttpRequestHandler.getThreadLocalRequest().get(IP_ADDRESS)
								 .getHostAddress();
	}

	/***************************************
	 * Releases an entity lock for an entity with a certain global ID.
	 *
	 * @param sGlobalId The global ID of the entity to release
	 */
	private void releaseEntityLock(String sGlobalId)
	{
		aLock.lock();

		try
		{
			String sCurrentLock = aEntityLocks.get(sGlobalId);

			if (sCurrentLock != null)
			{
				if (sCurrentLock.equals(getClientAddress()))
				{
					aEntityLocks.remove(sGlobalId);

					if (Log.isLevelEnabled(LogLevel.DEBUG))
					{
						Log.debug("Current locks: " + aEntityLocks);
					}
				}
				else
				{
					requestFailed(HttpStatusCode.FORBIDDEN,
								  "Locked by other client");
				}
			}
			else
			{
				requestFailed(HttpStatusCode.NOT_FOUND, "Not locked");
			}
		}
		finally
		{
			aLock.unlock();
		}
	}

	/***************************************
	 * Tries to acquire an entity lock for an entity with a certain global ID.
	 *
	 * @param sGlobalId The global ID of the entity to lock
	 */
	private void requestEntityLock(String sGlobalId)
	{
		aLock.lock();

		try
		{
			String sCurrentLock   = aEntityLocks.get(sGlobalId);
			String sClientAddress = getClientAddress();

			if (sCurrentLock == null)
			{
				aEntityLocks.put(sGlobalId, sClientAddress);

				if (Log.isLevelEnabled(LogLevel.DEBUG))
				{
					Log.debug("Current locks: " + aEntityLocks);
				}
			}
			else
			{
				if (sCurrentLock.equals(sClientAddress))
				{
					requestFailed(HttpStatusCode.ALREADY_REPORTED,
								  "Already locked");
				}
				else
				{
					requestFailed(HttpStatusCode.LOCKED,
								  "Locked by " + sClientAddress);
				}
			}
		}
		finally
		{
			aLock.unlock();
		}
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
	private void requestFailed(HttpStatusCode eStatus, String sMessage)
	{
		throw new HttpStatusException(eStatus, sMessage);
	}
}
