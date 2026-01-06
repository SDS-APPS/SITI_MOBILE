package com.siti.mobile.network.fingerprint

import android.content.SharedPreferences
import android.util.Log
import com.siti.mobile.Interface.ApiInterface
import com.siti.mobile.network.main.contracts.CallApiDB
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FingerprintProviderNetwork(
    private val apiInterface: ApiInterface,
    private val authToken : String,
    private val mPreferences: SharedPreferences
) : CallApiDB {

    private var callsRealized = 0

    fun onFinishFinterprint(onFinish : (String) -> Unit) {
        refreshFingerprint {
            callsRealized++
            if(callsRealized == totalCalls) onFinish("Fingerprint Fetched")
        }
    }

    private fun refreshFingerprint(onRefresh : () -> Unit) {
        val fingerprint = apiInterface.getFingerprint("b $authToken")
        fingerprint.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    try {
                        val body = response.body()
                        if (body != null) {
                            val root = JSONObject(body)
                            val data = root.getJSONObject("data")
                            val preferencesEditor = mPreferences.edit()
                            preferencesEditor.putString("fingerPrint", data.toString())
                            preferencesEditor.apply()
                        }
//                        onRefresh()
                    } catch (e: JSONException) {
//                        onRefresh()
                        e.printStackTrace()
                        Log.i(TAG, "onResponse: " + e.localizedMessage)
                    }
                }
                onRefresh()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                onRefresh()
                Log.i(TAG, "onFailure: " + t.message)
            }
        })
    }

    override val totalCalls: Int
        get() = 1
    override val TAG: String
        get() = "FingerprintProviderNetwork"

}