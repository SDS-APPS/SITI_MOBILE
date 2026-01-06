package com.siti.mobile.Player

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import java.util.UUID

@UnstableApi
class CustomDRMSessionManagerProvider(
    private val userAgent: String,
    private val licenseUrl: String,
    private val drmToken: String?
) : DrmSessionManagerProvider {

    override fun get(mediaItem: MediaItem): DrmSessionManager {
        val drmSchemeUuid: UUID = C.WIDEVINE_UUID
        val httpDataSourceFactory: HttpDataSource.Factory =
            DefaultHttpDataSource.Factory().setUserAgent(userAgent)

        val drmCallback = HttpMediaDrmCallback(licenseUrl, httpDataSourceFactory)
        drmToken?.let {
            drmCallback.setKeyRequestProperty("X-AxDRM-Message", it)
        }

        return DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(drmSchemeUuid) {
                FrameworkMediaDrm.newInstance(drmSchemeUuid)
            }
            .build(drmCallback)
    }
}