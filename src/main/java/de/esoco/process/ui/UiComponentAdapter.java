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

/**
 * An extension of {@link UiComponentFactory} that also supports the updating of
 * component value. The value update is implemented in the method
 * {@link #updateComponent(UiComponent, Object)}.
 *
 * <p>Implementations of this interface are intended to be stateless so that
 * they can be applied to multiple component instances (hat have been created by
 * the same instance). They should only update components they receive as
 * arguments to {@link #updateComponent(UiComponent, Object)} and not keep any
 * reference to components in fields.</p>
 *
 * @author eso
 */
public interface UiComponentAdapter<T> extends UiComponentFactory {

	/**
	 * Updates a component from the given value.
	 *
	 * @param rComponent The component to be updated
	 * @param rValue     The new value to update the component from
	 */
	public void updateComponent(UiComponent<?, ?> rComponent, T rValue);
}
