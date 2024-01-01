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
package de.esoco.process.step;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.property.InteractiveInputMode;
import de.esoco.lib.property.ListStyle;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.text.TextConvert;
import de.esoco.process.ProcessRelationTypes.ListAction;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static de.esoco.lib.property.ContentProperties.LABEL;
import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.LayoutProperties.COLUMNS;
import static de.esoco.lib.property.LayoutProperties.ROWS;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.StyleProperties.HAS_IMAGES;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.LIST_STYLE;
import static de.esoco.lib.property.StyleProperties.SORT;

/**
 * A fragment that allows to select a set of values of a certain comparable
 * type.
 *
 * @author eso
 */
public class SelectValues<T extends Comparable<T>> extends InteractionFragment {

	private static final long serialVersionUID = 1L;

	/**
	 * A standard parameter that can be used to display this fragment in a
	 * process step.
	 */
	static {
		RelationTypes.init(SelectValues.class);
	}

	private final String identifier;

	private final Class<T> valueDatatype;

	private Collection<T> allValues;

	private final Collection<T> preferredValues;

	private RelationType<Set<T>> unselectedValuesParam;

	private RelationType<Set<T>> selectedValuesParam;

	private RelationType<ListAction> listActionParam;

	private boolean sortValues;

	private SelectValueListener selectionListener;

	/**
	 * Creates a new instance. The Identifier should be a singular description
	 * in camel case and unique in the context of the current process step. In
	 * the case of enum values it is recommended to use the simple name of the
	 * enum class as the identifier. If the sort parameter is TRUE the list
	 * values will be sorted on the client side after resource expansion if
	 * their datatype implements the {@link Comparable} interface.
	 *
	 * @param identifier      A process step-unique identifier of this instance
	 * @param valueDatatype   The datatype of the values
	 * @param availableValues The available values to select from
	 * @param sortValues      TRUE to sort the list values
	 */
	public SelectValues(String identifier, Class<T> valueDatatype,
		Collection<T> availableValues, boolean sortValues) {
		this(identifier, valueDatatype, availableValues, null);

		this.sortValues = sortValues;
	}

	/**
	 * Creates a new instance that sorts the list values and inserts a list of
	 * preferred values at the beginning of each list.The values will be sorted
	 * according to their natural order (i.e. their {@link Comparable}
	 * implementation). They will also be sorted on the server side before
	 * resource expansion, therefore using this variant only makes sense with
	 * values that are not resources.
	 *
	 * @param identifier      A process step-unique identifier of this instance
	 * @param valueDatatype   The datatype of the values
	 * @param availableValues The available values to select from
	 * @param preferredValues An optional list of preferred values that should
	 *                        be listed first (empty for none)
	 */
	public SelectValues(String identifier, Class<T> valueDatatype,
		Collection<T> availableValues, Collection<T> preferredValues) {
		this.identifier = identifier;
		this.valueDatatype = valueDatatype;
		this.allValues = availableValues;
		this.preferredValues = preferredValues;
	}

