package com.example.meditrack.mainActivity.splash

import android.content.res.Resources
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caverock.androidsvg.SVG
import com.example.meditrack.R
import com.example.meditrack.dataModel.User
import com.example.meditrack.firebase.userReference
import com.google.firebase.database.DatabaseException
import kotlinx.coroutines.*

class SplashViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    var svg:SVG?=null

    fun getAppSVG(resources:Resources): SVG? {
        return if(svg!=null) {
            svg
        } else{
            svg=SVG.getFromResource(resources, R.raw.meditracklogo)
            svg
        }
    }

    suspend fun delayAndNavigate(delayMillis: Long) {
        withContext(Dispatchers.IO) {
            delay(delayMillis)
        }
    }

}