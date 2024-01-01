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

import de.esoco.lib.expression.predicate.ElementPredicate;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.property.SortDirection;
import de.esoco.storage.StorageException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static de.esoco.entity.EntityPredicates.hasExtraAttribute;
import static de.esoco.entity.EntityPredicates.ifAttribute;
import static de.esoco.entity.ExtraAttributes.newExtraAttribute;
import static de.esoco.entity.TestContact.CONTACT_VALUE;
import static de.esoco.entity.TestPerson.AGE;
import static de.esoco.entity.TestPerson.CITY;
import static de.esoco.entity.TestPerson.CONTACTS;
import static de.esoco.entity.TestPerson.FORENAME;
import static de.esoco.entity.TestPerson.LASTNAME;
import static de.esoco.entity.TestPerson.PARENT;
import static de.esoco.lib.expression.CollectionPredicates.elementOf;
import static de.esoco.lib.expression.Predicates.alwaysTrue;
import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.greaterOrEqual;
import static de.esoco.lib.expression.Predicates.greaterThan;
import static de.esoco.lib.expression.Predicates.lessOrEqual;
import static de.esoco.storage.StoragePredicates.like;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for the JDBC storage implementation.
 *
 * @author eso
 */
@SuppressWarnings("boxing")
public class EntityStorageTest extends AbstractEntityStorageTest {

	private static final RelationType<String> XA1 = newExtraAttribute();

	private static final RelationType<String> XA2 = newExtraAttribute();

	private static final RelationType<Integer> XA_INT = newExtraAttribute();

	private static final RelationType<Boolean> XA_FLAG = newExtraAttribute();

	private static final RelationType<Date> XA_DATE = newExtraAttribute();

	private static final RelationType<List<String>> XA_LIST =
		newExtraAttribute();

	private static final Date TEST_DATE = new Date();

	private static final int TEST_DATA_SIZE = 5;

	private static final String[][] TEST_DATA;

	static {
		RelationTypes.init(EntityStorageTest.class);

		TEST_DATA = new String[TEST_DATA_SIZE][];

		for (int i = 1; i <= TEST_DATA_SIZE; i++) {
			String email = i < TEST_DATA_SIZE ? "test" + i + "@test.com" :
			               null;

			TEST_DATA[i - 1] =
				new String[] { "Test" + i, "First" + (TEST_DATA_SIZE - i + 1),
					"Street" + i, "Postal" + i, "City" + i, "4" + i, email,
					i + "23-456789" };
		}
	}

	/**
	 * Invalidates the entity cache after
	 */
	@AfterEach
	public void afterTest() {
		EntityManager.invalidateCache();
	}

	/**
	 * Initializes the storage for the tests.
	 */
	@BeforeEach
	@Override
	public void setUp() throws Exception {
		super.setUp();

		for (String[] testPersonData : TEST_DATA) {
			EntityManager.storeEntity(createPerson(testPersonData), null);
		}
	}

	/**
	 * Tests queries by extra attributes.
	 */
	@Test
	public void testExtraAttributeQuery()
		throws StorageException, TransactionException {
		setupExtraAttributes();

		Entity entity =
			EntityManager.queryEntityByExtraAttribute(XA_INT, 42, true);

		assertEquals(TestPerson.class, entity.getClass());
		assertEquals("Test1", entity.get(LASTNAME));
		assertEquals("XA1-Test", entity.getExtraAttribute(XA1, null));
		assertEquals(TEST_DATE, entity.getExtraAttribute(XA_DATE, null));
		assertEquals(Integer.valueOf(42),
			entity.getExtraAttribute(XA_INT, null));

		entity =
			EntityManager.queryEntityByExtraAttribute(XA_FLAG, false, true);

		assertEquals("Test" + TEST_DATA_SIZE, entity.get(LASTNAME));

		try {
			EntityManager.queryEntityByExtraAttribute(XA2, "XA2-Test", true);
			fail();
		} catch (IllegalStateException e) {
			// this should happen
		}

		Collection<? extends Entity> entities =
			EntityManager.queryEntitiesByExtraAttribute(XA1, "XA1-Test",
				Integer.MAX_VALUE);

		assertEquals(2, entities.size());

		entities = EntityManager.queryEntitiesByExtraAttribute(XA_LIST,
			Arrays.asList("L1", "L2"), Integer.MAX_VALUE);

		assertEquals(1, entities.size());
		assertEquals("Test" + TEST_DATA_SIZE,
			entities.iterator().next().get(LASTNAME));

		entities =
			EntityManager.queryEntitiesByExtraAttribute(TestPerson.class, XA1,
				"XA1-Test", Integer.MAX_VALUE);

		assertEquals(2, entities.size());
	}

