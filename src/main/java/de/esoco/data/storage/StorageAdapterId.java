//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.data.storage;

import org.obrel.core.RelatedObject;

/**
 * Instances of this class that identify storage adapters that are registered
 * with a {@link StorageAdapterRegistry}.
 *
 * @author eso
 */
public class StorageAdapterId extends RelatedObject {

	private String sId;

	/**
	 * Creates a new instance from a string ID.
	 *
	 * @param sId The identifier string
	 */
	public StorageAdapterId(String sId) {
		this.sId = sId;
	}

	/**
	 * Creates a new instance from a numeric ID.
	 *
	 * @param nId The ID
	 */
	public StorageAdapterId(long nId) {
		this(Long.toString(nId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object rObj) {
		return rObj == this || rObj != null && rObj.getClass() == getClass() &&
			sId.equals(((StorageAdapterId) rObj).sId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return sId.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return sId;
	}
}
