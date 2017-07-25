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


/********************************************************************
 * Places components in a two-dimensional grid structure.
 *
 * @author eso
 */
public class UiGridLayout extends UiLayout
{
	//~ Instance fields --------------------------------------------------------

	private String sColumnGap = null;
	private String sRowGap    = null;

	private Alignment eHorizontalAlignment = null;
	private Alignment eVerticalAlignment   = null;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param nRows    The number of rows in the grid
	 * @param nColumns The number of columns in the grid
	 */
	public UiGridLayout(int nRows, int nColumns)
	{
		super(LayoutType.CSS_GRID, nRows, nColumns);

		ignoreProperties(HTML_WIDTH, HTML_HEIGHT);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the default cell alignments for both directions.
	 *
	 * @param  eHorizontalAlign The horizontal alignment
	 * @param  eVerticalAlign   The vertical alignment
	 *
	 * @return This instance for concatenation
	 */
	public UiGridLayout align(
		Alignment eHorizontalAlign,
		Alignment eVerticalAlign)
	{
		eHorizontalAlignment = eHorizontalAlign;
		eVerticalAlignment   = eVerticalAlign;

		return this;
	}

	/***************************************
	 * Sets the horizontal alignment of layout cells. Overridden to store this
	 * value locally, not in the properties because they must be applied to the
	 * container instead of the child components.
	 *
	 * @param  eAlignment The horizontal alignment
	 *
	 * @return This instance for concatenation
	 */
	@Override
	public UiGridLayout alignHorizontal(Alignment eAlignment)
	{
		eHorizontalAlignment = eAlignment;

		return this;
	}

	/***************************************
	 * Sets the vertical alignment value of layout cells. Overridden to store
	 * this value locally, not in the properties because they must be applied to
	 * the container instead of the child components.
	 *
	 * @param  eAlignment The vertical alignment
	 *
	 * @return This instance for concatenation
	 */
	@Override
	public UiGridLayout alignVertical(Alignment eAlignment)
	{
		eVerticalAlignment = eAlignment;

		return this;
	}

	/***************************************
	 * Sets identical gaps between layout columns and rows. The default is null,
	 * i.e. no gaps.
	 *
	 * @see #gaps(String, String)
	 */
	public UiGridLayout gaps(String sGap)
	{
		return gaps(sGap, sGap);
	}

	/***************************************
	 * Sets the gaps between layout columns and rows. The default is null, i.e.
	 * no gap.
	 *
	 * @param  sColumnGap The column gap in HTML units or NULL for the default
	 * @param  sRowGap    The row gap in HTML units or NULL for the default
	 *
	 * @return This instance for concatenation.
	 */
	public UiGridLayout gaps(String sColumnGap, String sRowGap)
	{
		this.sColumnGap = sColumnGap;
		this.sRowGap    = sRowGap;

		return this;
	}

	/***************************************
	 * Derives the cell-specific CSS grid properties from the layout components
	 * and applies them to the components.
	 */
	protected void applyCellStyles()
	{
		for (Cell rCell : getCells())
		{
			if (!rCell.isEmpty())
			{
				UiStyle rStyle = rCell.getComponent().style();

				applyCellPosition("gridColumn",
								  rCell.getColumn().getIndex(),
								  rCell.get(COLUMN_SPAN, 1),
								  rStyle);
				applyCellPosition("gridRow",
								  rCell.getRow().getIndex(),
								  rCell.get(ROW_SPAN, 1),
								  rStyle);

				applyAlignment("justifySelf",
							   rCell.get(HORIZONTAL_ALIGN, null),
							   rStyle);
				applyAlignment("alignSelf",
							   rCell.get(VERTICAL_ALIGN, null),
							   rStyle);
			}
		}
	}

	/***************************************
	 * Derives the grid structure from the columns and rows of this instance and
	 * applies it to the given style object
	 *
	 * @param rStyle The style object
	 */
	protected void applyGridStructure(UiStyle rStyle)
	{
		StringBuilder sRowsTemplate    = new StringBuilder();
		StringBuilder sColumnsTemplate = new StringBuilder();

		for (Row rRow : getRows())
		{
			sRowsTemplate.append(rRow.get(HTML_HEIGHT, "1fr"));
			sRowsTemplate.append(' ');
		}

		for (Column rColumn : getColumns())
		{
			sColumnsTemplate.append(rColumn.get(HTML_WIDTH, "1fr"));
			sColumnsTemplate.append(' ');
		}

		rStyle.css("display", "grid")
			  .css("gridTemplateRows", sRowsTemplate.toString().trim())
			  .css("gridTemplateColumns", sColumnsTemplate.toString().trim());
	}

	/***************************************
	 * Applies the grid style properties to the given style object.
	 *
	 * @param rStyle The style object
	 */
	protected void applyGridStyle(UiStyle rStyle)
	{
		applyStyle("gridColumnGap", sColumnGap, rStyle);
		applyStyle("gridRowGap", sRowGap, rStyle);
		applyAlignment("justifyItems", eHorizontalAlignment, rStyle);
		applyAlignment("alignItems", eVerticalAlignment, rStyle);
	}

	/***************************************
	 * Overridden to generate the CSS grid layout styles.
	 *
	 * @see UiLayout#applyTo(UiContainer)
	 */
	@Override
	protected void applyTo(UiContainer<?> rContainer)
	{
		super.applyTo(rContainer);

		if (getCurrentRow().isEmpty())
		{
			removeLastRow();
		}

		UiStyle rStyle = rContainer.style();

		applyGridStructure(rStyle);
		applyGridStyle(rStyle);

		applyCellStyles();
	}

	/***************************************
	 * Overridden to modify the placement of cells with preceding cells that
	 * have row and/or column spans different than 1.
	 *
	 * @see UiLayout#getLayoutCell(Row, Column)
	 */
	@Override
	protected Cell getLayoutCell(Row rRow, Column rColumn)
	{
		int nRow = rRow.getIndex();
		int nCol = rColumn.getIndex();

		Column rNextColumn = getNextGridColumn(nRow, nCol - 1);

		if (rNextColumn.getIndex() > nCol - 1)
		{
			nRow -= 1;
		}

		Row rNextRow = getNextGridRow(nRow, rNextColumn.getIndex());

		return super.getLayoutCell(rNextRow, rNextColumn);
	}

	/***************************************
	 * Shortcut method to map and set an alignment if it is not NULL.
	 *
	 * @param sStyleName The alignment style name
	 * @param eAlignment The alignment value or NULL for none
	 * @param rStyle     The style to set the alignment in
	 */
	private final void applyAlignment(String    sStyleName,
									  Alignment eAlignment,
									  UiStyle   rStyle)
	{
		if (eAlignment != null)
		{
			rStyle.css(sStyleName, mapAlignment(eAlignment));
		}
	}

	/***************************************
	 * Applies a cells position and size to a style object.
	 *
	 * @param sStyleName The style name
	 * @param nIndex     The column or row index
	 * @param nSpan      The span size
	 * @param rStyle     The style object to set the span in
	 */
	@SuppressWarnings("boxing")
	private void applyCellPosition(String  sStyleName,
								   int	   nIndex,
								   int	   nSpan,
								   UiStyle rStyle)
	{
		{
			int nGridPos = nIndex + 1;

			if (nSpan > 1)
			{
				rStyle.css(sStyleName,
						   String.format("%d / span %d", nGridPos, nSpan));
			}
			else
			{
				rStyle.css(sStyleName, Integer.toString(nGridPos));
			}
		}
	}

	/***************************************
	 * Sets a style property in the given style if the value isn't NULL.
	 *
	 * @param sStyleName The style name
	 * @param sValue     The style value or NULL for none
	 * @param rStyle     The style to set the property
	 */
	private void applyStyle(String sStyleName, String sValue, UiStyle rStyle)
	{
		if (sValue != null)
		{
			rStyle.css(sStyleName, sValue);
		}
	}

	/***************************************
	 * Maps an alignment value to the correspondings CSS grid layout value.
	 *
	 * @param  eAlignment The alignment to map
	 *
	 * @return The CSS grid alignment value
	 */
	private String mapAlignment(Alignment eAlignment)
	{
		switch (eAlignment)
		{
			case BEGIN:
				return "start";

			case CENTER:
				return "center";

			case END:
				return "end";

			case FILL:
				return "stretch";

			default:
				throw new AssertionError("Undefined: " + eAlignment);
		}
	}
}
