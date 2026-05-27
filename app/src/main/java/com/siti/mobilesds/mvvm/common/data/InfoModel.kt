package com.siti.mobilesds.mvvm.common.data

data class InfoModel(
    val data: List<Data>,
    val error: String,
    val message: String,
    val status: String
) {
    data class Data(
        val dataKey: String,
        val sno: Int,
        val value: String
    )
}

