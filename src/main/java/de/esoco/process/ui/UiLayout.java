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
import java.util.Collections;
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

/**
 * The base class for layouts of process UI {@link UiContainer Containers}. A
 * layout consists of rows and columns which in turn contain layout cells for
 * each component in the container at the respective layout position. If
 * properties are set on either the layout, a row, or a column, they define the
 * default values to be used for the creation of cells in the corresponding
 * element.
 *
 * @author eso
 */
public class UiLayout extends UiLayoutElement<UiLayout> {

	private final LayoutType layoutType;

	private final List<Row> rows = new ArrayList<>();

	private final List<Column> columns = new ArrayList<>();

	private final List<Cell> cells = new ArrayList<>();

	private final Set<PropertyName<?>> ignoredProperties = new HashSet<>();

	private boolean nextRow = false;

	private int currentRow = 0;

	private int nextColumn = 0;

	/**
	 * Creates a new instance with a single column and row (i.e. a single
	 * cell).
	 *
	 * @param layoutType The layout type
	 */
	public UiLayout(LayoutType layoutType) {
		this(layoutType, 1, 1);
	}

	/**
	 * Creates a new instance with a single row.
	 *
	 * @param layoutType The layout type
	 * @param columns    The number of columns in this layout
	 */
	public UiLayout(LayoutType layoutType, int columns) {
		this(layoutType, 1, columns);
	}

	/**
	 * Creates a new instance with a fixed number of columns and rows. All rows
	 * and columns will be created and filled with empty cells (component =
	 * NULL).
	 *
	 * @param layoutType The layout type
	 * @param rows       The number of rows in this layout
	 * @param columns    The number of columns in this layout
	 */
	public UiLayout(LayoutType layoutType, int rows, int columns) {
		this.layoutType = layoutType;

		reset(rows, columns);
	}

	/**
	 * Applies this layout to the given container.
	 *
	 * @param container The container
	 */
	public final void applyTo(UiContainer<?> container) {
		if (isModified()) {
			applyToContainer(container);
			setModified(false);
		}
	}

	/**
	 * Returns the cells in this layout. Their order will follow the layout
	 * structure, i.e. row by row and column after column in the respective
	 * row.
	 * Depending on the layout type cells can be empty, i.e. their component
	 * may
	 * be NULL.
	 *
	 * @return The layout cells
	 */
	public List<Cell> getCells() {
		return cells;
	}

	/**
	 * Returns the columns in this layout.
	 *
	 * @return The column list
	 */
	public List<Column> getColumns() {
		return columns;
	}

	/**
	 * Returns the row in the layout which is used to add new components.
	 *
	 * @return The last row
	 */
	public Row getCurrentRow() {
		return rows.get(currentRow);
	}

	/**
	 * Returns the layout type.
	 *
	 * @return The layout type
	 */
	public final LayoutType getLayoutType() {
		return layoutType;
	}

	/**
	 * Returns the rows in this layout.
	 *
	 * @return The row list
	 */
	public List<Row> getRows() {
		return rows;
	}

	/**
	 * Signals that the next component added to this layout should be placed at
	 * the beginning of the next layout row. The actual effect of this call
	 * depends on the respective layout implementation.
	 */
	public void nextRow() {
		nextRow = true;
	}

	/**
	 * Removes a certain column from this layout. This will also remove all
	 * components in the column cells from their parent container. Attention:
	 * depending on the layout type this call can have unforeseen effects if
	 * the
	 * layout parameters are not adjusted accordingly (e.g. column count).
	 *
	 * @param column The column to remove
	 */
	public void removeColumn(Column column) {
		for (Cell cell : column.getCells()) {
			UiComponent<?, ?> component = cell.getComponent();

			component.getParent().remove(component);
			cell.getRow().getCells().remove(cell);
		}

		columns.remove(column);
	}

	/**
	 * Removes a certain row from this layout. This will also remove all
	 * components in the row cells from their parent container.
	 *
	 * @param row The row to remove
	 */
	public void removeRow(Row row) {
		for (Cell cell : row.getCells()) {
			UiComponent<?, ?> component = cell.getComponent();

			component.getParent().remove(component);
			cell.getColumn().getCells().remove(cell);
		}

		rows.remove(row);
	}

