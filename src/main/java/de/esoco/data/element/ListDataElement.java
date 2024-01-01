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

import de.esoco.data.validate.HasValueList;
import de.esoco.data.validate.Validator;
import de.esoco.lib.model.DataModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A base class for data elements that store a list of elements. The generic
 * type E designates the type of the list elements. A subclass must implement
 * the method {@link #getList()} to return the list of values. The standard data
 * element methods {@link #updateValue(Object)} and {@link #getValue()} are
 * overridden to throw a runtime exception. Access to the list data must be done
 * through the corresponding methods like {@link #getElement(int)}.
 *
 * @author eso
 */
public abstract class ListDataElement<E> extends DataElement<List<E>>
	implements DataModel<E> {

	private static final long serialVersionUID = 1;

	private Validator<? super E> elementValidator;

	/**
	 * @see DataElement#DataElement(String, Validator, Set)
	 */
	public ListDataElement(String name, Validator<? super E> elementValidator,
		Set<Flag> flags) {
		super(name, null, flags);

		this.elementValidator = elementValidator;
	}

	/**
	 * Default constructor for serialization.
	 */
	protected ListDataElement() {
	}

	/**
	 * Adds a collection of new elements to this instance.
	 *
	 * @param newElements The element value to add
	 */
	public void addAll(Collection<E> newElements) {
		List<E> list = getList();

		checkImmutable();

		for (E element : newElements) {
			checkValidValue(elementValidator, element);
			list.add(element);
		}

		setModified(true);
	}

	/**
	 * Adds allowed values to the element validator.
	 *
	 * @param values The values to add
	 */
	@SuppressWarnings("unchecked")
	public void addAllowedValues(Collection<E> values) {
		((List<E>) getAllowedValues()).addAll(values);
	}

	/**
	 * Adds a new element value to this instance.
	 *
	 * @param element The element value to add
	 */
	public final void addElement(E element) {
		addElement(getList().size(), element);
	}

	/**
	 * Adds a new element value at a certain position of this instance.
	 *
	 * @param index   The position index
	 * @param element The element value to add
	 */
	public void addElement(int index, E element) {
		checkImmutable();
		checkValidValue(elementValidator, element);
		getList().add(index, element);
		setModified(true);
	}

	/**
	 * Removes all element values from this instance.
	 */
	public void clear() {
		checkImmutable();
		getList().clear();
		setModified(true);
	}

	/**
	 * Checks whether this instance contains a certain value.
	 *
	 * @param element The element value to check
	 * @return TRUE if this element contains the given element
	 */
	public boolean containsElement(E element) {
		return getList().contains(element);
	}

	/**
	 * Overridden to return the values from the element validator instead.
	 *
	 * @see DataElement#getAllowedValues()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<?> getAllowedValues() {
		return elementValidator instanceof HasValueList ?
		       ((HasValueList<?>) elementValidator).getValues() :
		       null;
	}

	/**
	 * Returns an element value of this instance.
	 *
	 * @param index The index
	 * @return The element
	 */
	@Override
	public E getElement(int index) {
		return getList().get(index);
	}

	/**
	 * Returns the number of element values in this instance.
	 *
	 * @return The current element count
	 */
	@Override
	public int getElementCount() {
		return getList().size();
	}

	/**
	 * Returns the index of a certain data element of this instance.
	 *
	 * @param element The data element to return the index of
	 * @return The element index (starting at zero) or -1 if the element
	 * couldn't be found
	 */
	public int getElementIndex(E element) {
		return getList().indexOf(element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Validator<? super E> getElementValidator() {
		return elementValidator;
	}

	/**
	 * Returns a new list containing the elements of this instance.
	 *
	 * @return A new list instance containing all elements
	 */
	public List<E> getElements() {
		return new ArrayList<E>(getList());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<E> getValue() {
		return getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator() {
		return getList().iterator();
	}

	/**
	 * Removes a certain element from this instance.
	 *
	 * @param element The element to remove
	 */
	public void removeElement(E element) {
		checkImmutable();
		getList().remove(element);
		setModified(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getName() + getList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void copyValue(DataElement<List<E>> copy) {
		((ListDataElement<E>) copy).getList().addAll(getList());
	}

	/**
	 * Returns the list of values of this data element. Must be implemented by
	 * subclasses to return the type-specific value list.
	 *
	 * @return The list of values for this instance (must not be NULL)
	 */
	protected abstract List<E> getList();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getValueHashCode() {
		return getList().hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean hasEqualValueAs(DataElement<?> other) {
		return getList().equals(((ListDataElement<?>) other).getList());
	}
}
