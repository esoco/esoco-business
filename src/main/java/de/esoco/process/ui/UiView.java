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

import de.esoco.lib.property.StandardProperties;
import de.esoco.lib.property.TitleAttribute;

/**
 * The base class for top-level UI rendering contexts.
 *
 * @author eso
 */
public abstract class UiView<V extends UiView<V>> extends UiLayoutContainer<V>
	implements TitleAttribute {

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent view
	 * @param layout The view layout
	 */
	public UiView(UiView<?> parent, UiLayout layout) {
		super(parent, layout);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle() {
		return get(StandardProperties.TITLE);
	}

	/**
	 * Overridden to always throw an {@link UnsupportedOperationException} as
	 * this functionality is not possible for views.
	 *
	 * @see de.esoco.process.ui.UiComponent#placeBefore(UiComponent)
	 */
	@Override
	public V placeBefore(UiComponent<?, ?> beforeComponent) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTitle(String title) {
		title(title);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract V setVisible(boolean visible);

	/**
	 * Fluent variant of {@link #setTitle(String)}.
	 *
	 * @param title The view title
	 * @return This instance
	 */
	public V title(String title) {
		return set(StandardProperties.TITLE, title);
	}
}
