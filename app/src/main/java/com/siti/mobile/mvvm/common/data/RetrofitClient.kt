package com.siti.mobile.mvvm.common.data

import android.content.Context
import com.siti.mobile.Utils.KEY_AUTH_TOKEN
import com.siti.mobile.Utils.sharedPrefFile
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.net.ssl.HostnameVerifier

class RetrofitClientEpg @Inject constructor(@ApplicationContext context : Context)
{

    val mPreferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

    fun getRetrofit(url : String, isForChannel: Boolean) : Retrofit?
    {

        val client = getClient(isForChannel)

        client?.let {
            return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        return null
    }

    private fun getClient(isForChannel : Boolean = false) : OkHttpClient?{
        try{
            if(isForChannel)
            {
                return OkHttpClient.Builder()
                    .hostnameVerifier(HostnameVerifier { hostname, session ->  true})
                    .addInterceptor(HeaderInterceptor())
                    .build()
            }else{
                val authToken = mPreferences.getString(KEY_AUTH_TOKEN, "")
                return OkHttpClient.Builder()
                    .hostnameVerifier(HostnameVerifier { hostname, session ->  true})
                    .addInterceptor(HeaderInterceptor("b $authToken"))
                    .build()
            }
        }catch (e : Exception)
        {
            return null
        }

    }
}
