package com.siti.mobilesds.mvvm.config.helpers

import com.siti.mobilesds.Log.FirestoreLog
import com.siti.mobilesds.mvvm.config.constans.EVENT_HOME_SCREEN
import com.siti.mobilesds.mvvm.config.constans.EVENT_LIVE_PREVIEW_SCREEN
import com.siti.mobilesds.mvvm.config.constans.EVENT_LIVE_TV_SCREEN
import com.siti.mobilesds.mvvm.config.constans.PARAM_CHANNEL_ID
import com.siti.mobilesds.mvvm.config.constans.PARAM_CHANNEL_NO
import com.siti.mobilesds.mvvm.config.constans.PARAM_SCREEN
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class AnalyticsEventsHelper @Inject constructor() {

    private var firebaseAnalytics : FirebaseAnalytics = Firebase.analytics

    fun eventLogIn() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null)
        FirestoreLog().registerLoggedIn();
    }

    fun eventAppOpen() = firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);

    fun eventHomeScreen() = firebaseAnalytics.logEvent(EVENT_HOME_SCREEN, null);

    fun eventPreviewScreen(channelName : String, channelNo : String, channelId : String) {
        firebaseAnalytics.logEvent(channelName.uppercase().replace(" ", "_")) {
            param(PARAM_SCREEN, EVENT_LIVE_PREVIEW_SCREEN)
            param(PARAM_CHANNEL_NO, channelNo)
            param(PARAM_CHANNEL_ID, channelId)
        }
    }

    fun eventLiveTvScreen(channelName : String, channelNo : String, channelId : String) {
        firebaseAnalytics.logEvent(channelName.uppercase().replace(" ", "_")) {
            param(PARAM_SCREEN, EVENT_LIVE_TV_SCREEN)
            param(PARAM_CHANNEL_NO, channelNo)
            param(PARAM_CHANNEL_ID, channelId)
        }
    }
}