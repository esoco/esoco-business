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
package de.esoco.entity;

/**
 * An interface for the implementation of class-specific entity caches.
 *
 * @author eso
 */
public interface EntityCache<E extends Entity> {

	/**
	 * Caches a certain entity.
	 *
	 * @param entity The entity to cache
	 */
	void cacheEntity(E entity);

	/**
	 * Returns the cached entity with a certain ID or NULL if no cached entity
	 * exists.
	 *
	 * @param id The entity ID
	 * @return The entity with the given ID or NULL for none
	 */
	E getEntity(long id);
}
