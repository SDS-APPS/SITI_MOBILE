package com.siti.mobile.Player

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.dash.DefaultDashChunkSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionEventListener
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import androidx.media3.exoplayer.drm.OfflineLicenseHelper
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TimeBar
import androidx.media3.ui.TimeBar.OnScrubListener
import com.bumptech.glide.Glide
import com.siti.mobile.R
import com.siti.mobile.Utils.CallReconnect
import com.siti.mobile.Utils.CurrentData
import com.siti.mobile.Utils.DialogFirstTimeCallback
import com.siti.mobile.Utils.KEY_FRAME_CHANGE
import com.siti.mobile.Utils.KEY_HW_SYNC
import com.siti.mobile.Utils.KEY_LEANBACK_ENABLED
import com.siti.mobile.Utils.KEY_SERVER_IP
import com.siti.mobile.Utils.KEY_START_LAST_CHANNEL
import com.siti.mobile.Utils.LOCAL_IP_STREAMS
import com.siti.mobile.Utils.NotSignal
import com.siti.mobile.Utils.SERVER_GLOBAL_IP_EMPTY
import com.siti.mobile.Utils.SERVER_LOCAL_IP_EMPTY
import com.siti.mobile.Utils.checkMemory
import com.siti.mobile.Utils.getLogoResolution
import com.siti.mobile.Utils.getStringResolution
import com.siti.mobile.Utils.network.NetworkHelper
import com.siti.mobile.Utils.sharedPrefFile
import com.siti.mobile.mvvm.config.helpers.JavaHelper
import com.siti.mobile.mvvm.fullscreen.view.PlayerScreen
import com.siti.mobile.mvvm.splash.view.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.http.BadContentTypeFormatException
import io.socket.engineio.parser.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import androidx.core.content.edit
import androidx.media3.common.DrmInitData
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.BehindLiveWindowException
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import java.net.HttpURLConnection
import java.net.URL


const val TAG = "PlayerManager"
const val TEST_URL = "https://iptv.skylink.net.in/movie/test1.mkv"


interface CallbackListen {
    fun callback()
}



@UnstableApi
class PlayerManager @Inject constructor(@ApplicationContext private val context: Context) {



    private var mPreferences: SharedPreferences

    //   var fakeUri : Uri

    init {
        mPreferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
//        lastPlayedUrl = lastPlayedUrlP ?: "";
        //       fakeUri  = Uri.parse("android.resource://"+context.packageName+"/"+ R.raw.pirobi);
    }

    fun playWithMediaPlayer(mediaPlayer: MediaPlayer, url: String) {
        Log.w(TAG, "MEdiaPlayer URL: $url")
        currentPlayer = PlayerType.MEDIAPLAYER
        mediaPlayerIsPlaying = true
        try {
            HttpsURLConnection.setDefaultHostnameVerifier { hostname, session ->
                try {
                    true
                } catch (e: Exception) {
                    false
                }
            }
            mediaPlayer.reset()
            mediaPlayer.setAudioAttributes(getAudioAttributes())

            var newUrl : String = url
            if(url.contains("iptvlocal")){
                val serverIp = mPreferences.getString(KEY_SERVER_IP, "")
                serverIp?.let {
                    if(!it.contains("iptvlocal")){
                        newUrl = url.replace("iptvlocal", "iptv")
                    }else{
                        newUrl = url
                    }
                }

            }

            val uri = Uri.parse(newUrl)

//            if(url[0] == 'h'){
//                val uri = getUriConvertedToLocal(url)
//                mediaPlayer.setDataSource(context, uri)
//                Log.w("PlayerManager", "New Uri: $uri");
//            }
//            else mediaPlayer.setDataSource(url.trim())
            mediaPlayer.setDataSource(context, uri)
            mediaPlayer.setScreenOnWhilePlaying(true)
            mediaPlayer.isLooping = true
            mediaPlayer.setWakeMode(context, PowerManager.FULL_WAKE_LOCK);
            mediaPlayer.prepareAsync()
            Log.w("PlayerManager", "PrepareAsync MediaPlayer")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "e:\$e")
        }
    }

    @androidx.media3.common.util.UnstableApi
    fun playUDPWithExoplayer(
        player: ExoPlayer,
        playerView : PlayerView,
        url: String,
        playWhenReady: Boolean
    ) {
        Log.w(TAG, "Exoplayer URL UDP: $url")
        currentPlayer = PlayerType.EXOPLAYER
        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
            .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES)
        val uri = Uri.parse(url.trim())
        val mediaSource =
            ProgressiveMediaSource.Factory(UdpDataSourceFactory().factory, extractorsFactory)
                .createMediaSource((MediaItem.fromUri(uri)));
        //  val fakeMediaItem = MediaItem.fromUri(fakeUri)
        player.playWhenReady = playWhenReady;
        player.setMediaSource(mediaSource, true);
        //     player.setMediaItem(fakeMediaItem)
        player.prepare();
        player.setWakeMode(C.WAKE_MODE_LOCAL)
        val ratio = mPreferences.getInt("ASPECT_RATIO", 3)
        setRatio(playerView, player, ratio)
    }

    @OptIn(UnstableApi::class)
    fun playUDPAESWithExoplayer(
        player: ExoPlayer,
        playerView : PlayerView,
        url: String,
        playWhenReady: Boolean,
        ip : String,
        port : String

    ) {
        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
            .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES)
        val uri = Uri.parse(url.trim())
        val mediaSource =
            ProgressiveMediaSource.Factory(UdpAESDataSourceFactory().getFactory(ip, port), extractorsFactory)
                .createMediaSource((MediaItem.fromUri(uri)));
        //  val fakeMediaItem = MediaItem.fromUri(fakeUri)
        player.playWhenReady = playWhenReady;
        player.setMediaSource(mediaSource, true);
        //     player.setMediaItem(fakeMediaItem)
        player.prepare();
        val ratio = mPreferences.getInt("ASPECT_RATIO", 3)
        setRatio(playerView, player, ratio)
    }

    fun getUriConvertedToLocal(url : String) : Uri {
//        val oldUri = Uri.parse(url.trim())
//        val newPath = oldUri.pathSegments.drop(2).joinToString("/", "/", "") // obtiene el nuevo path sin las primeras 2 segmentos
//        val newUrl = oldUri.buildUpon().apply {
//            authority(LOCAL_IP_STREAM_HTTPS) // reemplaza la autoridad (IP y puerto)
//            path(newPath) // establece el nuevo path
//        }.build().toString()
//
        val regex = Regex("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(:[0-9]+)?")

//        if(url.contains("https")){
//            regex = Regex("(https://)(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(:[0-9]+)?(/.*)?")
//        }else{
//            regex = Regex("(http://)(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(:[0-9]+)?(/.*)?")
//        }

        val nuevaUrl = regex.replace(url) {
            val puerto = it.groupValues[2] ?: "" // obtener el número de puerto, si existe
            "$LOCAL_IP_STREAMS$puerto"
        }
        return Uri.parse(nuevaUrl);
    }

    private val licenseUrl = "https://drm-widevine-licensing.axprod.net/AcquireLicense"
