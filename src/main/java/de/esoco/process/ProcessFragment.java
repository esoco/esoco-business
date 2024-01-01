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
package de.esoco.process;

import de.esoco.data.DataRelationTypes;
import de.esoco.data.DownloadData;
import de.esoco.data.FileType;
import de.esoco.data.SessionManager;
import de.esoco.data.element.DataElement;
import de.esoco.data.element.DataElementList;
import de.esoco.data.element.DataSetDataElement;
import de.esoco.data.element.DataSetDataElement.ChartType;
import de.esoco.data.element.DataSetDataElement.LegendPosition;
import de.esoco.data.element.SelectionDataElement;
import de.esoco.data.process.ProcessDescription;
import de.esoco.data.process.ProcessState;
import de.esoco.data.storage.StorageAdapter;
import de.esoco.data.storage.StorageAdapterId;
import de.esoco.data.storage.StorageAdapterRegistry;
import de.esoco.entity.Configuration;
import de.esoco.entity.Entity;
import de.esoco.entity.EntityManager;
import de.esoco.history.HistoryRecord;
import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.function.CalendarFunctions;
import de.esoco.lib.logging.Log;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.model.DataSet;
import de.esoco.lib.model.IntDataSet;
import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.HasProperties;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.MutableProperties;
import de.esoco.lib.property.Orientation;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.StateProperties;
import de.esoco.lib.property.StringProperties;
import de.esoco.lib.property.StyleProperties;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextUtil;
import de.esoco.process.step.Interaction;
import de.esoco.process.step.InteractionFragment;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StorageException;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.type.MetaTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static de.esoco.data.DataRelationTypes.STORAGE_ADAPTER_ID;
import static de.esoco.data.DataRelationTypes.STORAGE_ADAPTER_REGISTRY;
import static de.esoco.entity.EntityRelationTypes.DISPLAY_PROPERTIES;
import static de.esoco.entity.ExtraAttributes.EXTRA_ATTRIBUTE_FLAG;
import static de.esoco.lib.expression.Functions.doIf;
import static de.esoco.lib.expression.Functions.doIfElse;
import static de.esoco.lib.expression.Functions.value;
import static de.esoco.lib.expression.Predicates.isNull;
import static de.esoco.lib.expression.Predicates.lessThan;
import static de.esoco.lib.property.ContentProperties.CONTENT_TYPE;
import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.ContentProperties.TOOLTIP;
import static de.esoco.lib.property.LayoutProperties.COLUMNS;
import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.LAYOUT;
import static de.esoco.lib.property.LayoutProperties.ROWS;
import static de.esoco.lib.property.StateProperties.DISABLED;
import static de.esoco.lib.property.StateProperties.HIDDEN;
import static de.esoco.lib.property.StateProperties.INTERACTION_EVENT_TYPES;
import static de.esoco.lib.property.StateProperties.INTERACTIVE_INPUT_MODE;
import static de.esoco.lib.property.StateProperties.SELECTION_DEPENDENCY;
import static de.esoco.lib.property.StateProperties.SELECTION_DEPENDENCY_REVERSE_PREFIX;
import static de.esoco.lib.property.StateProperties.STRUCTURE_CHANGED;
import static de.esoco.lib.property.StyleProperties.DISABLED_ELEMENTS;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.LIST_STYLE;
import static de.esoco.lib.property.StyleProperties.WRAP;
import static de.esoco.process.ProcessRelationTypes.ALLOWED_VALUES;
import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_EVENT_PARAM;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_FILL;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.ORIGINAL_RELATION_TYPE;
import static de.esoco.process.ProcessRelationTypes.PROCESS_EXECUTOR;
import static de.esoco.process.ProcessRelationTypes.PROCESS_STEP_INFO;
import static de.esoco.process.ProcessRelationTypes.PROCESS_STEP_MESSAGE;
import static de.esoco.process.ProcessRelationTypes.PROGRESS;
import static de.esoco.process.ProcessRelationTypes.PROGRESS_INDICATOR;
import static de.esoco.process.ProcessRelationTypes.PROGRESS_INDICATOR_TEMPLATE;
import static de.esoco.process.ProcessRelationTypes.PROGRESS_MAXIMUM;
import static de.esoco.process.ProcessRelationTypes.SPAWN_PROCESSES;
import static de.esoco.process.ProcessRelationTypes.TEMPORARY_PARAM_TYPES;
import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newRelationType;
import static org.obrel.core.RelationTypes.newSetType;
import static org.obrel.type.MetaTypes.ELEMENT_DATATYPE;
import static org.obrel.type.StandardTypes.MAXIMUM;
import static org.obrel.type.StandardTypes.MINIMUM;

/**
 * The base class for all fragments of a process at runtime.
 *
 * @author eso
 */
public abstract class ProcessFragment extends ProcessElement {

	private static final long serialVersionUID = 1L;

	private static final Set<PropertyName<?>> NON_MODIFYING_PROPERTIES =
		CollectionUtil.setOf(DISABLED, HIDDEN);

	private final Map<RelationType<List<RelationType<?>>>, InteractionFragment>
		subFragments = new LinkedHashMap<>();

	private final Map<String, Consumer<ProcessFragment>> cleanupActions =
		new LinkedHashMap<>();

	private int fragmentId = -1;

	private String fragmentParamPackage = null;

	private Collection<RelationType<?>> panelParameters;

	/**
	 * Modifies a date by a certain calendar field and returns a new date with
	 * the update value.
	 *
	 * @param date          The date to modify (will not be changed)
	 * @param calendarField The calendar field to modify
	 * @param addValue      The update value for the calendar field
	 * @return A new date instance containing the updated value
	 */
	public static Date changeDate(Date date, int calendarField, int addValue) {
		Calendar calendar = Calendar.getInstance();

		calendar.setTime(date);
		calendar.add(calendarField, addValue);

		return calendar.getTime();
	}

	/**
	 * Return the interactive input mode for a certain list style.
	 *
	 * @param listStyle The list style
	 * @return The interactive input mode
	 */
	public static InteractiveInputMode getInputMode(ListStyle listStyle) {
		InteractiveInputMode inputMode = null;

		switch (listStyle) {
			case LIST:
			case DROP_DOWN:
			case EDITABLE:
				inputMode = InteractiveInputMode.CONTINUOUS;
				break;

			case DISCRETE:
			case IMMEDIATE:
				inputMode = InteractiveInputMode.ACTION;
				break;
		}

		return inputMode;
	}

	/**
	 * Registers an cleanup action that will be executed when this process
	 * fragment is finished, i.e. the fragment is removed, the process
	 * continues
	 * to the next step, or terminates (regularly or with an error). If a
	 * different finish action is already registered under a particular key it
	 * will be replaced. Therefore invoking code must make sure to use unique
	 * keys or handle the replacement of actions in appropriate ways.
	 *
	 * <p>When invoked a cleanup action receives the process fragment as it's
	 * argument to provide access to process parameters. Registered Actions can
	 * be removed with {@link #removeCleanupAction(String)}.</p>
	 *
	 * @param key    A key that identifies the action for later removal
	 * @param action The function to invoke on cleanup
	 */
	public void addCleanupAction(String key,
		Consumer<ProcessFragment> action) {
		cleanupActions.put(key, action);
	}

	/**
	 * Adds an invisible fill parameter to a layout.
	 *
	 * @param fillWidth  TRUE to fill the remaining layout width
	 * @param fillHeight TRUE to fill the remaining layout height
	 * @return The fill parameter that has been added by this method
	 */
	public RelationType<?> addLayoutFiller(boolean fillWidth,
		boolean fillHeight) {
		RelationType<String> fillParam = INTERACTION_FILL;

		if (!hasInteractionParameter(fillParam)) {
			addDisplayParameters(fillParam);
		}

		setUIFlag(HIDE_LABEL, fillParam);
		setUIProperty(TOOLTIP, "", fillParam);

		if (fillWidth) {
			setUIProperty(HTML_WIDTH, "100%", fillParam);
		}

		if (fillHeight) {
			setUIProperty(HTML_HEIGHT, "100%", fillParam);
		}

		return fillParam;
	}

	/**
	 * Configures a parameter to be displayed in a separate panel.
	 *
	 * @param panelParam         The data element list parameter to be
	 *                              displayed
	 *                           as a panel
	 * @param layout             The layout for the panel
	 * @param panelContentParams The list of parameters to be displayed in the
	 *                           panel
	 */
	public void addPanel(RelationType<List<RelationType<?>>> panelParam,
		LayoutType layout, List<RelationType<?>> panelContentParams) {
		setParameter(panelParam, panelContentParams);
		setLayout(layout, panelParam);
		setUIFlag(STRUCTURE_CHANGED, panelParam);

		// mark the content parameters as panel elements so that they
		// can be detected as subordinate parameters
		addPanelParameters(panelContentParams);
	}

	/**
	 * Configures a parameter to be displayed in a panel with 2 or 3 segments.
	 * Either the first or the last parameter may be NULL but not both and not
	 * the center parameter. For a vertical orientation of the panel the UI
	 * property {@link StyleProperties#ORIENTATION} should be set to
	 * {@link Orientation#VERTICAL VERTICAL}.
	 *
	 * @param panelParam  The data element list parameter to be displayed as a
	 *                    panel
	 * @param firstParam  The first parameter in the panel or NULL for none
	 * @param centerParam firstParam The center (main) parameter in the panel
	 * @param lastParam   The last parameter in the panel or NULL for none
	 * @param resizable   TRUE to make the panel resizable as a split panel
	 * @param iFlags      Boolean properties to be set on the panel parameter
	 */
	@SafeVarargs
	public final void addPanel(RelationType<List<RelationType<?>>> panelParam,
		RelationType<?> firstParam, RelationType<?> centerParam,
		RelationType<?> lastParam, boolean resizable,
		PropertyName<Boolean>... iFlags) {
		List<RelationType<?>> panelContentParams = new ArrayList<>(3);

		if (firstParam != null) {
			panelContentParams.add(firstParam);
		}

		panelContentParams.add(centerParam);

		if (lastParam != null) {
			panelContentParams.add(lastParam);
		}

		addPanel(panelParam, resizable ? LayoutType.SPLIT : LayoutType.DOCK,
			panelContentParams);

		for (PropertyName<Boolean> flag : iFlags) {
			setUIFlag(flag, panelParam);
		}
	}

