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
package de.esoco.process.step;

import de.esoco.lib.property.Updatable;
import de.esoco.lib.property.UserInterfaceProperties.Layout;

import java.util.List;

import org.obrel.core.RelationType;


/********************************************************************
 * A fragment that allows to edit the elements of an interaction.
 *
 * @author eso
 */
public class EditInteractionParameters extends InteractionFragment
	implements Updatable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private List<RelationType<?>> rRootParams;

	private InteractionParameterTree aElementTree;
	private EditInteractionParameter aElementEditor;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rRootParams The root fragment to edit
	 */
	public EditInteractionParameters(List<RelationType<?>> rRootParams)
	{
		this.rRootParams = rRootParams;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		setLayout(Layout.SPLIT, getFragmentParameter());

		aElementTree   = new InteractionParameterTree(rRootParams);
		aElementEditor = new EditInteractionParameter();

		addSubFragment(aElementTree).gridWidth("300px");
		addSubFragment(aElementEditor);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void update()
	{
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * This fragment allows the editing of a single element in the interaction
	 * hierarchy.
	 *
	 * @author eso
	 */
	public static class EditInteractionParameter extends InteractionFragment
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void handleInteraction(RelationType<?> rInteractionParam)
			throws Exception
		{
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception
		{
			textParam("NoInteractionParamSelected").display()
												   .value("Please select a parameter");
		}
	}

	/********************************************************************
	 * This fragment displays the tree of interaction elements and the controls
	 * to manipulate it.
	 *
	 * @author eso
	 */
	static class InteractionParameterTree extends InteractionFragment
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Instance fields ----------------------------------------------------

		private List<RelationType<?>> rRootParams;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rRootParams The root parameters to display in the tree
		 */
		public InteractionParameterTree(List<RelationType<?>> rRootParams)
		{
			this.rRootParams = rRootParams;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unused")
		public void init() throws Exception
		{
			for (RelationType<?> rParam : rRootParams)
			{
			}
		}
	}
}