	/**
	 * Internal method to initially add a component to the layout. This will
	 * add
	 * a dummy layout cell to the component that is not positioned in the
	 * layout
	 * but can be used to set layout parameters on the component by querying it
	 * with {@link UiComponent#cell()}. The actual layout is performed by the
	 * method {@link #layoutComponent(UiComponent)}.
	 *
	 * <p>Invoked by {@link UiContainer#addComponent(UiComponent)}.</p>
	 *
	 * @param component The component that has been added to the container
	 */
	protected void addComponent(UiComponent<?, ?> component) {
		Cell initialCell = createCell(null, null);

		initialCell.component = component;
		initialCell.startNewRow = nextRow;
		nextRow = false;

		component.setLayoutCell(initialCell);
	}

	/**
	 * Applies this layout to the given container.
	 *
	 * @param container The container
	 */
	protected void applyToContainer(UiContainer<?> container) {
		nextColumn = 0;
		currentRow = 0;

		for (UiComponent<?, ?> component : container.getComponents()) {
			layoutComponent(component);
		}

		for (Cell cell : cells) {
			cell.checkRepositioning();
		}

		// remove trailing empty rows that may have been created due to
		// cell repositioning
		while (currentRow > 0 && rows.get(currentRow).isEmpty()) {
			rows.remove(currentRow--);
		}

		container.set(LAYOUT, layoutType);
	}

	/**
	 * Creates a new layout cell. Can be overridden by subclasses to create
	 * layout-specific cell types. The default implementation returns an
	 * instance of the inner class {@link Cell}.
	 *
	 * @param row    The row of the cell
	 * @param column The column of the cell
	 * @return The new cell
	 */
	protected Cell createCell(Row row, Column column) {
		return new Cell(row, column);
	}

	/**
	 * Returns the layout cell for a certain component at the next calculated
	 * grid position. Can be overridden by subclasses that need to modify the
	 * grid position, e.g. because of irregular grid structures.
	 *
	 * @param row    The row at which to place the component
	 * @param column The column at which to place the component
	 * @return The layout cell
	 */
	protected Cell getLayoutCell(Row row, Column column) {
		return row.getCell(column.getIndex());
	}

	/**
	 * Returns the next grid column under consideration of column spans that
	 * may
	 * have been set on the cell at the given grid coordinate. This method is
	 * intended to be used by subclasses that allow to define irregular grids.
	 *
	 * @param row    The index of the cell column
	 * @param column The index of the cell row
	 * @return The next row
	 */
	protected Column getNextGridColumn(int row, int column) {
		if (column >= 0) {
			column += columns.get(column).getCell(row).get(COLUMN_SPAN, 1);

			if (column >= columns.size()) {
				column = 0;
				proceedToNextRow();
			}
		} else {
			column = 0;
		}

		return columns.get(column);
	}

	/**
	 * Returns the next grid row under consideration of row spans that may have
	 * been set on the cell at the given grid coordinate. This method is
	 * intended to be used by subclasses that allow to define irregular grids.
	 *
	 * @param row    The index of the cell row
	 * @param column The index of the cell column
	 * @return The next row
	 */
	protected Row getNextGridRow(int row, int column) {
		if (row >= 0) {
			int span = rows.get(row).getCell(column).get(ROW_SPAN, 1);

			row += span;
		} else {
			row = 0;
		}

		currentRow = row;

		while (rows.size() <= row) {
			addRow();
		}

		return rows.get(row);
	}

	/**
	 * Can be invoked by subclasses to exclude certain properties from
	 * application upon components.
	 *
	 * @param properties The properties to ignore
	 */
	protected void ignoreProperties(PropertyName<?>... properties) {
		Collections.addAll(ignoredProperties, properties);
	}

	/**
	 * Checks whether a certain property should be ignored for application on
	 * components.
	 *
	 * @param property The property name
	 * @return TRUE if the property is to be ignored
	 */
	protected boolean isIgnored(PropertyName<?> property) {
		return ignoredProperties.contains(property);
	}

	/**
	 * Internal method to place a component in the layout. Invoked by
	 * {@link UiContainer#applyProperties()}.
	 *
	 * @param component The component to layout
	 */
	protected void layoutComponent(UiComponent<?, ?> component) {
		if (component.cell().startNewRow || nextColumn >= columns.size()) {
			// do not proceed on first layout cell (e.g. if nextRow() has been
			// invoked on empty layout)
			if (currentRow > 0 || nextColumn > 0) {
				proceedToNextRow();
			}
		} else if (nextColumn > 0) {
			component.set(LayoutProperties.SAME_ROW);
		}

		Row row = rows.get(currentRow);
		Column column = columns.get(nextColumn);

		Cell cell = getLayoutCell(row, column);

		cell.updateFrom(component);
		component.setLayoutCell(cell);

		nextColumn = cell.getColumn().getIndex() + 1;
	}

