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
package de.esoco.data;

/********************************************************************
 * An enumeration of common MIME (Multi-purpose Internet Mail Extensions) types.
 */
public enum MimeType
{
	AUDIO_MIDI("audio/midi", ".mid .midi .kar"),
	AUDIO_MPEG("audio/mpeg", ".mp3 .mpga .mp1 .mp2"),
	AUDIO_OGG("audio/ogg", ".oga .ogg .opus .spx"),
	AUDIO_WAV("audio/x-wav", ".wav"), AUDIO_WMA("audio/x-ms-wma", ".wma"),
	BZIP2("application/x-bzip2", ".bz2"), DVI("application/x-dvi", ".dvi"),
	GZIP("application/gzip", ".gz .gzip"), IMAGE_BMP("image/bmp", ".bmp"),
	IMAGE_GIF("image/gif", ".gif"),
	IMAGE_JPEG("image/jpeg", ".jpg .jpeg .jpe .jfif"),
	IMAGE_PNG("image/png", ".png"),
	IMAGE_PSD("image/vnd.adobe.photoshop", ".psd"),
	IMAGE_SVG("image/svg+xml", ".svg .svgz"),
	IMAGE_TGA("image/x-targa", ".tga"), IMAGE_TIFF("image/tiff", ".tiff .tif"),
	JAR("application/x-java-archive", ".jar"),
	JAVA_SCRIPT("application/javascript", ".js"),
	JNLP("application/x-java-jnlp-file", ".jnlp"),
	JSON("application/json", ".json"),
	MS_EXCEL("application/vnd.ms-excel", ".xls .xlm .xla .xlc .xlt .xlw"),
	MS_POWERPOINT("application/vnd.ms-powerpoint", ".ppt .pps .pot"),
	MS_PROJECT("application/vnd.ms-project", ".mpp .mpt"),
	MS_XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			".xlsx"),
	MS_XLTX("application/vnd.openxmlformats-officedocument.spreadsheetml.template",
			".xltx"),
	MSWORD("application/msword", ".doc"), MU3("audio/x-mpegurl", ".mu3"),
	OCTET_STREAM("application/octet-stream",
				 ".bin .lha .lzh .exe .class .so .dll .img .iso"),
	OPENDOCUMENT_CHART("application/vnd.oasis.opendocument.chart", ".odc .otc"),
	OPENDOCUMENT_DATABASE("application/vnd.oasis.opendocument.database", ".odb"),
	OPENDOCUMENT_FORMULAR("application/vnd.oasis.opendocument.formula",
						  ".odf .otf"),
	OPENDOCUMENT_GRAPHIC("application/vnd.oasis.opendocument.graphics",
						 ".odg .otg"),
	OPENDOCUMENT_IMAGE("application/vnd.oasis.opendocument.image", ".odi .oti"),
	OPENDOCUMENT_PRESENT("application/vnd.oasis.opendocument.presentation",
						 ".odp .otp"),
	OPENDOCUMENT_SPREAD("application/vnd.oasis.opendocument.spreadsheet",
						".ods .ots"),
	OPENDOCUMENT_TEXT("application/vnd.oasis.opendocument.text",
					  ".odt .ott .odm .oth"),
	PDF("application/pdf", ".pdf"), PERL("application/x-perl", ".pl"),
	PGP_ENCRYPTED("application/pgp-encrypted", ".pgp"),
	PGP_KEYS("application/pgp-keys", ""),
	PGP_SIGNATURE("application/pgp-signature", ".sig"),
	PKCS10("application/pkcs10", ".p10"),
	PKCS7_MIME("application/pkcs7-mime", ".p7m .p7c"),
	PKCS7_SIGNATURE("application/pkcs7-signature", ".p7s"),
	PKCS8("application/pkcs8", ".p8"), PNG("image/png", ".png"),
	POSTSCRIPT("application/postscript", ".ps .eps .ai"),
	RPM("application/x-rpm", ".rpm"), RTF("application/rtf", ".rtf"),
	SQL("application/sql", ".sql"), TAR("application/x-tar", ".tar"),
	TEXT_CALENDAR("text/calendar", ".ics .ifb"), TEXT_CSS("text/css", ".css"),
	TEXT_CSV("text/csv", ".csv"), TEXT_DNS("text/dns", ".soa .zone"),
	TEXT_HTML("text/html", ".html .htm"), TEXT_PLAIN("text/plain", ".txt"),
	TEXT_RTF("text/rtf", ".rtf"), TEXT_SGML("text/sgml", ".sgml .sgm"),
	TEXT_XML("text/xml", ".xml .xsd .rng"), VCARD("text/vcard", ".vcf .vcard"),
	VIDEO_AVI("video/x-msvideo", ".avi"), VIDEO_FLV("video/x-flv", ".flv"),
	VIDEO_MP4("video/mp4", ".mp4 .mpeg4 .mpg4"),
	VIDEO_MPEG("video/mpeg", ".mpeg .mpg .mpe .m1v .m2v"),
	VND_DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			 ".docx"),
	VND_DOTX("application/vnd.openxmlformats-officedocument.wordprocessingml.template",
			 ".dotx"),
	WSDL("application/wsdl+xml", ".wsdl"),
	XHTML("application/xhtml+xml", ".xhtml .xhtm .xht"),
	XML_DTD("application/xml-dtd", ".dtd"),
	XSLT("application/xslt+xml", ".xsl .xslt"), ZIP("application/zip", ".zip");

	//~ Instance fields --------------------------------------------------------

	private String sDefinition;
	private String sFileExtensions;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sDefinition     The mime type definition string
	 * @param sFileExtensions The file extension of this MIME type
	 */
	private MimeType(String sDefinition, String sFileExtensions)
	{
		this.sDefinition     = sDefinition;
		this.sFileExtensions = sFileExtensions;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns the MIME type for a certain file extension.
	 *
	 * @param  sExtension The file extension to search for
	 *
	 * @return The MIME type for the given extension or NULL if no match could
	 *         be found
	 */
	public static MimeType forFileExtension(String sExtension)
	{
		sExtension = sExtension.toLowerCase();

		for (MimeType eMimeType : values())
		{
			for (String sTypeExtension : eMimeType.getFileExtensions())
			{
				if (sTypeExtension.contains(sExtension))
				{
					return eMimeType;
				}
			}
		}

		return null;
	}

	/***************************************
	 * Returns the MIME type instance for a certain MIME type definition string.
	 *
	 * @param  sMimeType The MIME type definition to search for
	 *
	 * @return The MIME type for the given definition or NULL if no match could
	 *         be found
	 */
	public static MimeType forMimeType(String sMimeType)
	{
		for (MimeType eMimeType : values())
		{
			if (eMimeType.sDefinition.equalsIgnoreCase(sMimeType))
			{
				return eMimeType;
			}
		}

		return null;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the MIME type string.
	 *
	 * @return The MIME type string
	 */
	public String getDefinition()
	{
		return sDefinition;
	}

	/***************************************
	 * Returns the file extensions associated with this MIME type.
	 *
	 * @return A string array containing the MIME type file extenstions (may be
	 *         empty but will never be NULL)
	 */
	public String[] getFileExtensions()
	{
		return sFileExtensions.split("\\w");
	}
}
