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

import de.esoco.lib.property.ContentType;
import de.esoco.lib.property.InteractiveInputMode;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.net.URL;
import java.util.List;

import static de.esoco.lib.property.ContentProperties.CONTENT_TYPE;
import static de.esoco.lib.property.LayoutProperties.COLUMNS;
import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.StateProperties.INTERACTIVE_INPUT_MODE;
import static de.esoco.lib.property.StyleProperties.HAS_IMAGES;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static org.obrel.core.RelationTypes.newType;

/**
 * A fragment that displays a website. The address of the website must be passed
 * as an argument to the constructor. If the URL parameter is not passed or is
 * NULL an empty page will be displayed. The URL will be displayed in an input
 * field that can be edited by the user and displayed by pressing a button or
 * the enter key in the edit field. Modifications of the URL will be stored in
 * the input parameter.
 *
 * @author u.eggers
 */
public class DisplayUrl extends InteractionFragment {

	/**
	 * Enumeration of the available actions for the displayed URL.
	 */
	public enum UrlAction {BROWSE, RESTORE, SAVE}

	/**
	 * A standard parameter that can be used to display this fragment in a
	 * process step.
	 */
	public static final RelationType<List<RelationType<?>>>
		DISPLAY_URL_FRAGMENT = newType();

	/**
	 * The URL string to be used to display an empty page if no input URL is
	 * provided.
	 */
	public static final String EMPTY_PAGE_URL = "about:blank";

	private static final long serialVersionUID = 1L;

	static {
		RelationTypes.init(DisplayUrl.class);
	}

	/**
	 * The URL to display in this fragment.
	 */
	private RelationType<URL> urlParameter;

	/**
	 * The parameter containing the URL input field.
	 */
	private RelationType<String> urlInputParameter;

	/**
	 * The parameter containing the fragment's browser frame.
	 */
	private RelationType<String> browserFrameParameter;

	/**
	 * The actions for the URL
	 */
	private RelationType<UrlAction> urlAction;

	/**
	 * The browser frame URL for opening in a separate browser tab.
	 */
	private RelationType<String> openUrlParameter;

	private URL url;

	private URL inputUrl;

	private UrlChangeListener urlChangeListener;

	private List<RelationType<?>> interactionParams;

	private List<RelationType<?>> inputParams;

	/**
	 * Creates a new instance.
	 */
	public DisplayUrl() {
	}

	/**
	 * Creates a new instance.
	 *
	 * @param url               The URL to display
	 * @param urlChangeListener A {@link UrlChangeListener}
	 */
	public DisplayUrl(URL url, UrlChangeListener urlChangeListener) {
		this.url = url;
		this.urlChangeListener = urlChangeListener;
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
	 * Returns the current URL of this instance.
	 *
	 * @return The current URL
	 */
	public URL getURL() {
		return getParameter(urlParameter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> interactionParam)
		throws Exception {
		String url = getParameter(urlInputParameter);

		if (interactionParam == urlInputParameter) {
			updateUrl(url);
		} else if (interactionParam == urlAction) {
			switch (getParameter(urlAction)) {
				case BROWSE:
					updateUrl(url);
					break;

				case RESTORE:
					updateUrl(inputUrl);
					break;

				case SAVE:

					URL newUrl = new URL(updateUrl(url));

					setParameter(urlParameter, newUrl);

					if (urlChangeListener != null) {
						urlChangeListener.onUrlChanged(newUrl);
					}

					break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		inputUrl = getParameter(urlParameter);

		updateUrl(inputUrl);

		setUIFlag(SAME_ROW, urlAction, openUrlParameter);
		setUIFlag(HAS_IMAGES, urlAction, openUrlParameter);
		setUIFlag(HIDE_LABEL, browserFrameParameter, urlAction,
			urlInputParameter);

		setUIProperty(HTML_WIDTH, "100%", browserFrameParameter,
			urlInputParameter);
		setUIProperty(HTML_HEIGHT, "100%", browserFrameParameter);
		setUIProperty(3, COLUMNS, urlAction);
		setUIProperty(3, COLUMN_SPAN, browserFrameParameter);

		setUIProperty(CONTENT_TYPE, ContentType.WEBSITE,
			browserFrameParameter);
		setUIProperty(CONTENT_TYPE, ContentType.ABSOLUTE_URL,
			openUrlParameter);

		setUIProperty(INTERACTIVE_INPUT_MODE, InteractiveInputMode.ACTION,
			urlInputParameter);

		setImmediateAction(urlAction);
	}

	/**
	 * Sets the current URL of this instance.
	 *
	 * @param url The new URL
	 */
	public void setURL(URL url) {
		this.url = url;
		setParameter(urlParameter, url);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup() {
		urlParameter = getNamedTmpParameterType("URL", URL.class);
		urlInputParameter = getNamedTmpParameterType("UrlInput", String.class);
		browserFrameParameter =
			getNamedTmpParameterType("BrowserFrame", String.class);
		urlAction = getNamedTmpParameterType("UrlAction", UrlAction.class);
		openUrlParameter = getNamedTmpParameterType("OpenUrl", String.class);

		inputParams = staticParams(urlInputParameter, urlAction);
		interactionParams =
			staticParams(urlInputParameter, urlAction, openUrlParameter,
				browserFrameParameter);

		setParameter(urlParameter, url);
	}

	/**
	 * Updates the URL if it has changed during the interaction.
	 *
	 * @see InteractionFragment#afterInteraction(RelationType)
	 */
	@Override
	protected void afterInteraction(RelationType<?> interactionParam) {
		URL newUrl = getParameter(urlParameter);

		if ((newUrl == null && inputUrl != null) ||
			newUrl != null && !newUrl.equals(inputUrl)) {
			inputUrl = newUrl;
			updateUrl(inputUrl);
		}
	}

	/**
	 * Updates the URL input field and the browser frame to the given URL.
	 *
	 * @param url The new URL
	 */
	private void updateUrl(URL url) {
		updateUrl(url != null ? url.toString() : null);
	}

	/**
	 * Updates the URL input field and the browser frame to the given URL
	 * string.
	 *
	 * @param url The new URL string
	 * @return The URL, modified if necessary
	 */
	private String updateUrl(String url) {
		setParameter(urlInputParameter, url);

		if (url == null) {
			url = EMPTY_PAGE_URL;
		} else {
			String urlLowercase = url.toLowerCase();

			if (!(urlLowercase.startsWith("http://") ||
				urlLowercase.startsWith("https://"))) {
				url = "http://" + url;
			}
		}

		setParameter(browserFrameParameter, url);
		setParameter(openUrlParameter, url);

		return url;
	}

	/**
	 * A listener interface for URL changes
	 *
	 * @author ueggers
	 */
	public interface UrlChangeListener {

		/**
		 * Called, if the URL changes
		 *
		 * @param newUrl The new URL value
		 */
		void onUrlChanged(URL newUrl);
	}
}
