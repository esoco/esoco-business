//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

/**
 * An interface that represents a request to an external service.
 *
 * @author eso
 */
public interface ExternalServiceRequest {

	/**
	 * Sends this request to the external service and returns the response.
	 *
	 * @return The response of the service
	 * @throws Exception If sending the request fails
	 */
	ExternalServiceResponse send() throws Exception;

	/**
	 * Sets the full body of this request. This is mutual exclusive to the
	 * method {@link #setParameter(String, String)} and will override any
	 * existing parameters.
	 *
	 * @param bodyData The request body
	 */
	void setBody(String bodyData);

	/**
	 * Sets a certain request header field.
	 *
	 * @param name  The name of the header field
	 * @param value The header field value
	 */
	void setHeader(String name, String value);

	/**
	 * Sets a certain request parameter. To set the complete request body for
	 * POST and PUT requests use {@link #setBody(String)} instead.
	 *
	 * @param name  The name of the parameter
	 * @param value The parameter value
	 */
	void setParameter(String name, String value);
}
