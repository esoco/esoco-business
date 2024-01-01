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
package de.esoco.data;

import de.esoco.data.element.DataElementList;
import de.esoco.entity.Entity;
import de.esoco.lib.logging.LogLevel;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.core.SerializableRelatedObject;

import java.util.Date;
import java.util.Map;

import static org.obrel.core.RelationTypes.newMapType;
import static org.obrel.core.RelationTypes.newType;

/**
 * A structure that contains data that is associated with a certain session
 * managed by a {@link SessionManager}. All session data is stored and accessed
 * as relations.
 *
 * @author eso
 */
public class SessionData extends SerializableRelatedObject {

	/**
	 * The session user.
	 */
	public static final RelationType<Entity> SESSION_USER = newType();

	/**
	 * The session user.
	 */
	public static final RelationType<String> SESSION_LOGIN_NAME = newType();

	/**
	 * The session-specific user data.
	 */
	public static final RelationType<DataElementList> SESSION_USER_DATA =
		newType();

	/**
	 * The creation time of the session.
	 */
	public static final RelationType<Date> SESSION_START_TIME = newType();

	/**
	 * The session-specific log level.
	 */
	public static final RelationType<LogLevel> SESSION_LOG_LEVEL = newType();

	/**
	 * A mapping from user names to session data objects.
	 */
	public static final RelationType<Map<String, SessionData>> USER_SESSIONS =
		newMapType(true);

	private static final long serialVersionUID = 1L;

	static {
		RelationTypes.init(SessionData.class);
	}

	/**
	 * Creates a new instance.
	 */
	public SessionData() {
		set(SESSION_START_TIME, new Date());
	}

	/**
	 * Updates this instance with the given values.
	 *
	 * @param user      The entity that describes the session user
	 * @param loginName The login name of the session user
	 * @param userData  The user data
	 */
	public void update(Entity user, String loginName,
		DataElementList userData) {
		set(SESSION_USER, user);
		set(SESSION_LOGIN_NAME, loginName);
		set(SESSION_USER_DATA, userData);

		set(SESSION_START_TIME, new Date());
	}
}
