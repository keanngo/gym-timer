package com.example.gymtimer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.ComponentActivity
import kotlin.system.exitProcess


class MainActivity : ComponentActivity() {
    private lateinit var mService: MainService
    private var mBound: Boolean = false

    //define callbacks for service binding, passed to bindService()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MainService.MainBinder
            mService = binder.getService()
            mBound = true
            mService.startRun()
            finish()
            exitProcess(0)

        }

        override fun onServiceDisconnected(p0: ComponentName) {
            mBound = false
        }

    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, MainService::class.java)
        serviceIntent.action = "START"
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }
}