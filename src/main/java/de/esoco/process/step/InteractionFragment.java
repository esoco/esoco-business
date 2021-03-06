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
package de.esoco.process.step;

import de.esoco.data.DataRelationTypes;
import de.esoco.data.SessionManager;
import de.esoco.data.UploadHandler;
import de.esoco.data.element.DataSetDataElement.ChartType;
import de.esoco.data.element.DateDataElement.DateInputType;
import de.esoco.data.element.SelectionDataElement;

import de.esoco.entity.Entity;
import de.esoco.entity.EntityManager;
import de.esoco.entity.EntityRelationTypes.HierarchicalQueryMode;
import de.esoco.entity.ExtraAttributes;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.function.Initializer;
import de.esoco.lib.manage.Initializable;
import de.esoco.lib.model.DataSet;
import de.esoco.lib.property.ButtonStyle;
import de.esoco.lib.property.CheckBoxStyle;
import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.lib.property.LabelStyle;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.StateProperties;
import de.esoco.lib.property.Updatable;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.property.ViewDisplayType;

import de.esoco.process.Process;
import de.esoco.process.ProcessElement;
import de.esoco.process.ProcessException;
import de.esoco.process.ProcessFragment;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ProcessStep;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.ViewFragment;
import de.esoco.process.param.CollectionParameter.ListParameter;
import de.esoco.process.param.CollectionParameter.SetParameter;
import de.esoco.process.param.DataSetParameter;
import de.esoco.process.param.EntityAttributeParameter;
import de.esoco.process.param.EntityParameter;
import de.esoco.process.param.EnumParameter;
import de.esoco.process.param.Parameter;
import de.esoco.process.param.ParameterList;
import de.esoco.process.step.DialogFragment.DialogAction;
import de.esoco.process.step.DialogFragment.DialogActionListener;
import de.esoco.process.step.Interaction.InteractionHandler;
import de.esoco.process.ui.UiBuilder;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.UiRootFragment;
import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StorageException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.filter.RelationCoupling;
import org.obrel.type.MetaTypes;

import static de.esoco.data.element.DateDataElement.DATE_INPUT_TYPE;

import static de.esoco.entity.EntityPredicates.forEntity;
import static de.esoco.entity.EntityRelationTypes.HIERARCHICAL_QUERY_MODE;

import static de.esoco.lib.property.ContentProperties.CONTENT_TYPE;
import static de.esoco.lib.property.ContentProperties.URL;
import static de.esoco.lib.property.LayoutProperties.LAYOUT;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.StateProperties.STRUCTURE_CHANGED;
import static de.esoco.lib.property.StyleProperties.CHECK_BOX_STYLE;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.LABEL_STYLE;
import static de.esoco.lib.property.StyleProperties.LIST_STYLE;

import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.PARAM_UPDATE_LISTENERS;
import static de.esoco.process.ProcessRelationTypes.VIEW_PARAMS;

import static org.obrel.type.StandardTypes.ERROR_MESSAGE;


/********************************************************************
 * A process element subclass that serves as a fragment of an interactive
 * process step. This allows to split the user interface of complex interactions
 * into different parts that can more easily be re-used.
 *
 * @author eso
 */
