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
package de.esoco.process.step;

import de.esoco.data.DataRelationTypes;
import de.esoco.data.SessionManager;
import de.esoco.data.UploadHandler;
import de.esoco.data.element.DataElementList.ViewDisplayType;
import de.esoco.data.element.SelectionDataElement;

import de.esoco.entity.Entity;
import de.esoco.entity.EntityRelationTypes.HierarchicalQueryMode;
import de.esoco.entity.ExtraAttributes;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.function.AbstractAction;
import de.esoco.lib.property.Updatable;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.property.UserInterfaceProperties.ContentType;
import de.esoco.lib.property.UserInterfaceProperties.InteractiveInputMode;

import de.esoco.process.Parameter;
import de.esoco.process.ParameterList;
import de.esoco.process.Process;
import de.esoco.process.ProcessElement;
import de.esoco.process.ProcessException;
import de.esoco.process.ProcessFragment;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ProcessStep;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.step.DialogFragment.DialogAction;
import de.esoco.process.step.DialogFragment.DialogActionListener;
import de.esoco.process.step.Interaction.InteractionHandler;

import de.esoco.storage.QueryPredicate;
import de.esoco.storage.StorageException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;

import static de.esoco.entity.EntityPredicates.forEntity;
import static de.esoco.entity.EntityRelationTypes.HIERARCHICAL_QUERY_MODE;

import static de.esoco.lib.property.UserInterfaceProperties.CONTENT_TYPE;
import static de.esoco.lib.property.UserInterfaceProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.UserInterfaceProperties.DISABLED;
import static de.esoco.lib.property.UserInterfaceProperties.URL;

import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.ORIGINAL_RELATION_TYPE;
import static de.esoco.process.ProcessRelationTypes.PARAM_UPDATE_LISTENERS;

import static org.obrel.type.StandardTypes.ERROR_MESSAGE;


/********************************************************************
 * A process element subclass that serves as a fragment of an interactive
 * process step. This allows to split the user interface of complex interactions
 * into different parts that can more easily be re-used.
 *
 * @author eso
 */
