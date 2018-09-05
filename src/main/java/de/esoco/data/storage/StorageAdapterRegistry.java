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

import de.esoco.storage.StorageException;


/********************************************************************
 * An interface that allows to register and query storage adapters.
 *
 * @author eso
 */
public interface StorageAdapterRegistry
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the registered storage adapter for a {@link StorageAdapterId}.
	 *
	 * @param  rId The adapter ID
	 *
	 * @return The storage adapter that has been registered for the given ID or
	 *         NULL for none
	 *
	 * @throws StorageException If retrieving the storage adapter fails
	 */
	public StorageAdapter getStorageAdapter(StorageAdapterId rId)
		throws StorageException;

	/***************************************
	 * Registers a certain {@link StorageAdapter} instance in the current user's
	 * session and associates it with a unique string ID. Either a reference to
	 * the returned ID or to the adapter must be kept by the invoking party
	 * because the adapter will be garbage collected if neither the ID nor the
	 * adapter are referenced any longer by any strong reference.
	 *
	 * @param  rAdapter The adapter to register
	 *
	 * @return The ID that identifies the adapter
	 *
	 * @throws StorageException If registering the adapter fails
	 */
	public StorageAdapterId registerStorageAdapter(StorageAdapter rAdapter)
		throws StorageException;

	/***************************************
	 * Returns the registered storage adapter for the string representation of a
	 * storage adapter ID as created by {@link StorageAdapterId#toString()}.
	 *
	 * @param  sId The adapter ID
	 *
	 * @return The storage adapter that has been registered for the given ID or
	 *         NULL for none
	 *
	 * @throws StorageException If retrieving the storage adapter fails
	 */
	default StorageAdapter getStorageAdapter(String sId) throws StorageException
	{
		return getStorageAdapter(new StorageAdapterId(sId));
	}
}
