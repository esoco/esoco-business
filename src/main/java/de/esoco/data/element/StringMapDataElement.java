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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/********************************************************************
 * A map data element implementation that contains string mappings. It uses a
 * {@link LinkedHashMap} to preserve the order in which mappings are added.
 *
 * @author eso
 */
public class StringMapDataElement extends MapDataElement<String, String>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Map<String, String> aDataMap = new LinkedHashMap<String, String>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain name.
	 *
	 * @param sName The name of the element
	 */
	public StringMapDataElement(String sName)
	{
		this(sName, null, null);
	}

	/***************************************
	 * @see MapDataElement#MapDataElement(String, Validator, Set)
	 */
	public StringMapDataElement(String					  sName,
								Validator<? super String> rValidator,
								Set<Flag>				  rFlags)
	{
		super(sName, rValidator, rFlags);
	}

	/***************************************
	 * Creates a new instance that is initialized from a certain map.
	 *
	 * @param sName            The name of this data element
	 * @param rInitialMappings The initial mappings
	 * @param rValidator       The validator for new values or NULL for none
	 * @param rFlags           The optional flags for this data element or NULL
	 *                         for none
	 */
	public StringMapDataElement(String					  sName,
								Map<String, String>		  rInitialMappings,
								Validator<? super String> rValidator,
								Set<Flag>				  rFlags)
	{
		this(sName, rValidator, rFlags);

		aDataMap.putAll(rInitialMappings);
	}

	/***************************************
	 * @see MapDataElement#MapDataElement()
	 */
	StringMapDataElement()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public StringMapDataElement copy(CopyMode eMode, PropertyName<?>... rCopyProperties)
	{
		return (StringMapDataElement) super.copy(eMode, rCopyProperties);
	}

	/***************************************
	 * Returns the internal map of this data element.
	 *
	 * @see MapDataElement#getMap()
	 */
	@Override
	public Map<String, String> getMap()
	{
		return aDataMap;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected StringMapDataElement newInstance()
	{
		return new StringMapDataElement();
	}
}
