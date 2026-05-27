package com.siti.mobilesds.mvvm.common.data

data class UserVerificationModel(
    val data: Data,
    val error: String,
    val message: String,
    val status: String
) {
    data class Data(
        val userId: Int,
        val username: String,
        val verified: Boolean,
        val exp_date: Int
    )
}

