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

import de.esoco.data.DataRelationTypes;
import de.esoco.data.SessionData;
import de.esoco.data.SessionManager;
import de.esoco.data.element.StringDataElement;
import de.esoco.entity.Entity;
import de.esoco.lib.logging.Log;
import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.TextFieldStyle;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ValueEventHandler;
import de.esoco.process.param.Parameter;

import static de.esoco.lib.property.StateProperties.DISABLE_ON_INTERACTION;
import static de.esoco.lib.property.StateProperties.FOCUSED;
import static de.esoco.lib.property.StyleProperties.TEXT_FIELD_STYLE;
import static de.esoco.process.ProcessRelationTypes.AUTO_UPDATE;
import static de.esoco.process.ProcessRelationTypes.CLIENT_INFO;
import static de.esoco.process.ProcessRelationTypes.PROCESS_SESSION_EXPIRED;
import static de.esoco.process.ProcessRelationTypes.PROCESS_USER;
import static org.obrel.type.MetaTypes.AUTHENTICATED;

/**
 * A generic login fragment for the authentication process of an application.
 *
 * @author eso
 */
public class LoginFragment extends InteractionFragment {

	/**
	 * Enumeration of the available login actions.
	 */
	public enum LoginAction {LOGIN, REGISTER, RESET_PASSWORD}

	private static final long serialVersionUID = 1L;

	private final int maxLoginAttempts;

	private final int initialLoginErrorWaitTime;

	private Parameter<String> loginName;

	private Parameter<String> password;

	private Parameter<String> errorMessage;

	private int errorCount = 0;

	private int errorWaitTime = 0;

	private long errorWaitStart;

	/**
	 * Default constructor with 3 allowed login attempts and 5 seconds initial
	 * wait time.
	 */
	public LoginFragment() {
		this(3, 5);
	}

	/**
	 * Creates a new instance with the given login failure parameters.
	 *
	 * @param maxLoginAttemptsUntilDelay The maximum number of login attempts
	 *                                   that can be done until the user must
	 *                                   wait a certain time before the next
	 *                                   attempt. -1 disabled login attempt
	 *                                   delays completely.
	 * @param initialLoginErrorWaitTime  The initial time in seconds the user
	 *                                   must wait after the maximum number of
	 *                                   login attempts has been reached; this
	 *                                   will double with each failure cycle.
	 */
	public LoginFragment(int maxLoginAttemptsUntilDelay,
		int initialLoginErrorWaitTime) {
		this.maxLoginAttempts = maxLoginAttemptsUntilDelay;
		this.initialLoginErrorWaitTime = initialLoginErrorWaitTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		layout(LayoutType.FORM).set(DISABLE_ON_INTERACTION);

		addInputFields();
		addErrorLabel();
		addLoginButton();
	}

	/**
	 * Adds the next domain availability check result to this fragment on an
	 * auto-update interaction.
	 *
	 * @see InteractionFragment#prepareInteraction()
	 */
	@Override
	@SuppressWarnings("boxing")
	public void prepareInteraction() throws Exception {
		if (errorWaitTime > 0) {
			int waitSeconds =
				(int) ((System.currentTimeMillis() - errorWaitStart) / 1000);

			if (waitSeconds > errorWaitTime) {
				if (hasFlag(AUTO_UPDATE)) {
					errorMessage.hide();
					set(AUTO_UPDATE, false);
					fragmentParam().enableEdit(true);
				}
			} else {
				errorMessage
					.show()
					.value(createErrorWaitMessage(errorWaitTime - waitSeconds));
				Thread.sleep(1000);
			}
		} else {
			if (hasFlagParameter(PROCESS_SESSION_EXPIRED)) {
				deleteParameters(PROCESS_SESSION_EXPIRED);
				errorMessage.show().value("$msgProcessSessionExpired");
			}

			fragmentParam().enableEdit(true);
		}
	}

	/**
	 * Adds the error label to this fragment.
	 */
	protected void addErrorLabel() {
		errorMessage = label("").style("ErrorMessage").hide();
	}

