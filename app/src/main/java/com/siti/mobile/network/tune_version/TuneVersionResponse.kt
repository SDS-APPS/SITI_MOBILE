package com.siti.mobile.network.tune_version

data class TuneVersionResponse(
    val `data`: List<TuneVersion>,
    val error: String,
    val message: String,
    val status: String
)