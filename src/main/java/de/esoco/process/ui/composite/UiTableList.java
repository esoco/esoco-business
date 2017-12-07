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

import de.esoco.data.element.DataElement;

import de.esoco.lib.model.ColumnDefinition;
import de.esoco.lib.model.DataProvider;
import de.esoco.lib.property.HasAttributeFilter;
import de.esoco.lib.property.HasAttributeSorting;
import de.esoco.lib.property.HasSelection;
import de.esoco.lib.property.RelativeSize;
import de.esoco.lib.property.SortDirection;
import de.esoco.lib.property.StyleProperties;
import de.esoco.lib.property.TextAttribute;
import de.esoco.lib.text.TextConvert;

import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.component.UiIcon;
import de.esoco.process.ui.component.UiLink;
import de.esoco.process.ui.composite.UiListPanel.ExpandableListStyle;
import de.esoco.process.ui.composite.UiListPanel.Item;
import de.esoco.process.ui.container.UiBuilder;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.event.UiHasActionEvents;
import de.esoco.process.ui.event.UiHasUpdateEvents;
import de.esoco.process.ui.graphics.UiIconSupplier;
import de.esoco.process.ui.graphics.UiImageResource;
import de.esoco.process.ui.layout.UiColumnGridLayout;
import de.esoco.process.ui.layout.UiFlowLayout;
import de.esoco.process.ui.style.UiStyle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.RELATIVE_WIDTH;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.StateProperties.NO_EVENT_PROPAGATION;