	/**
	 * Adds the login name and password input fields to this fragment.
	 */
	protected void addInputFields() {
		loginName = inputText("LoginName")
			.ensureNotEmpty()
			.set(FOCUSED)
			.onAction(new ValueEventHandler<String>() {
				@Override
				public void handleValueUpdate(String loginName)
					throws Exception {
					handleLoginNameInput(loginName);
				}
			});
		password = inputText("Password")
			.set(TEXT_FIELD_STYLE, TextFieldStyle.PASSWORD)
			.ensureNotEmpty()
			.onAction(new ValueEventHandler<String>() {
				@Override
				public void handleValueUpdate(String password)
					throws Exception {
					handlePasswordInput(password);
				}
			});
	}

	/**
	 * Adds the login button to this fragment.
	 */
	protected void addLoginButton() {
		buttons(LoginAction.LOGIN)
			.alignHorizontal(Alignment.CENTER)
			.continueOnInteraction(false)
			.onAction(action -> performLogin());
	}

	/**
	 * Creates the waiting message after successive login failures.
	 *
	 * @param remainingSeconds The number of seconds the user has to wait
	 * @return The error message
	 */
	@SuppressWarnings("boxing")
	protected String createErrorWaitMessage(int remainingSeconds) {
		return String.format("$${$msgSuccessiveLoginErrorStart} %d " +
			"{$msgSuccessiveLoginErrorEnd}", remainingSeconds);
	}

	/**
	 * Handles login name input.
	 *
	 * @param loginName The login name
	 */
	protected void handleLoginNameInput(String loginName) {
		if (password.value().length() > 5) {
			performLogin();
		} else {
			password.set(FOCUSED);
		}
	}

	/**
	 * Handles password input.
	 *
	 * @param password The password
	 */
	protected void handlePasswordInput(String password) {
		if (loginName.value().length() > 2) {
			performLogin();
		}
	}

	/**
	 * Will be invoked after the user has been successfully authenticated to
	 * check whether she is authorized to use the application. Can be
	 * overridden
	 * by subclasses that need to check additional constraints. Only if this
	 * method returns TRUE will the process be initialized with the
	 * authentication data. The default implementation always returns TRUE.
	 *
	 * @param user The user that has been authenticated
	 * @return TRUE if the user is authorized to use the application, FALSE if
	 * not
	 * @throws Exception Implementations may alternatively throw any kind of
	 *                   exception if the authorization fails. This has the
	 *                   same
	 *                   effect as returning FALSE.
	 */
	protected boolean isUserAuthorized(Entity user) throws Exception {
		return true;
	}

	/**
	 * Handles the login action that occurred.
	 */
	protected void performLogin() {
		String name = loginName.value().toLowerCase();
		String pass = password.value();

		StringDataElement loginData = new StringDataElement(name, pass);

		try {
			SessionManager sessionManager =
				getParameter(DataRelationTypes.SESSION_MANAGER);

			sessionManager.loginUser(loginData, getParameter(CLIENT_INFO));

			Entity user =
				sessionManager.getSessionData().get(SessionData.SESSION_USER);

			if (!isUserAuthorized(user)) {
				// handle in catch block
				throw new Exception();
			}

			errorCount = 0;
			errorWaitTime = 0;

			password.value("");

			// update the process user to the authenticated person
			setParameter(PROCESS_USER, user);
			setParameter(AUTHENTICATED, true);

			continueOnInteraction(getInteractiveInputParameter());
		} catch (Exception e) {
			String message = "$msgLoginError";

			if (maxLoginAttempts > 0 && ++errorCount >= maxLoginAttempts) {
				if (errorWaitTime > 0) {
					Log.warnf(e, "Repeated login failure of user %s", name);
				}

				errorWaitTime = errorWaitTime == 0 ?
				                initialLoginErrorWaitTime :
				                errorWaitTime * 2;

				errorCount = 0;
				errorWaitStart = System.currentTimeMillis();
				message = createErrorWaitMessage(errorWaitTime);

				fragmentParam().enableEdit(false);
				set(ProcessRelationTypes.AUTO_UPDATE);
			}

			errorMessage.show().value(message);
		}
	}
}
