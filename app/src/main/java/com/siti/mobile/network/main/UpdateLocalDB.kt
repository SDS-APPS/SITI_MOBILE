package com.siti.mobile.network.main

import android.app.Activity
import android.content.Context
import com.siti.mobile.Interface.ApiInterface
import com.siti.mobile.Utils.*
import com.siti.mobile.network.CheckPackagesUpdates
import com.siti.mobile.network.ProvidersService
import com.siti.mobile.network.advertisement.AdvertisementProviderNetwork
import com.siti.mobile.network.areaCode.AreaCodeProviderNetwork
import com.siti.mobile.network.defaultProviderServices
import com.siti.mobile.network.engineering.EngineeringProviderNetwork
import com.siti.mobile.network.fingerprint.FingerprintProviderNetwork
import com.siti.mobile.network.keys.NetworkPackageKeys
import com.siti.mobile.network.main.live_tv.LiveTVProviderNetwork
import com.siti.mobile.network.tune_version.TuneVersionProvider

private const val TAG = "UPDATELOCALDB"

class UpdateLocalDB(
    private val activity : Activity,
    private val dbHelperKt: DBHelperKt,
    private val dbHelper: DBHelper) {

    private val mPreferences =  activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
    private lateinit var serverIp : String
    private lateinit var authToken: String
    private lateinit var apiInterface: ApiInterface
    var totalTalls : Int = 0




    fun updateLocalDB(increaseCalls : IncreaseCalls) {
        serverIp = mPreferences.getString(KEY_SERVER_IP, PREFERENCES_STRING_DEFAULT_VALUE)!!
        authToken = mPreferences.getString(KEY_AUTH_TOKEN, PREFERENCES_STRING_DEFAULT_VALUE)!!
        apiInterface = RetrofitClient.getClient(serverIp).create(
            ApiInterface::class.java
        )

//        val statisticsProviderNetwork = StatisticsProviderNetwork(mPreferences = mPreferences, apiInterface = apiInterface)
//        statisticsProviderNetwork.refreshOnlineUser()

        val firstLoad = mPreferences.getBoolean(NetworkPackageKeys.FIRST_LOAD, true);
        if (firstLoad) {
            mPreferences.edit().putBoolean(NetworkPackageKeys.FIRST_LOAD, false).apply()
            totalTalls = defaultProviderServices.size
            defaultProviderServices.forEach {
                updateDBByServiceName(it) { callName ->
                    increaseCalls.call(callName)
                }
            }
            TuneVersionProvider.get(apiInterface, "b $authToken") {dataList ->
                if(dataList.data.isEmpty()) return@get

                val dataTuneVersion = dataList.data.first()
                val edit = mPreferences.edit()
                edit.putInt(NetworkPackageKeys.LIVE_TV, dataTuneVersion.livetv)
                edit.putInt(NetworkPackageKeys.VOD, dataTuneVersion.vod)
                edit.putInt(NetworkPackageKeys.SOD, dataTuneVersion.sod)
                edit.putInt(NetworkPackageKeys.MOD, dataTuneVersion.mod)
                edit.apply()
            }
        } else {

            val checkUpdates = CheckPackagesUpdates(mPreferences, apiInterface, "b $authToken")

            checkUpdates.call(
                onPackageExpired = {
                    totalTalls = defaultProviderServices.size
                    defaultProviderServices.forEach {
                        updateDBByServiceName(it) { callName ->
                            increaseCalls.call(callName)
                        }
                    }
                },
                onApiUpdated = { servicesUpdates ->
                    totalTalls = servicesUpdates.size
                    servicesUpdates.forEach {
                        updateDBByServiceName(it) { callName ->
                            increaseCalls.call(callName)
                        }
                    }
                },
                onResultOk = {
                    increaseCalls.call("Result OK, Not required update.")
                }
            )
        }
    }


    private fun updateDBByServiceName(
            providerService : ProvidersService,
            increaseCall : (String) -> Unit) {
        when(providerService) {
            ProvidersService.LIVE_TV -> {
                val liveTVProvider = LiveTVProviderNetwork(apiInterface = apiInterface, authToken = authToken, dbHelper = dbHelper, dbHelperKt)
                liveTVProvider.refreshAllLiveTV {
                    increaseCall(it)
                }
            }
            ProvidersService.ADVERTISMENT -> {
                val advertismentProviderNetwork = AdvertisementProviderNetwork(apiInterface = apiInterface, authToken = authToken, dbHelperKt = dbHelperKt)
                advertismentProviderNetwork.refreshAdvertisments {
                    increaseCall(it)
                }
            }
            ProvidersService.FINGERPRINT -> {
                val fingerprintProviderNetwork = FingerprintProviderNetwork(apiInterface = apiInterface, authToken = authToken, mPreferences)
                fingerprintProviderNetwork.onFinishFinterprint {
                    increaseCall(it)
                }
            }

            ProvidersService.ENGINEERING -> {
                val engineeringProviderNetwork = EngineeringProviderNetwork(apiInterface = apiInterface, authToken = authToken, mPreferences = mPreferences)
                engineeringProviderNetwork.refreshAll {
                    increaseCall(it)
                }
            }
            ProvidersService.AREA_CODE -> {
                val areaCodeProviderNetwork = AreaCodeProviderNetwork(mPreferences = mPreferences, apiInterface = apiInterface, authToken = authToken, )
                areaCodeProviderNetwork.refresh {
                    increaseCall(it)
                }
            }
        }
    }

}