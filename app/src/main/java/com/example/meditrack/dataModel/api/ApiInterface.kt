package com.example.meditrack.dataModel.api

import com.example.meditrack.dataModel.dataClasses.SearchItemData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


interface ApiInterface {
    @GET("/{query}")
    fun listDocument(@Path("query") query: String?): Call<List<SearchItemData?>?>?
}