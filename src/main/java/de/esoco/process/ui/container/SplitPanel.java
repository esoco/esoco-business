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

import de.esoco.lib.property.Orientation;

import de.esoco.process.ui.Container;
import de.esoco.process.ui.Layout;

import static de.esoco.lib.property.StyleProperties.VERTICAL;


/********************************************************************
 * A panel that layouts components so that they can be resized with a split
 * control between them.
 *
 * @author eso
 */
public class SplitPanel extends Container<SplitPanel>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent      The parent container
	 * @param eOrientation bVertical TRUE for vertical orientation
	 */
	public SplitPanel(Container<?> rParent, Orientation eOrientation)
	{
		super(rParent, new Layout(de.esoco.lib.property.LayoutType.SPLIT));

		if (eOrientation == Orientation.VERTICAL)
		{
			set(VERTICAL);
		}
	}
}
