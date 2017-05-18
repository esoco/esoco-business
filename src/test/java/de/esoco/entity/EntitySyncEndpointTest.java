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

import static de.esoco.entity.EntitySyncEndpoint.lockEntity;
import static de.esoco.entity.EntitySyncEndpoint.releaseEntityLock;


/********************************************************************
 * Tests the functionality of {@link EntitySyncEndpoint}.
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
		Endpoint aSync = Endpoint.at("http://localhost:8377");

		EndpointChain<String, String> fLock = lockEntity().from(aSync);

		EndpointChain<String, String> fRelease =
			releaseEntityLock().from(aSync);

		for (int i = 0; i < 10; i++)
		{
			System.out.printf("LOCK   : '%s'\n",
							  fLock.evaluate("\"E-100" + i + "\""));
		}

		for (int i = 9; i >= 8; i--)
		{
			System.out.printf("RELEASE: '%s'\n",
							  fRelease.evaluate("\"E-100" + i + "\""));
		}

		System.out.printf("LOCK   : '%s'\n", fLock.evaluate("\"E-1001\""));

		String sRunning = Service.CHECK_RUNNING.from(aSync).result();

		System.out.printf("RUNNING: %s\n", sRunning);

		Service.REQUEST_STOP.from(aSync).result();
	}
}
