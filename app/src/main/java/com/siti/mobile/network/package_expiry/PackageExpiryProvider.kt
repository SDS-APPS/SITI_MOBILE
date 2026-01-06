package com.siti.mobile.network.package_expiry

import android.util.Log
import com.siti.mobile.Interface.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "PackageExpiryProvider"

object PackageExpiryProvider {

    fun get(apiInterface : ApiInterface, authToken : String, onResponse : (PackageExpiryResponse) -> Unit) {
        val call = apiInterface.getPackageExpiry(authToken)
        call.enqueue(object : Callback<PackageExpiryResponse> {
            override fun onResponse(
                call: Call<PackageExpiryResponse>,
                response: Response<PackageExpiryResponse>
            ) {
                val body = response.body()
                Log.w(TAG, "Body: $body")
                if(body != null) {
                    onResponse(body)
                }else{
                    onResponse(PackageExpiryResponse(emptyList(), "Body is null", "Body null...", status = "-1"))
                }

            }

            override fun onFailure(call: Call<PackageExpiryResponse>, t: Throwable) {
                onResponse(PackageExpiryResponse(emptyList(), "${t.localizedMessage}", "${t.message}", status = "-1"))
            }

        })
    }

}