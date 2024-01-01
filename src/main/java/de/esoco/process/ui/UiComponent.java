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

/**
 * The base class for all process UI components.
 *
 * @author eso
 */
public abstract class UiComponent<T, C extends UiComponent<T, C>>
	extends ParameterWrapper<T, C> {

	private final UiContainer<?> parent;

	private UiLayout.Cell layoutCell;

	private final UiStyle style = new UiStyle();

	private UiImageDefinition<?> image = null;

	/**
	 * Creates a new instance for a certain datatype.
	 *
	 * @param parent   The parent container
	 * @param datatype The datatype of the component value
	 */
	protected UiComponent(UiContainer<?> parent, Class<? super T> datatype) {
		this(parent, parent.fragment(),
			parent.fragment().getTemporaryParameterType(datatype));
	}

	/**
	 * Creates a new instance for a certain parameter relation type.
	 *
	 * @param parent    The parent container
	 * @param fragment  The fragment this component belongs to
	 * @param paramType The parameter relation type
	 */
	protected UiComponent(UiContainer<?> parent, InteractionFragment fragment,
		RelationType<T> paramType) {
		super(fragment, paramType);

		this.parent = parent;

		if (parent != null) {
			attachTo(parent);
		}

		String componentStyle = getComponentStyleName();

		if (componentStyle.length() > 0) {
			style.defaultStyleName(componentStyle);
		}
	}

	/**
	 * Returns the {@link UiLayout} cell in which this component has been
	 * placed. If the parent container has a layout that creates subclasses of
	 * the {@link Cell} class which provide layout-specific methods the
	 * sub-type
	 * cells can be queried with {@link #cell(Class)}.
	 *
	 * @return The layout cell
	 */
	public final Cell cell() {
		return layoutCell;
	}

	/**
	 * Returns the layout cell in which this component has been placed, cast to
	 * a specific sub-type of the {@link Cell} class. The application must make
	 * sure that the given type is actually used by the parent container's
	 * layout or else an exception will occur.
	 *
	 * @param cellType A sub-type of {@link Cell} that must match the actual
	 *                 cell type
	 * @return The layout cell, cast to the given type
	 */
	@SuppressWarnings("hiding")
	public final <C extends Cell> C cell(Class<C> cellType) {
		return cellType.cast(layoutCell);
	}

	/**
	 * Returns the parent container.
	 *
	 * @return The parent
	 */
	public final UiContainer<?> getParent() {
		return parent;
	}

	/**
	 * Returns the root view of this component's hierarchy.
	 *
	 * @return The root view
	 */
	public UiRootView getRootView() {
		UiView<?> view = getView();

		return view instanceof UiRootView ?
		       (UiRootView) view :
		       view.getRootView();
	}

	/**
	 * Returns the parent view of this component.
	 *
	 * @return The parent view
	 */
	public UiView<?> getView() {
		return parent instanceof UiView ? (UiView<?>) parent :
		       parent.getView();
	}

	/**
	 * Sets the height of this component.
	 *
	 * @param height The height value
	 * @param unit   The height unit
	 * @return This instance for concatenation
	 */
	public C height(int height, SizeUnit unit) {
		return size(HTML_HEIGHT, height, unit);
	}

	/**
	 * Sets a label for this component. How exactly the label is rendered and
	 * where it is placed dependend on the parent container and it's layout.
	 *
	 * @param label The label text or NULL for none
	 * @return This instance for concatenation
	 */
	public C label(String label) {
		if (label != null) {
			clear(HIDE_LABEL);
			set(SHOW_LABEL);
		} else {
			set(HIDE_LABEL);
			clear(SHOW_LABEL);
		}

		return set(LABEL, label);
	}

	/**
	 * Places this component before another component in the same parent
	 * container.
	 *
	 * @param beforeComponent The component to place this component before
	 * @return This instance for concatenation
	 * @throws IllegalArgumentException If the given component is not found in
	 *                                  the parent container
	 */
	@SuppressWarnings("unchecked")
	public C placeBefore(UiComponent<?, ?> beforeComponent) {
		parent.placeComponentBefore(beforeComponent, this);

		return (C) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public C resid(String resourceId) {
		if (resourceId != null) {
			if (this instanceof TitleAttribute) {
				((TitleAttribute) this).setTitle("$ti" + resourceId);
			} else {
				set(LABEL, "$lbl" + resourceId);
			}
		}

		return super.resid(resourceId);
	}

	/**
	 * Sets the size of this component.
	 *
	 * @param width  The width
	 * @param height The height
	 * @param unit   The unit of the size values
	 * @return This instance for concatenation
	 */
	public C size(int width, int height, SizeUnit unit) {
		return width(width, unit).height(height, unit);
	}

	/**
	 * Returns the style object of this component which provides methods to
	 * modify the component's appearance.
	 *
	 * @return The component style
	 */
	public UiStyle style() {
		return style;
	}

	/**
	 * Sets the style of this component to a copy of an existing style
	 * definition.
	 *
	 * @param style The style object to apply
	 * @return The component style to allow subsequent modifications
	 */
	public UiStyle style(UiStyle style) {
		style = new UiStyle(style);

		String styleName = style.getStyleName();
		String componentStyle = getComponentStyleName();

		if (componentStyle.length() > 0 &&
			!styleName.startsWith(componentStyle)) {
			style.styleName(componentStyle + " " + styleName);
		}

		return style;
	}

	/**
	 * Shortcut to set the style name in the {@link #style()} object.
	 *
	 * @param styleName the new style name
	 * @return This instance
	 */
	@SuppressWarnings("unchecked")
	public C styleName(String styleName) {
		style().styleName(styleName);

		return (C) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("%s(%s)", getClass().getSimpleName(),
			fragment().getParameter(type()));
	}

	/**
	 * Sets the tooltip to be displayed for this component.
	 *
	 * @param tooltip The tooltip text or NULL for none
	 * @return This instance
	 */
	public C tooltip(String tooltip) {
		return set(ContentProperties.TOOLTIP, tooltip);
	}

	/**
	 * Sets the visibility in responsive layouts.
	 *
	 * @param visibilty The visibility
	 * @return This instance for concatenation
	 */
	public C visibleOn(LayoutVisibility visibilty) {
		return set(LAYOUT_VISIBILITY, visibilty);
	}

	/**
	 * Sets the width of this component.
	 *
	 * @param width The width value
	 * @param unit  The width unit
	 * @return This instance for concatenation
	 */
	public C width(int width, SizeUnit unit) {
		return size(HTML_WIDTH, width, unit);
	}

	/**
	 * Will be invoked to apply all properties of this component to the
	 * corresponding process parameter before it is rendered.
	 */
	protected void applyProperties() {
		style.applyTo(this);

		// is NULL in the root view
		if (layoutCell != null) {
			layoutCell.applyTo(this);
		}

		if (image != null) {
			image.applyTo(this);
		}

		update();
	}

	/**
	 * Attaches this component to it's parent container. This will be invoked
	 * just after the construction of a component instance.
	 *
	 * @param parent The parent container
	 */
	protected void attachTo(UiContainer<?> parent) {
		parent.addComponent(this);

		RelationType<T> paramType = type();
		Class<? super T> datatype = paramType.getTargetType();

		fragment().addDisplayParameters(paramType);

		if (datatype != null && datatype.isEnum()) {
			resid(datatype.getSimpleName());
		}
	}

	/**
	 * Returns the style name for this component. By default this is the simple
	 * class name of this component. Subclasses should override this if the
	 * class name is ambiguous and needs further specification. This is
	 * typically the case for non-static inner classes of composites which
	 * often
	 * have names that start without the 'Ui' prefix.
	 *
	 * @return The component style name
	 */
	protected String getComponentStyleName() {
		return getClass().getSimpleName();
	}

	/**
	 * Returns the image of this component if set.
	 *
	 * @return The component image or NULL for none
	 */
	protected UiImageDefinition<?> getImage() {
		return image;
	}

	/**
	 * Internal method to return the value of this component's parameter. This
	 * is intended to be used by subclasses only which should provide a
	 * type-specific public method (like String getText()).
	 *
	 * @return The current value
	 */
	protected final T getValueImpl() {
		return fragment().getParameter(type());
	}

	/**
	 * Sets an icon for this component. This method is protected to provide the
	 * icon handling functionality for all subclasses. Subclasses that support
	 * the setting of icon should override this method as public.
	 *
	 * @param iconSupplier The component icon (NULL for none)
	 * @return This instance so that this method can be used for fluent
	 * implementations
	 */
	protected C icon(UiIconSupplier iconSupplier) {
		return image(
			iconSupplier != null ? iconSupplier.getIcon().alignRight() : null);
	}

	/**
	 * Sets an image for this component. This method is protected to provide
	 * the
	 * image handling functionality for all subclasses. Subclasses that support
	 * the setting of images should override this method as public.
	 *
	 * @param image The component image
	 * @return This instance so that this method can be used for fluent
	 * implementations
	 */
	@SuppressWarnings("unchecked")
	protected C image(UiImageDefinition<?> image) {
		this.image = image;

		return (C) this;
	}

	/**
	 * Internal method to set the value of this component's parameter. This is
	 * intended to be used by subclasses which should provide a type-specific
	 * public method (like setText(String)).
	 *
	 * @param value The new value
	 * @return This instance so that this method can be used for fluent
	 * implementations
	 */
	@SuppressWarnings("unchecked")
	protected final C setValueImpl(T value) {
		fragment().setParameter(type(), value);

		return (C) this;
	}

	/**
	 * Will be invoked by {@link #applyProperties()} on each UI update. The
	 * default implementation does nothing:
	 */
	protected void update() {
	}

	/**
	 * Internal method to set the layout cell in which this component has been
	 * placed. Will be invoked from
	 * {@link UiLayout#layoutComponent(UiComponent)}.
	 *
	 * @param cell The new layout cell
	 */
	void setLayoutCell(UiLayout.Cell cell) {
		layoutCell = cell;
	}

	/**
	 * Internal method to set the UI properties of this component.
	 *
	 * @param newProperties The new UI properties
	 * @param replace       TRUE to replace existing properties, FALSE to only
	 *                      set new properties
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void setProperties(HasProperties newProperties, boolean replace) {
		if (newProperties.getPropertyCount() > 0) {
			for (PropertyName property : newProperties.getPropertyNames()) {
				set(property, newProperties.getProperty(property, null));
			}
		}
	}

	/**
	 * Internal method to set a string size property.
	 *
	 * @param sizeProperty The size property
	 * @param size         The size value
	 * @param unit         The size unit
	 * @return This instance to allow fluent invocations
	 */
	C size(PropertyName<String> sizeProperty, int size, SizeUnit unit) {
		return set(sizeProperty, unit.getHtmlSize(size));
	}
}
