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
package de.esoco.process.ui.component;

import de.esoco.lib.property.ContentType;

import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiContainer;

import static de.esoco.lib.property.ContentProperties.CONTENT_TYPE;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;


/********************************************************************
 * A component that renders a web page at a certain URL.
 *
 * @author eso
 */
public class UiWebView extends UiComponent<String, UiWebView>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent The parent container
	 * @param sUrl    sText The label text
	 */
	public UiWebView(UiContainer<?> rParent, String sUrl)
	{
		super(rParent, String.class);

		setUrl(sUrl);
		set(CONTENT_TYPE, ContentType.WEBSITE);
		set(HIDE_LABEL);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the URL of the displayed web page.
	 *
	 * @return The website URL
	 */
	public String getUrl()
	{
		return getValueImpl();
	}

	/***************************************
	 * Sets the URL of the web page to be displayed.
	 *
	 * @param sUrl The website URL
	 */
	public void setUrl(String sUrl)
	{
		setValueImpl(sUrl);
	}
}
