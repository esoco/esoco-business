//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.data.element.DateDataElement.DateInputType;
import de.esoco.entity.Entity;
import de.esoco.entity.EntityDefinition;
import de.esoco.entity.EntityDefinition.DisplayMode;
import de.esoco.entity.EntityManager;
import de.esoco.entity.ExtraAttribute;
import de.esoco.entity.ExtraAttributes;
import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.lib.text.TextConvert;
import de.esoco.process.step.DialogFragment.DialogAction;
import de.esoco.process.step.InteractionFragment;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StorageException;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static de.esoco.data.element.DateDataElement.DATE_INPUT_TYPE;
import static de.esoco.entity.EntityPredicates.forEntity;
import static de.esoco.entity.EntityPredicates.ifAttribute;
import static de.esoco.entity.ExtraAttributes.EXTRA_ATTRIBUTE_FLAG;
import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.property.ContentProperties.LABEL;
import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.ROWS;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.StateProperties.DISABLED;
import static de.esoco.lib.property.StyleProperties.HAS_IMAGES;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.HIERARCHICAL;
import static de.esoco.lib.property.StyleProperties.LIST_STYLE;
import static de.esoco.lib.property.StyleProperties.STYLE;
import static de.esoco.lib.property.StyleProperties.TABLE_ROWS;
import static de.esoco.storage.StoragePredicates.sortBy;
import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newType;
import static org.obrel.type.MetaTypes.ELEMENT_DATATYPE;

/**
 * A generic fragment for the editing of an entity's attributes and children.
 *
 * @author eso
 */
public class EditEntity extends InteractionFragment {

	/**
	 * Enumeration of the available attribute edit actions.
	 */
	public enum AttributesAction {SAVE}

	/**
	 * Enumeration of the available child edit actions.
	 */
	public enum DetailAction {NEW, EDIT, DELETE}

	/**
	 * Enumeration of the available extra attribute edit actions.
	 */
	public enum ExtraAttrAction {SAVE}

	/**
	 * A default parameter for this fragment.
	 */
	public static final RelationType<List<RelationType<?>>>
		EDIT_ENTITY_FRAGMENT = newListType();

	/**
	 * The entity to be edited.
	 */
	public static final RelationType<Entity> EDITED_ENTITY = newType();

	/**
	 * A child entity to be edited.
	 */
	public static final RelationType<Entity> EDITED_ENTITY_CHILD = newType();

	/**
	 * The parent of an edited child entity.
	 */
	public static final RelationType<Entity> EDITED_ENTITY_PARENT = newType();

	private static final long serialVersionUID = 1L;

	private static final int CHILD_TABLE_ROWS = -1;

	private static final RelationType<String> EDIT_ENTITY_ERROR_MESSAGE =
		newType();

	private static final RelationType<String> EXTRA_ATTR_KEY = newType();

	private static final RelationType<String> EXTRA_ATTR_VALUE = newType();

	private static final RelationType<ExtraAttrAction> EXTRA_ATTR_ACTION =
		newType();

	private static final RelationType<RelationType<? extends Entity>>
		TAB_LIST_PARAM = newType();

	private static final RelationType<RelationType<DetailAction>>
		TAB_ACTION_PARAM = newType();

	static {
		RelationTypes.init(EditEntity.class);
	}

	private final RelationType<Entity> editedEntityParam;

	private Entity editedEntity;

	private final Collection<String> extraAttrKeys;

	private RelationType<?> currentExtraAttrKey = null;

	private RelationType<AttributesAction> attrActionParam;

	private RelationType<?> extraAttrListParam;

	private final Map<RelationType<?>, RelationType<?>> entityAttrParamsMap =
		new HashMap<RelationType<?>, RelationType<?>>();

	private final List<RelationType<?>> interactionParams =
		new ArrayList<RelationType<?>>();

	private final List<RelationType<?>> inputParams =
		new ArrayList<RelationType<?>>();

	/**
	 * Creates a new instance that edits the entity in a certain process
	 * parameter.
	 *
	 * @param entityParam The parameter containing the entity to edit
	 */
	public EditEntity(RelationType<Entity> entityParam) {
		editedEntityParam = entityParam;
		extraAttrKeys = collectExtraAttributeKeys();
	}

