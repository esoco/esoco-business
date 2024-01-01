//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.history.HistoryRecord;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.manage.TransactionManager;
import de.esoco.storage.Storage;
import de.esoco.storage.StorageDefinition;
import de.esoco.storage.StorageException;
import de.esoco.storage.StorageManager;
import de.esoco.storage.impl.jdbc.JdbcStorageDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.obrel.core.RelationType;

import java.util.Arrays;
import java.util.List;

import static de.esoco.entity.TestContact.CONTACT_TYPE;
import static de.esoco.entity.TestContact.CONTACT_VALUE;
import static de.esoco.entity.TestPerson.ADDRESS;
import static de.esoco.entity.TestPerson.AGE;
import static de.esoco.entity.TestPerson.CITY;
import static de.esoco.entity.TestPerson.FORENAME;
import static de.esoco.entity.TestPerson.LASTNAME;
import static de.esoco.entity.TestPerson.POSTAL_CODE;

/**
 * A base class for entity storage tests.
 *
 * @author eso
 */
public abstract class AbstractEntityStorageTest {

	/**
	 * The storage reference (initialized in setUp())
	 */
	protected Storage storage;

	/**
	 * Test initialization.
	 */
	@BeforeAll
	public static void init() throws ClassNotFoundException {
		Class.forName("org.h2.Driver");
	}

	/**
	 * Initializes the storage.
	 */
	@BeforeEach
	public void setUp() throws Exception {
		StorageDefinition def = JdbcStorageDefinition.create(
			"jdbc:h2:mem:testdb;user=sa;password=");

		EntityManager.init();
		StorageManager.setDefaultStorage(def);

		// using TestPerson to get the default storage
		storage = StorageManager.getStorage(TestPerson.class);

		TransactionManager.begin();
		TransactionManager.addTransactionElement(storage);

		storage.initObjectStorage(TestPerson.class);
		storage.initObjectStorage(ExtraAttribute.class);
		storage.initObjectStorage(HistoryRecord.class);
	}

	/**
	 * Performs a rollback and closes the storage.
	 */
	@AfterEach
	public void tearDown() throws StorageException, TransactionException {
		TransactionManager.rollback();
	}

	/**
	 * Adds contacts to a person entity.
	 *
	 * @param entity   The person entity
	 * @param contacts The contacts in the order email, phone, fax
	 */
	protected void addContacts(Entity entity, String... contacts) {
		String[] types = new String[] { "EML", "TEL", "FAX" };
		int index = 0;

		RelationType<List<TestContact>> childAttr =
			(entity instanceof TestPerson ?
			 TestPerson.CONTACTS :
			 TestContact.CHILDREN);

		for (String contact : contacts) {
			String type = types[index++];

			if (contact != null) {
				TestContact testContact = new TestContact();

				testContact.set(CONTACT_TYPE, type);
				testContact.set(CONTACT_VALUE, contact);

				entity.addChildren(childAttr, testContact);
			}
		}
	}

	/**
	 * Creates a new person entity.
	 *
	 * @param attributes The attributes of the person (6 elements)
	 * @return The new entity
	 */
	protected TestPerson createPerson(String[] attributes) {
		TestPerson person = new TestPerson();

		person.set(LASTNAME, attributes[0]);
		person.set(FORENAME, attributes[1]);
		person.set(ADDRESS, attributes[2]);
		person.set(POSTAL_CODE, attributes[3]);
		person.set(CITY, attributes[4]);
		person.set(AGE, Integer.parseInt(attributes[5]));

		if (attributes.length > 6) {
			addContacts(person,
				Arrays.copyOfRange(attributes, 6, attributes.length));
		}

		return person;
	}

	/**
	 * Executes a query and returns the resulting entities in a list.
	 *
	 * @param criteria The query criteria
	 * @return A list containing the queried entities
	 * @throws StorageException On errors
	 */
	protected List<TestPerson> executePersonQuery(Predicate<Entity> criteria)
		throws StorageException {
		return EntityManager.queryEntities(TestPerson.class, criteria, 1000);
	}
}
