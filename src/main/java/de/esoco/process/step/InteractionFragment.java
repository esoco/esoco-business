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
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.filter.RelationCoupling;
import org.obrel.type.MetaTypes;

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

/**
 * A process element subclass that serves as a fragment of an interactive
 * process step. This allows to split the user interface of complex interactions
 * into different parts that can more easily be re-used.
 *
 * @author eso
 */
public abstract class InteractionFragment extends ProcessFragment
	implements Initializable {

	/**
	 * The resource string for an error message box icon.
	 */
	public static final String MESSAGE_BOX_ERROR_ICON = "#$imErrorMessage";

	/**
	 * The resource string for a warning message box icon.
	 */
	public static final String MESSAGE_BOX_WARNING_ICON = "#$imWarningMessage";

	/**
	 * The resource string for a question message box icon.
	 */
	public static final String MESSAGE_BOX_QUESTION_ICON =
		"#$imQuestionMessage";

	/**
	 * The resource string for an info message box icon.
	 */
	public static final String MESSAGE_BOX_INFO_ICON = "#$imInfoMessage";

	private static final long serialVersionUID = 1L;

	private final List<RelationType<?>> interactionParams = new ArrayList<>();

	private final Set<RelationType<?>> inputParams = new HashSet<>();

	private final Map<RelationType<?>, Function<?, String>> paramValidations =
		new HashMap<>();

	private final Map<RelationType<?>, Function<?, String>>
		paramInteractionValidations = new HashMap<>();

	private int nextParameterId = 0;

	private boolean initialized = false;

	private Interaction processStep;

	private InteractionFragment parent;

	private ParameterList fragmentParam;

	private List<RelationType<?>> fragmentContinuationParams = null;

	/**
	 * Creates a new instance.
	 */
	public InteractionFragment() {
		RelationTypes.init(getClass());
	}

	/**
	 * Internal method to abort the current execution of this fragment. It can
	 * be used to undo data and parameter initializations or interactive
	 * modifications that have been performed by this fragment. Subclasses must
	 * implement {@link #abort()} instead.
	 */
	public final void abortFragment() {
		for (InteractionFragment subFragment : getSubFragments()) {
			subFragment.abortFragment();
		}

		executeCleanupActions();
		abort();
	}

	/**
	 * Overridden to add nonexistent parameters to the list of interaction
	 * parameters of this instance as returned by the method
	 * {@link #getInteractionParameters()}. The returned collection must
	 * therefore be mutable (as is the case with the default parameter
	 * collection).
	 *
	 * <p>This implementation replaces the base class implementation which
	 * changes the interaction parameters of the process step instead of the
	 * fragment.</p>
	 *
	 * @param params The interaction parameters to add
	 */
	@Override
	public void addDisplayParameters(
		Collection<? extends RelationType<?>> params) {
		List<RelationType<?>> interactionParams = getInteractionParameters();

		for (RelationType<?> param : params) {
			markParameterAsModified(param);

			// do not add parameters that are displayed in panels because they
			// are stored in the parameter list of the panel parameter
			if (!isPanelParameter(param) &&
				!interactionParams.contains(param)) {
				interactionParams.add(param);
				structureModified();
			}
		}
	}

	/**
	 * @see #addInputParameters(Collection)
	 */
	@Override
	public void addInputParameters(RelationType<?>... params) {
		addInputParameters(Arrays.asList(params));
	}

	/**
	 * Adds the given parameters to the interaction and input parameters of
	 * this
	 * instance. The input parameters are queried with the method
	 * {@link #getInputParameters()}, the interaction parameters are updated
	 * with {@link #addDisplayParameters(Collection)}.
	 *
	 * <p>This implementation replaces the base class implementation which
	 * changes the interaction parameters of the process step instead of the
	 * fragment.</p>
	 *
	 * @param params The input parameters to add
	 * @see #addDisplayParameters(Collection)
	 */
	@Override
	public void addInputParameters(
		Collection<? extends RelationType<?>> params) {
		addDisplayParameters(params);
		markInputParams(true, params);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPanelParameters(Collection<RelationType<?>> panelParams) {
		super.addPanelParameters(panelParams);

		getInteractionParameters().removeAll(panelParams);
		structureModified();
	}

	/**
	 * Convenience method to add a listener to the process step relation with
	 * the type {@link ProcessRelationTypes#PARAM_UPDATE_LISTENERS}. To
	 * remove a
	 * listener it should be removed from the parameter set directly.
	 *
	 * @param listener The listener to add
	 */
	public void addParameterUpdateListener(Updatable listener) {
		get(PARAM_UPDATE_LISTENERS).add(listener);
	}

	/**
	 * A variant of {@link #addSubFragment(String, InteractionFragment)} that
	 * uses the name of the fragment class for the temporary fragment
	 * parameter.
	 *
	 * @see #addSubFragment(String, InteractionFragment)
	 */
	public ParameterList addSubFragment(InteractionFragment subFragment) {
		Class<? extends InteractionFragment> fragmentClass =
			subFragment.getClass();

		String fragmentName = fragmentClass.isAnonymousClass() ?
		                      null :
		                      fragmentClass.getSimpleName();

		return addSubFragment(fragmentName, subFragment);
	}

	/**
	 * Overridden to set the parent of the sub-fragment to this instance.
	 *
	 * @see ProcessFragment#addSubFragment(RelationType, InteractionFragment)
	 */
	@Override
	public void addSubFragment(
		RelationType<List<RelationType<?>>> fragmentParam,
		InteractionFragment subFragment) {
		addSubFragment(fragmentParam, subFragment, true);
	}

	/**
	 * Convenience method for the common case where the same sub-fragment type
	 * needs to be added multiple times to this parent fragment. The name of
	 * the
	 * fragment will be created from the fragment class and the given index
	 * (which must be different for each new instance). The fragment class name
	 * will also be set as the resource ID.
	 *
	 * @param index       The fragment index
	 * @param subFragment The fragment to add
	 * @return The wrapper for the fragment parameter
	 * @see #addSubFragment(String, InteractionFragment)
	 */
	public ParameterList addSubFragment(int index,
		InteractionFragment subFragment) {
		String name = subFragment.getClass().getSimpleName();

		return addSubFragment(name + index, subFragment).resid(name);
	}

	/**
	 * Adds a subordinate fragment to this instance into a temporary parameter
	 * and optionally displays it. The temporary parameter relation type
	 * will be
	 * created with the given name by invoking
	 * {@link #listParam(String, Class)}
	 * and the parameter wrapper will be returned. The fragment will be
	 * added by
	 * invoking {@link #addSubFragment(RelationType, InteractionFragment)}.
	 * Furthermore the UI property {@link UserInterfaceProperties#HIDE_LABEL}
	 * will be set on the new fragment parameter because fragments are
	 * typically
	 * displayed without a label.
	 *
	 * @param name        The name of the temporary fragment parameter
	 * @param subFragment The fragment to add
	 * @return The wrapper for the fragment parameter
	 */
	public ParameterList addSubFragment(String name,
		InteractionFragment subFragment) {
		ParameterList subFragmentParam = panel(name).hideLabel();

		addSubFragment(subFragmentParam.type(), subFragment);

		return subFragmentParam;
	}

	/**
	 * Adds a child fragment that shall be displayed as a view.
	 *
	 * @see #addSubFragment(RelationType, InteractionFragment)
	 */
	public void addViewFragment(
		RelationType<List<RelationType<?>>> viewFragmentParamType,
		InteractionFragment subFragment) {
		boolean modified =
			getProcessStep().isParameterModified(fragmentParam().type());

		getRoot().addSubFragment(viewFragmentParamType, subFragment, false);
		get(VIEW_PARAMS).add(viewFragmentParamType);

		if (!modified) {
			getProcessStep().removeParameterModification(fragmentParam());
		}
	}

	/**
	 * Applies all coupled parameters by setting their value onto their coupled
	 * targets.
	 *
	 * @see RelationCoupling#setAll(org.obrel.core.Relatable, Collection)
	 */
	public void applyAllCoupledParameters() {
		RelationCoupling.setAll(getProcess(), getInteractionParameters());
	}

	/**
	 * Internal method that will be invoked to attach this fragment to the
	 * given
	 * process step and fragment parameter. Multiple invocations are possible.
	 *
	 * @param processStep   The process step to attach this instance to
	 * @param fragmentParam The parameter this fragment will be stored in
	 */
	public void attach(Interaction processStep,
		RelationType<List<RelationType<?>>> fragmentParam) {
		this.fragmentParam = new ParameterList(this, fragmentParam, true);

		// reset internal state in the case of re-invocation caused by process
		// navigation
		inputParams.clear();
		interactionParams.clear();
		nextParameterId = 0;
		initialized = false;

		setProcessStep(processStep);
	}

	/**
	 * Returns a parameter that represents a single UI button with a text
	 * label.
	 *
	 * @param text The button text
	 * @return The new parameter
	 */
	public Parameter<String> button(String text) {
		return param(String.class)
			.input()
			.value(text)
			.set(HIDE_LABEL)
			.buttonStyle(ButtonStyle.DEFAULT);
	}

	/**
	 * Creates a parameter that displays interactive buttons from an enum.
	 *
	 * @param enumClass The enum class to create the buttons from
	 * @return The new parameter
	 */
	public <E extends Enum<E>> EnumParameter<E> buttons(Class<E> enumClass) {
		return enumParam(enumClass).buttons();
	}

	/**
	 * Creates a parameter that displays interactive buttons from certain
	 * values
	 * of an enum.
	 *
	 * @param allowedValues The enum values to create the buttons from (must
	 *                        not
	 *                      be empty)
	 * @return The new parameter
	 */
	@SafeVarargs
	public final <E extends Enum<E>> EnumParameter<E> buttons(
		E... allowedValues) {
		return enumParam(getValueDatatype(allowedValues[0])).buttons(
			allowedValues);
	}

	/**
	 * Create a parameter that displays a chart for a certain set of data.
	 *
	 * @param name      The parameter name
	 * @param chartType The initial type of the chart
	 * @param dataSet   The data set for the chart
	 * @return The new parameter
	 */
	public <T, D extends DataSet<T>> DataSetParameter<T, D> chart(String name,
		ChartType chartType, D dataSet) {
		@SuppressWarnings("unchecked")
		RelationType<D> paramType =
			getTemporaryParameterType(name, (Class<D>) dataSet.getClass());

		return new DataSetParameter<T, D>(this, paramType)
			.value(dataSet)
			.chartType(chartType);
	}

	/**
	 * Creates a boolean parameter that displays a checkbox for the input of a
	 * boolean value. The value will initially be set to FALSE.
	 *
	 * @param name The name of the parameter (used for the checkbox label)
	 * @return The new parameter
	 */
	public Parameter<Boolean> checkBox(String name) {
		return flagParam(name).input().hideLabel().value(Boolean.FALSE);
	}

	/**
	 * Creates a boolean parameter that displays a checkbox for the input of a
	 * boolean value with a certain style.
	 *
	 * @param name  The name of the parameter (used for the checkbox label)
	 * @param style The checkbox style
	 * @return The new parameter
	 */
	public Parameter<Boolean> checkBox(String name, CheckBoxStyle style) {
		return checkBox(name).set(CHECK_BOX_STYLE, style);
	}

	/**
	 * Creates a parameter that displays checkboxes for the selection of
	 * multiple enum values.
	 *
	 * @param enumClass The enum class to create the checkboxes parameter for
	 * @return The new parameter
	 */
	public <E extends Enum<E>> SetParameter<E> checkBoxes(Class<E> enumClass) {
		SetParameter<E> checkBoxes =
			setParam(enumClass.getSimpleName(), enumClass, true);

		return checkBoxes
			.input()
			.set(LIST_STYLE, ListStyle.DISCRETE)
			.layout(LayoutType.TABLE)
			.columns(1);
	}

	/**
	 * Can be overridden by subclasses to perform resource cleanups when the
	 * process ends. The default implementation does nothing.
	 */
	public void cleanup() {
	}

	/**
	 * Clears lists returned by the methods {@link #getInteractionParameters()}
	 * and {@link #getInputParameters()}. These lists must therefore be
	 * mutable!
	 *
	 * <p>This implementation replaces the base class implementation because
	 * the parent method changes the interaction parameters of the process
	 * step.</p>
	 */
	@Override
	public void clearInteractionParameters() {
		getInteractionParameters().clear();
		getInputParameters().clear();
	}

	/**
	 * Clear the selection of a certain parameter by setting it's value to NULL
	 * and the property {@link UserInterfaceProperties#CURRENT_SELECTION} to
	 * -1.
	 *
	 * @param param The parameter to clear the selection of
	 */
	public void clearSelection(RelationType<?> param) {
		Object paramValue = getParameter(param);
		boolean clearSelection = (paramValue != null);

		if (SelectionDataElement.class.isAssignableFrom(
			param.getTargetType())) {
			SelectionDataElement element = (SelectionDataElement) paramValue;

			if (!SelectionDataElement.NO_SELECTION.equals(element.getValue())) {
				element.setValue(SelectionDataElement.NO_SELECTION);
				clearSelection = true;
			}
		} else {
			setParameter(param, null);
		}

		// only clear selection if one exists to prevent unnecessary updates
		if (clearSelection) {
			setUIProperty(-1, CURRENT_SELECTION, param);
		}
	}

	/**
	 * Creates a new temporary parameter relation type for text input with a
	 * combo box that combines an editable text box with a drop-down list of
	 * value presets.
	 *
	 * @param name         The name of the parameter
	 * @param presetValues The preset values the user can select from
	 * @return The new parameter
	 */
	public final Parameter<String> comboBox(String name,
		Collection<String> presetValues) {
		return inputText(name)
			.set(LIST_STYLE, ListStyle.EDITABLE)
			.allow(presetValues);
	}

	/**
	 * Convenience method to create a new temporary parameter relation type
	 * with
	 * a string datatype.
	 *
	 * @see #param(String, Class)
	 */
	public Parameter<Date> dateParam(String name) {
		return param(name, Date.class);
	}

	/**
	 * Overridden to forward the call to the enclosing process step.
	 *
	 * @see ProcessStep#deleteRelation(Relation)
	 */
	@Override
	public void deleteRelation(Relation<?> relation) {
		processStep.deleteRelation(relation);
	}

	/**
	 * Creates a new anonymous parameter type for the display of a value. This
	 * is just a shortcut for the invocation of {@link #param(String, Class)}
	 * with the name argument set to NULL.
	 *
	 * @param datatype The datatype of the values to display
	 * @return The parameter wrapper
	 */
	public <T> Parameter<T> display(Class<T> datatype) {
		return param(null, datatype);
	}

	/**
	 * Creates a new temporary parameter relation type for the selection of an
	 * enum value from a drop down box.
	 *
	 * @param enumClass The enum class to display the values of
	 * @return The new parameter
	 */
	public final <E extends Enum<E>> Parameter<E> dropDown(Class<E> enumClass) {
		return dropDown(enumClass.getSimpleName(), EnumSet.allOf(enumClass));
	}

	/**
	 * Creates a new temporary parameter relation type for the selection of
	 * values from a drop down box. The first value will be used to determine
	 * the datatype of the parameter type and it will be preset as the
	 * parameter
	 * value.
	 *
	 * @param name          The name of the parameter
	 * @param allowedValues The values to be displayed in the drop down box
	 *                      (must not be empty)
	 * @return The new parameter
	 */
	public final <T> Parameter<T> dropDown(String name,
		Collection<T> allowedValues) {
		T firstValue = CollectionUtil.firstElementOf(allowedValues);
		Class<T> datatype = getValueDatatype(firstValue);

		return input(name, datatype)
			.set(LIST_STYLE, ListStyle.DROP_DOWN)
			.value(firstValue)
			.allow(allowedValues);
	}

	/**
	 * Enables or disables the editing of this fragment and of all it's
	 * children. This is achieved by clearing or setting the flag property
	 * {@link UserInterfaceProperties#DISABLED} on the fragment input
	 * parameters. Subclasses may override this method to implement a more
	 * specific handling but should normally also call the superclass
	 * implementation.
	 *
	 * @param enable TRUE to enable editing, FALSE to disable
	 */
	public void enableEdit(boolean enable) {
		setEnabled(enable, getInputParameters());

		for (InteractionFragment subFragment : getSubFragments()) {
			subFragment.enableEdit(enable);
		}
	}

	/**
	 * Create a new parameter wrapper for entity process parameters that is
	 * named after the entity type.
	 *
	 * @see #entityParam(String, Class)
	 */
	public <E extends Entity> EntityParameter<E> entityParam(
		Class<E> entityType) {
		return entityParam(entityType.getSimpleName(), entityType);
	}

	/**
	 * Create a new parameter wrapper for entity process parameters.
	 *
	 * @param name       The name of the parameter relation type
	 * @param entityType The entity type for the parameter
	 * @return the entity parameter wrapper
	 * @see #param(String, Class)
	 */
	public <E extends Entity> EntityParameter<E> entityParam(String name,
		Class<E> entityType) {
		RelationType<E> paramType = getTemporaryParameterType(name,
			entityType);

		return new EntityParameter<>(this, paramType);
	}

	/**
	 * Convenience method to create a new temporary parameter relation type
	 * with
	 * an enum datatype. The parameter will be named with the simple name of
	 * the
	 * enum class.
	 *
	 * @see #param(String, Class)
	 */
	public <E extends Enum<E>> EnumParameter<E> enumParam(Class<E> enumClass) {
		return new EnumParameter<>(this,
			getTemporaryParameterType(enumClass.getSimpleName(), enumClass));
	}

	/**
	 * @see ProcessFragment#executeCleanupActions()
	 */
	@Override
	public void executeCleanupActions() {
		super.executeCleanupActions();

		for (InteractionFragment fragment : getSubFragments()) {
			fragment.executeCleanupActions();
		}
	}

	/**
	 * Can be overridden by a fragment to execute actions when the process flow
	 * leaves this fragment.
	 *
	 * <p>The default implementation does nothing.</p>
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	public void finish() throws Exception {
	}

	/**
	 * Convenience method to create a new temporary parameter relation type
	 * with
	 * a boolean datatype.
	 *
	 * @see #param(String, Class)
	 */
	public Parameter<Boolean> flagParam(String name) {
		return param(name, Boolean.class);
	}

	/**
	 * Returns a parameter wrapper for the relation type this fragment is
	 * stored
	 * in.
	 *
	 * @return the parameter wrapper for the fragment parameter
	 */
	public ParameterList fragmentParam() {
		return fragmentParam;
	}

	/**
	 * Overridden to forward the call to the enclosing process step.
	 *
	 * @see ProcessStep#get(RelationType)
	 */
	@Override
	public <T> T get(RelationType<T> type) {
		return processStep.get(type);
	}

	/**
	 * Returns the parameter this fragment is displayed in.
	 *
	 * @return The fragment parameter
	 */
	public final RelationType<List<RelationType<?>>> getFragmentParameter() {
		return fragmentParam.type();
	}

	/**
	 * Returns the collection of input parameters of this fragment. These must
	 * be a subset of {@link #getInteractionParameters()}. The default
	 * implementation returns a mutable collection that can been modified
	 * directly by a subclass. Or it can be overridden by subclasses to return
	 * their own input parameter collection.
	 *
	 * @return The list of this fragment's input parameters
	 */
	public Collection<RelationType<?>> getInputParameters() {
		return inputParams;
	}

	/**
	 * Returns the list of interaction parameters for this fragment. The
	 * default
	 * implementation returns a mutable list that can been modified directly by
	 * a subclass. Or it can be overridden by subclasses to return their own
	 * interaction parameter list.
	 *
	 * @return The list of this fragment's interaction parameters
	 */
	public List<RelationType<?>> getInteractionParameters() {
		return interactionParams;
	}

	/**
	 * Sets the interaction handler for a certain parameter.
	 *
	 * @see Interaction#getParameterInteractionHandler(RelationType)
	 */
	public InteractionHandler getParameterInteractionHandler(
		RelationType<?> param) {
		return getProcessStep().getParameterInteractionHandler(param);
	}

	/**
	 * Returns the parent fragment of this instance.
	 *
	 * @return The parent fragment or NULL for a root fragment
	 */
	public final InteractionFragment getParent() {
		return parent;
	}

	/**
	 * @see ProcessFragment#getProcess()
	 */
	@Override
	public Process getProcess() {
		return processStep.getProcess();
	}

	/**
	 * Returns the interactive process step this element is associated with.
	 *
	 * @return The process step this fragment belongs to
	 */
	@Override
	public final Interaction getProcessStep() {
		return processStep;
	}

	/**
	 * Overridden to forward the call to the enclosing process step.
	 *
	 * @see ProcessStep#getRelation(RelationType)
	 */
	@Override
	public <T> Relation<T> getRelation(RelationType<T> type) {
		return processStep.getRelation(type);
	}

	/**
	 * Overridden to forward the call to the enclosing process step.
	 *
	 * @see ProcessStep#getRelations(Predicate)
	 */
	@Override
	public List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> filter) {
		return processStep.getRelations(filter);
	}

	/**
	 * Must be implemented by subclasses to handle interactions for this
	 * fragment. The default implementation does nothing.
	 *
	 * @param interactionParam The interaction parameter
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	public void handleInteraction(RelationType<?> interactionParam)
		throws Exception {
	}

	/**
	 * Checks whether an interaction has been caused by an interaction
	 * parameter
	 * from this fragment. The default implementation checks if the given
	 * parameter is one of this fragment's interaction parameters.
	 *
	 * @param interactionParam The interaction parameter to check
	 * @return TRUE if the interaction was caused by a parameter of this
	 * fragment
	 */
	public boolean hasInteraction(RelationType<?> interactionParam) {
		return getInputParameters().contains(interactionParam);
	}

	/**
	 * Creates a parameter for an empty label with the {@link LabelStyle#ICON}
	 * that displays a certain icon.
	 *
	 * @param iconIdentifier An identifier that describes the icon to display;
	 *                       will be converted to a string and should typically
	 *                       either be a string of an enum constant
	 * @return The new parameter
	 */
	public Parameter<String> icon(Object iconIdentifier) {
		return label("", LabelStyle.ICON).icon(iconIdentifier);
	}

	/**
	 * Creates a parameter that displays interactive buttons for certain enum
	 * values as icons.
	 *
	 * @param allowedValues enumClass The enum class to create the buttons from
	 * @return The new parameter
	 */
	@SafeVarargs
	public final <E extends Enum<E>> EnumParameter<E> iconButtons(
		E... allowedValues) {
		return buttons(allowedValues).buttonStyle(ButtonStyle.ICON).images();
	}

	/**
	 * Creates a parameter that displays interactive buttons from an enum as
	 * icons.
	 *
	 * @param enumClass The enum class to create the buttons from
	 * @return The new parameter
	 */
	public <E extends Enum<E>> EnumParameter<E> iconButtons(
		Class<E> enumClass) {
		return buttons(enumClass).buttonStyle(ButtonStyle.ICON).images();
	}

	/**
	 * Creates a parameter that displays an image.
	 *
	 * @param imageName The image name
	 * @return The new parameter
	 */
	public Parameter<String> image(String imageName) {
		return label(imageName, LabelStyle.IMAGE);
	}

	/**
	 * Creates a parameter that displays interactive buttons from an enum with
	 * images.
	 *
	 * @param enumClass The enum class to create the buttons from
	 * @return The new parameter
	 */
	public <E extends Enum<E>> EnumParameter<E> imageButtons(
		Class<E> enumClass) {
		return buttons(enumClass).images();
	}

	/**
	 * Must be implemented to initialize the interaction parameters of this
	 * fragment.
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	@Override
	public abstract void init() throws Exception;

	/**
	 * Initializes a parameter for the display of a storage query.
	 *
	 * @param param       The parameter to initialize the query for
	 * @param entityClass The entity class to query
	 * @param criteria    The query criteria the query criteria or NULL for
	 *                    none
	 * @param sortOrder   The sort predicate or NULL for the default order
	 * @param mode        hierarchical TRUE for a hierarchical query with a
	 *                    tree-table display
	 * @param columns     The columns to display
	 * @return The generated query predicate
	 */
	public <E extends Entity> QueryPredicate<E> initQueryParameter(
		RelationType<E> param, Class<E> entityClass,
		Predicate<? super E> criteria, Predicate<? super Entity> sortOrder,
		HierarchicalQueryMode mode, RelationType<?>... columns) {
		QueryPredicate<E> entities = forEntity(entityClass, criteria);

		entities.set(HIERARCHICAL_QUERY_MODE, mode);
		annotateForEntityQuery(param, entities, sortOrder, columns);

		return entities;
	}

	/**
	 * Creates an input parameter for a certain datatype. This method first
	 * invokes {@link #param(RelationType)} and then {@link Parameter#input()}.
	 *
	 * @param param The parameter to wrap
	 * @return A new parameter instance
	 */
	public <T> Parameter<T> input(RelationType<T> param) {
		return param(param).input();
	}

	/**
	 * Creates an anonymous input parameter for a certain datatype. The
	 * label of
	 * the parameter will be hidden because the parameter has no name so
	 * that no
	 * label resource will be available.
	 *
	 * @param datatype The datatype class
	 * @return A new parameter instance
	 */
	public <T> Parameter<T> input(Class<T> datatype) {
		return input(null, datatype).hideLabel();
	}

	/**
	 * Creates an input parameter for a certain datatype. This method combines
	 * {@link #param(String, Class)} and {@link Parameter#input()}.
	 *
	 * @param name     The name of the input parameter
	 * @param datatype The datatype class
	 * @return A new parameter instance
	 */
	public <T> Parameter<T> input(String name, Class<T> datatype) {
		return param(name, datatype).input();
	}

	/**
	 * Creates a new process parameter for the input of an entity attribute
	 * that
	 * is named like the original attribute relation type.
	 *
	 * @param attribute The attribute to edit
	 * @return The parameter wrapper
	 */
	public <E extends Entity, T> EntityAttributeParameter<E, T> inputAttr(
		RelationType<T> attribute) {
		return inputAttr(null, attribute);
	}

	/**
	 * Creates a new process parameter for the input of an entity attribute.
	 *
	 * @param name      The name of the parameter
	 * @param attribute The attribute to edit
	 * @return The parameter wrapper
	 */
	public <E extends Entity, T> EntityAttributeParameter<E, T> inputAttr(
		String name, RelationType<T> attribute) {
		RelationType<T> derivedParam =
			getTemporaryParameterType(name, attribute);

		EntityAttributeParameter<E, T> param =
			new EntityAttributeParameter<>(this, derivedParam);

		return param.input();
	}

	/**
	 * Creates a parameter for a date input field.
	 *
	 * @param name The name of the input parameter
	 * @return The label parameter
	 */
	public Parameter<Date> inputDate(String name) {
		return input(name, Date.class).set(DATE_INPUT_TYPE,
			DateInputType.INPUT_FIELD);
	}

	/**
	 * Creates an anonymous parameter for a text input field.
	 *
	 * @param presetValues The preset values the user can select from
	 * @return The label parameter
	 */
	public SetParameter<String> inputTags(Collection<String> presetValues) {
		SetParameter<String> setParam = setParam(null, String.class, true);

		return setParam
			.input()
			.set(LIST_STYLE, ListStyle.EDITABLE)
			.allowElements(presetValues);
	}

	/**
	 * Creates an anonymous parameter for a text input field.
	 *
	 * @param name The name of the input parameter
	 * @return The label parameter
	 */
	public Parameter<String> inputText(String name) {
		return input(name, String.class);
	}

	/**
	 * Creates an anonymous parameter for multi-line text input (a text area).
	 *
	 * @param name The name of the input parameter
	 * @return The label parameter
	 */
	public Parameter<String> inputTextLines(String name) {
		return inputText(name).rows(-1);
	}

	/**
	 * @see #insertInputParameters(RelationType, RelationType...)
	 */
	public void insertInputParameters(RelationType<?> beforeParam,
		RelationType<?>... params) {
		insertInputParameters(beforeParam, Arrays.asList(params));
	}

	/**
	 * Inserts additional parameters into the lists returned by the methods
	 * {@link #getInteractionParameters()} and {@link #getInputParameters()}.
	 * These lists must therefore be mutable!
	 *
	 * @param beforeParam The parameter to insert the other parameters before
	 * @param params      The parameters to add
	 */
	public void insertInputParameters(RelationType<?> beforeParam,
		Collection<RelationType<?>> params) {
		CollectionUtil.insert(getInteractionParameters(), beforeParam, params);
		getInputParameters().addAll(params);
	}

	/**
	 * Convenience method to create a new temporary parameter relation type
	 * with
	 * an integer datatype.
	 *
	 * @see #param(String, Class)
	 */
	public Parameter<Integer> intParam(String name) {
		return param(name, Integer.class);
	}

	/**
	 * Checks whether this fragment is (still) attached to a process.
	 *
	 * @return TRUE if the fragment is attached
	 */
	public boolean isAttached() {
		return processStep != null;
	}

	/**
	 * Checks whether this instance has already been initialized, i.e. returned
	 * from it's {@link #init()} method.
	 *
	 * @return TRUE if this instance has been completely initialized
	 */
	public final boolean isInitialized() {
		return initialized;
	}

	/**
	 * Creates a parameter that displays a label string with the default label
	 * style.
	 *
	 * @param labelText The label text
	 * @return The label parameter
	 */
	public Parameter<String> label(String labelText) {
		return label(labelText, null);
	}

	/**
	 * Creates a new display parameter for a string value with a certain label
	 * style.
	 *
	 * @param labelText The label text
	 * @param style     The style of the label
	 * @return The new parameter
	 */
	public Parameter<String> label(String labelText, LabelStyle style) {
		return textParam(null)
			.set(LABEL_STYLE, style)
			.hideLabel()
			.value(labelText);
	}

	/**
	 * Sets the layout of this fragment.
	 *
	 * @param layout The layout
	 * @return The parameter list of this fragment for concatenation
	 */
	public ParameterList layout(LayoutType layout) {
		return fragmentParam().layout(layout);
	}

	/**
	 * Create a new temporary relation type with a {@link List} datatype and
	 * returns a parameter wrapper for it.
	 *
	 * @param name        The name of the relation type
	 * @param elementType The datatype of the list elements
	 * @return the parameter instance
	 */
	public <T> ListParameter<T> listParam(String name,
		Class<? super T> elementType) {
		return new ListParameter<>(this,
			getTemporaryListType(name, elementType));
	}

	/**
	 * Tries to lock an entity during the remaining execution of the current
	 * process and displays an information message if the entity is already
	 * locked by some other context. The lock will automatically be removed if
	 * the process is terminated in any way but. The lock can also be removed
	 * explicitly by calling {@link Process#unlockEntity(Entity)}. Because the
	 * lock is process-wide this method should also be invoked in
	 * implementations of {@link #abort()} and {@link #rollback()} to handle
	 * process rollbacks.
	 *
	 * @param entity                 The entity to lock
	 * @param lockUnavailableMessage The message to display if the lock
	 *                                  couldn't
	 *                               be acquired
	 * @return TRUE if the lock could be acquired
	 */
	public boolean lockEntityForProcess(Entity entity,
		String lockUnavailableMessage) {
		return lockEntity(entity, lockUnavailableMessage, true);
	}

	/**
	 * Tries to lock an entity during the execution of the current step and
	 * displays an information message if the entity is already locked by some
	 * other context. The lock will automatically be removed if the process
	 * progresses to another step (including rollback) or is terminated in any
	 * way. The lock can also be removed explicitly by calling
	 * {@link #unlockEntity(Entity)}.
	 *
	 * @param entity                 The entity to lock
	 * @param lockUnavailableMessage The message to display if the lock
	 *                                  couldn't
	 *                               be acquired
	 * @return TRUE if the lock could be acquired
	 */
	public boolean lockEntityForStep(Entity entity,
		String lockUnavailableMessage) {
		return lockEntity(entity, lockUnavailableMessage, false);
	}

	/**
	 * Marks the input parameters of this fragment and all of it's
	 * sub-fragments.
	 */
	public void markFragmentInputParams() {
		get(INPUT_PARAMS).addAll(getInputParameters());

		for (InteractionFragment subFragment : getSubFragments()) {
			subFragment.markFragmentInputParams();
		}
	}

	/**
	 * Overridden to operate on the fragment input parameters.
	 *
	 * @see ProcessElement#markInputParams(boolean, Collection)
	 */
	@Override
	public void markInputParams(boolean input,
		Collection<? extends RelationType<?>> params) {
		Collection<RelationType<?>> inputParams = getInputParameters();

		for (RelationType<?> param : params) {
			boolean hasParam = inputParams.contains(param);

			if (!hasParam && input) {
				inputParams.add(param);
			} else if (hasParam && !input) {
				inputParams.remove(param);
			}
		}

		super.markInputParams(input, params);
	}

	/**
	 * Marks a hierarchy of parameters as modified.
	 *
	 * @param params The list of root parameters
	 */
	public void markParameterHierarchyAsModified(
		Collection<RelationType<?>> params) {
		for (RelationType<?> param : params) {
			markParameterAsModified(param);

			if (Collection.class.isAssignableFrom(param.getTargetType())) {
				if (param.get(MetaTypes.ELEMENT_DATATYPE) ==
					RelationType.class) {
					@SuppressWarnings("unchecked")
					Collection<RelationType<?>> childParams =
						(Collection<RelationType<?>>) getParameter(param);

					if (childParams != null) {
						markParameterHierarchyAsModified(childParams);
					}
				}
			}
		}
	}

	/**
	 * Notifies all listeners for parameter updates that are registered in the
	 * relation {@link ProcessRelationTypes#PARAM_UPDATE_LISTENERS} of this
	 * fragment's process step. Because relations are shared between fragments
	 * this will affect all fragments in the current interaction.
	 */
	public void notifyParameterUpdateListeners() {
		if (hasRelation(PARAM_UPDATE_LISTENERS)) {
			for (Updatable listener : get(PARAM_UPDATE_LISTENERS)) {
				listener.update();
			}
		}
	}

	/**
	 * Adds another fragment as a subordinate panel of this fragment. This is
	 * just a semantic variant of {@link #addSubFragment(InteractionFragment)}.
	 *
	 * @param panelFragment The panel fragment to add
	 * @return The parameter wrapper for the panel
	 */
	public ParameterList panel(InteractionFragment panelFragment) {
		return addSubFragment(panelFragment);
	}

	/**
	 * Adds a new anonymous panel fragment with a grid layout.
	 *
	 * @param initializer The fragment initializer
	 * @return the parameter wrapper for the panel parameter
	 * @see #panel(String, Initializer)
	 */
	public ParameterList panel(Initializer<InteractionFragment> initializer) {
		return panel(null, LayoutType.GRID, initializer);
	}

	/**
	 * Creates a new temporary relation type for a list of relation types that
	 * will be rendered in a panel without a separate fragment.
	 *
	 * @param name The name of the parameter list
	 * @return the parameter wrapper for the panel parameter
	 */
	public ParameterList panel(String name) {
		RelationType<List<RelationType<?>>> listType =
			getTemporaryListType(name, RelationType.class);

		return new ParameterList(this, listType, false).input();
	}

	/**
	 * Adds a new named panel fragment with a grid layout.
	 *
	 * @param initializer The fragment initializer
	 * @return the parameter wrapper for the panel parameter
	 * @see #panel(String, LayoutType, Initializer)
	 */
	public ParameterList panel(String name,
		Initializer<InteractionFragment> initializer) {
		return panel(name, LayoutType.GRID, initializer);
	}

	/**
	 * Adds a panel that contains components from the process UI API.
	 *
	 * @param layout     The UI layout of the panel
	 * @param buildPanel A builder function that creates the panel components
	 * @return the parameter wrapper for the panel parameter
	 */
	public ParameterList panel(UiLayout layout,
		Consumer<UiBuilder<?>> buildPanel) {
		@SuppressWarnings("serial")
		ParameterList panel =
			addSubFragment(new UiRootFragment(layout, buildPanel));

		return panel;
	}

	/**
	 * Creates a new anonymous interaction fragment and the associated
	 * parameter
	 * relation type. The initializer argument must perform the initialization
	 * of the new fragment which it receives as the argument to it's
	 * {@link Initializer#init(Object)} method.
	 *
	 * <p>This method is mainly intended to be used with lambda expressions
	 * introduced with Java 8. In that case it allows concise in-line
	 * declarations of panels by simply forwarding the initialization to a
	 * corresponding method in form of a method reference with an
	 * {@link InteractionFragment} parameter.</p>
	 *
	 * @param name        The name of the fragment parameter
	 * @param panelLayout The layout of the panel fragment
	 * @param initializer The fragment initializer
	 * @return A new parameter wrapper for the panel parameter
	 */
	@SuppressWarnings("serial")
	public ParameterList panel(String name, LayoutType panelLayout,
		final Initializer<InteractionFragment> initializer) {
		ParameterList panel = addSubFragment(name, new InteractionFragment() {
			@Override
			public void init() throws Exception {
				initializer.init(this);
			}
		});

		// only set if the panel hasn't set it's own layout (possible if it has
		// been added to an existing fragment and then initialized immediately)
		if (!panel.has(LAYOUT)) {
			panel.layout(panelLayout);
		}

		return panel;
	}

	/**
	 * Creates a new parameter wrapper for the given relation type in this
	 * fragment.
	 *
	 * @param param The parameter to wrap
	 * @return A new parameter instance
	 */
	public <T> Parameter<T> param(RelationType<T> param) {
		return new Parameter<>(this, param);
	}

	/**
	 * Returns a new anonymous parameter for a certain datatype.
	 *
	 * @param datatype The datatype class
	 * @return The new parameter wrapper
	 */
	public <T> Parameter<T> param(Class<? super T> datatype) {
		return param(null, datatype);
	}

	/**
	 * Create a new parameter wrapper for this fragment with a temporary
	 * relation type. If no matching temporary relation type exists already it
	 * will be created.
	 *
	 * @param name     The name of the parameter relation type
	 * @param datatype The datatype class
	 * @return the parameter wrapper
	 */
	public <T> Parameter<T> param(String name, Class<? super T> datatype) {
		RelationType<T> param = getTemporaryParameterType(name, datatype);

		return param(param).display();
	}

	/**
	 * Returns a parameter for a derived temporary parameter type created by
	 * {@link #getTemporaryParameterType(String, RelationType)}.
	 *
	 * @param originalType The original relation type the new parameter is
	 *                        based
	 *                     on
	 * @return A new parameter wrapper for the derived relation type
	 */
	public <T> Parameter<T> paramLike(RelationType<T> originalType) {
		return param(getTemporaryParameterType(null, originalType));
	}

	/**
	 * Can be implemented by subclasses to initialize the interaction of this
	 * fragment. This method will be invoked on every iteration of this
	 * fragment's interaction, i.e. on the first run and every time after an
	 * interaction event occurred. The default implementation does nothing.
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	public void prepareInteraction() throws Exception {
	}

	/**
	 * Creates a list parameter that displays radio buttons for the
	 * selection of
	 * multiple enum values
	 *
	 * @param enumClass The enum class to create the checkboxes for
	 * @return The new parameter
	 */
	public <E extends Enum<E>> EnumParameter<E> radioButtons(
		Class<E> enumClass) {
		return enumParam(enumClass)
			.input()
			.set(LIST_STYLE, ListStyle.DISCRETE)
			.hideLabel()
			.layout(LayoutType.TABLE)
			.columns(1);
	}

	/**
	 * Re-queries an entity that is stored in certain a process parameter. If
	 * the parameter is NULL it will be ignored.
	 *
	 * @param entityParam The parameter relation type under which the entity is
	 *                    stored
	 */
	@SuppressWarnings("unchecked")
	public <E extends Entity> void reloadEntity(RelationType<E> entityParam) {
		E entity = getParameter(entityParam);

		if (entity != null) {
			try {
				entity = (E) EntityManager.queryEntity(entity.getClass(),
					entity.getId());
				setParameter(entityParam, entity);
			} catch (StorageException e) {
				throw new ProcessException(this, e);
			}
		}
	}

	/**
	 * Removes parameters from the lists returned by the methods
	 * {@link #getInteractionParameters()} and {@link #getInputParameters()}.
	 * These lists must therefore be mutable!
	 *
	 * <p>This implementation replaces the base class implementation because
	 * the parent method changes the interaction parameters of the process
	 * step.</p>
	 *
	 * @param params The parameters to remove
	 */
	@Override
	public void removeInteractionParameters(
		Collection<RelationType<?>> params) {
		getInteractionParameters().removeAll(params);
		getInputParameters().removeAll(params);
		structureModified();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InteractionFragment removeSubFragment(
		RelationType<List<RelationType<?>>> subFragmentParamType) {
		return removeSubFragment(subFragmentParamType, true);
	}

	/**
	 * Removes the fragment of a child view from this fragment.
	 *
	 * @param viewFragmentParamType The parameter type of the view fragment
	 */
	public void removeViewFragment(
		RelationType<List<RelationType<?>>> viewFragmentParamType) {
		boolean modified =
			getProcessStep().isParameterModified(fragmentParam().type());

		getRoot().removeSubFragment(viewFragmentParamType, false);
		get(VIEW_PARAMS).remove(viewFragmentParamType);

		if (!modified) {
			getProcessStep().removeParameterModification(fragmentParam());
		}
	}

	/**
	 * Overridden to forward the call to the enclosing process step.
	 *
	 * @see ProcessStep#set(RelationType, Object)
	 */
	@Override
	public <T> Relation<T> set(RelationType<T> type, T target) {
		return processStep.set(type, target);
	}

	/**
	 * Overridden to remember the continuation parameters of this fragment.
	 *
	 * @see ProcessFragment#setContinueOnInteraction(boolean, RelationType[])
	 */
	@Override
	public void setContinueOnInteraction(boolean continueOnInteration,
		RelationType<?>... params) {
		List<RelationType<?>> paramList = Arrays.asList(params);

		if (continueOnInteration) {
			if (fragmentContinuationParams == null) {
				fragmentContinuationParams = new ArrayList<RelationType<?>>();
			}

			fragmentContinuationParams.addAll(paramList);
		} else if (fragmentContinuationParams != null) {
			fragmentContinuationParams.removeAll(paramList);
		}

		super.setContinueOnInteraction(continueOnInteration, params);
	}

	/**
	 * Create a new temporary relation type with a {@link Set} datatype and
	 * returns a parameter wrapper for it.
	 *
	 * @param name        The name of the relation type
	 * @param elementType The datatype of the set elements
	 * @param ordered     TRUE for a set that keeps the order of it's elements
	 * @return the parameter instance
	 */
	public <T> SetParameter<T> setParam(String name,
		Class<? super T> elementType, boolean ordered) {
		return new SetParameter<>(this,
			getTemporarySetType(name, elementType, ordered));
	}

	/**
	 * Sets the interaction event handler for a certain process parameter.
	 *
	 * @param param              The parameter relation type
	 * @param interactionHandler The interaction handler
	 */
	public void setParameterInteractionHandler(RelationType<?> param,
		InteractionHandler interactionHandler) {
		getProcessStep().setParameterInteractionHandler(param,
			interactionHandler);
	}

	/**
	 * Can be overridden to setup the internal state of a new fragment
	 * instance.
	 * Other than {@link #init()} this method will only be invoked once, right
	 * after an instance has been added to it's process step. The default
	 * implementation does nothing.
	 */
	public void setup() {
	}

	/**
	 * Displays a confirmation message that can either be accepted or rejected.
	 *
	 * @param message           The message to display
	 * @param yesNoQuestion     TRUE for YES and NO dialog buttons, FALSE
	 *                             for OK
	 *                          and CANCEL
	 * @param runOnComfirmation The code to be executed if the user accepts the
	 *                          message
	 * @return The message box fragment
	 */
	public MessageBoxFragment showConfirmationMessage(String message,
		boolean yesNoQuestion, final Runnable runOnComfirmation) {
		if (runOnComfirmation == null) {
			throw new IllegalArgumentException(
				"Runnable parameter must not be NULL");
		}

		return showMessageBox(message, MESSAGE_BOX_QUESTION_ICON,
			new DialogActionListener() {
				@Override
				public void onDialogAction(DialogAction action) {
					if (action == DialogAction.OK ||
						action == DialogAction.YES) {
						runOnComfirmation.run();
					}
				}
			}, yesNoQuestion ? DialogAction.YES_NO : DialogAction.OK_CANCEL);
	}

	/**
	 * Adds a sub-fragment to be displayed as a modal dialog.
	 *
	 * @see InteractionFragment#showDialog(String, InteractionFragment, boolean,
	 * String, DialogActionListener, Collection)
	 */
	public DialogFragment showDialog(String paramNameTemplate,
		InteractionFragment contentFragment,
		DialogActionListener dialogListener, DialogAction... dialogActions) {
		return showDialog(paramNameTemplate, contentFragment, true,
			dialogListener, Arrays.asList(dialogActions));
	}

	/**
	 * Adds a sub-fragment to be displayed as a modal dialog.
	 *
	 * @see InteractionFragment#showDialog(String, InteractionFragment, boolean,
	 * String, DialogActionListener, Collection)
	 */
	public DialogFragment showDialog(String paramNameTemplate,
		InteractionFragment contentFragment, boolean modal,
		DialogActionListener dialogListener,
		Collection<DialogAction> dialogActions) {
		return showDialog(paramNameTemplate, contentFragment, modal, null,
			dialogListener, dialogActions);
	}

	/**
	 * Adds a sub-fragment to be displayed as a dialog. The parameter for the
	 * dialog fragment will be added automatically to the input parameters of
	 * this instance. Therefore the parameter lists of this instance MUST be
	 * mutable!
	 *
	 * <p>If the creating code needs to programmatically close the dialog view
	 * instead of by a button click of the user it can do so by invoking the
	 * {@link ViewFragment#hide()} method on the returned view fragment
	 * instance
	 * on a corresponding interaction.</p>
	 *
	 * @param paramNameTemplate The name template to be used for generated
	 *                          dialog parameter names or NULL to derive it
	 *                          from
	 *                          the content fragment
	 * @param contentFragment   The fragment to be displayed as the dialog
	 *                          content
	 * @param modal             TRUE for a modal view
	 * @param question          A string (typically a question) that will be
	 *                          displayed next to the dialog action buttons.
	 * @param dialogListener    The dialog action listener or NULL for none
	 * @param dialogActions     The actions to be displayed as the dialog
	 *                          buttons
	 * @return The new dialog fragment instance
	 */
	public DialogFragment showDialog(String paramNameTemplate,
		InteractionFragment contentFragment, boolean modal, String question,
		DialogActionListener dialogListener,
		Collection<DialogAction> dialogActions) {
		DialogFragment dialog =
			new DialogFragment(paramNameTemplate, contentFragment, modal,
				question, dialogActions);

		showDialogImpl(dialog, dialogListener);

		return dialog;
	}

	/**
	 * Displays a message with an error icon and a single OK button.
	 *
	 * @see InteractionFragment#showMessageBox(String, String,
	 * DialogActionListener, Collection, RelationType...)
	 */
	public MessageBoxFragment showErrorMessage(String message) {
		return showMessageBox(message, MESSAGE_BOX_ERROR_ICON, null,
			DialogAction.OK);
	}

	/**
	 * Displays a message with an info icon and a single OK button.
	 *
	 * @see InteractionFragment#showMessageBox(String, String,
	 * DialogActionListener, Collection, RelationType...)
	 */
	public MessageBoxFragment showInfoMessage(String message) {
		return showMessageBox(message, MESSAGE_BOX_INFO_ICON, null,
			DialogAction.OK);
	}

	/**
	 * Displays a process message in a message box dialog. The parameter for
	 * the
	 * dialog fragment will be added automatically to the input parameters of
	 * this instance. Therefore the parameter lists of this instance MUST be
	 * mutable!
	 *
	 * @see InteractionFragment#showMessageBox(String, String,
	 * DialogActionListener, Collection, RelationType...)
	 */
	public MessageBoxFragment showMessageBox(String message, String icon,
		DialogActionListener dialogListener, DialogAction... dialogActions) {
		return showMessageBox(message, icon, dialogListener,
			Arrays.asList(dialogActions));
	}

	/**
	 * Displays a process message in a message box dialog. The parameter for
	 * the
	 * dialog fragment will be added automatically to the input parameters of
	 * this instance. Therefore the parameter lists of this instance MUST be
	 * mutable!
	 *
	 * <p>If one or more extras parameters are given they will be displayed
	 * between the message and the dialog buttons. Any necessary initialization
	 * of these parameters including UI properties must be done by the invoking
	 * code before invoking the message box.</p>
	 *
	 * @param message        The message to be displayed in the message box
	 * @param icon           The resource name for an icon or NULL for the
	 *                       standard icon.
	 * @param dialogListener The dialog action listener or NULL for none
	 * @param dialogActions  The actions to be displayed as the message box
	 *                       buttons
	 * @param extraParams    Optional extra parameters to be displayed in the
	 *                       message box
	 * @return The view fragment that has been created for the message box
	 */
	public MessageBoxFragment showMessageBox(String message, String icon,
		DialogActionListener dialogListener,
		Collection<DialogAction> dialogActions,
		RelationType<?>... extraParams) {
		MessageBoxFragment messageBox =
			new MessageBoxFragment(message, icon, dialogActions, extraParams);

		showDialogImpl(messageBox, dialogListener);

		return messageBox;
	}

	/**
	 * Displays a modal dialog with a name prefix that is derived from the name
	 * of the content fragment.
	 *
	 * @see InteractionFragment#showDialog(String, InteractionFragment, boolean,
	 * String, DialogActionListener, Collection)
	 */
	public DialogFragment showModalDialog(InteractionFragment contentFragment,
		Collection<DialogAction> dialogActions) {
		return showDialog(null, contentFragment, true, null, dialogActions);
	}

	/**
	 * Adds a sub-fragment to be displayed as a view. The parameter for the
	 * view
	 * fragment will be added automatically to the input parameters of this
	 * instance. Therefore the parameter lists of this instance MUST be
	 * mutable!
	 *
	 * <p>Because a view has no explicit buttons like dialogs it must be closed
	 * by the creating code by invoking the {@link ViewFragment#hide()} method
	 * on the returned view fragment instance on a corresponding interaction.
	 * </p>
	 *
	 * @param paramNameTemplate The name template to be used for generated view
	 *                          parameter names
	 * @param contentFragment   The fragment to be displayed as the view
	 *                          content
	 * @param modal             TRUE for a modal view
	 * @return The new view fragment to provide access to it's method
	 * {@link ViewFragment#hide()}
	 */
	public ViewFragment showView(String paramNameTemplate,
		InteractionFragment contentFragment, boolean modal) {
		ViewFragment viewFragment =
			new ViewFragment(paramNameTemplate, contentFragment,
				modal ? ViewDisplayType.MODAL_VIEW : ViewDisplayType.VIEW);

		viewFragment.show(this);

		return viewFragment;
	}

	/**
	 * Displays a message with an warning icon and a single OK button.
	 *
	 * @see InteractionFragment#showMessageBox(String, String,
	 * DialogActionListener, Collection, RelationType...)
	 */
	public MessageBoxFragment showWarningMessage(String message) {
		return showMessageBox(message, MESSAGE_BOX_WARNING_ICON, null,
			DialogAction.OK);
	}

	/**
	 * Marks the parameter of this fragment to indicate a structure
	 * modification
	 * by marking it as modified and with
	 * {@link StateProperties#STRUCTURE_CHANGED}.
	 */
	public void structureModified() {
		fragmentParam().modified();
		fragmentParam().set(STRUCTURE_CHANGED);
	}

	/**
	 * Convenience method to create a new temporary parameter relation type
	 * with
	 * a string datatype.
	 *
	 * @see #param(String, Class)
	 */
	public Parameter<String> textParam(String name) {
		return param(name, String.class);
	}

	/**
	 * Creates a parameter that displays a title string (i.e. with the style
	 * {@link LabelStyle#TITLE}).
	 *
	 * @param titleText The title text
	 * @return The label parameter
	 */
	public Parameter<String> title(String titleText) {
		return label(titleText, LabelStyle.TITLE);
	}

	/**
	 * Updates all coupled parameters by retrieving their values from the
	 * coupled sources.
	 *
	 * @param markParamsAsModified If TRUE all interaction parameters of this
	 *                             fragment will be marked as modified
	 * @see de.esoco.process.param.ParameterBase#couple(java.util.function.Consumer,
	 * java.util.function.Supplier)
	 */
	public void updateAllCoupledParameters(boolean markParamsAsModified) {
		List<RelationType<?>> interactionParams = getInteractionParameters();

		RelationCoupling.getAll(getProcess(), interactionParams);

		if (markParamsAsModified) {
			for (RelationType<?> param : interactionParams) {
				markParameterAsModified(param);
			}
		}
	}

	/**
	 * Request a complete update of this fragment's UI by marking all
	 * interaction parameters including their hierarchy as modified.
	 */
	public void updateUserInterface() {
		markParameterHierarchyAsModified(getInteractionParameters());
	}

	/**
	 * Internal method to validate the fragment's process parameters during
	 * state changes of the process. Subclasses must implement
	 * {@link #validateParameters(boolean)} instead.
	 *
	 * @param onInteraction TRUE if the validation occurs during an ongoing
	 *                      interaction, FALSE after the final interaction
	 *                      before the fragment is finished
	 * @return A mapping from invalid parameters to validation error message
	 * (empty for none)
	 */
	public Map<RelationType<?>, String> validateFragmentParameters(
		boolean onInteraction) {
		Map<RelationType<?>, String> validationErrors = new HashMap<>();

		for (InteractionFragment subFragment : getSubFragments()) {
			validationErrors.putAll(
				subFragment.validateFragmentParameters(onInteraction));
		}

		Map<RelationType<?>, String> fragmentErrors =
			validateParameters(onInteraction);

		Map<RelationType<?>, Function<?, String>> validations =
			new LinkedHashMap<>();

		if (onInteraction) {
			RelationType<?> interactionParam = getInteractiveInputParameter();

			Function<?, String> validation =
				getParameterValidations(true).get(interactionParam);

			if (validation != null) {
				validations.put(interactionParam, validation);
			}
		} else {
			validations.putAll(getParameterValidations(true));
			validations.putAll(getParameterValidations(false));
		}

		fragmentErrors.putAll(performParameterValidations(validations));

		if (!fragmentErrors.isEmpty()) {
			validationError(fragmentErrors);
		}

		validationErrors.putAll(fragmentErrors);

		return validationErrors;
	}

	/**
	 * This method can be overridden by subclasses to validate process
	 * parameters during state changes of the process. The default
	 * implementation returns an new empty map instance that may be modified
	 * freely by overriding methods to add their own error messages if
	 * necessary.
	 *
	 * @param onInteraction TRUE if the validation occurs during an ongoing
	 *                      interaction, FALSE after the final interaction
	 *                      before the fragment is finished
	 * @return A mapping from invalid parameters to validation error message
	 * (empty for none)
	 */
	public Map<RelationType<?>, String> validateParameters(
		boolean onInteraction) {
		return new HashMap<RelationType<?>, String>();
	}

	/**
	 * Signals validation errors that occurred in this fragment. The default
	 * implementation delegates the handling to the parent fragment (if such
	 * exists). Subclasses can override this method somewhere in the hierarchy
	 * if they need to display validation errors more prominently. This will
	 * not
	 * override the default process parameter validation.
	 *
	 * @param validationErrors A mapping from parameters to validation error
	 *                         messages
	 */
	public void validationError(Map<RelationType<?>, String> validationErrors) {
		if (parent != null) {
			parent.validationError(validationErrors);
		}
	}

	/**
	 * This method will be invoked if the current execution of this fragment is
	 * aborted before it has finished. and can be overridden by subclasses to
	 * perform data resets similar to the {@link #rollback()} method which will
	 * be invoked if the execution of a finished fragment is to be reverted.
	 */
	protected void abort() {
	}

	/**
	 * Can be implemented by subclasses to react on interactions that occurred
	 * in other fragments. This method will be invoked after
	 * {@link #handleInteraction(RelationType)}. The default implementation
	 * does
	 * nothing.
	 *
	 * @param interactionParam The interaction parameter
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	protected void afterInteraction(RelationType<?> interactionParam)
		throws Exception {
	}

	/**
	 * This method can be overridden by a subclass to indicate whether it
	 * supports a rollback of the modifications it has performed. The default
	 * implementation always returns TRUE.
	 *
	 * @return TRUE if the step implementation support a rollback
	 * @see #rollback()
	 */

	protected boolean canRollback() {
		return true;
	}

	/**
	 * Internal method to finish the fragment execution. Subclasses must
	 * implement {@link #finish()} instead.
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	protected final void finishFragment() throws Exception {
		for (InteractionFragment subFragment : getSubFragments()) {
			subFragment.finishFragment();
		}

		finish();
	}

	/**
	 * Overridden to return the fragment-local validation maps.
	 *
	 * @see de.esoco.process.ProcessElement#getParameterValidations(boolean)
	 */
	@Override
	protected Map<RelationType<?>, Function<?, String>> getParameterValidations(
		boolean onInteraction) {
		return onInteraction ? paramInteractionValidations : paramValidations;
	}

	/**
	 * Returns the root fragment of this fragment's hierarchy.
	 *
	 * @return The root fragment
	 */
	protected InteractionFragment getRoot() {
		return parent != null ? parent.getRoot() : this;
	}

	/**
	 * Overridden to return a parameter ID that is relative to the current
	 * fragment instance.
	 *
	 * @see ProcessFragment#getTemporaryParameterId()
	 */
	@Override
	protected int getTemporaryParameterId() {
		return nextParameterId++;
	}

	/**
	 * Determines the datatype of a certain value. This especially recognizes
	 * anonymous subclasses of enums and returns the correct enum type instead.
	 *
	 * @param value The value to determine the datatype of
	 * @return The value datatype
	 */
	@SuppressWarnings("unchecked")
	protected <T> Class<T> getValueDatatype(T value) {
		Class<T> datatype = (Class<T>) value.getClass();

		if (datatype.isAnonymousClass() && datatype.getSuperclass().isEnum()) {
			datatype = (Class<T>) datatype.getSuperclass();
		}

		return datatype;
	}

	/**
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
	protected void initComplete() throws Exception {
	}

	/**
	 * Will be invoked after the process step of this fragment has been set.
	 * Can
	 * be implemented by subclasses to initialize process step-specific
	 * parameters. The default implementation does nothing.
	 *
	 * @param processStep The process step of this fragment
	 */
	protected void initProcessStep(Interaction processStep) {
	}

	/**
	 * Prepares the upload of a file with . This requires two parameters. One
	 * string parameter that will be configured to invoke a file chooser and
	 * then holds the name of the selected file. This parameter must be
	 * configured as an input parameter. And a target parameter that will
	 * receive the result of a successful file upload.
	 *
	 * @param fileSelectParam The parameter for the file selection
	 * @param uploadHandler   The upload handler
	 * @throws Exception If preparing the upload fails
	 */
	protected void prepareUpload(RelationType<String> fileSelectParam,
		UploadHandler uploadHandler) throws Exception {
		final SessionManager sessionManager =
			getParameter(DataRelationTypes.SESSION_MANAGER);

		String oldUrl = getUIProperty(URL, fileSelectParam);

		if (oldUrl != null) {
			sessionManager.removeUpload(oldUrl);
			getProcessStep().removeCleanupAction(oldUrl);
		}

		final String uploadUrl = sessionManager.prepareUpload(uploadHandler);

		setUIProperty(CONTENT_TYPE, ContentType.FILE_UPLOAD, fileSelectParam);
		setUIProperty(URL, uploadUrl, fileSelectParam);
		setInteractive(InteractiveInputMode.ACTION, fileSelectParam);

		addCleanupAction(uploadUrl,
			f -> sessionManager.removeUpload(uploadUrl));
	}

	/**
	 * Prepares the upload of a file into a process parameter. This requires
	 * two
	 * parameters. One string parameter that will be configured to invoke a
	 * file
	 * chooser and then holds the name of the selected file. This parameter
	 * must
	 * be configured as an input parameter. And a target parameter that will
	 * receive the result of a successful file upload.
	 *
	 * @param fileSelectParam    The parameter for the file selection
	 * @param targetParam        The target parameter for the file content
	 * @param contentTypePattern A pattern that limits allowed content types or
	 *                           NULL for no restriction
	 * @param maxSize            The maximum upload size
	 * @throws Exception If preparing the upload fails
	 * @see #prepareUpload(RelationType, UploadHandler)
	 */
	protected void prepareUpload(RelationType<String> fileSelectParam,
		RelationType<byte[]> targetParam, Pattern contentTypePattern,
		int maxSize) throws Exception {
		ProcessParamUploadHandler uploadHandler =
			new ProcessParamUploadHandler(targetParam, contentTypePattern,
				maxSize);

		prepareUpload(fileSelectParam, uploadHandler);
	}

	/**
	 * Can be overridden to perform a rollback of data and parameter
	 * modifications that have been performed by this fragment. By default all
	 * fragments are assumed to be capable of being rolled back. The default
	 * implementation does nothing.
	 *
	 * @throws Exception If the rollback fails
	 */
	protected void rollback() throws Exception {
	}

	/**
	 * Sets the values of process parameters from the attributes of an entity.
	 * To make this work the given relation types must be entity attribute
	 * types
	 * which will then be set as process parameters. The attributes can either
	 * by direct or extra attributes (which must have the extra attribute flag
	 * set).
	 *
	 * <p>To set modified parameter values back into the entity the method
	 * {@link #updateEntityFromParameterValues(Entity, List)} can be invoked.
	 * </p>
	 *
	 * @param entity     The entity to read the attributes from
	 * @param attributes The entity attributes and process parameters
	 * @throws StorageException If querying an extra attribute fails
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setParameterValuesFromEntity(Entity entity,
		List<RelationType<?>> attributes) throws StorageException {
		for (RelationType<?> param : attributes) {
			if (entity.getDefinition().getAttributes().contains(param)) {
				Object value = entity.hasRelation(param) ?
				               entity.get(param) :
				               param.initialValue(entity);

				setParameter((RelationType) param, value);
			} else if (param.hasFlag(ExtraAttributes.EXTRA_ATTRIBUTE_FLAG)) {
				setParameter((RelationType) param,
					entity.getExtraAttribute(param, null));
			}
		}
	}

	/**
	 * @see #setParameterValuesFromEntity(Entity, List)
	 */
	protected void setParameterValuesFromEntity(Entity entity,
		RelationType<?>... params) throws StorageException {
		setParameterValuesFromEntity(entity, Arrays.asList(params));
	}

	/**
	 * Sets the parent fragment of this instance.
	 *
	 * @param parent The parent fragment
	 */
	protected void setParent(InteractionFragment parent) {
		this.parent = parent;
	}

	/**
	 * Updates the attributes of an entity from the process parameter values
	 * that are stored with the given entity attribute relation types. The
	 * attributes can either be direct or extra attributes.
	 *
	 * <p>To set the process parameters from entity attributes the reverse
	 * method {@link #setParameterValuesFromEntity(Entity, List)} can be used.
	 * </p>
	 *
	 * @param entity The entity to update
	 * @param params The process parameter and entity attribute relation types
	 * @throws StorageException if setting an extra attribute fails
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void updateEntityFromParameterValues(Entity entity,
		List<RelationType<?>> params) throws StorageException {
		for (RelationType<?> param : params) {
			if (hasParameter(param)) {
				if (entity.getDefinition().getAttributes().contains(param)) {
					entity.set((RelationType) param, getParameter(param));
				} else if (param.hasFlag(
					ExtraAttributes.EXTRA_ATTRIBUTE_FLAG)) {
					entity.setExtraAttribute((RelationType) param,
						getParameter(param));
				}
			}
		}
	}

	/**
	 * Internal method that handles the invocation of
	 * {@link #afterInteraction(RelationType)} for this instance and all
	 * registered sub-fragments.
	 *
	 * @param interactionParam The interaction parameter
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	final void afterFragmentInteraction(RelationType<?> interactionParam)
		throws Exception {
		for (InteractionFragment subFragment : getSubFragments()) {
			subFragment.afterFragmentInteraction(interactionParam);
		}

		afterInteraction(interactionParam);
	}

	/**
	 * This method can be overridden by a subclass to indicate whether it
	 * supports a rollback of the modifications it has performed. The default
	 * implementation always returns TRUE.
	 *
	 * @return TRUE if the step implementation support a rollback
	 * @see #rollback()
	 */

	final boolean canFragmentRollback() {
		for (InteractionFragment subFragment : getSubFragments()) {
			if (!subFragment.canFragmentRollback()) {
				return false;
			}
		}

		return canRollback();
	}

	/**
	 * Checks whether this fragment contains a certain continuation parameter.
	 *
	 * @param continuationParam The continuation parameter to check
	 * @return TRUE if the given continuation parameter belongs to this
	 * fragment
	 */
	InteractionFragment getContinuationFragment(
		RelationType<?> continuationParam) {
		InteractionFragment continuationFragment = null;

		for (InteractionFragment subFragment : getSubFragments()) {
			continuationFragment =
				subFragment.getContinuationFragment(continuationParam);

			if (continuationFragment != null) {
				break;
			}
		}

		if (continuationFragment == null &&
			fragmentContinuationParams != null &&
			fragmentContinuationParams.contains(continuationParam)) {
			continuationFragment = this;
		}

		return continuationFragment;
	}

	/**
	 * Internal method that handles the invocation of
	 * {@link #handleInteraction(RelationType)} for this instance and all
	 * registered sub-fragments.
	 *
	 * @param interactionParam The interaction parameter
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	final void handleFragmentInteraction(RelationType<?> interactionParam)
		throws Exception {
		boolean rootFragmentInteraction = true;

		for (InteractionFragment subFragment : getSubFragments()) {
			if (subFragment.hasFragmentInteraction(interactionParam)) {
				subFragment.handleFragmentInteraction(interactionParam);
				rootFragmentInteraction = false;

				break;
			}
		}

		if (rootFragmentInteraction || hasInteraction(interactionParam)) {
			handleInteraction(interactionParam);
		}
	}

	/**
	 * Internal method to check whether an interaction has been caused by an
	 * interaction parameter from this fragment or one of it's sub-fragments.
	 * Subclasses must implement {@link #hasInteraction(RelationType)} instead.
	 *
	 * @param interactionParam The interaction parameter to check
	 * @return TRUE if the interaction was caused by a parameter of this
	 * fragment
	 */
	final boolean hasFragmentInteraction(RelationType<?> interactionParam) {
		for (InteractionFragment subFragment : getSubFragments()) {
			if (subFragment.hasFragmentInteraction(interactionParam)) {
				return true;
			}
		}

		return hasInteraction(interactionParam);
	}

	/**
	 * Internal method to initialize this fragment. Subclasses must implement
	 * {@link #init()} instead.
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	final void initFragment() throws Exception {
		getSubFragments().clear();
		init();

		for (InteractionFragment subFragment : getSubFragments()) {
			subFragment.initFragment();
		}

		markFragmentInputParams();
		initialized = true;
		initComplete();
	}

	/**
	 * Internal method to prepare each interaction of this fragment. Subclasses
	 * must implement {@link #prepareInteraction()} instead.
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	final void prepareFragmentInteraction() throws Exception {
		prepareInteraction();

		for (InteractionFragment subFragment : getSubFragments()) {
			subFragment.prepareFragmentInteraction();
		}
	}

	/**
	 * Internal method to perform a rollback of data and parameter
	 * modifications
	 * that have been performed by this fragment. Subclasses must implement
	 * {@link #rollback()} instead.
	 *
	 * @throws Exception On errors
	 */
	final void rollbackFragment() throws Exception {
		for (InteractionFragment subFragment : getSubFragments()) {
			subFragment.rollbackFragment();
		}

		rollback();
	}

	/**
	 * Package-internal method to associate this fragment with a particular
	 * interactive process step.
	 *
	 * @param processStep The process step this fragment belongs to
	 */
	final void setProcessStep(Interaction processStep) {
		this.processStep = processStep;

		if (processStep != null) {
			initProcessStep(processStep);
		}
	}

	/**
	 * Adds a new sub-fragment through
	 * {@link ProcessFragment#addSubFragment(RelationType,
	 * InteractionFragment)}.
	 *
	 * @param subFragmentParamType The type of the parameter containing the
	 *                             child fragment parameters
	 * @param subFragment          The child fragment
	 * @param markAsModified       TRUE to mark the parameter of this fragment
	 *                             and it's structure as modified
	 */
	private void addSubFragment(
		RelationType<List<RelationType<?>>> subFragmentParamType,
		InteractionFragment subFragment, boolean markAsModified) {
		subFragment.setParent(this);

		super.addSubFragment(subFragmentParamType, subFragment);

		if (markAsModified) {
			structureModified();
		}

		if (initialized) {
			// if a fragment is added after initialization of the parent has
			// already completed it's init() method needs to be invoked too
			try {
				subFragment.initFragment();
			} catch (Exception e) {
				throw new RuntimeProcessException(subFragment, e);
			}
		}
	}

	/**
	 * Internal method that tries to lock an entity during the execution of
	 * either the current step or the remaining process execution. It displays
	 * an information message if the entity is already locked by some other
	 * context. The lock will automatically be removed if the process
	 * progresses
	 * to another step (including rollback) or is terminated in any way.
	 *
	 * @param entity                 The entity to lock
	 * @param lockUnavailableMessage The message to display if the lock
	 *                                  couldn't
	 *                               be acquired
	 * @param inProcess              TRUE to lock the entity for the process,
	 *                               FALSE to lock it only for the current step
	 * @return TRUE if the lock could be acquired
	 * @see #lockEntityForProcess(Entity, String)
	 * @see #lockEntityForStep(Entity, String)
	 */
	private boolean lockEntity(Entity entity, String lockUnavailableMessage,
		boolean inProcess) {
		boolean locked =
			inProcess ? getProcess().lockEntity(entity) : lockEntity(entity);

		if (!locked) {
			showInfoMessage(lockUnavailableMessage);
		}

		return locked;
	}

	/**
	 * Removes a child fragment from this parent by invoking
	 * {@link ProcessFragment#removeSubFragment(InteractionFragment)}.
	 *
	 * @param subFragmentParamType The type of the parameter containing the
	 *                             fragment parameters
	 * @param markAsModified       TRUE to mark the parameter of this fragment
	 *                             and it's structure as modified
	 * @return TODO: DOCUMENT ME!
	 */
	private InteractionFragment removeSubFragment(
		RelationType<List<RelationType<?>>> subFragmentParamType,
		boolean markAsModified) {
		InteractionFragment subFragment =
			super.removeSubFragment(subFragmentParamType);

		if (subFragment != null) {
			subFragment.setProcessStep(null);
			subFragment.setParent(null);

			if (markAsModified) {
				structureModified();
			}
		}

		return subFragment;
	}

	/**
	 * Internal method that displays any kind of dialog fragment.
	 *
	 * @param dialogFragment The dialog fragment
	 * @param dialogListener An optional dialog listener or NULL for none
	 * @throws Exception If displaying the dialog fails
	 */
	private void showDialogImpl(DialogFragment dialogFragment,
		DialogActionListener dialogListener) {
		if (dialogListener != null) {
			dialogFragment.addDialogActionListener(dialogListener);
		}

		dialogFragment.show(this);
	}

	/**
	 * An implementation of the {@link UploadHandler} interface that writes
	 * uploaded data into a process parameter. This class is used internally by
	 * {@link InteractionFragment#prepareUpload(RelationType, RelationType,
	 * Pattern, int)}.
	 *
	 * @author eso
	 */
	class ProcessParamUploadHandler implements UploadHandler {

		private final RelationType<byte[]> targetParam;

		private final Pattern contentTypePattern;

		private final int maxSize;

		/**
		 * Creates a new instance.
		 *
		 * @param targetParam        The target parameter for the uploaded data
		 * @param contentTypePattern A pattern that limits allowed content
		 *                                 types
		 *                           or NULL for no restriction
		 * @param maxSize            The maximum upload size
		 */
		public ProcessParamUploadHandler(RelationType<byte[]> targetParam,
			Pattern contentTypePattern, int maxSize) {
			this.targetParam = targetParam;
			this.contentTypePattern = contentTypePattern;
			this.maxSize = maxSize;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void processUploadData(String filename, String contentType,
			InputStream dataStream) throws Exception {
			byte[] buf = new byte[1024 * 16];
			int read;

			if (contentTypePattern != null &&
				!contentTypePattern.matcher(contentType).matches()) {
				error("InvalidUploadContentType");
			}

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();

			while ((read = dataStream.read(buf, 0, buf.length)) != -1) {
				if (outStream.size() + read <= maxSize) {
					outStream.write(buf, 0, read);
				} else {
					error("UploadSizeLimitExceeded");
				}
			}

			setParameter(targetParam, outStream.toByteArray());
			removeParameterAnnotation(targetParam, ERROR_MESSAGE);
		}

		/**
		 * Sets an error message on the target parameter and then throws an
		 * exception.
		 *
		 * @param message The error message
		 * @throws ProcessException The error exception
		 */
		private void error(String message) throws ProcessException {
			annotateParameter(targetParam, null, ERROR_MESSAGE, message);
			throw new ProcessException(getProcessStep(), message);
		}
	}
}
