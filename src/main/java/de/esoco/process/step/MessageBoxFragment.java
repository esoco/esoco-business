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
package de.esoco.process.step;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.ROW_SPAN;
import static de.esoco.lib.property.UserInterfaceProperties.SAME_ROW;
import static de.esoco.lib.property.UserInterfaceProperties.STYLE;
import static de.esoco.lib.property.UserInterfaceProperties.TOOLTIP;


/********************************************************************
 * A process interaction fragment that can be displayed in a dialog view.
 *
 * @author eso
 */
public class MessageBoxFragment extends DialogFragment
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;
	private static int		  PREFIX_COUNTER   = 0;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance. If extras parameters are given they will be
	 * displayed between the message and the buttons. Any necessary
	 * initialization of these parameters including UI properties must be done
	 * by the invoking code before invoking the message box.
	 *
	 * @param sMessage       The message to display
	 * @param sIcon          The resource name for the message box icon
	 * @param rDialogActions The actions to be displayed as the dialog buttons
	 * @param rExtraParams   Optional extra parameters to be displayed in the
	 *                       message box
	 */
	public MessageBoxFragment(String				   sMessage,
							  String				   sIcon,
							  Collection<DialogAction> rDialogActions,
							  RelationType<?>... 	   rExtraParams)
	{
		super("%s_" + PREFIX_COUNTER++,
			  new MessageBoxContent(sMessage, sIcon, rExtraParams),
			  true,
			  null,
			  rDialogActions);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to always return 'MessageBox'.
	 *
	 * @see ViewFragment#getResourceBaseName()
	 */
	@Override
	protected String getResourceBaseName()
	{
		return "MessageBox";
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A fragment that defines the content of a message box.
	 *
	 * @author eso
	 */
	public static class MessageBoxContent extends InteractionFragment
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Instance fields ----------------------------------------------------

		private List<RelationType<?>> aInteractionParams = new ArrayList<>();
		private List<RelationType<?>> aInputParams		 =
			Collections.emptyList();

		private String				  sMessage;
		private String				  sIcon;
		private List<RelationType<?>> rExtraParams;

		private RelationType<String> aContentMessageParam;
		private RelationType<String> aContentIconParam;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param sMessage     The message to display
		 * @param sIcon        The resource name for the message box icon
		 * @param rExtraParams Optional extra parameters to be displayed in this
		 *                     fragment
		 */
		public MessageBoxContent(String				sMessage,
								 String				sIcon,
								 RelationType<?>... rExtraParams)
		{
			this.sMessage     = sMessage;
			this.sIcon		  = sIcon;
			this.rExtraParams = Arrays.asList(rExtraParams);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public List<RelationType<?>> getInputParameters()
		{
			return aInputParams;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public List<RelationType<?>> getInteractionParameters()
		{
			return aInteractionParams;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception
		{
			setUIFlag(SAME_ROW, aContentMessageParam);
			setUIFlag(HIDE_LABEL, aContentIconParam, aContentMessageParam);

			if (rExtraParams.size() > 0)
			{
				setUIProperty(rExtraParams.size() + 1,
							  ROW_SPAN,
							  aContentIconParam);
			}

			setUIProperty(STYLE, "MessageBoxText", aContentMessageParam);
			setUIProperty(TOOLTIP, "", aContentMessageParam, aContentIconParam);

			setParameter(aContentMessageParam, sMessage);
			setParameter(aContentIconParam, sIcon);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void setup()
		{
			aContentMessageParam =
				getTemporaryParameterType("MESSAGE_BOX_MESSAGE", String.class);

			aContentIconParam =
				getTemporaryParameterType("MESSAGE_BOX_ICON", String.class);

			aInteractionParams.add(aContentIconParam);
			aInteractionParams.add(aContentMessageParam);
			aInteractionParams.addAll(rExtraParams);
		}
	}
}
