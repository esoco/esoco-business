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
package de.esoco.process.step;

import de.esoco.lib.property.ViewDisplayType;

import de.esoco.process.InvalidParametersException;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.ListenerType;

import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.ContentProperties.TOOLTIP;
import static de.esoco.lib.property.LayoutProperties.COLUMNS;
import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;


/********************************************************************
 * A process interaction fragment that can be displayed in a dialog view.
 *
 * @author eso
 */
public class DialogFragment extends ViewFragment
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the available actions for dialog fragments. Actions have a
	 * flag that defines whether the fragment needs to be validated when the
	 * dialog is closed with the respective action.
	 */
	public enum DialogAction
	{
		OK(true), YES(true), SAVE(true), CANCEL(false), NO(false), CLOSE(false);

		//~ Static fields/initializers -----------------------------------------

		/** A standard set of the {@link #OK} and {@link #CANCEL} actions. */
		public static final Set<DialogAction> OK_CANCEL =
			Collections.unmodifiableSet(EnumSet.of(OK, CANCEL));

		/** A standard set of the {@link #YES} and {@link #NO} actions. */
		public static final Set<DialogAction> YES_NO =
			Collections.unmodifiableSet(EnumSet.of(YES, NO));

		/**
		 * A standard set of the {@link #YES}, {@link #NO}, and {@link #CANCEL}
		 * actions.
		 */
		public static final Set<DialogAction> YES_NO_CANCEL =
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
		DialogAction(boolean bValidated)
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

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	private static boolean bUseFillParam = true;

	/**
	 * A listener relation type for the {@link DialogActionListener} interface.
	 */
	public static final ListenerType<DialogActionListener, DialogAction> DIALOG_ACTION_LISTENERS =
		new ListenerType<DialogActionListener, DialogAction>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void notifyListener(
				DialogActionListener rListener,
				DialogAction		 eAction)
			{
				rListener.onDialogAction(eAction);
			}
		};

	static
	{
		RelationTypes.init(DialogFragment.class);
	}

	//~ Instance fields --------------------------------------------------------

	private final Collection<DialogAction> rDialogActions;

	private RelationType<String>	   aDialogActionFillParam;
	private RelationType<String>	   aDialogActionQuestionParam;
	private RelationType<DialogAction> aDialogActionParam;

	private RelatedObject aDialogRelations = new RelatedObject();

	private String sQuestion;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain content fragment. If the given
	 * fragment implements {@link DialogActionListener} it will be registered as
	 * an action listener of this instance.
	 *
	 * @param sParamNameTemplate The string format pattern for the generation of
	 *                           the view fragment parameter names. If NULL a
	 *                           template will be generated from the class name
	 *                           of the content fragment
	 * @param rContentFragment   The fragment that contains the dialog content
	 *                           or NULL if the content will be created by the
	 *                           method {@link #createViewContent(String)}
	 * @param bModal             eViewStyle How the view should be displayed
	 * @param sQuestion          A string (typically a question) that will be
	 *                           displayed next to the dialog action buttons.
	 * @param rDialogActions     The actions to be displayed as the dialog
	 *                           buttons
	 */
	public DialogFragment(String				   sParamNameTemplate,
						  InteractionFragment	   rContentFragment,
						  boolean				   bModal,
						  String				   sQuestion,
						  Collection<DialogAction> rDialogActions)
	{
		super(sParamNameTemplate,
			  rContentFragment,
			  bModal ? ViewDisplayType.MODAL_DIALOG : ViewDisplayType.DIALOG);

		this.sQuestion	    = sQuestion;
		this.rDialogActions = rDialogActions;

		if (rContentFragment instanceof DialogActionListener)
		{
			addDialogActionListener((DialogActionListener) rContentFragment);
		}
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Global configuration method to disable the addition of a fill parameter
	 * to the button panel.
	 *
	 * <p>TODO This is just a workaround until a complete rework of the view
	 * fragment has been done (JIRA issue Framework-191).</p>
	 */
	public static void disableButtonFillParameter()
	{
		bUseFillParam = false;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a listener for dialog actions that will be invoked after the
	 * fragment dialog has been closed.
	 *
	 * @param rListener The listener to add
	 */
	public void addDialogActionListener(DialogActionListener rListener)
	{
		aDialogRelations.get(DIALOG_ACTION_LISTENERS).add(rListener);
	}

	/***************************************
	 * Finishes this dialog and hides it.
	 *
	 * @param  eAction The action to finish the dialog with
	 *
	 * @throws Exception If finishing a sub-fragment fails
	 */
	public void finishDialog(DialogAction eAction) throws Exception
	{
		if (eAction.isValidated())
		{
			Map<RelationType<?>, String> rInvalidParams =
				super.validateFragmentParameters(false);

			if (!rInvalidParams.isEmpty())
			{
				throw new InvalidParametersException(getProcessStep(),
													 rInvalidParams);
			}

			finishFragment();
		}

		DIALOG_ACTION_LISTENERS.notifyListeners(aDialogRelations, eAction);
		hide();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		if (rInteractionParam == aDialogActionParam)
		{
			DialogAction eAction = getParameter(aDialogActionParam);

			if (eAction != null)
			{
				finishDialog(eAction);
			}
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		super.init();

		setImmediateAction(aDialogActionParam, rDialogActions);

		setUIFlag(HIDE_LABEL,
				  aDialogActionFillParam,
				  aDialogActionParam,
				  aDialogActionQuestionParam);

		if (bUseFillParam)
		{
			setUIFlag(SAME_ROW, aDialogActionQuestionParam);
		}

		setUIFlag(SAME_ROW, aDialogActionParam);
		setUIProperty(3, COLUMN_SPAN, getViewContentParam());
		setUIProperty(TOOLTIP,
					  "",
					  aDialogActionFillParam,
					  aDialogActionQuestionParam);
		setUIProperty(HTML_WIDTH, "100%", aDialogActionFillParam);

		setUIProperty(rDialogActions.size(), COLUMNS, aDialogActionParam);
		setUIProperty(RESOURCE_ID, "DialogActionFill", aDialogActionFillParam);
		setUIProperty(RESOURCE_ID,
					  "DialogActionQuestion",
					  aDialogActionQuestionParam);
		setUIProperty(RESOURCE_ID, "DialogAction", aDialogActionParam);
		setParameter(aDialogActionQuestionParam, sQuestion);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void addExtraViewInteractionParams(String sParamBaseName)
	{
		aDialogActionFillParam =
			getTemporaryParameterType(sParamBaseName + "_ACTION_FILL",
									  String.class);

		aDialogActionQuestionParam =
			getTemporaryParameterType(sParamBaseName + "_ACTION_QUESTION",
									  String.class);
		aDialogActionParam		   =
			getTemporaryParameterType(sParamBaseName + "_ACTION",
									  DialogAction.class);

		if (bUseFillParam)
		{
			getInteractionParameters().add(aDialogActionFillParam);
		}

		getInteractionParameters().add(aDialogActionQuestionParam);
		addInputParameters(aDialogActionParam);
	}

	/***************************************
	 * Overridden to only validate on interactions. The final validation of a
	 * dialog will be invoked by the {@link #finishDialog(DialogAction)} method.
	 *
	 * @see ViewFragment#validateFragmentParameters(boolean)
	 */
	@Override
	Map<RelationType<?>, String> validateFragmentParameters(
		boolean bOnInteraction)
	{
		if (bOnInteraction)
		{
			return super.validateFragmentParameters(true);
		}
		else
		{
			return new HashMap<RelationType<?>, String>();
		}
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * The event listener interface for {@link DialogAction dialog actions}.
	 *
	 * @author eso
	 */
	public static interface DialogActionListener
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Will be invoked if a certain dialog action occurred.
		 *
		 * @param eAction The dialog action
		 */
		public void onDialogAction(DialogAction eAction);
	}
}
