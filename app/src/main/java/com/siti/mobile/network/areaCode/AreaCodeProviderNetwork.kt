package com.siti.mobile.network.areaCode

import android.content.SharedPreferences
import android.util.Log
import com.siti.mobile.Interface.ApiInterface
import com.siti.mobile.Utils.KEY_AREA_CODE
import com.siti.mobile.mvvm.common.data.AreaCodeResponse
import com.siti.mobile.network.main.contracts.CallApiDB
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AreaCodeProviderNetwork(
    private val apiInterface: ApiInterface,
    private val authToken : String,
    private val mPreferences: SharedPreferences
) : CallApiDB {

    private var callsRealized = 0

    fun refresh(onFinish : (String) -> Unit) {
        this@AreaCodeProviderNetwork.run {
            callsRealized++
            if(callsRealized == totalCalls) onFinish(TAG)
        }
    }

    private fun run(onRefresh : () -> Unit) {
        val areaCode = apiInterface.getAreaCode("b $authToken");
        areaCode.enqueue(object : Callback<AreaCodeResponse> {
            override fun onResponse(
                call: Call<AreaCodeResponse>,
                response: Response<AreaCodeResponse>
            ) {
                //     Log.i(TAG, "(ADVERTISMENT RESPONSE) RP: " + response.code() + " - RB: " + response.body())
                if(response.code() == 200){
                   mPreferences.edit().putString(KEY_AREA_CODE, response.body()?.data?.areaCode).apply()
                }
                onRefresh()
            }

            override fun onFailure(call: Call<AreaCodeResponse>, t: Throwable) {
                Log.w(TAG, "onFailure ADVERTISMENT: "+ t.message)
                onRefresh()
            }

        })
    }

    override val totalCalls: Int
        get() = 1
    override val TAG: String
        get() = "AdvertismentProviderNetwork"

}