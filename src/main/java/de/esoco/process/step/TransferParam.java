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

import de.esoco.process.ProcessException;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ProcessStep;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newBooleanType;

/**
 * A process step implementation that can be used to copy or move a process
 * parameter. Because the parameter types must be related it is recommended to
 * create instances of this step only through generic methods that ensure the
 * use of related types for both parameters.
 *
 * <p>Supported step parameters are (r = required, o = optional):</p>
 *
 * <ul>
 *   <li>TRANSFER_PARAM_SOURCE (r): The ID of the source process parameter to be
 *     transferred</li>
 *   <li>TRANSFER_PARAM_TARGET (r): The ID of the target process parameter to be
 *     set</li>
 *   <li>TRANSFER_PARAM_MOVE (o): a boolean value that indicates whether the
 *     parameter shall be moved (TRUE) or copied (FALSE, default)</li>
 * </ul>
 *
 * @author eso
 */
public class TransferParam extends ProcessStep {

	/**
	 * The transfer mode
	 */
	public static final RelationType<Boolean> TRANSFER_PARAM_MOVE =
		newBooleanType("de.esoco.process.step.TRANSFER_PARAM_MOVE");

	private static final long serialVersionUID = 1L;

	static {
		RelationTypes.init(TransferParam.class);
	}

	/**
	 * Creates a new instance.
	 */
	public TransferParam() {
	}

	/**
	 * @see ProcessStep#canRollback()
	 */
	@Override
	protected boolean canRollback() {
		return true;
	}

	/**
	 * Executes the parameter transfer.
	 *
	 * @throws ProcessException When a required parameter is missing or if the
	 *                          target datatypes of the source and target
	 *                          parameters are incompatible
	 */
	@Override
	@SuppressWarnings({ "boxing", "unchecked" })
	protected void execute() throws ProcessException {
		RelationType<?> rSource =
			checkParameter(ProcessRelationTypes.SOURCE_PARAM);
		RelationType<?> rTarget =
			checkParameter(ProcessRelationTypes.TARGET_PARAM);

		Object rValue = getParameter(rSource);
		boolean bMove = getParameter(TRANSFER_PARAM_MOVE);

		if (!rTarget
			.getTargetType()
			.isAssignableFrom(rSource.getTargetType())) {
			throw new ProcessException(this,
				"Incompatible source and target parameter datatypes");
		}

		setParameter((RelationType<Object>) rTarget, rValue);

		if (bMove) {
			deleteParameters(rSource);
		}
	}

	/**
	 * Reverses a previous move and removes all set parameters.
	 *
	 * @see ProcessStep#rollback()
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	protected void rollback() throws Exception {
		if (hasFlagParameter(TRANSFER_PARAM_MOVE)) {
			RelationType<?> rSource =
				getParameter(ProcessRelationTypes.SOURCE_PARAM);
			RelationType<?> rTarget =
				getParameter(ProcessRelationTypes.TARGET_PARAM);

			setParameter((RelationType<Object>) rSource,
				getParameter(rTarget));
			deleteParameters(rTarget);
		}
	}
}
