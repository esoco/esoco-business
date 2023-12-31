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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;

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

	private String sIdentifier;

	private Class<T> rValueDatatype;

	private Collection<T> rAllValues;

	private Collection<T> rPreferredValues;

	private RelationType<Set<T>> aUnselectedValuesParam;

	private RelationType<Set<T>> aSelectedValuesParam;

	private RelationType<ListAction> aListActionParam;

	private boolean bSortValues;

	private SelectValueListener rSelectionListener;

	/**
	 * Creates a new instance. The Identifier should be a singular description
	 * in camel case and unique in the context of the current process step. In
	 * the case of enum values it is recommended to use the simple name of the
	 * enum class as the identifier. If the sort parameter is TRUE the list
	 * values will be sorted on the client side after resource expansion if
	 * their datatype implements the {@link Comparable} interface.
	 *
	 * @param sIdentifier      A process step-unique identifier of this
	 *                         instance
	 * @param rValueDatatype   The datatype of the values
	 * @param rAvailableValues The available values to select from
	 * @param bSortValues      TRUE to sort the list values
	 */
	public SelectValues(String sIdentifier, Class<T> rValueDatatype,
		Collection<T> rAvailableValues, boolean bSortValues) {
		this(sIdentifier, rValueDatatype, rAvailableValues, null);

		this.bSortValues = bSortValues;
	}

	/**
	 * Creates a new instance that sorts the list values and inserts a list of
	 * preferred values at the beginning of each list.The values will be sorted
	 * according to their natural order (i.e. their {@link Comparable}
	 * implementation). They will also be sorted on the server side before
	 * resource expansion, therefore using this variant only makes sense with
	 * values that are not resources.
	 *
	 * @param sIdentifier      A process step-unique identifier of this
	 *                         instance
	 * @param rValueDatatype   The datatype of the values
	 * @param rAvailableValues The available values to select from
	 * @param rPreferredValues An optional list of preferred values that should
	 *                         be listed first (empty for none)
	 */
	public SelectValues(String sIdentifier, Class<T> rValueDatatype,
		Collection<T> rAvailableValues, Collection<T> rPreferredValues) {
		this.sIdentifier = sIdentifier;
		this.rValueDatatype = rValueDatatype;
		this.rAllValues = rAvailableValues;
		this.rPreferredValues = rPreferredValues;
	}

	/**
	 * Overridden to show only the selected values if editing is disabled.
	 *
	 * @see InteractionFragment#enableEdit(boolean)
	 */
	@Override
	public void enableEdit(boolean bEnable) {
		super.enableEdit(bEnable);

		setEnabled(bEnable, aUnselectedValuesParam, aListActionParam);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters() {
		return CollectionUtil.<RelationType<?>>listOf(aUnselectedValuesParam,
			aListActionParam, aSelectedValuesParam);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters() {
		return CollectionUtil.<RelationType<?>>listOf(aUnselectedValuesParam,
			aListActionParam, aSelectedValuesParam);
	}

	/**
	 * Returns the values that have been selected in this fragment.
	 *
	 * @return A collection containing the selected values
	 */
	public Collection<T> getSelectedValues() {
		return getAllowedElements(aSelectedValuesParam);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception {
		ListAction eListAction = null;

		if (rInteractionParam == aUnselectedValuesParam) {
			eListAction = ListAction.ADD_SELECTED;
		} else if (rInteractionParam == aSelectedValuesParam) {
			eListAction = ListAction.REMOVE_SELECTED;
		} else {
			eListAction = getParameter(aListActionParam);
		}

		if (eListAction != null) {
			switch (eListAction) {
				case ADD_ALL:
				case ADD_SELECTED:
					moveAllowedValues(aUnselectedValuesParam,
						aSelectedValuesParam,
						eListAction == ListAction.ADD_ALL);
					break;

				case REMOVE_ALL:
				case REMOVE_SELECTED:
					moveAllowedValues(aSelectedValuesParam,
						aUnselectedValuesParam,
						eListAction == ListAction.REMOVE_ALL);
					break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		setImmediateAction(aListActionParam);

		setInteractive(InteractiveInputMode.ACTION, aUnselectedValuesParam,
			aSelectedValuesParam);

		setUIFlag(HIDE_LABEL, aUnselectedValuesParam);
		setUIFlag(SAME_ROW, aListActionParam, aSelectedValuesParam);
		setUIFlag(HAS_IMAGES, aListActionParam);

		if (bSortValues) {
			setUIFlag(SORT, aUnselectedValuesParam, aSelectedValuesParam);
		}

		setUIProperty(2, COLUMNS, aListActionParam);

		setUIProperty(LIST_STYLE, ListStyle.LIST, aUnselectedValuesParam,
			aSelectedValuesParam);

		setUIProperty(LABEL, "", aUnselectedValuesParam, aSelectedValuesParam,
			aListActionParam);
		setUIProperty(RESOURCE_ID, "SelectValuesAction", aListActionParam);
		setUIProperty(RESOURCE_ID, sIdentifier, aUnselectedValuesParam,
			aSelectedValuesParam);
	}

	/**
	 * Initializes a parameter for this fragment. This sets a generic style
	 * name
	 * so that all fragments of this type can be styled similarly. This method
	 * should typically be invoked from the
	 * {@link InteractionFragment#init() init()} method of the parent fragment.
	 *
	 * @param rParam The parameter that will hold this fragment
	 */
	public void initFragmentParameter(
		RelationType<List<RelationType<?>>> rParam) {
		setUIProperty(UserInterfaceProperties.STYLE,
			getClass().getSimpleName(),
			rParam);
	}

	/**
	 * Sets the values that are available for selection.
	 *
	 * @param rValues The selectable values (can be NULL for none)
	 */
	public void setAvailableValues(Collection<T> rValues) {
		rAllValues = rValues;

		// init with empty collections to prevent NPEs
		setParameter(aUnselectedValuesParam, new LinkedHashSet<T>());
		setParameter(aSelectedValuesParam, new LinkedHashSet<T>());
		setAllowedElements(aUnselectedValuesParam, sortValues(rAllValues));

		if (getAllowedElements(aSelectedValuesParam) == null) {
			setAllowedElements(aSelectedValuesParam, new ArrayList<T>());
		}
	}

	/**
	 * Sets the currently selected values.
	 *
	 * @param rValues The selected values (may be NULL for none)
	 */
	public void setSelectedValues(Collection<T> rValues) {
		List<T> aAvailableValues = sortValues(rAllValues);
		List<T> aSelectedValues = sortValues(rValues);

		aAvailableValues.removeAll(aSelectedValues);
		setAllowedElements(aUnselectedValuesParam, aAvailableValues);
		setAllowedElements(aSelectedValuesParam, aSelectedValues);
	}

	/**
	 * Sets a listener that will be notified if the selection in this instance
	 * changes.
	 *
	 * @param rListener The listener
	 */
	public void setSelectionListener(SelectValueListener rListener) {
		this.rSelectionListener = rListener;
	}

	/**
	 * Sets the number of rows that are visible in the value lists.
	 *
	 * @param nRows The number of visible rows
	 */
	public void setVisibleRows(int nRows) {
		setUIProperty(nRows, ROWS, aUnselectedValuesParam,
			aSelectedValuesParam);
	}

	/**
	 * @see InteractionFragment#initProgressParameter()
	 */
	@Override
	protected void initProcessStep(Interaction rProcessStep) {
		String sId =
			TextConvert.uppercaseIdentifier(TextConvert.toPlural(sIdentifier));

		aUnselectedValuesParam =
			getTemporaryParameterType("UNSELECTED_" + sId, Set.class);

		aSelectedValuesParam =
			getTemporaryParameterType("SELECTED_" + sId, Set.class);

		aListActionParam =
			getTemporaryParameterType("SELECT_" + sId + "_ACTION",
				ListAction.class);

		aUnselectedValuesParam.annotate(MetaTypes.ELEMENT_DATATYPE,
			rValueDatatype);
		aSelectedValuesParam.annotate(MetaTypes.ELEMENT_DATATYPE,
			rValueDatatype);

		setAvailableValues(rAllValues);
	}

	/**
	 * Moves certain allowed values from one parameter to another.
	 *
	 * @param rSource    The source parameter
	 * @param rTarget    The target parameter
	 * @param bAllValues TRUE for all allowed values, FALSE for only the
	 *                   selected values in the source parameter
	 */
	void moveAllowedValues(RelationType<? extends Collection<T>> rSource,
		RelationType<? extends Collection<T>> rTarget, boolean bAllValues) {
		Collection<T> rSourceValues;

		List<T> rTargetValues = new ArrayList<>(getAllowedElements(rTarget));

		if (bAllValues) {
			rSourceValues = getAllowedElements(rSource);
			getParameter(rSource).clear();
		} else {
			rSourceValues = getParameter(rSource);
			getAllowedElements(rSource).removeAll(rSourceValues);
		}

		rTargetValues.addAll(rSourceValues);
		setAllowedElements(rTarget, sortValues(rTargetValues));
		rSourceValues.clear();

		if (rSelectionListener != null) {
			rSelectionListener.selectionChanged(this);
		}
	}

	/**
	 * Sorts a collection of values under consideration of the preferred
	 * values.
	 * The original collection will not be modified. If the input collection is
	 * NULL an empty list will be returned.
	 *
	 * @param rValues The collection to sort
	 * @return A sorted list of values
	 */
	private List<T> sortValues(Collection<T> rValues) {
		List<T> aSortedValues = new ArrayList<>();

		if (rValues != null) {
			aSortedValues.addAll(rValues);

			if (rPreferredValues != null) {
				Collection<T> aPrefValues = Collections.emptySet();

				aPrefValues =
					CollectionUtil.intersect(rPreferredValues, rValues);

				aSortedValues.removeAll(aPrefValues);
				Collections.sort(aSortedValues);
				aSortedValues.addAll(0, aPrefValues);
			}
		}

		return aSortedValues;
	}

	/**
	 * An event listener interface that can receive notifications on selection
	 * changes in an instance of {@link SelectValues}.
	 *
	 * @author eso
	 */
	public static interface SelectValueListener {

		/**
		 * Will be invoked after the selection has changed.
		 *
		 * @param rSource The source of the selection change
		 */
		public void selectionChanged(SelectValues<?> rSource);
	}
}
