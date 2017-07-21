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

import de.esoco.entity.EntityRelationTypes;

import de.esoco.lib.property.HasProperties;
import de.esoco.lib.property.MutableProperties;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.StringProperties;

import de.esoco.process.ParameterWrapper;
import de.esoco.process.ui.style.SizeUnit;
import de.esoco.process.ui.style.UiStyle;

import org.obrel.core.RelationType;

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
	private UiStyle				 aStyle = new UiStyle();

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

		if (rParent != null && rDatatype != null)
		{
			attachTo(rParent);
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
	 * Returns the parent view of this component.
	 *
	 * @return The view of the hierarchy this component is placed in
	 */
	public UiView<?> getView()
	{
		return rParent instanceof UiView ? (UiView<?>) rParent
										 : rParent.getView();
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
	 * Returns the style object of this component which provides methods to
	 * modify the component's appearance.
	 *
	 * @return The component style
	 */
	public final UiStyle style()
	{
		return aStyle;
	}

	/***************************************
	 * Sets the style of this component to a copy of an existing style
	 * definition.
	 *
	 * @param  rStyle The style object to apply
	 *
	 * @return The component style to allow subsequent modifications
	 */
	public final UiStyle style(UiStyle rStyle)
	{
		aStyle = new UiStyle(rStyle);

		return aStyle;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return String.format("%s[%s]",
							 getClass().getSimpleName(),
							 fragment().getParameter(type()));
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
		// is NULL in the root view
		if (rLayoutCell != null)
		{
			rLayoutCell.applyPropertiesTo(this);
		}

		aStyle.applyPropertiesTo(this);
	}

	/***************************************
	 * Attaches this component to it's parent container.
	 *
	 * @param rParent The parent container
	 */
	protected void attachTo(UiContainer<?> rParent)
	{
		rParent.addComponent(this);

		RelationType<T>  rParamType = type();
		Class<? super T> rDatatype  = rParamType.getTargetType();

		fragment().addDisplayParameters(rParamType);

		if (rDatatype.isEnum())
		{
			resid(rDatatype.getSimpleName());
		}
	}

	/***************************************
	 * Returns the value of this component's parameter. This is intended to be
	 * used by subclasses only which should provide a type-specific public
	 * method (like String getText()).
	 *
	 * @return The current value
	 */
	protected final T getValue()
	{
		return fragment().getParameter(type());
	}

	/***************************************
	 * Sets the value of this component's parameter. This is intended to be used
	 * by subclasses only which should provide a type-specific public method
	 * (like setText(String)).
	 *
	 * @param rValue The new value
	 */
	@SuppressWarnings("unchecked")
	protected final void setValue(T rValue)
	{
		fragment().setParameter(type(), rValue);
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
	 * Internal method to set the UI properties of this component.
	 *
	 * @param rNewProperties The new UI properties
	 * @param bReplace       TRUE to replace existing properties, FALSE to only
	 *                       set new properties
	 */
	void setProperties(HasProperties rNewProperties, boolean bReplace)
	{
		MutableProperties rProperties = fragment().getUIProperties(type());

		if (rProperties == null)
		{
			rProperties = new StringProperties(rNewProperties);
			fragment().annotateParameter(type(),
										 null,
										 EntityRelationTypes.DISPLAY_PROPERTIES,
										 rProperties);
		}
		else
		{
			rProperties.setProperties(rNewProperties, bReplace);
		}
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