	/**
	 * Appends another string below the current process step message.
	 *
	 * @param message The message string or resource key to append
	 */
	public void addProcessStepMessage(String message) {
		String stepMessage = getParameter(PROCESS_STEP_MESSAGE);

		if (stepMessage == null || stepMessage.length() == 0) {
			message = stepMessage;
		} else if (stepMessage.startsWith("$$")) {
			message = String.format("%s<br><br>{%s}", stepMessage, message);
		} else {
			message = String.format("$${%s}<br><br>{%s}", stepMessage,
				message);
		}

		setProcessStepMessage(message);
	}

	/**
	 * A convenience method to add selection dependencies to the UI property
	 * {@link UserInterfaceProperties#SELECTION_DEPENDENCY}.
	 *
	 * @param param           The parameter to set the dependency for
	 * @param reverseState    TRUE to reverse the selection state of buttons
	 * @param dependentParams The dependent parameters
	 */
	public final void addSelectionDependency(RelationType<?> param,
		boolean reverseState, RelationType<?>... dependentParams) {
		addSelectionDependency(param, reverseState,
			Arrays.asList(dependentParams));
	}

	/**
	 * A convenience method to add selection dependencies to the UI property
	 * {@link UserInterfaceProperties#SELECTION_DEPENDENCY}. Empty parameter
	 * lists will be ignored.
	 *
	 * @param param           The parameter to set the dependency for
	 * @param reverseState    TRUE to reverse the selection state of buttons
	 * @param dependentParams The dependent parameters
	 */
	public final void addSelectionDependency(RelationType<?> param,
		boolean reverseState,
		Collection<? extends RelationType<?>> dependentParams) {
		if (dependentParams.size() > 0) {
			StringBuilder dependencies = new StringBuilder();

			String currentDependencies =
				getUIProperty(SELECTION_DEPENDENCY, param);

			if (currentDependencies != null &&
				currentDependencies.length() > 0) {
				dependencies.append(currentDependencies).append(',');
			}

			for (RelationType<?> dependentParam : dependentParams) {
				if (reverseState) {
					dependencies.append(SELECTION_DEPENDENCY_REVERSE_PREFIX);
				}

				dependencies.append(dependentParam.getName());
				dependencies.append(',');
			}

			dependencies.setLength(dependencies.length() - 1);

			setUIProperty(SELECTION_DEPENDENCY, dependencies.toString(),
				param);
		}
	}

	/**
	 * Configures a parameter to be displayed in a separate stack panel.
	 *
	 * @param panelParam         The data element list parameter to be
	 *                              displayed
	 *                           as a stack panel
	 * @param panelContentParams The parameters to be displayed in the panel
	 */
	public void addStackPanel(RelationType<List<RelationType<?>>> panelParam,
		RelationType<?>... panelContentParams) {
		addPanel(panelParam, LayoutType.STACK,
			Arrays.asList(panelContentParams));
	}

	/**
	 * Adds a subordinate fragment that handles a part of this interaction and
	 * is displayed in a certain parameter. The fragment parameter will not be
	 * added to the display parameters on this instance, this must be done
	 * separately (e.g. with {@link #addDisplayParameters(RelationType...)} to
	 * allow the invoking code to separate fragment creation from fragment
	 * placement.
	 *
	 * <p>If a fragment already exist in the given parameter it will be
	 * replaced with the new instance.</p>
	 *
	 * @param fragmentParam The interactive process parameter in which the
	 *                      fragment will be displayed
	 * @param subFragment   The fragment to add
	 */
	public void addSubFragment(
		RelationType<List<RelationType<?>>> fragmentParam,
		InteractionFragment subFragment) {
		if (subFragments.containsKey(fragmentParam)) {
			InteractionFragment previousFragment =
				subFragments.remove(fragmentParam);

			previousFragment.cleanup();
		}

		subFragment.attach((Interaction) getProcessStep(), fragmentParam);
		subFragment.setup();
		subFragments.put(fragmentParam, subFragment);

		setParameter(fragmentParam, subFragment.getInteractionParameters());

		get(INPUT_PARAMS).add(fragmentParam);
		subFragment.markFragmentInputParams();
	}

	/**
	 * Configures a parameter to be displayed in a separate tab panel.
	 *
	 * @param panelParam         The data element list parameter to be
	 *                              displayed
	 *                           as a tab panel
	 * @param panelContentParams The parameters to be displayed in the panel
	 */
	public void addTabPanel(RelationType<List<RelationType<?>>> panelParam,
		RelationType<?>... panelContentParams) {
		addPanel(panelParam, LayoutType.TABS,
			Arrays.asList(panelContentParams));
	}

	/**
	 * Annotates a process parameter for a storage query.
	 *
	 * @param param      The parameter type
	 * @param query      The query predicate
	 * @param sortOrder  The sort order predicate of the query or NULL for the
	 *                   default sort order
	 * @param attributes The entity attributes to query
	 */
	public <E extends Entity> void annotateForEntityQuery(
		RelationType<? super E> param, QueryPredicate<E> query,
		Predicate<? super Entity> sortOrder,
		List<Function<? super E, ?>> attributes) {
		Relation<? super E> relation = getParameterRelation(param);

		if (relation == null) {
			relation = setParameter(param, null);
		} else {
			markParameterAsModified(param);
		}

		EntityProcessDefinition.annotateForEntityQuery(relation, query,
			sortOrder, attributes);
	}

	/**
	 * Annotates a process parameter for a storage query.
	 *
	 * @param param      The parameter type
	 * @param query      The query predicate
	 * @param sortOrder  The sort order predicate of the query or NULL for the
	 *                   default sort order
	 * @param attributes The entity attributes to query
	 */
	public <E extends Entity> void annotateForEntityQuery(
		RelationType<? super E> param, QueryPredicate<E> query,
		Predicate<? super Entity> sortOrder, RelationType<?>... attributes) {
		annotateForEntityQuery(param, query, sortOrder,
			Arrays.asList(attributes));
	}

	/**
	 * Sets an annotation on a certain process parameter. If the parameter
	 * hasn't been set before it will be set to the given initial value to
	 * create a relation that can be annotated.
	 *
	 * @param param          The parameter to annotate
	 * @param initialValue   The initial value if the parameter doesn't exist
	 * @param annotationType The relation type of the annotation
	 * @param value          The annotation value
	 * @see #removeParameterAnnotation(RelationType, RelationType)
	 */
	public <T, A> void annotateParameter(RelationType<T> param, T initialValue,
		RelationType<A> annotationType, A value) {
		Process process = getProcess();
		Relation<?> relation = process.getRelation(param);

		if (relation == null) {
			if (initialValue == null) {
				initialValue = param.initialValue(process.getContext());
			}

			relation = setParameter(param, initialValue);
		} else {
			markParameterAsModified(param);

			if (annotationType == ALLOWED_VALUES) {
				setUIFlag(DataElement.ALLOWED_VALUES_CHANGED, param);
			}
		}

		relation.annotate(annotationType, value);
	}

	/**
	 * Stores the value of a derived process parameter under the original
	 * relation type in a target object. For details about derived parameters
	 * see {@link ProcessRelationTypes#deriveParameter(String, RelationType)}.
	 * If the target object is an {@link Entity} and the original relation type
	 * is an extra attribute type this method will set the extra attribute
	 * value.
	 *
	 * @param derivedParam The derived parameter
	 * @param target       The target object to set the derived parameter on
	 */
	public <T> void applyDerivedParameter(RelationType<T> derivedParam,
		Relatable target) {
		@SuppressWarnings("unchecked")
		RelationType<T> originalType =
			(RelationType<T>) derivedParam.get(ORIGINAL_RELATION_TYPE);

		T paramValue = getParameter(derivedParam);

		if (paramValue instanceof String &&
			((String) paramValue).length() == 0) {
			paramValue = null;
		}

		if (target instanceof Entity &&
			originalType.hasFlag(EXTRA_ATTRIBUTE_FLAG)) {
			try {
				((Entity) target).setExtraAttribute(originalType, paramValue);
			} catch (StorageException e) {
				throw new RuntimeProcessException(this, e);
			}
		} else {
			target.set(originalType, paramValue);
		}
	}

	/**
	 * Applies multiple derived parameters to a certain target. See the method
	 * {@link #applyDerivedParameter(RelationType, Relatable)} for details.
	 *
	 * @param derivedParams The derived parameters
	 * @param target        The target object to set the derived parameters on
	 */
	public void applyDerivedParameters(List<RelationType<?>> derivedParams,
		Relatable target) {
		for (RelationType<?> param : derivedParams) {
			applyDerivedParameter(param, target);
		}
	}

	/**
	 * Sets a certain extra attribute on an entity if it is available as a
	 * parameter in the process of this fragment. NULL values, empty strings,
	 * and empty collections will be ignored.
	 *
	 * @param entity    The entity to set the extra attribute on
	 * @param extraAttr The extra attribute type
	 * @throws StorageException If setting the extra attribute fails
	 */
	public <T> void applyExtraAttribute(Entity entity,
		RelationType<T> extraAttr) throws StorageException {
		T value = getParameter(extraAttr);

		if (value != null) {
			if (!((value instanceof String && ((String) value).isEmpty()) ||
				(value instanceof Collection &&
					((Collection<?>) value).isEmpty()))) {
				entity.setExtraAttribute(extraAttr, value);
			}
		}
	}

	/**
	 * Convenience method to set a certain boolean display properties on one or
	 * more process parameters to FALSE.
	 *
	 * @see #setUIProperty(PropertyName, Object, RelationType...)
	 */
	public final void clearUIFlag(PropertyName<Boolean> property,
		RelationType<?>... params) {
		clearUIFlag(property, Arrays.asList(params));
	}

	/**
	 * Convenience method to set a certain boolean display properties on one or
	 * more process parameters to FALSE.
	 *
	 * @see #setUIProperty(PropertyName, Object, Collection)
	 */
	public final void clearUIFlag(PropertyName<Boolean> property,
		Collection<? extends RelationType<?>> params) {
		setUIProperty(property, Boolean.FALSE, params);
	}

	/**
	 * Reads the value of a derived process parameter with the original
	 * relation
	 * type from a certain object and stores it in the process. For details see
	 * {@link ProcessRelationTypes#deriveParameter(String, RelationType)}. If
	 * the source object is an {@link Entity} and the original relation type is
	 * an extra attribute type this method will retrieve the extra attribute
	 * value.
	 *
	 * @param source       The source object to read the parameter value from
	 * @param derivedParam The derived parameter
	 * @param skipExisting If TRUE only parameters that don't exist already
	 *                        will
	 *                     be collected from the source object
	 */
	public <T> void collectDerivedParameter(Relatable source,
		RelationType<T> derivedParam, boolean skipExisting) {
		if (!skipExisting || !hasParameter(derivedParam)) {
			setParameter(derivedParam,
				getDerivedParameterValue(source, derivedParam));
		}
	}

