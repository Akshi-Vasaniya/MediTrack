package com.example.meditrack.homeActivity.medicine.addMedicine

import androidx.lifecycle.ViewModel
import com.example.meditrack.dataModel.MedicineFrequency

class AddMedicineViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    var medName: String? = null
    var dosage: String? = null
    var medFreq: MedicineFrequency? = null
    var mfgDate: String? = null
    var expDate: String? = null

    val freqTags = mutableListOf("Daily", "Weekly")
    val breakFastTags = mutableListOf(
        "Before Breakfast",
        "After Breakfast",)
    val launchTags = mutableListOf(
        "Before Launch",
        "After Launch",)
    val dinnerTags = mutableListOf(
        "Before Dinner",
        "After Dinner",)

    var selectedfreqTags: String? = null
    var selectedbreakFastTags: String? = null
    var selectedlaunchTags: String? = null
    var selecteddinnerTags: String? = null


}