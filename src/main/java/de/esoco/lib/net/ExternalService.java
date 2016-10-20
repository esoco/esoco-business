//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.net;

import de.esoco.entity.Entity;

import de.esoco.storage.StorageException;

import java.math.BigInteger;

import java.net.URL;

import java.security.SecureRandom;

import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;


/********************************************************************
 * The base class for the access to external services.
 *
 * @author eso
 */
public abstract class ExternalService extends RelatedObject
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * The possible values for service access types.
	 */
	public enum AccessType { GET, POST, DELETE, PUT, PATCH }

	//~ Instance fields --------------------------------------------------------

	private final String sServiceId =
		new BigInteger(130, new SecureRandom()).toString(32);

	private Entity rUser;
	private Entity rConfig;

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method to create a new service instance of a certain type for a
	 * user.
	 *
	 * @param  rServiceDefinition The type of service to create
	 * @param  rUser              The user entity the service access shall be
	 *                            performed for
	 * @param  rConfig            The configuration entity to read configuration
	 *                            values from
	 *
	 * @return The new service instance
	 *
	 * @throws Exception If the service creation fails
	 */
	public static ExternalService create(
		ExternalServiceDefinition rServiceDefinition,
		Entity					  rUser,
		Entity					  rConfig) throws Exception
	{
		ExternalService aService =
			rServiceDefinition.getServiceClass().newInstance();

		aService.init(rUser, rConfig);

		return aService;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Authorizes access to the external service. If the external service
	 * supports different access scopes these can be given in the second
	 * parameter. The returned value depends on the actual service
	 * implementation. Typically it can be expected to be one of the following
	 * types:
	 *
	 * <ul>
	 *   <li>An {@link URL}: if the user needs to verify the request before the
	 *     service access can be authorized. In that case the callback URL in
	 *     the corresponding parameter will be invoked after the user has
	 *     confirmed the request.</li>
	 *   <li>A string: The access token that authorizes the access to the
	 *     service in subsequent requests.</li>
	 * </ul>
	 *
	 * @param  sCallbackUrl  The callback URL to be notified if the user needs
	 *                       to manually verify the request before the service
	 *                       access can be authorized
	 * @param  bForceAuth    TRUE to force the authorization even if cached
	 *                       authorization tokens or similar exist
	 * @param  rAccessScopes The optional access scopes
	 *
	 * @return Either a verification URL or an access token string
	 *
	 * @throws Exception If the authorization fails
	 */
	public abstract Object authorizeAccess(String    sCallbackUrl,
										   boolean   bForceAuth,
										   Object... rAccessScopes)
		throws Exception;

	/***************************************
	 * Prepares a request to the external service.
	 *
	 * @param  eAccessType The access type
	 * @param  sRequestUrl The request URL
	 *
	 * @return The request object
	 *
	 * @throws Exception If creating the request fails
	 */
	public abstract ExternalServiceRequest createRequest(
		AccessType eAccessType,
		String	   sRequestUrl) throws Exception;

	/***************************************
	 * Returns the name of the parameter that must be read from a callback HTTP
	 * request and then handed to the method {@link #processCallback(String)}.
	 *
	 * @return The name of the callback code request parameter
	 */
	public abstract String getCallbackCodeRequestParam();

	/***************************************
	 * Returns the name of the HTTP request parameter that contains the request
	 * ID.
	 *
	 * @return The request ID parameter
	 */
	public abstract String getRequestIdParam();

	/***************************************
	 * Processes the response received through the callback URL of a call to
	 * {@link #authorizeAccess(String, boolean, Object...)}. IMPORTANT: This
	 * method must be invoked on the same service instance on which the
	 * authorization has been initiated.
	 *
	 * @param  sCallbackCode sRequestParam The HTTP request of the callback
	 *
	 * @return The access token string received by the callback
	 *
	 * @throws Exception If the processing fails
	 */
	public abstract String processCallback(String sCallbackCode)
		throws Exception;

	/***************************************
	 * Revokes any previously authorized access of the current user for this
	 * service.
	 *
	 * @throws Exception If revoking the access fails
	 */
	public abstract void revokeAccess() throws Exception;

	/***************************************
	 * Returns the configuration entity for this service.
	 *
	 * @return The configuration entity
	 */
	public final Entity getConfig()
	{
		return rConfig;
	}

	/***************************************
	 * Returns a string that uniquely identifies this service instance.
	 *
	 * @return The unique service Id
	 */
	public final String getServiceId()
	{
		return sServiceId;
	}

	/***************************************
	 * Returns the user this service has been created for.
	 *
	 * @return The service user
	 */
	public final Entity getUser()
	{
		return rUser;
	}

	/***************************************
	 * Queries a value from the configuration returned by {@link #getConfig()}
	 * and throws an exception if the value doesn't exist.
	 *
	 * @param  rConfigKey The extra attribute to query from the configuration
	 *
	 * @return The configuration value
	 *
	 * @throws IllegalStateException If the value doesn't exist or if access to
	 *                               it fails
	 */
	protected String getRequiredConfigValue(RelationType<String> rConfigKey)
	{
		Entity rConfig = getConfig();

		try
		{
			if (rConfig.hasExtraAttribute(rConfigKey))
			{
				return rConfig.getExtraAttribute(rConfigKey, null);
			}
			else
			{
				throw new IllegalStateException("Service config undefined: " +
												rConfigKey.getSimpleName());
			}
		}
		catch (StorageException e)
		{
			throw new IllegalStateException("Service config not accessible: " +
											rConfigKey.getSimpleName());
		}
	}

	/***************************************
	 * Initializes this instance.
	 *
	 * @param rUser   The user entity the service access shall be performed for
	 * @param rConfig The configuration entity to read configuration values from
	 */
	private void init(Entity rUser, Entity rConfig)
	{
		this.rUser   = rUser;
		this.rConfig = rConfig;
	}
}
