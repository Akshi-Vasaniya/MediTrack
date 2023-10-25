package com.example.meditrack.dataModel.enumClasses.medicine

enum class MedicineTimeOfDayType1(val description: String,val time:String) {
    BEFORE_BREAKFAST("Before Breakfast","7:00"),
    AFTER_BREAKFAST("After Breakfast","8:00"),
    BEFORE_LUNCH("Before Lunch","12:00"),
    AFTER_LUNCH("After Lunch","14:00"),
    BEFORE_DINNER("Before Dinner","17:00"),
    AFTER_DINNER("After Dinner","21:00")

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
