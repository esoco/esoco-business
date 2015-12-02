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

import de.esoco.lib.property.UserInterfaceProperties.ContentType;
import de.esoco.lib.property.UserInterfaceProperties.InteractiveInputMode;

import java.net.URL;

import java.util.List;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static de.esoco.lib.property.UserInterfaceProperties.COLUMNS;
import static de.esoco.lib.property.UserInterfaceProperties.COLUMN_SPAN;
import static de.esoco.lib.property.UserInterfaceProperties.CONTENT_TYPE;
import static de.esoco.lib.property.UserInterfaceProperties.HAS_IMAGES;
import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.HTML_HEIGHT;
import static de.esoco.lib.property.UserInterfaceProperties.HTML_WIDTH;
import static de.esoco.lib.property.UserInterfaceProperties.INTERACTIVE_INPUT_MODE;
import static de.esoco.lib.property.UserInterfaceProperties.SAME_ROW;

import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * A fragment that displays a website. The address of the website must be passed
 * as an argument to the constructor. If the URL parameter is not passed or is
 * NULL an empty page will be displayed. The URL will be displayed in an input
 * field that can be edited by the user and displayed by pressing a button or
 * the enter key in the edit field. Modifications of the URL will be stored in
 * the input parameter.
 *
 * @author u.eggers
 */
