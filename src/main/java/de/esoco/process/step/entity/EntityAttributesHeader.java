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

import de.esoco.process.Parameter;
import de.esoco.process.step.InteractionFragment;

import de.esoco.storage.StoragePredicates;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.obrel.core.RelationType;


/********************************************************************
 * A {@link EntityList} header implementation that displays the names of entity
 * attributes and optionally provides sorting and attribute-specific filtering.
 *
 * @author eso
 */
public class EntityAttributesHeader<E extends Entity>
	extends EntityListHeader<E>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the available actions in the header.
	 */
	enum HeaderDataAction { CLEAR_FILTER }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Map<RelationType<?>, MutableProperties> aColumnProperties =
		new LinkedHashMap<>();

	private List<Parameter<?>> aFilterParams;

	private Map<RelationType<?>, Predicate<? super E>> aColumnFilters =
		new HashMap<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rEntityList The entity list this header belongs to
	 * @param rAttributes The attribute relation types
	 */
	public EntityAttributesHeader(
		EntityList<E, ?>   rEntityList,
		RelationType<?>... rAttributes)
	{
		this(rEntityList, Arrays.asList(rAttributes));
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rEntityList The entity list this header belongs to
	 * @param rAttributes The attribute relation types
	 */
	public EntityAttributesHeader(
		EntityList<E, ?>			rEntityList,
		Collection<RelationType<?>> rAttributes)
	{
		super(rEntityList);

		for (RelationType<?> rAttr : rAttributes)
		{
			aColumnProperties.put(rAttr, new StringProperties());
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets a column property for an attribute.
	 *
	 * @param rAttribute The attribute to set the property for
	 * @param rProperty  The property to set
	 * @param rValue     The property value
	 */
	public <T> void setColumnProperty(RelationType<?> rAttribute,
									  PropertyName<T> rProperty,
									  T				  rValue)
	{
		aColumnProperties.get(rAttribute).setProperty(rProperty, rValue);
	}

	/***************************************
	 * Shortcut to set the width of an attribute column as an integer defining
	 * the column span in the grid layout.
	 *
	 * @param rAttribute The attribute to set the width for
	 * @param nWidth     The column width
	 */
	@SuppressWarnings("boxing")
	public void setColumnWidth(RelationType<?> rAttribute, int nWidth)
	{
		setColumnProperty(rAttribute, LayoutProperties.COLUMN_SPAN, nWidth);
	}

	/***************************************
	 * Shortcut to set the column width for a column.
	 *
	 * @param rAttribute     The attribute to set the column width for
	 * @param eRelativeWidth The relative column width
	 */
	public void setColumnWidth(
		RelationType<?> rAttribute,
		RelativeSize    eRelativeWidth)
	{
		setColumnProperty(rAttribute,
						  LayoutProperties.RELATIVE_WIDTH,
						  eRelativeWidth);
	}

	/***************************************
	 * Applies the columns filters to the entity list.
	 */
	protected void applyColumnFilters()
	{
		Predicate<? super E> pFilter = null;

		for (Predicate<? super E> pColumnFilter : aColumnFilters.values())
		{
			pFilter = Predicates.and(pFilter, pColumnFilter);
		}

		getEntityList().setExtraCriteria(pFilter);
	}

	/***************************************
	 * Creates the parameter for a certain attribute filter.
	 *
	 * @param  rFilterPanel The fragment of the filter panel
	 * @param  rAttr        The attribute to create the filter for
	 *
	 * @return A parameter instance or NULL if no filter is available
	 */
	protected Parameter<?> createAttributeFilter(
		InteractionFragment rFilterPanel,
		RelationType<?>		rAttr)
	{
		Class<?>     rDatatype   = rAttr.getValueType();
		String		 sAttrName   = rAttr.getSimpleName();
		Parameter<?> aAttrFilter = null;

		if (rDatatype == String.class)
		{
			aAttrFilter = rFilterPanel.inputText(sAttrName);
		}
		else if (rDatatype.isEnum())
		{
			aAttrFilter =
				rFilterPanel.dropDown(sAttrName,
									  Arrays.asList(rDatatype
													.getEnumConstants()));
		}
		else if (Date.class.isAssignableFrom(rDatatype))
		{
			aAttrFilter = rFilterPanel.inputDate(sAttrName);
		}

		return aAttrFilter;
	}

	/***************************************
	 * Handle the input in the filter parameter of a column.
	 *
	 * @param rAttribute   The column attribute
	 * @param rFilterValue The value to be filtered
	 */
	protected void handleFilterInput(
		RelationType<?> rAttribute,
		Object			rFilterValue)
	{
		if (rFilterValue != null && !rFilterValue.toString().isEmpty())
		{
			Predicate<? super E> pAttrCriterion;

			if (rAttribute.getValueType() == String.class)
			{
				pAttrCriterion =
					rAttribute.is(StoragePredicates.createWildcardFilter(rFilterValue
																		 .toString()));
			}
			else
			{
				pAttrCriterion =
					rAttribute.is(Predicates.equalTo(rFilterValue));
			}

			aColumnFilters.put(rAttribute, pAttrCriterion);
		}
		else
		{
			aColumnFilters.remove(rAttribute);
		}

		applyColumnFilters();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void initDataPanel(InteractionFragment p)
	{
//		if (aFilterParams == null)
//		{
//			aFilterParams = new ArrayList<>(aColumnProperties.size());
//
//			p.fragmentParam().alignVertical(Alignment.END);
//
//			for (Entry<RelationType<?>, MutableProperties> rColumn :
//				 aColumnProperties.entrySet())
//			{
//				RelationType<?> rAttr		 = rColumn.getKey();
//				HasProperties	rProperties	 = rColumn.getValue();
//				Parameter<?>	aFilterParam = createAttributeFilter(p, rAttr);
//
//				if (aFilterParam == null)
//				{
//					aFilterParam = p.label("");
//				}
//				else
//				{
//					aFilterParam.onUpdate(v -> handleFilterInput(rAttr, v));
//				}
//
//				aFilterParam.sameRow().hideLabel();
//				applyColumnProperties(aFilterParam, rProperties);
//				aFilterParams.add(aFilterParam);
//
//				iconButtons(HeaderDataAction.class).alignHorizontal(Alignment.END)
//												   .onAction(this::handleHeaderDataAction);
//			}
//		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void initTitlePanel(InteractionFragment p)
	{
		// set the style name of the parent fragment (the actual header)
		fragmentParam().style(EntityAttributesHeader.class.getSimpleName());

		for (Entry<RelationType<?>, MutableProperties> rColumn :
			 aColumnProperties.entrySet())
		{
			RelationType<?>   rAttr		  = rColumn.getKey();
			HasProperties     rProperties = rColumn.getValue();
			Parameter<String> aTitleLabel =
				createColumnTitle(p, rAttr, rProperties);

			aTitleLabel.sameRow();
			applyColumnProperties(aTitleLabel, rProperties);
		}
	}

	/***************************************
	 * Applies the properties for a certain colum to the respective parameter.
	 *
	 * @param rColumnParam The column parameter
	 * @param rProperties  The properties to apply
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void applyColumnProperties(
		Parameter<?>  rColumnParam,
		HasProperties rProperties)
	{
		for (PropertyName rProperty : rProperties.getPropertyNames())
		{
			rColumnParam.set(rProperty,
							 rProperties.getProperty(rProperty, null));
		}
	}

	/***************************************
	 * Handles the actions for the header's data section.
	 *
	 * @param eAction The action
	 */
	@SuppressWarnings("unused")
	private void handleHeaderDataAction(HeaderDataAction eAction)
	{
		if (eAction == HeaderDataAction.CLEAR_FILTER)
		{
			for (Parameter<?> rFilterParam : aFilterParams)
			{
				rFilterParam.value(null);
			}

			aColumnFilters.clear();
			applyColumnFilters();
		}
	}
}
