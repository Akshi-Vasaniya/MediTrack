package com.example.meditrack.utility

import android.content.Context

object progressDialog {
    private var progressDialog: CustomProgressDialog? = null
    fun getInstance(context: Context): CustomProgressDialog {
        if (progressDialog == null) {
            progressDialog = CustomProgressDialog(context)
        }
        return progressDialog!!
    }
}