	/**
	 * Collects multiple derived parameters from a certain source. See the
	 * method
	 * {@link #collectDerivedParameter(Relatable, RelationType, boolean)}
	 * for details.
	 *
	 * @param source        The source object to read the the derived
	 *                         parameters
	 *                      from
	 * @param derivedParams The derived parameters
	 * @param skipExisting  If TRUE only parameters that don't exist already
	 *                      will be collected from the source object
	 */
	public void collectDerivedParameters(Relatable source,
		List<RelationType<?>> derivedParams, boolean skipExisting) {
		for (RelationType<?> param : derivedParams) {
			collectDerivedParameter(source, param, skipExisting);
		}
	}

	/**
	 * Deletes several parameters from the process.
	 *
	 * @param paramTypes The types of the parameters to delete
	 */
	public final void deleteParameters(RelationType<?>... paramTypes) {
		Process process = getProcess();

		for (RelationType<?> paramType : paramTypes) {
			process.deleteRelation(paramType);
		}
	}

	/**
	 * Convenience method with a varargs parameter.
	 *
	 * @see #disableElements(RelationType, Collection)
	 */
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void disableElements(RelationType<E> enumParam,
		E... disabledElements) {
		disableElements(enumParam, Arrays.asList(disabledElements));
	}

	/**
	 * Disables certain elements of an enum parameter. The elements will still
	 * be displayed but cannot be modified. This works only with discrete
	 * display types like check boxes.
	 *
	 * @param enumParam        The parameter to disable the elements of
	 * @param disabledElements The elements to disable (NULL or none to enable
	 *                         all)
	 */
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void disableElements(RelationType<E> enumParam,
		Collection<E> disabledElements) {
		disableElements(enumParam, (Class<E>) enumParam.getTargetType(),
			getAllowedValues(enumParam), disabledElements);
	}

	/**
	 * Sets the property {@link StyleProperties#DISABLED_ELEMENTS} for a
	 * certain
	 * set of values. If the datatype is an enum class and the all elements
	 * argument is NULL the enum values will be read from the datatype class
	 * with {@link Class#getEnumConstants()}.
	 *
	 * @param param            The parameter to disable the elements of
	 * @param datatype         The parameter datatype
	 * @param allElements      All values that are displayed (NULL for all
	 *                         values of an enum data type)
	 * @param disabledElements The elements to disable (NULL or none to enable
	 *                         all)
	 */
	public <T> void disableElements(RelationType<?> param, Class<T> datatype,
		Collection<T> allElements, Collection<T> disabledElements) {
		if (disabledElements != null && disabledElements.size() > 0) {
			if (allElements == null && datatype.isEnum()) {
				allElements = Arrays.asList(datatype.getEnumConstants());
			}

			StringBuilder disabled = new StringBuilder();
			List<T> indexedElements = new ArrayList<T>(allElements);

			for (T element : disabledElements) {
				int index = indexedElements.indexOf(element);

				if (index >= 0) {
					disabled.append('(');
					disabled.append(index);
					disabled.append(')');
				}
			}

			setUIProperty(DISABLED_ELEMENTS, disabled.toString(), param);
		} else {
			removeUIProperties(param, DISABLED_ELEMENTS);
		}
	}

	/**
	 * Disables certain elements of an enum parameter. The elements will still
	 * be displayed but cannot be modified. This works only with discrete
	 * display types like check boxes.
	 *
	 * @param enumCollectionParam The parameter to disable the elements of
	 * @param disabledElements    The elements to disable (NULL or none to
	 *                            enable all)
	 */
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>, C extends Collection<E>> void disableMultiSelectionElements(
		RelationType<C> enumCollectionParam, Collection<E> disabledElements) {
		disableElements(enumCollectionParam,
			(Class<E>) enumCollectionParam.get(MetaTypes.ELEMENT_DATATYPE),
			getAllowedElements(enumCollectionParam), disabledElements);
	}

	/**
	 * Sets the value of a selected history record into a certain parameter.
	 *
	 * @param historyParam      The parameter with the history selection
	 * @param historyValueParam The parameter for the history value
	 */
	public void displayHistoryValue(RelationType<HistoryRecord> historyParam,
		RelationType<String> historyValueParam) {
		HistoryRecord history = getParameter(historyParam);
		String historyValue = "";

		if (history != null) {
			historyValue = history.get(HistoryRecord.VALUE);
		}

		setParameter(historyValueParam, historyValue);
	}

	/**
	 * Enables all values of an enum parameter.
	 *
	 * @see #disableElements(RelationType, Collection)
	 */
	public void enableAllElements(RelationType<? extends Enum<?>> enumParam) {
		removeUIProperties(enumParam, DISABLED_ELEMENTS);
	}

	/**
	 * Allowed to query the allowed values of a collection-type process
	 * parameter.
	 *
	 * @param param The parameter type
	 * @return The allowed values for the parameter (can be NULL)
	 */
	@SuppressWarnings("unchecked")
	public <T, C extends Collection<T>> Collection<T> getAllowedElements(
		RelationType<C> param) {
		// mark the parameter as modified because it is probable the the list
		// is queried for modification
		markParameterAsModified(param);

		Relation<C> relation = getParameterRelation(param);

		return relation != null ?
		       (Collection<T>) relation.get(ALLOWED_VALUES) :
		       null;
	}

	/**
	 * Allowed to query the allowed values of a process parameter.
	 *
	 * @param param The parameter type
	 * @return The allowed values for the parameter (can be NULL)
	 */
	@SuppressWarnings("unchecked")
	public <T> Collection<T> getAllowedValues(RelationType<T> param) {
		// mark the parameter as modified because it is probable the list
		// is queried for modification
		markParameterAsModified(param);

		Relation<T> relation = getParameterRelation(param);

		return relation != null ?
		       (Collection<T>) relation.get(ALLOWED_VALUES) :
		       null;
	}

	/**
	 * Returns the current query predicate for a parameter that has been
	 * annotated with a storage query. If the query hasn't (yet) been executed
	 * the result will be NULL.
	 *
	 * @param queryParam The annotated parameter
	 * @return The current query predicate or NULL if no query is available for
	 * the given parameter
	 */
	@SuppressWarnings("unchecked")
	public <E extends Entity> QueryPredicate<E> getCurrentQuery(
		RelationType<E> queryParam) {
		QueryPredicate<E> query = null;

		StorageAdapterId adapterId =
			getParameterRelation(queryParam).get(STORAGE_ADAPTER_ID);

		if (adapterId != null) {
			StorageAdapterRegistry registry =
				getParameter(STORAGE_ADAPTER_REGISTRY);

			try {
				StorageAdapter storageAdapter =
					registry.getStorageAdapter(adapterId);

				if (storageAdapter != null) {
					query =
						(QueryPredicate<E>) storageAdapter.getCurrentQueryCriteria();
				}
			} catch (StorageException e) {
				throw new IllegalStateException(e);
			}
		}

		return query;
	}

	/**
	 * Retrieves the value for a derived parameter type from a source object.
	 * The value will be determined by retrieving the original relation type
	 * from the source. If the source object is an {@link Entity} and the
	 * original relation type is an extra attribute type this method will
	 * retrieve the extra attribute value.
	 *
	 * @param source       The source object to get the value from
	 * @param derivedParam The derived parameter type
	 * @return The value
	 */
	public <T> T getDerivedParameterValue(Relatable source,
		RelationType<T> derivedParam) {
		T value;
		@SuppressWarnings("unchecked")
		RelationType<T> originalType =
			(RelationType<T>) derivedParam.get(ORIGINAL_RELATION_TYPE);

		if (source instanceof Entity &&
			originalType.hasFlag(EXTRA_ATTRIBUTE_FLAG)) {
			try {
				value = ((Entity) source).getExtraAttribute(originalType,
					null);
			} catch (StorageException e) {
				throw new RuntimeProcessException(this, e);
			}
		} else {
			value = source.get(originalType);
		}

		return value;
	}

	/**
	 * Returns the process-relative ID of this fragment.
	 *
	 * @return The fragment ID
	 */
	public int getFragmentId() {
		if (fragmentId == -1) {
			fragmentId = getProcess().getNextFragmentId();
		}

		return fragmentId;
	}

	/**
	 * Returns the parameter that caused an interaction event.
	 *
	 * @return The interaction parameter or NULL for none
	 */
	public RelationType<?> getInteractiveInputParameter() {
		return getParameter(INTERACTION_EVENT_PARAM);
	}

	/**
	 * Returns the value of a certain parameter from this fragment. This method
	 * will first try to look up the parameter in the enclosing process by
	 * invoking {@link Process#getParameter(RelationType)}. If the parameter is
	 * not found there, it will be queried from this fragment's relations.
	 *
	 * @param paramType The type of the parameter to return the value of
	 * @return The parameter value or NULL if not set
	 */
	public <T> T getParameter(RelationType<T> paramType) {
		Process process = getProcess();
		T param;

		// also query the relation from the process if it is not set on the
		// step
		// to consider initial or default values; especially initial values
		// need
		// to be created to be available to subsequent steps
		if (process.hasParameter(paramType) || !hasRelation(paramType)) {
			param = process.getParameter(paramType);
		} else {
			param = get(paramType);
		}

		return param;
	}

	/**
	 * Returns a certain annotation from a process parameter.
	 *
	 * @param param          The parameter to get the annotation for
	 * @param annotationType The relation type of the annotation
	 * @see #annotateParameter(RelationType, Object, RelationType, Object)
	 */
	public <T> T getParameterAnnotation(RelationType<?> param,
		RelationType<T> annotationType) {
		Relation<?> relation = getProcess().getRelation(param);
		T annotation = null;

		if (relation != null) {
			annotation = relation.getAnnotation(annotationType);
		}

		return annotation;
	}

	/**
	 * Returns the relation of a certain parameter for this step. This method
	 * will first try to look up the relation in the enclosing process by
	 * invoking {@link Process#getRelation(RelationType)}. If the relation is
	 * not found there, it will be queried from this step.
	 *
	 * @param paramType The type of the relation to return
	 * @return The corresponding relation or NULL if not set
	 */
	public <T> Relation<T> getParameterRelation(RelationType<T> paramType) {
		Process process = getProcess();
		Relation<T> relation;

		if (process.hasParameter(paramType)) {
			relation = process.getRelation(paramType);
		} else {
			relation = getRelation(paramType);
		}

		return relation;
	}

