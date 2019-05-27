//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.process.ui.app;

import de.esoco.process.ui.UiBuilder;
import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.layout.UiInlineLayout;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import static de.esoco.lib.property.StateProperties.TARGET_ID;


/********************************************************************
 * A UI composite that provides the framework to render and manage a web page.
 * The page will typically consist of header, footer, content, and optionally
 * navigation menus which are added by this class in the required order. Menus
 * are added by overriding the methods {@link #addTopMenu(Collection)} and
 * {@link #addSideMenu(Collection)}.
 *
 * <p>By default the component itself is rendered with an inline layout, i.e. it
 * doesn't have a DOM representation and only the mentioned child components are
 * added to the DOM as direct children of the page's parent.</p>
 *
 * <p>This component can also be used to manage a full website by replacing or
 * switching the page content depending on the context. Content switching can be
 * achieved by embedding different pages a deck or tab panel, for example.</p>
 *
 * @author eso
 */
public class UiWebPage extends UiComposite<UiWebPage>
{
	//~ Static fields/initializers ---------------------------------------------

	// global index for menu IDs
	private static int nMenuId = 1;

	//~ Instance fields --------------------------------------------------------

	private Consumer<UiWebPage> fInitPage;

	private UiWebPageHeader  aPageHeader;
	private UiWebPageContent aPageContent;
	private UiWebPageFooter  aPageFooter;

