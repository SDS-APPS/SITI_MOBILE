package com.siti.mobile.Model

import com.google.gson.annotations.SerializedName

data class GeneralResponse(
    @SerializedName("message")
    val message : String,
    @SerializedName("status")
    val status : String,
    @SerializedName("error")
    val error: String
)
