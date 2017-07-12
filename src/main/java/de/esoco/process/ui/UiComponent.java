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
package de.esoco.process.ui;

import de.esoco.lib.property.MutableProperties;
import de.esoco.lib.property.PropertyName;

import de.esoco.process.ParameterWrapper;
import de.esoco.process.ui.style.SizeUnit;

import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;


/********************************************************************
 * The base class for all process UI components.
 *
 * @author eso
 */
public abstract class UiComponent<T, C extends UiComponent<T, C>>
	extends ParameterWrapper<T, C>
{
	//~ Instance fields --------------------------------------------------------

	private final UiContainer<?> rParent;
	private UiLayout.Cell		 rLayoutCell;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent   The parent container
	 * @param rDatatype The datatype of the component value
	 */
	public UiComponent(UiContainer<?> rParent, Class<? super T> rDatatype)
	{
		super(rParent != null ? rParent.fragment() : null,
			  rDatatype != null
			  ? rParent.fragment().getTemporaryParameterType(rDatatype) : null);

		this.rParent = rParent;

		if (rParent != null)
		{
			rParent.addComponent(this);

			if (rDatatype != null)
			{
				fragment().addDisplayParameters(type());

				if (rDatatype.isEnum())
				{
					resid(rDatatype.getSimpleName());
				}
			}
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the layout cell in which this component has been placed.
	 *
	 * @return The cell
	 */
	public final UiLayout.Cell cell()
	{
		return rLayoutCell;
	}

	/***************************************
	 * Returns the parent container.
	 *
	 * @return The parent
	 */
	public final UiContainer<?> getParent()
	{
		return rParent;
	}

	/***************************************
	 * Sets the height of this component.
	 *
	 * @param  nHeight The height value
	 * @param  eUnit   The height unit
	 *
	 * @return This instance for concatenation
	 */
	public C height(int nHeight, SizeUnit eUnit)
	{
		return size(HTML_HEIGHT, nHeight, eUnit);
	}

	/***************************************
	 * Sets the width of this component.
	 *
	 * @param  nWidth The width value
	 * @param  eUnit  The width unit
	 *
	 * @return This instance for concatenation
	 */
	public C width(int nWidth, SizeUnit eUnit)
	{
		return size(HTML_WIDTH, nWidth, eUnit);
	}

	/***************************************
	 * Will be invoked to apply all properties of this component to the
	 * corresponding process parameter after it has been attached to the parent
	 * container.
	 */
	protected void applyProperties()
	{
		rLayoutCell.applyPropertiesTo(this);
	}

	/***************************************
	 * Internal method to return the UI properties of this component.
	 *
	 * @return The UI properties or NULL for none
	 */
	MutableProperties getUiProperties()
	{
		return fragment().getUIProperties(type());
	}

	/***************************************
	 * Internal method to set the layout cell in which this component has been
	 * placed. Will be invoked from {@link
	 * UiLayout#layoutComponent(UiComponent)}.
	 *
	 * @param rCell The new layout cell
	 */
	void setLayoutCell(UiLayout.Cell rCell)
	{
		rLayoutCell = rCell;
	}

	/***************************************
	 * Internal method to set a string size property.
	 *
	 * @param  rSizeProperty The size property
	 * @param  nSize         The size value
	 * @param  eUnit         The size unit
	 *
	 * @return This instance to allow fluent invocations
	 */
	C size(PropertyName<String> rSizeProperty, int nSize, SizeUnit eUnit)
	{
		return set(rSizeProperty, eUnit.getHtmlSize(nSize));
	}
}
