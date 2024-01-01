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
import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.lib.text.TextConvert;
import de.esoco.process.ProcessRelationTypes.ListAction;
import de.esoco.process.step.Interaction;
import de.esoco.process.step.InteractionFragment;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StorageException;
import org.obrel.core.RelationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import static de.esoco.process.ProcessRelationTypes.INTERACTION_EVENT_TYPE;

/**
 * A process fragment that allows the interactive selection of entities of a
 * certain type. It consists of two lists with the source entities on the left,
 * the selected entities on the right, and list manipulation buttons between the
 * lists.
 *
 * @author eso
 */
public class SelectEntities<E extends Entity> extends InteractionFragment {

	private static final long serialVersionUID = 1L;

	private final String identifier;

	private final Class<E> entityClass;

	private final List<Function<? super E, ?>> columns;

	private final Set<Long> selectedEntityIds = new LinkedHashSet<>();

	private Predicate<? super E> queryCriteria;

	private Predicate<? super Entity> sortOrder;

	private List<Function<? super E, ?>> selectionColumns;

	private Function<? super E, String> createEntityInfo;

	private boolean update;

	private RelationType<String> unselectedHeaderParam;

	private RelationType<String> selectedHeaderParam;

	private RelationType<E> unselectedEntitiesParam;

	private RelationType<E> selectedEntitiesParam;

	private RelationType<ListAction> listActionParam;

	private RelationType<String> entityInfoParam;

	/**
	 * Creates a new instance with a varargs list for the column attributes
	 * list.
	 *
	 * @see SelectEntities#SelectEntities(String, Class, List)
	 */
	@SafeVarargs
	public SelectEntities(String identifier, Class<E> entityClass,
		Function<? super E, ?>... columns) {
		this(identifier, entityClass, Arrays.asList(columns));
	}

	/**
	 * Creates a new instance with specific parameters. The given identifier
	 * string must be unique in the context of this instance because it will be
	 * used to generate temporary parameters and resource identifiers.
	 *
	 * @param identifier  An identifier string for this instance
	 * @param entityClass The class of the entities to be selected
	 * @param columns     The entity attribute functions for the table columns
	 *                    to be displayed; empty to use the entity
	 *                    attributes of
	 *                    {@link DisplayMode#COMPACT}
	 */
	public SelectEntities(String identifier, Class<E> entityClass,
		List<Function<? super E, ?>> columns) {
		this.identifier = identifier;
		this.entityClass = entityClass;

		if (columns == null || columns.isEmpty()) {
			List<RelationType<?>> displayAttr = EntityManager
				.getEntityDefinition(entityClass)
				.getDisplayAttributes(DisplayMode.COMPACT);

			columns = new ArrayList<Function<? super E, ?>>(displayAttr);
		}

		this.columns = columns;
		this.selectionColumns = columns;
	}

	/**
	 * Returns a predicate that can be used as the criteria of a storage query
	 * for a certain set of entities based on their ID.
	 *
	 * @param entityClass The entity class of the list elements
	 * @param entities    The entities to create the predicate for (can be NULL
	 *                    for none)
	 * @return A predicate containing the entity ID criteria for the given
	 * entities
	 */
	public static <E extends Entity> Predicate<E> getEntityListPredicate(
		Class<E> entityClass, Collection<E> entities) {
		RelationType<Number> idAttr =
			EntityManager.getEntityDefinition(entityClass).getIdAttribute();

		// use a dummy predicate that will find nothing as the default
		Predicate<E> entityList =
			ifAttribute(idAttr, equalTo(Integer.valueOf(-1)));

		if (entities != null) {
			Collection<Number> ids = CollectionUtil.map(entities, idAttr);

			if (ids.size() > 0) {
				entityList = ifAttribute(idAttr, elementOf(ids));
			}
		}

		return entityList;
	}

