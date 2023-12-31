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
package de.esoco.entity;

import de.esoco.lib.logging.LogLevel;
import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.HasProperties;
import de.esoco.storage.StorageRelationTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.RelationType;

import static de.esoco.entity.EntityRelationTypes.arbitraryEntityAttribute;

import static de.esoco.lib.property.UserInterfaceProperties.CONTENT_TYPE;
import static de.esoco.lib.property.UserInterfaceProperties.MAX_CHARS;

import static org.obrel.core.RelationTypeModifier.FINAL;
import static org.obrel.core.RelationTypes.newType;

/**
 * The definition of an entity to store log records in a storage.
 *
 * @author eso
 */
@RelationTypeNamespace("de.esoco.entity.log")
public class LogEntry extends Entity {

	/**
	 * The log level.
	 */
	public static final RelationType<LogLevel> LEVEL = newType(FINAL);

	/**
	 * The log time.
	 */
	public static final RelationType<Date> TIME = newType(FINAL);

	/**
	 * The entity that is the source of the log entry (typically a person).
	 */
	public static final RelationType<Entity> SOURCE =
		arbitraryEntityAttribute(FINAL);

	/**
	 * The log message.
	 */
	public static final RelationType<String> MESSAGE = newType(FINAL);

	/**
	 * The prefix for global entity IDs
	 */
	public static final String ID_PREFIX = "LOG";

	/**
	 * The storage name
	 */
	public static final String STORAGE_NAME = "log";

	/**
	 * The attribute display properties map.
	 */
	public static final Map<RelationType<?>, HasProperties>
		ATTRIBUTE_DISPLAY_PROPERTIES =
		new HashMap<RelationType<?>, HasProperties>();

	private static final long serialVersionUID = 1L;

	static {
		MESSAGE.set(StorageRelationTypes.STORAGE_LENGTH,
			Integer.valueOf(16000));

		setAttributeDisplayProperty(LogEntry.class, CONTENT_TYPE,
			ContentType.DATE_TIME, TIME);
		setAttributeDisplayProperty(LogEntry.class, 20, MAX_CHARS, SOURCE);
	}
}
