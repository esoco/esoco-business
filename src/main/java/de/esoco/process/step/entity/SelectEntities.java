//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.lib.text.TextConvert;

import de.esoco.process.ProcessRelationTypes.ListAction;
import de.esoco.process.step.Interaction;
import de.esoco.process.step.InteractionFragment;

import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StorageException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obrel.core.RelationType;

import static de.esoco.entity.EntityPredicates.forEntity;
import static de.esoco.entity.EntityPredicates.ifAttribute;

import static de.esoco.lib.expression.CollectionPredicates.elementOf;
import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.not;
import static de.esoco.lib.property.ContentProperties.LABEL;
import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.ROWS;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.StateProperties.INTERACTIVE_INPUT_MODE;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;

import static de.esoco.process.ProcessRelationTypes.INTERACTIVE_INPUT_ACTION_EVENT;


/********************************************************************
 * A process fragment that allows the interactive selection of entities of a
 * certain type. It consists of two lists with the source entities on the left,
 * the selected entities on the right, and list manipulation buttons between the
 * lists.
 *
 * @author eso
 */
public class SelectEntities<E extends Entity> extends InteractionFragment
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private String   sIdentifier;
	private Class<E> rEntityClass;

	private Predicate<? super E>		 pQueryCriteria;
	private Predicate<? super Entity>    pSortOrder;
	private List<Function<? super E, ?>> rColumns;
	private List<Function<? super E, ?>> rSelectionColumns;

	private Function<? super E, String> fCreateEntityInfo;

	private boolean bUpdate;

	private Set<Long> aSelectedEntityIds = new LinkedHashSet<>();

	private RelationType<String>     aUnselectedHeaderParam;
	private RelationType<String>     aSelectedHeaderParam;
	private RelationType<E>			 aUnselectedEntitiesParam;
	private RelationType<E>			 aSelectedEntitiesParam;
	private RelationType<ListAction> aListActionParam;
	private RelationType<String>     aEntityInfoParam;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a varargs list for the column attributes
	 * list.
	 *
	 * @see SelectEntities#SelectEntities(String, Class, List)
	 */
	@SafeVarargs
	public SelectEntities(String					sIdentifier,
						  Class<E>					rEntityClass,
						  Function<? super E, ?>... rColumns)
	{
		this(sIdentifier, rEntityClass, Arrays.asList(rColumns));
	}

	/***************************************
	 * Creates a new instance with specific parameters. The given identifier
	 * string must be unique in the context of this instance because it will be
	 * used to generate temporary parameters and resource identifiers.
	 *
	 * @param sIdentifier  An identifier string for this instance
	 * @param rEntityClass The class of the entities to be selected
	 * @param rColumns     The entity attribute functions for the table columns
	 *                     to be displayed; empty to use the entity attributes
	 *                     of {@link DisplayMode#COMPACT}
	 */
	public SelectEntities(String					   sIdentifier,
						  Class<E>					   rEntityClass,
						  List<Function<? super E, ?>> rColumns)
	{
		this.sIdentifier  = sIdentifier;
		this.rEntityClass = rEntityClass;

		if (rColumns == null || rColumns.isEmpty())
		{
			List<RelationType<?>> rDisplayAttr =
				EntityManager.getEntityDefinition(rEntityClass)
							 .getDisplayAttributes(DisplayMode.COMPACT);

			rColumns = new ArrayList<Function<? super E, ?>>(rDisplayAttr);
		}

		this.rColumns		   = rColumns;
		this.rSelectionColumns = rColumns;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a predicate that can be used as the criteria of a storage query
	 * for a certain set of entities based on their ID.
	 *
	 * @param  rEntityClass The entity class of the list elements
	 * @param  rEntities    The entities to create the predicate for (can be
	 *                      NULL for none)
	 *
	 * @return A predicate containing the entity ID criteria for the given
	 *         entities
	 */
	public static <E extends Entity> Predicate<E> getEntityListPredicate(
		Class<E>	  rEntityClass,
		Collection<E> rEntities)
	{
		RelationType<Number> rIdAttr =
			EntityManager.getEntityDefinition(rEntityClass).getIdAttribute();

		// use a dummy predicate that will find nothing as the default
		Predicate<E> pEntityList =
			ifAttribute(rIdAttr, equalTo(Integer.valueOf(-1)));

		if (rEntities != null)
		{
			Collection<Number> rIds = CollectionUtil.map(rEntities, rIdAttr);

			if (rIds.size() > 0)
			{
				pEntityList = ifAttribute(rIdAttr, elementOf(rIds));
			}
		}

		return pEntityList;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see InteractionFragment#getInputParameters()
	 */
	@Override
	public List<RelationType<?>> getInputParameters()
	{
		return CollectionUtil.<RelationType<?>>listOf(aUnselectedEntitiesParam,
													  aListActionParam,
													  aSelectedEntitiesParam);
	}

	/***************************************
	 * @see InteractionFragment#getInteractionParameters()
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters()
	{
		List<RelationType<?>> rInteractionParams =
			CollectionUtil.<RelationType<?>>listOf(aUnselectedHeaderParam,
												   aSelectedHeaderParam,
												   aUnselectedEntitiesParam,
												   aListActionParam,
												   aSelectedEntitiesParam);

		if (fCreateEntityInfo != null)
		{
			rInteractionParams.add(aEntityInfoParam);
		}

		return rInteractionParams;
	}

	/***************************************
	 * Returns the IDs of the currently selected entities.
	 *
	 * @return A new set containing the selected entity IDs
	 */
	public final Set<Long> getSelectedEntityIds()
	{
		return new LinkedHashSet<>(aSelectedEntityIds);
	}

	/***************************************
	 * @see InteractionFragment#handleInteraction(RelationType)
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		ListAction eListAction = null;

		if (rInteractionParam == aUnselectedEntitiesParam ||
			rInteractionParam == aSelectedEntitiesParam)
		{
			boolean bAction = hasFlagParameter(INTERACTIVE_INPUT_ACTION_EVENT);

			if (fCreateEntityInfo != null && !bAction)
			{
				@SuppressWarnings("unchecked")
				E rEntity = (E) getParameter(rInteractionParam);

				setParameter(aEntityInfoParam,
							 fCreateEntityInfo.evaluate(rEntity));
			}
			else if (rInteractionParam == aSelectedEntitiesParam)
			{
				eListAction = ListAction.REMOVE_SELECTED;
			}
			else
			{
				eListAction = ListAction.ADD_SELECTED;
			}
		}
		else
		{
			eListAction = getParameter(aListActionParam);
		}

		if (eListAction != null)
		{
			handleListAction(eListAction);
		}
	}

	/***************************************
	 * @see InteractionFragment#init()
	 */
	@Override
	public void init() throws Exception
	{
		aSelectedEntityIds.clear();
		bUpdate = true;

		String sUnselectedHeader =
			TextConvert.capitalizedIdentifier(aUnselectedHeaderParam
											  .getSimpleName());
		String sSelectedHeader   =
			TextConvert.capitalizedIdentifier(aSelectedHeaderParam
											  .getSimpleName());

		setParameter(aUnselectedHeaderParam, "$lbl" + sUnselectedHeader);
		setParameter(aSelectedHeaderParam, "$lbl" + sSelectedHeader);

		setUIFlag(HIDE_LABEL, aUnselectedEntitiesParam, aUnselectedHeaderParam);
		setUIFlag(SAME_ROW,
				  aListActionParam,
				  aSelectedEntitiesParam,
				  aSelectedHeaderParam);

		setUIProperty(2, COLUMN_SPAN, aUnselectedHeaderParam);
		setUIProperty(HTML_WIDTH,
					  "50%",
					  aUnselectedEntitiesParam,
					  aSelectedEntitiesParam);
		setUIProperty(HTML_HEIGHT,
					  "100%",
					  aUnselectedEntitiesParam,
					  aSelectedEntitiesParam);

		setUIProperty(RESOURCE_ID, "ListAction", aListActionParam);
		setUIProperty(LABEL,
					  "",
					  aUnselectedHeaderParam,
					  aSelectedHeaderParam,
					  aListActionParam,
					  aUnselectedEntitiesParam,
					  aSelectedEntitiesParam);

		setUIProperty(INTERACTIVE_INPUT_MODE,
					  InteractiveInputMode.BOTH,
					  aUnselectedEntitiesParam,
					  aSelectedEntitiesParam);

		setUIFlag(HIDE_LABEL, aEntityInfoParam);
		setUIProperty(3, COLUMN_SPAN, aEntityInfoParam);
		setUIProperty(-1, ROWS, aEntityInfoParam);
		setUIProperty(LABEL, "", aEntityInfoParam);

		setImmediateAction(aListActionParam,
						   ListAction.ADD_ALL,
						   ListAction.REMOVE_ALL);
	}

	/***************************************
	 * @see InteractionFragment#prepareInteraction()
	 */
	@Override
	public void prepareInteraction() throws Exception
	{
		if (fCreateEntityInfo != null)
		{
			setAllowedValues(aListActionParam,
							 ListAction.ADD_SELECTED,
							 ListAction.ADD_ALL,
							 ListAction.REMOVE_ALL,
							 ListAction.REMOVE_SELECTED);
		}
		else
		{
			setAllowedValues(aListActionParam,
							 ListAction.ADD_ALL,
							 ListAction.REMOVE_ALL);
		}

		if (bUpdate)
		{
			bUpdate = false;
			update();
		}
	}

	/***************************************
	 * Queries a list of the currently selected entities.
	 *
	 * @return A new list containing the selected entities
	 *
	 * @throws StorageException If the query fails
	 */
	public final List<E> querySelectedEntities() throws StorageException
	{
		EntityDefinition<E> rDef =
			EntityManager.getEntityDefinition(rEntityClass);

		List<E> rEntities;

		if (aSelectedEntityIds.size() > 0)
		{
			rEntities =
				EntityManager.queryEntities(rEntityClass,
											ifAttribute(rDef.getIdAttribute(),
														elementOf(aSelectedEntityIds)),
											aSelectedEntityIds.size());
		}
		else
		{
			rEntities = new ArrayList<>();
		}

		return rEntities;
	}

	/***************************************
	 * Sets a function that generate an information string for an input entity.
	 * This information will then be displayed on selection of an entity.
	 *
	 * @param fCreateEntityInfo The entity info function
	 */
	public final void setEntityInfoFunction(
		Function<? super E, String> fCreateEntityInfo)
	{
		this.fCreateEntityInfo = fCreateEntityInfo;
	}

	/***************************************
	 * Sets the query constraints of this instance. This will also reset the
	 * current selection and should therefore only be invoked if the constraints
	 * actually have changed.
	 *
	 * @param pCriteria The query criteria or NULL for all entities of the
	 *                  chosen type
	 * @param pSort     The sort predicate or NULL for no explicit order
	 */
	public void setQueryConstraints(
		Predicate<? super E>	  pCriteria,
		Predicate<? super Entity> pSort)
	{
		if (pCriteria != null && !pCriteria.equals(pQueryCriteria) ||
			pQueryCriteria != null && !pQueryCriteria.equals(pCriteria))
		{
			bUpdate = true;
		}

		if (pSort != null && !pSort.equals(pSortOrder) ||
			pSortOrder != null && !pSortOrder.equals(pSort))
		{
			bUpdate = true;
		}

		pQueryCriteria = pCriteria;
		pSortOrder     = pSort;
	}

	/***************************************
	 * Sets the selection of this instance to the given entity IDs that match
	 * the current query criteria. Entity IDs, which entity does not comply with
	 * the current query criteria are filtered out.
	 *
	 * @param rIds A collection containing the IDs of the entities to select
	 */
	public final void setSelectedEntityIds(Collection<Long> rIds)
	{
		aSelectedEntityIds.clear();
		aSelectedEntityIds.addAll(rIds);
		bUpdate = true;
	}

	/***************************************
	 * Sets the column attributes to be displayed in the table containing the
	 * selected entities. If not they will default to the same columns as the
	 * all entities table.
	 *
	 * @param rSelectionColumns The columns of the selection table
	 */
	public void setSelectionColumns(
		List<Function<? super E, ?>> rSelectionColumns)
	{
		this.rSelectionColumns = rSelectionColumns;
	}

	/***************************************
	 * Updates the query criteria of this instance and re-initializes the
	 * storage query. All other parameters like columns or sort order must have
	 * been set before to be considered for the update.
	 *
	 * @throws StorageException
	 */
	public void update() throws StorageException
	{
		Predicate<? super E> pCriteria   = pQueryCriteria;
		Predicate<Object>    pIsSelected;

		RelationType<Number> rIdAttr =
			EntityManager.getEntityDefinition(rEntityClass).getIdAttribute();

		if (aSelectedEntityIds.size() > 0)
		{
			pIsSelected = elementOf(new HashSet<>(aSelectedEntityIds));
			pCriteria   =
				Predicates.and(pCriteria,
							   ifAttribute(rIdAttr, not(pIsSelected)));
		}
		else
		{
			// use dummy predicate to find nothing if selection is empty
			pIsSelected = equalTo(null);
		}

		QueryPredicate<E> pUnselectedEntities =
			forEntity(rEntityClass, pCriteria);

		QueryPredicate<E> pSelectedEntities =
			forEntity(rEntityClass,
					  Predicates.and(ifAttribute(rIdAttr, pIsSelected),
									 (pQueryCriteria)));

		annotateForEntityQuery(aUnselectedEntitiesParam,
							   pUnselectedEntities,
							   pSortOrder,
							   rColumns);
		annotateForEntityQuery(aSelectedEntitiesParam,
							   pSelectedEntities,
							   pSortOrder,
							   rSelectionColumns);

		updateSelectedEntityIds(pSelectedEntities);

		setUIProperty(-1,
					  CURRENT_SELECTION,
					  aUnselectedEntitiesParam,
					  aSelectedEntitiesParam);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void afterInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		markParameterAsModified(aUnselectedEntitiesParam);
		markParameterAsModified(aSelectedEntitiesParam);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void initProcessStep(Interaction rProcessStep)
	{
		String sPrefix   = TextConvert.uppercaseIdentifier(sIdentifier);
		String sEntities = rEntityClass.getSimpleName();

		sEntities =
			TextConvert.uppercaseIdentifier(TextConvert.toPlural(sEntities));

		String sUnselected = sPrefix + "_UNSELECTED_" + sEntities;
		String sSelected   = sPrefix + "_SELECTED_" + sEntities;
		String sAction     = sPrefix + "_SELECT_" + sEntities + "_ACTION";
		String sEntityInfo = sPrefix + "_SELECT_" + sEntities + "_INFOS";

		aUnselectedHeaderParam   =
			getTemporaryParameterType(sUnselected + "_HEADER", String.class);
		aSelectedHeaderParam     =
			getTemporaryParameterType(sSelected + "_HEADER", String.class);
		aUnselectedEntitiesParam =
			getTemporaryParameterType(sUnselected, Entity.class);
		aSelectedEntitiesParam   =
			getTemporaryParameterType(sSelected, Entity.class);

		aListActionParam = getTemporaryParameterType(sAction, ListAction.class);
		aEntityInfoParam = getTemporaryParameterType(sEntityInfo, String.class);
	}

	/***************************************
	 * Handles the manipulation actions for the list of selected domains.
	 *
	 * @param  eListAction The selected list action
	 *
	 * @throws StorageException If a storage access fails
	 */
	private void handleListAction(ListAction eListAction)
		throws StorageException
	{
		boolean bSelect =
			eListAction == ListAction.ADD_ALL ||
			eListAction == ListAction.ADD_SELECTED;

		switch (eListAction)
		{
			case ADD_ALL:

				QueryPredicate<E> pQuery =
					getCurrentQuery(aUnselectedEntitiesParam);

				if (pQuery != null)
				{
					List<E> rEntities =
						EntityManager.queryEntities(pQuery, Short.MAX_VALUE);

					for (Entity rEntity : rEntities)
					{
						setSelected(rEntity, bSelect);
					}
				}

				break;

			case REMOVE_ALL:
				aSelectedEntityIds.clear();
				break;

			case ADD_SELECTED:
			case REMOVE_SELECTED:

				Entity rEntity =
					getParameter(bSelect ? aUnselectedEntitiesParam
										 : aSelectedEntitiesParam);

				if (rEntity != null)
				{
					setSelected(rEntity, bSelect);
				}

				break;
		}

		bUpdate = true;
	}

	/***************************************
	 * Selects or deselects a certain entity.
	 *
	 * @param rEntity   The entity
	 * @param bSelected TRUE to select, FALSE to deselect
	 */
	private void setSelected(Entity rEntity, boolean bSelected)
	{
		@SuppressWarnings("boxing")
		Long rId = rEntity.getId();

		if (bSelected)
		{
			aSelectedEntityIds.add(rId);
		}
		else
		{
			aSelectedEntityIds.remove(rId);
		}
	}

	/***************************************
	 * Executes the given {@link QueryPredicate} and stored the Ids of the
	 * queried entities in {@link #aSelectedEntityIds}
	 *
	 * @param  qSelectedEntities
	 *
	 * @throws StorageException
	 */
	private void updateSelectedEntityIds(QueryPredicate<E> qSelectedEntities)
		throws StorageException
	{
		List<E> rSelectedEntities =
			EntityManager.queryEntities(qSelectedEntities, Short.MAX_VALUE);

		Collection<Long> rSelectedEntityIds =
			CollectionUtil.transform(rSelectedEntities, e -> e.getId());

		aSelectedEntityIds.clear();
		aSelectedEntityIds.addAll(rSelectedEntityIds);
	}
}
