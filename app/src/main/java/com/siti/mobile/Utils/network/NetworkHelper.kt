package com.siti.mobile.Utils.network

import android.content.Context
import java.net.Inet4Address
import java.net.NetworkInterface

object  NetworkHelper {
    fun getEthernetPrivateIP(context: Context): String? {

        val networkInterface = NetworkInterface.getNetworkInterfaces().toList()
        networkInterface.forEach {
            it.interfaceAddresses.forEach {iadd ->
                if(iadd.address is Inet4Address) {
                    return iadd.address.hostAddress
                }
            }
        }

        return null
    }
}