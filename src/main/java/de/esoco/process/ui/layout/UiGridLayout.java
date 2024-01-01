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

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.LayoutType;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.style.UiStyle;

import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.HORIZONTAL_ALIGN;
import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.ROW_SPAN;
import static de.esoco.lib.property.LayoutProperties.VERTICAL_ALIGN;

/**
 * Places components in a two-dimensional grid structure based on the CSS grid
 * layout type.
 *
 * @author eso
 */
public class UiGridLayout extends UiLayout {

	private String columnGap = null;

	private String rowGap = null;

	private Alignment horizontalAlignment = null;

	private Alignment verticalAlignment = null;

	/**
	 * Creates a new instance.
	 *
	 * @param rows    The number of rows in the grid
	 * @param columns The number of columns in the grid
	 */
	public UiGridLayout(int rows, int columns) {
		super(LayoutType.CSS_GRID, rows, columns);

		ignoreProperties(HTML_WIDTH, HTML_HEIGHT);
	}

	/**
	 * Sets the default cell alignments for both directions.
	 *
	 * @param horizontalAlign The horizontal alignment
	 * @param verticalAlign   The vertical alignment
	 * @return This instance for concatenation
	 */
	public UiGridLayout align(Alignment horizontalAlign,
		Alignment verticalAlign) {
		horizontalAlignment = horizontalAlign;
		verticalAlignment = verticalAlign;

		return this;
	}

	/**
	 * Sets the general horizontal alignment of elements in this layout.
	 * Overridden to store this value in an attribute instead of a property
	 * because it must be mapped to a CSS property.
	 *
	 * @param alignment The horizontal alignment
	 * @return This instance for concatenation
	 */
	@Override
	public UiGridLayout alignHorizontal(Alignment alignment) {
		horizontalAlignment = alignment;

		return this;
	}

	/**
	 * Sets the general vertical alignment of elements in this layout.
	 * Overridden to store this value in an attribute instead of a property
	 * because it must be mapped to a CSS property.
	 *
	 * @param alignment The vertical alignment
	 * @return This instance for concatenation
	 */
	@Override
	public UiGridLayout alignVertical(Alignment alignment) {
		verticalAlignment = alignment;

		return this;
	}

	/**
	 * Sets identical gaps between layout columns and rows. The default is
	 * null,
	 * i.e. no gaps.
	 *
	 * @see #gaps(String, String)
	 */
	public UiGridLayout gaps(String gap) {
		return gaps(gap, gap);
	}

	/**
	 * Sets the gaps between layout columns and rows. The default is null, i.e.
	 * no gap.
	 *
	 * @param columnGap The column gap in HTML units or NULL for the default
	 * @param rowGap    The row gap in HTML units or NULL for the default
	 * @return This instance for concatenation.
	 */
	public UiGridLayout gaps(String columnGap, String rowGap) {
		this.columnGap = columnGap;
		this.rowGap = rowGap;

		return this;
	}

	/**
	 * Overridden to generate the CSS grid layout styles.
	 *
	 * @see UiLayout#applyTo(UiContainer)
	 */
	@Override
	protected void applyToContainer(UiContainer<?> container) {
		super.applyToContainer(container);

		UiStyle style = container.style();

		applyGridStructure(style);
		applyGridStyle(style);

		applyCellStyles();
	}

	/**
	 * Overridden to modify the placement of cells with preceding cells that
	 * have row and/or column spans different than 1.
	 *
	 * @see UiLayout#getLayoutCell(Row, Column)
	 */
	@Override
	protected Cell getLayoutCell(Row row, Column column) {
		int rowNum = row.getIndex();
		int colNum = column.getIndex();

		Column nextColumn = getNextGridColumn(rowNum, colNum - 1);

		if (nextColumn.getIndex() > colNum - 1) {
			rowNum -= 1;
		}

		Row nextRow = getNextGridRow(rowNum, nextColumn.getIndex());

		return super.getLayoutCell(nextRow, nextColumn);
	}

