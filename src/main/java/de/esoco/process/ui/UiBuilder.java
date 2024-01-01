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

import de.esoco.data.element.HierarchicalDataObject;
import de.esoco.entity.Entity;
import de.esoco.lib.model.ColumnDefinition;
import de.esoco.lib.property.Orientation;
import de.esoco.process.ProcessDefinition;
import de.esoco.process.step.InteractionFragment;
import de.esoco.process.step.SubProcessFragment;
import de.esoco.process.ui.component.UiButton;
import de.esoco.process.ui.component.UiCalendar;
import de.esoco.process.ui.component.UiCheckBox;
import de.esoco.process.ui.component.UiCheckBoxes;
import de.esoco.process.ui.component.UiComboBox;
import de.esoco.process.ui.component.UiDataTable;
import de.esoco.process.ui.component.UiDateField;
import de.esoco.process.ui.component.UiDecimalField;
import de.esoco.process.ui.component.UiDropDown;
import de.esoco.process.ui.component.UiFileUpload;
import de.esoco.process.ui.component.UiIcon;
import de.esoco.process.ui.component.UiIconButton;
import de.esoco.process.ui.component.UiImage;
import de.esoco.process.ui.component.UiIntegerField;
import de.esoco.process.ui.component.UiLabel;
import de.esoco.process.ui.component.UiLink;
import de.esoco.process.ui.component.UiList;
import de.esoco.process.ui.component.UiMultiSelectionList;
import de.esoco.process.ui.component.UiPasswordField;
import de.esoco.process.ui.component.UiPhoneNumberField;
import de.esoco.process.ui.component.UiProgressBar;
import de.esoco.process.ui.component.UiPushButtons;
import de.esoco.process.ui.component.UiQueryTable;
import de.esoco.process.ui.component.UiRadioButtons;
import de.esoco.process.ui.component.UiTextArea;
import de.esoco.process.ui.component.UiTextField;
import de.esoco.process.ui.component.UiTitle;
import de.esoco.process.ui.component.UiToggleButtons;
import de.esoco.process.ui.component.UiWebView;
import de.esoco.process.ui.composite.UiCard;
import de.esoco.process.ui.composite.UiListPanel;
import de.esoco.process.ui.composite.UiThumbnail;
import de.esoco.process.ui.container.UiDeckPanel;
import de.esoco.process.ui.container.UiDockPanel;
import de.esoco.process.ui.container.UiLayoutPanel;
import de.esoco.process.ui.container.UiSplitPanel;
import de.esoco.process.ui.container.UiStackPanel;
import de.esoco.process.ui.container.UiSubFragment;
import de.esoco.process.ui.container.UiTabPanel;
import de.esoco.process.ui.graphics.UiIconSupplier;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;

/**
 * Provides factory methods that build UI components in a container.
 *
 * @author eso
 */
public class UiBuilder<C extends UiContainer<C>> {

	private final C container;

	/**
	 * Creates a new instance.
	 *
	 * @param container The container to build the UI in
	 */
	public UiBuilder(C container) {
		this.container = container;
	}

	/**
	 * Adds an arbitrary component to this container by applying a factory
	 * function to this builder's container. This allows to add components
	 * which
	 * don't have an explicit factory method in the builder API.
	 *
	 * <p>It is recommended to create custom components as subclasses of
	 * generic types like {@link UiComposite} because otherwise the Java type
	 * system may not be able to resolve the correct generic type when using
	 * this method.
	 * </p>
	 *
	 * @param create The factory function
	 * @return The new component
	 */
	public <T, V extends UiComponent<T, V>> V add(
		Function<C, ? extends V> create) {
		return create.apply(container);
	}

	/**
	 * Adds a clickable button.
	 *
	 * @param label The button label
	 * @return The new component
	 */
	public UiButton addButton(String label) {
		return new UiButton(container, label);
	}

	/**
	 * Adds a date selector. By default it only provides a calendar date input
	 * but an additional time of day selector can be enabled with the method
	 * {@link UiCalendar#withTimeInput()}.
	 *
	 * @param date The initial date value or NULL for the current date
	 * @return The new component
	 */
	public UiCalendar addCalendar(Date date) {
		return new UiCalendar(container, date);
	}

	/**
	 * Adds a card panel with a title image.
	 *
	 * @param titleImage The card title image
	 * @return The new card
	 */
	public UiCard addCard(UiImageDefinition<?> titleImage) {
		return new UiCard(container, titleImage);
	}

	/**
	 * Adds a card panel with a title image and title text inside the image.
	 *
	 * @param titleImage The title image
	 * @param title      The title text
	 * @return The new card
	 */
	public UiCard addCard(UiImageDefinition<?> titleImage, String title) {
		return new UiCard(container, titleImage, title);
	}

