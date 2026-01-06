package com.siti.mobile.lco

import com.google.gson.annotations.SerializedName

data class LCOCheckResponse(
    val opcode: String,
    @SerializedName("returncode")
    val returnCode: String,
    @SerializedName("returnmessage")
    val returnMessage: String
)