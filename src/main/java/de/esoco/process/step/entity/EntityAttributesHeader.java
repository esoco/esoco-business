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
package de.esoco.process.step.entity;

import de.esoco.entity.Entity;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.property.HasProperties;
import de.esoco.lib.property.LayoutProperties;
import de.esoco.lib.property.MutableProperties;
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.RelativeSize;
import de.esoco.lib.property.StringProperties;
import de.esoco.process.param.Parameter;
import de.esoco.process.step.InteractionFragment;
import de.esoco.storage.StoragePredicates;
import org.obrel.core.RelationType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A {@link EntityList} header implementation that displays the names of entity
 * attributes and optionally provides sorting and attribute-specific filtering.
 *
 * @author eso
 */
public class EntityAttributesHeader<E extends Entity>
	extends EntityListHeader<E> {

	/**
	 * Enumeration of the available actions in the header.
	 */
	enum HeaderDataAction {CLEAR_FILTER}

	private static final long serialVersionUID = 1L;

	private final Map<RelationType<?>, MutableProperties> columnProperties =
		new LinkedHashMap<>();

	private List<Parameter<?>> filterParams;

	private final Map<RelationType<?>, Predicate<? super E>> columnFilters =
		new HashMap<>();

	/**
	 * Creates a new instance.
	 *
	 * @param entityList The entity list this header belongs to
	 * @param attributes The attribute relation types
	 */
	public EntityAttributesHeader(EntityList<E, ?> entityList,
		RelationType<?>... attributes) {
		this(entityList, Arrays.asList(attributes));
	}

	/**
	 * Creates a new instance.
	 *
	 * @param entityList The entity list this header belongs to
	 * @param attributes The attribute relation types
	 */
	public EntityAttributesHeader(EntityList<E, ?> entityList,
		Collection<RelationType<?>> attributes) {
		super(entityList);

		for (RelationType<?> attr : attributes) {
			columnProperties.put(attr, new StringProperties());
		}
	}

	/**
	 * Sets a column property for an attribute.
	 *
	 * @param attribute The attribute to set the property for
	 * @param property  The property to set
	 * @param value     The property value
	 */
	public <T> void setColumnProperty(RelationType<?> attribute,
		PropertyName<T> property, T value) {
		columnProperties.get(attribute).setProperty(property, value);
	}

	/**
	 * Shortcut to set the width of an attribute column as an integer defining
	 * the column span in the grid layout.
	 *
	 * @param attribute The attribute to set the width for
	 * @param width     The column width
	 */
	@SuppressWarnings("boxing")
	public void setColumnWidth(RelationType<?> attribute, int width) {
		setColumnProperty(attribute, LayoutProperties.COLUMN_SPAN, width);
	}

	/**
	 * Shortcut to set the column width for a column.
	 *
	 * @param attribute     The attribute to set the column width for
	 * @param relativeWidth The relative column width
	 */
	public void setColumnWidth(RelationType<?> attribute,
		RelativeSize relativeWidth) {
		setColumnProperty(attribute, LayoutProperties.RELATIVE_WIDTH,
			relativeWidth);
	}

	/**
	 * Applies the columns filters to the entity list.
	 */
	protected void applyColumnFilters() {
		Predicate<? super E> filter = null;

		for (Predicate<? super E> columnFilter : columnFilters.values()) {
			filter = Predicates.and(filter, columnFilter);
		}

		getEntityList().setExtraCriteria(filter);
	}

	/**
	 * Creates the parameter for a certain attribute filter.
	 *
	 * @param filterPanel The fragment of the filter panel
	 * @param attr        The attribute to create the filter for
	 * @return A parameter instance or NULL if no filter is available
	 */
	protected Parameter<?> createAttributeFilter(
		InteractionFragment filterPanel, RelationType<?> attr) {
		Class<?> datatype = attr.getTargetType();
		String attrName = attr.getSimpleName();
		Parameter<?> attrFilter = null;

		if (datatype == String.class) {
			attrFilter = filterPanel.inputText(attrName);
		} else if (datatype.isEnum()) {
			attrFilter = filterPanel.dropDown(attrName,
				Arrays.asList(datatype.getEnumConstants()));
		} else if (Date.class.isAssignableFrom(datatype)) {
			attrFilter = filterPanel.inputDate(attrName);
		}

		return attrFilter;
	}

	/**
	 * Handle the input in the filter parameter of a column.
	 *
	 * @param attribute   The column attribute
	 * @param filterValue The value to be filtered
	 */
	protected void handleFilterInput(RelationType<?> attribute,
		Object filterValue) {
		if (filterValue != null && !filterValue.toString().isEmpty()) {
			Predicate<? super E> attrCriterion;

			if (attribute.getTargetType() == String.class) {
				attrCriterion = attribute.is(
					StoragePredicates.createWildcardFilter(
						filterValue.toString()));
			} else {
				attrCriterion = attribute.is(Predicates.equalTo(filterValue));
			}

			columnFilters.put(attribute, attrCriterion);
		} else {
			columnFilters.remove(attribute);
		}

		applyColumnFilters();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initDataPanel(InteractionFragment p) {
//		if (filterParams == null)
//		{
//			filterParams = new ArrayList<>(columnProperties.size());
//
//			p.fragmentParam().alignVertical(Alignment.END);
//
//			for (Entry<RelationType<?>, MutableProperties> column :
//				 columnProperties.entrySet())
//			{
//				RelationType<?> attr		 = column.getKey();
//				HasProperties	properties	 = column.getValue();
//				Parameter<?>	filterParam = createAttributeFilter(p, attr);
//
//				if (filterParam == null)
//				{
//					filterParam = p.label("");
//				}
//				else
//				{
//					filterParam.onUpdate(v -> handleFilterInput(attr, v));
//				}
//
//				filterParam.sameRow().hideLabel();
//				applyColumnProperties(filterParam, properties);
//				filterParams.add(filterParam);
//
//				iconButtons(HeaderDataAction.class).alignHorizontal(Alignment
//				.END)
//												   .onAction
//												   (this::handleHeaderDataAction);
//			}
//		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initTitlePanel(InteractionFragment p) {
		// set the style name of the parent fragment (the actual header)
		fragmentParam().style(EntityAttributesHeader.class.getSimpleName());

		for (Entry<RelationType<?>, MutableProperties> column :
			columnProperties.entrySet()) {
			RelationType<?> attr = column.getKey();
			HasProperties properties = column.getValue();
			Parameter<String> titleLabel =
				createColumnTitle(p, attr, properties);

			titleLabel.sameRow();
			applyColumnProperties(titleLabel, properties);
		}
	}

	/**
	 * Applies the properties for a certain colum to the respective parameter.
	 *
	 * @param columnParam The column parameter
	 * @param properties  The properties to apply
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void applyColumnProperties(Parameter<?> columnParam,
		HasProperties properties) {
		for (PropertyName property : properties.getPropertyNames()) {
			columnParam.set(property, properties.getProperty(property, null));
		}
	}

	/**
	 * Handles the actions for the header's data section.
	 *
	 * @param action The action
	 */
	@SuppressWarnings("unused")
	private void handleHeaderDataAction(HeaderDataAction action) {
		if (action == HeaderDataAction.CLEAR_FILTER) {
			for (Parameter<?> filterParam : filterParams) {
				filterParam.value(null);
			}

			columnFilters.clear();
			applyColumnFilters();
		}
	}
}
