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
package de.esoco.process.step.ui;

import de.esoco.process.step.ui.component.Label;
import de.esoco.process.step.ui.component.TextArea;
import de.esoco.process.step.ui.component.TextField;

import java.util.List;

import org.obrel.core.RelationType;


/********************************************************************
 * The base class for all UI containers.
 *
 * @author eso
 */
public abstract class Container<C extends Container<C>>
	extends Component<List<RelationType<?>>, C>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see Component#Component(Container, RelationType)
	 */
	public Container(Container<?> rContainer)
	{
		super(rContainer, null);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a label.
	 *
	 * @param  sText The label text
	 *
	 * @return The new component
	 */
	public Label addLabel(String sText)
	{
		return new Label(this, sText);
	}

	/***************************************
	 * Adds a single-line text input field.
	 *
	 * @return The new component
	 */
	public TextArea addTextArea()
	{
		return new TextArea(this);
	}

	/***************************************
	 * Adds a single-line text input field.
	 *
	 * @return The new component
	 */
	public TextField addTextField()
	{
		return new TextField(this);
	}
}
