package com.siti.mobilesds.lco

import com.google.gson.annotations.SerializedName

data class LCOCheckRequest(
    @SerializedName("macaddr")
    val macAddr: String
)