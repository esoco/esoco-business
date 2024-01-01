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
package de.esoco.process;

import de.esoco.entity.Entity;
import de.esoco.entity.EntityFunctions;
import de.esoco.entity.EntityRelationTypes;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.ReflectionFuntions;
import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextUtil;
import de.esoco.process.step.CopyEntityAttributes;
import de.esoco.process.step.CopyEntityAttributes.CopyMode;
import de.esoco.process.step.entity.AddEntityChild;
import de.esoco.process.step.entity.StoreEntity;
import de.esoco.storage.QueryPredicate;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static de.esoco.entity.EntityRelationTypes.ENTITY_ATTRIBUTES;
import static de.esoco.entity.EntityRelationTypes.ENTITY_ID;
import static de.esoco.entity.EntityRelationTypes.ENTITY_QUERY_PREDICATE;
import static de.esoco.entity.EntityRelationTypes.ENTITY_SORT_PREDICATE;
import static de.esoco.process.ProcessRelationTypes.ENTITY_PARAM;
import static de.esoco.storage.StorageRelationTypes.QUERY_DEPTH;

/**
 * A subclass of the step list-based process definition for entity-specific
 * processes. It contains additional methods to add process steps for the
 * querying and manipulation of entities.
 *
 * @author eso
 */
public abstract class EntityProcessDefinition
	extends StepListProcessDefinition {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 */
	public EntityProcessDefinition() {
		this(null);
	}

	/**
	 * @see StepListProcessDefinition#StepListProcessDefinition(String)
	 */
	protected EntityProcessDefinition(String processName) {
		super(processName);
	}

	/**
	 * Annotates a relation with the parameters for an entity query.
	 *
	 * @param relation   The relation to annotate
	 * @param query      The entity query
	 * @param sortOrder  The default sort order of the query or NULL for none
	 * @param attributes The optional entity attributes to display or NONE for
	 *                   the default
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> void annotateForEntityQuery(
		Relation<? super E> relation, QueryPredicate<E> query,
		Predicate<? super Entity> sortOrder,
		Function<? super E, ?>... attributes) {
		annotateForEntityQuery(relation, query, null,
			Arrays.asList(attributes));
	}

	/**
	 * Annotates a relation with the parameters for an entity query.
	 *
	 * @param relation   The relation to annotate
	 * @param query      The entity query
	 * @param sortOrder  The default sort order of the query or NULL for none
	 * @param attributes The optional entity attributes to display or NONE for
	 *                   the default
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> void annotateForEntityQuery(
		Relation<? super E> relation, QueryPredicate<E> query,
		Predicate<? super Entity> sortOrder,
		Collection<Function<? super E, ?>> attributes) {
		relation.annotate(ENTITY_QUERY_PREDICATE, query);
		relation.annotate(ENTITY_SORT_PREDICATE, sortOrder);

		if (attributes != null && attributes.size() > 0) {
			List<Function<? super Entity, ?>> attr =
				new ArrayList<Function<? super Entity, ?>>();

			for (Function<? super E, ?> function : attributes) {
				attr.add((Function<? super Entity, ?>) function);
			}

			relation.annotate(ENTITY_ATTRIBUTES, attr);
		}
	}

	/**
	 * Creates a new {@link AddEntityChild} process step and adds a new step
	 * list entry.
	 *
	 * @param name              The name of the step
	 * @param parentEntityParam The parent parameter
	 * @param childAttribute    The child attribute of the parent
	 * @param childEntityParam  The child parameter that will be set
	 * @return The new step list entry
	 */
	protected <C extends Entity> StepListEntry addEntityChild(String name,
		RelationType<? extends Entity> parentEntityParam,
		RelationType<? extends List<C>> childAttribute,
		RelationType<C> childEntityParam) {
		StepListEntry step = null;

		if (name == null) {
			step = invoke(AddEntityChild.class);
		} else {
			step = invoke(name, AddEntityChild.class);
		}

		step.set(AddEntityChild.PARENT_ENTITY_PARAM, parentEntityParam);
		step.set(AddEntityChild.CHILD_ENTITY_PARAM, childEntityParam);
		step.set(AddEntityChild.CHILD_ATTRIBUTE, childAttribute);

		return step;
	}

	/**
	 * Adds an {@link CopyEntityAttributes} step that updates the attributes of
	 * an entity from the process parameters. If no entity exists in the given
	 * parameter a new entity instance of the parameter's target type will be
	 * created.
	 *
	 * @param name        The step name
	 * @param entityParam The parameter that contains the entity
	 * @param copyMode    the copy mode
	 * @return The new step list entry
	 */
	protected StepListEntry copyEntityAttributes(String name,
		RelationType<? extends Entity> entityParam, CopyMode copyMode) {
		StepListEntry step = invoke(name, CopyEntityAttributes.class);

		step.set(ProcessRelationTypes.ENTITY_PARAM, entityParam);
		step.set(CopyEntityAttributes.COPY_MODE, copyMode);

		return step;
	}

	/**
	 * Adds a step with a default name that creates a new entity of a
	 * particular
	 * class and stores it in a certain parameter.
	 *
	 * @see #createEntity(String, Class, RelationType)
	 */
	protected <E extends Entity> StepListEntry createEntity(
		Class<E> entityClass, RelationType<? super E> targetParam) {
		return createEntity("create" + entityClass.getSimpleName(),
			entityClass,
			targetParam);
	}

	/**
	 * Adds a step that creates a new entity of a particular class and
	 * stores it
	 * in a certain parameter.
	 *
	 * @param name        The step name
	 * @param entityClass The class of the entity type to create
	 * @param targetParam The process parameter to store the new entity in
	 * @return The new step list entry
	 */
	protected <E extends Entity> StepListEntry createEntity(String name,
		Class<E> entityClass, RelationType<? super E> targetParam) {
		return invokeFunction(name, null, targetParam,
			ReflectionFuntions.newInstanceOf(entityClass));
	}

	/**
	 * Adds a step that queries an entity by it's ID and stores in in a certain
	 * parameter. The entity ID must be stored in the process parameter with
	 * the
	 * type {@link EntityRelationTypes#ENTITY_ID}.
	 *
	 * @param name        The step name
	 * @param targetParam The parameter to store the entity in
	 * @return The new step list entry
	 */
	protected <E extends Entity> StepListEntry queryEntityById(String name,
		RelationType<E> targetParam) {
		@SuppressWarnings("unchecked")
		Class<E> targetType = (Class<E>) targetParam.getTargetType();

		StepListEntry step = invokeFunction(name, ENTITY_ID, targetParam,
			EntityFunctions.queryEntity(targetType));

		return step;
	}

	/**
	 * A convenience method that always generates the step name from the target
	 * parameter by prepending "Select" to the parameter's simple name.
	 *
	 * @see #selectEntity(String, RelationType, QueryPredicate, Function...)
	 */
	@SuppressWarnings("unchecked")
	protected <E extends Entity> StepListEntry selectEntity(
		RelationType<? super E> targetParam, QueryPredicate<E> query,
		Function<? super E, ?>... attributes) {
		return selectEntity(null, targetParam, query, attributes);
	}

	/**
	 * Adds an interactive process step to select an entity from a list that is
	 * defined by certain query criteria. The displayed attributes of an entity
	 * can be controlled with the attributes parameter. It contains functions
	 * that will retrieve the displayed values from a queried entity. In the
	 * simplest case this will only be the attribute relation types as these
	 * also implement the function interface. But basically this can be
	 * arbitrary complex functions that access entity hierarchies or generate
	 * values based on certain criteria.
	 *
	 * <p>If the step name is null it will be generated from the target
	 * parameter by prepending "Select" to the parameter's simple name.</p>
	 *
	 * @param name        The name of the interactive step
	 * @param targetParam The parameter to store the selected entity in
	 * @param query       The query criteria (NULL for all entities of the
	 *                       given
	 *                    type)
	 * @param attributes  A list of functions that will extract the query
	 *                       values
	 *                    from a queried entity or NULL for a default
	 * @return The new step list entry
	 */
	@SuppressWarnings({ "unchecked" })
	protected <E extends Entity> StepListEntry selectEntity(String name,
		RelationType<? super E> targetParam, QueryPredicate<E> query,
		Function<? super E, ?>... attributes) {
		if (name == null) {
			String param = targetParam.getSimpleName();

			name = "Select" + TextUtil.capitalizedIdentifier(param);
		}

		StepListEntry step = input(name, targetParam);

		Relation<? super E> paramRelation = step.set(targetParam, null);

		query.set(QUERY_DEPTH, 0);

		annotateForEntityQuery(paramRelation, query, null, attributes);

		return step;
	}

	/**
	 * A convenience method that generates the step name from the entity
	 * parameter by prepending "Store" to the parameters simple name.
	 *
	 * @see #storeEntity(String, RelationType)
	 */
	protected StepListEntry storeEntity(
		RelationType<? extends Entity> entityParam) {
		return storeEntity("Store" +
				TextConvert.capitalizedIdentifier(entityParam.getSimpleName()),
			entityParam);
	}

	/**
	 * Adds a step to store an entity from a certain process parameter.
	 *
	 * @param name        The name of the step
	 * @param entityParam The parameter the entity is stored in
	 * @return The new step list entry
	 */
	protected StepListEntry storeEntity(String name,
		RelationType<? extends Entity> entityParam) {
		StepListEntry step = invoke(name, StoreEntity.class);

		step.set(ENTITY_PARAM, entityParam);

		return step;
	}
}
