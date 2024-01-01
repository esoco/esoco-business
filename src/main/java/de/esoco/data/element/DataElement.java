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
import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.StringProperties;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.text.TextConvert;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.StateProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.StateProperties.FILTER_CRITERIA;
import static de.esoco.lib.property.StateProperties.FOCUSED;
import static de.esoco.lib.property.StateProperties.INTERACTION_EVENT_DATA;

/**
 * A base class for named data values which can be used to transfer data in
 * serialized form between a server and a client. Data elements can be
 * hierarchical, i.e. a data element can be a child of another data element
 * which must be an instance of {@link DataElementList}. The parent of a data
 * element can be queried with the method {@link #getParent()}.
 *
 * @author eso
 */
public abstract class DataElement<T> extends StringProperties {

	/**
	 * The modes for copying data elements with
	 * {@link DataElement#copy(CopyMode, PropertyName...)}.
	 */
	public enum CopyMode {FULL, FLAT, PROPERTIES, PLACEHOLDER}

	/**
	 * Flags that can be set on a data element.
	 */
	public enum Flag {IMMUTABLE, OPTIONAL, SELECTED, MODIFIED}

	/**
	 * An array of the names of properties that should be applied from the
	 * client to the server.
	 */
	public static final PropertyName<?>[] SERVER_PROPERTIES =
		new PropertyName<?>[] { CURRENT_SELECTION, FOCUSED,
			INTERACTION_EVENT_DATA, FILTER_CRITERIA };

	/**
	 * The prefix for the name of anonymous data elements.
	 */
	public static final String ANONYMOUS_ELEMENT_PREFIX = "_";

	/**
	 * The prefix for item resource IDs
	 */
	public static final String ITEM_RESOURCE_PREFIX = "$itm";

	/**
	 * A generic resource string for a &lt;New&gt; item.
	 */
	public static final String ITEM_NEW_RESOURCE = "$itmNew";

	/**
	 * A generic resource string for an &lt;All&gt; item.
	 */
	public static final String ITEM_ALL_RESOURCE = "$itmAll";

	/**
	 * The separator for data element paths
	 */
	public static final char PATH_SEPARATOR_CHAR = '/';

	/**
	 * Flag property: allowed values have been modified.
	 */
	public static final PropertyName<Boolean> ALLOWED_VALUES_CHANGED =
		PropertyName.newBooleanName("ALLOWED_VALUES_CHANGED");

	/**
	 * String property: an URL to be invoked in a separate browser window.
	 */
	public static final PropertyName<String> INTERACTION_URL =
		PropertyName.newStringName("INTERACTION_URL");

	/**
	 * Boolean property: used in conjunction with {@link #INTERACTION_URL},
	 * indicates that the URL is app-relative and to be invoked in a hidden
	 * frame, typically to initiate a download.
	 */
	public static final PropertyName<Boolean> HIDDEN_URL =
		PropertyName.newBooleanName("HIDDEN_URL");

	/**
	 * The set of data element flags for display-only data elements
	 */
	public static final EnumSet<Flag> DISPLAY_FLAGS =
		EnumSet.of(Flag.IMMUTABLE);

	/**
	 * The set of data element flags for input data elements
	 */
	public static final EnumSet<Flag> INPUT_FLAGS = EnumSet.noneOf(Flag.class);

	private static final long serialVersionUID = 1L;

	private String name;

	private Validator<? super T> validator;

	// these fields are not included in serialization because they are only
	// used
	// locally on the client and/or server
	private transient DataElementList parent = null;

	private transient String resourceId = null;

	private transient boolean modified = false;

	private boolean immutable = false;

	private boolean optional = false;

	private boolean selected = false;

	/**
	 * Creates a new instance with a certain name and validator. The validator
	 * object will be used to validate any value that is set through the method
	 * {@link #setValue(Object)}.
	 *
	 * @param name      The name of this data element
	 * @param validator The validator for new values or NULL for none
	 * @param flags     The optional flags for this data element or NULL for
	 *                  none
	 */
	public DataElement(String name, Validator<? super T> validator,
		Set<Flag> flags) {
		this.name = name;
		this.validator = validator;

		if (flags != null) {
			immutable = flags.contains(Flag.IMMUTABLE);
			optional = flags.contains(Flag.OPTIONAL);
		}
	}

	/**
	 * Default constructor for serialization.
	 */
	protected DataElement() {
	}

