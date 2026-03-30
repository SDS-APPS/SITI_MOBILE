package com.siti.mobilesds.mvvm.common.data

data class ParkingChannelsResponse(
    val `data`: List<ParkingChannel>,
    val error: String,
    val message: String,
    val status: String
)