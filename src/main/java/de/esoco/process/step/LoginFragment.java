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

import de.esoco.data.DataRelationTypes;
import de.esoco.data.SessionData;
import de.esoco.data.SessionManager;
import de.esoco.data.element.StringDataElement;

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.Layout;

import de.esoco.process.Parameter;
import de.esoco.process.ParameterEventHandler;

import org.obrel.type.MetaTypes;

import static de.esoco.lib.property.StateProperties.FOCUSED;

import static de.esoco.process.ProcessRelationTypes.PROCESS_USER;


/********************************************************************
 * A generic login fragment for the authentication process of an application.
 *
 * @author eso
 */
public class LoginFragment extends InteractionFragment
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the available login actions.
	 */
	public enum LoginAction { LOGIN, REGISTER, RESET_PASSWORD }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Parameter<String> aLoginName;
	private Parameter<String> aPassword;
	private Parameter<String> aErrorMessage;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		layout(Layout.FORM);

		aLoginName =
			inputText("LoginName").ensureNotEmpty().set(FOCUSED)
								  .onAction(new ParameterEventHandler<String>()
				{
					@Override
					public void handleParameterUpdate(String sLoginName)
						throws Exception
					{
						handleLoginNameInput(sLoginName);
					}
				});
		aPassword  =
			inputText("Password").content(ContentType.PASSWORD).ensureNotEmpty()
								 .onAction(new ParameterEventHandler<String>()
				{
					@Override
					public void handleParameterUpdate(String sPassword)
						throws Exception
					{
						handlePasswordInput(sPassword);
					}
				});

		aErrorMessage = label("").style("AuthenticationError").hide();

		buttons(LoginAction.LOGIN).alignHorizontal(Alignment.CENTER)
								  .continueOnInteraction(false)
								  .onAction(new ParameterEventHandler<LoginAction>()
			{
				@Override
				public void handleParameterUpdate(LoginAction eAction)
					throws Exception
				{
					handleLoginAction(eAction);
				}
			});
	}

	/***************************************
	 * Handles the login action that occurred.
	 *
	 * @param eAction The login action
	 */
	private void handleLoginAction(LoginAction eAction)
	{
		String sLoginName = aLoginName.value();
		String sPassword  = aPassword.value();

		StringDataElement aLoginData =
			new StringDataElement(sLoginName, sPassword);

		try
		{
			SessionManager rSessionManager =
				getParameter(DataRelationTypes.SESSION_MANAGER);

			rSessionManager.loginUser(aLoginData);

			// update the process user to use the authenticated person
			setParameter(PROCESS_USER,
						 rSessionManager.getSessionData()
						 .get(SessionData.SESSION_USER));

			aPassword.value("");
			setParameter(MetaTypes.AUTHENTICATED, true);
			continueOnInteraction(getInteractiveInputParameter());
		}
		catch (Exception e)
		{
			aErrorMessage.show().value("$msgLoginError");
		}
	}

	/***************************************
	 * Handles login name input.
	 *
	 * @param sLoginName The login name value
	 */
	private void handleLoginNameInput(String sLoginName)
	{
		if (aPassword.value().length() > 5)
		{
			handleLoginAction(LoginAction.LOGIN);
		}
	}

	/***************************************
	 * Handles password input.
	 *
	 * @param sPassword The password
	 */
	private void handlePasswordInput(String sPassword)
	{
		handleLoginAction(LoginAction.LOGIN);
	}
}