	/**
	 * Returns the process that this fragment is associated with.
	 *
	 * @return The process of this fragment
	 */
	public abstract Process getProcess();

	/**
	 * Returns the process step this fragment represents or belongs to.
	 *
	 * @return The process step of this fragment
	 */
	public abstract ProcessStep getProcessStep();

	/**
	 * @see Process#getProcessUser()
	 */
	public final Entity getProcessUser() {
		return getProcess().getProcessUser();
	}

	/**
	 * Returns the sub-fragment that is associated with a certain fragment
	 * parameter.
	 *
	 * @param fragmentParam The sub fragment parameter
	 * @return The sub fragment (NULL for none)
	 */
	public InteractionFragment getSubFragment(
		RelationType<List<RelationType<?>>> fragmentParam) {
		return subFragments.get(fragmentParam);
	}

	/**
	 * Returns a temporary parameter list relation type with a default name.
	 *
	 * @see #getTemporaryListType(String, Class)
	 */
	public <T> RelationType<List<T>> getTemporaryListType(
		Class<? super T> elementType) {
		return getTemporaryListType(null, elementType);
	}

	/**
	 * Returns a temporary parameter relation type that references a list
	 * with a
	 * certain element datatype. The parameter will have an empty list as it's
	 * initial value.
	 *
	 * @param name        The name of the parameter
	 * @param elementType The list element datatype
	 * @return The temporary list parameter type
	 * @see #getTemporaryParameterType(String, Class)
	 */
	public <T> RelationType<List<T>> getTemporaryListType(String name,
		Class<? super T> elementType) {
		name = getTemporaryParameterName(name);

		@SuppressWarnings("unchecked")
		RelationType<List<T>> param =
			(RelationType<List<T>>) RelationType.valueOf(name);

		if (param == null) {
			param = newListType(name, elementType);
		} else {
			assert param.getTargetType() == List.class &&
				param.get(ELEMENT_DATATYPE) == elementType;
		}

		getProcess().registerTemporaryParameterType(param);

		return param;
	}

	/**
	 * Returns a temporary parameter relation type with an automatically
	 * generated name.
	 *
	 * @see #getTemporaryParameterType(String, Class)
	 */
	public <T> RelationType<T> getTemporaryParameterType(
		Class<? super T> datatype) {
		return getTemporaryParameterType(null, datatype);
	}

	/**
	 * Returns a temporary parameter relation type with a certain name. If the
	 * parameter doesn't exist yet it will be created. The name string will be
	 * converted to standard relation type notation, i.e. upper case text with
	 * separating underscores.
	 *
	 * <p>This method is intended to generate parameter types dynamically at
	 * runtime when it is not possible to create the parameters as static
	 * constants. The parameters will only be valid for the current process
	 * execution and will be removed when the process ends.</p>
	 *
	 * @param name     The name of the parameter type or NULL for a default
	 *                 name
	 * @param datatype The datatype class of the type
	 * @return The temporary relation type instance
	 */
	public <T> RelationType<T> getTemporaryParameterType(String name,
		Class<? super T> datatype) {
		name = getTemporaryParameterName(name);

		@SuppressWarnings("unchecked")
		RelationType<T> param = (RelationType<T>) RelationType.valueOf(name);

		if (param == null) {
			param = newRelationType(name, datatype);
		} else {
			assert param.getTargetType() == datatype;
		}

		getProcess().registerTemporaryParameterType(param);

		return param;
	}

	/**
	 * Returns a temporary parameter type for another relation type. The
	 * derived
	 * type will also contain the original relation type in the meta-relation
	 * {@link ProcessRelationTypes#ORIGINAL_RELATION_TYPE}.
	 *
	 * @param name         The name of the new relation type or NULL to use the
	 *                     simple name of the original type
	 * @param originalType The original parameter relation type
	 * @return The temporary parameter relation type
	 */
	public <T> RelationType<T> getTemporaryParameterType(String name,
		RelationType<T> originalType) {
		if (name == null) {
			name = originalType.getSimpleName();
		}

		RelationType<T> derivedType =
			getTemporaryParameterType(name, originalType.getTargetType());

		derivedType.annotate(ORIGINAL_RELATION_TYPE, originalType);

		return derivedType;
	}

	/**
	 * Returns the UI properties for a certain parameter if they exist.
	 *
	 * @param param The parameter relation type
	 * @return The UI properties or NULL for none
	 */
	public MutableProperties getUIProperties(RelationType<?> param) {
		return getUIProperties(param, false);
	}

	/**
	 * Returns the UI properties for a certain parameter. If the boolean
	 * parameter is TRUE and the properties don't exist empty properties
	 * will be
	 * created and set for the parameter.
	 *
	 * @param param  The parameter relation type
	 * @param create TRUE to create non-existing properties
	 * @return The UI properties or NULL for none if create is FALSE
	 */
	public MutableProperties getUIProperties(RelationType<?> param,
		boolean create) {
		MutableProperties uiProperties = null;

		if (hasParameter(param)) {
			uiProperties = getParameterRelation(param).get(DISPLAY_PROPERTIES);
		}

		if (uiProperties == null && create) {
			uiProperties = new StringProperties();
			annotateParameter(param, null, DISPLAY_PROPERTIES, uiProperties);
		}

		return uiProperties;
	}

	/**
	 * Returns a certain user interface property value for a particular
	 * parameter.
	 *
	 * @param property The property to return the value of
	 * @param param    The parameter to query for the property
	 * @return The property value or NULL if not set
	 */
	public <T> T getUIProperty(PropertyName<T> property,
		RelationType<?> param) {
		HasProperties properties = getUIProperties(param);
		T value = null;

		if (properties != null) {
			value = properties.getProperty(property, null);
		}

		return value;
	}

	/**
	 * Shortcut method to return a particular value from the settings of the
	 * current user with a default value of NULL.
	 *
	 * @param settingsExtraAttr The extra attribute of the setting
	 * @return The settings value or NULL for none
	 * @throws StorageException If accessing the user settings fails
	 * @see #getUserSettings(boolean)
	 */
	public <T> T getUserSetting(RelationType<T> settingsExtraAttr)
		throws StorageException {
		return Configuration.getSettingsValue(getProcessUser(),
			settingsExtraAttr, null);
	}

	/**
	 * Returns the configuration entity that contains the settings for the
	 * current process user.
	 *
	 * @param create TRUE to create the settings object if it doesn't exist
	 * @return The user's settings or NULL if none exist and create is FALSE
	 * @throws TransactionException If saving a new configuration fails
	 * @throws StorageException     If retrieving the configuration entity
	 *                              fails
	 */
	public Configuration getUserSettings(boolean create)
		throws StorageException, TransactionException {
		return Configuration.getSettings(getProcessUser(), create);
	}

	/**
	 * A convenience method to check the existence and value of a parameter
	 * with
	 * a boolean datatype.
	 *
	 * @param flagType The flag parameter type
	 * @return TRUE if the flag exists and is set to TRUE
	 */
	public final boolean hasFlagParameter(RelationType<Boolean> flagType) {
		Boolean flagParam = getParameter(flagType);

		return flagParam != null && flagParam.booleanValue();
	}

	/**
	 * Convenience method to check the availability of a certain parameter in
	 * the enclosing process.
	 *
	 * @see Process#hasParameter(RelationType)
	 */
	public boolean hasParameter(RelationType<?> paramType) {
		return hasRelation(paramType) || getProcess().hasParameter(paramType);
	}

	/**
	 * Convenience method to check the existence and state of a boolean UI
	 * property of a parameter.
	 *
	 * @param flagName The name of the flag property
	 * @param param    The parameter to check the flag for
	 * @return TRUE if the flag property exists and is TRUE
	 */
	public boolean hasUIFlag(PropertyName<Boolean> flagName,
		RelationType<?> param) {
		Boolean flag = getUIProperty(flagName, param);

		return flag != null && flag.booleanValue();
	}

	/**
	 * Shortcut method to check if a boolean flag ist set int the settings of
	 * the current user.
	 *
	 * @param settingsFlag The extra attribute of the settings flag
	 * @return TRUE if the flag is set, FALSE if not or if it doesn't exist
	 * @throws StorageException If accessing the user settings fails
	 * @see #getUserSettings(boolean)
	 */
	public boolean hasUserSetting(RelationType<Boolean> settingsFlag)
		throws StorageException {
		Boolean flag =
			Configuration.getSettingsValue(getProcessUser(), settingsFlag,
				null);

		return Boolean.TRUE.equals(flag);
	}

	/**
	 * Checks whether the process has been suspended because of an interactive
	 * input event.
	 *
	 * @return TRUE if the process is suspended because of an interactive input
	 * event
	 */
	public boolean isInteractiveInput() {
		return getParameter(INTERACTION_EVENT_PARAM) != null;
	}

	/**
	 * Tries to acquire a modification lock on an entity during the
	 * execution of
	 * this process fragment. If successful, a cleanup action will be
	 * registered
	 * with {@link #addCleanupAction(String, Consumer)} that removes the
	 * lock if
	 * the process is finished.
	 *
	 * @param entity The entity to lock
	 * @return TRUE if the lock could be acquired, FALSE if the entity is
	 * already locked
	 */
	public final boolean lockEntity(Entity entity) {
		assert entity.isPersistent();

		boolean success = entity.lock();

		if (success) {
			addCleanupAction(
				Process.CLEANUP_KEY_UNLOCK_ENTITY + entity.getGlobalId(),
				f -> entity.unlock());
		}

		return success;
	}

	/**
	 * Marks a certain parameter as modified to force a UI update in the next
	 * interaction.
	 *
	 * @param param The parameter to mark as modified
	 */
	public <T> void markParameterAsModified(RelationType<T> param) {
		getProcessStep().parameterModified(param);
	}

	/**
	 * Prepares a date parameter for input. If the parameter is not yet an
	 * element of this step's interaction parameters it will be added as an
	 * input parameter.
	 *
	 * @param param        The parameter to initialize
	 * @param date         The current date to initialize the parameter with
	 * @param earliestDate If not NULL an input validation will be added to
	 *                     ensure that the input date is not before this date
	 *                     and that the date is set
	 */
	public void prepareDateInput(RelationType<Date> param, Date date,
		Date earliestDate) {
		if (!get(INTERACTION_PARAMS).contains(param)) {
			addInputParameters(param);
		}

		if (getParameter(param) == null) {
			setParameter(param, date);
		}

		if (earliestDate != null) {
			setParameterValidation(param, false,
				doIfElse(isNull(), value(MSG_PARAM_NOT_SET),
					doIf(lessThan(earliestDate), value("DateIsBeforeToday"))));
		}
	}

