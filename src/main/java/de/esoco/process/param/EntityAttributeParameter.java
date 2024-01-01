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
package de.esoco.process.param;

import de.esoco.entity.Entity;
import de.esoco.process.step.InteractionFragment;
import org.obrel.core.RelationType;

import java.util.Objects;

/**
 * A parameter subclass that manages the relation between the process parameter
 * and an entity attribute.
 *
 * @author eso
 */
public class EntityAttributeParameter<E extends Entity, T>
	extends ParameterBase<T, EntityAttributeParameter<E, T>> {

	private E entity;

	/**
	 * @see ParameterBase#ParameterBase(InteractionFragment, RelationType)
	 */
	public EntityAttributeParameter(InteractionFragment fragment,
		RelationType<T> entityAttribute) {
		super(fragment, entityAttribute);
	}

	/**
	 * Applies the current process parameter value to an entity.
	 */
	public void apply() {
		if (entity != null) {
			fragment().applyDerivedParameter(type(), entity);
		}
	}

	/**
	 * Check whether the parameter value is different than the attribute value.
	 *
	 * @return The value changed
	 */
	public boolean isValueChanged() {
		boolean changed = false;

		if (entity != null) {
			T attrValue = fragment().getDerivedParameterValue(entity, type());
			T paramValue = value();

			changed = !Objects.equals(paramValue, attrValue) &&
				!(attrValue == null && "".equals(paramValue));
		}

		return changed;
	}

	/**
	 * Resets the value of this parameter to the entity attribute value.
	 */
	public void reset() {
		if (entity != null) {
			fragment().collectDerivedParameter(entity, type(), false);
		} else {
			value(null);
		}
	}

	/**
	 * Sets the entity of which the attribute value shall be displayed.
	 *
	 * @param entity The entity this parameter references
	 */
	public void setEntity(E entity) {
		this.entity = entity;

		reset();
	}
}
