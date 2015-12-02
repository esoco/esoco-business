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

/********************************************************************
 * An interface that represents a request to an external service.
 *
 * @author eso
 */
public interface ExternalServiceRequest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sends this request to the external service and returns the response.
	 *
	 * @return The response of the service
	 *
	 * @throws Exception If sending the request fails
	 */
	public ExternalServiceResponse send() throws Exception;

	/***************************************
	 * Sets the full body of this request. This is mutual exclusive to the
	 * method {@link #setParameter(String, String)} and will override any
	 * existing parameters.
	 *
	 * @param sBodyData The request body
	 */
	public void setBody(String sBodyData);

	/***************************************
	 * Sets a certain request header field.
	 *
	 * @param sName  The name of the header field
	 * @param sValue The header field value
	 */
	public void setHeader(String sName, String sValue);

	/***************************************
	 * Sets a certain request parameter. To set the complete request body for
	 * POST and PUT requests use {@link #setBody(String)} instead.
	 *
	 * @param sName  The name of the parameter
	 * @param sValue The parameter value
	 */
	public void setParameter(String sName, String sValue);
}
