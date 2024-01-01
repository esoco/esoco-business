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

import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.layout.UiFooterLayout;
import de.esoco.process.ui.style.DefaultStyleNames;

import java.util.Date;

/**
 * A composite that represents the header of a web page.
 *
 * @author eso
 */
public class UiWebPageFooter extends UiComposite<UiWebPageFooter> {

	/**
	 * Creates a new instance.
	 *
	 * @param parent the parent container
	 */
	public UiWebPageFooter(UiContainer<?> parent) {
		super(parent, new UiFooterLayout());
	}

	/**
	 * Adds a copyright message to the footer that includes the current year.
	 * The label has the style name {@link DefaultStyleNames#FOOTER_COPYRIGHT}.
	 *
	 * @param copyrightHolder The display name of the copyright holder(s)
	 */
	public void addCopyrightMessage(String copyrightHolder) {
		builder()
			.addLabel(String.format("$$Copyright %tY {%s}", new Date(),
				copyrightHolder))
			.styleName(DefaultStyleNames.FOOTER_COPYRIGHT);
	}
}
