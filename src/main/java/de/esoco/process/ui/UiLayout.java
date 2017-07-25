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

import de.esoco.lib.property.LayoutProperties;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.RelativeSize;

import de.esoco.process.ui.style.SizeUnit;
import de.esoco.process.ui.style.UiStyle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.LAYOUT;
import static de.esoco.lib.property.LayoutProperties.ROW_SPAN;


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
public abstract class UiLayout extends UiLayoutElement<UiLayout>
{
	//~ Instance fields --------------------------------------------------------

	private final LayoutType eLayoutType;

	private int nLastRow    = 0;
	private int nNextColumn = 0;

	private List<Row>    aRows    = new ArrayList<>();
	private List<Column> aColumns = new ArrayList<>();
	private List<Cell>   aCells   = new ArrayList<>();

	private Set<PropertyName<?>> aIgnoredProperties = new HashSet<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a single column and row (i.e. a single cell).
	 *
	 * @param eLayoutType The layout type
	 */
	public UiLayout(LayoutType eLayoutType)
	{
		this(eLayoutType, 1, 1);
	}

	/***************************************
	 * Creates a new instance with a single row.
	 *
	 * @param eLayoutType The layout type
	 * @param nColumns    The number of columns in this layout
	 */
	public UiLayout(LayoutType eLayoutType, int nColumns)
	{
		this(eLayoutType, 1, nColumns);
	}

