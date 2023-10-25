package com.example.meditrack.dataModel

data class MedicineInfo(
    var medImage:String?,
    var medName:String,
    var medicineType: MedicineType,
    var dosage:Double,
    var mfgDate:String,
    var expDate:String,
    var medFreq:MedicineFrequency,
    var weekDay:MutableList<MedicineWeekDay>?=null,
    var takeTime:Any,
    var instruction:String?,
    var doctorName:String?,
    var doctorContact:String?,
    var notes:String?,
    var totalQuantity:Int)
