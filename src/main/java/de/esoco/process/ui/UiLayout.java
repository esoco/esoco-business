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
package de.esoco.process.ui;

import de.esoco.lib.property.LayoutProperties;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.MutableProperties;
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
import static de.esoco.lib.property.LayoutProperties.MEDIUM_COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.RELATIVE_WIDTH;
import static de.esoco.lib.property.LayoutProperties.ROW_SPAN;
import static de.esoco.lib.property.LayoutProperties.SMALL_COLUMN_SPAN;


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

	private boolean bNextRow    = false;
	private int     nCurrentRow = 0;
	private int     nNextColumn = 0;

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

		reset(nRows, nColumns);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Applies this layout to the given container.
	 *
	 * @param rContainer The container
	 */
	public final void applyTo(UiContainer<?> rContainer)
	{
		if (isModified())
		{
			applyToContainer(rContainer);
			setModified(false);
		}
	}

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
	 * Returns the row in the layout which is used to add new components.
	 *
	 * @return The last row
	 */
	public Row getCurrentRow()
	{
		return aRows.get(nCurrentRow);
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
	 * Signals that the next component added to this layout should be placed at
	 * the beginning of the next layout row.
	 */
	public void nextRow()
	{
		bNextRow = true;
	}

	/***************************************
	 * Removes a certain column from this layout. This will also remove all
	 * components in the column cells from their parent container. Attention:
	 * depending on the layout type this call can have unforeseen effects if the
	 * layout parameters are not adjusted accordingly (e.g. column count).
	 *
	 * @param rColumn The column to remove
	 */
	public void removeColumn(Column rColumn)
	{
		for (Cell rCell : rColumn.getCells())
		{
			UiComponent<?, ?> rComponent = rCell.getComponent();

			rComponent.getParent().removeComponent(rComponent);
			rCell.getRow().getCells().remove(rCell);
		}

		aColumns.remove(rColumn);
	}

	/***************************************
	 * Removes a certain row from this layout. This will also remove all
	 * components in the row cells from their parent container.
	 *
	 * @param rRow The row to remove
	 */
	public void removeRow(Row rRow)
	{
		for (Cell rCell : rRow.getCells())
		{
			UiComponent<?, ?> rComponent = rCell.getComponent();

			rComponent.getParent().removeComponent(rComponent);
			rCell.getColumn().getCells().remove(rCell);
		}

		aRows.remove(rRow);
	}

	/***************************************
	 * Internal method to initially add a component to the layout. This will add
	 * a dummy layout cell to the component that is not positioned in the layout
	 * but can be used to set layout parameters on the component by querying it
	 * with {@link UiComponent#cell()}. The actual layout is performed by the
	 * method {@link #layoutComponent(UiComponent)}.
	 *
	 * <p>Invoked by {@link UiContainer#addComponent(UiComponent)}.</p>
	 *
	 * @param rComponent The component that has been added to the container
	 */
	protected void addComponent(UiComponent<?, ?> rComponent)
	{
		Cell aInitialCell = createCell(null, null);

		aInitialCell.rComponent   = rComponent;
		aInitialCell.bStartNewRow = bNextRow;
		bNextRow				  = false;

		rComponent.setLayoutCell(aInitialCell);
	}

	/***************************************
	 * Applies this layout to the given container.
	 *
	 * @param rContainer The container
	 */
	protected void applyToContainer(UiContainer<?> rContainer)
	{
		nNextColumn = 0;
		nCurrentRow = 0;

		for (UiComponent<?, ?> rComponent : rContainer.getComponents())
		{
			layoutComponent(rComponent);
		}

		for (Cell rCell : aCells)
		{
			rCell.checkRepositioning();
		}

		// remove trailing empty rows that may have been created due to
		// cell repositioning
		while (nCurrentRow > 0 && aRows.get(nCurrentRow).isEmpty())
		{
			aRows.remove(nCurrentRow--);
		}

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
		return new Cell(rRow, rColumn);
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
				proceedToNextRow();
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

		nCurrentRow = nRow;

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
	 * Internal method to place a component in the layout. Invoked by {@link
	 * UiContainer#applyProperties()}.
	 *
	 * @param rComponent The component to layout
	 */
	protected void layoutComponent(UiComponent<?, ?> rComponent)
	{
		if (rComponent.cell().bStartNewRow || nNextColumn >= aColumns.size())
		{
			// do not proceed on first layout cell (e.g. if nextRow() has been
			// invoked on empty layout)
			if (nCurrentRow > 0 || nNextColumn > 0)
			{
				proceedToNextRow();
			}
		}
		else if (nNextColumn > 0)
		{
			rComponent.set(LayoutProperties.SAME_ROW);
		}

		Row    rRow    = aRows.get(nCurrentRow);
		Column rColumn = aColumns.get(nNextColumn);

		Cell aCell = getLayoutCell(rRow, rColumn);

		aCell.updateFrom(rComponent);
		rComponent.setLayoutCell(aCell);

		nNextColumn = aCell.getColumn().getIndex() + 1;
	}

	/***************************************
	 * Resets this layout for recalculation.
	 *
	 * @param nRows    The number of rows
	 * @param nColumns The number of column
	 */
	protected void reset(int nRows, int nColumns)
	{
		setModified(true);
		aColumns.clear();
		aRows.clear();
		aCells.clear();

		for (int nCol = 0; nCol < nColumns; nCol++)
		{
			aColumns.add(new Column(nCol));
		}

		for (int nRow = 0; nRow < nRows; nRow++)
		{
			addRow();
		}
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

	/***************************************
	 * Proceeds to the next row in this layout even if the current row hasn't
	 * been filled completely yet. Adds a new row if necessary.
	 */
	private void proceedToNextRow()
	{
		nNextColumn = 0;
		nCurrentRow++;

		if (aRows.size() == nCurrentRow)
		{
			addRow();
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

		private boolean bStartNewRow	  = false;
		private int     nRepositionRow    = -1;
		private int     nRepositionColumn = -1;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance of a cell that is placed at a certain layout
		 * position. These are the actual layout cells while
		 *
		 * @param rRow    The row of the cell
		 * @param rColumn The column of the cell
		 */
		protected Cell(Row rRow, Column rColumn)
		{
			this.rRow    = rRow;
			this.rColumn = rColumn;

			if (rRow != null)
			{
				rRow.getCells().add(this);
				rColumn.getCells().add(this);
			}
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Overridden to also apply all properties from the hierarchy.
		 *
		 * @see ChildElement#applyPropertiesTo(UiComponent)
		 */
		@Override
		public void applyPropertiesTo(UiComponent<?, ?> rComponent)
		{
			super.applyPropertiesTo(rComponent);

			rRow.applyPropertiesTo(rComponent);
			rColumn.applyPropertiesTo(rComponent);
			getLayout().applyPropertiesTo(rComponent);
		}

		/***************************************
		 * Sets the number of columns this cell occupies in it's layout.
		 *
		 * @param  nColumns The number of columns
		 *
		 * @return This instance for fluent invocation
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
		 * @return This instance for fluent invocation
		 */
		public Cell colSpan(RelativeSize eRelativeWidth)
		{
			return colSpan(eRelativeWidth.calcSize(getColumns().size()));
		}

		/***************************************
		 * Returns the column of this cell. This method in intended for internal
		 * use by subclasses because it may return NULL before a layout has been
		 * applied to it's container. It is therefore recommended that
		 * Application code accesses rows and column only through the layout and
		 * not through the cell returned by a component.
		 *
		 * @return The column
		 */
		public Column getColumn()
		{
			return rColumn;
		}

		/***************************************
		 * Returns the component that is placed in this cell. The cell of a
		 * layout will only contain a valid component reference after the layout
		 * has been applied to it's container.
		 *
		 * @return The component in this cell
		 */
		public final UiComponent<?, ?> getComponent()
		{
			return rComponent;
		}

		/***************************************
		 * Returns the row of this cell. This method in intended for internal
		 * use by subclasses because it may return NULL before a layout has been
		 * applied to it's container. It is therefore recommended that
		 * Application code accesses rows and column only through the layout and
		 * not through the cell returned by a component.
		 *
		 * @return The row
		 */
		public Row getRow()
		{
			return rRow;
		}

		/***************************************
		 * Sets the height of this cell.
		 *
		 * @param  nHeight The height value
		 * @param  eUnit   The height unit
		 *
		 * @return This instance for fluent invocation
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
		 * @return This instance for fluent invocation
		 */
		public Cell position(int nRow, int nColumn)
		{
			nRepositionRow    = nRow;
			nRepositionColumn = nColumn;

			return this;
		}

		/***************************************
		 * Sets the UI properties {@link LayoutProperties#SMALL_COLUMN_SPAN} and
		 * {@link LayoutProperties#MEDIUM_COLUMN_SPAN}.
		 *
		 * @param  nSmall  the number of columns to span in small-size layouts
		 * @param  nMedium the number of columns to span in medium-size layouts
		 *
		 * @return This instance for fluent invocation
		 */
		public final Cell responsiveColSpans(int nSmall, int nMedium)
		{
			return set(SMALL_COLUMN_SPAN, nSmall).set(MEDIUM_COLUMN_SPAN,
													  nMedium);
		}

		/***************************************
		 * Sets the number of columns this cell occupies as a relative value.
		 *
		 * @param  eRelativeHeight eRelativeWidth The relative column span
		 *
		 * @return This instance for fluent invocation
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
		 * @return This instance for fluent invocation
		 */
		public Cell rowSpan(int nRows)
		{
			return set(ROW_SPAN, nRows);
		}

		/***************************************
		 * Marks a cell to start a new layout row. This can be used as an
		 * alternative to {@link UiLayout#nextRow()}, for example when moving
		 * components with {@link UiComponent#placeBefore(UiComponent)}.
		 *
		 * @return This instance for fluent invocation
		 */
		public Cell startNewRow()
		{
			bStartNewRow = true;

			return this;
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
		 * Sets the relative width of this cell.
		 *
		 * @param  eWidth The relative size constant for the cell width
		 *
		 * @return This instance for fluent invocation
		 */
		public final Cell width(RelativeSize eWidth)
		{
			return set(RELATIVE_WIDTH, eWidth);
		}

		/***************************************
		 * Sets the width of this cell.
		 *
		 * @param  nWidth The width value
		 * @param  eUnit  The width unit
		 *
		 * @return This instance for fluent invocation
		 */
		public Cell width(int nWidth, SizeUnit eUnit)
		{
			return size(HTML_WIDTH, nWidth, eUnit);
		}

		/***************************************
		 * Updates this cell with the data from a certain component's (initial)
		 * layout cell and also sets the component reference.
		 *
		 * @param rComponent The component
		 */
		protected void updateFrom(UiComponent<?, ?> rComponent)
		{
			Cell rComponentCell = rComponent.cell();

			this.rComponent   = rComponent;
			bStartNewRow	  = rComponentCell.bStartNewRow;
			nRepositionRow    = rComponentCell.nRepositionRow;
			nRepositionColumn = rComponentCell.nRepositionColumn;

			// replace cell properties with the combination of component cell and
			// layout cell, with precedence for component cell styles
			MutableProperties rComponentCellProperties =
				rComponentCell.getProperties();

			rComponentCellProperties.setProperties(getProperties(), false);
			setProperties(rComponentCellProperties);
		}

		/***************************************
		 * Performs a repositioning of this cell if new layout coordinates had
		 * been set with {@link #position(int, int)}.
		 */
		void checkRepositioning()
		{
			if (nRepositionRow >= 0 && nRepositionColumn >= 0)
			{
				Cell rNewCell =
					getLayout().getRows()
							   .get(nRepositionRow)
							   .getCell(nRepositionColumn);

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
			}
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
		 * Sets the relative width of this column.
		 *
		 * @param  eWidth The relative size constant for the column width
		 *
		 * @return This instance for fluent invocation
		 */
		public final Column width(RelativeSize eWidth)
		{
			return set(RELATIVE_WIDTH, eWidth);
		}

		/***************************************
		 * Sets the width of this column to a certain value and unit. Which
		 * units are supported depends on the actual layout type used.
		 *
		 * @param  nWidth The width value
		 * @param  eUnit  The width unit
		 *
		 * @return This instance for fluent invocation
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
		 * @return This instance for fluent invocation
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
		 * Overridden to also check if the given property is marked to be
		 * ignored by the layout.
		 *
		 * @see UiLayoutElement#applyProperty(UiComponent, PropertyName)
		 */
		@Override
		protected <T> void applyProperty(
			UiComponent<?, ?> rComponent,
			PropertyName<T>   rProperty)
		{
			if (!getLayout().isIgnored(rProperty))
			{
				super.applyProperty(rComponent, rProperty);
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
		 * Overridden to also apply the styles of this instance.
		 *
		 * @see UiElement#applyPropertiesTo(UiComponent)
		 * @see UiLayout#ignoreProperties(PropertyName...)
		 */
		@Override
		public void applyPropertiesTo(UiComponent<?, ?> rComponent)
		{
			super.applyPropertiesTo(rComponent);

			aStyle.applyPropertiesTo(rComponent);
		}

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
	}
}
