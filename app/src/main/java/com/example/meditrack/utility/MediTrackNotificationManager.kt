package com.example.meditrack.utility

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MediTrackNotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "new_login_channel"
        private const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "New Login Channel"
            val descriptionText = "Channel for new login notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(context)
                .withPermissions(
                    Manifest.permission.POST_NOTIFICATIONS
                    // Add more permissions if needed
                )
                .withListener(multiplePermissionsListener)
                .check()
        }
    }



    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }

    fun showNewLoginNotification() {
        if (checkNotificationPermission()) {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New Login Detected")
                .setContentText("A new login was detected on your account.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
        else{
            showLocationSettingsDialog()
        }
    }

    fun showLocationSettingsDialog() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setTitle("Enable Notification")
        alertDialogBuilder.setMessage("Notification services are required for this app. Please enable location.")

        alertDialogBuilder.setPositiveButton("Settings") { _, _ ->
            // Open device settings for location
            var settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            if (settingsIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(settingsIntent)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    settingsIntent = Intent(Settings.ACTION_ALL_APPS_NOTIFICATION_SETTINGS)
                    context.startActivity(settingsIntent)
                }

            }

        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    private val multiplePermissionsListener = object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
            // Check if all permissions are granted
            if (!(report != null && report.areAllPermissionsGranted())) {
                showLocationSettingsDialog()
            }
        }

        override fun onPermissionRationaleShouldBeShown(
            permissions: MutableList<PermissionRequest>?,
            token: PermissionToken?
        ) {
            token?.continuePermissionRequest()
            // You can show a rationale dialog here and call token.continuePermissionRequest() if the user agrees
        }
    }
}