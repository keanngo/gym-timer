package com.example.gymtimer

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
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

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }
}