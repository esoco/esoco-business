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
import de.esoco.lib.model.DataProvider;
import de.esoco.lib.property.HasSelection;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
	implements HasSelection<UiTableList<T>.Row>
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

	private DataProvider<T> rDataProvider;

	private UiListPanel   aHeaderPanel;
	private UiLayoutPanel aTableHeader;
	private UiListPanel   aDataList;

	private Row rSelectedRow = null;

	private String sColumnPrefix = null;

	private List<Column> aColumns = new ArrayList<>();
	private List<Row>    aRows    = new ArrayList<>();

	private BiConsumer<UiBuilder<?>, T> fRowContentBuilder;
	private Consumer<Column>		    fHandleColumnSelection;
	private Consumer<Row>			    fHandleRowSelection;

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

		for (Row rRow : aRows)
		{
			rRow.addColumnComponent(aColumn);
		}

		return aColumn;
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
	 * Returns the provider of the table data.
	 *
	 * @return The table row data provider
	 */
	public final DataProvider<T> getData()
	{
		return rDataProvider;
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
	 * Returns the currently selected row.
	 *
	 * @return The selected or (NULL for none)
	 */
	@Override
	public Row getSelection()
	{
		return rSelectedRow;
	}

	/***************************************
	 * Registers a listener for column selections (i.e. clicks on column
	 * headers). The listener will be invoked with the respective column as it's
	 * argument.
	 *
	 * @param  fHandleColumnSelection The column selection handler
	 *
	 * @return This instance
	 */
	public UiTableList<T> onColumnSelection(
		Consumer<Column> fHandleColumnSelection)
	{
		this.fHandleColumnSelection = fHandleColumnSelection;

		return this;
	}

	/***************************************
	 * Registers a listener for row selections (i.e. clicks on rows). The
	 * listener will be invoked with the respective row as it's argument.
	 *
	 * @param  fHandleRowSelection The row selection handler
	 *
	 * @return This instance
	 */
	public UiTableList<T> onRowSelection(Consumer<Row> fHandleRowSelection)
	{
		this.fHandleRowSelection = fHandleRowSelection;

		return this;
	}

	/***************************************
	 * Removes a certain row from this table.
	 *
	 * @param rRow The row to remove
	 */
	public void removeRow(Row rRow)
	{
		aDataList.removeItem(rRow.rRowItem);
		aRows.remove(rRow);
	}

	/***************************************
	 * Sets the prefix to be used for column titles.
	 *
	 * @param sPrefix The column title prefix
	 */
	public void setColumnPrefix(String sPrefix)
	{
		sColumnPrefix = sPrefix;
	}

	/***************************************
	 * Sets the data provider of the table row data. This will immediately build
	 * the table rows from the data so all necessary initializations of this
	 * table should have been performed already (e.g. settings columns or an
	 * expanded row builder).
	 *
	 * @param rRowDataProvider The data provider that returns the table rows
	 */
	public void setData(DataProvider<T> rRowDataProvider)
	{
		Objects.requireNonNull(rRowDataProvider);

		aDataList.clear();
		aRows.clear();
		rDataProvider = rRowDataProvider;

		update();
	}

	/***************************************
	 * Sets a consumer that will be invoked to build the content of expanded
	 * rows if this table has an expansion style. This can be used for rows with
	 * simple row content where the full content can be build at once. For
	 * complex cases where the expanded row content should be updated only upon
	 * row selection a {@link Row} subclass should be used instead with
	 * overridden methods {@link Row#initExpandedContent(UiContainer)} and
	 * {@link Row#updateExpandedContent()}.
	 *
	 * <p>The argument is a binary consumer that will be invoked with the
	 * builder for the row content container and the data object of the row.</p>
	 *
	 * @param fBuilder The builder for the row content
	 */
	public void setExpandedRowBuilder(BiConsumer<UiBuilder<?>, T> fBuilder)
	{
		fRowContentBuilder = fBuilder;
	}

	/***************************************
	 * Sets the selection to a certain row.
	 *
	 * @param rRow The row to select or NULL for no selection
	 */
	@Override
	public void setSelection(Row rRow)
	{
		handleRowSelection(rRow, false);
	}

	/***************************************
	 * Creates a new row. Subclasses can override this method to return their
	 * own row subclasses, e.g. to handle the row content.
	 *
	 * @param  rItem    The row item
	 * @param  rRowData The row data
	 *
	 * @return A new row instance
	 */
	protected Row createRow(Item rItem, T rRowData)
	{
		return new Row(rItem, rRowData);
	}

	/***************************************
	 * Update the rows of this table. This will also adjust the number of table
	 * rows (i.e. add or remove rows) to match the size of the given data set.
	 *
	 * @param nFirstRow The index of the first row to display
	 * @param nCount    The number of rows to display
	 */
	protected void displayRows(int nFirstRow, int nCount)
	{
		int nRowIndex = 0;

		for (T rRowData : rDataProvider.getData(nFirstRow, nCount))
		{
			if (nRowIndex < aRows.size())
			{
				aRows.get(nRowIndex).update(rRowData);
			}
			else
			{
				aRows.add(createRow(aDataList.addItem(), rRowData));
			}

			nRowIndex++;
		}

		while (nRowIndex < aRows.size())
		{
			removeRow(aRows.get(nRowIndex));
		}
	}

	/***************************************
	 * Updates the table display from the data provider that has been set
	 * through {@link #setData(DataProvider)}. The default implementation
	 * renders all data objects from the provider. Subclasses can override this
	 * method if they need to display only part of the data, e.g. for a paging
	 * table.
	 */
	protected void update()
	{
		displayRows(0, rDataProvider.size());
	}

	/***************************************
	 * Handles the selection event of a certain column.
	 *
	 * @param rColumn The selected column
	 */
	void handleColumnSelection(Column rColumn)
	{
		if (fHandleColumnSelection != null)
		{
			fHandleColumnSelection.accept(rColumn);
		}
	}

	/***************************************
	 * Handles the selection event of a certain row.
	 *
	 * @param rRow       The selected row
	 * @param bFireEvent TRUE to notifiy a row selection listener if available
	 */
	void handleRowSelection(Row rRow, boolean bFireEvent)
	{
		boolean bHasSelection = !rRow.isSelected();

		if (rSelectedRow != null)
		{
			rSelectedRow.setSelected(false);
			rSelectedRow = null;
		}

		rRow.setSelected(bHasSelection);

		if (bHasSelection)
		{
			rSelectedRow = rRow;
			rSelectedRow.updateExpandedContent();
		}

		if (fHandleRowSelection != null)
		{
			fHandleRowSelection.accept(rRow);
		}
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * The component that describes a table column.
	 *
	 * @author eso
	 */
	public class Column extends TableElement<Column>
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
			aColumnTitle.onClick(v -> handleColumnSelection(this));
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
	public class Row extends TableElement<Row>
	{
		//~ Instance fields ----------------------------------------------------

		private Item    rRowItem;
		private T	    rRowData;
		private boolean bSelected = false;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rRowItem The item to place the row in
		 * @param rRowData The row data object
		 */
		protected Row(Item rRowItem, T rRowData)
		{
			super(rRowItem.getHeader(), new UiColumnGridLayout());

			this.rRowItem = rRowItem;
			this.rRowData = rRowData;

			for (Column rColumn : aColumns)
			{
				addColumnComponent(rColumn);
			}

			rRowItem.getHeader().onClick(v -> handleRowSelection(this, true));

			initExpandedContent(rRowItem.builder());
		}

		//~ Methods ------------------------------------------------------------

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
		 * Returns the row's selection state.
		 *
		 * @return The selection state
		 */
		public final boolean isSelected()
		{
			return bSelected;
		}

		/***************************************
		 * Sets this row's selection state.
		 *
		 * @param bSelected The selection state
		 */
		public final void setSelected(boolean bSelected)
		{
			this.bSelected = bSelected;
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

			if (bSelected)
			{
				updateExpandedContent();
			}
		}

		/***************************************
		 * Adds a row component for a certain column.
		 *
		 * @param rColumn The column to add the component for
		 */
		protected void addColumnComponent(Column rColumn)
		{
			Object rValue = rColumn.getColumnValue(rRowData);
			String sText  = rValue != null ? rValue.toString() : "";

			builder().addLabel(sText);
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

		/***************************************
		 * Can be overridden by subclasses to initialize the content of
		 * expandable rows. To update the content upon selection or setting of
		 * new row data the method {@link #updateExpandedContent()} needs to be
		 * implemented too. The row data is available through {@link
		 * #getData()}.
		 *
		 * <p>The default implementation invokes the expanded row content
		 * builder if one has been set through the method {@link
		 * UiTableList#setExpandedRowBuilder(Consumer)}.</p>
		 *
		 * @param rBuilder The builder to build the content with
		 */
		protected void initExpandedContent(UiBuilder<?> rBuilder)
		{
			if (fRowContentBuilder != null)
			{
				fRowContentBuilder.accept(rBuilder, rRowData);
			}
		}

		/***************************************
		 * Can be overridden by subclasses that need to update the content of
		 * expandable rows on selection or if a new row data is set. The content
		 * must be created in {@link #initExpandedContent(UiContainer)}. The row
		 * data is available through {@link #getData()}.
		 */
		protected void updateExpandedContent()
		{
		}
	}

	/********************************************************************
	 * The base class for child components of {@link UiTableList}.
	 *
	 * @author eso
	 */
	abstract class TableElement<E extends TableElement<E>>
		extends UiComposite<E>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rParent The parent container
		 * @param eLayout The element layout
		 */
		public TableElement(UiContainer<?> rParent, UiLayout eLayout)
		{
			super(rParent, eLayout);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the parent table of this element.
		 *
		 * @return The parent table
		 */
		public UiTableList<T> getTable()
		{
			return UiTableList.this;
		}
	}
}
