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
package de.esoco.process.ui.composite;

import de.esoco.data.element.DataElement;
import de.esoco.lib.expression.monad.Option;
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
import de.esoco.process.ui.UiBuilder;
import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiLayout;
import de.esoco.process.ui.component.UiIcon;
import de.esoco.process.ui.component.UiIconButton;
import de.esoco.process.ui.component.UiLink;
import de.esoco.process.ui.composite.UiListPanel.ExpandableListStyle;
import de.esoco.process.ui.composite.UiListPanel.Item;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.event.UiHasActionEvents;
import de.esoco.process.ui.event.UiHasUpdateEvents;
import de.esoco.process.ui.graphics.UiIconSupplier;
import de.esoco.process.ui.graphics.UiImageResource;
import de.esoco.process.ui.graphics.UiMaterialIcon;
import de.esoco.process.ui.layout.UiColumnGridLayout;
import de.esoco.process.ui.layout.UiFlowLayout;
import de.esoco.process.ui.style.UiStyle;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.type.StandardTypes;

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

import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.RELATIVE_WIDTH;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.StateProperties.NO_EVENT_PROPAGATION;
import static de.esoco.process.ProcessRelationTypes.CLIENT_LOCALE;

/**
 * A table-like rendering of components in a list. The header and data areas are
 * rendered with {@link UiListPanel} instances where the list items represent
 * the header and data rows. The table appearance is achieved by using the same
 * column grid layout for the item content.
 *
 * @author eso
 */
