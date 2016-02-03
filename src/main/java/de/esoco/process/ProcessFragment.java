//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.data.element.DataElementList.ListDisplayMode;
import de.esoco.data.element.DataSetDataElement;
import de.esoco.data.element.DataSetDataElement.ChartType;
import de.esoco.data.element.DataSetDataElement.LegendPosition;
import de.esoco.data.element.SelectionDataElement;
import de.esoco.data.storage.StorageAdapter;
import de.esoco.data.storage.StorageAdapterId;
import de.esoco.data.storage.StorageAdapterRegistry;

import de.esoco.entity.Configuration;
import de.esoco.entity.Entity;

import de.esoco.history.HistoryRecord;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.function.AbstractAction;
import de.esoco.lib.expression.function.CalendarFunctions;
import de.esoco.lib.manage.TransactionException;
import de.esoco.lib.model.DataSet;
import de.esoco.lib.model.IntDataSet;
import de.esoco.lib.property.HasProperties;
import de.esoco.lib.property.MutableProperties;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.StringProperties;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.property.UserInterfaceProperties.ContentType;
import de.esoco.lib.property.UserInterfaceProperties.InteractiveInputMode;
import de.esoco.lib.property.UserInterfaceProperties.ListStyle;
import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextUtil;

import de.esoco.process.step.Interaction;
import de.esoco.process.step.InteractionFragment;

import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StorageException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;

import static de.esoco.data.DataRelationTypes.STORAGE_ADAPTER_ID;
import static de.esoco.data.DataRelationTypes.STORAGE_ADAPTER_REGISTRY;

import static de.esoco.entity.EntityRelationTypes.DISPLAY_PROPERTIES;

import static de.esoco.lib.expression.Functions.doIf;
import static de.esoco.lib.expression.Functions.doIfElse;
import static de.esoco.lib.expression.Functions.value;
import static de.esoco.lib.expression.Predicates.isNull;
import static de.esoco.lib.expression.Predicates.lessThan;
import static de.esoco.lib.property.UserInterfaceProperties.COLUMNS;
import static de.esoco.lib.property.UserInterfaceProperties.CONTENT_TYPE;
import static de.esoco.lib.property.UserInterfaceProperties.DISABLED;
import static de.esoco.lib.property.UserInterfaceProperties.DISABLED_ELEMENTS;
import static de.esoco.lib.property.UserInterfaceProperties.HIDDEN;
import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.HTML_HEIGHT;
import static de.esoco.lib.property.UserInterfaceProperties.HTML_WIDTH;
import static de.esoco.lib.property.UserInterfaceProperties.INTERACTIVE_INPUT_MODE;
import static de.esoco.lib.property.UserInterfaceProperties.LIST_STYLE;
import static de.esoco.lib.property.UserInterfaceProperties.RESOURCE_ID;
import static de.esoco.lib.property.UserInterfaceProperties.ROWS;
import static de.esoco.lib.property.UserInterfaceProperties.SELECTION_DEPENDENCY;
import static de.esoco.lib.property.UserInterfaceProperties.SELECTION_DEPENDENCY_REVERSE_PREFIX;
import static de.esoco.lib.property.UserInterfaceProperties.TOOLTIP;
import static de.esoco.lib.property.UserInterfaceProperties.WRAP;

import static de.esoco.process.ProcessRelationTypes.ALLOWED_VALUES;
import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_FILL;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTIVE_INPUT_PARAM;
import static de.esoco.process.ProcessRelationTypes.IS_PANEL_ELEMENT;
import static de.esoco.process.ProcessRelationTypes.ORIGINAL_RELATION_TYPE;
import static de.esoco.process.ProcessRelationTypes.PROCESS_STEP_INFO;
import static de.esoco.process.ProcessRelationTypes.PROCESS_STEP_MESSAGE;
import static de.esoco.process.ProcessRelationTypes.PROGRESS;
import static de.esoco.process.ProcessRelationTypes.PROGRESS_INDICATOR;
import static de.esoco.process.ProcessRelationTypes.PROGRESS_INDICATOR_TEMPLATE;
import static de.esoco.process.ProcessRelationTypes.PROGRESS_MAXIMUM;
import static de.esoco.process.ProcessRelationTypes.TEMPORARY_PARAM_TYPES;

import static org.obrel.core.RelationTypes.newRelationType;
import static org.obrel.type.MetaTypes.ELEMENT_DATATYPE;
import static org.obrel.type.StandardTypes.MAXIMUM;
import static org.obrel.type.StandardTypes.MINIMUM;


/********************************************************************
 * The base class for all fragments of a process at runtime.
 *
 * @author eso
 */
