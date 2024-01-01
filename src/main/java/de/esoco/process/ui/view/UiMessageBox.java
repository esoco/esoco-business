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

/**
 * A dialog view for message boxes that display an image or icon and a message
 * string. By default the message box has OK and Cancel buttons but that be
 * changed by invoking
 *
 * @author eso
 */
public class UiMessageBox extends UiDialogView<UiMessageBox> {

	/**
	 * Creates a new instance.
	 *
	 * @param parent  The parent view
	 * @param title   The message box title
	 * @param image   The image
	 * @param message The message
	 */
	public UiMessageBox(UiView<?> parent, String title,
		UiImageDefinition<?> image, String message) {
		super(parent, title, new UiFlowLayout(), true);

		UiBuilder<?> messageBuilder =
			builder().addPanel(new UiFlowLayout()).builder();

		messageBuilder.addImage(image).style().styleName("UiMessageBoxIcon");
		messageBuilder
			.addLabel(message)
			.style()
			.styleName("UiMessageBoxMessage");
	}

	/**
	 * Displays a message box with a message to be confirmed, a corresponding
	 * icon (from resource "imUiMessageBoxConfirm") and YES and No buttons. The
	 * view title will be the resource "tiUiMessageBoxConfirm".
	 *
	 * @param parentView        The parent view
	 * @param message           The message text
	 * @param runOnConfirmation A runnable that should be executed if the
	 *                          message box is confirmed (i.e. the YES
	 *                          button is
	 *                          pressed)
	 * @return The message box instance
	 */
	public static UiMessageBox showConfirmation(UiView<?> parentView,
		String message, Runnable runOnConfirmation) {
		UiMessageBox messageBox =
			new UiMessageBox(parentView, "$tiUiMessageBoxConfirm",
				new UiImageResource("$imUiMessageBoxConfirm"), message);

		messageBox.setButtons(Button.YES_NO);
		messageBox.onDialogButton(b -> {
			if (b.isValidated()) {
				runOnConfirmation.run();
			}
		});
		messageBox.show();

		return messageBox;
	}

	/**
	 * Displays a message box with an error message and a corresponding icon
	 * (from resource "imUiMessageBoxError") and a single OK button. The view
	 * title will be the resource "tiUiMessageBoxError".
	 *
	 * @param parentView The parent view
	 * @param message    The message text
	 * @return The message box instance
	 */
	public static UiMessageBox showError(UiView<?> parentView,
		String message) {
		UiMessageBox messageBox =
			new UiMessageBox(parentView, "$tiUiMessageBoxError",
				new UiImageResource("$imUiMessageBoxError"), message);

		messageBox.setButtons(Button.OK);
		messageBox.show();

		return messageBox;
	}

	/**
	 * Displays a message box with an information message and a corresponding
	 * icon (from resource "imUiMessageBoxInfo") and a single OK button. The
	 * view title will be the resource "tiUiMessageBoxInfo".
	 *
	 * @param parentView The parent view
	 * @param message    The message text
	 * @return The message box instance
	 */
	public static UiMessageBox showInfo(UiView<?> parentView, String message) {
		UiMessageBox messageBox =
			new UiMessageBox(parentView, "$tiUiMessageBoxInfo",
				new UiImageResource("$imUiMessageBoxInfo"), message);

		messageBox.setButtons(Button.OK);
		messageBox.show();

		return messageBox;
	}

	/**
	 * Displays a message box with a warning message that needs to be
	 * confirmed,
	 * a corresponding icon (from resource "imUiMessageBoxWarning") and OK and
	 * Cancel buttons. The view title will be the resource
	 * "tiUiMessageBoxWarning".
	 *
	 * @param parentView        The parent view
	 * @param message           The message text
	 * @param runOnConfirmation A runnable that should be executed if the
	 *                          message box is confirmed (i.e. the OK button is
	 *                          pressed)
	 * @return The message box instance
	 */
	public static UiMessageBox showWarning(UiView<?> parentView,
		String message,
		Runnable runOnConfirmation) {
		UiMessageBox messageBox =
			new UiMessageBox(parentView, "$tiUiMessageBoxWarning",
				new UiImageResource("$imUiMessageBoxWarning"), message);

		messageBox.onDialogButton(b -> {
			if (b.isValidated()) {
				runOnConfirmation.run();
			}
		});
		messageBox.show();

		return messageBox;
	}
}
