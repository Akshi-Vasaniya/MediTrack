package com.example.meditrack.dataModel

enum class MedicineType {
    Tablets,
    Liquid;

    fun getNotation():String
    {
        when(this){
            Tablets -> return "mg"
            Liquid -> return "ml"
        }
    }
}