package com.example.gymtimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import java.util.Random
import java.util.concurrent.TimeUnit

class MainService: Service() {

    //Binder given to clients
    private val binder = MainBinder()

    inner class MainBinder: Binder() {
        //Return instance of mainService so clients can call public methods
        fun getService(): MainService = this@MainService
    }

    private val mGenerator = Random()

    companion object {
        var MAX_TIME: Long = 180000
//        lateinit var countDownTimer: CountDownTimer
        lateinit var countDownTimer: PreciseCountdown
        var currentTime: Long = MAX_TIME
        var newMaxTime: Long = MAX_TIME
    }
    private var isTimerRunning = false

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if("QUIT" == intent.action){
            Log.v("kean", "This was hit")
            stopSelf()
        }

        if("START" == intent.action){
            Log.v("kean","start is hit")
            isTimerRunning = intent.getBooleanExtra("TIMER_RUNNING", false)
            //if timer is not running, start it
            if (!isTimerRunning){
                if (newMaxTime != MAX_TIME){
                    currentTime = newMaxTime
                    MAX_TIME = newMaxTime
                }
                startTimer(this);
            }//if we get an intent while the timer is running, stop it
            else {
                pauseTimer(this);
            }

        }
        else if("INCREMENT" == intent.action){
            addToTime(intent, "INCREMENT", 10000)
        }
        else if("DECREMENT" == intent.action){
            addToTime(intent, "DECREMENT", -10000)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun addToTime(intent:Intent, intentString: String, valueToAdd:Long){
        isTimerRunning = intent.getBooleanExtra("TIMER_RUNNING", false)
        if(isTimerRunning){
            pauseTimer(this);
            val timeDec = intent.getLongExtra(intentString, valueToAdd)
            currentTime += timeDec
            startTimer(this);
        }else{
            if(currentTime != MAX_TIME){
                currentTime = MAX_TIME
                textNotification(currentTime, false)
            }else{
                val timeDec = intent.getLongExtra(intentString, valueToAdd)
                newMaxTime += timeDec
                textNotification(newMaxTime, isTimerRunning)
            }
        }
    }

    private fun textNotification(timeLong:Long, isTimerRunning:Boolean){
//        var time = (timeLong * 0.001f).roundToLong()
//        Log.v("kean", time.toString())
        var ms = timeLong % 1000
        var time = TimeUnit.MILLISECONDS.toSeconds(timeLong)
        var minutes = time/60
        var seconds = time % 60
        var minutesText:String = if(minutes < 10){
            "0${minutes}"
        } else{
            "$minutes"
        }
        var secondsText:String = if(seconds < 10){
            "0${seconds}"
        }else{
            "$seconds"
        }
        var timerText = "$minutesText:$secondsText:$ms"
//        var timerText = "$timeLong"
        updateNotification(this, timerText, isTimerRunning)
    }

    val randomNumber: Int
        get() = mGenerator.nextInt(100)

    override fun onBind(p0: Intent?): IBinder? {
        Log.v("kean", "onBind()")

        createNotificationChannel()
        textNotification(currentTime, false)

        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.v("kean", "onUnbind()")
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(0)
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
            val channel = NotificationChannel("12345", name, importance).apply {
                description = descriptionText
                setSound(null, null)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun startTimer(context: Context) {

        // Start the timer and update the notification text
//        countDownTimer = object : CountDownTimer(currentTime, 100) {
//            override fun onTick(millisUntilFinished: Long) {
//                textNotification(millisUntilFinished, true)
//                currentTime = millisUntilFinished
//            }
//
//            override fun onFinish() {
//                // Timer finished, update the notification
//                updateNotification(context, "00:00", false)
//                currentTime = MAX_TIME
//            }
//        }.start()
        countDownTimer = object:PreciseCountdown(currentTime, 1000){
            override fun onTick(timeLeft: Long) {
                textNotification(timeLeft, true)
                currentTime = timeLeft
            }

            override fun onFinished() {
                onTick(0);
                updateNotification(context, "00:00", false)
                currentTime = MAX_TIME
            }

        }
        countDownTimer.start()



    }

    private fun pauseTimer(context: Context) {
        Log.v("kean","pause timer pressed")
        if (countDownTimer != null) {
            Log.v("kean", "hello")
//            countDownTimer.cancel()
            countDownTimer.stop()
            isTimerRunning = false
            textNotification(currentTime, false)
        }
    }

    private fun updateNotification(context:Context, contentText:String, isTimerRunning:Boolean){
        val notificationId = 0
        // Increase the counter and update the notification
        val updateIntent = Intent(context, MainService::class.java).apply{
            action = "START"
            putExtra("TIMER_RUNNING", isTimerRunning)
        }
        val incrementIntent = Intent(context, MainService::class.java).apply {
            action = "INCREMENT"
            putExtra("INCREMENT", 10000.toLong())
            putExtra("TIMER_RUNNING", isTimerRunning)
        }
        val decrementIntent = Intent(context, MainService::class.java).apply {
            action = "DECREMENT"
            putExtra("DECREMENT", (-10000).toLong())
            putExtra("TIMER_RUNNING", isTimerRunning)
        }
        val quitIntent = Intent(context, MainService::class.java).apply {
            action = "QUIT"
        }

        val pendingIntent = PendingIntent.getService(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val incrementPendingIntent: PendingIntent = PendingIntent.getService(context, 0, incrementIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val decrementPendingIntent: PendingIntent = PendingIntent.getService(context, 0, decrementIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val quitPendingIntent: PendingIntent = PendingIntent.getService(context, 0, quitIntent, PendingIntent.FLAG_MUTABLE)

        var buttonText:String
        if(isTimerRunning){
            buttonText = "Stop"
        }else{
            buttonText = "Start"
        }

        //layout inflatoer
        val contentView = RemoteViews(packageName, R.layout.custom_notification)
        contentView.setOnClickPendingIntent(R.id.startButton, pendingIntent)
        contentView.setOnClickPendingIntent(R.id.incrementButton, incrementPendingIntent)
        contentView.setOnClickPendingIntent(R.id.decrementButton, decrementPendingIntent)
        contentView.setTextViewText(R.id.timerTextView, "$contentText")
        contentView.setTextViewText(R.id.startButton, buttonText)

        // Increase the counter and update the notification
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, "12345")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("My notification")
                .setContentText("$contentText")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCustomContentView(contentView)
                .setDeleteIntent(quitPendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build());
    }
}