package com.example.gymtimer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat


class MyBroadcastReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        Log.v("kean", intent.toString())
        if("com.example.UPDATE_NOTIFICATION" == intent.action){
            Log.v("kean", "intent pressed")
            val notificationId = 0
            var currentCounter = intent.getIntExtra("COUNTER", 1)
            currentCounter++

            // Increase the counter and update the notification
            val updateIntent = Intent(context, MyBroadcastReceiver::class.java)
            updateIntent.action = "com.example.UPDATE_NOTIFICATION"
            updateIntent.putExtra("COUNTER", currentCounter)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Increase the counter and update the notification
            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(context, "123")
                    .setSmallIcon(com.example.gymtimer.R.drawable.ic_launcher_foreground)
                    .setContentTitle("My notification")
                    .setContentText("Counter: $currentCounter")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
//            .setContentIntent(pendingIntent)
                    .addAction(
                        androidx.core.R.drawable.notification_bg_normal, "Update",
                        pendingIntent)
//            .setAutoCancel(true)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, builder.build());
        }
    }

}
