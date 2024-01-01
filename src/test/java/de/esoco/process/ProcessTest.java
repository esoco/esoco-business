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
package de.esoco.process;

import de.esoco.lib.expression.MathFunctions;
import de.esoco.process.Process.ProcessEventType;
import de.esoco.process.StepListProcessDefinition.StepListEntry;
import de.esoco.process.step.FunctionStep;
import de.esoco.process.step.TransferParam;
import org.junit.jupiter.api.Test;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.process.Process.PROCESS_END;
import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.TestStep.TEST_INTERACTION_COUNT;
import static de.esoco.process.TestStep.TEST_INT_PARAM;
import static de.esoco.process.TestStep.TEST_STRING_RESULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.obrel.core.RelationTypes.newIntType;
import static org.obrel.core.RelationTypes.newType;
import static org.obrel.type.MetaTypes.INTERACTIVE;

/**
 * Process test case.
 *
 * @author eso
 */
public class ProcessTest extends AbstractProcessTest {

	private static final RelationType<Integer> TEST_INT_PARAM2 = newIntType();

	private static final RelationType<String> TEST_RESULT2 = newType();

	private static final RelationType<String> TEST_RESULT3 = newType();

	private static final RelationType<Integer> INT_RESULT = newIntType();

	static {
		RelationTypes.init(ProcessTest.class);
	}

