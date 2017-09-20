//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.process.ui.composite;

import de.esoco.lib.property.Alignment;

import de.esoco.process.ui.UiComposite;
import de.esoco.process.ui.UiContainer;
import de.esoco.process.ui.UiImageDefinition;
import de.esoco.process.ui.component.UiImage;
import de.esoco.process.ui.graphics.UiStandardIcon;
import de.esoco.process.ui.layout.UiColumnGridLayout;
import de.esoco.process.ui.layout.UiInlineLayout;
import de.esoco.process.ui.style.SizeUnit;
import de.esoco.process.ui.view.UiPopupView;

import static de.esoco.lib.property.ContentProperties.LABEL;
import static de.esoco.lib.property.ContentProperties.TOOLTIP;


/********************************************************************
 * A composite that display an small image that on click events opens a view
 * that displays the full-size image. If a caption label has been set on the
 * image it will be used as the title of the full-size view.
 *
 * @author eso
 */
public class UiThumbnail extends UiComposite<UiThumbnail>
{
	//~ Instance fields --------------------------------------------------------

	private UiImage aImage;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a default width of 300 pixels.
	 *
	 * @param rParent   The parent container
	 * @param rImageDef The image to display
	 */
	public UiThumbnail(UiContainer<?> rParent, UiImageDefinition<?> rImageDef)
	{
		super(rParent, new UiInlineLayout());

		aImage = builder().addImage(rImageDef).onClick(this::displayImageView);
		width(300);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the caption of the thumbnail image.
	 *
	 * @param  sCaption The image caption
	 *
	 * @return This instance
	 */
	public UiThumbnail caption(String sCaption)
	{
		aImage.setCaption(sCaption);

		return this;
	}

	/***************************************
	 * Sets the thumbnail image.
	 *
	 * @param rImage The component's image.
	 */
	public void setImage(UiImageDefinition<?> rImage)
	{
		aImage.setImage(rImage);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public UiThumbnail tooltip(String sTooltip)
	{
		aImage.tooltip(sTooltip);

		return this;
	}

	/***************************************
	 * Sets the width of the thumbnail image in pixels.
	 *
	 * @param  nPixelWidth The width in pixels
	 *
	 * @return This instance
	 */
	public UiThumbnail width(int nPixelWidth)
	{
		cell().width(nPixelWidth, SizeUnit.PIXEL);

		return this;
	}

	/***************************************
	 * Displays the full-size image if the thumbnail image is clicked.
	 */
	private void displayImageView()
	{
		UiPopupView aImageView =
			new UiPopupView(getView(), new UiColumnGridLayout(), true);

		aImageView.builder().addTitle(aImage.get(LABEL));
		aImageView.builder()
				  .addIconButton(UiStandardIcon.CLOSE)
				  .onClick(v -> aImageView.hide())
				  .cell()
				  .colSpan(1)
				  .alignHorizontal(Alignment.END);
		aImageView.nextRow();
		aImageView.builder()
				  .addImage(aImage.getImage())
				  .tooltip(aImage.get(TOOLTIP));

		aImageView.center().autoHide().show();
	}
}
