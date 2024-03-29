//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;

import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static org.obrel.core.RelationTypes.newInitialValueType;
import static org.obrel.core.RelationTypes.newIntType;

/**
 * Test process step implementation.
 *
 * @author eso
 */
public class TestStep extends ProcessStep {

	/**
	 * Test parameter ID
	 */
	public static final RelationType<Integer> TEST_INT_PARAM = newIntType();

	/**
	 * Interactive test interruptions
	 */
	public static final RelationType<Integer> TEST_INTERACTION_COUNT =
		newIntType();

	/**
	 * Test result parameter ID
	 */
	public static final RelationType<String> TEST_STRING_RESULT =
		newInitialValueType("");

	private static final long serialVersionUID = 1L;

	static {
		RelationTypes.init(TestStep.class);
	}

	/**
	 * Creates a new instance.
	 */
	public TestStep() {
		setMandatory(TEST_INT_PARAM);
	}

	/**
	 * @see de.esoco.process.ProcessStep#canRollback()
	 */
	@Override
	protected boolean canRollback() {
		return true;
	}

	/**
	 * Test execution. Test
	 */
	@Override
	@SuppressWarnings("boxing")
	protected void execute() {
		int value = checkParameter(TEST_INT_PARAM);
		String result = checkParameter(TEST_STRING_RESULT);

		result += getName().charAt(getName().length() - 1);

		setParameter(TEST_INT_PARAM, value + 1);
		setParameter(TEST_STRING_RESULT, result);

		if (hasFlag(MetaTypes.INTERACTIVE)) {
			setParameter(TEST_INTERACTION_COUNT,
				checkParameter(TEST_INTERACTION_COUNT) + 1);
		}
	}

	/**
	 * Returns a list containing {@link #TEST_INT_PARAM} if this instance has
	 * the flag relation {@link MetaTypes#INTERACTIVE} is set. Else returns
	 * NULL.
	 *
	 * @see ProcessStep#prepareStep()
	 */
	@Override
	protected boolean prepareStep() throws Exception {
		boolean interactive = hasFlag(MetaTypes.INTERACTIVE);

		if (interactive && !hasRelation(INPUT_PARAMS)) {
			addInputParameters(TEST_INT_PARAM, TEST_STRING_RESULT);
		}

		return !interactive;
	}

	/**
	 * @see ProcessStep#rollback()
	 */
	@Override
	@SuppressWarnings("boxing")
	protected void rollback() throws Exception {
		int v = checkParameter(TEST_INT_PARAM);
		String result = checkParameter(TEST_STRING_RESULT);

		result = result.substring(0, result.length() - 1);

		setParameter(TEST_INT_PARAM, v - 1);
		setParameter(TEST_STRING_RESULT, result);
	}
}
