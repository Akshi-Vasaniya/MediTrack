package com.example.meditrack.userSession

import java.text.SimpleDateFormat
import java.util.*

object SessionUtils {

    private const val DATE_FORMAT = "dd-MM-yyyy HH:mm:ss"
    private const val SEVEN_DAYS_IN_MILLIS = 7 * 24 * 60 * 60 * 1000L

    fun getSevenDaysExpiryTimestamp(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() + SEVEN_DAYS_IN_MILLIS

        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(Date(calendar.timeInMillis))
    }

    fun isSessionExpired(expiryTimestamp: String): Boolean {
        val currentTimestamp = System.currentTimeMillis()

        return try {
            val expiryDate = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(expiryTimestamp)
            val expiryTimestampInMillis = expiryDate?.time ?: 0

            currentTimestamp > expiryTimestampInMillis
        } catch (e: Exception) {
            e.printStackTrace()
            true // Consider expired if there is an exception (e.g., parsing error)
        }
    }
}