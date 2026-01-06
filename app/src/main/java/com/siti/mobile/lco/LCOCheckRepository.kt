package com.siti.mobile.lco

import LCOCheckApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object LCOCheckRepository {

    private val api: LCOCheckApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://iptv.panmetro.in/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LCOCheckApi::class.java)
    }

    @JvmStatic
    fun checkLCO(macAddress: String, callback: LCOCheckCallback) {
        val request = LCOCheckRequest(macAddress)
        val call = api.checkLCO(request)

        call.enqueue(object : Callback<LCOCheckResponse> {
            override fun onResponse(
                call: Call<LCOCheckResponse>,
                response: Response<LCOCheckResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { callback.onSuccess(it) }
                        ?: callback.onFailure(Throwable("Response body is null"))
                } else {
                    callback.onFailure(Throwable("Response not successful: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<LCOCheckResponse>, t: Throwable) {
                callback.onFailure(t)
            }
        })
    }
}