	/**
	 * @see InteractionFragment#getInputParameters()
	 */
	@Override
	public List<RelationType<?>> getInputParameters() {
		return CollectionUtil.listOf(unselectedEntitiesParam, listActionParam,
			selectedEntitiesParam);
	}

	/**
	 * @see InteractionFragment#getInteractionParameters()
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters() {
		List<RelationType<?>> interactionParams =
			CollectionUtil.listOf(unselectedHeaderParam, selectedHeaderParam,
				unselectedEntitiesParam, listActionParam,
				selectedEntitiesParam);

		if (createEntityInfo != null) {
			interactionParams.add(entityInfoParam);
		}

		return interactionParams;
	}

	/**
	 * Returns the IDs of the currently selected entities.
	 *
	 * @return A new set containing the selected entity IDs
	 */
	public final Set<Long> getSelectedEntityIds() {
		return new LinkedHashSet<>(selectedEntityIds);
	}

	/**
	 * @see InteractionFragment#handleInteraction(RelationType)
	 */
	@Override
	public void handleInteraction(RelationType<?> interactionParam)
		throws Exception {
		ListAction listAction = null;

		if (interactionParam == unselectedEntitiesParam ||
			interactionParam == selectedEntitiesParam) {
			if (createEntityInfo != null &&
				getParameter(INTERACTION_EVENT_TYPE) !=
					InteractionEventType.ACTION) {
				@SuppressWarnings("unchecked")
				E entity = (E) getParameter(interactionParam);

				setParameter(entityInfoParam,
					createEntityInfo.evaluate(entity));
			} else if (interactionParam == selectedEntitiesParam) {
				listAction = ListAction.REMOVE_SELECTED;
			} else {
				listAction = ListAction.ADD_SELECTED;
			}
		} else {
			listAction = getParameter(listActionParam);
		}

		if (listAction != null) {
			handleListAction(listAction);
		}
	}

	/**
	 * @see InteractionFragment#init()
	 */
	@Override
	public void init() throws Exception {
		selectedEntityIds.clear();
		update = true;

		String unselectedHeader = TextConvert.capitalizedIdentifier(
			unselectedHeaderParam.getSimpleName());
		String selectedHeader = TextConvert.capitalizedIdentifier(
			selectedHeaderParam.getSimpleName());

		setParameter(unselectedHeaderParam, "$lbl" + unselectedHeader);
		setParameter(selectedHeaderParam, "$lbl" + selectedHeader);

		setUIFlag(HIDE_LABEL, unselectedEntitiesParam, unselectedHeaderParam);
		setUIFlag(SAME_ROW, listActionParam, selectedEntitiesParam,
			selectedHeaderParam);

		setUIProperty(2, COLUMN_SPAN, unselectedHeaderParam);
		setUIProperty(HTML_WIDTH, "50%", unselectedEntitiesParam,
			selectedEntitiesParam);
		setUIProperty(HTML_HEIGHT, "100%", unselectedEntitiesParam,
			selectedEntitiesParam);

		setUIProperty(RESOURCE_ID, "ListAction", listActionParam);
		setUIProperty(LABEL, "", unselectedHeaderParam, selectedHeaderParam,
			listActionParam, unselectedEntitiesParam, selectedEntitiesParam);

		setUIProperty(INTERACTIVE_INPUT_MODE, InteractiveInputMode.BOTH,
			unselectedEntitiesParam, selectedEntitiesParam);

		setUIFlag(HIDE_LABEL, entityInfoParam);
		setUIProperty(3, COLUMN_SPAN, entityInfoParam);
		setUIProperty(-1, ROWS, entityInfoParam);
		setUIProperty(LABEL, "", entityInfoParam);

		setImmediateAction(listActionParam, ListAction.ADD_ALL,
			ListAction.REMOVE_ALL);
	}

