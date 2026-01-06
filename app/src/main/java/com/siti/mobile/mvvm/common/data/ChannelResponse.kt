package com.siti.mobile.mvvm.common.data

import com.siti.mobile.Model.RetroFit.LiveStream


data class ChannelResponseList(
    val data : List<ChannelResponse>,
    val message : String,
    val status:  String,
    val error : String )

data class StreamResponse(
    val data : List<LiveStream>,
    val message : String,
    val status:  String,
    val error : String )

data class ChannelResponse(
    val channel_id : Long,
    val channel_no:  Int,
    val category_id : Int,
    val name : String,
    val description : String,
    val logo : String,
    val source : String,
    val drm_source : String,
    val drm_enabled : Int,
    val createdAt : String
)
