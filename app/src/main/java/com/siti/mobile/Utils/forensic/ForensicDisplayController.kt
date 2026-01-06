package com.siti.mobile.Utils.forensic

import android.os.Handler
import android.os.Looper
import android.widget.ImageView

class ForensicDisplayController(private val ivForensic: ImageView) {

    private val handler = Handler(Looper.getMainLooper())
    private val showDurationMs = 17L
    private val intervalMs = 1500L

    private lateinit var showRunnable: Runnable
    private lateinit var hideRunnable: Runnable

    init {
        showRunnable = Runnable {
            ivForensic.alpha = 0.07f
            ivForensic.visibility = ImageView.VISIBLE
            handler.postDelayed(hideRunnable, showDurationMs)
        }
        hideRunnable = Runnable {
            ivForensic.visibility = ImageView.INVISIBLE
            handler.postDelayed(showRunnable, intervalMs - showDurationMs)
        }
    }

    fun start() {
        ivForensic.visibility = ImageView.INVISIBLE
        handler.post(showRunnable)
    }

    fun stop() {
        handler.removeCallbacks(showRunnable)
        handler.removeCallbacks(hideRunnable)
        ivForensic.visibility = ImageView.INVISIBLE
    }
}

