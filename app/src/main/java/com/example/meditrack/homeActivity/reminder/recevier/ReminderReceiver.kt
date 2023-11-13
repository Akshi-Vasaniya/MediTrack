package com.example.meditrack.homeActivity.reminder.recevier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.meditrack.R
import com.example.meditrack.homeActivity.reminder.notification.NotificationFragment

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("TAG", "onReceive: 1")
        val title = intent?.getStringExtra("title")
        val content = intent?.getStringExtra("content")

        if (!title.isNullOrBlank() && !content.isNullOrBlank()) {
            showNotification(context, title, content)
        }

        // Vibrate the device before showing the notification
//        vibrateDevice(context)

    }

//    private fun vibrateDevice(context: Context?) {
//        Log.i("TAG", "vibrateDevice: 2")
//        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//
//        // Vibrate for 1000 milliseconds (1 second)
//        vibrator.vibrate(1000)
//    }

    private fun showNotification(context: Context?, title: String, content: String) {
        Log.i("TAG", "showNotification: 3")
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 0 // You can use a unique ID for each notification

        // Create a notification channel for Android 8.0+ (Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "0",
                "Medicine Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create a notification
        val notificationBuilder = NotificationCompat.Builder(context, "0")
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(content)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI) // Add sound
            .setVibrate(longArrayOf(1000, 1000)) // Add vibration
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Close the notification when clicked

        val notificationIntent = Intent(context, NotificationFragment::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationBuilder.setContentIntent(pendingIntent)

        // Show the notification
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}