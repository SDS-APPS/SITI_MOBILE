package com.siti.mobile.Utils

import android.graphics.Color
import android.widget.TextView
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject

class LatencyHelper  @Inject constructor(){

    private var job: Job? = null

    /**
     * Inicia la medición continua de latencia.
     * @param ip Dirección IP (ej: "8.8.8.8")
     * @param puerto Puerto para conexión (por defecto 443)
     * @param textView TextView donde se muestra el resultado
     * @param intervaloMs Intervalo entre mediciones (por defecto 5000ms)
     */
    fun startChecking(ip: String, puerto: Int = 443, textView: TextView, intervaloMs: Long = 5000L) {
        stopChecking() // Si ya hay un Job corriendo, lo cancelamos antes

        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val startTime = System.currentTimeMillis()
                    val socket = Socket()
                    socket.connect(InetSocketAddress(ip, puerto), 2000)
                    val endTime = System.currentTimeMillis()
                    socket.close()

                    val latencia = endTime - startTime

                    withContext(Dispatchers.Main) {
                        textView.text = "$latencia ms"
                        when {
                            latencia < 50 -> textView.setTextColor(Color.GREEN)
                            latencia < 150 -> textView.setTextColor(Color.YELLOW)
                            else -> textView.setTextColor(Color.RED)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        textView.text = "Error"
                        textView.setTextColor(Color.GRAY)
                    }
                }

                delay(intervaloMs)
            }
        }
    }

    /**
     * Detiene la medición de latencia.
     */
    fun stopChecking() {
        job?.cancel()
        job = null
    }
}
