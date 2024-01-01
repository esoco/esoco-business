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
import org.obrel.core.RelationType;
import org.obrel.type.MetaTypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static de.esoco.process.ProcessRelationTypes.CONTINUATION_FRAGMENT_CLASS;
import static de.esoco.process.ProcessRelationTypes.CONTINUATION_PARAM;
import static de.esoco.process.ProcessRelationTypes.CONTINUATION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_EVENT_TYPE;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAMS;
import static org.obrel.type.MetaTypes.INTERACTIVE;

/**
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
public class Interaction extends RollbackStep {

	private static final long serialVersionUID = 1L;

	private Map<RelationType<?>, InteractionHandler> paramInteractionHandlers;

	/**
	 * Returns the currently registered interaction handler for a certain
	 * parameter.
	 *
	 * @param param The parameter to return the interaction handler for
	 * @return The registered interaction handler or NULL for none
	 */
	public InteractionHandler getParameterInteractionHandler(
		RelationType<?> param) {
		InteractionHandler interactionHandler = null;

		if (paramInteractionHandlers != null) {
			interactionHandler = paramInteractionHandlers.get(param);
		}

		return interactionHandler;
	}

	/**
	 * Inserts a display parameter before another parameter in the list of this
	 * step's interaction parameters. If the parameter to insert before doesn't
	 * exist the new parameter will be added to the end of the list.
	 *
	 * @param param       The parameter type to insert
	 * @param beforeParam The parameter type to insert the new type before
	 */
	public void insertDisplayParameter(RelationType<?> param,
		RelationType<?> beforeParam) {
		CollectionUtil.insert(get(INTERACTION_PARAMS), beforeParam, param);

		prepareNewInteractionParameters(CollectionUtil.setOf(param));
	}

	/**
	 * Inserts an input parameter before another parameter in the list of this
	 * step's interaction and input parameters. If the parameter to insert
	 * before doesn't exist the new parameter will be added to the end of the
	 * list.
	 *
	 * @param param       The parameter type to insert
	 * @param beforeParam The parameter type to insert the new type before
	 */
	public void insertInputParameter(RelationType<?> param,
		RelationType<?> beforeParam) {
		insertDisplayParameter(param, beforeParam);
		get(INPUT_PARAMS).add(param);
	}

	/**
	 * Sets the interaction handler for a certain parameter. This will replace
	 * any previous interaction handler for this parameter.
	 *
	 * @param param              The parameter to add the handler for
	 * @param interactionHandler The interaction handler or NULL to remove the
	 *                           current handler
	 */
	public void setParameterInteractionHandler(RelationType<?> param,
		InteractionHandler interactionHandler) {
		if (interactionHandler != null) {
			if (paramInteractionHandlers == null) {
				paramInteractionHandlers = new HashMap<>();
			}

			paramInteractionHandlers.put(param, interactionHandler);
		} else if (paramInteractionHandlers != null) {
			paramInteractionHandlers.remove(param);
		}
	}

	/**
	 * Cancels the current execution of all registered fragments.
	 *
	 * @see ProcessStep#abort()
	 */
	@Override
	protected void abort() throws Exception {
		for (InteractionFragment fragment : getSubFragments()) {
			fragment.abortFragment();
		}
	}

	/**
	 * Returns TRUE if all registered fragments support a rollback.
	 *
	 * @see ProcessStep#canRollback()
	 */
	@Override
	protected boolean canRollback() {
		for (InteractionFragment fragment : getSubFragments()) {
			if (!fragment.canRollback()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @see ProcessStep#cleanup()
	 */
	@Override
	protected void cleanup() {
		for (InteractionFragment fragment : getSubFragments()) {
			fragment.cleanup();
		}
	}

	/**
	 * Empty implementation to make this class non-abstract so that it can be
	 * used for simple data-collecting interactions. Subclasses may implement
	 * this method.
	 *
	 * @see ProcessStep#execute()
	 */
	@Override
	protected void execute() throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void executeCleanupActions() {
		super.executeCleanupActions();

		for (InteractionFragment fragment : getSubFragments()) {
			fragment.executeCleanupActions();
		}
	}

	/**
	 * Returns the fragments of this interaction. The returned collection
	 * should
	 * not be modified by the invoking code.
	 *
	 * @return The collection of fragments
	 */
	protected Collection<InteractionFragment> getFragments() {
		return getSubFragments();
	}

	/**
	 * Handles any interactions that may occur in a sub-fragment. This method
	 * should be invoked from the {@link #execute()} method of a subclass.
	 *
	 * @param interactionParam The interaction parameter
	 * @throws Exception If an error occurs
	 */
	protected void handleFragmentInteractions(RelationType<?> interactionParam)
		throws Exception {
		Iterator<InteractionFragment> iterator = getSubFragments().iterator();
		InteractionFragment fragment = null;

		InteractionHandler interactionHandler =
			getParameterInteractionHandler(interactionParam);

		if (interactionHandler != null) {
			InteractionEventType eventType =
				getParameter(INTERACTION_EVENT_TYPE);

			InteractionEvent event =
				new InteractionEvent(this, interactionParam, eventType);

			interactionHandler.handleInteraction(event);
		} else {
			while (fragment == null && iterator.hasNext()) {
				fragment = iterator.next();

				if (fragment.hasFragmentInteraction(interactionParam)) {
					fragment.handleFragmentInteraction(interactionParam);
				} else {
					fragment = null;
				}
			}
		}
	}

	/**
	 * Initializes all sub-fragments.
	 *
	 * @throws Exception If an error occurs
	 */
	protected void initFragments() throws Exception {
		for (InteractionFragment fragment : getSubFragments()) {
			fragment.initFragment();
		}
	}

	/**
	 * Overridden to invoke
	 * {@link #handleFragmentInteractions(RelationType)} or
	 * {@link InteractionFragment#finishFragment()} before super.
	 *
	 * @see RollbackStep#internalExecute()
	 */
	@Override
	protected void internalExecute() throws Exception {
		RelationType<?> interactionParam = getInteractiveInputParameter();

		if (interactionParam != null) {
			handleFragmentInteractions(interactionParam);
		}

		if (interactionParam == null ||
			get(CONTINUATION_PARAMS).contains(interactionParam)) {
			for (InteractionFragment fragment : getSubFragments()) {
				fragment.finishFragment();
			}
		}

		super.internalExecute();

		if (interactionParam != null) {
			for (InteractionFragment fragment : getSubFragments()) {
				fragment.afterFragmentInteraction(interactionParam);
			}
		}
	}

	/**
	 * Checks which fragment of this instance has caused a continuation.
	 *
	 * @see RollbackStep#prepareContinuation()
	 */
	@Override
	protected void prepareContinuation() throws Exception {
		RelationType<?> continuationParam = getParameter(CONTINUATION_PARAM);

		for (InteractionFragment fragment : getSubFragments()) {
			InteractionFragment continuationFragment =
				fragment.getContinuationFragment(continuationParam);

			if (continuationFragment != null) {
				setParameter(CONTINUATION_FRAGMENT_CLASS,
					continuationFragment.getClass());

				break;
			}
		}
	}

	/**
	 * Overridden to invoke {@link #initFragments()} after super.
	 *
	 * @see RollbackStep#prepareExecution()
	 */
	@Override
	protected void prepareExecution() throws Exception {
		super.prepareExecution();

		initFragments();
		prepareFragmentInteractions();
	}

	/**
	 * Prepares the interactions of all sub-fragments.
	 *
	 * @throws Exception If an error occurs
	 */
	protected void prepareFragmentInteractions() throws Exception {
		setParameter(CONTINUATION_FRAGMENT_CLASS, null);

		for (InteractionFragment fragment : getSubFragments()) {
			fragment.prepareFragmentInteraction();
		}
	}

	/**
	 * Overridden to invoke {@link #prepareFragmentInteractions()} after super.
	 *
	 * @see RollbackStep#prepareInteraction()
	 */
	@Override
	protected void prepareInteraction() throws Exception {
		super.prepareInteraction();

		prepareFragmentInteractions();
	}

	/**
	 * Removes all sub-fragments.
	 */
	protected void removeAllFragments() {
		for (InteractionFragment fragment : getSubFragments()) {
			removeInteractionParameters(fragment.getInteractionParameters());
			fragment.setProcessStep(null);
		}

		getSubFragments().clear();
	}

	/**
	 * Removes a certain sub-fragment.
	 *
	 * @param fragment The fragment to remove
	 */
	protected void removeFragment(InteractionFragment fragment) {
		removeInteractionParameters(fragment.getInteractionParameters());
		getSubFragments().remove(fragment);
		fragment.setProcessStep(null);
	}

	/**
	 * @see RollbackStep#rollback()
	 */
	@Override
	protected void rollback() throws Exception {
		for (InteractionFragment fragment : getSubFragments()) {
			fragment.rollbackFragment();
		}
	}

	/**
	 * Initializes this step to be interactive by setting a flag relation of
	 * the
	 * type {@link MetaTypes#INTERACTIVE}.
	 *
	 * @see RollbackStep#setup()
	 */
	@Override
	protected void setup() throws ProcessException {
		super.setup();

		set(INTERACTIVE);
	}

	/**
	 * Overridden to also validate the parameters of sub-fragments.
	 *
	 * @see RollbackStep#validateParameters(boolean)
	 */
	@Override
	protected Map<RelationType<?>, String> validateParameters(
		boolean onInteraction) {
		Map<RelationType<?>, String> validationErrors =
			super.validateParameters(onInteraction);

		for (InteractionFragment fragment : getSubFragments()) {
			validationErrors.putAll(
				fragment.validateFragmentParameters(onInteraction));
		}

		return validationErrors;
	}

	/**
	 * An interface that defines a method for the handling of an interaction.
	 *
	 * @author eso
	 */
	public interface InteractionHandler {

		/**
		 * Will be invoked to handle the interaction for the annotated
		 * parameter.
		 *
		 * @param event actionEvent TRUE if an action event occurred, FALSE for
		 *              a continuous interaction event
		 * @throws Exception Any kind of exception can be thrown if the
		 * handling
		 *                   fails
		 */
		void handleInteraction(InteractionEvent event) throws Exception;
	}
}
