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

import de.esoco.data.validate.Validator;

import de.esoco.lib.model.DataModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/********************************************************************
 * A base class for data elements that store a list of elements. The generic
 * type E designates the type of the list elements. A subclass must implement
 * the method {@link #getList()} to return the list of values. The standard data
 * element methods {@link #updateValue(Object)} and {@link #getValue()} are
 * overridden to throw a runtime exception. Access to the list data must be done
 * through the corresponding methods like {@link #getElement(int)}.
 *
 * @author eso
 */
public abstract class ListDataElement<E> extends DataElement<E>
	implements DataModel<E>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see DataElement#DataElement(String, Validator, Set)
	 */
	public ListDataElement(String				sName,
						   Validator<? super E> rValidator,
						   Set<Flag>			rFlags)
	{
		super(sName, rValidator, rFlags);
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	protected ListDataElement()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a collection of new elements to this instance.
	 *
	 * @param rNewElements The element value to add
	 */
	public void addAll(Collection<E> rNewElements)
	{
		List<E> rList = getList();

		checkImmutable();

		for (E rElement : rNewElements)
		{
			checkValidValue(rElement);
			rList.add(rElement);
		}
	}

	/***************************************
	 * Adds a new element value to this instance.
	 *
	 * @param rElement The element value to add
	 */
	public final void addElement(E rElement)
	{
		addElement(getList().size(), rElement);
	}

	/***************************************
	 * Adds a new element value at a certain position of this instance.
	 *
	 * @param nIndex   The position index
	 * @param rElement The element value to add
	 */
	public void addElement(int nIndex, E rElement)
	{
		checkImmutable();
		checkValidValue(rElement);
		getList().add(nIndex, rElement);
	}

	/***************************************
	 * Removes all element values from this instance.
	 */
	public void clear()
	{
		checkImmutable();
		getList().clear();
	}

	/***************************************
	 * Checks whether this instance contains a certain value.
	 *
	 * @param  rElement The element value to check
	 *
	 * @return TRUE if this element contains the given element
	 */
	public boolean containsElement(E rElement)
	{
		return getList().contains(rElement);
	}

	/***************************************
	 * Returns an element value of this instance.
	 *
	 * @param  nIndex The index
	 *
	 * @return The element
	 */
	@Override
	public E getElement(int nIndex)
	{
		return getList().get(nIndex);
	}

	/***************************************
	 * Returns the number of element values in this instance.
	 *
	 * @return The current element count
	 */
	@Override
	public int getElementCount()
	{
		return getList().size();
	}

	/***************************************
	 * Returns the index of a certain data element of this instance.
	 *
	 * @param  rElement The data element to return the index of
	 *
	 * @return The element index (starting at zero) or -1 if the element
	 *         couldn't be found
	 */
	public int getElementIndex(E rElement)
	{
		return getList().indexOf(rElement);
	}

	/***************************************
	 * Returns a new list containing the elements of this instance.
	 *
	 * @return A new list instance containing all elements
	 */
	public List<E> getElements()
	{
		return new ArrayList<E>(getList());
	}

	/***************************************
	 * Always throws a runtime exception. Access to the data of a list data
	 * element must always happen through it's element access methods.
	 *
	 * @return Always throws an {@link UnsupportedOperationException}
	 */
	@Override
	public final E getValue()
	{
		throw new UnsupportedOperationException("Use element access methods instead");
	}

	/***************************************
	 * @see Iterable#iterator()
	 */
	@Override
	public Iterator<E> iterator()
	{
		return getList().iterator();
	}

	/***************************************
	 * Removes a certain element from this instance.
	 *
	 * @param rElement The element to remove
	 */
	public void removeElement(E rElement)
	{
		checkImmutable();
		getList().remove(rElement);
	}

	/***************************************
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		return getName() + getList();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void trim()
	{
		if (!isModified())
		{
			getList().clear();
		}
	}

	/***************************************
	 * Must be implemented by subclasses to return the type-specific value list.
	 *
	 * @return The list of values for this instance
	 */
	protected abstract List<E> getList();

	/***************************************
	 * @see DataElement#getValueHashCode()
	 */
	@Override
	protected int getValueHashCode()
	{
		return getList().hashCode();
	}

	/***************************************
	 * @see DataElement#isValueEqual(DataElement)
	 */
	@Override
	protected boolean isValueEqual(DataElement<?> rOther)
	{
		return getList().equals(((ListDataElement<?>) rOther).getList());
	}

	/***************************************
	 * Always throws a runtime exception. Manipulations of a list data element
	 * must always be done through the list manipulation methods.
	 *
	 * @see DataElement#updateValue(Object)
	 */
	@Override
	protected final void updateValue(E rNewValue)
	{
		throw new UnsupportedOperationException("Use element manipulation methods instead");
	}
}
