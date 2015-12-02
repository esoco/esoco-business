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

import java.io.Serializable;


/********************************************************************
 * Instances of this interface serve to validate data element values. Validators
 * that contain additional fields must implement the {@link #equals(Object)} and
 * {@link #hashCode()} methods.
 *
 * @author eso
 */
public interface Validator<T> extends Serializable
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Must be implemented to validate data element values.
	 *
	 * @param  rValue The value to validate
	 *
	 * @return TRUE if the value is valid according to this validator's rules
	 */
	public abstract boolean isValid(T rValue);
}
