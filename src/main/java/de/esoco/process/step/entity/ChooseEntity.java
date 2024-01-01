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
package de.esoco.process.step.entity;

import de.esoco.entity.Entity;
import de.esoco.entity.EntityFunctions;
import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.text.TextConvert;
import de.esoco.process.step.DialogFragment.DialogAction;
import de.esoco.process.step.DialogFragment.DialogActionListener;
import de.esoco.process.step.InteractionFragment;
import de.esoco.storage.QueryPredicate;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.util.List;

import static de.esoco.entity.EntityPredicates.forEntity;
import static de.esoco.lib.property.LayoutProperties.COLUMNS;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.StateProperties.HIDDEN;
import static de.esoco.lib.property.StyleProperties.HAS_IMAGES;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newType;

/**
 * An interactive process fragment to input an entity by selecting it from a
 * dialog. The entity type and the target process parameter must be defined
 * through the constructor.
 *
 * @author eso
 */
public class ChooseEntity<E extends Entity> extends InteractionFragment
	implements DialogActionListener {

	/**
	 * Enumeration of the actions for the entity selection.
	 */
	public enum ChooseEntityAction {SELECT, REMOVE}

	/**
	 * A standard parameter that can be used to display this fragment in a
	 * process step.
	 */
	public static final RelationType<List<RelationType<?>>>
		CHOOSE_ENTITY_FRAGMENT = newListType();

	private static final long serialVersionUID = 1L;

	private static final RelationType<String> CURRENT_CHOOSE_ENTITY =
		newType();

	private static final RelationType<ChooseEntityAction> CHOOSE_ENTITY_ACTION =
		newType();

	static {
		RelationTypes.init(ChooseEntity.class);
	}

	private final List<RelationType<?>> interactionParams =
		CollectionUtil.listOf(CURRENT_CHOOSE_ENTITY, CHOOSE_ENTITY_ACTION);

	private final List<RelationType<?>> inputParams =
		CollectionUtil.listOf(CHOOSE_ENTITY_ACTION);

	private final RelationType<E> targetParam;

	private final SelectEntity<E> selectEntity;

	/**
	 * Creates a new instance.
	 *
	 * @param targetParam        The parameter to store the selected entity in
	 * @param selectableEntities The class of the entity type to select
	 * @param sortOrder          A predicate defining the sorting of the
	 *                           entities or NULL for the default order
	 * @param queryAttributes    The entity attributes to display
	 * @param validateSelection  TRUE to validate that an entity has been
	 *                           selected; FALSE to allow no selection (i.e. a
	 *                           NULL value) without a validation error
	 */
	public ChooseEntity(RelationType<E> targetParam,
		Predicate<? super E> selectableEntities,
		Predicate<? super Entity> sortOrder,
		List<Function<? super E, ?>> queryAttributes,
		boolean validateSelection) {
		this.targetParam = targetParam;

		@SuppressWarnings("unchecked")
		QueryPredicate<E> querySelectableEntities =
			forEntity((Class<E>) targetParam.getTargetType(),
				selectableEntities);

		selectEntity = new SelectEntity<E>(querySelectableEntities, sortOrder,
			queryAttributes, validateSelection);
	}

	/**
	 * @see InteractionFragment#enableEdit(boolean)
	 */
	@Override
	public void enableEdit(boolean enable) {
		super.enableEdit(enable);

		if (enable) {
			clearUIFlag(HIDDEN, CHOOSE_ENTITY_ACTION);
		} else {
			setUIFlag(HIDDEN, CHOOSE_ENTITY_ACTION);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters() {
		return inputParams;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters() {
		return interactionParams;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> interactionParam)
		throws Exception {
		if (interactionParam == CHOOSE_ENTITY_ACTION) {
			switch (getParameter(CHOOSE_ENTITY_ACTION)) {
				case REMOVE:
					setParameter(targetParam, null);
					updateEntityDisplay();
					break;

				case SELECT:

					String prefix = getClass().getSimpleName();

					prefix += targetParam.getSimpleName();
					prefix = TextConvert.uppercaseIdentifier(prefix) + "_";
					setParameter(SelectEntity.SELECTED_ENTITY,
						getParameter(targetParam));
					removeUIProperties(SelectEntity.SELECTED_ENTITY,
						CURRENT_SELECTION);

					showDialog(prefix, selectEntity, this, DialogAction.OK,
						DialogAction.CANCEL);
					break;
			}
		}
	}

	/**
	 * Set the query predicate.
	 */
	@Override
	public void init() {
		setImmediateAction(CHOOSE_ENTITY_ACTION);

		setUIFlag(HIDE_LABEL, CURRENT_CHOOSE_ENTITY);
		setUIFlag(SAME_ROW, CHOOSE_ENTITY_ACTION);
		setUIFlag(HAS_IMAGES, CHOOSE_ENTITY_ACTION);

		setUIProperty(2, COLUMNS, CHOOSE_ENTITY_ACTION);

		updateEntityDisplay();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void onDialogAction(DialogAction action) {
		if (action == DialogAction.OK) {
			E entity = (E) getParameter(SelectEntity.SELECTED_ENTITY);

			setParameter(targetParam, entity);
			updateEntityDisplay();
		}
	}

	/**
	 * Updates the entity display.
	 */
	public void updateEntityDisplay() {
		Entity entity = getParameter(targetParam);

		setParameter(CURRENT_CHOOSE_ENTITY,
			entity != null ? EntityFunctions.format(entity) : "$lblNoValue");
	}
}