	/**
	 * A shortcut method that only creates a download URL without assigning it
	 * to a parameter.
	 *
	 * @see #prepareDownload(RelationType, String, FileType, Function)
	 */
	public String prepareDownload(String fileName, FileType fileType,
		Function<FileType, ?> dataGenerator) throws Exception {
		return prepareDownload(null, fileName, fileType, dataGenerator);
	}

	/**
	 * Prepares the file download for a URL interaction parameter. This method
	 * will perform the necessary cleanup operations if the download changes or
	 * the process step is left.
	 *
	 * @param urlParam      The interaction parameter to store the URL in or
	 *                      NULL for none
	 * @param fileName      The file name
	 * @param fileType      the file type
	 * @param dataGenerator The function that generates the download data
	 * @return The app-relative download URL
	 * @throws Exception If preparing the download fails
	 */
	public String prepareDownload(RelationType<String> urlParam,
		String fileName, FileType fileType,
		Function<FileType, ?> dataGenerator)
		throws Exception {
		final SessionManager sessionManager =
			getParameter(DataRelationTypes.SESSION_MANAGER);

		DownloadData downloadData =
			new DownloadData(fileName, fileType, dataGenerator, false);

		if (urlParam != null) {
			String oldUrl = getParameter(urlParam);

			if (oldUrl != null) {
				sessionManager.removeDownload(oldUrl);
				getProcessStep().removeCleanupAction(oldUrl);
			}
		}

		final String downloadUrl =
			sessionManager.prepareDownload(downloadData);

		if (urlParam != null) {
			setParameter(urlParam, downloadUrl);
		}

		addCleanupAction(downloadUrl,
			f -> sessionManager.removeDownload(downloadUrl));

		return downloadUrl;
	}

	/**
	 * Removes all sub-fragments from this instance.
	 */
	public void removeAllSubFragments() {
		List<RelationType<List<RelationType<?>>>> subFragmentParams =
			new ArrayList<>(subFragments.keySet());

		for (RelationType<List<RelationType<?>>> subFragmentParam :
			subFragmentParams) {
			removeSubFragment(subFragmentParam);
		}
	}

	/**
	 * Completely removes all display properties for one or more process
	 * parameters.
	 *
	 * @param params The parameters to remove the display properties from
	 */
	public void removeAllUIProperties(RelationType<?>... params) {
		removeAllUIProperties(Arrays.asList(params));
	}

	/**
	 * Completely removes all display properties for multiple process
	 * parameters.
	 *
	 * @param params The parameters to remove the display properties from
	 */
	public void removeAllUIProperties(
		Collection<? extends RelationType<?>> params) {
		for (RelationType<?> param : params) {
			if (hasParameter(param)) {
				getParameterRelation(param).deleteRelation(DISPLAY_PROPERTIES);
			}
		}
	}

	/**
	 * Removes a cleanup action that has previously been registered through the
	 * method {@link #addCleanupAction(String, Consumer)}.
	 *
	 * @param key The key that identifies the action to remove
	 * @return The registered action or NULL for none
	 */
	public Consumer<ProcessFragment> removeCleanupAction(String key) {
		return cleanupActions.remove(key);
	}

	/**
	 * Removes all parameters for a panel parameter that had previously been
	 * added through {@link #addPanel(RelationType, LayoutType, List)}.
	 *
	 * @param panelParam The parameter of the panel to remove
	 */
	public void removePanel(RelationType<List<RelationType<?>>> panelParam) {
		List<RelationType<?>> panelElements = getParameter(panelParam);

		if (panelElements != null) {
			panelParameters.removeAll(panelElements);
		}

		deleteParameters(panelParam);
	}

	/**
	 * Remove an annotation from a certain process parameter.
	 *
	 * @param param          The parameter to annotate
	 * @param annotationType The relation type of the annotation
	 * @see #annotateParameter(RelationType, Object, RelationType, Object)
	 */
	public void removeParameterAnnotation(RelationType<?> param,
		RelationType<?> annotationType) {
		Relation<?> relation = getProcess().getRelation(param);

		if (relation != null) {
			relation.deleteRelation(annotationType);
		}
	}

	/**
	 * Removes a certain sub-fragment instance.
	 *
	 * @param subFragment The sub-fragment to remove
	 */
	public void removeSubFragment(InteractionFragment subFragment) {
		removeSubFragment(subFragment.getFragmentParameter());
	}

	/**
	 * Removes a subordinate fragment that had been added previously by
	 * means of
	 * {@link #addSubFragment(RelationType, InteractionFragment)}.
	 *
	 * @param fragmentParam The parameter the fragment is stored in
	 * @return The removed fragment instance (may be NULL)
	 */
	public InteractionFragment removeSubFragment(
		RelationType<List<RelationType<?>>> fragmentParam) {
		InteractionFragment subFragment = subFragments.remove(fragmentParam);

		if (subFragment != null) {
			get(INPUT_PARAMS).removeAll(subFragment.getInputParameters());
			removeInteractionParameters(fragmentParam);
			deleteParameters(fragmentParam);
			subFragment.abortFragment();
		}

		return subFragment;
	}

	/**
	 * Removes display properties from a certain parameter.
	 *
	 * @param param      The parameter
	 * @param properties The properties to remove
	 */
	public <T> void removeUIProperties(RelationType<?> param,
		PropertyName<?>... properties) {
		MutableProperties uiProperties = getUIProperties(param);

		if (uiProperties != null) {
			for (PropertyName<?> property : properties) {
				uiProperties.removeProperty(property);
			}

			markParameterAsModified(param);
		}
	}

	/**
	 * Shortcut method to annotate a collection-type process parameters with
	 * the
	 * elements that are allowed to occur in the collection. The elements are
	 * stored in an {@link ProcessRelationTypes#ALLOWED_VALUES} annotation. If
	 * the parameter hasn't been set yet it will be initialized with the given
	 * value.
	 *
	 * @param param  The parameter to annotate
	 * @param values The list of allowed values for the parameter
	 */
	public <T, C extends Collection<T>, V extends Collection<T>> void setAllowedElements(
		RelationType<C> param, V values) {
		annotateParameter(param, null, ALLOWED_VALUES, values);
	}

	/**
	 * Shortcut method to set the allowed values of a process parameters. The
	 * possible values for the parameters are stored in an annotation of type
	 * {@link ProcessRelationTypes#ALLOWED_VALUES}. If the parameter hasn't
	 * been
	 * set yet it will be initialized with the given value.
	 *
	 * @param param  The parameter to annotate
	 * @param values The allowed values for the parameter
	 */
	@SafeVarargs
	public final <T> void setAllowedValues(RelationType<T> param,
		T... values) {
		setAllowedValues(param, Arrays.asList(values));
	}

	/**
	 * Shortcut method to annotate a process parameters with allowed values.
	 * The
	 * possible values for the parameters are stored in an annotation of type
	 * {@link ProcessRelationTypes#ALLOWED_VALUES}. If the parameter hasn't
	 * been
	 * set yet it will be initialized with the given value.
	 *
	 * @param param  The parameter to annotate
	 * @param values The list of allowed values for the parameter
	 */
	public <T, C extends Collection<T>> void setAllowedValues(
		RelationType<T> param, C values) {
		annotateParameter(param, null, ALLOWED_VALUES, values);
	}

	/**
	 * @see #setEnabled(boolean, Collection)
	 */
	public final void setEnabled(boolean enabled, RelationType<?>... params) {
		setEnabled(enabled, Arrays.asList(params));
	}

	/**
	 * A convenience method to enable or disable certain interaction parameter
	 * by changing the UI flag {@link UserInterfaceProperties#DISABLED}
	 * accordingly.
	 *
	 * @param enabled The enabled state to set
	 * @param params  The parameters to set the enabled state of
	 */
	public final void setEnabled(boolean enabled,
		Collection<? extends RelationType<?>> params) {
		if (enabled) {
			clearUIFlag(DISABLED, params);
		} else {
			setUIFlag(DISABLED, params);
		}
	}

	/**
	 * Sets the properties {@link UserInterfaceProperties#HTML_WIDTH} and
	 * {@link UserInterfaceProperties#HTML_HEIGHT} on the given parameters.
	 *
	 * @param width  The HTML width string
	 * @param height width The HTML height string
	 * @param params The parameters to set the size of
	 */
	public void setHtmlSize(String width, String height,
		RelationType<?>... params) {
		for (RelationType<?> param : params) {
			setUIProperty(HTML_WIDTH, width, param);
			setUIProperty(HTML_HEIGHT, height, param);
		}
	}

	/**
	 * Shortcut method to initialize an enum parameter for an immediate action
	 * and with a certain number of columns to arrange the enum buttons in.
	 * This
	 * method will also set the
	 * {@link UserInterfaceProperties#HIDE_LABEL HIDE_LABEL} flag as it is
	 * typically used with immediate action buttons.
	 *
	 * @param param   The enum action parameter
	 * @param columns The number of columns
	 * @see #setInteractive(RelationType, Collection, ListStyle, Object...)
	 */
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void setImmediateAction(RelationType<E> param,
		int columns) {
		setImmediateAction(param);
		setUIFlag(HIDE_LABEL, param);
		setUIProperty(columns, COLUMNS, param);
	}

	/**
	 * Shortcut method to initialize an enum parameter for an immediate action.
	 * The enum will be displayed as buttons with {@link ListStyle#IMMEDIATE}.
	 *
	 * @param param         The enum action parameter
	 * @param allowedValues The allowed values
	 * @see #setInteractive(RelationType, Collection, ListStyle, Object...)
	 */
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void setImmediateAction(RelationType<E> param,
		E... allowedValues) {
		setInteractive(param, null, ListStyle.IMMEDIATE, allowedValues);
	}

	/**
	 * Shortcut method to initialize an enum parameter for an immediate action.
	 * The enum will be displayed as buttons with {@link ListStyle#IMMEDIATE}.
	 *
	 * @param param         The enum action parameter
	 * @param allowedValues The allowed values (NULL or empty for all values of
	 *                      the enum)
	 * @see #setInteractive(RelationType, Object, ListStyle, Collection)
	 */
	public <E extends Enum<E>> void setImmediateAction(RelationType<E> param,
		Collection<E> allowedValues) {
		setInteractive(param, null, ListStyle.IMMEDIATE, allowedValues);
	}

