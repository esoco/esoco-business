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

import de.esoco.process.ViewFragment;
import org.obrel.core.RelationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.ROW_SPAN;
import static de.esoco.lib.property.UserInterfaceProperties.SAME_ROW;
import static de.esoco.lib.property.UserInterfaceProperties.STYLE;
import static de.esoco.lib.property.UserInterfaceProperties.TOOLTIP;

/**
 * A process interaction fragment that can be displayed in a dialog view.
 *
 * @author eso
 */
public class MessageBoxFragment extends DialogFragment {

	private static final long serialVersionUID = 1L;

	private static int PREFIX_COUNTER = 0;

	/**
	 * Creates a new instance. If extras parameters are given they will be
	 * displayed between the message and the buttons. Any necessary
	 * initialization of these parameters including UI properties must be done
	 * by the invoking code before invoking the message box.
	 *
	 * @param message       The message to display
	 * @param icon          The resource name for the message box icon
	 * @param dialogActions The actions to be displayed as the dialog buttons
	 * @param extraParams   Optional extra parameters to be displayed in the
	 *                      message box
	 */
	public MessageBoxFragment(String message, String icon,
		Collection<DialogAction> dialogActions,
		RelationType<?>... extraParams) {
		super("%s_" + PREFIX_COUNTER++,
			new MessageBoxContent(message, icon, extraParams), true, null,
			dialogActions);
	}

	/**
	 * Overridden to always return 'MessageBox'.
	 *
	 * @see ViewFragment#getResourceBaseName()
	 */
	@Override
	protected String getResourceBaseName() {
		return "MessageBox";
	}

	/**
	 * A fragment that defines the content of a message box.
	 *
	 * @author eso
	 */
	public static class MessageBoxContent extends InteractionFragment {

		private static final long serialVersionUID = 1L;

		private final List<RelationType<?>> interactionParams = new ArrayList<>();

		private final List<RelationType<?>> inputParams = Collections.emptyList();

		private final String message;

		private final String icon;

		private final List<RelationType<?>> extraParams;

		private RelationType<String> contentMessageParam;

		private RelationType<String> contentIconParam;

		/**
		 * Creates a new instance.
		 *
		 * @param message     The message to display
		 * @param icon        The resource name for the message box icon
		 * @param extraParams Optional extra parameters to be displayed in this
		 *                    fragment
		 */
		public MessageBoxContent(String message, String icon,
			RelationType<?>... extraParams) {
			this.message = message;
			this.icon = icon;
			this.extraParams = Arrays.asList(extraParams);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<RelationType<?>> getInputParameters() {
			return inputParams;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<RelationType<?>> getInteractionParameters() {
			return interactionParams;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception {
			setUIFlag(SAME_ROW, contentMessageParam);
			setUIFlag(HIDE_LABEL, contentIconParam, contentMessageParam);

			if (extraParams.size() > 0) {
				setUIProperty(extraParams.size() + 1, ROW_SPAN,
					contentIconParam);
			}

			setUIProperty(STYLE, "MessageBoxText", contentMessageParam);
			setUIProperty(TOOLTIP, "", contentMessageParam, contentIconParam);

			setParameter(contentMessageParam, message);
			setParameter(contentIconParam, icon);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setup() {
			contentMessageParam =
				getTemporaryParameterType("MESSAGE_BOX_MESSAGE", String.class);

			contentIconParam =
				getTemporaryParameterType("MESSAGE_BOX_ICON", String.class);

			interactionParams.add(contentIconParam);
			interactionParams.add(contentMessageParam);
			interactionParams.addAll(extraParams);
		}
	}
}
