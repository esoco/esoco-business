//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.FunctionException;

import de.esoco.process.ProcessException;
import de.esoco.process.ProcessStep;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.obrel.core.RelationType;

import static org.obrel.core.RelationTypes.newRelationType;


/********************************************************************
 * A process step implementation that evaluates input parameters with a function
 * and stores the resulting output value in a process parameter. All function
 * steps inherently support a rollback of their execution. During a rollback the
 * step will simply clear the output parameter where the previous execution had
 * stored the result (if the output parameter is defined).
 *
 * <p>This class support both unary and binary functions. For the latter the
 * step parameter {@link #FUNCTION_SECONDARY_INPUT} refers to the parameter
 * which contains with the second input value for the invocation of the method
 * {@link BinaryFunction#evaluate(Object, Object)}. If the secondary input
 * parameter is not set a binary function will be invoked as a unary function,
 * thus keeping any preset secondary input.</p>
 *
 * <p><b>Attention</b>: The relation types of the input and output parameters
 * need to be stored in wildcard form to allow the use of arbitrary parameters.
 * This means that no generic type checking is possible between the function and
 * parameter types. Therefore the application code must make sure that the types
 * of the parameters match that of the given function or else an exception will
 * occur at runtime.</p>
 *
 * <p>The following parameters are supported (r = required, o = optional):</p>
 *
 * <ul>
 *   <li>{@link #FUNCTION} (r): The function to be evaluated. If this is a
 *     binary function it will be applied to both the main and secondary input
 *     values.</li>
 *   <li> {@link #FUNCTION_MAIN_INPUT}(o): The type of the parameter containing
 *     the main input value (the left value for binary functions); if not set
 *     NULL will be used.</li>
 *   <li> {@link #FUNCTION_SECONDARY_INPUT}(o): The type of the parameter
 *     containing the secondary (right-side) input value for binary functions;
 *     if not set NULL will be used.</li>
 *   <li>{@link #FUNCTION_OUTPUT} (o): The type of the parameter to receive the
 *     function result; if not set the result will be ignored; if neither input
 *     nor output parameter are set an exception will be thrown.</li>
 * </ul>
 *
 * @author eso
 */
public class FunctionStep extends ProcessStep
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The function to evaluate the input parameter with. */
	public static final RelationType<java.util.function.Function<?, ?>> FUNCTION =
		newRelationType("de.esoco.process.FUNCTION", Function.class);

	/**
	 * The process parameter containing the main (left) input value for the
	 * function.
	 */
	public static final RelationType<RelationType<?>> FUNCTION_MAIN_INPUT =
		newRelationType(
			"de.esoco.process.FUNCTION_MAIN_INPUT",
			RelationType.class);

	/**
	 * The process parameter containing the secondary (right) input value for a
	 * binary function.
	 */
	public static final RelationType<RelationType<?>> FUNCTION_SECONDARY_INPUT =
		newRelationType(
			"de.esoco.process.FUNCTION_SECONDARY_INPUT",
			RelationType.class);

	/** The process parameter to store the function result in. */
	public static final RelationType<RelationType<?>> FUNCTION_OUTPUT =
		newRelationType("de.esoco.process.FUNCTION_OUTPUT", RelationType.class);

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public FunctionStep()
	{
		setMandatory(FUNCTION);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to always return TRUE.
	 *
	 * @see ProcessStep#canRollback()
	 */
	@Override
	protected boolean canRollback()
	{
		return true;
	}

	/***************************************
	 * Evaluates the value of the input parameter with the function and stores
	 * the result in the output parameter.
	 *
	 * @throws ProcessException If the function evaluation yields an exception
	 */
	@Override
	protected void execute()
	{
		@SuppressWarnings("unchecked")
		UnaryOperator<Object> rFunction =
			(UnaryOperator<Object>) checkParameter(FUNCTION);
		@SuppressWarnings("unchecked")
		RelationType<Object>  rOutParam =
			(RelationType<Object>) getParameter(FUNCTION_OUTPUT);

		RelationType<?> rLeftParam  = getParameter(FUNCTION_MAIN_INPUT);
		RelationType<?> rRightParam = getParameter(FUNCTION_SECONDARY_INPUT);

		if (rLeftParam == null && rOutParam == null)
		{
			throw new IllegalStateException(
				"Both input and output parameters missing for " +
				rFunction);
		}

		if (rLeftParam != null && !hasParameter(rLeftParam))
		{
			throw new ProcessException(
				this,
				String.format(
					"Input value %s missing for function [%s]",
					rLeftParam,
					rFunction));
		}

		Object rResult = evaluateFunction(rFunction, rLeftParam, rRightParam);

		if (rOutParam != null)
		{
			setParameter(rOutParam, rResult);
		}
	}

	/***************************************
	 * Removes the {@link #FUNCTION_OUTPUT} parameter from the process.
	 *
	 * @see ProcessStep#rollback()
	 */
	@Override
	protected final void rollback() throws Exception
	{
		RelationType<?> rOutParam = getParameter(FUNCTION_OUTPUT);

		if (rOutParam != null)
		{
			deleteParameters(rOutParam);
		}
	}

	/***************************************
	 * Internal method to evaluate the value of the input parameters with this
	 * step's function.
	 *
	 * @param  rFunction   The function
	 * @param  rLeftParam  The parameter containing the left (main) input value
	 * @param  rRightParam The parameter containing the right (secondary) input
	 *                     value for binary functions
	 *
	 * @return The result of the function evaluation
	 *
	 * @throws ProcessException If the evaluation fails
	 */
	@SuppressWarnings("unchecked")
	private Object evaluateFunction(UnaryOperator<Object> rFunction,
									RelationType<?>		  rLeftParam,
									RelationType<?>		  rRightParam)
	{
		Object rLeft   = (rLeftParam != null ? getParameter(rLeftParam) : null);
		Object rRight  = null;
		Object rResult;

		try
		{
			if (rRightParam != null)
			{
				if (!(rFunction instanceof BinaryFunction<?, ?, ?>))
				{
					throw new ProcessException(
						this,
						String.format(
							"Secondary function parameter %s cannot be used with unary function [%s]",
							rRightParam,
							rFunction));
				}

				if (!hasParameter(rRightParam))
				{
					throw new ProcessException(
						this,
						String.format(
							"Secondary input value %s missing for binary function [%s]",
							rRightParam,
							rFunction));
				}

				rRight = getParameter(rRightParam);

				rResult =
					((BinaryFunction<Object, Object, Object>) rFunction)
					.evaluate(rLeft, rRight);
			}
			else
			{
				rResult = rFunction.apply(rLeft);
			}
		}
		catch (FunctionException e)
		{
			Throwable t = e.getCause();

			if (t instanceof ProcessException)
			{
				throw (ProcessException) t;
			}
			else
			{
				throw new ProcessException(this, t);
			}
		}
		catch (ProcessException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String sMessage;

			if (rRightParam != null)
			{
				sMessage =
					String.format(
						"Could not evaluate binary function %s (input: [%s=%s], [%s=%s])",
						rFunction,
						rLeftParam,
						rLeft,
						rRightParam,
						rRight);
			}
			else
			{
				sMessage =
					String.format(
						"Could not evaluate function %s (input: [%s=%s])",
						rFunction,
						rLeftParam,
						rLeft);
			}

			throw new ProcessException(this, sMessage, e);
		}

		return rResult;
	}
}
