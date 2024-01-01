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
import de.esoco.entity.EntityDefinition;
import de.esoco.entity.EntityDefinition.DisplayMode;
import de.esoco.entity.EntityManager;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.process.step.DialogFragment;
import de.esoco.process.step.InteractionFragment;
import de.esoco.storage.QueryPredicate;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newType;

/**
 * An interactive process fragment to select an entity from a list.
 *
 * @author eso
 */
public class SelectEntity<E extends Entity> extends InteractionFragment {

	/**
	 * A standard parameter that can be used to display this fragment in a
	 * process step.
	 */
	public static final RelationType<List<RelationType<?>>>
		SELECT_ENTITY_FRAGMENT = newListType();

	/**
	 * The parameter containing the selected entity.
	 */
	public static final RelationType<Entity> SELECTED_ENTITY = newType();

	private static final long serialVersionUID = 1L;

	private static final List<RelationType<?>> INTERACTION_PARAMS =
		Collections.singletonList(SELECTED_ENTITY);

	private static final List<RelationType<?>> INPUT_PARAMS =
		Collections.singletonList(SELECTED_ENTITY);

	static {
		RelationTypes.init(SelectEntity.class);
	}

	List<Function<? super E, ?>> attributes;

	private final QueryPredicate<E> selectableEntities;

	private final Predicate<? super Entity> sortOrder;

	private final boolean validateSelection;

	/**
	 * Creates a new instance.
	 *
	 * @param selectableEntities The class of the entity type to select
	 * @param sortOrder          A predicate defining the sorting of the
	 *                           entities or NULL for the default order
	 * @param attributes         The entity attributes to display
	 * @param validateSelection  TRUE to validate that an entity has been
	 *                           selected; FALSE to allow no selection (i.e. a
	 *                           NULL value) without a validation error
	 */
	public SelectEntity(QueryPredicate<E> selectableEntities,
		Predicate<? super Entity> sortOrder,
		List<Function<? super E, ?>> attributes, boolean validateSelection) {
		this.selectableEntities = selectableEntities;
		this.sortOrder = sortOrder;
		this.attributes = attributes;
		this.validateSelection = validateSelection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters() {
		return INPUT_PARAMS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters() {
		return INTERACTION_PARAMS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> interactionParam)
		throws Exception {
		if (interactionParam == SELECTED_ENTITY) {
			InteractionFragment parent = getParent();

			if (parent instanceof DialogFragment) {
				((DialogFragment) parent).finishDialog(
					DialogFragment.DialogAction.OK);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		setInteractive(InteractiveInputMode.ACTION, SELECTED_ENTITY);
		setUIFlag(HIDE_LABEL, SELECTED_ENTITY);

		initEntityQuery();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<RelationType<?>, String> validateParameters(
		boolean onInteraction) {
		Map<RelationType<?>, String> invalidParams =
			super.validateParameters(onInteraction);

		if (validateSelection && !onInteraction &&
			getParameter(SELECTED_ENTITY) == null) {
			invalidParams.put(SELECTED_ENTITY, MSG_PARAM_NOT_SET);
		}

		return invalidParams;
	}

	/**
	 * Initializes the entity query.
	 */
	private void initEntityQuery() {
		if (attributes == null) {
			EntityDefinition<E> def = EntityManager.getEntityDefinition(
				selectableEntities.getQueryType());

			List<RelationType<?>> displayAttr =
				def.getDisplayAttributes(DisplayMode.COMPACT);

			attributes = new ArrayList<Function<? super E, ?>>(displayAttr);
		}

		annotateForEntityQuery(SELECTED_ENTITY, selectableEntities, sortOrder,
			attributes);
	}
}
