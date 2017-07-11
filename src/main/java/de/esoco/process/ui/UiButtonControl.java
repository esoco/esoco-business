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

import de.esoco.process.ui.event.HasActionEvents;

import java.util.Collection;

import static de.esoco.lib.property.LayoutProperties.COLUMNS;


/********************************************************************
 * The base class for button components.
 *
 * @author eso
 */
public abstract class UiButtonControl<T, C extends UiButtonControl<T, C>>
	extends UiControl<T, C> implements HasActionEvents<T, C>
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
