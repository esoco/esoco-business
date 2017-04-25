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
import de.esoco.lib.comm.http.HttpStatusCode;
import de.esoco.lib.comm.http.HttpStatusException;
import de.esoco.lib.security.AuthenticationService;

import java.net.InetAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.space.ObjectSpace;
import org.obrel.space.RelationSpace;

import static org.obrel.core.RelationTypes.newType;


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

	private static final RelationType<String> REQUEST_LOCK = newType();
	private static final RelationType<String> RELEASE_LOCK = newType();

	//~ Instance fields --------------------------------------------------------

	private Map<String, InetAddress> aEntityLocks = new HashMap<>();

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
		ObjectSpace<Object> rControlSpace = super.buildRestServerSpace();
		ObjectSpace<String> rApi		  = rControlSpace.get(API);
		ObjectSpace<Object> aSyncSpace    = new RelationSpace<>(true);

		rApi.set(SYNC, aSyncSpace);

		aSyncSpace.init(REQUEST_LOCK).onChange(this::requestEntityLock);
		aSyncSpace.init(RELEASE_LOCK).onChange(this::releaseEntityLock);

		return rControlSpace;
	}

	/***************************************
	 * @see de.esoco.lib.app.Service#runService()
	 */
	@Override
	protected void runService() throws Exception
	{
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
			aEntityLocks.remove(sGlobalId);
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
			if (aEntityLocks.containsKey(sGlobalId))
			{
				throw new HttpStatusException(HttpStatusCode.OK,
											  "Already locked");
			}
			else
			{
				aEntityLocks.put(sGlobalId, null);
			}
		}
		finally
		{
			aLock.unlock();
		}
	}
}
