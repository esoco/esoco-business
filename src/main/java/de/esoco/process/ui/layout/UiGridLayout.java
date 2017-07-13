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

import de.esoco.lib.property.LayoutType;

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;

import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;


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

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param nColumns The number of columns in the grid
	 * @param nRows    The number of rows in the grid
	 */
	public UiGridLayout(int nColumns, int nRows)
	{
		super(LayoutType.CSS_GRID, nColumns, nRows);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the gaps between layout columns and rows. The default is null, i.e.
	 * no gap.
	 *
	 * @param sColumnGap The column gap in HTML units
	 * @param sRowGap    The row gap in HTML units
	 */
	public void setGaps(String sColumnGap, String sRowGap)
	{
		this.sColumnGap = sColumnGap;
		this.sRowGap    = sRowGap;
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

		rContainer.style().css("display", "grid")
				  .css("gridTemplateRows", sRowsTemplate.toString().trim())
				  .css("gridTemplateColumns",
					   sColumnsTemplate.toString().trim());

		if (sColumnGap != null)
		{
			rContainer.style().css("gridColumnGap", sColumnGap);
		}

		if (sRowGap != null)
		{
			rContainer.style().css("gridRowGap", sRowGap);
		}
	}
}
