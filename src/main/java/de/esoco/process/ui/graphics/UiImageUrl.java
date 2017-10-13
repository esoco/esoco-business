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

import static de.esoco.lib.property.ContentProperties.IMAGE;


/********************************************************************
 * An image that is referenced by a URL.
 *
 * @author eso
 */
public class UiImageUrl extends UiImageDefinition<UiImageUrl>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance from a certain URL. The URL can either be
	 * application-relative or absolute, in which case it must start with
	 * http:// (or https://).
	 *
	 * @param sUrl The image URL
	 */
	public UiImageUrl(String sUrl)
	{
		set(IMAGE, "f:" + sUrl);
	}
}
