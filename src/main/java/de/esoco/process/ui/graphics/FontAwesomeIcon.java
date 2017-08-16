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

/********************************************************************
 * Enumeration of Font Awesome (4.7.0) icons.
 *
 * @see <a href="http://fontawesome.io">Font Awesome</a>
 */
public enum FontAwesomeIcon implements UiIconSupplier
{
	ADDRESS_BOOK('\uf2b9'), ADDRESS_BOOK_O('\uf2ba'), ADDRESS_CARD('\uf2bb'),
	ADDRESS_CARD_O('\uf2bc'), ADJUST('\uf042'), ADN('\uf170'),
	ALIGN_CENTER('\uf037'), ALIGN_JUSTIFY('\uf039'), ALIGN_LEFT('\uf036'),
	ALIGN_RIGHT('\uf038'), AMAZON('\uf270'), AMBULANCE('\uf0f9'),
	AMERICAN_SIGN_LANGUAGE_INTERPRETING('\uf2a3'), ANCHOR('\uf13d'),
	ANDROID('\uf17b'), ANGELLIST('\uf209'), ANGLE_DOUBLE_DOWN('\uf103'),
	ANGLE_DOUBLE_LEFT('\uf100'), ANGLE_DOUBLE_RIGHT('\uf101'),
	ANGLE_DOUBLE_UP('\uf102'), ANGLE_DOWN('\uf107'), ANGLE_LEFT('\uf104'),
	ANGLE_RIGHT('\uf105'), ANGLE_UP('\uf106'), APPLE('\uf179'),
	ARCHIVE('\uf187'), AREA_CHART('\uf1fe'), ARROW_CIRCLE_DOWN('\uf0ab'),
	ARROW_CIRCLE_LEFT('\uf0a8'), ARROW_CIRCLE_O_DOWN('\uf01a'),
	ARROW_CIRCLE_O_LEFT('\uf190'), ARROW_CIRCLE_O_RIGHT('\uf18e'),
	ARROW_CIRCLE_O_UP('\uf01b'), ARROW_CIRCLE_RIGHT('\uf0a9'),
	ARROW_CIRCLE_UP('\uf0aa'), ARROW_DOWN('\uf063'), ARROW_LEFT('\uf060'),
	ARROW_RIGHT('\uf061'), ARROW_UP('\uf062'), ARROWS('\uf047'),
	ARROWS_ALT('\uf0b2'), ARROWS_H('\uf07e'), ARROWS_V('\uf07d'),
	ASSISTIVE_LISTENING_SYSTEMS('\uf2a2'), ASTERISK('\uf069'), AT('\uf1fa'),
	AUDIO_DESCRIPTION('\uf29e'), BACKWARD('\uf04a'), BALANCE_SCALE('\uf24e'),
	BAN('\uf05e'), BANDCAMP('\uf2d5'), BAR_CHART('\uf080'), BARCODE('\uf02a'),
	BARS('\uf0c9'), BATH('\uf2cd'), BATTERY_EMPTY('\uf244'),
	BATTERY_FULL('\uf240'), BATTERY_HALF('\uf242'), BATTERY_QUARTER('\uf243'),
	BATTERY_THREE_QUARTERS('\uf241'), BED('\uf236'), BEER('\uf0fc'),
	BEHANCE('\uf1b4'), BEHANCE_SQUARE('\uf1b5'), BELL('\uf0f3'),
	BELL_O('\uf0a2'), BELL_SLASH('\uf1f6'), BELL_SLASH_O('\uf1f7'),
	BICYCLE('\uf206'), BINOCULARS('\uf1e5'), BIRTHDAY_CAKE('\uf1fd'),
	BITBUCKET('\uf171'), BITBUCKET_SQUARE('\uf172'), BLACK_TIE('\uf27e'),
	BLIND('\uf29d'), BLUETOOTH('\uf293'), BLUETOOTH_B('\uf294'), BOLD('\uf032'),
	BOLT('\uf0e7'), BOMB('\uf1e2'), BOOK('\uf02d'), BOOKMARK('\uf02e'),
	BOOKMARK_O('\uf097'), BRAILLE('\uf2a1'), BRIEFCASE('\uf0b1'), BTC('\uf15a'),
	BUG('\uf188'), BUILDING('\uf1ad'), BUILDING_O('\uf0f7'), BULLHORN('\uf0a1'),
	BULLSEYE('\uf140'), BUS('\uf207'), BUYSELLADS('\uf20d'),
	CALCULATOR('\uf1ec'), CALENDAR('\uf073'), CALENDAR_CHECK_O('\uf274'),
	CALENDAR_MINUS_O('\uf272'), CALENDAR_O('\uf133'), CALENDAR_PLUS_O('\uf271'),
	CALENDAR_TIMES_O('\uf273'), CAMERA('\uf030'), CAMERA_RETRO('\uf083'),
	CAR('\uf1b9'), CARET_DOWN('\uf0d7'), CARET_LEFT('\uf0d9'),
	CARET_RIGHT('\uf0da'), CARET_SQUARE_O_DOWN('\uf150'),
	CARET_SQUARE_O_LEFT('\uf191'), CARET_SQUARE_O_RIGHT('\uf152'),
	CARET_SQUARE_O_UP('\uf151'), CARET_UP('\uf0d8'), CART_ARROW_DOWN('\uf218'),
	CART_PLUS('\uf217'), CC('\uf20a'), CC_AMEX('\uf1f3'),
	CC_DINERS_CLUB('\uf24c'), CC_DISCOVER('\uf1f2'), CC_JCB('\uf24b'),
	CC_MASTERCARD('\uf1f1'), CC_PAYPAL('\uf1f4'), CC_STRIPE('\uf1f5'),
	CC_VISA('\uf1f0'), CERTIFICATE('\uf0a3'), CHAIN_BROKEN('\uf127'),
	CHECK('\uf00c'), CHECK_CIRCLE('\uf058'), CHECK_CIRCLE_O('\uf05d'),
	CHECK_SQUARE('\uf14a'), CHECK_SQUARE_O('\uf046'),
	CHEVRON_CIRCLE_DOWN('\uf13a'), CHEVRON_CIRCLE_LEFT('\uf137'),
	CHEVRON_CIRCLE_RIGHT('\uf138'), CHEVRON_CIRCLE_UP('\uf139'),
	CHEVRON_DOWN('\uf078'), CHEVRON_LEFT('\uf053'), CHEVRON_RIGHT('\uf054'),
	CHEVRON_UP('\uf077'), CHILD('\uf1ae'), CHROME('\uf268'), CIRCLE('\uf111'),
	CIRCLE_O('\uf10c'), CIRCLE_O_NOTCH('\uf1ce'), CIRCLE_THIN('\uf1db'),
	CLIPBOARD('\uf0ea'), CLOCK_O('\uf017'), CLONE('\uf24d'), CLOUD('\uf0c2'),
	CLOUD_DOWNLOAD('\uf0ed'), CLOUD_UPLOAD('\uf0ee'), CODE('\uf121'),
	CODE_FORK('\uf126'), CODEPEN('\uf1cb'), CODIEPIE('\uf284'),
	COFFEE('\uf0f4'), COG('\uf013'), COGS('\uf085'), COLUMNS('\uf0db'),
	COMMENT('\uf075'), COMMENT_O('\uf0e5'), COMMENTING('\uf27a'),
	COMMENTING_O('\uf27b'), COMMENTS('\uf086'), COMMENTS_O('\uf0e6'),
	COMPASS('\uf14e'), COMPRESS('\uf066'), CONNECTDEVELOP('\uf20e'),
	CONTAO('\uf26d'), COPYRIGHT('\uf1f9'), CREATIVE_COMMONS('\uf25e'),
	CREDIT_CARD('\uf09d'), CREDIT_CARD_ALT('\uf283'), CROP('\uf125'),
	CROSSHAIRS('\uf05b'), CSS3('\uf13c'), CUBE('\uf1b2'), CUBES('\uf1b3'),
	CUTLERY('\uf0f5'), DASHCUBE('\uf210'), DATABASE('\uf1c0'), DEAF('\uf2a4'),
	DELICIOUS('\uf1a5'), DESKTOP('\uf108'), DEVIANTART('\uf1bd'),
	DIAMOND('\uf219'), DIGG('\uf1a6'), DOT_CIRCLE_O('\uf192'),
	DOWNLOAD('\uf019'), DRIBBBLE('\uf17d'), DROPBOX('\uf16b'), DRUPAL('\uf1a9'),
	EDGE('\uf282'), EERCAST('\uf2da'), EJECT('\uf052'), ELLIPSIS_H('\uf141'),
	ELLIPSIS_V('\uf142'), EMPIRE('\uf1d1'), ENVELOPE('\uf0e0'),
	ENVELOPE_O('\uf003'), ENVELOPE_OPEN('\uf2b6'), ENVELOPE_OPEN_O('\uf2b7'),
	ENVELOPE_SQUARE('\uf199'), ENVIRA('\uf299'), ERASER('\uf12d'),
	ETSY('\uf2d7'), EUR('\uf153'), EXCHANGE('\uf0ec'), EXCLAMATION('\uf12a'),
	EXCLAMATION_CIRCLE('\uf06a'), EXCLAMATION_TRIANGLE('\uf071'),
	EXPAND('\uf065'), EXPEDITEDSSL('\uf23e'), EXTERNAL_LINK('\uf08e'),
	EXTERNAL_LINK_SQUARE('\uf14c'), EYE('\uf06e'), EYE_SLASH('\uf070'),
	EYEDROPPER('\uf1fb'), FACEBOOK('\uf09a'), FACEBOOK_OFFICIAL('\uf230'),
	FACEBOOK_SQUARE('\uf082'), FAST_BACKWARD('\uf049'), FAST_FORWARD('\uf050'),
	FAX('\uf1ac'), FEMALE('\uf182'), FIGHTER_JET('\uf0fb'), FILE('\uf15b'),
	FILE_ARCHIVE_O('\uf1c6'), FILE_AUDIO_O('\uf1c7'), FILE_CODE_O('\uf1c9'),
	FILE_EXCEL_O('\uf1c3'), FILE_IMAGE_O('\uf1c5'), FILE_O('\uf016'),
	FILE_PDF_O('\uf1c1'), FILE_POWERPOINT_O('\uf1c4'), FILE_TEXT('\uf15c'),
	FILE_TEXT_O('\uf0f6'), FILE_VIDEO_O('\uf1c8'), FILE_WORD_O('\uf1c2'),
	FILES_O('\uf0c5'), FILM('\uf008'), FILTER('\uf0b0'), FIRE('\uf06d'),
	FIRE_EXTINGUISHER('\uf134'), FIREFOX('\uf269'), FIRST_ORDER('\uf2b0'),
	FLAG('\uf024'), FLAG_CHECKERED('\uf11e'), FLAG_O('\uf11d'), FLASK('\uf0c3'),
	FLICKR('\uf16e'), FLOPPY_O('\uf0c7'), FOLDER('\uf07b'), FOLDER_O('\uf114'),
	FOLDER_OPEN('\uf07c'), FOLDER_OPEN_O('\uf115'), FONT('\uf031'),
	FONT_AWESOME('\uf2b4'), FONTICONS('\uf280'), FORT_AWESOME('\uf286'),
	FORUMBEE('\uf211'), FORWARD('\uf04e'), FOURSQUARE('\uf180'),
	FREE_CODE_CAMP('\uf2c5'), FROWN_O('\uf119'), FUTBOL_O('\uf1e3'),
	GAMEPAD('\uf11b'), GAVEL('\uf0e3'), GBP('\uf154'), GENDERLESS('\uf22d'),
	GET_POCKET('\uf265'), GG('\uf260'), GG_CIRCLE('\uf261'), GIFT('\uf06b'),
	GIT('\uf1d3'), GIT_SQUARE('\uf1d2'), GITHUB('\uf09b'), GITHUB_ALT('\uf113'),
	GITHUB_SQUARE('\uf092'), GITLAB('\uf296'), GLASS('\uf000'), GLIDE('\uf2a5'),
	GLIDE_G('\uf2a6'), GLOBE('\uf0ac'), GOOGLE('\uf1a0'), GOOGLE_PLUS('\uf0d5'),
	GOOGLE_PLUS_OFFICIAL('\uf2b3'), GOOGLE_PLUS_SQUARE('\uf0d4'),
	GOOGLE_WALLET('\uf1ee'), GRADUATION_CAP('\uf19d'), GRATIPAY('\uf184'),
	GRAV('\uf2d6'), H_SQUARE('\uf0fd'), HACKER_NEWS('\uf1d4'),
	HAND_LIZARD_O('\uf258'), HAND_O_DOWN('\uf0a7'), HAND_O_LEFT('\uf0a5'),
	HAND_O_RIGHT('\uf0a4'), HAND_O_UP('\uf0a6'), HAND_PAPER_O('\uf256'),
	HAND_PEACE_O('\uf25b'), HAND_POINTER_O('\uf25a'), HAND_ROCK_O('\uf255'),
	HAND_SCISSORS_O('\uf257'), HAND_SPOCK_O('\uf259'), HANDSHAKE_O('\uf2b5'),
	HASHTAG('\uf292'), HDD_O('\uf0a0'), HEADER('\uf1dc'), HEADPHONES('\uf025'),
	HEART('\uf004'), HEART_O('\uf08a'), HEARTBEAT('\uf21e'), HISTORY('\uf1da'),
	HOME('\uf015'), HOSPITAL_O('\uf0f8'), HOURGLASS('\uf254'),
	HOURGLASS_END('\uf253'), HOURGLASS_HALF('\uf252'), HOURGLASS_O('\uf250'),
	HOURGLASS_START('\uf251'), HOUZZ('\uf27c'), HTML5('\uf13b'),
	I_CURSOR('\uf246'), ID_BADGE('\uf2c1'), ID_CARD('\uf2c2'),
	ID_CARD_O('\uf2c3'), ILS('\uf20b'), IMDB('\uf2d8'), INBOX('\uf01c'),
	INDENT('\uf03c'), INDUSTRY('\uf275'), INFO('\uf129'), INFO_CIRCLE('\uf05a'),
	INR('\uf156'), INSTAGRAM('\uf16d'), INTERNET_EXPLORER('\uf26b'),
	IOXHOST('\uf208'), ITALIC('\uf033'), JOOMLA('\uf1aa'), JPY('\uf157'),
	JSFIDDLE('\uf1cc'), KEY('\uf084'), KEYBOARD_O('\uf11c'), KRW('\uf159'),
	LANGUAGE('\uf1ab'), LAPTOP('\uf109'), LASTFM('\uf202'),
	LASTFM_SQUARE('\uf203'), LEAF('\uf06c'), LEANPUB('\uf212'),
	LEMON_O('\uf094'), LEVEL_DOWN('\uf149'), LEVEL_UP('\uf148'),
	LIFE_RING('\uf1cd'), LIGHTBULB_O('\uf0eb'), LINE_CHART('\uf201'),
	LINK('\uf0c1'), LINKEDIN('\uf0e1'), LINKEDIN_SQUARE('\uf08c'),
	LINODE('\uf2b8'), LINUX('\uf17c'), LIST('\uf03a'), LIST_ALT('\uf022'),
	LIST_OL('\uf0cb'), LIST_UL('\uf0ca'), LOCATION_ARROW('\uf124'),
	LOCK('\uf023'), LONG_ARROW_DOWN('\uf175'), LONG_ARROW_LEFT('\uf177'),
	LONG_ARROW_RIGHT('\uf178'), LONG_ARROW_UP('\uf176'), LOW_VISION('\uf2a8'),
	MAGIC('\uf0d0'), MAGNET('\uf076'), MALE('\uf183'), MAP('\uf279'),
	MAP_MARKER('\uf041'), MAP_O('\uf278'), MAP_PIN('\uf276'),
	MAP_SIGNS('\uf277'), MARS('\uf222'), MARS_DOUBLE('\uf227'),
	MARS_STROKE('\uf229'), MARS_STROKE_H('\uf22b'), MARS_STROKE_V('\uf22a'),
	MAXCDN('\uf136'), MEANPATH('\uf20c'), MEDIUM('\uf23a'), MEDKIT('\uf0fa'),
	MEETUP('\uf2e0'), MEH_O('\uf11a'), MERCURY('\uf223'), MICROCHIP('\uf2db'),
	MICROPHONE('\uf130'), MICROPHONE_SLASH('\uf131'), MINUS('\uf068'),
	MINUS_CIRCLE('\uf056'), MINUS_SQUARE('\uf146'), MINUS_SQUARE_O('\uf147'),
	MIXCLOUD('\uf289'), MOBILE('\uf10b'), MODX('\uf285'), MONEY('\uf0d6'),
	MOON_O('\uf186'), MOTORCYCLE('\uf21c'), MOUSE_POINTER('\uf245'),
	MUSIC('\uf001'), NEUTER('\uf22c'), NEWSPAPER_O('\uf1ea'),
	OBJECT_GROUP('\uf247'), OBJECT_UNGROUP('\uf248'), ODNOKLASSNIKI('\uf263'),
	ODNOKLASSNIKI_SQUARE('\uf264'), OPENCART('\uf23d'), OPENID('\uf19b'),
	OPERA('\uf26a'), OPTIN_MONSTER('\uf23c'), OUTDENT('\uf03b'),
	PAGELINES('\uf18c'), PAINT_BRUSH('\uf1fc'), PAPER_PLANE('\uf1d8'),
	PAPER_PLANE_O('\uf1d9'), PAPERCLIP('\uf0c6'), PARAGRAPH('\uf1dd'),
	PAUSE('\uf04c'), PAUSE_CIRCLE('\uf28b'), PAUSE_CIRCLE_O('\uf28c'),
	PAW('\uf1b0'), PAYPAL('\uf1ed'), PENCIL('\uf040'), PENCIL_SQUARE('\uf14b'),
	PENCIL_SQUARE_O('\uf044'), PERCENT('\uf295'), PHONE('\uf095'),
	PHONE_SQUARE('\uf098'), PICTURE_O('\uf03e'), PIE_CHART('\uf200'),
	PIED_PIPER('\uf2ae'), PIED_PIPER_ALT('\uf1a8'), PIED_PIPER_PP('\uf1a7'),
	PINTEREST('\uf0d2'), PINTEREST_P('\uf231'), PINTEREST_SQUARE('\uf0d3'),
	PLANE('\uf072'), PLAY('\uf04b'), PLAY_CIRCLE('\uf144'),
	PLAY_CIRCLE_O('\uf01d'), PLUG('\uf1e6'), PLUS('\uf067'),
	PLUS_CIRCLE('\uf055'), PLUS_SQUARE('\uf0fe'), PLUS_SQUARE_O('\uf196'),
	PODCAST('\uf2ce'), POWER_OFF('\uf011'), PRINT('\uf02f'),
	PRODUCT_HUNT('\uf288'), PUZZLE_PIECE('\uf12e'), QQ('\uf1d6'),
	QRCODE('\uf029'), QUESTION('\uf128'), QUESTION_CIRCLE('\uf059'),
	QUESTION_CIRCLE_O('\uf29c'), QUORA('\uf2c4'), QUOTE_LEFT('\uf10d'),
	QUOTE_RIGHT('\uf10e'), RANDOM('\uf074'), RAVELRY('\uf2d9'), REBEL('\uf1d0'),
	RECYCLE('\uf1b8'), REDDIT('\uf1a1'), REDDIT_ALIEN('\uf281'),
	REDDIT_SQUARE('\uf1a2'), REFRESH('\uf021'), REGISTERED('\uf25d'),
	RENREN('\uf18b'), REPEAT('\uf01e'), REPLY('\uf112'), REPLY_ALL('\uf122'),
	RETWEET('\uf079'), ROAD('\uf018'), ROCKET('\uf135'), RSS('\uf09e'),
	RSS_SQUARE('\uf143'), RUB('\uf158'), SAFARI('\uf267'), SCISSORS('\uf0c4'),
	SCRIBD('\uf28a'), SEARCH('\uf002'), SEARCH_MINUS('\uf010'),
	SEARCH_PLUS('\uf00e'), SELLSY('\uf213'), SERVER('\uf233'), SHARE('\uf064'),
	SHARE_ALT('\uf1e0'), SHARE_ALT_SQUARE('\uf1e1'), SHARE_SQUARE('\uf14d'),
	SHARE_SQUARE_O('\uf045'), SHIELD('\uf132'), SHIP('\uf21a'),
	SHIRTSINBULK('\uf214'), SHOPPING_BAG('\uf290'), SHOPPING_BASKET('\uf291'),
	SHOPPING_CART('\uf07a'), SHOWER('\uf2cc'), SIGN_IN('\uf090'),
	SIGN_LANGUAGE('\uf2a7'), SIGN_OUT('\uf08b'), SIGNAL('\uf012'),
	SIMPLYBUILT('\uf215'), SITEMAP('\uf0e8'), SKYATLAS('\uf216'),
	SKYPE('\uf17e'), SLACK('\uf198'), SLIDERS('\uf1de'), SLIDESHARE('\uf1e7'),
	SMILE_O('\uf118'), SNAPCHAT('\uf2ab'), SNAPCHAT_GHOST('\uf2ac'),
	SNAPCHAT_SQUARE('\uf2ad'), SNOWFLAKE_O('\uf2dc'), SORT('\uf0dc'),
	SORT_ALPHA_ASC('\uf15d'), SORT_ALPHA_DESC('\uf15e'),
	SORT_AMOUNT_ASC('\uf160'), SORT_AMOUNT_DESC('\uf161'), SORT_ASC('\uf0de'),
	SORT_DESC('\uf0dd'), SORT_NUMERIC_ASC('\uf162'),
	SORT_NUMERIC_DESC('\uf163'), SOUNDCLOUD('\uf1be'), SPACE_SHUTTLE('\uf197'),
	SPINNER('\uf110'), SPOON('\uf1b1'), SPOTIFY('\uf1bc'), SQUARE('\uf0c8'),
	SQUARE_O('\uf096'), STACK_EXCHANGE('\uf18d'), STACK_OVERFLOW('\uf16c'),
	STAR('\uf005'), STAR_HALF('\uf089'), STAR_HALF_O('\uf123'),
	STAR_O('\uf006'), STEAM('\uf1b6'), STEAM_SQUARE('\uf1b7'),
	STEP_BACKWARD('\uf048'), STEP_FORWARD('\uf051'), STETHOSCOPE('\uf0f1'),
	STICKY_NOTE('\uf249'), STICKY_NOTE_O('\uf24a'), STOP('\uf04d'),
	STOP_CIRCLE('\uf28d'), STOP_CIRCLE_O('\uf28e'), STREET_VIEW('\uf21d'),
	STRIKETHROUGH('\uf0cc'), STUMBLEUPON('\uf1a4'),
	STUMBLEUPON_CIRCLE('\uf1a3'), SUBSCRIPT('\uf12c'), SUBWAY('\uf239'),
	SUITCASE('\uf0f2'), SUN_O('\uf185'), SUPERPOWERS('\uf2dd'),
	SUPERSCRIPT('\uf12b'), TABLE('\uf0ce'), TABLET('\uf10a'),
	TACHOMETER('\uf0e4'), TAG('\uf02b'), TAGS('\uf02c'), TASKS('\uf0ae'),
	TAXI('\uf1ba'), TELEGRAM('\uf2c6'), TELEVISION('\uf26c'),
	TENCENT_WEIBO('\uf1d5'), TERMINAL('\uf120'), TEXT_HEIGHT('\uf034'),
	TEXT_WIDTH('\uf035'), TH('\uf00a'), TH_LARGE('\uf009'), TH_LIST('\uf00b'),
	THEMEISLE('\uf2b2'), THERMOMETER_EMPTY('\uf2cb'),
	THERMOMETER_FULL('\uf2c7'), THERMOMETER_HALF('\uf2c9'),
	THERMOMETER_QUARTER('\uf2ca'), THERMOMETER_THREE_QUARTERS('\uf2c8'),
	THUMB_TACK('\uf08d'), THUMBS_DOWN('\uf165'), THUMBS_O_DOWN('\uf088'),
	THUMBS_O_UP('\uf087'), THUMBS_UP('\uf164'), TICKET('\uf145'),
	TIMES('\uf00d'), TIMES_CIRCLE('\uf057'), TIMES_CIRCLE_O('\uf05c'),
	TINT('\uf043'), TOGGLE_OFF('\uf204'), TOGGLE_ON('\uf205'),
	TRADEMARK('\uf25c'), TRAIN('\uf238'), TRANSGENDER('\uf224'),
	TRANSGENDER_ALT('\uf225'), TRASH('\uf1f8'), TRASH_O('\uf014'),
	TREE('\uf1bb'), TRELLO('\uf181'), TRIPADVISOR('\uf262'), TROPHY('\uf091'),
	TRUCK('\uf0d1'), TRY('\uf195'), TTY('\uf1e4'), TUMBLR('\uf173'),
	TUMBLR_SQUARE('\uf174'), TWITCH('\uf1e8'), TWITTER('\uf099'),
	TWITTER_SQUARE('\uf081'), UMBRELLA('\uf0e9'), UNDERLINE('\uf0cd'),
	UNDO('\uf0e2'), UNIVERSAL_ACCESS('\uf29a'), UNIVERSITY('\uf19c'),
	UNLOCK('\uf09c'), UNLOCK_ALT('\uf13e'), UPLOAD('\uf093'), USB('\uf287'),
	USD('\uf155'), USER('\uf007'), USER_CIRCLE('\uf2bd'),
	USER_CIRCLE_O('\uf2be'), USER_MD('\uf0f0'), USER_O('\uf2c0'),
	USER_PLUS('\uf234'), USER_SECRET('\uf21b'), USER_TIMES('\uf235'),
	USERS('\uf0c0'), VENUS('\uf221'), VENUS_DOUBLE('\uf226'),
	VENUS_MARS('\uf228'), VIACOIN('\uf237'), VIADEO('\uf2a9'),
	VIADEO_SQUARE('\uf2aa'), VIDEO_CAMERA('\uf03d'), VIMEO('\uf27d'),
	VIMEO_SQUARE('\uf194'), VINE('\uf1ca'), VK('\uf189'),
	VOLUME_CONTROL_PHONE('\uf2a0'), VOLUME_DOWN('\uf027'), VOLUME_OFF('\uf026'),
	VOLUME_UP('\uf028'), WEIBO('\uf18a'), WEIXIN('\uf1d7'), WHATSAPP('\uf232'),
	WHEELCHAIR('\uf193'), WHEELCHAIR_ALT('\uf29b'), WIFI('\uf1eb'),
	WIKIPEDIA_W('\uf266'), WINDOW_CLOSE('\uf2d3'), WINDOW_CLOSE_O('\uf2d4'),
	WINDOW_MAXIMIZE('\uf2d0'), WINDOW_MINIMIZE('\uf2d1'),
	WINDOW_RESTORE('\uf2d2'), WINDOWS('\uf17a'), WORDPRESS('\uf19a'),
	WPBEGINNER('\uf297'), WPEXPLORER('\uf2de'), WPFORMS('\uf298'),
	WRENCH('\uf0ad'), XING('\uf168'), XING_SQUARE('\uf169'),
	Y_COMBINATOR('\uf23b'), YAHOO('\uf19e'), YELP('\uf1e9'), YOAST('\uf2b1'),
	YOUTUBE('\uf167'), YOUTUBE_PLAY('\uf16a'), YOUTUBE_SQUARE('\uf166');

	//~ Instance fields --------------------------------------------------------

	private final char cFontChar;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param cFontChar The character code in the font awesome icon font.
	 */
	private FontAwesomeIcon(char cFontChar)
	{
		this.cFontChar = cFontChar;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the character code of this instance in the font awesome icon
	 * font.
	 *
	 * @return The character code
	 */
	public final char getFontChar()
	{
		return cFontChar;
	}

	/***************************************
	 * Returns a new {@link UiIconDefinition} initialized from this enumeration constant.
	 *
	 * @return The new icon
	 */
	@Override
	public UiIconDefinition getIcon()
	{
		return new UiIconDefinition(this);
	}
}