	/**
	 * Test of storing, updating and accessing extra entity attributes.
	 */
	@Test
	public void testExtraAttributeReference()
		throws StorageException, TransactionException {
		setupExtraAttributes();

		List<TestPerson> entities =
			EntityManager.queryEntities(TestPerson.class,
				hasExtraAttribute(TestPerson.class, ExtraAttribute.KEY
					.is(equalTo(XA1))
					.and(ExtraAttribute.VALUE.is(
						elementOf("XA1-Test", "XA2-Test")))), 10);

		assertEquals(2, entities.size());

		entities = EntityManager.queryEntities(TestPerson.class,
			hasExtraAttribute(TestPerson.class,
				ExtraAttribute.VALUE.is(like("%-Test"))), 10);
		assertEquals(2, entities.size());

		entities = EntityManager.queryEntities(TestPerson.class,
			hasExtraAttribute(TestPerson.class,
				ExtraAttribute.VALUE.is(equalTo("42"))), 10);
		assertEquals(1, entities.size());
	}

	/**
	 * Test of storing, updating and accessing extra entity attributes.
	 */
	@Test
	public void testExtraAttributes()
		throws StorageException, TransactionException {
		setupExtraAttributes();

		Entity person = queryPersonByLastName("Test1");

		assertEquals("XA1-Test", person.getExtraAttribute(XA1, null));
		assertEquals("XA2-Test", person.getExtraAttribute(XA2, null));
		assertEquals(Integer.valueOf(42),
			person.getExtraAttribute(XA_INT, null));
		assertTrue(person.getExtraAttribute(XA_FLAG, null));
		assertEquals(3, person.getExtraAttribute(XA_LIST, null).size());
		assertEquals(Arrays.asList("L1", "L2", "L3"),
			person.getExtraAttribute(XA_LIST, null));

		person.setExtraAttribute(XA1, "XA1-Updated");
		person.setExtraAttribute(XA_INT, -42);
		person.setExtraAttribute(XA_FLAG, false);
		person.setExtraAttribute(XA_LIST, Arrays.asList("L1", "L2"));
		EntityManager.storeEntity(person, null);

		person = queryPersonByLastName("Test1");

		assertEquals("XA1-Updated", person.getExtraAttribute(XA1, null));
		assertEquals("XA2-Test", person.getExtraAttribute(XA2, null));
		assertEquals(Integer.valueOf(-42),
			person.getExtraAttribute(XA_INT, null));
		assertFalse(person.getExtraAttribute(XA_FLAG, null));
		assertEquals(2, person.getExtraAttribute(XA_LIST, null).size());
		assertEquals(Arrays.asList("L1", "L2"),
			person.getExtraAttribute(XA_LIST, null));
	}

	/**
	 * Test of storage updates.
	 */
	@Test
	public void testHierarchy() throws StorageException, TransactionException {
		createHierarchy(1);

		TestPerson person;

		person = queryPersonByLastName("Test1");

		TestPerson subPerson = queryPersonByLastName("SubTest11");

		assertEquals(person, subPerson.get(PARENT));
		assertEquals("SubFirst11", subPerson.get(FORENAME));
		assertEquals(2, subPerson.get(CONTACTS).size());
	}

	/**
	 * Test of queries.
	 */
	@Test
	public void testQuery() throws StorageException {
		assertEquals(TEST_DATA_SIZE, executePersonQuery(null).size());
		assertEquals(TEST_DATA_SIZE - 1,
			executePersonQuery(ifAttribute(AGE, greaterOrEqual(42))).size());
		assertEquals(TEST_DATA_SIZE - 2,
			executePersonQuery(ifAttribute(AGE, greaterThan(42))).size());
		assertEquals(2, executePersonQuery(
			ifAttribute(AGE, greaterThan(42)).and(
				ifAttribute(AGE, lessOrEqual(44)))).size());
		assertEquals(1,
			executePersonQuery(ifAttribute(CITY, like("%2"))).size());
		assertEquals(2, executePersonQuery(
			ifAttribute(LASTNAME, elementOf("Test1", "Test2"))).size());

		assertEquals(2, queryPersonByLastName("Test1").get(CONTACTS).size());
		assertEquals(1, queryPersonByLastName("Test" + TEST_DATA_SIZE)
			.get(CONTACTS)
			.size());
	}

