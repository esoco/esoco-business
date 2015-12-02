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

import de.esoco.lib.expression.MathFunctions;

import de.esoco.process.Process.ProcessEventType;
import de.esoco.process.StepListProcessDefinition.StepListEntry;
import de.esoco.process.step.FunctionStep;
import de.esoco.process.step.TransferParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static de.esoco.lib.expression.Predicates.equalTo;

import static de.esoco.process.Process.PROCESS_END;
import static de.esoco.process.ProcessRelationTypes.INPUT_PARAMS;
import static de.esoco.process.TestStep.TEST_INTERACTION_COUNT;
import static de.esoco.process.TestStep.TEST_INT_PARAM;
import static de.esoco.process.TestStep.TEST_STRING_RESULT;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.obrel.core.RelationTypes.newIntType;
import static org.obrel.core.RelationTypes.newType;
import static org.obrel.type.MetaTypes.INTERACTIVE;


/********************************************************************
 * Process test case.
 *
 * @author eso
 */
public class ProcessTest extends AbstractProcessTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final RelationType<Integer> TEST_INT_PARAM2 = newIntType();
	private static final RelationType<String>  TEST_RESULT2    = newType();
	private static final RelationType<String>  TEST_RESULT3    = newType();
	private static final RelationType<Integer> INT_RESULT	   = newIntType();

	static
	{
		RelationTypes.init(ProcessTest.class);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of {@link FunctionStep}.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testBinaryFunctionStep() throws ProcessException
	{
		StepListProcessDefinition aDef =
			new StepListProcessDefinition("BinaryFunctionStep");

		aDef.invokeBinaryFunction("STEP1",
								  TEST_INT_PARAM,
								  TEST_INT_PARAM2,
								  INT_RESULT,
								  MathFunctions.add(0));

		try
		{
			executeProcess(aDef);
			assertTrue(false);
		}
		catch (ProcessException e)
		{
			// correct execution path, input parameter is missing
		}

		aDef.set(TEST_INT_PARAM, 20);
		aDef.set(TEST_INT_PARAM2, 22);
		aDef.get(POSTCONDITIONS).put(INT_RESULT, equalTo(42));

		executeProcess(aDef);
	}

	/***************************************
	 * Tests execution of a process that contains branch steps.
	 *
	 * @throws Exception On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testBranchStep() throws Exception
	{
		StepListProcessDefinition aDef = createBranchProcess();

		// precondition
		aDef.set(TEST_INT_PARAM, 100);

		aDef.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(105));

		// assert correct execution sequence of process steps
		aDef.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("12434"));

		executeProcess(aDef);
	}

	/***************************************
	 * Test of {@link FunctionStep}.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testFunctionStep() throws ProcessException
	{
		StepListProcessDefinition aDef =
			new StepListProcessDefinition("FunctionStep");

		aDef.invokeFunction("STEP1",
							TEST_INT_PARAM,
							INT_RESULT,
							MathFunctions.add(21));

		try
		{
			executeProcess(aDef);
			assertTrue(false);
		}
		catch (ProcessException e)
		{
			// correct execution path, input parameter is missing
		}

		aDef.set(TEST_INT_PARAM, 21);
		aDef.get(POSTCONDITIONS).put(INT_RESULT, equalTo(42));

		executeProcess(aDef);
	}

	/***************************************
	 * Tests execution of a process that performs interaction through an
	 * InteractionHandler.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testInteractionHandler() throws ProcessException
	{
		StepListProcessDefinition aDef =
			new StepListProcessDefinition("InteractionHandler");

		aDef.invoke("STEP1", TestStep.class);
		aDef.invoke("STEP2", TestStep.class).set(INTERACTIVE);
		aDef.invoke("STEP3", TestStep.class);
		aDef.invoke("STEP4", TestStep.class);

		StepListEntry aLastStep = aDef.invoke("STEP5", TestStep.class);

		aLastStep.set(INTERACTIVE);
		aLastStep.addInputParameters(TEST_RESULT2, TEST_RESULT3);

		assertEquals(2, aLastStep.get(INPUT_PARAMS).size());

		Process aProcess = aDef.createProcess();

		aProcess.setParameter(TEST_INT_PARAM, new Integer(0));
		aProcess.setInteractionHandler(new ProcessInteractionHandler()
			{
				@Override
				public void performInteraction(ProcessFragment rProcessStep)
					throws Exception
				{
					int    nValue  =
						rProcessStep.checkParameter(TEST_INT_PARAM);
					String sResult =
						rProcessStep.checkParameter(TEST_STRING_RESULT);

					nValue  += 1;
					sResult += "-";

					rProcessStep.setParameter(TEST_INT_PARAM, nValue);
					rProcessStep.setParameter(TEST_STRING_RESULT, sResult);
				}
			});

		aProcess.execute();

		assertEquals(new Integer(7), aProcess.getParameter(TEST_INT_PARAM));
		assertEquals("1-234-5", aProcess.getParameter(TEST_STRING_RESULT));
	}

	/***************************************
	 * Tests execution of an interactive process.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testInteractiveProcess() throws ProcessException
	{
		StepListProcessDefinition aDef =
			new StepListProcessDefinition("Interactive");

		aDef.invoke("STEP1", TestStep.class);

		StepListEntry aSecondStep = aDef.invoke("STEP2", TestStep.class);

		aDef.invoke("STEP3", TestStep.class);
		aDef.invoke("STEP4", TestStep.class);

		StepListEntry aLastStep = aDef.invoke("STEP5", TestStep.class);

		aDef.set(TEST_INT_PARAM, 0);
		aSecondStep.set(INTERACTIVE);
		aSecondStep.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(1));
		aSecondStep.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("1"));
		aSecondStep.get(POSTCONDITIONS).put(TEST_INTERACTION_COUNT, equalTo(0));
		aLastStep.set(INTERACTIVE);
		aLastStep.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(4));
		aLastStep.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("1234"));
		aLastStep.get(POSTCONDITIONS).put(TEST_INTERACTION_COUNT, equalTo(1));

		aDef.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(5));
		aDef.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("12345"));
		aDef.get(POSTCONDITIONS).put(TEST_INTERACTION_COUNT, equalTo(2));

		executeProcess(aDef);
	}

	/***************************************
	 * Test the invocation of an interactive sub-process.
	 *
	 * @throws Exception On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testInteractiveSubProcess() throws Exception
	{
		StepListEntry[] aSubSteps =
			new StepListEntry[]
			{
				new StepListEntry("STEP3", TestStep.class),
				new StepListEntry("STEP4", TestStep.class),
				new StepListEntry("STEP5", TestStep.class),
			};

		aSubSteps[1].set(INTERACTIVE);

		ProcessDefinition aSubDef =
			new StepListProcessDefinition("SubProcess", aSubSteps);

		StepListEntry[] aSteps =
			new StepListEntry[]
			{
				new StepListEntry("STEP1", TestStep.class),
				new StepListEntry("STEP2", TestStep.class),
				new StepListEntry("SUBSTEP", SubProcessStep.class),
				new StepListEntry("STEP6", TestStep.class),
				new StepListEntry("STEP7", TestStep.class),
			};

		aSteps[1].set(INTERACTIVE, true);
		aSteps[2].set(ProcessRelationTypes.SUB_PROCESS_DEFINITION, aSubDef);
		aSteps[4].set(INTERACTIVE, true);

		ProcessDefinition aDef =
			new StepListProcessDefinition("TestInteractiveSubProcess", aSteps);

		aDef.set(TEST_INT_PARAM, 0);

		aDef.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(7));
		aDef.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("1234567"));
		aDef.get(POSTCONDITIONS).put(TEST_INTERACTION_COUNT, equalTo(3));

		executeProcess(aDef);
	}

	/***************************************
	 * Tests the cancellation of an interactive process.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testProcessCancel() throws ProcessException
	{
		StepListEntry[] aSteps =
			new StepListEntry[]
			{
				new StepListEntry("STEP1", TestStep.class),
				new StepListEntry("STEP2", TestStep.class),
				new StepListEntry("STEP3", TestStep.class),
			};

		aSteps[2].set(INTERACTIVE);

		Process aProcess =
			new StepListProcessDefinition("Cancel", aSteps).createProcess();

		aProcess.setParameter(TEST_INT_PARAM, 0);

		aProcess.execute();
		aProcess.cancel();
		assertTrue(aProcess.isFinished());
		assertEquals(new Integer(2), aProcess.getParameter(TEST_INT_PARAM));
		assertEquals("12", aProcess.getParameter(TEST_STRING_RESULT));
	}

	/***************************************
	 * Tests the execution of the {@link TransferParam} step.
	 *
	 * @throws ProcessException
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testProcessListener() throws ProcessException
	{
		StepListProcessDefinition aDef =
			new StepListProcessDefinition("ProcessListener");

		final List<ProcessEventType> aListenerMethods =
			new ArrayList<ProcessEventType>();

		ProcessListener aTestListener =
			new ProcessListener()
			{
				@Override
				public void processSuspended(Process rProcess)
				{
					aListenerMethods.add(ProcessEventType.SUSPENDED);
				}
				@Override
				public void processStarted(Process rProcess)
				{
					aListenerMethods.add(ProcessEventType.STARTED);
				}
				@Override
				public void processFinished(Process rProcess)
				{
					aListenerMethods.add(ProcessEventType.FINISHED);
				}
				@Override
				public void processCanceled(Process rProcess)
				{
					aListenerMethods.add(ProcessEventType.CANCELED);
				}
				@Override
				public void processFailed(Process rProcess)
				{
					aListenerMethods.add(ProcessEventType.FAILED);
				}
				@Override
				public void processResumed(Process rProcess)
				{
					aListenerMethods.add(ProcessEventType.RESUMED);
				}
			};

		aDef.invoke("STEP1", TestStep.class);
		aDef.invoke("STEP2", TestStep.class).set(INTERACTIVE);
		aDef.invoke("STEP3", TestStep.class);

		Process aProcess = aDef.createProcess();

		aProcess.setParameter(TEST_INT_PARAM, 0);
		aProcess.get(ProcessRelationTypes.PROCESS_LISTENERS).add(aTestListener);

		aProcess.execute();
		assertEquals(2, aListenerMethods.size());
		assertTrue(aListenerMethods.contains(ProcessEventType.STARTED));
		assertTrue(aListenerMethods.contains(ProcessEventType.SUSPENDED));
		aProcess.execute();
		assertEquals(4, aListenerMethods.size());
		assertTrue(aListenerMethods.contains(ProcessEventType.RESUMED));
		assertTrue(aListenerMethods.contains(ProcessEventType.FINISHED));

		aListenerMethods.clear();

		aProcess = aDef.createProcess();
		aProcess.setParameter(TEST_INT_PARAM, 0);
		aProcess.get(ProcessRelationTypes.PROCESS_LISTENERS).add(aTestListener);
		aProcess.execute();
		aProcess.cancel();
		assertEquals(3, aListenerMethods.size());
		assertTrue(aListenerMethods.contains(ProcessEventType.STARTED));
		assertTrue(aListenerMethods.contains(ProcessEventType.SUSPENDED));
		assertTrue(aListenerMethods.contains(ProcessEventType.CANCELED));

		aListenerMethods.clear();

		aProcess = aDef.createProcess();

		// not setting TEST_INT_PARAM will cause an exception
		aProcess.get(ProcessRelationTypes.PROCESS_LISTENERS).add(aTestListener);

		try
		{
			aProcess.execute();
			assertTrue(false);
		}
		catch (Exception e)
		{
			// correct execution path, input parameter is missing
		}

		assertEquals(2, aListenerMethods.size());
		assertTrue(aListenerMethods.contains(ProcessEventType.STARTED));
		assertTrue(aListenerMethods.contains(ProcessEventType.FAILED));
	}

	/***************************************
	 * Tests the rollback of an interactive process.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testProcessRollbackInteraction() throws ProcessException
	{
		StepListProcessDefinition aDef =
			new StepListProcessDefinition("Cancel");

		aDef.invoke("STEP1", TestStep.class).set(INTERACTIVE);
		aDef.invoke("STEP2", TestStep.class);
		aDef.invoke("STEP3", TestStep.class).set(INTERACTIVE);

		Process aProcess = aDef.createProcess();

		aProcess.setParameter(TEST_INT_PARAM, 0);

		aProcess.execute(); // to first interactive step
		aProcess.execute(); // to second interactive step
		assertEquals(new Integer(2), aProcess.getParameter(TEST_INT_PARAM));
		assertEquals("12", aProcess.getParameter(TEST_STRING_RESULT));

		aProcess.rollbackToPreviousInteraction();
		aProcess.execute(); // rollback to and re-run first interactive step
		assertEquals(new Integer(0), aProcess.getParameter(TEST_INT_PARAM));
		assertEquals("", aProcess.getParameter(TEST_STRING_RESULT));

		aProcess.execute(); // to second interactive step
		assertEquals(new Integer(2), aProcess.getParameter(TEST_INT_PARAM));
		assertEquals("12", aProcess.getParameter(TEST_STRING_RESULT));

		aProcess.execute(); // finish process
		assertEquals(new Integer(3), aProcess.getParameter(TEST_INT_PARAM));
		assertEquals("123", aProcess.getParameter(TEST_STRING_RESULT));
	}

	/***************************************
	 * Test the invocation of a sub-process.
	 *
	 * @throws Exception On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testSubProcess() throws Exception
	{
		StepListProcessDefinition aDef =
			new StepListProcessDefinition("TestSubProcess");

		aDef.invokeSubProcess("SUB", createBranchProcess());
		aDef.copyParam(TEST_STRING_RESULT, TEST_RESULT2);
		aDef.invoke("STEP!", TestStep.class);

		// precondition
		aDef.set(TEST_INT_PARAM, 100);
		aDef.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(106));

		// assert correct execution sequence of process steps
		aDef.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("12434!"));
		aDef.get(POSTCONDITIONS).put(TEST_RESULT2, equalTo("12434"));

		executeProcess(aDef);
	}

	/***************************************
	 * Tests execution of a process that performs branching and parameter
	 * copying.
	 *
	 * @throws Exception On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testSwitchStep() throws Exception
	{
		StepListProcessDefinition aDef =
			new StepListProcessDefinition("Switch");

		Map<Integer, Object> aSwitchMap = new HashMap<Integer, Object>();

		aSwitchMap.put(101, "STEP3");
		aSwitchMap.put(104, "STEP4");
		aSwitchMap.put(106, Process.PROCESS_END);

		aDef.invoke("STEP1", TestStep.class);
		aDef.switchOnParam(TEST_INT_PARAM, aSwitchMap);
		aDef.invoke("STEP2", TestStep.class);
		aDef.invoke("STEP3", TestStep.class);
		aDef.invoke("STEP4", TestStep.class);
		aDef.goTo("STEP1");

		// precondition
		aDef.set(TEST_INT_PARAM, 100);

		aDef.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(106));

		// assert correct execution sequence of process steps
		aDef.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("134141"));

		executeProcess(aDef);
	}

	/***************************************
	 * Tests the execution of the {@link TransferParam} step.
	 *
	 * @throws ProcessException
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testTransferStep() throws ProcessException
	{
		StepListProcessDefinition aDef =
			new StepListProcessDefinition("Transfer");

		aDef.invoke("STEP1", TestStep.class);
		aDef.invoke("STEP2", TestStep.class);
		aDef.invoke("STEP3", TestStep.class);
		aDef.copyParam(TEST_STRING_RESULT, TEST_RESULT2);
		aDef.moveParam(TEST_RESULT2, TEST_RESULT3);

		// precondition
		aDef.set(TEST_INT_PARAM, 100);

		// assert correct results of copy and move steps
		aDef.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("123"));
		aDef.get(POSTCONDITIONS).put(TEST_RESULT2, null);
		aDef.get(POSTCONDITIONS).put(TEST_RESULT3, equalTo("123"));

		executeProcess(aDef);
	}

	/***************************************
	 * Tests execution of an unordered process.
	 *
	 * @throws ProcessException On errors
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testUnorderedProcess() throws ProcessException
	{
		StepListProcessDefinition aDef =
			new StepListProcessDefinition("Unordered");

		aDef.invoke("STEP1", TestStep.class).thenGoTo("STEP3");
		aDef.invoke("STEP2", TestStep.class).thenGoTo(PROCESS_END);
		aDef.invoke("STEP3", TestStep.class).thenGoTo("STEP2");

		aDef.set(TEST_INT_PARAM, 0);

		aDef.get(POSTCONDITIONS).put(TEST_INT_PARAM, equalTo(3));
		aDef.get(POSTCONDITIONS).put(TEST_STRING_RESULT, equalTo("132"));

		executeProcess(aDef);
	}

	/***************************************
	 * Creates a branching test process.
	 *
	 * @return The process definition
	 */
	@SuppressWarnings("boxing")
	private StepListProcessDefinition createBranchProcess()
	{
		StepListProcessDefinition aDef =
			new StepListProcessDefinition("Branch");

		aDef.invoke("STEP1", TestStep.class);
		aDef.invoke("STEP2", TestStep.class);
		aDef.branchTo("STEP4", TEST_INT_PARAM, equalTo(102));
		aDef.invoke("STEP3", TestStep.class);
		aDef.invoke("STEP4", TestStep.class);
		aDef.branchTo("STEP3", TEST_INT_PARAM, equalTo(103));

		return aDef;
	}
}
