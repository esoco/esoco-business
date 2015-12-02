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
import de.esoco.entity.EntityFunctions;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.text.TextConvert;

import de.esoco.process.ProcessStep;
import de.esoco.process.step.DialogFragment.DialogAction;
import de.esoco.process.step.DialogFragment.DialogActionListener;
import de.esoco.process.step.InteractionFragment;

import de.esoco.storage.QueryPredicate;

import java.util.List;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static de.esoco.entity.EntityPredicates.forEntity;

import static de.esoco.lib.property.UserInterfaceProperties.COLUMNS;
import static de.esoco.lib.property.UserInterfaceProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.UserInterfaceProperties.HAS_IMAGES;
import static de.esoco.lib.property.UserInterfaceProperties.HIDDEN;
import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.SAME_ROW;

import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * An interactive process fragment to input an entity by selecting it from a
 * dialog. The entity type and the target process parameter must be defined
 * through the constructor.
 *
 * @author eso
 */
public class ChooseEntity<E extends Entity> extends InteractionFragment
	implements DialogActionListener
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the actions for the entity selection.
	 */
	public enum ChooseEntityAction { SELECT, REMOVE }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/**
	 * A standard parameter that can be used to display this fragment in a
	 * process step.
	 */
	public static final RelationType<List<RelationType<?>>> CHOOSE_ENTITY_FRAGMENT =
		newListType();

	private static final RelationType<String> CURRENT_CHOOSE_ENTITY = newType();

	private static final RelationType<ChooseEntityAction> CHOOSE_ENTITY_ACTION =
		newType();

	static
	{
		RelationTypes.init(ChooseEntity.class);
	}

	//~ Instance fields --------------------------------------------------------

	private List<RelationType<?>> aInteractionParams =
		CollectionUtil.<RelationType<?>>listOf(CURRENT_CHOOSE_ENTITY,
											   CHOOSE_ENTITY_ACTION);

	private List<RelationType<?>> aInputParams =
		CollectionUtil.<RelationType<?>>listOf(CHOOSE_ENTITY_ACTION);

	private RelationType<E> rTargetParam;

	private SelectEntity<E> aSelectEntity;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rTargetParam        The parameter to store the selected entity in
	 * @param pSelectableEntities The class of the entity type to select
	 * @param pSortOrder          A predicate defining the sorting of the
	 *                            entities or NULL for the default order
	 * @param rQueryAttributes    The entity attributes to display
	 * @param bValidateSelection  TRUE to validate that an entity has been
	 *                            selected; FALSE to allow no selection (i.e. a
	 *                            NULL value) without a validation error
	 */
	public ChooseEntity(RelationType<E>				 rTargetParam,
						Predicate<? super E>		 pSelectableEntities,
						Predicate<? super Entity>    pSortOrder,
						List<Function<? super E, ?>> rQueryAttributes,
						boolean						 bValidateSelection)
	{
		this.rTargetParam = rTargetParam;

		@SuppressWarnings("unchecked")
		QueryPredicate<E> qSelectableEntities =
			forEntity((Class<E>) rTargetParam.getTargetType(),
					  pSelectableEntities);

		aSelectEntity =
			new SelectEntity<E>(qSelectableEntities,
								pSortOrder,
								rQueryAttributes,
								bValidateSelection);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see InteractionFragment#enableEdit(boolean)
	 */
	@Override
	public void enableEdit(boolean bEnable)
	{
		super.enableEdit(bEnable);

		if (bEnable)
		{
			clearUIFlag(HIDDEN, CHOOSE_ENTITY_ACTION);
		}
		else
		{
			setUIFlag(HIDDEN, CHOOSE_ENTITY_ACTION);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters()
	{
		return aInputParams;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters()
	{
		return aInteractionParams;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		if (rInteractionParam == CHOOSE_ENTITY_ACTION)
		{
			switch (getParameter(CHOOSE_ENTITY_ACTION))
			{
				case REMOVE:
					setParameter(rTargetParam, null);
					updateEntityDisplay();
					break;

				case SELECT:

					String sPrefix = getClass().getSimpleName();

					sPrefix += rTargetParam.getSimpleName();
					sPrefix = TextConvert.uppercaseIdentifier(sPrefix) + "_";
					setParameter(SelectEntity.SELECTED_ENTITY,
								 getParameter(rTargetParam));
					removeUIProperties(SelectEntity.SELECTED_ENTITY,
									   CURRENT_SELECTION);

					showDialog(sPrefix,
							   aSelectEntity,
							   this,
							   DialogAction.OK,
							   DialogAction.CANCEL);
					break;
			}
		}
	}

	/***************************************
	 * Set the query predicate.
	 *
	 * @see ProcessStep#prepareParameters()
	 */
	@Override
	public void init()
	{
		setImmediateAction(CHOOSE_ENTITY_ACTION);

		setUIFlag(HIDE_LABEL, CURRENT_CHOOSE_ENTITY);
		setUIFlag(SAME_ROW, CHOOSE_ENTITY_ACTION);
		setUIFlag(HAS_IMAGES, CHOOSE_ENTITY_ACTION);

		setUIProperty(2, COLUMNS, CHOOSE_ENTITY_ACTION);

		updateEntityDisplay();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void onDialogAction(DialogAction eAction)
	{
		if (eAction == DialogAction.OK)
		{
			E rEntity = (E) getParameter(SelectEntity.SELECTED_ENTITY);

			setParameter(rTargetParam, rEntity);
			updateEntityDisplay();
		}
	}

	/***************************************
	 * Updates the entity display.
	 */
	public void updateEntityDisplay()
	{
		Entity rEntity = getParameter(rTargetParam);

		setParameter(CURRENT_CHOOSE_ENTITY,
					 rEntity != null ? EntityFunctions.format(rEntity)
									 : "$lblNoValue");
	}
}
