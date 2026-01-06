package com.siti.mobile.mvvm.common.data

import com.siti.mobile.mvvm.common.data.epg.EPGAPIService
import kotlinx.coroutines.*

class ChannelProvider(retrofitClientEpg: RetrofitClientEpg) {
    val BASE_URL = "http://65.21.153.139:3001/"
    val retrofit = retrofitClientEpg.getRetrofit(BASE_URL, true)

    fun getChannels() = runBlocking {
            try{
                val call = async {retrofit!!.create(EPGAPIService::class.java).getChannels() }

                val channels = call.await()?.body()
                channels
            }catch (e : Exception)
            {
               null
            }

    }
}