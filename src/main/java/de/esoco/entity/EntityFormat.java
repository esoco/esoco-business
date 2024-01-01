//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.expression.Function;
import org.obrel.core.RelationType;
import org.obrel.type.StandardTypes;

import java.util.Collection;

/**
 * A function that converts an entity into a formatted string value.
 *
 * @author eso
 */
public class EntityFormat<E extends Entity> implements Function<E, String> {

	private final String nullString;

	/**
	 * Creates a new instance.
	 *
	 * @param nullString The string to be displayed if the input entity is NULL
	 */
	public EntityFormat(String nullString) {
		this.nullString = nullString;
	}

	/**
	 * Formats an entity in to a describing string.
	 *
	 * @param entity The entity to format
	 * @return The resulting string
	 */
	public static String toString(Entity entity) {
		EntityDefinition<?> definition = entity.getDefinition();
		RelationType<String> nameAttr = definition.getNameAttribute();

		Collection<RelationType<?>> attributes = definition.getAttributes();
		String result = null;

		if (nameAttr != null) {
			result = entity.get(nameAttr);

			String firstName = entity.get(StandardTypes.FIRST_NAME);

			if (firstName != null && firstName.length() > 0) {
				result = firstName + " " + result;
			}
		} else if (attributes.contains(StandardTypes.INFO)) {
			String info = entity.get(StandardTypes.INFO);

			if (info != null && info.length() > 0) {
				result = info;
			}
		} else {
			RelationType<Enum<?>> typeAttribute = entity.getTypeAttribute();

			if (typeAttribute != null) {
				Enum<?> type = entity.get(typeAttribute);

				if (type != null) {
					result = type.name();
				}
			}
		}

		return result != null ? result : "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String evaluate(E entity) {
		String result = nullString;

		if (entity != null) {
			result = toString(entity);
		}

		return result;
	}
}
