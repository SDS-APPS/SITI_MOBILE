package com.siti.mobile.mvvm.preview.domain

import com.siti.mobile.Model.JoinData.JoinLiveStreams
import com.siti.mobile.Model.Room.RM_LiveStreamCategory
import com.siti.mobile.Model.advertisment.AdvertismentModel
import com.siti.mobile.mvvm.common.data.ParkingChannelModel
import com.siti.mobile.mvvm.common.data.toDomain
import com.siti.mobile.mvvm.preview.data.PreviewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PreviewDomain
@Inject constructor(
    private val data: PreviewData,
) {

    companion object {
        var advertisements : List<AdvertismentModel> = emptyList()
    }

    suspend fun getChannels(categoryId: Int?): List<JoinLiveStreams> = withContext(Dispatchers.IO) {
        data.getChannels(categoryId).sortedBy { it.channel_no }
    }

    suspend fun getLandingChannel() : JoinLiveStreams? = withContext(Dispatchers.IO) {
        data.getLandingChannel()
    }

    suspend fun addChannelToFavorite(channel : JoinLiveStreams) = withContext(Dispatchers.IO) {
        data.addChannelToFavorite(channel)
    }

    suspend fun getParkingChannels() : List<ParkingChannelModel> = withContext(Dispatchers.IO) {
        val channels = data.getChannels(null)
        data.getParkingChannels().map { entity -> entity.toDomain() }.sortedBy { it.parkingPosition }
            .map { channelApi ->
                val channel = channels.first{ channelApi.channelId.toString() == it.channel_id  }
                ParkingChannelModel(channel = channel, milliSecs = channelApi.sec * 1000L)
            }
    }

    suspend fun getCategories(): List<RM_LiveStreamCategory> = withContext(Dispatchers.IO) {
        val cats = data.getCategories().toMutableList()

//        val allCategory = RM_LiveStreamCategory().apply {
//            category_id = null
//            category_name = "All channels"
//            parent_id = 0
//            category_count = 0
//        }
//        cats.add(0, allCategory)

        /*val favoriteCategory = RM_LiveStreamCategory().apply {
            category_id = "$CATEGORY_ID_FAVORITE"
            category_name = "Favourites"
            parent_id = 0
            category_count = 1
        }
        cats.add(index = 1, favoriteCategory)*/

        cats
    }

    suspend fun getAllChannels(): List<JoinLiveStreams> = withContext(Dispatchers.IO) {
        data.channels
    }

    suspend fun onChannelClick(channel: JoinLiveStreams) = withContext(Dispatchers.IO) {
        data.onChannelClick(channel)
    }

    suspend fun onCategoryClick(index: Int, category: RM_LiveStreamCategory) = withContext(Dispatchers.IO) {
        data.onCategoryClick(index, category)
    }

    suspend fun getCategoryIndex(): Int = withContext(Dispatchers.IO) {
        data.getCategoryIndex()
    }

    suspend fun getSelectedChannel(): JoinLiveStreams = withContext(Dispatchers.IO) {
        data.getSelectedChannel()
    }


    suspend fun getAdvertisements() : List<AdvertismentModel> = withContext(Dispatchers.IO) {
        if(advertisements.isNotEmpty()) advertisements
        else data.getAdvertisements()
    }

}
