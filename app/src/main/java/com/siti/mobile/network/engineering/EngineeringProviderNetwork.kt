package com.siti.mobile.network.engineering

import android.content.SharedPreferences
import android.util.Log
import com.siti.mobile.Interface.ApiInterface
import com.siti.mobile.Utils.KEY_AREA_CODE
import com.siti.mobile.Utils.KEY_AUDIT_MODE
import com.siti.mobile.Utils.KEY_LOW_PROFILE
import com.siti.mobile.Utils.KEY_PLAY_WITH_DRM_SOURCE
import com.siti.mobile.Utils.KEY_USER_ID
import com.siti.mobile.network.main.contracts.CallApiDB
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EngineeringProviderNetwork(
    private val apiInterface: ApiInterface,
    private val authToken : String,
    private val mPreferences : SharedPreferences,
) : CallApiDB {

    private var callsRealized = 0

    fun refreshAll(onFinish : (String) -> Unit) {
        refreshEngineering {
            callsRealized++
            if(callsRealized == totalCalls) onFinish(TAG)
        }
    }

    private fun refreshEngineering(onRefresh : () -> Unit) {
        val engineering = apiInterface.getUserEngineering("b $authToken");
        engineering.enqueue(object : Callback<EngineeringResponse> {
            override fun onResponse(
                call: Call<EngineeringResponse>,
                response: Response<EngineeringResponse>
            ) {
                response.body()?.let {
                    saveNewEngineeringSettings(it.data)
                }
                onRefresh()
            }

            override fun onFailure(call: Call<EngineeringResponse>, t: Throwable) {
                Log.w(TAG, "onFailure ADVERTISMENT: "+ t.message)
                onRefresh()
            }

        })
    }

    fun saveNewEngineeringSettings(engineeringModelList: List<EngineeringModel>) {
        if(engineeringModelList.isNotEmpty()) {
            val engineering = engineeringModelList.first()
            val userId = mPreferences.getInt(KEY_USER_ID, 0)
            val areaCode = mPreferences.getString(KEY_AREA_CODE, "")
            if((engineering.user_id != null && userId != null && engineering.user_id == userId.toString()) || (areaCode != null && engineering.area_code != null && areaCode == engineering.area_code)) {
                val edit = mPreferences.edit();
                edit.putBoolean(KEY_AUDIT_MODE, engineering.audit_mode == "1")
                edit.putBoolean(KEY_LOW_PROFILE, engineering.low_profile_mode == "1")
                edit.putBoolean(KEY_PLAY_WITH_DRM_SOURCE, engineering.stream_type == "1")
                edit.putString(KEY_AREA_CODE, engineering.area_code)
                edit.apply()
            }

        }
    }

    override val totalCalls: Int
        get() = 1
    override val TAG: String
        get() = "EngineeringProviderNetwork"

}