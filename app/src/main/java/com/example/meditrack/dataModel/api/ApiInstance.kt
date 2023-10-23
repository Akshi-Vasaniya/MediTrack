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
            .baseUrl("https://meditrackcpproject-8ee22365-80c5-4115-8559-13963d3f2ec0.socketxp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create()
    }
}