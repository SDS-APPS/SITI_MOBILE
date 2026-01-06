package com.siti.mobile.mvvm.common.data.models

data class CatchupChannelsResponse(
    val `data`: List<CatchupChannel>,
    val error: String,
    val message: String,
    val status: String
)