	/**
	 * Determines the temporary parameter name prefix for a certain entity.
	 *
	 * @param entity The entity
	 * @return The parameter name prefix
	 */
	public static String getParameterPrefix(Entity entity) {
		String entityPrefix;

		if (entity.isPersistent()) {
			entityPrefix = entity.getGlobalId();
		} else {
			entityPrefix = entity.getClass().getSimpleName();
			entityPrefix = TextConvert.capitalizedIdentifier(entityPrefix);
		}

		return entityPrefix + "_";
	}

	/**
	 * @see InteractionFragment#getInputParameters()
	 */
	@Override
	public List<RelationType<?>> getInputParameters() {
		return inputParams;
	}

	/**
	 * @see InteractionFragment#getInteractionParameters()
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters() {
		return interactionParams;
	}

	/**
	 * @see InteractionFragment#handleInteraction(RelationType)
	 */
	@Override
	public void handleInteraction(RelationType<?> interactionParam)
		throws Exception {
		setParameter(EDIT_ENTITY_ERROR_MESSAGE, "");

		try {
			handleInteractionParam(interactionParam);
		} catch (Exception e) {
			setParameter(EDIT_ENTITY_ERROR_MESSAGE, "ERR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @see InteractionFragment#init()
	 */
	@Override
	public void init() throws Exception {
		setEntity(editedEntity);
	}

	/**
	 * @see InteractionFragment#prepareInteraction()
	 */
	@Override
	public void prepareInteraction() throws Exception {
		Entity newEntity = get(editedEntityParam);

		if (newEntity == null) {
			newEntity = checkParameter(editedEntityParam);
		}

		if (newEntity != editedEntity) {
			setEntity(newEntity);
		}
	}

	/**
	 * Creates a collection of the names of the available extra attribute keys.
	 *
	 * @return The new collection
	 */
	private Collection<String> collectExtraAttributeKeys() {
		List<RelationType<?>> extraAttrs = new ArrayList<RelationType<?>>(
			RelationType.getRelationTypes(
				r -> r.hasFlag(EXTRA_ATTRIBUTE_FLAG)));

		List<String> keys =
			CollectionUtil.map(extraAttrs, RelationType::getSimpleName);

		keys.add(StandardTypes.INFO.getSimpleName());
		Collections.sort(keys);

		return keys;
	}

	/**
	 * Creates and sets the process parameters for the entity attributes.
	 *
	 * @param prefix    The prefix for the parameter names
	 * @param entityDef The entity attributes
	 * @return A new list containing the attribute process parameters
	 */
	@SuppressWarnings("unchecked")
	private List<RelationType<?>> createAttributeParameters(String prefix,
		EntityDefinition<?> entityDef) {
		Collection<RelationType<?>> attributes = entityDef.getAttributes();
		RelationType<Number> idAttr = entityDef.getIdAttribute();

		List<RelationType<?>> attrTabParams =
			new ArrayList<RelationType<?>>(attributes.size());

		attrActionParam = getTemporaryParameterType(prefix + "ATTR_ACTION",
			AttributesAction.class);

		for (RelationType<?> attr : attributes) {
			String attrName = attr.getSimpleName();
			Class<Object> targetType = (Class<Object>) attr.getTargetType();

			RelationType<Object> attrParam =
				getTemporaryParameterType(prefix + attrName, targetType);

			attrTabParams.add(attrParam);
			setParameter(attrParam, editedEntity.get(attr));
			setAttributeProperties(editedEntity, attrParam, attrName);

			if (attr == idAttr) {
				if (editedEntity.isPersistent()) {
					inputParams.remove(attrParam);
				} else {
					attrParam.set(MetaTypes.OPTIONAL);
				}
			}

			entityAttrParamsMap.put(attr, attrParam);
			setUIProperty(STYLE, "EntityAttribute", attrParam);
			setUIProperty(HTML_WIDTH, "100%", attrParam);
		}

		if (attrTabParams.size() > 0) {
			attrTabParams.add(1, attrActionParam);
			inputParams.add(attrActionParam);

			setUIFlag(SAME_ROW, attrActionParam);
			setUIFlag(HAS_IMAGES, attrActionParam);
			setUIProperty(RESOURCE_ID, "AttrAction", attrActionParam);

			setImmediateAction(attrActionParam);
		}

		return attrTabParams;
	}

	/**
	 * Creates a process parameter for a certain entity child attribute.
	 *
	 * @param prefix    The prefix for the parameter name
	 * @param parentDef The entity definition of the parent
	 * @param childAttr The child attribute
	 * @return The new process parameter type
	 */
	private RelationType<List<RelationType<?>>> createChildParameter(
		String prefix, EntityDefinition<?> parentDef,
		RelationType<List<Entity>> childAttr) {
		String childName = childAttr.getSimpleName();

		@SuppressWarnings("unchecked")
		Class<Entity> childType =
			(Class<Entity>) childAttr.get(ELEMENT_DATATYPE);

		EntityDefinition<Entity> childDef =
			EntityManager.getEntityDefinition(childType);

		@SuppressWarnings("boxing")
		QueryPredicate<Entity> children = forEntity(childType,
			ifAttribute(childDef.getParentAttribute(parentDef),
				equalTo(editedEntity.getId())));

		List<Function<? super Entity, ?>> columns =
			new ArrayList<Function<? super Entity, ?>>();

		for (RelationType<?> attr : childDef.getDisplayAttributes(
			DisplayMode.COMPACT)) {
			columns.add(attr);
		}

		RelationType<List<RelationType<?>>> childTabParam =
			createDetailTabParameter(prefix + childName, childType, children,
				null, columns, CHILD_TABLE_ROWS);

		setResourceId(childName, childTabParam);

		return childTabParam;
	}

	/**
	 * Creates a temporary parameter type for a detail tab of an entity which
	 * displays subordinate entities of the edited entity.
	 *
	 * @param baseName       The base name for the temporary parameters
	 * @param detail         The query for the detail elements
	 * @param sortOrder      The optional sort order predicate
	 * @param columns        The query columns
	 * @param tableRows      The number of table rows to display
	 * @param allowedActions The allowed detail actions
	 * @return The temporary detail parameter
	 */
	private <E extends Entity> RelationType<List<RelationType<?>>> createDetailTabParameter(
		String baseName, Class<E> detailType, QueryPredicate<E> detail,
		Predicate<? super Entity> sortOrder,
		List<Function<? super E, ?>> columns, int tableRows,
		DetailAction... allowedActions) {
		RelationType<List<RelationType<?>>> tabParam =
			getTemporarySubPanelParameter(baseName + "_TAB", false);

		RelationType<E> listParam =
			getTemporaryParameterType(baseName + "_LIST", detailType);

		RelationType<DetailAction> actionParam =
			getTemporaryParameterType(baseName + "_ACTION",
				DetailAction.class);

		List<RelationType<?>> childParams = new ArrayList<RelationType<?>>();

		listParam.set(TAB_ACTION_PARAM, actionParam);
		actionParam.set(TAB_LIST_PARAM, listParam);

		childParams.add(listParam);
		childParams.add(actionParam);

		setParameter(tabParam, childParams);
		inputParams.addAll(childParams);

		setUIFlag(HIDE_LABEL, listParam);
		setUIFlag(SAME_ROW, actionParam);
		setUIFlag(HAS_IMAGES, actionParam);

		setUIProperty(tableRows, TABLE_ROWS, listParam);
		setUIProperty(-1, CURRENT_SELECTION, listParam);
		setUIProperty(HTML_HEIGHT, "100%", listParam);

		setUIProperty(RESOURCE_ID,
			TextConvert.toPlural(detailType.getSimpleName()), listParam);
		setUIProperty(RESOURCE_ID, DetailAction.class.getSimpleName(),
			actionParam);

		setImmediateAction(actionParam, allowedActions);
		setInteractive(InteractiveInputMode.CONTINUOUS, listParam);
		disableElements(actionParam, DetailAction.EDIT, DetailAction.DELETE);

		annotateForEntityQuery(listParam, detail, sortOrder, columns);

		return tabParam;
	}

	/**
	 * Creates and adds the process parameters for the current entity.
	 */
	private void createEntityParameters() {
		EntityDefinition<?> entityDef = editedEntity.getDefinition();

		boolean persistent = editedEntity.isPersistent();
		boolean childEdit = editedEntityParam == EDITED_ENTITY_CHILD;
		String entityPrefix = getParameterPrefix(editedEntity);

		Collection<RelationType<List<Entity>>> childAttributes =
			entityDef.getChildAttributes();

		RelationType<List<RelationType<?>>> entityTabsParam =
			getTemporarySubPanelParameter(entityPrefix + "TABS", true);

		RelationType<List<RelationType<?>>> attrTabParam =
			getTemporarySubPanelParameter(entityPrefix + "ATTRIBUTES", false);

		List<RelationType<?>> tabParams =
			new ArrayList<RelationType<?>>(childAttributes.size() + 2);

		tabParams.add(attrTabParam);

		List<RelationType<?>> attrParams =
			createAttributeParameters(entityPrefix, entityDef);

		if (persistent && !childEdit) {
			for (RelationType<List<Entity>> childAttr : childAttributes) {
				RelationType<List<RelationType<?>>> childParameter =
					createChildParameter(entityPrefix, entityDef, childAttr);

				tabParams.add(childParameter);
			}
		}

		if (persistent) {
			RelationType<List<RelationType<?>>> extraAttrParam =
				createExtraAttributesParameter(entityPrefix);

			extraAttrListParam = getParameter(extraAttrParam).get(0);
			tabParams.add(extraAttrParam);
		}

		setParameter(entityTabsParam, tabParams);
		setParameter(attrTabParam, attrParams);

		interactionParams.add(entityTabsParam);
		interactionParams.add(EDIT_ENTITY_ERROR_MESSAGE);

		inputParams.add(entityTabsParam);
		inputParams.addAll(tabParams);

		setUIFlag(HIDE_LABEL, EDIT_ENTITY_ERROR_MESSAGE);

		setResourceId("EditEntityTabs", entityTabsParam);
		setResourceId("EntityAttributes", attrTabParam);

		markInputParams(true, inputParams);
	}

	/**
	 * Creates a process parameter for an entities extra attributes.
	 *
	 * @param prefix The prefix for the parameter name
	 * @return The new process parameter type
	 */
	private RelationType<List<RelationType<?>>> createExtraAttributesParameter(
		String prefix) {
		String baseName = prefix + "XA";

		List<Function<? super ExtraAttribute, ?>> columns =
			new ArrayList<Function<? super ExtraAttribute, ?>>();

		QueryPredicate<ExtraAttribute> extraAttr =
			forEntity(ExtraAttribute.class, ifAttribute(ExtraAttribute.ENTITY,
				equalTo(editedEntity.getGlobalId())));

		columns.add(ExtraAttribute.KEY);
		columns.add(ExtraAttribute.VALUE);

		RelationType<List<RelationType<?>>> extraAttrTabParam =
			createDetailTabParameter(baseName, ExtraAttribute.class, extraAttr,
				sortBy(ExtraAttribute.KEY), columns, CHILD_TABLE_ROWS,
				DetailAction.DELETE);

		setResourceId("ExtraAttributes", extraAttrTabParam);

		List<RelationType<?>> extraAttrParams =
			getParameter(extraAttrTabParam);

		RelationType<?> extraAttrListParam = extraAttrParams.get(0);

		List<RelationType<?>> extraAttrEditParams =
			Arrays.asList(EXTRA_ATTR_KEY, EXTRA_ATTR_ACTION,
				EXTRA_ATTR_VALUE);

		extraAttrParams.addAll(extraAttrEditParams);
		inputParams.addAll(extraAttrEditParams);

		setImmediateAction(EXTRA_ATTR_ACTION);

		setUIFlag(HIDE_LABEL, EXTRA_ATTR_KEY, EXTRA_ATTR_VALUE);
		setUIFlag(SAME_ROW, EXTRA_ATTR_ACTION);
		setUIFlag(HAS_IMAGES, EXTRA_ATTR_ACTION);
		setUIProperty(-1, ROWS, EXTRA_ATTR_VALUE);
		setUIProperty(2, COLUMN_SPAN, extraAttrListParam, EXTRA_ATTR_VALUE);
		setUIProperty(LIST_STYLE, ListStyle.DROP_DOWN, EXTRA_ATTR_KEY);
		setUIProperty(HTML_WIDTH, "100%", EXTRA_ATTR_KEY);
		setAllowedValues(EXTRA_ATTR_KEY, extraAttrKeys);

		setParameter(EXTRA_ATTR_KEY,
			CollectionUtil.firstElementOf(extraAttrKeys));
		setParameter(EXTRA_ATTR_VALUE, "");

		return extraAttrTabParam;
	}

	/**
	 * Creates a temporary parameter type for a sub-panel parameter that
	 * contains a list of parameter types. This will also set the UI properties
	 * to hide the parameter's label and for hierarchical evaluation of the
	 * panel's parameters.
	 *
	 * @param name              The name of the temporary parameter type
	 * @param displayAsTabPanel TRUE to display the new parameter as a tab
	 *                          panel, FALSE for a normal panel
	 * @return The new temporary parameter type
	 */
	private RelationType<List<RelationType<?>>> getTemporarySubPanelParameter(
		String name, boolean displayAsTabPanel) {
		RelationType<List<RelationType<?>>> param =
			getTemporaryListType(name, RelationType.class);

		if (displayAsTabPanel) {
			setLayout(LayoutType.TABS, param);
		}

		setUIFlag(HIERARCHICAL, param);
		setUIFlag(HIDE_LABEL, param);

		return param;
	}

	/**
	 * Performs a detail action for a certain entity list parameter.
	 *
	 * @param detailAction The detail action
	 * @param listParam    The entity list parameter
	 * @throws Exception If displaying the edit dialog fails
	 */
	private void handleDetailAction(DetailAction detailAction,
		RelationType<? extends Entity> listParam) throws Exception {
		switch (detailAction) {
			case DELETE:
				showMessageBox("$msgDeleteEntityChild", "#imWarning", null,
					DialogAction.OK, DialogAction.CANCEL);
				break;

			case EDIT:
				showEditChildDialog(getParameter(listParam));
				break;

			case NEW:

				Class<?> childType = listParam.getTargetType();

				showEditChildDialog(
					(Entity) ReflectUtil.newInstance(childType));
				break;

			default:
				assert false;
		}
	}

	/**
	 * Handles the (de-) selection in a detail tab panel.
	 *
	 * @param interactionParam The interaction parameter
	 */
	private void handleDetailSelection(RelationType<?> interactionParam) {
		Object paramValue = getParameter(interactionParam);
		Class<?> paramDatatype = interactionParam.getTargetType();

		RelationType<DetailAction> tabActionParam =
			interactionParam.get(TAB_ACTION_PARAM);

		if (paramValue != null) {
			disableElements(tabActionParam);

			if (paramDatatype == ExtraAttribute.class) {
				ExtraAttribute extraAttr = (ExtraAttribute) paramValue;

				currentExtraAttrKey = extraAttr.get(ExtraAttribute.KEY);

				Object value = extraAttr.get(ExtraAttribute.VALUE);

				setParameter(EXTRA_ATTR_KEY,
					currentExtraAttrKey.getSimpleName());
				setParameter(EXTRA_ATTR_VALUE, Conversions.asString(value));

				setUIFlag(DISABLED, EXTRA_ATTR_KEY);
			}
		} else {
			disableElements(tabActionParam, DetailAction.EDIT,
				DetailAction.DELETE);

			if (paramDatatype == ExtraAttribute.class) {
				currentExtraAttrKey = null;
				setParameter(EXTRA_ATTR_VALUE, "");

				clearUIFlag(DISABLED, EXTRA_ATTR_KEY);
			}
		}
	}

	/**
	 * Performs the actual interaction handling (without error handling).
	 *
	 * @param interactionParam The interaction parameter
	 * @throws TransactionException If storing an entity fails
	 * @throws StorageException     If accessing storage data fails
	 * @throws Exception            If displaying the edit dialog fails
	 */
	private void handleInteractionParam(RelationType<?> interactionParam)
		throws Exception {
		Class<?> paramDatatype = interactionParam.getTargetType();

		if (interactionParam == attrActionParam) {
			if (getParameter(interactionParam) == AttributesAction.SAVE) {
				updateAndStoreEditedEntity();
			}
		} else if (Entity.class.isAssignableFrom(paramDatatype)) {
			handleDetailSelection(interactionParam);
		} else if (paramDatatype == DetailAction.class) {
			handleDetailAction((DetailAction) getParameter(interactionParam),
				interactionParam.get(TAB_LIST_PARAM));
		} else if (paramDatatype == ExtraAttrAction.class) {
			updateAndStoreExtraAttribute();
			setParameter(EXTRA_ATTR_VALUE, "");
		}
	}

	/**
	 * Sets the UI properties for a certain entity attribute parameter.
	 *
	 * @param entity    The entity
	 * @param attrParam The attribute parameter
	 * @param attrName  The name of the attribute
	 */
	private void setAttributeProperties(Entity entity,
		RelationType<?> attrParam, String attrName) {
		Class<?> datatype = attrParam.getTargetType();

		setUIProperty(RESOURCE_ID, entity.getClass().getSimpleName() +
			TextConvert.capitalizedIdentifier(attrName), attrParam);

		if (Enum.class.isAssignableFrom(datatype)) {
			setUIProperty(RESOURCE_ID, datatype.getSimpleName(), attrParam);
			setUIProperty(LIST_STYLE, ListStyle.DROP_DOWN, attrParam);
		} else if (Date.class.isAssignableFrom(datatype)) {
			setUIProperty(DATE_INPUT_TYPE, DateInputType.INPUT_FIELD,
				attrParam);
		}

		if (!Entity.class.isAssignableFrom(datatype)) {
			inputParams.add(attrParam);
		}
	}

	/**
	 * Sets the entity to be displayed by this instance.
	 *
	 * @param newEntity The entity
	 */
	private void setEntity(Entity newEntity) {
		editedEntity = newEntity;

		for (RelationType<?> attrParam : entityAttrParamsMap.values()) {
			removeTemporaryParameterType(attrParam);
		}

		entityAttrParamsMap.clear();
		interactionParams.clear();
		inputParams.clear();

		if (editedEntity != null) {
			createEntityParameters();
		} else {
			interactionParams.add(StandardTypes.INFO);
			setParameter(StandardTypes.INFO, "$msgSelectEntity");

			setUIProperty(LABEL, "", StandardTypes.INFO);
		}
	}

	/**
	 * Sets an entity-specific resource ID for the given parameter.
	 *
	 * @param name  The name to generate the resource ID from
	 * @param param The parameter to set the resource ID for
	 */
	private void setResourceId(String name, RelationType<?> param) {
		name = TextConvert.capitalizedIdentifier(name);

		setUIProperty(RESOURCE_ID, name, param);
	}

	/**
	 * Adds the fragment for the editing on an entity child and configures
	 * it to
	 * be displayed in a dialog.
	 *
	 * @param child The entity to be edited
	 */
	private void showEditChildDialog(Entity child) throws Exception {
		EditEntity editChildFragment = new EditEntity(EDITED_ENTITY_CHILD);

		showDialog(getParameterPrefix(child), editChildFragment, null,
			DialogAction.CLOSE);

		editChildFragment.set(EDITED_ENTITY_CHILD, child);
		editChildFragment.set(EDITED_ENTITY_PARENT, editedEntity);
	}

	/**
	 * Updates the the edited entity from the input parameters and stores it.
	 */
	private void updateAndStoreEditedEntity() throws TransactionException {
		for (Entry<RelationType<?>, RelationType<?>> attrParam :
			entityAttrParamsMap.entrySet()) {
			@SuppressWarnings("unchecked")
			RelationType<Object> attr =
				(RelationType<Object>) attrParam.getKey();

			Object newValue = getParameter(attrParam.getValue());
			Object oldValue = editedEntity.get(attr);

			// do not replace NULL values with empty strings
			if (oldValue != null ||
				(newValue != null && newValue.toString().length() > 0)) {
				editedEntity.set(attr, newValue);
			}
		}

		if (!editedEntity.isPersistent()) {
			Entity parent = get(EDITED_ENTITY_PARENT);

			if (parent == null) {
				parent = getParameter(EDITED_ENTITY_PARENT);
			}

			if (parent != null) {
				for (RelationType<List<Entity>> childAttr : parent
					.getDefinition()
					.getChildAttributes()) {
					if (editedEntity.getClass() ==
						childAttr.get(ELEMENT_DATATYPE)) {
						parent.addChild(childAttr, editedEntity);

						break;
					}
				}
			}
		}

		EntityManager.storeEntity(editedEntity, getProcessUser());
	}

	/**
	 * Updates an extra attribute from the input values, sets it in the edited
	 * entity, and stores the entity.
	 *
	 * @throws StorageException     If setting the extra attribute fails
	 * @throws TransactionException If storing the entity fails
	 */
	@SuppressWarnings("unchecked")
	private void updateAndStoreExtraAttribute()
		throws StorageException, TransactionException {
		String rawValue = getParameter(EXTRA_ATTR_VALUE);
		RelationType<?> key = currentExtraAttrKey;

		if (key == null) {
			key = RelationType.valueOf(
				ExtraAttributes.EXTRA_ATTRIBUTES_NAMESPACE + "." +
					getParameter(EXTRA_ATTR_KEY));
		}

		Object value = Conversions.parseValue(rawValue, key);

		editedEntity.setExtraAttribute((RelationType<Object>) key, value);
		EntityManager.storeEntity(editedEntity, getProcessUser());
		markParameterAsModified(extraAttrListParam);
	}
}