	/**
	 * Initializes non-list parameters for interactive input by setting the
	 * property {@link UserInterfaceProperties#INTERACTIVE_INPUT_MODE} to the
	 * given mode.
	 *
	 * @param mode   The interactive input mode
	 * @param params The parameters
	 */
	public void setInteractive(InteractiveInputMode mode,
		RelationType<?>... params) {
		for (RelationType<?> param : params) {
			setUIProperty(INTERACTIVE_INPUT_MODE, mode, param);
		}
	}

	/**
	 * Initializes a parameter for interactive input. If no allowed values are
	 * provided and the parameter datatype is an enum class all enum values
	 * will
	 * be allowed.
	 *
	 * @param param         The parameter
	 * @param initialValue  The initial parameter value or NULL for none
	 * @param listStyle     The style in which to display the list of values
	 * @param allowedValues The allowed values
	 */
	@SafeVarargs
	public final <T> void setInteractive(RelationType<T> param, T initialValue,
		ListStyle listStyle, T... allowedValues) {
		setInteractive(param, initialValue, listStyle,
			Arrays.asList(allowedValues));
	}

	/**
	 * Initializes a parameter for interactive input. If no allowed values are
	 * provided and the parameter datatype is an enum class all enum values
	 * will
	 * be allowed.
	 *
	 * @param param         The parameter
	 * @param intialValue   The initial parameter value or NULL for none
	 * @param listStyle     The style in which to display the list of values
	 * @param allowedValues The allowed values (NULL or empty for all values of
	 *                      an enum datatype)
	 */
	public <T> void setInteractive(RelationType<T> param, T intialValue,
		ListStyle listStyle, Collection<T> allowedValues) {
		setInteractiveImpl(param, intialValue, listStyle, allowedValues);
	}

	/**
	 * Initializes a list parameter for interactive input. The resulting user
	 * interface will allow the selection of multiple values in the list. If no
	 * allowed values are provided and the parameter datatype is an enum class
	 * all enum values will be allowed.
	 *
	 * @param param         The parameter
	 * @param defaultValues The default parameter value or NULL for none
	 * @param listStyle     The style in which to display the list of values
	 * @param allowedValues The allowed values
	 */
	@SuppressWarnings("unchecked")
	public <T, C extends Collection<T>> void setInteractive(
		RelationType<C> param, C defaultValues, ListStyle listStyle,
		T... allowedValues) {
		setInteractive(param, defaultValues, listStyle,
			Arrays.asList(allowedValues));
	}

	/**
	 * Initializes a list parameter for interactive input. The resulting user
	 * interface will allow the selection of multiple values in the list. If no
	 * allowed values are provided and the parameter datatype is an enum class
	 * all enum values will be allowed.
	 *
	 * @param param         The parameter
	 * @param initialValues The initial parameter values or NULL for none
	 * @param listStyle     The style in which to display the list of values
	 * @param allowedValues The allowed values (NULL or empty for all values of
	 *                      an enum datatype)
	 */
	public <T, C extends Collection<T>> void setInteractive(
		RelationType<C> param, C initialValues, ListStyle listStyle,
		Collection<T> allowedValues) {
		setInteractiveImpl(param, initialValues, listStyle, allowedValues);
	}

	/**
	 * Sets the {@link UserInterfaceProperties#LAYOUT LAYOUT} property for a
	 * {@link DataElementList} parameter.
	 *
	 * @param mode  The list display mode
	 * @param param The parameter
	 */
	public void setLayout(LayoutType mode,
		RelationType<List<RelationType<?>>> param) {
		setUIProperty(LAYOUT, mode, param);
	}

	/**
	 * Sets a certain parameter in the enclosing process. Other than the
	 * methods
	 * that query process step parameters this method will not access this
	 * step's relations. It always writes directly to the process parameters.
	 *
	 * @return The relation of the parameter
	 * @see Process#setParameter(RelationType, Object)
	 */
	public <T, R> Relation<T> setParameter(RelationType<T> param, T value) {
		Relation<T> paramRelation = getProcess().setParameter(param, value);

		markParameterAsModified(param);

		if (param.getTargetType() == List.class &&
			param.get(MetaTypes.ELEMENT_DATATYPE) == RelationType.class) {
			setUIFlag(STRUCTURE_CHANGED, param);
		}

		return paramRelation;
	}

	/**
	 * Convenience method to set boolean parameters without a boxing warning.
	 *
	 * @see #setParameter(RelationType, Object)
	 */
	public final Relation<Boolean> setParameter(RelationType<Boolean> param,
		boolean value) {
		return setParameter(param, Boolean.valueOf(value));
	}

	/**
	 * Sets the minimum and maximum values of an integer parameter.
	 *
	 * @param param The parameter
	 * @param min   The minimum value
	 * @param max   The maximum value
	 */
	@SuppressWarnings("boxing")
	public final void setParameterBounds(RelationType<Integer> param, int min,
		int max) {
		annotateParameter(param, null, MINIMUM, min);
		annotateParameter(param, null, MAXIMUM, max);
	}

	/**
	 * Sets a information string to be displayed as a readonly text for this
	 * process step. If other interaction parameters exist the information will
	 * be displayed as the first parameter if it doesn't exist already or, if a
	 * {@link #setProcessStepMessage(String) process message} exists, as the
	 * second.
	 *
	 * @param info The information string or a resource ID or NULL to reset
	 * @param rows The number of rows to display (&gt; 1 for a multi-line
	 *             display) or -1 to calculate the number of rows in the input
	 *             text
	 */
	public void setProcessStepInfo(String info, int rows) {
		if (info != null) {
			List<RelationType<?>> interactionParams = get(INTERACTION_PARAMS);

			if (!interactionParams.contains(PROCESS_STEP_INFO)) {
				int position =
					interactionParams.contains(PROCESS_STEP_MESSAGE) ? 1 : 0;

				interactionParams.add(position, PROCESS_STEP_INFO);
			}

			if (rows == -1) {
				rows = TextUtil.count(info, "\n");
			}

			setParameter(PROCESS_STEP_INFO, info);
			setUIFlag(HIDE_LABEL, PROCESS_STEP_INFO);
			setUIFlag(WRAP, PROCESS_STEP_INFO);
			setUIProperty(rows, ROWS, PROCESS_STEP_INFO);
			setUIProperty(CONTENT_TYPE, ContentType.HTML, PROCESS_STEP_INFO);
		} else {
			removeInteractionParameters(PROCESS_STEP_INFO);
		}
	}

	/**
	 * Sets a message that should be displayed with emphasis for this process
	 * step. If other interaction parameters exist the message will be
	 * displayed
	 * as the first parameter if it doesn't exist already.
	 *
	 * @param message The message or message resource ID or NULL to reset
	 */
	public void setProcessStepMessage(String message) {
		if (message != null) {
			List<RelationType<?>> interactionParams = get(INTERACTION_PARAMS);

			if (!interactionParams.contains(PROCESS_STEP_MESSAGE)) {
				interactionParams.add(0, PROCESS_STEP_MESSAGE);
			}

			setParameter(PROCESS_STEP_MESSAGE, message);
			setUIFlag(HIDE_LABEL, PROCESS_STEP_MESSAGE);
			setUIFlag(WRAP, PROCESS_STEP_MESSAGE);
			setUIProperty(CONTENT_TYPE, ContentType.HTML,
				PROCESS_STEP_MESSAGE);
		} else {
			get(INTERACTION_PARAMS).remove(PROCESS_STEP_MESSAGE);
		}
	}

	/**
	 * A convenience method to set a selection dependency with the property
	 * {@link UserInterfaceProperties#SELECTION_DEPENDENCY}.
	 *
	 * @param param           The parameter to set the dependency for
	 * @param reverseState    TRUE to reverse the selection state of buttons
	 * @param dependentParams The dependent parameters
	 */
	public final void setSelectionDependency(RelationType<?> param,
		boolean reverseState, RelationType<?>... dependentParams) {
		setSelectionDependency(param, reverseState,
			Arrays.asList(dependentParams));
	}

	/**
	 * A convenience method to set the selection dependencies in the UI
	 * property
	 * {@link UserInterfaceProperties#SELECTION_DEPENDENCY}. Empty parameter
	 * lists will be ignored.
	 *
	 * @param param           The parameter to set the dependency for
	 * @param reverseState    TRUE to reverse the selection state of buttons
	 * @param dependentParams The dependent parameters
	 */
	public final void setSelectionDependency(RelationType<?> param,
		boolean reverseState,
		Collection<? extends RelationType<?>> dependentParams) {
		removeUIProperties(param, SELECTION_DEPENDENCY);

		if (dependentParams.size() > 0) {
			addSelectionDependency(param, reverseState, dependentParams);
		}
	}

	/**
	 * A convenience method to set a certain boolean display properties on one
	 * or more process parameters to TRUE.
	 *
	 * @see #setUIProperty(PropertyName, Object, RelationType...)
	 */
	public final void setUIFlag(PropertyName<Boolean> property,
		RelationType<?>... params) {
		setUIProperty(property, Boolean.TRUE, params);
	}

	/**
	 * A convenience method to set a certain boolean display properties on one
	 * or more process parameters to TRUE.
	 *
	 * @see #setUIProperty(PropertyName, Object, Collection)
	 */
	public final void setUIFlag(PropertyName<Boolean> property,
		Collection<? extends RelationType<?>> params) {
		setUIProperty(property, Boolean.TRUE, params);
	}

	/**
	 * Convenience method for the setting of integer properties.
	 *
	 * @see #setUIProperty(PropertyName, Object, Collection)
	 */
	public void setUIProperty(int value, PropertyName<Integer> property,
		Collection<? extends RelationType<?>> params) {
		setUIProperty(property, Integer.valueOf(value), params);
	}

	/**
	 * Convenience method for the setting of integer properties.
	 *
	 * @see #setUIProperty(PropertyName, Object, RelationType...)
	 */
	public void setUIProperty(int value, PropertyName<Integer> property,
		RelationType<?>... params) {
		setUIProperty(property, Integer.valueOf(value), params);
	}

	/**
	 * Sets a certain display property on one or more process parameters. This
	 * method will create the display properties object if necessary.
	 *
	 * @param property The property to set
	 * @param value    The property value
	 * @param params   The parameter to set the property on
	 */
	public <T> void setUIProperty(PropertyName<T> property, T value,
		RelationType<?>... params) {
		setUIProperty(property, value, Arrays.asList(params));
	}

