//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.Orientation;

import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.style.UiStyle;


/********************************************************************
 * Places components in a horizontal or vertical flow that can be flexibly
 * controlled by the properties defined by the CSS Flexbox layout.
 *
 * @author eso
 */
public class UiFlexLayout extends UiLayout
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the component alignments in a CSS Flexbox layout.
	 */
	public enum FlexboxAlignment
	{
		START("flex-start"), CENTER("center"), END("flex-end"),
		STRETCH("stretch"), SPACE_BETWEEN("space-between"),
		SPACE_AROUND("space-around"), BASELINE("baseline");

		//~ Instance fields ----------------------------------------------------

		private final String sCssName;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param sCssName The CSS name of this alignment
		 */
		private FlexboxAlignment(String sCssName)
		{
			this.sCssName = sCssName;
		}

		//~ Static methods -----------------------------------------------------

		/***************************************
		 * Returns the value that corresponds to the given {@link Alignment}.
		 *
		 * @param  eAlignment The alignment to map
		 * @param  bCrossAxis TRUE if the mapping is for the cross-axis
		 *
		 * @return The matching instance
		 */
		public static FlexboxAlignment valueOf(
			Alignment eAlignment,
			boolean   bCrossAxis)
		{
			switch (eAlignment)
			{
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

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the CSS name of this alignment.
		 *
		 * @return The CSS name
		 */
		public String getCssName()
		{
			return sCssName;
		}
	}

	//~ Instance fields --------------------------------------------------------

	private Orientation eDirection;
	private boolean     bReverse;

	private FlexboxAlignment eJustifyContent = FlexboxAlignment.START;
	private FlexboxAlignment eAlignContent   = FlexboxAlignment.STRETCH;
	private FlexboxAlignment eAlignItems     = FlexboxAlignment.STRETCH;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a horizontal flow direction.
	 */
	public UiFlexLayout()
	{
		this(Orientation.HORIZONTAL);
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param eDirection The direction of the layout
	 */
	public UiFlexLayout(Orientation eDirection)
	{
		this(eDirection, false);
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param eDirection The direction of the layout
	 * @param bReverse   TRUE to reverse the flow along the given direction
	 */
	public UiFlexLayout(Orientation eDirection, boolean bReverse)
	{
		super(LayoutType.FLEX);

		this.eDirection = eDirection;
		this.bReverse   = bReverse;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the alignment of the layout elements along the axis perpendicular to
	 * the layout flow. The alignment {@link FlexboxAlignment#BASELINE BASELINE}
	 * is not supported for the content, only for {@link
	 * #alignItems(FlexboxAlignment)}.
	 *
	 * @param  eAlignment The cross-axis alignment
	 *
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout alignContent(FlexboxAlignment eAlignment)
	{
		eAlignContent = eAlignment;

		return this;
	}

	/***************************************
	 * Sets the horizontal alignment of elements. Overridden to map on either
	 * {@link #justifyContent(FlexboxAlignment)} or {@link
	 * #alignContent(FlexboxAlignment)} depending on the layout direction. This
	 * methods should be preferred for better readability and because they
	 * support additional Flexbox alignments.
	 *
	 * @param  eAlignment The horizontal alignment
	 *
	 * @return This instance for fluent invocation
	 */
	@Override
	public UiFlexLayout alignHorizontal(Alignment eAlignment)
	{
		return align(Orientation.HORIZONTAL, eAlignment);
	}

	/***************************************
	 * Sets the alignment of layout elements in their respective layout cell
	 * along the axis perpendicular to the layout flow.
	 *
	 * @param  eAlignment The cross-axis alignment
	 *
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout alignItems(FlexboxAlignment eAlignment)
	{
		eAlignItems = eAlignment;

		return this;
	}

	/***************************************
	 * Sets the vertical alignment of elements. Overridden to map on either
	 * {@link #justifyContent(FlexboxAlignment)} or {@link
	 * #alignContent(FlexboxAlignment)} depending on the layout direction. This
	 * methods should be preferred for better readability and because they
	 * support additional Flexbox alignments.
	 *
	 * @param  eAlignment The horizontal alignment
	 *
	 * @return This instance for fluent invocation
	 */
	@Override
	public UiFlexLayout alignVertical(Alignment eAlignment)
	{
		return align(Orientation.VERTICAL, eAlignment);
	}

	/***************************************
	 * Sets the alignment of the layout elements along the axis of the layout
	 * flow. The alignment values{@link FlexboxAlignment#STRETCH STRETCH} and
	 * {@link FlexboxAlignment#BASELINE BASELINE} are not supported for the main
	 * layout axis.
	 *
	 * @param  eAlignment The element alignment
	 *
	 * @return This instance for fluent invocation
	 */
	public UiFlexLayout justifyContent(FlexboxAlignment eAlignment)
	{
		eJustifyContent = eAlignment;

		return this;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void applyToContainer(UiContainer<?> rContainer)
	{
		super.applyToContainer(rContainer);

		UiStyle rStyle = rContainer.style();

		if (eDirection == Orientation.VERTICAL)
		{
			rStyle.css("flexDirection", bReverse ? "column-reverse" : "column");
		}
		else if (bReverse)
		{
			rStyle.css("flexDirection", "row-reverse");
		}

		if (eJustifyContent != FlexboxAlignment.START)
		{
			rStyle.css("justifyContent", eJustifyContent.getCssName());
		}

		if (eAlignContent != FlexboxAlignment.STRETCH)
		{
			rStyle.css("alignContent", eAlignContent.getCssName());
		}

		if (eAlignItems != FlexboxAlignment.STRETCH)
		{
			rStyle.css("alignItems", eAlignItems.getCssName());
		}
	}

	/***************************************
	 * Internal implementation for {@link #alignHorizontal(Alignment)} and
	 * {@link #alignVertical(Alignment)}.
	 *
	 * @param  eAlignDirection The alignment direction
	 * @param  eAlignment      The alignment
	 *
	 * @return This instance for fluent invocation
	 */
	private UiFlexLayout align(
		Orientation eAlignDirection,
		Alignment   eAlignment)
	{
		if (eDirection == eAlignDirection)
		{
			justifyContent(FlexboxAlignment.valueOf(eAlignment, false));
		}
		else
		{
			alignItems(FlexboxAlignment.valueOf(eAlignment, true));
		}

		return this;
	}
}
