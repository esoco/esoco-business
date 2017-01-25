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

import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import org.obrel.core.RelationType;

import static de.esoco.entity.TestContact.CONTACT_TYPE;
import static de.esoco.entity.TestContact.CONTACT_VALUE;
import static de.esoco.entity.TestPerson.ADDRESS;
import static de.esoco.entity.TestPerson.AGE;
import static de.esoco.entity.TestPerson.CITY;
import static de.esoco.entity.TestPerson.FORENAME;
import static de.esoco.entity.TestPerson.LASTNAME;
import static de.esoco.entity.TestPerson.POSTAL_CODE;


/********************************************************************
 * A base class for entity storage tests.
 *
 * @author eso
 */
public abstract class AbstractEntityStorageTest
{
	//~ Instance fields --------------------------------------------------------

	/** The storage reference (initialized in setUp()) */
	protected Storage rStorage;

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Test initialization.
	 *
	 * @throws ClassNotFoundException
	 */
	@BeforeClass
	public static void init() throws ClassNotFoundException
	{
		Class.forName("org.h2.Driver");
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Initializes the storage.
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		StorageDefinition aDef =
			JdbcStorageDefinition.create("jdbc:h2:mem:testdb;user=sa;password=");

		EntityManager.init();
		StorageManager.setDefaultStorage(aDef);

		// using TestPerson to get the default storage
		rStorage = StorageManager.getStorage(TestPerson.class);

		TransactionManager.begin();
		TransactionManager.addTransactionElement(rStorage);

		rStorage.initObjectStorage(TestPerson.class);
		rStorage.initObjectStorage(ExtraAttribute.class);
		rStorage.initObjectStorage(HistoryRecord.class);
	}

	/***************************************
	 * Performs a rollback and closes the storage.
	 *
	 * @throws StorageException
	 * @throws SQLException
	 * @throws TransactionException
	 */
	@After
	public void tearDown() throws StorageException, SQLException,
								  TransactionException
	{
		TransactionManager.rollback();
	}

	/***************************************
	 * Adds contacts to a person entity.
	 *
	 * @param rEntity   The person entity
	 * @param aContacts The contacts in the order email, phone, fax
	 */
	protected void addContacts(Entity rEntity, String... aContacts)
	{
		String[] aTypes = new String[] { "EML", "TEL", "FAX" };
		int		 nIndex = 0;

		RelationType<List<TestContact>> rChildAttr =
			(rEntity instanceof TestPerson ? TestPerson.CONTACTS
										   : TestContact.CHILDREN);

		for (String sContact : aContacts)
		{
			String sType = aTypes[nIndex++];

			if (sContact != null)
			{
				TestContact aContact = new TestContact();

				aContact.set(CONTACT_TYPE, sType);
				aContact.set(CONTACT_VALUE, sContact);

				rEntity.addChildren(rChildAttr, aContact);
			}
		}
	}

	/***************************************
	 * Creates a new person entity.
	 *
	 * @param  rAttributes The attributes of the person (6 elements)
	 *
	 * @return The new entity
	 */
	protected TestPerson createPerson(String[] rAttributes)
	{
		TestPerson aPerson = new TestPerson();

		aPerson.set(LASTNAME, rAttributes[0]);
		aPerson.set(FORENAME, rAttributes[1]);
		aPerson.set(ADDRESS, rAttributes[2]);
		aPerson.set(POSTAL_CODE, rAttributes[3]);
		aPerson.set(CITY, rAttributes[4]);
		aPerson.set(AGE, Integer.parseInt(rAttributes[5]));

		if (rAttributes.length > 6)
		{
			addContacts(aPerson,
						Arrays.copyOfRange(rAttributes, 6, rAttributes.length));
		}

		return aPerson;
	}

	/***************************************
	 * Executes a query and returns the resulting entities in a list.
	 *
	 * @param  pCriteria The query criteria
	 *
	 * @return A list containing the queried entities
	 *
	 * @throws StorageException On errors
	 */
	protected List<TestPerson> executePersonQuery(Predicate<Entity> pCriteria)
		throws StorageException
	{
		return EntityManager.queryEntities(TestPerson.class, pCriteria, 1000);
	}
}
