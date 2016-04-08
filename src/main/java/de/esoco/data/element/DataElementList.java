//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;


/********************************************************************
 * A data element implementation that contains a hierarchical structure of data
 * elements.
 *
 * @author eso
 */
public class DataElementList extends ListDataElement<DataElement<?>>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the display modes for data element lists that define how
	 * data elements will be arranged in the generated user interface. The
	 * possible values are:
	 *
	 * <ul>
	 *   <li>{@link #TABLE}: Data elements are placed in the cells of a
	 *     table-like structure (HTML: table).</li>
	 *   <li>{@link #DOCK}: Elements are arranged arround the edges of a center
	 *     element (HTML: divs with the center at 100% size). The size and
	 *     orientation (horizontal or vertical) of the surrounding must be set
	 *     as UI properties.</li>
	 *   <li>{@link #SPLIT}: Like {@link #DOCK} but with resizable side areas.
	 *   </li>
	 *   <li>{@link #TABS}: A panel with selectable tabs for each contained data
	 *     element (HTML: full size div).</li>
	 *   <li>{@link #STACK}: Like {@link #TABS} but arranged as a vertical stack
	 *     of collapsing stacks for each element child.</li>
	 *   <li>{@link #DECK}: Like {@link #TABS} but without an UI for selecting
	 *     child elements. Selection must occur programmatically.</li>
	 *   <li>{@link #FILL}: A single UI elements fills the available area (HTML:
	 *     div with 100% size).</li>
	 *   <li>{@link #FLOW}: UI elements flow in the natural order defined by the
	 *     UI context (HTML: div).</li>
	 *   <li>{@link #GRID}: Like FLOW but elements are automatically arranged
	 *     according to their properties like in a {@link #FORM} (HTML: div with
	 *     a CSS grid layout).</li>
	 *   <li>{@link #FORM}: Arranges data elements according to their properties
	 *     in an input form (HTML: form).</li>
	 *   <li>{@link #GROUP}: Arranges data elements in a distinctive group
	 *     (HTML: fieldset).</li>
	 *   <li>{@link #MENU}: A menu or navigation structure (HTML: nav).</li>
	 * </ul>
	 */
	public enum ListDisplayMode
	{
		TABLE, DOCK, SPLIT, TABS, STACK, DECK, FILL, FLOW, GRID, FORM, GROUP,
		MENU
	}

	/********************************************************************
	 * Enumeration of the style for displaying data element lists in separate
	 * views.
	 */
	public enum ViewDisplayType { DIALOG, MODAL_DIALOG, VIEW, MODAL_VIEW }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	private static final String PATH_SEPARATOR_STRING =
		String.valueOf(PATH_SEPARATOR_CHAR);

	/**
	 * UI property: the display mode for data elements lists. Defaults to a
	 * simple panel if not set.
	 */
	public static final PropertyName<ListDisplayMode> LIST_DISPLAY_MODE =
		PropertyName.newEnumName("LIST_DISPLAY_MODE", ListDisplayMode.class);

	/**
	 * UI property: the display style for a data element list that is displayed
	 * in a separate view.
	 */
	public static final PropertyName<ViewDisplayType> VIEW_DISPLAY_TYPE =
		PropertyName.newEnumName("VIEW_DISPLAY_TYPE", ViewDisplayType.class);

	//~ Instance fields --------------------------------------------------------

	private List<DataElement<?>> aDataElements =
		new ArrayList<DataElement<?>>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that is initialized from a certain set of data
	 * elements. The contents of the collection argument will be copied into
	 * this list element but not the collection itself.
	 *
	 * @param sName     The name of this element
	 * @param rElements A collection containing the initial elements of this
	 *                  list element (may be NULL)
	 */
	public DataElementList(
		String								 sName,
		Collection<? extends DataElement<?>> rElements)
	{
		this(sName, null, rElements, null);
	}

	/***************************************
	 * Creates a new instance with certain data elements and without a validator
	 * and flags.
	 *
	 * @param sName       The name of this element
	 * @param sResourceId A resource ID or NULL for the default
	 * @param rElements   The child data elements
	 */
	public DataElementList(String			 sName,
						   String			 sResourceId,
						   DataElement<?>... rElements)
	{
		this(sName, sResourceId, Arrays.asList(rElements), null);
	}

	/***************************************
	 * Creates a new instance that is initialized from a certain set of data
	 * elements. The contents of the collection argument will be copied into
	 * this list element but not the collection itself.
	 *
	 * @param sName       The name of this element
	 * @param sResourceId A resource ID or NULL for the default
	 * @param rElements   A collection containing the initial elements of this
	 *                    list element (may be NULL)
	 * @param rFlags      The optional flags for this data element
	 */
	public DataElementList(String								sName,
						   String								sResourceId,
						   Collection<? extends DataElement<?>> rElements,
						   Set<Flag>							rFlags)
	{
		super(sName, null, rFlags);

		setResourceId(sResourceId);

		if (rElements != null)
		{
			for (DataElement<?> rElement : rElements)
			{
				updateParent(rElement);
				aDataElements.add(rElement);
			}
		}
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	protected DataElementList()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Searches for a data element with a certain name in a collection of data
	 * elements. If the collection contains {@link DataElementList data element
	 * lists} these will be searched recursively.
	 *
	 * @param  sName     The name of the data element
	 * @param  rElements The elements to search
	 *
	 * @return The matching data element or NULL if no such element exists
	 */
	public static DataElement<?> findDataElement(
		String					   sName,
		Collection<DataElement<?>> rElements)
	{
		DataElement<?> rResult = null;

		for (DataElement<?> rElement : rElements)
		{
			if (sName.equals(rElement.getName()))
			{
				rResult = rElement;
			}
			else if (rElement instanceof DataElementList)
			{
				rResult =
					findDataElement(sName,
									((DataElementList) rElement)
									.getDataElements());
			}

			if (rResult != null)
			{
				break;
			}
		}

		return rResult;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Shortcut method to add a new {@link StringDataElement} with a certain
	 * name and string value. See {@link #addElement(DataElement)} for more
	 * information.
	 *
	 * @param sName  The name of the data element to add
	 * @param sValue The value of the new data element
	 */
	public void add(String sName, String sValue)
	{
		addElement(new StringDataElement(sName, sValue));
	}

	/***************************************
	 * Shortcut method to add a new {@link DateDataElement} with a certain name
	 * and {@link Date} value.
	 *
	 * @param sName  The name of the data element to add
	 * @param rValue The value of the new data element
	 */
	public void add(String sName, Date rValue)
	{
		addElement(new DateDataElement(sName, rValue, null, null));
	}

	/***************************************
	 * Shortcut method to add a new {@link IntegerDataElement} with a certain
	 * name and value.
	 *
	 * @param sName  The name of the data element to add
	 * @param nValue The value of the new data element
	 */
	public void add(String sName, int nValue)
	{
		addElement(new IntegerDataElement(sName, nValue, null, null));
	}

	/***************************************
	 * Shortcut method to add a new {@link BooleanDataElement} with a certain
	 * name and value.
	 *
	 * @param sName  The name of the data element to add
	 * @param bValue The value of the new data element
	 */
	@SuppressWarnings("boxing")
	public void add(String sName, boolean bValue)
	{
		addElement(new BooleanDataElement(sName, bValue, null));
	}

	/***************************************
	 * Overridden to modify the parent reference of the argument data element.
	 *
	 * @see ListDataElement#addElement(int, Object)
	 */
	@Override
	public void addElement(int nIndex, DataElement<?> rElement)
	{
		super.addElement(nIndex, rElement);
		updateParent(rElement);
	}

	/***************************************
	 * Searches for a child data element in the full hierarchy of this instance
	 * and returns it if found. Invokes {@link #findDataElement(String,
	 * Collection)} to perform the search.
	 *
	 * @param  sName The name of the data element to search
	 *
	 * @return The child data element with the given name or NULL for none
	 */
	public DataElement<?> findChild(String sName)
	{
		return findDataElement(sName, aDataElements);
	}

	/***************************************
	 * Returns the boolean value of a certain data element. The returned value
	 * will be the result of invoking {@link Boolean#booleanValue()} on the
	 * type-casted value of the data element with the given name if such exists.
	 * If not, FALSE will be returned. If the data element's value is not of
	 * type {@link Boolean} an exception will occur.
	 *
	 * @param  sName The name of the data element to return as a boolean value
	 *
	 * @return The boolean value of the data element or FALSE if none could be
	 *         found
	 */
	public boolean getBoolean(String sName)
	{
		DataElement<?> rElement = getElement(sName);

		return rElement != null ? ((Boolean) rElement.getValue())
								.booleanValue() : false;
	}

	/***************************************
	 * A convenience method that casts the return value of the name-based method
	 * {@link #getElementAt(String)} to a data element list. If the type of the
	 * named element is not {@link DataElementList} or if no such element exists
	 * NULL will be returned.
	 *
	 * @param  sElementPath The path of the element list to return
	 *
	 * @return The element list at the given path or NULL if no list with the
	 *         given name exists
	 */
	public DataElementList getChildList(String sElementPath)
	{
		DataElement<?> rElement = getElementAt(sElementPath);

		return rElement instanceof DataElementList ? (DataElementList) rElement
												   : null;
	}

	/***************************************
	 * Returns the list of data elements in this instance. The list must not be
	 * modified by the invoking code.
	 *
	 * @return The list of data elements
	 */
	public List<DataElement<?>> getDataElements()
	{
		return aDataElements;
	}

	/***************************************
	 * Returns the date value of a certain data element. The returned value will
	 * be the type-casted value of the data element with the given name if such
	 * exists. If not, NULL will be returned. If the data element's value is not
	 * of type {@link Date} an exception will occur.
	 *
	 * @param  sName The name of the data element to return as a date value
	 *
	 * @return The date value of the data element or NULL if none could be found
	 */
	public Date getDate(String sName)
	{
		DataElement<?> rElement = getElement(sName);

		return rElement != null ? (Date) rElement.getValue() : null;
	}

	/***************************************
	 * Returns the first element with a certain name from this list.
	 *
	 * @param  sElementName The name of the element to return
	 *
	 * @return The element or NULL if no element with the given name exists
	 */
	public DataElement<?> getElement(String sElementName)
	{
		for (DataElement<?> rElement : aDataElements)
		{
			if (rElement.getName().equals(sElementName))
			{
				return rElement;
			}
		}

		return null;
	}

	/***************************************
	 * Returns a particular data element from the hierarchy of this list. The
	 * element path argument can either be a simple element name of a path to an
	 * element in the hierarchy of this list's sub-lists. The path separator is
	 * a forward slash as defined by {@link DataElement#PATH_SEPARATOR_CHAR}.
	 *
	 * <p>If the path start with a separator it is considered to be absolute
	 * with the name of this list as the first path element. Without a leading
	 * separator it must be relative to this instance, i.e. the first element in
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
	 * @param  sElementPath The path of the element to return
	 *
	 * @return The element with the given path or NULL if no such element exists
	 */
	public DataElement<?> getElementAt(String sElementPath)
	{
		String[]	    aPathElements =
			sElementPath.split(PATH_SEPARATOR_STRING);
		DataElementList rCurrentList  = this;
		DataElement<?>  rResult		  = null;
		int			    nLastElement  = aPathElements.length - 1;
		int			    nPathElement  = 0;

		if (nLastElement > 0 && sElementPath.charAt(0) == PATH_SEPARATOR_CHAR)
		{
			if (aPathElements[1].equals(getName()))
			{
				if (nLastElement == 1)
				{
					rResult = this;
				}
				else
				{ // 0 = empty string, 1 = this element
					nPathElement = 2;
				}
			}
			else
			{
				throw new IllegalArgumentException("Absolute path must start with " +
												   getName());
			}
		}

		while (rResult == null &&
			   rCurrentList != null &&
			   nPathElement <= nLastElement)
		{
			String sElementName = aPathElements[nPathElement++];

			rResult = rCurrentList.getElement(sElementName);

			if (nPathElement <= nLastElement)
			{
				if (rResult instanceof DataElementList)
				{
					rCurrentList = (DataElementList) rResult;
					rResult		 = null;
				}
				else
				{
					throw new IllegalArgumentException("Not an element list: " +
													   sElementName);
				}
			}
		}

		return rResult;
	}

	/***************************************
	 * Returns the integer value of a certain data element. The returned value
	 * will be the result of invoking {@link Number#intValue()} on the
	 * type-casted value of the data element with the given name if such exists.
	 * If not, 0 (zero) will be returned. If the data element's value is not of
	 * type {@link Number} an exception will occur.
	 *
	 * @param  sName The name of the data element to return as an integer value
	 *
	 * @return The integer value of the data element or 0 if none could be found
	 */
	public int getInt(String sName)
	{
		DataElement<?> rElement = getElement(sName);

		return rElement != null ? ((Number) rElement.getValue()).intValue() : 0;
	}

	/***************************************
	 * Returns the string value of a certain data element. The returned value
	 * will be the result of invoking {@link Object#toString()} on the value of
	 * the data element with the given name if such exists. If not, NULL will be
	 * returned.
	 *
	 * @param  sName The name of the data element to return the string value of
	 *
	 * @return The string value of the data element or NULL if none could be
	 *         found
	 */
	public String getString(String sName)
	{
		DataElement<?> rElement = getElement(sName);

		return rElement != null ? rElement.getValue().toString() : null;
	}

	/***************************************
	 * Overridden to also mark the child hierarchy as modified.
	 */
	@Override
	public void markAsChanged()
	{
		super.markAsChanged();

		for (DataElement<?> rChildElement : aDataElements)
		{
			rChildElement.markAsChanged();
		}
	}

	/***************************************
	 * Overridden to modify the parent reference of the argument data element.
	 *
	 * @see ListDataElement#removeElement(Object)
	 */
	@Override
	public void removeElement(DataElement<?> rElement)
	{
		super.removeElement(rElement);
		rElement.setParent(null);
	}

	/***************************************
	 * Shortcut method to set a {@link StringDataElement} with a certain name
	 * and string value. See {@link #setElement(DataElement)} for more
	 * information.
	 *
	 * @param sName  The name of the data element to add
	 * @param sValue The value of the new data element
	 */
	public void set(String sName, String sValue)
	{
		setElement(new StringDataElement(sName, sValue));
	}

	/***************************************
	 * Sets an element in this instance. If an element with the given name
	 * already exists it will be replaced. Otherwise a new element will be added
	 * to the end of the list.
	 *
	 * @param rElement The element to set
	 */
	public void setElement(DataElement<?> rElement)
	{
		DataElement<?> rExisting = getElement(rElement.getName());

		if (rExisting != null)
		{
			int nIndex = getElementIndex(rExisting);

			removeElement(rExisting);
			addElement(nIndex, rElement);
		}
		else
		{
			addElement(rElement);
		}
	}

	/***************************************
	 * Can be overridden by subclasses to return a resource id prefix for child
	 * elements. This default implementation returns an empty string.
	 *
	 * @return The child resource id prefix
	 */
	protected String getChildResourceIdPrefix()
	{
		return "";
	}

	/***************************************
	 * @see ListDataElement#getList()
	 */
	@Override
	protected List<DataElement<?>> getList()
	{
		return aDataElements;
	}

	/***************************************
	 * Prepares the addition of an element to this list.
	 *
	 * @param rElement The element that will be added
	 */
	private void updateParent(DataElement<?> rElement)
	{
		DataElementList rOldParent = rElement.getParent();

		if (rOldParent != this)
		{
			if (rOldParent != null)
			{
				rOldParent.removeElement(rElement);
			}

			rElement.setParent(this);
		}
	}
}
