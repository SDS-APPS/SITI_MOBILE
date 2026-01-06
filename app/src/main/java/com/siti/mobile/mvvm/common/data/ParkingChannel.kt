package com.siti.mobile.mvvm.common.data

import com.google.gson.annotations.SerializedName

data class ParkingChannel(
    @SerializedName("channel_id")
    val channelId: Long,
    @SerializedName("parking_position")
    val parkingPosition: Int,
    val sec: Int
)

fun ParkingChannel.toEntity() = ParkingChannelEntity(channelId = channelId, parkingPosition = parkingPosition, sec = sec);