	/**
	 * Creates the string name for an item value (typically an enum constant).
	 * The name will be composed from the simple name of the value's datatype
	 * class and the capitalized string representation of the value.
	 *
	 * @param value The value to create the item name for
	 * @return The resulting item string (an empty string for a NULL value)
	 */
	public static String createItemName(Object value) {
		StringBuilder item = new StringBuilder();

		if (value != null) {
			Class<? extends Object> datatype = value.getClass();
			String className = datatype.getSimpleName();

			// replace empty name of anonymous classes with name of parent
			// class
			if (className.isEmpty()) {
				className = datatype.getSuperclass().getSimpleName();
			}

			item.append(className);
			item.append(TextConvert.capitalizedIdentifier(value.toString()));
		}

		return item.toString();
	}

	/**
	 * Creates an item resource string for a certain value.
	 *
	 * @param value The value
	 * @return The resulting item string (an empty string for a NULL value)
	 */
	public static String createItemResource(Object value) {
		StringBuilder item = new StringBuilder();

		if (value != null) {
			item.append(ITEM_RESOURCE_PREFIX);
			item.append(createItemName(value));
		}

		return item.toString();
	}

	/**
	 * This method should be invoked to initialize the property name constants
	 * for de-serialization.
	 */
	public static void init() {
	}

	/**
	 * Reads the attributes of this data element from the given data reader.
	 *
	 * @param reader The data reader
	 */
	public static void readFrom(DataReader reader) {
	}

	/**
	 * Reads the attributes of this data element from the given data reader.
	 *
	 * @param writer reader The data reader
	 */
	public static void writeTo(DataWriter writer) {
	}

	/**
	 * Returns a copy of this data element that contains all or a subset of
	 * it's
	 * current state. Always copied are the name and {@link Flag flags}. Never
	 * copied is the parent reference because upon copying typically a
	 * reference
	 * to a copied parent needs to be set. The further data the copy contains
	 * depends on the copy mode:
	 *
	 * <ul>
	 *   <li>{@link CopyMode#FULL}: The copy contains all data (except the
	 *     parent reference).</li>
	 *   <li>{@link CopyMode#FLAT}: like {@link CopyMode#FULL} but without
	 *     sub-ordinate data elements.</li>
	 *   <li>{@link CopyMode#PROPERTIES}: The copy contains only the properties
	 *     but not the element value and attributes.</li>
	 *   <li>{@link CopyMode#PLACEHOLDER}: The copy contains only the element
	 *     name to serve as a placeholder.</li>
	 * </ul>
	 *
	 * <p>The copy instance is created by invoking {@link #newInstance()} which
	 * has the recommendation to overwrite the return type to the concrete
	 * subtype to prevent the need for casting by the invoking code. For the
	 * same reason it is recommended that subclasses also override this method
	 * with the concrete return type and cast the result of <code>
	 * super.copy()</code> to that type.</p>
	 *
	 * @param mode           The copy mode
	 * @param copyProperties An optional list of properties to copy. If not
	 *                       provided all properties will be copied (unless the
	 *                       mode is {@link CopyMode#PLACEHOLDER})
	 * @return The copied instance
	 */
	@SuppressWarnings("unchecked")
	public DataElement<T> copy(CopyMode mode,
		PropertyName<?>... copyProperties) {
		DataElement<T> copy = newInstance();

		copyAttributes(copy, mode);

		if (mode == CopyMode.FULL || mode == CopyMode.FLAT) {
			copyValue(copy);
		}

		if (mode != CopyMode.PLACEHOLDER) {
			if (copyProperties.length == 0) {
				copy.setProperties(this, true);
			} else {
				for (PropertyName<?> property : copyProperties) {
					copy.setProperty((PropertyName<Object>) property,
						getProperty(property, null));
				}
			}
		}

		return copy;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!super.equals(obj) || getClass() != obj.getClass()) {
			return false;
		}

		DataElement<?> other = (DataElement<?>) obj;