	/***************************************
	 * Creates a new instance with a fixed number of columns and rows. All rows
	 * and columns will be created and filled with empty cells (component =
	 * NULL).
	 *
	 * @param eLayoutType The layout type
	 * @param nRows       The number of rows in this layout
	 * @param nColumns    The number of columns in this layout
	 */
	public UiLayout(LayoutType eLayoutType, int nRows, int nColumns)
	{
		this.eLayoutType = eLayoutType;

		for (int nCol = 0; nCol < nColumns; nCol++)
		{
			aColumns.add(new Column(nCol));
		}

		for (int nRow = 0; nRow < nRows; nRow++)
		{
			addRow();
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the cells in this layout. Their order will follow the layout
	 * structure, i.e. row by row and column after column in the respective row.
	 * Depending on the layout type cells can be empty, i.e. their component may
	 * be NULL.
	 *
	 * @return The layout cells
	 */
	public List<Cell> getCells()
	{
		return aCells;
	}

	/***************************************
	 * Returns the columns in this layout.
	 *
	 * @return The column list
	 */
	public List<Column> getColumns()
	{
		return aColumns;
	}

	/***************************************
	 * Returns the last row in the layout which is used to add new components.
	 *
	 * @return The last row
	 */
	public Row getLastRow()
	{
		return aRows.get(nLastRow);
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
	 * Returns the rows in this layout.
	 *
	 * @return The row list
	 */
	public List<Row> getRows()
	{
		return aRows;
	}

	/***************************************
	 * Proceeds to the next row in this layout even if the current row hasn't
	 * been filled completely yet. Adds a new row if necessary.
	 */
	public void nextRow()
	{
		nNextColumn = 0;
		nLastRow++;

		if (aRows.size() == nLastRow)
		{
			addRow();
		}
	}

	/***************************************
	 * Applies this layout to the given container.
	 *
	 * @param rContainer The container
	 */
	protected void applyTo(UiContainer<?> rContainer)
	{
		rContainer.set(LAYOUT, eLayoutType);
	}

	/***************************************
	 * Creates a new layout cell. Can be overridden by subclasses to create
	 * layout-specific cell types. The default implementation returns an
	 * instance of the inner class {@link Cell}.
	 *
	 * @param  rRow    The row of the cell
	 * @param  rColumn The column of the cell
	 *
	 * @return The new cell
	 */
	protected Cell createCell(Row rRow, Column rColumn)
	{
		return new Cell(rRow, rColumn, null);
	}

	/***************************************
	 * Returns the layout cell for a certain component at the next calculated
	 * grid position. Can be overridden by subclasses that need to modify the
	 * grid position, e.g. because of irregular grid structures.
	 *
	 * @param  rRow    The row at which to place the component
	 * @param  rColumn The column at which to place the component
	 *
	 * @return The layout cell
	 */
	protected Cell getLayoutCell(Row rRow, Column rColumn)
	{
		return rRow.getCell(rColumn.getIndex());
	}

	/***************************************
	 * Returns the next grid column under consideration of column spans that may
	 * have been set on the cell at the given grid coordinate. This method is
	 * intended to be used by subclasses that allow to define irregular grids.
	 *
	 * @param  nRow    The index of the cell column
	 * @param  nColumn The index of the cell row
	 *
	 * @return The next row
	 */
	protected Column getNextGridColumn(int nRow, int nColumn)
	{
		if (nColumn >= 0)
		{
			nColumn += aColumns.get(nColumn).getCell(nRow).get(COLUMN_SPAN, 1);

			if (nColumn >= aColumns.size())
			{
				nColumn = 0;
				nextRow();
			}
		}
		else
		{
			nColumn = 0;
		}

		return aColumns.get(nColumn);
	}

	/***************************************
	 * Returns the next grid row under consideration of row spans that may have
	 * been set on the cell at the given grid coordinate. This method is
	 * intended to be used by subclasses that allow to define irregular grids.
	 *
	 * @param  nRow    The index of the cell row
	 * @param  nColumn The index of the cell column
	 *
	 * @return The next row
	 */
	protected Row getNextGridRow(int nRow, int nColumn)
	{
		if (nRow >= 0)
		{
			int nSpan = aRows.get(nRow).getCell(nColumn).get(ROW_SPAN, 1);

			nRow += nSpan;
		}
		else
		{
			nRow = 0;
		}

		nLastRow = nRow;

		while (aRows.size() <= nRow)
		{
			addRow();
		}

		return aRows.get(nRow);
	}

	/***************************************
	 * Can be invoked by subclasses to exclude certain properties from
	 * application upon components.
	 *
	 * @param rProperties The properties to ignore
	 */
	protected void ignoreProperties(PropertyName<?>... rProperties)
	{
		for (PropertyName<?> rProperty : rProperties)
		{
			aIgnoredProperties.add(rProperty);
		}
	}

	/***************************************
	 * Checks whether a certain property should be ignored for application on
	 * components.
	 *
	 * @param  rProperty The property name
	 *
	 * @return TRUE if the property is to be ignored
	 */
	protected boolean isIgnored(PropertyName<?> rProperty)
	{
		return aIgnoredProperties.contains(rProperty);
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
		if (nNextColumn >= aColumns.size())
		{
			nextRow();
		}
		else if (nNextColumn > 0)
		{
			rComponent.set(LayoutProperties.SAME_ROW);
		}

		Row    rRow    = aRows.get(nLastRow);
		Column rColumn = aColumns.get(nNextColumn);

		Cell aCell = getLayoutCell(rRow, rColumn);

		nNextColumn = aCell.getColumn().getIndex() + 1;

		aCell.rComponent = rComponent;
		rComponent.setLayoutCell(aCell);

		aCells.add(aCell);
	}

	/***************************************
	 * Removes the last row and decrements the last row pointer. Allows
	 * subclasses that build complex grid structures to remove the last row
	 * while building the grid.
	 */
	protected void removeLastRow()
	{
		aRows.remove(nLastRow--);
	}

	/***************************************
	 * Adds a new row at the end of the layout grid and fills each column with
	 * an empty cell.
	 */
	private void addRow()
	{
		Row aRow = new Row(aRows.size());

		aRows.add(aRow);

		for (Column rColumn : aColumns)
		{
			aCells.add(createCell(aRow, rColumn));
		}
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A cell in a layout (at a crossing of a row and a column) that contains a
	 * single component. Depending on the layout type cells can be empty, i.e.
	 * their component may be NULL.
	 *
	 * @author eso
	 */
	public class Cell extends ChildElement<Cell>
	{
		//~ Instance fields ----------------------------------------------------

		private Row				  rRow;
		private Column			  rColumn;
		private UiComponent<?, ?> rComponent;

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

			rRow.getCells().add(this);
			rColumn.getCells().add(this);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Sets the number of columns this cell occupies in it's layout.
		 *
		 * @param  nColumns The number of columns
		 *
		 * @return This instance for concatenation
		 */
		public Cell colSpan(int nColumns)
		{
			return set(COLUMN_SPAN, nColumns);
		}

		/***************************************
		 * Sets the number of columns this cell occupies as a relative value.
		 *
		 * @param  eRelativeWidth The relative column span
		 *
		 * @return This instance for concatenation
		 */
		public Cell colSpan(RelativeSize eRelativeWidth)
		{
			return colSpan(eRelativeWidth.calcSize(getColumns().size()));
		}

		/***************************************
		 * Returns the column of this cell.
		 *
		 * @return The column
		 */
		public Column getColumn()
		{
			return rColumn;
		}

		/***************************************
		 * Returns the component that is placed in this cell.
		 *
		 * @return The component
		 */
		public final UiComponent<?, ?> getComponent()
		{
			return rComponent;
		}

		/***************************************
		 * Returns the row of this cell.
		 *
		 * @return The row
		 */
		public Row getRow()
		{
			return rRow;
		}

		/***************************************
		 * Returns the row neighbor.
		 *
		 * @param  nDistance The row neighbor
		 *
		 * @return The row neighbor
		 */
		public Cell getRowNeighbor(int nDistance)
		{
			Row rNeighborRow =
				getLayout().getRows().get(rRow.getIndex() + nDistance);

			return rNeighborRow.getCell(rColumn.getIndex());
		}

		/***************************************
		 * Sets the height of this cell.
		 *
		 * @param  nHeight The height value
		 * @param  eUnit   The height unit
		 *
		 * @return This instance for concatenation
		 */
		public Cell height(int nHeight, SizeUnit eUnit)
		{
			return size(HTML_HEIGHT, nHeight, eUnit);
		}

		/***************************************
		 * Checks whether this cell is empty (i.e. the component is NULL).
		 *
		 * @return TRUE for an empty cell without a component
		 */
		public final boolean isEmpty()
		{
			return rComponent == null;
		}

		/***************************************
		 * Sets the grid position at which this cell should be placed.
		 *
		 * @param  nRow    The row index
		 * @param  nColumn The column index
		 *
		 * @return The component's (new) cell for concatenation
		 */
		public Cell position(int nRow, int nColumn)
		{
			Cell rNewCell = getLayout().getRows().get(nRow).getCell(nColumn);

			if (rNewCell != this)
			{
				if (rNewCell.rComponent != null)
				{
					throw new IllegalArgumentException("Cell already has component " +
													   rNewCell.rComponent);
				}

				rNewCell.rComponent = rComponent;

				rNewCell.clearProperties();
				rNewCell.copyPropertiesFrom(this, true);
				clearProperties();

				rComponent.setLayoutCell(rNewCell);
				rComponent = null;
			}

			return rNewCell;
		}

		/***************************************
		 * Sets the number of columns this cell occupies as a relative value.
		 *
		 * @param  eRelativeHeight eRelativeWidth The relative column span
		 *
		 * @return This instance for concatenation
		 */
		public Cell rowSpan(RelativeSize eRelativeHeight)
		{
			return rowSpan(eRelativeHeight.calcSize(getRows().size()));
		}

		/***************************************
		 * Sets the number of rows this cell occupies in it's layout.
		 *
		 * @param  nRows The number of rows
		 *
		 * @return This instance for concatenation
		 */
		public Cell rowSpan(int nRows)
		{
			return set(ROW_SPAN, nRows);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("boxing")
		public String toString()
		{
			return String.format("%s(R%d,C%d)",
								 getClass().getSimpleName(),
								 rRow.getIndex(),
								 rColumn.getIndex());
		}

		/***************************************
		 * Sets the width of this cell.
		 *
		 * @param  nWidth The width value
		 * @param  eUnit  The width unit
		 *
		 * @return This instance for concatenation
		 */
		public Cell width(int nWidth, SizeUnit eUnit)
		{
			return size(HTML_WIDTH, nWidth, eUnit);
		}

		/***************************************
		 * Overridden to also apply the parent properties if no set in the cell.
		 *
		 * @see ChildElement#applyPropertiesTo(UiComponent)
		 */
		@Override
		protected void applyPropertiesTo(UiComponent<?, ?> rComponent)
		{
			super.applyPropertiesTo(rComponent);

			rRow.applyPropertiesTo(rComponent);
			rColumn.applyPropertiesTo(rComponent);
			getLayout().applyPropertiesTo(rComponent);
		}
	}

	/********************************************************************
	 * A single column in a layout.
	 *
	 * @author eso
	 */
	public class Column extends StructureElement<Column>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param nIndex The column index
		 */
		public Column(int nIndex)
		{
			super(nIndex);
		}

		//~ Methods ------------------------------------------------------------

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
			return size(HTML_WIDTH, nWidth, eUnit);
		}
	}

	/********************************************************************
	 * A single row in a layout.
	 *
	 * @author eso
	 */
	public class Row extends StructureElement<Row>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param nIndex The row index
		 */
		public Row(int nIndex)
		{
			super(nIndex);
		}

		//~ Methods ------------------------------------------------------------

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
			return size(HTML_HEIGHT, nHeight, eUnit);
		}

		/***************************************
		 * Checks whether no cell in this row contain a component.
		 *
		 * @return TRUE if the doesn't contain a component
		 */
		public boolean isEmpty()
		{
			for (Cell rCell : getCells())
			{
				if (rCell.rComponent != null)
				{
					return false;
				}
			}

			return true;
		}
	}

	/********************************************************************
	 * The base class for child elements of layouts.
	 *
	 * @author eso
	 */
	protected abstract class ChildElement<E extends ChildElement<E>>
		extends UiLayoutElement<E>
	{
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
		 * Overridden to skip ignored properties.
		 *
		 * @see UiElement#applyPropertiesTo(UiComponent)
		 * @see UiLayout#ignoreProperties(PropertyName...)
		 */
		@Override
		protected void applyPropertiesTo(UiComponent<?, ?> rComponent)
		{
			for (PropertyName<?> rProperty : getProperties().getPropertyNames())
			{
				applyProperty(rComponent, rProperty);
			}
		}

		/***************************************
		 * Internal method to set a string size property.
		 *
		 * @param  rSizeProperty The size property
		 * @param  nSize         The size value
		 * @param  eUnit         The size unit
		 *
		 * @return This instance to allow fluent invocations
		 */
		E size(PropertyName<String> rSizeProperty, int nSize, SizeUnit eUnit)
		{
			return set(rSizeProperty, eUnit.getHtmlSize(nSize));
		}

		/***************************************
		 * Applies a single property to a component if it is not to be ignored.
		 *
		 * @param rComponent The component
		 * @param rProperty  The property
		 */
		private <T> void applyProperty(
			UiComponent<?, ?> rComponent,
			PropertyName<T>   rProperty)
		{
			if (!rComponent.has(rProperty) && !getLayout().isIgnored(rProperty))
			{
				rComponent.set(rProperty, get(rProperty, null));
			}
		}
	}

	/********************************************************************
	 * The base class for child elements of layouts that define the layout
	 * structure (columns and rows).
	 *
	 * @author eso
	 */
	protected abstract class StructureElement<E extends StructureElement<E>>
		extends ChildElement<E>
	{
		//~ Instance fields ----------------------------------------------------

		private final int  nIndex;
		private UiStyle    aStyle = new UiStyle();
		private List<Cell> aCells = new ArrayList<>();

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param nIndex The index of the element in the layout structure
		 */
		public StructureElement(int nIndex)
		{
			this.nIndex = nIndex;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the cell at a certain position in this element. All layout
		 * positions will be filled but depending on the layout type cells can
		 * be empty, i.e. their component may be NULL.
		 *
		 * @param  nIndex The cell index in this element
		 *
		 * @return The list of column cells
		 */
		public final Cell getCell(int nIndex)
		{
			return aCells.get(nIndex);
		}

		/***************************************
		 * Returns the cells in this column. Depending on the layout type cells
		 * can be empty, i.e. their component may be NULL.
		 *
		 * @return The list of column cells
		 */
		public final List<Cell> getCells()
		{
			return aCells;
		}

		/***************************************
		 * Returns the index of this element in the layout structure.
		 *
		 * @return The index
		 */
		public final int getIndex()
		{
			return nIndex;
		}

		/***************************************
		 * Returns the style object of this component which provides methods to
		 * modify the component's appearance.
		 *
		 * @return The component style
		 */
		public final UiStyle style()
		{
			return aStyle;
		}

		/***************************************
		 * Sets the style of this component to a copy of an existing style
		 * definition.
		 *
		 * @param  rStyle The style object to apply
		 *
		 * @return The component style to allow subsequent modifications
		 */
		public final UiStyle style(UiStyle rStyle)
		{
			aStyle = new UiStyle(rStyle);

			return aStyle;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("boxing")
		public String toString()
		{
			return String.format("%s(%d)",
								 getClass().getSimpleName(),
								 getIndex());
		}

		/***************************************
		 * Overridden to also apply the styles of this instance.
		 *
		 * @see UiElement#applyPropertiesTo(UiComponent)
		 * @see UiLayout#ignoreProperties(PropertyName...)
		 */
		@Override
		protected void applyPropertiesTo(UiComponent<?, ?> rComponent)
		{
			super.applyPropertiesTo(rComponent);

			aStyle.applyPropertiesTo(rComponent);
		}
	}
}
