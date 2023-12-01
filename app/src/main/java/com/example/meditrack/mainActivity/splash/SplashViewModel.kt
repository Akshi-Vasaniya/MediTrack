package com.example.meditrack.mainActivity.splash

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SplashViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    suspend fun delayAndNavigate(delayMillis: Long) {
        withContext(Dispatchers.IO) {
            delay(delayMillis)
        }
    }

}