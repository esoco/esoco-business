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
package de.esoco.process.ui.composite;

import de.esoco.lib.model.ColumnDefinition;
import de.esoco.lib.property.RelativeSize;
import de.esoco.lib.text.TextConvert;

import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.component.UiLabel;
import de.esoco.process.ui.component.UiLink;
import de.esoco.process.ui.composite.UiListPanel.ExpandableListStyle;
import de.esoco.process.ui.composite.UiListPanel.Item;
import de.esoco.process.ui.container.UiBuilder;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.layout.UiColumnGridLayout;
import de.esoco.process.ui.layout.UiFlowLayout;
import de.esoco.process.ui.style.UiStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.RELATIVE_WIDTH;


/********************************************************************
 * A table-like rendering of components in a list. The header and data areas are
 * rendered with {@link UiListPanel} instances where the list items represent
 * the header and data rows. The table appearance is achieved by using the same
 * column grid layout for the item content.
 *
 * @author eso
 */
public class UiTableList<T> extends UiComposite<UiTableList<T>>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * The available styles for expandable tables:
	 *
	 * <ul>
	 *   <li>{@link #EXPAND}: expands a row inside the table.</li>
	 *   <li>{@link #POPOUT}: expands a row and separates it from the other
	 *     table rows.</li>
	 * </ul>
	 */
	public enum ExpandableTableStyle { EXPAND, POPOUT }

	//~ Instance fields --------------------------------------------------------

	private UiListPanel   aHeaderPanel;
	private UiLayoutPanel aTableHeader;
	private UiListPanel   aDataList;

	private String sColumnPrefix = null;

	private List<Column> aColumns = new ArrayList<>();
	private List<Row>    aRows    = new ArrayList<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with rows that can be expanded by selecting their
	 * header area. Expanding an item will reveal the item content and hide any
	 * other previously expanded item content.
	 *
	 * @param rParent The parent container
	 */
	public UiTableList(UiContainer<?> rParent)
	{
		this(rParent, null);
	}

	/***************************************
	 * Creates a new instance with rows that can be expanded by selecting their
	 * header area. Expanding an item will reveal the item content and hide any
	 * other previously expanded item content.
	 *
	 * @param rParent      The parent container
	 * @param eExpandStyle The expand style
	 */
	public UiTableList(
		UiContainer<?>		 rParent,
		ExpandableTableStyle eExpandStyle)
	{
		super(rParent, new UiFlowLayout());

		ExpandableListStyle eListStyle =
			eExpandStyle != null
			? ExpandableListStyle.valueOf(eExpandStyle.name()) : null;

		aHeaderPanel = new UiListPanel(this);
		aTableHeader =
			aHeaderPanel.addItem().createHeaderPanel(new UiColumnGridLayout());
		aDataList    = new UiListPanel(this, eListStyle);

		aHeaderPanel.style().addStyleName(getComponentStyleName() + "Header");
		aDataList.style().addStyleName(getComponentStyleName() + "DataList");
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a column to this table.
	 *
	 * @param  fGetColumnData A function that retrieves the column value of a
	 *                        table cell from a row data object
	 *
	 * @return The new column
	 */
	public Column addColumn(Function<? super T, ?> fGetColumnData)
	{
		Objects.requireNonNull(fGetColumnData);

		Column aColumn = new Column(aTableHeader, fGetColumnData);

		aColumns.add(aColumn);

		return aColumn;
	}

	/***************************************
	 * Adds a new row for a certain data object to this table.
	 *
	 * @param  rRowData The data object for the row
	 *
	 * @return The new row
	 */
	public Row addRow(T rRowData)
	{
		Objects.requireNonNull(rRowData);

		Row rRow = new Row(aDataList.addItem(), rRowData);

		aRows.add(rRow);

		return rRow;
	}

	/***************************************
	 * Returns the columns of this table.
	 *
	 * @return The column list
	 */
	public List<Column> getColumns()
	{
		return aColumns;
	}

	/***************************************
	 * Returns the data rows of this table.
	 *
	 * @return The data row list
	 */
	public List<Row> getRows()
	{
		return aRows;
	}

	/***************************************
	 * Sets the prefix to be used for column titles.
	 *
	 * @param  sPrefix The column title prefix
	 *
	 * @return This instance
	 */
	public UiTableList<T> withColumnPrefix(String sPrefix)
	{
		sColumnPrefix = sPrefix;

		return this;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * The component that describes a table column.
	 *
	 * @author eso
	 */
	public class Column extends UiComposite<Column>
	{
		//~ Instance fields ----------------------------------------------------

		private Function<? super T, ?> fGetColumnData;
		private UiLink				   aColumnTitle;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rParent        The parent container
		 * @param fGetColumnData A function that retrieves the column value from
		 *                       a row data object
		 */
		Column(UiContainer<?> rParent, Function<? super T, ?> fGetColumnData)
		{
			super(rParent, new UiFlowLayout());

			this.fGetColumnData = fGetColumnData;

			aColumnTitle = new UiLink(this, deriveColumnTitle(fGetColumnData));
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the column value.
		 *
		 * @param  rDataObject The column value
		 *
		 * @return The column value
		 */
		public Object getColumnValue(T rDataObject)
		{
			return fGetColumnData.apply(rDataObject);
		}

		/***************************************
		 * Sets the title of this column.
		 *
		 * @param sTitle The new column title
		 */
		public void setTitle(String sTitle)
		{
			aColumnTitle.setText(sTitle);
		}

		/***************************************
		 * Sets the relative width of this column.
		 *
		 * @param  eWidth The relative size constant for the column width
		 *
		 * @return This instance for concatenation
		 */
		public final Column width(RelativeSize eWidth)
		{
			cell().width(eWidth);

			return this;
		}

		/***************************************
		 * Sets the width of this column as an absolute number of layout
		 * columns. The number must be less or equal to the total number of
		 * layout columns available (see {@link UiLayout#getColumns()}).
		 *
		 * @param  nGridColumns The number of layout columns this column should
		 *                      span
		 *
		 * @return This instance for concatenation
		 */
		public final Column width(int nGridColumns)
		{
			return set(nGridColumns, COLUMN_SPAN);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected String getComponentStyleName()
		{
			return UiTableList.this.getComponentStyleName() +
				   super.getComponentStyleName();
		}

		/***************************************
		 * Tries to derive a column title from a data access function.
		 *
		 * @param  fGetColumnData The data access function
		 *
		 * @return The column title
		 */
		private String deriveColumnTitle(Function<? super T, ?> fGetColumnData)
		{
			String sTitle = null;

			if (fGetColumnData instanceof RelationType)
			{
				String sPrefix =
					sColumnPrefix != null ? sColumnPrefix
										  : ColumnDefinition.STD_COLUMN_PREFIX;

				sTitle = ((RelationType<?>) fGetColumnData).getSimpleName();
				sTitle = sPrefix + TextConvert.capitalizedIdentifier(sTitle);
			}
			else if (fGetColumnData instanceof Relatable)
			{
				sTitle = ((Relatable) fGetColumnData).get(StandardTypes.NAME);
			}

			if (sTitle == null)
			{
				sTitle = fGetColumnData.toString();
			}

			return sTitle;
		}
	}

	/********************************************************************
	 * The component that contains the contents of a table row.
	 *
	 * @author eso
	 */
	public class Row extends UiComposite<Row>
	{
		//~ Instance fields ----------------------------------------------------

		private Item rRowItem;
		private T    rRowData;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rRowItem The item to place the row in
		 * @param rRowData The row data object
		 */
		Row(Item rRowItem, T rRowData)
		{
			super(rRowItem.getHeader(), new UiColumnGridLayout());

			this.rRowItem = rRowItem;
			this.rRowData = rRowData;

			for (Column rColumn : aColumns)
			{
				Object rValue = rColumn.getColumnValue(rRowData);
				String sText  = rValue != null ? rValue.toString() : "";

				builder().addLabel(sText);
			}
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns a builder for the content of an expandable row.
		 *
		 * @return The row content builder
		 */
		public UiBuilder<?> getContentBuilder()
		{
			return rRowItem.builder();
		}

		/***************************************
		 * Returns the row data object.
		 *
		 * @return The row data object
		 */
		public final T getData()
		{
			return rRowData;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public UiStyle style()
		{
			// redirect to the item style
			return rRowItem.style();
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public UiStyle style(UiStyle rStyle)
		{
			// redirect to the item style
			return rRowItem.style(rStyle);
		}

		/***************************************
		 * Updates this row from a new row data object.
		 *
		 * @param rRowData The new row data object
		 */
		public final void update(T rRowData)
		{
			this.rRowData = rRowData;

			int nIndex = 0;

			for (Column rColumn : aColumns)
			{
				Object rValue = rColumn.getColumnValue(rRowData);
				String sText  = rValue != null ? rValue.toString() : "";

				((UiLabel) getComponents().get(nIndex++)).setText(sText);
			}
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected void applyProperties()
		{
			super.applyProperties();

			int nIndex = 0;

			for (Column rColumn : aColumns)
			{
				UiComponent<?, ?> rComponent = getComponents().get(nIndex++);

				RelativeSize eColumnWidth =
					rColumn.cell().get(RELATIVE_WIDTH, null);

				int nColumnSpan = rColumn.cell().get(COLUMN_SPAN, 0);

				rColumn.style().applyPropertiesTo(rComponent);

				if (eColumnWidth != null)
				{
					rComponent.set(RELATIVE_WIDTH, eColumnWidth);
				}

				if (nColumnSpan > 0)
				{
					rComponent.set(nColumnSpan, COLUMN_SPAN);
				}
			}
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected String getComponentStyleName()
		{
			return UiTableList.this.getComponentStyleName() +
				   super.getComponentStyleName();
		}
	}
}
