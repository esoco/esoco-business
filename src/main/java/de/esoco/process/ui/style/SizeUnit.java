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
package de.esoco.process.ui.style;

/********************************************************************
 * An enumeration of size units that have HTML representations.
 */
public enum SizeUnit
{
	CHAR("em"), FRACTION("fr"), PERCENT("%"), PIXEL("px"), POINT("pt");

	//~ Instance fields --------------------------------------------------------

	private final String sHtmlSizeUnit;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sHtmlSizeUnit The HTML size unit token
	 */
	private SizeUnit(String sHtmlSizeUnit)
	{
		this.sHtmlSizeUnit = sHtmlSizeUnit;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the HTML size unit token.
	 *
	 * @return The token
	 */
	public final String getHtmlSizeUnit()
	{
		return sHtmlSizeUnit;
	}
}
