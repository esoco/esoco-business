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
package de.esoco.process.step.entity;

import de.esoco.entity.Entity;

import de.esoco.process.ProcessStep;

import java.util.List;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * Process step to set children to to a parents child attribute.
 *
 * @author t.kuechenthal
 */
public class AddEntityChild extends EntityStep
{
	//~ Static fields/initializers ---------------------------------------------

	/** Parameter that holds the parent parameter. */
	public static final RelationType<RelationType<? extends Entity>> PARENT_ENTITY_PARAM =
		newType();

	/** Parameter that holds the child parameter. */
	public static final RelationType<RelationType<? extends Entity>> CHILD_ENTITY_PARAM =
		newType();

	/** Parameter that holds the parents child attribute. */
	public static final RelationType<RelationType<? extends List<? extends Entity>>> CHILD_ATTRIBUTE =
		newType();

	private static final long serialVersionUID = 1L;

	static
	{
		RelationTypes.init(AddEntityChild.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public AddEntityChild()
	{
		setMandatory(PARENT_ENTITY_PARAM, CHILD_ENTITY_PARAM, CHILD_ATTRIBUTE);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to make the process step be able to rollback.
	 *
	 * @return TRUE
	 */
	@Override
	protected boolean canRollback()
	{
		return true;
	}

	/***************************************
	 * @see ProcessStep#execute()
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void execute() throws Exception
	{
		Entity rParent = checkParameter(checkParameter(PARENT_ENTITY_PARAM));
		Entity rChild  = checkParameter(checkParameter(CHILD_ENTITY_PARAM));

		RelationType<? extends List<? extends Entity>> rChildAttribute =
			checkParameter(CHILD_ATTRIBUTE);

		rParent.addChild((RelationType<List<Entity>>) rChildAttribute, rChild);
	}

	/***************************************
	 * Removes the child from the parent.
	 *
	 * @see ProcessStep#rollback()
	 */
	@Override
	protected void rollback() throws Exception
	{
		Entity										   rParent		   =
			checkParameter(checkParameter(PARENT_ENTITY_PARAM));
		RelationType<? extends List<? extends Entity>> rChildAttribute =
			checkParameter(CHILD_ATTRIBUTE);

		List<? extends Entity> rList = rParent.get(rChildAttribute);

		Entity rChild = checkParameter(checkParameter(CHILD_ENTITY_PARAM));

		rList.remove(rChild);

		super.rollback();
	}
}
