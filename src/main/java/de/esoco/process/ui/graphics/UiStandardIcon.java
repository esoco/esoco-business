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
package de.esoco.process.ui.graphics;

import java.util.function.Function;

/**
 * An enumeration of some standard icons that are used internally by the process
 * UI API. Applications must register a mapping to a specific icon library by
 * invoking {@link #registerIconMapping(Function)} to make the standard icons
 * available.
 */
public enum UiStandardIcon implements UiIconSupplier {
	ARROW_LEFT, ARROW_RIGHT, ARROW_UP, ARROW_DOWN, LEFT, RIGHT, UP, DOWN,
	PREVIOUS, NEXT, FIRST_PAGE, LAST_PAGE, PLAY, PAUSE, STOP, SKIP_PREVIOUS,
	SKIP_NEXT, FAST_REWIND, FAST_FORWARD, BACKSPACE, CANCEL, CLOSE, DONE,
	ERROR,
	HELP, INFO, WARNING, FILE_DOWNLOAD, FILE_UPLOAD, HISTORY, MENU,
	MORE_HORIZONTAL, MORE_VERTICAL, SEND, SETTINGS, UNDO;

	private static Function<UiStandardIcon, UiIconSupplier> fIconMapping;

	/**
	 * Registers a mapping function that maps implementation icons to standard
	 * icon constants.
	 *
	 * @param fMapping The icon mapping function
	 */
	public static void registerIconMapping(
		Function<UiStandardIcon, UiIconSupplier> fMapping) {
		fIconMapping = fMapping;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UiIconName getIcon() {
		UiIconName rIcon = null;

		if (fIconMapping != null) {
			UiIconSupplier rIconSupplier = fIconMapping.apply(this);

			if (rIconSupplier != null) {
				rIcon = rIconSupplier.getIcon();
			}
		} else {
			assert false :
				"No standard icon mapping available. Icon mapping registered?";
		}

		return rIcon;
	}
}