//    private val licenseUrl = "https://drm-widevine-licensing.axprod.net/AcquireLicense"
//    private val jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2ZXJzaW9uIjogMSwKImJlZ2luX2RhdGUiOiAiMjAwMC0wMS0wMVQxMzoxMzo1MSswMzowMCIsCiJleHBpcmF0aW9uX2RhdGUiOiAiMjAyNS0xMi0zMVQyMzo1OTo0MCswMzowMCIsCiJjb21fa2V5X2lkIjogIjkzYWVjNGNjLWU0YWMtNDRmZC1iMjhmLWIxMDIwMGM0NWM3MCIsCiJtZXNzYWdlIjogewogICJ0eXBlIjogImVudGl0bGVtZW50X21lc3NhZ2UiLAogICJ2ZXJzaW9uIjogMiwKICAibGljZW5zZSI6IHsKICAgICJkdXJhdGlvbiI6IDM2MDAKICB9LAogICJjb250ZW50X2tleXNfc291cmNlIjogewogICAgImlubGluZSI6IFsKICAgICAgewogICAgICAgICJpZCI6ICJjMGUxZTJjMS0xN2VlLWM4NWUtYWM3NS1mOWZhYzYyMWJkZmMiCiAgICAgIH0KICAgIF0KICB9Cn19.3ik-MQSxc_HPXNFY3lQYvfhoUmjJzg3B62OjRK1EUh8"
//    private val jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2ZXJzaW9uIjogMSwKImJlZ2luX2RhdGUiOiAiMjAwMC0wMS0wMVQyMzo0NzowMyswMzowMCIsCiJleHBpcmF0aW9uX2RhdGUiOiAiMjAyNS0xMi0zMVQyMzo1OTo0MCswMzowMCIsCiJjb21fa2V5X2lkIjogIjkzYWVjNGNjLWU0YWMtNDRmZC1iMjhmLWIxMDIwMGM0NWM3MCIsCiJtZXNzYWdlIjogewogICJ0eXBlIjogImVudGl0bGVtZW50X21lc3NhZ2UiLAogICJ2ZXJzaW9uIjogMiwKICAibGljZW5zZSI6IHsKICAgICJkdXJhdGlvbiI6IDM2MDAsCiAgICAiYWxsb3dfcGVyc2lzdGVuY2UiOiB0cnVlCiAgfSwKICAiY29udGVudF9rZXlzX3NvdXJjZSI6IHsKICAgICJpbmxpbmUiOiBbCiAgICAgIHsKICAgICAgICAiaWQiOiAiYzBlMWUyYzEtMTdlZS1jODVlLWFjNzUtZjlmYWM2MjFiZGZjIgogICAgICB9CiAgICBdCiAgfQp9fQ.bsd7K00DhjHfAR-RyQjksgGU8JjpaQjdcRSDrlGahxA"

    fun extractPsshFromMpd(mpdContent: String): ByteArray? {
        val regex = "<cenc:pssh>(.*?)</cenc:pssh>".toRegex()
        val match = regex.find(mpdContent)
        return match?.groups?.get(1)?.value?.let { base64Pssh ->
            android.util.Base64.decode(base64Pssh, android.util.Base64.DEFAULT)
        }
    }

    fun fetchMpdContent(mpdUrl: String): String? {
        return try {
            val url = URL(mpdUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.requestMethod = "GET"
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun downloadAndStoreLicense(
        context: Context,
        licenseUrl: String,
        mediaUrl: String,
        licenseHelper : OfflineLicenseHelper
    ): ByteArray? {


        return withContext(Dispatchers.IO) {
            try {

                val psshBase64 = "AAAAZnBzc2gAAAAA7e+LqXnWSs6jyCfc1R0h7QAAAEYSEMDh4sEX7sherHX5+sYhvfwaBkF4aW5vbSIkYzBlMWUyYzEtMTdlZS1jODVlLWFjNzUtZjlmYWM2MjFiZGZjSOPclZsG"
                val pssh = Base64.decode(psshBase64, Base64.DEFAULT)

                val schemeData = DrmInitData.SchemeData(
                    C.WIDEVINE_UUID,
                    licenseUrl,
                    MimeTypes.VIDEO_MP4,
                    pssh
                )

                val format = Format.Builder()
                    .setId("tracks-v1") // id de la Representation
                    .setSampleMimeType(MimeTypes.VIDEO_H264) // avc1.4d401f es AVC/H.264
                    .setCodecs("avc1.4d401f")
                    .setContainerMimeType(MimeTypes.VIDEO_MP4)
                    .setDrmInitData(DrmInitData(schemeData))
                    .setWidth(720)
                    .setHeight(576)
                    .build()

                val keySetId = licenseHelper.downloadLicense(format)
                saveLicenseKey(context, mediaUrl, keySetId)
                keySetId
            } catch (e: Exception) {
                Log.e("DRM", "Error downloading license: ${e.message}")
                null
            } finally {
                licenseHelper.release()
            }
        }
    }


    fun saveLicenseKey(context: Context, url: String, keySetId: ByteArray) {
        val base64 = Base64.encodeToString(keySetId, Base64.DEFAULT)
        context.getSharedPreferences("drm_licenses", Context.MODE_PRIVATE)
            .edit {
                putString(url, base64)
            }
    }

    suspend fun ensureLicenseSavedIfNeeded(mediaUrl: String, token : String) {
        val keySetId = loadLicenseKey(context, mediaUrl)

        val userAgent = Util.getUserAgent(context, "MiAppExoPlayer")
        val httpDataSourceFactory = DefaultHttpDataSource.Factory().setUserAgent(userAgent)
        val drmCallback = HttpMediaDrmCallback(licenseUrl, httpDataSourceFactory)

        drmCallback.setKeyRequestProperty("X-AxDRM-Message", token)



        val drmSessionManager =  DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID) {
                FrameworkMediaDrm.newInstance(C.WIDEVINE_UUID)
            }
            .build(drmCallback)

        val licenseHelper = OfflineLicenseHelper(
            drmSessionManager,
            DrmSessionEventListener.EventDispatcher(),
        )

        if (keySetId == null ||  licenseHelper.getLicenseDurationRemainingSec(keySetId).first < 60) {

            val newKeySetId = downloadAndStoreLicense(
                licenseHelper = licenseHelper,
                context = context,
                licenseUrl = licenseUrl,
                mediaUrl = mediaUrl
            )
            Log.d(TAG, "Nueva licencia descargada: ${newKeySetId?.size ?: 0} bytes")
        }else{
            val remaningTime = licenseHelper.getLicenseDurationRemainingSec(keySetId).first
            Log.w(TAG, "Remaning time :$remaningTime")
        }
    }

    fun loadLicenseKey(context: Context, url: String): ByteArray? {
        val base64 = context.getSharedPreferences("drm_licenses", Context.MODE_PRIVATE)
            .getString(url, null) ?: return null
        return Base64.decode(base64, Base64.DEFAULT)
    }

    @OptIn(UnstableApi::class)
    fun playHttpWithExoPlayer(
        player: ExoPlayer,
        playerView: PlayerView,
        url: String,
        playWhenReady: Boolean,
        token : String?
    ) {

        token?.let {
            if(token.isNotEmpty()){
                CoroutineScope(Dispatchers.IO).launch {
                    ensureLicenseSavedIfNeeded(url, token)
                }
            }

        }

        val loadErrorHandlingPolicy = object :
            DefaultLoadErrorHandlingPolicy() {
            override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
                if (loadErrorInfo.exception is BehindLiveWindowException) {
                    return C.TIME_UNSET
                }
                return super.getRetryDelayMsFor(loadErrorInfo)
            }
        }

        currentPlayer = PlayerType.EXOPLAYER
        JavaHelper.disableSSLCertificateVerify()
        Log.w(TAG, "URL: $url")

        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }

        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
            .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES)

        val userAgent = Util.getUserAgent(context, "MiAppExoPlayer")

        val defaultHttpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS * 2)
            .setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS * 2)
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(
                mapOf(
                    "Referer" to "http://115.187.52.252",
                    "Origin" to "http://115.187.52.252"
                )
            )

        val dataSourceFactory: DataSource.Factory = defaultHttpFactory

        var newUrl = url
        if (url.contains("catchup")) {
            val hoursInMillis = 2 * 60 * 60 * 1000
            newUrl = url.replace("catchup", "${((System.currentTimeMillis() - hoursInMillis) / 1000)}")
        }

        val uri = Uri.parse(newUrl)
