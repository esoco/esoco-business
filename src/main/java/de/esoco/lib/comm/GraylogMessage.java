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

import de.esoco.lib.json.JsonUtil;
import de.esoco.lib.text.TextConvert;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.obrel.core.RelatedObject;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newInitialValueType;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * A data object that holds the informations for a Graylog message in the
 * Graylog extended log format (GELF).
 *
 * @author eso
 */
public class GraylogMessage extends RelatedObject
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the RFC 5424 severity levels used by graylog.
	 */
	public enum Level
	{
		EMERGENCY, ALERT, CRITICAL, ERROR, WARNING, NOTICE, INFORMATIONAL, DEBUG
	}

	//~ Static fields/initializers ---------------------------------------------

	/** The message format version. */
	public static final RelationType<String> VERSION =
		newInitialValueType("1.1");

	/** The host sending the message. */
	public static final RelationType<String> HOST = newType();

	/** The short log message. */
	public static final RelationType<String> SHORT_MESSAGE = newType();

	/** The full log message. */
	public static final RelationType<String> FULL_MESSAGE = newType();

	/** The log timestamp in milliseconds. */
	public static final RelationType<Long> TIMESTAMP = newType();

	/** The severity level. */
	public static final RelationType<Level> LEVEL = newType();

	/** Optional field: the name of the file in which the logging occurred. */
	public static final RelationType<String> _FILE_NAME = newType();

	/**
	 * Optional field: the line number of the file in which the logging
	 * occurred.
	 */
	public static final RelationType<Integer> _LINE_NUMBER = newType();

	/**
	 * Optional field: a description of the origin that caused the generation of
	 * the log message. This is typically something like a person, process, or
	 * system.
	 */
	public static final RelationType<String> _ORIGIN = newType();

	private static final String LOCALHOST;

	static
	{
		try
		{
			LOCALHOST = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
			throw new IllegalStateException(e);
		}

		RelationTypes.init(GraylogMessage.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rLevel        The severity level
	 * @param sShortMessage The short message
	 * @param sFullMessage  The full message or NULL for none
	 */
	@SuppressWarnings("boxing")
	public GraylogMessage(Level  rLevel,
						  String sShortMessage,
						  String sFullMessage)
	{
		assert rLevel != null && sShortMessage != null &&
			   sShortMessage.length() > 0;

		set(TIMESTAMP, System.currentTimeMillis());
		set(HOST, LOCALHOST);
		set(LEVEL, rLevel);
		set(SHORT_MESSAGE, sShortMessage);

		if (sFullMessage != null)
		{
			set(FULL_MESSAGE, sFullMessage);
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns a string containing a JSON representation of this message.
	 *
	 * @return The JSON string for this message
	 */
	public String toJson()
	{
		StringBuilder aJsonMessage = new StringBuilder("{\n");

		for (Relation<?> rRelation : getRelations(null))
		{
			if (appendRelation(aJsonMessage, rRelation))
			{
				aJsonMessage.append(",\n");
			}
		}

		// remove trailing ',\n'
		aJsonMessage.setLength(aJsonMessage.length() - 2);
		aJsonMessage.append("\n}\0");

		return aJsonMessage.toString();
	}

	/***************************************
	 * Appends a relation to a JSON string.
	 *
	 * @param  rJsonMessage The string builder
	 * @param  rRelation    The relation
	 *
	 * @return TRUE if a relation has been appended
	 */
	private boolean appendRelation(
		StringBuilder rJsonMessage,
		Relation<?>   rRelation)
	{
		Object  rValue    = rRelation.getTarget();
		boolean bHasValue = rValue != null;

		if (bHasValue)
		{
			RelationType<?> rRelationType = rRelation.getType();
			Class<?>	    rDatatype     = rRelationType.getTargetType();

			rJsonMessage.append('\"');
			rJsonMessage.append(rRelationType.getSimpleName().toLowerCase());
			rJsonMessage.append("\":");

			if (Number.class.isAssignableFrom(rDatatype))
			{
				if (rRelationType == TIMESTAMP)
				{
					long   nTimestamp    = ((Long) rValue).longValue();
					String sMilliseconds =
						TextConvert.padLeft("" + (nTimestamp % 1000), 4, '0');

					rJsonMessage.append(nTimestamp / 1000);
					rJsonMessage.append('.');
					rJsonMessage.append(sMilliseconds);
				}
				else
				{
					rJsonMessage.append(rValue.toString());
				}
			}
			else if (rRelationType == LEVEL)
			{
				rJsonMessage.append(((Level) rValue).ordinal());
			}
			else
			{
				rJsonMessage.append('\"');
				JsonUtil.escapeJsonValue(rJsonMessage, rValue.toString());
				rJsonMessage.append('\"');
			}
		}

		return bHasValue;
	}
}
