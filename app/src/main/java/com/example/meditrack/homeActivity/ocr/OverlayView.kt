package com.example.meditrack.homeActivity.ocr

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class OverlayView(context: Context) : View(context) {

    var overlaySelectionListener: OverlaySelectionListener? = null

//    private var rectPaint: Paint = Paint().apply {
//        color = Color.RED
//        style = Paint.Style.STROKE
//        strokeWidth = 5f
//    }
    interface OverlaySelectionListener {
        fun onRectSelected(rect: ArrayList<Pair<Rect, String>>)
    }

    var selectedItemColor = Color.argb(204, 173, 216, 230) // Light Blue color with 60% transparency
    var unselectedItemColor = Color.argb(128, 255, 255, 255) // White color with 50% transparency
    private var rectPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = unselectedItemColor
        pathEffect = CornerPathEffect(10f)
    }

    private var selectedRectPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = selectedItemColor
        pathEffect = CornerPathEffect(10f) // Adjust the corner radius as needed
    }

    private var deselectedRectPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = unselectedItemColor
        pathEffect = CornerPathEffect(10f) // Adjust the corner radius as needed
    }

    private var boundingRects: ArrayList<Pair<Rect, String>>? = null
    private var selectedRects: ArrayList<Pair<Rect, String>> = ArrayList()

    fun setBoundingRects(rects: ArrayList<Pair<Rect, String>>) {
        boundingRects = rects
        selectedRects.clear()
        invalidate()
    }

    fun clearOverlay() {
        boundingRects?.clear()
        selectedRects.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        boundingRects?.forEach { (rect, text) ->
            if (selectedRects.contains(Pair(rect, text))) {
                rectPaint.color = selectedItemColor
            } else {
                rectPaint.color = unselectedItemColor
            }
            canvas.drawRect(rect, rectPaint)
        }
        /*selectedRects.forEach { (rect, text) ->
            val x = rect.left.toFloat()
            val y = rect.top.toFloat()
            rectPaint.color = Color.WHITE
            rectPaint.textSize = 40f
            canvas.drawText(text, x, y - 10, rectPaint)
            Log.i("OverlayView", "Selected Text: $text")
        }*/
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x.toInt()
                val y = event.y.toInt()
                val clickedPair = boundingRects?.find { it.first.contains(x, y) }
                if (clickedPair != null) {
                    if (selectedRects.contains(clickedPair)) {
                        selectedRects.remove(clickedPair)
                    } else {
                        selectedRects.add(clickedPair)
                    }
                    overlaySelectionListener?.onRectSelected(selectedRects)
                    invalidate()
                }
                return true
            }
        }
        return false
    }
}