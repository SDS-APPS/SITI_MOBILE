package com.siti.mobile.mvvm.common.data.programs

import com.siti.mobile.Model.RetroFit.ProgramsAllChannelsModel
import com.siti.mobile.mvvm.common.data.RetrofitClientEpg
import com.siti.mobile.mvvm.common.data.epg.EPGAPIService
import com.siti.mobile.mvvm.common.data.epg.model.EPGAllModel
import kotlinx.coroutines.*
import org.threeten.bp.LocalDateTime

class ProgramProvider(private val serverIp : String?, private val retrofitClientEpg: RetrofitClientEpg) {
 //   val BASE_URL = "https://iptvsds.in/apis/getEPG/epg_program/"
    val retrofit = retrofitClientEpg.getRetrofit("${serverIp}getEPG/current_epg/", false)

    private suspend fun getCurrentProgramsById(programId: String, authToken : String) = withContext(Dispatchers.IO) {
        try {
            val job = async { retrofit!!.create(EPGAPIService::class.java).getProgramById(programId = programId) }
            val call = job.await()
            val programs = call.body()
            programs?.data ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getProgramsById(programId: String, authToken : String) = withContext(Dispatchers.IO) {
        try {
            val retrofit = retrofitClientEpg.getRetrofit("${serverIp}getEPG/epg_program/", false)
            val job = async { retrofit!!.create(EPGAPIService::class.java).getProgramById(programId = programId) }
            val call = job.await()
            val programs = call.body()
            programs?.data ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAllPrograms() = runBlocking{
        serverIp?.let {
            val retrofit = retrofitClientEpg.getRetrofit(serverIp, false)
            try {
                val job = async { retrofit!!.create(EPGAPIService::class.java).getProgramsNextAllChannels() }
                val call = job.await()
                val programs = call.body()
                programs?.data ?: emptyList<ProgramsAllChannelsModel>()
            } catch (e: Exception) {
                emptyList<ProgramsAllChannelsModel>()
            }
        }

    }

    fun getAllEPG() = runBlocking {
        serverIp?.let {
            val retrofit = retrofitClientEpg.getRetrofit(serverIp, false)
            val nullMap = hashMapOf<Long, List<EPGAllModel>>()
            nullMap[0] = emptyList();
            try {
                val job = async { retrofit!!.create(EPGAPIService::class.java).getAllPrograms() }
                val call = job.await()
                val programs = call.body()
                programs?.data?.groupBy { it.channel_id } ?: nullMap
            } catch (e: Exception) {
                nullMap
            }
        }
    }

    fun getCurrentAndNextProgram(
        channelId: String,
        authToken : String
    ) = runBlocking{
        getCurrentProgramsById(channelId,authToken)
    }

    fun getCatchupChannelProgram(
        channelId: String,
        authToken : String
    ) = runBlocking{
        getProgramsById(channelId,authToken)
    }

    private fun getDatetime(date: String): LocalDateTime {
        val hourFormat = date.substring(0, 16)
        return LocalDateTime.parse(hourFormat)
    }

}