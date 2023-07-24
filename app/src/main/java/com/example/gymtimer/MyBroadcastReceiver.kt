package com.example.gymtimer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.properties.Delegates


class MyBroadcastReceiver: BroadcastReceiver(){
    companion object {
        var MAX_TIME: Long = 180000
        lateinit var countDownTimer: CountDownTimer
        var currentTime: Long = MAX_TIME
        var newMaxTime: Long = MAX_TIME
    }
    private var isTimerRunning = false
    override fun onReceive(context: Context, intent: Intent) {

        if("START" == intent.action){
            isTimerRunning = intent.getBooleanExtra("TIMER_RUNNING", false)
            Log.v("kean", isTimerRunning.toString())
            //if timer is not running, start it
            if (!isTimerRunning){
                if (newMaxTime != currentTime){
                    currentTime = newMaxTime
                    MAX_TIME = newMaxTime
                }
                startTimer(context);
            }//if we get an intent while the timer is running, stop it
            else {
                pauseTimer(context);
            }

        }
        else if("INCREMENT" == intent.action){
            isTimerRunning = intent.getBooleanExtra("TIMER_RUNNING", false)
            if(isTimerRunning){
                pauseTimer(context);
                val timeInc = intent.getLongExtra("INCREMENT", 10000)
                currentTime += timeInc
                startTimer(context);
            }else{
                if(currentTime != MAX_TIME){
                    currentTime = MAX_TIME
                    val secondsRemaining = currentTime / 1000
                    val timerText = "$secondsRemaining seconds"
                    updateNotification(context, timerText, false)
                }else{
                    val timeInc = intent.getLongExtra("INCREMENT", 10000)
                    newMaxTime += timeInc
                    val secondsRemaining = newMaxTime / 1000
                    val timerText = "$secondsRemaining seconds"
                    updateNotification(context, timerText, false)
                }
            }
        }
        else if("DECREMENT" == intent.action){
            isTimerRunning = intent.getBooleanExtra("TIMER_RUNNING", false)
            if(isTimerRunning){
                pauseTimer(context);
                val timeDec = intent.getLongExtra("DECREMENT", 5000)
                currentTime -= timeDec
                startTimer(context);
            }else{
                if(currentTime != MAX_TIME){
                    currentTime = MAX_TIME
                    val secondsRemaining = currentTime / 1000
                    val timerText = "$secondsRemaining seconds"
                    updateNotification(context, timerText, false)
                }else{
                    val timeDec = intent.getLongExtra("DECREMENT", 5000)
                    newMaxTime -= timeDec
                    val secondsRemaining = newMaxTime / 1000
                    val timerText = "$secondsRemaining seconds"
                    updateNotification(context, timerText, false)
                }
            }
        }
    }
    private fun startTimer(context: Context) {
        Log.v("kean", "startimer called once more")
        // Start the timer and update the notification text
        countDownTimer = object : CountDownTimer(currentTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.v("kean", isTimerRunning.toString())
                val secondsRemaining = millisUntilFinished / 1000
                val timerText = "$secondsRemaining seconds"
                updateNotification(context, timerText,true)
                currentTime=millisUntilFinished
            }

            override fun onFinish() {
                // Timer finished, update the notification
                updateNotification(context, "0 seconds", false)
                currentTime=MAX_TIME
            }
        }.start()
    }

    private fun pauseTimer(context: Context) {
        Log.v("kean","pause timer pressed")
        if (countDownTimer != null) {
            Log.v("kean", "hello")
            countDownTimer.cancel()
            isTimerRunning = false
            val secondsRemaining = currentTime / 1000
            val timerText = "$secondsRemaining seconds"
            updateNotification(context, timerText, false)
        }
    }

    private fun updateNotification(context:Context, contentText:String, isTimerRunning:Boolean){
        val notificationId = 0
        // Increase the counter and update the notification
        val updateIntent = Intent(context, MyBroadcastReceiver::class.java).apply{
            action = "START"
            putExtra("TIMER_RUNNING", isTimerRunning)
        }
        val incrementIntent = Intent(context, MyBroadcastReceiver::class.java).apply {
            action = "INCREMENT"
            putExtra("INCREMENT", 10000.toLong())
            putExtra("TIMER_RUNNING", isTimerRunning)
        }
        val decrementIntent = Intent(context, MyBroadcastReceiver::class.java).apply {
            action = "DECREMENT"
            putExtra("DECREMENT", 5000.toLong())
            putExtra("TIMER_RUNNING", isTimerRunning)
        }


        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            updateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val incrementPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, incrementIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val decrementPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, decrementIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var buttonText:String
        if(isTimerRunning){
            buttonText = "Stop"
        }else{
            buttonText = "Start"
        }

        // Increase the counter and update the notification
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, "123")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("My notification")
                .setContentText("$contentText")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(
                    androidx.core.R.drawable.notification_bg_normal, buttonText,
                    pendingIntent)
                .addAction(
                    androidx.core.R.drawable.notification_bg_normal, "+",
                    incrementPendingIntent)
                .addAction(
                    androidx.core.R.drawable.notification_bg_normal, "-",
                    decrementPendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build());
    }

}
