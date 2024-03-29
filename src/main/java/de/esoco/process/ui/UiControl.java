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
package de.esoco.process.ui;

import de.esoco.lib.expression.function.Validation.ValidationResult;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.process.InvalidParametersException;
import de.esoco.process.ui.event.UiHasFocusEvents;
import org.obrel.core.RelationType;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The base class for components that allow interactive input.
 *
 * @author eso
 */
public class UiControl<T, C extends UiControl<T, C>> extends UiComponent<T, C>
	implements UiHasFocusEvents<T, C> {

	/**
	 * Creates a new instance with an existing parameter type.
	 *
	 * @see UiComponent#UiComponent(UiContainer,
	 * de.esoco.process.step.InteractionFragment, RelationType)
	 */
	public UiControl(UiContainer<?> parent, RelationType<T> paramType) {
		super(parent, parent.fragment(), paramType);

		fragment().addInputParameters(type());
	}

	/**
	 * Creates a new instance.
	 *
	 * @see UiComponent#UiComponent(UiContainer, Class)
	 */
	public UiControl(UiContainer<?> parent, Class<? super T> datatype) {
		super(parent, datatype);

		fragment().addInputParameters(type());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final C onFocusLost(Consumer<T> eventHandler) {
		return setParameterEventHandler(InteractionEventType.FOCUS_LOST,
			v -> eventHandler.accept(v));
	}

	/**
	 * Sets a final validation for the control's input value. This validation
	 * will be executed when the interactive process step this control belongs
	 * to is finished. All registered validations will be executed together and
	 * any errors that are detected will be signaled to the user.
	 *
	 * @param validation The validation function
	 * @return This instance
	 * @see #validateNow(Function)
	 * @see #validateInteractive(Function)
	 */
	@SuppressWarnings("unchecked")
	public C validateFinally(Function<? super T, ValidationResult> validation) {
		fragment().setParameterValidation(type(), false,
			v -> validation.apply(v).getMessage());

		return (C) this;
	}

	/**
	 * Sets an interactive validation for the control's input value. This
	 * validation will be executed on each interaction (i.e. user input event)
	 * that occurs in the process step to which this control belongs. All
	 * registered validations will be executed together and any errors that are
	 * detected will be signaled to the user.
	 *
	 * <p>Because this validation type is executed on each interaction event it
	 * should be used cautiously and only if continuous validation is really
	 * needed. Otherwise a continuous signaling of errors can become annoying
	 * for the user.</p>
	 *
	 * @param validation The validation function
	 * @return This instance
	 * @see #validateNow(Function)
	 * @see #validateFinally(Function)
	 */
	@SuppressWarnings("unchecked")
	public C validateInteractive(
		Function<? super T, ValidationResult> validation) {
		fragment().setParameterValidation(type(), true,
			v -> validation.apply(v).getMessage());

		return (C) this;
	}

	/**
	 * Immediately validates whether the input value of this control fulfills a
	 * certain constraint. If not an exception will be thrown that will then be
	 * handled by the process framework. This will then only indicate this
	 * single error to the user. To validate multiple components together the
	 * methods {@link #validateInteractive(Function)} and
	 * {@link #validateFinally(Function)} should be used.
	 *
	 * @param validation The validation function for the control value
	 * @throws InvalidParametersException If the validation fails
	 * @see #validateInteractive(Function)
	 * @see #validateFinally(Function)
	 */
	public void validateNow(Function<? super T, ValidationResult> validation) {
		ValidationResult result = validation.apply(getValueImpl());

		if (!result.isValid()) {
			String message = result.getMessage();
			RelationType<T> param = type();

			fragment().validationError(
				Collections.singletonMap(param, message));

			throw new InvalidParametersException(fragment(), message, param);
		}
	}
}
