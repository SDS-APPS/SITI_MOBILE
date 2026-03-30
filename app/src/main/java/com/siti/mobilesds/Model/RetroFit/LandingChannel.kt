package com.siti.mobilesds.Model.RetroFit

import com.siti.mobilesds.Model.Room.RM_LandingChannel
import com.google.gson.annotations.SerializedName

data class LandingChannel(
    @SerializedName("channel_id")
    val channelId: Long
)

fun LandingChannel.toEntity() : RM_LandingChannel {
    return RM_LandingChannel(channelId);
}