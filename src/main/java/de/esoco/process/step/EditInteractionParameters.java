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

import de.esoco.lib.property.LayoutType;
import de.esoco.lib.property.Updatable;
import org.obrel.core.RelationType;

import java.util.List;

/**
 * A fragment that allows to edit the elements of an interaction.
 *
 * @author eso
 */
public class EditInteractionParameters extends InteractionFragment
	implements Updatable {

	private static final long serialVersionUID = 1L;

	private final List<RelationType<?>> rootParams;

	private InteractionParameterTree elementTree;

	private EditInteractionParameter elementEditor;

	/**
	 * Creates a new instance.
	 *
	 * @param rootParams The root fragment to edit
	 */
	public EditInteractionParameters(List<RelationType<?>> rootParams) {
		this.rootParams = rootParams;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		setLayout(LayoutType.SPLIT, getFragmentParameter());

		elementTree = new InteractionParameterTree(rootParams);
		elementEditor = new EditInteractionParameter();

		addSubFragment(elementTree).width("300px");
		addSubFragment(elementEditor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update() {
	}

	/**
	 * This fragment allows the editing of a single element in the interaction
	 * hierarchy.
	 *
	 * @author eso
	 */
	public static class EditInteractionParameter extends InteractionFragment {

		private static final long serialVersionUID = 1L;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleInteraction(RelationType<?> interactionParam)
			throws Exception {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception {
			textParam("NoInteractionParamSelected")
				.display()
				.value("Please select a parameter");
		}
	}

	/**
	 * This fragment displays the tree of interaction elements and the controls
	 * to manipulate it.
	 *
	 * @author eso
	 */
	static class InteractionParameterTree extends InteractionFragment {

		private static final long serialVersionUID = 1L;

		private final List<RelationType<?>> rootParams;

		/**
		 * Creates a new instance.
		 *
		 * @param rootParams The root parameters to display in the tree
		 */
		public InteractionParameterTree(List<RelationType<?>> rootParams) {
			this.rootParams = rootParams;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unused")
		public void init() throws Exception {
			for (RelationType<?> param : rootParams) {
			}
		}
	}
}
