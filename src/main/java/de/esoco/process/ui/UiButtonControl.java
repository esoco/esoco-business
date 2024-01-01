//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.data.FileType;
import de.esoco.lib.expression.Function;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.process.RuntimeProcessException;
import de.esoco.process.ui.event.UiHasActionEvents;
import org.obrel.core.RelationType;

import java.util.Collection;
import java.util.function.Consumer;

import static de.esoco.lib.property.LayoutProperties.COLUMNS;

/**
 * The base class for button components.
 *
 * @author eso
 */
public abstract class UiButtonControl<T, C extends UiButtonControl<T, C>>
	extends UiControl<T, C> implements UiHasActionEvents<T, C> {

	/**
	 * Creates a new instance with an existing parameter type.
	 *
	 * @see UiControl#UiControl(UiContainer, RelationType)
	 */
	public UiButtonControl(UiContainer<?> parent, RelationType<T> paramType) {
		super(parent, paramType);
	}

	/**
	 * Creates a new instance.
	 *
	 * @see UiControl#UiControl(UiContainer, Class)
	 */
	public UiButtonControl(UiContainer<?> parent, Class<? super T> datatype) {
		super(parent, datatype);
	}

	/**
	 * Initiates a download from this button. This method must be invoked
	 * during
	 * the handling of an event and the download will then be executed as the
	 * result of the event. After being processed by the process interaction
	 * the
	 * generated download URL will be removed from the button's process
	 * parameter. A typical usage would look like this:
	 *
	 * <pre>
	 * button.onClick(v -> initiateDownload(fileName,
	 * fileType,
	 * downloadGenerator));
	 * </pre>
	 *
	 * @param fileName          The file name of the download
	 * @param fileType          The file type of the download
	 * @param downloadGenerator The function that generated the download data
	 * @throws RuntimeProcessException If the download preparation fails
	 */
	public void initiateDownload(String fileName, FileType fileType,
		Function<FileType, ?> downloadGenerator) {
		initiateDownload(this, fileName, fileType, downloadGenerator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public C onAction(Consumer<T> eventHandler) {
		return onClick(eventHandler);
	}

	/**
	 * Sets the event handler for click events on buttons.
	 *
	 * @param eventHandler The event handler
	 * @return This instance for concatenation
	 */
	public final C onClick(Consumer<T> eventHandler) {
		return setParameterEventHandler(InteractionEventType.ACTION,
			v -> eventHandler.accept(v));
	}

	/**
	 * Checks whether the columns should be set to the number of buttons.
	 */
	void checkSetColumns(Collection<?> newButtons) {
		if (!has(COLUMNS)) {
			set(newButtons.size(), COLUMNS);
		}
	}
}
