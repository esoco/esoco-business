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

import de.esoco.lib.net.ExternalService.AccessType;


/********************************************************************
 * An interface that defines the methods needed to access external services that
 * are secured by an authorization method (typically the OAuth protocol).
 *
 * @author eso
 */
public interface ExternalServiceAccess
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Performs an authorization of the access to an external service. The
	 * invocation of this method can have two possible results. The first is a
	 * URL that needs to be visited by the user to confirm the authorization
	 * request before the access to the service will be granted. This is
	 * typically the case for services using an OAuth protocol. After the
	 * confirmation has succeeded the remote server will callback this service
	 * with the authorization request and the given callback object will be
	 * notified of the granted access code.
	 *
	 * <p>The second possibility is that the access had previously been granted
	 * already and is still valid or could be refreshed and therefore the
	 * callback object will be invoked immediately. In that case the return
	 * value will be NULL.</p>
	 *
	 * <p>Some external services (e.g. Google) provide access to multiple
	 * functionalities that must be named when requesting access to the service.
	 * This can be done with the access scopes parameter. The scope objects
	 * should have a string representation that can be interpreted by the
	 * corresponding service implementation.</p>
	 *
	 * @param  rServiceDefinition The definition of the service to authorize
	 * @param  rCallback          The callback to be notified of successful
	 *                            authorization
	 * @param  bForceAuth         TRUE to force the authorization even if cached
	 *                            authorization tokens or similar exist
	 * @param  rAccessScopes      The optional access scopes
	 *
	 * @return A verification URL or NULL if no verification is necessary
	 *
	 * @throws Exception If the authorization fails
	 */
	public String authorizeExternalServiceAccess(
		ExternalServiceDefinition rServiceDefinition,
		AuthorizationCallback	  rCallback,
		boolean					  bForceAuth,
		Object... 				  rAccessScopes) throws Exception;

	/***************************************
	 * Creates a request to an external service. The access to the external
	 * service must have been authorized previously by a call to the method
	 * {@link #authorizeExternalServiceAccess(ExternalServiceDefinition,
	 * AuthorizationCallback, boolean, Object...)}.
	 *
	 * @param  rServiceDefinition The type of service to create the request for
	 * @param  eAccessType        The service access type
	 * @param  sRequestUrl        The URL of the service request
	 *
	 * @return The service request instance
	 *
	 * @throws Exception If creating the request fails
	 */
	public ExternalServiceRequest createExternalServiceRequest(
		ExternalServiceDefinition rServiceDefinition,
		AccessType				  eAccessType,
		String					  sRequestUrl) throws Exception;

	/***************************************
	 * Revokes any previously authorized access to an external service. This is
	 * typically handled by removing all internal references and authorization
	 * tokens to the external service for the current user from the system.
	 *
	 * @param  rServiceDefinition The definition of the service to revoke the
	 *                            access to
	 *
	 * @throws Exception In the case of unrecoverable errors
	 */
	public void revokeExternalServiceAccess(
		ExternalServiceDefinition rServiceDefinition) throws Exception;
}
