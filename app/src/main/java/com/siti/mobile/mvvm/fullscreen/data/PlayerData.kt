package com.siti.mobile.mvvm.fullscreen.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.siti.mobile.Log.FirestoreLog
import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.Model.RetroFit.ProgramsAllChannelsModel
import com.siti.mobile.Player.PlayerLiveContainer
import com.siti.mobile.Utils.KEY_AUTH_TOKEN
import com.siti.mobile.Utils.KEY_SERVER_IP
import com.siti.mobile.mvvm.common.data.RetrofitClientEpg
import com.siti.mobile.mvvm.common.data.programs.Program
import com.siti.mobile.mvvm.common.data.programs.ProgramProvider
import com.siti.mobile.mvvm.config.helpers.AnalyticsEventsHelper
import com.siti.mobile.mvvm.config.helpers.AuthHelper
import com.siti.mobile.mvvm.config.helpers.LastSeenHelper
import com.siti.mobile.mvvm.config.helpers.LastSeenModel
import com.siti.mobile.network.keys.NetworkPackageKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlayerData @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mPreferences: SharedPreferences,
    private val lastSeenHelper: LastSeenHelper,
    private val retrofitClientEpg: RetrofitClientEpg,
    private val analyticsEventsHelper: AnalyticsEventsHelper
) {

    suspend fun saveCategoryIndex(index: Int) = withContext(Dispatchers.IO) {
        mPreferences.edit { putInt("SELECTED_CATEGORY_INDEX", index) }
    }

    suspend fun saveLastSeen(lastSeenModel: LastSeenModel) = withContext(Dispatchers.IO) {
        lastSeenHelper.saveNewLastSeen(lastSeenModel)
    }

    suspend fun logOut(onFinished : () -> Unit)  = withContext(Dispatchers.IO){
        mPreferences.edit().apply {
            putString("AuthCode", "null")
            putString("subscribeStatus", "null")
            putString("LiveStream", "null")
            putString("serverIP", "null")
            putString("LiveCategory", "null")
            putString("VODStream", "null")
            putString("VODCategory", "null")
            putString("SeriesStream", "null")
            putString("SeriesCategory", "null")
            putString("LAST_PLAYED_URL", PlayerLiveContainer.nullUrl)
            putInt(NetworkPackageKeys.LIVE_TV, 0)
            putInt(NetworkPackageKeys.VOD, 0)
            putInt(NetworkPackageKeys.SOD, 0)
            putInt(NetworkPackageKeys.MOD, 0)
            putString("LAST_PLAYED_URL", "") // 👈 ojo: esto sobreescribe el valor anterior
            putBoolean(NetworkPackageKeys.FIRST_LOAD, true)
            apply()
        }

        val authHelper = AuthHelper()
        authHelper.signOut()
        onFinished()
    }

    suspend fun getActualAndNextProgram(selectedChannelId: String): List<Program> = withContext(Dispatchers.IO) {
        val serverIP = mPreferences.getString(KEY_SERVER_IP, "")!!
        val authToken = mPreferences.getString(KEY_AUTH_TOKEN, "")!!
        val programProvider = ProgramProvider(serverIP, retrofitClientEpg)
        programProvider.getCurrentAndNextProgram(selectedChannelId, authToken)
    }

    suspend fun getAllPrograms(): List<ProgramsAllChannelsModel> = withContext(Dispatchers.IO) {
        val serverIP = mPreferences.getString(KEY_SERVER_IP, "")!!
        val programProvider = ProgramProvider(serverIP, retrofitClientEpg)
        programProvider.getAllPrograms() ?: emptyList()
    }

    suspend fun startIncreaseMinChannels(channelId: String, channelName: String) = withContext(
        Dispatchers.IO){
        FirestoreLog().startTimerIncreaseMinsChannels(
            channelId,
            channelName,
            context,
            System.currentTimeMillis()
        )
    }

    suspend fun eventChannelPlay(channel: JoinLiveStreams) = withContext(Dispatchers.IO) {
        analyticsEventsHelper.eventLiveTvScreen(
            channelName = channel.channel_name,
            channelNo = channel.channel_no.toString(),
            channelId = channel.channel_id
        )
    }
}
