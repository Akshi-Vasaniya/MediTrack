package com.example.meditrack.dataModel.dataClasses

import com.example.meditrack.dataModel.enumClasses.medicine.MedicineFrequency
import com.example.meditrack.dataModel.enumClasses.medicine.MedicineType
import com.example.meditrack.dataModel.enumClasses.medicine.MedicineWeekDay

data class MedicineData(
    var medImage:String?,
    var medName:String,
    var medicineType: MedicineType,
    var dosage:Double,
    var mfgDate:String,
    var expDate:String,
    var medFreq: MedicineFrequency,
    var weekDay:MutableList<MedicineWeekDay>?=null,
    var takeTime:Any,
    var instruction:String?,
    var doctorName:String?,
    var doctorContact:String?,
    var notes:String?,
    var totalQuantity:Int,
    var mediDeleted: String,
    var createdDate:String,
    var createdTime:String)
