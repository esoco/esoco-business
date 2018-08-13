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

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.property.InteractionEventType;

import de.esoco.process.ProcessException;
import de.esoco.process.ProcessStep;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.obrel.core.RelationType;
import org.obrel.type.MetaTypes;

import static de.esoco.process.ProcessRelationTypes.CONTINUATION_FRAGMENT_CLASS;
import static de.esoco.process.ProcessRelationTypes.CONTINUATION_PARAM;
import static de.esoco.process.ProcessRelationTypes.CONTINUATION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_EVENT_TYPE;

import static org.obrel.type.MetaTypes.INTERACTIVE;


/********************************************************************
 * A generic process step for interactions in a process. The interaction is
 * achieved by setting the flag relation {@link MetaTypes#INTERACTIVE} in the
 * {@link #setup()} method. Please see the documentation of {@link Process} for
 * general information about interactive processes.
 *
 * <p>This class can either be used to subclass it and implement functionality
 * in the {@link #execute()} method that will be invoked after the interaction
 * has been completed. Or it can be used directly to create process steps that
 * are only intended to collect informations into process parameters and to
 * leave their processing to further process steps.</p>
 *
 * <p>Although it is possible to implement interactive process steps that are
 * derived from other step classes it is recommended to derive interactive steps
 * from this class because it handles several typical interaction cases like
 * automatic interaction loops (see {@link #getNextStep()}.</p>
 *
 * @author eso
 */
