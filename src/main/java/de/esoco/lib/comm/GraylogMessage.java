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
package de.esoco.lib.comm;

import de.esoco.lib.json.Json;
import de.esoco.lib.text.TextConvert;
import org.obrel.core.RelatedObject;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.obrel.core.RelationTypes.newInitialValueType;
import static org.obrel.core.RelationTypes.newType;

/**
 * A data object that holds the informations for a Graylog message in the
 * Graylog extended log format (GELF).
 *
 * @author eso
 */
public class GraylogMessage extends RelatedObject {

	/**
	 * Enumeration of the RFC 5424 severity levels used by graylog.
	 */
	public enum Level {
		EMERGENCY, ALERT, CRITICAL, ERROR, WARNING, NOTICE, INFORMATIONAL,
		DEBUG
	}

	/**
	 * The message format version.
	 */
	public static final RelationType<String> VERSION =
		newInitialValueType("1.1");

	/**
	 * The host sending the message.
	 */
	public static final RelationType<String> HOST = newType();

	/**
	 * The short log message.
	 */
	public static final RelationType<String> SHORT_MESSAGE = newType();

	/**
	 * The full log message.
	 */
	public static final RelationType<String> FULL_MESSAGE = newType();

	/**
	 * The log timestamp in milliseconds.
	 */
	public static final RelationType<Long> TIMESTAMP = newType();

	/**
	 * The severity level.
	 */
	public static final RelationType<Level> LEVEL = newType();

	/**
	 * Optional field: the name of the file in which the logging occurred.
	 */
	public static final RelationType<String> _FILE_NAME = newType();

	/**
	 * Optional field: the line number of the file in which the logging
	 * occurred.
	 */
	public static final RelationType<Integer> _LINE_NUMBER = newType();

	/**
	 * Optional field: a description of the origin that caused the
	 * generation of
	 * the log message. This is typically something like a person, process, or
	 * system.
	 */
	public static final RelationType<String> _ORIGIN = newType();

	private static final String LOCALHOST;

	static {
		try {
			LOCALHOST = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new IllegalStateException(e);
		}

		RelationTypes.init(GraylogMessage.class);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param level        The severity level
	 * @param shortMessage The short message
	 * @param fullMessage  The full message or NULL for none
	 */
	@SuppressWarnings("boxing")
	public GraylogMessage(Level level, String shortMessage,
		String fullMessage) {
		assert
			level != null && shortMessage != null && shortMessage.length() > 0;

		set(TIMESTAMP, System.currentTimeMillis());
		set(HOST, LOCALHOST);
		set(LEVEL, level);
		set(SHORT_MESSAGE, shortMessage);

		if (fullMessage != null) {
			set(FULL_MESSAGE, fullMessage);
		}
	}

	/**
	 * Returns a string containing a JSON representation of this message.
	 *
	 * @return The JSON string for this message
	 */
	public String toJson() {
		StringBuilder jsonMessage = new StringBuilder("{\n");

		for (Relation<?> relation : getRelations(null)) {
			if (appendRelation(jsonMessage, relation)) {
				jsonMessage.append(",\n");
			}
		}

		// remove trailing ',\n'
		jsonMessage.setLength(jsonMessage.length() - 2);
		jsonMessage.append("\n}\0");

		return jsonMessage.toString();
	}

	/**
	 * Appends a relation to a JSON string.
	 *
	 * @param jsonMessage The string builder
	 * @param relation    The relation
	 * @return TRUE if a relation has been appended
	 */
	private boolean appendRelation(StringBuilder jsonMessage,
		Relation<?> relation) {
		Object value = relation.getTarget();
		boolean hasValue = value != null;

		if (hasValue) {
			RelationType<?> relationType = relation.getType();
			Class<?> datatype = relationType.getTargetType();

			jsonMessage.append('\"');
			jsonMessage.append(relationType.getSimpleName().toLowerCase());
			jsonMessage.append("\":");

			if (Number.class.isAssignableFrom(datatype)) {
				if (relationType == TIMESTAMP) {
					long timestamp = ((Long) value).longValue();
					String milliseconds =
						TextConvert.padLeft("" + (timestamp % 1000), 4, '0');

					jsonMessage.append(timestamp / 1000);
					jsonMessage.append('.');
					jsonMessage.append(milliseconds);
				} else {
					jsonMessage.append(value);
				}
			} else if (relationType == LEVEL) {
				jsonMessage.append(((Level) value).ordinal());
			} else {
				jsonMessage.append('\"');
				jsonMessage.append(Json.escape(value.toString()));
				jsonMessage.append('\"');
			}
		}

		return hasValue;
	}
}
