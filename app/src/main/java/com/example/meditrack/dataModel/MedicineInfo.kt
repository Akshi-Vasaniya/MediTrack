package com.example.meditrack.dataModel

data class MedicineInfo(
    var medImage:String="NA",
    var medName:String,
    var medicineType: MedicineType,
    var dosage:Double,
    var mfgDate:String,
    var expDate:String,
    var medFreq:MedicineFrequency,
    var weekDay:MedicineWeekDay?=null,
    var takeTime:MedicineTime,
    var instruction:String="NA",
    var doctorName:String="NA",
    var notes:String="NA",
    var totalQuantity:Int,
    var notation:String,)
