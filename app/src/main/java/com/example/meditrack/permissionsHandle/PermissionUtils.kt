package com.example.meditrack.permissionsHandle

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class PermissionUtils {

    companion object{
        private val requiredPermissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        )
        fun Context.toCheckCameraAccess() = requiredPermissions[0].let {
            ContextCompat.checkSelfPermission(
                this, it
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun Context.toCheckReadStorageAccess() = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            requiredPermissions[1].let {
                ContextCompat.checkSelfPermission(
                    this, it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }else{
            true
        }

        fun Context.toCheckWriteStorageAccess() = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            requiredPermissions[2].let {
                ContextCompat.checkSelfPermission(
                    this, it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }else{
            true
        }

        fun Context.toCheckReadMediaImagesAccess() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions[3].let {
                ContextCompat.checkSelfPermission(
                    this, it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }else{
            true
        }

        fun Context.toCheckPostNotificationAccess() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions[4].let {
                ContextCompat.checkSelfPermission(
                    this, it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }else{
            true
        }

        fun Context.toCheckLocationAccess() = requiredPermissions[5].let {
            ContextCompat.checkSelfPermission(
                this, it
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun Context.checkAllRequiredPermissions() = (
                this.toCheckCameraAccess() &&
                        this.toCheckReadStorageAccess() &&
                        this.toCheckWriteStorageAccess() &&
                        this.toCheckReadMediaImagesAccess() &&
                        this.toCheckPostNotificationAccess() &&
                        this.toCheckLocationAccess()
                )

        fun Context.requestPermissions(listOfPermission:MutableList<String>,callback: (Boolean) -> Unit) {
            if(listOfPermission.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
                {
                    listOfPermission.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            if(listOfPermission.contains(Manifest.permission.READ_MEDIA_IMAGES))
            {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                {
                    listOfPermission.remove(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            if(listOfPermission.contains(Manifest.permission.POST_NOTIFICATIONS))
            {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                {
                    listOfPermission.remove(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            if(listOfPermission.contains(Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                    listOfPermission.remove(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            Dexter.withContext(this)
                .withPermissions(listOfPermission)
                .withListener(object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report != null && report.areAllPermissionsGranted()) {
                            callback(true)
                        }
                        else {
                            callback(false)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }

                })
                .check()
        }

        fun Context.isLocationEnabled(): Boolean {
            val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }
}