		return Objects.equals(name, other.name) && hasEqualValueAs(other) &&
			immutable == other.immutable && modified == other.modified &&
			optional == other.optional && selected == other.selected &&
			Objects.equals(resourceId, other.resourceId) &&
			Objects.equals(validator, other.validator) &&
			Objects.equals(parent, other.parent);
	}

	/**
	 * Returns the allowed values for this data element or NULL if the value is
	 * not constrained. The returned list has no specific generic type to allow
	 * overloading by subclasses that wrap composite types like collections.
	 *
	 * @return The allowed values
	 */
	public List<?> getAllowedValues() {
		return validator instanceof HasValueList ?
		       ((HasValueList<?>) validator).getValues() :
		       null;
	}

	/**
	 * Returns the validator for single elements of subclasses with composite
	 * values. The default implementation returns the same as
	 * {@link #getValidator()}.
	 *
	 * @return The element validator
	 */
	public Validator<?> getElementValidator() {
		return validator;
	}

	/**
	 * Returns the name of this data element.
	 *
	 * @return The data element's name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns the parent data element list of this element or NULL if it
	 * doesn't have a parent. Attention: the parent attribute is transient and
	 * will therefore not be restored on deserialization.
	 *
	 * @return The parent element or NULL for none
	 */
	public final DataElementList getParent() {
		return parent;
	}

	/**
	 * Returns the full path of this element in it's hierarchy. The returned
	 * path will always start with the {@link #PATH_SEPARATOR_CHAR}.
	 *
	 * @return The full element path
	 */
	public final String getPath() {
		StringBuilder path = new StringBuilder();
		DataElement<?> element = this;

		do {
			path.insert(0, PATH_SEPARATOR_CHAR);
			path.insert(1, element.getName());
			element = element.getParent();
		} while (element != null);

		return path.toString();
	}

	/**
	 * Returns a resource ID string for this element. This ID can be used by
	 * user interface code to display a descriptive text for the element value
	 * (e.g. as a label). The default implementation first checks for a string
	 * property with the type {@link UserInterfaceProperties#RESOURCE_ID}. If
	 * not found it invokes the method {@link #createResourceId()} which by
	 * default returns the capitalized last part of the point-separated element
	 * name. Subclasses can override that method to modify the resource ID
	 * generation.
	 *
	 * <p>If this instance has a parent data element it prepends the returned
	 * resource ID with the parent's child prefix that is returned by the
	 * method
	 * {@link DataElementList#getChildResourceIdPrefix()}.</p>
	 *
	 * @return The resource ID for this element
	 */
	public final String getResourceId() {
		if (resourceId == null) {
			resourceId = getProperty(RESOURCE_ID, null);

			if (resourceId == null) {
				resourceId = createResourceId();
			}

			if (parent != null) {
				resourceId = parent.getChildResourceIdPrefix() + resourceId;
			}
		}

		return resourceId;
	}

	/**
	 * Returns the root data element list of this element's hierarchy or
	 * NULL if
	 * this element doesn't have a parent at all. Attention: the parent
	 * attribute is transient and will therefore not be restored on
	 * deserialization so that this method will also not return a valid root
	 * for
	 * deserialized elements.
	 *
	 * @return The root element or NULL for none
	 */
	public final DataElementList getRoot() {
		DataElementList root = parent;

		if (root != null) {
			while (root.getParent() != null) {
				root = root.getParent();
			}
		}

		return root;
	}

	/**
	 * Returns the name of this element without a preceding package.
	 *
	 * @return The simple data element name
	 */
	public final String getSimpleName() {
		return TextConvert.lastElementOf(name);
	}

	/**
	 * Returns the validator for values to be set on this element.
	 *
	 * @return The validator or NULL if this element is read-only
	 */
	public final Validator<? super T> getValidator() {
		return validator;
	}

	/**
	 * ---------------------------------------------------------------- /**
	 * Returns the value of this element. Each subclass must implement this
	 * method separately because some environments (like GWT) require
	 * serializable objects to use specific types for serializable fields.
	 * Therefore it is not possible to store a generic Object value in the base
	 * class.
	 *
	 * @return The element value
	 */
	public abstract T getValue();

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 37 * name.hashCode() + getValueHashCode();
	}

	/**
	 * Returns the immutable state of this element. If this method returns TRUE
	 * an invocation of the method {@link #setValue(Object)} will result in an
	 * {@link UnsupportedOperationException} being thrown.
	 *
	 * @return TRUE if this element is immutable, FALSE if the element value
	 * can
	 * be modified
	 */
	public final boolean isImmutable() {
		return immutable;
	}

	/**
	 * Returns TRUE if this element's value has been modified.
	 *
	 * @return TRUE if the element value has changed
	 */
	public final boolean isModified() {
		return modified;
	}

	/**
	 * Returns the immutable state of this element. If this method returns TRUE
	 * an invocation of the method {@link #setValue(Object)} will result in an
	 * {@link UnsupportedOperationException} being thrown.
	 *
	 * @return TRUE if this element is immutable, FALSE if the element value
	 * can
	 * be modified
	 */
	public final boolean isOptional() {
		return optional;
	}

	/**
	 * Returns the selected state of this element. This attribute will be set
	 * for optional data elements depending on the state of the associated
	 * option selector in the UI.
	 *
	 * @return TRUE if this element is selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Checks whether a certain value is valid for this element. A value is
	 * considered valid if either this element has no validator, or if the
	 * value
	 * is NULL and the flag {@link Flag#OPTIONAL} is set, or if the method
	 * {@link Validator#isValid(Object)} of this element's validator returns
	 * TRUE for the given value. This validation will also be performed by
	 * {@link #setValue(Object)} to throw an exception if a new value is
	 * invalid. Therefore applications can use this method to verify values
	 * before setting them (e.g. when performing input validation).
	 *
	 * @param validator The validator to check the value with or NULL for none
	 * @param value     The value to check
	 * @return TRUE if the value is valid for this data element
	 */
	public <V> boolean isValidValue(Validator<? super V> validator, V value) {
		return validator == null || (value == null && isOptional()) ||
			validator.isValid(value);
	}

	/**
	 * Marks this data element as changed.
	 */
	public void markAsChanged() {
		setFlag(UserInterfaceProperties.VALUE_CHANGED);
	}

	/**
	 * Removes a property from this element.
	 *
	 * @param name The property name
	 */
	@Override
	public void removeProperty(PropertyName<?> name) {
		if (hasProperty(name)) {
			super.removeProperty(name);
			setModified(true);
		}
	}

	/**
	 * Sets the modified state of this element.
	 *
	 * @param modified The new modified state
	 */
	public void setModified(boolean modified) {
		this.modified = modified;
	}

	/**
	 * Sets a certain property of this data element and mark it as modified if
	 * the value has changed.
	 *
	 * @param name     The name of the property
	 * @param newValue The property value
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <P> void setProperty(PropertyName<P> name, P newValue) {
		P currentValue = getProperty(name, null);

		if (!Objects.equals(currentValue, newValue)) {
			super.setProperty(name, newValue);
			setModified(true);
		}
	}

	/**
	 * Sets the selected state of this element.
	 *
	 * @param selected The new selected state
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
		setModified(true);
	}

	/**
	 * Sets a string value. This method must be overridden by subclasses that
	 * want to support the setting of string values in parallel to their native
	 * datatype. The default implementation always throws a runtime exception.
	 *
	 * @param value The new string value
	 * @throws UnsupportedOperationException If not overridden
	 */
	public void setStringValue(String value) {
		throw new UnsupportedOperationException(
			"Cannot convert " + value + " for " + this);
	}

	/**
	 * Sets the validator of this element.
	 *
	 * @param validator The new validator
	 */
	public final void setValidator(Validator<? super T> validator) {
		this.validator = validator;
	}

	/**
	 * Sets the value of this element. If this element is read only an
	 * exception
	 * will be thrown. See the method {@link #isImmutable()} for details.
	 *
	 * @param newValue The new element value
	 * @throws UnsupportedOperationException If this element is read only
	 */
	public final void setValue(T newValue) {
		checkImmutable();
		checkValidValue(validator, newValue);

		T currentValue = getValue();

		if (!valuesEqual(currentValue, newValue)) {
			updateValue(newValue);
			setModified(true);
		}
	}

	/**
	 * Creates a string that describes this element for debugging purposes.
	 *
	 * @param indent            The indentation of the returned string
	 * @param includeProperties TRUE to include the properties, FALSE to omit
	 * @return The debug description
	 */
	public String toDebugString(String indent, boolean includeProperties) {
		StringBuilder debugString = new StringBuilder(indent);

		debugString.append(getSimpleName());
		debugString.append('(');
		debugString.append(immutable ? 'I' : '_');
		debugString.append(modified ? 'M' : '_');
		debugString.append(optional ? 'O' : '_');
		debugString.append(selected ? 'S' : '_');
		debugString.append(')');

		if (includeProperties) {
			debugString.append(getPropertyMap());
		}

		return debugString.toString();
	}

	/**
	 * Returns a string representation of this element.
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return name + "[" + getValue() + "]";
	}

	/**
	 * Checks whether this instance is read only. Should be invoked by
	 * subclasses that contain additional methods to modify attributes to
	 * support the read-only state of data elements.
	 *
	 * @throws UnsupportedOperationException If this element is read only
	 */
	protected final void checkImmutable() {
		if (isImmutable()) {
			throw new UnsupportedOperationException(
				"Element " + this + " is readonly");
		}
	}

	/**
	 * Checks whether the given value is valid for this element and throws an
	 * exception if not. Invokes {@link #isValidValue(Validator, Object)} to
	 * validate the given value.
	 *
	 * @param validator The validator to check the value with or NULL for none
	 * @param value     The value to check
	 * @throws IllegalArgumentException If the given value is not valid for
	 * this
	 *                                  element
	 */
	protected final <V> void checkValidValue(Validator<? super V> validator,
		V value) {
		if (!isValidValue(validator, value)) {
			throw new IllegalArgumentException(
				"Invalid value for " + this + ": " + value);
		}
	}

	/**
	 * Copies the attributes of this instance into another data element of the
	 * same type. Subclasses that have additional attributes beside their value
	 * should override this method to copy their attributes after invoking
	 * super.
	 *
	 * @param target   The target to copy the attributes to
	 * @param copyMode The copy mode
	 */
	protected void copyAttributes(DataElement<T> target, CopyMode copyMode) {
		target.name = name;

		if (copyMode == CopyMode.PLACEHOLDER) {
			// make sure that a placeholder cannot be modified
			target.immutable = true;
		} else {
			target.immutable = immutable;
			target.optional = optional;
			target.selected = selected;
			target.modified = modified;
		}

		if (copyMode == CopyMode.FULL || copyMode == CopyMode.FLAT) {
			target.validator = validator;
			target.resourceId = resourceId;
		}
	}

	/**
	 * Copies the value of this data element into another data element of the
	 * same type. This is used by the {@link #copy(CopyMode, PropertyName...)}
	 * method and by default uses {@link #updateValue(Object)} to set the value
	 * in the target element. Subclasses that don't support updating the value
	 * (e.g. because they manage a collection) need to override this method and
	 * implement the copying as needed. They can assume that the target object
	 * is of exactly the same type as their own.
	 *
	 * @param copy The copied data element to copy the value into
	 */
	protected void copyValue(DataElement<T> copy) {
		copy.updateValue(getValue());
	}

	/**
	 * Creates a resource ID from this data element. This ID will only be
	 * created once and is then cached in a transient instance field. See the
	 * method {@link #getResourceId()} for details.
	 *
	 * @return The resource ID string
	 */
	protected String createResourceId() {
		String resId = TextConvert.lastElementOf(name);

		if (resId.startsWith(ANONYMOUS_ELEMENT_PREFIX)) {
			resId = "";
		} else {
			resId = TextConvert.capitalizedIdentifier(resId);
		}

		return resId;
	}

	/**
	 * Returns the value hash code. Can be overridden by subclasses that have a
	 * non-standard internal structure.
	 *
	 * @return The value hash code
	 */
	protected int getValueHashCode() {
		Object value = getValue();

		return (value != null ? value.hashCode() : 0);
	}

	/**
	 * Checks whether this element has a value that is equal to that of another
	 * element. Can be overridden by subclasses that have a non-standard
	 * internal structure.
	 *
	 * @param other The other data element which will always be of the same
	 *                 type
	 *              as this instance
	 * @return TRUE if the value are equal
	 */
	protected boolean hasEqualValueAs(final DataElement<?> other) {
		return Objects.equals(getValue(), other.getValue());
	}

	/**
	 * Returns a new instance of the respective data element sub-type on which
	 * it is invoked. This is needed for GWT which doesn't support reflection.
	 * Used by {@link #copy(CopyMode, PropertyName...)} for cloning an
	 * instance.
	 * Implementations should overwrite the return type with their concrete
	 * type
	 * to prevent the need for casting by the invoking code.
	 *
	 * @return The new instance
	 */
	protected abstract DataElement<T> newInstance();

	/**
	 * Updates the element value. Will be invoked by {@link #setValue(Object)}
	 * to store a new value after validation. If a subclass wants to reject
	 * certain values it should do so in the
	 * {@link #isValidValue(Validator, Object)} method. Subclasses that are
	 * always immutable should implement an assertion because if the element
	 * has
	 * been initialized correctly (validator = NULL) this method should then be
	 * reached.
	 *
	 * @param newValue The new value for this element
	 */
	protected abstract void updateValue(T newValue);

	/**
	 * Checks two values for equality. The default implementation invokes the
	 * method {@link Object#equals(Object)} but subclasses can override this
	 * for
	 * more specific comparisons (e.g. by using {@link Comparable}).
	 *
	 * @param a The first value to compare (can be be NULL)
	 * @param b The second value to compare (can be be NULL)
	 * @return TRUE if the values are equal
	 */
	protected boolean valuesEqual(T a, T b) {
		return Objects.equals(a, b);
	}

	/**
	 * Package-internal method to set the parent of this element.
	 *
	 * @param newParent The new parent element
	 */
	final void setParent(DataElementList newParent) {
		parent = newParent;
	}

	/**
	 * Package-internal method to set the resource ID.
	 *
	 * @param resourceId The resource ID
	 */
	final void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
}
