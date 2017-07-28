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
import de.esoco.lib.comm.Endpoint;
import de.esoco.lib.comm.EndpointChain;
import de.esoco.lib.expression.Function;
import de.esoco.service.ModificationSyncEndpoint;
import de.esoco.service.ModificationSyncEndpoint.SyncData;

import static de.esoco.service.ModificationSyncEndpoint.requestLock;
import static de.esoco.service.ModificationSyncEndpoint.releaseLock;
import static de.esoco.service.ModificationSyncEndpoint.syncRequest;


/********************************************************************
 * Tests the functionality of {@link ModificationSyncEndpoint}.
 *
 * @author eso
 */
public class EntitySyncEndpointTest
{
	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Main method.
	 *
	 * @param rArgs
	 */
	public static void main(String[] rArgs)
	{
		String   sContext     = "test";
		Endpoint aSyncService = Endpoint.at("http://localhost:8377");

		EndpointChain<SyncData, String> fLock =
			requestLock().from(aSyncService);

		EndpointChain<SyncData, String> fRelease =
			releaseLock().from(aSyncService);

		lockEntities("test1", fLock);
		lockEntities("test2", fLock);
		releaseEntities("test1", fRelease);
		releaseEntities("test2", fRelease);

		System.out.printf("LOCK   : '%s'\n",
						  fLock.evaluate(syncRequest(sContext, "E-1001")));

		String sRunning = Service.CHECK_RUNNING.from(aSyncService).result();

		System.out.printf("RUNNING: %s\n", sRunning);

//		Service.REQUEST_STOP.from(aSyncService).result();
	}

	/***************************************
	 * Test lock entities
	 *
	 * @param sContext
	 * @param fLock
	 */
	private static void lockEntities(
		String							 sContext,
		Function<SyncData, String> fLock)
	{
		for (int i = 0; i < 10; i++)
		{
			String sGlobalId = "E-100" + i;

			System.out.printf("LOCK %s:%s: '%s'\n",
							  sContext,
							  sGlobalId,
							  fLock.evaluate(syncRequest(sContext, sGlobalId)));
		}
	}

	/***************************************
	 * Test release entities
	 *
	 * @param sContext
	 * @param fRelease
	 */
	private static void releaseEntities(
		String								  sContext,
		EndpointChain<SyncData, String> fRelease)
	{
		for (int i = 9; i >= 0; i--)
		{
			String sGlobalId = "E-100" + i;

			System.out.printf("RELEASE %s:%s: '%s'\n",
							  sContext,
							  sGlobalId,
							  fRelease.evaluate(syncRequest(sContext, sGlobalId)));
		}
	}
}
