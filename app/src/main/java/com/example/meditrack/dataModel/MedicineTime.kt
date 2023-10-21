package com.example.meditrack.dataModel

enum class MedicineTime {
    BEFORE_BREAKFAST,
    AFTER_BREAKFAST,
    BEFORE_LUNCH,
    AFTER_LUNCH,
    BEFORE_DINNER,
    AFTER_DINNER;

    override fun toString(): String {
        return when (this) {
            BEFORE_BREAKFAST -> "Before Breakfast"
            AFTER_BREAKFAST -> "After Breakfast"
            BEFORE_LUNCH -> "Before Lunch"
            AFTER_LUNCH -> "After Lunch"
            BEFORE_DINNER -> "Before Dinner"
            AFTER_DINNER -> "After Dinner"
        }
    }

}
