package com.example.meditrack.utility

import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.meditrack.dataModel.api.ApiInstance
import com.example.meditrack.utility.UtilsFunctions.Companion.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class UtilsFunctions {

    companion object{
        private const val tAG="utilityFunction"
        const val DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss"
        const val DATE_FORMAT = "dd-MM-yyyy"
        const val TIME_FORMAT = "hh:mm:ss a"
        const val MEDICINE_DATE_FORMAT = "MM/yyyy"
        suspend fun Uri.toBitmap(context: Context): Bitmap? {
            return withContext(Dispatchers.IO){
                var inputStream: InputStream? = null
                val bitmap: Bitmap?
                try {
                    inputStream = context.contentResolver.openInputStream(this@toBitmap)
                    bitmap = BitmapFactory.decodeStream(inputStream)
                } catch (e: FileNotFoundException) {
                    throw e
                } finally {
                    try {
                        inputStream?.close()
                    } catch (e: IOException) {
                        throw e
                    }
                }
                bitmap
            }
        }

        // Function to get the current date in dd/MM/yyyy format
        fun getCurrentDate(): String {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            return sdf.format(Date())
        }

        // Function to get the current time in hh:mm a format
        fun getCurrentTime(): String {
            val sdf = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
            return sdf.format(Date())
        }
        suspend fun Bitmap?.toCircularBitmap(): Bitmap {
            return withContext(Dispatchers.IO) {
                try {
                    val squareBitmapWidth = this@toCircularBitmap!!.width.coerceAtMost(this@toCircularBitmap.height)
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
                    val left = ((squareBitmapWidth - this@toCircularBitmap.width) / 2).toFloat()
                    val top = ((squareBitmapWidth - this@toCircularBitmap.height) / 2).toFloat()
                    canvas.drawBitmap(this@toCircularBitmap, left, top, paint)
                    this@toCircularBitmap.recycle()
                    dstBitmap
                }
                catch (ex:Exception)
                {
                    throw ex
                }

            }
        }

        suspend fun Bitmap.toByteArray(): ByteArray {
            return withContext(Dispatchers.IO){
                val stream = ByteArrayOutputStream()
                this@toByteArray.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            }
        }

        fun ByteArray.toBitmap(): Bitmap {
            return BitmapFactory.decodeByteArray(this, 0, this.size)
        }

        suspend fun Bitmap.toBase64(): String {
            return withContext(Dispatchers.IO){
                try {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    this@toBase64.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    Base64.encodeToString(byteArray, Base64.DEFAULT)
                }
                catch (ex:Exception)
                {
                    Log.e(tAG,"utilityFunction.kt -> function bitmapToBase64() -> $ex")
                    throw ex
                }

            }
        }

        fun stringNormalize(inputString: String):String{
            return inputString.trim().replace(Regex("[\t ]+"), " ").replace(" ", "_")
        }

        /*fun stringCompress(inputString: String): String {
            // Compress the long string
            val compressedBytes = ByteArrayOutputStream().use { byteStream ->
                GZIPOutputStream(byteStream).use { gzipStream ->
                    gzipStream.write(inputString.toByteArray())
                }
                byteStream.toByteArray()
            }

            // Convert compressed bytes back to a string
            return String(compressedBytes)
        }*/

        fun String.toBase64(): String {
            val trimmedLowerCaseString = this.trim().lowercase(Locale.getDefault())
            return Base64.encodeToString(trimmedLowerCaseString.toByteArray(), Base64.DEFAULT)
        }

        suspend fun String.fromBase64ToBitmap(): Bitmap {
            return withContext(Dispatchers.IO){
                try{
                    val imageBytes = Base64.decode(this@fromBase64ToBitmap, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                }
                catch (exNull:NullPointerException)
                {
                    Log.e(tAG,"utilityFunction.kt -> function decodeBase64ToBitmap() -> NullPointerException: $exNull")
                    throw exNull
                }
                catch (ex:Exception){
                    Log.e(tAG,"utilityFunction.kt -> function decodeBase64ToBitmap() -> $ex")
                    throw ex
                }

            }

        }

        fun validateMedicine(mfgDate: String, expDate: String): Boolean {
            val dateFormat = SimpleDateFormat(MEDICINE_DATE_FORMAT, Locale.getDefault())
            dateFormat.isLenient = false

            try {
                val manufacturingDate = dateFormat.parse(mfgDate)
                val expiryDate = dateFormat.parse(expDate)

                if (expiryDate.before(manufacturingDate)) {
                    return false // Expiry date is before manufacturing date
                }

                val calManufacturing = Calendar.getInstance()
                manufacturingDate?.let { calManufacturing.time = it }

                val calExpiry = Calendar.getInstance()
                expiryDate?.let { calExpiry.time = it }

                val diff = calExpiry.get(Calendar.YEAR) - calManufacturing.get(Calendar.YEAR)

                return diff in 0..5 && (diff != 5 || calExpiry.get(Calendar.MONTH) >= calManufacturing.get(Calendar.MONTH))

            } catch (e: Exception) {
                e.printStackTrace() // Handle or log the exception here
                return false
            }
        }

        // Function to find nearly similar rectangles based on height, including the highest rectangle
        /*fun findNearlySimilarRectangles(
            rectangles: ArrayList<Pair<Rect, String>>,
            tolerancePercentage: Double
        ): ArrayList<Pair<Rect, String>> {
            // Find the highest-height rectangle
            val highestRectPair = rectangles.maxByOrNull { it.first.height() }
                ?: return ArrayList()

            // Calculate tolerance based on the highest rectangle's height
            val tolerance = highestRectPair.first.height() * tolerancePercentage

            // Filter rectangles based on the tolerance
            val similarRects = rectangles.filter { rectPair ->
                val difference = Math.abs(rectPair.first.height() - highestRectPair.first.height())
                difference <= tolerance
            }.toMutableList()

            // Add the highest rectangle to the list of similar rectangles
            similarRects.add(0, highestRectPair)

            return ArrayList(similarRects)
        }*/

        /*fun filterFirstSecondThirdLargestRect(rectList: List<Pair<Rect, String>>): Triple<List<Pair<Rect, String>>, List<Pair<Rect, String>>, List<Pair<Rect, String>>>? {
            if (rectList.size < 3) return null

            val sortedRects = rectList.sortedByDescending { it.first.width() * it.first.height() }

            val firstLargestRect = sortedRects[0].first
            val secondLargestRect = sortedRects[1].first
            val thirdLargestRect = sortedRects[2].first

            val firstLargestRects = sortedRects.filter { it.first.width() * it.first.height() == firstLargestRect.width() * firstLargestRect.height() }
            val secondLargestRects = sortedRects.filter { it.first.width() * it.first.height() == secondLargestRect.width() * secondLargestRect.height() }
            val thirdLargestRects = sortedRects.filter { it.first.width() * it.first.height() == thirdLargestRect.width() * thirdLargestRect.height() }

            return Triple(firstLargestRects, secondLargestRects, thirdLargestRects)
        }*/


        // Function to convert Bitmap to Uri
        fun Bitmap.toUri(context: Context): Uri? {
            // Get the context's cache directory
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()

            // Create a temporary file
            val file = File(cachePath, "${UUID.randomUUID()}.jpg")
            try {
                // Compress the bitmap and write it to the temporary file
                val stream = FileOutputStream(file)
                this.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // Return the FileProvider Uri for the temporary file
            return try {
                val contentUri = FileProvider.getUriForFile(context, "com.example.fileprovider", file)
                contentUri
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                null
            }
        }

        fun Context.getDeviceInformation(): HashMap<String, String>? {
            try {
                val deviceInfo = HashMap<String, String>()

                // Device ID or Name
                deviceInfo["deviceId"] = Build.ID
                deviceInfo["deviceName"] = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

                // Device Type
                val isTablet = this.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
                deviceInfo["deviceType"] = if (isTablet) "Tablet" else "Phone"

                // Device Operating System
                deviceInfo["osVersion"] = Build.VERSION.RELEASE
                deviceInfo["apiLevel"] = Build.VERSION.SDK_INT.toString()

                // App Version
                try {
                    val packageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
                    deviceInfo["appVersion"] = packageInfo.versionName
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

                return deviceInfo
            }
            catch (ex:Exception){
                return null
            }

        }



        fun Context.showToast(message: String) {
            MainScope().launch(Dispatchers.Main) {
                Toast.makeText(this@showToast, message, Toast.LENGTH_SHORT).show()
            }

        }



        // Function to get the Uri from ImageView
        /*fun ImageView.toUri(context: Context): Uri? {
            val bitmap = (this.drawable as BitmapDrawable).bitmap
            var imageUri: Uri? = null
            try {
                val file = File(context.externalCacheDir, "${UUID.randomUUID()}.jpg")
                val fOut = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.flush()
                fOut.close()
                imageUri = Uri.fromFile(file)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return imageUri
        }*/

        fun isMediTrackServerLive(callback: (Boolean) -> Unit)
        {
            val call: Call<Void> =  ApiInstance.api.checkServer()

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        callback(true)
                    } else {
                        callback(false)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    callback(false)
                }
            })
        }
    }


}