public abstract class InteractionFragment extends ProcessFragment
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The resource string for an error message box icon. */
	public static final String MESSAGE_BOX_ERROR_ICON = "#imErrorMessage";

	/** The resource string for a warning message box icon. */
	public static final String MESSAGE_BOX_WARNING_ICON = "#imWarningMessage";

	/** The resource string for a question message box icon. */
	public static final String MESSAGE_BOX_QUESTION_ICON = "#imQuestionMessage";

	/** The resource string for an info message box icon. */
	public static final String MESSAGE_BOX_INFO_ICON = "#imInfoMessage";

	private static int nNextFragmentId = 0;

	//~ Instance fields --------------------------------------------------------

	private int nFragmentId = nNextFragmentId++;

	private boolean bInitialized = false;

	private Interaction						    rProcessStep;
	private InteractionFragment				    rParent;
	private RelationType<List<RelationType<?>>> rFragmentParam;

	private List<RelationType<?>> aFragmentContinuationParams = null;

	private List<RelationType<?>> aInteractionParams = new ArrayList<>();
	private Set<RelationType<?>>  aInputParams		 = new HashSet<>();

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
	public abstract void init() throws Exception;

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
			// do not add parameters that are displayed in panels because they
			// are stored in the parameter list of the panel parameter
			if (!isPanelParameter(rParam) &&
				!rInteractionParams.contains(rParam))
			{
				rInteractionParams.add(rParam);
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
	public Parameter<List<RelationType<?>>> addSubFragment(
		InteractionFragment rSubFragment)
	{
		return addSubFragment(rSubFragment.getClass().getSimpleName(),
							  rSubFragment);
	}

	/***************************************
	 * Adds a subordinate fragment to this instance into a temporary parameter
	 * and directly displays it.
	 *
	 * @see #addSubFragment(String, InteractionFragment, boolean)
	 */
	public Parameter<List<RelationType<?>>> addSubFragment(
		String				sName,
		InteractionFragment rSubFragment)
	{
		return addSubFragment(sName, rSubFragment, true);
	}

	/***************************************
	 * Overridden to set the parent of the sub-fragment to this instance.
	 *
	 * @return
	 *
	 * @see    ProcessFragment#addSubFragment(RelationType, InteractionFragment)
	 */
	@Override
	public void addSubFragment(
		RelationType<List<RelationType<?>>> rFragmentParam,
		InteractionFragment					rSubFragment)
	{
		rSubFragment.rParent = this;

		super.addSubFragment(rFragmentParam, rSubFragment);

		if (bInitialized)
		{
			// if a fragment is added after initialization of the parent has
			// already completed it's init() method needs to be invoked too
			try
			{
				rSubFragment.init();
			}
			catch (Exception e)
			{
				throw new RuntimeProcessException(rSubFragment, e);
			}
		}
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
	 * @param  bDisplay     TRUE to invoke {@link Parameter#display()} on the
	 *                      new fragment parameter
	 *
	 * @return The wrapper for the fragment parameter
	 */
	public Parameter<List<RelationType<?>>> addSubFragment(
		String				sName,
		InteractionFragment rSubFragment,
		boolean				bDisplay)
	{
		Parameter<List<RelationType<?>>> rSubFragmentParam =
			listParam(sName, RelationType.class);

		addSubFragment(rSubFragmentParam.type(), rSubFragment);

		if (bDisplay)
		{
			rSubFragmentParam.display();
		}

		return rSubFragmentParam.hideLabel();
	}

	/***************************************
	 * Internal method that will be invoked to attach this fragment to the given
	 * process step and fragment parameter.
	 *
	 * @param rProcessStep   The process step to attach this instance to
	 * @param rFragmentParam The parameter this fragment will be stored in
	 */
	public void attach(
		Interaction							rProcessStep,
		RelationType<List<RelationType<?>>> rFragmentParam)
	{
		this.rFragmentParam = rFragmentParam;

		setProcessStep(rProcessStep);
		setup();
	}

	/***************************************
	 * Can be overridden by subclasses to perform resource cleanups when the
	 * process ends. The default implementation does nothing.
	 *
	 * @see ProcessStep#cleanup()
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
	 * Convenience method to create a new temporary parameter relation type with
	 * a {@link Date} datatype.
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
		if (bEnable)
		{
			clearUIFlag(DISABLED, getInputParameters());
		}
		else
		{
			setUIFlag(DISABLED, getInputParameters());
		}

		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rSubFragment.enableEdit(bEnable);
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
	 * Creates a new parameter wrapper for the relation type this fragment is
	 * stored in.
	 *
	 * @return the parameter wrapper for the fragment parameter
	 */
	public ParameterList fragmentParam()
	{
		return new ParameterList(rParent != null ? rParent : this,
								 getFragmentParameter(),
								 false);
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
		return rFragmentParam;
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
		CollectionUtil.insert(getInteractionParameters(),
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
	 * Create a new parameter wrapper for this fragment with a temporary
	 * relation type.
	 *
	 * @param  sName        The name of the relation type
	 * @param  rElementType rDatatype The parameter datatype
	 *
	 * @return the parameter instance
	 */
	public <T> Parameter<List<T>> listParam(
		String			 sName,
		Class<? super T> rElementType)
	{
		return param(getTemporaryListType(sName, rElementType));
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
	 * Creates a new temporary relation type for a list of relation types and
	 * returns a parameter wrapper for it.
	 *
	 * @param  sName The name of the parameter list
	 *
	 * @return the parameter wrapper for the parameter list
	 */
	public ParameterList panel(String sName)
	{
		RelationType<List<RelationType<?>>> rListType =
			getTemporaryListType(sName, RelationType.class);

		return new ParameterList(this, rListType, true);
	}

	/***************************************
	 * Creates a new parameter wrapper for the given relation type in this
	 * fragment.
	 *
	 * @param  rParam The parameter to wrap
	 *
	 * @return the parameter wrapper
	 */
	public <T> Parameter<T> param(RelationType<T> rParam)
	{
		return new Parameter<>(this, rParam);
	}

	/***************************************
	 * Convenience method to create a new temporary parameter relation type with
	 * an enum datatype. The parameter will be named with the simple name of the
	 * enum class.
	 *
	 * @see #param(String, Class)
	 */
	public <E extends Enum<E>> Parameter<E> param(Class<E> rEnumClass)
	{
		return param(rEnumClass.getSimpleName(), rEnumClass);
	}

	/***************************************
	 * Create a new parameter wrapper for this fragment with a temporary
	 * relation type. If no matching temporary relation type exists already it
	 * will be created.
	 *
	 * @param  sName     The name of the relation type
	 * @param  rDatatype The parameter datatype
	 *
	 * @return the parameter wrapper
	 */
	public <T> Parameter<T> param(String sName, Class<? super T> rDatatype)
	{
		return param(getTemporaryParameterType(sName, rDatatype));
	}

	/***************************************
	 * Creates a new temporary parameter relation type that is derived from
	 * another relation type. The other type must be from a different scope
	 * (i.e. not the same fragment) or else a name conflict will occur. The
	 * derived relation type will have the original relation in a meta relation
	 * with the type {@link ProcessRelationTypes#ORIGINAL_RELATION_TYPE}.
	 *
	 * @param  rOriginalType The original relation type the new parameter is
	 *                       based on
	 *
	 * @return A new parameter wrapper for the derived relation type
	 */
	public <T> Parameter<T> paramLike(RelationType<T> rOriginalType)
	{
		Parameter<T> rDerivedParam =
			param(rOriginalType.getSimpleName(), rOriginalType.getTargetType());

		rDerivedParam.type().set(ORIGINAL_RELATION_TYPE, rOriginalType);

		return rDerivedParam;
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
	}

	/***************************************
	 * Removes a subordinate fragment that had been added previously by means of
	 * {@link #addSubFragment(RelationType, InteractionFragment)}.
	 *
	 * @param rFragmentParam The target parameter of the fragment parameters
	 * @param rSubFragment   The sub-fragment instance to remove
	 */
	public void removeSubFragment(
		RelationType<List<RelationType<?>>> rFragmentParam,
		InteractionFragment					rSubFragment)
	{
		get(INPUT_PARAMS).removeAll(rSubFragment.getInputParameters());
		get(INPUT_PARAMS).remove(rFragmentParam);
		getSubFragments().remove(rSubFragment);
		deleteParameters(rFragmentParam);
		rSubFragment.setProcessStep(null);

		rSubFragment.rParent = null;
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
	 * Sets the interaction handler for a certain parameter.
	 *
	 * @see Interaction#setParameterInteractionHandler(RelationType, InteractionHandler)
	 */
	public void setParameterInteractionHandler(
		RelationType<?>    rParam,
		InteractionHandler rInteractionHandler)
	{
		getProcessStep().setParameterInteractionHandler(rParam,
														rInteractionHandler);
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
	 * Request a complete update of this fragment's UI by marking all
	 * interaction parameters including their hierarchy as modified.
	 */
	public void updateUserInterface()
	{
		markParameterHierarchyAsModified(getInteractionParameters());
	}

	/***************************************
	 * This method can be overridden by subclasses to validate process
	 * parameters during state changes of the process. The default
	 * implementation returns an new empty map instance that may be modified
	 * freely by overriding methods to add their own error messages if
	 * necessary.
	 *
	 * @see ProcessStep#validateParameters(boolean)
	 */
	public Map<RelationType<?>, String> validateParameters(
		boolean bOnInteraction)
	{
		return new HashMap<RelationType<?>, String>();
	}

	/***************************************
	 * This method will be invoked if the current execution of this fragment is
	 * aborted and can be overridden by subclasses to perform data resets
	 * similar to the {@link #rollback()} method.
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
	 * @see    ProcessStep#canRollback()
	 */

	protected boolean canRollback()
	{
		return true;
	}

	/***************************************
	 * Overridden to return a package name that is relative to the current
	 * fragment instance.
	 *
	 * @see ProcessFragment#getTemporaryParameterPackage()
	 */
	@Override
	protected String getTemporaryParameterPackage()
	{
		return getClass().getSimpleName().toLowerCase() + nFragmentId;
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
	 */
	protected void prepareUpload(RelationType<String> rFileSelectParam,
								 RelationType<byte[]> rTargetParam,
								 Pattern			  rContentTypePattern,
								 int				  nMaxSize) throws Exception
	{
		final SessionManager rSessionManager =
			getParameter(DataRelationTypes.SESSION_MANAGER);

		ProcessParamUploadHandler aUploadHandler =
			new ProcessParamUploadHandler(rTargetParam,
										  rContentTypePattern,
										  nMaxSize);

		String sOldUrl = getUIProperty(URL, rFileSelectParam);

		if (sOldUrl != null)
		{
			rSessionManager.removeUpload(sOldUrl);
			getProcessStep().removeFinishAction(sOldUrl);
		}

		final String sUploadUrl = rSessionManager.prepareUpload(aUploadHandler);

		setUIProperty(CONTENT_TYPE, ContentType.FILE_UPLOAD, rFileSelectParam);
		setUIProperty(URL, sUploadUrl, rFileSelectParam);
		setInteractive(InteractiveInputMode.ACTION, rFileSelectParam);

		getProcessStep().addFinishAction(sUploadUrl,
			new AbstractAction<ProcessStep>("removeUpload")
			{
				@Override
				public void execute(ProcessStep rValue)
				{
					rSessionManager.removeUpload(sUploadUrl);
				}
			});
	}

	/***************************************
	 * Can be overridden to perform a rollback of data and parameter
	 * modifications that have been performed by this fragment. By default all
	 * fragments are assumed to be capable of being rolled back. The default
	 * implementation does nothing.
	 *
	 * @see ProcessStep#rollback()
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
				setParameter((RelationType) rParam,
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
	 * Sets the parent value.
	 *
	 * @param rParent The parent value
	 */
	protected final void setParent(InteractionFragment rParent)
	{
		this.rParent = rParent;
	}

	/***************************************
	 * Can be overridden to setup the internal state of a new fragment instance.
	 * Other than {@link #init()} this method will only be invoked once, right
	 * after an instance has been added to it's process step. The default
	 * implementation does nothing.
	 */
	protected void setup()
	{
	}

	/***************************************
	 * Adds a sub-fragment to be displayed as a modal dialog.
	 *
	 * @see #showDialog(String, InteractionFragment, ViewDisplayType,
	 *      DialogActionListener, DialogAction...)
	 */
	protected DialogFragment showDialog(String				 sParamNameTemplate,
										InteractionFragment  rContentFragment,
										DialogActionListener rDialogListener,
										DialogAction... 	 rDialogActions)
		throws Exception
	{
		return showDialog(sParamNameTemplate,
						  rContentFragment,
						  true,
						  rDialogListener,
						  Arrays.asList(rDialogActions));
	}

	/***************************************
	 * Adds a sub-fragment to be displayed as a modal dialog.
	 *
	 * @see #showDialog(String, InteractionFragment, boolean, String,
	 *      DialogActionListener, Collection)
	 */
	protected DialogFragment showDialog(
		String					 sParamNameTemplate,
		InteractionFragment		 rContentFragment,
		boolean					 bModal,
		DialogActionListener	 rDialogListener,
		Collection<DialogAction> rDialogActions) throws Exception
	{
		return showDialog(sParamNameTemplate,
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
	 *
	 * @throws Exception If displaying the dialog fails
	 */
	protected DialogFragment showDialog(
		String					 sParamNameTemplate,
		InteractionFragment		 rContentFragment,
		boolean					 bModal,
		String					 sQuestion,
		DialogActionListener	 rDialogListener,
		Collection<DialogAction> rDialogActions) throws Exception
	{
		DialogFragment aDialog =
			new DialogFragment(sParamNameTemplate,
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
	 * @see #showMessageBox(String, String, DialogActionListener, Collection,
	 *      RelationType...)
	 */
	protected MessageBoxFragment showErrorMessage(String sMessage)
	{
		return showMessageBox(sMessage,
							  MESSAGE_BOX_ERROR_ICON,
							  null,
							  DialogAction.OK);
	}

	/***************************************
	 * Displays a message with an info icon and a single OK button.
	 *
	 * @see #showMessageBox(String, String, DialogActionListener, Collection,
	 *      RelationType...)
	 */
	protected MessageBoxFragment showInfoMessage(String sMessage)
	{
		return showMessageBox(sMessage,
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
	 * @see #showMessageBox(String, String, DialogActionListener, Collection,
	 *      RelationType...)
	 */
	protected MessageBoxFragment showMessageBox(
		String				 sMessage,
		String				 sIcon,
		DialogActionListener rDialogListener,
		DialogAction... 	 rDialogActions)
	{
		return showMessageBox(sMessage,
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
	protected MessageBoxFragment showMessageBox(
		String					 sMessage,
		String					 sIcon,
		DialogActionListener	 rDialogListener,
		Collection<DialogAction> rDialogActions,
		RelationType<?>... 		 rExtraParams)
	{
		MessageBoxFragment aMessageBox =
			new MessageBoxFragment(sMessage,
								   sIcon,
								   rDialogActions,
								   rExtraParams);

		try
		{
			showDialogImpl(aMessageBox, rDialogListener);
		}
		catch (Exception e)
		{
			// message boxes should not fail
			throw new IllegalStateException(e);
		}

		return aMessageBox;
	}

	/***************************************
	 * Displays a modal dialog with a name prefix that is derived from the name
	 * of the content fragment.
	 *
	 * @see #showDialog(String, InteractionFragment, boolean,
	 *      DialogActionListener, Collection)
	 */
	protected DialogFragment showModalDialog(
		InteractionFragment		 rContentFragment,
		Collection<DialogAction> rDialogActions) throws Exception
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
	 *
	 * @throws Exception If displaying the dialog fails
	 */
	protected ViewFragment showView(String				sParamNameTemplate,
									InteractionFragment rContentFragment,
									boolean				bModal) throws Exception
	{
		ViewFragment aViewFragment =
			new ViewFragment(sParamNameTemplate,
							 rContentFragment,
							 bModal ? ViewDisplayType.MODAL_VIEW
									: ViewDisplayType.VIEW);

		aViewFragment.show(this);

		return aViewFragment;
	}

	/***************************************
	 * Displays a message with an warning icon and a single OK button.
	 *
	 * @see #showMessageBox(String, String, DialogActionListener, Collection,
	 *      RelationType...)
	 */
	protected MessageBoxFragment showWarningMessage(String sMessage)
	{
		return showMessageBox(sMessage,
							  MESSAGE_BOX_WARNING_ICON,
							  null,
							  DialogAction.OK);
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
					rEntity.setExtraAttribute((RelationType) rParam,
											  getParameter(rParam));
				}
			}
		}
	}

	/***************************************
	 * Internal method to abort the current execution of this fragment. It can
	 * be used to undo data and parameter initializations or interactive
	 * modifications that have been performed by this fragment. Subclasses must
	 * implement {@link #abort()} instead.
	 *
	 * @throws Exception On errors
	 */
	final void abortFragment() throws Exception
	{
		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rSubFragment.abortFragment();
		}

		abort();
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
	 * @see    ProcessStep#canRollback()
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
	 * Internal method to finish the fragment execution. Subclasses must
	 * implement {@link #finish()} instead.
	 *
	 * @throws Exception Any kind of exception may be thrown in case of errors
	 */
	final void finishFragment() throws Exception
	{
		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rSubFragment.finishFragment();
		}

		finish();
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
	 * Sets the parameter this fragment is displayed in.
	 *
	 * @param rFragmentParam The fragment parameter
	 */
	final void setFragmentParam(
		RelationType<List<RelationType<?>>> rFragmentParam)
	{
		this.rFragmentParam = rFragmentParam;
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
		initProcessStep(rProcessStep);
	}

	/***************************************
	 * Internal method to validate the fragment's process parameters during
	 * state changes of the process. Subclasses must implement {@link
	 * #validateParameters(boolean)} instead.
	 *
	 * @see ProcessStep#validateParameters(boolean)
	 */
	Map<RelationType<?>, String> validateFragmentParameters(
		boolean bOnInteraction)
	{
		HashMap<RelationType<?>, String> rErrorParams =
			new HashMap<RelationType<?>, String>();

		for (InteractionFragment rSubFragment : getSubFragments())
		{
			rErrorParams.putAll(rSubFragment.validateFragmentParameters(bOnInteraction));
		}

		rErrorParams.putAll(validateParameters(bOnInteraction));

		return rErrorParams;
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
		DialogActionListener rDialogListener) throws Exception
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
	 * int)}.
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
