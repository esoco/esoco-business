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

import java.util.Set;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newInitialValueType;
import static org.obrel.core.RelationTypes.newSetType;

/**
 * Enumeration of the available service types.
 */
public class ExternalServices {

	/**
	 * Contains the prefix for the scopes in {@link #ACCESS_SCOPES}. The
	 * default
	 * value is an empty string.
	 */
	public static final RelationType<String> ACCESS_SCOPE_PREFIX =
		newInitialValueType("");

	/**
	 * A collection of access scopes that are available on a service.
	 */
	public static final RelationType<Set<String>> ACCESS_SCOPES =
		newSetType(true);

	static {
		// must occur before instances of ServiceDefinitionImpl are created!
		RelationTypes.init(ExternalServices.class);
	}
}
