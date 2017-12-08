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

import de.esoco.data.FileType;

import de.esoco.lib.expression.Function;
import de.esoco.lib.property.InteractionEventType;

import de.esoco.process.RuntimeProcessException;
import de.esoco.process.ui.event.UiHasActionEvents;

import java.util.Collection;
import java.util.function.Consumer;

import static de.esoco.lib.property.LayoutProperties.COLUMNS;


/********************************************************************
 * The base class for button components.
 *
 * @author eso
 */
public abstract class UiButtonControl<T, C extends UiButtonControl<T, C>>
	extends UiControl<T, C> implements UiHasActionEvents<T, C>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent   The parent container
	 * @param rDatatype The datatype of the component value
	 */
	public UiButtonControl(UiContainer<?> rParent, Class<? super T> rDatatype)
	{
		super(rParent, rDatatype);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Initiates a download from this button. This method must be invoked during
	 * the handling of an event and the download will then be executed as the
	 * result of the event. After being processed by the process interaction the
	 * generated download URL will be removed from the button's process
	 * parameter. A typical usage would look like this:
	 *
	 * <pre>
	    aButton.onClick(v -> initiateDownload(sFileName,
	                                          eFileType,
	                                          fDownloadGenerator));
	 * </pre>
	 *
	 * @param  sFileName          The file name of the download
	 * @param  eFileType          The file type of the download
	 * @param  fDownloadGenerator The function that generated the download data
	 *
	 * @throws RuntimeProcessException If the download preparation fails
	 */
	public void initiateDownload(String				   sFileName,
								 FileType			   eFileType,
								 Function<FileType, ?> fDownloadGenerator)
	{
		initiateDownload(this, sFileName, eFileType, fDownloadGenerator);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public C onAction(Consumer<T> rEventHandler)
	{
		return onClick(rEventHandler);
	}

	/***************************************
	 * Sets the event handler for click events on buttons.
	 *
	 * @param  rEventHandler The event handler
	 *
	 * @return This instance for concatenation
	 */
	public final C onClick(Consumer<T> rEventHandler)
	{
		return setParameterEventHandler(InteractionEventType.ACTION,
										v -> rEventHandler.accept(v));
	}

	/***************************************
	 * Checks whether the columns should be set to the number of buttons.
	 *
	 * @param rNewButtons
	 */
	void checkSetColumns(Collection<?> rNewButtons)
	{
		if (!has(COLUMNS))
		{
			set(rNewButtons.size(), COLUMNS);
		}
	}
}
