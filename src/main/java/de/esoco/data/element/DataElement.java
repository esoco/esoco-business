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

import de.esoco.lib.property.PropertyName;
import de.esoco.lib.property.StringProperties;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.text.TextConvert;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;


/********************************************************************
 * A base class for named data values which can be used to transfer data in
 * serialized form between a server and a client. Data elements can be
 * hierarchical, i.e. a data element can be a child of another data element
 * which must be an instance of {@link DataElementList}. The parent of a data
 * element can be queried with the method {@link #getParent()}.
 *
 * @author eso
 */
public abstract class DataElement<T> extends StringProperties
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Flags that can be set on a data element.
	 */
	public enum Flag { IMMUTABLE, OPTIONAL, SELECTED, MODIFIED }

	/********************************************************************
	 * The modes for copying data elements with {@link
	 * DataElement#copy(CopyMode)}.
	 */
	public enum CopyMode { FULL, FLAT, PROPERTIES, PLACEHOLDER }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** TODO: DOCUMENT ME */
	public static UnchangedElement UNCHANGED_ELEMENT = new UnchangedElement();

	/** The prefix for item resource IDs */
	public static final String ITEM_RESOURCE_PREFIX = "$itm";

	/** A generic resource string for a &lt;New&gt; item. */
	public static final String ITEM_NEW_RESOURCE = "$itmNew";

	/** A generic resource string for an &lt;All&gt; item. */
	public static final String ITEM_ALL_RESOURCE = "$itmAll";

	/** The separator for data element paths */
	public static final char PATH_SEPARATOR_CHAR = '/';

	/** Flag property: allowed values have been modified. */
	public static final PropertyName<Boolean> ALLOWED_VALUES_CHANGED =
		PropertyName.newBooleanName("ALLOWED_VALUES_CHANGED");

	/** String property: an URL to be invoked in a separate browser window. */
	public static final PropertyName<String> INTERACTION_URL =
		PropertyName.newStringName("INTERACTION_URL");

	/**
	 * Boolean property: used in conjunction with {@link #INTERACTION_URL},
	 * indicates that the URL is app-relative and to be invoked in a hidden
	 * frame, typically to initiate a download.
	 */
	public static final PropertyName<Boolean> HIDDEN_URL =
		PropertyName.newBooleanName("HIDDEN_URL");

	/** The set of data element flags for display-only data elements */
	public static final EnumSet<Flag> DISPLAY_FLAGS =
		EnumSet.of(Flag.IMMUTABLE);

	/** The set of data element flags for input data elements */
	public static final EnumSet<Flag> INPUT_FLAGS = EnumSet.noneOf(Flag.class);

	//~ Instance fields --------------------------------------------------------

	private String				 sName;
	private Validator<? super T> rValidator;

	private DataElementList rParent     = null;
	private String		    sResourceId = null;

	private boolean bImmutable = false;
	private boolean bOptional  = false;
	private boolean bSelected  = false;
	private boolean bModified  = false;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain name and validator. The validator
	 * object will be used to validate any value that is set through the method
	 * {@link #setValue(Object)}.
	 *
	 * @param sName      The name of this data element
	 * @param rValidator The validator for new values or NULL for none
	 * @param rFlags     The optional flags for this data element or NULL for
	 *                   none
	 */
	public DataElement(String				sName,
					   Validator<? super T> rValidator,
					   Set<Flag>			rFlags)
	{
		this.sName	    = sName;
		this.rValidator = rValidator;

		if (rFlags != null)
		{
			bImmutable = rFlags.contains(Flag.IMMUTABLE);
			bOptional  = rFlags.contains(Flag.OPTIONAL);
		}
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	protected DataElement()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates the string name for an item value (typically an enum constant).
	 * The name will be composed from the simple name of the value's datatype
	 * class and the capitalized string representation of the value.
	 *
	 * @param  rValue The value to create the item name for
	 *
	 * @return The resulting item string (an empty string for a NULL value)
	 */
	public static String createItemName(Object rValue)
	{
		StringBuilder aItem = new StringBuilder();

		if (rValue != null)
		{
			Class<? extends Object> rDatatype  = rValue.getClass();
			String				    sClassName = rDatatype.getSimpleName();

			// replace empty name of anonymous classes with name of parent class
			if (sClassName.isEmpty())
			{
				sClassName = rDatatype.getSuperclass().getSimpleName();
			}

			aItem.append(sClassName);
			aItem.append(TextConvert.capitalizedIdentifier(rValue.toString()));
		}

		return aItem.toString();
	}

	/***************************************
	 * Creates an item resource string for a certain value.
	 *
	 * @param  rValue The value
	 *
	 * @return The resulting item string (an empty string for a NULL value)
	 */
	public static String createItemResource(Object rValue)
	{
		StringBuilder aItem = new StringBuilder();

		if (rValue != null)
		{
			aItem.append(ITEM_RESOURCE_PREFIX);
			aItem.append(createItemName(rValue));
		}

		return aItem.toString();
	}

	/***************************************
	 * This method should be invoked to initialize the property name constants
	 * for de-serialization.
	 */
	public static void init()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * //~ Methods
	 * ----------------------------------------------------------------
	 * /*************************************** Returns the value of this
	 * element. Each subclass must implement this method separately because some
	 * environments (like GWT) require serializable objects to use specific
	 * types for serializable fields. Therefore it is not possible to store a
	 * generic Object value in the base class.
	 *
	 * @return The element value
	 */
	public abstract T getValue();

	/***************************************
	 * Returns a copy of this data element that contains all or a subset of it's
	 * current state. Always copied are the name and {@link Flag flags}. Never
	 * copied is the parent reference because upon copying typically a reference
	 * to a copied parent needs to be set. What else the copy contains depends
	 * on the copy mode:
	 *
	 * <ul>
	 *   <li>{@link CopyMode#FULL}: The copy contains all data (except the
	 *     parent reference)</li>
	 *   <li>{@link CopyMode#FLAT}: like {@link CopyMode#FULL} but without
	 *     sub-ordinate data elements</li>
	 *   <li>{@link CopyMode#PROPERTIES}: The copy contains only the properties
	 *     but not the element value</li>
	 *   <li>{@link CopyMode#PLACEHOLDER}: The copy contains only the element
	 *     name to serve as a placeholder</li>
	 * </ul>
	 *
	 * <p>The copy instance is created by invoking {@link #newInstance()} which
	 * has the recommendation to overwrite the return type to the concrete
	 * subtype to prevent the need for casting by the invoking code. For the
	 * same reason it is recommended that subclasses also override this method
	 * with the concrete return type and cast the result of <code>
	 * super.copy()</code> to that type.</p>
	 *
	 * @param  eMode The copy mode
	 *
	 * @return The copied instance
	 */
	public DataElement<T> copy(CopyMode eMode)
	{
		DataElement<T> aCopy = newInstance();

		copyAttributes(aCopy, eMode);

		if (eMode == CopyMode.FULL || eMode == CopyMode.FLAT)
		{
			copyValue(aCopy);
		}

		if (eMode != CopyMode.PLACEHOLDER)
		{
			aCopy.setProperties(this, true);
		}

		return aCopy;
	}

	/***************************************
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object rObj)
	{
		if (this == rObj)
		{
			return true;
		}

		if (!super.equals(rObj) || getClass() != rObj.getClass())
		{
			return false;
		}

		DataElement<?> rOther = (DataElement<?>) rObj;

		return Objects.equals(sName, rOther.sName) && hasEqualValueAs(rOther) &&
			   bImmutable == rOther.bImmutable &&
			   bModified == rOther.bModified && bOptional == rOther.bOptional &&
			   bSelected == rOther.bSelected &&
			   Objects.equals(sResourceId, rOther.sResourceId) &&
			   Objects.equals(rValidator, rOther.rValidator) &&
			   Objects.equals(rParent, rOther.rParent);
	}

	/***************************************
	 * Returns the name of this data element.
	 *
	 * @return The data element's name
	 */
	public final String getName()
	{
		return sName;
	}

	/***************************************
	 * Returns the parent data element list of this element or NULL if it
	 * doesn't have a parent.
	 *
	 * @return The parent element or NULL for none
	 */
	public final DataElementList getParent()
	{
		return rParent;
	}

	/***************************************
	 * Returns the full path of this element in it's hierarchy. The returned
	 * path will always start with the {@link #PATH_SEPARATOR_CHAR}.
	 *
	 * @return The full element path
	 */
	public final String getPath()
	{
		StringBuilder  aPath    = new StringBuilder();
		DataElement<?> rElement = this;

		do
		{
			aPath.insert(0, PATH_SEPARATOR_CHAR);
			aPath.insert(1, rElement.getName());
			rElement = rElement.getParent();
		}
		while (rElement != null);

		return aPath.toString();
	}

	/***************************************
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
	 * resource ID with the parent's child prefix that is returned by the method
	 * {@link DataElementList#getChildResourceIdPrefix()}.</p>
	 *
	 * @return The resource ID for this element
	 */
	public final String getResourceId()
	{
		if (sResourceId == null)
		{
			sResourceId = getProperty(RESOURCE_ID, null);

			if (sResourceId == null)
			{
				sResourceId = createResourceId();
			}

			if (rParent != null)
			{
				sResourceId = rParent.getChildResourceIdPrefix() + sResourceId;
			}
		}

		return sResourceId;
	}

	/***************************************
	 * Returns the root data element list of this element's hierarchy or NULL if
	 * this element doesn't have a parent at all.
	 *
	 * @return The root element or NULL for none
	 */
	public final DataElementList getRoot()
	{
		DataElementList rRoot = rParent;

		if (rRoot != null)
		{
			while (rRoot.getParent() != null)
			{
				rRoot = rRoot.getParent();
			}
		}

		return rRoot;
	}

	/***************************************
	 * Returns the name of this element without a preceding package.
	 *
	 * @return The simple data element name
	 */
	public final String getSimpleName()
	{
		return TextConvert.lastElementOf(sName);
	}

	/***************************************
	 * Returns the validator of this element.
	 *
	 * @return The validator or NULL if this element is read-only
	 */
	public final Validator<? super T> getValidator()
	{
		return rValidator;
	}

	/***************************************
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return 37 * sName.hashCode() + getValueHashCode();
	}

	/***************************************
	 * Returns the immutable state of this element. If this method returns TRUE
	 * an invocation of the method {@link #setValue(Object)} will result in an
	 * {@link UnsupportedOperationException} being thrown.
	 *
	 * @return TRUE if this element is immutable, FALSE if the element value can
	 *         be modified
	 */
	public final boolean isImmutable()
	{
		return bImmutable;
	}

	/***************************************
	 * Returns TRUE if this element's value has been modified.
	 *
	 * @return TRUE if the element value has changed
	 */
	public final boolean isModified()
	{
		return bModified;
	}

	/***************************************
	 * Returns the immutable state of this element. If this method returns TRUE
	 * an invocation of the method {@link #setValue(Object)} will result in an
	 * {@link UnsupportedOperationException} being thrown.
	 *
	 * @return TRUE if this element is immutable, FALSE if the element value can
	 *         be modified
	 */
	public final boolean isOptional()
	{
		return bOptional;
	}

	/***************************************
	 * Returns the selected state of this element. This attribute will be set
	 * for optional data elements depending on the state of the associated
	 * option selector in the UI.
	 *
	 * @return TRUE if this element is selected
	 */
	public boolean isSelected()
	{
		return bSelected;
	}

	/***************************************
	 * Checks whether a certain value is valid for this element. A value is
	 * considered valid if either this element has no validator, or if the value
	 * is NULL and the flag {@link Flag#OPTIONAL} is set, or if the method
	 * {@link Validator#isValid(Object)} of this element's validator returns
	 * TRUE for the given value. This validation will also be performed by
	 * {@link #setValue(Object)} to throw an exception if a new value is
	 * invalid. Therefore applications can use this method to verify values
	 * before setting them (e.g. when performing input validation).
	 *
	 * @param  rValue The value to check
	 *
	 * @return TRUE if the value is valid for this data element
	 */
	public boolean isValidValue(T rValue)
	{
		return rValidator == null || (rValue == null && isOptional()) ||
			   rValidator.isValid(rValue);
	}

	/***************************************
	 * Marks the data element value as changed.
	 */
	public void markAsValueChanged()
	{
		setFlag(UserInterfaceProperties.VALUE_CHANGED);

		// setFlag will mark this instance as modified but only the value
		// should be marked as changed
		setModified(false);
	}

	/***************************************
	 * Removes a property from this element.
	 *
	 * @param rName The property name
	 */
	@Override
	public void removeProperty(PropertyName<?> rName)
	{
		if (hasProperty(rName))
		{
			super.removeProperty(rName);
			setModified(true);
		}
	}

	/***************************************
	 * Sets the modified state of this element.
	 *
	 * @param bModified The new modified state
	 */
	public void setModified(boolean bModified)
	{
		this.bModified = bModified;
	}

	/***************************************
	 * Sets a certain property of this data element and mark it as modified if
	 * the value has changed.
	 *
	 * @param rName     The name of the property
	 * @param rNewValue The property value
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <P> void setProperty(PropertyName<P> rName, P rNewValue)
	{
		P rCurrentValue = getProperty(rName, null);

		if (!Objects.equals(rCurrentValue, rNewValue))
		{
			super.setProperty(rName, rNewValue);
			setModified(true);
		}
	}

	/***************************************
	 * Sets the selected state of this element.
	 *
	 * @param bSelected The new selected state
	 */
	public void setSelected(boolean bSelected)
	{
		this.bSelected = bSelected;
		setModified(true);
	}

	/***************************************
	 * Sets a string value. This method must be overridden by subclasses that
	 * want to support the setting of string values in parallel to their native
	 * datatype. The default implementation always throws a runtime exception.
	 *
	 * @param  sValue The new string value
	 *
	 * @throws UnsupportedOperationException If not overridden
	 */
	public void setStringValue(String sValue)
	{
		throw new UnsupportedOperationException("Cannot convert " + sValue +
												" for " + this);
	}

	/***************************************
	 * Sets the validator of this element.
	 *
	 * @param rValidator The new validator
	 */
	public final void setValidator(Validator<? super T> rValidator)
	{
		this.rValidator = rValidator;
	}

	/***************************************
	 * Sets the value of this element. If this element is read only an exception
	 * will be thrown. See the method {@link #isImmutable()} for details.
	 *
	 * @param  rNewValue The new element value
	 *
	 * @throws UnsupportedOperationException If this element is read only
	 */
	public final void setValue(T rNewValue)
	{
		checkImmutable();
		checkValidValue(rNewValue);

		T rCurrentValue = getValue();

		if (!valuesEqual(rCurrentValue, rNewValue))
		{
			updateValue(rNewValue);
			setModified(true);
		}
	}

	/***************************************
	 * Returns a string representation of this element.
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		return sName + "[" + getValue() + "]";
	}

	/***************************************
	 * Returns a new instance of the respective data element sub-type on which
	 * it is invoked. This is needed for GWT which doesn't support reflection.
	 * Used by {@link #copy()} for cloning an instance. Implementations should
	 * overwrite the return type with their concrete type to prevent the need
	 * for casting by the invoking code.
	 *
	 * @return The new instance
	 */
	protected abstract DataElement<T> newInstance();

	/***************************************
	 * Updates the element value. Will be invoked by {@link #setValue(Object)}
	 * to store a new value after validation. If a subclass wants to reject
	 * certain values it should do so in the {@link #isValidValue(Object)}
	 * method. Subclasses that are always immutable should implement an
	 * assertion because if the element has been initialized correctly
	 * (validator = NULL) this method should then be reached.
	 *
	 * @param rNewValue The new value for this element
	 */
	protected abstract void updateValue(T rNewValue);

	/***************************************
	 * Checks whether this instance is read only. Should be invoked by
	 * subclasses that contain additional methods to modify attributes to
	 * support the read-only state of data elements.
	 *
	 * @throws UnsupportedOperationException If this element is read only
	 */
	protected final void checkImmutable()
	{
		if (isImmutable())
		{
			throw new UnsupportedOperationException("Element " + this +
													" is readonly");
		}
	}

	/***************************************
	 * Checks whether the given value is valid for this element and throws an
	 * exception if not. Invokes {@link #isValidValue(Object)} to validate the
	 * given value.
	 *
	 * @param  rValue The value to check
	 *
	 * @throws IllegalArgumentException If the given value is not valid for this
	 *                                  element
	 */
	protected final void checkValidValue(T rValue)
	{
		if (!isValidValue(rValue))
		{
			throw new IllegalArgumentException("Invalid value for " +
											   this + ": " + rValue);
		}
	}

	/***************************************
	 * Copies the attributes of this instance into another data element of the
	 * same type. Subclasses that have additional attributes beside their value
	 * should override this method to copy their attributes after invoking
	 * super.
	 *
	 * @param rTarget   The target to copy the attributes to
	 * @param eCopyMode The copy mode
	 */
	protected void copyAttributes(DataElement<T> rTarget, CopyMode eCopyMode)
	{
		rTarget.sName = sName;

		if (eCopyMode == CopyMode.PLACEHOLDER)
		{
			// make sure that a placeholder cannot be modified
			rTarget.bImmutable = true;
		}
		else
		{
			rTarget.bImmutable = bImmutable;
			rTarget.bOptional  = bOptional;
			rTarget.bSelected  = bSelected;
			rTarget.bModified  = bModified;
		}

		if (eCopyMode == CopyMode.FULL || eCopyMode == CopyMode.FLAT)
		{
			rTarget.rValidator  = rValidator;
			rTarget.sResourceId = sResourceId;
		}
	}

	/***************************************
	 * Copies the value of this data element into another data element of the
	 * same type. This is used by the {@link #copy()} method and by default uses
	 * {@link #updateValue(Object)} to set the value in the target element.
	 * Subclasses that don't support updating the value (e.g. because they
	 * manage a collection) need to override this method and implement the
	 * copying as needed. They can assume that the target object is of exactly
	 * the same type as their own.
	 *
	 * @param aCopy The copied data element to copy the value into
	 */
	protected void copyValue(DataElement<T> aCopy)
	{
		aCopy.updateValue(getValue());
	}

	/***************************************
	 * Creates a resource ID from this data element. This ID will only be
	 * created once and is then cached in a transient instance field. See the
	 * method {@link #getResourceId()} for details.
	 *
	 * @return The resource ID string
	 */
	protected String createResourceId()
	{
		String sResId = TextConvert.lastElementOf(sName);

		if (sResId.startsWith("__"))
		{
			sResId = "";
		}
		else
		{
			sResId = TextConvert.capitalizedIdentifier(sResId);
		}

		return sResId;
	}

	/***************************************
	 * Returns the value hash code. Can be overridden by subclasses that have a
	 * non-standard internal structure.
	 *
	 * @return The value hash code
	 */
	protected int getValueHashCode()
	{
		Object rValue = getValue();

		return (rValue != null ? rValue.hashCode() : 0);
	}

	/***************************************
	 * Checks whether this element has a value that is equal to that of another
	 * element. Can be overridden by subclasses that have a non-standard
	 * internal structure.
	 *
	 * @param  rOther The other data element which will always be of the same
	 *                type as this instance
	 *
	 * @return TRUE if the value are equal
	 */
	protected boolean hasEqualValueAs(final DataElement<?> rOther)
	{
		return Objects.equals(getValue(), rOther.getValue());
	}

	/***************************************
	 * Checks two values for equality. The default implementation invokes the
	 * method {@link Object#equals(Object)} but subclasses can override this for
	 * more specific comparisons (e.g. by using {@link Comparable}).
	 *
	 * @param  a The first value to compare (can be be NULL)
	 * @param  b The second value to compare (can be be NULL)
	 *
	 * @return TRUE if the values are equal
	 */
	protected boolean valuesEqual(T a, T b)
	{
		return Objects.equals(a, b);
	}

	/***************************************
	 * Package-internal method to set the parent of this element.
	 *
	 * @param rNewParent The new parent element
	 */
	final void setParent(DataElementList rNewParent)
	{
		rParent = rNewParent;
	}

	/***************************************
	 * Package-internal method to set the resource ID.
	 *
	 * @param sResourceId The resource ID
	 */
	final void setResourceId(String sResourceId)
	{
		this.sResourceId = sResourceId;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An empty data element as a placeholder for unchanged values.
	 *
	 * @author eso
	 */
	private static class UnchangedElement extends DataElement<String>
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * TODO: `Description`
		 */
		UnchangedElement()
		{
			super("UNCHANGED", null, DISPLAY_FLAGS);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public String getValue()
		{
			return null;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void setModified(boolean bModified)
		{
			// ignore
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected void copyAttributes(
			DataElement<String> rTarget,
			CopyMode			eCopyMode)
		{
			// ignore
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected UnchangedElement newInstance()
		{
			return UNCHANGED_ELEMENT;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected void updateValue(String sNewValue)
		{
			// ignore
		}
	}
}