	/**
	 * Sets a certain display property on multiple process parameters. This
	 * method will create the display properties object if necessary.
	 *
	 * @param property The property to set
	 * @param value    The property value
	 * @param params   The parameters to set the property on
	 */
	@SuppressWarnings("unchecked")
	public <T> void setUIProperty(PropertyName<T> property, T value,
		Collection<? extends RelationType<?>> params) {
		// check whether the deprecated input mode needs to be converted into
		// the corresponding event types
		if (property == INTERACTIVE_INPUT_MODE) {
			property = (PropertyName<T>) INTERACTION_EVENT_TYPES;
			value =
				(T) convertInputModeToEventTypes((InteractiveInputMode) value);
		}

		for (RelationType<?> param : params) {
			MutableProperties uiProperties = getUIProperties(param, true);

			uiProperties.setProperty(property, value);

			if (NON_MODIFYING_PROPERTIES.contains(property)) {
				uiProperties.setFlag(StateProperties.PROPERTIES_CHANGED);
			} else {
				markParameterAsModified(param);
			}
		}
	}

	/**
	 * A convenience method to hide or show certain interaction parameter by
	 * changing the UI flag {@link UserInterfaceProperties#HIDDEN} accordingly.
	 *
	 * @param visible The new visible
	 * @param params  The new visible
	 */
	public final void setVisible(boolean visible, RelationType<?>... params) {
		setVisible(visible, Arrays.asList(params));
	}

	/**
	 * A convenience method to hide or show certain interaction parameter by
	 * changing the UI flag {@link UserInterfaceProperties#HIDDEN} accordingly.
	 *
	 * @param visible The new visible
	 * @param params  The new visible
	 */
	public final void setVisible(boolean visible,
		Collection<? extends RelationType<?>> params) {
		if (visible) {
			clearUIFlag(HIDDEN, params);
		} else {
			setUIFlag(HIDDEN, params);
		}
	}

	/**
	 * Spawns a new process that will run independently from the current
	 * process
	 * context. If the process is interactive a process state will be created
	 * and registered in {@link ProcessRelationTypes#SPAWN_PROCESSES} so that
	 * it's interaction can be displayed on the client side.
	 *
	 * @param description The process description
	 * @param initParams  The optional initialization parameters or NULL for
	 *                    none
	 * @throws Exception If the process execution fails
	 */
	public void spawnProcess(ProcessDescription description,
		Relatable initParams) throws Exception {
		ProcessState processState =
			getParameter(PROCESS_EXECUTOR).executeProcess(description,
				initParams);

		if (processState != null) {
			getParameter(SPAWN_PROCESSES).add(processState);
		}
	}

	/**
	 * Stores an entity through the {@link EntityManager} with the current
	 * process user as the change origin.
	 *
	 * @param entity The entity to store
	 * @throws TransactionException If storing the entity fails
	 */
	public void storeEntity(Entity entity) throws TransactionException {
		EntityManager.storeEntity(entity, getProcessUser());
	}

	/**
	 * Removes an entity lock that had been acquired by a successful call to
	 * {@link #lockEntity(Entity)}. This will also remove the associated
	 * cleanup
	 * action.
	 *
	 * @param entity The entity to unlock
	 */
	public final void unlockEntity(Entity entity) {
		removeCleanupAction(
			Process.CLEANUP_KEY_UNLOCK_ENTITY + entity.getGlobalId());
		entity.unlock();
	}

	/**
	 * Marks a parameter as an element of a subordinate panel in this fragment.
	 *
	 * @param panelParams The parameters to mark as panel elements
	 */
	protected void addPanelParameters(Collection<RelationType<?>> panelParams) {
		if (panelParameters == null) {
			panelParameters = new HashSet<>();
		}

		panelParameters.addAll(panelParams);

		// if the parameters have already been added to this fragment remove
		// them because they are already displayed as members of their panel
		get(INTERACTION_PARAMS).removeAll(panelParams);
	}

	/**
	 * Method for subclasses to check and retrieve the value of a certain
	 * parameter. Invokes {@link #getParameter(RelationType)} to retrieve the
	 * value and if it is NULL but the parameter has been marked as
	 * mandatory an
	 * exception will be thrown. Future versions may also check additional
	 * conditions, therefore subclasses should always invoke this methods
	 * instead of {@link #getParameter(RelationType)} unless circumventing the
	 * parameters checks is explicitly needed.
	 *
	 * @param paramType The type of the parameter to check and return
	 * @return The parameter value (may be NULL for non-mandatory parameters)
	 * @throws IllegalStateException If the parameter has been marked as
	 *                               mandatory but is NULL
	 */
	protected <T> T checkParameter(RelationType<T> paramType) {
		T param = getParameter(paramType);

		if (param == null) {
			throwMissingParameterException(paramType);
		}

		return param;
	}

	/**
	 * Executes all actions that have previously been registered through the
	 * method {@link #addCleanupAction(String, Consumer)}.
	 */
	protected void executeCleanupActions() {
		for (String key : cleanupActions.keySet()) {
			Consumer<ProcessFragment> action = cleanupActions.get(key);

			try {
				action.accept(this);
			} catch (Exception e) {
				Log.errorf(e, "Fragment cleanup action failed: %s", key);
			}
		}

		cleanupActions.clear();
	}

	/**
	 * Returns the absolute path and file name for a relative file path. This
	 * will only work if a session manager reference is available from the
	 * process parameter {@link DataRelationTypes#SESSION_MANAGER}. Otherwise
	 * the input path is returned unchanged.
	 *
	 * @param fileName The relative path to the file
	 * @return The absolute file path
	 */
	protected String getAbsoluteFilePath(String fileName) {
		SessionManager sessionManager =
			getParameter(DataRelationTypes.SESSION_MANAGER);

		if (sessionManager != null) {
			fileName = sessionManager.getAbsoluteFileName(fileName);
		}

		return fileName;
	}

	/**
	 * Creates a temporary list type with a random unique name and sets the
	 * {@link UserInterfaceProperties#RESOURCE_ID} to the given string
	 * parameter.
	 *
	 * @param resourceId  The resource id to use.
	 * @param elementType The collection element data type
	 * @return a temporary list type with a random unique name and sets the
	 * {@link UserInterfaceProperties#RESOURCE_ID} to the given string
	 * parameter.
	 */
	protected <T> RelationType<List<T>> getNamedTmpListType(String resourceId,
		Class<? super T> elementType) {
		String releationTypeName = resourceId + UUID.randomUUID();

		RelationType<List<T>> temporaryListType =
			getTemporaryListType(releationTypeName, elementType);

		setUIProperty(RESOURCE_ID, resourceId, temporaryListType);

		return temporaryListType;
	}

	/**
	 * Creates a temporary parameter type with a random unique name and sets
	 * the
	 * {@link UserInterfaceProperties#RESOURCE_ID} to the given string
	 * parameter.
	 *
	 * @param resourceId The resource id to use.
	 * @param datatype   elementType The collection element data type
	 * @return a temporary parameter type with a random unique name and sets
	 * the
	 * {@link UserInterfaceProperties#RESOURCE_ID} to the given string
	 * parameter.
	 */
	protected <T> RelationType<T> getNamedTmpParameterType(String resourceId,
		Class<? super T> datatype) {
		String relationTypeName = resourceId + UUID.randomUUID();

		RelationType<T> temporaryParameterType =
			getTemporaryParameterType(relationTypeName, datatype);

		setUIProperty(RESOURCE_ID, resourceId, temporaryParameterType);

		return temporaryParameterType;
	}

	/**
	 * A helper method for subclasses that returns the selection index for a
	 * selection data element parameter.
	 *
	 * @param selectionParam The selection data element parameter type
	 * @return The selection index for the parameter (-1 for no selection)
	 */
	protected int getSelectionIndex(
		RelationType<SelectionDataElement> selectionParam) {
		SelectionDataElement selectionElement = getParameter(selectionParam);
		int selection = -1;

		if (selectionElement != null) {
			selection = Integer.parseInt(selectionElement.getValue());
		}

		return selection;
	}

	/**
	 * Returns the subordinate fragments of this instance.
	 *
	 * @return The subordinate fragments
	 */
	protected final Collection<InteractionFragment> getSubFragments() {
		return subFragments.values();
	}

	/**
	 * Returns an integer ID for the automatic naming of process parameters.
	 * The
	 * default implementation return the result of
	 * {@link Process#getNextParameterId()}.
	 *
	 * @return A new temporary parameter ID
	 */
	protected int getTemporaryParameterId() {
		return getProcess().getNextParameterId();
	}

	/**
	 * Returns the name for a temporary parameter relation type that is derived
	 * from a certain base name. The name will be local to the current
	 * fragment.
	 *
	 * @param baseName The temporary parameter base name
	 * @return The temporary parameter name
	 */
	protected String getTemporaryParameterName(String baseName) {
		StringBuilder paramName =
			new StringBuilder(getTemporaryParameterPackage());

		paramName.append('.');

		if (baseName == null) {
			paramName
				.append(DataElement.ANONYMOUS_ELEMENT_PREFIX)
				.append(getTemporaryParameterId());
		} else {
			if (Character.isDigit(baseName.charAt(0))) {
				paramName.append('_');
			}

			paramName.append(TextConvert
				.uppercaseIdentifier(baseName)
				.replaceAll("[.-]", "_"));
		}

		return paramName.toString();
	}

	/**
	 * Returns the package name for temporary parameter types created by the
	 * method {@link #getTemporaryParameterType(String, Class)}. Subclasses may
	 * override this method to modify the default which creates a package name
	 * that is unique for the current process instance (but will be shared by
	 * all process steps). The package name must be returned without leading or
	 * trailing dots.
	 *
	 * @return The package name for temporary parameter types
	 */
	@SuppressWarnings("boxing")
	protected String getTemporaryParameterPackage() {
		if (fragmentParamPackage == null) {
			fragmentParamPackage =
				String.format("P%d.F%d", getProcess().getId(),
					getFragmentId());
		}

		return fragmentParamPackage;
	}

