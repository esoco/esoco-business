//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.property.ContentProperties;
import de.esoco.lib.property.HasProperties;
import de.esoco.lib.property.LayoutVisibility;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.TitleAttribute;

import de.esoco.process.param.ParameterWrapper;
import de.esoco.process.step.InteractionFragment;
import de.esoco.process.ui.UiLayout.Cell;
import de.esoco.process.ui.graphics.UiIconSupplier;
import de.esoco.process.ui.style.SizeUnit;
import de.esoco.process.ui.style.UiStyle;
import de.esoco.process.ui.view.UiRootView;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.ContentProperties.LABEL;
import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.LAYOUT_VISIBILITY;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.SHOW_LABEL;


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
	private UiImageDefinition<?> rImage = null;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain datatype.
	 *
	 * @param rParent   The parent container
	 * @param rDatatype The datatype of the component value
	 */
	protected UiComponent(UiContainer<?> rParent, Class<? super T> rDatatype)
	{
		this(
			rParent,
			rParent.fragment(),
			rParent.fragment().getTemporaryParameterType(rDatatype));
	}

	/***************************************
	 * Creates a new instance for a certain parameter relation type.
	 *
	 * @param rParent    The parent container
	 * @param rFragment  The fragment this component belongs to
	 * @param rParamType The parameter relation type
	 */
	protected UiComponent(UiContainer<?>	  rParent,
						  InteractionFragment rFragment,
						  RelationType<T>	  rParamType)
	{
		super(rFragment, rParamType);

		this.rParent = rParent;

		if (rParent != null)
		{
			attachTo(rParent);
		}

		String sComponentStyle = getComponentStyleName();

		if (sComponentStyle.length() > 0)
		{
			aStyle.defaultStyleName(sComponentStyle);
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the {@link UiLayout} cell in which this component has been
	 * placed. If the parent container has a layout that creates subclasses of
	 * the {@link Cell} class which provide layout-specific methods the sub-type
	 * cells can be queried with {@link #cell(Class)}.
	 *
	 * @return The layout cell
	 */
	public final Cell cell()
	{
		return rLayoutCell;
	}

	/***************************************
	 * Returns the layout cell in which this component has been placed, cast to
	 * a specific sub-type of the {@link Cell} class. The application must make
	 * sure that the given type is actually used by the parent container's
	 * layout or else an exception will occur.
	 *
	 * @param  rCellType A sub-type of {@link Cell} that must match the actual
	 *                   cell type
	 *
	 * @return The layout cell, cast to the given type
	 */
	@SuppressWarnings("hiding")
	public final <C extends Cell> C cell(Class<C> rCellType)
	{
		return rCellType.cast(rLayoutCell);
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
	 * Returns the root view of this component's hierarchy.
	 *
	 * @return The root view
	 */
	public UiRootView getRootView()
	{
		UiView<?> rView = getView();

		return rView instanceof UiRootView ? (UiRootView) rView
										   : rView.getRootView();
	}

	/***************************************
	 * Returns the parent view of this component.
	 *
	 * @return The parent view
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
	 * Sets a label for this component. How exactly the label is rendered and
	 * where it is placed dependend on the parent container and it's layout.
	 *
	 * @param  sLabel The label text or NULL for none
	 *
	 * @return This instance for concatenation
	 */
	public C label(String sLabel)
	{
		if (sLabel != null)
		{
			clear(HIDE_LABEL);
			set(SHOW_LABEL);
		}
		else
		{
			set(HIDE_LABEL);
			clear(SHOW_LABEL);
		}

		return set(LABEL, sLabel);
	}

	/***************************************
	 * Places this component before another component in the same parent
	 * container.
	 *
	 * @param  rBeforeComponent The component to place this component before
	 *
	 * @return This instance for concatenation
	 *
	 * @throws IllegalArgumentException If the given component is not found in
	 *                                  the parent container
	 */
	@SuppressWarnings("unchecked")
	public C placeBefore(UiComponent<?, ?> rBeforeComponent)
	{
		rParent.placeComponentBefore(rBeforeComponent, this);

		return (C) this;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public C resid(String sResourceId)
	{
		if (sResourceId != null)
		{
			if (this instanceof TitleAttribute)
			{
				((TitleAttribute) this).setTitle("$ti" + sResourceId);
			}
			else
			{
				set(LABEL, "$lbl" + sResourceId);
			}
		}

		return super.resid(sResourceId);
	}

	/***************************************
	 * Sets the size of this component.
	 *
	 * @param  nWidth  The width
	 * @param  nHeight The height
	 * @param  eUnit   The unit of the size values
	 *
	 * @return This instance for concatenation
	 */
	public C size(int nWidth, int nHeight, SizeUnit eUnit)
	{
		return width(nWidth, eUnit).height(nHeight, eUnit);
	}

	/***************************************
	 * Returns the style object of this component which provides methods to
	 * modify the component's appearance.
	 *
	 * @return The component style
	 */
	public UiStyle style()
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
	public UiStyle style(UiStyle rStyle)
	{
		aStyle = new UiStyle(rStyle);

		String sStyleName	   = aStyle.getStyleName();
		String sComponentStyle = getComponentStyleName();

		if (sComponentStyle.length() > 0 &&
			!sStyleName.startsWith(sComponentStyle))
		{
			aStyle.styleName(sComponentStyle + " " + sStyleName);
		}

		return aStyle;
	}

	/***************************************
	 * Shortcut to set the style name in the {@link #style()} object.
	 *
	 * @param  sStyleName the new style name
	 *
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public C styleName(String sStyleName)
	{
		style().styleName(sStyleName);

		return (C) this;
	}

	/***************************************
	 * Sets the tooltip to be displayed for this component.
	 *
	 * @param  sTooltip The tooltip text or NULL for none
	 *
	 * @return This instance
	 */
	public C tooltip(String sTooltip)
	{
		return set(ContentProperties.TOOLTIP, sTooltip);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return String.format(
			"%s(%s)",
			getClass().getSimpleName(),
			fragment().getParameter(type()));
	}

	/***************************************
	 * Sets the visibility in responsive layouts.
	 *
	 * @param  eVisibilty The visibility
	 *
	 * @return This instance for concatenation
	 */
	public C visibleOn(LayoutVisibility eVisibilty)
	{
		return set(LAYOUT_VISIBILITY, eVisibilty);
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
	 * corresponding process parameter before it is rendered.
	 */
	protected void applyProperties()
	{
		aStyle.applyTo(this);

		// is NULL in the root view
		if (rLayoutCell != null)
		{
			rLayoutCell.applyTo(this);
		}

		if (rImage != null)
		{
			rImage.applyTo(this);
		}
	}

	/***************************************
	 * Attaches this component to it's parent container. This will be invoked
	 * just after the construction of a component instance.
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
	 * Returns the style name for this component. By default this is the simple
	 * class name of this component. Subclasses should override this if the
	 * class name is ambiguous and needs further specification. This is
	 * typically the case for non-static inner classes of composites which often
	 * have names that start without the 'Ui' prefix.
	 *
	 * @return The component style name
	 */
	protected String getComponentStyleName()
	{
		return getClass().getSimpleName();
	}

	/***************************************
	 * Returns the image of this component if set.
	 *
	 * @return The component image or NULL for none
	 */
	protected UiImageDefinition<?> getImage()
	{
		return rImage;
	}

	/***************************************
	 * Internal method to return the value of this component's parameter. This
	 * is intended to be used by subclasses only which should provide a
	 * type-specific public method (like String getText()).
	 *
	 * @return The current value
	 */
	protected final T getValueImpl()
	{
		return fragment().getParameter(type());
	}

	/***************************************
	 * Sets an icon for this component. This method is protected to provide the
	 * icon handling functionality for all subclasses. Subclasses that support
	 * the setting of icon should override this method as public.
	 *
	 * @param  rIconSupplier The component icon (NULL for none)
	 *
	 * @return This instance so that this method can be used for fluent
	 *         implementations
	 */
	protected C icon(UiIconSupplier rIconSupplier)
	{
		return image(
			rIconSupplier != null ? rIconSupplier.getIcon().alignRight()
								  : null);
	}

	/***************************************
	 * Sets an image for this component. This method is protected to provide the
	 * image handling functionality for all subclasses. Subclasses that support
	 * the setting of images should override this method as public.
	 *
	 * @param  rImage The component image
	 *
	 * @return This instance so that this method can be used for fluent
	 *         implementations
	 */
	@SuppressWarnings("unchecked")
	protected C image(UiImageDefinition<?> rImage)
	{
		this.rImage = rImage;

		return (C) this;
	}

	/***************************************
	 * Internal method to set the value of this component's parameter. This is
	 * intended to be used by subclasses which should provide a type-specific
	 * public method (like setText(String)).
	 *
	 * @param  rValue The new value
	 *
	 * @return This instance so that this method can be used for fluent
	 *         implementations
	 */
	@SuppressWarnings("unchecked")
	protected final C setValueImpl(T rValue)
	{
		fragment().setParameter(type(), rValue);

		return (C) this;
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void setProperties(HasProperties rNewProperties, boolean bReplace)
	{
		if (rNewProperties.getPropertyCount() > 0)
		{
			for (PropertyName rProperty : rNewProperties.getPropertyNames())
			{
				set(rProperty, rNewProperties.getProperty(rProperty, null));
			}
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
