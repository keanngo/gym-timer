package com.example.gymtimer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.NotificationCompat


class MyBroadcastReceiver: BroadcastReceiver(){
    private var isTimerRunning = false
    private var countDownTimer: CountDownTimer? = null
    override fun onReceive(context: Context, intent: Intent) {

        if("com.example.START" == intent.action){
            isTimerRunning = intent.getBooleanExtra("TIMER_RUNNING", false)
            Log.v("kean", isTimerRunning.toString())
            //if timer is not running, start it
            if (!isTimerRunning){
                isTimerRunning = true
                startTimer(context);
            }//if we get an intent while the timer is running, stop it
            else {
                pauseTimer(context);
            }

        }
        else if("com.example.INCREMENT" == intent.action){}
        else if("com.example.DECREMENT" == intent.action){}
    }
    private fun startTimer(context: Context) {
        Log.v("kean", "startimer called once more")
        // Start the timer and update the notification text
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.v("kean", isTimerRunning.toString())
                if(!isTimerRunning){
                    Log.v("kean", "cancelling inside works")
                    cancel()
                }
                val secondsRemaining = millisUntilFinished / 1000
                val timerText = "Timer: $secondsRemaining seconds remaining."
                updateNotification(context, timerText, true)
            }

            override fun onFinish() {
                // Timer finished, update the notification
                updateNotification(context, "Timer completed!", false)
            }
        }.start()
    }

    private fun pauseTimer(context: Context) {
        if (countDownTimer != null) {
            Log.v("kean", "hello")
            countDownTimer?.cancel()
            countDownTimer = null
            isTimerRunning = false
            updateNotification(context, "Timer completed!", false)
        }
    }

    private fun updateNotification(context:Context, contentText:String, isTimerRunning:Boolean){
        val notificationId = 0
        // Increase the counter and update the notification
        val updateIntent = Intent(context, MyBroadcastReceiver::class.java)
        updateIntent.action = "com.example.START"
        updateIntent.putExtra("TIMER_RUNNING", false)


        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            updateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Increase the counter and update the notification
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, "123")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("My notification")
                .setContentText("$contentText")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(
                    androidx.core.R.drawable.notification_bg_normal, "Stop",
                    pendingIntent)
                .addAction(
                    androidx.core.R.drawable.notification_bg_normal, "+",
                    pendingIntent)
                .addAction(
                    androidx.core.R.drawable.notification_bg_normal, "-",
                    pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build());
    }

}
