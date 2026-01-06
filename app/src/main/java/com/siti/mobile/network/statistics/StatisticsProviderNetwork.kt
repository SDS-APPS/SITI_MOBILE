package com.siti.mobile.network.statistics

import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.siti.mobile.Interface.ApiInterface
import com.siti.mobile.Model.GeneralResponse
import com.siti.mobile.Utils.KEY_AUTH_TOKEN
import com.siti.mobile.Utils.KEY_INTERVAL
import com.siti.mobile.Utils.KEY_MAC
import com.siti.mobile.Utils.KEY_USER_ID
import com.siti.mobile.mvvm.common.data.post.OnlineCustomerRequest
import com.siti.mobile.network.main.contracts.CallApiDB
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.InetAddress
import java.net.NetworkInterface
import javax.inject.Inject

class StatisticsProviderNetwork @Inject constructor(
    private val mPreferences : SharedPreferences,
    private val apiInterface: ApiInterface
) : CallApiDB {

    companion object {
        private val handler = Handler(Looper.getMainLooper())
    }

    private val refreshRunnable = object : Runnable {
        override fun run() {
            val customerId = mPreferences.getInt(KEY_USER_ID, 0)
            val mac = mPreferences.getString(KEY_MAC, "") ?: ""
            val channelId = mPreferences.getString("SELECTED_CHANNEL_ID", "0")?.toLongOrNull() ?: 0
            val interval = mPreferences.getInt(KEY_INTERVAL, 5).toLong()

            refreshOnlineUserDate(
                customerId = customerId,
                mac = mac,
                lastViewedChannelId = channelId
            )

//            handler.postDelayed(this, 5 * 60 * 1000)
            handler.postDelayed(this, interval * 60 * 1000)
        }
    }

    private var callsRealized = 0

    fun refreshOnlineUser() {
        handler.removeCallbacksAndMessages(null)
        handler.post(refreshRunnable)

//        val customerId = mPreferences.getInt(KEY_USER_ID, 0)
//        val mac = mPreferences.getString(KEY_MAC, "") ?: ""
//        val channelId = mPreferences.getString("SELECTED_CHANNEL_ID", "0")?.toLongOrNull() ?: 0;
//
//        refreshOnlineUserDate(
//            customerId = customerId,
//            mac = mac,
//            lastViewedChannelId = channelId
//        )
    }

    private fun getCurrentFormattedDate(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return current.format(formatter)
    }

    private fun getPrivateIp(): String? {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
            val addresses = intf.inetAddresses
            for (addr in addresses) {
                if (!addr.isLoopbackAddress && addr is InetAddress) {
                    val hostAddress = addr.hostAddress
                    if (hostAddress.indexOf(':') < 0) { // Ignorar direcciones IPv6
                        return hostAddress
                    }
                }
            }
        }
        return null
    }

    private fun getFullDeviceName(): String {
        return Build.MANUFACTURER + " " + Build.MODEL
    }

    private fun refreshOnlineUserDate(customerId: Int, mac: String, lastViewedChannelId: Long) {
        val model = OnlineCustomerRequest(
            customerId = customerId,
            mac = mac,
            channelId = lastViewedChannelId,
            lastOnline = getCurrentFormattedDate(),
            ip = getPrivateIp() ?: "",
            deviceModel = getFullDeviceName()
        )
        val authToken = mPreferences.getString(KEY_AUTH_TOKEN, "")
        val advertisment = apiInterface.refreshOnlineData("b $authToken", model);
        advertisment.enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                Log.i(TAG, "(STATISTICS RESPONSE) RP: " + response.code() + " - RB: " + response.body())
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                Log.w(TAG, "onFailure POST STATISTICS: "+ t.message)
            }

        })
    }

    override val totalCalls: Int
        get() = 1
    override val TAG: String
        get() = "AdvertismentProviderNetwork"

}