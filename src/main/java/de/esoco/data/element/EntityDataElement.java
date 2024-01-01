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
package de.esoco.data.element;

import de.esoco.lib.property.PropertyName;

import java.util.List;
import java.util.Set;

/**
 * A data element implementation that holds the attributes of an entity.
 *
 * @author eso
 */
public class EntityDataElement extends DataElementList {

	/**
	 * The name of the attribute data element containing the child
	 */
	public static final String CHILDREN_ELEMENT = "CHILDREN";

	private static final long serialVersionUID = 1L;

	private String childPrefix;

	/**
	 * Creates a new instance.
	 *
	 * @param name        The name of this data element
	 * @param resourceId  The resource to label the data element with
	 * @param childPrefix The prefix string for child elements
	 * @param attributes  The child elements that represent the entity
	 *                    attributes (including children)
	 * @param flags       The optional flags for this data element
	 */
	public EntityDataElement(String name, String resourceId,
		String childPrefix,
		List<DataElement<?>> attributes, Set<Flag> flags) {
		super(name, resourceId, attributes, flags);

		this.childPrefix = childPrefix;
	}

	/**
	 * Default constructor for serialization.
	 */
	EntityDataElement() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityDataElement copy(CopyMode mode,
		PropertyName<?>... copyProperties) {
		EntityDataElement copy =
			(EntityDataElement) super.copy(mode, copyProperties);

		copy.childPrefix = childPrefix;

		return copy;
	}

	/**
	 * Overridden to return the simple name of the entity class.
	 *
	 * @see DataElementList#getChildResourceIdPrefix()
	 */
	@Override
	protected String getChildResourceIdPrefix() {
		return childPrefix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityDataElement newInstance() {
		return new EntityDataElement();
	}
}
