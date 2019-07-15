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

import static de.esoco.entity.EntityRelationTypes.childAttribute;
import static de.esoco.entity.EntityRelationTypes.parentAttribute;

import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * Test entity definition for person addresses.
 *
 * @author eso
 */
public class TestContact extends Entity
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The parent contact */
	public static final RelationType<TestContact> PARENT = parentAttribute();

	/** The child contacts */
	public static final RelationType<List<TestContact>> CHILDREN =
		childAttribute();

	/** The parent person */
	public static final RelationType<TestPerson> PERSON = parentAttribute();

	/** Contact type */
	public static final RelationType<String> CONTACT_TYPE = newType();

	/** Contact value */
	public static final RelationType<String> CONTACT_VALUE = newType();
}
