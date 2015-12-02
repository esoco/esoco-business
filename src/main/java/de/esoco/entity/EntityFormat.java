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
package de.esoco.entity;

import de.esoco.lib.expression.function.AbstractBinaryFunction;
import de.esoco.lib.expression.function.AbstractFunction;

import java.util.Collection;

import org.obrel.core.RelationType;
import org.obrel.type.StandardTypes;


/********************************************************************
 * A function that converts an entity into a formatted string value.
 *
 * @author eso
 */
public class EntityFormat<E extends Entity> extends AbstractFunction<E, String>
{
	//~ Instance fields --------------------------------------------------------

	private final String sNullString;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sNullString The string to be displayed if the input entity is NULL
	 * @param bImmutable  TRUE for an immutable right value
	 */
	public EntityFormat(String sNullString, boolean bImmutable)
	{
		super("EntityFormat");

		this.sNullString = sNullString;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Formats an entity in to a describing string.
	 *
	 * @param  rEntity The entity to format
	 *
	 * @return The resulting string
	 */
	public static String toString(Entity rEntity)
	{
		EntityDefinition<?>		    rDefinition = rEntity.getDefinition();
		Collection<RelationType<?>> rAttributes = rDefinition.getAttributes();
		String					    sResult     = null;

		if (rAttributes.contains(StandardTypes.NAME))
		{
			sResult = rEntity.get(StandardTypes.NAME);

			String sFirstName = rEntity.get(StandardTypes.FIRST_NAME);

			if (sFirstName != null && sFirstName.length() > 0)
			{
				sResult = sFirstName + " " + sResult;
			}
		}
		else if (rAttributes.contains(StandardTypes.INFO))
		{
			String sInfo = rEntity.get(StandardTypes.INFO);

			if (sInfo != null && sInfo.length() > 0)
			{
				sResult = sInfo;
			}
		}
		else
		{
			RelationType<Enum<?>> rTypeAttribute = rEntity.getTypeAttribute();

			if (rTypeAttribute != null)
			{
				Enum<?> eType = rEntity.get(rTypeAttribute);

				if (eType != null)
				{
					sResult = eType.name();
				}
			}
		}

		return sResult != null ? sResult : "";
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see AbstractBinaryFunction#evaluate(Object, Object)
	 */
	@Override
	public String evaluate(E rEntity)
	{
		String sResult = sNullString;

		if (rEntity != null)
		{
			sResult = toString(rEntity);
		}

		return sResult;
	}
}
