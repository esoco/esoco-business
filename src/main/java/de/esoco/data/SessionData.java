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
package de.esoco.data;

import de.esoco.data.element.DataElementList;

import de.esoco.entity.Entity;

import de.esoco.lib.logging.LogLevel;

import java.util.Date;
import java.util.Map;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.core.SerializableRelatedObject;
import org.obrel.type.StandardTypes;

import static org.obrel.core.RelationTypes.newMapType;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * A structure that contains data that is associated with a certain session
 * managed by a {@link SessionManager}. The data is stored and accessed as
 * relations.
 *
 * @author eso
 */
public class SessionData extends SerializableRelatedObject
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The session user. */
	public static final RelationType<Entity> SESSION_USER = newType();

	/** The session user. */
	public static final RelationType<String> SESSION_LOGIN_NAME = newType();

	/** The session-specific user data. */
	public static final RelationType<DataElementList> SESSION_USER_DATA =
		newType();

	/** The session-specific log level. */
	public static final RelationType<LogLevel> SESSION_LOG_LEVEL = newType();

	/** A mapping from user names to session data objects. */
	public static final RelationType<Map<String, SessionData>> USER_SESSIONS =
		newMapType(true);

	static
	{
		RelationTypes.init(SessionData.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with certain user data.
	 *
	 * @param rUser      The entity that describes the session user
	 * @param sLoginName The login name of the session user
	 * @param rUserData  The user data
	 */
	public SessionData(Entity		   rUser,
					   String		   sLoginName,
					   DataElementList rUserData)
	{
		set(SESSION_USER, rUser);
		set(SESSION_LOGIN_NAME, sLoginName);
		set(SESSION_USER_DATA, rUserData);
		set(StandardTypes.START_DATE, new Date());
	}
}
