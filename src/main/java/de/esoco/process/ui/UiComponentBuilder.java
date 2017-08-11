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

/********************************************************************
 * A factory for UI components that also supports lazy initialization and
 * subsequent updating of the component content. This is handled by the method
 * {@link #updateComponent(Object)} that will be invoked to set the component
 * value. It has an empty default implementation so that this interface can also
 * be used as a functional interface in simple cases.
 *
 * @author eso
 */
@FunctionalInterface
public interface UiComponentBuilder<T>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Creates a new component in the given parent.
	 *
	 * @param rParent The parent container to create the component in
	 * @param rValue  The component value
	 */
	public void buildComponent(UiContainer<?> rParent, T rValue);

	/***************************************
	 * Updates the component from the given value.
	 *
	 * @param rValue The new component value
	 */
	default public void updateComponent(T rValue)
	{
	}
}
