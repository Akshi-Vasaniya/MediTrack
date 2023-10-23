package com.example.meditrack.homeActivity.medicine.addMedicine

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.lifecycle.ViewModel
import com.example.meditrack.dataModel.*

class AddMedicineViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    var medName: String? = null
    var medImage: String? = null
    var bimapMedImage:Bitmap? = null
    var medInstruction: String? = null
    var medNotes: String? = null
    var dosage: Double? = null
    var medQuantity: Int? = null
    var mfgDate: String? = null
    var expDate: String? = null
    var doctorName: String? = null
    var doctorContact: String? = null
    var selectedTextArray:ArrayList<Pair<Rect,String>>? = null

    val freqTags = MedicineFrequency.values()
    val medTypeTags = MedicineType.values()
    val medicineTimeOfDayType2 = MedicineTimeOfDayType2.values()
    val breakFastTags = arrayOf(
        MedicineTimeOfDayType1.BEFORE_BREAKFAST,
        MedicineTimeOfDayType1.AFTER_BREAKFAST,)
    val launchTags = arrayOf(
        MedicineTimeOfDayType1.BEFORE_LUNCH,
        MedicineTimeOfDayType1.AFTER_LUNCH,)
    val dinnerTags = arrayOf(
        MedicineTimeOfDayType1.BEFORE_DINNER,
        MedicineTimeOfDayType1.AFTER_DINNER,)

    var selectedfreqTags: MedicineFrequency? = null
    var selectedMedicineTimeOfDayType1: ArrayList<MedicineTimeOfDayType1> = ArrayList()
    var selectedMedicineTimeOfDayType2: ArrayList<MedicineTimeOfDayType2> = ArrayList()
    var selectedMedTypeTags: MedicineType? = null

    val weekDayItems = MedicineWeekDay.values()
    var selectedWeekDayItem: MedicineWeekDay? = null

}