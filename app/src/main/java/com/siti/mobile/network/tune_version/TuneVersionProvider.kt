package com.siti.mobile.network.tune_version

import com.siti.mobile.Interface.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val TAG = "TuneVersionProvider"

object TuneVersionProvider {
    fun get(apiInterface : ApiInterface, authToken : String, onResponse : (TuneVersionResponse) -> Unit) {
        val call = apiInterface.getTuneVersion(authToken)
        call.enqueue(object : Callback<TuneVersionResponse> {
            override fun onResponse(
                call: Call<TuneVersionResponse>,
                response: Response<TuneVersionResponse>
            ) {
                val body = response.body()
                if(body != null){
                    onResponse(body)
                }else{
                    onResponse(TuneVersionResponse(emptyList(), "Error 500 Internal Error", "tune version", status = "-1"))
                }
            }

            override fun onFailure(call: Call<TuneVersionResponse>, t: Throwable) {
                onResponse(TuneVersionResponse(emptyList(), t.localizedMessage, "${t.message}", status = "-1"))
            }

        })
    }
}