	/**
	 * Shortcut method to map and set an alignment if it is not NULL.
	 *
	 * @param styleName The alignment style name
	 * @param alignment The alignment value or NULL for none
	 * @param style     The style to set the alignment in
	 */
	private final void applyAlignment(String styleName, Alignment alignment,
		UiStyle style) {
		if (alignment != null) {
			style.css(styleName, mapAlignment(alignment));
		}
	}

	/**
	 * Applies a cells position and size to a style object.
	 *
	 * @param styleName The style name
	 * @param index     The column or row index
	 * @param span      The span size
	 * @param style     The style object to set the span in
	 */
	@SuppressWarnings("boxing")
	private void applyCellPosition(String styleName, int index, int span,
		UiStyle style) {
		{
			int gridPos = index + 1;

			if (span > 1) {
				style.css(styleName,
					String.format("%d / span %d", gridPos, span));
			} else {
				style.css(styleName, Integer.toString(gridPos));
			}
		}
	}

	/**
	 * Derives the cell-specific CSS grid properties from the layout components
	 * and applies them to the components.
	 */
	private void applyCellStyles() {
		for (Cell cell : getCells()) {
			if (!cell.isEmpty()) {
				UiStyle style = cell.getComponent().style();

				applyCellPosition("gridColumn", cell.getColumn().getIndex(),
					cell.get(COLUMN_SPAN, 1), style);
				applyCellPosition("gridRow", cell.getRow().getIndex(),
					cell.get(ROW_SPAN, 1), style);

				applyAlignment("justifySelf", cell.get(HORIZONTAL_ALIGN, null),
					style);
				applyAlignment("alignSelf", cell.get(VERTICAL_ALIGN, null),
					style);
			}
		}
	}

	/**
	 * Derives the grid structure from the columns and rows of this instance
	 * and
	 * applies it to the given style object
	 *
	 * @param style The style object
	 */
	private void applyGridStructure(UiStyle style) {
		StringBuilder rowsTemplate = new StringBuilder();
		StringBuilder columnsTemplate = new StringBuilder();

		for (Row row : getRows()) {
			rowsTemplate.append(row.get(HTML_HEIGHT, "auto"));
			rowsTemplate.append(' ');
		}

		for (Column column : getColumns()) {
			columnsTemplate.append(column.get(HTML_WIDTH, "auto"));
			columnsTemplate.append(' ');
		}

		style
			.css("display", "grid")
			.css("gridTemplateRows", rowsTemplate.toString().trim())
			.css("gridTemplateColumns", columnsTemplate.toString().trim());
	}

	/**
	 * Applies the grid style properties to the given style object.
	 *
	 * @param style The style object
	 */
	private void applyGridStyle(UiStyle style) {
		applyStyle("gridColumnGap", columnGap, style);
		applyStyle("gridRowGap", rowGap, style);
		applyAlignment("justifyItems", horizontalAlignment, style);
		applyAlignment("alignItems", verticalAlignment, style);
	}

	/**
	 * Sets a style property in the given style if the value isn't NULL.
	 *
	 * @param styleName The style name
	 * @param value     The style value or NULL for none
	 * @param style     The style to set the property
	 */
	private void applyStyle(String styleName, String value, UiStyle style) {
		if (value != null) {
			style.css(styleName, value);
		}
	}

	/**
	 * Maps an alignment value to the correspondings CSS grid layout value.
	 *
	 * @param alignment The alignment to map
	 * @return The CSS grid alignment value
	 */
	private String mapAlignment(Alignment alignment) {
		switch (alignment) {
			case BEGIN:
				return "start";

			case CENTER:
				return "center";

			case END:
				return "end";

			case FILL:
				return "stretch";

			default:
				throw new AssertionError("Undefined: " + alignment);
		}
	}
}
