package com.siti.mobile.mvvm.common.data.post

import com.google.gson.annotations.SerializedName

data class OnlineCustomerRequest(
    @SerializedName("customer_id")
    private val customerId : Int,
    @SerializedName("mac")
    private val mac: String,
    @SerializedName("channel_id")
    private val channelId: Long,
    @SerializedName("last_online")
    private val lastOnline : String,
    @SerializedName("ip")
    private val ip: String,
    @SerializedName("device_id")
    private val deviceModel: String
)