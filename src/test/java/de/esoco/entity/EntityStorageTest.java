//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//		 http://www.apache.org/licenses/LICENSE-2.0
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

import de.esoco.storage.StorageException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;

import static de.esoco.entity.EntityPredicates.ifAttribute;
import static de.esoco.entity.EntityPredicates.hasExtraAttribute;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/********************************************************************
 * Test for the JDBC storage implementation.
 *
 * @author eso
 */
@RunWith(Parameterized.class)
@SuppressWarnings("boxing")
public class EntityStorageTest extends AbstractEntityStorageTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final RelationType<String>	    XA1     =
		newExtraAttribute();
	private static final RelationType<String>	    XA2     =
		newExtraAttribute();
	private static final RelationType<Integer>	    XA_INT  =
		newExtraAttribute();
	private static final RelationType<Boolean>	    XA_FLAG =
		newExtraAttribute();
	private static final RelationType<Date>		    XA_DATE =
		newExtraAttribute();
	private static final RelationType<List<String>> XA_LIST =
		newExtraAttribute();

	private static final Date TEST_DATE = new Date();

	private static final int	    TEST_DATA_SIZE = 5;
	private static final String[][] TEST_DATA;

	static
	{
		RelationTypes.init(EntityStorageTest.class);

		TEST_DATA = new String[TEST_DATA_SIZE][];

		for (int i = 1; i <= TEST_DATA_SIZE; i++)
		{
			String sEmail =
				i < TEST_DATA_SIZE ? "test" + i + "@test.com" : null;

			TEST_DATA[i - 1] =
				new String[]
				{
					"Test" + i, "First" + (TEST_DATA_SIZE - i + 1),
					"Street" + i, "Postal" + i, "City" + i, "4" + i, sEmail,
					i + "23-456789"
				};
		}
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param nCacheLevel1 Size of the first cache level
	 * @param nCacheLevel2 Size of the second cache level
	 * @param nCacheLevel3 Size of the third cache level
	 */
	public EntityStorageTest(int nCacheLevel1,
							 int nCacheLevel2,
							 int nCacheLevel3)
	{
		EntityManager.setCacheCapacity(nCacheLevel1,
									   nCacheLevel2,
									   nCacheLevel3);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns the cache size parameters for different test runs.
	 *
	 * @return The list of cache sizes
	 */
	@Parameters
	public static List<Object[]> cacheSizes()
	{
		return Arrays.asList(new Object[][]
							 {
								 { 0, 0, 0 },
								 { 1, 1, 1 },
								 { 1, 2, 3 },
								 { 2, 2, 2 },
								 { 5, 10, 15 },
								 { 3, 2, 1 },
								 { 0, 2, 2 },
								 { 2, 0, 2 },
								 { 2, 2, 0 },
							 });
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Invalidates the entity cache after
	 */
	@After
	public void afterTest()
	{
		EntityManager.invalidateCache();
	}

	/***************************************
	 * Initializes the storage for the tests.
	 *
	 * @throws Exception
	 */
	@Before
	@Override
	public void setUp() throws Exception
	{
		super.setUp();

		List<TestPerson> aInitData = new ArrayList<TestPerson>();
		TestPerson		 aPerson;

		for (String[] rTestPersonData : TEST_DATA)
		{
			aPerson = createPerson(rTestPersonData);
			aInitData.add(aPerson);
			EntityManager.storeEntity(aPerson, null);
		}
	}

	/***************************************
	 * Tests queries by extra attributes.
	 *
	 * @throws StorageException
	 * @throws TransactionException
	 */
	@Test
	public void testExtraAttributeQuery() throws StorageException,
												 TransactionException
	{
		setupExtraAttributes();

		Entity rEntity =
			EntityManager.queryEntityByExtraAttribute(XA_INT, 42, true);

		assertEquals(TestPerson.class, rEntity.getClass());
		assertEquals("Test1", rEntity.get(LASTNAME));
		assertEquals("XA1-Test", rEntity.getExtraAttribute(XA1, null));
		assertEquals(TEST_DATE, rEntity.getExtraAttribute(XA_DATE, null));
		assertEquals(Integer.valueOf(42),
					 rEntity.getExtraAttribute(XA_INT, null));

		rEntity =
			EntityManager.queryEntityByExtraAttribute(XA_FLAG, false, true);

		assertEquals("Test" + TEST_DATA_SIZE, rEntity.get(LASTNAME));

		try
		{
			EntityManager.queryEntityByExtraAttribute(XA2, "XA2-Test", true);
			assertFalse(true);
		}
		catch (IllegalStateException e)
		{
			// this should happen
		}

		Collection<? extends Entity> rEntities =
			EntityManager.queryEntitiesByExtraAttribute(XA1,
														"XA1-Test",
														Integer.MAX_VALUE);

		assertEquals(2, rEntities.size());

		rEntities =
			EntityManager.queryEntitiesByExtraAttribute(XA_LIST,
														Arrays.asList("L1",
																	  "L2"),
														Integer.MAX_VALUE);

		assertEquals(1, rEntities.size());
		assertEquals("Test" + TEST_DATA_SIZE,
					 rEntities.iterator().next().get(LASTNAME));

		rEntities =
			EntityManager.queryEntitiesByExtraAttribute(TestPerson.class,
														XA1,
														"XA1-Test",
														Integer.MAX_VALUE);

		assertEquals(2, rEntities.size());
	}

	/***************************************
	 * Test of storing, updating and accessing extra entity attributes.
	 *
	 * @throws StorageException
	 * @throws TransactionException
	 */
	@Test
	public void testExtraAttributeReference() throws StorageException,
													 TransactionException
	{
		setupExtraAttributes();

		List<TestPerson> rEntities =
			EntityManager.queryEntities(TestPerson.class,
										hasExtraAttribute(TestPerson.class,
															ExtraAttribute.KEY
															.is(equalTo(XA1))
															.and(ExtraAttribute
																 .VALUE.is(elementOf("XA1-Test",
																					 "XA2-Test")))),
										10);

		assertEquals(2, rEntities.size());

		rEntities =
			EntityManager.queryEntities(TestPerson.class,
										hasExtraAttribute(TestPerson.class,
															ExtraAttribute.VALUE
															.is(like("%-Test"))),
										10);
		assertEquals(2, rEntities.size());

		rEntities =
			EntityManager.queryEntities(TestPerson.class,
										hasExtraAttribute(TestPerson.class,
															ExtraAttribute.VALUE
															.is(equalTo("42"))),
										10);
		assertEquals(1, rEntities.size());
	}

	/***************************************
	 * Test of storing, updating and accessing extra entity attributes.
	 *
	 * @throws StorageException
	 * @throws TransactionException
	 */
	@Test
	public void testExtraAttributes() throws StorageException,
											 TransactionException
	{
		setupExtraAttributes();

		Entity rPerson = queryPersonByLastName("Test1");

		assertEquals("XA1-Test", rPerson.getExtraAttribute(XA1, null));
		assertEquals("XA2-Test", rPerson.getExtraAttribute(XA2, null));
		assertEquals(Integer.valueOf(42),
					 rPerson.getExtraAttribute(XA_INT, null));
		assertTrue(rPerson.getExtraAttribute(XA_FLAG, null));
		assertEquals(3, rPerson.getExtraAttribute(XA_LIST, null).size());
		assertEquals(Arrays.asList("L1", "L2", "L3"),
					 rPerson.getExtraAttribute(XA_LIST, null));

		rPerson.setExtraAttribute(XA1, "XA1-Updated");
		rPerson.setExtraAttribute(XA_INT, -42);
		rPerson.setExtraAttribute(XA_FLAG, false);
		rPerson.setExtraAttribute(XA_LIST, Arrays.asList("L1", "L2"));
		EntityManager.storeEntity(rPerson, null);

		rPerson = queryPersonByLastName("Test1");

		assertEquals("XA1-Updated", rPerson.getExtraAttribute(XA1, null));
		assertEquals("XA2-Test", rPerson.getExtraAttribute(XA2, null));
		assertEquals(Integer.valueOf(-42),
					 rPerson.getExtraAttribute(XA_INT, null));
		assertFalse(rPerson.getExtraAttribute(XA_FLAG, null));
		assertEquals(2, rPerson.getExtraAttribute(XA_LIST, null).size());
		assertEquals(Arrays.asList("L1", "L2"),
					 rPerson.getExtraAttribute(XA_LIST, null));
	}

	/***************************************
	 * Test of storage updates.
	 *
	 * @throws StorageException
	 * @throws TransactionException
	 */
	@Test
	public void testHierarchy() throws StorageException, TransactionException
	{
		createHierarchy(1);

		TestPerson rPerson;

		rPerson = queryPersonByLastName("Test1");

		TestPerson rSubPerson = queryPersonByLastName("SubTest11");

		assertEquals(rPerson, rSubPerson.get(PARENT));
		assertEquals("SubFirst11", rSubPerson.get(FORENAME));
		assertEquals(2, rSubPerson.get(CONTACTS).size());
	}

	/***************************************
	 * Test of queries.
	 *
	 * @throws StorageException
	 */
	@Test
	public void testQuery() throws StorageException
	{
		assertEquals(TEST_DATA_SIZE, executePersonQuery(null).size());
		assertEquals(TEST_DATA_SIZE - 1,
					 executePersonQuery(ifAttribute(AGE, greaterOrEqual(42)))
					 .size());
		assertEquals(TEST_DATA_SIZE - 2,
					 executePersonQuery(ifAttribute(AGE, greaterThan(42)))
					 .size());
		assertEquals(2,
					 executePersonQuery(ifAttribute(AGE, greaterThan(42)).and(ifAttribute(AGE,
																						  lessOrEqual(44))))
					 .size());
		assertEquals(1,
					 executePersonQuery(ifAttribute(CITY, like("%2"))).size());
		assertEquals(2,
					 executePersonQuery(ifAttribute(LASTNAME,
													elementOf("Test1",
															  "Test2")))
					 .size());

		assertEquals(2, queryPersonByLastName("Test1").get(CONTACTS).size());
		assertEquals(1,
					 queryPersonByLastName("Test" + TEST_DATA_SIZE).get(CONTACTS)
					 .size());
	}

	/***************************************
	 * Test of sorted queries.
	 *
	 * @throws StorageException
	 */
	@Test
	public void testSortedQuery() throws StorageException
	{
		ElementPredicate<Entity, String> aSortPredicate =
			ifAttribute(FORENAME, alwaysTrue());

		aSortPredicate.set(MetaTypes.SORT_ASCENDING, true);

		List<TestPerson> aEntities = executePersonQuery(aSortPredicate);

		assertTrue(aEntities.get(0).get(FORENAME).equals("First1"));

		aSortPredicate.set(MetaTypes.SORT_ASCENDING, false);

		aEntities = executePersonQuery(aSortPredicate);
		assertTrue(aEntities.get(0).get(FORENAME)
				   .equals("First" + TEST_DATA_SIZE));
	}

	/***************************************
	 * Test the sub-hierarchy of contacts
	 *
	 * @throws StorageException
	 * @throws TransactionException
	 */
	@Test
	public void testSubContacts() throws StorageException, TransactionException
	{
		Entity rPerson = queryPersonByLastName("Test1");

		addContacts(rPerson.get(CONTACTS).get(0), "sub@test.net", "12345");
		assertEquals(2,
					 rPerson.get(CONTACTS).get(0).get(TestContact.CHILDREN)
					 .size());
		EntityManager.storeEntity(rPerson, null);
		assertEquals(2,
					 rPerson.get(CONTACTS).get(0).get(TestContact.CHILDREN)
					 .size());

		rPerson = queryPersonByLastName("Test1");
		assertEquals(2,
					 rPerson.get(CONTACTS).get(0).get(TestContact.CHILDREN)
					 .size());
		assertEquals("sub@test.net",
					 rPerson.get(CONTACTS).get(0).get(TestContact.CHILDREN)
					 .get(0)
					 .get(CONTACT_VALUE));
		assertEquals("12345",
					 rPerson.get(CONTACTS).get(0).get(TestContact.CHILDREN)
					 .get(1)
					 .get(CONTACT_VALUE));
	}

	/***************************************
	 * Test of storage updates.
	 *
	 * @throws StorageException
	 * @throws TransactionException
	 */
	@Test
	public void testUpdate() throws StorageException, TransactionException
	{
		Entity rPerson = queryPersonByLastName("Test1");

		rPerson.set(AGE, 24);
		EntityManager.storeEntity(rPerson, null);
		rPerson = queryPersonByLastName("Test1");
		assertEquals(new Integer(24), rPerson.get(AGE));
	}

	/***************************************
	 * Test of storage updates for child entities.
	 *
	 * @throws StorageException
	 * @throws TransactionException
	 */
	@Test
	public void testUpdateChildren() throws StorageException,
											TransactionException
	{
		Entity rPerson = queryPersonByLastName("Test1");

		rPerson.get(CONTACTS).get(0).set(CONTACT_VALUE, "sub@update.com");
		EntityManager.storeEntity(rPerson, null);
		rPerson = queryPersonByLastName("Test1");
		assertEquals("sub@update.com",
					 rPerson.get(CONTACTS).get(0).get(CONTACT_VALUE));
	}

	/***************************************
	 * Query an entity by it's last name.
	 *
	 * @param  sName The name
	 *
	 * @return The entity
	 *
	 * @throws StorageException
	 */
	protected TestPerson queryPersonByLastName(String sName)
		throws StorageException
	{
		return EntityManager.queryEntity(TestPerson.class,
										 ifAttribute(LASTNAME, equalTo(sName)),
										 true);
	}

	/***************************************
	 * Creates a hierarchy of test persons.
	 *
	 * @param  nParent
	 *
	 * @throws StorageException
	 * @throws TransactionException
	 */
	private void createHierarchy(int nParent) throws StorageException,
													 TransactionException
	{
		TestPerson rPerson = queryPersonByLastName("Test" + nParent);

		TestPerson aSubPerson =
			createPerson(new String[]
						 {
							 "SubTest1" + nParent, "SubFirst1" + nParent,
							 "SubStreet1" + nParent, "SubPostal1" + nParent,
							 "SubCity1" + nParent, "1" + nParent,
							 "subtest1" + nParent + "@test.com",
							 "111-222333-" + nParent
						 });

		rPerson.addChild(TestPerson.CHILDREN, aSubPerson);
		EntityManager.storeEntity(rPerson, null);
	}

	/***************************************
	 * Initializes test extra attributes.
	 *
	 * @throws StorageException
	 * @throws TransactionException
	 */
	private void setupExtraAttributes() throws StorageException,
											   TransactionException
	{
		Entity rPerson = queryPersonByLastName("Test1");

		assertFalse(rPerson.hasExtraAttribute(XA_FLAG));

		rPerson.setExtraAttribute(XA1, "XA1-Test");
		rPerson.setExtraAttribute(XA2, "XA2-Test");
		rPerson.setExtraAttribute(XA_INT, 42);
		rPerson.setExtraAttribute(XA_FLAG, true);
		rPerson.setExtraAttribute(XA_DATE, TEST_DATE);
		rPerson.setExtraAttribute(XA_LIST, Arrays.asList("L1", "L2", "L3"));
		rStorage.store(rPerson);

		rPerson = queryPersonByLastName("Test" + TEST_DATA_SIZE);

		assertFalse(rPerson.hasExtraAttribute(XA_FLAG));

		rPerson.setExtraAttribute(XA1, "XA1-Test");
		rPerson.setExtraAttribute(XA2, "XA2-Test");
		rPerson.setExtraAttribute(XA_INT, 99);
		rPerson.setExtraAttribute(XA_FLAG, false);
		rPerson.setExtraAttribute(XA_DATE, TEST_DATE);
		rPerson.setExtraAttribute(XA_LIST, Arrays.asList("L1", "L2"));
		rStorage.store(rPerson);
	}
}
