package com.example.meditrack.homeActivity.medicine.addMedicine

import androidx.lifecycle.ViewModel
import com.example.meditrack.dataModel.MedicineFrequency
import com.example.meditrack.dataModel.MedicineTime
import com.example.meditrack.dataModel.MedicineType
import com.example.meditrack.dataModel.MedicineWeekDay

class AddMedicineViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    var medName: String? = null
    var dosage: Double? = null
    var medQuantity: Int? = null
    var mfgDate: String? = null
    var expDate: String? = null
    var doctorName: String? = null
    var doctorContact: String? = null

    val freqTags = mutableListOf(MedicineFrequency.DAILY, MedicineFrequency.WEEKLY)
    val medTypeTags = mutableListOf(MedicineType.Tablets, MedicineType.Liquid)
    val breakFastTags = mutableListOf(
        MedicineTime.BEFORE_BREAKFAST,
        MedicineTime.AFTER_BREAKFAST,)
    val launchTags = mutableListOf(
        MedicineTime.BEFORE_LUNCH,
        MedicineTime.AFTER_LUNCH,)
    val dinnerTags = mutableListOf(
        MedicineTime.BEFORE_DINNER,
        MedicineTime.AFTER_DINNER,)

    var selectedfreqTags: MedicineFrequency? = null
    var selectedbreakFastTags: MedicineTime? = null
    var selectedlaunchTags: MedicineTime? = null
    var selecteddinnerTags: MedicineTime? = null
    var selectedMedTypeTags: MedicineType? = null

    val weekDayItems = arrayOf(MedicineWeekDay.Sunday,MedicineWeekDay.Monday,MedicineWeekDay.Tuesday,MedicineWeekDay.Wednesday,MedicineWeekDay.Thursday,MedicineWeekDay.Friday,MedicineWeekDay.Saturday)
    var selectedWeekDayItem: MedicineWeekDay? = null

}