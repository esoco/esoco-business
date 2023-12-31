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

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.ViewDisplayType;
import de.esoco.lib.text.TextConvert;

import de.esoco.process.step.InteractionFragment;

import java.util.ArrayList;
import java.util.List;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.LayoutProperties.VERTICAL_ALIGN;
import static de.esoco.lib.property.LayoutProperties.VIEW_DISPLAY_TYPE;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.STYLE;

/**
 * A process interaction fragment that can be displayed in a dialog view.
 *
 * @author eso
 */
public class ViewFragment extends InteractionFragment {

	private static final long serialVersionUID = 1L;

	static {
		RelationTypes.init(ViewFragment.class);
	}

	private final String sParamBaseName;

	private final String sViewFragmentParamStyle;

	private final InteractionFragment rContentFragment;

	private final ViewDisplayType eViewDisplayType;

	private RelationType<List<RelationType<?>>> aViewFragmentParam;

	private RelationType<List<RelationType<?>>> aViewContentParam;

	private List<RelationType<?>> aInteractionParams = new ArrayList<>();

	private List<RelationType<?>> aInputParams = new ArrayList<>();

	/**
	 * Creates a new view fragment with a certain name and optional content
	 * fragment. The parameter name template must contain a string formatting
	 * pattern with a single '%s' placeholder that will be replaced with the
	 * view type. The remaining string should define an uppercase identifier
	 * that is separated by underscores. If the pattern doesn't contain a
	 * placeholder it will be used as a prefix.
	 *
	 * @param sParamNameTemplate The string format pattern for the
	 *                              generation of
	 *                           the view fragment parameter names. If NULL a
	 *                           template will be generated from the class name
	 *                           of the content fragment
	 * @param rContentFragment   The fragment that contains the view content
	 * @param eViewDisplayType   How the view should be displayed
	 */
	public ViewFragment(String sParamNameTemplate,
		InteractionFragment rContentFragment,
		ViewDisplayType eViewDisplayType) {
		if (sParamNameTemplate == null) {
			sParamNameTemplate = TextConvert.uppercaseIdentifier(
				rContentFragment.getClass().getSimpleName());
		}

		if (!sParamNameTemplate.contains("%s")) {
			sParamNameTemplate += "_%s";
		}

		this.rContentFragment = rContentFragment;
		this.eViewDisplayType = eViewDisplayType;

		sParamBaseName = String.format(sParamNameTemplate, getViewType(true));
		sViewFragmentParamStyle =
			TextConvert.capitalize(sParamBaseName, "", false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters() {
		return aInputParams;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters() {
		return aInteractionParams;
	}

	/**
	 * Returns the parameter that holds the view content.
	 *
	 * @return The view content parameter
	 */
	public final RelationType<List<RelationType<?>>> getViewContentParam() {
		return aViewContentParam;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception {
		if (rInteractionParam == fragmentParam().type()) {
			// will happen on update event if AUTO_HIDE flag is present
			hide();
		}
	}

	/**
	 * Hides this view.
	 */
	public void hide() {
		getParent().removeViewFragment(aViewFragmentParam);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		String sBaseName = getResourceBaseName();

		setUIFlag(HIDE_LABEL, aViewContentParam, aViewFragmentParam);

		setUIProperty(RESOURCE_ID, sBaseName, aViewFragmentParam);
		setUIProperty(RESOURCE_ID, sBaseName + "Content", aViewContentParam);
		setUIProperty(VIEW_DISPLAY_TYPE, eViewDisplayType, aViewFragmentParam);

		if (sViewFragmentParamStyle != null) {
			setUIProperty(STYLE, sViewFragmentParamStyle, aViewFragmentParam);
		}

		if (rContentFragment != null) {
			addSubFragment(aViewContentParam, rContentFragment);
		}
	}

	/**
	 * Initializes the parameters of the parent fragment to display the dialog
	 * for this fragment.
	 *
	 * @param rParent The parent fragment to display the dialog in
	 */
	public void show(InteractionFragment rParent) {
		aViewFragmentParam =
			rParent.getTemporaryListType(sParamBaseName, RelationType.class);

		attach(rParent.getProcessStep(), aViewFragmentParam);
		markInputParams(true, aViewFragmentParam);

		aViewContentParam = getTemporaryListType(sParamBaseName + "_CONTENT",
			RelationType.class);

		aInteractionParams.add(aViewContentParam);
		addExtraViewInteractionParams(sParamBaseName);

		rParent.addViewFragment(aViewFragmentParam, this);
	}

	/**
	 * This method can be overridden by subclasses to add additional
	 * interaction
	 * parameters to this instance. The default implementation does nothing.
	 *
	 * @param sParamBaseName The base name for temporary parameters of this
	 *                       instance
	 */
	protected void addExtraViewInteractionParams(String sParamBaseName) {
	}

	/**
	 * Returns the base name for resource IDs as a camel case identifier. The
	 * default implementation returns the simple name of the content fragment's
	 * class. If no content fragment has been set the view type returned by
	 * {@link #getViewType(boolean)} will be used.
	 *
	 * @return The resource ID base name
	 */
	protected String getResourceBaseName() {
		String sBaseName = getViewType(false);

		if (rContentFragment != null) {
			sBaseName =
				rContentFragment.getClass().getSimpleName() + sBaseName;
		}

		return sBaseName;
	}

	/**
	 * Returns the type of this view fragment. By default this will be the name
	 * of the view implementation class without any 'Fragment' suffix.
	 *
	 * @param bUpperCase TRUE to return an upper case identifier, FALSE for
	 *                   camel case
	 * @return The view type string
	 */
	protected String getViewType(boolean bUpperCase) {
		String sBaseName;

		sBaseName = getClass().getSimpleName();

		int nSuffixPos = sBaseName.indexOf("Fragment");

		if (nSuffixPos > 0) {
			sBaseName = sBaseName.substring(0, nSuffixPos);
		}

		return bUpperCase ?
		       TextConvert.uppercaseIdentifier(sBaseName) :
		       sBaseName;
	}

	/**
	 * Overridden to transfer properties from the content fragment parameter to
	 * the view parameter after all child fragments have been initialized.
	 *
	 * @see InteractionFragment#initComplete()
	 */
	@Override
	protected void initComplete() throws Exception {
		Alignment eViewAlignment =
			getUIProperty(VERTICAL_ALIGN, aViewContentParam);

		if (eViewAlignment != null) {
			setUIProperty(VERTICAL_ALIGN, eViewAlignment, aViewFragmentParam);
		}
	}
}
