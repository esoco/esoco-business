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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.obrel.core.Relation;
import org.obrel.core.RelationType;

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
	protected EntityProcessDefinition(String sProcessName) {
		super(sProcessName);
	}

	/**
	 * Annotates a relation with the parameters for an entity query.
	 *
	 * @param rRelation   The relation to annotate
	 * @param pQuery      The entity query
	 * @param pSortOrder  The default sort order of the query or NULL for none
	 * @param rAttributes The optional entity attributes to display or NONE for
	 *                    the default
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> void annotateForEntityQuery(
		Relation<? super E> rRelation, QueryPredicate<E> pQuery,
		Predicate<? super Entity> pSortOrder,
		Function<? super E, ?>... rAttributes) {
		annotateForEntityQuery(rRelation, pQuery, null,
			Arrays.asList(rAttributes));
	}

	/**
	 * Annotates a relation with the parameters for an entity query.
	 *
	 * @param rRelation   The relation to annotate
	 * @param pQuery      The entity query
	 * @param pSortOrder  The default sort order of the query or NULL for none
	 * @param rAttributes The optional entity attributes to display or NONE for
	 *                    the default
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> void annotateForEntityQuery(
		Relation<? super E> rRelation, QueryPredicate<E> pQuery,
		Predicate<? super Entity> pSortOrder,
		Collection<Function<? super E, ?>> rAttributes) {
		rRelation.annotate(ENTITY_QUERY_PREDICATE, pQuery);
		rRelation.annotate(ENTITY_SORT_PREDICATE, pSortOrder);

		if (rAttributes != null && rAttributes.size() > 0) {
			List<Function<? super Entity, ?>> rAttr =
				new ArrayList<Function<? super Entity, ?>>();

			for (Function<? super E, ?> rFunction : rAttributes) {
				rAttr.add((Function<? super Entity, ?>) rFunction);
			}

			rRelation.annotate(ENTITY_ATTRIBUTES, rAttr);
		}
	}

	/**
	 * Creates a new {@link AddEntityChild} process step and adds a new step
	 * list entry.
	 *
	 * @param sName              The name of the step
	 * @param rParentEntityParam The parent parameter
	 * @param rChildAttribute    The child attribute of the parent
	 * @param rChildEntityParam  The child parameter that will be set
	 * @return The new step list entry
	 */
	protected <C extends Entity> StepListEntry addEntityChild(String sName,
		RelationType<? extends Entity> rParentEntityParam,
		RelationType<? extends List<C>> rChildAttribute,
		RelationType<C> rChildEntityParam) {
		StepListEntry aStep = null;

		if (sName == null) {
			aStep = invoke(AddEntityChild.class);
		} else {
			aStep = invoke(sName, AddEntityChild.class);
		}

		aStep.set(AddEntityChild.PARENT_ENTITY_PARAM, rParentEntityParam);
		aStep.set(AddEntityChild.CHILD_ENTITY_PARAM, rChildEntityParam);
		aStep.set(AddEntityChild.CHILD_ATTRIBUTE, rChildAttribute);

		return aStep;
	}

	/**
	 * Adds an {@link CopyEntityAttributes} step that updates the attributes of
	 * an entity from the process parameters. If no entity exists in the given
	 * parameter a new entity instance of the parameter's target type will be
	 * created.
	 *
	 * @param sName        The step name
	 * @param rEntityParam The parameter that contains the entity
	 * @param eCopyMode    the copy mode
	 * @return The new step list entry
	 */
	protected StepListEntry copyEntityAttributes(String sName,
		RelationType<? extends Entity> rEntityParam, CopyMode eCopyMode) {
		StepListEntry aStep = invoke(sName, CopyEntityAttributes.class);

		aStep.set(ProcessRelationTypes.ENTITY_PARAM, rEntityParam);
		aStep.set(CopyEntityAttributes.COPY_MODE, eCopyMode);

		return aStep;
	}

	/**
	 * Adds a step with a default name that creates a new entity of a
	 * particular
	 * class and stores it in a certain parameter.
	 *
	 * @see #createEntity(String, Class, RelationType)
	 */
	protected <E extends Entity> StepListEntry createEntity(
		Class<E> rEntityClass, RelationType<? super E> rTargetParam) {
		return createEntity("create" + rEntityClass.getSimpleName(),
			rEntityClass, rTargetParam);
	}

	/**
	 * Adds a step that creates a new entity of a particular class and
	 * stores it
	 * in a certain parameter.
	 *
	 * @param sName        The step name
	 * @param rEntityClass The class of the entity type to create
	 * @param rTargetParam The process parameter to store the new entity in
	 * @return The new step list entry
	 */
	protected <E extends Entity> StepListEntry createEntity(String sName,
		Class<E> rEntityClass, RelationType<? super E> rTargetParam) {
		return invokeFunction(sName, null, rTargetParam,
			ReflectionFuntions.newInstanceOf(rEntityClass));
	}

	/**
	 * Adds a step that queries an entity by it's ID and stores in in a certain
	 * parameter. The entity ID must be stored in the process parameter with
	 * the
	 * type {@link EntityRelationTypes#ENTITY_ID}.
	 *
	 * @param sName        The step name
	 * @param rTargetParam The parameter to store the entity in
	 * @return The new step list entry
	 */
	protected <E extends Entity> StepListEntry queryEntityById(String sName,
		RelationType<E> rTargetParam) {
		@SuppressWarnings("unchecked")
		Class<E> rTargetType = (Class<E>) rTargetParam.getTargetType();

		StepListEntry aStep = invokeFunction(sName, ENTITY_ID, rTargetParam,
			EntityFunctions.queryEntity(rTargetType));

		return aStep;
	}

	/**
	 * A convenience method that always generates the step name from the target
	 * parameter by prepending "Select" to the parameter's simple name.
	 *
	 * @see #selectEntity(String, RelationType, QueryPredicate, Function...)
	 */
	@SuppressWarnings("unchecked")
	protected <E extends Entity> StepListEntry selectEntity(
		RelationType<? super E> rTargetParam, QueryPredicate<E> rQuery,
		Function<? super E, ?>... rAttributes) {
		return selectEntity(null, rTargetParam, rQuery, rAttributes);
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
	 * @param sName        The name of the interactive step
	 * @param rTargetParam The parameter to store the selected entity in
	 * @param rQuery       The query criteria (NULL for all entities of the
	 *                     given type)
	 * @param rAttributes  A list of functions that will extract the query
	 *                     values from a queried entity or NULL for a default
	 * @return The new step list entry
	 */
	@SuppressWarnings({ "unchecked" })
	protected <E extends Entity> StepListEntry selectEntity(String sName,
		RelationType<? super E> rTargetParam, QueryPredicate<E> rQuery,
		Function<? super E, ?>... rAttributes) {
		if (sName == null) {
			String sParam = rTargetParam.getSimpleName();

			sName = "Select" + TextUtil.capitalizedIdentifier(sParam);
		}

		StepListEntry aStep = input(sName, rTargetParam);

		Relation<? super E> rParamRelation = aStep.set(rTargetParam, null);

		rQuery.set(QUERY_DEPTH, 0);

		annotateForEntityQuery(rParamRelation, rQuery, null, rAttributes);

		return aStep;
	}

	/**
	 * A convenience method that generates the step name from the entity
	 * parameter by prepending "Store" to the parameters simple name.
	 *
	 * @see #storeEntity(String, RelationType)
	 */
	protected StepListEntry storeEntity(
		RelationType<? extends Entity> rEntityParam) {
		return storeEntity("Store" +
				TextConvert.capitalizedIdentifier(rEntityParam.getSimpleName()),
			rEntityParam);
	}

	/**
	 * Adds a step to store an entity from a certain process parameter.
	 *
	 * @param sName        The name of the step
	 * @param rEntityParam The parameter the entity is stored in
	 * @return The new step list entry
	 */
	protected StepListEntry storeEntity(String sName,
		RelationType<? extends Entity> rEntityParam) {
		StepListEntry aStep = invoke(sName, StoreEntity.class);

		aStep.set(ENTITY_PARAM, rEntityParam);

		return aStep;
	}
}
