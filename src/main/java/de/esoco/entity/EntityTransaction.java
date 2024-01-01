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

import de.esoco.lib.manage.TransactionManager;
import de.esoco.lib.manage.Transactional;
import de.esoco.storage.Storage;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;

/**
 * A transaction implementation that stores an entity instance on a call to the
 * {@link #commit()} method. The entity will then be stored in the storage that
 * is associated with it's entity definition by performing a lookup with the
 * method {@link StorageManager#getStorage(Object)}.
 *
 * <p>Instances of this class are intended to be used only inside transactions.
 * They must be added to a thread's current transaction by means of the method
 * {@link TransactionManager#addTransactionElement(Transactional)}. If invoked
 * outside a transaction an exception will occur.</p>
 *
 * @author eso
 */
public class EntityTransaction implements Transactional {

	// flag that can be set by package test classes to enable the test mode
	private static boolean testMode = false;

	private Entity entity;

	/**
	 * Creates a new transaction for a particular entity.
	 *
	 * @param entity The entity to wrap into the transaction
	 */
	public EntityTransaction(Entity entity) {
		this.entity = entity;
	}

	/**
	 * Package-internal method that can be used by (unit) tests to enable the
	 * test mode. In this mode history records will be stored in the history
	 * storage but not committed so that the test code can perform a
	 * rollback of
	 * any changes.
	 */
	static void activateTestMode() {
		testMode = true;
	}

	/**
	 * Commits the entity by storing it in the storage that is associated with
	 * it's entity definition in the current thread. The storage will be added
	 * to an active transaction of the current thread's by invoking the method
	 * {@link TransactionManager#addTransactionElement(Transactional)}. If no
	 * such transaction is currently active an exception will occur.
	 */
	@Override
	public void commit() throws StorageException {
		Storage storage = StorageManager.getStorage(entity.getClass());

		storage.store(entity);

		if (!testMode) {
			// do not add in test mode to prevent commit during test
			// the transaction will release the storage after  use, so it is
			// not
			// necessary to have a finally block with a release call here
			TransactionManager.addTransactionElement(storage);
		}

		entity = null;
	}

	/**
	 * This method simply sets the entity reference to NULL.
	 */
	@Override
	public void rollback() {
		entity = null;
	}
}
