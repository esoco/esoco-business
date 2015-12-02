//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.data.validate;

import java.util.List;


/********************************************************************
 * An additional interface for validators that constrain their value to a list
 * of values that can be queried with {@link #getValues()}.
 *
 * @author eso
 */
public interface HasValueList<T>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the list of values from which a value can be selected.
	 *
	 * @return The value list
	 */
	public List<T> getValues();
}
