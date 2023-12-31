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
package de.esoco.process;

import de.esoco.lib.expression.Predicate;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.obrel.core.RelationTypes.newMapType;
import static org.obrel.core.RelationTypes.newType;

/**
 * A base class for the test of processes and process steps.
 *
 * @author eso
 */
public class AbstractProcessTest {

	/**
	 * Postconditions for the automatic testing of processes
	 */
	public static final RelationType<Map<RelationType<?>, Predicate<?>>>
		POSTCONDITIONS = newMapType(true);

	/**
	 * Stores the current test process; can be used to apply postcondition
	 * predicates to the current process
	 */
	protected static final RelationType<Process> TEST_PROCESS = newType();

	static {
		RelationTypes.init(AbstractProcessTest.class);
	}

	/**
	 * Assets that the predicates stored in the relation
	 * {@link #POSTCONDITIONS}
	 * of the given process (or the current process step if the process has not
	 * finished yet) yield TRUE.
	 *
	 * @param aProcess The process to assert
	 */
	@SuppressWarnings("boxing")
	public static void assertPostconditions(Process aProcess) {
		Set<Entry<RelationType<?>, Predicate<?>>> rConditions;

		if (aProcess.isFinished()) {
			rConditions = aProcess.get(POSTCONDITIONS).entrySet();
		} else {
			rConditions =
				aProcess.getCurrentStep().get(POSTCONDITIONS).entrySet();
		}

		for (Map.Entry<RelationType<?>, Predicate<?>> e : rConditions) {
			RelationType<?> rType = e.getKey();

			@SuppressWarnings("unchecked")
			Predicate<Object> rPredicate = (Predicate<Object>) e.getValue();

			if (rPredicate != null) {
				Object rValue = aProcess.getParameter(rType);

				assertTrue(rPredicate.evaluate(rValue),
					"Expected: " + rType + " " + rPredicate + " but is " +
						rValue);
			} else {
				assertFalse(aProcess.hasParameter(rType), rType + " exists");
			}
		}
	}

	/**
	 * A method for subclasses to create a process from a process definition,
	 * execute it, and assert a set of postconditions. The postconditions must
	 * be set as a map relation of type {@link #POSTCONDITIONS} in the given
	 * process definition. The map keys are the relation types to be asserted
	 * and the map values are predicates that check the relation value by
	 * evaluating it and returning TRUE if the postcondition is valid and FALSE
	 * if not. If the map value is NULL it will be asserted that NO relation
	 * with the given type exists. If the relation type is
	 * {@link #TEST_PROCESS}
	 * the predicate will we applied to the current process instead of one of
	 * it's parameters.
	 *
	 * <p>This method can also be used to test interactive processes. The
	 * postconditions to be checked after the execution stopped at a particular
	 * process step must be set on the definition of the respective step. The
	 * postconditions that are set on the process definition will always be
	 * checked after the process has finished execution completely.</p>
	 *
	 * @param rProcessDefinition The definition of the process to execute
	 * @return The finished process
	 * @throws ProcessException If executing the process fails
	 */
	protected Process executeProcess(ProcessDefinition rProcessDefinition)
		throws ProcessException {
		Process aProcess = rProcessDefinition.createProcess();

		// add TEST_PROCESS as self-reference so that predicates can be applied
		// to the process itself
		aProcess.setParameter(TEST_PROCESS, aProcess);

		do {
			aProcess.execute();

			assertPostconditions(aProcess);
		} while (!aProcess.isFinished());

		return aProcess;
	}
}
