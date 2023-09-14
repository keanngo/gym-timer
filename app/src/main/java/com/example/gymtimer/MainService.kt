package com.example.gymtimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import java.util.concurrent.TimeUnit


class MainService: Service() {

    //Binder given to clients
    private val binder = MainBinder()

    inner class MainBinder: Binder() {
        //Return instance of mainService so clients can call public methods
        fun getService(): MainService = this@MainService
    }
    lateinit var countDownTimer: PreciseCountdown

    var maxTime: Long = 180000
    var newMaxTime: Long = maxTime
    var currentTime: Long = maxTime

    var isTimerRunning = false

    private var notificationId = 7337194
    private val channelId = "testkean123"

    private fun startRingtone(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.ring)
        mediaPlayer.start()
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.v("kean", "onStartCommand")

        if("QUIT" == intent.action){
            Log.v("kean", "QUIT")
            pauseTimer()
            clearNotification()
            stopSelf()
        }

        if("START" == intent.action){
            Log.v("kean","START")
            isTimerRunning = intent.getBooleanExtra("TIMER_RUNNING", false)
            //if timer is not running, start it
            if (!isTimerRunning){
                if (newMaxTime != maxTime){
                    currentTime = newMaxTime
                    maxTime = newMaxTime
                }
                startTimer(this)
            }//if we get an intent while the timer is running, stop it
            else {
                pauseTimer()
            }

        }
        else if("INCREMENT" == intent.action){
            Log.v("kean", "INCREMENT")
            addToTime(intent, "INCREMENT", 30000)
        }
        else if("DECREMENT" == intent.action){
            Log.v("kean", "DECREMENT")
            addToTime(intent, "DECREMENT", -30000)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun addToTime(intent:Intent, intentString: String, valueToAdd:Long){
        isTimerRunning = intent.getBooleanExtra("TIMER_RUNNING", false)

        //if timer is running, add time to running timer
        if(isTimerRunning){
            pauseTimer()
            val timeDec = intent.getLongExtra(intentString, valueToAdd)
            currentTime += timeDec
            startTimer(this)
        }else{
            //pause timer
            if(currentTime != maxTime){
                currentTime = maxTime
                textNotification(currentTime, false)
            }else{
                //add to start time
                val timeDec = intent.getLongExtra(intentString, valueToAdd)
                newMaxTime += timeDec
                textNotification(newMaxTime, isTimerRunning)
            }
        }
    }

    private fun textNotification(timeLong:Long, isTimerRunning:Boolean){
        val time = TimeUnit.MILLISECONDS.toSeconds(timeLong)
        val minutes = time/60
        val seconds = time % 60
        val minutesText:String = if(minutes < 10){
            "0${minutes}"
        } else{
            "$minutes"
        }
        val secondsText:String = if(seconds < 10){
            "0${seconds}"
        }else{
            "$seconds"
        }
        val timerText = "$minutesText:$secondsText"
        updateNotification(this, timerText, isTimerRunning)
    }
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun startRun(){
        createNotificationChannel()
        if(isTimerRunning){
            startTimer(this)
        }else{
            textNotification(currentTime, false)
        }

    }

    private fun clearNotification(){
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        pauseTimer()
        clearNotification()

        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }
    private fun startTimer(context: Context) {

        countDownTimer = object:PreciseCountdown(currentTime, 1000){
            override fun onTick(timeLeft: Long) {
                isTimerRunning = true
                textNotification(timeLeft, true)
                currentTime = timeLeft
            }

            override fun onFinished() {
                startRingtone(applicationContext)
                onTick(0)
                updateNotification(context, "00:00", false)
                currentTime = maxTime
            }

        }
        countDownTimer.start()



    }

    private fun pauseTimer() {
        if (::countDownTimer.isInitialized) {
            countDownTimer.stop()
            isTimerRunning = false
            textNotification(currentTime, false)
        }
    }

    private fun updateNotification(context:Context, contentText:String, isTimerRunning:Boolean){

        val updateIntent = Intent(context, MainService::class.java).apply{
            action = "START"
            putExtra("TIMER_RUNNING", isTimerRunning)
        }
        val incrementIntent = Intent(context, MainService::class.java).apply {
            action = "INCREMENT"
            putExtra("INCREMENT", 30000.toLong())
            putExtra("TIMER_RUNNING", isTimerRunning)
        }
        val decrementIntent = Intent(context, MainService::class.java).apply {
            action = "DECREMENT"
            putExtra("DECREMENT", (-30000).toLong())
            putExtra("TIMER_RUNNING", isTimerRunning)
        }
        val quitIntent = Intent(context, MainService::class.java).apply {
            action = "QUIT"
        }

        val pendingIntent = PendingIntent.getService(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val incrementPendingIntent: PendingIntent = PendingIntent.getService(context, 0, incrementIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val decrementPendingIntent: PendingIntent = PendingIntent.getService(context, 0, decrementIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val quitPendingIntent: PendingIntent = PendingIntent.getService(context, 0, quitIntent, PendingIntent.FLAG_MUTABLE)

        val buttonText = if (isTimerRunning) "Stop" else "Start"

        //layout inflater
        val contentView = RemoteViews(packageName, R.layout.custom_notification)
        contentView.setOnClickPendingIntent(R.id.startButton, pendingIntent)
        contentView.setOnClickPendingIntent(R.id.incrementButton, incrementPendingIntent)
        contentView.setOnClickPendingIntent(R.id.decrementButton, decrementPendingIntent)
        contentView.setTextViewText(R.id.timerTextView, contentText)
        contentView.setTextViewText(R.id.startButton, buttonText)

        val contentViewSmall = RemoteViews(packageName, R.layout.custom_notification_small)
        contentViewSmall.setOnClickPendingIntent(R.id.startButton, pendingIntent)
        contentViewSmall.setOnClickPendingIntent(R.id.incrementButton, incrementPendingIntent)
        contentViewSmall.setOnClickPendingIntent(R.id.decrementButton, decrementPendingIntent)
        contentViewSmall.setTextViewText(R.id.timerTextView, contentText)
        contentViewSmall.setTextViewText(R.id.startButton, buttonText)

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCustomContentView(contentViewSmall)
                .setCustomBigContentView(contentView)
                .setDeleteIntent(quitPendingIntent)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    //it is safe to call this repeatedly because creating an existing notification channel performs no operation
    private fun createNotificationChannel() {
        //require API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "test"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                setSound(null, null)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}