public class Interaction extends RollbackStep
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Map<RelationType<?>, InteractionHandler> aParamInteractionHandlers;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the currently registered interaction handler for a certain
	 * parameter.
	 *
	 * @param  rParam The parameter to return the interaction handler for
	 *
	 * @return The registered interaction handler or NULL for none
	 */
	public InteractionHandler getParameterInteractionHandler(
		RelationType<?> rParam)
	{
		InteractionHandler rInteractionHandler = null;

		if (aParamInteractionHandlers != null)
		{
			rInteractionHandler = aParamInteractionHandlers.get(rParam);
		}

		return rInteractionHandler;
	}

	/***************************************
	 * Inserts a display parameter before another parameter in the list of this
	 * step's interaction parameters. If the parameter to insert before doesn't
	 * exist the new parameter will be added to the end of the list.
	 *
	 * @param rParam       The parameter type to insert
	 * @param rBeforeParam The parameter type to insert the new type before
	 */
	public void insertDisplayParameter(
		RelationType<?> rParam,
		RelationType<?> rBeforeParam)
	{
		CollectionUtil.insert(get(INTERACTION_PARAMS), rBeforeParam, rParam);

		prepareNewInteractionParameters(CollectionUtil.setOf(rParam));
	}

	/***************************************
	 * Inserts an input parameter before another parameter in the list of this
	 * step's interaction and input parameters. If the parameter to insert
	 * before doesn't exist the new parameter will be added to the end of the
	 * list.
	 *
	 * @param rParam       The parameter type to insert
	 * @param rBeforeParam The parameter type to insert the new type before
	 */
	public void insertInputParameter(
		RelationType<?> rParam,
		RelationType<?> rBeforeParam)
	{
		insertDisplayParameter(rParam, rBeforeParam);
		get(INPUT_PARAMS).add(rParam);
	}

	/***************************************
	 * Sets the interaction handler for a certain parameter. This will replace
	 * any previous interaction handler for this parameter.
	 *
	 * @param rParam              The parameter to add the handler for
	 * @param rInteractionHandler The interaction handler or NULL to remove the
	 *                            current handler
	 */
	public void setParameterInteractionHandler(
		RelationType<?>    rParam,
		InteractionHandler rInteractionHandler)
	{
		if (rInteractionHandler != null)
		{
			if (aParamInteractionHandlers == null)
			{
				aParamInteractionHandlers = new HashMap<>();
			}

			aParamInteractionHandlers.put(rParam, rInteractionHandler);
		}
		else if (aParamInteractionHandlers != null)
		{
			aParamInteractionHandlers.remove(rParam);
		}
	}

	/***************************************
	 * Cancels the current execution of all registered fragments.
	 *
	 * @see ProcessStep#abort()
	 */
	@Override
	protected void abort() throws Exception
	{
		for (InteractionFragment rFragment : getSubFragments())
		{
			rFragment.abortFragment();
		}
	}

	/***************************************
	 * Returns TRUE if all registered fragments support a rollback.
	 *
	 * @see ProcessStep#canRollback()
	 */
	@Override
	protected boolean canRollback()
	{
		for (InteractionFragment rFragment : getSubFragments())
		{
			if (!rFragment.canRollback())
			{
				return false;
			}
		}

		return true;
	}

	/***************************************
	 * @see ProcessStep#cleanup()
	 */
	@Override
	protected void cleanup()
	{
		for (InteractionFragment rFragment : getSubFragments())
		{
			rFragment.cleanup();
		}
	}

	/***************************************
	 * Empty implementation to make this class non-abstract so that it can be
	 * used for simple data-collecting interactions. Subclasses may implement
	 * this method.
	 *
	 * @see ProcessStep#execute()
	 */
	@Override
	protected void execute() throws Exception
	{
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void executeCleanupActions()
	{
		super.executeCleanupActions();

		for (InteractionFragment rFragment : getSubFragments())
		{
			rFragment.executeCleanupActions();
		}
	}

	/***************************************
	 * Returns the fragments of this interaction. The returned collection should
	 * not be modified by the invoking code.
	 *
	 * @return The collection of fragments
	 */
	protected Collection<InteractionFragment> getFragments()
	{
		return getSubFragments();
	}

	/***************************************
	 * Handles any interactions that may occur in a sub-fragment. This method
	 * should be invoked from the {@link #execute()} method of a subclass.
	 *
	 * @param  rInteractionParam The interaction parameter
	 *
	 * @throws Exception If an error occurs
	 */
	protected void handleFragmentInteractions(RelationType<?> rInteractionParam)
		throws Exception
	{
		Iterator<InteractionFragment> rIterator = getSubFragments().iterator();
		InteractionFragment			  rFragment = null;

		InteractionHandler rInteractionHandler =
			getParameterInteractionHandler(rInteractionParam);

		if (rInteractionHandler != null)
		{
			InteractionEventType eEventType =
				getParameter(INTERACTION_EVENT_TYPE);

			InteractionEvent aEvent =
				new InteractionEvent(this, rInteractionParam, eEventType);

			rInteractionHandler.handleInteraction(aEvent);
		}
		else
		{
			while (rFragment == null && rIterator.hasNext())
			{
				rFragment = rIterator.next();

				if (rFragment.hasFragmentInteraction(rInteractionParam))
				{
					rFragment.handleFragmentInteraction(rInteractionParam);
				}
				else
				{
					rFragment = null;
				}
			}
		}
	}

	/***************************************
	 * Initializes all sub-fragments.
	 *
	 * @throws Exception If an error occurs
	 */
	protected void initFragments() throws Exception
	{
		for (InteractionFragment rFragment : getSubFragments())
		{
			rFragment.initFragment();
		}
	}

	/***************************************
	 * Overridden to invoke {@link #handleFragmentInteractions(RelationType)} or
	 * {@link InteractionFragment#finishFragment()} before super.
	 *
	 * @see RollbackStep#internalExecute()
	 */
	@Override
	protected void internalExecute() throws Exception
	{
		RelationType<?> rInteractionParam = getInteractiveInputParameter();

		if (rInteractionParam != null)
		{
			handleFragmentInteractions(rInteractionParam);
		}

		if (rInteractionParam == null ||
			get(CONTINUATION_PARAMS).contains(rInteractionParam))
		{
			for (InteractionFragment rFragment : getSubFragments())
			{
				rFragment.finishFragment();
			}
		}

		super.internalExecute();

		if (rInteractionParam != null)
		{
			for (InteractionFragment rFragment : getSubFragments())
			{
				rFragment.afterFragmentInteraction(rInteractionParam);
			}
		}
	}

	/***************************************
	 * Checks which fragment of this instance has caused a continuation.
	 *
	 * @see RollbackStep#prepareContinuation()
	 */
	@Override
	protected void prepareContinuation() throws Exception
	{
		RelationType<?> rContinuationParam = getParameter(CONTINUATION_PARAM);

		for (InteractionFragment rFragment : getSubFragments())
		{
			InteractionFragment rContinuationFragment =
				rFragment.getContinuationFragment(rContinuationParam);

			if (rContinuationFragment != null)
			{
				setParameter(CONTINUATION_FRAGMENT_CLASS,
							 rContinuationFragment.getClass());

				break;
			}
		}
	}

	/***************************************
	 * Overridden to invoke {@link #initFragments()} after super.
	 *
	 * @see RollbackStep#prepareExecution()
	 */
	@Override
	protected void prepareExecution() throws Exception
	{
		super.prepareExecution();

		initFragments();
		prepareFragmentInteractions();
	}

	/***************************************
	 * Prepares the interactions of all sub-fragments.
	 *
	 * @throws Exception If an error occurs
	 */
	protected void prepareFragmentInteractions() throws Exception
	{
		setParameter(CONTINUATION_FRAGMENT_CLASS, null);

		for (InteractionFragment rFragment : getSubFragments())
		{
			rFragment.prepareFragmentInteraction();
		}
	}

	/***************************************
	 * Overridden to invoke {@link #prepareFragmentInteractions()} after super.
	 *
	 * @see RollbackStep#prepareInteraction()
	 */
	@Override
	protected void prepareInteraction() throws Exception
	{
		super.prepareInteraction();

		prepareFragmentInteractions();
	}

	/***************************************
	 * Removes all sub-fragments.
	 */
	protected void removeAllFragments()
	{
		for (InteractionFragment rFragment : getSubFragments())
		{
			removeInteractionParameters(rFragment.getInteractionParameters());
			rFragment.setProcessStep(null);
		}

		getSubFragments().clear();
	}

	/***************************************
	 * Removes a certain sub-fragment.
	 *
	 * @param rFragment The fragment to remove
	 */
	protected void removeFragment(InteractionFragment rFragment)
	{
		removeInteractionParameters(rFragment.getInteractionParameters());
		getSubFragments().remove(rFragment);
		rFragment.setProcessStep(null);
	}

	/***************************************
	 * @see RollbackStep#rollback()
	 */
	@Override
	protected void rollback() throws Exception
	{
		for (InteractionFragment rFragment : getSubFragments())
		{
			rFragment.rollbackFragment();
		}
	}

	/***************************************
	 * Initializes this step to be interactive by setting a flag relation of the
	 * type {@link MetaTypes#INTERACTIVE}.
	 *
	 * @see RollbackStep#setup()
	 */
	@Override
	protected void setup() throws ProcessException
	{
		super.setup();

		set(INTERACTIVE);
	}

	/***************************************
	 * Overridden to also validate the parameters of sub-fragments.
	 *
	 * @see RollbackStep#validateParameters(boolean)
	 */
	@Override
	protected Map<RelationType<?>, String> validateParameters(
		boolean bOnInteraction)
	{
		Map<RelationType<?>, String> rValidationErrors =
			super.validateParameters(bOnInteraction);

		for (InteractionFragment rFragment : getSubFragments())
		{
			rValidationErrors.putAll(rFragment.validateFragmentParameters(bOnInteraction));
		}

		return rValidationErrors;
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * An interface that defines a method for the handling of an interaction.
	 *
	 * @author eso
	 */
	public interface InteractionHandler
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Will be invoked to handle the interaction for the annotated
		 * parameter.
		 *
		 * @param  rEvent bActionEvent TRUE if an action event occurred, FALSE
		 *                for a continuous interaction event
		 *
		 * @throws Exception Any kind of exception can be thrown if the handling
		 *                   fails
		 */
		public void handleInteraction(InteractionEvent rEvent) throws Exception;
	}
}
