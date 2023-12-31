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
package de.esoco.process;

/**
 * An event handler interface for value updates.
 *
 * @author eso
 */
@FunctionalInterface
public interface ValueEventHandler<T> {

	/**
	 * Will be invoked if a value update has been performed.
	 *
	 * @param rNewValue The updated value
	 * @throws Exception May throw an exception on errors
	 */
	public void handleValueUpdate(T rNewValue) throws Exception;
}
