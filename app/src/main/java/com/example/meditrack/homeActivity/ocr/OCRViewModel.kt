package com.example.meditrack.homeActivity.ocr

import android.graphics.Rect
import androidx.lifecycle.ViewModel

class OCRViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var listSelectedMedName:ArrayList<Pair<Rect,String>> = ArrayList()
}