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
package de.esoco.process.ui.event;

import de.esoco.process.ui.UiComponent;

import java.util.function.Consumer;

/**
 * Indicates that a component can provide input focus events.
 *
 * @author eso
 */
public interface UiHasFocusEvents<T, C extends UiComponent<T, ?>> {

	/**
	 * Registers an event handler that will be invoked if a interactive
	 * component loses the input focus.
	 *
	 * @param eventHandler The event handler to be invoked
	 * @return The component the handler has been registered on
	 */
	C onFocusLost(Consumer<T> eventHandler);
}
