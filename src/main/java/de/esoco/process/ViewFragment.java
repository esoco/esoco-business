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
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.util.ArrayList;
import java.util.List;

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

	private final String paramBaseName;

	private final String viewFragmentParamStyle;

	private final InteractionFragment contentFragment;

	private final ViewDisplayType viewDisplayType;

	private RelationType<List<RelationType<?>>> viewFragmentParam;

	private RelationType<List<RelationType<?>>> viewContentParam;

	private final List<RelationType<?>> interactionParams = new ArrayList<>();

	private final List<RelationType<?>> inputParams = new ArrayList<>();

	/**
	 * Creates a new view fragment with a certain name and optional content
	 * fragment. The parameter name template must contain a string formatting
	 * pattern with a single '%s' placeholder that will be replaced with the
	 * view type. The remaining string should define an uppercase identifier
	 * that is separated by underscores. If the pattern doesn't contain a
	 * placeholder it will be used as a prefix.
	 *
	 * @param paramNameTemplate The string format pattern for the generation of
	 *                          the view fragment parameter names. If NULL a
	 *                          template will be generated from the class name
	 *                          of the content fragment
	 * @param contentFragment   The fragment that contains the view content
	 * @param viewDisplayType   How the view should be displayed
	 */
	public ViewFragment(String paramNameTemplate,
		InteractionFragment contentFragment, ViewDisplayType viewDisplayType) {
		if (paramNameTemplate == null) {
			paramNameTemplate = TextConvert.uppercaseIdentifier(
				contentFragment.getClass().getSimpleName());
		}

		if (!paramNameTemplate.contains("%s")) {
			paramNameTemplate += "_%s";
		}

		this.contentFragment = contentFragment;
		this.viewDisplayType = viewDisplayType;

		paramBaseName = String.format(paramNameTemplate, getViewType(true));
		viewFragmentParamStyle =
			TextConvert.capitalize(paramBaseName, "", false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters() {
		return inputParams;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters() {
		return interactionParams;
	}

	/**
	 * Returns the parameter that holds the view content.
	 *
	 * @return The view content parameter
	 */
	public final RelationType<List<RelationType<?>>> getViewContentParam() {
		return viewContentParam;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> interactionParam)
		throws Exception {
		if (interactionParam == fragmentParam().type()) {
			// will happen on update event if AUTO_HIDE flag is present
			hide();
		}
	}

	/**
	 * Hides this view.
	 */
	public void hide() {
		getParent().removeViewFragment(viewFragmentParam);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		String baseName = getResourceBaseName();

		setUIFlag(HIDE_LABEL, viewContentParam, viewFragmentParam);

		setUIProperty(RESOURCE_ID, baseName, viewFragmentParam);
		setUIProperty(RESOURCE_ID, baseName + "Content", viewContentParam);
		setUIProperty(VIEW_DISPLAY_TYPE, viewDisplayType, viewFragmentParam);

		if (viewFragmentParamStyle != null) {
			setUIProperty(STYLE, viewFragmentParamStyle, viewFragmentParam);
		}

		if (contentFragment != null) {
			addSubFragment(viewContentParam, contentFragment);
		}
	}

	/**
	 * Initializes the parameters of the parent fragment to display the dialog
	 * for this fragment.
	 *
	 * @param parent The parent fragment to display the dialog in
	 */
	public void show(InteractionFragment parent) {
		viewFragmentParam =
			parent.getTemporaryListType(paramBaseName, RelationType.class);

		attach(parent.getProcessStep(), viewFragmentParam);
		markInputParams(true, viewFragmentParam);

		viewContentParam = getTemporaryListType(paramBaseName + "_CONTENT",
			RelationType.class);

		interactionParams.add(viewContentParam);
		addExtraViewInteractionParams(paramBaseName);

		parent.addViewFragment(viewFragmentParam, this);
	}

	/**
	 * This method can be overridden by subclasses to add additional
	 * interaction
	 * parameters to this instance. The default implementation does nothing.
	 *
	 * @param paramBaseName The base name for temporary parameters of this
	 *                      instance
	 */
	protected void addExtraViewInteractionParams(String paramBaseName) {
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
		String baseName = getViewType(false);

		if (contentFragment != null) {
			baseName = contentFragment.getClass().getSimpleName() + baseName;
		}

		return baseName;
	}

	/**
	 * Returns the type of this view fragment. By default this will be the name
	 * of the view implementation class without any 'Fragment' suffix.
	 *
	 * @param upperCase TRUE to return an upper case identifier, FALSE for
	 *                     camel
	 *                  case
	 * @return The view type string
	 */
	protected String getViewType(boolean upperCase) {
		String baseName;

		baseName = getClass().getSimpleName();

		int suffixPos = baseName.indexOf("Fragment");

		if (suffixPos > 0) {
			baseName = baseName.substring(0, suffixPos);
		}

		return upperCase ? TextConvert.uppercaseIdentifier(baseName) :
		       baseName;
	}

	/**
	 * Overridden to transfer properties from the content fragment parameter to
	 * the view parameter after all child fragments have been initialized.
	 *
	 * @see InteractionFragment#initComplete()
	 */
	@Override
	protected void initComplete() throws Exception {
		Alignment viewAlignment =
			getUIProperty(VERTICAL_ALIGN, viewContentParam);

		if (viewAlignment != null) {
			setUIProperty(VERTICAL_ALIGN, viewAlignment, viewFragmentParam);
		}
	}
}