	private UiTopMenu<?>  aTopMenu;
	private UiSideMenu<?> aSideMenu;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for direct usage, i.e. without subclassing. The
	 * argument function will be invoked from within the {@link
	 * #buildContent(UiBuilder)} method with this instance as the argument after the
	 * page structure has been created.
	 *
	 * @param rParent   The parent container (typically a root view)
	 * @param fInitPage A function that initializes the page after it's
	 *                  structure has been created
	 */
	public UiWebPage(UiContainer<?> rParent, Consumer<UiWebPage> fInitPage)
	{
		super(rParent, new UiInlineLayout());

		this.fInitPage = fInitPage;
	}

	/***************************************
	 * Constructor for subclassing.
	 *
	 * @param rParent The parent container (typically a root view)
	 */
	protected UiWebPage(UiContainer<?> rParent)
	{
		this(rParent, null);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds an empty side menu to this page.
	 *
	 * @see #addSideMenu(Collection)
	 */
	public <T> UiSideMenu<T> addSideMenu()
	{
		return addSideMenu(null);
	}

	/***************************************
	 * Adds a side navigation menu to this page. The actual menu creation is
	 * done by invoking {@link #createSideMenu(UiContainer)}. Because menus
	 * depend on other page components this call must be performed after the
	 * page structure has been fully created (typically by extending the method
	 * {@link #buildStructure()}).
	 *
	 * @param  rMenuItems The initial menu items to display
	 *
	 * @return The new top menu
	 */
	public <T> UiSideMenu<T> addSideMenu(Collection<T> rMenuItems)
	{
		Objects.requireNonNull(aPageHeader);

		UiSideMenu<T> aMenu = createSideMenu(aPageHeader, rMenuItems);

		aSideMenu = aMenu;

		return aMenu;
	}

	/***************************************
	 * Adds an empty top menu to this page.
	 *
	 * @see #addTopMenu(Collection)
	 */
	public <T> UiTopMenu<T> addTopMenu()
	{
		return addTopMenu(null);
	}

	/***************************************
	 * Adds a top navigation menu to this page. The actual menu creation is done
	 * by invoking {@link #createTopMenu(UiContainer)}. Because menus depend on
	 * other page components this call must be performed after the page
	 * structure has been fully created (typically by extending the method
	 * {@link #buildStructure()}).
	 *
	 * @param  rMenuItems The initial menu items to display
	 *
	 * @return The new top menu
	 */
	public <T> UiTopMenu<T> addTopMenu(Collection<T> rMenuItems)
	{
		Objects.requireNonNull(aPageHeader);

		UiTopMenu<T> aMenu = createTopMenu(aPageHeader, rMenuItems);

		aTopMenu = aMenu;

		return aMenu;
	}

	/***************************************
	 * Returns the content container.
	 *
	 * @return The content container
	 */
	public UiWebPageContent getContent()
	{
		return aPageContent;
	}

	/***************************************
	 * Returns the footer of this page.
	 *
	 * @return The page footer
	 */
	public UiWebPageFooter getFooter()
	{
		return aPageFooter;
	}

	/***************************************
	 * Returns the header of this page.
	 *
	 * @return The page header
	 */
	public UiWebPageHeader getHeader()
	{
		return aPageHeader;
	}

	/***************************************
	 * Returns the side menu of this page. If no menu has been created by
	 * overriding {@link #addSideMenu(Collection)} this method will return NULL.
	 *
	 * @return The side menu or NULL for none
	 */
	public UiSideMenu<?> getSideMenu()
	{
		return aSideMenu;
	}

	/***************************************
	 * Returns the top menu of this page. If no menu has been created by
	 * overriding {@link #addTopMenu(Collection)} this method will return NULL.
	 *
	 * @return The top menu or NULL for none
	 */
	public UiTopMenu<?> getTopMenu()
	{
		return aTopMenu;
	}

	/***************************************
	 * Builds and configures the website elements. Subclasses should typically
	 * not override this but instead append additional site building code to the
	 * {@link #buildStructure()} method or perform additional initializations
	 * (after building) by overriding{@link #initPage()}.
	 *
	 * @param rBuilder The parent container builder
	 */
	@Override
	protected void buildContent(UiBuilder<?> rBuilder)
	{
		buildStructure();
		initPage();

		if (aTopMenu != null && aSideMenu != null)
		{
			String sId = "PageMenu" + nMenuId++;

			aSideMenu.id(sId);
			aTopMenu.set(TARGET_ID, sId);
		}
	}

	/***************************************
	 * Builds the site structure.
	 */
	protected void buildStructure()
	{
		aPageHeader  = createHeader(this);
		aPageContent = createContent(this);
		aPageFooter  = createFooter(this);
	}

	/***************************************
	 * Creates the page content container. Subclasses can override this to
	 * return a subclass of {@link UiWebPageContent} if necessary.
	 *
	 * @param  rParent The parent container to create the content in
	 *
	 * @return The page header
	 */
	protected UiWebPageContent createContent(UiContainer<?> rParent)
	{
		return new UiWebPageContent(rParent);
	}

	/***************************************
	 * Create the page footer. Subclasses can override this to return a subclass
	 * of {@link UiWebPageFooter} if necessary.
	 *
	 * @param  rParent The parent container to create the footer in
	 *
	 * @return The page header
	 */
	protected UiWebPageFooter createFooter(UiContainer<?> rParent)
	{
		return new UiWebPageFooter(rParent);
	}

	/***************************************
	 * Creates the page header. Subclasses can override this to return a
	 * subclass of {@link UiWebPageHeader} if necessary.
	 *
	 * @param  rParent The parent container to create the header in
	 *
	 * @return The page header
	 */
	protected UiWebPageHeader createHeader(UiContainer<?> rParent)
	{
		return new UiWebPageHeader(rParent);
	}

	/***************************************
	 * Creates the side menu of this page. Will be invoked from {@link
	 * #addTopMenu(Collection)} and can be overridden to create a menu subclass
	 * if necessary.
	 *
	 * @param  rParent    The parent container of the menu
	 * @param  rMenuItems The initial menu items
	 *
	 * @return the new menu
	 */
	protected <T> UiSideMenu<T> createSideMenu(
		UiContainer<?> rParent,
		Collection<T>  rMenuItems)
	{
		return new UiSideMenu<T>(rParent, rMenuItems);
	}

	/***************************************
	 * Creates the top menu of this page. Will be invoked from {@link
	 * #addTopMenu(Collection)} and can be overridden to create a menu subclass
	 * if necessary.
	 *
	 * @param  rParent    The parent container of the menu
	 * @param  rMenuItems The initial menu items
	 *
	 * @return the new menu
	 */
	protected <T> UiTopMenu<T> createTopMenu(
		UiContainer<?> rParent,
		Collection<T>  rMenuItems)
	{
		return new UiTopMenu<T>(rParent, rMenuItems);
	}

	/***************************************
	 * Initializes the page content after it has been created by {@link
	 * #buildStructure()}. The default implementation invokes the initialization
	 * function if such has been provided at construction. When subclassing this
	 * method can be overridden to replace the super version with the page
	 * initialization code. Especially menus should be added here by means of
	 * {@link #addTopMenu(Collection)} and {@link #addSideMenu(Collection)}.
	 */
	protected void initPage()
	{
		if (fInitPage != null)
		{
			fInitPage.accept(this);
		}
	}
}
