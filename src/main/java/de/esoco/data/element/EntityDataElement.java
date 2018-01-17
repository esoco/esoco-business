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

import de.esoco.lib.property.PropertyName;

import java.util.List;
import java.util.Set;


/********************************************************************
 * A data element implementation that holds the attributes of an entity.
 *
 * @author eso
 */
public class EntityDataElement extends DataElementList
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The name of the attribute data element containing the child */
	public static final String CHILDREN_ELEMENT = "CHILDREN";

	//~ Instance fields --------------------------------------------------------

	private String sChildPrefix;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sName        The name of this data element
	 * @param sResourceId  The resource to label the data element with
	 * @param sChildPrefix The prefix string for child elements
	 * @param rAttributes  The child elements that represent the entity
	 *                     attributes (including children)
	 * @param rFlags       The optional flags for this data element
	 */
	public EntityDataElement(String				  sName,
							 String				  sResourceId,
							 String				  sChildPrefix,
							 List<DataElement<?>> rAttributes,
							 Set<Flag>			  rFlags)
	{
		super(sName, sResourceId, rAttributes, rFlags);

		this.sChildPrefix = sChildPrefix;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	EntityDataElement()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public EntityDataElement copy(CopyMode eMode, PropertyName<?>... rCopyProperties)
	{
		EntityDataElement aCopy = (EntityDataElement) super.copy(eMode, rCopyProperties);

		aCopy.sChildPrefix = sChildPrefix;

		return aCopy;
	}

	/***************************************
	 * Overridden to return the simple name of the entity class.
	 *
	 * @see DataElementList#getChildResourceIdPrefix()
	 */
	@Override
	protected String getChildResourceIdPrefix()
	{
		return sChildPrefix;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected EntityDataElement newInstance()
	{
		return new EntityDataElement();
	}
}
