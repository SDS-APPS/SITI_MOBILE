package com.siti.mobile.Player

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket

class AsyncTaskStartMulticast {

    companion object {
        var g_ChannelStopped = true
        var currentLoopTime: Long? = null
    }

    var isRunning = false
    private val TAG = "AsyncTaskStartMulticast"
    private lateinit var aesEncryptionHelper: AESEncryptionHelper
    val DecryptedStreamAddress = "udp://@192.168.0.100:1234"

    @OptIn(UnstableApi::class)
    fun doInBackground(vararg params: String){
        CoroutineScope(Dispatchers.IO).launch {
            isRunning = true
            val originalMulticastPort = PlayerManager.currentPort
            val g_local_port = 1234
            try {
                val new_local_address = InetAddress.getByName(params[2])
                val sudp = DatagramSocket()
                val p: DatagramPacket

                val originalMulticastIp = params[0].replace("@", "")
                val originalStringPort = originalMulticastPort.toString()
                aesEncryptionHelper = AESEncryptionHelper(originalMulticastIp, originalStringPort)
                aesEncryptionHelper.startDecryptionAES()

                val group = InetAddress.getByName(originalMulticastIp)
                val s = MulticastSocket(originalMulticastPort)
                s.joinGroup(group)
                val inbuffer = ByteArray(1328)
                val revbuffer = ByteArray(1316)
                var i = 0
                var bFound = false
                var bFirstTime = true
                val recv = DatagramPacket(inbuffer, 1328)
                p = DatagramPacket(revbuffer, 1316, new_local_address, g_local_port)
                var countr = 0
                while (!g_ChannelStopped) {
                    try{
                        countr++
                        s.receive(recv)
                        var unAES : ByteArray? = ByteArray(1296)
                        val enAES = ByteArray(1328)
                        System.arraycopy(inbuffer, 0, enAES, 0, 1328)
                        unAES = aesEncryptionHelper.decrypt(enAES)
                        if(unAES == null) continue
                        System.arraycopy(unAES, 0, revbuffer, 0, unAES!!.size)
                        sudp.send(p)
                        continue
                    }catch (e : Exception) {
                        Log.w(TAG, "${e.message}")
                    }
                }
                sudp.close()
                s.leaveGroup(group)

                isRunning = false
            } catch (ex: Exception) {
                println("error Exeption: ${ex.message} - ")
                ex.printStackTrace()
                isRunning = false
            }
        }

    }

}