	/**
	 * Adds a card panel.
	 *
	 * @param title The card title (NULL for none)
	 * @param icon  The title icon (NULL for none)
	 * @return The new card
	 */
	public UiCard addCard(String title, UiIconSupplier icon) {
		return new UiCard(container, title, icon);
	}

	/**
	 * Adds a single check boxes that represents a boolean value.
	 *
	 * @param label The check box label
	 * @return The new component
	 */
	public UiCheckBox addCheckBox(String label) {
		return new UiCheckBox(container, label);
	}

	/**
	 * Adds a group of check boxes with string labels.
	 *
	 * @param labels The initial check box labels (may be empty)
	 * @return The new component
	 */
	public UiCheckBoxes<String> addCheckBoxes(String... labels) {
		UiCheckBoxes<String> checkBoxes =
			new UiCheckBoxes<>(container, String.class);

		checkBoxes.addButtons(labels);

		return checkBoxes;
	}

	/**
	 * Adds a group of check boxes with labels derived from an enum. All enum
	 * values of the given type will be pre-set as check boxes.
	 *
	 * @param enumType The enum class for the check box labels
	 * @return The new component
	 */
	public <E extends Enum<E>> UiCheckBoxes<E> addCheckBoxes(
		Class<E> enumType) {
		return new UiCheckBoxes<>(container, enumType);
	}

	/**
	 * Adds a single-line text input field with a drop-down list of value
	 * suggestions.
	 *
	 * @param text The text to edit
	 * @return The new component
	 */
	public UiComboBox addComboBox(String text) {
		return new UiComboBox(container, text);
	}

	/**
	 * Adds a table that displays static data.
	 *
	 * @param data    The data objects to display
	 * @param columns The data columns to display
	 * @return The new component
	 */
	public <E extends Entity> UiDataTable addDataTable(
		Collection<HierarchicalDataObject> data,
		Collection<ColumnDefinition> columns) {
		return new UiDataTable(container, data, columns);
	}

	/**
	 * Adds a date input field with a pop-up date selector. By default it
	 * accepts only date input but additional time input can be enabled with
	 * {@link UiCalendar#withTimeInput()}.
	 *
	 * @param date The initial value or NULL for the current date
	 * @return The new component
	 */
	public UiDateField addDateField(Date date) {
		return new UiDateField(container, date);
	}

	/**
	 * Adds an input field for decimal values.
	 *
	 * @param value The initial value
	 * @return The new component
	 */
	public UiDecimalField addDecimalField(BigDecimal value) {
		return new UiDecimalField(container, value);
	}

	/**
	 * Adds a deck panel.
	 *
	 * @return The new panel
	 */
	public UiDeckPanel addDeckPanel() {
		return new UiDeckPanel(container);
	}

	/**
	 * Adds a dock panel.
	 *
	 * @param orientation The panel orientation
	 * @return The new panel
	 */
	public UiDockPanel addDockPanel(Orientation orientation) {
		return new UiDockPanel(container, orientation);
	}

	/**
	 * Adds a single-line field with a list of selectable elements. If the
	 * datatype is an enum all enum values will be pre-set as the list values.
	 *
	 * @param datatype The datatype of the list elements
	 * @return The new component
	 */
	public <T> UiDropDown<T> addDropDown(Class<T> datatype) {
		return new UiDropDown<>(container, datatype);
	}

	/**
	 * Adds a file upload component.
	 *
	 * @param label The label text
	 * @return The new component
	 */
	public UiFileUpload addFileSelect(String label) {
		return new UiFileUpload(container, label);
	}

	/**
	 * Adds an arbitrary process interaction fragment to this container. This
	 * allows to use fragments in conjunction with process UIs.
	 *
	 * @param fragment iconSupplier The icon supplier
	 * @param layout   The layout to embed the fragment in
	 * @return The new component
	 */
	public UiSubFragment addFragment(InteractionFragment fragment,
		UiLayout layout) {
		return new UiSubFragment(container, fragment, layout);
	}

	/**
	 * Adds a non-interactive icon.
	 *
	 * @param iconSupplier The icon supplier
	 * @return The new component
	 */
	public UiIcon addIcon(UiIconSupplier iconSupplier) {
		return new UiIcon(container, iconSupplier);
	}

	/**
	 * Adds a clickable icon button.
	 *
	 * @param icon The button icon
	 * @return The new component
	 */
	public UiIconButton addIconButton(UiIconSupplier icon) {
		return new UiIconButton(container, icon);
	}

