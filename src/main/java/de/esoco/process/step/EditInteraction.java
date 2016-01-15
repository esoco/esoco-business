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
package de.esoco.process.step;

import de.esoco.data.element.DataElementList.ListDisplayMode;

import de.esoco.lib.property.Updatable;

import java.util.List;

import org.obrel.core.RelationType;

import static org.obrel.core.RelationTypes.newListType;


/********************************************************************
 * A fragment that allows to edit the elements of an interaction.
 *
 * @author eso
 */
public class EditInteraction extends InteractionFragment implements Updatable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** Standard parameter to display this fragment in. */
	public static final RelationType<List<RelationType<?>>> EDIT_INTERACTION_FRAGMENT =
		newListType();

	//~ Instance fields --------------------------------------------------------

	private InteractionElementTree aElementTree;
	private EditInteractionElement aElementEditor;

	private InteractionFragment rRootFragment;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Updates this instance to edit the hierarchy of the given root fragment.
	 *
	 * @param rRootFragment The root fragment to edit
	 */
	public void editInteraction(InteractionFragment rRootFragment)
	{
		this.rRootFragment = rRootFragment;

		update();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		aElementTree   = new InteractionElementTree();
		aElementEditor = new EditInteractionElement();

		addSubFragment(InteractionElementTree.INTERACTION_ELEMENT_TREE_FRAGMENT,
					   aElementTree);
		addSubFragment(EditInteractionElement.EDIT_INTERACTION_ELEMENT_FRAGMENT,
					   aElementEditor);
		setListDisplayMode(ListDisplayMode.SPLIT, getFragmentParameter());
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
	public static class EditInteractionElement extends InteractionFragment
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		/** Standard parameter to display this fragment in. */
		public static final RelationType<List<RelationType<?>>> EDIT_INTERACTION_ELEMENT_FRAGMENT =
			newListType();

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
		}
	}

	/********************************************************************
	 * This fragment displays the tree of interaction elements and the controls
	 * to manipulate it.
	 *
	 * @author eso
	 */
	static class InteractionElementTree extends InteractionFragment
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		/** Standard parameter to display this fragment in. */
		public static final RelationType<List<RelationType<?>>> INTERACTION_ELEMENT_TREE_FRAGMENT =
			newListType();

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception
		{
		}
	}
}
