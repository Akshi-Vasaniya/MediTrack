package com.example.meditrack.dataModel.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit


object ApiInstance {
    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .build()
    val api: ApiInterface by lazy{
        Retrofit
            .Builder()
            .baseUrl("https://meditrackcpproject-3f0956db-b449-4328-a5ba-e52a1fa5d377.socketxp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create()
    }
}