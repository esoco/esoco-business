//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.expression.Function;

import de.esoco.process.ProcessStep;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newRelationType;

/**
 * A ProcessStep subclass that can switch between different process execution
 * paths based on the value of a certain process parameter. If the value of the
 * process parameter is NULL (either because it is not set or explicitly set to
 * NULL) this step will fall through to the successive step. Otherwise if will
 * invoke the switch function on the value and set the next step to the
 * resulting value. The following parameters must be set in the step
 * configuration:
 *
 * <ul>
 *   <li>{@link #SWITCH_PARAM}: Contains the relation type of the process
 *     parameter that will be checked by the target selector function.</li>
 *   <li>{@link #SWITCH_TARGET_SELECTOR}: A function that evaluates the switch
 *     parameter and returns the associated target process step at which the
 *     process shall continue. The input value of the function must be
 *     compatible with the value of the switch parameter or else an exception
 *     will occur during the process execution.</li>
 * </ul>
 *
 * @author eso
 */
public final class SwitchStep extends ProcessStep {

	/**
	 * The process parameter that contains the value to switch upon
	 */
	public static final RelationType<RelationType<?>> SWITCH_PARAM =
		newRelationType("de.esoco.process.SWITCH_PARAM", RelationType.class);

	/**
	 * A function that selects the switch target for the switch parameter. The
	 * function result can either be a process step class or an arbitrary
	 * object
	 * with a toString() method that produces the name of a process step.
	 */
	public static final RelationType<Function<?, Object>>
		SWITCH_TARGET_SELECTOR =
		newRelationType("de.esoco.process.SWITCH_TARGET_SELECTOR",
			Function.class);

	private static final long serialVersionUID = 1L;

	static {
		RelationTypes.init(SwitchStep.class);
	}

	private String sDefaultNextStep = null;

	/**
	 * Initializes the step parameters.
	 */
	public SwitchStep() {
		setMandatory(SWITCH_PARAM, SWITCH_TARGET_SELECTOR);
	}

	/**
	 * @see ProcessStep#canRollback()
	 */
	@Override
	protected boolean canRollback() {
		return true;
	}

	/**
	 * Checks the defined switch parameter and sets the process to continue at
	 * the associated process step. This is done by modifying the name of the
	 * next process step through {@link #setNextStep(String)}.
	 */
	@Override
	protected void execute() {
		@SuppressWarnings("unchecked")
		Function<Object, Object> fTargetSelector =
			(Function<Object, Object>) checkParameter(SWITCH_TARGET_SELECTOR);

		Object rSwitchValue = getParameter(checkParameter(SWITCH_PARAM));

		if (rSwitchValue != null) {
			Object rNextStep = fTargetSelector.evaluate(rSwitchValue);
			String sSwitchTargetStep = null;

			if (sDefaultNextStep == null) {
				sDefaultNextStep = getNextStep();
			}

			if (rNextStep instanceof Class) {
				sSwitchTargetStep = ((Class<?>) rNextStep).getSimpleName();
			} else if (rNextStep != null) {
				sSwitchTargetStep = rNextStep.toString();
			}

			setNextStep(sSwitchTargetStep);
		} else if (sDefaultNextStep != null) {
			setNextStep(sDefaultNextStep);
		}
	}

	/**
	 * Overridden to do nothing to prevent the superclass exception.
	 *
	 * @see ProcessStep#rollback()
	 */
	@Override
	protected void rollback() throws Exception {
	}
}
