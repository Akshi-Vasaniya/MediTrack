package com.example.meditrack.utility

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class UtilityFunction {

    companion object{
        private const val tAG="utilityFunction"
        suspend fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
            return withContext(Dispatchers.IO){
                var inputStream: InputStream? = null
                val bitmap: Bitmap?
                try {
                    inputStream = context.contentResolver.openInputStream(uri)
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
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date())
        }

        // Function to get the current time in hh:mm a format
        fun getCurrentTime(): String {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date())
        }
        suspend fun getCircularBitmap(srcBitmap: Bitmap?): Bitmap {
            return withContext(Dispatchers.IO) {
                try {
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
                    dstBitmap
                }
                catch (ex:Exception)
                {
                    throw ex
                }

            }
        }

        suspend fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
            return withContext(Dispatchers.IO){
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            }

        }

        suspend fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
            return withContext(Dispatchers.IO){
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            }
        }

        suspend fun bitmapToBase64(bitmap: Bitmap): String {
            return withContext(Dispatchers.IO){
                try {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream)
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

        fun stringtobase64(inputString: String): String {
            val trimmedLowerCaseString = inputString.trim().lowercase(Locale.getDefault())

            return Base64.encodeToString(trimmedLowerCaseString.toByteArray(), Base64.DEFAULT)
        }

        suspend fun decodeBase64ToBitmap(base64String: String): Bitmap {
            return withContext(Dispatchers.IO){
                try{
                    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
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
            val dateFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
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
        fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
            // Get the context's cache directory
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()

            // Create a temporary file
            val file = File(cachePath, "${UUID.randomUUID()}.jpg")
            try {
                // Compress the bitmap and write it to the temporary file
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
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

        fun getDeviceInformation(context: Context): HashMap<String, String>? {
            try {
                val deviceInfo = HashMap<String, String>()

                // Device ID or Name
                deviceInfo["deviceId"] = Build.ID
                deviceInfo["deviceName"] = Build.MODEL

                // Device Type
                val isTablet = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
                deviceInfo["deviceType"] = if (isTablet) "Tablet" else "Phone"

                // Device Operating System
                deviceInfo["osVersion"] = Build.VERSION.RELEASE
                deviceInfo["apiLevel"] = Build.VERSION.SDK_INT.toString()

                // App Version
                try {
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
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



        fun showToast(context: Context,message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        // Function to get the Uri from ImageView
        /*fun getImageUriFromImageView(context: Context,imageView: ImageView): Uri? {
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
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
    }
}