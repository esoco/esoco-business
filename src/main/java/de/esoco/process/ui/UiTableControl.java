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
package de.esoco.process.ui;

import de.esoco.lib.property.TableStyle;

import static de.esoco.lib.property.StyleProperties.TABLE_STYLE;

/**
 * The base class for UI tables.
 *
 * @author eso
 */
public abstract class UiTableControl<T, C extends UiTableControl<T, C>>
	extends UiControl<T, C> {

	/**
	 * Creates a new instance.
	 *
	 * @param parent   The parent container
	 * @param datatype The value datatype
	 */
	public UiTableControl(UiContainer<?> parent, Class<T> datatype) {
		super(parent, datatype);
	}

	/**
	 * Sets the table style.
	 *
	 * @param style The new table style
	 */
	public void setTableStyle(TableStyle style) {
		tableStyle(style);
	}

	/**
	 * Sets the table style.
	 *
	 * @param style The table style
	 * @return This instance for fluent invocation
	 */
	public C tableStyle(TableStyle style) {
		return set(TABLE_STYLE, style);
	}
}
