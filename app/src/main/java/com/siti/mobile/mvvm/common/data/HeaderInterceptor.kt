package com.siti.mobile.mvvm.common.data

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull

const val admin_token = "b eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZG1pbG..."

class HeaderInterceptor(private val token: String? = null) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", token ?: admin_token)
            .build()

        return try {
            chain.proceed(request)
        } catch (e: Exception) {
            // Log para depuración
            Log.e("HeaderInterceptor", "No hay conexión o server unreachable", e)

            // Devuelve una respuesta ficticia sin lanzar excepción
            Response.Builder()
                .request(chain.request())
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(503) // Service Unavailable
                .message("No connection")
                .body(ResponseBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    "Server not reachable or no internet"
                ))
                .build()
        }
    }
}
