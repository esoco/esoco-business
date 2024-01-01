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

import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.process.Process;
import de.esoco.process.ProcessException;
import de.esoco.process.ProcessStep;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newRelationType;
import static org.obrel.core.RelationTypes.newStringType;

/**
 * A ProcessStep subclass that can branch during the process execution depending
 * on the value of a certain process parameter. The following parameters can be
 * set in the step configuration:
 *
 * <ul>
 *   <li>{@link #BRANCH_PARAM}: Contains the relation type of the process
 *     parameter that will be checked for the branch condition.</li>
 *   <li>{@link #BRANCH_CONDITION}: A predicate to evaluate the branch parameter
 *     with. The input value of the predicate must be compatible with the value
 *     of the branch parameter or else an exception will occur during the
 *     process execution.</li>
 *   <li>{@link #BRANCH_TARGET}: The name of the target step to be executed if
 *     the branch condition is true. If the process should end in that case the
 *     value must be {@link Process#PROCESS_END}.</li>
 * </ul>
 *
 * @author eso
 */
public final class BranchStep extends ProcessStep {

	/**
	 * The process parameter to check for the branch condition
	 */
	public static final RelationType<RelationType<?>> BRANCH_PARAM =
		newRelationType("de.esoco.process.BRANCH_PARAM", RelationType.class);

	/**
	 * The predicate to evaluate the branch parameter with
	 */
	public static final RelationType<Predicate<?>> BRANCH_CONDITION =
		newRelationType("de.esoco.process.BRANCH_CONDITION", Predicate.class);

	/**
	 * The target process step if the branch condition is true
	 */
	public static final RelationType<String> BRANCH_TARGET =
		newStringType("de.esoco.process.BRANCH_TARGET");

	private static final long serialVersionUID = 1L;

	static {
		RelationTypes.init(BranchStep.class);
	}

	private String noBranchTarget;

	/**
	 * Initializes the step parameters.
	 */
	public BranchStep() {
		setMandatory(BRANCH_CONDITION);
	}

	/**
	 * @see ProcessStep#canRollback()
	 */
	@Override
	protected boolean canRollback() {
		return true;
	}

	/**
	 * Checks the defined branching condition and if the condition is
	 * fulfilled,
	 * modifies the superclass' reference to the next step accordingly.
	 *
	 * @throws ProcessException If the step configuration is invalid
	 */
	@Override
	@SuppressWarnings("boxing")
	protected void execute() throws ProcessException {
		@SuppressWarnings("unchecked")
		Predicate<Object> condition =
			(Predicate<Object>) checkParameter(BRANCH_CONDITION);

		RelationType<?> branchParam = getParameter(BRANCH_PARAM);
		String branchTarget = getParameter(BRANCH_TARGET);
		String nextStep;

		if (condition != Predicates.alwaysTrue() && branchParam == null &&
			!hasRelation(BRANCH_PARAM)) {
			throw new ProcessException(this,
				String.format("Parameter %s not set", BRANCH_PARAM));
		}

		if (!hasRelation(BRANCH_TARGET)) {
			throw new ProcessException(this,
				String.format("Parameter %s not set", BRANCH_TARGET));
		}

		Object branchValue =
			branchParam != null ? getParameter(branchParam) : null;

		if (condition.evaluate(branchValue)) {
			nextStep = branchTarget;
		} else {
			nextStep = noBranchTarget;
		}

		super.setNextStep(nextStep);
	}

	/**
	 * Overridden to do nothing to prevent the superclass exception.
	 *
	 * @see ProcessStep#rollback()
	 */
	@Override
	protected void rollback() throws Exception {
	}

	/**
	 * Overridden to store the name of the step to be invoked if no branching
	 * occurs.
	 *
	 * @param nextStep The next step to be invoked if no branching occurs
	 */
	@Override
	protected void setNextStep(String nextStep) {
		super.setNextStep(nextStep);

		noBranchTarget = nextStep;
	}
}
