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
package de.esoco.process.ui.graphics;

import java.util.function.Function;


/********************************************************************
 * An enumeration of some standard icons that are used internally by the process
 * UI API. Applications must register a mapping to a specific icon library by
 * invoking {@link #registerStandardIconMapping(Function)} to make the standard
 * icons available.
 */
public enum UiStandardIcon implements UiIconSupplier
{
	CLOSE, HELP, INFO, PREVIOUS, NEXT, FIRST_PAGE, LAST_PAGE;

	//~ Static fields/initializers ---------------------------------------------

	private static Function<UiStandardIcon, UiIconSupplier> fIconMapping;

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Registers an icon mapping that provides implementation icons for standard
	 * icon constants.
	 *
	 * @param fMapping The icon mapping function
	 */
	public static void registerStandardIconMapping(
		Function<UiStandardIcon, UiIconSupplier> fMapping)
	{
		fIconMapping = fMapping;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public UiIconDefinition getIcon()
	{
		UiIconDefinition rIcon = null;

		if (fIconMapping != null)
		{
			UiIconSupplier rIconSupplier = fIconMapping.apply(this);

			if (rIconSupplier != null)
			{
				rIcon = rIconSupplier.getIcon();
			}
		}

		if (rIcon == null)
		{
			// if no mapping available try the standard value
			rIcon = getIcon();
		}

		return rIcon;
	}
}