public abstract class InteractionFragment extends ProcessFragment
	implements Initializable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The resource string for an error message box icon. */
	public static final String MESSAGE_BOX_ERROR_ICON = "#$imErrorMessage";

	/** The resource string for a warning message box icon. */
	public static final String MESSAGE_BOX_WARNING_ICON = "#$imWarningMessage";

	/** The resource string for a question message box icon. */
	public static final String MESSAGE_BOX_QUESTION_ICON =
		"#$imQuestionMessage";

	/** The resource string for an info message box icon. */
	public static final String MESSAGE_BOX_INFO_ICON = "#$imInfoMessage";

	//~ Instance fields --------------------------------------------------------

	private int     nNextParameterId = 0;
	private boolean bInitialized     = false;

	private Interaction		    rProcessStep;
	private InteractionFragment rParent;
	private ParameterList	    rFragmentParam;

	private List<RelationType<?>> aFragmentContinuationParams = null;

	private List<RelationType<?>> aInteractionParams = new ArrayList<>();
	private Set<RelationType<?>>  aInputParams		 = new HashSet<>();

	private Map<RelationType<?>, Function<?, String>> aParamValidations			   =
		new HashMap<>();
	private Map<RelationType<?>, Function<?, String>> aParamInteractionValidations =
		new HashMap<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public InteractionFragment()
	{
		RelationTypes.init(getClass());
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Must be implemented to initialize the interaction parameters of this
	 * fragment.
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	@Override
	public abstract void init() throws Exception;

	/***************************************
	 * Internal method to abort the current execution of this fragment. It can
	 * be used to undo data and parameter initializations or interactive
	 * modifications that have been performed by this fragment. Subclasses must
	 * implement {@link #abort()} instead.
	 */
	public final void abortFragment()
	{
		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rSubFragment.abortFragment();
		}

		executeCleanupActions();
		abort();
	}

	/***************************************
	 * Overridden to add nonexistent parameters to the list of interaction
	 * parameters of this instance as returned by the method {@link
	 * #getInteractionParameters()}. The returned collection must therefore be
	 * mutable (as is the case with the default parameter collection).
	 *
	 * <p>This implementation replaces the base class implementation which
	 * changes the interaction parameters of the process step instead of the
	 * fragment.</p>
	 *
	 * @param rParams The interaction parameters to add
	 */
	@Override
	public void addDisplayParameters(
		Collection<? extends RelationType<?>> rParams)
	{
		List<RelationType<?>> rInteractionParams = getInteractionParameters();

		for (RelationType<?> rParam : rParams)
		{
			markParameterAsModified(rParam);

			// do not add parameters that are displayed in panels because they
			// are stored in the parameter list of the panel parameter
			if (!isPanelParameter(rParam) &&
				!rInteractionParams.contains(rParam))
			{
				rInteractionParams.add(rParam);
				structureModified();
			}
		}
	}

	/***************************************
	 * @see #addInputParameters(Collection)
	 */
	@Override
	public void addInputParameters(RelationType<?>... rParams)
	{
		addInputParameters(Arrays.asList(rParams));
	}

	/***************************************
	 * Adds the given parameters to the interaction and input parameters of this
	 * instance. The input parameters are queried with the method {@link
	 * #getInputParameters()}, the interaction parameters are updated with
	 * {@link #addDisplayParameters(Collection)}.
	 *
	 * <p>This implementation replaces the base class implementation which
	 * changes the interaction parameters of the process step instead of the
	 * fragment.</p>
	 *
	 * @param rParams The input parameters to add
	 *
	 * @see   #addDisplayParameters(Collection)
	 */
	@Override
	public void addInputParameters(
		Collection<? extends RelationType<?>> rParams)
	{
		addDisplayParameters(rParams);
		markInputParams(true, rParams);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void addPanelParameters(Collection<RelationType<?>> rPanelParams)
	{
		super.addPanelParameters(rPanelParams);

		getInteractionParameters().removeAll(rPanelParams);
		structureModified();
	}

	/***************************************
	 * Convenience method to add a listener to the process step relation with
	 * the type {@link ProcessRelationTypes#PARAM_UPDATE_LISTENERS}. To remove a
	 * listener it should be removed from the parameter set directly.
	 *
	 * @param rListener The listener to add
	 */
	public void addParameterUpdateListener(Updatable rListener)
	{
		get(PARAM_UPDATE_LISTENERS).add(rListener);
	}

	/***************************************
	 * A variant of {@link #addSubFragment(String, InteractionFragment)} that
	 * uses the name of the fragment class for the temporary fragment parameter.
	 *
	 * @see #addSubFragment(String, InteractionFragment)
	 */
	public ParameterList addSubFragment(InteractionFragment rSubFragment)
	{
		Class<? extends InteractionFragment> rFragmentClass =
			rSubFragment.getClass();

		String sFragmentName =
			rFragmentClass.isAnonymousClass() ? null
											  : rFragmentClass.getSimpleName();

		return addSubFragment(sFragmentName, rSubFragment);
	}

	/***************************************
	 * Overridden to set the parent of the sub-fragment to this instance.
	 *
	 * @see ProcessFragment#addSubFragment(RelationType, InteractionFragment)
	 */
	@Override
	public void addSubFragment(
		RelationType<List<RelationType<?>>> rFragmentParam,
		InteractionFragment					rSubFragment)
	{
		addSubFragment(rFragmentParam, rSubFragment, true);
	}

	/***************************************
	 * Convenience method for the common case where the same sub-fragment type
	 * needs to be added multiple times to this parent fragment. The name of the
	 * fragment will be created from the fragment class and the given index
	 * (which must be different for each new instance). The fragment class name
	 * will also be set as the resource ID.
	 *
	 * @param  nIndex       The fragment index
	 * @param  rSubFragment The fragment to add
	 *
	 * @return The wrapper for the fragment parameter
	 *
	 * @see    #addSubFragment(String, InteractionFragment)
	 */
	public ParameterList addSubFragment(
		int					nIndex,
		InteractionFragment rSubFragment)
	{
		String sName = rSubFragment.getClass().getSimpleName();

		return addSubFragment(sName + nIndex, rSubFragment).resid(sName);
	}

	/***************************************
	 * Adds a subordinate fragment to this instance into a temporary parameter
	 * and optionally displays it. The temporary parameter relation type will be
	 * created with the given name by invoking {@link #listParam(String, Class)}
	 * and the parameter wrapper will be returned. The fragment will be added by
	 * invoking {@link #addSubFragment(RelationType, InteractionFragment)}.
	 * Furthermore the UI property {@link UserInterfaceProperties#HIDE_LABEL}
	 * will be set on the new fragment parameter because fragments are typically
	 * displayed without a label.
	 *
	 * @param  sName        The name of the temporary fragment parameter
	 * @param  rSubFragment The fragment to add
	 *
	 * @return The wrapper for the fragment parameter
	 */
	public ParameterList addSubFragment(
		String				sName,
		InteractionFragment rSubFragment)
	{
		ParameterList rSubFragmentParam = panel(sName).hideLabel();

		addSubFragment(rSubFragmentParam.type(), rSubFragment);

		return rSubFragmentParam;
	}

	/***************************************
	 * Adds a child fragment that shall be displayed as a view.
	 *
	 * @see #addSubFragment(RelationType, InteractionFragment)
	 */
	public void addViewFragment(
		RelationType<List<RelationType<?>>> rViewFragmentParamType,
		InteractionFragment					rSubFragment)
	{
		boolean bModified =
			getProcessStep().isParameterModified(fragmentParam().type());

		getRoot().addSubFragment(rViewFragmentParamType, rSubFragment, false);
		get(VIEW_PARAMS).add(rViewFragmentParamType);

		if (!bModified)
		{
			getProcessStep().removeParameterModification(fragmentParam());
		}
	}

	/***************************************
	 * Applies all coupled parameters by setting their value onto their coupled
	 * targets.
	 *
	 * @see RelationCoupling#setAll(org.obrel.core.Relatable, Collection)
	 */
	public void applyAllCoupledParameters()
	{
		RelationCoupling.setAll(getProcess(), getInteractionParameters());
	}

	/***************************************
	 * Internal method that will be invoked to attach this fragment to the given
	 * process step and fragment parameter. Multiple invocations are possible.
	 *
	 * @param rProcessStep   The process step to attach this instance to
	 * @param rFragmentParam The parameter this fragment will be stored in
	 */
	public void attach(
		Interaction							rProcessStep,
		RelationType<List<RelationType<?>>> rFragmentParam)
	{
		this.rFragmentParam = new ParameterList(this, rFragmentParam, true);

		// reset internal state in the case of re-invocation caused by process
		// navigation
		aInputParams.clear();
		aInteractionParams.clear();
		nNextParameterId = 0;
		bInitialized     = false;

		setProcessStep(rProcessStep);
	}

	/***************************************
	 * Returns a parameter that represents a single UI button with a text label.
	 *
	 * @param  sText The button text
	 *
	 * @return The new parameter
	 */
	public Parameter<String> button(String sText)
	{
		return param(String.class).input()
								  .value(sText)
								  .set(HIDE_LABEL)
								  .buttonStyle(ButtonStyle.DEFAULT);
	}

	/***************************************
	 * Creates a parameter that displays interactive buttons from an enum.
	 *
	 * @param  rEnumClass The enum class to create the buttons from
	 *
	 * @return The new parameter
	 */
	public <E extends Enum<E>> EnumParameter<E> buttons(Class<E> rEnumClass)
	{
		return enumParam(rEnumClass).buttons();
	}

	/***************************************
	 * Creates a parameter that displays interactive buttons from certain values
	 * of an enum.
	 *
	 * @param  rAllowedValues The enum values to create the buttons from (must
	 *                        not be empty)
	 *
	 * @return The new parameter
	 */
	@SafeVarargs
	public final <E extends Enum<E>> EnumParameter<E> buttons(
		E... rAllowedValues)
	{
		return enumParam(getValueDatatype(rAllowedValues[0])).buttons(
			rAllowedValues);
	}

	/***************************************
	 * Create a parameter that displays a chart for a certain set of data.
	 *
	 * @param  sName      The parameter name
	 * @param  eChartType The initial type of the chart
	 * @param  rDataSet   The data set for the chart
	 *
	 * @return The new parameter
	 */
	public <T, D extends DataSet<T>> DataSetParameter<T, D> chart(
		String    sName,
		ChartType eChartType,
		D		  rDataSet)
	{
		@SuppressWarnings("unchecked")
		RelationType<D> rParamType =
			getTemporaryParameterType(sName, (Class<D>) rDataSet.getClass());

		return new DataSetParameter<T, D>(this, rParamType).value(rDataSet)
														   .chartType(
												   			eChartType);
	}

	/***************************************
	 * Creates a boolean parameter that displays a checkbox for the input of a
	 * boolean value. The value will initially be set to FALSE.
	 *
	 * @param  sName The name of the parameter (used for the checkbox label)
	 *
	 * @return The new parameter
	 */
	public Parameter<Boolean> checkBox(String sName)
	{
		return flagParam(sName).input().hideLabel().value(Boolean.FALSE);
	}

	/***************************************
	 * Creates a boolean parameter that displays a checkbox for the input of a
	 * boolean value with a certain style.
	 *
	 * @param  sName  The name of the parameter (used for the checkbox label)
	 * @param  eStyle The checkbox style
	 *
	 * @return The new parameter
	 */
	public Parameter<Boolean> checkBox(String sName, CheckBoxStyle eStyle)
	{
		return checkBox(sName).set(CHECK_BOX_STYLE, eStyle);
	}

	/***************************************
	 * Creates a parameter that displays checkboxes for the selection of
	 * multiple enum values.
	 *
	 * @param  rEnumClass The enum class to create the checkboxes parameter for
	 *
	 * @return The new parameter
	 */
	public <E extends Enum<E>> SetParameter<E> checkBoxes(Class<E> rEnumClass)
	{
		SetParameter<E> aCheckBoxes =
			setParam(rEnumClass.getSimpleName(), rEnumClass, true);

		return aCheckBoxes.input()
						  .set(LIST_STYLE, ListStyle.DISCRETE)
						  .layout(LayoutType.TABLE)
						  .columns(1);
	}

	/***************************************
	 * Can be overridden by subclasses to perform resource cleanups when the
	 * process ends. The default implementation does nothing.
	 */
	public void cleanup()
	{
	}

	/***************************************
	 * Clears lists returned by the methods {@link #getInteractionParameters()}
	 * and {@link #getInputParameters()}. These lists must therefore be mutable!
	 *
	 * <p>This implementation replaces the base class implementation because the
	 * parent method changes the interaction parameters of the process step.</p>
	 */
	@Override
	public void clearInteractionParameters()
	{
		getInteractionParameters().clear();
		getInputParameters().clear();
	}

	/***************************************
	 * Clear the selection of a certain parameter by setting it's value to NULL
	 * and the property {@link UserInterfaceProperties#CURRENT_SELECTION} to -1.
	 *
	 * @param rParam The parameter to clear the selection of
	 */
	public void clearSelection(RelationType<?> rParam)
	{
		Object  rParamValue     = getParameter(rParam);
		boolean bClearSelection = (rParamValue != null);

		if (SelectionDataElement.class.isAssignableFrom(rParam.getTargetType()))
		{
			SelectionDataElement rElement = (SelectionDataElement) rParamValue;

			if (!SelectionDataElement.NO_SELECTION.equals(rElement.getValue()))
			{
				rElement.setValue(SelectionDataElement.NO_SELECTION);
				bClearSelection = true;
			}
		}
		else
		{
			setParameter(rParam, null);
		}

		// only clear selection if one exists to prevent unnecessary updates
		if (bClearSelection)
		{
			setUIProperty(-1, CURRENT_SELECTION, rParam);
		}
	}

	/***************************************
	 * Creates a new temporary parameter relation type for text input with a
	 * combo box that combines an editable text box with a drop-down list of
	 * value presets.
	 *
	 * @param  sName         The name of the parameter
	 * @param  rPresetValues The preset values the user can select from
	 *
	 * @return The new parameter
	 */
	public final Parameter<String> comboBox(
		String			   sName,
		Collection<String> rPresetValues)
	{
		return inputText(sName).set(LIST_STYLE, ListStyle.EDITABLE)
							   .allow(rPresetValues);
	}

	/***************************************
	 * Convenience method to create a new temporary parameter relation type with
	 * a string datatype.
	 *
	 * @see #param(String, Class)
	 */
	public Parameter<Date> dateParam(String sName)
	{
		return param(sName, Date.class);
	}

	/***************************************
	 * Overridden to forward the call to the enclosing process step.
	 *
	 * @see ProcessStep#deleteRelation(Relation)
	 */
	@Override
	public void deleteRelation(Relation<?> rRelation)
	{
		rProcessStep.deleteRelation(rRelation);
	}

	/***************************************
	 * Creates a new anonymous parameter type for the display of a value. This
	 * is just a shortcut for the invocation of {@link #param(String, Class)}
	 * with the name argument set to NULL.
	 *
	 * @param  rDatatype The datatype of the values to display
	 *
	 * @return The parameter wrapper
	 */
	public <T> Parameter<T> display(Class<T> rDatatype)
	{
		return param(null, rDatatype);
	}

	/***************************************
	 * Creates a new temporary parameter relation type for the selection of an
	 * enum value from a drop down box.
	 *
	 * @param  rEnumClass The enum class to display the values of
	 *
	 * @return The new parameter
	 */
	public final <E extends Enum<E>> Parameter<E> dropDown(Class<E> rEnumClass)
	{
		return dropDown(rEnumClass.getSimpleName(), EnumSet.allOf(rEnumClass));
	}

	/***************************************
	 * Creates a new temporary parameter relation type for the selection of
	 * values from a drop down box. The first value will be used to determine
	 * the datatype of the parameter type and it will be preset as the parameter
	 * value.
	 *
	 * @param  sName          The name of the parameter
	 * @param  rAllowedValues The values to be displayed in the drop down box
	 *                        (must not be empty)
	 *
	 * @return The new parameter
	 */
	public final <T> Parameter<T> dropDown(
		String		  sName,
		Collection<T> rAllowedValues)
	{
		T		 rFirstValue = CollectionUtil.firstElementOf(rAllowedValues);
		Class<T> rDatatype   = getValueDatatype(rFirstValue);

		return input(sName, rDatatype).set(LIST_STYLE, ListStyle.DROP_DOWN)
									  .value(rFirstValue)
									  .allow(rAllowedValues);
	}

	/***************************************
	 * Enables or disables the editing of this fragment and of all it's
	 * children. This is achieved by clearing or setting the flag property
	 * {@link UserInterfaceProperties#DISABLED} on the fragment input
	 * parameters. Subclasses may override this method to implement a more
	 * specific handling but should normally also call the superclass
	 * implementation.
	 *
	 * @param bEnable TRUE to enable editing, FALSE to disable
	 */
	public void enableEdit(boolean bEnable)
	{
		setEnabled(bEnable, getInputParameters());

		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rSubFragment.enableEdit(bEnable);
		}
	}

	/***************************************
	 * Create a new parameter wrapper for entity process parameters that is
	 * named after the entity type.
	 *
	 * @see #entityParam(String, Class)
	 */
	public <E extends Entity> EntityParameter<E> entityParam(
		Class<E> rEntityType)
	{
		return entityParam(rEntityType.getSimpleName(), rEntityType);
	}

	/***************************************
	 * Create a new parameter wrapper for entity process parameters.
	 *
	 * @param  sName       The name of the parameter relation type
	 * @param  rEntityType The entity type for the parameter
	 *
	 * @return the entity parameter wrapper
	 *
	 * @see    #param(String, Class)
	 */
	public <E extends Entity> EntityParameter<E> entityParam(
		String   sName,
		Class<E> rEntityType)
	{
		RelationType<E> rParamType =
			getTemporaryParameterType(sName, rEntityType);

		return new EntityParameter<>(this, rParamType);
	}

	/***************************************
	 * Convenience method to create a new temporary parameter relation type with
	 * an enum datatype. The parameter will be named with the simple name of the
	 * enum class.
	 *
	 * @see #param(String, Class)
	 */
	public <E extends Enum<E>> EnumParameter<E> enumParam(Class<E> rEnumClass)
	{
		return new EnumParameter<>(
			this,
			getTemporaryParameterType(rEnumClass.getSimpleName(), rEnumClass));
	}

	/***************************************
	 * @see ProcessFragment#executeCleanupActions()
	 */
	@Override
	public void executeCleanupActions()
	{
		super.executeCleanupActions();

		for (InteractionFragment rFragment : getSubFragments())
		{
			rFragment.executeCleanupActions();
		}
	}

	/***************************************
	 * Can be overridden by a fragment to execute actions when the process flow
	 * leaves this fragment.
	 *
	 * <p>The default implementation does nothing.</p>
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	public void finish() throws Exception
	{
	}

	/***************************************
	 * Convenience method to create a new temporary parameter relation type with
	 * a boolean datatype.
	 *
	 * @see #param(String, Class)
	 */
	public Parameter<Boolean> flagParam(String sName)
	{
		return param(sName, Boolean.class);
	}

	/***************************************
	 * Returns a parameter wrapper for the relation type this fragment is stored
	 * in.
	 *
	 * @return the parameter wrapper for the fragment parameter
	 */
	public ParameterList fragmentParam()
	{
		return rFragmentParam;
	}

	/***************************************
	 * Overridden to forward the call to the enclosing process step.
	 *
	 * @see ProcessStep#get(RelationType)
	 */
	@Override
	public <T> T get(RelationType<T> rType)
	{
		return rProcessStep.get(rType);
	}

	/***************************************
	 * Returns the parameter this fragment is displayed in.
	 *
	 * @return The fragment parameter
	 */
	public final RelationType<List<RelationType<?>>> getFragmentParameter()
	{
		return rFragmentParam.type();
	}

	/***************************************
	 * Returns the collection of input parameters of this fragment. These must
	 * be a subset of {@link #getInteractionParameters()}. The default
	 * implementation returns a mutable collection that can been modified
	 * directly by a subclass. Or it can be overridden by subclasses to return
	 * their own input parameter collection.
	 *
	 * @return The list of this fragment's input parameters
	 */
	public Collection<RelationType<?>> getInputParameters()
	{
		return aInputParams;
	}

	/***************************************
	 * Returns the list of interaction parameters for this fragment. The default
	 * implementation returns a mutable list that can been modified directly by
	 * a subclass. Or it can be overridden by subclasses to return their own
	 * interaction parameter list.
	 *
	 * @return The list of this fragment's interaction parameters
	 */
	public List<RelationType<?>> getInteractionParameters()
	{
		return aInteractionParams;
	}

	/***************************************
	 * Sets the interaction handler for a certain parameter.
	 *
	 * @see Interaction#getParameterInteractionHandler(RelationType)
	 */
	public InteractionHandler getParameterInteractionHandler(
		RelationType<?> rParam)
	{
		return getProcessStep().getParameterInteractionHandler(rParam);
	}

	/***************************************
	 * Returns the parent fragment of this instance.
	 *
	 * @return The parent fragment or NULL for a root fragment
	 */
	public final InteractionFragment getParent()
	{
		return rParent;
	}

	/***************************************
	 * @see ProcessFragment#getProcess()
	 */
	@Override
	public Process getProcess()
	{
		return rProcessStep.getProcess();
	}

	/***************************************
	 * Returns the interactive process step this element is associated with.
	 *
	 * @return The process step this fragment belongs to
	 */
	@Override
	public final Interaction getProcessStep()
	{
		return rProcessStep;
	}

	/***************************************
	 * Overridden to forward the call to the enclosing process step.
	 *
	 * @see ProcessStep#getRelation(RelationType)
	 */
	@Override
	public <T> Relation<T> getRelation(RelationType<T> rType)
	{
		return rProcessStep.getRelation(rType);
	}

	/***************************************
	 * Overridden to forward the call to the enclosing process step.
	 *
	 * @see ProcessStep#getRelations(Predicate)
	 */
	@Override
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> rFilter)
	{
		return rProcessStep.getRelations(rFilter);
	}

	/***************************************
	 * Must be implemented by subclasses to handle interactions for this
	 * fragment. The default implementation does nothing.
	 *
	 * @param  rInteractionParam The interaction parameter
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
	}

	/***************************************
	 * Checks whether an interaction has been caused by an interaction parameter
	 * from this fragment. The default implementation checks if the given
	 * parameter is one of this fragment's interaction parameters.
	 *
	 * @param  rInteractionParam The interaction parameter to check
	 *
	 * @return TRUE if the interaction was caused by a parameter of this
	 *         fragment
	 */
	public boolean hasInteraction(RelationType<?> rInteractionParam)
	{
		return getInputParameters().contains(rInteractionParam);
	}

	/***************************************
	 * Creates a parameter for an empty label with the {@link LabelStyle#ICON}
	 * that displays a certain icon.
	 *
	 * @param  rIconIdentifier An identifier that describes the icon to display;
	 *                         will be converted to a string and should
	 *                         typically either be a string of an enum constant
	 *
	 * @return The new parameter
	 */
	public Parameter<String> icon(Object rIconIdentifier)
	{
		return label("", LabelStyle.ICON).icon(rIconIdentifier);
	}

	/***************************************
	 * Creates a parameter that displays interactive buttons for certain enum
	 * values as icons.
	 *
	 * @param  rAllowedValues rEnumClass The enum class to create the buttons
	 *                        from
	 *
	 * @return The new parameter
	 */
	@SafeVarargs
	public final <E extends Enum<E>> EnumParameter<E> iconButtons(
		E... rAllowedValues)
	{
		return buttons(rAllowedValues).buttonStyle(ButtonStyle.ICON).images();
	}

	/***************************************
	 * Creates a parameter that displays interactive buttons from an enum as
	 * icons.
	 *
	 * @param  rEnumClass The enum class to create the buttons from
	 *
	 * @return The new parameter
	 */
	public <E extends Enum<E>> EnumParameter<E> iconButtons(Class<E> rEnumClass)
	{
		return buttons(rEnumClass).buttonStyle(ButtonStyle.ICON).images();
	}

	/***************************************
	 * Creates a parameter that displays an image.
	 *
	 * @param  sImageName The image name
	 *
	 * @return The new parameter
	 */
	public Parameter<String> image(String sImageName)
	{
		return label(sImageName, LabelStyle.IMAGE);
	}

	/***************************************
	 * Creates a parameter that displays interactive buttons from an enum with
	 * images.
	 *
	 * @param  rEnumClass The enum class to create the buttons from
	 *
	 * @return The new parameter
	 */
	public <E extends Enum<E>> EnumParameter<E> imageButtons(
		Class<E> rEnumClass)
	{
		return buttons(rEnumClass).images();
	}

	/***************************************
	 * Initializes a parameter for the display of a storage query.
	 *
	 * @param  rParam       The parameter to initialize the query for
	 * @param  rEntityClass The entity class to query
	 * @param  pCriteria    The query criteria the query criteria or NULL for
	 *                      none
	 * @param  pSortOrder   The sort predicate or NULL for the default order
	 * @param  eMode        bHierarchical TRUE for a hierarchical query with a
	 *                      tree-table display
	 * @param  rColumns     The columns to display
	 *
	 * @return The generated query predicate
	 */
	public <E extends Entity> QueryPredicate<E> initQueryParameter(
		RelationType<E>			  rParam,
		Class<E>				  rEntityClass,
		Predicate<? super E>	  pCriteria,
		Predicate<? super Entity> pSortOrder,
		HierarchicalQueryMode	  eMode,
		RelationType<?>... 		  rColumns)
	{
		QueryPredicate<E> qEntities = forEntity(rEntityClass, pCriteria);

		qEntities.set(HIERARCHICAL_QUERY_MODE, eMode);
		annotateForEntityQuery(rParam, qEntities, pSortOrder, rColumns);

		return qEntities;
	}

	/***************************************
	 * Creates an input parameter for a certain datatype. This method first
	 * invokes {@link #param(RelationType)} and then {@link Parameter#input()}.
	 *
	 * @param  rParam The parameter to wrap
	 *
	 * @return A new parameter instance
	 */
	public <T> Parameter<T> input(RelationType<T> rParam)
	{
		return param(rParam).input();
	}

	/***************************************
	 * Creates an anonymous input parameter for a certain datatype. The label of
	 * the parameter will be hidden because the parameter has no name so that no
	 * label resource will be available.
	 *
	 * @param  rDatatype The datatype class
	 *
	 * @return A new parameter instance
	 */
	public <T> Parameter<T> input(Class<T> rDatatype)
	{
		return input(null, rDatatype).hideLabel();
	}

	/***************************************
	 * Creates an input parameter for a certain datatype. This method combines
	 * {@link #param(String, Class)} and {@link Parameter#input()}.
	 *
	 * @param  sName     The name of the input parameter
	 * @param  rDatatype The datatype class
	 *
	 * @return A new parameter instance
	 */
	public <T> Parameter<T> input(String sName, Class<T> rDatatype)
	{
		return param(sName, rDatatype).input();
	}

	/***************************************
	 * Creates a new process parameter for the input of an entity attribute that
	 * is named like the original attribute relation type.
	 *
	 * @param  rAttribute The attribute to edit
	 *
	 * @return The parameter wrapper
	 */
	public <E extends Entity, T> EntityAttributeParameter<E, T> inputAttr(
		RelationType<T> rAttribute)
	{
		return inputAttr(null, rAttribute);
	}

	/***************************************
	 * Creates a new process parameter for the input of an entity attribute.
	 *
	 * @param  sName      The name of the parameter
	 * @param  rAttribute The attribute to edit
	 *
	 * @return The parameter wrapper
	 */
	public <E extends Entity, T> EntityAttributeParameter<E, T> inputAttr(
		String			sName,
		RelationType<T> rAttribute)
	{
		RelationType<T> rDerivedParam =
			getTemporaryParameterType(sName, rAttribute);

		EntityAttributeParameter<E, T> rParam =
			new EntityAttributeParameter<>(this, rDerivedParam);

		return rParam.input();
	}

	/***************************************
	 * Creates a parameter for a date input field.
	 *
	 * @param  sName The name of the input parameter
	 *
	 * @return The label parameter
	 */
	public Parameter<Date> inputDate(String sName)
	{
		return input(sName, Date.class).set(
			DATE_INPUT_TYPE,
			DateInputType.INPUT_FIELD);
	}

	/***************************************
	 * Creates an anonymous parameter for a text input field.
	 *
	 * @param  rPresetValues The preset values the user can select from
	 *
	 * @return The label parameter
	 */
	public SetParameter<String> inputTags(Collection<String> rPresetValues)
	{
		SetParameter<String> aSetParam = setParam(null, String.class, true);

		return aSetParam.input()
						.set(LIST_STYLE, ListStyle.EDITABLE)
						.allowElements(rPresetValues);
	}

	/***************************************
	 * Creates an anonymous parameter for a text input field.
	 *
	 * @param  sName The name of the input parameter
	 *
	 * @return The label parameter
	 */
	public Parameter<String> inputText(String sName)
	{
		return input(sName, String.class);
	}

	/***************************************
	 * Creates an anonymous parameter for multi-line text input (a text area).
	 *
	 * @param  sName The name of the input parameter
	 *
	 * @return The label parameter
	 */
	public Parameter<String> inputTextLines(String sName)
	{
		return inputText(sName).rows(-1);
	}

	/***************************************
	 * @see #insertInputParameters(RelationType, RelationType...)
	 */
	public void insertInputParameters(
		RelationType<?>    rBeforeParam,
		RelationType<?>... rParams)
	{
		insertInputParameters(rBeforeParam, Arrays.asList(rParams));
	}

	/***************************************
	 * Inserts additional parameters into the lists returned by the methods
	 * {@link #getInteractionParameters()} and {@link #getInputParameters()}.
	 * These lists must therefore be mutable!
	 *
	 * @param rBeforeParam The parameter to insert the other parameters before
	 * @param rParams      The parameters to add
	 */
	public void insertInputParameters(
		RelationType<?>				rBeforeParam,
		Collection<RelationType<?>> rParams)
	{
		CollectionUtil.insert(
			getInteractionParameters(),
			rBeforeParam,
			rParams);
		getInputParameters().addAll(rParams);
	}

	/***************************************
	 * Convenience method to create a new temporary parameter relation type with
	 * an integer datatype.
	 *
	 * @see #param(String, Class)
	 */
	public Parameter<Integer> intParam(String sName)
	{
		return param(sName, Integer.class);
	}

	/***************************************
	 * Checks whether this fragment is (still) attached to a process.
	 *
	 * @return TRUE if the fragment is attached
	 */
	public boolean isAttached()
	{
		return rProcessStep != null;
	}

	/***************************************
	 * Checks whether this instance has already been initialized, i.e. returned
	 * from it's {@link #init()} method.
	 *
	 * @return TRUE if this instance has been completely initialized
	 */
	public final boolean isInitialized()
	{
		return bInitialized;
	}

	/***************************************
	 * Creates a parameter that displays a label string with the default label
	 * style.
	 *
	 * @param  sLabelText The label text
	 *
	 * @return The label parameter
	 */
	public Parameter<String> label(String sLabelText)
	{
		return label(sLabelText, null);
	}

	/***************************************
	 * Creates a new display parameter for a string value with a certain label
	 * style.
	 *
	 * @param  sLabelText The label text
	 * @param  eStyle     The style of the label
	 *
	 * @return The new parameter
	 */
	public Parameter<String> label(String sLabelText, LabelStyle eStyle)
	{
		return textParam(null).set(LABEL_STYLE, eStyle)
							  .hideLabel()
							  .value(sLabelText);
	}

	/***************************************
	 * Sets the layout of this fragment.
	 *
	 * @param  eLayout The layout
	 *
	 * @return The parameter list of this fragment for concatenation
	 */
	public ParameterList layout(LayoutType eLayout)
	{
		return fragmentParam().layout(eLayout);
	}

	/***************************************
	 * Create a new temporary relation type with a {@link List} datatype and
	 * returns a parameter wrapper for it.
	 *
	 * @param  sName        The name of the relation type
	 * @param  rElementType The datatype of the list elements
	 *
	 * @return the parameter instance
	 */
	public <T> ListParameter<T> listParam(
		String			 sName,
		Class<? super T> rElementType)
	{
		return new ListParameter<>(
			this,
			getTemporaryListType(sName, rElementType));
	}

	/***************************************
	 * Tries to lock an entity during the remaining execution of the current
	 * process and displays an information message if the entity is already
	 * locked by some other context. The lock will automatically be removed if
	 * the process is terminated in any way but. The lock can also be removed
	 * explicitly by calling {@link Process#unlockEntity(Entity)}. Because the
	 * lock is process-wide this method should also be invoked in
	 * implementations of {@link #abort()} and {@link #rollback()} to handle
	 * process rollbacks.
	 *
	 * @param  rEntity                 The entity to lock
	 * @param  sLockUnavailableMessage The message to display if the lock
	 *                                 couldn't be acquired
	 *
	 * @return TRUE if the lock could be acquired
	 */
	public boolean lockEntityForProcess(
		Entity rEntity,
		String sLockUnavailableMessage)
	{
		return lockEntity(rEntity, sLockUnavailableMessage, true);
	}

	/***************************************
	 * Tries to lock an entity during the execution of the current step and
	 * displays an information message if the entity is already locked by some
	 * other context. The lock will automatically be removed if the process
	 * progresses to another step (including rollback) or is terminated in any
	 * way. The lock can also be removed explicitly by calling {@link
	 * #unlockEntity(Entity)}.
	 *
	 * @param  rEntity                 The entity to lock
	 * @param  sLockUnavailableMessage The message to display if the lock
	 *                                 couldn't be acquired
	 *
	 * @return TRUE if the lock could be acquired
	 */
	public boolean lockEntityForStep(
		Entity rEntity,
		String sLockUnavailableMessage)
	{
		return lockEntity(rEntity, sLockUnavailableMessage, false);
	}

	/***************************************
	 * Marks the input parameters of this fragment and all of it's
	 * sub-fragments.
	 */
	public void markFragmentInputParams()
	{
		get(INPUT_PARAMS).addAll(getInputParameters());

		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rSubFragment.markFragmentInputParams();
		}
	}

	/***************************************
	 * Overridden to operate on the fragment input parameters.
	 *
	 * @see ProcessElement#markInputParams(boolean, Collection)
	 */
	@Override
	public void markInputParams(
		boolean								  bInput,
		Collection<? extends RelationType<?>> rParams)
	{
		Collection<RelationType<?>> rInputParams = getInputParameters();

		for (RelationType<?> rParam : rParams)
		{
			boolean bHasParam = rInputParams.contains(rParam);

			if (!bHasParam && bInput)
			{
				rInputParams.add(rParam);
			}
			else if (bHasParam && !bInput)
			{
				rInputParams.remove(rParam);
			}
		}

		super.markInputParams(bInput, rParams);
	}

	/***************************************
	 * Marks a hierarchy of parameters as modified.
	 *
	 * @param rParams The list of root parameters
	 */
	public void markParameterHierarchyAsModified(
		Collection<RelationType<?>> rParams)
	{
		for (RelationType<?> rParam : rParams)
		{
			markParameterAsModified(rParam);

			if (Collection.class.isAssignableFrom(rParam.getTargetType()))
			{
				if (rParam.get(MetaTypes.ELEMENT_DATATYPE) ==
					RelationType.class)
				{
					@SuppressWarnings("unchecked")
					Collection<RelationType<?>> rChildParams =
						(Collection<RelationType<?>>) getParameter(rParam);

					if (rChildParams != null)
					{
						markParameterHierarchyAsModified(rChildParams);
					}
				}
			}
		}
	}

	/***************************************
	 * Notifies all listeners for parameter updates that are registered in the
	 * relation {@link ProcessRelationTypes#PARAM_UPDATE_LISTENERS} of this
	 * fragment's process step. Because relations are shared between fragments
	 * this will affect all fragments in the current interaction.
	 */
	public void notifyParameterUpdateListeners()
	{
		if (hasRelation(PARAM_UPDATE_LISTENERS))
		{
			for (Updatable rListener : get(PARAM_UPDATE_LISTENERS))
			{
				rListener.update();
			}
		}
	}

	/***************************************
	 * Adds another fragment as a subordinate panel of this fragment. This is
	 * just a semantic variant of {@link #addSubFragment(InteractionFragment)}.
	 *
	 * @param  rPanelFragment The panel fragment to add
	 *
	 * @return The parameter wrapper for the panel
	 */
	public ParameterList panel(InteractionFragment rPanelFragment)
	{
		return addSubFragment(rPanelFragment);
	}

	/***************************************
	 * Adds a new anonymous panel fragment with a grid layout.
	 *
	 * @param  rInitializer The fragment initializer
	 *
	 * @return the parameter wrapper for the panel parameter
	 *
	 * @see    #panel(String, Initializer)
	 */
	public ParameterList panel(Initializer<InteractionFragment> rInitializer)
	{
		return panel(null, LayoutType.GRID, rInitializer);
	}

	/***************************************
	 * Creates a new temporary relation type for a list of relation types that
	 * will be rendered in a panel without a separate fragment.
	 *
	 * @param  sName The name of the parameter list
	 *
	 * @return the parameter wrapper for the panel parameter
	 */
	public ParameterList panel(String sName)
	{
		RelationType<List<RelationType<?>>> rListType =
			getTemporaryListType(sName, RelationType.class);

		return new ParameterList(this, rListType, false).input();
	}

	/***************************************
	 * Adds a new named panel fragment with a grid layout.
	 *
	 * @param  rInitializer The fragment initializer
	 *
	 * @return the parameter wrapper for the panel parameter
	 *
	 * @see    #panel(String, LayoutType, Initializer)
	 */
	public ParameterList panel(
		String							 sName,
		Initializer<InteractionFragment> rInitializer)
	{
		return panel(sName, LayoutType.GRID, rInitializer);
	}

	/***************************************
	 * Adds a panel that contains components from the process UI API.
	 *
	 * @param  rLayout     The UI layout of the panel
	 * @param  fBuildPanel A builder function that creates the panel components
	 *
	 * @return the parameter wrapper for the panel parameter
	 */
	public ParameterList panel(
		UiLayout			   rLayout,
		Consumer<UiBuilder<?>> fBuildPanel)
	{
		@SuppressWarnings("serial")
		ParameterList aPanel =
			addSubFragment(new UiRootFragment(rLayout, fBuildPanel));

		return aPanel;
	}

	/***************************************
	 * Creates a new anonymous interaction fragment and the associated parameter
	 * relation type. The initializer argument must perform the initialization
	 * of the new fragment which it receives as the argument to it's {@link
	 * Initializer#init(Object)} method.
	 *
	 * <p>This method is mainly intended to be used with lambda expressions
	 * introduced with Java 8. In that case it allows concise in-line
	 * declarations of panels by simply forwarding the initialization to a
	 * corresponding method in form of a method reference with an {@link
	 * InteractionFragment} parameter.</p>
	 *
	 * @param  sName        The name of the fragment parameter
	 * @param  ePanelLayout The layout of the panel fragment
	 * @param  rInitializer The fragment initializer
	 *
	 * @return A new parameter wrapper for the panel parameter
	 */
	@SuppressWarnings("serial")
	public ParameterList panel(
		String								   sName,
		LayoutType							   ePanelLayout,
		final Initializer<InteractionFragment> rInitializer)
	{
		ParameterList aPanel =
			addSubFragment(
				sName,
				new InteractionFragment()
				{
					@Override
					public void init() throws Exception
					{
						rInitializer.init(this);
					}
				});

		// only set if the panel hasn't set it's own layout (possible if it has
		// been added to an existing fragment and then initialized immediately)
		if (!aPanel.has(LAYOUT))
		{
			aPanel.layout(ePanelLayout);
		}

		return aPanel;
	}

	/***************************************
	 * Creates a new parameter wrapper for the given relation type in this
	 * fragment.
	 *
	 * @param  rParam The parameter to wrap
	 *
	 * @return A new parameter instance
	 */
	public <T> Parameter<T> param(RelationType<T> rParam)
	{
		return new Parameter<>(this, rParam);
	}

	/***************************************
	 * Returns a new anonymous parameter for a certain datatype.
	 *
	 * @param  rDatatype The datatype class
	 *
	 * @return The new parameter wrapper
	 */
	public <T> Parameter<T> param(Class<? super T> rDatatype)
	{
		return param(null, rDatatype);
	}

	/***************************************
	 * Create a new parameter wrapper for this fragment with a temporary
	 * relation type. If no matching temporary relation type exists already it
	 * will be created.
	 *
	 * @param  sName     The name of the parameter relation type
	 * @param  rDatatype The datatype class
	 *
	 * @return the parameter wrapper
	 */
	public <T> Parameter<T> param(String sName, Class<? super T> rDatatype)
	{
		RelationType<T> rParam = getTemporaryParameterType(sName, rDatatype);

		return param(rParam).display();
	}

	/***************************************
	 * Returns a parameter for a derived temporary parameter type created by
	 * {@link #getTemporaryParameterType(String, RelationType)}.
	 *
	 * @param  rOriginalType The original relation type the new parameter is
	 *                       based on
	 *
	 * @return A new parameter wrapper for the derived relation type
	 */
	public <T> Parameter<T> paramLike(RelationType<T> rOriginalType)
	{
		return param(getTemporaryParameterType(null, rOriginalType));
	}

	/***************************************
	 * Can be implemented by subclasses to initialize the interaction of this
	 * fragment. This method will be invoked on every iteration of this
	 * fragment's interaction, i.e. on the first run and every time after an
	 * interaction event occurred. The default implementation does nothing.
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	public void prepareInteraction() throws Exception
	{
	}

	/***************************************
	 * Creates a list parameter that displays radio buttons for the selection of
	 * multiple enum values
	 *
	 * @param  rEnumClass The enum class to create the checkboxes for
	 *
	 * @return The new parameter
	 */
	public <E extends Enum<E>> EnumParameter<E> radioButtons(
		Class<E> rEnumClass)
	{
		return enumParam(rEnumClass).input()
									.set(LIST_STYLE, ListStyle.DISCRETE)
									.hideLabel()
									.layout(LayoutType.TABLE)
									.columns(1);
	}

	/***************************************
	 * Re-queries an entity that is stored in certain a process parameter. If
	 * the parameter is NULL it will be ignored.
	 *
	 * @param rEntityParam The parameter relation type under which the entity is
	 *                     stored
	 */
	@SuppressWarnings("unchecked")
	public <E extends Entity> void reloadEntity(RelationType<E> rEntityParam)
	{
		E rEntity = getParameter(rEntityParam);

		if (rEntity != null)
		{
			try
			{
				rEntity =
					(E) EntityManager.queryEntity(
						rEntity.getClass(),
						rEntity.getId());
				setParameter(rEntityParam, rEntity);
			}
			catch (StorageException e)
			{
				throw new ProcessException(this, e);
			}
		}
	}

	/***************************************
	 * Removes parameters from the lists returned by the methods {@link
	 * #getInteractionParameters()} and {@link #getInputParameters()}. These
	 * lists must therefore be mutable!
	 *
	 * <p>This implementation replaces the base class implementation because the
	 * parent method changes the interaction parameters of the process step.</p>
	 *
	 * @param rParams The parameters to remove
	 */
	@Override
	public void removeInteractionParameters(Collection<RelationType<?>> rParams)
	{
		getInteractionParameters().removeAll(rParams);
		getInputParameters().removeAll(rParams);
		structureModified();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public InteractionFragment removeSubFragment(
		RelationType<List<RelationType<?>>> rSubFragmentParamType)
	{
		return removeSubFragment(rSubFragmentParamType, true);
	}

	/***************************************
	 * Removes the fragment of a child view from this fragment.
	 *
	 * @param rViewFragmentParamType The parameter type of the view fragment
	 */
	public void removeViewFragment(
		RelationType<List<RelationType<?>>> rViewFragmentParamType)
	{
		boolean bModified =
			getProcessStep().isParameterModified(fragmentParam().type());

		getRoot().removeSubFragment(rViewFragmentParamType, false);
		get(VIEW_PARAMS).remove(rViewFragmentParamType);

		if (!bModified)
		{
			getProcessStep().removeParameterModification(fragmentParam());
		}
	}

	/***************************************
	 * Overridden to forward the call to the enclosing process step.
	 *
	 * @see ProcessStep#set(RelationType, Object)
	 */
	@Override
	public <T> Relation<T> set(RelationType<T> rType, T rTarget)
	{
		return rProcessStep.set(rType, rTarget);
	}

	/***************************************
	 * Overridden to remember the continuation parameters of this fragment.
	 *
	 * @see ProcessFragment#setContinueOnInteraction(boolean, RelationType...)
	 */
	@Override
	public void setContinueOnInteraction(
		boolean			   bContinue,
		RelationType<?>... rParams)
	{
		List<RelationType<?>> aParamList = Arrays.asList(rParams);

		if (bContinue)
		{
			if (aFragmentContinuationParams == null)
			{
				aFragmentContinuationParams = new ArrayList<RelationType<?>>();
			}

			aFragmentContinuationParams.addAll(aParamList);
		}
		else if (aFragmentContinuationParams != null)
		{
			aFragmentContinuationParams.removeAll(aParamList);
		}

		super.setContinueOnInteraction(bContinue, rParams);
	}

	/***************************************
	 * Create a new temporary relation type with a {@link Set} datatype and
	 * returns a parameter wrapper for it.
	 *
	 * @param  sName        The name of the relation type
	 * @param  rElementType The datatype of the set elements
	 * @param  bOrdered     TRUE for a set that keeps the order of it's elements
	 *
	 * @return the parameter instance
	 */
	public <T> SetParameter<T> setParam(String			 sName,
										Class<? super T> rElementType,
										boolean			 bOrdered)
	{
		return new SetParameter<>(
			this,
			getTemporarySetType(sName, rElementType, bOrdered));
	}

	/***************************************
	 * Sets the interaction event handler for a certain process parameter.
	 *
	 * @param rParam              The parameter relation type
	 * @param rInteractionHandler The interaction handler
	 */
	public void setParameterInteractionHandler(
		RelationType<?>    rParam,
		InteractionHandler rInteractionHandler)
	{
		getProcessStep().setParameterInteractionHandler(
			rParam,
			rInteractionHandler);
	}

	/***************************************
	 * Can be overridden to setup the internal state of a new fragment instance.
	 * Other than {@link #init()} this method will only be invoked once, right
	 * after an instance has been added to it's process step. The default
	 * implementation does nothing.
	 */
	public void setup()
	{
	}

	/***************************************
	 * Displays a confirmation message that can either be accepted or rejected.
	 *
	 * @param  sMessage           The message to display
	 * @param  bYesNoQuestion     TRUE for YES and NO dialog buttons, FALSE for
	 *                            OK and CANCEL
	 * @param  rRunOnComfirmation The code to be executed if the user accepts
	 *                            the message
	 *
	 * @return The message box fragment
	 */
	public MessageBoxFragment showConfirmationMessage(
		String		   sMessage,
		boolean		   bYesNoQuestion,
		final Runnable rRunOnComfirmation)
	{
		if (rRunOnComfirmation == null)
		{
			throw new IllegalArgumentException(
				"Runnable parameter must not be NULL");
		}

		return showMessageBox(
			sMessage,
			MESSAGE_BOX_QUESTION_ICON,
			new DialogActionListener()
			{
				@Override
				public void onDialogAction(DialogAction eAction)
				{
					if (eAction == DialogAction.OK ||
						eAction == DialogAction.YES)
					{
						rRunOnComfirmation.run();
					}
				}
			},
			bYesNoQuestion ? DialogAction.YES_NO : DialogAction.OK_CANCEL);
	}

	/***************************************
	 * Adds a sub-fragment to be displayed as a modal dialog.
	 *
	 * @see InteractionFragment#showDialog(String, InteractionFragment, boolean,
	 *      String, DialogActionListener, Collection)
	 */
	public DialogFragment showDialog(String				  sParamNameTemplate,
									 InteractionFragment  rContentFragment,
									 DialogActionListener rDialogListener,
									 DialogAction... 	  rDialogActions)
	{
		return showDialog(
			sParamNameTemplate,
			rContentFragment,
			true,
			rDialogListener,
			Arrays.asList(rDialogActions));
	}

	/***************************************
	 * Adds a sub-fragment to be displayed as a modal dialog.
	 *
	 * @see InteractionFragment#showDialog(String, InteractionFragment, boolean,
	 *      String, DialogActionListener, Collection)
	 */
	public DialogFragment showDialog(
		String					 sParamNameTemplate,
		InteractionFragment		 rContentFragment,
		boolean					 bModal,
		DialogActionListener	 rDialogListener,
		Collection<DialogAction> rDialogActions)
	{
		return showDialog(
			sParamNameTemplate,
			rContentFragment,
			bModal,
			null,
			rDialogListener,
			rDialogActions);
	}

	/***************************************
	 * Adds a sub-fragment to be displayed as a dialog. The parameter for the
	 * dialog fragment will be added automatically to the input parameters of
	 * this instance. Therefore the parameter lists of this instance MUST be
	 * mutable!
	 *
	 * <p>If the creating code needs to programmatically close the dialog view
	 * instead of by a button click of the user it can do so by invoking the
	 * {@link ViewFragment#hide()} method on the returned view fragment instance
	 * on a corresponding interaction.</p>
	 *
	 * @param  sParamNameTemplate The name template to be used for generated
	 *                            dialog parameter names or NULL to derive it
	 *                            from the content fragment
	 * @param  rContentFragment   The fragment to be displayed as the dialog
	 *                            content
	 * @param  bModal             TRUE for a modal view
	 * @param  sQuestion          A string (typically a question) that will be
	 *                            displayed next to the dialog action buttons.
	 * @param  rDialogListener    The dialog action listener or NULL for none
	 * @param  rDialogActions     The actions to be displayed as the dialog
	 *                            buttons
	 *
	 * @return The new dialog fragment instance
	 */
	public DialogFragment showDialog(
		String					 sParamNameTemplate,
		InteractionFragment		 rContentFragment,
		boolean					 bModal,
		String					 sQuestion,
		DialogActionListener	 rDialogListener,
		Collection<DialogAction> rDialogActions)
	{
		DialogFragment aDialog =
			new DialogFragment(
				sParamNameTemplate,
				rContentFragment,
				bModal,
				sQuestion,
				rDialogActions);

		showDialogImpl(aDialog, rDialogListener);

		return aDialog;
	}

	/***************************************
	 * Displays a message with an error icon and a single OK button.
	 *
	 * @see InteractionFragment#showMessageBox(String, String,
	 *      DialogActionListener, Collection, RelationType...)
	 */
	public MessageBoxFragment showErrorMessage(String sMessage)
	{
		return showMessageBox(
			sMessage,
			MESSAGE_BOX_ERROR_ICON,
			null,
			DialogAction.OK);
	}

	/***************************************
	 * Displays a message with an info icon and a single OK button.
	 *
	 * @see InteractionFragment#showMessageBox(String, String,
	 *      DialogActionListener, Collection, RelationType...)
	 */
	public MessageBoxFragment showInfoMessage(String sMessage)
	{
		return showMessageBox(
			sMessage,
			MESSAGE_BOX_INFO_ICON,
			null,
			DialogAction.OK);
	}

	/***************************************
	 * Displays a process message in a message box dialog. The parameter for the
	 * dialog fragment will be added automatically to the input parameters of
	 * this instance. Therefore the parameter lists of this instance MUST be
	 * mutable!
	 *
	 * @see InteractionFragment#showMessageBox(String, String,
	 *      DialogActionListener, Collection, RelationType...)
	 */
	public MessageBoxFragment showMessageBox(
		String				 sMessage,
		String				 sIcon,
		DialogActionListener rDialogListener,
		DialogAction... 	 rDialogActions)
	{
		return showMessageBox(
			sMessage,
			sIcon,
			rDialogListener,
			Arrays.asList(rDialogActions));
	}

	/***************************************
	 * Displays a process message in a message box dialog. The parameter for the
	 * dialog fragment will be added automatically to the input parameters of
	 * this instance. Therefore the parameter lists of this instance MUST be
	 * mutable!
	 *
	 * <p>If one or more extras parameters are given they will be displayed
	 * between the message and the dialog buttons. Any necessary initialization
	 * of these parameters including UI properties must be done by the invoking
	 * code before invoking the message box.</p>
	 *
	 * @param  sMessage        The message to be displayed in the message box
	 * @param  sIcon           The resource name for an icon or NULL for the
	 *                         standard icon.
	 * @param  rDialogListener The dialog action listener or NULL for none
	 * @param  rDialogActions  The actions to be displayed as the message box
	 *                         buttons
	 * @param  rExtraParams    Optional extra parameters to be displayed in the
	 *                         message box
	 *
	 * @return The view fragment that has been created for the message box
	 */
	public MessageBoxFragment showMessageBox(
		String					 sMessage,
		String					 sIcon,
		DialogActionListener	 rDialogListener,
		Collection<DialogAction> rDialogActions,
		RelationType<?>... 		 rExtraParams)
	{
		MessageBoxFragment aMessageBox =
			new MessageBoxFragment(
				sMessage,
				sIcon,
				rDialogActions,
				rExtraParams);

		showDialogImpl(aMessageBox, rDialogListener);

		return aMessageBox;
	}

	/***************************************
	 * Displays a modal dialog with a name prefix that is derived from the name
	 * of the content fragment.
	 *
	 * @see InteractionFragment#showDialog(String, InteractionFragment, boolean,
	 *      String, DialogActionListener, Collection)
	 */
	public DialogFragment showModalDialog(
		InteractionFragment		 rContentFragment,
		Collection<DialogAction> rDialogActions)
	{
		return showDialog(null, rContentFragment, true, null, rDialogActions);
	}

	/***************************************
	 * Adds a sub-fragment to be displayed as a view. The parameter for the view
	 * fragment will be added automatically to the input parameters of this
	 * instance. Therefore the parameter lists of this instance MUST be mutable!
	 *
	 * <p>Because a view has no explicit buttons like dialogs it must be closed
	 * by the creating code by invoking the {@link ViewFragment#hide()} method
	 * on the returned view fragment instance on a corresponding interaction.
	 * </p>
	 *
	 * @param  sParamNameTemplate The name template to be used for generated
	 *                            view parameter names
	 * @param  rContentFragment   The fragment to be displayed as the view
	 *                            content
	 * @param  bModal             TRUE for a modal view
	 *
	 * @return The new view fragment to provide access to it's method {@link
	 *         ViewFragment#hide()}
	 */
	public ViewFragment showView(String				 sParamNameTemplate,
								 InteractionFragment rContentFragment,
								 boolean			 bModal)
	{
		ViewFragment aViewFragment =
			new ViewFragment(
				sParamNameTemplate,
				rContentFragment,
				bModal ? ViewDisplayType.MODAL_VIEW : ViewDisplayType.VIEW);

		aViewFragment.show(this);

		return aViewFragment;
	}

	/***************************************
	 * Displays a message with an warning icon and a single OK button.
	 *
	 * @see InteractionFragment#showMessageBox(String, String,
	 *      DialogActionListener, Collection, RelationType...)
	 */
	public MessageBoxFragment showWarningMessage(String sMessage)
	{
		return showMessageBox(
			sMessage,
			MESSAGE_BOX_WARNING_ICON,
			null,
			DialogAction.OK);
	}

	/***************************************
	 * Marks the parameter of this fragment to indicate a structure modification
	 * by marking it as modified and with {@link
	 * StateProperties#STRUCTURE_CHANGED}.
	 */
	public void structureModified()
	{
		fragmentParam().modified();
		fragmentParam().set(STRUCTURE_CHANGED);
	}

	/***************************************
	 * Convenience method to create a new temporary parameter relation type with
	 * a string datatype.
	 *
	 * @see #param(String, Class)
	 */
	public Parameter<String> textParam(String sName)
	{
		return param(sName, String.class);
	}

	/***************************************
	 * Creates a parameter that displays a title string (i.e. with the style
	 * {@link LabelStyle#TITLE}).
	 *
	 * @param  sTitleText The title text
	 *
	 * @return The label parameter
	 */
	public Parameter<String> title(String sTitleText)
	{
		return label(sTitleText, LabelStyle.TITLE);
	}

	/***************************************
	 * Updates all coupled parameters by retrieving their values from the
	 * coupled sources.
	 *
	 * @param bMarkParamsAsModified If TRUE all interaction parameters of this
	 *                              fragment will be marked as modified
	 *
	 * @see   de.esoco.process.param.ParameterBase#couple(java.util.function.Consumer,
	 *        java.util.function.Supplier)
	 */
	public void updateAllCoupledParameters(boolean bMarkParamsAsModified)
	{
		List<RelationType<?>> rInteractionParams = getInteractionParameters();

		RelationCoupling.getAll(getProcess(), rInteractionParams);

		if (bMarkParamsAsModified)
		{
			for (RelationType<?> rParam : rInteractionParams)
			{
				markParameterAsModified(rParam);
			}
		}
	}

	/***************************************
	 * Request a complete update of this fragment's UI by marking all
	 * interaction parameters including their hierarchy as modified.
	 */
	public void updateUserInterface()
	{
		markParameterHierarchyAsModified(getInteractionParameters());
	}

	/***************************************
	 * Internal method to validate the fragment's process parameters during
	 * state changes of the process. Subclasses must implement {@link
	 * #validateParameters(boolean)} instead.
	 *
	 * @param  bOnInteraction TRUE if the validation occurs during an ongoing
	 *                        interaction, FALSE after the final interaction
	 *                        before the fragment is finished
	 *
	 * @return A mapping from invalid parameters to validation error message
	 *         (empty for none)
	 */
	public Map<RelationType<?>, String> validateFragmentParameters(
		boolean bOnInteraction)
	{
		Map<RelationType<?>, String> aValidationErrors = new HashMap<>();

		for (InteractionFragment rSubFragment : getSubFragments())
		{
			aValidationErrors.putAll(
				rSubFragment.validateFragmentParameters(bOnInteraction));
		}

		Map<RelationType<?>, String> aFragmentErrors =
			validateParameters(bOnInteraction);

		Map<RelationType<?>, Function<?, String>> aValidations =
			new LinkedHashMap<>();

		if (bOnInteraction)
		{
			RelationType<?> rInteractionParam = getInteractiveInputParameter();

			Function<?, String> fValidation =
				getParameterValidations(true).get(rInteractionParam);

			if (fValidation != null)
			{
				aValidations.put(rInteractionParam, fValidation);
			}
		}
		else
		{
			aValidations.putAll(getParameterValidations(true));
			aValidations.putAll(getParameterValidations(false));
		}

		aFragmentErrors.putAll(performParameterValidations(aValidations));

		if (!aFragmentErrors.isEmpty())
		{
			validationError(aFragmentErrors);
		}

		aValidationErrors.putAll(aFragmentErrors);

		return aValidationErrors;
	}

	/***************************************
	 * This method can be overridden by subclasses to validate process
	 * parameters during state changes of the process. The default
	 * implementation returns an new empty map instance that may be modified
	 * freely by overriding methods to add their own error messages if
	 * necessary.
	 *
	 * @param  bOnInteraction TRUE if the validation occurs during an ongoing
	 *                        interaction, FALSE after the final interaction
	 *                        before the fragment is finished
	 *
	 * @return A mapping from invalid parameters to validation error message
	 *         (empty for none)
	 */
	public Map<RelationType<?>, String> validateParameters(
		boolean bOnInteraction)
	{
		return new HashMap<RelationType<?>, String>();
	}

	/***************************************
	 * Signals validation errors that occurred in this fragment. The default
	 * implementation delegates the handling to the parent fragment (if such
	 * exists). Subclasses can override this method somewhere in the hierarchy
	 * if they need to display validation errors more prominently. This will not
	 * override the default process parameter validation.
	 *
	 * @param rValidationErrors A mapping from parameters to validation error
	 *                          messages
	 */
	public void validationError(Map<RelationType<?>, String> rValidationErrors)
	{
		if (rParent != null)
		{
			rParent.validationError(rValidationErrors);
		}
	}

	/***************************************
	 * This method will be invoked if the current execution of this fragment is
	 * aborted before it has finished. and can be overridden by subclasses to
	 * perform data resets similar to the {@link #rollback()} method which will
	 * be invoked if the execution of a finished fragment is to be reverted.
	 */
	protected void abort()
	{
	}

	/***************************************
	 * Can be implemented by subclasses to react on interactions that occurred
	 * in other fragments. This method will be invoked after {@link
	 * #handleInteraction(RelationType)}. The default implementation does
	 * nothing.
	 *
	 * @param  rInteractionParam The interaction parameter
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	protected void afterInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
	}

	/***************************************
	 * This method can be overridden by a subclass to indicate whether it
	 * supports a rollback of the modifications it has performed. The default
	 * implementation always returns TRUE.
	 *
	 * @return TRUE if the step implementation support a rollback
	 *
	 * @see    #rollback()
	 */

	protected boolean canRollback()
	{
		return true;
	}

	/***************************************
	 * Internal method to finish the fragment execution. Subclasses must
	 * implement {@link #finish()} instead.
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	protected final void finishFragment() throws Exception
	{
		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rSubFragment.finishFragment();
		}

		finish();
	}

	/***************************************
	 * Overridden to return the fragment-local validation maps.
	 *
	 * @see de.esoco.process.ProcessElement#getParameterValidations(boolean)
	 */
	@Override
	protected Map<RelationType<?>, Function<?, String>> getParameterValidations(
		boolean bOnInteraction)
	{
		return bOnInteraction ? aParamInteractionValidations
							  : aParamValidations;
	}

	/***************************************
	 * Returns the root fragment of this fragment's hierarchy.
	 *
	 * @return The root fragment
	 */
	protected InteractionFragment getRoot()
	{
		return rParent != null ? rParent.getRoot() : this;
	}

	/***************************************
	 * Overridden to return a parameter ID that is relative to the current
	 * fragment instance.
	 *
	 * @see ProcessFragment#getTemporaryParameterId()
	 */
	@Override
	protected int getTemporaryParameterId()
	{
		return nNextParameterId++;
	}

	/***************************************
	 * Determines the datatype of a certain value. This especially recognizes
	 * anonymous subclasses of enums and returns the correct enum type instead.
	 *
	 * @param  rValue The value to determine the datatype of
	 *
	 * @return The value datatype
	 */
	@SuppressWarnings("unchecked")
	protected <T> Class<T> getValueDatatype(T rValue)
	{
		Class<T> rDatatype = (Class<T>) rValue.getClass();

		if (rDatatype.isAnonymousClass() && rDatatype.getSuperclass().isEnum())
		{
			rDatatype = (Class<T>) rDatatype.getSuperclass();
		}

		return rDatatype;
	}

	/***************************************
	 * This method will be invoked after the initialization of a fragment and
	 * it's hierarchy of sub-fragments has been completed. This means that when
	 * this method is invoked on a child fragment it's parent has been
	 * initialized but it's {@link #initComplete()} method hasn't been invoked
	 * yet.
	 *
	 * <p>The purpose of this method is for subclasses to execute code that
	 * depends on a full initialization. The default implementation does
	 * nothing.</p>
	 *
	 * @throws Exception If the post-initialization code fails
	 */
	protected void initComplete() throws Exception
	{
	}

	/***************************************
	 * Will be invoked after the process step of this fragment has been set. Can
	 * be implemented by subclasses to initialize process step-specific
	 * parameters. The default implementation does nothing.
	 *
	 * @param rProcessStep The process step of this fragment
	 */
	protected void initProcessStep(Interaction rProcessStep)
	{
	}

	/***************************************
	 * Prepares the upload of a file with . This requires two parameters. One
	 * string parameter that will be configured to invoke a file chooser and
	 * then holds the name of the selected file. This parameter must be
	 * configured as an input parameter. And a target parameter that will
	 * receive the result of a successful file upload.
	 *
	 * @param  rFileSelectParam The parameter for the file selection
	 * @param  rUploadHandler   The upload handler
	 *
	 * @throws Exception If preparing the upload fails
	 */
	protected void prepareUpload(
		RelationType<String> rFileSelectParam,
		UploadHandler		 rUploadHandler) throws Exception
	{
		final SessionManager rSessionManager =
			getParameter(DataRelationTypes.SESSION_MANAGER);

		String sOldUrl = getUIProperty(URL, rFileSelectParam);

		if (sOldUrl != null)
		{
			rSessionManager.removeUpload(sOldUrl);
			getProcessStep().removeCleanupAction(sOldUrl);
		}

		final String sUploadUrl = rSessionManager.prepareUpload(rUploadHandler);

		setUIProperty(CONTENT_TYPE, ContentType.FILE_UPLOAD, rFileSelectParam);
		setUIProperty(URL, sUploadUrl, rFileSelectParam);
		setInteractive(InteractiveInputMode.ACTION, rFileSelectParam);

		addCleanupAction(
			sUploadUrl,
			f -> rSessionManager.removeUpload(sUploadUrl));
	}

	/***************************************
	 * Prepares the upload of a file into a process parameter. This requires two
	 * parameters. One string parameter that will be configured to invoke a file
	 * chooser and then holds the name of the selected file. This parameter must
	 * be configured as an input parameter. And a target parameter that will
	 * receive the result of a successful file upload.
	 *
	 * @param  rFileSelectParam    The parameter for the file selection
	 * @param  rTargetParam        The target parameter for the file content
	 * @param  rContentTypePattern A pattern that limits allowed content types
	 *                             or NULL for no restriction
	 * @param  nMaxSize            The maximum upload size
	 *
	 * @throws Exception If preparing the upload fails
	 *
	 * @see    #prepareUpload(RelationType, UploadHandler)
	 */
	protected void prepareUpload(RelationType<String> rFileSelectParam,
								 RelationType<byte[]> rTargetParam,
								 Pattern			  rContentTypePattern,
								 int				  nMaxSize) throws Exception
	{
		ProcessParamUploadHandler aUploadHandler =
			new ProcessParamUploadHandler(
				rTargetParam,
				rContentTypePattern,
				nMaxSize);

		prepareUpload(rFileSelectParam, aUploadHandler);
	}

	/***************************************
	 * Can be overridden to perform a rollback of data and parameter
	 * modifications that have been performed by this fragment. By default all
	 * fragments are assumed to be capable of being rolled back. The default
	 * implementation does nothing.
	 *
	 * @throws Exception If the rollback fails
	 */
	protected void rollback() throws Exception
	{
	}

	/***************************************
	 * Sets the values of process parameters from the attributes of an entity.
	 * To make this work the given relation types must be entity attribute types
	 * which will then be set as process parameters. The attributes can either
	 * by direct or extra attributes (which must have the extra attribute flag
	 * set).
	 *
	 * <p>To set modified parameter values back into the entity the method
	 * {@link #updateEntityFromParameterValues(Entity, List)} can be invoked.
	 * </p>
	 *
	 * @param  rEntity     The entity to read the attributes from
	 * @param  rAttributes The entity attributes and process parameters
	 *
	 * @throws StorageException If querying an extra attribute fails
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setParameterValuesFromEntity(
		Entity				  rEntity,
		List<RelationType<?>> rAttributes) throws StorageException
	{
		for (RelationType<?> rParam : rAttributes)
		{
			if (rEntity.getDefinition().getAttributes().contains(rParam))
			{
				Object rValue =
					rEntity.hasRelation(rParam) ? rEntity.get(rParam)
												: rParam.initialValue(rEntity);

				setParameter((RelationType) rParam, rValue);
			}
			else if (rParam.hasFlag(ExtraAttributes.EXTRA_ATTRIBUTE_FLAG))
			{
				setParameter(
					(RelationType) rParam,
					rEntity.getExtraAttribute(rParam, null));
			}
		}
	}

	/***************************************
	 * @see #setParameterValuesFromEntity(Entity, List)
	 */
	protected void setParameterValuesFromEntity(
		Entity			   rEntity,
		RelationType<?>... rParams) throws StorageException
	{
		setParameterValuesFromEntity(rEntity, Arrays.asList(rParams));
	}

	/***************************************
	 * Sets the parent fragment of this instance.
	 *
	 * @param rParent The parent fragment
	 */
	protected void setParent(InteractionFragment rParent)
	{
		this.rParent = rParent;
	}

	/***************************************
	 * Updates the attributes of an entity from the process parameter values
	 * that are stored with the given entity attribute relation types. The
	 * attributes can either be direct or extra attributes.
	 *
	 * <p>To set the process parameters from entity attributes the reverse
	 * method {@link #setParameterValuesFromEntity(Entity, List)} can be used.
	 * </p>
	 *
	 * @param  rEntity The entity to update
	 * @param  rParams The process parameter and entity attribute relation types
	 *
	 * @throws StorageException if setting an extra attribute fails
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void updateEntityFromParameterValues(
		Entity				  rEntity,
		List<RelationType<?>> rParams) throws StorageException
	{
		for (RelationType<?> rParam : rParams)
		{
			if (hasParameter(rParam))
			{
				if (rEntity.getDefinition().getAttributes().contains(rParam))
				{
					rEntity.set((RelationType) rParam, getParameter(rParam));
				}
				else if (rParam.hasFlag(ExtraAttributes.EXTRA_ATTRIBUTE_FLAG))
				{
					rEntity.setExtraAttribute(
						(RelationType) rParam,
						getParameter(rParam));
				}
			}
		}
	}

	/***************************************
	 * Internal method that handles the invocation of {@link
	 * #afterInteraction(RelationType)} for this instance and all registered
	 * sub-fragments.
	 *
	 * @param  rInteractionParam The interaction parameter
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	final void afterFragmentInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rSubFragment.afterFragmentInteraction(rInteractionParam);
		}

		afterInteraction(rInteractionParam);
	}

	/***************************************
	 * This method can be overridden by a subclass to indicate whether it
	 * supports a rollback of the modifications it has performed. The default
	 * implementation always returns TRUE.
	 *
	 * @return TRUE if the step implementation support a rollback
	 *
	 * @see    #rollback()
	 */

	final boolean canFragmentRollback()
	{
		for (InteractionFragment rSubFragment : getSubFragments())
		{
			if (!rSubFragment.canFragmentRollback())
			{
				return false;
			}
		}

		return canRollback();
	}

	/***************************************
	 * Checks whether this fragment contains a certain continuation parameter.
	 *
	 * @param  rContinuationParam The continuation parameter to check
	 *
	 * @return TRUE if the given continuation parameter belongs to this fragment
	 */
	InteractionFragment getContinuationFragment(
		RelationType<?> rContinuationParam)
	{
		InteractionFragment rContinuationFragment = null;

		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rContinuationFragment =
				rSubFragment.getContinuationFragment(rContinuationParam);

			if (rContinuationFragment != null)
			{
				break;
			}
		}

		if (rContinuationFragment == null &&
			aFragmentContinuationParams != null &&
			aFragmentContinuationParams.contains(rContinuationParam))
		{
			rContinuationFragment = this;
		}

		return rContinuationFragment;
	}

	/***************************************
	 * Internal method that handles the invocation of {@link
	 * #handleInteraction(RelationType)} for this instance and all registered
	 * sub-fragments.
	 *
	 * @param  rInteractionParam The interaction parameter
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	final void handleFragmentInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		boolean bRootFragmentInteraction = true;

		for (InteractionFragment rSubFragment : getSubFragments())
		{
			if (rSubFragment.hasFragmentInteraction(rInteractionParam))
			{
				rSubFragment.handleFragmentInteraction(rInteractionParam);
				bRootFragmentInteraction = false;

				break;
			}
		}

		if (bRootFragmentInteraction || hasInteraction(rInteractionParam))
		{
			handleInteraction(rInteractionParam);
		}
	}

	/***************************************
	 * Internal method to check whether an interaction has been caused by an
	 * interaction parameter from this fragment or one of it's sub-fragments.
	 * Subclasses must implement {@link #hasInteraction(RelationType)} instead.
	 *
	 * @param  rInteractionParam The interaction parameter to check
	 *
	 * @return TRUE if the interaction was caused by a parameter of this
	 *         fragment
	 */
	final boolean hasFragmentInteraction(RelationType<?> rInteractionParam)
	{
		for (InteractionFragment rSubFragment : getSubFragments())
		{
			if (rSubFragment.hasFragmentInteraction(rInteractionParam))
			{
				return true;
			}
		}

		return hasInteraction(rInteractionParam);
	}

	/***************************************
	 * Internal method to initialize this fragment. Subclasses must implement
	 * {@link #init()} instead.
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	final void initFragment() throws Exception
	{
		getSubFragments().clear();
		init();

		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rSubFragment.initFragment();
		}

		markFragmentInputParams();
		bInitialized = true;
		initComplete();
	}

	/***************************************
	 * Internal method to prepare each interaction of this fragment. Subclasses
	 * must implement {@link #prepareInteraction()} instead.
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	final void prepareFragmentInteraction() throws Exception
	{
		prepareInteraction();

		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rSubFragment.prepareFragmentInteraction();
		}
	}

	/***************************************
	 * Internal method to perform a rollback of data and parameter modifications
	 * that have been performed by this fragment. Subclasses must implement
	 * {@link #rollback()} instead.
	 *
	 * @throws Exception On errors
	 */
	final void rollbackFragment() throws Exception
	{
		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rSubFragment.rollbackFragment();
		}

		rollback();
	}

	/***************************************
	 * Package-internal method to associate this fragment with a particular
	 * interactive process step.
	 *
	 * @param rProcessStep The process step this fragment belongs to
	 */
	final void setProcessStep(Interaction rProcessStep)
	{
		this.rProcessStep = rProcessStep;

		if (rProcessStep != null)
		{
			initProcessStep(rProcessStep);
		}
	}

	/***************************************
	 * Adds a new sub-fragment through {@link
	 * ProcessFragment#addSubFragment(RelationType, InteractionFragment)}.
	 *
	 * @param rSubFragmentParamType The type of the parameter containing the
	 *                              child fragment parameters
	 * @param rSubFragment          The child fragment
	 * @param bMarkAsModified       TRUE to mark the parameter of this fragment
	 *                              and it's structure as modified
	 */
	private void addSubFragment(
		RelationType<List<RelationType<?>>> rSubFragmentParamType,
		InteractionFragment					rSubFragment,
		boolean								bMarkAsModified)
	{
		rSubFragment.setParent(this);

		super.addSubFragment(rSubFragmentParamType, rSubFragment);

		if (bMarkAsModified)
		{
			structureModified();
		}

		if (bInitialized)
		{
			// if a fragment is added after initialization of the parent has
			// already completed it's init() method needs to be invoked too
			try
			{
				rSubFragment.initFragment();
			}
			catch (Exception e)
			{
				throw new RuntimeProcessException(rSubFragment, e);
			}
		}
	}

	/***************************************
	 * Internal method that tries to lock an entity during the execution of
	 * either the current step or the remaining process execution. It displays
	 * an information message if the entity is already locked by some other
	 * context. The lock will automatically be removed if the process progresses
	 * to another step (including rollback) or is terminated in any way.
	 *
	 * @param  rEntity                 The entity to lock
	 * @param  sLockUnavailableMessage The message to display if the lock
	 *                                 couldn't be acquired
	 * @param  bInProcess              TRUE to lock the entity for the process,
	 *                                 FALSE to lock it only for the current
	 *                                 step
	 *
	 * @return TRUE if the lock could be acquired
	 *
	 * @see    #lockEntityForProcess(Entity, String)
	 * @see    #lockEntityForStep(Entity, String)
	 */
	private boolean lockEntity(Entity  rEntity,
							   String  sLockUnavailableMessage,
							   boolean bInProcess)
	{
		boolean bLocked =
			bInProcess ? getProcess().lockEntity(rEntity) : lockEntity(rEntity);

		if (!bLocked)
		{
			showInfoMessage(sLockUnavailableMessage);
		}

		return bLocked;
	}

	/***************************************
	 * Removes a child fragment from this parent by invoking {@link
	 * ProcessFragment#removeSubFragment(InteractionFragment)}.
	 *
	 * @param  rSubFragmentParamType The type of the parameter containing the
	 *                               fragment parameters
	 * @param  bMarkAsModified       TRUE to mark the parameter of this fragment
	 *                               and it's structure as modified
	 *
	 * @return TODO: DOCUMENT ME!
	 */
	private InteractionFragment removeSubFragment(
		RelationType<List<RelationType<?>>> rSubFragmentParamType,
		boolean								bMarkAsModified)
	{
		InteractionFragment rSubFragment =
			super.removeSubFragment(rSubFragmentParamType);

		if (rSubFragment != null)
		{
			rSubFragment.setProcessStep(null);
			rSubFragment.setParent(null);

			if (bMarkAsModified)
			{
				structureModified();
			}
		}

		return rSubFragment;
	}

	/***************************************
	 * Internal method that displays any kind of dialog fragment.
	 *
	 * @param  rDialogFragment The dialog fragment
	 * @param  rDialogListener An optional dialog listener or NULL for none
	 *
	 * @throws Exception If displaying the dialog fails
	 */
	private void showDialogImpl(
		DialogFragment		 rDialogFragment,
		DialogActionListener rDialogListener)
	{
		if (rDialogListener != null)
		{
			rDialogFragment.addDialogActionListener(rDialogListener);
		}

		rDialogFragment.show(this);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An implementation of the {@link UploadHandler} interface that writes
	 * uploaded data into a process parameter. This class is used internally by
	 * {@link InteractionFragment#prepareUpload(RelationType, RelationType,
	 * Pattern, int)}.
	 *
	 * @author eso
	 */
	class ProcessParamUploadHandler implements UploadHandler
	{
		//~ Instance fields ----------------------------------------------------

		private RelationType<byte[]> rTargetParam;
		private Pattern				 rContentTypePattern;
		private int					 nMaxSize;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rTargetParam        The target parameter for the uploaded data
		 * @param rContentTypePattern A pattern that limits allowed content
		 *                            types or NULL for no restriction
		 * @param nMaxSize            The maximum upload size
		 */
		public ProcessParamUploadHandler(
			RelationType<byte[]> rTargetParam,
			Pattern				 rContentTypePattern,
			int					 nMaxSize)
		{
			this.rTargetParam		 = rTargetParam;
			this.rContentTypePattern = rContentTypePattern;
			this.nMaxSize			 = nMaxSize;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void processUploadData(String	  sFilename,
									  String	  sContentType,
									  InputStream rDataStream) throws Exception
		{
			byte[] aBuf  = new byte[1024 * 16];
			int    nRead;

			if (rContentTypePattern != null &&
				!rContentTypePattern.matcher(sContentType).matches())
			{
				error("InvalidUploadContentType");
			}

			ByteArrayOutputStream aOutStream = new ByteArrayOutputStream();

			while ((nRead = rDataStream.read(aBuf, 0, aBuf.length)) != -1)
			{
				if (aOutStream.size() + nRead <= nMaxSize)
				{
					aOutStream.write(aBuf, 0, nRead);
				}
				else
				{
					error("UploadSizeLimitExceeded");
				}
			}

			setParameter(rTargetParam, aOutStream.toByteArray());
			removeParameterAnnotation(rTargetParam, ERROR_MESSAGE);
		}

		/***************************************
		 * Sets an error message on the target parameter and then throws an
		 * exception.
		 *
		 * @param  sMessage The error message
		 *
		 * @throws ProcessException The error exception
		 */
		private void error(String sMessage) throws ProcessException
		{
			annotateParameter(rTargetParam, null, ERROR_MESSAGE, sMessage);
			throw new ProcessException(getProcessStep(), sMessage);
		}
	}
}