//        val uri = Uri.parse("https://115.187.52.252/sdscoder1/NKTVPLUS/index.mpd")
        Log.w(TAG, "**************** NEW URI:::::::::::::: $uri")

        val drmSchemeUuid = C.WIDEVINE_UUID
        val type = Util.inferContentType(uri)
        var mediaSource: MediaSource? = null
        val mediaItemBuilder = MediaItem.Builder().setUri(uri)

        if (type == C.TYPE_DASH) {
            Log.i(TAG, "onStart: type dash")

            val chunkSourceFactory = DefaultDashChunkSource.Factory(dataSourceFactory)
            val dashFactory = DashMediaSource.Factory(chunkSourceFactory, dataSourceFactory)

            val savedKeySetId = loadLicenseKey(context, uri.toString())
            val drmBuilder = MediaItem.DrmConfiguration.Builder(drmSchemeUuid)
                .setLicenseUri(licenseUrl)
                .setMultiSession(false)

            savedKeySetId?.let {
                Log.d(TAG, "Usando licencia persistente para $uri")
                drmBuilder.setKeySetId(it)
            }

            val mediaItem = mediaItemBuilder.setDrmConfiguration(drmBuilder.build()).build()

            if(token?.isNotEmpty() == true) {
                mediaSource = dashFactory
                    .setDrmSessionManagerProvider(
                        CustomDRMSessionManagerProvider(userAgent, licenseUrl, token)
                    )
                    .createMediaSource(mediaItem)
            }else{
                mediaSource = dashFactory
                    .createMediaSource(mediaItem)
            }

        } else if (type == C.TYPE_HLS) {
            val hlsFactory = HlsMediaSource.Factory(dataSourceFactory)
                .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
            mediaSource = hlsFactory.createMediaSource(mediaItemBuilder.build())
        }

        if (mediaSource == null) {
            when (type) {
                C.TYPE_SS -> Log.i(TAG, "onStart: type ss")
                C.TYPE_HLS -> Log.i(TAG, "onStart: type hls")
                C.TYPE_OTHER -> {
                    Log.i(TAG, "onStart: type other")
                    mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
                        .createMediaSource(mediaItemBuilder.build())
                }
            }
        }

        mediaSource?.let { source ->

            player.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    var cause: Throwable? = error
                    while (cause != null) {
                        if (cause is BehindLiveWindowException) {
                            player.seekToDefaultPosition()
                            player.prepare()
                            player.play()
                            break
                        }
                        cause = cause.cause
                    }
                }
            })
            player.playWhenReady = playWhenReady
            player.setMediaSource(source, false)
            player.setWakeMode(C.WAKE_MODE_LOCAL)
            player.prepare()
        }

        val ratio = mPreferences.getInt("ASPECT_RATIO", 3)
        setRatio(playerView, player, ratio)
    }






    fun refreshRatio(playerView : PlayerView, player: ExoPlayer){
        val ratio = mPreferences.getInt("ASPECT_RATIO", 3)
        setRatio(playerView, player, ratio)
    }

    fun getPlayerByTypeCast(type: StreamType): PlayerType {
        var playerType: PlayerType? = null
        when (type) {
            StreamType.UNICAST -> {
                val playerTypeString =
                    mPreferences.getString(type.toString(), PlayerType.EXOPLAYER.toString())
                playerType = when (playerTypeString) {
                    PlayerType.EXOPLAYER.toString() -> PlayerType.EXOPLAYER
                    else -> PlayerType.MEDIAPLAYER
                }
            }
            StreamType.MULTICAST -> {
                val playerTypeString =
                    mPreferences.getString(type.toString(), PlayerType.EXOPLAYER.toString())
                playerType = when (playerTypeString) {
                    PlayerType.EXOPLAYER.toString() -> PlayerType.EXOPLAYER
                    else -> PlayerType.MEDIAPLAYER
                }
            }
        }
        return playerType
    }

    fun savePlayerByTypeCast(type: StreamType, player: PlayerType) =
        mPreferences.edit().putString(type.toString(), player.toString()).apply()

    @OptIn(UnstableApi::class)
    fun setRatio(playerView: PlayerView, player : ExoPlayer, ratio: Int) {
        Log.i(TAG, "setRatio: $ratio")
        if (ratio == 1) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        } else if (ratio == 2) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        } else if (ratio == 3) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        } else if (ratio == 4) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
        } else if (ratio == 5) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            player.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
        }
    }

