//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.process.ui.layout;

import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.Orientation;
import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.style.SizeUnit;

import static de.esoco.lib.property.LayoutProperties.HEIGHT;
import static de.esoco.lib.property.LayoutProperties.WIDTH;
import static de.esoco.lib.property.StyleProperties.ORIENTATION;

/**
 * A layout that docks components to one or both sides of a center component,
 * either in horizontal or in vertical direction. The side components must have
 * a corresponding size value (width or height) set because the center component
 * will fill the remaining space.
 *
 * @author eso
 */
public class UiDockLayout extends UiLayout {

	private final Orientation orientation;

	/**
	 * Creates a new instance.
	 *
	 * @param orientation The layout orientation
	 * @param allowResize TRUE to provide controls for the resizing of the
	 *                    components in the layout direction (split panel)
	 */
	public UiDockLayout(Orientation orientation, boolean allowResize) {
		super(allowResize ? LayoutType.SPLIT : LayoutType.DOCK,
			orientation == Orientation.VERTICAL ? 1 : 3);

		this.orientation = orientation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponent(UiComponent<?, ?> component) {
		if (component.getParent().getComponents().size() > 3) {
			throw new IllegalStateException(
				"UiDockLayout can contain a maxium of 3 components");
		}

		super.addComponent(component);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyToContainer(UiContainer<?> container) {
		super.applyToContainer(container);

		if (orientation == Orientation.VERTICAL) {
			container.set(ORIENTATION, orientation);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Cell createCell(Row row, Column column) {
		return new DockCell(row, column);
	}

	/**
	 * A layout cell subclass that maps size values to the integer size fields
	 * needed for the rendering.
	 *
	 * @author eso
	 */
	class DockCell extends Cell {

		/**
		 * Creates a new instance.
		 *
		 * @see Cell#Cell(Row, Column, UiComponent)
		 */
		protected DockCell(Row row, Column column) {
			super(row, column);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Cell height(int height, SizeUnit unit) {
			return set(HEIGHT, height);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Cell width(int width, SizeUnit unit) {
			return set(WIDTH, width);
		}
	}
}
