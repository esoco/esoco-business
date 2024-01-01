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
package de.esoco.process.step;

import de.esoco.lib.property.ViewDisplayType;
import de.esoco.process.InvalidParametersException;
import de.esoco.process.ViewFragment;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.ListenerType;
import org.obrel.type.ListenerTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.ContentProperties.TOOLTIP;
import static de.esoco.lib.property.LayoutProperties.COLUMNS;
import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;

/**
 * A process interaction fragment that can be displayed in a dialog view.
 *
 * @author eso
 */
public class DialogFragment extends ViewFragment {

	/**
	 * Enumeration of the available actions for dialog fragments. Actions
	 * have a
	 * flag that defines whether the fragment needs to be validated when the
	 * dialog is closed with the respective action.
	 */
	public enum DialogAction {
		OK(true), YES(true), APPLY(true), SAVE(true), START(true), SEND(true),
		LOGIN(true), CANCEL(false), NO(false), CLOSE(false);

		/**
		 * A standard set of the {@link #OK} and {@link #CANCEL} actions.
		 */
		public static final Set<DialogAction> OK_CANCEL =
			Collections.unmodifiableSet(EnumSet.of(OK, CANCEL));

		/**
		 * A standard set of the {@link #YES} and {@link #NO} actions.
		 */
		public static final Set<DialogAction> YES_NO =
			Collections.unmodifiableSet(EnumSet.of(YES, NO));

		/**
		 * A standard set of the {@link #YES}, {@link #NO}, and {@link #CANCEL}
		 * actions.
		 */
		public static final Set<DialogAction> YES_NO_CANCEL =
			Collections.unmodifiableSet(EnumSet.of(YES, NO, CANCEL));

		private final boolean validated;

