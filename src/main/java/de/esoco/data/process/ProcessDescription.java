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
package de.esoco.data.process;

import de.esoco.data.element.DataElement;
import de.esoco.data.element.StringDataElement;

/**
 * A data element that describes a process for the use on the client.
 *
 * @author eso
 */
public class ProcessDescription extends StringDataElement {

	private static final long serialVersionUID = 1L;

	private static final String SEPARATOR_NAME = "Separator";

	private int id;

	private boolean inputRequired;

	private DataElement<?> processInput = null;

	private String clientInfo;

	private String clientLocale;

	private int clientWidth;

	private int clientHeight;

	/**
	 * Copy constructor for subclasses.
	 *
	 * @param other The other instance to copy the state of
	 */
	public ProcessDescription(ProcessDescription other) {
		this(other.getName(), other.getValue(), other.id, other.inputRequired);
	}

	/**
	 * Creates a new instance with certain attributes.
	 *
	 * @param name          The process name
	 * @param description   The process description
	 * @param id            The internal process definition ID
	 * @param inputRequired TRUE if the process can only be executed with an
	 *                      input value
	 */
	public ProcessDescription(String name, String description, int id,
		boolean inputRequired) {
		super(name, description, null, null);

		this.id = id;
		this.inputRequired = inputRequired;
	}

	/**
	 * Default constructor for serialization.
	 */
	ProcessDescription() {
	}

	/**
	 * Creates a new instance that can be used to indicate a separator in UI
	 * listings between description.
	 *
	 * @return A new separator UI description
	 */
	public static final ProcessDescription createSeparator() {
		return new ProcessDescription(SEPARATOR_NAME, null, -1, false);
	}

	/**
	 * Returns the height of the client area of the current user's web browser.
	 *
	 * @return The client area height
	 */
	public final int getClientHeight() {
		return clientHeight;
	}

	/**
	 * Returns a string with information about the connecting client (web
	 * browser).
	 *
	 * @return The client information
	 */
	public final String getClientInfo() {
		return clientInfo;
	}

	/**
	 * Returns the name of the client's locale (e.g. 'en_US' or 'de_DE'.
	 *
	 * @return The name of the client locale
	 */
	public final String getClientLocale() {
		return clientLocale;
	}

	/**
	 * Returns the width of the client area of the current user's web browser.
	 *
	 * @return The client area width
	 */
	public final int getClientWidth() {
		return clientWidth;
	}

	/**
	 * Returns the ID of the described process. This method is only intended to
	 * be used internally by the framework.
	 *
	 * @return The ID
	 */
	public final int getDescriptionId() {
		return id;
	}

	/**
	 * Returns an entity ID to be used as an initialization parameter for the
	 * process execution.
	 *
	 * @return The entity ID or -1 for none
	 */
	public final DataElement<?> getProcessInput() {
		return processInput;
	}

	/**
	 * Checks whether the process execution requires an input value. If so it
	 * must be set with the method {@link #setProcessInput(DataElement)}.
	 *
	 * @return TRUE if a process input value is required
	 */
	public final boolean isInputRequired() {
		return inputRequired;
	}

	/**
	 * Checks whether this description is a placeholder for a separator between
	 * descriptions.
	 *
	 * @return TRUE if this instance is a separator
	 */
	public final boolean isSeparator() {
		return getName().equals(SEPARATOR_NAME);
	}

	/**
	 * Sets a string with information about the client (web browser).
	 *
	 * @param info The client information
	 */
	public final void setClientInfo(String info) {
		clientInfo = info;
	}

	/**
	 * Sets the name of the client's locale (e.g. 'en_US' or 'de_DE'.
	 *
	 * @param locale The name of the client locale
	 */
	public final void setClientLocale(String locale) {
		clientLocale = locale;
	}

	/**
	 * Sets the size of the client area of the current user's web browser. This
	 * will be used to transfer the available UI area and it's proportions to
	 * the server upon and during a process execution.
	 *
	 * @param width  The width of the client area
	 * @param height The height of the client area
	 */
	public final void setClientSize(int width, int height) {
		clientWidth = width;
		clientHeight = height;
	}

	/**
	 * Sets an entity ID that will be set as a parameter on the process when it
	 * is started. This allows to execute a process for an entity that has been
	 * selected on the client.
	 *
	 * @param input processInput entityId The entityId value
	 */
	public final void setProcessInput(DataElement<?> input) {
		processInput = input;
	}
}
