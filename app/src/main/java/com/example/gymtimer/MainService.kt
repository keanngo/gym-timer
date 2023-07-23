package com.example.gymtimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Random

class MainService: Service() {

    //Binder given to clients
    private val binder = MainBinder()

    inner class MainBinder: Binder() {
        //Return instance of mainService so clients can call public methods
        fun getService(): MainService = this@MainService
    }

    private val mGenerator = Random()

    val randomNumber: Int
        get() = mGenerator.nextInt(100)

    val counter = 1;

    override fun onBind(p0: Intent?): IBinder? {

        createNotificationChannel()
        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MyBroadcastReceiver::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = "com.example.UPDATE_NOTIFICATION"
            putExtra("COUNTER", counter)
        }
//        val intent = Intent(this, MyBroadcastReceiver::class.java)
//        intent.action = "UPDATE_NOTIFICATION"

        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, counter, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, "123")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("My notification")
            .setContentText("Counter: $counter")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(
                androidx.core.R.drawable.notification_bg_normal, "Update",
                pendingIntent)

//        with(NotificationManagerCompat.from(this)) {
//            // notificationId is a unique int for each notification that you must define
//            notify(0, builder.build())
//        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, builder.build());

        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.v("kean", "onUnbind()")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        Log.v("kean", "onRebind()")
        super.onRebind(intent)
    }

    //it is safe to call this repeatedly because creating an existing notification channel performs no operation
    private fun createNotificationChannel() {
        //require API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "test";
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("123", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}