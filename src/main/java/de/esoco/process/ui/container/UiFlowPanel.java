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
package de.esoco.process.ui.container;

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayoutContainer;
import de.esoco.process.ui.layout.UiFlowLayout;

/**
 * A panel that layouts it's children with a {@link UiFlowLayout}.
 *
 * @author eso
 */
public class UiFlowPanel extends UiLayoutContainer<UiFlowPanel> {

	/**
	 * Creates a new instance.
	 *
	 * @param parent The parent container
	 */
	public UiFlowPanel(UiContainer<?> parent) {
		super(parent, new UiFlowLayout());
	}
}
