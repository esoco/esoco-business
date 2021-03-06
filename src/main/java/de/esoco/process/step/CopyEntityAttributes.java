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
package de.esoco.process.step;

import de.esoco.entity.Entity;

import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.reflect.ReflectUtil;

import de.esoco.process.ProcessException;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ProcessStep;

import de.esoco.storage.StorageException;

import java.util.Collection;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;

import static de.esoco.process.ProcessRelationTypes.ENTITY_PARAM;

import static org.obrel.core.RelationTypes.newEnumType;
import static org.obrel.core.RelationTypes.newRelationType;


/********************************************************************
 * Copies the attributes of an entity from or to a certain relatable object. The
 * following parameters can be set:
 *
 * <ul>
 *   <li>{@link ProcessRelationTypes#ENTITY_PARAM}: refers to the parameter that
 *     contains the entity to read from or write to.</li>
 *   <li>{@link #COPY_OBJECT_PARAM}: The parameter that contains the relatable
 *     object that is the source or target of the copy. If this parameter is not
 *     set the copy object will be either the process as the target or the
 *     process step (which falls through to the process) as the source.</li>
 *   <li>{@link #COPY_MODE}: The {@link CopyMode copy mode}.</li>
 * </ul>
 *
 * @author eso
 */
public class CopyEntityAttributes extends RollbackStep
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the possible copy modes. The available values are:
	 *
	 * <ul>
	 *   <li>{@link #FROM_ENTITY}: The entity attributes will be copied from the
	 *     entity to the copy object.</li>
	 *   <li>{@link #TO_ENTITY}: The entity attributes will be copied from the
	 *     copy object to the entity. If the entity doesn't exist a new instance
	 *     will be created.</li>
	 *   <li>{@link #TO_NEW_ENTITY}: The entity attributes will be copied from
	 *     the copy object to a new entity instance. If the entity parameter
	 *     already contains an entity instance it will be overwritten with the
	 *     new instance.</li>
	 *   <li>{@link #TO_EXISTING_ENTITY}: The entity attributes will be copied
	 *     from the copy object to the entity if it exists. If the entity
	 *     parameter doesn't contain an entity no values will be copied.</li>
	 * </ul>
	 */
	public enum CopyMode
	{
		FROM_ENTITY, TO_ENTITY, TO_NEW_ENTITY, TO_EXISTING_ENTITY
	}

	//~ Static fields/initializers ---------------------------------------------

	/**
	 * Refers to the parameter that contains the relatable object that is the
	 * target or source for the attribute copy.
	 */
	public static final RelationType<RelationType<? extends Relatable>> COPY_OBJECT_PARAM =
		newRelationType("de.esoco.process.COPY_OBJECT", RelationType.class);

	/** Contains the {@link CopyMode copy mode}. */
	public static final RelationType<CopyMode> COPY_MODE =
		newEnumType("de.esoco.process.COPY_MODE", CopyMode.class);

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public CopyEntityAttributes()
	{
		setMandatory(ENTITY_PARAM, COPY_MODE);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see ProcessStep#execute()
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void execute() throws StorageException, ProcessException,
									TransactionException
	{
		RelationType<? extends Entity> rEntityParam =
			checkParameter(ENTITY_PARAM);

		RelationType<? extends Relatable> rCopyObjectParam =
			getParameter(COPY_OBJECT_PARAM);

		CopyMode  eCopyMode   = checkParameter(COPY_MODE);
		Entity    rEntity     = getParameter(rEntityParam);
		Relatable rCopyObject = null;

		if (rCopyObjectParam != null)
		{
			rCopyObject = getParameter(rCopyObjectParam);

			if (rCopyObject == null)
			{
				throwMissingParameterException(rCopyObjectParam);
			}
		}

		Class<? extends Entity> rEntityClass =
			(Class<? extends Entity>) rEntityParam.getTargetType();

		Relatable rSource;
		Relatable rTarget;

		switch (eCopyMode)
		{
			case FROM_ENTITY:
				rSource = rEntity;
				rTarget = rCopyObject != null ? rCopyObject : getProcess();
				break;

			case TO_NEW_ENTITY:
				rEntity = null;
				// fall through

			case TO_ENTITY:
				if (rEntity == null)
				{
					rEntity = ReflectUtil.newInstance(rEntityClass);
					setParameter((RelationType<Entity>) rEntityParam, rEntity);
				}
				// fall through

			case TO_EXISTING_ENTITY:
				rSource = rCopyObject != null ? rCopyObject : this;
				rTarget = rEntity;
				break;

			default:
				throw new AssertionError("Unknown copy mode " + eCopyMode);
		}

		if (rEntity == null)
		{
			throwMissingParameterException(rEntityParam);
		}

		copyAttributes(rSource,
					   rTarget,
					   rEntity.getDefinition().getAttributes());
	}

	/***************************************
	 * Performs the attribute copy.
	 *
	 * @param rSource     The source object
	 * @param rTarget     The target object
	 * @param rAttributes The attributes to copy
	 */
	@SuppressWarnings("unchecked")
	private void copyAttributes(Relatable					rSource,
								Relatable					rTarget,
								Collection<RelationType<?>> rAttributes)
	{
		for (RelationType<?> rAttribute : rAttributes)
		{
			boolean bHasAttribute =
				rSource == this ? hasParameter(rAttribute)
								: rSource.hasRelation(rAttribute);

			if (bHasAttribute)
			{
				Object rValue =
					rSource == this ? getParameter(rAttribute)
									: rSource.get(rAttribute);

				rTarget.set((RelationType<Object>) rAttribute, rValue);
			}
		}
	}
}
