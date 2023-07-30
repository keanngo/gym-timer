package com.example.gymtimer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel() {
    private val isTimerRunning = MutableLiveData(false)
    private val currentTime = MutableLiveData<Long>(0)
    private val insideApp = MutableLiveData(0)

    fun getTimerRunning(): LiveData<Boolean> {
        return isTimerRunning
    }
    fun getCurrentTime(): LiveData<Long> {
        return currentTime
    }

    fun getInsideApp(): LiveData<Int>{
        return insideApp
    }

    fun setTimerRunning(value: Boolean) {
        isTimerRunning.postValue(value)
    }

    fun setCurrentTime(value: Long){
        currentTime.postValue(value)
    }

    fun setInsideApp(value: Int){
        Log.v("kean", "setInsideApp(): "+value)
        insideApp.postValue(value)
    }

}