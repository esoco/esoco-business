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
package de.esoco.process.ui;

import de.esoco.process.ParameterBase;


/********************************************************************
 * The base class for all process UI components.
 *
 * @author eso
 */
public abstract class Component<T, C extends Component<T, C>>
	extends ParameterBase<T, C>
{
	//~ Instance fields --------------------------------------------------------

	private final Container<?> rParent;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent   The parent container
	 * @param rDatatype The datatype of the component value
	 */
	public Component(Container<?> rParent, Class<? super T> rDatatype)
	{
		super(rParent != null ? rParent.fragment() : null,
			  rDatatype != null
			  ? rParent.fragment().getTemporaryParameterType(rDatatype) : null);

		this.rParent = rParent;

		if (rParent != null && rDatatype != null)
		{
			display();
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the parent container.
	 *
	 * @return The parent
	 */
	public final Container<?> getParent()
	{
		return rParent;
	}
}