public abstract class ProcessFragment extends ProcessElement
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	private static final Set<PropertyName<?>> NON_MODIFYING_PROPERTIES =
		CollectionUtil.<PropertyName<?>>setOf(DISABLED, HIDDEN);

	//~ Instance fields --------------------------------------------------------

	private Map<RelationType<List<RelationType<?>>>, InteractionFragment> aSubFragments =
		new LinkedHashMap<>();

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Modifies a date by a certain calendar field and returns a new date with
	 * the update value.
	 *
	 * @param  rDate          The date to modify (will not be changed)
	 * @param  nCalendarField The calendar field to modify
	 * @param  nAddValue      The update value for the calendar field
	 *
	 * @return A new date instance containing the updated value
	 */
	public static Date changeDate(Date rDate, int nCalendarField, int nAddValue)
	{
		Calendar rCalendar = Calendar.getInstance();

		rCalendar.setTime(rDate);
		rCalendar.add(nCalendarField, nAddValue);

		return rCalendar.getTime();
	}

	/***************************************
	 * Return the interactive input mode for a certain list style.
	 *
	 * @param  eListStyle The list style
	 *
	 * @return The interactive input mode
	 */
	public static InteractiveInputMode getInputMode(ListStyle eListStyle)
	{
		InteractiveInputMode eInputMode = null;

		switch (eListStyle)
		{
			case LIST:
			case DROP_DOWN:
			case EDITABLE:
				eInputMode = InteractiveInputMode.CONTINUOUS;
				break;

			case DISCRETE:
			case IMMEDIATE:
				eInputMode = InteractiveInputMode.ACTION;
				break;
		}

		return eInputMode;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the process that this fragment is associated with.
	 *
	 * @return The process of this fragment
	 */
	public abstract Process getProcess();

	/***************************************
	 * Adds an invisible fill parameter to a layout.
	 *
	 * @param  bFillWidth  TRUE to fill the remaining layout width
	 * @param  bFillHeight TRUE to fill the remaining layout height
	 *
	 * @return The fill parameter that has been added by this method
	 */
	public RelationType<?> addLayoutFiller(
		boolean bFillWidth,
		boolean bFillHeight)
	{
		RelationType<String> rFillParam = INTERACTION_FILL;

		if (!hasInteractionParameter(rFillParam))
		{
			addDisplayParameters(rFillParam);
		}

		setUIFlag(HIDE_LABEL, rFillParam);
		setUIProperty(TOOLTIP, "", rFillParam);

		if (bFillWidth)
		{
			setUIProperty(HTML_WIDTH, "100%", rFillParam);
		}

		if (bFillHeight)
		{
			setUIProperty(HTML_HEIGHT, "100%", rFillParam);
		}

		return rFillParam;
	}

	/***************************************
	 * Configures a parameter to be displayed in a separate panel.
	 *
	 * @param rPanelParam         The data element list parameter to be
	 *                            displayed as a panel
	 * @param eDisplayMode        The display mode for the panel
	 * @param rPanelContentParams The list of parameters to be displayed in the
	 *                            panel
	 */
	public void addPanel(
		RelationType<List<RelationType<?>>> rPanelParam,
		ListDisplayMode						eDisplayMode,
		List<RelationType<?>>				rPanelContentParams)
	{
		setParameter(rPanelParam, rPanelContentParams);
		setListDisplayMode(eDisplayMode, rPanelParam);

		// mark the content parameter relations as panel elements so that they
		// can be detected as subordinate parameters
		for (RelationType<?> rContentParam : rPanelContentParams)
		{
			Relation<?> rParamRelation = getParameterRelation(rContentParam);

			if (rParamRelation == null)
			{
				rParamRelation = setParameter(rContentParam, null);
			}

			rParamRelation.set(IS_PANEL_ELEMENT);
		}
	}

	/***************************************
	 * Configures a parameter to be displayed in a panel with 2 or 3 segments.
	 * Either the first or the last parameter may be NULL but not both and not
	 * the center parameter. For a vertical orientation of the panel the UI
	 * property {@link UserInterfaceProperties#VERTICAL} should be given as a
	 * flag.
	 *
	 * @param rPanelParam  The data element list parameter to be displayed as a
	 *                     panel
	 * @param rFirstParam  The first parameter in the panel or NULL for none
	 * @param rCenterParam rFirstParam The center (main) parameter in the panel
	 * @param rLastParam   The last parameter in the panel or NULL for none
	 * @param bResizable   TRUE to make the panel resizable as a split panel
	 * @param rUIFlags     Boolean properties to be set on the panel parameter
	 *                     (e.g. {@link UserInterfaceProperties#VERTICAL})
	 */
	@SafeVarargs
	public final void addPanel(RelationType<List<RelationType<?>>> rPanelParam,
							   RelationType<?>					   rFirstParam,
							   RelationType<?>					   rCenterParam,
							   RelationType<?>					   rLastParam,
							   boolean							   bResizable,
							   PropertyName<Boolean>... 		   rUIFlags)
	{
		List<RelationType<?>> rPanelContentParams = new ArrayList<>(3);

		if (rFirstParam != null)
		{
			rPanelContentParams.add(rFirstParam);
		}

		rPanelContentParams.add(rCenterParam);

		if (rLastParam != null)
		{
			rPanelContentParams.add(rLastParam);
		}

		addPanel(rPanelParam,
				 bResizable ? ListDisplayMode.SPLIT : ListDisplayMode.PLAIN,
				 rPanelContentParams);

		for (PropertyName<Boolean> rFlag : rUIFlags)
		{
			setUIFlag(rFlag, rPanelParam);
		}
	}

	/***************************************
	 * Appends another string below the current process step message.
	 *
	 * @param sMessage The message string or resource key to append
	 */
	public void addProcessStepMessage(String sMessage)
	{
		String sStepMessage = getParameter(PROCESS_STEP_MESSAGE);

		if (sStepMessage == null || sStepMessage.length() == 0)
		{
			sMessage = sStepMessage;
		}
		else if (sStepMessage.startsWith("$$"))
		{
			sMessage = String.format("%s<br><br>{%s}", sStepMessage, sMessage);
		}
		else
		{
			sMessage =
				String.format("$${%s}<br><br>{%s}", sStepMessage, sMessage);
		}

		setProcessStepMessage(sMessage);
	}

	/***************************************
	 * A convenience method to add selection dependencies to the UI property
	 * {@link UserInterfaceProperties#SELECTION_DEPENDENCY}.
	 *
	 * @param rParam           The parameter to set the dependency for
	 * @param bReverseState    TRUE to reverse the selection state of buttons
	 * @param rDependentParams The dependent parameters
	 */
	public final void addSelectionDependency(
		RelationType<?>    rParam,
		boolean			   bReverseState,
		RelationType<?>... rDependentParams)
	{
		addSelectionDependency(rParam,
							   bReverseState,
							   Arrays.asList(rDependentParams));
	}

	/***************************************
	 * A convenience method to add selection dependencies to the UI property
	 * {@link UserInterfaceProperties#SELECTION_DEPENDENCY}. Empty parameter
	 * lists will be ignored.
	 *
	 * @param rParam           The parameter to set the dependency for
	 * @param bReverseState    TRUE to reverse the selection state of buttons
	 * @param rDependentParams The dependent parameters
	 */
	public final void addSelectionDependency(
		RelationType<?>						  rParam,
		boolean								  bReverseState,
		Collection<? extends RelationType<?>> rDependentParams)
	{
		if (rDependentParams.size() > 0)
		{
			StringBuilder aDependencies = new StringBuilder();

			String sCurrentDependencies =
				getUIProperty(SELECTION_DEPENDENCY, rParam);

			if (sCurrentDependencies != null &&
				sCurrentDependencies.length() > 0)
			{
				aDependencies.append(sCurrentDependencies).append(',');
			}

			for (RelationType<?> rDependentParam : rDependentParams)
			{
				if (bReverseState)
				{
					aDependencies.append(SELECTION_DEPENDENCY_REVERSE_PREFIX);
				}

				aDependencies.append(rDependentParam.getName());
				aDependencies.append(',');
			}

			aDependencies.setLength(aDependencies.length() - 1);

			setUIProperty(SELECTION_DEPENDENCY,
						  aDependencies.toString(),
						  rParam);
		}
	}

	/***************************************
	 * Configures a parameter to be displayed in a separate stack panel.
	 *
	 * @param rPanelParam         The data element list parameter to be
	 *                            displayed as a stack panel
	 * @param rPanelContentParams The parameters to be displayed in the panel
	 */
	public void addStackPanel(
		RelationType<List<RelationType<?>>> rPanelParam,
		RelationType<?>... 					rPanelContentParams)
	{
		addPanel(rPanelParam,
				 ListDisplayMode.STACK,
				 Arrays.asList(rPanelContentParams));
	}

	/***************************************
	 * Adds a subordinate fragment that handles a part of this interaction and
	 * is displayed in a certain parameter. The fragment parameter will not be
	 * added to the display parameters on this instance, this must be done
	 * separately (e.g. with {@link #addDisplayParameters(RelationType...)} to
	 * allow the invoking code to separate fragment creation from fragment
	 * placement.
	 *
	 * @param rFragmentParam The interactive process parameter in which the
	 *                       fragment will be displayed
	 * @param rSubFragment   The fragment to add
	 */
	public void addSubFragment(
		RelationType<List<RelationType<?>>> rFragmentParam,
		InteractionFragment					rSubFragment)
	{
		if (aSubFragments.containsKey(rFragmentParam))
		{
			InteractionFragment rPreviousFragment =
				aSubFragments.remove(rFragmentParam);

			rPreviousFragment.cleanup();
		}

		rSubFragment.attach((Interaction) getProcessStep(), rFragmentParam);
		aSubFragments.put(rFragmentParam, rSubFragment);

		setParameter(rFragmentParam, rSubFragment.getInteractionParameters());

		// fragment parameters must be marked as input for fragments that
		get(INPUT_PARAMS).add(rFragmentParam);
		rSubFragment.markFragmentInputParams();
	}

	/***************************************
	 * Configures a parameter to be displayed in a separate tab panel.
	 *
	 * @param rPanelParam         The data element list parameter to be
	 *                            displayed as a tab panel
	 * @param rPanelContentParams The parameters to be displayed in the panel
	 */
	public void addTabPanel(
		RelationType<List<RelationType<?>>> rPanelParam,
		RelationType<?>... 					rPanelContentParams)
	{
		addPanel(rPanelParam,
				 ListDisplayMode.TABS,
				 Arrays.asList(rPanelContentParams));
	}

	/***************************************
	 * Annotates a process parameter for a storage query.
	 *
	 * @param rParam      The parameter type
	 * @param pQuery      The query predicate
	 * @param pSortOrder  The sort order predicate of the query or NULL for the
	 *                    default sort order
	 * @param rAttributes The entity attributes to query
	 */
	public <E extends Entity> void annotateForEntityQuery(
		RelationType<? super E>		 rParam,
		QueryPredicate<E>			 pQuery,
		Predicate<? super Entity>    pSortOrder,
		List<Function<? super E, ?>> rAttributes)
	{
		Relation<? super E> rRelation = getParameterRelation(rParam);

		if (rRelation == null)
		{
			rRelation = setParameter(rParam, null);
		}
		else
		{
			markParameterAsModified(rParam);
		}

		EntityProcessDefinition.annotateForEntityQuery(rRelation,
													   pQuery,
													   pSortOrder,
													   rAttributes);
	}

	/***************************************
	 * Annotates a process parameter for a storage query.
	 *
	 * @param rParam      The parameter type
	 * @param pQuery      The query predicate
	 * @param pSortOrder  The sort order predicate of the query or NULL for the
	 *                    default sort order
	 * @param rAttributes The entity attributes to query
	 */
	public <E extends Entity> void annotateForEntityQuery(
		RelationType<? super E>   rParam,
		QueryPredicate<E>		  pQuery,
		Predicate<? super Entity> pSortOrder,
		RelationType<?>... 		  rAttributes)
	{
		annotateForEntityQuery(rParam,
							   pQuery,
							   pSortOrder,
							   Arrays.<Function<? super E, ?>>asList(rAttributes));
	}

	/***************************************
	 * Sets an annotation on a certain process parameter. If the parameter
	 * hasn't been set before it will be set to the given initial value to
	 * create a relation that can be annotated.
	 *
	 * @param rParam          The parameter to annotate
	 * @param rInitialValue   The initial value if the parameter doesn't exist
	 * @param rAnnotationType The relation type of the annotation
	 * @param rValue          The annotation value
	 *
	 * @see   #removeParameterAnnotation(RelationType, RelationType)
	 */
	public <T, A> void annotateParameter(RelationType<T> rParam,
										 T				 rInitialValue,
										 RelationType<A> rAnnotationType,
										 A				 rValue)
	{
		Process     rProcess  = getProcess();
		Relation<?> rRelation = rProcess.getRelation(rParam);

		if (rRelation == null)
		{
			if (rInitialValue == null)
			{
				rInitialValue = rParam.initialValue(rProcess.getContext());
			}

			rRelation = setParameter(rParam, rInitialValue);
		}
		else
		{
			markParameterAsModified(rParam);

			if (rAnnotationType == ALLOWED_VALUES)
			{
				setUIFlag(DataElement.ALLOWED_VALUES_CHANGED, rParam);
			}
		}

		rRelation.annotate(rAnnotationType, rValue);
	}

	/***************************************
	 * Applies multiple derived parameters to a certain target. See the method
	 * {@link #applyDerivedParameter(RelationType, Relatable)} for details.
	 *
	 * @param rDerivedParams The derived parameters
	 * @param rTarget        The target object to set the derived parameters on
	 */
	public void applyDerivedParameters(
		List<RelationType<?>> rDerivedParams,
		Relatable			  rTarget)
	{
		for (RelationType<?> rParam : rDerivedParams)
		{
			applyDerivedParameter(rParam, rTarget);
		}
	}

	/***************************************
	 * Sets a certain extra attribute on an entity if it is available as a
	 * parameter in the process of this fragment. NULL values, empty strings,
	 * and empty collections will be ignored.
	 *
	 * @param  rEntity    The entity to set the extra attribute on
	 * @param  rExtraAttr The extra attribute type
	 *
	 * @throws StorageException If setting the extra attribute fails
	 */
	public <T> void applyExtraAttribute(
		Entity			rEntity,
		RelationType<T> rExtraAttr) throws StorageException
	{
		T rValue = getParameter(rExtraAttr);

		if (rValue != null)
		{
			if (!((rValue instanceof String && ((String) rValue).isEmpty()) ||
				  (rValue instanceof Collection &&
				   ((Collection<?>) rValue).isEmpty())))
			{
				rEntity.setExtraAttribute(rExtraAttr, rValue);
			}
		}
	}

	/***************************************
	 * Convenience method to set a certain boolean display properties on one or
	 * more process parameters to FALSE.
	 *
	 * @see #setUIProperty(PropertyName, Object, RelationType...)
	 */
	public final void clearUIFlag(
		PropertyName<Boolean> rProperty,
		RelationType<?>...    rParams)
	{
		clearUIFlag(rProperty, Arrays.asList(rParams));
	}

	/***************************************
	 * Convenience method to set a certain boolean display properties on one or
	 * more process parameters to FALSE.
	 *
	 * @see #setUIProperty(PropertyName, Object, Collection)
	 */
	public final void clearUIFlag(
		PropertyName<Boolean>				  rProperty,
		Collection<? extends RelationType<?>> rParams)
	{
		setUIProperty(rProperty, Boolean.FALSE, rParams);
	}

	/***************************************
	 * Collects multiple derived parameters from a certain source. See the
	 * method {@link #collectDerivedParameter(Relatable, RelationType, boolean)}
	 * for details.
	 *
	 * @param rSource        The source object to read the the derived
	 *                       parameters from
	 * @param rDerivedParams The derived parameters
	 * @param bSkipExisting  If TRUE only parameters that don't exist already
	 *                       will be collected from the source object
	 */
	public void collectDerivedParameters(Relatable			   rSource,
										 List<RelationType<?>> rDerivedParams,
										 boolean			   bSkipExisting)
	{
		for (RelationType<?> rParam : rDerivedParams)
		{
			collectDerivedParameter(rSource, rParam, bSkipExisting);
		}
	}

	/***************************************
	 * Deletes several parameters from the process.
	 *
	 * @param rParamTypes The types of the parameters to delete
	 */
	public final void deleteParameters(RelationType<?>... rParamTypes)
	{
		Process rProcess = getProcess();

		for (RelationType<?> rParamType : rParamTypes)
		{
			rProcess.deleteRelation(rParamType);
		}
	}

	/***************************************
	 * Convenience method that applies to all elements of an enum.
	 *
	 * @see #disableElements(RelationType, Collection)
	 */
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void disableElements(
		RelationType<E> rEnumParam,
		E... 			rDisabledElements)
	{
		disableElements(rEnumParam, null, Arrays.asList(rDisabledElements));
	}

	/***************************************
	 * Sets an enum parameter's user interface property {@link
	 * UserInterfaceProperties#DISABLED_ELEMENTS DISABLED_ELEMENTS} for a
	 * certain set of enum values.
	 *
	 * @param rEnumParam        The parameter to disabled the elements of
	 * @param rAllElements      All elements that will be displayed (NULL for
	 *                          the allowed values of the parameter enum)
	 * @param rDisabledElements The elements to disable (NULL or none to enable
	 *                          all)
	 */
	public <E extends Enum<E>> void disableElements(
		RelationType<E> rEnumParam,
		Collection<E>   rAllElements,
		Collection<E>   rDisabledElements)
	{
		if (rDisabledElements != null && rDisabledElements.size() > 0)
		{
			if (rAllElements == null)
			{
				rAllElements = getAllowedValues(rEnumParam);

				if (rAllElements == null)
				{
					@SuppressWarnings("unchecked")
					E[] rEnumConstants =
						(E[]) rEnumParam.getTargetType().getEnumConstants();

					rAllElements = Arrays.asList(rEnumConstants);
				}
			}

			StringBuilder aDisabledElements = new StringBuilder();
			List<E>		  aIndexedElements  = new ArrayList<E>(rAllElements);

			for (E eElement : rDisabledElements)
			{
				int nIndex = aIndexedElements.indexOf(eElement);

				if (nIndex >= 0)
				{
					aDisabledElements.append('(');
					aDisabledElements.append(nIndex);
					aDisabledElements.append(')');
				}
			}

			setUIProperty(DISABLED_ELEMENTS,
						  aDisabledElements.toString(),
						  rEnumParam);
		}
		else
		{
			removeUIProperties(rEnumParam, DISABLED_ELEMENTS);
		}
	}

	/***************************************
	 * @see #disableElements(RelationType, Collection)
	 */
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void disableElements(
		RelationType<E> rEnumParam,
		Collection<E>   rAllElements,
		E... 			rDisabledElements)
	{
		disableElements(rEnumParam,
						rAllElements,
						Arrays.asList(rDisabledElements));
	}

	/***************************************
	 * Sets the value of a selected history record into a certain parameter.
	 *
	 * @param rHistoryParam      The parameter with the history selection
	 * @param rHistoryValueParam The parameter for the history value
	 */
	public void displayHistoryValue(
		RelationType<HistoryRecord> rHistoryParam,
		RelationType<String>		rHistoryValueParam)
	{
		HistoryRecord rHistory	    = getParameter(rHistoryParam);
		String		  sHistoryValue = "";

		if (rHistory != null)
		{
			sHistoryValue = rHistory.get(HistoryRecord.VALUE);
		}

		setParameter(rHistoryValueParam, sHistoryValue);
	}

	/***************************************
	 * Enables all values of an enum parameter.
	 *
	 * @see #disableElements(RelationType, Collection)
	 */
	public void enableAllElements(RelationType<? extends Enum<?>> rEnumParam)
	{
		removeUIProperties(rEnumParam, DISABLED_ELEMENTS);
	}

	/***************************************
	 * Allowed to query the allowed values of a collection-type process
	 * parameter.
	 *
	 * @param  rParam The parameter type
	 *
	 * @return The allowed values for the parameter (can be NULL)
	 */
	@SuppressWarnings("unchecked")
	public <T, C extends Collection<T>> Collection<T> getAllowedElements(
		RelationType<C> rParam)
	{
		// mark the parameter as modified because it is probable the the list
		// is queried for modification
		markParameterAsModified(rParam);

		return (Collection<T>) getParameterRelation(rParam).get(ALLOWED_VALUES);
	}

	/***************************************
	 * Allowed to query the allowed values of a process parameter.
	 *
	 * @param  rParam The parameter type
	 *
	 * @return The allowed values for the parameter (can be NULL)
	 */
	@SuppressWarnings("unchecked")
	public <T> Collection<T> getAllowedValues(RelationType<T> rParam)
	{
		// mark the parameter as modified because it is probable the the list
		// is queried for modification
		markParameterAsModified(rParam);

		return (Collection<T>) getParameterRelation(rParam).get(ALLOWED_VALUES);
	}

	/***************************************
	 * Returns the current query predicate for a parameter that has been
	 * annotated with a storage query. If the query hasn't (yet) been executed
	 * the result will be NULL.
	 *
	 * @param  rQueryParam The annotated parameter
	 *
	 * @return The current query predicate or NULL if no query is available for
	 *         the given parameter
	 *
	 * @throws StorageException If accessing the storage adapter fails
	 */
	@SuppressWarnings("unchecked")
	public <E extends Entity> QueryPredicate<E> getCurrentQuery(
		RelationType<E> rQueryParam) throws StorageException
	{
		QueryPredicate<E> pQuery = null;

		StorageAdapterId rAdapterId =
			getParameterRelation(rQueryParam).get(STORAGE_ADAPTER_ID);

		if (rAdapterId != null)
		{
			StorageAdapterRegistry rRegistry =
				getParameter(STORAGE_ADAPTER_REGISTRY);

			StorageAdapter rStorageAdapter =
				rRegistry.getStorageAdapter(rAdapterId.toString());

			if (rStorageAdapter != null)
			{
				pQuery =
					(QueryPredicate<E>) rStorageAdapter
					.getCurrentQueryCriteria();
			}
		}

		return pQuery;
	}

	/***************************************
	 * Returns the parameter that caused an interaction event.
	 *
	 * @return The interaction parameter or NULL for none
	 */
	public RelationType<?> getInteractiveInputParameter()
	{
		return getParameter(INTERACTIVE_INPUT_PARAM);
	}

	/***************************************
	 * Returns the value of a certain parameter from this fragment. This method
	 * will first try to look up the parameter in the enclosing process by
	 * invoking {@link Process#getParameter(RelationType)}. If the parameter is
	 * not found there, it will be queried from this fragment's relations.
	 *
	 * @param  rParamType The type of the parameter to return the value of
	 *
	 * @return The parameter value or NULL if not set
	 */
	public <T> T getParameter(RelationType<T> rParamType)
	{
		Process rProcess = getProcess();
		T	    rParam;

		// also query the relation from the process if it is not set on the step
		// to consider initial or default values; especially initial values need
		// to be created to be available to subsequent steps
		if (rProcess.hasParameter(rParamType) || !hasRelation(rParamType))
		{
			rParam = rProcess.getParameter(rParamType);
		}
		else
		{
			rParam = get(rParamType);
		}

		return rParam;
	}

	/***************************************
	 * Returns a certain annotation from a process parameter.
	 *
	 * @param rParam          The parameter to get the annotation for
	 * @param rAnnotationType The relation type of the annotation
	 *
	 * @see   #annotateParameter(RelationType, Object, RelationType, Object)
	 */
	public <T> T getParameterAnnotation(
		RelationType<?> rParam,
		RelationType<T> rAnnotationType)
	{
		Relation<?> rRelation   = getProcess().getRelation(rParam);
		T		    rAnnotation = null;

		if (rRelation != null)
		{
			rAnnotation = rRelation.getAnnotation(rAnnotationType);
		}

		return rAnnotation;
	}

	/***************************************
	 * Returns the relation of a certain parameter for this step. This method
	 * will first try to look up the relation in the enclosing process by
	 * invoking {@link Process#getRelation(RelationType)}. If the relation is
	 * not found there, it will be queried from this step.
	 *
	 * @param  rParamType The type of the relation to return
	 *
	 * @return The corresponding relation or NULL if not set
	 */
	public <T> Relation<T> getParameterRelation(RelationType<T> rParamType)
	{
		Process     rProcess  = getProcess();
		Relation<T> rRelation;

		if (rProcess.hasParameter(rParamType))
		{
			rRelation = rProcess.getRelation(rParamType);
		}
		else
		{
			rRelation = getRelation(rParamType);
		}

		return rRelation;
	}

	/***************************************
	 * @see Process#getProcessUser()
	 */
	public final Entity getProcessUser()
	{
		return getProcess().getProcessUser();
	}

	/***************************************
	 * Returns a certain user interface property value for a particular
	 * parameter.
	 *
	 * @param  rProperty The property to return the value of
	 * @param  rParam    The parameter to query for the property
	 *
	 * @return The property value or NULL if not set
	 */
	public <T> T getUIProperty(
		PropertyName<T> rProperty,
		RelationType<?> rParam)
	{
		T rValue = null;

		if (hasParameter(rParam))
		{
			HasProperties rProperties =
				getParameterRelation(rParam).get(DISPLAY_PROPERTIES);

			if (rProperties != null)
			{
				rValue = rProperties.getProperty(rProperty, null);
			}
		}

		return rValue;
	}

	/***************************************
	 * Shortcut method to return a particular value from the settings of the
	 * current user with a default value of NULL.
	 *
	 * @param  rSettingsExtraAttr The extra attribute of the setting
	 *
	 * @return The settings value or NULL for none
	 *
	 * @throws StorageException If accessing the user settings fails
	 *
	 * @see    #getUserSettings(boolean)
	 */
	public <T> T getUserSetting(RelationType<T> rSettingsExtraAttr)
		throws StorageException
	{
		return Configuration.getSettingsValue(getProcessUser(),
											  rSettingsExtraAttr,
											  null);
	}

	/***************************************
	 * Returns the configuration entity that contains the settings for the
	 * current process user.
	 *
	 * @param  bCreate TRUE to create the settings object if it doesn't exist
	 *
	 * @return The user's settings or NULL if none exist and bCreate is FALSE
	 *
	 * @throws TransactionException If saving a new configuration fails
	 * @throws StorageException     If retrieving the configuration entity fails
	 */
	public Configuration getUserSettings(boolean bCreate)
		throws StorageException, TransactionException
	{
		return Configuration.getSettings(getProcessUser(), bCreate);
	}

	/***************************************
	 * A convenience method to check the existence and value of a parameter with
	 * a boolean datatype.
	 *
	 * @param  rFlagType The flag parameter type
	 *
	 * @return TRUE if the flag exists and is set to TRUE
	 */
	public final boolean hasFlagParameter(RelationType<Boolean> rFlagType)
	{
		Boolean rFlagParam = getParameter(rFlagType);

		return rFlagParam != null && rFlagParam.booleanValue() == true;
	}

	/***************************************
	 * Convenience method to check the availability of a certain parameter in
	 * the enclosing process.
	 *
	 * @see Process#hasParameter(RelationType)
	 */
	public boolean hasParameter(RelationType<?> rParamType)
	{
		return hasRelation(rParamType) || getProcess().hasParameter(rParamType);
	}

	/***************************************
	 * Convenience method to check the existence and state of a boolean UI
	 * property of a parameter.
	 *
	 * @param  rFlagName The name of the flag property
	 * @param  rParam    The parameter to check the flag for
	 *
	 * @return TRUE if the flag property exists and is TRUE
	 */
	public boolean hasUIFlag(
		PropertyName<Boolean> rFlagName,
		RelationType<?>		  rParam)
	{
		Boolean rFlag = getUIProperty(rFlagName, rParam);

		return rFlag != null ? rFlag.booleanValue() : false;
	}

	/***************************************
	 * Shortcut method to check if a boolean flag ist set int the settings of
	 * the current user.
	 *
	 * @param  rSettingsFlag The extra attribute of the settings flag
	 *
	 * @return TRUE if the flag is set, FALSE if not or if it doesn't exist
	 *
	 * @throws StorageException If accessing the user settings fails
	 *
	 * @see    #getUserSettings(boolean)
	 */
	public boolean hasUserSetting(RelationType<Boolean> rSettingsFlag)
		throws StorageException
	{
		Boolean rFlag =
			Configuration.getSettingsValue(getProcessUser(),
										   rSettingsFlag,
										   null);

		return Boolean.TRUE.equals(rFlag);
	}

	/***************************************
	 * Checks whether the process has been suspended because of an interactive
	 * input event.
	 *
	 * @return TRUE if the process is suspended because of an interactive input
	 *         event
	 */
	public boolean isInteractiveInput()
	{
		return getParameter(INTERACTIVE_INPUT_PARAM) != null;
	}

	/***************************************
	 * Marks a certain parameter as modified to force a UI update in the next
	 * interaction.
	 *
	 * @param rParam The parameter to mark as modified
	 */
	public <T> void markParameterAsModified(RelationType<T> rParam)
	{
		getProcessStep().parameterModified(rParam);
	}

	/***************************************
	 * Prepares a date parameter for input. If the parameter is not yet an
	 * element of this step's interaction parameters it will be added as an
	 * input parameter.
	 *
	 * @param rParam        The parameter to initialize
	 * @param rDate         The current date to initialize the parameter with
	 * @param rEarliestDate If not NULL an input validation will be added to
	 *                      ensure that the input date is not before this date
	 *                      and that the date is set
	 */
	public void prepareDateInput(RelationType<Date> rParam,
								 Date				rDate,
								 Date				rEarliestDate)
	{
		if (!get(INTERACTION_PARAMS).contains(rParam))
		{
			addInputParameters(rParam);
		}

		if (!hasParameter(rParam))
		{
			setParameter(rParam, rDate);
		}

		if (rEarliestDate != null)
		{
			setParameterValidation(rParam,
								   doIfElse(isNull(),
											value(MSG_PARAM_NOT_SET),
											doIf(lessThan(rEarliestDate),
												 value("DateIsBeforeToday"))));
		}
	}

	/***************************************
	 * A shortcut method that only creates a download URL without assigning it
	 * to a parameter.
	 *
	 * @see #prepareDownload(RelationType, String, FileType, Function)
	 */
	public String prepareDownload(String				sFileName,
								  FileType				eFileType,
								  Function<FileType, ?> fDataGenerator)
		throws Exception
	{
		return prepareDownload(null, sFileName, eFileType, fDataGenerator);
	}

	/***************************************
	 * Prepares the file download for a URL interaction parameter. This method
	 * will perform the necessary cleanup operations if the download changes or
	 * the process step is left.
	 *
	 * @param  rUrlParam      The interaction parameter to store the URL in or
	 *                        NULL for none
	 * @param  sFileName      The file name
	 * @param  eFileType      the file type
	 * @param  fDataGenerator The function that generates the download data
	 *
	 * @return The app-relative download URL
	 *
	 * @throws Exception If preparing the download fails
	 */
	public String prepareDownload(RelationType<String>  rUrlParam,
								  String				sFileName,
								  FileType				eFileType,
								  Function<FileType, ?> fDataGenerator)
		throws Exception
	{
		final SessionManager rSessionManager =
			getParameter(DataRelationTypes.SESSION_MANAGER);

		DownloadData rDownloadData =
			new DownloadData(sFileName, eFileType, fDataGenerator, false);

		if (rUrlParam != null)
		{
			String sOldUrl = getParameter(rUrlParam);

			if (sOldUrl != null)
			{
				rSessionManager.removeDownload(sOldUrl);
				getProcessStep().removeFinishAction(sOldUrl);
			}
		}

		final String sDownloadUrl =
			rSessionManager.prepareDownload(rDownloadData);

		if (rUrlParam != null)
		{
			setParameter(rUrlParam, sDownloadUrl);
		}

		getProcessStep().addFinishAction(sDownloadUrl,
			new AbstractAction<ProcessStep>("removeDownload")
			{
				@Override
				public void execute(ProcessStep rValue)
				{
					rSessionManager.removeDownload(sDownloadUrl);
				}
			});

		return sDownloadUrl;
	}

	/***************************************
	 * Completely removes all display properties for one or more process
	 * parameters.
	 *
	 * @param rParams The parameters to remove the display properties from
	 */
	public void removeAllUIProperties(RelationType<?>... rParams)
	{
		removeAllUIProperties(Arrays.asList(rParams));
	}

	/***************************************
	 * Completely removes all display properties for multiple process
	 * parameters.
	 *
	 * @param rParams The parameters to remove the display properties from
	 */
	public void removeAllUIProperties(
		Collection<? extends RelationType<?>> rParams)
	{
		for (RelationType<?> rParam : rParams)
		{
			if (hasParameter(rParam))
			{
				getParameterRelation(rParam).deleteRelation(DISPLAY_PROPERTIES);
			}
		}
	}

	/***************************************
	 * Remove an annotation from a certain process parameter.
	 *
	 * @param rParam          The parameter to annotate
	 * @param rAnnotationType The relation type of the annotation
	 *
	 * @see   #annotateParameter(RelationType, Object, RelationType, Object)
	 */
	public void removeParameterAnnotation(
		RelationType<?> rParam,
		RelationType<?> rAnnotationType)
	{
		Relation<?> rRelation = getProcess().getRelation(rParam);

		if (rRelation != null)
		{
			rRelation.deleteRelation(rAnnotationType);
		}
	}

	/***************************************
	 * Removes display properties from a certain parameter.
	 *
	 * @param rParam      The parameter
	 * @param rProperties The properties to remove
	 */
	public <T> void removeUIProperties(
		RelationType<?>    rParam,
		PropertyName<?>... rProperties)
	{
		MutableProperties rDisplayProperties = null;

		if (hasParameter(rParam))
		{
			rDisplayProperties =
				getParameterRelation(rParam).get(DISPLAY_PROPERTIES);

			if (rDisplayProperties != null)
			{
				for (PropertyName<?> rProperty : rProperties)
				{
					rDisplayProperties.removeProperty(rProperty);
				}

				markParameterAsModified(rParam);
			}
		}
	}

	/***************************************
	 * Shortcut method to annotate a collection-type process parameters with the
	 * elements that are allowed to occur in the collection. The elements are
	 * stored in an {@link ProcessRelationTypes#ALLOWED_VALUES} annotation. If
	 * the parameter hasn't been set yet it will be initialized with the given
	 * value.
	 *
	 * @param rParam  The parameter to annotate
	 * @param rValues The list of allowed values for the parameter
	 */
	public <T, C extends Collection<T>, V extends Collection<T>> void setAllowedElements(
		RelationType<C> rParam,
		V				rValues)
	{
		annotateParameter(rParam, null, ALLOWED_VALUES, rValues);
	}

	/***************************************
	 * Shortcut method to set the allowed values of a process parameters. The
	 * possible values for the parameters are stored in an annotation of type
	 * {@link ProcessRelationTypes#ALLOWED_VALUES}. If the parameter hasn't been
	 * set yet it will be initialized with the given value.
	 *
	 * @param rParam  The parameter to annotate
	 * @param rValues The allowed values for the parameter
	 */
	@SafeVarargs
	public final <T> void setAllowedValues(RelationType<T> rParam, T... rValues)
	{
		setAllowedValues(rParam, Arrays.asList(rValues));
	}

	/***************************************
	 * Shortcut method to annotate a process parameters with allowed values. The
	 * possible values for the parameters are stored in an annotation of type
	 * {@link ProcessRelationTypes#ALLOWED_VALUES}. If the parameter hasn't been
	 * set yet it will be initialized with the given value.
	 *
	 * @param rParam  The parameter to annotate
	 * @param rValues The list of allowed values for the parameter
	 */
	public <T, C extends Collection<T>> void setAllowedValues(
		RelationType<T> rParam,
		C				rValues)
	{
		annotateParameter(rParam, null, ALLOWED_VALUES, rValues);
	}

	/***************************************
	 * @see #setEnabled(boolean, Collection)
	 */
	public final void setEnabled(boolean bEnabled, RelationType<?>... rParams)
	{
		setEnabled(bEnabled, Arrays.asList(rParams));
	}

	/***************************************
	 * A convenience method to enable or disable certain interaction parameter
	 * by changing the UI flag {@link UserInterfaceProperties#DISABLED}
	 * accordingly.
	 *
	 * @param bEnabled The enabled state to set
	 * @param rParams  The parameters to set the enabled state of
	 */
	public final void setEnabled(
		boolean								  bEnabled,
		Collection<? extends RelationType<?>> rParams)
	{
		if (bEnabled)
		{
			clearUIFlag(DISABLED, rParams);
		}
		else
		{
			setUIFlag(DISABLED, rParams);
		}
	}

	/***************************************
	 * Sets the properties {@link UserInterfaceProperties#HTML_WIDTH} and {@link
	 * UserInterfaceProperties#HTML_HEIGHT} on the given parameters.
	 *
	 * @param sWidth  The HTML width string
	 * @param sHeight sWidth The HTML height string
	 * @param rParams The parameters to set the size of
	 */
	public void setHtmlSize(String			   sWidth,
							String			   sHeight,
							RelationType<?>... rParams)
	{
		for (RelationType<?> rParam : rParams)
		{
			setUIProperty(HTML_WIDTH, sWidth, rParam);
			setUIProperty(HTML_HEIGHT, sHeight, rParam);
		}
	}

	/***************************************
	 * Shortcut method to initialize an enum parameter for an immediate action
	 * and with a certain number of columns to arrange the enum buttons in. This
	 * method will also set the {@link UserInterfaceProperties#HIDE_LABEL
	 * HIDE_LABEL} flag as it is typically used with immediate action buttons.
	 *
	 * @param rParam   The enum action parameter
	 * @param nColumns The number of columns
	 *
	 * @see   #setInteractive(RelationType, Collection, ListStyle, Object...)
	 */
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void setImmediateAction(
		RelationType<E> rParam,
		int				nColumns)
	{
		setImmediateAction(rParam);
		setUIFlag(HIDE_LABEL, rParam);
		setUIProperty(nColumns, COLUMNS, rParam);
	}

	/***************************************
	 * Shortcut method to initialize an enum parameter for an immediate action.
	 * The enum will be displayed as buttons with {@link ListStyle#IMMEDIATE}.
	 *
	 * @param rParam         The enum action parameter
	 * @param rAllowedValues The allowed values
	 *
	 * @see   #setInteractive(RelationType, Collection, ListStyle, Object...)
	 */
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void setImmediateAction(
		RelationType<E> rParam,
		E... 			rAllowedValues)
	{
		setInteractive(rParam, null, ListStyle.IMMEDIATE, rAllowedValues);
	}

	/***************************************
	 * Shortcut method to initialize an enum parameter for an immediate action.
	 * The enum will be displayed as buttons with {@link ListStyle#IMMEDIATE}.
	 *
	 * @param rParam         The enum action parameter
	 * @param rAllowedValues The allowed values (NULL or empty for all values of
	 *                       the enum)
	 *
	 * @see   #setInteractive(RelationType, Collection, ListStyle, Collection)
	 */
	public <E extends Enum<E>> void setImmediateAction(
		RelationType<E> rParam,
		Collection<E>   rAllowedValues)
	{
		setInteractive(rParam, null, ListStyle.IMMEDIATE, rAllowedValues);
	}

	/***************************************
	 * Initializes non-list parameters for interactive input by setting the
	 * property {@link UserInterfaceProperties#INTERACTIVE_INPUT_MODE} to the
	 * given mode.
	 *
	 * @param eMode   The interactive input mode
	 * @param rParams The parameters
	 */
	public void setInteractive(
		InteractiveInputMode eMode,
		RelationType<?>...   rParams)
	{
		for (RelationType<?> rParam : rParams)
		{
			setUIProperty(INTERACTIVE_INPUT_MODE, eMode, rParam);
		}
	}

	/***************************************
	 * Initializes a parameter for interactive input. If no allowed values are
	 * provided and the parameter datatype is an enum class all enum values will
	 * be allowed.
	 *
	 * @param rParam         The parameter
	 * @param rInitialValue  The initial parameter value or NULL for none
	 * @param rListStyle     The style in which to display the list of values
	 * @param rAllowedValues The allowed values
	 */
	@SafeVarargs
	public final <T> void setInteractive(RelationType<T> rParam,
										 T				 rInitialValue,
										 ListStyle		 rListStyle,
										 T... 			 rAllowedValues)
	{
		setInteractive(rParam,
					   rInitialValue,
					   rListStyle,
					   Arrays.asList(rAllowedValues));
	}

	/***************************************
	 * Initializes a parameter for interactive input. If no allowed values are
	 * provided and the parameter datatype is an enum class all enum values will
	 * be allowed.
	 *
	 * @param rParam         The parameter
	 * @param rIntialValue   The initial parameter value or NULL for none
	 * @param eListStyle     The style in which to display the list of values
	 * @param rAllowedValues The allowed values (NULL or empty for all values of
	 *                       an enum datatype)
	 */
	public <T> void setInteractive(RelationType<T> rParam,
								   T			   rIntialValue,
								   ListStyle	   eListStyle,
								   Collection<T>   rAllowedValues)
	{
		setInteractiveImpl(rParam, rIntialValue, eListStyle, rAllowedValues);
	}

	/***************************************
	 * Initializes a list parameter for interactive input. The resulting user
	 * interface will allow the selection of multiple values in the list. If no
	 * allowed values are provided and the parameter datatype is an enum class
	 * all enum values will be allowed.
	 *
	 * @param rParam         The parameter
	 * @param rDefaultValues The default parameter value or NULL for none
	 * @param rListStyle     The style in which to display the list of values
	 * @param rAllowedValues The allowed values
	 */
	@SuppressWarnings("unchecked")
	public <T, C extends Collection<T>> void setInteractive(
		RelationType<C> rParam,
		C				rDefaultValues,
		ListStyle		rListStyle,
		T... 			rAllowedValues)
	{
		setInteractive(rParam,
					   rDefaultValues,
					   rListStyle,
					   Arrays.asList(rAllowedValues));
	}

	/***************************************
	 * Initializes a list parameter for interactive input. The resulting user
	 * interface will allow the selection of multiple values in the list. If no
	 * allowed values are provided and the parameter datatype is an enum class
	 * all enum values will be allowed.
	 *
	 * @param rParam         The parameter
	 * @param rInitialValues The initial parameter values or NULL for none
	 * @param eListStyle     The style in which to display the list of values
	 * @param rAllowedValues The allowed values (NULL or empty for all values of
	 *                       an enum datatype)
	 */
	public <T, C extends Collection<T>> void setInteractive(
		RelationType<C> rParam,
		C				rInitialValues,
		ListStyle		eListStyle,
		Collection<T>   rAllowedValues)
	{
		setInteractiveImpl(rParam, rInitialValues, eListStyle, rAllowedValues);
	}

	/***************************************
	 * Sets the {@link DataElementList#LIST_DISPLAY_MODE LIST_DISPLAY_MODE}
	 * property for a {@link DataElementList} parameter.
	 *
	 * @param eMode  The list display mode
	 * @param rParam The parameter
	 */
	public void setListDisplayMode(
		ListDisplayMode						eMode,
		RelationType<List<RelationType<?>>> rParam)
	{
		setUIProperty(DataElementList.LIST_DISPLAY_MODE, eMode, rParam);
	}

	/***************************************
	 * Sets a certain parameter in the enclosing process. Other than the methods
	 * that query process step parameters this method will not access this
	 * step's relations. It always writes directly to the process parameters.
	 *
	 * @return The relation of the parameter
	 *
	 * @see    Process#setParameter(RelationType, Object)
	 */
	public <T, R> Relation<T> setParameter(RelationType<T> rParam, T rValue)
	{
		markParameterAsModified(rParam);

		return getProcess().setParameter(rParam, rValue);
	}

	/***************************************
	 * Convenience method to set boolean parameters without a boxing warning.
	 *
	 * @see #setParameter(RelationType, Object)
	 */
	public final Relation<Boolean> setParameter(
		RelationType<Boolean> rParam,
		boolean				  bValue)
	{
		return setParameter(rParam, Boolean.valueOf(bValue));
	}

	/***************************************
	 * Sets the minimum and maximum values of an integer parameter.
	 *
	 * @param rParam The parameter
	 * @param nMin   The minimum value
	 * @param nMax   The maximum value
	 */
	@SuppressWarnings("boxing")
	public final void setParameterBounds(RelationType<Integer> rParam,
										 int				   nMin,
										 int				   nMax)
	{
		annotateParameter(rParam, null, MINIMUM, nMin);
		annotateParameter(rParam, null, MAXIMUM, nMax);
	}

	/***************************************
	 * Sets a information string to be displayed as a readonly text for this
	 * process step. If other interaction parameters exist the information will
	 * be displayed as the first parameter if it doesn't exist already or, if a
	 * {@link #setProcessStepMessage(String) process message} exists, as the
	 * second.
	 *
	 * @param sInfo The information string or a resource ID or NULL to reset
	 * @param nRows The number of rows to display (> 1 for a multi-line display)
	 *              or -1 to calculate the number of rows in the input text
	 */
	public void setProcessStepInfo(String sInfo, int nRows)
	{
		if (sInfo != null)
		{
			List<RelationType<?>> rInteractionParams = get(INTERACTION_PARAMS);

			if (!rInteractionParams.contains(PROCESS_STEP_INFO))
			{
				int nPosition =
					rInteractionParams.contains(PROCESS_STEP_MESSAGE) ? 1 : 0;

				rInteractionParams.add(nPosition, PROCESS_STEP_INFO);
			}

			if (nRows == -1)
			{
				nRows = TextUtil.count(sInfo, "\n");
			}

			setParameter(PROCESS_STEP_INFO, sInfo);
			setUIFlag(HIDE_LABEL, PROCESS_STEP_INFO);
			setUIFlag(WRAP, PROCESS_STEP_INFO);
			setUIProperty(nRows, ROWS, PROCESS_STEP_INFO);
		}
		else
		{
			removeInteractionParameters(PROCESS_STEP_INFO);
		}
	}

	/***************************************
	 * Sets a message that should be displayed with emphasis for this process
	 * step. If other interaction parameters exist the message will be displayed
	 * as the first parameter if it doesn't exist already.
	 *
	 * @param sMessage The message or message resource ID or NULL to reset
	 */
	public void setProcessStepMessage(String sMessage)
	{
		if (sMessage != null)
		{
			List<RelationType<?>> rInteractionParams = get(INTERACTION_PARAMS);

			if (!rInteractionParams.contains(PROCESS_STEP_MESSAGE))
			{
				rInteractionParams.add(0, PROCESS_STEP_MESSAGE);
			}

			setParameter(PROCESS_STEP_MESSAGE, sMessage);
			setUIFlag(HIDE_LABEL, PROCESS_STEP_MESSAGE);
			setUIFlag(WRAP, PROCESS_STEP_MESSAGE);
		}
		else
		{
			get(INTERACTION_PARAMS).remove(PROCESS_STEP_MESSAGE);
		}
	}

	/***************************************
	 * A convenience method to set a selection dependency with the property
	 * {@link UserInterfaceProperties#SELECTION_DEPENDENCY}.
	 *
	 * @param rParam           The parameter to set the dependency for
	 * @param bReverseState    TRUE to reverse the selection state of buttons
	 * @param rDependentParams The dependent parameters
	 */
	public final void setSelectionDependency(
		RelationType<?>    rParam,
		boolean			   bReverseState,
		RelationType<?>... rDependentParams)
	{
		setSelectionDependency(rParam,
							   bReverseState,
							   Arrays.asList(rDependentParams));
	}

	/***************************************
	 * A convenience method to set the selection dependencies in the UI property
	 * {@link UserInterfaceProperties#SELECTION_DEPENDENCY}. Empty parameter
	 * lists will be ignored.
	 *
	 * @param rParam           The parameter to set the dependency for
	 * @param bReverseState    TRUE to reverse the selection state of buttons
	 * @param rDependentParams The dependent parameters
	 */
	public final void setSelectionDependency(
		RelationType<?>						  rParam,
		boolean								  bReverseState,
		Collection<? extends RelationType<?>> rDependentParams)
	{
		if (rDependentParams.size() > 0)
		{
			StringBuilder aDependencies = new StringBuilder();

			for (RelationType<?> rDependentParam : rDependentParams)
			{
				if (bReverseState)
				{
					aDependencies.append(SELECTION_DEPENDENCY_REVERSE_PREFIX);
				}

				aDependencies.append(rDependentParam.getName());
				aDependencies.append(',');
			}

			aDependencies.setLength(aDependencies.length() - 1);

			setUIProperty(SELECTION_DEPENDENCY,
						  aDependencies.toString(),
						  rParam);
		}
		else
		{
			removeUIProperties(rParam, SELECTION_DEPENDENCY);
		}
	}

	/***************************************
	 * A convenience method to set a certain boolean display properties on one
	 * or more process parameters to TRUE.
	 *
	 * @see #setUIProperty(PropertyName, Object, RelationType...)
	 */
	public final void setUIFlag(
		PropertyName<Boolean> rProperty,
		RelationType<?>...    rParams)
	{
		setUIProperty(rProperty, Boolean.TRUE, rParams);
	}

	/***************************************
	 * A convenience method to set a certain boolean display properties on one
	 * or more process parameters to TRUE.
	 *
	 * @see #setUIProperty(PropertyName, Object, Collection)
	 */
	public final void setUIFlag(
		PropertyName<Boolean>				  rProperty,
		Collection<? extends RelationType<?>> rParams)
	{
		setUIProperty(rProperty, Boolean.TRUE, rParams);
	}

	/***************************************
	 * Convenience method for the setting of integer properties.
	 *
	 * @see #setUIProperty(PropertyName, Object, Collection)
	 */
	public void setUIProperty(int									nValue,
							  PropertyName<Integer>					rProperty,
							  Collection<? extends RelationType<?>> rParams)
	{
		setUIProperty(rProperty, Integer.valueOf(nValue), rParams);
	}

	/***************************************
	 * Convenience method for the setting of integer properties.
	 *
	 * @see #setUIProperty(PropertyName, Object, RelationType...)
	 */
	public void setUIProperty(int					nValue,
							  PropertyName<Integer> rProperty,
							  RelationType<?>...    rParams)
	{
		setUIProperty(rProperty, Integer.valueOf(nValue), rParams);
	}

	/***************************************
	 * Sets a certain display property on one or more process parameters. This
	 * method will create the display properties object if necessary.
	 *
	 * @param rProperty The property to set
	 * @param rValue    The property value
	 * @param rParams   The parameter to set the property on
	 */
	public <T> void setUIProperty(PropertyName<T>    rProperty,
								  T					 rValue,
								  RelationType<?>... rParams)
	{
		setUIProperty(rProperty, rValue, Arrays.asList(rParams));
	}

	/***************************************
	 * Sets a certain display property on multiple process parameters. This
	 * method will create the display properties object if necessary.
	 *
	 * @param rProperty The property to set
	 * @param rValue    The property value
	 * @param rParams   The parameters to set the property on
	 */
	public <T> void setUIProperty(
		PropertyName<T>						  rProperty,
		T									  rValue,
		Collection<? extends RelationType<?>> rParams)
	{
		for (RelationType<?> rParam : rParams)
		{
			MutableProperties rDisplayProperties = null;

			if (hasParameter(rParam))
			{
				rDisplayProperties =
					getParameterRelation(rParam).get(DISPLAY_PROPERTIES);
			}

			if (rDisplayProperties == null)
			{
				rDisplayProperties = new StringProperties();
				annotateParameter(rParam,
								  null,
								  DISPLAY_PROPERTIES,
								  rDisplayProperties);
			}
			else if (!NON_MODIFYING_PROPERTIES.contains(rProperty))
			{
				markParameterAsModified(rParam);
			}

			rDisplayProperties.setProperty(rProperty, rValue);
		}
	}

	/***************************************
	 * A convenience method to hide or show certain interaction parameter by
	 * changing the UI flag {@link UserInterfaceProperties#HIDDEN} accordingly.
	 *
	 * @param bVisible The new visible
	 * @param rParams  The new visible
	 */
	public final void setVisible(boolean bVisible, RelationType<?>... rParams)
	{
		setVisible(bVisible, Arrays.asList(rParams));
	}

	/***************************************
	 * A convenience method to hide or show certain interaction parameter by
	 * changing the UI flag {@link UserInterfaceProperties#HIDDEN} accordingly.
	 *
	 * @param bVisible The new visible
	 * @param rParams  The new visible
	 */
	public final void setVisible(
		boolean								  bVisible,
		Collection<? extends RelationType<?>> rParams)
	{
		if (bVisible)
		{
			clearUIFlag(HIDDEN, rParams);
		}
		else
		{
			setUIFlag(HIDDEN, rParams);
		}
	}

	/***************************************
	 * Returns the process step this fragment represents or belongs to.
	 *
	 * @return The process step of this fragment
	 */
	protected abstract ProcessStep getProcessStep();

	/***************************************
	 * Stores the value of a derived process parameter under the original
	 * relation type in a target object. For details about derived parameters
	 * see {@link ProcessRelationTypes#deriveParameter(String, RelationType)}.
	 *
	 * @param rDerivedParam The derived parameter
	 * @param rTarget       The target object to set the derived parameter on
	 */
	protected <T> void applyDerivedParameter(
		RelationType<T> rDerivedParam,
		Relatable		rTarget)
	{
		@SuppressWarnings("unchecked")
		RelationType<T> rTargetType =
			(RelationType<T>) rDerivedParam.get(ORIGINAL_RELATION_TYPE);

		T rParamValue = getParameter(rDerivedParam);

		if (rParamValue instanceof String &&
			((String) rParamValue).length() == 0)
		{
			rParamValue = null;
		}

		rTarget.set(rTargetType, rParamValue);
	}

	/***************************************
	 * Method for subclasses to check and retrieve the value of a certain
	 * parameter. Invokes {@link #getParameter(RelationType)} to retrieve the
	 * value and if it is NULL but the parameter has been marked as mandatory an
	 * exception will be thrown. Future versions may also check additional
	 * conditions, therefore subclasses should always invoke this methods
	 * instead of {@link #getParameter(RelationType)} unless circumventing the
	 * parameters checks is explicitly needed.
	 *
	 * @param  rParamType The type of the parameter to check and return
	 *
	 * @return The parameter value (may be NULL for non-mandatory parameters)
	 *
	 * @throws IllegalStateException If the parameter has been marked as
	 *                               mandatory but is NULL
	 */
	protected <T> T checkParameter(RelationType<T> rParamType)
	{
		T rParam = getParameter(rParamType);

		if (rParam == null)
		{
			throwMissingParameterException(rParamType);
		}

		return rParam;
	}

	/***************************************
	 * Reads the value of a derived process parameter with the original relation
	 * type from a certain object and stores it in the process. For details see
	 * {@link ProcessRelationTypes#deriveParameter(String, RelationType)}.
	 *
	 * @param rSource       The source object to read the parameter value from
	 * @param rDerivedParam The derived parameter
	 * @param bSkipExisting If TRUE only parameters that don't exist already
	 *                      will be collected from the source object
	 */
	protected <T> void collectDerivedParameter(Relatable	   rSource,
											   RelationType<T> rDerivedParam,
											   boolean		   bSkipExisting)
	{
		if (!(bSkipExisting && hasParameter(rDerivedParam)))
		{
			@SuppressWarnings("unchecked")
			RelationType<T> rSourceType =
				(RelationType<T>) rDerivedParam.get(ORIGINAL_RELATION_TYPE);

			setParameter(rDerivedParam, rSource.get(rSourceType));
		}
	}

	/***************************************
	 * Returns the absolute path and file name for a relative file path. This
	 * will only work if a session manager reference is available from the
	 * process parameter {@link DataRelationTypes#SESSION_MANAGER}. Otherwise
	 * the input path is returned unchanged.
	 *
	 * @param  sFileName The relative path to the file
	 *
	 * @return The absolute file path
	 */
	protected String getAbsoluteFilePath(String sFileName)
	{
		SessionManager rSessionManager =
			getParameter(DataRelationTypes.SESSION_MANAGER);

		if (rSessionManager != null)
		{
			sFileName = rSessionManager.getAbsoluteFileName(sFileName);
		}

		return sFileName;
	}

	/***************************************
	 * Creates a temporary list type with a random unique name and sets the
	 * {@link UserInterfaceProperties#RESOURCE_ID} to the given string
	 * parameter.
	 *
	 * @param  sResourceId  The resource id to use.
	 * @param  rElementType The collection element data type
	 *
	 * @return a temporary list type with a random unique name and sets the
	 *         {@link UserInterfaceProperties#RESOURCE_ID} to the given string
	 *         parameter.
	 */
	protected <T> RelationType<List<T>> getNamedTmpListType(
		String			 sResourceId,
		Class<? super T> rElementType)
	{
		String sReleationTypeName = sResourceId + UUID.randomUUID().toString();

		RelationType<List<T>> rTemporaryListType =
			getTemporaryListType(sReleationTypeName, rElementType);

		setUIProperty(RESOURCE_ID, sResourceId, rTemporaryListType);

		return rTemporaryListType;
	}

	/***************************************
	 * Creates a temporary parameter type with a random unique name and sets the
	 * {@link UserInterfaceProperties#RESOURCE_ID} to the given string
	 * parameter.
	 *
	 * @param  sResourceId The resource id to use.
	 * @param  rDatatype   rElementType The collection element data type
	 *
	 * @return a temporary parameter type with a random unique name and sets the
	 *         {@link UserInterfaceProperties#RESOURCE_ID} to the given string
	 *         parameter.
	 */
	protected <T> RelationType<T> getNamedTmpParameterType(
		String			 sResourceId,
		Class<? super T> rDatatype)
	{
		String sRelationTypeName = sResourceId + UUID.randomUUID().toString();

		RelationType<T> rTemporaryParameterType =
			getTemporaryParameterType(sRelationTypeName, rDatatype);

		setUIProperty(RESOURCE_ID, sResourceId, rTemporaryParameterType);

		return rTemporaryParameterType;
	}

	/***************************************
	 * A helper method for subclasses that returns the selection index for a
	 * selection data element parameter.
	 *
	 * @param  rSelectionParam The selection data element parameter type
	 *
	 * @return The selection index for the parameter (-1 for no selection)
	 */
	protected int getSelectionIndex(
		RelationType<SelectionDataElement> rSelectionParam)
	{
		SelectionDataElement rSelection = getParameter(rSelectionParam);
		int					 nSelection = -1;

		if (rSelection != null)
		{
			nSelection = Integer.parseInt(rSelection.getValue());
		}

		return nSelection;
	}

	/***************************************
	 * Returns the subordinate fragments of this instance.
	 *
	 * @return The subordinate fragments
	 */
	protected final Collection<InteractionFragment> getSubFragments()
	{
		return aSubFragments.values();
	}

	/***************************************
	 * Returns a temporary parameter relation type that references a list with a
	 * certain element datatype.
	 *
	 * @param  sName        The name of the parameter
	 * @param  rElementType The collection element datatype
	 *
	 * @return The temporary list parameter type
	 *
	 * @see    #getTemporaryParameterType(String, Class)
	 */
	protected <T> RelationType<List<T>> getTemporaryListType(
		String			 sName,
		Class<? super T> rElementType)
	{
		RelationType<List<T>> rParam =
			getTemporaryParameterType(sName, List.class);

		rParam.set(ELEMENT_DATATYPE, rElementType);

		return rParam;
	}

	/***************************************
	 * Returns the package name for temporary parameter types created by the
	 * method {@link #getTemporaryParameterType(String, Class)}. Subclasses may
	 * override this method to modify the default which creates a package name
	 * that is unique for the current process instance (but will be shared by
	 * all process steps). The package name must be returned without leading or
	 * trailing dots.
	 *
	 * @return The package name for temporary parameter types
	 */
	protected String getTemporaryParameterPackage()
	{
		return "process" + getProcess().getId();
	}

	/***************************************
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
	 * @param  sName     The name of the parameter type
	 * @param  rDatatype The datatype class of the type
	 *
	 * @return The temporary relation type instance
	 */
	protected <T> RelationType<T> getTemporaryParameterType(
		String			 sName,
		Class<? super T> rDatatype)
	{
		sName = TextConvert.uppercaseIdentifier(sName).replaceAll("[.-]", "_");

		if (Character.isDigit(sName.charAt(0)))
		{
			sName = "_" + sName;
		}

		sName = getTemporaryParameterPackage() + "." + sName;

		@SuppressWarnings("unchecked")
		RelationType<T> rParam = (RelationType<T>) RelationType.valueOf(sName);

		if (rParam == null)
		{
			rParam = newRelationType(sName, rDatatype);
			getParameter(TEMPORARY_PARAM_TYPES).add(rParam);
		}
		else
		{
			assert rParam.getTargetType() == rDatatype;
		}

		return rParam;
	}

	/***************************************
	 * Creates a new data set data element with chart data and sets the
	 * corresponding process parameter.
	 *
	 * @param rTargetParam     The parameter to store the chart data element in
	 * @param aDataSet         The chart data
	 * @param eChartType       The chart type
	 * @param eLegendPosition  The legend position
	 * @param sBackgroundColor The chart background color
	 * @param b3D              TRUE for a 3D chart
	 */
	protected void initChartParameter(
		RelationType<DataSetDataElement> rTargetParam,
		DataSet<?>						 aDataSet,
		ChartType						 eChartType,
		LegendPosition					 eLegendPosition,
		String							 sBackgroundColor,
		boolean							 b3D)
	{
		DataSetDataElement aChartElement =
			new DataSetDataElement(rTargetParam.getName(),
								   aDataSet,
								   eChartType,
								   eLegendPosition,
								   sBackgroundColor,
								   b3D);

		setParameter(rTargetParam, aChartElement);
	}

	/***************************************
	 * Initializes a chart parameter that displays integer counts for certain
	 * elements. The chart data is defined as a mapping from string names that
	 * are used as labels to the corresponding integer count values.
	 *
	 * @param rTargetParam     The target {@link DataSet} parameter
	 * @param aDataMap         The mapping from data labels to counts
	 * @param eChartType       The type of chart to display
	 * @param sRowAxisLabel    The label for the data row (x) axis
	 * @param sValueAxisLabel  The label for the data value (y) axis
	 * @param eLegendPosition  The legend position
	 * @param sBackgroundColor The chart background color
	 * @param b3D              TRUE for a 3D chart
	 */
	protected void initCountChartParameter(
		RelationType<DataSetDataElement> rTargetParam,
		Map<String, Integer>			 aDataMap,
		ChartType						 eChartType,
		String							 sRowAxisLabel,
		String							 sValueAxisLabel,
		LegendPosition					 eLegendPosition,
		String							 sBackgroundColor,
		boolean							 b3D)
	{
		IntDataSet aDataSet =
			new IntDataSet(null, sRowAxisLabel, sValueAxisLabel, "");

		aDataMap = CollectionUtil.sort(aDataMap);

		for (Entry<String, Integer> rRow : aDataMap.entrySet())
		{
			aDataSet.addRow(rRow.getKey(), rRow.getValue());
		}

		initChartParameter(rTargetParam,
						   aDataSet,
						   eChartType,
						   eLegendPosition,
						   sBackgroundColor,
						   b3D);
	}

	/***************************************
	 * Initializes the parameter {@link ProcessRelationTypes#PROGRESS} to be
	 * displayed as a progress indicator. The maximum progress value must be set
	 * in the {@link ProcessRelationTypes#PROGRESS_MAXIMUM} parameter or else an
	 * exception will be thrown.
	 */
	@SuppressWarnings("boxing")
	protected void initProgressParameter()
	{
		annotateParameter(PROGRESS, null, MINIMUM, 0);
		annotateParameter(PROGRESS,
						  null,
						  MAXIMUM,
						  checkParameter(PROGRESS_MAXIMUM));

		setUIProperty(CONTENT_TYPE, ContentType.PROGRESS, PROGRESS);
	}

	/***************************************
	 * Removes a temporary parameter type that has previously been created by
	 * invoking {@link #getTemporaryParameterType(String, Class)}.
	 *
	 * @param rTempParam The temporary parameter type
	 */
	protected void removeTemporaryParameterType(RelationType<?> rTempParam)
	{
		getParameter(TEMPORARY_PARAM_TYPES).remove(rTempParam);
		RelationType.unregisterRelationType(rTempParam);
	}

	/***************************************
	 * Sets two date parameters to span a certain date period around a certain
	 * date. The resulting start date will be inclusive, the next date exclusive
	 * of the requested date range.
	 *
	 * @param rAroundDate     The date to calculate the period around
	 * @param rStartDateParam The parameter type for the start date
	 * @param rEndDateParam   The parameter type for the end date
	 * @param nCalendarField  The calendar field to calculate the period for
	 * @param nPeriodSize     The size of the period
	 */
	protected void setDatePeriod(Date				rAroundDate,
								 RelationType<Date> rStartDateParam,
								 RelationType<Date> rEndDateParam,
								 int				nCalendarField,
								 int				nPeriodSize)
	{
		Calendar rCalendar = Calendar.getInstance();

		rCalendar.setTime(rAroundDate);
		CalendarFunctions.clearTime(rCalendar);
		rCalendar.setFirstDayOfWeek(Calendar.MONDAY);

		int nCurrentValue  = rCalendar.get(nCalendarField);
		int nBoundaryField = -1;

		if (nCalendarField == Calendar.WEEK_OF_YEAR)
		{
			nBoundaryField = Calendar.DAY_OF_WEEK;
		}
		else if (nCalendarField == Calendar.WEEK_OF_MONTH)
		{
			nBoundaryField = Calendar.DAY_OF_WEEK_IN_MONTH;
		}
		else if (nCalendarField == Calendar.MONTH)
		{
			nBoundaryField = Calendar.DAY_OF_MONTH;
		}
		else if (nCalendarField == Calendar.YEAR)
		{
			nBoundaryField = Calendar.DAY_OF_YEAR;
		}

		// convert other fields than MONTH to zero-based value
		if (nCalendarField != Calendar.MONTH)
		{
			nCurrentValue =
				((nCurrentValue - 1) / nPeriodSize * nPeriodSize) + 1;
		}
		else
		{
			nCurrentValue = nCurrentValue / nPeriodSize * nPeriodSize;
		}

		rCalendar.set(nCalendarField, nCurrentValue);

		if (nBoundaryField >= 0)
		{
			rCalendar.set(nBoundaryField, 1);
		}

		Date rStartDate = rCalendar.getTime();

		rCalendar.add(nCalendarField, nPeriodSize);

		Date rEndDate = rCalendar.getTime();

		setParameter(rStartDateParam, rStartDate);
		setParameter(rEndDateParam, rEndDate);
	}

	/***************************************
	 * Sets the parameter {@link ProcessRelationTypes#PROGRESS_INDICATOR} to the
	 * current progress values.
	 *
	 * @see #initProgressParameter()
	 */
	protected void setProgressIndicator()
	{
		setParameter(PROGRESS_INDICATOR,
					 String.format(getParameter(PROGRESS_INDICATOR_TEMPLATE),
								   getParameter(PROGRESS),
								   getParameter(PROGRESS_MAXIMUM)));
	}

	/***************************************
	 * Throws a runtime exception that signals a missing process parameter.
	 *
	 * @param rParamType The relation type of the missing parameter
	 */
	protected <T> void throwMissingParameterException(
		RelationType<T> rParamType)
	{
		throw new IllegalStateException(String.format("Parameter %s not set",
													  rParamType));
	}

	/***************************************
	 * Internal implementation to initialize an interactive enum parameter.
	 *
	 * @param rParam         The parameter to initialize
	 * @param rIntialValue   The initial parameter value or NULL for none
	 * @param eListStyle     The style in which to display the list of values
	 * @param rAllowedValues The allowed enum values (NULL or empty for all
	 *                       values of the given enum)
	 */
	@SuppressWarnings("unchecked")
	private <E, T> void setInteractiveImpl(RelationType<T> rParam,
										   T			   rIntialValue,
										   ListStyle	   eListStyle,
										   Collection<E>   rAllowedValues)
	{
		if (rAllowedValues == null || rAllowedValues.size() == 0)
		{
			Class<?> rDatatype = rParam.getTargetType();

			if (Collection.class.isAssignableFrom(rDatatype))
			{
				rDatatype = rParam.get(ELEMENT_DATATYPE);
			}

			if (rDatatype.isEnum())
			{
				rAllowedValues =
					Arrays.asList((E[]) rDatatype.getEnumConstants());
			}
		}

		annotateParameter(rParam, rIntialValue, ALLOWED_VALUES, rAllowedValues);
		setUIProperty(INTERACTIVE_INPUT_MODE, getInputMode(eListStyle), rParam);
		setUIProperty(LIST_STYLE, eListStyle, rParam);
	}
}
