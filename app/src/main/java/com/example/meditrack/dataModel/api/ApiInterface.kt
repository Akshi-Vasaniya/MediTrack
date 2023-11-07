package com.example.meditrack.dataModel.api

import com.example.meditrack.dataModel.dataClasses.SearchItemData
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


interface ApiInterface {
    @GET("/{query} medicine")
    fun listDocument(@Path("query") query: String?): Call<List<SearchItemData?>?>?

//    @GET("/medname/{query}")
//    fun insertDocument(@Path("query") query: String?): JSONObject
}