//    fun listenMediaPlayerPreview(currentPlayerType: PlayerType, callback: CallbackListen) =
//        CoroutineScope(Dispatchers.IO).launch {
//            while (currentPlayerType === PlayerType.MEDIAPLAYER && LiveTvPreview.ON_LOOPER) {
//                Log.v("MediaPlayerManager", "EXECUTING -- Preview --")
//                if (LiveTvPreview.lastUpdateBuffering != null && System.currentTimeMillis() > LiveTvPreview.lastUpdateBuffering + 1300 && LiveTvPreview.ON_LOOPER && LiveTvPreview.lastUpdateBuffering != -1L) {
//                    CoroutineScope(Dispatchers.Main).launch {
//                        callback.callback()
//                    }
//                    return@launch
//                }
//                delay(1000)
//            }
//        }


    var counterMediaPlayer = 0;

    fun listenMediaPlayerFullScreen(currentPlayerType: PlayerType, callback: CallbackListen, notSignal: NotSignal) =
        CoroutineScope(Dispatchers.IO).launch {
            while (currentPlayerType === PlayerType.MEDIAPLAYER && PlayerScreen.ON_LOOPER) {
                Log.v("MediaPlayerManager", "EXECUTING -- Full Screen")
                Log.v("MediaPlayerManager", "LU: ${PlayerScreen.lastUpdateBuffering} - Future: ${System.currentTimeMillis()}")
                if (PlayerScreen.lastUpdateBuffering != 0L && System.currentTimeMillis() > PlayerScreen.lastUpdateBuffering + 1300 && PlayerScreen.ON_LOOPER && PlayerScreen.lastUpdateBuffering != -1L) {
                    CoroutineScope(Dispatchers.Main).launch {
                        callback.callback()
                    }
                    return@launch
                }
                counterMediaPlayer++
                delay(1000)
                if(counterMediaPlayer > 2){
                    counterMediaPlayer = 0;
                    notSignal.run()
                }
            }
        }

    fun removePostDelayed(
        handlerListenMediaPlayer: Handler?,
        runnableListenMediaPlayer: Runnable?
    ) {
        if (handlerListenMediaPlayer != null && runnableListenMediaPlayer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (handlerListenMediaPlayer.hasCallbacks(runnableListenMediaPlayer)) {
                    handlerListenMediaPlayer.removeCallbacks(runnableListenMediaPlayer)
                }
            } else {
                try {
                    handlerListenMediaPlayer.removeCallbacks(runnableListenMediaPlayer)
                } catch (e: java.lang.Exception) {
                    Log.w("LiveTvPreviewHandler", e.message!!)
                }
            }
        }
    }

    private fun getAudioAttributes(): AudioAttributes {
        return if (mPreferences.getBoolean(KEY_HW_SYNC, true)) {
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .setFlags(AudioAttributes.FLAG_HW_AV_SYNC)
                .build()
        } else {
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build()
        }
    }

    fun showDialogFirstTime(layoutInflater: LayoutInflater, dialogFirstTimecallback : DialogFirstTimeCallback) {
        val uri = Uri.parse(
            "android.resource://" + context.getPackageName().toString() + "/" + R.raw.test_audio
        )
        val mediaPlayer: MediaPlayer? = MediaPlayer().apply {
            setAudioAttributes(getAudioAttributes())
            setDataSource(context, uri)
            prepare()
            start()
        }
        mediaPlayer?.isLooping = true
        val alertDialogBuilder = AlertDialog.Builder(context)
        val inflater: LayoutInflater = layoutInflater
        val view: View = inflater.inflate(R.layout.first_time, null)
        alertDialogBuilder.setView(view)
        val dialog = alertDialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rbNo = view.findViewById<RadioButton>(R.id.btnNo)
        val rbYes = view.findViewById<RadioButton>(R.id.btnYes)
        val rbLeanback = view.findViewById<RadioButton>(R.id.rbLeanback)
        val rbOriginal = view.findViewById<RadioButton>(R.id.rbOriginal)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val ivLeanbackTheme = view.findViewById<ImageView>(R.id.ivLeanbackTheme)
        val ivOriginalTheme = view.findViewById<ImageView>(R.id.ivOriginal)

        Glide.with(view).load(R.drawable.leanback_theme).into(ivLeanbackTheme)
        Glide.with(view).load(R.drawable.original_theme).into(ivOriginalTheme)

        rbLeanback.setOnClickListener {
            rbOriginal.isChecked = !rbLeanback.isChecked
        }

        rbOriginal.setOnClickListener {
            rbLeanback.isChecked = !rbOriginal.isChecked
        }

        btnSave.setOnClickListener {
            mPreferences.edit().putBoolean(KEY_LEANBACK_ENABLED, rbLeanback.isChecked).apply()
            saveHWSync(rbYes.isChecked)
            stopAudio(mediaPlayer)
            dialog.hide()
            dialog.cancel()
            dialogFirstTimecallback.callback()
        }

        dialog.setOnCancelListener { stopAudio(mediaPlayer) }
        dialog.setOnDismissListener { stopAudio(mediaPlayer) }
        dialog.show()
    }

    private fun stopAudio(mediaPlayer: MediaPlayer?) {
        try{
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                    it.release()
                }
            }
        }catch (e : IllegalStateException) {
            e.printStackTrace()
        }

    }

    private fun saveHWSync(value: Boolean) {
        mPreferences.edit().putBoolean(KEY_HW_SYNC, value).apply();
    }

    fun isFrameBlackEnabled() : Boolean
    {
        return when(mPreferences.getString(KEY_FRAME_CHANGE, PlayerChangeType.LAST_FRAME.toString())){
            PlayerChangeType.BLACK_FRAME.toString() -> true
            else -> false
        }
    }

    fun saveFrameChanged(type : PlayerChangeType) = mPreferences.edit().putString(KEY_FRAME_CHANGE, type.toString()).apply()

    fun saveStartAsLastChannel(value : Boolean) = mPreferences.edit().putBoolean(KEY_START_LAST_CHANNEL, value).apply()

    fun isStartAsLastChannel() = mPreferences.getBoolean(KEY_START_LAST_CHANNEL, true)



    fun setPlayerFrozenCallback(playerFrozenFrame: PlayerFrozenFrame) {
        _playerFrozenFrame = playerFrozenFrame
    }

    companion object {
        var exoPlayer : ExoPlayer? = null
        var mediaPlayer : MediaPlayer? = null
        var currentPort = 9000
        var lastPlayedUrl: String = ""
        var isReady = false
        var currentPlayerScreen : CurrentPlayerScreen = CurrentPlayerScreen.NONE
        var currentProgressBar : ProgressBar? = null
        lateinit var currentContainerPlayer : LinearLayout
        lateinit var currentTvHoldSec : TextView
        private var _playerFrozenFrame : PlayerFrozenFrame? = null
        private var playInterface: PlayInterface? = null
        lateinit var containerChannelNotAvailable: FrameLayout
        lateinit var ivSubscriptionExpired: ImageView

    }

    var currentCathupIcon : ImageView? = null
    var currentHdIcon : ImageView? = null

    fun releaseExoPlayer(from : String){
            try {
                if(exoPlayer != null){
                    Log.w(TAG, "Releasing Exo Player from $from")
                    PlayerScreen.actualUrl = "";
                    AsyncTaskStartMulticast.g_ChannelStopped = true
//                    if(clearInterface) {
//                        playInterface = null;
//                    }
                    exoPlayer?.stop()
                    exoPlayer?.release()
                    exoPlayer = null
                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
    }

    private var helperLastPosition : Int = 0
    private var lastPosition : Int = 0

    fun updateReadyAgain(){
        exoPlayer?.let {
            if (it.duration <= 0) {
                currentContainerPlayer.visibility = View.GONE
                currentTvHoldSec.visibility = View.INVISIBLE
                currentHdIcon?.alpha = 0.5f
                currentHdIcon?.setColorFilter(Color.parseColor("#ffffff"))
                currentCathupIcon?.alpha = 0.5f
                currentCathupIcon?.setColorFilter(Color.parseColor("#ffffff"))
            } else {
                currentContainerPlayer.visibility = View.VISIBLE
                currentTvHoldSec.visibility = View.VISIBLE
                currentHdIcon?.alpha = 1f
                currentHdIcon?.setColorFilter(Color.parseColor("#FFD700"))
                currentCathupIcon?.alpha = 1f
                currentCathupIcon?.setColorFilter(Color.parseColor("#FFD700"))
            }
        }
        mediaPlayer?.let {
            if (it.duration <= 0) {
                currentContainerPlayer.visibility = View.GONE
                currentTvHoldSec.visibility = View.INVISIBLE
            } else {
                currentContainerPlayer.visibility = View.VISIBLE
                currentTvHoldSec.visibility = View.VISIBLE
            }
        }

    }

    fun createMediaPlayer(playerView: PlayerView, surfaceHolder: SurfaceHolder, ivInfoLiveCatchup : ImageView?)
    {
        currentCathupIcon = ivInfoLiveCatchup
        releaseMediaPlayer()
        mediaPlayer = MediaPlayer()
        val fullScreenLayout = LayoutInflater.from(context).inflate(R.layout.activity_video_player, null);
        currentContainerPlayer = fullScreenLayout.findViewById(R.id.containerPlayer);
        val onPreparedListenerMediaPlayer = OnPreparedListenerMediaPlayer(
            playerView = playerView,
            containerSurfaceView = fullScreenLayout.findViewById(R.id.containerSurfaceView),
            exoProgress = fullScreenLayout.findViewById(R.id.exo_progress),
            exoPosition = fullScreenLayout.findViewById(R.id.exo_position),
            exoDuration = fullScreenLayout.findViewById(R.id.exo_duration),
            containerPlayer = currentContainerPlayer
        )
        mediaPlayer!!.setOnPreparedListener(onPreparedListenerMediaPlayer)
        mediaPlayer!!.setOnErrorListener(MediaPlayer.OnErrorListener { mp, what, extra ->
            containerChannelNotAvailable.visibility = View.VISIBLE
            if(mediaPlayer == null) {
                true
            }else {
                currentProgressBar?.visibility = View.VISIBLE
                mediaPlayer?.let {
                    val run = java.lang.Runnable { playWithMediaPlayer(it, lastPlayedUrl) }
                    Handler().postDelayed(run, 6000)
                }
                false //-1004
            }
        })
        if(mediaPlayer!!.videoWidth >= 1280){
            currentHdIcon?.alpha = 1f
            currentHdIcon?.setColorFilter(Color.parseColor("#FFD700"))
        }else{
            currentHdIcon?.alpha = 0.5f
            currentHdIcon?.setColorFilter(Color.parseColor("#ffffff"))
        }
        mediaPlayer!!.setDisplay(surfaceHolder)
        val firstTime = false
        //   if(HomeActivity.isActive){
        //   if(HomeActivity.isActive){
        if (lastPlayedUrl.length > 0 && lastPlayedUrl[0] == 'h') {
            PlayerScreen.currentPlayer = getPlayerByTypeCast(StreamType.UNICAST)
            if (PlayerScreen.currentPlayer === PlayerType.MEDIAPLAYER) {
                playWithMediaPlayer(mediaPlayer!!, lastPlayedUrl)
            }
        } else {
            PlayerScreen.currentPlayer = getPlayerByTypeCast(StreamType.MULTICAST)
            if (PlayerScreen.currentPlayer === PlayerType.MEDIAPLAYER) {
                playWithMediaPlayer(mediaPlayer!!, lastPlayedUrl)
            }
        }
        mediaPlayer!!.setOnBufferingUpdateListener { mp, percent ->
//            Log.w("MediaPlayerManager", "1: ${!playerView.ifMediaplayerIsPlaying}")
            if (playerView.player?.isPlaying == false) {
                Log.v("MediaPlayerManager", "Updating")
                PlayerScreen.lastUpdateBuffering = System.currentTimeMillis()
                //     return@OnBufferingUpdateListener
            }
            helperLastPosition = mp?.currentPosition ?: 0
            Log.w(
                "MediaPlayerManager",
                "Helper last position : $helperLastPosition - Last position :$lastPosition - MD Position - ${mp?.currentPosition}"
            )
            if (helperLastPosition != lastPosition) {
                PlayerScreen.lastUpdateBuffering = System.currentTimeMillis()
                lastPosition = helperLastPosition
            }
        }
        //   }
    }

    fun setPlayInterface(_playInfercae : PlayInterface?) {
        playInterface = _playInfercae
    }


    @OptIn(UnstableApi::class)
    fun createExoPlayer(loadControl: DefaultLoadControl, context : Context, playerView: PlayerView, badSignalViews: BadSignalViews?, ivInfoLiveCatchup: ImageView?, onFrameStuck : () -> Unit, onPlayerReady : () -> Unit) {
        currentCathupIcon = ivInfoLiveCatchup
        releaseExoPlayer("[PlayerManager] -- Create Player")
        val fullScreenLayout = LayoutInflater.from(context).inflate(R.layout.activity_video_player, null);
        val ivResolution = fullScreenLayout.findViewById<TextView>(R.id.ivResolution);
        val tvResolution = fullScreenLayout.findViewById<TextView>(R.id.tvResolution);
        val containerPlayer = fullScreenLayout.findViewById<LinearLayout>(R.id.containerPlayer);
        val tvHoldOk3Sec = fullScreenLayout.findViewById<TextView>(R.id.tvOk3SecForLive);
        val renderers = CustomRenderersFactory(context)
        renderers.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        val trackSelectionFactory: ExoTrackSelection.Factory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(context, trackSelectionFactory)
        exoPlayer = ExoPlayer.Builder(context, renderers).setTrackSelector(trackSelector).build()
        currentContainerPlayer = containerPlayer
        currentTvHoldSec = tvHoldOk3Sec;
        exoPlayer!!.setTrackSelectionParameters(
            exoPlayer!!.getTrackSelectionParameters()
                .buildUpon()
                .setMaxVideoSizeSd()
                .setForceLowestBitrate(true)
                .setPreferredAudioLanguage("hu")
                .build()
        )
        playerView.player = exoPlayer
//        playerView.setShutterBackgroundColor(Color.BLACK)
        exoPlayer!!.setVideoFrameMetadataListener(VideoFrameMetadataListener { presentationTimeUs, releaseTimeNs, format, mediaFormat ->
            PlayerScreen.lastFrameTimeInMillis = System.currentTimeMillis()
            playerView.setShutterBackgroundColor(Color.TRANSPARENT)
            if (!isReady) isReady = true
//            if(containerChannelNotAvailable.isVisible) {
                containerChannelNotAvailable.visibility = View.GONE
//            }
            if(ivSubscriptionExpired.isVisible) {
                ivSubscriptionExpired.visibility = View.GONE
            }
        })

        exoPlayer!!.addListener(object : Player.Listener {
            override fun onSurfaceSizeChanged(width: Int, height: Int) {
                Log.i("SurfaceSizeChanged", "Width: $width Height: $height")
                super<Player.Listener>.onSurfaceSizeChanged(width, height)
            }

            override fun onPlayerError(error: PlaybackException) {
//                println(" --> error 1")
//                handler.removeCallbacksAndMessages(null)
//                handler.postDelayed({
//                    containerChannelNotAvailable.visibility = View.VISIBLE
//                }, 1000)
                super.onPlayerError(error)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super<Player.Listener>.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    ExoPlayer.STATE_READY -> {
                        onPlayerReady()
                        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
                        containerChannelNotAvailable.visibility = View.GONE
                        isReady = true
                        currentProgressBar?.visibility = View.GONE
                        _playerFrozenFrame?.onFrameUnfrozen()
                        checkMemory(lastPlayedUrl, object : CallReconnect {
                            override fun run(delay: Boolean) {
                        //        if (exoPlayer!!.duration <= 0) {
                                if(playInterface != null){
                                    playInterface?.onPlay(lastPlayedUrl, lastPlayedUrlDrm, token = lastToken)
                                }else{
                                    Log.w(TAG, "PLAY INTERFACE IS NULL")
                                }
                         //       }
                            }
                        }, currentPlayerScreen) {
                            onFrameStuck()
                        }
                        exoPlayer!!.playWhenReady = true
                        //      }
                        tvResolution.text = getStringResolution(
                            exoPlayer!!.videoSize.width,
                            exoPlayer!!.videoSize.height
                        )
                        ivResolution.text = getLogoResolution(
                            exoPlayer!!.videoSize.width,
                            exoPlayer!!.videoSize.height
                        )
                        if(exoPlayer!!.videoSize.width >= 1280){
                            currentHdIcon?.alpha = 1f
                            currentHdIcon?.setColorFilter(Color.parseColor("#FFD700"))
                        }else{
                            currentHdIcon?.alpha = 0.5f
                            currentHdIcon?.setColorFilter(Color.parseColor("#ffffff"))
                        }
                        if (exoPlayer!!.duration <= 0) {
                            currentCathupIcon?.alpha = 0.5f
                            currentCathupIcon?.setColorFilter(Color.parseColor("#ffffff"))
                            currentContainerPlayer.visibility = View.GONE
                            currentTvHoldSec.visibility = View.INVISIBLE
                        } else {
                            currentCathupIcon?.alpha = 1f
                            currentCathupIcon?.setColorFilter(Color.parseColor("#FFD700"))
                            currentContainerPlayer.visibility = View.VISIBLE
                            currentTvHoldSec.visibility = View.VISIBLE
                        }
                    }

                    ExoPlayer.STATE_BUFFERING ->{
                        if (badSignalViews?.containerNotSignal?.getVisibility() != View.VISIBLE) {
                            currentProgressBar?.visibility = View.VISIBLE
                        }
                    }
                    ExoPlayer.STATE_ENDED, ExoPlayer.STATE_IDLE -> {
                        val inmutableUrl = lastPlayedUrl;
                        Log.i("ExoPlayerState", "STATE_IDLE")
                        if(currentPlayerScreen != CurrentPlayerScreen.NONE) {
                            if (lastPlayedUrl != "") {
                                counterReconnectExo++
                                if (counterReconnectExo <= 2) {
                                    playInterface?.onPlay(inmutableUrl, lastPlayedUrlDrm, token = lastToken)
                                }
                                else {
                                    counterReconnectExo = 0
                                    playInterface?.onPlay(inmutableUrl, lastPlayedUrlDrm, token = lastToken)
                                }
                            }
                        }
                    }
                }

            }
        })
    }

    var mediaPlayerIsPlaying = false;
    private lateinit var runnableListenMediaPlayer : Runnable
    private lateinit var handlerListenMediaPlayer : Handler
    private var MediaPlayerJob: Job? = null
    var lastToken : String? = null

    private inner class OnPreparedListenerMediaPlayer(
        private val playerView: PlayerView,
        private val containerSurfaceView : AspectRatioFrameLayout,
        private val exoProgress : DefaultTimeBar,
        private val exoPosition : TextView,
        private val exoDuration : TextView,
        private val containerPlayer : LinearLayout) : MediaPlayer.OnPreparedListener {
        @OptIn(UnstableApi::class)
        override fun onPrepared(mediaPlayer: MediaPlayer) {
            playerView.setShutterBackgroundColor(Color.TRANSPARENT)
            containerChannelNotAvailable.visibility = View.GONE
            ivSubscriptionExpired.visibility = View.GONE
            isReady = true
            _playerFrozenFrame?.onFrameUnfrozen()
            if (!isFrameBlackEnabled()) {
//               playerView.hideSurface()
                playerView.visibility = View.INVISIBLE // similar to hideSurface()
            } else {
                containerSurfaceView.visibility = View.VISIBLE
            }
            currentProgressBar?.visibility = View.GONE
            mediaPlayer.start()
            val delay =
                1000 // Delay en milisegundos para actualizar el progreso (1 segundo en este caso)
            val handler = Handler()
            val runnableProgress: java.lang.Runnable = object : java.lang.Runnable {
                override fun run() {
                    try {
                        if (mediaPlayer.isPlaying) {
                            val currentPositionInMillis = mediaPlayer.currentPosition
                            val currentPositionInSeconds = currentPositionInMillis / 1000
                            handler.postDelayed(this, delay.toLong())
                            exoProgress.setPosition(currentPositionInMillis.toLong())
                            exoPosition.text = formatDuration(currentPositionInMillis.toLong())
                        }
                    } catch (e: java.lang.Exception) {
                        // Log.e(TAG, e.getMessage());
                    }
                }
            }
            //            tvResolution.setText(getStringResolution(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight()));
//            ivResolution.setText(getLogoResolution(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight()));
            PlayerScreen.ON_LOOPER = true
            val runnable = java.lang.Runnable { playerView.hideController() }
            Handler().postDelayed(runnable, 6000)
            runnableListenMediaPlayer = java.lang.Runnable {
                MediaPlayerJob = listenMediaPlayerFullScreen(
                    currentPlayer,
                    object : CallbackListen {
                        override fun callback() {
                            playWithMediaPlayer(mediaPlayer, lastPlayedUrl)
                        }
                    },
                    object : NotSignal {
                        override fun run() {
                              //  reconnect();
                        }
                    })
            }
            handlerListenMediaPlayer = Handler()
            handlerListenMediaPlayer.postDelayed(runnableListenMediaPlayer, 30000)
            val duration = mediaPlayer.duration
            if (duration <= 0) {
                currentCathupIcon?.setColorFilter(Color.parseColor("#ffffff"))
                currentCathupIcon?.alpha = 0.5f
                currentContainerPlayer.setVisibility(View.GONE)
            } else {
                currentCathupIcon?.setColorFilter(Color.parseColor("#FFD700"))
                currentCathupIcon?.alpha = 1f
                Log.w(TAG, "Duration: $duration")
                currentContainerPlayer.setVisibility(View.VISIBLE)
                //mediaPlayer.seekTo(duration - 3000)
                exoProgress.setDuration(duration.toLong())
                exoProgress.setPosition(duration.toLong())
                val durationInSecs = duration / 1000
                val currentPosAndDur: String = formatDuration(duration.toLong())
                exoDuration.text = currentPosAndDur
                exoPosition.text = currentPosAndDur
                exoProgress.addListener(object : OnScrubListener {
                    override fun onScrubStart(timeBar: TimeBar, position: Long) {}
                    override fun onScrubMove(timeBar: TimeBar, position: Long) {}
                    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                        mediaPlayer.seekTo(position.toInt())
                    }
                })
            }
            if (!mediaPlayerIsPlaying) {
                mediaPlayer.pause()
            }
            handler.post(runnableProgress)
        }
    }

    private fun formatDuration(durationInMillis: Long): String {
        val seconds = (durationInMillis / 1000).toInt() % 60
        val minutes = (durationInMillis / (1000 * 60) % 60).toInt()
        val hours = (durationInMillis / (1000 * 60 * 60) % 24).toInt()
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    var countDownTimerReconnect : CountDownTimer? = null
    lateinit var actualUrl : String
    private fun badSignalReconnect(badSignalViews: BadSignalViews, loadingProgressBar : ProgressBar?, replay: () -> Unit) {
        badSignalViews.containerNotSignal.setVisibility(View.VISIBLE)
        badSignalViews.tvCounter.setText("15")
        loadingProgressBar?.setVisibility(View.GONE)
        Log.w(TAG, "************CALLING bad signal reconnect************")
        if(countDownTimerReconnect != null) countDownTimerReconnect!!.cancel()
        countDownTimerReconnect = object : CountDownTimer(15000, 1000) {
            override fun onTick(l: Long) {
                badSignalViews.tvCounter.setText("" + (l / 1000).toInt() + " sec")
            }

            override fun onFinish() {
                countDownTimerReconnect = null
                badSignalViews.containerNotSignal.setVisibility(View.GONE)
                reconnect(badSignalViews, loadingProgressBar, replay)
            }
        }
        countDownTimerReconnect!!.start()
    }


    private fun reconnect(badSignalViews: BadSignalViews?, loadingProgressBar: ProgressBar?, replay : () -> Unit) {
        if(currentPlayerScreen == CurrentPlayerScreen.NONE) return;
        Log.w(TAG, "************CALLING RECONNECT************")
        replay()
//        connectToStream(PlayerFragment.actualUrl, object : ConnectToStream {
//            override fun connect() {
//                    replay()
////                if (PlayerFragment.actualUrl[0] == 'h') {
////                    if (PlayerLiveContainer.player != null && !PlayerLiveContainer.player.isPlaying) {
////
////                    }
////                }
//            }
//        }, object : NotSignal {
//            override fun run() {
//                badSignalReconnect(badSignalViews, loadingProgressBar, replay)
//            }
//        })
    }


    var player: ExoPlayer? = null
    var lastPlayedUrlDrm = 0
    var counterReconnectExo = 0
    val handler = Handler()
    var currentPlayer: PlayerType = PlayerType.EXOPLAYER

    fun releaseMediaPlayer(){
        try {
            if (mediaPlayer != null) {
                mediaPlayer!!.pause()
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    @OptIn(UnstableApi::class)
    fun playChannel(forcedExoPlayer : Boolean, playerView : PlayerView, progressBar: ProgressBar, url: String, drm: Int, playerFuncI: PlayerFuncI, isCatchup : Boolean, currentPlayerScreenP: CurrentPlayerScreen, token : String?) {
        lastToken = token
        try{
            if(url == "null") {
                throw BadContentTypeFormatException("Url must not be null")
            }
            currentProgressBar?.visibility = View.VISIBLE
            val currentLoopTime = System.currentTimeMillis()
            playerFuncI.releasePlayerCallback()
            playerFuncI.createPlayerCallback()
            AsyncTaskStartMulticast.g_ChannelStopped = true
            AsyncTaskStartMulticast.currentLoopTime = currentLoopTime
            var realUrl = ""
            realUrl = if (url.contains(SERVER_LOCAL_IP_EMPTY) && CurrentData.ip.contains(SERVER_GLOBAL_IP_EMPTY)) {
                url.replace(SERVER_LOCAL_IP_EMPTY, SERVER_GLOBAL_IP_EMPTY)
            }
            else {
                url
             //   url.replace("192.168.10.42", "117.216.44.13")
            }

            realUrl = if (realUrl.contains("10.22.254.30") && CurrentData.ip.contains(SERVER_GLOBAL_IP_EMPTY)) {
                realUrl.replace("10.22.254.30", "115.187.52.252")
            }
            else {
                realUrl
            }
//            realUrl = "http://115.187.52.252:8020/KAIRALIWE/index.mpd"
            PlayerScreen.actualUrl = realUrl
            lastPlayedUrl = realUrl
            lastPlayedUrlDrm = drm
            isReady = false
            if (realUrl.isEmpty()) {
                Log.w(TAG, "Url is Empty (Returning from Player )")
                return
            } else {
                Log.w(TAG, "Play URL Home Screen: $realUrl")
            }
            if (realUrl[0] == 'h') {
                currentPlayer = getPlayerByTypeCast(StreamType.UNICAST)
                if (currentPlayer !== getPlayerByTypeCast(StreamType.UNICAST)) {
                    playerView.visibility = View.GONE
                    playerView.visibility = View.VISIBLE
                    currentPlayer = getPlayerByTypeCast(StreamType.UNICAST)
                }
                if (isCatchup || forcedExoPlayer) {
                    playerView.visibility = View.GONE
                    playerView.visibility = View.VISIBLE
//                   playerView.showSurface()
                    playerView.visibility = View.VISIBLE // similar to showSurface()
                    playerView.player = exoPlayer!!
                    playHttpWithExoPlayer(exoPlayer!!, playerView, realUrl, true, token)
                } else {
                    when (currentPlayer) {
                        PlayerType.EXOPLAYER ->{
                            playHttpWithExoPlayer(exoPlayer!!, playerView, realUrl, true, token)
                        }
                        PlayerType.MEDIAPLAYER -> playWithMediaPlayer(mediaPlayer!!, realUrl)
                    }
                }
            } else {
                currentPlayer = getPlayerByTypeCast(StreamType.MULTICAST)
                if (currentPlayer !== getPlayerByTypeCast(StreamType.MULTICAST)) {
                    playerView.visibility = View.GONE
                    playerView.visibility = View.VISIBLE
                    currentPlayer = getPlayerByTypeCast(StreamType.MULTICAST)
                }
                if (drm == 0) {
                    Log.w(TAG, "Playing URL Decrypted: $realUrl")
                    if (PlayerScreen.isCatchup) {
                        playUDPWithExoplayer(exoPlayer!!, playerView, realUrl, true)
                    } else {
                        if (PlayerScreen.currentPlayer === PlayerType.EXOPLAYER || forcedExoPlayer){
                            playUDPWithExoplayer(exoPlayer!!, playerView, realUrl, true)
                        } else {
                            playWithMediaPlayer(mediaPlayer!!, realUrl)
                        }
                    }
                } else {
                    Log.w(TAG, "Playing URL Encrypted: $realUrl")
                    val multicastUrl = realUrl.replace("udp://", "").split(":".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    if (multicastUrl.isNotEmpty()) {
                        if(true){
                            playUDPAESWithExoplayer(
                                exoPlayer!!,
                                playerView,
                                realUrl,
                                playWhenReady = true,
                                multicastUrl[0],
                                multicastUrl[1])
                        }else{
                            playUDPEncryptedWithDefaultDecryptionMethod(
                                playerView = playerView,
                                multicastUrl = multicastUrl,
                                currentLoopTime = currentLoopTime,
                                forcedExoPlayer = forcedExoPlayer,
                                realUrl = realUrl
                            )
                        }
                    }
                }
            }
//            handler.removeCallbacksAndMessages(null)
//            handler.postDelayed(Runnable {
//                if(!isReady && currentPlayerScreenP == currentPlayerScreen && lastPlayedUrl != ""){
//                    Log.w(TAG, "PLAYING FROM RECONNECT 20 SECONDS DELAY")
//                    containerChannelNotAvailable.visibility = View.VISIBLE
//                    playChannel(forcedExoPlayer, playerView, progressBar, url ,drm, playerFuncI, isCatchup, currentPlayerScreenP)
//                }
//            }, 20000)

        }catch(e : Exception){
            e.printStackTrace()
        }
    }

    private fun playUDPEncryptedWithDefaultDecryptionMethod(
        playerView : PlayerView,
        multicastUrl : Array<String>,
        currentLoopTime : Long,
        forcedExoPlayer: Boolean,
        realUrl : String){
        AsyncTaskStartMulticast.g_ChannelStopped = false
        val asyncTaskStartMulticast = AsyncTaskStartMulticast()
        currentPort = multicastUrl[1].toInt()
        val hostAddr = NetworkHelper.getEthernetPrivateIP(context) ?: "224.225.226.1";
        val endUrl = "udp://@$hostAddr:1234"
        Toast.makeText(context, endUrl, Toast.LENGTH_SHORT).show()
        asyncTaskStartMulticast.doInBackground(
            multicastUrl[0], currentLoopTime.toString() + "", hostAddr
        )
        if (currentPlayer === PlayerType.EXOPLAYER || forcedExoPlayer){
            playUDPWithExoplayer(
                exoPlayer!!,
                playerView,
                endUrl,
                true
            )
        }else {
            playWithMediaPlayer(mediaPlayer!!, realUrl)
        }
    }


    }