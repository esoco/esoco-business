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
package de.esoco.process.ui.view;

import de.esoco.process.step.InteractionFragment;
import de.esoco.process.ui.UiBuilder;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.UiView;

/**
 * The root view of an application UI. All other components or the UI must be
 * (direct or indirect) children of a root view. This class is abstract and must
 * be subclassed to provide an implementation of the
 * {@link #buildContent(UiBuilder)} method.
 *
 * @author eso
 */
public abstract class UiRootView extends UiView<UiRootView> {

	/**
	 * Creates a new instance.
	 *
	 * @param fragment The fragment this view shall be rendered in
	 * @param layout   The view layout
	 */
	public UiRootView(InteractionFragment fragment, UiLayout layout) {
		super(null, layout);

		setFragment(fragment);
		setParameterType(fragment.getFragmentParameter());
	}

	/**
	 * Overridden to show or hide this view.
	 *
	 * @see UiContainer#setVisible(boolean)
	 */
	@Override
	public UiRootView setVisible(boolean visible) {
		if (visible) {
			applyProperties();
		}

		return this;
	}

	/**
	 * Overridden to be abstract as it must be implemented.
	 */
	@Override
	protected abstract void buildContent(UiBuilder<?> builder);
}
