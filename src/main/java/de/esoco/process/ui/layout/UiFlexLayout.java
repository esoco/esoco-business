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
package de.esoco.process.ui.layout;

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.HasCssName;
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.Orientation;
import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.style.SizeUnit;
import de.esoco.process.ui.style.UiStyle;

/**
 * Places components in a horizontal or vertical flow that can be flexibly
 * controlled by the properties defined by the CSS Flexbox layout.
 *
 * @author eso
 */
public class UiFlexLayout extends UiLayout {

	/**
	 * Enumeration of the component alignments in a CSS Flexbox layout.
	 */
	public enum FlexAlign implements HasCssName {
		START("flex-start"), CENTER("center"), END("flex-end"),
		STRETCH("stretch"), SPACE_BETWEEN("space-between"),
		SPACE_AROUND("space-around"), BASELINE("baseline");

		private final String cssName;

		/**
		 * Creates a new instance.
		 *
		 * @param cssName The CSS name of this alignment
		 */
		FlexAlign(String cssName) {
			this.cssName = cssName;
		}

		/**
		 * Returns the value that corresponds to the given {@link Alignment}.
		 *
		 * @param alignment The alignment to map
		 * @param crossAxis TRUE if the mapping is for the cross-axis
		 * @return The matching instance
		 */
		public static FlexAlign valueOf(Alignment alignment,
			boolean crossAxis) {
			switch (alignment) {
				case BEGIN:
					return START;

				case CENTER:
					return CENTER;

				case END:
					return END;

				case FILL:
					return crossAxis ? STRETCH : SPACE_BETWEEN;

				default:
					return null;
			}
		}

		/**
		 * Returns the CSS name of this alignment.
		 *
		 * @return The CSS name
		 */
		@Override
		public String getCssName() {
			return cssName;
		}
	}

	/**
	 * Enumeration of the wrapping options in a CSS Flexbox layout.
	 */
	public enum FlexWrap implements HasCssName {
		NONE("nowrap"), WRAP("wrap"), REVERSE("wrap-reverse");

		private final String cssName;

		/**
		 * Creates a new instance.
		 *
		 * @param cssName The CSS name
		 */
		FlexWrap(String cssName) {
			this.cssName = cssName;
		}

		/**
		 * Returns the CSS name of this alignment.
		 *
		 * @return The CSS name of this option
		 */
		@Override
		public String getCssName() {
			return cssName;
		}
	}

	private final Orientation direction;

	private final boolean reverse;

	private FlexAlign justifyContent = null;

	private FlexAlign alignContent = null;

	private FlexAlign alignItems = null;

	private FlexWrap wrap = null;

