package com.siti.mobile.Model.app_update

data class AppUpdateResponse(
    val `data`: List<AppUpdateModel>,
    val error: String,
    val message: String,
    val status: String
)