package com.siti.mobile.Utils

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import javax.inject.Inject

class ServerChecker @Inject constructor(private val context: Context, private val view : View, private val serverNotReachableView : ImageView) {

    private var wasReachable = true // asumimos que inicialmente está reachable

    private var job: Job? = null

    @Inject
    lateinit var preferences : SharedPreferences

    private var snackbar : Snackbar? = null

    fun startChecking(intervalMillis: Long = 5000) {
        preferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val ipSaved = preferences.getString(KEY_SERVER_IP, "")
                val ip : String = if(ipSaved!!.contains(SERVER_GLOBAL_IP_EMPTY)) {
                    SERVER_GLOBAL_IP_EMPTY
                }else{
                    SERVER_LOCAL_IP_EMPTY
                }
                val reachable = isServerReachable(ip)
                if (!reachable && wasReachable) {
                    // mostrar toast en el hilo principal
                    withContext(Dispatchers.Main) {
                        serverNotReachableView.visibility = View.VISIBLE
                        snackbar = Snackbar.make(view, "Server not reachable", Snackbar.LENGTH_INDEFINITE)
                        snackbar!!.show()
                    }
                }else if(reachable) {
                    snackbar?.dismiss()
                    withContext(Dispatchers.Main) {
                        serverNotReachableView.visibility = View.GONE
                    }
                }
                wasReachable = reachable
                delay(intervalMillis)
            }
        }
    }

    fun stopChecking() {
        job?.cancel()
    }

    private fun isServerReachable(ip: String, timeout: Int = 2000): Boolean {
        return try {
            val address = java.net.InetAddress.getByName(ip)
            address.isReachable(timeout)
        } catch (e: Exception) {
            false
        }
    }
}
