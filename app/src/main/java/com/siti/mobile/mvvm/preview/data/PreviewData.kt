package com.siti.mobile.mvvm.preview.data

import android.content.SharedPreferences
import com.siti.mobile.Interface.ApiInterface
import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.Model.Room.RM_LiveStreamCategory
import com.siti.mobile.Utils.RetrofitClient
import com.siti.mobile.Utils.RootDatabase
import com.siti.mobile.network.statistics.StatisticsProviderNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.content.edit
import com.siti.mobile.Model.Room.RM_ImageDB
import com.siti.mobile.Model.advertisment.AdvertismentModel
import com.siti.mobile.mvvm.common.data.ParkingChannelEntity
import com.siti.mobile.mvvm.common.data.toDomain

const val CATEGORY_ID_FAVORITE = 555;

class PreviewData @Inject constructor(
    private val database: RootDatabase,
    private val sharedPreferences: SharedPreferences
) {

    var channels: List<JoinLiveStreams> = emptyList()

    suspend fun getChannels(categoryId: Int?): List<JoinLiveStreams> = withContext(Dispatchers.IO) {
        channels = database.liveChannelDAO().liveStreams
        if (categoryId != null) {
            if(categoryId == CATEGORY_ID_FAVORITE){
                database.liveChannelDAO().liveStreamsFavSortASC
            }else{
                database.liveChannelDAO().getLiveStreamsByCatId(categoryId.toString())
            }
        } else {
            channels
        }
    }

    suspend fun getCategories(): List<RM_LiveStreamCategory> = withContext(Dispatchers.IO) {
        database.liveChannelDAO().liveStreamsCategory
    }

    suspend fun getParkingChannels() : List<ParkingChannelEntity> = withContext(Dispatchers.IO) {
        database.liveChannelDAO().parkingChannels
    }

    suspend fun addChannelToFavorite(channel : JoinLiveStreams) = withContext(Dispatchers.IO) {
        val imageDB = RM_ImageDB().apply {
            logoBase64 = ""
            referenceId = channel.referenceId ?: channel.channel_id
        }

        if (channel.isFavorite != null) {
            if (channel.isFavorite == "true") {
                imageDB.isFavorite = "false"
                channel.isFavorite = "false"
            } else {
                imageDB.isFavorite = "true"
                channel.isFavorite = "true"
            }
        } else {
            imageDB.isFavorite = "true"
            channel.isFavorite = "true"
        }

        database.imageDBDAO().insertChannelImage(imageDB)
    }

    suspend fun getCategoryIndex(): Int = withContext(Dispatchers.IO) {
        sharedPreferences.getInt("SELECTED_CATEGORY_INDEX", 0)
    }

    suspend fun getSelectedChannel(): JoinLiveStreams = withContext(Dispatchers.IO) {
        val selectedChannelId = sharedPreferences.getString("SELECTED_CHANNEL_ID", "")
        channels.firstOrNull {
            it.channel_id == selectedChannelId
        } ?: channels.first()
    }

    suspend fun getLandingChannel() : JoinLiveStreams? = withContext(Dispatchers.IO) {
        val landingChannel = database.landingChannelDao().getAll().firstOrNull()
        if(channels.isNotEmpty() && landingChannel != null) {
            channels.firstOrNull { it.channel_id == landingChannel.channelId.toString() }
        }else if(channels.isEmpty() && landingChannel != null){
            val ch = database.liveChannelDAO().liveStreams
            ch.firstOrNull { it.channel_id == landingChannel.channelId.toString() }
        }else{
            null
        }



    }

    suspend fun onChannelClick(channel: JoinLiveStreams) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putString("LAST_PLAYED_URL", channel.source)
            putInt("LAST_PLAYED_URL_DRM", channel.drm_enabled)
            putString("SELECTED_CHANNEL_ID", channel.channel_id)
            putInt("CHANNEL_NO", channel.channel_no)
            putString("SELECTED_CHANNEL_LOGO", channel.logo)
        }
//        refreshStatistics()
    }

    suspend fun onCategoryClick(index: Int, category: RM_LiveStreamCategory) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putString("SELECTED_CATEGORY_ID", "${category.id}")
            putString("SELECTED_CATEGORY_NAME", category.category_name)
            putInt("SELECTED_CATEGORY_INDEX", index)
        }
    }

    private suspend fun refreshStatistics() = withContext(Dispatchers.IO){
        val serverIp: String = sharedPreferences.getString("serverIP", "null") ?: ""
        val apiInterface = RetrofitClient.getClient(serverIp).create<ApiInterface?>(ApiInterface::class.java)
        StatisticsProviderNetwork(sharedPreferences, apiInterface).refreshOnlineUser()
    }

    suspend fun getAdvertisements() : List<AdvertismentModel> = withContext(Dispatchers.IO) {
        database.advertismentDao().getAllAdvertisment().map { it.toDomain() }
    }
}
