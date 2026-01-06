package com.siti.mobile.Utils

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.siti.mobile.mvvm.fullscreen.view.PlayerScreen
import io.socket.client.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket

//http://192.168.32.1:8088
const val TESTING_URL_HTTP = "http://192.168.32.1:8088/livelan/stream.m3u8"
const val TAG = "SocketUtilReconnect"

interface ConnectToStream {
    fun connect()
}

interface NotSignal{
    fun run()
}

var counter = 0
var counterReconnectUdp = 0;

@OptIn(UnstableApi::class)
fun connectToStream(url: String, connect: ConnectToStream, notSignal: NotSignal) {
    if(PlayerScreen.actualUrl != url) return;
    counter++
    if(counter > 2){
        counter = 0;
        CoroutineScope(Dispatchers.Main).launch {
            notSignal.run()
        }
        return;
    }
    Log.i(TAG, "ConnectToStreamCalled + $url")
    var portHttp : Int? = null
    if(url[0] == 'h') portHttp = 443
    CoroutineScope(Dispatchers.IO).launch {
        try {
            var isUdp = false;
            val socket = Socket()
            if(url.contains("udp://")) {
                isUdp = true;
                counterReconnectUdp++;
                url.replace("udp://@", "").split(":")
                url.replace("udp://@", "").split(":")
            }
            if(!isUdp){
                val urlSplit =
                    if (url.contains("https://")) url.replace("https://", "").split(":")
                    else url.replace("http://", "").split(":")


                Log.i(TAG, "URL SPLIT + $urlSplit")

                if(urlSplit.size < 2){
                    Log.i(TAG, "RETURNED + $urlSplit")
                    return@launch
                }
                val ip = if(urlSplit[0].contains("/")){
                    urlSplit[0].split("/")[0]
                }else{
                    urlSplit[0]
                }
                val port = if(urlSplit[1].contains("p=alta@")){
                    urlSplit[1].split("/")[0].replace("p=alta@", "")
                }else if(urlSplit[1].contains("/")){
                    urlSplit[1].split("/")[0]
                }else if(urlSplit.isNotEmpty()){
                    urlSplit[1]
                }else{
                    "4000"
                }
                val finalPort = portHttp ?: port.toInt()
                Log.i(TAG, "IP: $ip PORT: $finalPort")
                socket.connect(InetSocketAddress(ip, finalPort), 5000)
                if (socket.isConnected) {
                    Log.i(TAG, "IS CONNECTED")
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(7500)
                        socket.close()
                        connect.connect()
                    }
                } else {
                    Log.i(TAG, "NOT CONNECTED")
                    counter++
                    delay(1000)
                    connectToStream(url, connect, notSignal)

                }
            }else{
                CoroutineScope(Dispatchers.Main).launch {
                  //  delay(7500)
                    if(counterReconnectUdp <= 2){
                        connect.connect()
                    }else{
                        counterReconnectUdp = 0;
                        notSignal.run()
                    }

                }
            }


        } catch (e: Exception) {
            counter++
            val message: String? = e.message
            Log.i(TAG, "Exception: ${e.localizedMessage} Cause: ${e.cause} - \n $e")
            delay(1000)
            connectToStream(url, connect, notSignal)
        }
    }
}

interface StateChangedServer{
    fun stateChanged(value : Boolean)
}

fun checkStateServer(address: String?, port : Int?, state: StateChangedServer?) {
    if(address == null || port == null || state == null) return
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val socket = Socket()
            val newAddress =if (address.contains("https://")) address.replace("https://", "")
                    else address.replace("http://", "")

            val ip =
                if(newAddress.contains(":")) newAddress.split(":")[0]
                else if(newAddress.contains("/")) newAddress.split("/")[0]
                else newAddress

            val opts = IO.Options()
          //  opts.transports = arrayOf(WebSocket.NAME)
            opts.timeout = -1
            opts.callFactory = SocketSingleton.getOkHttpClientTrust()
            opts.webSocketFactory = SocketSingleton.getOkHttpClientTrust()

            socket.connect(InetSocketAddress(ip, 443), 5000)
            if (socket.isConnected) {
                CoroutineScope(Dispatchers.Main).launch {
                    state.stateChanged(true)
                    socket.close()
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    state.stateChanged(false)
                }
            }

        } catch (e: Exception) {
            val message: String? = e.message
            Log.w(TAG, "$message")
            CoroutineScope(Dispatchers.Main).launch {
                state.stateChanged(false)
            }
        }
    }
}