	/**
	 * Adds an image.
	 *
	 * @param image The image definition
	 * @return The new component
	 */
	public UiImage addImage(UiImageDefinition<?> image) {
		return new UiImage(container, image);
	}

	/**
	 * Adds an integer input field. To allow only the input of a certain value
	 * range with spinner controls {@link UiIntegerField#withBounds(int, int)}
	 * can be invoked on it.
	 *
	 * @param value The initial value
	 * @return The new component
	 */
	public UiIntegerField addIntegerField(int value) {
		return new UiIntegerField(container, value);
	}

	/**
	 * Adds a non-interactive label.
	 *
	 * @param text The label text
	 * @return The new component
	 */
	public UiLabel addLabel(String text) {
		return new UiLabel(container, text);
	}

	/**
	 * Adds a clickable link.
	 *
	 * @param label The link label
	 * @return The new component
	 */
	public UiLink addLink(String label) {
		return new UiLink(container, label);
	}

	/**
	 * Adds a list of selectable strings.
	 *
	 * @return The new component
	 */
	public UiList<String> addList() {
		return new UiList<>(container, String.class);
	}

	/**
	 * Adds a list of selectable enum values. All enum values of the given type
	 * will be pre-set as the list values.
	 *
	 * @param enumType enumClass The enum class of the list values
	 * @return The new component
	 */
	public <E extends Enum<E>> UiList<E> addList(Class<E> enumType) {
		return new UiList<>(container, enumType);
	}

	/**
	 * Adds a list of selectable strings.
	 *
	 * @return The new component
	 */
	public UiListPanel addListPanel() {
		return new UiListPanel(container);
	}

	/**
	 * Adds a list of strings that allows to select multiple values.
	 *
	 * @return The new component
	 */
	public UiMultiSelectionList<String> addMultiSelectionList() {
		return new UiMultiSelectionList<>(container, String.class);
	}

	/**
	 * Adds a list of enums that allows to select multiple values. All enum
	 * values of the given type will be pre-set as the list values.
	 *
	 * @param enumType enumClass The enum for the list values
	 * @return The new component
	 */
	public <E extends Enum<E>> UiMultiSelectionList<E> addMultiSelectionList(
		Class<E> enumType) {
		return new UiMultiSelectionList<>(container, enumType);
	}

	/**
	 * Adds a panel with a certain layout.
	 *
	 * @param layout The panel layout
	 * @return The new panel
	 */
	public UiLayoutPanel addPanel(UiLayout layout) {
		return new UiLayoutPanel(container, layout);
	}

	/**
	 * Adds a single-line text input field that hides the input.
	 *
	 * @param text The text to edit
	 * @return The new component
	 */
	public UiPasswordField addPasswordField(String text) {
		return new UiPasswordField(container, text);
	}

	/**
	 * Adds a single-line text input field for the input of international phone
	 * numbers.
	 *
	 * @param phoneNumber The phone number to edit
	 * @return The new component
	 */
	public UiPhoneNumberField addPhoneNumberField(String phoneNumber) {
		return new UiPhoneNumberField(container, phoneNumber);
	}

	/**
	 * Adds a progress bar. The integer value of the bar defines the current
	 * progress in relation to the progress bar bounds. These default to 0 and
	 * 100 and can be changed with {@link UiProgressBar#withBounds(int, int)}.
	 *
	 * @return The new component
	 */
	public UiProgressBar addProgressBar() {
		return new UiProgressBar(container);
	}

	/**
	 * Adds a group of push buttons with string labels.
	 *
	 * @param buttonLabels The initial button labels (may be empty)
	 * @return The new component
	 */
	public UiPushButtons<String> addPushButtons(String... buttonLabels) {
		UiPushButtons<String> pushButtons =
			new UiPushButtons<>(container, String.class);

		pushButtons.addButtons(buttonLabels);

		return pushButtons;
	}

	/**
	 * Adds a group of push buttons with labels derived from an enum. All enum
	 * values of the given type will be pre-set as buttons.
	 *
	 * @param enumType The enum class for the button labels
	 * @return The new component
	 */
	public <E extends Enum<E>> UiPushButtons<E> addPushButtons(
		Class<E> enumType) {
		return new UiPushButtons<>(container, enumType);
	}

	/**
	 * Adds a table that performs an entity query.
	 *
	 * @param entityType The entity type to display
	 * @return The new component
	 */
	public <E extends Entity> UiQueryTable<E> addQueryTable(
		Class<E> entityType) {
		return new UiQueryTable<>(container, entityType);
	}

