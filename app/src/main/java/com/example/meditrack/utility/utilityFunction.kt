package com.example.meditrack.utility

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class utilityFunction {
    companion object{
        fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
            var inputStream: InputStream? = null
            var bitmap: Bitmap? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return bitmap
        }
        fun getCircularBitmap(srcBitmap: Bitmap?): Bitmap {
            val squareBitmapWidth = srcBitmap!!.width.coerceAtMost(srcBitmap.height)
            val dstBitmap = Bitmap.createBitmap(
                squareBitmapWidth,
                squareBitmapWidth,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(dstBitmap)
            val paint = Paint().apply {
                isAntiAlias = true
            }
            val rect = Rect(0, 0, squareBitmapWidth, squareBitmapWidth)
            val rectF = RectF(rect)
            canvas.drawOval(rectF, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val left = ((squareBitmapWidth - srcBitmap.width) / 2).toFloat()
            val top = ((squareBitmapWidth - srcBitmap.height) / 2).toFloat()
            canvas.drawBitmap(srcBitmap, left, top, paint)
            srcBitmap.recycle()
            return dstBitmap
        }

        fun bitmapToBase64(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
        fun decodeBase64ToBitmap(base64String: String): Bitmap? {
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }
}