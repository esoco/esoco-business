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
package de.esoco.process.ui.view;

import de.esoco.process.ui.UiBuilder;
import de.esoco.process.ui.UiDialogView;
import de.esoco.process.ui.UiImageDefinition;
import de.esoco.process.ui.UiView;
import de.esoco.process.ui.graphics.UiImageResource;
import de.esoco.process.ui.layout.UiFlowLayout;


/********************************************************************
 * A dialog view for message boxes that display an image or icon and a message
 * string. By default the message box has OK and Cancel buttons but that be
 * changed by invoking
 *
 * @author eso
 */
public class UiMessageBox extends UiDialogView<UiMessageBox>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent  The parent view
	 * @param sTitle   The message box title
	 * @param rImage   The image
	 * @param sMessage The message
	 */
	public UiMessageBox(UiView<?>			 rParent,
						String				 sTitle,
						UiImageDefinition<?> rImage,
						String				 sMessage)
	{
		super(rParent, sTitle, new UiFlowLayout(), true);

		UiBuilder<?> aMessageBuilder =
			builder().addPanel(new UiFlowLayout()).builder();

		aMessageBuilder.addImage(rImage).style().styleName("UiMessageBoxIcon");
		aMessageBuilder.addLabel(sMessage)
					   .style()
					   .styleName("UiMessageBoxMessage");
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Displays a message box with a message to be confirmed, a corresponding
	 * icon (from resource "imUiMessageBoxConfirm") and YES and No buttons. The
	 * view title will be the resource "tiUiMessageBoxConfirm".
	 *
	 * @param  rParentView        The parent view
	 * @param  sMessage           The message text
	 * @param  fRunOnConfirmation A runnable that should be executed if the
	 *                            message box is confirmed (i.e. the YES button
	 *                            is pressed)
	 *
	 * @return The message box instance
	 */
	public static UiMessageBox showConfirmation(UiView<?> rParentView,
												String    sMessage,
												Runnable  fRunOnConfirmation)
	{
		UiMessageBox aMessageBox =
			new UiMessageBox(rParentView,
							 "$tiUiMessageBoxConfirm",
							 new UiImageResource("$imUiMessageBoxConfirm"),
							 sMessage);

		aMessageBox.setButtons(Button.YES_NO);
		aMessageBox.onDialogButton(b ->
					   			{
					   				if (b.isValidated())
					   				{
					   					fRunOnConfirmation.run();
					   				}
								   });
		aMessageBox.show();

		return aMessageBox;
	}

	/***************************************
	 * Displays a message box with an error message and a corresponding icon
	 * (from resource "imUiMessageBoxError") and a single OK button. The view
	 * title will be the resource "tiUiMessageBoxError".
	 *
	 * @param  rParentView The parent view
	 * @param  sMessage    The message text
	 *
	 * @return The message box instance
	 */
	public static UiMessageBox showError(UiView<?> rParentView, String sMessage)
	{
		UiMessageBox aMessageBox =
			new UiMessageBox(rParentView,
							 "$tiUiMessageBoxError",
							 new UiImageResource("$imUiMessageBoxError"),
							 sMessage);

		aMessageBox.setButtons(Button.OK);
		aMessageBox.show();

		return aMessageBox;
	}

	/***************************************
	 * Displays a message box with an information message and a corresponding
	 * icon (from resource "imUiMessageBoxInfo") and a single OK button. The
	 * view title will be the resource "tiUiMessageBoxInfo".
	 *
	 * @param  rParentView The parent view
	 * @param  sMessage    The message text
	 *
	 * @return The message box instance
	 */
	public static UiMessageBox showInfo(UiView<?> rParentView, String sMessage)
	{
		UiMessageBox aMessageBox =
			new UiMessageBox(rParentView,
							 "$tiUiMessageBoxInfo",
							 new UiImageResource("$imUiMessageBoxInfo"),
							 sMessage);

		aMessageBox.setButtons(Button.OK);
		aMessageBox.show();

		return aMessageBox;
	}

	/***************************************
	 * Displays a message box with a warning message that needs to be confirmed,
	 * a corresponding icon (from resource "imUiMessageBoxWarning") and OK and
	 * Cancel buttons. The view title will be the resource
	 * "tiUiMessageBoxWarning".
	 *
	 * @param  rParentView        The parent view
	 * @param  sMessage           The message text
	 * @param  fRunOnConfirmation A runnable that should be executed if the
	 *                            message box is confirmed (i.e. the OK button
	 *                            is pressed)
	 *
	 * @return The message box instance
	 */
	public static UiMessageBox showWarning(UiView<?> rParentView,
										   String    sMessage,
										   Runnable  fRunOnConfirmation)
	{
		UiMessageBox aMessageBox =
			new UiMessageBox(rParentView,
							 "$tiUiMessageBoxWarning",
							 new UiImageResource("$imUiMessageBoxWarning"),
							 sMessage);

		aMessageBox.onDialogButton(b ->
					   			{
					   				if (b.isValidated())
					   				{
					   					fRunOnConfirmation.run();
					   				}
								   });
		aMessageBox.show();

		return aMessageBox;
	}
}