	/**
	 * Test of {@link FunctionStep}.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testBinaryFunctionStep() throws ProcessException {
		StepListProcessDefinition def =
			new StepListProcessDefinition("BinaryFunctionStep");

		def.invokeBinaryFunction("STEP1", TEST_INT_PARAM, TEST_INT_PARAM2,
			INT_RESULT, MathFunctions.add(0));

		try {
			executeProcess(def);
			fail();
		} catch (ProcessException e) {
			// correct execution path, input parameter is missing
		}

		def.set(TEST_INT_PARAM, 20);
		def.set(TEST_INT_PARAM2, 22);
		def.get(POSTCONDITIONS).put(INT_RESULT, equalTo(42));

		executeProcess(def);
	}

	/**
	 * Tests execution of a process that contains branch steps.
	 *
	 * @throws Exception On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testBranchStep() throws Exception {
		StepListProcessDefinition def = createBranchProcess();

		// precondition
		def.set(TEST_INT_PARAM, 100);

		def.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(105));

		// assert correct execution sequence of process steps
		def.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("12434"));

		executeProcess(def);
	}

	/**
	 * Test of {@link FunctionStep}.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testFunctionStep() throws ProcessException {
		StepListProcessDefinition def =
			new StepListProcessDefinition("FunctionStep");

		def.invokeFunction("STEP1", TEST_INT_PARAM, INT_RESULT,
			MathFunctions.add(21));

		try {
			executeProcess(def);
			fail();
		} catch (ProcessException e) {
			// correct execution path, input parameter is missing
		}

		def.set(TEST_INT_PARAM, 21);
		def.get(POSTCONDITIONS).put(INT_RESULT, equalTo(42));

		executeProcess(def);
	}

	/**
	 * Tests execution of a process that performs interaction through an
	 * InteractionHandler.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testInteractionHandler() throws ProcessException {
		StepListProcessDefinition def =
			new StepListProcessDefinition("InteractionHandler");

		def.invoke("STEP1", TestStep.class);
		def.invoke("STEP2", TestStep.class).set(INTERACTIVE);
		def.invoke("STEP3", TestStep.class);
		def.invoke("STEP4", TestStep.class);

		StepListEntry lastStep = def.invoke("STEP5", TestStep.class);

		lastStep.set(INTERACTIVE);
		lastStep.addInputParameters(TEST_RESULT2, TEST_RESULT3);

		assertEquals(2, lastStep.get(INPUT_PARAMS).size());

		Process process = def.createProcess();

		process.setParameter(TEST_INT_PARAM, Integer.valueOf(0));
		process.setInteractionHandler(new ProcessInteractionHandler() {
			@Override
			public void performInteraction(ProcessFragment processStep)
				throws Exception {
				int value = processStep.checkParameter(TEST_INT_PARAM);
				String result = processStep.checkParameter(TEST_STRING_RESULT);

				value += 1;
				result += "-";

				processStep.setParameter(TEST_INT_PARAM, value);
				processStep.setParameter(TEST_STRING_RESULT, result);
			}
		});

		process.execute();

		assertEquals(Integer.valueOf(7), process.getParameter(TEST_INT_PARAM));
		assertEquals("1-234-5", process.getParameter(TEST_STRING_RESULT));
	}

	/**
	 * Tests execution of an interactive process.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testInteractiveProcess() throws ProcessException {
		StepListProcessDefinition def =
			new StepListProcessDefinition("Interactive");

		def.invoke("STEP1", TestStep.class);

		StepListEntry secondStep = def.invoke("STEP2", TestStep.class);

		def.invoke("STEP3", TestStep.class);
		def.invoke("STEP4", TestStep.class);

		StepListEntry lastStep = def.invoke("STEP5", TestStep.class);

		def.set(TEST_INT_PARAM, 0);
		secondStep.set(INTERACTIVE);
		secondStep.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(1));
		secondStep.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("1"));
		secondStep.get(POSTCONDITIONS).put(TEST_INTERACTION_COUNT, equalTo(0));
		lastStep.set(INTERACTIVE);
		lastStep.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(4));
		lastStep.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("1234"));
		lastStep.get(POSTCONDITIONS).put(TEST_INTERACTION_COUNT, equalTo(1));

		def.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(5));
		def.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("12345"));
		def.get(POSTCONDITIONS).put(TEST_INTERACTION_COUNT, equalTo(2));

		executeProcess(def);
	}

	/**
	 * Test the invocation of an interactive sub-process.
	 *
	 * @throws Exception On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testInteractiveSubProcess() throws Exception {
		StepListEntry[] subSteps =
			new StepListEntry[] { new StepListEntry("STEP3", TestStep.class),
				new StepListEntry("STEP4", TestStep.class),
				new StepListEntry("STEP5", TestStep.class), };

		subSteps[1].set(INTERACTIVE);

		ProcessDefinition subDef =
			new StepListProcessDefinition("SubProcess", subSteps);

		StepListEntry[] steps =
			new StepListEntry[] { new StepListEntry("STEP1", TestStep.class),
				new StepListEntry("STEP2", TestStep.class),
				new StepListEntry("SUBSTEP", SubProcessStep.class),
				new StepListEntry("STEP6", TestStep.class),
				new StepListEntry("STEP7", TestStep.class), };

		steps[1].set(INTERACTIVE, true);
		steps[2].set(ProcessRelationTypes.SUB_PROCESS_DEFINITION, subDef);
		steps[4].set(INTERACTIVE, true);

		ProcessDefinition def =
			new StepListProcessDefinition("TestInteractiveSubProcess", steps);

		def.set(TEST_INT_PARAM, 0);

		def.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(7));
		def.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("1234567"));
		def.get(POSTCONDITIONS).put(TEST_INTERACTION_COUNT, equalTo(3));

		executeProcess(def);
	}

	/**
	 * Tests the cancellation of an interactive process.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testProcessCancel() throws ProcessException {
		StepListEntry[] steps =
			new StepListEntry[] { new StepListEntry("STEP1", TestStep.class),
				new StepListEntry("STEP2", TestStep.class),
				new StepListEntry("STEP3", TestStep.class), };

		steps[2].set(INTERACTIVE);

		Process process =
			new StepListProcessDefinition("Cancel", steps).createProcess();

		process.setParameter(TEST_INT_PARAM, 0);

		process.execute();
		process.cancel();
		assertTrue(process.isFinished());
		assertEquals(Integer.valueOf(2), process.getParameter(TEST_INT_PARAM));
		assertEquals("12", process.getParameter(TEST_STRING_RESULT));
	}

	/**
	 * Tests the execution of the {@link TransferParam} step.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testProcessListener() throws ProcessException {
		StepListProcessDefinition def =
			new StepListProcessDefinition("ProcessListener");

		final List<ProcessEventType> listenerMethods =
			new ArrayList<ProcessEventType>();

		ProcessListener testListener = new ProcessListener() {
			@Override
			public void processCanceled(Process process) {
				listenerMethods.add(ProcessEventType.CANCELED);
			}

			@Override
			public void processFailed(Process process) {
				listenerMethods.add(ProcessEventType.FAILED);
			}

			@Override
			public void processFinished(Process process) {
				listenerMethods.add(ProcessEventType.FINISHED);
			}

			@Override
			public void processResumed(Process process) {
				listenerMethods.add(ProcessEventType.RESUMED);
			}

			@Override
			public void processStarted(Process process) {
				listenerMethods.add(ProcessEventType.STARTED);
			}

			@Override
			public void processSuspended(Process process) {
				listenerMethods.add(ProcessEventType.SUSPENDED);
			}
		};

		def.invoke("STEP1", TestStep.class);
		def.invoke("STEP2", TestStep.class).set(INTERACTIVE);
		def.invoke("STEP3", TestStep.class);

		Process process = def.createProcess();

		process.setParameter(TEST_INT_PARAM, 0);
		process.get(ProcessRelationTypes.PROCESS_LISTENERS).add(testListener);

		process.execute();
		assertEquals(2, listenerMethods.size());
		assertTrue(listenerMethods.contains(ProcessEventType.STARTED));
		assertTrue(listenerMethods.contains(ProcessEventType.SUSPENDED));
		process.execute();
		assertEquals(4, listenerMethods.size());
		assertTrue(listenerMethods.contains(ProcessEventType.RESUMED));
		assertTrue(listenerMethods.contains(ProcessEventType.FINISHED));

		listenerMethods.clear();

		process = def.createProcess();
		process.setParameter(TEST_INT_PARAM, 0);
		process.get(ProcessRelationTypes.PROCESS_LISTENERS).add(testListener);
		process.execute();
		process.cancel();
		assertEquals(3, listenerMethods.size());
		assertTrue(listenerMethods.contains(ProcessEventType.STARTED));
		assertTrue(listenerMethods.contains(ProcessEventType.SUSPENDED));
		assertTrue(listenerMethods.contains(ProcessEventType.CANCELED));

		listenerMethods.clear();

		process = def.createProcess();

		// not setting TEST_INT_PARAM will cause an exception
		process.get(ProcessRelationTypes.PROCESS_LISTENERS).add(testListener);

		try {
			process.execute();
			fail();
		} catch (Exception e) {
			// correct execution path, input parameter is missing
		}

		assertEquals(2, listenerMethods.size());
		assertTrue(listenerMethods.contains(ProcessEventType.STARTED));
		assertTrue(listenerMethods.contains(ProcessEventType.FAILED));
	}

	/**
	 * Tests the rollback of an interactive process.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testProcessRollbackInteraction() throws ProcessException {
		StepListProcessDefinition def = new StepListProcessDefinition(
			"Cancel");

		def.invoke("STEP1", TestStep.class).set(INTERACTIVE);
		def.invoke("STEP2", TestStep.class);
		def.invoke("STEP3", TestStep.class).set(INTERACTIVE);

		Process process = def.createProcess();

		process.setParameter(TEST_INT_PARAM, 0);

		process.execute(); // to first interactive step
		process.execute(); // to second interactive step
		assertEquals(Integer.valueOf(2), process.getParameter(TEST_INT_PARAM));
		assertEquals("12", process.getParameter(TEST_STRING_RESULT));

		process.rollbackToPreviousInteraction();
		process.execute(); // rollback to and re-run first interactive step
		assertEquals(Integer.valueOf(0), process.getParameter(TEST_INT_PARAM));
		assertEquals("", process.getParameter(TEST_STRING_RESULT));

		process.execute(); // to second interactive step
		assertEquals(Integer.valueOf(2), process.getParameter(TEST_INT_PARAM));
		assertEquals("12", process.getParameter(TEST_STRING_RESULT));

		process.execute(); // finish process
		assertEquals(Integer.valueOf(3), process.getParameter(TEST_INT_PARAM));
		assertEquals("123", process.getParameter(TEST_STRING_RESULT));
	}

	/**
	 * Test the invocation of a sub-process.
	 *
	 * @throws Exception On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testSubProcess() throws Exception {
		StepListProcessDefinition def =
			new StepListProcessDefinition("TestSubProcess");

		def.invokeSubProcess("SUB", createBranchProcess());
		def.copyParam(TEST_STRING_RESULT, TEST_RESULT2);
		def.invoke("STEP!", TestStep.class);

		// precondition
		def.set(TEST_INT_PARAM, 100);
		def.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(106));

		// assert correct execution sequence of process steps
		def.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("12434!"));
		def.get(POSTCONDITIONS).put(TEST_RESULT2, equalTo("12434"));

		executeProcess(def);
	}

	/**
	 * Tests execution of a process that performs branching and parameter
	 * copying.
	 *
	 * @throws Exception On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testSwitchStep() throws Exception {
		StepListProcessDefinition def = new StepListProcessDefinition(
			"Switch");

		Map<Integer, Object> switchMap = new HashMap<Integer, Object>();

		switchMap.put(101, "STEP3");
		switchMap.put(104, "STEP4");
		switchMap.put(106, Process.PROCESS_END);

		def.invoke("STEP1", TestStep.class);
		def.switchOnParam(TEST_INT_PARAM, switchMap);
		def.invoke("STEP2", TestStep.class);
		def.invoke("STEP3", TestStep.class);
		def.invoke("STEP4", TestStep.class);
		def.goTo("STEP1");

		// precondition
		def.set(TEST_INT_PARAM, 100);

		def.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(106));

		// assert correct execution sequence of process steps
		def.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("134141"));

		executeProcess(def);
	}

	/**
	 * Tests the execution of the {@link TransferParam} step.
	 */
	@Test
	public void testTransferStep() throws ProcessException {
		StepListProcessDefinition def =
			new StepListProcessDefinition("Transfer");

		def.invoke("STEP1", TestStep.class);
		def.invoke("STEP2", TestStep.class);
		def.invoke("STEP3", TestStep.class);
		def.copyParam(TEST_STRING_RESULT, TEST_RESULT2);
		def.moveParam(TEST_RESULT2, TEST_RESULT3);

		// precondition
		def.set(TEST_INT_PARAM, 100);

		// assert correct results of copy and move steps
		def.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("123"));
		def.get(POSTCONDITIONS).put(TEST_RESULT2, null);
		def.get(POSTCONDITIONS).put(TEST_RESULT3, equalTo("123"));

		executeProcess(def);
	}

	/**
	 * Tests execution of an unordered process.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testUnorderedProcess() throws ProcessException {
		StepListProcessDefinition def =
			new StepListProcessDefinition("Unordered");

		def.invoke("STEP1", TestStep.class).thenGoTo("STEP3");
		def.invoke("STEP2", TestStep.class).thenGoTo(PROCESS_END);
		def.invoke("STEP3", TestStep.class).thenGoTo("STEP2");

		def.set(TEST_INT_PARAM, 0);

		def.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(3));
		def.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("132"));

		executeProcess(def);
	}

	/**
	 * Creates a branching test process.
	 *
	 * @return The process definition
	 */
	@SuppressWarnings("boxing")
	private StepListProcessDefinition createBranchProcess() {
		StepListProcessDefinition def = new StepListProcessDefinition(
			"Branch");

		def.invoke("STEP1", TestStep.class);
		def.invoke("STEP2", TestStep.class);
		def.branchTo("STEP4", TEST_INT_PARAM, equalTo(102));
		def.invoke("STEP3", TestStep.class);
		def.invoke("STEP4", TestStep.class);
		def.branchTo("STEP3", TEST_INT_PARAM, equalTo(103));

		return def;
	}
}
