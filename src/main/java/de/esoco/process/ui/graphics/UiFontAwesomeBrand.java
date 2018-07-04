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
package de.esoco.process.ui.graphics;

/********************************************************************
 * Enumeration of Font Awesome (5.1.0) brand icons.
 *
 * @see <a href="http://fontawesome.io">Font Awesome</a>
 */
public enum UiFontAwesomeBrand implements UiIconSupplier
{
	_500PX('\uF26E'), ACCESSIBLE_ICON('\uF368'), ACCUSOFT('\uF369'),
	ADN('\uF170'), ADVERSAL('\uF36A'), AFFILIATETHEME('\uF36B'),
	ALGOLIA('\uF36C'), AMAZON('\uF270'), AMAZON_PAY('\uF42C'), AMILIA('\uF36D'),
	ANDROID('\uF17B'), ANGELLIST('\uF209'), ANGRYCREATIVE('\uF36E'),
	ANGULAR('\uF420'), APP_STORE('\uF36F'), APP_STORE_IOS('\uF370'),
	APPER('\uF371'), APPLE('\uF179'), APPLE_PAY('\uF415'), ASYMMETRIK('\uF372'),
	AUDIBLE('\uF373'), AUTOPREFIXER('\uF41C'), AVIANEX('\uF374'),
	AVIATO('\uF421'), AWS('\uF375'), BANDCAMP('\uF2D5'), BEHANCE('\uF1B4'),
	BEHANCE_SQUARE('\uF1B5'), BIMOBJECT('\uF378'), BITBUCKET('\uF171'),
	BITCOIN('\uF379'), BITY('\uF37A'), BLACK_TIE('\uF27E'),
	BLACKBERRY('\uF37B'), BLOGGER('\uF37C'), BLOGGER_B('\uF37D'),
	BLUETOOTH('\uF293'), BLUETOOTH_B('\uF294'), BTC('\uF15A'),
	BUROMOBELEXPERTE('\uF37F'), BUYSELLADS('\uF20D'), CC_AMAZON_PAY('\uF42D'),
	CC_AMEX('\uF1F3'), CC_APPLE_PAY('\uF416'), CC_DINERS_CLUB('\uF24C'),
	CC_DISCOVER('\uF1F2'), CC_JCB('\uF24B'), CC_MASTERCARD('\uF1F1'),
	CC_PAYPAL('\uF1F4'), CC_STRIPE('\uF1F5'), CC_VISA('\uF1F0'),
	CENTERCODE('\uF380'), CHROME('\uF268'), CLOUDSCALE('\uF383'),
	CLOUDSMITH('\uF384'), CLOUDVERSIFY('\uF385'), CODEPEN('\uF1CB'),
	CODIEPIE('\uF284'), CONNECTDEVELOP('\uF20E'), CONTAO('\uF26D'),
	CPANEL('\uF388'), CREATIVE_COMMONS('\uF25E'), CREATIVE_COMMONS_BY('\uF4E7'),
	CREATIVE_COMMONS_NC('\uF4E8'), CREATIVE_COMMONS_NC_EU('\uF4E9'),
	CREATIVE_COMMONS_NC_JP('\uF4EA'), CREATIVE_COMMONS_ND('\uF4EB'),
	CREATIVE_COMMONS_PD('\uF4EC'), CREATIVE_COMMONS_PD_ALT('\uF4ED'),
	CREATIVE_COMMONS_REMIX('\uF4EE'), CREATIVE_COMMONS_SA('\uF4EF'),
	CREATIVE_COMMONS_SAMPLING('\uF4F0'),
	CREATIVE_COMMONS_SAMPLING_PLUS('\uF4F1'), CREATIVE_COMMONS_SHARE('\uF4F2'),
	CREATIVE_COMMONS_ZERO('\uF4F3'), CSS3('\uF13C'), CSS3_ALT('\uF38B'),
	CUTTLEFISH('\uF38C'), D_AND_D('\uF38D'), DASHCUBE('\uF210'),
	DELICIOUS('\uF1A5'), DEPLOYDOG('\uF38E'), DESKPRO('\uF38F'),
	DEVIANTART('\uF1BD'), DIGG('\uF1A6'), DIGITAL_OCEAN('\uF391'),
	DISCORD('\uF392'), DISCOURSE('\uF393'), DOCHUB('\uF394'), DOCKER('\uF395'),
	DRAFT2DIGITAL('\uF396'), DRIBBBLE('\uF17D'), DRIBBBLE_SQUARE('\uF397'),
	DROPBOX('\uF16B'), DRUPAL('\uF1A9'), DYALOG('\uF399'), EARLYBIRDS('\uF39A'),
	EBAY('\uF4F4'), EDGE('\uF282'), ELEMENTOR('\uF430'), EMBER('\uF423'),
	EMPIRE('\uF1D1'), ENVIRA('\uF299'), ERLANG('\uF39D'), ETHEREUM('\uF42E'),
	ETSY('\uF2D7'), EXPEDITEDSSL('\uF23E'), FACEBOOK('\uF09A'),
	FACEBOOK_F('\uF39E'), FACEBOOK_MESSENGER('\uF39F'),
	FACEBOOK_SQUARE('\uF082'), FIREFOX('\uF269'), FIRST_ORDER('\uF2B0'),
	FIRST_ORDER_ALT('\uF50A'), FIRSTDRAFT('\uF3A1'), FLICKR('\uF16E'),
	FLIPBOARD('\uF44D'), FLY('\uF417'), FONT_AWESOME('\uF2B4'),
	FONT_AWESOME_ALT('\uF35C'), FONT_AWESOME_FLAG('\uF425'),
	FONT_AWESOME_LOGO_FULL('\uF4E6'), FONTICONS('\uF280'),
	FONTICONS_FI('\uF3A2'), FORT_AWESOME('\uF286'), FORT_AWESOME_ALT('\uF3A3'),
	FORUMBEE('\uF211'), FOURSQUARE('\uF180'), FREE_CODE_CAMP('\uF2C5'),
	FREEBSD('\uF3A4'), FULCRUM('\uF50B'), GALACTIC_REPUBLIC('\uF50C'),
	GALACTIC_SENATE('\uF50D'), GET_POCKET('\uF265'), GG('\uF260'),
	GG_CIRCLE('\uF261'), GIT('\uF1D3'), GIT_SQUARE('\uF1D2'), GITHUB('\uF09B'),
	GITHUB_ALT('\uF113'), GITHUB_SQUARE('\uF092'), GITKRAKEN('\uF3A6'),
	GITLAB('\uF296'), GITTER('\uF426'), GLIDE('\uF2A5'), GLIDE_G('\uF2A6'),
	GOFORE('\uF3A7'), GOODREADS('\uF3A8'), GOODREADS_G('\uF3A9'),
	GOOGLE('\uF1A0'), GOOGLE_DRIVE('\uF3AA'), GOOGLE_PLAY('\uF3AB'),
	GOOGLE_PLUS('\uF2B3'), GOOGLE_PLUS_G('\uF0D5'),
	GOOGLE_PLUS_SQUARE('\uF0D4'), GOOGLE_WALLET('\uF1EE'), GRATIPAY('\uF184'),
	GRAV('\uF2D6'), GRIPFIRE('\uF3AC'), GRUNT('\uF3AD'), GULP('\uF3AE'),
	HACKER_NEWS('\uF1D4'), HACKER_NEWS_SQUARE('\uF3AF'), HIPS('\uF452'),
	HIRE_A_HELPER('\uF3B0'), HOOLI('\uF427'), HORNBILL('\uF592'),
	HOTJAR('\uF3B1'), HOUZZ('\uF27C'), HTML5('\uF13B'), HUBSPOT('\uF3B2'),
	IMDB('\uF2D8'), INSTAGRAM('\uF16D'), INTERNET_EXPLORER('\uF26B'),
	IOXHOST('\uF208'), ITUNES('\uF3B4'), ITUNES_NOTE('\uF3B5'), JAVA('\uF4E4'),
	JEDI_ORDER('\uF50E'), JENKINS('\uF3B6'), JOGET('\uF3B7'), JOOMLA('\uF1AA'),
	JS('\uF3B8'), JS_SQUARE('\uF3B9'), JSFIDDLE('\uF1CC'), KEYBASE('\uF4F5'),
	KEYCDN('\uF3BA'), KICKSTARTER('\uF3BB'), KICKSTARTER_K('\uF3BC'),
	KORVUE('\uF42F'), LARAVEL('\uF3BD'), LASTFM('\uF202'),
	LASTFM_SQUARE('\uF203'), LEANPUB('\uF212'), LESS('\uF41D'), LINE('\uF3C0'),
	LINKEDIN('\uF08C'), LINKEDIN_IN('\uF0E1'), LINODE('\uF2B8'),
	LINUX('\uF17C'), LYFT('\uF3C3'), MAGENTO('\uF3C4'), MAILCHIMP('\uF59E'),
	MANDALORIAN('\uF50F'), MASTODON('\uF4F6'), MAXCDN('\uF136'),
	MEDAPPS('\uF3C6'), MEDIUM('\uF23A'), MEDIUM_M('\uF3C7'), MEDRT('\uF3C8'),
	MEETUP('\uF2E0'), MEGAPORT('\uF5A3'), MICROSOFT('\uF3CA'), MIX('\uF3CB'),
	MIXCLOUD('\uF289'), MIZUNI('\uF3CC'), MODX('\uF285'), MONERO('\uF3D0'),
	NAPSTER('\uF3D2'), NIMBLR('\uF5A8'), NINTENDO_SWITCH('\uF418'),
	NODE('\uF419'), NODE_JS('\uF3D3'), NPM('\uF3D4'), NS8('\uF3D5'),
	NUTRITIONIX('\uF3D6'), ODNOKLASSNIKI('\uF263'),
	ODNOKLASSNIKI_SQUARE('\uF264'), OLD_REPUBLIC('\uF510'), OPENCART('\uF23D'),
	OPENID('\uF19B'), OPERA('\uF26A'), OPTIN_MONSTER('\uF23C'), OSI('\uF41A'),
	PAGE4('\uF3D7'), PAGELINES('\uF18C'), PALFED('\uF3D8'), PATREON('\uF3D9'),
	PAYPAL('\uF1ED'), PERISCOPE('\uF3DA'), PHABRICATOR('\uF3DB'),
	PHOENIX_FRAMEWORK('\uF3DC'), PHOENIX_SQUADRON('\uF511'), PHP('\uF457'),
	PIED_PIPER('\uF2AE'), PIED_PIPER_ALT('\uF1A8'), PIED_PIPER_HAT('\uF4E5'),
	PIED_PIPER_PP('\uF1A7'), PINTEREST('\uF0D2'), PINTEREST_P('\uF231'),
	PINTEREST_SQUARE('\uF0D3'), PLAYSTATION('\uF3DF'), PRODUCT_HUNT('\uF288'),
	PUSHED('\uF3E1'), PYTHON('\uF3E2'), QQ('\uF1D6'), QUINSCAPE('\uF459'),
	QUORA('\uF2C4'), R_PROJECT('\uF4F7'), RAVELRY('\uF2D9'), REACT('\uF41B'),
	README('\uF4D5'), REBEL('\uF1D0'), RED_RIVER('\uF3E3'), REDDIT('\uF1A1'),
	REDDIT_ALIEN('\uF281'), REDDIT_SQUARE('\uF1A2'), RENDACT('\uF3E4'),
	RENREN('\uF18B'), REPLYD('\uF3E6'), RESEARCHGATE('\uF4F8'),
	RESOLVING('\uF3E7'), REV('\uF5B2'), ROCKETCHAT('\uF3E8'), ROCKRMS('\uF3E9'),
	SAFARI('\uF267'), SASS('\uF41E'), SCHLIX('\uF3EA'), SCRIBD('\uF28A'),
	SEARCHENGIN('\uF3EB'), SELLCAST('\uF2DA'), SELLSY('\uF213'),
	SERVICESTACK('\uF3EC'), SHIRTSINBULK('\uF214'), SHOPWARE('\uF5B5'),
	SIMPLYBUILT('\uF215'), SISTRIX('\uF3EE'), SITH('\uF512'),
	SKYATLAS('\uF216'), SKYPE('\uF17E'), SLACK('\uF198'), SLACK_HASH('\uF3EF'),
	SLIDESHARE('\uF1E7'), SNAPCHAT('\uF2AB'), SNAPCHAT_GHOST('\uF2AC'),
	SNAPCHAT_SQUARE('\uF2AD'), SOUNDCLOUD('\uF1BE'), SPEAKAP('\uF3F3'),
	SPOTIFY('\uF1BC'), SQUARESPACE('\uF5BE'), STACK_EXCHANGE('\uF18D'),
	STACK_OVERFLOW('\uF16C'), STAYLINKED('\uF3F5'), STEAM('\uF1B6'),
	STEAM_SQUARE('\uF1B7'), STEAM_SYMBOL('\uF3F6'), STICKER_MULE('\uF3F7'),
	STRAVA('\uF428'), STRIPE('\uF429'), STRIPE_S('\uF42A'),
	STUDIOVINARI('\uF3F8'), STUMBLEUPON('\uF1A4'), STUMBLEUPON_CIRCLE('\uF1A3'),
	SUPERPOWERS('\uF2DD'), SUPPLE('\uF3F9'), TEAMSPEAK('\uF4F9'),
	TELEGRAM('\uF2C6'), TELEGRAM_PLANE('\uF3FE'), TENCENT_WEIBO('\uF1D5'),
	THEMECO('\uF5C6'), THEMEISLE('\uF2B2'), TRADE_FEDERATION('\uF513'),
	TRELLO('\uF181'), TRIPADVISOR('\uF262'), TUMBLR('\uF173'),
	TUMBLR_SQUARE('\uF174'), TWITCH('\uF1E8'), TWITTER('\uF099'),
	TWITTER_SQUARE('\uF081'), TYPO3('\uF42B'), UBER('\uF402'), UIKIT('\uF403'),
	UNIREGISTRY('\uF404'), UNTAPPD('\uF405'), USB('\uF287'), USSUNNAH('\uF407'),
	VAADIN('\uF408'), VIACOIN('\uF237'), VIADEO('\uF2A9'),
	VIADEO_SQUARE('\uF2AA'), VIBER('\uF409'), VIMEO('\uF40A'),
	VIMEO_SQUARE('\uF194'), VIMEO_V('\uF27D'), VINE('\uF1CA'), VK('\uF189'),
	VNV('\uF40B'), VUEJS('\uF41F'), WEEBLY('\uF5CC'), WEIBO('\uF18A'),
	WEIXIN('\uF1D7'), WHATSAPP('\uF232'), WHATSAPP_SQUARE('\uF40C'),
	WHMCS('\uF40D'), WIKIPEDIA_W('\uF266'), WINDOWS('\uF17A'), WIX('\uF5CF'),
	WOLF_PACK_BATTALION('\uF514'), WORDPRESS('\uF19A'),
	WORDPRESS_SIMPLE('\uF411'), WPBEGINNER('\uF297'), WPEXPLORER('\uF2DE'),
	WPFORMS('\uF298'), XBOX('\uF412'), XING('\uF168'), XING_SQUARE('\uF169'),
	Y_COMBINATOR('\uF23B'), YAHOO('\uF19E'), YANDEX('\uF413'),
	YANDEX_INTERNATIONAL('\uF414'), YELP('\uF1E9'), YOAST('\uF2B1'),
	YOUTUBE('\uF167'), YOUTUBE_SQUARE('\uF431');

	//~ Instance fields --------------------------------------------------------

	private final char cFontChar;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param cFontChar The character code in the font awesome icon font.
	 */
	private UiFontAwesomeBrand(char cFontChar)
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
	 * Returns a new {@link UiIconDefinition} initialized from this enumeration
	 * constant.
	 *
	 * @return The new icon
	 */
	@Override
	public UiIconDefinition getIcon()
	{
		return new UiIconDefinition(this);
	}
}
