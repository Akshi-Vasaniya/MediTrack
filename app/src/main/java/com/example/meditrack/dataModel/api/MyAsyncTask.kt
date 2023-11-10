package com.example.meditrack.dataModel.api

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MyAsyncTask(private val callback: (String) -> Unit) : AsyncTask<String, Void, String>() {
    override fun doInBackground(vararg params: String): String {
        val urlString = params[0]

        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?

            while (inputStream.readLine().also { line = it } != null) {
                response.append(line)
            }

            inputStream.close()
            connection.disconnect()

            return response.toString()
        } catch (e: Exception) {
            // Handle exception
            return "Error: ${e.message}"
        }
    }

    override fun onPostExecute(result: String) {
        // Call the callback function with the result
        callback(result)
    }
}