//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.text.TextConvert;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;


/********************************************************************
 * A base class for data elements that store their data as a mapping from keys
 * to values. The generic types K and V designate the types of the map keys and
 * values. A subclass must implement the method {@link #getMap()} to return the
 * internal map. The standard data element methods {@link #updateValue(Object)}
 * and {@link #getValue()} are overridden to throw a runtime exception. Access
 * to the map data must be done through one of the corresponding methods like
 * {@link #put(Object, Object)}.
 *
 * <p>It is recommended that subclasses either always use a map implementation
 * that preserves the order in which mappings are added or provide a boolean
 * constructor parameter to choose such ordered behavior.</p>
 *
 * @author eso
 */
public abstract class MapDataElement<K, V> extends DataElement<V>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see MapDataElement#MapDataElement(String, Validator, Set)
	 */
	public MapDataElement(String			   sName,
						  Validator<? super V> rValidator,
						  Set<Flag>			   rFlags)
	{
		super(sName, rValidator, rFlags);
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	protected MapDataElement()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Removes all mappings from this element.
	 */
	public void clear()
	{
		checkImmutable();
		getMap().clear();
	}

	/***************************************
	 * Checks whether this element contains a mapping with a certain key.
	 *
	 * @param  rKey The key to check
	 *
	 * @return TRUE if a mapping exists for the given key
	 */
	public boolean containsKey(K rKey)
	{
		return getMap().containsKey(rKey);
	}

	/***************************************
	 * Checks whether this element contains at least one mapping with a certain
	 * value.
	 *
	 * @param  rValue The value to check
	 *
	 * @return TRUE if at least one mapping exists for the given value
	 */
	public boolean containsValue(V rValue)
	{
		return getMap().containsValue(rValue);
	}

	/***************************************
	 * Returns a certain value from this element's map.
	 *
	 * @param  rKey The key to return the value for
	 *
	 * @return The value associated with the key or NULL for none
	 */
	public V get(K rKey)
	{
		return getMap().get(rKey);
	}

	/***************************************
	 * Returns an immutable {@link Collection} of the map entries stored in this
	 * data element.
	 *
	 * @return The entry collection
	 */
	public Collection<Map.Entry<K, V>> getEntries()
	{
		return Collections.unmodifiableCollection(getMap().entrySet());
	}

	/***************************************
	 * Returns an immutable {@link Collection} view of the mapping keys stored
	 * in this element.
	 *
	 * @return A collection containing the mapping keys of this element
	 */
	public Collection<K> getKeys()
	{
		return Collections.unmodifiableCollection(getMap().keySet());
	}

	/***************************************
	 * Returns the number of mappings in this element's map.
	 *
	 * @return The map size
	 */
	public int getMapSize()
	{
		return getMap().size();
	}

	/***************************************
	 * Always throws a runtime exception. Access to the data of a map data
	 * element must always happen through it's element access methods.
	 *
	 * @see DataElement#getValue()
	 */
	@Override
	public final V getValue()
	{
		throw new UnsupportedOperationException("Use element access methods instead");
	}

	/***************************************
	 * Returns an immutable {@link Collection} view of the map values stored in
	 * this element.
	 *
	 * @return A collection containing the map values of this element
	 */
	public Collection<V> getValues()
	{
		return Collections.unmodifiableCollection(getMap().values());
	}

	/***************************************
	 * Puts a new key-value mapping into the map.
	 *
	 * @param  rKey   The key to store the value under
	 * @param  rValue The value to store
	 *
	 * @return The previous value stored under the key or NULL for none
	 */
	public V put(K rKey, V rValue)
	{
		checkImmutable();
		checkValidValue(rValue);

		return getMap().put(rKey, rValue);
	}

	/***************************************
	 * Copies all mappings from the argument map to this element.
	 *
	 * @param rSourceMap The map to copy the mappings from
	 */
	public void putAll(Map<? extends K, ? extends V> rSourceMap)
	{
		Map<K, V> rMap = getMap();

		checkImmutable();

		for (K rKey : rSourceMap.keySet())
		{
			V rValue = rSourceMap.get(rKey);

			checkValidValue(rValue);
			rMap.put(rKey, rValue);
		}
	}

	/***************************************
	 * Removes a certain key-value mapping from this element.
	 *
	 * @param  rKey The key to remove the mapping for
	 *
	 * @return The value associated with the key or NULL for none
	 */
	public V remove(K rKey)
	{
		checkImmutable();

		return getMap().remove(rKey);
	}

	/***************************************
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		return TextConvert.lastElementOf(getName()) + getMap();
	}

	/***************************************
	 * Must be implemented by subclasses to return the map used to store this
	 * element's entries.
	 *
	 * @return The internal map
	 */
	protected abstract Map<K, V> getMap();

	/***************************************
	 * @see DataElement#isValueEqual(DataElement)
	 */
	@Override
	protected boolean isValueEqual(DataElement<?> rOther)
	{
		return getMap().equals(((MapDataElement<?, ?>) rOther).getMap());
	}

	/***************************************
	 * Always throws a runtime exception. Manipulations of a map data element
	 * must always be done through the map manipulation methods.
	 *
	 * @see DataElement#updateValue(Object)
	 */
	@Override
	protected final void updateValue(V rNewValue)
	{
		throw new UnsupportedOperationException("Use element manipulation methods instead");
	}
}
