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

		private final String sCssName;

		/**
		 * Creates a new instance.
		 *
		 * @param sCssName The CSS name of this alignment
		 */
		private FlexAlign(String sCssName) {
			this.sCssName = sCssName;
		}

		/**
		 * Returns the value that corresponds to the given {@link Alignment}.
		 *
		 * @param eAlignment The alignment to map
		 * @param bCrossAxis TRUE if the mapping is for the cross-axis
		 * @return The matching instance
		 */
		public static FlexAlign valueOf(Alignment eAlignment,
			boolean bCrossAxis) {
			switch (eAlignment) {
				case BEGIN:
					return START;

				case CENTER:
					return CENTER;

				case END:
					return END;

				case FILL:
					return bCrossAxis ? STRETCH : SPACE_BETWEEN;

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
			return sCssName;
		}
	}

	/**
	 * Enumeration of the wrapping options in a CSS Flexbox layout.
	 */
	public enum FlexWrap implements HasCssName {
		NONE("nowrap"), WRAP("wrap"), REVERSE("wrap-reverse");

		private String sCssName;

		/**
		 * Creates a new instance.
		 *
		 * @param sCssName The CSS name
		 */
		private FlexWrap(String sCssName) {
			this.sCssName = sCssName;
		}

		/**
		 * Returns the CSS name of this alignment.
		 *
		 * @return The CSS name of this option
		 */
		@Override
		public String getCssName() {
			return sCssName;
		}
	}

	private Orientation eDirection;

	private boolean bReverse;

	private FlexAlign eJustifyContent = null;

	private FlexAlign eAlignContent = null;

	private FlexAlign eAlignItems = null;

	private FlexWrap eWrap = null;

	/**
	 * Creates a new instance with a horizontal flow direction.
	 */
	public UiFlexLayout() {
		this(Orientation.HORIZONTAL);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param eDirection The direction of the layout
	 */
	public UiFlexLayout(Orientation eDirection) {
		this(eDirection, false);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param eDirection The direction of the layout
	 * @param bReverse   TRUE to reverse the flow along the given direction
	 */
	public UiFlexLayout(Orientation eDirection, boolean bReverse) {
		super(LayoutType.FLEX);

		this.eDirection = eDirection;
		this.bReverse = bReverse;
	}

	/**
	 * Sets the alignment of the layout elements along the axis
	 * perpendicular to
	 * the layout flow. The default value if not set is
	 * {@link FlexAlign#STRETCH}. The alignment
	 * {@link FlexAlign#BASELINE BASELINE} is not supported for the content,
	 * only for {@link #alignItems(FlexAlign)}.
	 *
	 * @param eAlign The cross-axis alignment
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout alignContent(FlexAlign eAlign) {
		eAlignContent = eAlign;

		return this;
	}

	/**
	 * Sets the horizontal alignment of elements. Overridden to map on either
	 * {@link #justifyContent(FlexAlign)} or {@link #alignContent(FlexAlign)}
	 * depending on the layout direction. These methods should be preferred for
	 * better readability and because they support additional Flexbox
	 * alignments.
	 *
	 * @param eAlign The horizontal alignment
	 * @return This instance for fluent invocation
	 */
	@Override
	public UiFlexLayout alignHorizontal(Alignment eAlign) {
		return align(Orientation.HORIZONTAL, eAlign);
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
	 * @param eAlign The cross-axis alignment
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout alignItems(FlexAlign eAlign) {
		eAlignItems = eAlign;

		return this;
	}

	/**
	 * Sets the vertical alignment of elements. Overridden to map on either
	 * {@link #justifyContent(FlexAlign)} or {@link #alignContent(FlexAlign)}
	 * depending on the layout direction. This methods should be preferred for
	 * better readability and because they support additional Flexbox
	 * alignments.
	 *
	 * @param eAlignment The horizontal alignment
	 * @return This instance for fluent invocation
	 */
	@Override
	public UiFlexLayout alignVertical(Alignment eAlignment) {
		return align(Orientation.VERTICAL, eAlignment);
	}

	/**
	 * Sets the positioning of all layout elements along the axis of the layout
	 * flow. The default value if not set is {@link FlexAlign#START}. The
	 * alignment values {@link FlexAlign#STRETCH STRETCH} and
	 * {@link FlexAlign#BASELINE BASELINE} are not supported for the main
	 * layout
	 * axis.
	 *
	 * @param eAlign The element alignment
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout justifyContent(FlexAlign eAlign) {
		eJustifyContent = eAlign;

		return this;
	}

	/**
	 * Enables wrapping for this layout.
	 *
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout noWrap() {
		this.eWrap = FlexWrap.NONE;

		return this;
	}

	/**
	 * Enables wrapping for this layout.
	 *
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout wrap() {
		this.eWrap = FlexWrap.WRAP;

		return this;
	}

	/**
	 * Enables wrapping for this layout.
	 *
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout wrapReverve() {
		this.eWrap = FlexWrap.REVERSE;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyToContainer(UiContainer<?> rContainer) {
		super.applyToContainer(rContainer);

		UiStyle rStyle = rContainer.style();

		if (eDirection == Orientation.VERTICAL) {
			rStyle.css("flexDirection", bReverse ? "column-reverse" :
			                            "column");
		} else if (bReverse) {
			rStyle.css("flexDirection", "row-reverse");
		}

		rStyle.css("justifyContent", eJustifyContent);
		rStyle.css("alignContent", eAlignContent);
		rStyle.css("alignItems", eAlignItems);
		rStyle.css("flexWrap", eWrap);
	}

	/**
	 * Returns a new instance of {@link FlexCell}.
	 *
	 * @see UiLayout#createCell(Row, Column)
	 */
	@Override
	protected Cell createCell(Row rRow, Column rColumn) {
		return new FlexCell(rRow, rColumn);
	}

	/**
	 * Internal implementation for {@link #alignHorizontal(Alignment)} and
	 * {@link #alignVertical(Alignment)}.
	 *
	 * @param eAlignDirection The alignment direction
	 * @param eAlignment      The alignment
	 * @return This instance for fluent invocation
	 */
	private UiFlexLayout align(Orientation eAlignDirection,
		Alignment eAlignment) {
		if (eDirection == eAlignDirection) {
			justifyContent(FlexAlign.valueOf(eAlignment, false));
		} else {
			alignItems(FlexAlign.valueOf(eAlignment, true));
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

		private FlexAlign eAlign = null;

		private String sBaseSize = null;

		private int nGrow = -1;

		private int nShrink = 0;

		/**
		 * Creates a new instance.
		 *
		 * @param rRow    The row
		 * @param rColumn the column
		 */
		protected FlexCell(Row rRow, Column rColumn) {
			super(rRow, rColumn);
		}

		/**
		 * Sets the alignment of the element in this cell along the axis
		 * perpendicular to the layout flow. This overrides the general item
		 * alignment of the layout.
		 *
		 * @param eAlign The element alignment for this cell
		 * @return This instance for fluent invocation
		 */
		public FlexCell align(FlexAlign eAlign) {
			this.eAlign = eAlign;

			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void applyPropertiesTo(UiComponent<?, ?> rComponent) {
			UiStyle rStyle = rComponent.style();

			super.applyPropertiesTo(rComponent);

			rStyle.css("alignSelf", eAlign);
			rStyle.css("flexBasis", sBaseSize);

			if (nGrow >= 0) {
				rStyle.css("flexGrow", Integer.toString(nGrow));
			}

			if (nShrink > 0) {
				rStyle.css("flexShrink", Integer.toString(nShrink));
			}
		}

		/**
		 * Sets the base size of the element in this cell along the axis of the
		 * layout flow. This will set the "flex-basis" attribute of the
		 * element.
		 *
		 * @param sHtmlSize A valid HTML size for Flexbox layouts
		 * @return This instance for fluent invocation
		 */
		public FlexCell baseSize(String sHtmlSize) {
			sBaseSize = sHtmlSize;

			return this;
		}

		/**
		 * Sets the base size of the element in this cell along the axis of the
		 * layout flow.
		 *
		 * @param nSize The size integer
		 * @param eUnit The size unit
		 * @return This instance for fluent invocation
		 * @see #baseSize(String)
		 */
		public FlexCell baseSize(int nSize, SizeUnit eUnit) {
			return baseSize(eUnit.getHtmlSize(nSize));
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
		 * @param nGrow The grow factor (a positive integer or zero)
		 * @return This instance for fluent invocation
		 */
		public FlexCell grow(int nGrow) {
			this.nGrow = nGrow;

			return this;
		}

		/**
		 * Sets the shrink factor of this cell for the Flexbox layout
		 * distribution along the flow axis. Similar to {@link #grow(int)},
		 * this
		 * will reduce the size cells with a high shrink factor more when the
		 * available layout space is reduced.
		 *
		 * @param nShrink nGrow The shrink factor (a positive, non-zero
		 *                integer)
		 * @return This instance for fluent invocation
		 */
		public FlexCell shrink(int nShrink) {
			this.nShrink = nShrink;

			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void updateFrom(UiComponent<?, ?> rComponent) {
			super.updateFrom(rComponent);

			FlexCell rOther = rComponent.cell(FlexCell.class);

			eAlign = rOther.eAlign;
			sBaseSize = rOther.sBaseSize;
			nGrow = rOther.nGrow;
			nShrink = rOther.nShrink;
		}
	}
}
