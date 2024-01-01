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

import org.junit.jupiter.api.Test;

import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.process.AbstractProcessTest.POSTCONDITIONS;
import static de.esoco.process.AbstractProcessTest.assertPostconditions;
import static de.esoco.process.TestStep.TEST_INT_PARAM;
import static de.esoco.process.TestStep.TEST_STRING_RESULT;

/**
 * Test of {@link ProcessRunner}
 *
 * @author eso
 */
public class ProcessRunnerTest {

	/**
	 * Test of {@link ProcessRunner#run()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testRun() {
		StepListProcessDefinition def =
			new StepListProcessDefinition("Transfer");

		def.invoke("STEP1", TestStep.class);
		def.invoke("STEP2", TestStep.class);
		def.invoke("STEP3", TestStep.class);

		def.set(TEST_INT_PARAM, 0);

		// assert correct results of copy and move steps
		def.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(3));
		def.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("123"));

		ProcessRunner runner = new ProcessRunner(def);

		runner.executeOnce();

		assertPostconditions(runner.getProcess());
	}
}
