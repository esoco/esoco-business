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

	private Orientation eOrientation;

	/**
	 * Creates a new instance.
	 *
	 * @param eOrientation The layout orientation
	 * @param bAllowResize TRUE to provide controls for the resizing of the
	 *                     components in the layout direction (split panel)
	 */
	public UiDockLayout(Orientation eOrientation, boolean bAllowResize) {
		super(bAllowResize ? LayoutType.SPLIT : LayoutType.DOCK,
			eOrientation == Orientation.VERTICAL ? 1 : 3);

		this.eOrientation = eOrientation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponent(UiComponent<?, ?> rComponent) {
		if (rComponent.getParent().getComponents().size() > 3) {
			throw new IllegalStateException(
				"UiDockLayout can contain a maxium of 3 components");
		}

		super.addComponent(rComponent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyToContainer(UiContainer<?> rContainer) {
		super.applyToContainer(rContainer);

		if (eOrientation == Orientation.VERTICAL) {
			rContainer.set(ORIENTATION, eOrientation);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Cell createCell(Row rRow, Column rColumn) {
		return new DockCell(rRow, rColumn);
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
		protected DockCell(Row rRow, Column rColumn) {
			super(rRow, rColumn);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Cell height(int nHeight, SizeUnit eUnit) {
			return set(HEIGHT, nHeight);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Cell width(int nWidth, SizeUnit eUnit) {
			return set(WIDTH, nWidth);
		}
	}
}
