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

import de.esoco.lib.model.DataModel;
import de.esoco.lib.model.HierarchicalDataModel;
import de.esoco.lib.model.ListDataModel;
import de.esoco.lib.property.Editable;
import de.esoco.lib.property.Flags;
import de.esoco.lib.property.HasId;

import java.io.Serializable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/********************************************************************
 * A simple hierarchical data object to efficiently transfer query data between
 * client and server. An object contains a list of string values and references
 * to parent and child objects. It also implements {@link HierarchicalDataModel}
 * so that it can be used directly in UI components.
 *
 * @author eso
 */
public class HierarchicalDataObject implements HierarchicalDataModel<String>,
											   HasId<String>, Flags<String>,
											   Editable, Serializable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	// the fields must be package accessible for the custom field serializer
	String			   sId;
	List<String>	   rValues;
	boolean			   bEditable = false;
	Collection<String> rFlags    = null;

	DataModel<DataModel<String>> aChildren = null;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new readonly instance without children or flags.
	 *
	 * @param sId     This object's ID
	 * @param rValues The attribute values
	 */
	public HierarchicalDataObject(String sId, List<String> rValues)
	{
		this.sId     = sId;
		this.rValues = rValues;
	}

	/***************************************
	 * Creates a new instance without children. The reference arguments will not
	 * be copied but will be used directly.
	 *
	 * @param sId       The object's ID
	 * @param rValues   The attribute values
	 * @param bEditable FALSE to mark the instance as readonly
	 * @param rFlags    The string flags for the object or NULL for none
	 */
	public HierarchicalDataObject(String			 sId,
								  List<String>		 rValues,
								  boolean			 bEditable,
								  Collection<String> rFlags)
	{
		this(sId, rValues);

		this.bEditable = bEditable;
		this.rFlags    = rFlags;
	}

	/***************************************
	 * Creates a new instance. The reference arguments will not be copied but
	 * will be used directly.
	 *
	 * @param sId       The object's ID
	 * @param rValues   The attribute values
	 * @param bEditable FALSE to mark the instance as readonly
	 * @param rFlags    The string flags for the object or NULL for none
	 * @param rChildren The list of child objects (NULL or empty for none)
	 */
	public HierarchicalDataObject(String				  sId,
								  List<String>			  rValues,
								  boolean				  bEditable,
								  Collection<String>	  rFlags,
								  List<DataModel<String>> rChildren)
	{
		this(sId, rValues, bEditable, rFlags);

		if (rChildren != null)
		{
			this.aChildren =
				new ListDataModel<DataModel<String>>("", rChildren);
		}
	}

	/***************************************
	 * Creates a new instance. The reference arguments will not be copied but
	 * will be used directly.
	 *
	 * @param sId       The object's ID
	 * @param rValues   The attribute values
	 * @param bEditable FALSE to mark the instance as readonly
	 * @param rFlags    The string flags for the object or NULL for none
	 * @param rChildren The child data model (NULL or empty for none)
	 */
	public HierarchicalDataObject(String					   sId,
								  List<String>				   rValues,
								  boolean					   bEditable,
								  Collection<String>		   rFlags,
								  DataModel<DataModel<String>> rChildren)
	{
		this(sId, rValues, bEditable, rFlags);

		this.aChildren = rChildren;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Clear a certain flag in this instance.
	 *
	 * @param sFlag The flag to clear
	 */
	public final void clearFlag(String sFlag)
	{
		if (rFlags != null)
		{
			rFlags.remove(sFlag);
		}
	}

	/***************************************
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object rObject)
	{
		if (this == rObject)
		{
			return true;
		}

		if (rObject == null || getClass() != rObject.getClass())
		{
			return false;
		}

		HierarchicalDataObject rOther = (HierarchicalDataObject) rObject;

		return sId.equals(rOther.sId) && rValues.equals(rOther.rValues) &&
			   getFlags().equals(rOther.getFlags()) &&
			   bEditable == rOther.bEditable &&
			   (aChildren == null && rOther.aChildren == null ||
				aChildren != null && aChildren.equals(rOther.aChildren));
	}

	/***************************************
	 * @see HierarchicalDataModel#getChildModels()
	 */
	@Override
	public DataModel<DataModel<String>> getChildModels()
	{
		return aChildren;
	}

	/***************************************
	 * @see HierarchicalDataModel#getElement(int)
	 */
	@Override
	public String getElement(int nIndex)
	{
		return rValues.get(nIndex);
	}

	/***************************************
	 * @see HierarchicalDataModel#getElementCount()
	 */
	@Override
	public int getElementCount()
	{
		return rValues.size();
	}

	/***************************************
	 * @see Flags#getFlags()
	 */
	@Override
	public final Collection<String> getFlags()
	{
		return rFlags != null ? rFlags : Collections.<String>emptySet();
	}

	/***************************************
	 * @see HasId#getId()
	 */
	@Override
	public final String getId()
	{
		return sId;
	}

	/***************************************
	 * @see Flags#hasFlag(Object)
	 */
	@Override
	public final boolean hasFlag(String sFlag)
	{
		return getFlags().contains(sFlag);
	}

	/***************************************
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime  = 31;
		int		  result = 1;

		result =
			prime * result + ((aChildren == null) ? 0 : aChildren.hashCode());
		result = prime * result + (bEditable ? 1231 : 1237);
		result = prime * result + getFlags().hashCode();
		result = prime * result + ((rValues == null) ? 0 : rValues.hashCode());
		result = prime * result + ((sId == null) ? 0 : sId.hashCode());

		return result;
	}

	/***************************************
	 * @see Editable#isEditable()
	 */
	@Override
	public boolean isEditable()
	{
		return bEditable;
	}

	/***************************************
	 * @see HierarchicalDataModel#iterator()
	 */
	@Override
	public Iterator<String> iterator()
	{
		return rValues.iterator();
	}

	/***************************************
	 * Sets a certain flag in this instance.
	 *
	 * @param sFlag The flag to set
	 */
	public final void setFlag(String sFlag)
	{
		if (rFlags == null)
		{
			rFlags = new HashSet<String>();
		}

		rFlags.add(sFlag);
	}

	/***************************************
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		return "HierarchicalDataObject" + rValues;
	}
}
