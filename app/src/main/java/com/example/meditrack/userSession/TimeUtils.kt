package com.example.meditrack.userSession

import java.text.SimpleDateFormat
import java.util.*

class TimeUtils {

    companion object {

        private const val DATE_FORMAT = "dd-MM-yyyy HH:mm:ss"

        // Function to get the current timestamp in a specified format
        private fun getCurrentTimestamp(): String {
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
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
}