	/**
	 * Resets this layout for recalculation.
	 *
	 * @param rowCount The number of rows
	 * @param colCount The number of column
	 */
	protected void reset(int rowCount, int colCount) {
		setModified(true);
		columns.clear();
		rows.clear();
		cells.clear();

		for (int col = 0; col < colCount; col++) {
			columns.add(new Column(col));
		}

		for (int row = 0; row < rowCount; row++) {
			addRow();
		}
	}

	/**
	 * Adds a new row at the end of the layout grid and fills each column with
	 * an empty cell.
	 */
	private void addRow() {
		Row row = new Row(rows.size());

		rows.add(row);

		for (Column column : columns) {
			cells.add(createCell(row, column));
		}
	}

	/**
	 * Proceeds to the next row in this layout even if the current row hasn't
	 * been filled completely yet. Adds a new row if necessary.
	 */
	private void proceedToNextRow() {
		nextColumn = 0;
		currentRow++;

		if (rows.size() == currentRow) {
			addRow();
		}
	}

	/**
	 * A cell in a layout (at a crossing of a row and a column) that contains a
	 * single component. Depending on the layout type cells can be empty, i.e.
	 * their component may be NULL.
	 *
	 * @author eso
	 */
	public class Cell extends ChildElement<Cell> {

		private final Row row;

		private final Column column;

		private UiComponent<?, ?> component;

		private boolean startNewRow = false;

		private int repositionRow = -1;

		private int repositionColumn = -1;

		/**
		 * Creates a new instance of a cell that is placed at a certain layout
		 * position. These are the actual layout cells while
		 *
		 * @param row    The row of the cell
		 * @param column The column of the cell
		 */
		protected Cell(Row row, Column column) {
			this.row = row;
			this.column = column;

			if (row != null) {
				row.getCells().add(this);
				column.getCells().add(this);
			}
		}

		/**
		 * Overridden to also apply all properties from the hierarchy.
		 *
		 * @see ChildElement#applyPropertiesTo(UiComponent)
		 */
		@Override
		public void applyPropertiesTo(UiComponent<?, ?> component) {
			super.applyPropertiesTo(component);

			row.applyPropertiesTo(component);
			column.applyPropertiesTo(component);
			getLayout().applyPropertiesTo(component);
		}

		/**
		 * Sets the number of columns this cell occupies in it's layout.
		 *
		 * @param columns The number of columns
		 * @return This instance for fluent invocation
		 */
		public Cell colSpan(int columns) {
			return set(COLUMN_SPAN, columns);
		}

		/**
		 * Sets the number of columns this cell occupies as a relative value.
		 *
		 * @param relativeWidth The relative column span
		 * @return This instance for fluent invocation
		 */
		public Cell colSpan(RelativeSize relativeWidth) {
			return colSpan(relativeWidth.calcSize(getColumns().size()));
		}

		/**
		 * Returns the column of this cell. This method in intended for
		 * internal
		 * use by subclasses because it may return NULL before a layout has
		 * been
		 * applied to it's container. It is therefore recommended that
		 * Application code accesses rows and column only through the layout
		 * and
		 * not through the cell returned by a component.
		 *
		 * @return The column
		 */
		public Column getColumn() {
			return column;
		}

		/**
		 * Returns the component that is placed in this cell. The cell of a
		 * layout will only contain a valid component reference after the
		 * layout
		 * has been applied to it's container.
		 *
		 * @return The component in this cell
		 */
		public final UiComponent<?, ?> getComponent() {
			return component;
		}

		/**
		 * Returns the row of this cell. This method in intended for internal
		 * use by subclasses because it may return NULL before a layout has
		 * been
		 * applied to it's container. It is therefore recommended that
		 * Application code accesses rows and column only through the layout
		 * and
		 * not through the cell returned by a component.
		 *
		 * @return The row
		 */
		public Row getRow() {
			return row;
		}

		/**
		 * Sets the height of this cell.
		 *
		 * @param height The height value
		 * @param unit   The height unit
		 * @return This instance for fluent invocation
		 */
		public Cell height(int height, SizeUnit unit) {
			return size(HTML_HEIGHT, height, unit);
		}

		/**
		 * Checks whether this cell is empty (i.e. the component is NULL).
		 *
		 * @return TRUE for an empty cell without a component
		 */
		public final boolean isEmpty() {
			return component == null;
		}

		/**
		 * Sets the grid position at which this cell should be placed.
		 *
		 * @param row    The row index
		 * @param column The column index
		 * @return This instance for fluent invocation
		 */
		public Cell position(int row, int column) {
			repositionRow = row;
			repositionColumn = column;

			return this;
		}

