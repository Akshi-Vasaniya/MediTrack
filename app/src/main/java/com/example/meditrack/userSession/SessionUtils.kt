package com.example.meditrack.userSession

import com.example.meditrack.utility.UtilsFunctions.Companion.DATE_TIME_FORMAT
import java.text.SimpleDateFormat
import java.util.*

object SessionUtils {
    private const val SEVEN_DAYS_IN_MILLIS = 7 * 24 * 60 * 60 * 1000L

    fun getSevenDaysExpiryTimestamp(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() + SEVEN_DAYS_IN_MILLIS

        val dateFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
        return dateFormat.format(Date(calendar.timeInMillis))
    }

    fun isSessionExpired(expiryTimestamp: String): Boolean {
        val currentTimestamp = System.currentTimeMillis()

        return try {
            val expiryDate = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()).parse(expiryTimestamp)
            val expiryTimestampInMillis = expiryDate?.time ?: 0

            currentTimestamp > expiryTimestampInMillis
        } catch (e: Exception) {
            e.printStackTrace()
            true // Consider expired if there is an exception (e.g., parsing error)
        }
    }

    // Function to get the current timestamp in a specified format
    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Function to get the login timestamp
    fun getLoginTimestamp(): String {
        return getCurrentTimestamp()
    }

    // Function to get the logout timestamp
    fun getLogoutTimestamp(): String {
        return getCurrentTimestamp()
    }
}