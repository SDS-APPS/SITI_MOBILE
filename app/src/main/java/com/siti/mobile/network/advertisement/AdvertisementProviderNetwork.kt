package com.siti.mobile.network.advertisement

import android.util.Log
import com.siti.mobile.Interface.ApiInterface
import com.siti.mobile.Model.advertisment.AdvertismentResponse
import com.siti.mobile.Utils.DBHelperKt
import com.siti.mobile.network.main.contracts.CallApiDB
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdvertisementProviderNetwork(
    private val apiInterface: ApiInterface,
    private val authToken : String,
    private val dbHelperKt: DBHelperKt
) : CallApiDB {

    private var callsRealized = 0

    fun refreshAdvertisments(onFinish : (String) -> Unit) {
        refrshAdvertisments {
            callsRealized++
            if(callsRealized == totalCalls) onFinish(TAG)
        }
    }

    private fun refrshAdvertisments(onRefresh : () -> Unit) {
        val advertisment = apiInterface.getAdvertisment("b $authToken");
        advertisment.enqueue(object : Callback<AdvertismentResponse> {
            override fun onResponse(
                call: Call<AdvertismentResponse>,
                response: Response<AdvertismentResponse>
            ) {
                //     Log.i(TAG, "(ADVERTISMENT RESPONSE) RP: " + response.code() + " - RB: " + response.body())
                if(response.code() == 200){
                    response.body()?.let {
                        dbHelperKt.insertAdvertisment(it.data)
                    }
                }
                onRefresh()
            }

            override fun onFailure(call: Call<AdvertismentResponse>, t: Throwable) {
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