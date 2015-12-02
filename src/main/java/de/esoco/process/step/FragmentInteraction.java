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

import de.esoco.data.element.DataElementList;
import de.esoco.data.element.DataElementList.ListDisplayMode;

import de.esoco.lib.property.UserInterfaceProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.obrel.core.RelationType;

import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.HTML_HEIGHT;
import static de.esoco.lib.property.UserInterfaceProperties.LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.RESOURCE_ID;
import static de.esoco.lib.property.UserInterfaceProperties.TOOLTIP;


/********************************************************************
 * An interactive process step that displays of one or more instances of the
 * class {@link InteractionFragment} in tabs or similar group panels.
 *
 * @author eso
 */
public class FragmentInteraction extends Interaction
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	private static final String FRAGMENT_PARAM = "ProcessStepFragment";

	private static int nFragmentId = 0;

	//~ Instance fields --------------------------------------------------------

	/** A default parameter to display a single fragment in. */
	private RelationType<List<RelationType<?>>> rFragmentParam;

	private RelationType<List<RelationType<List<RelationType<?>>>>> rTabsParam =
		null;

	private List<RelationType<List<RelationType<?>>>> rFragmentParams =
		new ArrayList<>();

	private List<InteractionFragment> rFragments = new ArrayList<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that displays a single interaction fragment.
	 *
	 * @param rFragment The interaction fragment
	 */
	public FragmentInteraction(InteractionFragment rFragment)
	{
		rFragments.add(rFragment);
	}

	/***************************************
	 * Creates a new instance that displays multiple interaction fragments in
	 * tabs. The fragment instances can either be set in the variable argument
	 * list of this method or later in an overridden {@link #prepareExecution()}
	 * method. In the first case the number of fragments must be equal to the
	 * number of fragment parameters or else there must be no fragments at all.
	 *
	 * @param rTabsParam      The parameter for the tabs
	 * @param rFragmentParams One tab parameter for each fragment
	 * @param rFragments      The interaction fragments for the tabs
	 */
	public FragmentInteraction(
		RelationType<List<RelationType<List<RelationType<?>>>>> rTabsParam,
		List<RelationType<List<RelationType<?>>>>				rFragmentParams,
		InteractionFragment... 									rFragments)
	{
		assert rFragments.length == 0 ||
			   rFragmentParams.size() == rFragments.length : "Fragment and param counts must be equal";

		this.rTabsParam = rTabsParam;

		this.rFragmentParams.addAll(rFragmentParams);
		this.rFragments.addAll(Arrays.asList(rFragments));

		addInputParameters(rTabsParam);
		markInputParams(true, rFragmentParams);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to ignore a single fragment parameter without content to
	 * prevent process lockup.
	 *
	 * @see Interaction#needsInteraction()
	 */
	@Override
	protected boolean needsInteraction() throws Exception
	{
		return super.needsInteraction() &&
			   (rFragmentParam == null ||
				getParameter(rFragmentParam).size() > 0);
	}

	/***************************************
	 * @see Interaction#prepareExecution()
	 */
	@Override
	protected void prepareExecution() throws Exception
	{
		if (rTabsParam != null)
		{
			setParameter(rTabsParam, rFragmentParams);

			setUIFlag(HIDE_LABEL, rTabsParam);
			setUIProperty(HTML_HEIGHT, "100%", rTabsParam);
			setUIProperty(DataElementList.LIST_DISPLAY_MODE,
						  ListDisplayMode.TABS,
						  rTabsParam);
		}
		else
		{
			if (rFragmentParam == null)
			{
				rFragmentParam =
					getTemporaryListType(FRAGMENT_PARAM + "_" + nFragmentId++,
										 RelationType.class);
				rFragmentParams.add(rFragmentParam);
				addInputParameters(rFragmentParam);
			}

			setUIFlag(HIDE_LABEL, rFragmentParam);
			setUIProperty(RESOURCE_ID, FRAGMENT_PARAM, rFragmentParam);
			setUIProperty(LABEL, "", rFragmentParam);
			setUIProperty(TOOLTIP, "", rFragmentParam);
			setUIProperty(UserInterfaceProperties.STYLE,
						  rFragments.get(0).getClass().getSimpleName(),
						  rFragmentParam);
		}

		int nFragments = rFragments.size();

		for (int i = 0; i < nFragments; i++)
		{
			addFragment(rFragmentParams.get(i), rFragments.get(i));
		}

		super.prepareExecution();
	}

	/***************************************
	 * @see Interaction#prepareFragmentInteractions()
	 */
	@Override
	protected void prepareFragmentInteractions() throws Exception
	{
		Collection<InteractionFragment> rFragments = getFragments();

		int nIndex = 0;

		for (InteractionFragment rFragment : rFragments)
		{
			setParameter(rFragmentParams.get(nIndex++),
						 rFragment.getInteractionParameters());

			rFragment.markFragmentInputParams();
		}

		super.prepareFragmentInteractions();
	}
}
