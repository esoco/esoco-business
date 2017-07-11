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
package de.esoco.process.ui.container;

import de.esoco.lib.property.LayoutType;

import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;

import java.util.List;

import static de.esoco.lib.property.LayoutProperties.SAME_ROW;


/********************************************************************
 * A panel that layouts in a vertically aligned grid. Components are added to
 * the same grid row until {@link #newRow()} is called.
 *
 * @author eso
 */
public class UiGridPanel extends UiContainer<UiGridPanel>
{
	//~ Instance fields --------------------------------------------------------

	private int nRowStart = 0;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 */
	public UiGridPanel(UiContainer<?> rParent)
	{
		super(rParent, new UiLayout(LayoutType.GRID));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Starts a new row.
	 */
	public void newRow()
	{
		List<UiComponent<?, ?>> rComponents = getComponents();
		int					    nCount	    = rComponents.size();

		for (int i = nRowStart + 1; i < nCount; i++)
		{
			rComponents.get(i).set(SAME_ROW);
		}

		nRowStart = nCount;
	}
}
