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
package de.esoco.process.ui.graphics;

import de.esoco.process.ui.UiImageDefinition;

import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.StyleProperties.HAS_IMAGES;


/********************************************************************
 * An image that is defined in the application resource. The resource name is
 * derived from the component's resource ID.
 *
 * @author eso
 */
public class UiImageResource extends UiImageDefinition<UiImageResource>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain resource ID. The argument ID will
	 * override any existing resource ID of component the image is set on. Use
	 * NULL to keep existing IDs.
	 *
	 * @param sResourceId The resource ID
	 */
	public UiImageResource(String sResourceId)
	{
		if (sResourceId != null)
		{
			set(RESOURCE_ID, sResourceId);
		}

		set(HAS_IMAGES);
	}
}
