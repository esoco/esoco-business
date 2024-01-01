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
package de.esoco.data.element;

import de.esoco.lib.model.DataModel;
import de.esoco.lib.model.HierarchicalDataModel;
import de.esoco.lib.model.ListDataModel;
import de.esoco.lib.property.Editable;
import de.esoco.lib.property.Flags;
import de.esoco.lib.property.HasId;
import de.esoco.lib.property.Indexed;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * A simple hierarchical data object to efficiently transfer query data between
 * client and server. An object contains a list of string values and references
 * to parent and child objects. It also implements {@link HierarchicalDataModel}
 * so that it can be used directly in UI components.
 *
 * @author eso
 */
public class HierarchicalDataObject
	implements HierarchicalDataModel<String>, HasId<String>, Flags<String>,
	Indexed, Editable, Serializable {

	private static final long serialVersionUID = 1L;

	// the fields must be package accessible for the custom field serializer
	String id;

	int index;

	boolean editable;

	List<String> values;

	Collection<String> flags;

	DataModel<DataModel<String>> children;

	/**
	 * Creates a new readonly instance without children or flags and an
	 * index of
	 * zero.
	 *
	 * @param id     This object's ID
	 * @param values The attribute values
	 */
	public HierarchicalDataObject(String id, List<String> values) {
		this(id, 0, values, false, null);
	}

	/**
	 * Creates a new instance without children. The reference arguments will
	 * not
	 * be copied but will be used directly.
	 *
	 * @param id       The object's ID
	 * @param index    The index of this object
	 * @param values   The attribute values
	 * @param editable FALSE to mark the instance as readonly
	 * @param flags    The string flags for the object or NULL for none
	 */
	public HierarchicalDataObject(String id, int index, List<String> values,
		boolean editable, Collection<String> flags) {
		this.id = id;
		this.index = index;
		this.editable = editable;
		this.values = values;
		this.flags = flags;
	}

	/**
	 * Creates a new instance. The reference arguments will not be copied but
	 * will be used directly.
	 *
	 * @param id       The object's ID
	 * @param index    The index of this object
	 * @param values   The attribute values
	 * @param editable FALSE to mark the instance as readonly
	 * @param flags    The string flags for the object or NULL for none
	 * @param children The list of child objects (NULL or empty for none)
	 */
	public HierarchicalDataObject(String id, int index, List<String> values,
		boolean editable, Collection<String> flags,
		List<DataModel<String>> children) {
		this(id, index, values, editable, flags);

		if (children != null) {
			this.children = new ListDataModel<DataModel<String>>("", children);
		}
	}

	/**
	 * Creates a new instance. The reference arguments will not be copied but
	 * will be used directly.
	 *
	 * @param id       The object's ID
	 * @param index    The index of this object
	 * @param values   The attribute values
	 * @param editable FALSE to mark the instance as readonly
	 * @param flags    The string flags for the object or NULL for none
	 * @param children The child data model (NULL or empty for none)
	 */
	public HierarchicalDataObject(String id, int index, List<String> values,
		boolean editable, Collection<String> flags,
		DataModel<DataModel<String>> children) {
		this(id, index, values, editable, flags);

		this.children = children;
	}

	/**
	 * Clear a certain flag in this instance.
	 *
	 * @param flag The flag to clear
	 */
	public final void clearFlag(String flag) {
		if (flags != null) {
			flags.remove(flag);
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object == null || getClass() != object.getClass()) {
			return false;
		}

		HierarchicalDataObject other = (HierarchicalDataObject) object;

		return id.equals(other.id) && values.equals(other.values) &&
			getFlags().equals(other.getFlags()) && editable == other.editable &&
			(children == null && other.children == null ||
				children != null && children.equals(other.children));
	}

	/**
	 * @see HierarchicalDataModel#getChildModels()
	 */
	@Override
	public DataModel<DataModel<String>> getChildModels() {
		return children;
	}

	/**
	 * @see HierarchicalDataModel#getElement(int)
	 */
	@Override
	public String getElement(int index) {
		return values.get(index);
	}

	/**
	 * @see HierarchicalDataModel#getElementCount()
	 */
	@Override
	public int getElementCount() {
		return values.size();
	}

	/**
	 * @see Flags#getFlags()
	 */
	@Override
	public final Collection<String> getFlags() {
		return flags != null ? flags : Collections.emptySet();
	}

	/**
	 * @see HasId#getId()
	 */
	@Override
	public final String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getIndex() {
		return index;
	}

	/**
	 * @see Flags#hasFlag(Object)
	 */
	@Override
	public final boolean hasFlag(String flag) {
		return getFlags().contains(flag);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result =
			prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + (editable ? 1231 : 1237);
		result = prime * result + getFlags().hashCode();
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());

		return result;
	}

	/**
	 * @see Editable#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @see HierarchicalDataModel#iterator()
	 */
	@Override
	public Iterator<String> iterator() {
		return values.iterator();
	}

	/**
	 * Sets a certain flag in this instance.
	 *
	 * @param flag The flag to set
	 */
	public final void setFlag(String flag) {
		if (flags == null) {
			flags = new HashSet<String>();
		}

		flags.add(flag);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "HierarchicalDataObject" + values;
	}
}
