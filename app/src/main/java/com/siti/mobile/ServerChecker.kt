package com.siti.mobile

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.siti.mobile.Utils.KEY_SERVER_IP
import com.siti.mobile.Utils.sharedPrefFile
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@SuppressLint("StaticFieldLeak")
object ServerChecker {

    private lateinit var context: Context
    private var running = false
    private lateinit var mPreferences: SharedPreferences
    private lateinit var serverUrl : String

    fun init(appContext: Context) {
        context = appContext
        mPreferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
        serverUrl = mPreferences.getString(KEY_SERVER_IP, "")?.replace("/apis",  "")?: ""
        if (!running) {
            running = true
            startMonitoring()
        }
    }

    private fun startMonitoring() {
        Thread {
            while (true) {
                checkServer()
                Thread.sleep(10_000)
            }
        }.start()
    }

    private fun checkServer() {
        try {
            trustAllCertificates()
            val url = URL(serverUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) showToast()
        } catch (e: Exception) {
            showToast()
        }
    }

    fun trustAllCertificates() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)

            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showToast() {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "DRM Server Not Reachable", Toast.LENGTH_SHORT).show()
        }
    }
}
