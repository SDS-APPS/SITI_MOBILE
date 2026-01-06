package com.siti.mobile.mvvm.fullscreen.domain

import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.mvvm.common.data.programs.Program
import com.siti.mobile.mvvm.config.helpers.LastSeenModel
import com.siti.mobile.mvvm.fullscreen.data.PlayerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlayerDomain @Inject constructor(
    private val data: PlayerData
) {

    suspend fun saveLastSeen(channel: JoinLiveStreams) = withContext(Dispatchers.IO) {
        data.saveLastSeen(
            LastSeenModel(
                channel.channel_id,
                channel.logo
            )
        )
    }

    suspend fun logOut(onFinished : () -> Unit) = withContext(Dispatchers.IO){
        data.logOut(onFinished)
    }

    suspend fun getActualAndNextProgram(selectedChannelId: String): List<Program> = withContext(Dispatchers.IO) {
        data.getActualAndNextProgram(selectedChannelId)
    }

    suspend fun startTimerIncreaseMinsChannels(currentChannel: JoinLiveStreams) = withContext(Dispatchers.IO){
        data.startIncreaseMinChannels(currentChannel.channel_id, currentChannel.channel_name)
    }

    suspend fun eventChannelPlay(currentChannel: JoinLiveStreams) = withContext(Dispatchers.IO) {
        data.eventChannelPlay(currentChannel)
    }

    suspend fun getAllPrograms() = withContext(Dispatchers.IO) {
        data.getAllPrograms()
    }

    suspend fun saveCategoryIndex(index: Int) = withContext(Dispatchers.IO) {
        data.saveCategoryIndex(index)
    }
}
