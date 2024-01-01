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
package de.esoco.process.ui.graphics;

import de.esoco.data.MimeType;
import de.esoco.process.ui.UiComponent;
import de.esoco.process.ui.UiImageDefinition;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Supplier;

import static de.esoco.lib.property.ContentProperties.IMAGE;

/**
 * An image that is defined from the actual image data. The main constructor
 * {@link #UiImageData(MimeType, Supplier)} accepts a supplier function for the
 * image data that is only evaluated just before application to a component.
 * That allows it to be used for lazy image initialization.
 *
 * @author eso
 */
public class UiImageData extends UiImageDefinition<UiImageData> {

	private final MimeType mimeType;

	private final Supplier<String> imageData;

	/**
	 * Creates a new instance from binary image data. The data will be Base64
	 * encoded.
	 *
	 * @param mimeType  The MIME type of the image
	 * @param imageData The binary image data
	 */
	public UiImageData(MimeType mimeType, byte[] imageData) {
		this(mimeType, Base64.getEncoder().encodeToString(imageData));
	}

	/**
	 * Creates a new instance that receives the image data from a function. The
	 * function will be evaluated only just before the image needs to be
	 * displayed. That allows this constructor to be used for lazy
	 * initialization of images.
	 *
	 * @param mimeType  The MIME type of the image
	 * @param imageData The function that provides the image data
	 */
	public UiImageData(MimeType mimeType, Supplier<String> imageData) {
		this.mimeType = mimeType;
		this.imageData = imageData;
	}

	/**
	 * Creates a new instance from an image data string. The image data can
	 * either be Base64 encoded data for binary images or an SVG image
	 * description. In the latter case the MIME type must be
	 * {@link MimeType#IMAGE_SVG}.
	 *
	 * @param mimeType  The MIME type of the image
	 * @param imageData The image data string
	 */
	public UiImageData(MimeType mimeType, String imageData) {
		this(mimeType, () -> imageData);
	}

	/**
	 * Factory method to return a new instance for a Base64 encoded GIF image.
	 *
	 * @param base64Data The Base64 encoded GIF image data
	 * @return A new image data instance
	 */
	public static UiImageData gif(String base64Data) {
		return new UiImageData(MimeType.IMAGE_GIF, base64Data);
	}

	/**
	 * Factory method to return a new instance for a Base64 encoded JPEG image.
	 *
	 * @param base64Data The Base64 encoded JPEG image data
	 * @return A new image data instance
	 */
	public static UiImageData jpg(String base64Data) {
		return new UiImageData(MimeType.IMAGE_JPEG, base64Data);
	}

	/**
	 * Factory method to return a new instance for a Base64 encoded PNG image.
	 *
	 * @param base64Data The Base64 encoded PNG image data
	 * @return A new image data instance
	 */
	public static UiImageData png(String base64Data) {
		return new UiImageData(MimeType.IMAGE_PNG, base64Data);
	}

	/**
	 * Factory method to return a new instance for an SVG image.
	 *
	 * @param svgImage The SVG image string
	 * @return A new image data instance
	 */
	public static UiImageData svg(String svgImage) {
		return new UiImageData(MimeType.IMAGE_SVG, svgImage);
	}

	/**
	 * @see UiImageDefinition#applyPropertiesTo(UiComponent)
	 */
	@Override
	protected void applyPropertiesTo(UiComponent<?, ?> component) {
		StringBuilder dataUri = new StringBuilder("d:data:");
		String image = imageData.get();

		dataUri.append(mimeType.getDefinition());

		if (mimeType == MimeType.IMAGE_SVG) {
			image = encodeSvg(image);
		} else {
			dataUri.append(";base64");
		}

		dataUri.append(',').append(image);

		set(IMAGE, dataUri.toString());
		super.applyPropertiesTo(component);
	}

	/**
	 * Returns the MIME type of this image.
	 *
	 * @return The MIME type
	 */
	protected final MimeType getMimeType() {
		return mimeType;
	}

	/**
	 * Encodes an SVG image data string for use in a data URI.
	 *
	 * @param data The SVG image data to encode
	 * @return The encoded string
	 */
	private String encodeSvg(String data) {
		try {
			data = URLEncoder.encode(data, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}

		// optimize encoding for minimal size
		// see https://codepen.io/tigt/post/optimizing-svgs-in-data-uris
		// removes newlines, then puts spaces, equal signs, colons, and
		// slashes back in; finally replaces quotes with apostrophes (may
		// break certain SVGs)
		data = data
			.replaceAll("\n", "")
			.replaceAll("\\+", " ")
			.replaceAll("%3D", "=")
			.replaceAll("%3A", ":")
			.replaceAll("%2F", "/")
			.replaceAll("%22", "'");

		return data;
	}
}
