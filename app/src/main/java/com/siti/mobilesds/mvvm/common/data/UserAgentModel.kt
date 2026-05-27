package com.siti.mobilesds.mvvm.common.data

data class UserAgentModel(
    val data: Data,
    val error: String,
    val message: String,
    val status: String
) {
    data class Data(
        val agent_name: String
    )
}