	/**
	 * @see InteractionFragment#prepareInteraction()
	 */
	@Override
	public void prepareInteraction() throws Exception {
		if (createEntityInfo != null) {
			setAllowedValues(listActionParam, ListAction.ADD_SELECTED,
				ListAction.ADD_ALL, ListAction.REMOVE_ALL,
				ListAction.REMOVE_SELECTED);
		} else {
			setAllowedValues(listActionParam, ListAction.ADD_ALL,
				ListAction.REMOVE_ALL);
		}

		if (update) {
			update = false;
			update();
		}
	}

	/**
	 * Queries a list of the currently selected entities.
	 *
	 * @return A new list containing the selected entities
	 * @throws StorageException If the query fails
	 */
	public final List<E> querySelectedEntities() throws StorageException {
		EntityDefinition<E> def =
			EntityManager.getEntityDefinition(entityClass);

		List<E> entities;

		if (selectedEntityIds.size() > 0) {
			entities = EntityManager.queryEntities(entityClass,
				ifAttribute(def.getIdAttribute(),
					elementOf(selectedEntityIds)),
				selectedEntityIds.size());
		} else {
			entities = new ArrayList<>();
		}

		return entities;
	}

	/**
	 * Sets a function that generate an information string for an input entity.
	 * This information will then be displayed on selection of an entity.
	 *
	 * @param createEntityInfo The entity info function
	 */
	public final void setEntityInfoFunction(
		Function<? super E, String> createEntityInfo) {
		this.createEntityInfo = createEntityInfo;
	}

	/**
	 * Sets the query constraints of this instance. This will also reset the
	 * current selection and should therefore only be invoked if the
	 * constraints
	 * actually have changed.
	 *
	 * @param criteria The query criteria or NULL for all entities of the
	 *                    chosen
	 *                 type
	 * @param sort     The sort predicate or NULL for no explicit order
	 */
	public void setQueryConstraints(Predicate<? super E> criteria,
		Predicate<? super Entity> sort) {
		if (criteria != null && !criteria.equals(queryCriteria) ||
			queryCriteria != null && !queryCriteria.equals(criteria)) {
			update = true;
		}

		if (sort != null && !sort.equals(sortOrder) ||
			sortOrder != null && !sortOrder.equals(sort)) {
			update = true;
		}

		queryCriteria = criteria;
		sortOrder = sort;
	}

	/**
	 * Sets the selection of this instance to the given entity IDs that match
	 * the current query criteria. Entity IDs, which entity does not comply
	 * with
	 * the current query criteria are filtered out.
	 *
	 * @param ids A collection containing the IDs of the entities to select
	 */
	public final void setSelectedEntityIds(Collection<Long> ids) {
		selectedEntityIds.clear();
		selectedEntityIds.addAll(ids);
		update = true;
	}

	/**
	 * Sets the column attributes to be displayed in the table containing the
	 * selected entities. If not they will default to the same columns as the
	 * all entities table.
	 *
	 * @param selectionColumns The columns of the selection table
	 */
	public void setSelectionColumns(
		List<Function<? super E, ?>> selectionColumns) {
		this.selectionColumns = selectionColumns;
	}

