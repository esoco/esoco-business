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

import de.esoco.data.storage.StorageAdapter;
import de.esoco.data.storage.StorageAdapterId;
import de.esoco.data.storage.StorageAdapterRegistry;

import java.util.List;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newType;

/**
 * Contains specific relation type definitions for the data package.
 *
 * @author eso
 */
public class DataRelationTypes {

	/**
	 * A reference to a storage adapter ID string.
	 */
	public static final RelationType<StorageAdapterId> STORAGE_ADAPTER_ID =
		newType();

	/**
	 * The ID of a storage adapter that is a child to another.
	 */
	public static final RelationType<StorageAdapterId>
		CHILD_STORAGE_ADAPTER_ID = newType();

	/**
	 * A list of storage adapter IDs.
	 */
	public static final RelationType<List<StorageAdapterId>>
		STORAGE_ADAPTER_IDS = newListType();

	/**
	 * A relation type to store a storage adapter.
	 */
	public static final RelationType<StorageAdapter> STORAGE_ADAPTER =
		newType();

	/**
	 * A relation type to store a storage adapter registry.
	 */
	public static final RelationType<StorageAdapterRegistry>
		STORAGE_ADAPTER_REGISTRY = newType();

	/**
	 * A relation to a session manager instance.
	 */
	public static final RelationType<SessionManager> SESSION_MANAGER =
		newType();

	/**
	 * A relation type that contains an attribute that should be used as a flag
	 * value on result objects.
	 */
	public static final RelationType<RelationType<?>> FLAG_ATTRIBUTE =
		newType();

	static {
		RelationTypes.init(DataRelationTypes.class);
	}

	/**
	 * Private, only static use.
	 */
	private DataRelationTypes() {
	}
}
