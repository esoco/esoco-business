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
import org.obrel.core.RelationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	private RelationType<List<RelationType<?>>> rootFragmentParam;

	private final InteractionFragment rootFragment;

	/**
	 * Creates a new instance that displays a single interaction fragment.
	 *
	 * @param fragment The interaction fragment
	 */
	public FragmentInteraction(InteractionFragment fragment) {
		rootFragment = fragment;
	}

	/**
	 * Creates a new instance that displays multiple interaction fragments in
	 * tabs. The fragment instances can either be set in the variable argument
	 * list of this method or later in an overridden
	 * {@link #prepareExecution()}
	 * method. In the first case the number of fragments must be equal to the
	 * number of fragment parameters or else there must be no fragments at all.
	 *
	 * @param tabsParam      The parameter for the tabs
	 * @param fragmentParams One tab parameter for each fragment
	 * @param fragments      The interaction fragments for the tabs
	 */
	public FragmentInteraction(RelationType<List<RelationType<?>>> tabsParam,
		List<RelationType<List<RelationType<?>>>> fragmentParams,
		InteractionFragment... fragments) {
		rootFragmentParam = tabsParam;
		rootFragment = new RootFragment(fragmentParams, fragments);
	}

	/**
	 * Returns the root fragment that of this interaction.
	 *
	 * @return The root fragment
	 */
	public final InteractionFragment getRootFragment() {
		return rootFragment;
	}

	/**
	 * Returns the parameter wrapper for the root fragment's parameter relation
	 * type.
	 *
	 * @return The root fragment parameter
	 */
	public final ParameterList getRootFragmentParam() {
		return rootFragment.fragmentParam();
	}

	/**
	 * Overridden to ignore a single fragment parameter without content to
	 * prevent process lockup.
	 *
	 * @see Interaction#needsInteraction()
	 */
	@Override
	protected boolean needsInteraction() throws Exception {
		return super.needsInteraction() && (rootFragmentParam == null ||
			getParameter(rootFragmentParam).size() > 0);
	}

	/**
	 * @see Interaction#prepareExecution()
	 */
	@Override
	protected void prepareExecution() throws Exception {
		// generate a temporary parameter type for a single fragment
		if (rootFragmentParam == null) {
			String name = FRAGMENT_PARAM_NAME + "_" + getFragmentId();

			rootFragmentParam = getTemporaryListType(name, RelationType.class);
		}

		addDisplayParameters(rootFragmentParam);
		addSubFragment(rootFragmentParam, rootFragment);

		rootFragment
			.fragmentParam()
			.hideLabel()
			.resid(FRAGMENT_PARAM_NAME)
			.label("")
			.tooltip("")
			.style(rootFragment.getClass().getSimpleName());

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

		private final List<RelationType<List<RelationType<?>>>> fragmentParams =
			new ArrayList<>();

		private final List<InteractionFragment> fragments = new ArrayList<>();

		/**
		 * Creates a new instance.
		 *
		 * @param fragmentParams The fragment parameters
		 * @param fragments      The interaction fragments for the parameters
		 */
		public RootFragment(
			List<RelationType<List<RelationType<?>>>> fragmentParams,
			InteractionFragment... fragments) {
			// allow empty fragment list for lazy initialization
			assert fragments.length == 0 ||
				fragmentParams.size() == fragments.length :
				"Fragment and param counts must be equal";

			this.fragmentParams.addAll(fragmentParams);
			this.fragments.addAll(Arrays.asList(fragments));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void init() throws Exception {
			layout(LayoutType.TABS).set(HTML_HEIGHT, "100%");

			addDisplayParameters(fragmentParams);

			for (int i = 0; i < fragments.size(); i++) {
				addSubFragment(fragmentParams.get(i), fragments.get(i));
			}
		}
	}
}
