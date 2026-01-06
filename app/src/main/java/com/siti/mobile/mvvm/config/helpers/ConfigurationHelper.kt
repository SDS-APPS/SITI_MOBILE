package com.siti.mobile.mvvm.config.helpers

import android.content.SharedPreferences
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.upstream.DefaultAllocator
import com.siti.mobile.Utils.DEFAULT_BUFFER_PLAYBACK
import com.siti.mobile.Utils.DEFAULT_BUFFER_PLAYBACK_AFTER_REBUFFER
import com.siti.mobile.Utils.DEFAULT_BUFFER_PLAYBACK_AFTER_REBUFFER_MULTICAST
import com.siti.mobile.Utils.DEFAULT_BUFFER_PLAYBACK_MULTICAST
import com.siti.mobile.Utils.DEFAULT_MAX_BUFFER
import com.siti.mobile.Utils.DEFAULT_MAX_BUFFER_MULTICAST
import com.siti.mobile.Utils.DEFAULT_MIN_BUFFER
import com.siti.mobile.Utils.DEFAULT_MIN_BUFFER_MULTICAST
import com.siti.mobile.Utils.KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MULTICAST
import com.siti.mobile.Utils.KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_UNICAST
import com.siti.mobile.Utils.KEY_BUFFER_FOR_PLAYBACK_MS_MULTICAST
import com.siti.mobile.Utils.KEY_BUFFER_FOR_PLAYBACK_MS_UNICAST
import com.siti.mobile.Utils.KEY_MAX_BUFFER_MS_MULTICAST
import com.siti.mobile.Utils.KEY_MAX_BUFFER_MS_UNICAST
import com.siti.mobile.Utils.KEY_MIN_BUFFER_MS_MULTICAST
import com.siti.mobile.Utils.KEY_MIN_BUFFER_MS_UNICAST
import javax.inject.Inject


class ConfigurationHelper @Inject constructor(
    private val mPreferences: SharedPreferences
) {

    private var minBufferMsUnicast: Int = 0
    private var maxBufferMsUnicast: Int = 0
    private var bufferForPlaybackMsUnicast: Int = 0
    private var bufferForPlaybackAfterRebufferMsUnicast: Int = 0

    private var minBufferMsMulticast: Int = 0
    private var maxBufferMsMulticast: Int = 0
    private var bufferForPlaybackMsMulticast: Int = 0
    private var bufferForPlaybackAfterRebufferMsMulticast: Int = 0




    private fun setBufferValues() {
            // Default Values: 2500, 10000, 2000, 2000
            minBufferMsUnicast = mPreferences.getInt(KEY_MIN_BUFFER_MS_UNICAST, DEFAULT_MIN_BUFFER)
            maxBufferMsUnicast = mPreferences.getInt(KEY_MAX_BUFFER_MS_UNICAST, DEFAULT_MAX_BUFFER)
            bufferForPlaybackMsUnicast =
                mPreferences.getInt(KEY_BUFFER_FOR_PLAYBACK_MS_UNICAST, DEFAULT_BUFFER_PLAYBACK)
            bufferForPlaybackAfterRebufferMsUnicast = mPreferences.getInt(
                KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_UNICAST,
                DEFAULT_BUFFER_PLAYBACK_AFTER_REBUFFER
            )

            minBufferMsMulticast =
                mPreferences.getInt(KEY_MIN_BUFFER_MS_MULTICAST, DEFAULT_MIN_BUFFER_MULTICAST)
            maxBufferMsMulticast =
                mPreferences.getInt(KEY_MAX_BUFFER_MS_MULTICAST, DEFAULT_MAX_BUFFER_MULTICAST)
            bufferForPlaybackMsMulticast = mPreferences.getInt(
                KEY_BUFFER_FOR_PLAYBACK_MS_MULTICAST,
                DEFAULT_BUFFER_PLAYBACK_MULTICAST
            )
            bufferForPlaybackAfterRebufferMsMulticast = mPreferences.getInt(
                KEY_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MULTICAST,
                DEFAULT_BUFFER_PLAYBACK_AFTER_REBUFFER_MULTICAST
            )
        }

        @androidx.annotation.OptIn(UnstableApi::class)
        fun getDefaultLoadControl(isUnicast: Boolean): DefaultLoadControl {
            setBufferValues()
            val loadControl: DefaultLoadControl?
            val allocator = DefaultAllocator(true, 5 * 1024 * 1024)
            if (isUnicast) {
                loadControl = DefaultLoadControl.Builder().setAllocator(allocator)
                    .setBufferDurationsMs(
                        minBufferMsUnicast,
                        maxBufferMsUnicast,
                        bufferForPlaybackMsUnicast,
                        bufferForPlaybackAfterRebufferMsUnicast
                    )
                    .setPrioritizeTimeOverSizeThresholds(false) //     .setTargetBufferBytes(1024 * 1024)
                    .build()
            } else {
                loadControl = DefaultLoadControl.Builder().setAllocator(allocator)
                    .setBufferDurationsMs(
                        minBufferMsMulticast,
                        maxBufferMsMulticast,
                        bufferForPlaybackMsMulticast,
                        bufferForPlaybackAfterRebufferMsMulticast
                    )
                    .setPrioritizeTimeOverSizeThresholds(false) //    .setTargetBufferBytes(1024 * 1024)
                    .build()
            }
            return loadControl
        }
}