package com.siti.mobile.mvvm.common.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.siti.mobile.mvvm.splash.view.TAG
import com.siti.mobile.Interface.ApiInterface
import com.siti.mobile.Model.RetroFit.*
import com.siti.mobile.Model.advertisment.AdvertismentResponse
import com.siti.mobile.Utils.*
import com.siti.mobile.mvvm.common.data.models.CatchupChannelsResponse
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateLocalDB(private val activity : Activity) {

    private val mPreferences =  activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

    fun updateLocalDB(dbHelperKt: DBHelperKt,
                               dbHelper: DBHelper,
                               increaseCalls : IncreaseCalls)
    {
        val ServerIP = mPreferences.getString(KEY_SERVER_IP, PREFERENCES_STRING_DEFAULT_VALUE)
        val ServerIPAdmin = mPreferences.getString(KEY_SERVER_IP_ADMIN, PREFERENCES_STRING_DEFAULT_VALUE)
        val AuthToken = mPreferences.getString(KEY_AUTH_TOKEN, PREFERENCES_STRING_DEFAULT_VALUE)
        Log.i("AuthToken", "Auth token: $AuthToken")
        val apiInterface = RetrofitClient.getClient(ServerIP).create(
            ApiInterface::class.java
        )
        val liveCategory = apiInterface.getLiveCategory("b $AuthToken")
        liveCategory.enqueue(object : Callback<LiveCategory?> {
            override fun onResponse(call: Call<LiveCategory?>, response: Response<LiveCategory?>) {
                if (response.code() == 200) {
                    val category = response.body()
                    val data = category!!.data
                    dbHelper.insertLiveStreamCategory(data)
                }
                increaseCalls.call("Live TV CAtegory")
            }

            override fun onFailure(call: Call<LiveCategory?>, t: Throwable) {
                Log.i(TAG, "onFailure: " + t.message)
                increaseCalls.call("Live TV CAtegory")
            }
        })
        val LiveStream = apiInterface.getLiveStream("b $AuthToken")
        LiveStream.enqueue(object : Callback<LiveStream?> {
            override fun onResponse(call: Call<LiveStream?>, response: Response<LiveStream?>) {
                if (response.code() == 200) {
                    val data = response.body()
                    val list = data!!.getData()
                    dbHelper.insertLiveStreamData(list)
//                    val programProvider =
//                        ProgramProvider(ServerIP!!, RetrofitClientEpg(context = context))
               //     dbepgHelper.deleteAllPrograms()
//                    for (i in 0 until list.size - 1) {
//                        val channel_id = list[i].channelId
//                        val programs: List<Program?>? = programProvider.getProgramById(channel_id)
//                        if (programs != null && programs.size > 0) dbepgHelper.insertPrograms(programs)
//                    }
                }
                increaseCalls.call("Live Stream")
            }

            override fun onFailure(call: Call<LiveStream?>, t: Throwable) {
                increaseCalls.call("Live Stream")
                Log.i(TAG, "onFailure: " + t.message)
            }
        })
        val CatchupChannels = apiInterface.getCatchupChannels("b $AuthToken")
        CatchupChannels.enqueue(object : Callback<CatchupChannelsResponse?> {
            override fun onResponse(call: Call<CatchupChannelsResponse?>, response: Response<CatchupChannelsResponse?>) {
                if (response.code() == 200) {
                    val data = response.body()
                    val list = data!!.data
                    dbHelper.insertCatchupChannels(list)
                }
                Log.w(TAG, "***********************Response CAtchupChannels: " + response.body())
                increaseCalls.call("Catchup Channels")
            }

            override fun onFailure(call: Call<CatchupChannelsResponse?>, t: Throwable) {
                increaseCalls.call("Catchup Channels Failed")
                Log.i(TAG, "***************onFailure CATCHUP CHANNELS:********** " + t.message)
            }
        })

        val fingerprint = apiInterface.getFingerprint("b $AuthToken")
        fingerprint.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    Log.i(TAG, "onResponse: fingerprint " + response.body())
                    try {
                        val body = response.body()
                        if (body != null) {
                            val root = JSONObject(body)
                            val data = root.getJSONObject("data")
                            val preferencesEditor = mPreferences.edit()
                            preferencesEditor.putString("fingerPrint", data.toString())
                            preferencesEditor.apply()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.i(TAG, "onResponse: " + e.localizedMessage)
                    }
                }
                increaseCalls.call("FP")
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                increaseCalls.call("FP")
                Log.i(TAG, "onFailure: " + t.message)
            }
        })

        val advertisment = apiInterface.getAdvertisment("b $AuthToken");
        advertisment.enqueue(object : Callback<AdvertismentResponse> {
            override fun onResponse(
                call: Call<AdvertismentResponse>,
                response: Response<AdvertismentResponse>
            ) {
                if(response.code() == 200){
                    response.body()?.let {
                        dbHelperKt.insertAdvertisment(it.data)
                    }
                }
                increaseCalls.call("ADS")
            }

            override fun onFailure(call: Call<AdvertismentResponse>, t: Throwable) {
                Log.w(TAG, "onFailure ADVERTISMENT: "+ t.message)
                increaseCalls.call("ADS")
            }

        })


//        val appStore = apiInterface.appStore
//        appStore.enqueue(object : Callback<AppStoreResponse> {
//            override fun onResponse(
//                call: Call<AppStoreResponse>,
//                response: Response<AppStoreResponse>
//            ) {
//                if(response.code() == 200){
//                    response.body()?.let {
//                        dbHelperKt.insertAppStore(it.data)
//                    }
//                }
//                increaseCalls.call(" APP STORE")
//            }
//
//            override fun onFailure(call: Call<AppStoreResponse>, t: Throwable) {
//                Log.w(TAG, "onFailure AppStore: ${t.localizedMessage}")
//                increaseCalls.call("APP STORE")
//            }
//
//        })
//        val appStoreCat = apiInterface.appStoreCat
//        appStoreCat.enqueue(object : Callback<AppStoreCatResponse> {
//            override fun onResponse(
//                call: Call<AppStoreCatResponse>,
//                response: Response<AppStoreCatResponse>
//            ) {
//                Log.i(TAG, "(APPSCATTORERESPONSE) RP: " + response.code() + " - RB: " + response.body())
//                if(response.code() == 200){
//                    response.body()?.let {
//                        dbHelperKt.insertAppStoreCat(it.data)
//                    }
//                }
//                increaseCalls.call("APP STORE CAT")
//            }
//
//            override fun onFailure(call: Call<AppStoreCatResponse>, t: Throwable) {
//                Log.w(TAG, "onFailure AppStore: ${t.localizedMessage}")
//                increaseCalls.call("APP STORE CAT")
//            }
//
//        })


    }
}