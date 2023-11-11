package com.example.meditrack.dataModel.api

import com.example.meditrack.dataModel.dataClasses.SearchItemData
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


interface ApiInterface {
    @GET("/{query} medicine")
    fun listDocument(@Path("query") query: String?): Call<List<SearchItemData?>?>?

    @GET("/medicine_web_scrap/{medicinename}")
    fun insertDocument(@Path("medicinename") medicinename: String?): Call<JsonElement>

    @GET("/spell_correct/{medicinename}")
    fun spellCorrect(@Path("medicinename") medicinename: String?): Call<JsonElement>

}