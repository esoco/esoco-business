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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

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


/********************************************************************
 * A generic fragment for the editing of an entity's attributes and children.
 *
 * @author eso
 */
public class EditEntity extends InteractionFragment
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the available attribute edit actions.
	 */
	public enum AttributesAction { SAVE }

	/********************************************************************
	 * Enumeration of the available child edit actions.
	 */
	public enum DetailAction { NEW, EDIT, DELETE }

	/********************************************************************
	 * Enumeration of the available extra attribute edit actions.
	 */
	public enum ExtraAttrAction { SAVE }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	private static final int CHILD_TABLE_ROWS = -1;

	/** A default parameter for this fragment. */
	public static final RelationType<List<RelationType<?>>> EDIT_ENTITY_FRAGMENT =
		newListType();

	/** The entity to be edited. */
	public static final RelationType<Entity> EDITED_ENTITY = newType();

	/** A child entity to be edited. */
	public static final RelationType<Entity> EDITED_ENTITY_CHILD = newType();

	/** The parent of an edited child entity. */
	public static final RelationType<Entity> EDITED_ENTITY_PARENT = newType();

	private static final RelationType<String> EDIT_ENTITY_ERROR_MESSAGE =
		newType();

	private static final RelationType<String> EXTRA_ATTR_KEY = newType();

	private static final RelationType<String> EXTRA_ATTR_VALUE = newType();

	private static final RelationType<ExtraAttrAction> EXTRA_ATTR_ACTION =
		newType();

	private static final RelationType<RelationType<? extends Entity>> TAB_LIST_PARAM =
		newType();

	private static final RelationType<RelationType<DetailAction>> TAB_ACTION_PARAM =
		newType();

	static
	{
		RelationTypes.init(EditEntity.class);
	}

	//~ Instance fields --------------------------------------------------------

	private RelationType<Entity> rEditedEntityParam;

	private Entity rEditedEntity;

	private Collection<String> aExtraAttrKeys;

	private RelationType<?>				   rCurrentExtraAttrKey = null;
	private RelationType<AttributesAction> aAttrActionParam;
	private RelationType<?>				   rExtraAttrListParam;

	private Map<RelationType<?>, RelationType<?>> aEntityAttrParamsMap =
		new HashMap<RelationType<?>, RelationType<?>>();

	private List<RelationType<?>> aInteractionParams =
		new ArrayList<RelationType<?>>();

	private List<RelationType<?>> aInputParams =
		new ArrayList<RelationType<?>>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that edits the entity in a certain process
	 * parameter.
	 *
	 * @param rEntityParam The parameter containing the entity to edit
	 */
	public EditEntity(RelationType<Entity> rEntityParam)
	{
		rEditedEntityParam = rEntityParam;
		aExtraAttrKeys     = collectExtraAttributeKeys();
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Determines the temporary parameter name prefix for a certain entity.
	 *
	 * @param  rEntity The entity
	 *
	 * @return The parameter name prefix
	 */
	public static String getParameterPrefix(Entity rEntity)
	{
		String sEntityPrefix;

		if (rEntity.isPersistent())
		{
			sEntityPrefix = rEntity.getGlobalId();
		}
		else
		{
			sEntityPrefix = rEntity.getClass().getSimpleName();
			sEntityPrefix = TextConvert.capitalizedIdentifier(sEntityPrefix);
		}

		return sEntityPrefix + "_";
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see InteractionFragment#getInputParameters()
	 */
	@Override
	public List<RelationType<?>> getInputParameters()
	{
		return aInputParams;
	}

	/***************************************
	 * @see InteractionFragment#getInteractionParameters()
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters()
	{
		return aInteractionParams;
	}

	/***************************************
	 * @see InteractionFragment#handleInteraction(RelationType)
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		setParameter(EDIT_ENTITY_ERROR_MESSAGE, "");

		try
		{
			handleInteractionParam(rInteractionParam);
		}
		catch (Exception e)
		{
			setParameter(EDIT_ENTITY_ERROR_MESSAGE, "ERR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/***************************************
	 * @see InteractionFragment#init()
	 */
	@Override
	public void init() throws Exception
	{
		setEntity(rEditedEntity);
	}

	/***************************************
	 * @see InteractionFragment#prepareInteraction()
	 */
	@Override
	public void prepareInteraction() throws Exception
	{
		Entity rNewEntity = get(rEditedEntityParam);

		if (rNewEntity == null)
		{
			rNewEntity = checkParameter(rEditedEntityParam);
		}

		if (rNewEntity != rEditedEntity)
		{
			setEntity(rNewEntity);
		}
	}

	/***************************************
	 * Creates a collection of the names of the available extra attribute keys.
	 *
	 * @return The new collection
	 */
	private Collection<String> collectExtraAttributeKeys()
	{
		List<RelationType<?>> aExtraAttrs =
			new ArrayList<RelationType<?>>(
				RelationType.getRelationTypes(
					r -> r.hasFlag(EXTRA_ATTRIBUTE_FLAG)));

		List<String> aKeys =
			CollectionUtil.map(aExtraAttrs, RelationType::getSimpleName);

		aKeys.add(StandardTypes.INFO.getSimpleName());
		Collections.sort(aKeys);

		return aKeys;
	}

	/***************************************
	 * Creates and sets the process parameters for the entity attributes.
	 *
	 * @param  sPrefix    The prefix for the parameter names
	 * @param  rEntityDef The entity attributes
	 *
	 * @return A new list containing the attribute process parameters
	 */
	@SuppressWarnings("unchecked")
	private List<RelationType<?>> createAttributeParameters(
		String				sPrefix,
		EntityDefinition<?> rEntityDef)
	{
		Collection<RelationType<?>> rAttributes = rEntityDef.getAttributes();
		RelationType<Number>	    rIdAttr     = rEntityDef.getIdAttribute();

		List<RelationType<?>> aAttrTabParams =
			new ArrayList<RelationType<?>>(rAttributes.size());

		aAttrActionParam =
			getTemporaryParameterType(
				sPrefix + "ATTR_ACTION",
				AttributesAction.class);

		for (RelationType<?> rAttr : rAttributes)
		{
			String		  sAttrName   = rAttr.getSimpleName();
			Class<Object> rTargetType = (Class<Object>) rAttr.getTargetType();

			RelationType<Object> aAttrParam =
				getTemporaryParameterType(sPrefix + sAttrName, rTargetType);

			aAttrTabParams.add(aAttrParam);
			setParameter(aAttrParam, rEditedEntity.get(rAttr));
			setAttributeProperties(rEditedEntity, aAttrParam, sAttrName);

			if (rAttr == rIdAttr)
			{
				if (rEditedEntity.isPersistent())
				{
					aInputParams.remove(aAttrParam);
				}
				else
				{
					aAttrParam.set(MetaTypes.OPTIONAL);
				}
			}

			aEntityAttrParamsMap.put(rAttr, aAttrParam);
			setUIProperty(STYLE, "EntityAttribute", aAttrParam);
			setUIProperty(HTML_WIDTH, "100%", aAttrParam);
		}

		if (aAttrTabParams.size() > 0)
		{
			aAttrTabParams.add(1, aAttrActionParam);
			aInputParams.add(aAttrActionParam);

			setUIFlag(SAME_ROW, aAttrActionParam);
			setUIFlag(HAS_IMAGES, aAttrActionParam);
			setUIProperty(RESOURCE_ID, "AttrAction", aAttrActionParam);

			setImmediateAction(aAttrActionParam);
		}

		return aAttrTabParams;
	}

	/***************************************
	 * Creates a process parameter for a certain entity child attribute.
	 *
	 * @param  sPrefix    The prefix for the parameter name
	 * @param  rParentDef The entity definition of the parent
	 * @param  rChildAttr The child attribute
	 *
	 * @return The new process parameter type
	 */
	private RelationType<List<RelationType<?>>> createChildParameter(
		String					   sPrefix,
		EntityDefinition<?>		   rParentDef,
		RelationType<List<Entity>> rChildAttr)
	{
		String sChildName = rChildAttr.getSimpleName();

		@SuppressWarnings("unchecked")
		Class<Entity> rChildType =
			(Class<Entity>) rChildAttr.get(ELEMENT_DATATYPE);

		EntityDefinition<Entity> rChildDef =
			EntityManager.getEntityDefinition(rChildType);

		@SuppressWarnings("boxing")
		QueryPredicate<Entity> qChildren =
			forEntity(
				rChildType,
				ifAttribute(
					rChildDef.getParentAttribute(rParentDef),
					equalTo(rEditedEntity.getId())));

		List<Function<? super Entity, ?>> aColumns =
			new ArrayList<Function<? super Entity, ?>>();

		for (RelationType<?> rAttr :
			 rChildDef.getDisplayAttributes(DisplayMode.COMPACT))
		{
			aColumns.add(rAttr);
		}

		RelationType<List<RelationType<?>>> aChildTabParam =
			createDetailTabParameter(
				sPrefix + sChildName,
				rChildType,
				qChildren,
				null,
				aColumns,
				CHILD_TABLE_ROWS);

		setResourceId(sChildName, aChildTabParam);

		return aChildTabParam;
	}

	/***************************************
	 * Creates a temporary parameter type for a detail tab of an entity which
	 * displays subordinate entities of the edited entity.
	 *
	 * @param  sBaseName       The base name for the temporary parameters
	 * @param  rDetailType
	 * @param  qDetail         The query for the detail elements
	 * @param  pSortOrder      The optional sort order predicate
	 * @param  aColumns        The query columns
	 * @param  nTableRows      The number of table rows to display
	 * @param  rAllowedActions The allowed detail actions
	 *
	 * @return The temporary detail parameter
	 */
	private <E extends Entity> RelationType<List<RelationType<?>>>
	createDetailTabParameter(String						  sBaseName,
							 Class<E>					  rDetailType,
							 QueryPredicate<E>			  qDetail,
							 Predicate<? super Entity>    pSortOrder,
							 List<Function<? super E, ?>> aColumns,
							 int						  nTableRows,
							 DetailAction... 			  rAllowedActions)
	{
		RelationType<List<RelationType<?>>> aTabParam =
			getTemporarySubPanelParameter(sBaseName + "_TAB", false);

		RelationType<E> aListParam =
			getTemporaryParameterType(sBaseName + "_LIST", rDetailType);

		RelationType<DetailAction> aActionParam =
			getTemporaryParameterType(
				sBaseName + "_ACTION",
				DetailAction.class);

		List<RelationType<?>> aChildParams = new ArrayList<RelationType<?>>();

		aListParam.set(TAB_ACTION_PARAM, aActionParam);
		aActionParam.set(TAB_LIST_PARAM, aListParam);

		aChildParams.add(aListParam);
		aChildParams.add(aActionParam);

		setParameter(aTabParam, aChildParams);
		aInputParams.addAll(aChildParams);

		setUIFlag(HIDE_LABEL, aListParam);
		setUIFlag(SAME_ROW, aActionParam);
		setUIFlag(HAS_IMAGES, aActionParam);

		setUIProperty(nTableRows, TABLE_ROWS, aListParam);
		setUIProperty(-1, CURRENT_SELECTION, aListParam);
		setUIProperty(HTML_HEIGHT, "100%", aListParam);

		setUIProperty(
			RESOURCE_ID,
			TextConvert.toPlural(rDetailType.getSimpleName()),
			aListParam);
		setUIProperty(
			RESOURCE_ID,
			DetailAction.class.getSimpleName(),
			aActionParam);

		setImmediateAction(aActionParam, rAllowedActions);
		setInteractive(InteractiveInputMode.CONTINUOUS, aListParam);
		disableElements(aActionParam, DetailAction.EDIT, DetailAction.DELETE);

		annotateForEntityQuery(aListParam, qDetail, pSortOrder, aColumns);

		return aTabParam;
	}

	/***************************************
	 * Creates and adds the process parameters for the current entity.
	 */
	private void createEntityParameters()
	{
		EntityDefinition<?> rEntityDef = rEditedEntity.getDefinition();

		boolean bPersistent   = rEditedEntity.isPersistent();
		boolean bChildEdit    = rEditedEntityParam == EDITED_ENTITY_CHILD;
		String  sEntityPrefix = getParameterPrefix(rEditedEntity);

		Collection<RelationType<List<Entity>>> rChildAttributes =
			rEntityDef.getChildAttributes();

		RelationType<List<RelationType<?>>> aEntityTabsParam =
			getTemporarySubPanelParameter(sEntityPrefix + "TABS", true);

		RelationType<List<RelationType<?>>> aAttrTabParam =
			getTemporarySubPanelParameter(sEntityPrefix + "ATTRIBUTES", false);

		List<RelationType<?>> aTabParams =
			new ArrayList<RelationType<?>>(rChildAttributes.size() + 2);

		aTabParams.add(aAttrTabParam);

		List<RelationType<?>> aAttrParams =
			createAttributeParameters(sEntityPrefix, rEntityDef);

		if (bPersistent && !bChildEdit)
		{
			for (RelationType<List<Entity>> rChildAttr : rChildAttributes)
			{
				RelationType<List<RelationType<?>>> aChildParameter =
					createChildParameter(sEntityPrefix, rEntityDef, rChildAttr);

				aTabParams.add(aChildParameter);
			}
		}

		if (bPersistent)
		{
			RelationType<List<RelationType<?>>> rExtraAttrParam =
				createExtraAttributesParameter(sEntityPrefix);

			rExtraAttrListParam = getParameter(rExtraAttrParam).get(0);
			aTabParams.add(rExtraAttrParam);
		}

		setParameter(aEntityTabsParam, aTabParams);
		setParameter(aAttrTabParam, aAttrParams);

		aInteractionParams.add(aEntityTabsParam);
		aInteractionParams.add(EDIT_ENTITY_ERROR_MESSAGE);

		aInputParams.add(aEntityTabsParam);
		aInputParams.addAll(aTabParams);

		setUIFlag(HIDE_LABEL, EDIT_ENTITY_ERROR_MESSAGE);

		setResourceId("EditEntityTabs", aEntityTabsParam);
		setResourceId("EntityAttributes", aAttrTabParam);

		markInputParams(true, aInputParams);
	}

	/***************************************
	 * Creates a process parameter for an entities extra attributes.
	 *
	 * @param  sPrefix The prefix for the parameter name
	 *
	 * @return The new process parameter type
	 */
	private RelationType<List<RelationType<?>>> createExtraAttributesParameter(
		String sPrefix)
	{
		String sBaseName = sPrefix + "XA";

		List<Function<? super ExtraAttribute, ?>> aColumns =
			new ArrayList<Function<? super ExtraAttribute, ?>>();

		QueryPredicate<ExtraAttribute> qExtraAttr =
			forEntity(
				ExtraAttribute.class,
				ifAttribute(
					ExtraAttribute.ENTITY,
					equalTo(rEditedEntity.getGlobalId())));

		aColumns.add(ExtraAttribute.KEY);
		aColumns.add(ExtraAttribute.VALUE);

		RelationType<List<RelationType<?>>> aExtraAttrTabParam =
			createDetailTabParameter(
				sBaseName,
				ExtraAttribute.class,
				qExtraAttr,
				sortBy(ExtraAttribute.KEY),
				aColumns,
				CHILD_TABLE_ROWS,
				DetailAction.DELETE);

		setResourceId("ExtraAttributes", aExtraAttrTabParam);

		List<RelationType<?>> rExtraAttrParams =
			getParameter(aExtraAttrTabParam);

		RelationType<?> rExtraAttrListParam = rExtraAttrParams.get(0);

		List<RelationType<?>> rExtraAttrEditParams =
			Arrays.<RelationType<?>>asList(
				EXTRA_ATTR_KEY,
				EXTRA_ATTR_ACTION,
				EXTRA_ATTR_VALUE);

		rExtraAttrParams.addAll(rExtraAttrEditParams);
		aInputParams.addAll(rExtraAttrEditParams);

		setImmediateAction(EXTRA_ATTR_ACTION);

		setUIFlag(HIDE_LABEL, EXTRA_ATTR_KEY, EXTRA_ATTR_VALUE);
		setUIFlag(SAME_ROW, EXTRA_ATTR_ACTION);
		setUIFlag(HAS_IMAGES, EXTRA_ATTR_ACTION);
		setUIProperty(-1, ROWS, EXTRA_ATTR_VALUE);
		setUIProperty(2, COLUMN_SPAN, rExtraAttrListParam, EXTRA_ATTR_VALUE);
		setUIProperty(LIST_STYLE, ListStyle.DROP_DOWN, EXTRA_ATTR_KEY);
		setUIProperty(HTML_WIDTH, "100%", EXTRA_ATTR_KEY);
		setAllowedValues(EXTRA_ATTR_KEY, aExtraAttrKeys);

		setParameter(
			EXTRA_ATTR_KEY,
			CollectionUtil.firstElementOf(aExtraAttrKeys));
		setParameter(EXTRA_ATTR_VALUE, "");

		return aExtraAttrTabParam;
	}

	/***************************************
	 * Creates a temporary parameter type for a sub-panel parameter that
	 * contains a list of parameter types. This will also set the UI properties
	 * to hide the parameter's label and for hierarchical evaluation of the
	 * panel's parameters.
	 *
	 * @param  sName              The name of the temporary parameter type
	 * @param  bDisplayAsTabPanel TRUE to display the new parameter as a tab
	 *                            panel, FALSE for a normal panel
	 *
	 * @return The new temporary parameter type
	 */
	private RelationType<List<RelationType<?>>> getTemporarySubPanelParameter(
		String  sName,
		boolean bDisplayAsTabPanel)
	{
		RelationType<List<RelationType<?>>> aParam =
			getTemporaryListType(sName, RelationType.class);

		if (bDisplayAsTabPanel)
		{
			setLayout(LayoutType.TABS, aParam);
		}

		setUIFlag(HIERARCHICAL, aParam);
		setUIFlag(HIDE_LABEL, aParam);

		return aParam;
	}

	/***************************************
	 * Performs a detail action for a certain entity list parameter.
	 *
	 * @param  eDetailAction The detail action
	 * @param  rListParam    The entity list parameter
	 *
	 * @throws Exception If displaying the edit dialog fails
	 */
	private void handleDetailAction(
		DetailAction				   eDetailAction,
		RelationType<? extends Entity> rListParam) throws Exception
	{
		switch (eDetailAction)
		{
			case DELETE:
				showMessageBox(
					"$msgDeleteEntityChild",
					"#imWarning",
					null,
					DialogAction.OK,
					DialogAction.CANCEL);
				break;

			case EDIT:
				showEditChildDialog(getParameter(rListParam));
				break;

			case NEW:

				Class<?> rChildType = rListParam.getTargetType();

				showEditChildDialog(
					(Entity) ReflectUtil.newInstance(rChildType));
				break;

			default:
				assert false;
		}
	}

	/***************************************
	 * Handles the (de-) selection in a detail tab panel.
	 *
	 * @param rInteractionParam The interaction parameter
	 */
	private void handleDetailSelection(RelationType<?> rInteractionParam)
	{
		Object   rParamValue    = getParameter(rInteractionParam);
		Class<?> rParamDatatype = rInteractionParam.getTargetType();

		RelationType<DetailAction> rTabActionParam =
			rInteractionParam.get(TAB_ACTION_PARAM);

		if (rParamValue != null)
		{
			disableElements(rTabActionParam);

			if (rParamDatatype == ExtraAttribute.class)
			{
				ExtraAttribute rExtraAttr = (ExtraAttribute) rParamValue;

				rCurrentExtraAttrKey = rExtraAttr.get(ExtraAttribute.KEY);

				Object rValue = rExtraAttr.get(ExtraAttribute.VALUE);

				setParameter(
					EXTRA_ATTR_KEY,
					rCurrentExtraAttrKey.getSimpleName());
				setParameter(EXTRA_ATTR_VALUE, Conversions.asString(rValue));

				setUIFlag(DISABLED, EXTRA_ATTR_KEY);
			}
		}
		else
		{
			disableElements(
				rTabActionParam,
				DetailAction.EDIT,
				DetailAction.DELETE);

			if (rParamDatatype == ExtraAttribute.class)
			{
				rCurrentExtraAttrKey = null;
				setParameter(EXTRA_ATTR_VALUE, "");

				clearUIFlag(DISABLED, EXTRA_ATTR_KEY);
			}
		}
	}

	/***************************************
	 * Performs the actual interaction handling (without error handling).
	 *
	 * @param  rInteractionParam The interaction parameter
	 *
	 * @throws TransactionException If storing an entity fails
	 * @throws StorageException     If accessing storage data fails
	 * @throws Exception            If displaying the edit dialog fails
	 */
	private void handleInteractionParam(RelationType<?> rInteractionParam)
		throws Exception
	{
		Class<?> rParamDatatype = rInteractionParam.getTargetType();

		if (rInteractionParam == aAttrActionParam)
		{
			if (getParameter(rInteractionParam) == AttributesAction.SAVE)
			{
				updateAndStoreEditedEntity();
			}
		}
		else if (Entity.class.isAssignableFrom(rParamDatatype))
		{
			handleDetailSelection(rInteractionParam);
		}
		else if (rParamDatatype == DetailAction.class)
		{
			handleDetailAction(
				(DetailAction) getParameter(rInteractionParam),
				rInteractionParam.get(TAB_LIST_PARAM));
		}
		else if (rParamDatatype == ExtraAttrAction.class)
		{
			updateAndStoreExtraAttribute();
			setParameter(EXTRA_ATTR_VALUE, "");
		}
	}

	/***************************************
	 * Sets the UI properties for a certain entity attribute parameter.
	 *
	 * @param rEntity    The entity
	 * @param rAttrParam The attribute parameter
	 * @param sAttrName  The name of the attribute
	 */
	private void setAttributeProperties(Entity			rEntity,
										RelationType<?> rAttrParam,
										String			sAttrName)
	{
		Class<?> rDatatype = rAttrParam.getTargetType();

		setUIProperty(
			RESOURCE_ID,
			rEntity.getClass().getSimpleName() +
			TextConvert.capitalizedIdentifier(sAttrName),
			rAttrParam);

		if (Enum.class.isAssignableFrom(rDatatype))
		{
			setUIProperty(RESOURCE_ID, rDatatype.getSimpleName(), rAttrParam);
			setUIProperty(LIST_STYLE, ListStyle.DROP_DOWN, rAttrParam);
		}
		else if (Date.class.isAssignableFrom(rDatatype))
		{
			setUIProperty(
				DATE_INPUT_TYPE,
				DateInputType.INPUT_FIELD,
				rAttrParam);
		}

		if (!Entity.class.isAssignableFrom(rDatatype))
		{
			aInputParams.add(rAttrParam);
		}
	}

	/***************************************
	 * Sets the entity to be displayed by this instance.
	 *
	 * @param rNewEntity The entity
	 */
	private void setEntity(Entity rNewEntity)
	{
		rEditedEntity = rNewEntity;

		for (RelationType<?> rAttrParam : aEntityAttrParamsMap.values())
		{
			removeTemporaryParameterType(rAttrParam);
		}

		aEntityAttrParamsMap.clear();
		aInteractionParams.clear();
		aInputParams.clear();

		if (rEditedEntity != null)
		{
			createEntityParameters();
		}
		else
		{
			aInteractionParams.add(StandardTypes.INFO);
			setParameter(StandardTypes.INFO, "$msgSelectEntity");

			setUIProperty(LABEL, "", StandardTypes.INFO);
		}
	}

	/***************************************
	 * Sets an entity-specific resource ID for the given parameter.
	 *
	 * @param sName  The name to generate the resource ID from
	 * @param rParam The parameter to set the resource ID for
	 */
	private void setResourceId(String sName, RelationType<?> rParam)
	{
		sName = TextConvert.capitalizedIdentifier(sName);

		setUIProperty(RESOURCE_ID, sName, rParam);
	}

	/***************************************
	 * Adds the fragment for the editing on an entity child and configures it to
	 * be displayed in a dialog.
	 *
	 * @param  rChild The entity to be edited
	 *
	 * @throws Exception
	 */
	private void showEditChildDialog(Entity rChild) throws Exception
	{
		EditEntity aEditChildFragment = new EditEntity(EDITED_ENTITY_CHILD);

		showDialog(
			getParameterPrefix(rChild),
			aEditChildFragment,
			null,
			DialogAction.CLOSE);

		aEditChildFragment.set(EDITED_ENTITY_CHILD, rChild);
		aEditChildFragment.set(EDITED_ENTITY_PARENT, rEditedEntity);
	}

	/***************************************
	 * Updates the the edited entity from the input parameters and stores it.
	 *
	 * @throws TransactionException
	 */
	private void updateAndStoreEditedEntity() throws TransactionException
	{
		for (Entry<RelationType<?>, RelationType<?>> rAttrParam :
			 aEntityAttrParamsMap.entrySet())
		{
			@SuppressWarnings("unchecked")
			RelationType<Object> rAttr =
				(RelationType<Object>) rAttrParam.getKey();

			Object rNewValue = getParameter(rAttrParam.getValue());
			Object rOldValue = rEditedEntity.get(rAttr);

			// do not replace NULL values with empty strings
			if (rOldValue != null ||
				(rNewValue != null && rNewValue.toString().length() > 0))
			{
				rEditedEntity.set(rAttr, rNewValue);
			}
		}

		if (!rEditedEntity.isPersistent())
		{
			Entity rParent = get(EDITED_ENTITY_PARENT);

			if (rParent == null)
			{
				rParent = getParameter(EDITED_ENTITY_PARENT);
			}

			if (rParent != null)
			{
				for (RelationType<List<Entity>> rChildAttr :
					 rParent.getDefinition().getChildAttributes())
				{
					if (rEditedEntity.getClass() ==
						rChildAttr.get(ELEMENT_DATATYPE))
					{
						rParent.addChild(rChildAttr, rEditedEntity);

						break;
					}
				}
			}
		}

		EntityManager.storeEntity(rEditedEntity, getProcessUser());
	}

	/***************************************
	 * Updates an extra attribute from the input values, sets it in the edited
	 * entity, and stores the entity.
	 *
	 * @throws StorageException     If setting the extra attribute fails
	 * @throws TransactionException If storing the entity fails
	 */
	@SuppressWarnings("unchecked")
	private void updateAndStoreExtraAttribute() throws StorageException,
													   TransactionException
	{
		String		    sRawValue = getParameter(EXTRA_ATTR_VALUE);
		RelationType<?> rKey	  = rCurrentExtraAttrKey;

		if (rKey == null)
		{
			rKey =
				RelationType.valueOf(
					ExtraAttributes.EXTRA_ATTRIBUTES_NAMESPACE +
					"." + getParameter(EXTRA_ATTR_KEY));
		}

		Object rValue = Conversions.parseValue(sRawValue, rKey);

		rEditedEntity.setExtraAttribute((RelationType<Object>) rKey, rValue);
		EntityManager.storeEntity(rEditedEntity, getProcessUser());
		markParameterAsModified(rExtraAttrListParam);
	}
}
