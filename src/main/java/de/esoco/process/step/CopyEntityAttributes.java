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
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;

import java.util.Collection;

import static de.esoco.process.ProcessRelationTypes.ENTITY_PARAM;
import static org.obrel.core.RelationTypes.newEnumType;
import static org.obrel.core.RelationTypes.newRelationType;

/**
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
public class CopyEntityAttributes extends RollbackStep {

	/**
	 * Enumeration of the possible copy modes. The available values are:
	 *
	 * <ul>
	 *   <li>{@link #FROM_ENTITY}: The entity attributes will be copied from
	 *   the
	 *     entity to the copy object.</li>
	 *   <li>{@link #TO_ENTITY}: The entity attributes will be copied from the
	 *     copy object to the entity. If the entity doesn't exist a new
	 *     instance
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
	public enum CopyMode {
		FROM_ENTITY, TO_ENTITY, TO_NEW_ENTITY, TO_EXISTING_ENTITY
	}

	/**
	 * Refers to the parameter that contains the relatable object that is the
	 * target or source for the attribute copy.
	 */
	public static final RelationType<RelationType<? extends Relatable>>
		COPY_OBJECT_PARAM =
		newRelationType("de.esoco.process.COPY_OBJECT", RelationType.class);

	/**
	 * Contains the {@link CopyMode copy mode}.
	 */
	public static final RelationType<CopyMode> COPY_MODE =
		newEnumType("de.esoco.process.COPY_MODE", CopyMode.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 */
	public CopyEntityAttributes() {
		setMandatory(ENTITY_PARAM, COPY_MODE);
	}

	/**
	 * @see ProcessStep#execute()
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void execute()
		throws StorageException, ProcessException, TransactionException {
		RelationType<? extends Entity> entityParam =
			checkParameter(ENTITY_PARAM);

		RelationType<? extends Relatable> copyObjectParam =
			getParameter(COPY_OBJECT_PARAM);

		CopyMode copyMode = checkParameter(COPY_MODE);
		Entity entity = getParameter(entityParam);
		Relatable copyObject = null;

		if (copyObjectParam != null) {
			copyObject = getParameter(copyObjectParam);

			if (copyObject == null) {
				throwMissingParameterException(copyObjectParam);
			}
		}

		Class<? extends Entity> entityClass =
			(Class<? extends Entity>) entityParam.getTargetType();

		Relatable source;
		Relatable target;

		switch (copyMode) {
			case FROM_ENTITY:
				source = entity;
				target = copyObject != null ? copyObject : getProcess();
				break;

			case TO_NEW_ENTITY:
				entity = null;
				// fall through

			case TO_ENTITY:
				if (entity == null) {
					entity = ReflectUtil.newInstance(entityClass);
					setParameter((RelationType<Entity>) entityParam, entity);
				}
				// fall through

			case TO_EXISTING_ENTITY:
				source = copyObject != null ? copyObject : this;
				target = entity;
				break;

			default:
				throw new AssertionError("Unknown copy mode " + copyMode);
		}

		if (entity == null) {
			throwMissingParameterException(entityParam);
		}

		copyAttributes(source, target, entity.getDefinition().getAttributes());
	}

	/**
	 * Performs the attribute copy.
	 *
	 * @param source     The source object
	 * @param target     The target object
	 * @param attributes The attributes to copy
	 */
	@SuppressWarnings("unchecked")
	private void copyAttributes(Relatable source, Relatable target,
		Collection<RelationType<?>> attributes) {
		for (RelationType<?> attribute : attributes) {
			boolean hasAttribute = source == this ?
			                       hasParameter(attribute) :
			                       source.hasRelation(attribute);

			if (hasAttribute) {
				Object value = source == this ?
				               getParameter(attribute) :
				               source.get(attribute);

				target.set((RelationType<Object>) attribute, value);
			}
		}
	}
}