	/**
	 * Adds a group of radio buttons with string labels.
	 *
	 * @param buttonLabels The initial button labels (may be empty)
	 * @return The new component
	 */
	public UiRadioButtons<String> addRadioButtons(String... buttonLabels) {
		UiRadioButtons<String> radioButtons =
			new UiRadioButtons<>(container, String.class);

		radioButtons.addButtons(buttonLabels);

		return radioButtons;
	}

	/**
	 * Adds a group of radio buttons with labels derived from an enum. All enum
	 * values of the given type will be pre-set as buttons.
	 *
	 * @param enumType The enum class for the button labels
	 * @return The new component
	 */
	public <E extends Enum<E>> UiRadioButtons<E> addRadioButtons(
		Class<E> enumType) {
		return new UiRadioButtons<>(container, enumType);
	}

	/**
	 * Adds a split panel.
	 *
	 * @param orientation The panel orientation
	 * @return The new panel
	 */
	public UiSplitPanel addSplitPanel(Orientation orientation) {
		return new UiSplitPanel(container, orientation);
	}

	/**
	 * Adds a stack panel.
	 *
	 * @return The new panel
	 */
	public UiStackPanel addStackPanel() {
		return new UiStackPanel(container);
	}

	/**
	 * Adds an interactive sub-process to be rendered in a sub-fragment of this
	 * container.
	 *
	 * @param subProcessClass The sub-process definition class
	 * @param layout          The layout of the sub-process fragment
	 * @return The new component
	 */
	public UiSubFragment addSubProcess(
		Class<? extends ProcessDefinition> subProcessClass, UiLayout layout) {
		return addFragment(new SubProcessFragment(subProcessClass), layout);
	}

	/**
	 * Adds a tab panel.
	 *
	 * @return The new panel
	 */
	public UiTabPanel addTabPanel() {
		return new UiTabPanel(container);
	}

	/**
	 * Adds a multi-line text input field.
	 *
	 * @param text The text to edit
	 * @return The new component
	 */
	public UiTextArea addTextArea(String text) {
		return new UiTextArea(container, text);
	}

	/**
	 * Adds a single-line text input field.
	 *
	 * @param text The text to edit
	 * @return The new component
	 */
	public UiTextField addTextField(String text) {
		return new UiTextField(container, text);
	}

	/**
	 * Adds an image thumbnail that opens a larger image in a popup view when
	 * clicked.
	 *
	 * @param image The image definition
	 * @return The new component
	 */
	public UiThumbnail addThumbnail(UiImageDefinition<?> image) {
		return new UiThumbnail(container, image);
	}

	/**
	 * Adds an image thumbnail that opens a larger image in a popup view when
	 * clicked.
	 *
	 * @param thumbImage The thumbnail image
	 * @param fullImage  The larger image
	 * @return The new component
	 */
	public UiThumbnail addThumbnail(UiImageDefinition<?> thumbImage,
		UiImageDefinition<?> fullImage) {
		return new UiThumbnail(container, thumbImage, fullImage);
	}

	/**
	 * Adds a non-interactive title label.
	 *
	 * @param text The title text
	 * @return The new component
	 */
	public UiTitle addTitle(String text) {
		return new UiTitle(container, text);
	}

	/**
	 * Adds a group of radio buttons with labels derived from an enum. All enum
	 * values of the given type will be pre-set as buttons.
	 *
	 * @param enumType The enum class for the button labels
	 * @return The new component
	 */
	public <E extends Enum<E>> UiToggleButtons<E> addToggleButtons(
		Class<E> enumType) {
		return new UiToggleButtons<>(container, enumType);
	}

	/**
	 * Adds a group of radio buttons with string labels.
	 *
	 * @param buttonLabels The initial button labels (may be empty)
	 * @return The new component
	 */
	public UiToggleButtons<String> addToggleButtons(String... buttonLabels) {
		UiToggleButtons<String> toggleButtons =
			new UiToggleButtons<>(container, String.class);

		toggleButtons.addButtons(buttonLabels);

		return toggleButtons;
	}

	/**
	 * Adds a web page display.
	 *
	 * @param url The URL of the page to display
	 * @return The new component
	 */
	public UiWebView addWebView(String url) {
		return new UiWebView(container, url);
	}

	/**
	 * Returns the container that is built by container instance.
	 *
	 * @return The container
	 */
	public final C getContainer() {
		return container;
	}

	/**
	 * Invokes {@link UiLayout#nextRow()} and returns this instance to allow
	 * fluent invocations like <code>nextRow().addButton(...)</code>.
	 *
	 * @return This instance for fluent invocations
	 */
	public UiBuilder<C> nextRow() {
		container.getLayout().nextRow();

		return this;
	}
}
