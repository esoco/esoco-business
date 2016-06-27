//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.process;

import de.esoco.entity.Entity;

import de.esoco.process.step.InteractionFragment;

import java.util.Objects;

import org.obrel.core.RelationType;


/********************************************************************
 * A parameter subclass that manages the relation between the process parameter
 * and an entity attribute.
 *
 * @author eso
 */
public class EntityAttributeParameter<E extends Entity, T>
	extends ParameterBase<T, EntityAttributeParameter<E, T>>
{
	//~ Instance fields --------------------------------------------------------

	private E rEntity;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public EntityAttributeParameter(
		InteractionFragment rFragment,
		RelationType<T>		rEntityAttribute)
	{
		super(rFragment, rEntityAttribute);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Applies the current process parameter value to an entity.
	 */
	public void apply()
	{
		if (rEntity != null)
		{
			fragment().applyDerivedParameter(type(), rEntity);
		}
	}

	/***************************************
	 * Check whether the parameter value is different than the attribute value.
	 *
	 * @return The value changed
	 */
	public boolean isValueChanged()
	{
		boolean bChanged = false;

		if (rEntity != null)
		{
			T rAttrValue = fragment().getDerivedParameterValue(rEntity, type());

			bChanged = !Objects.equals(value(), rAttrValue);
		}

		return bChanged;
	}

	/***************************************
	 * Resets the value of this parameter to the entity attribute value.
	 */
	public void reset()
	{
		if (rEntity != null)
		{
			fragment().collectDerivedParameter(rEntity, type(), false);
		}
		else
		{
			value(null);
		}
	}

	/***************************************
	 * Sets the entity of which the attribute value shall be displayed.
	 *
	 * @param rEntity The entity this parameter references
	 */
	public void setEntity(E rEntity)
	{
		this.rEntity = rEntity;

		reset();
	}
}