		/**
		 * Creates a new instance.
		 *
		 * @param validated TRUE if the dialog needs to be validated if closed
		 *                  by this action
		 */
		DialogAction(boolean validated) {
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

	/**
	 * A listener relation type for the {@link DialogActionListener} interface.
	 */
	public static final ListenerType<DialogActionListener, DialogAction>
		DIALOG_ACTION_LISTENERS =
		ListenerTypes.newListenerType((l, e) -> l.onDialogAction(e));

	private static final long serialVersionUID = 1L;

	private static boolean useFillParam = true;

	static {
		RelationTypes.init(DialogFragment.class);
	}

	private final Collection<DialogAction> dialogActions;

	private RelationType<String> dialogActionFillParam;

	private RelationType<String> dialogActionQuestionParam;

	private RelationType<DialogAction> dialogActionParam;

	private final RelatedObject dialogRelations = new RelatedObject();

	private final String question;

	/**
	 * Creates a new instance for a certain content fragment. If the given
	 * fragment implements {@link DialogActionListener} it will be
	 * registered as
	 * an action listener of this instance.
	 *
	 * @param paramNameTemplate The string format pattern for the generation of
	 *                          the view fragment parameter names. If NULL a
	 *                          template will be generated from the class name
	 *                          of the content fragment
	 * @param contentFragment   The fragment that contains the dialog content
	 * @param modal             viewStyle How the view should be displayed
	 * @param question          A string (typically a question) that will be
	 *                          displayed next to the dialog action buttons.
	 * @param dialogActions     The actions to be displayed as the dialog
	 *                          buttons
	 */
	public DialogFragment(String paramNameTemplate,
		InteractionFragment contentFragment, boolean modal, String question,
		Collection<DialogAction> dialogActions) {
		super(paramNameTemplate, contentFragment,
			modal ? ViewDisplayType.MODAL_DIALOG : ViewDisplayType.DIALOG);

		this.question = question;
		this.dialogActions = dialogActions;

		if (contentFragment instanceof DialogActionListener) {
			addDialogActionListener((DialogActionListener) contentFragment);
		}
	}

	/**
	 * Global configuration method to disable the addition of a fill parameter
	 * to the button panel.
	 *
	 * <p>TODO This is just a workaround until the dialog fragment has been
	 * adapted to layout-bayed UI (instead of table-based).</p>
	 */
	public static void disableButtonFillParameter() {
		useFillParam = false;
	}

	/**
	 * Adds a listener for dialog actions that will be invoked after the
	 * fragment dialog has been closed.
	 *
	 * @param listener The listener to add
	 */
	public void addDialogActionListener(DialogActionListener listener) {
		dialogRelations.get(DIALOG_ACTION_LISTENERS).add(listener);
	}

	/**
	 * Finishes this dialog and hides it.
	 *
	 * @param action The action to finish the dialog with
	 * @throws Exception If finishing a sub-fragment fails
	 */
	public void finishDialog(DialogAction action) throws Exception {
		if (action.isValidated()) {
			Map<RelationType<?>, String> invalidParams =
				super.validateFragmentParameters(false);

			if (!invalidParams.isEmpty()) {
				throw new InvalidParametersException(getProcessStep(),
					invalidParams);
			}

			finishFragment();
		}

		DIALOG_ACTION_LISTENERS.notifyListeners(dialogRelations, action);
		hide();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> interactionParam)
		throws Exception {
		if (interactionParam == dialogActionParam) {
			DialogAction action = getParameter(dialogActionParam);

			if (action != null) {
				finishDialog(action);
			}
		} else {
			super.handleInteraction(interactionParam);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		super.init();

		setImmediateAction(dialogActionParam, dialogActions);

		setUIFlag(HIDE_LABEL, dialogActionFillParam, dialogActionParam,
			dialogActionQuestionParam);

		if (useFillParam) {
			setUIFlag(SAME_ROW, dialogActionQuestionParam);
		}

		setUIFlag(SAME_ROW, dialogActionParam);
		setUIProperty(3, COLUMN_SPAN, getViewContentParam());
		setUIProperty(TOOLTIP, "", dialogActionFillParam,
			dialogActionQuestionParam);
		setUIProperty(HTML_WIDTH, "100%", dialogActionFillParam);

		setUIProperty(dialogActions.size(), COLUMNS, dialogActionParam);
		setUIProperty(RESOURCE_ID, "DialogActionFill", dialogActionFillParam);
		setUIProperty(RESOURCE_ID, "DialogActionQuestion",
			dialogActionQuestionParam);
		setUIProperty(RESOURCE_ID, "DialogAction", dialogActionParam);
		setParameter(dialogActionQuestionParam, question);
	}

	/**
	 * Overridden to only validate on interactions. The final validation of a
	 * dialog will be invoked by the {@link #finishDialog(DialogAction)}
	 * method.
	 *
	 * @see ViewFragment#validateFragmentParameters(boolean)
	 */
	@Override
	public Map<RelationType<?>, String> validateFragmentParameters(
		boolean onInteraction) {
		if (onInteraction) {
			return super.validateFragmentParameters(true);
		} else {
			return new HashMap<RelationType<?>, String>();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addExtraViewInteractionParams(String paramBaseName) {
		dialogActionFillParam =
			getTemporaryParameterType(paramBaseName + "_ACTION_FILL",
				String.class);

		dialogActionQuestionParam =
			getTemporaryParameterType(paramBaseName + "_ACTION_QUESTION",
				String.class);
		dialogActionParam = getTemporaryParameterType(paramBaseName +
				"_ACTION",
			DialogAction.class);

		if (useFillParam) {
			getInteractionParameters().add(dialogActionFillParam);
		}

		getInteractionParameters().add(dialogActionQuestionParam);
		addInputParameters(dialogActionParam);
	}

	/**
	 * The event listener interface for {@link DialogAction dialog actions}.
	 *
	 * @author eso
	 */
	public interface DialogActionListener {

		/**
		 * Will be invoked if a certain dialog action occurred.
		 *
		 * @param action The dialog action
		 */
		void onDialogAction(DialogAction action);
	}
}
