//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.property.ViewDisplayType;
import de.esoco.process.InvalidParametersException;
import de.esoco.process.ui.component.UiPushButtons;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.layout.UiFlowLayout;
import org.obrel.core.RelationType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The base class for dialog views.
 *
 * @author eso
 */
public abstract class UiDialogView<V extends UiDialogView<V>>
	extends UiChildView<V> {

	/**
	 * Enumeration of the available buttons for dialog view. The button values
	 * have a flag that defines whether the dialog needs to be validated when
	 * the dialog is closed with the respective action.
	 */
	public enum Button {
		OK(true), YES(true), APPLY(true), SAVE(true), START(true), SEND(true),
		LOGIN(true), CANCEL(false), NO(false), CLOSE(false);

		/**
		 * A standard set of the {@link #OK} and {@link #CANCEL} actions.
		 */
		public static final Set<Button> OK_CANCEL =
			Collections.unmodifiableSet(EnumSet.of(OK, CANCEL));

		/**
		 * A standard set of the {@link #YES} and {@link #NO} actions.
		 */
		public static final Set<Button> YES_NO =
			Collections.unmodifiableSet(EnumSet.of(YES, NO));

		/**
		 * A standard set of the {@link #YES}, {@link #NO}, and {@link #CANCEL}
		 * actions.
		 */
		public static final Set<Button> YES_NO_CANCEL =
			Collections.unmodifiableSet(EnumSet.of(YES, NO, CANCEL));

		private final boolean validated;

		/**
		 * Creates a new instance.
		 *
		 * @param validated TRUE if the dialog needs to be validated if closed
		 *                  by this action
		 */
		Button(boolean validated) {
			this.validated = validated;
		}

		/**
		 * Checks whether this action needs a dialog validation.
		 *
		 * @return TRUE if the dialog needs to be validated if closed by this
		 * action
		 */
		public final boolean isValidated() {
			return validated;
		}
	}

	private Collection<Button> buttons = Button.OK_CANCEL;

	private UiPushButtons<Button> dialogButtons;

	private Consumer<Button> dialogListener;

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent view
	 * @param title  The dialog title
	 * @param layout The layout of the dialog content
	 * @param modal  TRUE for a modal view
	 */
	public UiDialogView(UiView<?> parent, String title, UiLayout layout,
		boolean modal) {
		super(parent, new UiFlowLayout(),
			modal ? ViewDisplayType.MODAL_DIALOG : ViewDisplayType.DIALOG);

		setTitle(title);
	}

	/**
	 * Sets the listener to be notified if a dialog button is pressed. The
	 * listener will be invoked after the dialog has been closed. If the dialog
	 * content needs to be validated before closing the application code must
	 * register the corresponding validations on it's control components (with
	 * {@link UiControl#validateFinally(java.util.function.Function)}.
	 * Validations will only be performed if a button returns TRUE from it's
	 * {@link Button#isValidated()} method.
	 *
	 * @param listener The listener function
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public V onDialogButton(Consumer<Button> listener) {
		dialogListener = listener;

		return (V) this;
	}

	/**
	 * Sets the dialog buttons from a collection of buttons. By default a
	 * dialog
	 * has OK and Cancel buttons.
	 *
	 * @param buttons The new buttons
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public V setButtons(Collection<Button> buttons) {
		this.buttons = buttons;

		if (dialogButtons != null) {
			dialogButtons.setButtons(buttons);
		}

		return (V) this;
	}

	/**
	 * Sets the dialog buttons. By default a dialog has OK and Cancel buttons.
	 *
	 * @param buttons The new buttons
	 * @return This instance
	 */
	public V setButtons(Button... buttons) {
		return setButtons(Arrays.asList(buttons));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buildContent(UiBuilder<?> builder) {
		if (dialogButtons == null) {
			UiLayoutPanel buttonPanel = builder.addPanel(new UiFlowLayout());

			buttonPanel.style().addStyleName("UiDialogButtonPanel");

			dialogButtons = buttonPanel.builder().addPushButtons(Button.class);

			dialogButtons
				.resid("UiDialogButton")
				.setButtons(buttons)
				.onClick(this::handleButton);
		}
	}

	/**
	 * Handles button events.
	 *
	 * @param button The selected button
	 */
	private void handleButton(Button button) {
		if (button.isValidated()) {
			Map<RelationType<?>, String> invalidParams =
				fragment().validateFragmentParameters(false);

			if (!invalidParams.isEmpty()) {
				throw new InvalidParametersException(fragment(),
					invalidParams);
			}
		}

		if (dialogListener != null) {
			dialogListener.accept(button);
		}

		hide();
	}
}