	/**
	 * Overridden to show only the selected values if editing is disabled.
	 *
	 * @see InteractionFragment#enableEdit(boolean)
	 */
	@Override
	public void enableEdit(boolean enable) {
		super.enableEdit(enable);

		setEnabled(enable, unselectedValuesParam, listActionParam);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters() {
		return CollectionUtil.listOf(unselectedValuesParam,
			listActionParam, selectedValuesParam);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters() {
		return CollectionUtil.listOf(unselectedValuesParam,
			listActionParam, selectedValuesParam);
	}

	/**
	 * Returns the values that have been selected in this fragment.
	 *
	 * @return A collection containing the selected values
	 */
	public Collection<T> getSelectedValues() {
		return getAllowedElements(selectedValuesParam);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> interactionParam)
		throws Exception {
		ListAction listAction = null;

		if (interactionParam == unselectedValuesParam) {
			listAction = ListAction.ADD_SELECTED;
		} else if (interactionParam == selectedValuesParam) {
			listAction = ListAction.REMOVE_SELECTED;
		} else {
			listAction = getParameter(listActionParam);
		}

		if (listAction != null) {
			switch (listAction) {
				case ADD_ALL:
				case ADD_SELECTED:
					moveAllowedValues(unselectedValuesParam,
						selectedValuesParam, listAction == ListAction.ADD_ALL);
					break;

				case REMOVE_ALL:
				case REMOVE_SELECTED:
					moveAllowedValues(selectedValuesParam,
						unselectedValuesParam,
						listAction == ListAction.REMOVE_ALL);
					break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		setImmediateAction(listActionParam);

		setInteractive(InteractiveInputMode.ACTION, unselectedValuesParam,
			selectedValuesParam);

		setUIFlag(HIDE_LABEL, unselectedValuesParam);
		setUIFlag(SAME_ROW, listActionParam, selectedValuesParam);
		setUIFlag(HAS_IMAGES, listActionParam);

		if (sortValues) {
			setUIFlag(SORT, unselectedValuesParam, selectedValuesParam);
		}

		setUIProperty(2, COLUMNS, listActionParam);

		setUIProperty(LIST_STYLE, ListStyle.LIST, unselectedValuesParam,
			selectedValuesParam);

		setUIProperty(LABEL, "", unselectedValuesParam, selectedValuesParam,
			listActionParam);
		setUIProperty(RESOURCE_ID, "SelectValuesAction", listActionParam);
		setUIProperty(RESOURCE_ID, identifier, unselectedValuesParam,
			selectedValuesParam);
	}

	/**
	 * Initializes a parameter for this fragment. This sets a generic style
	 * name
	 * so that all fragments of this type can be styled similarly. This method
	 * should typically be invoked from the
	 * {@link InteractionFragment#init() init()} method of the parent fragment.
	 *
	 * @param param The parameter that will hold this fragment
	 */
	public void initFragmentParameter(
		RelationType<List<RelationType<?>>> param) {
		setUIProperty(UserInterfaceProperties.STYLE,
			getClass().getSimpleName(),
			param);
	}

	/**
	 * Sets the values that are available for selection.
	 *
	 * @param values The selectable values (can be NULL for none)
	 */
	public void setAvailableValues(Collection<T> values) {
		allValues = values;

		// init with empty collections to prevent NPEs
		setParameter(unselectedValuesParam, new LinkedHashSet<T>());
		setParameter(selectedValuesParam, new LinkedHashSet<T>());
		setAllowedElements(unselectedValuesParam, sortValues(allValues));

		if (getAllowedElements(selectedValuesParam) == null) {
			setAllowedElements(selectedValuesParam, new ArrayList<T>());
		}
	}

	/**
	 * Sets the currently selected values.
	 *
	 * @param values The selected values (may be NULL for none)
	 */
	public void setSelectedValues(Collection<T> values) {
		List<T> availableValues = sortValues(allValues);
		List<T> selectedValues = sortValues(values);

		availableValues.removeAll(selectedValues);
		setAllowedElements(unselectedValuesParam, availableValues);
		setAllowedElements(selectedValuesParam, selectedValues);
	}

	/**
	 * Sets a listener that will be notified if the selection in this instance
	 * changes.
	 *
	 * @param listener The listener
	 */
	public void setSelectionListener(SelectValueListener listener) {
		this.selectionListener = listener;
	}

	/**
	 * Sets the number of rows that are visible in the value lists.
	 *
	 * @param rows The number of visible rows
	 */
	public void setVisibleRows(int rows) {
		setUIProperty(rows, ROWS, unselectedValuesParam, selectedValuesParam);
	}

	/**
	 * @see InteractionFragment#initProgressParameter()
	 */
	@Override
	protected void initProcessStep(Interaction processStep) {
		String id =
			TextConvert.uppercaseIdentifier(TextConvert.toPlural(identifier));

		unselectedValuesParam =
			getTemporaryParameterType("UNSELECTED_" + id, Set.class);

		selectedValuesParam =
			getTemporaryParameterType("SELECTED_" + id, Set.class);

		listActionParam = getTemporaryParameterType("SELECT_" + id + "_ACTION",
			ListAction.class);

		unselectedValuesParam.annotate(MetaTypes.ELEMENT_DATATYPE,
			valueDatatype);
		selectedValuesParam.annotate(MetaTypes.ELEMENT_DATATYPE,
			valueDatatype);

		setAvailableValues(allValues);
	}

	/**
	 * Moves certain allowed values from one parameter to another.
	 *
	 * @param source    The source parameter
	 * @param target    The target parameter
	 * @param allValues TRUE for all allowed values, FALSE for only the
	 *                     selected
	 *                  values in the source parameter
	 */
	void moveAllowedValues(RelationType<? extends Collection<T>> source,
		RelationType<? extends Collection<T>> target, boolean allValues) {
		Collection<T> sourceValues;

		List<T> targetValues = new ArrayList<>(getAllowedElements(target));

		if (allValues) {
			sourceValues = getAllowedElements(source);
			getParameter(source).clear();
		} else {
			sourceValues = getParameter(source);
			getAllowedElements(source).removeAll(sourceValues);
		}

		targetValues.addAll(sourceValues);
		setAllowedElements(target, sortValues(targetValues));
		sourceValues.clear();

		if (selectionListener != null) {
			selectionListener.selectionChanged(this);
		}
	}

	/**
	 * Sorts a collection of values under consideration of the preferred
	 * values.
	 * The original collection will not be modified. If the input collection is
	 * NULL an empty list will be returned.
	 *
	 * @param values The collection to sort
	 * @return A sorted list of values
	 */
	private List<T> sortValues(Collection<T> values) {
		List<T> sortedValues = new ArrayList<>();

		if (values != null) {
			sortedValues.addAll(values);

			if (preferredValues != null) {
				Collection<T> prefValues = Collections.emptySet();

				prefValues = CollectionUtil.intersect(preferredValues, values);

				sortedValues.removeAll(prefValues);
				Collections.sort(sortedValues);
				sortedValues.addAll(0, prefValues);
			}
		}

		return sortedValues;
	}

	/**
	 * An event listener interface that can receive notifications on selection
	 * changes in an instance of {@link SelectValues}.
	 *
	 * @author eso
	 */
	public interface SelectValueListener {

		/**
		 * Will be invoked after the selection has changed.
		 *
		 * @param source The source of the selection change
		 */
		void selectionChanged(SelectValues<?> source);
	}
}
