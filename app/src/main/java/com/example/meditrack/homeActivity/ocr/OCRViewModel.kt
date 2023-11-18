package com.example.meditrack.homeActivity.ocr

import android.graphics.Rect
import android.graphics.RectF
import androidx.lifecycle.ViewModel

class OCRViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var listSelectedMedName:ArrayList<Pair<RectF,String>> = ArrayList()
}