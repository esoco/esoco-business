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

	private Entity rOrigin;

	private Entity rTarget;

	/**
	 * Initializes the storage for the tests.
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		rOrigin = createPerson(
			new String[] { "Origin", "Test", "-", "-", "-", "11" });
		rTarget = createPerson(
			new String[] { "Target", "Test", "-", "-", "-", "22" });

		rStorage.initObjectStorage(HistoryRecord.class);
		rStorage.initObjectStorage(TestPerson.class);

		rStorage.store(rOrigin);
		rStorage.store(rTarget);
	}

	/**
	 * Test of discarding empty hierarchy.
	 */
	@Test
	public void testDiscardEmptyHierarchy()
		throws StorageException, TransactionException {
		HistoryManager.begin(rOrigin, rTarget, "TEST");
		HistoryManager.begin(rOrigin, rTarget, "SUBTEST");
		HistoryManager.commit(false);
		HistoryManager.commit(false);

		List<HistoryRecord> rHistory =
			HistoryManager.getHistoryFor(rTarget, GROUP);

		assertEquals(0, rHistory.size());
	}

	/**
	 * History hierarchy test.
	 */
	@Test
	public void testHierarchy() throws StorageException, TransactionException {
		HistoryManager.begin(rOrigin, rTarget, "TEST");
		HistoryManager.record(INFO, null, rTarget, "TESTINFO");
		HistoryManager.record(NOTE, null, rTarget, "TESTNOTE");
		HistoryManager.commit(true);

		List<HistoryRecord> rHistory =
			HistoryManager.getHistoryFor(rTarget, GROUP);

		assertEquals(1, rHistory.size());

		HistoryRecord rRecord = rHistory.get(0);

		assertEquals("TEST", rRecord.get(HistoryRecord.VALUE));

		List<HistoryRecord> rRecords = rRecord.get(HistoryRecord.DETAILS);

		assertEquals(2, rRecords.size());
	}

	/**
	 * Basic history test.
	 */
	@Test
	public void testHistory() throws StorageException, TransactionException {
		HistoryManager.record(INFO, rOrigin, rTarget, "TEST");

		List<HistoryRecord> rHistory =
			HistoryManager.getHistoryFor(rTarget, INFO);

		assertEquals(1, rHistory.size());
		assertEquals("TEST", rHistory.get(0).get(HistoryRecord.VALUE));
	}

	/**
	 * Test of saving empty hierarchy.
	 */
	@Test
	public void testKeepEmptyHierarchy()
		throws StorageException, TransactionException {
		HistoryManager.begin(rOrigin, rTarget, "TEST");
		HistoryManager.commit(true);

		List<HistoryRecord> rHistory =
			HistoryManager.getHistoryFor(rTarget, GROUP);

		assertEquals(1, rHistory.size());

		HistoryRecord rRecord = rHistory.get(0);

		assertEquals("TEST", rRecord.get(HistoryRecord.VALUE));

		List<HistoryRecord> rRecords = rRecord.get(HistoryRecord.DETAILS);

		assertEquals(0, rRecords.size());
	}
}
