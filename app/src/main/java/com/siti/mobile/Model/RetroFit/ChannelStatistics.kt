package com.siti.mobile.Model.RetroFit

import com.google.gson.annotations.SerializedName

data class ChannelStatistics(
    @SerializedName("channel_id")
    val channelId: Long,
    val duration: Int,
    @SerializedName("viewed_at")
    val viewedAt : String
)

data class ChannelStatisticsResponse(
    val message : String,
    val status: String,
    val error : String
)
