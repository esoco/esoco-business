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
package de.esoco.process.ui;

import de.esoco.lib.expression.function.Validation.ValidationResult;

import de.esoco.process.InvalidParametersException;
import de.esoco.process.ui.event.HasFocusEvents;

import java.util.Collections;
import java.util.function.Function;

import org.obrel.core.RelationType;


/********************************************************************
 * The base class for interactive components.
 *
 * @author eso
 */
public class UiControl<T, C extends UiControl<T, C>> extends UiComponent<T, C>
	implements HasFocusEvents<T, UiComponent<?, C>>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @see UiComponent#Component(UiContainer, Class)
	 */
	public UiControl(UiContainer<?> rParent, Class<? super T> rDatatype)
	{
		super(rParent, rDatatype);

		if (rDatatype != null)
		{
			fragment().addInputParameters(type());
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets a validation for the control's input value. This validation will be
	 * executed on process interactions. To immediately validate the component
	 * input the method {@link #validate(Function)} can be used.
	 *
	 * @param fValidation The validation function
	 */
	public void setValidation(Function<? super T, ValidationResult> fValidation)
	{
		fragment().setParameterValidation(type(),
										  v -> fValidation.apply(v)
										  .getMessage());
	}

	/***************************************
	 * Immediately validates whether the input value of this control fulfills a
	 * certain constraint. If not an exception will be thrown that will then be
	 * handled by the process framework. To let the validation be performed by
	 * the process framework use the method {@link #setValidation(Function)}
	 * instead.
	 *
	 * @param  fValidation The validation function for the control value
	 *
	 * @throws InvalidParametersException If the validation fails
	 */
	public void validate(Function<? super T, ValidationResult> fValidation)
	{
		ValidationResult rResult = fValidation.apply(getValueImpl());

		if (!rResult.isValid())
		{
			String		    sMessage = rResult.getMessage();
			RelationType<T> rParam   = type();

			fragment().validationError(Collections.singletonMap(rParam,
																sMessage));

			throw new InvalidParametersException(fragment(), sMessage, rParam);
		}
	}

	/***************************************
	 * Initializes this control with a list parameter type. This is intended for
	 * subclasses that have a list parameter type which must be initialized with
	 * an additional element datatype. In that case the subclass should use NULL
	 * for the datatype parameter to the superclass constructor and instead
	 * invoke this method afterwards.
	 *
	 * @param rElementDatatype The element datatype of the list parameter type
	 */
	@SuppressWarnings("unchecked")
	protected <D> void initListParameterType(Class<D> rElementDatatype)
	{
		@SuppressWarnings("rawtypes")
		RelationType rListType =
			fragment().getTemporaryListType(null, rElementDatatype);

		setParameterType(rListType);
		fragment().addInputParameters(rListType);
		getParent().addComponent(this);
	}
}
