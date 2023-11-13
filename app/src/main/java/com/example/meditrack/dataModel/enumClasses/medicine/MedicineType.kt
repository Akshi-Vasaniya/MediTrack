package com.example.meditrack.dataModel.enumClasses.medicine

import android.content.Context
import com.example.meditrack.R

enum class MedicineType(private val stringResourceId: Int) {
    Topical(R.string.Topical_Dec),
    Drops(R.string.Drops_Dec),
    Inhaler(R.string.Inhaler_Dec),
    Injection(R.string.Injection_Dec),
    Implants_or_Patches(R.string.Implants_or_Patches_Dec),
    Tablets(R.string.Tablets_Dec),
    Capsules(R.string.Capsules_Dec),
    Liquid(R.string.Liquid_Dec);

    // Access the description from strings.xml using the context
    fun getDescription(context: Context): String {
        return context.getString(stringResourceId)
    }
}