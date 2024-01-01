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
package de.esoco.data.element;

import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.StateProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * A data element implementation that contains a hierarchical structure of data
 * elements.
 *
 * @author eso
 */
public class DataElementList extends ListDataElement<DataElement<?>> {

	private static final long serialVersionUID = 1L;

	private static final String PATH_SEPARATOR_STRING =
		String.valueOf(PATH_SEPARATOR_CHAR);

	private List<DataElement<?>> dataElements =
		new ArrayList<DataElement<?>>();

	/**
	 * Creates a new instance that is initialized from a certain set of data
	 * elements. The contents of the collection argument will be copied into
	 * this list element but not the collection itself.
	 *
	 * @param name     The name of this element
	 * @param elements A collection containing the initial elements of this
	 *                    list
	 *                 element (may be NULL)
	 */
	public DataElementList(String name,
		Collection<? extends DataElement<?>> elements) {
		this(name, null, elements, null);
	}

	/**
	 * Creates a new instance with certain data elements and without a
	 * validator
	 * and flags.
	 *
	 * @param name       The name of this element
	 * @param resourceId A resource ID or NULL for the default
	 * @param elements   The child data elements
	 */
	public DataElementList(String name, String resourceId,
		DataElement<?>... elements) {
		this(name, resourceId, Arrays.asList(elements), null);
	}

	/**
	 * Creates a new instance that is initialized from a certain set of data
	 * elements. The contents of the collection argument will be copied into
	 * this list element but not the collection itself.
	 *
	 * @param name       The name of this element
	 * @param resourceId A resource ID or NULL for the default
	 * @param elements   A collection containing the initial elements of this
	 *                   list element (may be NULL)
	 * @param flags      The optional flags for this data element
	 */
	public DataElementList(String name, String resourceId,
		Collection<? extends DataElement<?>> elements, Set<Flag> flags) {
		super(name, null, flags);

		setResourceId(resourceId);

		if (elements != null) {
			for (DataElement<?> element : elements) {
				updateParent(element);
				dataElements.add(element);
			}
		}
	}

	/**
	 * Default constructor for serialization.
	 */
	protected DataElementList() {
	}

	/**
	 * Searches for a data element with a certain name in a collection of data
	 * elements. If the collection contains
	 * {@link DataElementList data element lists} these will be searched
	 * recursively.
	 *
	 * @param name     The name of the data element
	 * @param elements The elements to search
	 * @return The matching data element or NULL if no such element exists
	 */
	public static DataElement<?> findDataElement(String name,
		Collection<DataElement<?>> elements) {
		DataElement<?> result = null;

		for (DataElement<?> element : elements) {
			if (name.equals(element.getName())) {
				result = element;
			} else if (element instanceof DataElementList) {
				result = findDataElement(name,
					((DataElementList) element).getDataElements());
			}

			if (result != null) {
				break;
			}
		}

		return result;
	}

	/**
	 * This method should be invoked to initialize the property name constants
	 * for de-serialization.
	 */
	public static void init() {
	}

	/**
	 * Shortcut method to add a new {@link StringDataElement} with a certain
	 * name and string value. See {@link ListDataElement#addElement(Object)}
	 * for
	 * more information.
	 *
	 * @param name  The name of the data element to add
	 * @param value The value of the new data element
	 */
	public void add(String name, String value) {
		addElement(new StringDataElement(name, value));
	}

	/**
	 * Shortcut method to add a new {@link DateDataElement} with a certain name
	 * and {@link Date} value.
	 *
	 * @param name  The name of the data element to add
	 * @param value The value of the new data element
	 */
	public void add(String name, Date value) {
		addElement(new DateDataElement(name, value, null, null));
	}

	/**
	 * Shortcut method to add a new {@link IntegerDataElement} with a certain
	 * name and value.
	 *
	 * @param name  The name of the data element to add
	 * @param value The value of the new data element
	 */
	public void add(String name, int value) {
		addElement(new IntegerDataElement(name, value, null, null));
	}