public class UiTableList<T> extends UiComposite<UiTableList<T>>
	implements HasSelection<UiTableList<T>.Row> {

	private final UiListPanel headerPanel;

	private final Item headerItem;

	private final UiContainer<?> tableHeader;

	private final UiListPanel dataList;

	private final UiContainer<?> emptyTableInfo;

	private final List<Column<?>> columns = new ArrayList<>();

	private final List<Row> rows = new ArrayList<>();

	private DataProvider<T> dataProvider;

	private Row selectedRow = null;

	private String columnPrefix = null;

	private Column<?> sortColumn = null;

	private BiConsumer<UiBuilder<?>, T> rowContentBuilder;

	private Consumer<Column<?>> handleColumnSelection;

	private Consumer<Row> handleRowSelection;

	/**
	 * Creates a new instance with rows that can be expanded by selecting their
	 * header area. Expanding an item will reveal the item content and hide any
	 * other previously expanded item content.
	 *
	 * @param parent The parent container
	 */
	public UiTableList(UiContainer<?> parent) {
		this(parent, null);
	}

	/**
	 * Creates a new instance with rows that can be expanded by selecting their
	 * header area. Expanding an item will reveal the item content and hide any
	 * other previously expanded item content.
	 *
	 * @param parent      The parent container
	 * @param expandStyle The expand style
	 */
	public UiTableList(UiContainer<?> parent,
		Option<ExpandableListStyle> expandStyle) {
		super(parent, new UiFlowLayout());

		headerPanel = new UiListPanel(this);
		headerItem = headerPanel.addItem();
		tableHeader = headerItem
			.createHeaderPanel(new UiColumnGridLayout())
			.getContainer();
		dataList = new UiListPanel(this, expandStyle);
		emptyTableInfo = new UiLayoutPanel(this, new UiFlowLayout());

		emptyTableInfo.hide();

		headerPanel
			.style()
			.addStyleName(UiTableList.class.getSimpleName() + "Header");
		dataList
			.style()
			.addStyleName(UiTableList.class.getSimpleName() + "Data");
	}

	/**
	 * Adds a column to this table.
	 *
	 * @param getColumnData A function that retrieves the column value of a
	 *                      table cell from a row data object
	 * @return The new column
	 */
	public <V> Column<V> addColumn(Function<? super T, V> getColumnData) {
		Objects.requireNonNull(getColumnData);

		Column<V> column = createColumn(getColumnData);

		columns.add(column);

		for (Row row : rows) {
			row.addColumnComponent(column);
		}

		return column;
	}

	/**
	 * Adds multiple columns at once.
	 *
	 * @param columnDataReaders The
	 */
	@SuppressWarnings("unchecked")
	public void addColumns(Function<? super T, ?>... columnDataReaders) {
		for (Function<? super T, ?> getColumnData : columnDataReaders) {
			addColumn(getColumnData);
		}
	}

	/**
	 * Adds components that will be displayed if a table is empty, i.e. it has
	 * now visible data rows. The argument is a function that receives an UI
	 * builder that must be used to build the empty table info component. This
	 * component will then be displayed below the table header.
	 *
	 * <p>The application should not make assumptions about the layout of the
	 * builder's container and only add a single component. If it needs a more
	 * complex UI it should add a container with the required layout.</p>
	 *
	 * @param createEmtpyTableInfo A consumer that receives a builder for the
	 *                             empty table info area
	 */
	public void addEmptyTableInfo(Consumer<UiBuilder<?>> createEmtpyTableInfo) {
		createEmtpyTableInfo.accept(emptyTableInfo.builder());
	}

	/**
	 * Creates and returns a container that allows to add expanded content to
	 * the header of this list. The content will be displayed if the header is
	 * expanded by clicking on it. This method must be invoked before the list
	 * is rendered for the first time because it needs to modify the styles of
	 * the header components to support expansion.
	 *
	 * @param layout The layout of the expanded header content panel the
	 *                  builder
	 *               is created for
	 * @return The container for the expanded header content
	 */
	public UiContainer<?> addExpandedHeader(UiLayout layout) {
		headerPanel.expandStyle(Option.of(ExpandableListStyle.EXPAND));

		UiIconButton indicator = tableHeader
			.builder()
			.addIconButton(UiMaterialIcon.MORE_VERT)
			.tooltip("ttExpandedListHeader");

		indicator.style().styleName("ExpandableHeaderIndicator");
		indicator.cell().colSpan(1);

		return headerItem.builder().addPanel(layout);
	}

	/**
	 * Returns the columns of this table.
	 *
	 * @return The column list
	 */
	public List<Column<?>> getColumns() {
		return columns;
	}

	/**
	 * Returns the provider of the table data.
	 *
	 * @return The table row data provider
	 */
	public final DataProvider<T> getData() {
		return dataProvider;
	}

	/**
	 * Returns the data rows of this table.
	 *
	 * @return The data row list
	 */
	public List<Row> getRows() {
		return rows;
	}

	/**
	 * Returns the currently selected row.
	 *
	 * @return The selected or (NULL for none)
	 */
	@Override
	public Row getSelection() {
		return selectedRow;
	}

	/**
	 * Registers a listener for column selections (i.e. clicks on column
	 * headers). The listener will be invoked with the respective column as
	 * it's
	 * argument.
	 *
	 * @param handleColumnSelection The column selection handler
	 * @return This instance
	 */
	public UiTableList<T> onColumnSelection(
		Consumer<Column<?>> handleColumnSelection) {
		this.handleColumnSelection = handleColumnSelection;

		return this;
	}

	/**
	 * Registers a listener for row selections (i.e. clicks on rows). The
	 * listener will be invoked with the respective row as it's argument.
	 *
	 * @param handleRowSelection The row selection handler
	 * @return This instance
	 */
	public UiTableList<T> onRowSelection(Consumer<Row> handleRowSelection) {
		this.handleRowSelection = handleRowSelection;

		return this;
	}

	/**
	 * Removes a certain row from this table.
	 *
	 * @param row The row to remove
	 */
	public void removeRow(Row row) {
		dataList.removeItem(row.rowItem);
		rows.remove(row);
	}

	/**
	 * Sets the prefix to be used for column titles.
	 *
	 * @param prefix The column title prefix
	 */
	public void setColumnPrefix(String prefix) {
		columnPrefix = prefix;
	}

	/**
	 * Sets the data provider of the table row data. This will immediately
	 * build
	 * the table rows from the data so all necessary initializations of this
	 * table should have been performed already (e.g. settings columns or an
	 * expanded row builder).
	 *
	 * @param rowDataProvider The data provider that returns the table rows
	 */
	public void setData(DataProvider<T> rowDataProvider) {
		Objects.requireNonNull(rowDataProvider);

		dataList.clear();
		rows.clear();
		dataProvider = rowDataProvider;

		for (Column<?> column : columns) {
			column.dataAvailable(dataProvider);
		}

		// only update if the container has been built already, otherwise leave
		// it to the build() method
		if (isBuilt()) {
			updateData();
		}
	}

	/**
	 * Sets a consumer that will be invoked to build the content of expanded
	 * rows if this table has an expansion style. This can be used for rows
	 * with
	 * simple row content where the full content can be build at once. For
	 * complex cases where the expanded row content should be updated only upon
	 * row selection a {@link Row} subclass should be used instead with
	 * overridden methods {@link Row#initExpandedContent(UiBuilder)} and
	 * {@link Row#updateExpandedContent()}.
	 *
	 * <p>The argument is a binary consumer that will be invoked with the
	 * builder for the row content container and the data object of the
	 * row.</p>
	 *
	 * @param builder The builder for the row content
	 */
	public void setExpandedRowBuilder(BiConsumer<UiBuilder<?>, T> builder) {
		rowContentBuilder = builder;
	}

	/**
	 * Sets the selection to a certain row.
	 *
	 * @param row The row to select or NULL for no selection
	 */
	@Override
	public void setSelection(Row row) {
		handleRowSelection(row, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buildContent(UiBuilder<?> builder) {
		if (dataProvider != null) {
			updateData();
		}
	}

	/**
	 * Creates a new column. Subclasses can override this method to return
	 * their
	 * own column subclasses, e.g. to handle special formatting or value
	 * parsing.
	 *
	 * @param getColumnData The column data access function
	 * @return The new column instance
	 */
	protected <V> Column<V> createColumn(Function<? super T, V> getColumnData) {
		return new Column<>(tableHeader, getColumnData);
	}

	/**
	 * Creates a new row. Subclasses can override this method to return their
	 * own row subclasses, e.g. to handle the row content.
	 *
	 * @param item    The row item
	 * @param rowData The row data
	 * @return A new row instance
	 */
	protected Row createRow(Item item, T rowData) {
		return new Row(item, rowData);
	}

	/**
	 * Update the rows of this table. This will also adjust the number of table
	 * rows (i.e. add or remove rows) to match the size of the given data set.
	 *
	 * @param firstRow The index of the first row to display
	 * @param count    The number of rows to display
	 */
	protected void displayRows(int firstRow, int count) {
		int rowIndex = 0;

		for (T rowData : dataProvider.getData(firstRow, count)) {
			if (rowIndex < rows.size()) {
				Row row = rows.get(rowIndex);

				row.update(rowData);

				if (rowContentBuilder != null) {
					// rebuild content if now row subclass with a method
					// updateExpandedContent is used
					row.rowItem.remove(row.contentPanel);
					row.initContent();
				}
			} else {
				Row row = createRow(dataList.addItem(), rowData);

				rows.add(row);
				row.setIndex(rowIndex);
				row.initContent();
			}

			rowIndex++;
		}

		while (rowIndex < rows.size()) {
			removeRow(rows.get(rowIndex));
		}

		emptyTableInfo.setVisible(
			rows.size() == 0 && emptyTableInfo.getComponents().size() > 0);
	}

	/**
	 * Updates the table display from the data provider that has been set
	 * through {@link #setData(DataProvider)}. The default implementation
	 * renders all data objects from the provider. Subclasses can override this
	 * method if they need to display only part of the data, e.g. for a paging
	 * table.
	 */
	protected void updateData() {
		displayRows(0, dataProvider.size());
	}

	/**
	 * Handles the selection event of a certain row.
	 *
	 * @param row       The selected row
	 * @param fireEvent TRUE to notify a row selection listener if available
	 */
	void handleRowSelection(Row row, boolean fireEvent) {
		if (selectedRow != row) {
			if (selectedRow != null) {
				selectedRow.setSelected(false);
			}

			selectedRow = row;

			if (selectedRow != null) {
				selectedRow.setSelected(true);
				selectedRow.updateExpandedContent();
			}

			dataList.set(selectedRow != null ? selectedRow.getIndex() : -1,
				CURRENT_SELECTION);
		}

		if (fireEvent && handleRowSelection != null) {
			handleRowSelection.accept(row);
		}
	}

	/**
	 * The component that describes a table column.
	 *
	 * @author eso
	 */
	public class Column<V> extends UiComposite<Column<V>> {

		private final Function<? super T, V> getColumnData;

		private final UiLink columnTitle;

		private final UiStyle componentStyle = new UiStyle();

		private Class<? super V> datatype;

		private Class<?> displayDatatype;

		private Function<UiBuilder<?>, UiComponent<?, ?>> displayFactory;

		private BiConsumer<UiComponent<?, ?>, V> displayUpdate;

		private SortDirection initialSortDirection;

		private Function<V, String> valueFormat;

		private Consumer<V> actionHandler;

		private Consumer<V> updateHandler;

		/**
		 * Creates a new instance that retrieves the column value from a row
		 * data object with the given value access function. If the function is
		 * an instance of {@link RelationType} the column datatype will be
		 * queried from it. Otherwise the method {@link #datatype(Class)}
		 * should
		 * be invoked to set the column datatype or else some functionality
		 * like
		 * sorting and value formatting may not be available.
		 *
		 * @param parent        The parent container
		 * @param getColumnData A function that retrieves the column value from
		 *                      a row data object
		 */
		@SuppressWarnings("unchecked")
		Column(UiContainer<?> parent, Function<? super T, V> getColumnData) {
			super(parent, new UiFlowLayout());

			this.getColumnData = getColumnData;

			if (getColumnData instanceof RelationType) {
				datatype(((RelationType<V>) getColumnData).getTargetType());
			}

			columnTitle = new UiLink(this, "")
				.set(NO_EVENT_PROPAGATION)
				.onClick(v -> handleColumnSelection());
		}

		/**
		 * Returns a {@link UiStyle} object that holds styles that will be
		 * applied to all components in a certain column.
		 *
		 * @return The component style
		 */
		public UiStyle componentStyle() {
			return componentStyle;
		}

		/**
		 * Sets the datatype of this column. If the value access function is an
		 * instance of {@link RelationType} the datatype will be determined
		 * automatically.
		 *
		 * @param datatype The column datatype class
		 * @return This instance
		 */
		public Column<V> datatype(Class<? super V> datatype) {
			this.datatype = datatype;

			return this;
		}

		/**
		 * Sets a factory function for the display components of this column.
		 * The function must return a new component that has been created in
		 * the
		 * container of the given UI builder. If a display factory is set it
		 * will be used to create the display components for this column in
		 * each
		 * row. Otherwise default display components will be created. If the
		 * components created by the factory need special update values the
		 * method {@link #updateWith(BiConsumer)} also needs to be invoked to
		 * set an update function.
		 *
		 * @param displayFactory The display component factory function
		 * @return This instance
		 */
		public Column<V> displayWith(
			Function<UiBuilder<?>, UiComponent<?, ?>> displayFactory) {
			this.displayFactory = displayFactory;

			return this;
		}

		/**
		 * Sets a function that will be used for format values of this
		 * column as
		 * a string. The function must be able to handle NULL values if the
		 * column value in a certain rows can be NULL. It may return NULL
		 * values
		 * which will be rendered as an empty cell.
		 *
		 * @param valueFormat A function that formats values as a string
		 * @return This instance
		 */
		public Column<V> formatWith(Function<V, String> valueFormat) {
			this.valueFormat = valueFormat;

			return this;
		}

		/**
		 * Returns the value of this column from a data object.
		 *
		 * @param dataObject The data object
		 * @return The column value (can be NULL)
		 */
		public V getColumnValue(T dataObject) {
			return getColumnData.apply(dataObject);
		}

		/**
		 * Adds an event handler that should be invoked if the column component
		 * receives an action event as defined by {@link UiHasActionEvents}.
		 * The
		 * handler will receive the value of the column in the respective row.
		 * This must be used in conjunction with {@link #displayWith(Function)}
		 * because the default column components don't produce events.
		 *
		 * @param handler The event handler
		 * @return This instance
		 * @see #displayWith(Function)
		 * @see #onUpdate(Consumer)
		 */
		public Column<V> onAction(Consumer<V> handler) {
			actionHandler = handler;

			return this;
		}

		/**
		 * Adds an event handler that should be invoked if the column component
		 * receives an action event as defined by {@link UiHasUpdateEvents}.
		 * The
		 * handler will receive the value of the column in the respective row.
		 * This must be used in conjunction with {@link #displayWith(Function)}
		 * because the default column components don't produce events.
		 *
		 * @param handler The event handler
		 * @return This instance
		 * @see #displayWith(Function)
		 * @see #onAction(Consumer)
		 */
		public Column<V> onUpdate(Consumer<V> handler) {
			updateHandler = handler;

			return this;
		}

		/**
		 * Sets the title of this column.
		 *
		 * @param title The new column title
		 */
		public void setTitle(String title) {
			columnTitle.setText(title);
		}

		/**
		 * Applies or removes sorting of this column in the given direction.
		 *
		 * @param direction The sort direction or NULL to remove explicit
		 *                  sorting
		 * @return This instance
		 */
		public Column<V> sort(SortDirection direction) {
			if (dataProvider == null) {
				initialSortDirection = direction;
			} else {
				setSorting(direction);
			}

			return this;
		}

		/**
		 * A shortcut method for {@link #sort(SortDirection)} with ascending
		 * direction.
		 *
		 * @return This instance
		 */
		public Column<V> sortAscending() {
			return sort(SortDirection.ASCENDING);
		}

		/**
		 * A shortcut method for {@link #sort(SortDirection)} with descending
		 * direction.
		 *
		 * @return This instance
		 */
		public Column<V> sortDescending() {
			return sort(SortDirection.DESCENDING);
		}

		/**
		 * @see de.esoco.process.ui.UiContainer#toString()
		 */
		@Override
		public String toString() {
			return super.toString() + fragment().getUIProperties(type());
		}

		/**
		 * Sets a consumer that will be invoked to update display components of
		 * this column. This is typically used when an application defines
		 * their
		 * own display components through {@link #displayWith(Function)}.
		 *
		 * @param displayUpdate A binary consumer that updates the given
		 *                      component with a new column value
		 * @return This instance
		 */
		public Column<V> updateWith(
			BiConsumer<UiComponent<?, ?>, V> displayUpdate) {
			this.displayUpdate = displayUpdate;

			return this;
		}

		/**
		 * Sets the relative width of this column.
		 *
		 * @param width The relative size constant for the column width
		 * @return This instance for concatenation
		 */
		public final Column<V> width(RelativeSize width) {
			return set(RELATIVE_WIDTH, width);
		}

		/**
		 * Sets the width of this column as an absolute number of layout
		 * columns. The number must be less or equal to the total number of
		 * layout columns available (see {@link UiLayout#getColumns()}).
		 *
		 * @param gridColumns The number of layout columns this column should
		 *                    span
		 * @return This instance for concatenation
		 */
		public final Column<V> width(int gridColumns) {
			return set(gridColumns, COLUMN_SPAN);
		}

		/**
		 * Adds a display component for this column and the corresponding value
		 * in a certain data object.
		 *
		 * @param builder The builder to create the component with
		 * @param data    A function that provides access to the data object
		 * @return The new component
		 */
		protected UiComponent<?, ?> addDisplayComponent(UiBuilder<?> builder,
			Supplier<T> data) {
			UiComponent<?, ?> component = null;

			if (displayFactory != null) {
				component = displayFactory.apply(builder);
			} else {
				Class<?> componentDatatype =
					displayDatatype != null ? displayDatatype : datatype;

				if (componentDatatype != null) {
					if (componentDatatype.isEnum() ||
						UiIconSupplier.class.isAssignableFrom(
							componentDatatype)) {
						component = builder.addIcon(null);
					}
				}

				if (component == null) {
					component = builder.addLabel("");
				}
			}

			if (actionHandler != null &&
				component instanceof UiHasActionEvents) {
				((UiHasActionEvents<?, ?>) component).onAction(
					v -> actionHandler.accept(getColumnValue(data.get())));
			}

			if (updateHandler != null &&
				component instanceof UiHasUpdateEvents) {
				((UiHasUpdateEvents<?, ?>) component).onUpdate(
					v -> updateHandler.accept(getColumnValue(data.get())));
			}

			updateDisplay(component, data.get());

			return component;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void applyProperties() {
			if (!isBuilt()) {
				set(StyleProperties.HIDE_LABEL);
			}

			super.applyProperties();

			applyColumnTitle();
		}

		/**
		 * Will be notified when the table data is available.
		 *
		 * @param dataProvider The table data provider
		 */
		protected void dataAvailable(DataProvider<T> dataProvider) {
			if (initialSortDirection != null) {
				setSorting(initialSortDirection);
			}
		}

		/**
		 * Formats a value into a string representation.
		 *
		 * @param value The value to format
		 * @return The formatted string
		 */
		protected String formatAsString(V value) {
			String valueText = null;

			if (valueFormat != null) {
				valueText = valueFormat.apply(value);
			} else if (value != null) {
				if (value.getClass().isEnum()) {
					valueText = DataElement.createItemResource(value);
				} else if (value instanceof Date) {
					Locale locale = fragment().getParameter(CLIENT_LOCALE);

					DateFormat dateFormat =
						SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT,
							locale);

					// on first format of a data assign a formatting function
					// that will be used subsequently
					valueFormat = v -> v != null ? dateFormat.format(v) : null;

					valueText = valueFormat.apply(value);
				} else {
					valueText = value.toString();
				}
			}

			if (valueText == null) {
				valueText = " ";
			}

			return valueText;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getComponentStyleName() {
			return UiTableList.this.getComponentStyleName() +
				super.getComponentStyleName();
		}

		/**
		 * Handles the selection event of a certain column.
		 */
		protected void handleColumnSelection() {
			setSorting(nextSortDirection());

			if (handleColumnSelection != null) {
				handleColumnSelection.accept(this);
			}
		}

		/**
		 * Updates the display of column data from a certain data object. The
		 * argument component must be one that has been created by the method
		 * {@link #addDisplayComponent(UiBuilder, Supplier)}.
		 *
		 * @param component  The component to update
		 * @param dataObject The data object to read the update value from
		 */
		protected void updateDisplay(UiComponent<?, ?> component,
			T dataObject) {
			V value = getColumnValue(dataObject);

			if (displayUpdate != null) {
				displayUpdate.accept(component, value);
			} else {
				if (component instanceof UiIcon) {
					updateIcon((UiIcon) component, value);
				} else if (component instanceof TextAttribute) {
					((TextAttribute) component).setText(formatAsString(value));
				}
			}
		}

		/**
		 * Updates an icon component from a column value.
		 *
		 * @param icon  The icon component
		 * @param value The column value
		 */
		protected void updateIcon(UiIcon icon, V value) {
			if (value instanceof UiIconSupplier) {
				icon.setIcon(((UiIconSupplier) value).getIcon());
			} else {
				UiImageResource iconResource = null;

				if (value != null) {
					iconResource = new UiImageResource(
						"$im" + DataElement.createItemName(value));
				}

				icon.setIcon(iconResource);
			}
		}

		/**
		 * Checks whether the table data can be filtered by this column.
		 */
		boolean allowsFiltering() {
			return datatype != null && dataProvider != null &&
				dataProvider instanceof HasAttributeFilter;
		}

		/**
		 * Checks whether the table data can be sorted by this column.
		 */
		boolean allowsSorting() {
			return datatype != null && dataProvider != null &&
				Comparable.class.isAssignableFrom(datatype) &&
				dataProvider instanceof HasAttributeSorting;
		}

		/**
		 * Toggles the current sort direction of this column to the next values
		 * (in the order ascending, descending, none).
		 *
		 * @return The new sort direction
		 */
		@SuppressWarnings("unchecked")
		SortDirection nextSortDirection() {
			SortDirection sortDirection = null;

			if (allowsSorting()) {
				sortDirection =
					((HasAttributeSorting<T>) dataProvider).getSortDirection(
						getColumnData);

				if (sortDirection == null) {
					sortDirection = SortDirection.ASCENDING;
				} else if (sortDirection == SortDirection.ASCENDING) {
					sortDirection = SortDirection.DESCENDING;
				} else {
					sortDirection = null;
				}
			}

			return sortDirection;
		}

		/**
		 * Sets the sorting of this column.
		 *
		 * @param direction The sort direction or NULL to remove sorting
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void setSorting(SortDirection direction) {
			if (allowsSorting()) {
				String columnStyle = "";

				if (direction == SortDirection.ASCENDING) {
					columnStyle = "sort ascending";
				} else if (direction == SortDirection.DESCENDING) {
					columnStyle = "sort descending";
				}

				if (sortColumn != null && sortColumn != this) {
					Column prevCol = sortColumn;

					sortColumn = null;
					prevCol.setSorting(null);
				}

				((HasAttributeSorting<T>) dataProvider).applySorting(
					(Function<T, Comparable>) getColumnData, direction);

				columnTitle.style().styleName(columnStyle);
				sortColumn = this;
				updateData();
			}
		}

		/**
		 * Creates the title string of this column by trying to derive it from
		 * the resource ID or, if not availabe, the data access function.
		 */
		private void applyColumnTitle() {
			String title = columnTitle.getText();

			if (title == null || title.isEmpty()) {
				title = get(RESOURCE_ID);

				if (title != null) {
					title = ColumnDefinition.STD_COLUMN_PREFIX + title;
				} else if (getColumnData instanceof RelationType) {
					String prefix = columnPrefix != null ?
					                columnPrefix :
					                ColumnDefinition.STD_COLUMN_PREFIX;

					title = ((RelationType<?>) getColumnData).getSimpleName();
					title = prefix + TextConvert.capitalizedIdentifier(title);
				} else if (getColumnData instanceof Relatable) {
					title =
						((Relatable) getColumnData).get(StandardTypes.NAME);
				} else {
					title = getColumnData.toString();
				}

				columnTitle.setText(title);
			}
		}
	}

	/**
	 * The component that contains the contents of a table row.
	 *
	 * @author eso
	 */
	public class Row extends UiComposite<Row> {

		private final Item rowItem;

		private T rowData;

		private int rowIndex;

		private boolean selected = false;

		private UiLayoutPanel contentPanel;

		/**
		 * Creates a new instance.
		 *
		 * @param rowItem The item to place the row in
		 * @param rowData The row data object
		 */
		protected Row(Item rowItem, T rowData) {
			super(rowItem.getHeader().getContainer(),
				new UiColumnGridLayout());

			this.rowItem = rowItem;
			this.rowData = rowData;

			for (Column<?> column : columns) {
				addColumnComponent(column);
			}

			rowItem
				.getHeader()
				.getContainer()
				.onClickInContainerArea(v -> handleRowSelection(this, true));
		}

		/**
		 * Returns the row data object.
		 *
		 * @return The row data object
		 */
		public final T getData() {
			return rowData;
		}

		/**
		 * Returns the index of the row in it's list.
		 *
		 * @return The row index
		 */
		public final int getIndex() {
			return rowIndex;
		}

		/**
		 * Returns the row's selection state.
		 *
		 * @return The selection state
		 */
		public final boolean isSelected() {
			return selected;
		}

		/**
		 * Sets this row's selection state.
		 *
		 * @param selected The selection state
		 */
		public final void setSelected(boolean selected) {
			this.selected = selected;
			setRowItemStyle();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public UiStyle style() {
			// redirect to the item style
			return rowItem.style();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public UiStyle style(UiStyle style) {
			// redirect to the item style
			return rowItem.style(style);
		}

		/**
		 * Updates this row from a new row data object.
		 *
		 * @param rowData The new row data object
		 */
		public void update(T rowData) {
			this.rowData = rowData;

			List<UiComponent<?, ?>> components = getComponents();
			int index = 0;

			for (Column<?> column : columns) {
				column.updateDisplay(components.get(index++), rowData);
			}

			if (selected) {
				updateExpandedContent();
			}
		}

		/**
		 * Adds a row component for a certain column.
		 *
		 * @param column The column to add the component for
		 */
		protected void addColumnComponent(Column<?> column) {
			column.addDisplayComponent(builder(), this::getData);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void applyProperties() {
			super.applyProperties();

			int index = 0;

			for (Column<?> column : columns) {
				UiComponent<?, ?> component = getComponents().get(index++);

				RelativeSize columnWidth = column.get(RELATIVE_WIDTH);
				Integer columnSpan = column.get(COLUMN_SPAN);

				column.componentStyle().applyPropertiesTo(component);

				if (columnWidth != null) {
					component.set(RELATIVE_WIDTH, columnWidth);
				}

				if (columnSpan != null && columnSpan.intValue() > 0) {
					component.set(columnSpan.intValue(), COLUMN_SPAN);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getComponentStyleName() {
			return UiTableList.this.getComponentStyleName() +
				super.getComponentStyleName();
		}

		/**
		 * Can be overridden to return a specific layout instance for the row
		 * content. Will be invoked before the row content is initialized. The
		 * default implementation returns a new instance of
		 * {@link UiFlowLayout}.
		 *
		 * @return The content layout
		 */
		protected UiLayout getContentLayout() {
			return new UiFlowLayout();
		}

		/**
		 * Can be overridden by subclasses to initialize the content of
		 * expandable rows. To update the content upon selection or setting of
		 * new row data the method {@link #updateExpandedContent()} needs to be
		 * implemented too. The row data is available through
		 * {@link #getData()}.
		 *
		 * <p>The default implementation invokes the expanded row content
		 * builder if one has been set through the method
		 * {@link UiTableList#setExpandedRowBuilder(BiConsumer)}.</p>
		 *
		 * @param builder The builder to build the content with
		 */
		protected void initExpandedContent(UiBuilder<?> builder) {
			if (rowContentBuilder != null) {
				rowContentBuilder.accept(builder, rowData);
			}
		}

		/**
		 * Can be overridden by subclasses that need to update the content of
		 * expandable rows on selection or if a new row data is set. The
		 * content
		 * must be created in {@link #initExpandedContent(UiBuilder)}. The row
		 * data is available through {@link #getData()}.
		 */
		protected void updateExpandedContent() {
		}

		/**
		 * Internal method to init the expanded content of a row. Invokes the
		 * method {@link #initExpandedContent(UiBuilder)} which may be
		 * overridden by subclasses.
		 */
		final void initContent() {
			contentPanel = rowItem.builder().addPanel(getContentLayout());
			initExpandedContent(contentPanel.builder());
		}

		/**
		 * Internal method to set the index of this row in the table list.
		 *
		 * @param index The new index
		 */
		void setIndex(int index) {
			rowIndex = index;
			setRowItemStyle();
		}

		/**
		 * Sets the style of the parent item according to the row state.
		 */
		private void setRowItemStyle() {
			String itemStyle = rowIndex % 2 == 1 ? "odd" : "even";

			if (selected) {
				itemStyle += " selected";
			}

			rowItem.style().styleName(itemStyle);
		}
	}
}
