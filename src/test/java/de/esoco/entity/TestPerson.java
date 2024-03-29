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

import java.util.List;

import org.obrel.core.RelationType;

import static de.esoco.entity.EntityRelationTypes.ENTITY_ID;
import static de.esoco.entity.EntityRelationTypes.childAttribute;
import static de.esoco.entity.EntityRelationTypes.parentAttribute;

import static org.obrel.core.RelationTypes.newIntType;
import static org.obrel.core.RelationTypes.newType;

/**
 * A simple person entity for test purposes.
 *
 * @author eso
 */
public class TestPerson extends Entity {

	/**
	 * The parent person.
	 */
	public static final RelationType<TestPerson> PARENT = parentAttribute();

	//- Attributes ------------------------------

	/**
	 * The child contacts
	 */
	public static final RelationType<List<TestPerson>> CHILDREN =
		childAttribute();

	/**
	 * Last name
	 */
	public static final RelationType<String> LASTNAME = newType();

	/**
	 * Forename
	 */
	public static final RelationType<String> FORENAME = newType();

	/**
	 * Address
	 */
	public static final RelationType<String> ADDRESS = newType();

	/**
	 * Postal code
	 */
	public static final RelationType<String> POSTAL_CODE = newType();

	/**
	 * City
	 */
	public static final RelationType<String> CITY = newType();

	/**
	 * Age
	 */
	public static final RelationType<Integer> AGE = newIntType();

	/**
	 * Contact informations
	 */
	public static final RelationType<List<TestContact>> CONTACTS =
		childAttribute();

	//- Child entities --------------------------

	/**
	 * Minimal display attributes
	 */
	public static final RelationType<?>[] DISPLAY_ATTRIBUTES_MINIMAL =
		new RelationType<?>[] { ENTITY_ID, FORENAME, LASTNAME };

	/**
	 * Compact display attributes
	 */
	public static final RelationType<?>[] DISPLAY_ATTRIBUTES_COMPACT =
		new RelationType<?>[] { ENTITY_ID, FORENAME, LASTNAME, ADDRESS, CITY };

	private static final long serialVersionUID = 1L;
}
