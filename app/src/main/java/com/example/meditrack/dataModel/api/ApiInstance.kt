package com.example.meditrack.dataModel.api

import com.example.meditrack.homeActivity.home.HomeFragment
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit


object ApiInstance {
    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(120, TimeUnit.SECONDS)
        .connectTimeout(120, TimeUnit.SECONDS)
        .build()
    val api: ApiInterface by lazy{
        Retrofit
            .Builder()
            .baseUrl("https://friendly-adapted-gobbler.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create()
    }
}