	/**
	 * Test of sorted queries.
	 */
	@Test
	public void testSortedQuery() throws StorageException {
		ElementPredicate<Entity, String> sortPredicate =
			ifAttribute(FORENAME, alwaysTrue());

		sortPredicate.set(MetaTypes.SORT_DIRECTION, SortDirection.ASCENDING);

		List<TestPerson> entities = executePersonQuery(sortPredicate);

		assertEquals("First1", entities.get(0).get(FORENAME));

		sortPredicate.set(MetaTypes.SORT_DIRECTION, SortDirection.DESCENDING);

		entities = executePersonQuery(sortPredicate);
		assertEquals("First" + TEST_DATA_SIZE, entities.get(0).get(FORENAME));
	}

	/**
	 * Test the sub-hierarchy of contacts
	 */
	@Test
	public void testSubContacts()
		throws StorageException, TransactionException {
		Entity person = queryPersonByLastName("Test1");

		addContacts(person.get(CONTACTS).get(0), "sub@test.net", "12345");
		assertEquals(2,
			person.get(CONTACTS).get(0).get(TestContact.CHILDREN).size());
		EntityManager.storeEntity(person, null);
		assertEquals(2,
			person.get(CONTACTS).get(0).get(TestContact.CHILDREN).size());

		person = queryPersonByLastName("Test1");
		assertEquals(2,
			person.get(CONTACTS).get(0).get(TestContact.CHILDREN).size());
		assertEquals("sub@test.net", person
			.get(CONTACTS)
			.get(0)
			.get(TestContact.CHILDREN)
			.get(0)
			.get(CONTACT_VALUE));
		assertEquals("12345", person
			.get(CONTACTS)
			.get(0)
			.get(TestContact.CHILDREN)
			.get(1)
			.get(CONTACT_VALUE));
	}

	/**
	 * Test of storage updates.
	 */
	@Test
	public void testUpdate() throws StorageException, TransactionException {
		Entity person = queryPersonByLastName("Test1");

		person.set(AGE, 24);
		EntityManager.storeEntity(person, null);
		person = queryPersonByLastName("Test1");
		assertEquals(Integer.valueOf(24), person.get(AGE));
	}

	/**
	 * Test of storage updates for child entities.
	 */
	@Test
	public void testUpdateChildren()
		throws StorageException, TransactionException {
		Entity person = queryPersonByLastName("Test1");

		person.get(CONTACTS).get(0).set(CONTACT_VALUE, "sub@update.com");
		EntityManager.storeEntity(person, null);
		person = queryPersonByLastName("Test1");
		assertEquals("sub@update.com",
			person.get(CONTACTS).get(0).get(CONTACT_VALUE));
	}

	/**
	 * Query an entity by it's last name.
	 *
	 * @param name The name
	 * @return The entity
	 */
	protected TestPerson queryPersonByLastName(String name)
		throws StorageException {
		return EntityManager.queryEntity(TestPerson.class,
			ifAttribute(LASTNAME, equalTo(name)), true);
	}

	/**
	 * Creates a hierarchy of test persons.
	 */
	private void createHierarchy(int parent)
		throws StorageException, TransactionException {
		TestPerson person = queryPersonByLastName("Test" + parent);

		TestPerson subPerson = createPerson(
			new String[] { "SubTest1" + parent, "SubFirst1" + parent,
				"SubStreet1" + parent, "SubPostal1" + parent,
				"SubCity1" + parent, "1" + parent,
				"subtest1" + parent + "@test.com", "111-222333-" + parent });

		person.addChild(TestPerson.CHILDREN, subPerson);
		EntityManager.storeEntity(person, null);
	}

	/**
	 * Initializes test extra attributes.
	 */
	private void setupExtraAttributes()
		throws StorageException, TransactionException {
		Entity person = queryPersonByLastName("Test1");

		assertFalse(person.hasExtraAttribute(XA_FLAG));

		person.setExtraAttribute(XA1, "XA1-Test");
		person.setExtraAttribute(XA2, "XA2-Test");
		person.setExtraAttribute(XA_INT, 42);
		person.setExtraAttribute(XA_FLAG, true);
		person.setExtraAttribute(XA_DATE, TEST_DATE);
		person.setExtraAttribute(XA_LIST, Arrays.asList("L1", "L2", "L3"));
		storage.store(person);

		person = queryPersonByLastName("Test" + TEST_DATA_SIZE);

		assertFalse(person.hasExtraAttribute(XA_FLAG));

		person.setExtraAttribute(XA1, "XA1-Test");
		person.setExtraAttribute(XA2, "XA2-Test");
		person.setExtraAttribute(XA_INT, 99);
		person.setExtraAttribute(XA_FLAG, false);
		person.setExtraAttribute(XA_DATE, TEST_DATE);
		person.setExtraAttribute(XA_LIST, Arrays.asList("L1", "L2"));
		storage.store(person);
	}
}