import static de.esoco.process.ProcessRelationTypes.CLIENT_LOCALE;


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

	private UiListPanel    aHeaderPanel;
	private Item		   aHeaderItem;
	private UiLayoutPanel  aTableHeader;
	private UiListPanel    aDataList;
	private UiContainer<?> aEmptyTableInfo;

	private Row rSelectedRow = null;

	private String sColumnPrefix = null;

	private Column<?> rSortColumn = null;

	private List<Column<?>> aColumns = new ArrayList<>();

	private List<Row> aRows = new ArrayList<>();

	private BiConsumer<UiBuilder<?>, T> fRowContentBuilder;
	private Consumer<Column<?>>		    fHandleColumnSelection;
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

		aHeaderPanel    = new UiListPanel(this);
		aHeaderItem     = aHeaderPanel.addItem();
		aTableHeader    =
			aHeaderItem.createHeaderPanel(new UiColumnGridLayout());
		aDataList	    = new UiListPanel(this, eListStyle);
		aEmptyTableInfo = new UiLayoutPanel(this, new UiFlowLayout());

		aEmptyTableInfo.hide();

		aHeaderPanel.style()
					.addStyleName(UiTableList.class.getSimpleName() + "Header");
		aDataList.style()
				 .addStyleName(UiTableList.class.getSimpleName() + "Data");
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
	public <V> Column<V> addColumn(Function<? super T, V> fGetColumnData)
	{
		Objects.requireNonNull(fGetColumnData);

		Column<V> aColumn = createColumn(fGetColumnData);

		aColumns.add(aColumn);

		for (Row rRow : aRows)
		{
			rRow.addColumnComponent(aColumn);
		}

		return aColumn;
	}

	/***************************************
	 * Adds multiple columns at once.
	 *
	 * @param rColumnDataReaders The
	 */
	@SuppressWarnings("unchecked")
	public void addColumns(Function<? super T, ?>... rColumnDataReaders)
	{
		for (Function<? super T, ?> fGetColumnData : rColumnDataReaders)
		{
			addColumn(fGetColumnData);
		}
	}

	/***************************************
	 * Adds components that will be displayed if a table is empty, i.e. it has
	 * now visible data rows. The argument is a function that receives an UI
	 * builder that must be used to build the empty table info component. This
	 * component will then be displayed below the table header.
	 *
	 * <p>The application should not make assumptions about the layout of the
	 * builder's container and only add a single component. If it needs a more
	 * complex UI it should add a container with the required layout.</p>
	 *
	 * @param fCreateEmtpyTableInfo A consumer that receives a builder for the
	 *                              empty table info area
	 */
	public void addEmptyTableInfo(Consumer<UiBuilder<?>> fCreateEmtpyTableInfo)
	{
		fCreateEmtpyTableInfo.accept(aEmptyTableInfo.builder());
	}

	/***************************************
	 * Creates and returns a container that allows to add expanded content to
	 * the header of this list. The content will be displayed if the header is
	 * expanded by clicking on it. This method must be invoked before the list
	 * is rendered for the first time because it needs to modify the styles of
	 * the header components to support expansion.
	 *
	 * @param  rLayout The layout of the expanded header content panel the
	 *                 builder is created for
	 *
	 * @return TODO: The container for the expanded header content
	 */
	public UiContainer<?> addExpandedHeader(UiLayout rLayout)
	{
		aHeaderPanel.setExpandStyle(ExpandableListStyle.EXPAND);

		return aHeaderItem.builder().addPanel(rLayout);
	}

	/***************************************
	 * Returns the columns of this table.
	 *
	 * @return The column list
	 */
	public List<Column<?>> getColumns()
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
		Consumer<Column<?>> fHandleColumnSelection)
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

		for (Column<?> rColumn : aColumns)
		{
			rColumn.dataAvailable(rDataProvider);
		}

		// only update if the container has been built already, otherwise leave
		// it to the build() method
		if (isBuilt())
		{
			update();
		}
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
	 * {@inheritDoc}
	 */
	@Override
	protected void build()
	{
		if (rDataProvider != null)
		{
			update();
		}
	}

	/***************************************
	 * Creates a new column. Subclasses can override this method to return their
	 * own column subclasses, e.g. to handle special formatting or value
	 * parsing.
	 *
	 * @param  fGetColumnData The column data access function
	 *
	 * @return The new column instance
	 */
	protected <V> Column<V> createColumn(Function<? super T, V> fGetColumnData)
	{
		return new Column<>(aTableHeader, fGetColumnData);
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
				Row aRow = createRow(aDataList.addItem(), rRowData);

				aRows.add(aRow);
				aRow.setIndex(nRowIndex);
				aRow.initExpandedContent(aRow.rRowItem.builder()
										 .addPanel(aRow.getContentLayout())
										 .builder());
			}

			nRowIndex++;
		}

		while (nRowIndex < aRows.size())
		{
			removeRow(aRows.get(nRowIndex));
		}

		aEmptyTableInfo.setVisible(aRows.size() == 0 &&
								   aEmptyTableInfo.getComponents().size() > 0);
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
	 * Handles the selection event of a certain row.
	 *
	 * @param rRow       The selected row
	 * @param bFireEvent TRUE to notify a row selection listener if available
	 */
	void handleRowSelection(Row rRow, boolean bFireEvent)
	{
		if (rSelectedRow != rRow)
		{
			if (rSelectedRow != null)
			{
				rSelectedRow.setSelected(false);
			}

			rSelectedRow = rRow;

			if (rSelectedRow != null)
			{
				rSelectedRow.setSelected(true);
				rSelectedRow.updateExpandedContent();
			}

			aDataList.set(rSelectedRow != null ? rSelectedRow.getIndex() : -1,
						  CURRENT_SELECTION);
		}

		if (bFireEvent && fHandleRowSelection != null)
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
	public class Column<V> extends UiComposite<Column<V>>
	{
		//~ Instance fields ----------------------------------------------------

		private Function<? super T, V> fGetColumnData;
		private Class<? super V>	   rDatatype;
		private Class<?>			   rDisplayDatatype;

		private Function<UiBuilder<?>, UiComponent<?, ?>> fDisplayFactory;
		private BiConsumer<UiComponent<?, ?>, V>		  fDisplayUpdate;

		private UiLink aColumnTitle;

		private SortDirection	    eInitialSortDirection;
		private Function<V, String> fValueFormat;

		private UiStyle aComponentStyle = new UiStyle();

		private Consumer<V> fActionHandler;
		private Consumer<V> fUpdateHandler;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that retrieves the column value from a row
		 * data object with the given value access function. If the function is
		 * an instance of {@link RelationType} the column datatype will be
		 * queried from it. Otherwise the method {@link #datatype(Class)} should
		 * be invoked to set the column datatype or else some functionality like
		 * sorting and value formatting may not be available.
		 *
		 * @param rParent        The parent container
		 * @param fGetColumnData A function that retrieves the column value from
		 *                       a row data object
		 */
		@SuppressWarnings("unchecked")
		Column(UiContainer<?> rParent, Function<? super T, V> fGetColumnData)
		{
			super(rParent, new UiFlowLayout());

			this.fGetColumnData = fGetColumnData;

			if (fGetColumnData instanceof RelationType)
			{
				datatype(((RelationType<V>) fGetColumnData).getValueType());
			}

			aColumnTitle =
				new UiLink(this, "").set(NO_EVENT_PROPAGATION)
									.onClick(v -> handleColumnSelection());
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns a {@link UiStyle} object that holds styles that will be
		 * applied to all components in a certain column.
		 *
		 * @return The component style
		 */
		public UiStyle componentStyle()
		{
			return aComponentStyle;
		}

		/***************************************
		 * Sets the datatype of this column. If the value access function is an
		 * instance of {@link RelationType} the datatype will be determined
		 * automatically.
		 *
		 * @param  rDatatype The column datatype class
		 *
		 * @return This instance
		 */
		public Column<V> datatype(Class<? super V> rDatatype)
		{
			this.rDatatype = rDatatype;

			return this;
		}

		/***************************************
		 * Sets a factory function for the display components of this column.
		 * The function must return a new component that has been created in the
		 * container of the given UI builder. If a display factory is set it
		 * will be used to create the display components for this column in each
		 * row. Otherwise default display components will be created. If the
		 * components created by the factory need special update values the
		 * method {@link #updateWith(BiConsumer)} also needs to be invoked to
		 * set an update function.
		 *
		 * @param  fDisplayFactory The display component factory function
		 *
		 * @return This instance
		 */
		public Column<V> displayWith(
			Function<UiBuilder<?>, UiComponent<?, ?>> fDisplayFactory)
		{
			this.fDisplayFactory = fDisplayFactory;

			return this;
		}

		/***************************************
		 * Sets a function that will be used for format values of this column as
		 * a string. The function must be able to handle NULL values if the
		 * column value in a certain rows can be NULL. It may return NULL values
		 * which will be rendered as an empty cell.
		 *
		 * @param  fValueFormat A function that formats values as a string
		 *
		 * @return This instance
		 */
		public Column<V> formatWith(Function<V, String> fValueFormat)
		{
			this.fValueFormat = fValueFormat;

			return this;
		}

		/***************************************
		 * Returns the value of this column from a data object.
		 *
		 * @param  rDataObject The data object
		 *
		 * @return The column value (can be NULL)
		 */
		public V getColumnValue(T rDataObject)
		{
			return fGetColumnData.apply(rDataObject);
		}

		/***************************************
		 * Adds an event handler that should be invoked if the column component
		 * receives an action event as defined by {@link UiHasActionEvents}. The
		 * handler will receive the value of the column in the respective row.
		 * This must be used in conjunction with {@link #displayWith(Function)}
		 * because the default column components don't produce events.
		 *
		 * @param  fHandler The event handler
		 *
		 * @return This instance
		 *
		 * @see    #displayWith(Function)
		 * @see    #onUpdate(Consumer)
		 */
		public Column<V> onAction(Consumer<V> fHandler)
		{
			fActionHandler = fHandler;

			return this;
		}

		/***************************************
		 * Adds an event handler that should be invoked if the column component
		 * receives an action event as defined by {@link UiHasUpdateEvents}. The
		 * handler will receive the value of the column in the respective row.
		 * This must be used in conjunction with {@link #displayWith(Function)}
		 * because the default column components don't produce events.
		 *
		 * @param  fHandler The event handler
		 *
		 * @return This instance
		 *
		 * @see    #displayWith(Function)
		 * @see    #onAction(Consumer)
		 */
		public Column<V> onUpdate(Consumer<V> fHandler)
		{
			fUpdateHandler = fHandler;

			return this;
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
		 * Applies or removes sorting of this column in the given direction.
		 *
		 * @param  eDirection The sort direction or NULL to remove explicit
		 *                    sorting
		 *
		 * @return This instance
		 */
		public Column<V> sort(SortDirection eDirection)
		{
			if (rDataProvider == null)
			{
				eInitialSortDirection = eDirection;
			}
			else
			{
				setSorting(eDirection);
			}

			return this;
		}

		/***************************************
		 * A shortcut method for {@link #sort(SortDirection)} with ascending
		 * direction.
		 *
		 * @return This instance
		 */
		public Column<V> sortAscending()
		{
			return sort(SortDirection.ASCENDING);
		}

		/***************************************
		 * A shortcut method for {@link #sort(SortDirection)} with descending
		 * direction.
		 *
		 * @return This instance
		 */
		public Column<V> sortDescending()
		{
			return sort(SortDirection.DESCENDING);
		}

		/***************************************
		 * @see de.esoco.process.ui.UiContainer#toString()
		 */
		@Override
		public String toString()
		{
			return super.toString() + fragment().getUIProperties(type());
		}

		/***************************************
		 * Sets a consumer that will be invoked to update display components of
		 * this column. This is typically used when an application defines their
		 * own display components through {@link #displayWith(Function)}.
		 *
		 * @param  fDisplayUpdate A binary consumer that updates the given
		 *                        component with a new column value
		 *
		 * @return This instance
		 */
		public Column<V> updateWith(
			BiConsumer<UiComponent<?, ?>, V> fDisplayUpdate)
		{
			this.fDisplayUpdate = fDisplayUpdate;

			return this;
		}

		/***************************************
		 * Sets the relative width of this column.
		 *
		 * @param  eWidth The relative size constant for the column width
		 *
		 * @return This instance for concatenation
		 */
		public final Column<V> width(RelativeSize eWidth)
		{
			return set(RELATIVE_WIDTH, eWidth);
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
		public final Column<V> width(int nGridColumns)
		{
			return set(nGridColumns, COLUMN_SPAN);
		}

		/***************************************
		 * Adds a display component for this column and the corresponding value
		 * in a certain data object.
		 *
		 * @param  rBuilder The builder to create the component with
		 * @param  fData    A function that provides access to the data object
		 *
		 * @return The new component
		 */
		protected UiComponent<?, ?> addDisplayComponent(
			UiBuilder<?> rBuilder,
			Supplier<T>  fData)
		{
			UiComponent<?, ?> aComponent = null;

			if (fDisplayFactory != null)
			{
				aComponent = fDisplayFactory.apply(rBuilder);
			}
			else
			{
				Class<?> rComponentDatatype =
					rDisplayDatatype != null ? rDisplayDatatype : rDatatype;

				if (rComponentDatatype != null)
				{
					if (rComponentDatatype.isEnum() ||
						UiIconSupplier.class.isAssignableFrom(rComponentDatatype))
					{
						aComponent = rBuilder.addIcon(null);
					}
				}

				if (aComponent == null)
				{
					aComponent = rBuilder.addLabel("");
				}
			}

			if (fActionHandler != null &&
				aComponent instanceof UiHasActionEvents)
			{
				((UiHasActionEvents<?, ?>) aComponent).onAction(v ->
																fActionHandler
																.accept(getColumnValue(fData
																					   .get())));
			}

			if (fUpdateHandler != null &&
				aComponent instanceof UiHasUpdateEvents)
			{
				((UiHasUpdateEvents<?, ?>) aComponent).onUpdate(v ->
																fUpdateHandler
																.accept(getColumnValue(fData
																					   .get())));
			}

			updateDisplay(aComponent, fData.get());

			return aComponent;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected void applyProperties()
		{
			super.applyProperties();

			set(StyleProperties.HIDE_LABEL);
			applyColumnTitle();
		}

		/***************************************
		 * Will be notified when the table data is available.
		 *
		 * @param rDataProvider The table data provider
		 */
		protected void dataAvailable(DataProvider<T> rDataProvider)
		{
			if (eInitialSortDirection != null)
			{
				setSorting(eInitialSortDirection);
			}
		}

		/***************************************
		 * Formats a value into a string representation.
		 *
		 * @param  rValue The value to format
		 *
		 * @return The formatted string
		 */
		protected String formatAsString(V rValue)
		{
			String sValue = null;

			if (fValueFormat != null)
			{
				sValue = fValueFormat.apply(rValue);
			}
			else if (rValue != null)
			{
				if (rValue.getClass().isEnum())
				{
					sValue = DataElement.createItemResource(rValue);
				}
				else if (rValue instanceof Date)
				{
					Locale rLocale = fragment().getParameter(CLIENT_LOCALE);

					DateFormat rDateFormat =
						SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT,
														 rLocale);

					// on first format of a data assign a formatting function
					// that will be used subsequently
					fValueFormat =
						v -> v != null ? rDateFormat.format(v) : null;

					sValue = fValueFormat.apply(rValue);
				}
				else
				{
					sValue = rValue.toString();
				}
			}

			if (sValue == null)
			{
				sValue = "&nbsp;";
			}

			return sValue;
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
		 * Handles the selection event of a certain column.
		 */
		protected void handleColumnSelection()
		{
			setSorting(nextSortDirection());

			if (fHandleColumnSelection != null)
			{
				fHandleColumnSelection.accept(this);
			}
		}

		/***************************************
		 * Updates the display of column data from a certain data object. The
		 * argument component must be one that has been created by the method
		 * {@link #addDisplayComponent(UiBuilder, Object)}.
		 *
		 * @param rComponent  The component to update
		 * @param rDataObject The data object to read the update value from
		 */
		protected void updateDisplay(
			UiComponent<?, ?> rComponent,
			T				  rDataObject)
		{
			V rValue = getColumnValue(rDataObject);

			if (fDisplayUpdate != null)
			{
				fDisplayUpdate.accept(rComponent, rValue);
			}
			else
			{
				if (rComponent instanceof UiIcon)
				{
					updateIcon((UiIcon) rComponent, rValue);
				}
				else if (rComponent instanceof TextAttribute)
				{
					((TextAttribute) rComponent).setText(formatAsString(rValue));
				}
			}
		}

		/***************************************
		 * Updates an icon component from a column value.
		 *
		 * @param rIcon  The icon component
		 * @param rValue The column value
		 */
		protected void updateIcon(UiIcon rIcon, V rValue)
		{
			if (rValue instanceof UiIconSupplier)
			{
				rIcon.setIcon(((UiIconSupplier) rValue).getIcon());
			}
			else
			{
				UiImageResource rIconResource = null;

				if (rValue != null)
				{
					rIconResource =
						new UiImageResource("$im" +
											DataElement.createItemName(rValue));
				}

				rIcon.setIcon(rIconResource);
			}
		}

		/***************************************
		 * Checks whether the table data can be filtered by this column.
		 *
		 * @return
		 */
		boolean allowsFiltering()
		{
			return rDatatype != null && rDataProvider != null &&
				   rDataProvider instanceof HasAttributeFilter;
		}

		/***************************************
		 * Checks whether the table data can be sorted by this column.
		 *
		 * @return
		 */
		boolean allowsSorting()
		{
			return rDatatype != null && rDataProvider != null &&
				   Comparable.class.isAssignableFrom(rDatatype) &&
				   rDataProvider instanceof HasAttributeSorting;
		}

		/***************************************
		 * Toggles the current sort direction of this column to the next values
		 * (in the order ascending, descending, none).
		 *
		 * @return The new sort direction
		 */
		@SuppressWarnings("unchecked")
		SortDirection nextSortDirection()
		{
			SortDirection eSortDirection = null;

			if (allowsSorting())
			{
				eSortDirection =
					((HasAttributeSorting<T>) rDataProvider).getSortDirection(fGetColumnData);

				if (eSortDirection == null)
				{
					eSortDirection = SortDirection.ASCENDING;
				}
				else if (eSortDirection == SortDirection.ASCENDING)
				{
					eSortDirection = SortDirection.DESCENDING;
				}
				else
				{
					eSortDirection = null;
				}
			}

			return eSortDirection;
		}

		/***************************************
		 * Sets the sorting of this column.
		 *
		 * @param eDirection The sort direction or NULL to remove sorting
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void setSorting(SortDirection eDirection)
		{
			if (allowsSorting())
			{
				String sColumnStyle = "";

				if (eDirection == SortDirection.ASCENDING)
				{
					sColumnStyle = "sort ascending";
				}
				else if (eDirection == SortDirection.DESCENDING)
				{
					sColumnStyle = "sort descending";
				}

				if (rSortColumn != null && rSortColumn != this)
				{
					Column rPrevCol = rSortColumn;

					rSortColumn = null;
					rPrevCol.setSorting(null);
				}

				((HasAttributeSorting<T>) rDataProvider).applySorting((Function<T, Comparable>)
																	  fGetColumnData,
																	  eDirection);

				aColumnTitle.style().styleName(sColumnStyle);
				rSortColumn = this;
				update();
			}
		}

		/***************************************
		 * Creates the title string of this column by trying to derive it from
		 * the resource ID or, if not availabe, the data access function.
		 */
		private void applyColumnTitle()
		{
			String sTitle = aColumnTitle.getText();

			if (sTitle == null || sTitle.isEmpty())
			{
				sTitle = get(RESOURCE_ID);

				if (sTitle != null)
				{
					sTitle = ColumnDefinition.STD_COLUMN_PREFIX + sTitle;
				}
				else if (fGetColumnData instanceof RelationType)
				{
					String sPrefix =
						sColumnPrefix != null
						? sColumnPrefix : ColumnDefinition.STD_COLUMN_PREFIX;

					sTitle = ((RelationType<?>) fGetColumnData).getSimpleName();
					sTitle =
						sPrefix + TextConvert.capitalizedIdentifier(sTitle);
				}
				else if (fGetColumnData instanceof Relatable)
				{
					sTitle =
						((Relatable) fGetColumnData).get(StandardTypes.NAME);
				}
				else
				{
					sTitle = fGetColumnData.toString();
				}

				aColumnTitle.setText(sTitle);
			}
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

		private int     nRowIndex;
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

			for (Column<?> rColumn : aColumns)
			{
				addColumnComponent(rColumn);
			}

			rRowItem.getHeader()
					.onClickInContainerArea(v -> handleRowSelection(this, true));
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
		 * Returns the index of the row in it's list.
		 *
		 * @return The row index
		 */
		public final int getIndex()
		{
			return nRowIndex;
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
			setRowItemStyle();
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
		public void update(T rRowData)
		{
			this.rRowData = rRowData;

			List<UiComponent<?, ?>> rComponents = getComponents();
			int					    nIndex	    = 0;

			for (Column<?> rColumn : aColumns)
			{
				rColumn.updateDisplay(rComponents.get(nIndex++), rRowData);
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
		protected void addColumnComponent(Column<?> rColumn)
		{
			rColumn.addDisplayComponent(builder(), this::getData);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected void applyProperties()
		{
			super.applyProperties();

			int nIndex = 0;

			for (Column<?> rColumn : aColumns)
			{
				UiComponent<?, ?> rComponent = getComponents().get(nIndex++);

				RelativeSize eColumnWidth = rColumn.get(RELATIVE_WIDTH);
				Integer		 rColumnSpan  = rColumn.get(COLUMN_SPAN);

				rColumn.componentStyle().applyPropertiesTo(rComponent);

				if (eColumnWidth != null)
				{
					rComponent.set(RELATIVE_WIDTH, eColumnWidth);
				}

				if (rColumnSpan != null && rColumnSpan.intValue() > 0)
				{
					rComponent.set(rColumnSpan.intValue(), COLUMN_SPAN);
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
		 * Can be overridden to return a specific layout instance for the row
		 * content. Will be invoked before the row content is initialized. The
		 * default implementation returns a new instance of {@link
		 * UiFlowLayout}.
		 *
		 * @return
		 */
		protected UiLayout getContentLayout()
		{
			return new UiFlowLayout();
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

		/***************************************
		 * Internal method to set the index of this row in the table list.
		 *
		 * @param nIndex The new index
		 */
		void setIndex(int nIndex)
		{
			nRowIndex = nIndex;
			setRowItemStyle();
		}

		/***************************************
		 * Sets the style of the parent item according to the row state.
		 */
		private void setRowItemStyle()
		{
			String sItemStyle = nRowIndex % 2 == 1 ? "odd" : "even";

			if (bSelected)
			{
				sItemStyle += " selected";
			}

			rRowItem.style().styleName(sItemStyle);
		}
	}
}
