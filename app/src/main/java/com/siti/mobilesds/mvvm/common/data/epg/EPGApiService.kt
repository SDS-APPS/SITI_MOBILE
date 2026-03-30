package com.siti.mobilesds.mvvm.common.data.epg


import com.siti.mobilesds.Model.RetroFit.ProgramsAllChannelsResponse
import com.siti.mobilesds.mvvm.common.data.ChannelResponseList
import com.siti.mobilesds.mvvm.common.data.epg.model.AllEPGResponse
import com.siti.mobilesds.mvvm.common.data.programs.ProgramResponse
import retrofit2.Response
import retrofit2.http.*

interface EPGAPIService {

    @GET("admin/channels")
    suspend fun getChannels() : Response<ChannelResponseList>?


    @GET
    suspend fun getProgramById(@Url programId: String) : Response<ProgramResponse>

    @GET("getepg/epgallprogram")
    suspend fun getAllPrograms() : Response<AllEPGResponse>


    @GET("getepg/current_epgAllProgram")
    suspend fun getProgramsNextAllChannels() : Response<ProgramsAllChannelsResponse>
}