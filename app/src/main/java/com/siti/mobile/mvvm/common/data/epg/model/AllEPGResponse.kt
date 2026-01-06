package com.siti.mobile.mvvm.common.data.epg.model

data class AllEPGResponse(
    val `data`: List<EPGAllModel>,
    val error: String,
    val message: String,
    val status: String
)

data class EPGAllModel(
    val channel_id: Long,
    val description: String,
    val duration: Int,
    val endAt: String,
    val endAtXMLTV: String,
    val id: Int,
    val name: String,
    val program_id: Int,
    val source: String,
    val startAt: String,
    val startAtXMLTV: String,
    var title: String,
    val type: String
)