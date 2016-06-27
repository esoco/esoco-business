//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.process;

/********************************************************************
 * An event handler interface for parameter value updates.
 *
 * @author eso
 */
public interface ParameterEventHandler<T>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Will be invoked if an event occurred for the parameter.
	 *
	 * @param  rParamValue The current parameter value
	 *
	 * @throws ProcessException May throw an exception on errors
	 * @throws Exception        TODO: DOCUMENT ME!
	 */
	public void handleParameterUpdate(T rParamValue) throws Exception;
}
