package com.siti.mobile.Utils

// -- *** SERVER *** -- //
//LOCAL
const val SERVER_LOCAL_NAME = "MAC ID"
const val SERVER_LOCAL_IP_LOGIN = "https://10.22.254.26/apis/"
const val SERVER_LOCAL_IP_EMPTY = "10.22.254.26"
const val SERVER_LOCAL_IP_SOCKET = "https://10.22.254.26"
const val SERVER_LOCAL_IP_ADMIN = "https://10.22.254.26:3001/admin/"
//IPTV
const val SERVER_IPTV_NAME = "MAC ID\u200E "
const val SERVER_GLOBAL_IP_LOGIN = "https://103.187.78.90/apis/"
const val SERVER_GLOBAL_IP_EMPTY = "103.187.78.90"
const val SERVER_GLOBAL_IP_ADMIN = "http://103.187.78.90/admin/"
//const val SERVER_GLOBAL_IP_ADMIN = "https://115.187.38.242/uploads/update/app_panmetro_v34.apk"
//const val SERVER_GLOBAL_IP_LOGIN = "https://65.21.153.139/apis/"
const val SERVER_GLOBAL_IP_SOCKET = "https://103.187.78.90"
//https://103.50.148.2/uploads/AppTV/A1000007.jpg

const val LOCAL_IP_STREAMS = "10.200.200.2"

// UPDATE
const val APP_NAME_COLLECTION = "SITI"
const val APP_UPDATE_DOCUMENT = "app_updates"

const val KEY_PREF_TOKEN_STREAM = "keyPrefTokenStream"

// -- KEY PREFERENCES ---

const val KEY_HEADERS_HIDDEN_TITLE = "HEADERS_HIDDEN_TITLE"
const val KEY_HEADERS_AUTO_HIDE = "HEADERS_AUTO_HIDE"

const val sharedPrefFile = "rootPreference"
const val PREFERENCES_STRING_DEFAULT_VALUE = "null"
const val KEY_LAST_PLAYED_URL = "LAST_PLAYED_URL"
const val KEY_LAST_PLAYED_URL_DRM = "LAST_PLAYED_URL_DRM"
const val KEY_SELECTED_CATEGORY_ID = "SELECTED_CATEGORY_ID"
const val KEY_SELECTED_CATEGORY_INDEX = "SELECTED_CATEGORY_INDEX"
const val KEY_SELECTED_CATEGORY_NAME = "SELECTED_CATEGORY_NAME"
const val KEY_SELECTED_CHANNEL_ID = "SELECTED_CHANNEL_ID"
const val KEY_SELECTED_CHANNEL_INDEX = "SELECTED_CHANNEL_INDEX"
const val KEY_HEADER_VALIDITY = "HEADER_VALIDITY"

const val KEY_AUDIT_MODE = "keyAuditMode"
const val KEY_LOW_PROFILE = "keyLowProfileMode"

const val KEY_SELECTED_VOD_CATEGORY_INDEX = "SELECTED_VOD_CATEGORY_INDEX"
const val KEY_SELECTED_LAST_VOD = "LAST_VOD_SELECTED"

const val KEY_SELECTED_SOD_CATEGORY_INDEX = "SELECTED_SOD_CATEGORY_INDEX"
const val KEY_SELECTED_LAST_SOD = "LAST_SOD_SELECTED"
const val KEY_LAST_ROW_SELECTED_SOD = "LAST_SOD_SELECTED"

const val KEY_SELECTED_MOD_CATEGORY_INDEX = "SELECTED_MOD_CATEGORY_INDEX"
const val KEY_SELECTED_LAST_MOD = "LAST_MOD_SELECTED"
const val KEY_LAST_ROW_SELECTED_MOD = "LAST_MOD_SELECTED"

const val KEY_PLAY_WITH_DRM_SOURCE = "PLAY_WITH_DRM_SOURCE"

const val KEY_CHANNEL_NO = "CHANNEL_NO"
const val KEY_AUTHCODE = "AuthCode"
const val KEY_USERNAME = "username"
const val KEY_PASSWORD = "password"
const val KEY_BANNER = "banner"
const val KEY_MAC = "mac"
const val KEY_INTERVAL = "intervalInMins"
const val KEY_NAME = "name"
const val KEY_FIRST_LOGIN = "firstlogin"
const val KEY_ADMIN_ID = "adminID"
const val KEY_EXP_DATE = "expDate"
const val KEY_AUTH_TOKEN = "authToken"
const val KEY_AREA_CODE = "areaCode"
const val KEY_USER_ID = "userId"
const val VALUE_LOGGED_IN = "loggedin"
const val KEY_MAX_CONNECTIONS = "max_connections"
const val KEY_ACTIVE_CONNECTIONS = "active_cons"
const val KEY_CATEGORY_NAME = "Category_name"
const val KEY_BOOTUP_ACTIVITY = "bootup_activity"
const val KEY_SERVER_STATUS = "server_status"
const val COUNT_SERVER_OFF = "countServerOff"
const val VALUE_HOME_ACTIVITY = "homeActivity"
const val VALUE_FULL_SCREEN_ACTIVITY = "fullScreenActivity"
const val KEY_SUBSCRIBE_STATUS = ""
const val KEY_LIVESTREAM = "LiveStream"
const val KEY_LIVE_CATEGORY = "LiveCategory"
const val KEY_VOD_STREAM = "VODStream"
const val KEY_VOD_CATEGORY = "VODCategory"
const val KEY_SERIES_STREAM = "SeriesStream"
const val KEY_SERIES_CATEGORY = "SeriesCategory"

