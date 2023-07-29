package com.example.gymtimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
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

    var MAX_TIME: Long = 180000
    lateinit var countDownTimer: PreciseCountdown
    var currentTime: Long = MAX_TIME
    var newMaxTime: Long = MAX_TIME
    var isTimerRunning = false
    private var notificationId = 7337194
    private val channelId = "testkean123"

    private var ringtone: Ringtone? = null
    private var isRingtonePlaying: Boolean = false

    private fun startRingtone(context: Context) {
        if (!isRingtonePlaying) {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(context.applicationContext, ringtoneUri)
            ringtone?.play()
            isRingtonePlaying = true
        }
    }
    private fun stopRingtone() {
        if (isRingtonePlaying) {
            ringtone?.stop()
            isRingtonePlaying = false
        }
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.v("kean", "onStartCommand")
        if(isRingtonePlaying){
            stopRingtone()
            isRingtonePlaying = false
        }
        if("QUIT" == intent.action){
            Log.v("kean", "This was hit")
            pauseTimer(this)
            val notificationManager =
                applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
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
            addToTime(intent, "INCREMENT", 30000)
        }
        else if("DECREMENT" == intent.action){
            addToTime(intent, "DECREMENT", -30000)
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
        var timerText = "$minutesText:$secondsText"
        updateNotification(this, timerText, isTimerRunning)
    }
    override fun onBind(intent: Intent): IBinder? {
        Log.v("kean", "onBind()")
        currentTime = intent.getLongExtra("currentTime", 0)
        isTimerRunning = intent.getBooleanExtra("isTimerRunning", false)
        start(currentTime, isTimerRunning)

        return binder
    }

    public fun start(currentTime: Long?, isTimerRunning: Boolean?){

        createNotificationChannel()
        if(isTimerRunning == true){
            startTimer(this)
        }else{
            if (currentTime != null) {
                textNotification(currentTime, false)
            }
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.v("kean", "onUnbind()")
//        pauseTimer(this)
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        Log.v("kean", "onRebind()")
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
            countDownTimer.stop()
            isTimerRunning = false
            textNotification(currentTime, false)
        }
    }

    private fun updateNotification(context:Context, contentText:String, isTimerRunning:Boolean){
        // Increase the counter and update the notification
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

        var buttonText = if (isTimerRunning) "Stop" else "Start"

        //layout inflatoer
        val contentView = RemoteViews(packageName, R.layout.custom_notification)
        contentView.setOnClickPendingIntent(R.id.startButton, pendingIntent)
        contentView.setOnClickPendingIntent(R.id.incrementButton, incrementPendingIntent)
        contentView.setOnClickPendingIntent(R.id.decrementButton, decrementPendingIntent)
        contentView.setTextViewText(R.id.timerTextView, "$contentText")
        contentView.setTextViewText(R.id.startButton, buttonText)

        val contentViewSmall = RemoteViews(packageName, R.layout.custom_notification_small)
        contentViewSmall.setOnClickPendingIntent(R.id.startButton, pendingIntent)
        contentViewSmall.setOnClickPendingIntent(R.id.incrementButton, incrementPendingIntent)
        contentViewSmall.setOnClickPendingIntent(R.id.decrementButton, decrementPendingIntent)
        contentViewSmall.setTextViewText(R.id.timerTextView, "$contentText")
        contentViewSmall.setTextViewText(R.id.startButton, buttonText)

        // Increase the counter and update the notification
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
        notificationManager.notify(notificationId, builder.build());
    }

    //it is safe to call this repeatedly because creating an existing notification channel performs no operation
    private fun createNotificationChannel() {
        //require API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "test";
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