		/**
		 * Sets the UI properties {@link LayoutProperties#SMALL_COLUMN_SPAN}
		 * and
		 * {@link LayoutProperties#MEDIUM_COLUMN_SPAN}.
		 *
		 * @param small  the number of columns to span in small-size layouts
		 * @param medium the number of columns to span in medium-size layouts
		 * @return This instance for fluent invocation
		 */
		public final Cell responsiveColSpans(int small, int medium) {
			return set(SMALL_COLUMN_SPAN, small).set(MEDIUM_COLUMN_SPAN,
				medium);
		}

		/**
		 * Sets the number of columns this cell occupies as a relative value.
		 *
		 * @param relativeHeight relativeWidth The relative column span
		 * @return This instance for fluent invocation
		 */
		public Cell rowSpan(RelativeSize relativeHeight) {
			return rowSpan(relativeHeight.calcSize(getRows().size()));
		}

		/**
		 * Sets the number of rows this cell occupies in it's layout.
		 *
		 * @param rows The number of rows
		 * @return This instance for fluent invocation
		 */
		public Cell rowSpan(int rows) {
			return set(ROW_SPAN, rows);
		}

		/**
		 * Marks a cell to start a new layout row. This can be used as an
		 * alternative to {@link UiLayout#nextRow()}, for example when moving
		 * components with {@link UiComponent#placeBefore(UiComponent)}.
		 *
		 * @return This instance for fluent invocation
		 */
		public Cell startNewRow() {
			startNewRow = true;

			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("boxing")
		public String toString() {
			return String.format("%s(R%d,C%d)", getClass().getSimpleName(),
				row.getIndex(), column.getIndex());
		}

		/**
		 * Sets the relative width of this cell.
		 *
		 * @param width The relative size constant for the cell width
		 * @return This instance for fluent invocation
		 */
		public final Cell width(RelativeSize width) {
			return set(RELATIVE_WIDTH, width);
		}

		/**
		 * Sets the width of this cell.
		 *
		 * @param width The width value
		 * @param unit  The width unit
		 * @return This instance for fluent invocation
		 */
		public Cell width(int width, SizeUnit unit) {
			return size(HTML_WIDTH, width, unit);
		}

		/**
		 * Updates this cell with the data from a certain component's (initial)
		 * layout cell and also sets the component reference.
		 *
		 * @param component The component
		 */
		protected void updateFrom(UiComponent<?, ?> component) {
			Cell componentCell = component.cell();

			this.component = component;
			startNewRow = componentCell.startNewRow;
			repositionRow = componentCell.repositionRow;
			repositionColumn = componentCell.repositionColumn;

			// replace cell properties with the combination of component cell
			// and
			// layout cell, with precedence for component cell styles
			MutableProperties componentCellProperties =
				componentCell.getProperties();

			componentCellProperties.setProperties(getProperties(), false);
			setProperties(componentCellProperties);
		}

		/**
		 * Performs a repositioning of this cell if new layout coordinates had
		 * been set with {@link #position(int, int)}.
		 */
		void checkRepositioning() {
			if (repositionRow >= 0 && repositionColumn >= 0) {
				Cell newCell = getLayout()
					.getRows()
					.get(repositionRow)
					.getCell(repositionColumn);

				if (newCell != this) {
					if (newCell.component != null) {
						throw new IllegalArgumentException(
							"Cell already has component " + newCell.component);
					}

					newCell.component = component;

					newCell.clearProperties();
					newCell.copyPropertiesFrom(this, true);
					clearProperties();

					component.setLayoutCell(newCell);
					component = null;
				}
			}
		}
	}

	/**
	 * A single column in a layout.
	 *
	 * @author eso
	 */
	public class Column extends StructureElement<Column> {

		/**
		 * Creates a new instance.
		 *
		 * @param index The column index
		 */
		public Column(int index) {
			super(index);
		}

		/**
		 * Sets the relative width of this column.
		 *
		 * @param width The relative size constant for the column width
		 * @return This instance for fluent invocation
		 */
		public final Column width(RelativeSize width) {
			return set(RELATIVE_WIDTH, width);
		}

		/**
		 * Sets the width of this column to a certain HTML size value.
		 *
		 * @param htmlWidth The HTML width value
		 * @return This instance for fluent invocation
		 */
		public Column width(String htmlWidth) {
			return set(HTML_WIDTH, htmlWidth);
		}

		/**
		 * Sets the width of this column to a certain value and unit. Which
		 * units are supported depends on the actual layout type used.
		 *
		 * @param width The width value
		 * @param unit  The width unit
		 * @return This instance for fluent invocation
		 */
		public Column width(int width, SizeUnit unit) {
			return size(HTML_WIDTH, width, unit);
		}
	}

