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

import java.util.ConcurrentModificationException;

/**
 * A runtime exception to signal the concurrent modification of an entity.
 *
 * @author eso
 */
public class ConcurrentEntityModificationException
	extends ConcurrentModificationException {

	private static final long serialVersionUID = 1L;

	private String entityId;

	/**
	 * Default constructor.
	 */
	public ConcurrentEntityModificationException() {
	}

	/**
	 * Creates a new instance.
	 *
	 * @param entity  The modified entity
	 * @param message The error message
	 */
	public ConcurrentEntityModificationException(Entity entity,
		String message) {
		super(message);

		entityId = entity.getGlobalId();
	}

	/**
	 * Returns the global ID of the concurrently modified entity.
	 *
	 * @return The global entity ID
	 */
	public final String getEntityId() {
		return entityId;
	}
}
