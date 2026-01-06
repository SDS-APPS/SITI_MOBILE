package com.siti.mobile.network

import android.content.SharedPreferences
import com.siti.mobile.Interface.ApiInterface
import com.siti.mobile.Utils.KEY_EXP_DATE
import com.siti.mobile.network.keys.NetworkPackageKeys
import com.siti.mobile.network.package_expiry.PackageExpiryProvider
import com.siti.mobile.network.package_expiry.PackageExpiryResponse
import com.siti.mobile.network.tune_version.TuneVersionProvider
import com.siti.mobile.network.tune_version.TuneVersionResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckPackagesUpdates(
    private val preferences: SharedPreferences,
    private val apiInterface: ApiInterface,
    private val authToken : String) {
    fun call(onPackageExpired : (String) -> Unit, onApiUpdated : (List<ProvidersService>) -> Unit, onResultOk : () -> Unit) {
        getTuneVersionAndExpirityDate { tuneVersionResponse, packageExpiryResponse ->

//            if(tuneVersionResponse.data.isEmpty() || packageExpiryResponse.data.isEmpty()) {
//                onResultOk()
//                return@getTuneVersionAndExpirityDate
//            }

            val currentExpDate = preferences.getString(KEY_EXP_DATE, "0");
            val expDateNetwork = packageExpiryResponse.data;

            if(currentExpDate != null){
                if(expDateNetwork.isNotEmpty()){
                    val newExpDate = expDateNetwork.first()
                    if(isPackageExpired(currentExpDate, newExpDate.endDate)) {

                        onPackageExpired(newExpDate.endDate)
                        return@getTuneVersionAndExpirityDate
                    }
                }

            }


            val listUpdates  = mutableListOf<ProvidersService>()

            if(tuneVersionResponse.data.isNotEmpty()){
                val dataTuneVersion = tuneVersionResponse.data.first()

                val edit = preferences.edit()
                edit.putInt(NetworkPackageKeys.LIVE_TV, dataTuneVersion.livetv)
                edit.putInt(NetworkPackageKeys.VOD, dataTuneVersion.vod)
                edit.putInt(NetworkPackageKeys.SOD, dataTuneVersion.sod)
                edit.putInt(NetworkPackageKeys.MOD, dataTuneVersion.mod)
                edit.apply()
            }
            var oneIsUpdated = false

//            if(currentVersionLiveTV < dataTuneVersion.livetv) {
                listUpdates.add(ProvidersService.LIVE_TV)
                oneIsUpdated= true
//            }

            listUpdates.add(ProvidersService.ENGINEERING)
            listUpdates.add(ProvidersService.AREA_CODE)
            listUpdates.add(ProvidersService.ADVERTISMENT)
            listUpdates.add(ProvidersService.FINGERPRINT)



            if(oneIsUpdated) {
                onApiUpdated(listUpdates)
            }else{
                onResultOk()
            }
        }
    }

    private fun isPackageExpired(cachedDate : String, networkDates : String) : Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

        val cachedDateFormated = Date(cachedDate.toLong() * 1000)
        val networkDateFormated = dateFormat.parse(networkDates)

        if(networkDateFormated != null){
            return cachedDateFormated > networkDateFormated
        }

        return false
    }

    private fun getTuneVersionAndExpirityDate(onResponse : (TuneVersionResponse, PackageExpiryResponse) -> Unit) {
        var tuneVersionResponse : TuneVersionResponse? = null
        var packageExpiryResponse : PackageExpiryResponse? = null

        TuneVersionProvider.get(apiInterface, authToken) {
            tuneVersionResponse = it
            packageExpiryResponse?.let {packageExpiryRep ->
                onResponse(tuneVersionResponse!!, packageExpiryRep)
            }
        }
        PackageExpiryProvider.get(apiInterface, authToken) {
            packageExpiryResponse = it
            tuneVersionResponse?.let {tuneVersionRep ->
                onResponse(tuneVersionRep, packageExpiryResponse!!)
            }
        }
    }
}