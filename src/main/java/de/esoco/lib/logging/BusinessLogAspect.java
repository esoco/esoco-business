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
package de.esoco.lib.logging;

import de.esoco.data.DataRelationTypes;
import de.esoco.data.SessionData;
import de.esoco.data.SessionManager;
import de.esoco.entity.Entity;

/**
 * A base class for the implementation of business-specific log aspects that
 * need to access business-related data like entities. It provides the method
 * {@link #getLogSource()} that can be used by subclasses to query an entity
 * that identifies the source of a log record. This method requires certain
 * configuration relations that must be injected on a log aspect instance after
 * creation.
 *
 * @author eso
 */
public abstract class BusinessLogAspect<T> extends LogAspect<T> {

	/**
	 * Returns the log source entity for the current context. The context is
	 * determined by querying the {@link SessionData#SESSION_USER} from a
	 * {@link DataRelationTypes#SESSION_MANAGER} set on this instance. If no
	 * session is available or it doesn't contain a session user NULL will be
	 * returned.
	 *
	 * @return The log source entity or NULL if none is available
	 */
	protected Entity getLogSource() {
		SessionManager sessionManager = get(DataRelationTypes.SESSION_MANAGER);
		Entity source = null;

		if (sessionManager != null) {
			try {
				SessionData sessionData = sessionManager.getSessionData();

				if (sessionData != null) {
					source = sessionData.get(SessionData.SESSION_USER);
				}
			} catch (Exception e) {
				// if no access to session data leave source as NULL
			}
		}

		return source;
	}
}
