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

import de.esoco.process.ProcessFragment;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static de.esoco.lib.property.ContentProperties.TOOLTIP;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;

import static de.esoco.process.ProcessRelationTypes.AUTO_CONTINUE;
import static de.esoco.process.ProcessRelationTypes.PROGRESS;
import static de.esoco.process.ProcessRelationTypes.PROGRESS_DESCRIPTION;
import static de.esoco.process.ProcessRelationTypes.PROGRESS_INDICATOR;
import static de.esoco.process.ProcessRelationTypes.PROGRESS_MAXIMUM;

/**
 * An interactive and automatically continuing step that displays the progress
 * of an iterating process.
 *
 * @author eso
 */
public class DisplayProgress extends Interaction {

	/**
	 * An interaction enum to skip the query.
	 */
	public enum SkipAction {SKIP}

	/**
	 * Flag to enable or disable the skip button.
	 */
	public static final RelationType<Boolean> SHOW_SKIP_BUTTON =
		RelationTypes.newFlagType();

	/**
	 * Flag to enable or disable the skip button.
	 */
	public static final RelationType<Boolean> SKIP_PROCESSING =
		RelationTypes.newFlagType();

	/**
	 * The interactive skip action parameter.
	 */
	public static final RelationType<SkipAction> SKIP_ACTION =
		RelationTypes.newType();

	private static final long serialVersionUID = 1L;

	static {
		RelationTypes.init(DisplayProgress.class);
	}

	/**
	 * Creates a new instance.
	 */
	public DisplayProgress() {
		continueOnInteraction(SKIP_ACTION);
		addDisplayParameters(PROGRESS, PROGRESS_INDICATOR,
			PROGRESS_DESCRIPTION);
		set(AUTO_CONTINUE);
	}

	/**
	 * Resets the progress display parameters.
	 *
	 * @param rProcessStep The step for which to reset the parameters
	 */
	public static void resetProgress(ProcessFragment rProcessStep) {
		rProcessStep.deleteParameters(PROGRESS, PROGRESS_INDICATOR,
			SKIP_PROCESSING);
	}

	/**
	 * @see Interaction#execute()
	 */
	@Override
	@SuppressWarnings("boxing")
	protected void execute() throws Exception {
		if (getInteractiveInputParameter() == SKIP_ACTION) {
			setParameter(SKIP_PROCESSING, true);
		}

		// if this was the last invocation reset all progress parameter for
		// the case of an re-execution
		if (getParameter(PROGRESS) >= getParameter(PROGRESS_MAXIMUM)) {
			resetProgress(this);
		}
	}

	/**
	 * @see Interaction#prepareParameters()
	 */
	@Override
	protected void prepareParameters() throws Exception {
		if (hasFlagParameter(SHOW_SKIP_BUTTON)) {
			if (!hasParameter(SKIP_ACTION)) {
				addInputParameters(SKIP_ACTION);
				setImmediateAction(SKIP_ACTION);
				setUIFlag(HIDE_LABEL, SKIP_ACTION);
				setUIProperty(TOOLTIP, null, SKIP_ACTION);
			}
		} else {
			removeInteractionParameters(SKIP_ACTION);
		}

		initProgressParameter();
	}

	/**
	 * @see Interaction#prepareValues()
	 */
	@Override
	protected void prepareValues() {
		markParameterAsModified(PROGRESS_DESCRIPTION);
		setProgressIndicator();
	}

	/**
	 * @see Interaction#rollback()
	 */
	@Override
	protected void rollback() {
		resetProgress(this);
	}
}
