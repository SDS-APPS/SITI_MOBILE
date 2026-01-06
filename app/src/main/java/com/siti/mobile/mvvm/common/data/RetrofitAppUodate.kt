package com.siti.mobile.mvvm.common.data

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitAppUodate(context : Context) {

    fun getRetrofit() : Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}