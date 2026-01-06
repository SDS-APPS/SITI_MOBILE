package com.siti.mobile.network.main.live_tv

import android.util.Log
import com.siti.mobile.Interface.ApiInterface
import com.siti.mobile.Model.RetroFit.ChannelStatistics
import com.siti.mobile.Model.RetroFit.ChannelStatisticsResponse
import com.siti.mobile.Model.RetroFit.LandingChannelResponse
import com.siti.mobile.Model.RetroFit.LiveCategory
import com.siti.mobile.Model.RetroFit.LiveStream
import com.siti.mobile.Utils.DBHelper
import com.siti.mobile.Utils.DBHelperKt
import com.siti.mobile.mvvm.common.data.ParkingChannelsResponse
import com.siti.mobile.mvvm.common.data.models.CatchupChannelsResponse
import com.siti.mobile.network.main.contracts.CallApiDB
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LiveTVProviderNetwork(
    private val apiInterface: ApiInterface,
    private val authToken : String,
    private val dbHelper: DBHelper,
    private val dbHelperKt: DBHelperKt
) : CallApiDB {

    private var callsRealized = 0

    fun refreshAllLiveTV(onFinish : (String) -> Unit) {
        refreshCategories {
            callsRealized++
            if(callsRealized == totalCalls) onFinish(TAG)
        }
        refreshChannels {
            callsRealized++
            if(callsRealized == totalCalls) onFinish(TAG)
        }
        refreshCatchupChannels {
            callsRealized++
            if(callsRealized == totalCalls) onFinish(TAG)
        }
        refreshLandingChannel {
            callsRealized++
            if(callsRealized == totalCalls) onFinish(TAG)
        }
        refreshParkingChannels {
            callsRealized++
            if(callsRealized == totalCalls) onFinish(TAG)
        }
    }

    private fun refreshParkingChannels(onRefresh: () -> Unit) {
        val parkingChannels = apiInterface.getParkingChannels("b $authToken")
        parkingChannels.enqueue(object : Callback<ParkingChannelsResponse> {
            override fun onResponse(
                call: Call<ParkingChannelsResponse?>,
                response: Response<ParkingChannelsResponse?>
            ) {
                if(response.code() == 200) {
                    val channels = response.body()!!.data;
                    dbHelperKt.openDatabase()
                    dbHelperKt.insertParkingChannels(channels);
                    dbHelperKt.closeDb()
                }
                onRefresh()
            }

            override fun onFailure(
                call: Call<ParkingChannelsResponse?>,
                t: Throwable
            ) {
                Log.i(TAG, "onFailure: " + t.message)
                onRefresh()
            }

        })
    }

    private fun refreshCategories(onRefresh : () -> Unit) {
        val liveCategory = apiInterface.getLiveCategory("b $authToken")
        liveCategory.enqueue(object : Callback<LiveCategory?> {
            override fun onResponse(call: Call<LiveCategory?>, response: Response<LiveCategory?>) {
                if (response.code() == 200) {
                    val category = response.body()
                    val data = category!!.data
                    dbHelper.insertLiveStreamCategory(data)
                }
                onRefresh()
            }

            override fun onFailure(call: Call<LiveCategory?>, t: Throwable) {
                Log.i(TAG, "onFailure: " + t.message)
                onRefresh()
            }
        })
    }

    private fun refreshChannels(onRefresh : () -> Unit) {
        val LiveStream = apiInterface.getLiveStream("b $authToken")
        LiveStream.enqueue(object : Callback<LiveStream?> {
            override fun onResponse(call: Call<LiveStream?>, response: Response<LiveStream?>) {
                if (response.code() == 200) {
                    val data = response.body()
                    val list = data!!.getData()
                    dbHelper.insertLiveStreamData(list)
                }
                onRefresh()
            }

            override fun onFailure(call: Call<LiveStream?>, t: Throwable) {
                onRefresh()
                Log.i(TAG, "onFailure: " + t.message)
            }
        })
    }

    private fun refreshCatchupChannels(onRefresh : () -> Unit) {
        val CatchupChannels = apiInterface.getCatchupChannels("b $authToken")
        CatchupChannels.enqueue(object : Callback<CatchupChannelsResponse?> {
            override fun onResponse(call: Call<CatchupChannelsResponse?>, response: Response<CatchupChannelsResponse?>) {
                if (response.code() == 200) {
                    val data = response.body()
                    val list = data!!.data
                    dbHelper.insertCatchupChannels(list)
                }
                Log.w(TAG, "***********************Response CAtchupChannels: " + response.body())
                onRefresh()
            }

            override fun onFailure(call: Call<CatchupChannelsResponse?>, t: Throwable) {
                onRefresh()
                Log.i(TAG, "***************onFailure CATCHUP CHANNELS:********** " + t.message)
            }
        })
    }

    private fun refreshLandingChannel(onRefresh: () -> Unit) {
        val landingChannel = apiInterface.getLandingChannel("b $authToken")
        landingChannel.enqueue(object : Callback<LandingChannelResponse?> {
            override fun onResponse(call: Call<LandingChannelResponse?>, response: Response<LandingChannelResponse?>) {
                if (response.code() == 200) {
                    val data = response.body()
                    val dataLanding = data!!.data
                    dbHelper.insertLandingChannel(dataLanding)
                }
                Log.w(TAG, "***********************Response Landing Channel: " + response.body())
                onRefresh()
            }

            override fun onFailure(call: Call<LandingChannelResponse?>, t: Throwable) {
                onRefresh()
                Log.i(TAG, "***************onFailure Landing Channels CHANNELS:********** " + t.message)
            }
        })
    }

    companion object {

        val TAG = "LiveTVProviderNetworkCompanion"

        fun postStatistics(apiInterface : ApiInterface, authToken : String, data: ChannelStatistics) {
            val statistics = apiInterface.postChannelStatistics("b $authToken", Gson().toJson(data))
            statistics.enqueue(object : Callback<ChannelStatisticsResponse> {
                override fun onResponse(
                    call: Call<ChannelStatisticsResponse>,
                    response: Response<ChannelStatisticsResponse>
                ) {
                    if (response.code() == 200) {
                        val data = response.body()
                        data?.let {
                            Log.w(TAG, "Statistics: ${data.message} - ${data.status}")
                        } ?: {
                            Log.w(TAG, "DATA NULL")
                        }
                    }else{
                        Log.w(TAG, "Response Code: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<ChannelStatisticsResponse>,
                    t: Throwable
                ) {
                    Log.w(TAG, "On Failure post Statistics.")
                }

            })
        }
    }



    override val totalCalls: Int
        get() = 5
    override val TAG: String
        get() = "LIVE TV PROVIDER"

}