package com.example.meditrack.mainActivity.splash

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import com.caverock.androidsvg.SVG
import com.example.meditrack.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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