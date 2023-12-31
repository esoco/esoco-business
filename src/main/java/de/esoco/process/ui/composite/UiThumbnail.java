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

/**
 * A composite that display an small image that on click events opens a view
 * that displays the full-size image. If a caption label has been set on the
 * image it will be used as the title of the full-size view.
 *
 * @author eso
 */
public class UiThumbnail extends UiComposite<UiThumbnail> {

	private UiImage aImage;

	private UiImageDefinition<?> rFullImageDef;

	/**
	 * Creates a new instance that displays a down-scaled image as the
	 * thumbnail
	 * with a default width of 300 pixels.
	 *
	 * @param rParent The parent container
	 * @param rImage  The image to display
	 */
	public UiThumbnail(UiContainer<?> rParent, UiImageDefinition<?> rImage) {
		this(rParent, rImage, rImage);
	}

	/**
	 * Creates a new instance that displays a thumbnail with a default width of
	 * 300 pixels.
	 *
	 * @param rParent     The parent container
	 * @param rThumbImage The thumbnail image to display
	 * @param rFullImage  The full image to display if the the thumbnail is
	 *                    selected
	 */
	public UiThumbnail(UiContainer<?> rParent,
		UiImageDefinition<?> rThumbImage,
		UiImageDefinition<?> rFullImage) {
		super(rParent, new UiInlineLayout());

		this.rFullImageDef = rFullImage;

		aImage =
			builder().addImage(rThumbImage).onClick(this::displayImageView);
		width(300);
	}

	/**
	 * Sets the caption of the thumbnail image.
	 *
	 * @param sCaption The image caption
	 * @return This instance
	 */
	public UiThumbnail caption(String sCaption) {
		aImage.setCaption(sCaption);

		return this;
	}

	/**
	 * Sets the image to be displayed as a thumbnail and on selection. To set
	 * different images
	 * {@link #setImages(UiImageDefinition, UiImageDefinition)}
	 * can be used.
	 *
	 * @param rImage The component's image.
	 */
	public void setImage(UiImageDefinition<?> rImage) {
		aImage.setImage(rImage);
		rFullImageDef = rImage;
	}

	/**
	 * Sets the images for this thumbnail.
	 *
	 * @param rThumbImage The thumbnail image to display
	 * @param rFullImage  The full image to display if the the thumbnail is
	 *                    selected
	 */
	public void setImages(UiImageDefinition<?> rThumbImage,
		UiImageDefinition<?> rFullImage) {
		aImage.setImage(rThumbImage);
		rFullImageDef = rFullImage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UiThumbnail tooltip(String sTooltip) {
		aImage.tooltip(sTooltip);

		return this;
	}

	/**
	 * Sets the width of the thumbnail image in pixels.
	 *
	 * @param nPixelWidth The width in pixels
	 * @return This instance
	 */
	public UiThumbnail width(int nPixelWidth) {
		cell().width(nPixelWidth, SizeUnit.PIXEL);

		return this;
	}

	/**
	 * Displays the full-size image if the thumbnail image is clicked.
	 */
	private void displayImageView() {
		UiPopupView aImageView =
			new UiPopupView(getView(), new UiColumnGridLayout(), true);

		aImageView.builder().addTitle(aImage.get(LABEL));
		aImageView
			.builder()
			.addIconButton(UiStandardIcon.CLOSE)
			.onClick(v -> aImageView.hide())
			.cell()
			.colSpan(1)
			.alignHorizontal(Alignment.END);
		aImageView.getLayout().nextRow();
		aImageView
			.builder()
			.addImage(rFullImageDef)
			.tooltip(aImage.get(TOOLTIP));

		aImageView.center().autoHide().show();
	}
}
