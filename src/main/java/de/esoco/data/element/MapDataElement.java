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

import de.esoco.data.validate.Validator;
import de.esoco.lib.text.TextConvert;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
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
public abstract class MapDataElement<K, V> extends DataElement<Map<K, V>> {

	private static final long serialVersionUID = 1L;

	private Validator<? super V> valueValidator;

	/**
	 * @see MapDataElement#MapDataElement(String, Validator, Set)
	 */
	public MapDataElement(String name, Validator<? super V> valueValidator,
		Set<Flag> flags) {
		super(name, null, flags);

		this.valueValidator = valueValidator;
	}

	/**
	 * Default constructor for serialization.
	 */
	protected MapDataElement() {
	}

	/**
	 * Removes all mappings from this element.
	 */
	public void clear() {
		checkImmutable();
		getMap().clear();
	}

	/**
	 * Checks whether this element contains a mapping with a certain key.
	 *
	 * @param key The key to check
	 * @return TRUE if a mapping exists for the given key
	 */
	public boolean containsKey(K key) {
		return getMap().containsKey(key);
	}

	/**
	 * Checks whether this element contains at least one mapping with a certain
	 * value.
	 *
	 * @param value The value to check
	 * @return TRUE if at least one mapping exists for the given value
	 */
	public boolean containsValue(V value) {
		return getMap().containsValue(value);
	}

	/**
	 * Returns a certain value from this element's map.
	 *
	 * @param key The key to return the value for
	 * @return The value associated with the key or NULL for none
	 */
	public V get(K key) {
		return getMap().get(key);
	}

	/**
	 * Returns an immutable {@link Collection} of the map entries stored in
	 * this
	 * data element.
	 *
	 * @return The entry collection
	 */
	public Collection<Map.Entry<K, V>> getEntries() {
		return Collections.unmodifiableCollection(getMap().entrySet());
	}

	/**
	 * Returns an immutable {@link Collection} view of the mapping keys stored
	 * in this element.
	 *
	 * @return A collection containing the mapping keys of this element
	 */
	public Collection<K> getKeys() {
		return Collections.unmodifiableCollection(getMap().keySet());
	}

	/**
	 * Returns the number of mappings in this element's map.
	 *
	 * @return The map size
	 */
	public int getMapSize() {
		return getMap().size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Map<K, V> getValue() {
		return getMap();
	}

	/**
	 * Returns the validator for the map values.
	 *
	 * @return The map value validator or NULL for none
	 */
	public Validator<? super V> getValueValidator() {
		return valueValidator;
	}

	/**
	 * Returns an immutable {@link Collection} view of the map values stored in
	 * this element.
	 *
	 * @return A collection containing the map values of this element
	 */
	public Collection<V> getValues() {
		return Collections.unmodifiableCollection(getMap().values());
	}

	/**
	 * Puts a new key-value mapping into the map.
	 *
	 * @param key   The key to store the value under
	 * @param value The value to store
	 * @return The previous value stored under the key or NULL for none
	 */
	public V put(K key, V value) {
		checkImmutable();
		checkValidValue(valueValidator, value);

		return getMap().put(key, value);
	}

	/**
	 * Copies all mappings from the argument map to this element.
	 *
	 * @param sourceMap The map to copy the mappings from
	 */
	public void putAll(Map<? extends K, ? extends V> sourceMap) {
		Map<K, V> map = getMap();

		checkImmutable();

		for (K key : sourceMap.keySet()) {
			V value = sourceMap.get(key);

			checkValidValue(valueValidator, value);
			map.put(key, value);
		}
	}

	/**
	 * Removes a certain key-value mapping from this element.
	 *
	 * @param key The key to remove the mapping for
	 * @return The value associated with the key or NULL for none
	 */
	public V remove(K key) {
		checkImmutable();

		return getMap().remove(key);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return TextConvert.lastElementOf(getName()) + getMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void copyValue(DataElement<Map<K, V>> copy) {
		((MapDataElement<K, V>) copy).getMap().putAll(getMap());
	}

	/**
	 * Must be implemented by subclasses to return the map used to store this
	 * element's entries.
	 *
	 * @return The internal map
	 */
	protected abstract Map<K, V> getMap();

	/**
	 * @see DataElement#hasEqualValueAs(DataElement)
	 */
	@Override
	protected boolean hasEqualValueAs(DataElement<?> other) {
		return getMap().equals(((MapDataElement<?, ?>) other).getMap());
	}

	/**
	 * Overridden to always throw a runtime exception. Manipulations of a map
	 * data element must always be done through the map manipulation methods.
	 *
	 * @param newValue Ignored
	 */
	@Override
	protected final void updateValue(Map<K, V> newValue) {
		throw new UnsupportedOperationException(
			"Use element manipulation methods instead");
	}
}
