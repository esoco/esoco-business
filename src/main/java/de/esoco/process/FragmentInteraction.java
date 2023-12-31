//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.process;

import de.esoco.lib.property.LayoutType;

import de.esoco.process.param.ParameterList;
import de.esoco.process.step.Interaction;
import de.esoco.process.step.InteractionFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;

/**
 * An interactive process step that displays of one or more instances of the
 * class {@link InteractionFragment} in tabs or similar group panels.
 *
 * @author eso
 */
public class FragmentInteraction extends Interaction {

	private static final long serialVersionUID = 1L;

	private static final String FRAGMENT_PARAM_NAME =
		FragmentInteraction.class.getSimpleName();

	/**
	 * A default parameter to display a single fragment in.
	 */
	private RelationType<List<RelationType<?>>> rRootFragmentParam;

	private InteractionFragment rRootFragment;

	/**
	 * Creates a new instance that displays a single interaction fragment.
	 *
	 * @param rFragment The interaction fragment
	 */
	public FragmentInteraction(InteractionFragment rFragment) {
		rRootFragment = rFragment;
	}

	/**
	 * Creates a new instance that displays multiple interaction fragments in
	 * tabs. The fragment instances can either be set in the variable argument
	 * list of this method or later in an overridden
	 * {@link #prepareExecution()}
	 * method. In the first case the number of fragments must be equal to the
	 * number of fragment parameters or else there must be no fragments at all.
	 *
	 * @param rTabsParam      The parameter for the tabs
	 * @param rFragmentParams One tab parameter for each fragment
	 * @param rFragments      The interaction fragments for the tabs
	 */
	public FragmentInteraction(RelationType<List<RelationType<?>>> rTabsParam,
		List<RelationType<List<RelationType<?>>>> rFragmentParams,
		InteractionFragment... rFragments) {
		rRootFragmentParam = rTabsParam;
		rRootFragment = new RootFragment(rFragmentParams, rFragments);
	}

	/**
	 * Returns the root fragment that of this interaction.
	 *
	 * @return The root fragment
	 */
	public final InteractionFragment getRootFragment() {
		return rRootFragment;
	}

	/**
	 * Returns the parameter wrapper for the root fragment's parameter relation
	 * type.
	 *
	 * @return The root fragment parameter
	 */
	public final ParameterList getRootFragmentParam() {
		return rRootFragment.fragmentParam();
	}

	/**
	 * Overridden to ignore a single fragment parameter without content to
	 * prevent process lockup.
	 *
	 * @see Interaction#needsInteraction()
	 */
	@Override
	protected boolean needsInteraction() throws Exception {
		return super.needsInteraction() && (rRootFragmentParam == null ||
			getParameter(rRootFragmentParam).size() > 0);
	}

	/**
	 * @see Interaction#prepareExecution()
	 */
	@Override
	protected void prepareExecution() throws Exception {
		// generate a temporary parameter type for a single fragment
		if (rRootFragmentParam == null) {
			String sName = FRAGMENT_PARAM_NAME + "_" + getFragmentId();

			rRootFragmentParam =
				getTemporaryListType(sName, RelationType.class);
		}

		addDisplayParameters(rRootFragmentParam);
		addSubFragment(rRootFragmentParam, rRootFragment);

		rRootFragment
			.fragmentParam()
			.hideLabel()
			.resid(FRAGMENT_PARAM_NAME)
			.label("")
			.tooltip("")
			.style(rRootFragment.getClass().getSimpleName());

		super.prepareExecution();
	}

	/**
	 * A root fragment for fragment interactions that contain multiple
	 * fragments.
	 *
	 * @author eso
	 */
	static class RootFragment extends InteractionFragment {

		private static final long serialVersionUID = 1L;

		private List<RelationType<List<RelationType<?>>>> rFragmentParams =
			new ArrayList<>();

		private List<InteractionFragment> rFragments = new ArrayList<>();

		/**
		 * Creates a new instance.
		 *
		 * @param rFragmentParams The fragment parameters
		 * @param rFragments      The interaction fragments for the parameters
		 */
		public RootFragment(
			List<RelationType<List<RelationType<?>>>> rFragmentParams,
			InteractionFragment... rFragments) {
			// allow empty fragment list for lazy initialization
			assert rFragments.length == 0 ||
				rFragmentParams.size() == rFragments.length :
				"Fragment and param counts must be equal";

			this.rFragmentParams.addAll(rFragmentParams);
			this.rFragments.addAll(Arrays.asList(rFragments));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception {
			layout(LayoutType.TABS).set(HTML_HEIGHT, "100%");

			addDisplayParameters(rFragmentParams);

			for (int i = 0; i < rFragments.size(); i++) {
				addSubFragment(rFragmentParams.get(i), rFragments.get(i));
			}
		}
	}
}
