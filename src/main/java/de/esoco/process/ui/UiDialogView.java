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

import de.esoco.lib.property.ViewDisplayType;

import de.esoco.process.InvalidParametersException;
import de.esoco.process.ui.component.UiPushButtons;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.layout.UiFlowLayout;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.obrel.core.RelationType;


/********************************************************************
 * The base class for dialog views.
 *
 * @author eso
 */
public abstract class UiDialogView<V extends UiDialogView<V>>
	extends UiChildView<V>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the available buttons for dialog view. The button values
	 * have a flag that defines whether the dialog needs to be validated when
	 * the dialog is closed with the respective action.
	 */
	public enum Button
	{
		OK(true), YES(true), APPLY(true), SAVE(true), START(true), SEND(true),
		LOGIN(true), CANCEL(false), NO(false), CLOSE(false);

		//~ Static fields/initializers -----------------------------------------

		/** A standard set of the {@link #OK} and {@link #CANCEL} actions. */
		public static final Set<Button> OK_CANCEL =
			Collections.unmodifiableSet(EnumSet.of(OK, CANCEL));

		/** A standard set of the {@link #YES} and {@link #NO} actions. */
		public static final Set<Button> YES_NO =
			Collections.unmodifiableSet(EnumSet.of(YES, NO));

		/**
		 * A standard set of the {@link #YES}, {@link #NO}, and {@link #CANCEL}
		 * actions.
		 */
		public static final Set<Button> YES_NO_CANCEL =
			Collections.unmodifiableSet(EnumSet.of(YES, NO, CANCEL));

		//~ Instance fields ----------------------------------------------------

		private final boolean bValidated;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param bValidated TRUE if the dialog needs to be validated if closed
		 *                   by this action
		 */
		Button(boolean bValidated)
		{
			this.bValidated = bValidated;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Checks whether this action needs a dialog validation.
		 *
		 * @return TRUE if the dialog needs to be validated if closed by this
		 *         action
		 */
		public final boolean isValidated()
		{
			return bValidated;
		}
	}

	//~ Instance fields --------------------------------------------------------

	private Collection<Button>    rButtons		  = Button.OK_CANCEL;
	private UiPushButtons<Button> aDialogButtons;
	private Consumer<Button>	  fDialogListener;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent view
	 * @param sTitle  The dialog title
	 * @param rLayout The layout of the dialog content
	 * @param bModal  TRUE for a modal view
	 */
	public UiDialogView(UiView<?> rParent,
						String    sTitle,
						UiLayout  rLayout,
						boolean   bModal)
	{
		super(rParent,
			  new UiFlowLayout(),
			  bModal ? ViewDisplayType.MODAL_DIALOG : ViewDisplayType.DIALOG);

		setTitle(sTitle);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the listener to be notified if a dialog button is pressed. The
	 * listener will be invoked after the dialog has been closed. If the dialog
	 * content needs to be validated before closing the application code must
	 * register the corresponding validations on it's control components (with
	 * {@link UiControl#validateFinally(java.util.function.Function)}.
	 * Validations will only be performed if a button returns TRUE from it's
	 * {@link Button#isValidated()} method.
	 *
	 * @param  fListener The listener function
	 *
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public V onDialogButton(Consumer<Button> fListener)
	{
		fDialogListener = fListener;

		return (V) this;
	}

	/***************************************
	 * Sets the dialog buttons from a collection of buttons. By default a dialog
	 * has OK and Cancel buttons.
	 *
	 * @param  rButtons The new buttons
	 *
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public V setButtons(Collection<Button> rButtons)
	{
		this.rButtons = rButtons;

		if (aDialogButtons != null)
		{
			aDialogButtons.setButtons(rButtons);
		}

		return (V) this;
	}

	/***************************************
	 * Sets the dialog buttons. By default a dialog has OK and Cancel buttons.
	 *
	 * @param  rButtons The new buttons
	 *
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public V setButtons(Button... rButtons)
	{
		return setButtons(Arrays.asList(rButtons));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void build()
	{
		if (aDialogButtons == null)
		{
			UiLayoutPanel aButtonPanel = builder().addPanel(new UiFlowLayout());

			aButtonPanel.style().addStyleName("UiDialogButtonPanel");

			aDialogButtons =
				aButtonPanel.builder().addPushButtons(Button.class);

			aDialogButtons.resid("UiDialogButton")
						  .setButtons(rButtons)
						  .onClick(this::handleButton);
		}
	}

	/***************************************
	 * Handles button events.
	 *
	 * @param eButton The selected button
	 */
	private void handleButton(Button eButton)
	{
		if (eButton.isValidated())
		{
			Map<RelationType<?>, String> rInvalidParams =
				fragment().validateFragmentParameters(false);

			if (!rInvalidParams.isEmpty())
			{
				throw new InvalidParametersException(fragment(),
													 rInvalidParams);
			}
		}

		hide();

		if (fDialogListener != null)
		{
			fDialogListener.accept(eButton);
		}
	}
}
