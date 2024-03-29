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
 * A callback interface for the authorization of resources.
 *
 * @author eso
 */
public interface AuthorizationCallback {

	/**
	 * Will be invoked if the authorization failed.
	 *
	 * @param error The error exception
	 */
	void authorizationFailure(Exception error);

	/**
	 * Will be invoked if the authorization was successful.
	 *
	 * @param accessToken The token that grants access to the authorized
	 *                    resource
	 */
	void authorizationSuccess(String accessToken);
}
