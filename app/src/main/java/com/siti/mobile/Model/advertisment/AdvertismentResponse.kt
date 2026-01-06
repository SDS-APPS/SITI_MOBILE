package com.siti.mobile.Model.advertisment

data class AdvertismentResponse(
    val data: List<AdvertismentModel>,
    val error: String,
    val message: String,
    val status: String
)