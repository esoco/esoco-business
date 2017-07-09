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
package de.esoco.process.ui.layout;

import de.esoco.process.ui.Component;
import de.esoco.process.ui.Container;


/********************************************************************
 * The base class for layouts of process UI {@link Container Containers}. A
 * layout consists of rows and columns which in turn contain layout cells for
 * each component in the container at the respective layout position. If
 * properties are set on either the layout, a row, or a column, they define the
 * default values for cells at the corresponding location.
 *
 * @author eso
 */
public class Layout extends LayoutElement<Layout>
{
	//~ Instance fields --------------------------------------------------------

	private de.esoco.lib.property.Layout eLayoutType;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param eLayoutType The layout type
	 */
	public Layout(de.esoco.lib.property.Layout eLayoutType)
	{
		this.eLayoutType = eLayoutType;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A single cell in a layout.
	 *
	 * @author eso
	 */
	public class Cell extends ChildElement<Cell>
	{
		//~ Instance fields ----------------------------------------------------

		private Component<?, ?> rComponent;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rComponent The component that has been placed in the layout
		 */
		public Cell(Component<?, ?> rComponent)
		{
			this.rComponent = rComponent;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Sets the width of the component in this cell in HTML units.
		 *
		 * @param  sHeight The HTML width string
		 *
		 * @return This instance for concatenation
		 */
		public Cell height(String sHeight)
		{
			rComponent.height(sHeight);

			return this;
		}

		/***************************************
		 * Sets the width of the component in this cell in HTML units.
		 *
		 * @param  sWidth The HTML width string
		 *
		 * @return This instance for concatenation
		 */
		public Cell width(String sWidth)
		{
			rComponent.width(sWidth);

			return this;
		}
	}

	/********************************************************************
	 * A single column in a layout.
	 *
	 * @author eso
	 */
	public class Column extends ChildElement<Column>
	{
	}

	/********************************************************************
	 * A single row in a layout.
	 *
	 * @author eso
	 */
	public class Row extends ChildElement<Row>
	{
	}

	/********************************************************************
	 * The base class for child elements of layouts.
	 *
	 * @author eso
	 */
	abstract class ChildElement<E extends ChildElement<E>>
		extends LayoutElement<E>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the layout this element belongs to.
		 *
		 * @return The parent layout
		 */
		public final Layout getLayout()
		{
			return Layout.this;
		}
	}
}