public class DisplayUrl extends InteractionFragment
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the available actions for the displayed URL.
	 */
	public enum UrlAction { BROWSE, RESTORE, SAVE }

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/**
	 * A standard parameter that can be used to display this fragment in a
	 * process step.
	 */
	public static final RelationType<List<RelationType<?>>> DISPLAY_URL_FRAGMENT =
		newType();

	/**
	 * The URL string to be used to display an empty page if no input URL is
	 * provided.
	 */
	public static final String EMPTY_PAGE_URL = "about:blank";

	static
	{
		RelationTypes.init(DisplayUrl.class);
	}

	//~ Instance fields --------------------------------------------------------

	/** The URL to display in this fragment. */
	private RelationType<URL> aUrlParameter;

	/** The parameter containing the URL input field. */
	private RelationType<String> aUrlInputParameter;

	/** The parameter containing the fragment's browser frame. */
	private RelationType<String> aBrowserFrameParameter;

	/** The actions for the URL */
	private RelationType<UrlAction> aUrlAction;

	/** The browser frame URL for opening in a separate browser tab. */
	private RelationType<String> aOpenUrlParameter;

	private URL rUrl;
	private URL rInputUrl;

	private UrlChangeListener rUrlChangeListener;

	private List<RelationType<?>> aInteractionParams;
	private List<RelationType<?>> aInputParams;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public DisplayUrl()
	{
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rUrl               The URL to display
	 * @param rUrlChangeListener A {@link UrlChangeListener}
	 */
	public DisplayUrl(URL rUrl, UrlChangeListener rUrlChangeListener)
	{
		this.rUrl			    = rUrl;
		this.rUrlChangeListener = rUrlChangeListener;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInputParameters()
	{
		return aInputParams;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public List<RelationType<?>> getInteractionParameters()
	{
		return aInteractionParams;
	}

	/***************************************
	 * Returns the current URL of this instance.
	 *
	 * @return The current URL
	 */
	public URL getURL()
	{
		return getParameter(aUrlParameter);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void handleInteraction(RelationType<?> rInteractionParam)
		throws Exception
	{
		String sUrl = getParameter(aUrlInputParameter);

		if (rInteractionParam == aUrlInputParameter)
		{
			updateUrl(sUrl);
		}
		else if (rInteractionParam == aUrlAction)
		{
			switch (getParameter(aUrlAction))
			{
				case BROWSE:
					updateUrl(sUrl);
					break;

				case RESTORE:
					updateUrl(rInputUrl);
					break;

				case SAVE:

					URL rNewUrl = new URL(updateUrl(sUrl));

					setParameter(aUrlParameter, rNewUrl);

					if (rUrlChangeListener != null)
					{
						rUrlChangeListener.onUrlChanged(rNewUrl);
					}

					break;
			}
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception
	{
		rInputUrl = getParameter(aUrlParameter);

		updateUrl(rInputUrl);

		setUIFlag(SAME_ROW, aUrlAction, aOpenUrlParameter);
		setUIFlag(HAS_IMAGES, aUrlAction, aOpenUrlParameter);
		setUIFlag(HIDE_LABEL,
				  aBrowserFrameParameter,
				  aUrlAction,
				  aUrlInputParameter);

		setUIProperty(HTML_WIDTH,
					  "100%",
					  aBrowserFrameParameter,
					  aUrlInputParameter);
		setUIProperty(HTML_HEIGHT, "100%", aBrowserFrameParameter);
		setUIProperty(3, COLUMNS, aUrlAction);
		setUIProperty(3, COLUMN_SPAN, aBrowserFrameParameter);

		setUIProperty(CONTENT_TYPE,
					  ContentType.WEBSITE,
					  aBrowserFrameParameter);
		setUIProperty(CONTENT_TYPE,
					  ContentType.ABSOLUTE_URL,
					  aOpenUrlParameter);

		setUIProperty(INTERACTIVE_INPUT_MODE,
					  InteractiveInputMode.ACTION,
					  aUrlInputParameter);

		setImmediateAction(aUrlAction);
	}

	/***************************************
	 * Sets the current URL of this instance.
	 *
	 * @param rUrl The new URL
	 */
	public void setURL(URL rUrl)
	{
		this.rUrl = rUrl;
		setParameter(aUrlParameter, rUrl);
	}

	/***************************************
	 * Updates the URL if it has changed during the interaction.
	 *
	 * @see InteractionFragment#afterInteraction(RelationType)
	 */
	@Override
	protected void afterInteraction(RelationType<?> rInteractionParam)
	{
		URL rNewUrl = getParameter(aUrlParameter);

		if ((rNewUrl == null && rInputUrl != null) ||
			rNewUrl != null && !rNewUrl.equals(rInputUrl))
		{
			rInputUrl = rNewUrl;
			updateUrl(rInputUrl);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void setup()
	{
		aUrlParameter		   = getNamedTmpParameterType("URL", URL.class);
		aUrlInputParameter     =
			getNamedTmpParameterType("UrlInput", String.class);
		aBrowserFrameParameter =
			getNamedTmpParameterType("BrowserFrame", String.class);
		aUrlAction			   =
			getNamedTmpParameterType("UrlAction", UrlAction.class);
		aOpenUrlParameter	   =
			getNamedTmpParameterType("OpenUrl", String.class);

		aInputParams	   = staticParams(aUrlInputParameter, aUrlAction);
		aInteractionParams =
			staticParams(aUrlInputParameter,
						 aUrlAction,
						 aOpenUrlParameter,
						 aBrowserFrameParameter);

		setParameter(aUrlParameter, rUrl);
	}

	/***************************************
	 * Updates the URL input field and the browser frame to the given URL.
	 *
	 * @param rUrl The new URL
	 */
	private void updateUrl(URL rUrl)
	{
		updateUrl(rUrl != null ? rUrl.toString() : null);
	}

	/***************************************
	 * Updates the URL input field and the browser frame to the given URL
	 * string.
	 *
	 * @param  sUrl The new URL string
	 *
	 * @return The URL, modified if necessary
	 */
	private String updateUrl(String sUrl)
	{
		setParameter(aUrlInputParameter, sUrl);

		if (sUrl == null)
		{
			sUrl = EMPTY_PAGE_URL;
		}
		else
		{
			String sUrlLowercase = sUrl.toLowerCase();

			if (!(sUrlLowercase.startsWith("http://") ||
				  sUrlLowercase.startsWith("https://")))
			{
				sUrl = "http://" + sUrl;
			}
		}

		setParameter(aBrowserFrameParameter, sUrl);
		setParameter(aOpenUrlParameter, sUrl);

		return sUrl;
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * A listener interface for URL changes
	 *
	 * @author ueggers
	 */
	public interface UrlChangeListener
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Called, if the URL changes
		 *
		 * @param rNewUrl The new URL value
		 */
		public void onUrlChanged(URL rNewUrl);
	}
}
