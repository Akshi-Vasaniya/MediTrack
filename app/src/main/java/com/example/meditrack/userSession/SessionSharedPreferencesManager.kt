package com.example.meditrack.userSession

import android.content.Context

object SessionSharedPreferencesManager {

    private const val PREF_NAME = "session_preferences"
    private const val SESSION_ID_KEY = "session_id"

    // Function to create SharedPreferences and store session ID
    fun createSharedPreferences(context: Context, sessionId: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(SESSION_ID_KEY, sessionId)
        editor.apply()
    }

    // Function to delete SharedPreferences
    fun deleteSharedPreferences(context: Context) {
        if(isSessionAvailable(context))
        {
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
        }
    }

    // Function to fetch session ID from SharedPreferences
    fun fetchSessionId(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(SESSION_ID_KEY, null)
    }

    // Function to edit session ID in SharedPreferences
    fun editSessionId(context: Context, newSessionId: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(SESSION_ID_KEY, newSessionId)
        editor.apply()
    }

    fun isSessionAvailable(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.contains(SESSION_ID_KEY)
    }
}