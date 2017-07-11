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

import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.RelativeSize;

import de.esoco.process.ui.style.SizeUnit;

import java.util.ArrayList;
import java.util.List;


/********************************************************************
 * The base class for layouts of process UI {@link UiContainer Containers}. A
 * layout consists of rows and columns which in turn contain layout cells for
 * each component in the container at the respective layout position. If
 * properties are set on either the layout, a row, or a column, they define the
 * default values to be used for the creation of cells in the corresponding
 * element.
 *
 * @author eso
 */
public class UiLayout extends UiLayoutElement<UiLayout>
{
	//~ Instance fields --------------------------------------------------------

	private final LayoutType eLayoutType;

	private int nCurrentColumn = 0;

	private List<Column> aColumns = new ArrayList<>();
	private List<Row>    aRows    = new ArrayList<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a single column.
	 *
	 * @param eLayoutType The layout type
	 */
	public UiLayout(LayoutType eLayoutType)
	{
		this(eLayoutType, 1);
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param eLayoutType The layout type
	 * @param nColumns    The number of columns in this layout
	 */
	public UiLayout(LayoutType eLayoutType, int nColumns)
	{
		this.eLayoutType = eLayoutType;

		for (int i = 0; i < nColumns; i++)
		{
			aColumns.add(new Column());
		}

		addRow();
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a new row to this layout.
	 *
	 * @return The new row
	 */
	public Row addRow()
	{
		Row aRow = new Row();

		aRows.add(aRow);

		return aRow;
	}

	/***************************************
	 * Returns a certain column.
	 *
	 * @param  nIndex The column index
	 *
	 * @return The column
	 */
	public Column getColumn(int nIndex)
	{
		return aColumns.get(nIndex);
	}

	/***************************************
	 * Returns the number of columns in this layout.
	 *
	 * @return The column count
	 */
	public int getColumnCount()
	{
		return aColumns.size();
	}

	/***************************************
	 * Returns the layout type.
	 *
	 * @return The layout type
	 */
	public final LayoutType getLayoutType()
	{
		return eLayoutType;
	}

	/***************************************
	 * Returns a certain row.
	 *
	 * @param  nIndex The row index
	 *
	 * @return The row
	 */
	public Row getRow(int nIndex)
	{
		return aRows.get(nIndex);
	}

	/***************************************
	 * Returns the number of rows in this layout.
	 *
	 * @return The row count
	 */
	public int getRowCount()
	{
		return aRows.size();
	}

	/***************************************
	 * Internal method to setup the layout for a component after it has been
	 * added to it's parent container. Invoked by {@link
	 * UiContainer#addComponent(UiComponent)}.
	 *
	 * @param rComponent The component that has been added to the container
	 */
	protected void layoutComponent(UiComponent<?, ?> rComponent)
	{
		Row    rRow  = aRows.get(aRows.size() - 1);
		Column rCol  = aColumns.get(nCurrentColumn++);
		Cell   aCell = new Cell(rRow, rCol, rComponent);

		if (nCurrentColumn >= aColumns.size())
		{
			nCurrentColumn = 0;
		}

		rComponent.setLayoutCell(aCell);

		applyDefaults(rComponent);
		rCol.applyDefaults(rComponent);
		rRow.applyDefaults(rComponent);
	}

	/***************************************
	 * Applies this layout to the given container.
	 *
	 * @param rContainer The container
	 */
	void applyTo(UiContainer<?> rContainer)
	{
		rContainer.layout(eLayoutType);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A cell in a layout (at a crossing of a row and a column) that contains a
	 * single component.
	 *
	 * @author eso
	 */
	public class Cell extends ChildElement<Cell>
	{
		//~ Instance fields ----------------------------------------------------

		private final Row			  rRow;
		private final Column		  rColumn;
		private final UiComponent<?, ?> rComponent;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rRow       The row of the cell
		 * @param rColumn    The column of the cell
		 * @param rComponent The component that has been placed in the layout
		 */
		public Cell(Row rRow, Column rColumn, UiComponent<?, ?> rComponent)
		{
			this.rRow	    = rRow;
			this.rColumn    = rColumn;
			this.rComponent = rComponent;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the column of this cell.
		 *
		 * @return The column
		 */
		public Column column()
		{
			return rColumn;
		}

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
		 * Returns the row of this cell.
		 *
		 * @return The row
		 */
		public Row row()
		{
			return rRow;
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
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Sets the width of this column relative to the total number of
		 * columns.
		 *
		 * @param  eRelativeWidth The relative width
		 *
		 * @return This instance for concatenation
		 */
		public Column width(RelativeSize eRelativeWidth)
		{
			return width(eRelativeWidth.calcSize(getColumnCount()),
						 SizeUnit.FRACTION);
		}

		/***************************************
		 * Sets the width of this column to a certain value and unit. Which
		 * units are supported depends on the actual layout type used.
		 *
		 * @param  nWidth The width value
		 * @param  eUnit  The width unit
		 *
		 * @return This instance for concatenation
		 */
		public Column width(int nWidth, SizeUnit eUnit)
		{
			return size(nWidth, eUnit);
		}
	}

	/********************************************************************
	 * A single row in a layout.
	 *
	 * @author eso
	 */
	public class Row extends ChildElement<Row>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Sets the height of this row relative to the total number of columns.
		 *
		 * @param  eRelativeHeight The relative height
		 *
		 * @return This instance for concatenation
		 */
		public Row height(RelativeSize eRelativeHeight)
		{
			return height(eRelativeHeight.calcSize(getColumnCount()),
						  SizeUnit.FRACTION);
		}

		/***************************************
		 * Sets the height of this row to a certain value and unit. Which units
		 * are supported depends on the actual layout type used.
		 *
		 * @param  nHeight The height value
		 * @param  eUnit   The height unit
		 *
		 * @return This instance for concatenation
		 */
		public Row height(int nHeight, SizeUnit eUnit)
		{
			return size(nHeight, eUnit);
		}
	}

	/********************************************************************
	 * The base class for child elements of layouts.
	 *
	 * @author eso
	 */
	abstract class ChildElement<E extends ChildElement<E>>
		extends UiLayoutElement<E>
	{
		//~ Instance fields ----------------------------------------------------

		private int		 nSize     = 1;
		private SizeUnit eSizeUnit = SizeUnit.FRACTION;

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the layout this element belongs to.
		 *
		 * @return The parent layout
		 */
		public final UiLayout getLayout()
		{
			return UiLayout.this;
		}

		/***************************************
		 * Internal method to set the element size.
		 *
		 * @param  nSize The size value
		 * @param  eUnit The size unit
		 *
		 * @return This instance to allow fluent invocations
		 */
		@SuppressWarnings("unchecked")
		E size(int nSize, SizeUnit eUnit)
		{
			this.nSize     = nSize;
			this.eSizeUnit = eUnit;

			return (E) this;
		}
	}
}