	/**
	 * Creates a new instance with a horizontal flow direction.
	 */
	public UiFlexLayout() {
		this(Orientation.HORIZONTAL);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param direction The direction of the layout
	 */
	public UiFlexLayout(Orientation direction) {
		this(direction, false);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param direction The direction of the layout
	 * @param reverse   TRUE to reverse the flow along the given direction
	 */
	public UiFlexLayout(Orientation direction, boolean reverse) {
		super(LayoutType.FLEX);

		this.direction = direction;
		this.reverse = reverse;
	}

	/**
	 * Sets the alignment of the layout elements along the axis
	 * perpendicular to
	 * the layout flow. The default value if not set is
	 * {@link FlexAlign#STRETCH}. The alignment
	 * {@link FlexAlign#BASELINE BASELINE} is not supported for the content,
	 * only for {@link #alignItems(FlexAlign)}.
	 *
	 * @param align The cross-axis alignment
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout alignContent(FlexAlign align) {
		alignContent = align;

		return this;
	}

	/**
	 * Sets the horizontal alignment of elements. Overridden to map on either
	 * {@link #justifyContent(FlexAlign)} or {@link #alignContent(FlexAlign)}
	 * depending on the layout direction. These methods should be preferred for
	 * better readability and because they support additional Flexbox
	 * alignments.
	 *
	 * @param align The horizontal alignment
	 * @return This instance for fluent invocation
	 */
	@Override
	public UiFlexLayout alignHorizontal(Alignment align) {
		return align(Orientation.HORIZONTAL, align);
	}

	/**
	 * Sets the alignment of layout elements in their respective layout cell
	 * along the axis perpendicular to the layout flow. The default value if
	 * not
	 * set is {@link FlexAlign#STRETCH}. This value only has an effect that
	 * differs from the value set on {@link #alignContent(FlexAlign)} if
	 * wrapping is enabled through {@link #wrap()}. If not this value takes
	 * precedence over the content alignment although that should not be relied
	 * upon.
	 *
	 * @param align The cross-axis alignment
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout alignItems(FlexAlign align) {
		alignItems = align;

		return this;
	}

	/**
	 * Sets the vertical alignment of elements. Overridden to map on either
	 * {@link #justifyContent(FlexAlign)} or {@link #alignContent(FlexAlign)}
	 * depending on the layout direction. This methods should be preferred for
	 * better readability and because they support additional Flexbox
	 * alignments.
	 *
	 * @param alignment The horizontal alignment
	 * @return This instance for fluent invocation
	 */
	@Override
	public UiFlexLayout alignVertical(Alignment alignment) {
		return align(Orientation.VERTICAL, alignment);
	}

	/**
	 * Sets the positioning of all layout elements along the axis of the layout
	 * flow. The default value if not set is {@link FlexAlign#START}. The
	 * alignment values {@link FlexAlign#STRETCH STRETCH} and
	 * {@link FlexAlign#BASELINE BASELINE} are not supported for the main
	 * layout
	 * axis.
	 *
	 * @param align The element alignment
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout justifyContent(FlexAlign align) {
		justifyContent = align;

		return this;
	}

	/**
	 * Enables wrapping for this layout.
	 *
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout noWrap() {
		this.wrap = FlexWrap.NONE;

		return this;
	}

	/**
	 * Enables wrapping for this layout.
	 *
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout wrap() {
		this.wrap = FlexWrap.WRAP;

		return this;
	}

	/**
	 * Enables wrapping for this layout.
	 *
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout wrapReverve() {
		this.wrap = FlexWrap.REVERSE;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyToContainer(UiContainer<?> container) {
		super.applyToContainer(container);

		UiStyle style = container.style();

		if (direction == Orientation.VERTICAL) {
			style.css("flexDirection", reverse ? "column-reverse" : "column");
		} else if (reverse) {
			style.css("flexDirection", "row-reverse");
		}

		style.css("justifyContent", justifyContent);
		style.css("alignContent", alignContent);
		style.css("alignItems", alignItems);
		style.css("flexWrap", wrap);
	}

	/**
	 * Returns a new instance of {@link FlexCell}.
	 *
	 * @see UiLayout#createCell(Row, Column)
	 */
	@Override
	protected Cell createCell(Row row, Column column) {
		return new FlexCell(row, column);
	}

	/**
	 * Internal implementation for {@link #alignHorizontal(Alignment)} and
	 * {@link #alignVertical(Alignment)}.
	 *
	 * @param alignDirection The alignment direction
	 * @param alignment      The alignment
	 * @return This instance for fluent invocation
	 */
	private UiFlexLayout align(Orientation alignDirection,
		Alignment alignment) {
		if (direction == alignDirection) {
			justifyContent(FlexAlign.valueOf(alignment, false));
		} else {
			alignItems(FlexAlign.valueOf(alignment, true));
		}

		return this;
	}

	/**
	 * A {@link UiLayout.Cell} subclass that provides additional access methods
	 * for single Flexbox layout elements.
	 *
	 * @author eso
	 */
	public class FlexCell extends Cell {

		private FlexAlign align = null;

		private String baseSize = null;

		private int grow = -1;

		private int shrink = 0;

		/**
		 * Creates a new instance.
		 *
		 * @param row    The row
		 * @param column the column
		 */
		protected FlexCell(Row row, Column column) {
			super(row, column);
		}

		/**
		 * Sets the alignment of the element in this cell along the axis
		 * perpendicular to the layout flow. This overrides the general item
		 * alignment of the layout.
		 *
		 * @param align The element alignment for this cell
		 * @return This instance for fluent invocation
		 */
		public FlexCell align(FlexAlign align) {
			this.align = align;

			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void applyPropertiesTo(UiComponent<?, ?> component) {
			UiStyle style = component.style();

			super.applyPropertiesTo(component);

			style.css("alignSelf", align);
			style.css("flexBasis", baseSize);

			if (grow >= 0) {
				style.css("flexGrow", Integer.toString(grow));
			}

			if (shrink > 0) {
				style.css("flexShrink", Integer.toString(shrink));
			}
		}

		/**
		 * Sets the base size of the element in this cell along the axis of the
		 * layout flow. This will set the "flex-basis" attribute of the
		 * element.
		 *
		 * @param htmlSize A valid HTML size for Flexbox layouts
		 * @return This instance for fluent invocation
		 */
		public FlexCell baseSize(String htmlSize) {
			baseSize = htmlSize;

			return this;
		}

		/**
		 * Sets the base size of the element in this cell along the axis of the
		 * layout flow.
		 *
		 * @param size The size integer
		 * @param unit The size unit
		 * @return This instance for fluent invocation
		 * @see #baseSize(String)
		 */
		public FlexCell baseSize(int size, SizeUnit unit) {
			return baseSize(unit.getHtmlSize(size));
		}

		/**
		 * Sets the grow factor of this cell for the Flexbox layout
		 * distribution
		 * along the flow axis. The sum of the grow factors of all cells in a
		 * layout represents the total additional layout space that is
		 * available
		 * (exceeding the minimum element sizes). The size of a cell is then
		 * determined by distributing the available space according to the
		 * ratio
		 * of it's grow factor to the sum. A factor of zero stands for the
		 * minimum size of the cell's element.
		 *
		 * @param grow The grow factor (a positive integer or zero)
		 * @return This instance for fluent invocation
		 */
		public FlexCell grow(int grow) {
			this.grow = grow;

			return this;
		}

		/**
		 * Sets the shrink factor of this cell for the Flexbox layout
		 * distribution along the flow axis. Similar to {@link #grow(int)},
		 * this
		 * will reduce the size cells with a high shrink factor more when the
		 * available layout space is reduced.
		 *
		 * @param shrink grow The shrink factor (a positive, non-zero integer)
		 * @return This instance for fluent invocation
		 */
		public FlexCell shrink(int shrink) {
			this.shrink = shrink;

			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void updateFrom(UiComponent<?, ?> component) {
			super.updateFrom(component);

			FlexCell other = component.cell(FlexCell.class);

			align = other.align;
			baseSize = other.baseSize;
			grow = other.grow;
			shrink = other.shrink;
		}
	}
}
