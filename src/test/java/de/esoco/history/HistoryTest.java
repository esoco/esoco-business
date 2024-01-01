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
package de.esoco.history;

import de.esoco.entity.AbstractEntityStorageTest;
import de.esoco.entity.Entity;
import de.esoco.entity.TestPerson;
import de.esoco.lib.manage.TransactionException;
import de.esoco.storage.StorageException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.esoco.history.HistoryRecord.HistoryType.GROUP;
import static de.esoco.history.HistoryRecord.HistoryType.INFO;
import static de.esoco.history.HistoryRecord.HistoryType.NOTE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of history functions.
 *
 * @author eso
 */
public class HistoryTest extends AbstractEntityStorageTest {

	private Entity origin;

	private Entity target;

	/**
	 * Initializes the storage for the tests.
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		origin = createPerson(
			new String[] { "Origin", "Test", "-", "-", "-", "11" });
		target = createPerson(
			new String[] { "Target", "Test", "-", "-", "-", "22" });

		storage.initObjectStorage(HistoryRecord.class);
		storage.initObjectStorage(TestPerson.class);

		storage.store(origin);
		storage.store(target);
	}

	/**
	 * Test of discarding empty hierarchy.
	 */
	@Test
	public void testDiscardEmptyHierarchy()
		throws StorageException, TransactionException {
		HistoryManager.begin(origin, target, "TEST");
		HistoryManager.begin(origin, target, "SUBTEST");
		HistoryManager.commit(false);
		HistoryManager.commit(false);

		List<HistoryRecord> history =
			HistoryManager.getHistoryFor(target, GROUP);

		assertEquals(0, history.size());
	}

	/**
	 * History hierarchy test.
	 */
	@Test
	public void testHierarchy() throws StorageException, TransactionException {
		HistoryManager.begin(origin, target, "TEST");
		HistoryManager.record(INFO, null, target, "TESTINFO");
		HistoryManager.record(NOTE, null, target, "TESTNOTE");
		HistoryManager.commit(true);

		List<HistoryRecord> history =
			HistoryManager.getHistoryFor(target, GROUP);

		assertEquals(1, history.size());

		HistoryRecord record = history.get(0);

		assertEquals("TEST", record.get(HistoryRecord.VALUE));

		List<HistoryRecord> records = record.get(HistoryRecord.DETAILS);

		assertEquals(2, records.size());
	}

	/**
	 * Basic history test.
	 */
	@Test
	public void testHistory() throws StorageException, TransactionException {
		HistoryManager.record(INFO, origin, target, "TEST");

		List<HistoryRecord> history =
			HistoryManager.getHistoryFor(target, INFO);

		assertEquals(1, history.size());
		assertEquals("TEST", history.get(0).get(HistoryRecord.VALUE));
	}

	/**
	 * Test of saving empty hierarchy.
	 */
	@Test
	public void testKeepEmptyHierarchy()
		throws StorageException, TransactionException {
		HistoryManager.begin(origin, target, "TEST");
		HistoryManager.commit(true);

		List<HistoryRecord> history =
			HistoryManager.getHistoryFor(target, GROUP);

		assertEquals(1, history.size());

		HistoryRecord record = history.get(0);

		assertEquals("TEST", record.get(HistoryRecord.VALUE));

		List<HistoryRecord> records = record.get(HistoryRecord.DETAILS);

		assertEquals(0, records.size());
	}
}
