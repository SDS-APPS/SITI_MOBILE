package com.siti.mobile.Player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.video.VideoRendererEventListener

@SuppressLint("UnsafeOptInUsageError")
class CustomRenderersFactory(context: Context) : DefaultRenderersFactory(context) {
    @OptIn(UnstableApi::class)
    override fun buildVideoRenderers(
        context: Context,
        extensionRendererMode: Int,
        mediaCodecSelector: MediaCodecSelector,
        enableDecoderFallback: Boolean,
        eventHandler: Handler,
        eventListener: VideoRendererEventListener,
        allowedVideoJoiningTimeMs: Long,
        out: ArrayList<Renderer>
    ) {
        // Forzar a usar decodificadores NO seguros
        val customSelector = MediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
            mediaCodecSelector.getDecoderInfos(mimeType, false, requiresTunnelingDecoder) // false = NO secure
        }

        super.buildVideoRenderers(
            context,
            extensionRendererMode,
            customSelector,
            enableDecoderFallback,
            eventHandler,
            eventListener,
            allowedVideoJoiningTimeMs,
            out
        )
    }
}