const val KEY_SERVER_IP = "serverIP"
const val KEY_SERVER_IP_ADMIN = "serverIPAdmin"

const val KEY_ASPECT_RATIO = "ASPECT_RATIO"

// -- ROOM --
const val DBNAME_SDSIPTV = "sdsiptvdb"

// -- CATEGORIES
const val CATEGORY_ALL_ID = "0001"
const val CATEGORY_FAVORITES_ID = "0002"

// -- KEY INTENT --

const val KEY_INTENT_URL = "url"
const val KEY_INTENT_URL_DRM = "urlDrm"
const val KEY_INTENT_CATEGORY = "category"
const val KEY_INTENT_CHANNEL = "channel"
const val KEY_INTENT_NAME = "name"

const val KEY_INTENT_SOD_ID = "sodId"
const val KEY_INTENT_MOD_ID = "modId"

// -- KEY PLAYER

const val INIT_HTTP = 'h'
const val INIT_UDP = 'u'

// - OTHERS
const val KEY_FIRST_TIME_LIVE_TV = "first_time"
const val KEY_FIRST_TIME_LIVE_TV_HIDE_NOW = "first_time_hide"
const val FALSE_STRING = "false"
const val TRUE_STRING = "true"

// PlayerManager
const val drmLicenseUrl = "https://widevine-dash.ezdrm.com/proxy?pX=0F76EA"
const val KEY_HW_SYNC = "HY_SYNC"
const val KEY_FIRST_TIME = "FIRST_TIME_IN_APP"
const val KEY_FRAME_CHANGE = "FRAME_CHANGE"
const val KEY_START_LAST_CHANNEL = "KEY_START_CHANNEL"

// Sockets
const val KEY_SOCKET = "socket_key"

// SPLASH

// LEANBACK
const val KEY_LEANBACK_ENABLED = "keyLeanbackEnabled"


// BUFFER KEYS
const val KEY_MIN_BUFFER_MS_UNICAST = "MinBufferMsUnicast"
const val KEY_MAX_BUFFER_MS_UNICAST = "MaxBufferMsUnicast"
const val KEY_BUFFER_FOR_PLAYBACK_MS_UNICAST = "BufferForPlaybackMsUnicast"
const val KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_UNICAST = "BufferForPlaybackMsUnicast"

const val KEY_MIN_BUFFER_MS_MULTICAST = "MinBufferMsMulticast"
const val KEY_MAX_BUFFER_MS_MULTICAST  = "MaxBufferMsMulticast"
const val KEY_BUFFER_FOR_PLAYBACK_MS_MULTICAST  = "BufferForPlaybackMsMulticast"
const val KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MULTICAST  = "BufferForPlaybackMsMulticast"

const val DEFAULT_MIN_BUFFER = 2500
const val DEFAULT_MAX_BUFFER = 60000
const val DEFAULT_BUFFER_PLAYBACK = 2000
const val DEFAULT_BUFFER_PLAYBACK_AFTER_REBUFFER = 2000

const val DEFAULT_MIN_BUFFER_MULTICAST = 250
const val DEFAULT_MAX_BUFFER_MULTICAST = 60000
const val DEFAULT_BUFFER_PLAYBACK_MULTICAST = 200
const val DEFAULT_BUFFER_PLAYBACK_AFTER_REBUFFER_MULTICAST = 200

const val TOTAL_CALLS = 27;
const val TOTAL_CALLS_DOUBLE = 27.0;

const val KEY_LANGUAGE = "keyLanguage";

const val VALUE_LANGUAGE_EN = "en";
const val VALUE_LANGUAGE_AR = "ar";

const val KEY_EXTRA_CHANNEL_UP_DOWN = "channelChangeUpDown"
const val KEY_EXTRA_CHANNEL_FORWARD_REWIND = "channelForwardRewind"
const val INTENT_FILTER_CHANNEL_UP_DOWN = "intent_filter_channel_up_down"
const val INTENT_FILTER_GO_LIVE = "intent_filter_channel_go_live"
const val INTENT_FILTER_CHANNEL_FORWARD_REWIND = "intent_filter_channel_forward_rewind"
const val INTENT_FILTER_CHANNEL_OK_BUTTON= "intent_filter_channel_ok_button"
const val KEY_PREFERENCES_IS_CATEGORY_VIEW_ENABLED = "keyModeChangeChannelButton";
const val DEFAULT_CATEGORY_VIEW_ENABLED = true;

const val INTENT_FILTER_SOCKET_FULL_SCREEN = "intent_filter_socket_full_screen"

const val KEY_URL_EXTRA_CATCHUP = "key_json_extra_epg"
const val KEY_ID_EXTRA_CATCHUP = "key_id_extra_epg"
const val KEY_STARTAT_EXTRA_CATCHUP = "key_startat_extra_epg"
const val KEY_ENDAT_EXTRA_CATCHUP = "key_endat_extra_epg"

const val INTENT_FILTER_RESIZE_PLAYER ="intent_filter_resize_player"
const val KEY_INTENT_FILTER_RESIZE_DELTA_X = "intent_filter_resize_deltax"
const val KEY_INTENT_FILTER_RESIZE_DELTA_Y = "intent_filter_resize_deltay"


const val KEY_FORENSIC_TEXT = "keyForensicText"
const val KEY_FORENSIC_DELAY_MS = "keyForensicDelayMs"
const val KEY_FORENSIC_SHOW_MS = "keyForensicShowMs"
const val KEY_FORENSIC_TRANSPARENCY = "keyForensicTransparency"
const val KEY_FORENSIC_SIZE_PIXEL = "keyForensicSizePixel"








