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
package de.esoco.process.step;

import de.esoco.data.DataRelationTypes;
import de.esoco.data.SessionData;
import de.esoco.data.SessionManager;
import de.esoco.data.element.StringDataElement;

import de.esoco.entity.Entity;

import de.esoco.lib.logging.Log;
import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.LayoutType;

import de.esoco.process.Parameter;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ValueEventHandler;

import static de.esoco.lib.property.StateProperties.DISABLE_ON_INTERACTION;
import static de.esoco.lib.property.StateProperties.FOCUSED;

import static de.esoco.process.ProcessRelationTypes.AUTO_UPDATE;
import static de.esoco.process.ProcessRelationTypes.CLIENT_INFO;
import static de.esoco.process.ProcessRelationTypes.PROCESS_USER;

import static org.obrel.type.MetaTypes.AUTHENTICATED;


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

	private final int nMaxLoginAttempts;
	private final int nInitialLoginErrorWaitTime;

	private Parameter<String> aLoginName;
	private Parameter<String> aPassword;
	private Parameter<String> aErrorMessage;

	private int  nErrorCount     = 0;
	private int  nErrorWaitTime  = 0;
	private long nErrorWaitStart;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Default constructor with 3 allowed login attempts and 5 seconds initial
	 * wait time.
	 */
	public LoginFragment()
	{
		this(3, 5);
	}

	/***************************************
	 * Creates a new instance with the given login failure parameters.
	 *
	 * @param nMaxLoginAttemptsUntilDelay The maximum number of login attempts
	 *                                    that can be done until the user must
	 *                                    wait a certain time before the next
	 *                                    attempt. -1 disabled login attempt
	 *                                    delays completely.
	 * @param nInitialLoginErrorWaitTime  The initial time in seconds the user
	 *                                    must wait after the maximum number of
	 *                                    login attempts has been reached; this
	 *                                    will double with each failure cycle.
	 */
	public LoginFragment(
		int nMaxLoginAttemptsUntilDelay,
		int nInitialLoginErrorWaitTime)
	{
		this.nMaxLoginAttempts		    = nMaxLoginAttemptsUntilDelay;
		this.nInitialLoginErrorWaitTime = nInitialLoginErrorWaitTime;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		layout(LayoutType.FORM).set(DISABLE_ON_INTERACTION);

		addInputFields();
		addErrorLabel();
		addLoginButton();
	}

	/***************************************
	 * Adds the next domain availability check result to this fragment on an
	 * auto-update interaction.
	 *
	 * @see InteractionFragment#prepareInteraction()
	 */
	@Override
	@SuppressWarnings("boxing")
	public void prepareInteraction() throws Exception
	{
		if (nErrorWaitTime > 0)
		{
			int nWaitSeconds =
				(int) ((System.currentTimeMillis() - nErrorWaitStart) / 1000);

			if (nWaitSeconds > nErrorWaitTime)
			{
				if (hasFlag(AUTO_UPDATE))
				{
					aErrorMessage.hide();
					set(AUTO_UPDATE, false);
					fragmentParam().enableEdit(true);
				}
			}
			else
			{
				aErrorMessage.value(createErrorWaitMessage(nErrorWaitTime -
														   nWaitSeconds));
				Thread.sleep(1000);
			}
		}
		else
		{
			fragmentParam().enableEdit(true);
		}
	}

	/***************************************
	 * Adds the error label to this fragment.
	 */
	protected void addErrorLabel()
	{
		aErrorMessage = label("").style("AuthenticationError").hide();
	}

	/***************************************
	 * Adds the login name and password input fields to this fragment.
	 */
	protected void addInputFields()
	{
		aLoginName =
			inputText("LoginName").ensureNotEmpty()
								  .set(FOCUSED)
								  .onAction(new ValueEventHandler<String>()
				{
					@Override
					public void handleValueUpdate(String sLoginName)
						throws Exception
					{
						handleLoginNameInput(sLoginName);
					}
				});
		aPassword  =
			inputText("Password").content(ContentType.PASSWORD)
								 .ensureNotEmpty()
								 .onAction(new ValueEventHandler<String>()
				{
					@Override
					public void handleValueUpdate(String sPassword)
						throws Exception
					{
						handlePasswordInput(sPassword);
					}
				});
	}

	/***************************************
	 * Adds the login button to this fragment.
	 */
	protected void addLoginButton()
	{
		buttons(LoginAction.LOGIN).alignHorizontal(Alignment.CENTER)
								  .continueOnInteraction(false)
								  .onAction(eAction -> performLogin());
	}

	/***************************************
	 * Will be invoked after the user has been successfully authenticated to
	 * check whether she is authorized to use the application. Can be overridden
	 * by subclasses that need to check additional constraints. Only if this
	 * method returns TRUE will the process be initialized with the
	 * authentication data. The default implementation always returns TRUE.
	 *
	 * @param  rUser The user that has been authenticated
	 *
	 * @return TRUE if the user is authorized to use the application, FALSE if
	 *         not
	 *
	 * @throws Exception Implementations may alternatively throw any kind of
	 *                   exception if the authorization fails. This has the same
	 *                   effect as returning FALSE.
	 */
	protected boolean isUserAuthorized(Entity rUser) throws Exception
	{
		return true;
	}

	/***************************************
	 * Creates the waiting message after successive login failures.
	 *
	 * @param  nRemainingSeconds The number of seconds the user has to wait
	 *
	 * @return
	 */
	@SuppressWarnings("boxing")
	private String createErrorWaitMessage(int nRemainingSeconds)
	{
		return String.format("$${$msgSuccessiveLoginErrorStart} %d " +
							 "{$msgSuccessiveLoginErrorEnd}",
							 nRemainingSeconds);
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
			performLogin();
		}
		else
		{
			aPassword.set(FOCUSED);
		}
	}

	/***************************************
	 * Handles password input.
	 *
	 * @param sPassword The password
	 */
	private void handlePasswordInput(String sPassword)
	{
		if (aLoginName.value().length() > 2)
		{
			performLogin();
		}
	}

	/***************************************
	 * Handles the login action that occurred.
	 */
	private void performLogin()
	{
		String sLoginName = aLoginName.value();
		String sPassword  = aPassword.value();

		StringDataElement aLoginData =
			new StringDataElement(sLoginName, sPassword);

		try
		{
			SessionManager rSessionManager =
				getParameter(DataRelationTypes.SESSION_MANAGER);

			rSessionManager.loginUser(aLoginData, getParameter(CLIENT_INFO));

			Entity rUser =
				rSessionManager.getSessionData().get(SessionData.SESSION_USER);

			if (!isUserAuthorized(rUser))
			{
				// handle in catch block
				throw new Exception();
			}

			nErrorCount    = 0;
			nErrorWaitTime = 0;

			aPassword.value("");

			// update the process user to the authenticated person
			setParameter(PROCESS_USER, rUser);
			setParameter(AUTHENTICATED, true);

			continueOnInteraction(getInteractiveInputParameter());
		}
		catch (Exception e)
		{
			String sMessage = "$msgLoginError";

			if (nMaxLoginAttempts > 0 && ++nErrorCount >= nMaxLoginAttempts)
			{
				if (nErrorWaitTime > 0)
				{
					Log.warnf(e,
							  "Repeated login failure of user %s",
							  sLoginName);
				}

				nErrorWaitTime =
					nErrorWaitTime == 0 ? nInitialLoginErrorWaitTime
										: nErrorWaitTime * 2;

				nErrorCount     = 0;
				nErrorWaitStart = System.currentTimeMillis();
				sMessage	    = createErrorWaitMessage(nErrorWaitTime);

				fragmentParam().enableEdit(false);
				set(ProcessRelationTypes.AUTO_UPDATE);
			}

			aErrorMessage.show().value(sMessage);
		}
	}
}