	/**
	 * Updates the query criteria of this instance and re-initializes the
	 * storage query. All other parameters like columns or sort order must have
	 * been set before to be considered for the update.
	 */
	public void update() throws StorageException {
		Predicate<? super E> criteria = queryCriteria;
		Predicate<Object> isSelected;

		RelationType<Number> idAttr =
			EntityManager.getEntityDefinition(entityClass).getIdAttribute();

		if (selectedEntityIds.size() > 0) {
			isSelected = elementOf(new HashSet<>(selectedEntityIds));
			criteria =
				Predicates.and(criteria, ifAttribute(idAttr, not(isSelected)));
		} else {
			// use dummy predicate to find nothing if selection is empty
			isSelected = equalTo(null);
		}

		QueryPredicate<E> unselectedEntities = forEntity(entityClass,
			criteria);

		QueryPredicate<E> selectedEntities = forEntity(entityClass,
			Predicates.and(ifAttribute(idAttr, isSelected), (queryCriteria)));

		annotateForEntityQuery(unselectedEntitiesParam, unselectedEntities,
			sortOrder, columns);
		annotateForEntityQuery(selectedEntitiesParam, selectedEntities,
			sortOrder, selectionColumns);

		updateSelectedEntityIds(selectedEntities);

		setUIProperty(-1, CURRENT_SELECTION, unselectedEntitiesParam,
			selectedEntitiesParam);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void afterInteraction(RelationType<?> interactionParam)
		throws Exception {
		markParameterAsModified(unselectedEntitiesParam);
		markParameterAsModified(selectedEntitiesParam);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initProcessStep(Interaction processStep) {
		String prefix = TextConvert.uppercaseIdentifier(identifier);
		String entities = entityClass.getSimpleName();

		entities =
			TextConvert.uppercaseIdentifier(TextConvert.toPlural(entities));

		String unselected = prefix + "_UNSELECTED_" + entities;
		String selected = prefix + "_SELECTED_" + entities;
		String action = prefix + "_SELECT_" + entities + "_ACTION";
		String entityInfo = prefix + "_SELECT_" + entities + "_INFOS";

		unselectedHeaderParam =
			getTemporaryParameterType(unselected + "_HEADER", String.class);
		selectedHeaderParam =
			getTemporaryParameterType(selected + "_HEADER", String.class);
		unselectedEntitiesParam =
			getTemporaryParameterType(unselected, Entity.class);
		selectedEntitiesParam =
			getTemporaryParameterType(selected, Entity.class);

		listActionParam = getTemporaryParameterType(action, ListAction.class);
		entityInfoParam = getTemporaryParameterType(entityInfo, String.class);
	}

	/**
	 * Handles the manipulation actions for the list of selected domains.
	 *
	 * @param listAction The selected list action
	 * @throws StorageException If a storage access fails
	 */
	private void handleListAction(ListAction listAction)
		throws StorageException {
		boolean select = listAction == ListAction.ADD_ALL ||
			listAction == ListAction.ADD_SELECTED;

		switch (listAction) {
			case ADD_ALL:

				QueryPredicate<E> query =
					getCurrentQuery(unselectedEntitiesParam);

				if (query != null) {
					List<E> entities =
						EntityManager.queryEntities(query, Short.MAX_VALUE);

					for (Entity entity : entities) {
						setSelected(entity, select);
					}
				}

				break;

			case REMOVE_ALL:
				selectedEntityIds.clear();
				break;

			case ADD_SELECTED:
			case REMOVE_SELECTED:

				Entity entity = getParameter(
					select ? unselectedEntitiesParam : selectedEntitiesParam);

				if (entity != null) {
					setSelected(entity, select);
				}

				break;
		}

		update = true;
	}

	/**
	 * Selects or deselects a certain entity.
	 *
	 * @param entity   The entity
	 * @param selected TRUE to select, FALSE to deselect
	 */
	private void setSelected(Entity entity, boolean selected) {
		@SuppressWarnings("boxing")
		Long id = entity.getId();

		if (selected) {
			selectedEntityIds.add(id);
		} else {
			selectedEntityIds.remove(id);
		}
	}

	/**
	 * Executes the given {@link QueryPredicate} and stored the Ids of the
	 * queried entities in {@link #selectedEntityIds}
	 */
	private void updateSelectedEntityIds(
		QueryPredicate<E> selectedEntitiesCriteria) throws StorageException {
		List<E> selectedEntities =
			EntityManager.queryEntities(selectedEntitiesCriteria,
				Short.MAX_VALUE);

		Collection<Long> selectedEntityIds =
			CollectionUtil.transform(selectedEntities, e -> e.getId());

		selectedEntityIds.clear();
		selectedEntityIds.addAll(selectedEntityIds);
	}
}
