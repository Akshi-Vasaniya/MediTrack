package com.example.meditrack.dataModel.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


interface ApiInterface {
    @GET("/{query}")
    fun listDocument(@Path("query") query: String?): Call<List<ApiData?>?>?
}