	/**
	 * Returns a temporary parameter relation type that references a
	 * {@link Set}
	 * with a certain element datatype. The parameter will have an empty set as
	 * it's initial value.
	 *
	 * @param name        The name of the parameter
	 * @param elementType The set element datatype
	 * @param ordered     TRUE for a set that keeps the order of it's elements
	 * @return The temporary set parameter type
	 * @see #getTemporaryParameterType(String, Class)
	 */
	protected <T> RelationType<Set<T>> getTemporarySetType(String name,
		Class<? super T> elementType, boolean ordered) {
		name = getTemporaryParameterName(name);

		@SuppressWarnings("unchecked")
		RelationType<Set<T>> param =
			(RelationType<Set<T>>) RelationType.valueOf(name);

		if (param == null) {
			param = newSetType(name, elementType, true, ordered);

			getParameter(TEMPORARY_PARAM_TYPES).add(param);
		} else {
			assert param.getTargetType() == Set.class &&
				param.get(ELEMENT_DATATYPE) == elementType;
		}

		getProcess().registerTemporaryParameterType(param);

		return param;
	}

	/**
	 * Creates a new data set data element with chart data and sets the
	 * corresponding process parameter.
	 *
	 * @param targetParam     The parameter to store the chart data element in
	 * @param dataSet         The chart data
	 * @param chartType       The chart type
	 * @param legendPosition  The legend position
	 * @param backgroundColor The chart background color
	 * @param b3D             TRUE for a 3D chart
	 */
	protected void initChartParameter(
		RelationType<DataSetDataElement> targetParam, DataSet<?> dataSet,
		ChartType chartType, LegendPosition legendPosition,
		String backgroundColor, boolean b3D) {
		DataSetDataElement chartElement =
			new DataSetDataElement(targetParam.getName(), dataSet, chartType,
				legendPosition, backgroundColor, b3D);

		setParameter(targetParam, chartElement);
	}

	/**
	 * Initializes a chart parameter that displays integer counts for certain
	 * elements. The chart data is defined as a mapping from string names that
	 * are used as labels to the corresponding integer count values.
	 *
	 * @param targetParam     The target {@link DataSet} parameter
	 * @param dataMap         The mapping from data labels to counts
	 * @param chartType       The type of chart to display
	 * @param rowAxisLabel    The label for the data row (x) axis
	 * @param valueAxisLabel  The label for the data value (y) axis
	 * @param legendPosition  The legend position
	 * @param backgroundColor The chart background color
	 * @param b3D             TRUE for a 3D chart
	 */
	protected void initCountChartParameter(
		RelationType<DataSetDataElement> targetParam,
		Map<String, Integer> dataMap, ChartType chartType, String rowAxisLabel,
		String valueAxisLabel, LegendPosition legendPosition,
		String backgroundColor, boolean b3D) {
		IntDataSet dataSet =
			new IntDataSet(null, rowAxisLabel, valueAxisLabel, "");

		dataMap = CollectionUtil.sort(dataMap);

		for (Entry<String, Integer> row : dataMap.entrySet()) {
			dataSet.addRow(row.getKey(), row.getValue());
		}

		initChartParameter(targetParam, dataSet, chartType, legendPosition,
			backgroundColor, b3D);
	}

	/**
	 * Initializes the parameter {@link ProcessRelationTypes#PROGRESS} to be
	 * displayed as a progress indicator. The maximum progress value must be
	 * set
	 * in the {@link ProcessRelationTypes#PROGRESS_MAXIMUM} parameter or
	 * else an
	 * exception will be thrown.
	 */
	@SuppressWarnings("boxing")
	protected void initProgressParameter() {
		annotateParameter(PROGRESS, null, MINIMUM, 0);
		annotateParameter(PROGRESS, null, MAXIMUM,
			checkParameter(PROGRESS_MAXIMUM));

		setUIProperty(CONTENT_TYPE, ContentType.PROGRESS, PROGRESS);
	}

	/**
	 * Checks whether a certain parameter is contained in a subordinate
	 * parameter list of a panel instead of directly in this fragment.
	 *
	 * @param param The parameter relation type to check
	 * @return TRUE if the given parameter is an element of a panel
	 */
	protected boolean isPanelParameter(RelationType<?> param) {
		return panelParameters != null && panelParameters.contains(param);
	}

	/**
	 * Performs the actual parameter validation for this process element by
	 * executing the validation functions on the parameter values. The argument
	 * is a mapping from parameter relation types to their respective
	 * validation
	 * functions. The returned map contains a mapping from invalid
	 * parameters to
	 * the corresponding error message. The map will never be empty and may be
	 * modified by the receiver.
	 *
	 * @param validations The mapping from parameters to validation functions
	 * @return A mapping for all invalid parameters to the corresponding error
	 * messages (may be empty but will never be NULL)
	 */
	@SuppressWarnings("unchecked")
	protected Map<RelationType<?>, String> performParameterValidations(
		Map<RelationType<?>, Function<?, String>> validations) {
		Map<RelationType<?>, String> invalidParams =
			new HashMap<RelationType<?>, String>();

		for (Entry<RelationType<?>, Function<?, String>> entry :
			validations.entrySet()) {
			RelationType<Object> param = (RelationType<Object>) entry.getKey();
			Function<Object, String> validate =
				(Function<Object, String>) entry.getValue();

			Object paramValue = getParameter(param);
			String invalidInfo;

			try {
				invalidInfo = validate.evaluate(paramValue);
			} catch (Exception e) {
				invalidInfo = "ParamValidationFailed";
			}

			if (invalidInfo != null) {
				invalidParams.put(param, invalidInfo);
			}
		}

		return invalidParams;
	}

	/**
	 * Removes a temporary parameter type that has previously been created by
	 * invoking {@link #getTemporaryParameterType(String, Class)}.
	 *
	 * @param tempParam The temporary parameter type
	 */
	protected void removeTemporaryParameterType(RelationType<?> tempParam) {
		getProcess().unregisterTemporaryParameterType(tempParam, true);
	}

	/**
	 * Sets two date parameters to span a certain date period around a certain
	 * date. The resulting start date will be inclusive, the next date
	 * exclusive
	 * of the requested date range.
	 *
	 * @param aroundDate     The date to calculate the period around
	 * @param startDateParam The parameter type for the start date
	 * @param endDateParam   The parameter type for the end date
	 * @param calendarField  The calendar field to calculate the period for
	 * @param periodSize     The size of the period
	 */
	protected void setDatePeriod(Date aroundDate,
		RelationType<Date> startDateParam, RelationType<Date> endDateParam,
		int calendarField, int periodSize) {
		Calendar calendar = Calendar.getInstance();

		calendar.setTime(aroundDate);
		CalendarFunctions.clearTime(calendar);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		int currentValue = calendar.get(calendarField);
		int boundaryField = -1;

		if (calendarField == Calendar.WEEK_OF_YEAR) {
			boundaryField = Calendar.DAY_OF_WEEK;
		} else if (calendarField == Calendar.WEEK_OF_MONTH) {
			boundaryField = Calendar.DAY_OF_WEEK_IN_MONTH;
		} else if (calendarField == Calendar.MONTH) {
			boundaryField = Calendar.DAY_OF_MONTH;
		} else if (calendarField == Calendar.YEAR) {
			boundaryField = Calendar.DAY_OF_YEAR;
		}

		// convert other fields than MONTH to zero-based value
		if (calendarField != Calendar.MONTH) {
			currentValue = ((currentValue - 1) / periodSize * periodSize) + 1;
		} else {
			currentValue = currentValue / periodSize * periodSize;
		}

		calendar.set(calendarField, currentValue);

		if (boundaryField >= 0) {
			calendar.set(boundaryField, 1);
		}

		Date startDate = calendar.getTime();

		calendar.add(calendarField, periodSize);

		Date endDate = calendar.getTime();

		setParameter(startDateParam, startDate);
		setParameter(endDateParam, endDate);
	}

	/**
	 * Sets the parameter {@link ProcessRelationTypes#PROGRESS_INDICATOR} to
	 * the
	 * current progress values.
	 *
	 * @see #initProgressParameter()
	 */
	protected void setProgressIndicator() {
		setParameter(PROGRESS_INDICATOR,
			String.format(getParameter(PROGRESS_INDICATOR_TEMPLATE),
				getParameter(PROGRESS), getParameter(PROGRESS_MAXIMUM)));
	}

	/**
	 * Throws a runtime exception that signals a missing process parameter.
	 *
	 * @param paramType The relation type of the missing parameter
	 */
	protected <T> void throwMissingParameterException(
		RelationType<T> paramType) {
		throw new IllegalStateException(
			String.format("Parameter %s not set", paramType));
	}

	/**
	 * Converts (deprecated) {@link InteractiveInputMode} values into a set of
	 * {@link InteractionEventType InteractionEventTypes}.
	 *
	 * @param mode The interactive input mode to convert
	 * @return The set of event types
	 */
	private Set<InteractionEventType> convertInputModeToEventTypes(
		InteractiveInputMode mode) {
		EnumSet<InteractionEventType> eventTypes =
			EnumSet.noneOf(InteractionEventType.class);

		switch (mode) {
			case ACTION:
				eventTypes.add(InteractionEventType.ACTION);
				break;

			case CONTINUOUS:
				eventTypes.add(InteractionEventType.UPDATE);
				break;

			case BOTH:
				eventTypes.add(InteractionEventType.ACTION);
				eventTypes.add(InteractionEventType.UPDATE);
				break;
		}

		return eventTypes;
	}

	/**
	 * Internal implementation to initialize an interactive enum parameter.
	 *
	 * @param param         The parameter to initialize
	 * @param intialValue   The initial parameter value or NULL for none
	 * @param listStyle     The style in which to display the list of values
	 * @param allowedValues The allowed enum values (NULL or empty for all
	 *                      values of the given enum)
	 */
	@SuppressWarnings("unchecked")
	private <E, T> void setInteractiveImpl(RelationType<T> param,
		T intialValue,
		ListStyle listStyle, Collection<E> allowedValues) {
		if (allowedValues == null || allowedValues.size() == 0) {
			Collection<E> existingValues =
				(Collection<E>) getAllowedValues(param);

			if (existingValues == null || existingValues.size() == 0) {
				Class<?> datatype = param.getTargetType();

				if (Collection.class.isAssignableFrom(datatype)) {
					datatype = param.get(ELEMENT_DATATYPE);
				}

				if (datatype.isEnum()) {
					allowedValues =
						Arrays.asList((E[]) datatype.getEnumConstants());
				}
			} else {
				allowedValues = existingValues;
			}
		}

		annotateParameter(param, intialValue, ALLOWED_VALUES, allowedValues);
		setUIProperty(INTERACTIVE_INPUT_MODE, getInputMode(listStyle), param);
		setUIProperty(LIST_STYLE, listStyle, param);
	}
}
