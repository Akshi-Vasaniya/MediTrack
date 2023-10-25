package com.example.meditrack.dataModel.enumClasses.medicine

enum class MedicineTimeOfDayType1(val description: String) {
    BEFORE_BREAKFAST("Before Breakfast"),
    AFTER_BREAKFAST("After Breakfast"),
    BEFORE_LUNCH("Before Lunch"),
    AFTER_LUNCH("After Lunch"),
    BEFORE_DINNER("Before Dinner"),
    AFTER_DINNER("After Dinner")

}

enum class MedicineTimeOfDayType2(val description: String) {
    EARLY_MORNING("Early Morning"),
    MORNING("Morning"),
    NOON("Noon"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    NIGHT("Night"),
    LATE_NIGHT("Late Night")
}
