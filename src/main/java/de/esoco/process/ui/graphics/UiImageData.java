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


/********************************************************************
 * An image that is defined from the actual image data. The main constructor
 * {@link #UiImageData(MimeType, Supplier)} accepts a supplier function for the
 * image data that is only evaluated just before application to a component.
 * That allows it to be used for lazy image initialization.
 *
 * @author eso
 */
public class UiImageData extends UiImageDefinition<UiImageData>
{
	//~ Instance fields --------------------------------------------------------

	private MimeType		 eMimeType;
	private Supplier<String> fImageData;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance from binary image data. The data will be Base64
	 * encoded.
	 *
	 * @param eMimeType  The MIME type of the image
	 * @param rImageData The binary image data
	 */
	public UiImageData(MimeType eMimeType, byte[] rImageData)
	{
		this(eMimeType, Base64.getEncoder().encodeToString(rImageData));
	}

	/***************************************
	 * Creates a new instance that receives the image data from a function. The
	 * function will be evaluated only just before the image needs to be
	 * displayed. That allows this constructor to be used for lazy
	 * initialization of images.
	 *
	 * @param eMimeType  The MIME type of the image
	 * @param fImageData The function that provides the image data
	 */
	public UiImageData(MimeType eMimeType, Supplier<String> fImageData)
	{
		this.eMimeType  = eMimeType;
		this.fImageData = fImageData;
	}

	/***************************************
	 * Creates a new instance from an image data string. The image data can
	 * either be Base64 encoded data for binary images or an SVG image
	 * description. In the latter case the MIME type must be {@link
	 * MimeType#IMAGE_SVG}.
	 *
	 * @param eMimeType  The MIME type of the image
	 * @param sImageData The image data string
	 */
	public UiImageData(MimeType eMimeType, String sImageData)
	{
		this(eMimeType, () -> sImageData);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method to return a new instance for a Base64 encoded GIF image.
	 *
	 * @param  sBase64Data The Base64 encoded GIF image data
	 *
	 * @return A new image data instance
	 */
	public static UiImageData gif(String sBase64Data)
	{
		return new UiImageData(MimeType.IMAGE_GIF, sBase64Data);
	}

	/***************************************
	 * Factory method to return a new instance for a Base64 encoded JPEG image.
	 *
	 * @param  sBase64Data The Base64 encoded JPEG image data
	 *
	 * @return A new image data instance
	 */
	public static UiImageData jpg(String sBase64Data)
	{
		return new UiImageData(MimeType.IMAGE_JPEG, sBase64Data);
	}

	/***************************************
	 * Factory method to return a new instance for a Base64 encoded PNG image.
	 *
	 * @param  sBase64Data The Base64 encoded PNG image data
	 *
	 * @return A new image data instance
	 */
	public static UiImageData png(String sBase64Data)
	{
		return new UiImageData(MimeType.IMAGE_PNG, sBase64Data);
	}

	/***************************************
	 * Factory method to return a new instance for an SVG image.
	 *
	 * @param  sSvgImage The SVG image string
	 *
	 * @return A new image data instance
	 */
	public static UiImageData svg(String sSvgImage)
	{
		return new UiImageData(MimeType.IMAGE_SVG, sSvgImage);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see UiImageDefinition#applyPropertiesTo(UiComponent)
	 */
	@Override
	protected void applyPropertiesTo(UiComponent<?, ?> rComponent)
	{
		StringBuilder aDataUri   = new StringBuilder("d:data:");
		String		  sImageData = fImageData.get();

		aDataUri.append(eMimeType.getDefinition());

		if (eMimeType == MimeType.IMAGE_SVG)
		{
			sImageData = encodeSvg(sImageData);
		}
		else
		{
			aDataUri.append(";base64");
		}

		aDataUri.append(',').append(sImageData);

		set(IMAGE, aDataUri.toString());
		super.applyPropertiesTo(rComponent);
	}

	/***************************************
	 * Returns the MIME type of this image.
	 *
	 * @return The MIME type
	 */
	protected final MimeType getMimeType()
	{
		return eMimeType;
	}

	/***************************************
	 * Encodes an SVG image data string for use in a data URI.
	 *
	 * @param  sData The SVG image data to encode
	 *
	 * @return The encoded string
	 */
	private String encodeSvg(String sData)
	{
		try
		{
			sData = URLEncoder.encode(sData, StandardCharsets.UTF_8.toString());
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalStateException(e);
		}

		// optimize encoding for minimal size
		// see https://codepen.io/tigt/post/optimizing-svgs-in-data-uris
		// removes newlines, then puts spaces, equal signs, colons, and
		// slashes back in; finally replaces quotes with apostrophes (may
		// break certain SVGs)
		sData =
			sData.replaceAll("\n", "")
				 .replaceAll("\\+", " ")
				 .replaceAll("%3D", "=")
				 .replaceAll("%3A", ":")
				 .replaceAll("%2F", "/")
				 .replaceAll("%22", "'");

		return sData;
	}
}