	/**
	 * Shortcut method to add a new {@link BooleanDataElement} with a certain
	 * name and value.
	 *
	 * @param name  The name of the data element to add
	 * @param value The value of the new data element
	 */
	@SuppressWarnings("boxing")
	public void add(String name, boolean value) {
		addElement(new BooleanDataElement(name, value, null));
	}

	/**
	 * Overridden to modify the parent reference of the argument data element.
	 *
	 * @see ListDataElement#addElement(int, Object)
	 */
	@Override
	public void addElement(int index, DataElement<?> element) {
		super.addElement(index, element);
		updateParent(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataElementList copy(CopyMode mode,
		PropertyName<?>... copyProperties) {
		DataElementList copy =
			(DataElementList) super.copy(mode, copyProperties);

		// copyValue() is overridden to do nothing, so the child list is empty
		if (mode == CopyMode.FULL) {
			for (DataElement<?> child : this) {
				DataElement<?> childCopy = child.copy(mode);

				childCopy.setParent(copy);
				copy.dataElements.add(childCopy);
			}
		}

		return copy;
	}

	/**
	 * Searches for a child data element in the full hierarchy of this instance
	 * and returns it if found. Invokes
	 * {@link #findDataElement(String, Collection)} to perform the search.
	 *
	 * @param name The name of the data element to search
	 * @return The child data element with the given name or NULL for none
	 */
	public DataElement<?> findChild(String name) {
		return findDataElement(name, dataElements);
	}

	/**
	 * Returns the boolean value of a certain data element. The returned value
	 * will be the result of invoking {@link Boolean#booleanValue()} on the
	 * type-casted value of the data element with the given name if such
	 * exists.
	 * If not, FALSE will be returned. If the data element's value is not of
	 * type {@link Boolean} an exception will occur.
	 *
	 * @param name The name of the data element to return as a boolean value
	 * @return The boolean value of the data element or FALSE if none could be
	 * found
	 */
	public boolean getBoolean(String name) {
		DataElement<?> element = getElement(name);

		return element != null && ((Boolean) element.getValue()).booleanValue();
	}

	/**
	 * A convenience method that casts the return value of the name-based
	 * method
	 * {@link #getElementAt(String)} to a data element list. If the type of the
	 * named element is not {@link DataElementList} or if no such element
	 * exists
	 * NULL will be returned.
	 *
	 * @param elementPath The path of the element list to return
	 * @return The element list at the given path or NULL if no list with the
	 * given name exists
	 */
	public DataElementList getChildList(String elementPath) {
		DataElement<?> element = getElementAt(elementPath);

		return element instanceof DataElementList ?
		       (DataElementList) element :
		       null;
	}

	/**
	 * Returns the list of data elements in this instance. The list must not be
	 * modified by the invoking code.
	 *
	 * @return The list of data elements
	 */
	public List<DataElement<?>> getDataElements() {
		return dataElements;
	}

	/**
	 * Returns the date value of a certain data element. The returned value
	 * will
	 * be the type-casted value of the data element with the given name if such
	 * exists. If not, NULL will be returned. If the data element's value is
	 * not
	 * of type {@link Date} an exception will occur.
	 *
	 * @param name The name of the data element to return as a date value
	 * @return The date value of the data element or NULL if none could be
	 * found
	 */
	public Date getDate(String name) {
		DataElement<?> element = getElement(name);

		return element != null ? (Date) element.getValue() : null;
	}

	/**
	 * Returns the first element with a certain name from this list.
	 *
	 * @param elementName The name of the element to return
	 * @return The element or NULL if no element with the given name exists
	 */
	public DataElement<?> getElement(String elementName) {
		for (DataElement<?> element : dataElements) {
			if (element.getName().equals(elementName)) {
				return element;
			}
		}

		return null;
	}

	/**
	 * Returns a particular data element from the hierarchy of this list. The
	 * element path argument can either be a simple element name of a path
	 * to an
	 * element in the hierarchy of this list's sub-lists. The path separator is
	 * a forward slash as defined by {@link DataElement#PATH_SEPARATOR_CHAR}.
	 *
	 * <p>If the path start with a separator it is considered to be absolute
	 * with the name of this list as the first path element. Without a leading
	 * separator it must be relative to this instance, i.e. the first
	 * element in
	 * the path must be the name of a child data element of this instance. The
	 * path must never end with a separator. Examples:</p>
	 *
	 * <ul>
	 *   <li>'Test': returns the element 'Test' from this list</li>
	 *   <li>'/Data/Preferences/Setting1': returns the element 'Setting1' from
	 *     the sub-list 'Preferences' of this element which is called
	 *     'Data'</li>
	 *   <li>'Preferences/Setting1': returns the element 'Setting1' from the
	 *     sub-list 'Preferences'</li>
	 *   <li>'Preferences': returns the sub-list named 'Preferences'</li>
	 * </ul>
	 *
	 * @param elementPath The path of the element to return
	 * @return The element with the given path or NULL if no such element
	 * exists
	 */
	public DataElement<?> getElementAt(String elementPath) {
		String[] pathElements = elementPath.split(PATH_SEPARATOR_STRING);
		DataElementList currentList = this;
		DataElement<?> result = null;
		int lastElement = pathElements.length - 1;
		int pathElement = 0;

		if (lastElement > 0 && elementPath.charAt(0) == PATH_SEPARATOR_CHAR) {
			if (pathElements[1].equals(getName())) {
				if (lastElement == 1) {
					result = this;
				} else { // 0 = empty string, 1 = this element
					pathElement = 2;
				}
			} else {
				throw new IllegalArgumentException(
					"Absolute path must start with " + getName());
			}
		}

		while (result == null && currentList != null &&
			pathElement <= lastElement) {
			String elementName = pathElements[pathElement++];

			result = currentList.getElement(elementName);

			if (pathElement <= lastElement) {
				if (result instanceof DataElementList) {
					currentList = (DataElementList) result;
					result = null;
				} else {
					throw new IllegalArgumentException(
						"Not an element list: " + elementName);
				}
			}
		}

		return result;
	}

	/**
	 * Returns a formatted multi-line string that describes the data element
	 * hierarchy of this instance.
	 *
	 * @param indent The initial indent of the hierarchy (empty for none)
	 * @return The data element hierarchy string
	 */
	public String getElementHierarchy(String indent) {
		StringBuilder builder = new StringBuilder(getName());

		builder.append('\n');
		indent += "  ";

		for (DataElement<?> e : this) {
			builder.append(indent);

			if (e instanceof DataElementList) {
				builder.append(
					((DataElementList) e).getElementHierarchy(indent));
			} else {
				builder.append(e.getName());
				builder.append('\n');
			}
		}

		return builder.toString();
	}

	/**
	 * Returns the integer value of a certain data element. The returned value
	 * will be the result of invoking {@link Number#intValue()} on the
	 * type-casted value of the data element with the given name if such
	 * exists.
	 * If not, 0 (zero) will be returned. If the data element's value is not of
	 * type {@link Number} an exception will occur.
	 *
	 * @param name The name of the data element to return as an integer value
	 * @return The integer value of the data element or 0 if none could be
	 * found
	 */
	public int getInt(String name) {
		DataElement<?> element = getElement(name);

		return element != null ? ((Number) element.getValue()).intValue() : 0;
	}

	/**
	 * Returns the string value of a certain data element. The returned value
	 * will be the result of invoking {@link Object#toString()} on the value of
	 * the data element with the given name if such exists. If not, NULL
	 * will be
	 * returned.
	 *
	 * @param name The name of the data element to return the string value of
	 * @return The string value of the data element or NULL if none could be
	 * found
	 */
	public String getString(String name) {
		DataElement<?> element = getElement(name);

		return element != null ? element.getValue().toString() : null;
	}

	/**
	 * Overridden to also mark the child hierarchy as modified.
	 */
	@Override
	public void markAsChanged() {
		super.markAsChanged();
		setFlag(StateProperties.STRUCTURE_CHANGED);

		for (DataElement<?> childElement : dataElements) {
			childElement.markAsChanged();
		}
	}

	/**
	 * Overridden to modify the parent reference of the argument data element.
	 *
	 * @see ListDataElement#removeElement(Object)
	 */
	@Override
	public void removeElement(DataElement<?> element) {
		super.removeElement(element);
		element.setParent(null);
	}

	/**
	 * Replaces the first data element in this list with a new element with the
	 * same name.
	 *
	 * @param newElement The element to replace another with the same name
	 * @return TRUE if an element has been replaced
	 */
	public boolean replaceElement(DataElement<?> newElement) {
		int count = dataElements.size();

		for (int i = 0; i < count; i++) {
			if (dataElements.get(i).getName().equals(newElement.getName())) {
				updateParent(newElement);
				dataElements.set(i, newElement);

				return true;
			}
		}

		return false;
	}

	/**
	 * Shortcut method to set a {@link StringDataElement} with a certain name
	 * and string value. See {@link #setElement(DataElement)} for more
	 * information.
	 *
	 * @param name  The name of the data element to add
	 * @param value The value of the new data element
	 */
	public void set(String name, String value) {
		setElement(new StringDataElement(name, value));
	}

	/**
	 * Sets an element in this instance. If an element with the given name
	 * already exists it will be replaced. Otherwise a new element will be
	 * added
	 * to the end of the list.
	 *
	 * @param element The element to set
	 */
	public void setElement(DataElement<?> element) {
		DataElement<?> existing = getElement(element.getName());

		if (existing != null) {
			int index = getElementIndex(existing);

			removeElement(existing);
			addElement(index, element);
		} else {
			addElement(element);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toDebugString(String indent, boolean includeProperties) {
		StringBuilder hierarchy =
			new StringBuilder(super.toDebugString(indent, includeProperties));

		indent += "  ";

		for (DataElement<?> child : dataElements) {
			hierarchy.append('\n');
			hierarchy.append(child.toDebugString(indent, includeProperties));
		}

		return hierarchy.toString();
	}

	/**
	 * Returns the full hierarchy of this data element list as a string.
	 *
	 * @return The hierarchy string
	 */
	public String toHierarchyString() {
		return toHierarchyString("");
	}

	/**
	 * Overridden to to nothing as the copying of the child data elements is
	 * handled in {@link #copy(CopyMode, PropertyName...)}.
	 *
	 * @see ListDataElement#copyValue(DataElement)
	 */
	@Override
	protected void copyValue(DataElement<List<DataElement<?>>> copy) {
	}

	/**
	 * Can be overridden by subclasses to return a resource id prefix for child
	 * elements. This default implementation returns an empty string.
	 *
	 * @return The child resource id prefix
	 */
	protected String getChildResourceIdPrefix() {
		return "";
	}

	/**
	 * @see ListDataElement#getList()
	 */
	@Override
	protected List<DataElement<?>> getList() {
		return dataElements;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataElementList newInstance() {
		return new DataElementList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateValue(List<DataElement<?>> newElements) {
		dataElements = newElements;
	}

	/**
	 * Returns the full hierarchy of this data element list.
	 *
	 * @param indent The indentation of the hierarchy
	 * @return The hierarchy string
	 */
	private String toHierarchyString(String indent) {
		StringBuilder hierarchy = new StringBuilder();

		hierarchy.append(indent);
		hierarchy.append(getName());
		hierarchy.append(" [");
		hierarchy.append(dataElements.size());
		hierarchy.append("]\n");

		indent += "  ";

		for (DataElement<?> child : this) {
			if (child instanceof DataElementList) {
				hierarchy.append(
					((DataElementList) child).toHierarchyString(indent));
			} else {
				hierarchy.append(indent);
				hierarchy.append(child.getName());
				hierarchy.append('\n');
			}
		}

		return hierarchy.toString();
	}

	/**
	 * Prepares the addition of an element to this list.
	 *
	 * @param element The element that will be added
	 */
	private void updateParent(DataElement<?> element) {
		DataElementList oldParent = element.getParent();

		if (oldParent != this) {
			if (oldParent != null) {
				oldParent.removeElement(element);
			}

			element.setParent(this);
		}
	}
}
