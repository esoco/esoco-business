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

	private final UiImage image;

	private UiImageDefinition<?> fullImageDef;

	/**
	 * Creates a new instance that displays a down-scaled image as the
	 * thumbnail
	 * with a default width of 300 pixels.
	 *
	 * @param parent The parent container
	 * @param image  The image to display
	 */
	public UiThumbnail(UiContainer<?> parent, UiImageDefinition<?> image) {
		this(parent, image, image);
	}

	/**
	 * Creates a new instance that displays a thumbnail with a default width of
	 * 300 pixels.
	 *
	 * @param parent     The parent container
	 * @param thumbImage The thumbnail image to display
	 * @param fullImage  The full image to display if the the thumbnail is
	 *                   selected
	 */
	public UiThumbnail(UiContainer<?> parent, UiImageDefinition<?> thumbImage,
		UiImageDefinition<?> fullImage) {
		super(parent, new UiInlineLayout());

		this.fullImageDef = fullImage;

		image = builder().addImage(thumbImage).onClick(this::displayImageView);
		width(300);
	}

	/**
	 * Sets the caption of the thumbnail image.
	 *
	 * @param caption The image caption
	 * @return This instance
	 */
	public UiThumbnail caption(String caption) {
		image.setCaption(caption);

		return this;
	}

	/**
	 * Sets the image to be displayed as a thumbnail and on selection. To set
	 * different images
	 * {@link #setImages(UiImageDefinition, UiImageDefinition)}
	 * can be used.
	 *
	 * @param imageDef The component's image.
	 */
	public void setImage(UiImageDefinition<?> imageDef) {
		image.setImage(imageDef);
		fullImageDef = imageDef;
	}

	/**
	 * Sets the images for this thumbnail.
	 *
	 * @param thumbImage The thumbnail image to display
	 * @param fullImage  The full image to display if the the thumbnail is
	 *                   selected
	 */
	public void setImages(UiImageDefinition<?> thumbImage,
		UiImageDefinition<?> fullImage) {
		image.setImage(thumbImage);
		fullImageDef = fullImage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UiThumbnail tooltip(String tooltip) {
		image.tooltip(tooltip);

		return this;
	}

	/**
	 * Sets the width of the thumbnail image in pixels.
	 *
	 * @param pixelWidth The width in pixels
	 * @return This instance
	 */
	public UiThumbnail width(int pixelWidth) {
		cell().width(pixelWidth, SizeUnit.PIXEL);

		return this;
	}

	/**
	 * Displays the full-size image if the thumbnail image is clicked.
	 */
	private void displayImageView() {
		UiPopupView imageView =
			new UiPopupView(getView(), new UiColumnGridLayout(), true);

		imageView.builder().addTitle(image.get(LABEL));
		imageView
			.builder()
			.addIconButton(UiStandardIcon.CLOSE)
			.onClick(v -> imageView.hide())
			.cell()
			.colSpan(1)
			.alignHorizontal(Alignment.END);
		imageView.getLayout().nextRow();
		imageView.builder().addImage(fullImageDef).tooltip(image.get(TOOLTIP));

		imageView.center().autoHide().show();
	}
}