	/**
	 * A single row in a layout.
	 *
	 * @author eso
	 */
	public class Row extends StructureElement<Row> {

		/**
		 * Creates a new instance.
		 *
		 * @param index The row index
		 */
		public Row(int index) {
			super(index);
		}

		/**
		 * Sets the height of this row to a certain HTML size value.
		 *
		 * @param htmlHeigth The HTML height value
		 * @return This instance for fluent invocation
		 */
		public Row height(String htmlHeigth) {
			return set(HTML_HEIGHT, htmlHeigth);
		}

		/**
		 * Sets the height of this row to a certain value and unit. Which units
		 * are supported depends on the actual layout type used.
		 *
		 * @param height The height value
		 * @param unit   The height unit
		 * @return This instance for fluent invocation
		 */
		public Row height(int height, SizeUnit unit) {
			return size(HTML_HEIGHT, height, unit);
		}

		/**
		 * Checks whether no cell in this row contain a component.
		 *
		 * @return TRUE if the doesn't contain a component
		 */
		public boolean isEmpty() {
			for (Cell cell : getCells()) {
				if (cell.component != null) {
					return false;
				}
			}

			return true;
		}
	}

	/**
	 * The base class for child elements of layouts.
	 *
	 * @author eso
	 */
	protected abstract class ChildElement<E extends ChildElement<E>>
		extends UiLayoutElement<E> {

		/**
		 * Returns the layout this element belongs to.
		 *
		 * @return The parent layout
		 */
		public final UiLayout getLayout() {
			return UiLayout.this;
		}

		/**
		 * Overridden to also check if the given property is marked to be
		 * ignored by the layout.
		 *
		 * @see UiLayoutElement#applyProperty(UiComponent, PropertyName)
		 */
		@Override
		protected <T> void applyProperty(UiComponent<?, ?> component,
			PropertyName<T> property) {
			if (!getLayout().isIgnored(property)) {
				super.applyProperty(component, property);
			}
		}

		/**
		 * Internal method to set a string size property.
		 *
		 * @param sizeProperty The size property
		 * @param size         The size value
		 * @param unit         The size unit
		 * @return This instance to allow fluent invocations
		 */
		E size(PropertyName<String> sizeProperty, int size, SizeUnit unit) {
			return set(sizeProperty, unit.getHtmlSize(size));
		}
	}

	/**
	 * The base class for child elements of layouts that define the layout
	 * structure (columns and rows).
	 *
	 * @author eso
	 */
	protected abstract class StructureElement<E extends StructureElement<E>>
		extends ChildElement<E> {

		private final int index;

		private final UiStyle style = new UiStyle();

		private final List<Cell> cells = new ArrayList<>();

		/**
		 * Creates a new instance.
		 *
		 * @param index The index of the element in the layout structure
		 */
		public StructureElement(int index) {
			this.index = index;
		}

		/**
		 * Overridden to also apply the styles of this instance.
		 *
		 * @see UiElement#applyPropertiesTo(UiComponent)
		 * @see UiLayout#ignoreProperties(PropertyName...)
		 */
		@Override
		public void applyPropertiesTo(UiComponent<?, ?> component) {
			super.applyPropertiesTo(component);

			style.applyPropertiesTo(component);
		}

		/**
		 * Returns the cell at a certain position in this element. All layout
		 * positions will be filled but depending on the layout type cells can
		 * be empty, i.e. their component may be NULL.
		 *
		 * @param index The cell index in this element
		 * @return The list of column cells
		 */
		public final Cell getCell(int index) {
			return cells.get(index);
		}

		/**
		 * Returns the cells in this column. Depending on the layout type cells
		 * can be empty, i.e. their component may be NULL.
		 *
		 * @return The list of column cells
		 */
		public final List<Cell> getCells() {
			return cells;
		}

		/**
		 * Returns the index of this element in the layout structure.
		 *
		 * @return The index
		 */
		public final int getIndex() {
			return index;
		}

		/**
		 * Returns the style object of this component which provides methods to
		 * modify the component's appearance.
		 *
		 * @return The component style
		 */
		public final UiStyle style() {
			return style;
		}

		/**
		 * Sets the style of this component to a copy of an existing style
		 * definition.
		 *
		 * @param style The style object to apply
		 * @return The component style to allow subsequent modifications
		 */
		public final UiStyle style(UiStyle style) {
			style = new UiStyle(style);

			return style;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("boxing")
		public String toString() {
			return String.format("%s(%d)", getClass().getSimpleName(),
				getIndex());
		}
	}
}
