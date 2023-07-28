package com.example.gymtimer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.gestures.ModifierLocalScrollableContainerProvider.value
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gymtimer.ui.theme.GymTimerTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import org.w3c.dom.Text
import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.sin

class MainActivity : ComponentActivity() {
    private lateinit var mService: MainService
    private var mBound: Boolean = false

    var isTimerRunning =  MutableLiveData(false)
    var currentTimeLive = MutableLiveData<Long>(20000)

    //define callbacks for service binding, passed to bindService()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.v("kean", "service connected")
            val binder = service as MainService.MainBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName) {
            Log.v("kean", "service disconnected")
            mBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Test2(currentTimeLive, isTimerRunning)

//            Test(totalTime = currentTime, text = "test")
//            Surface (
//                color = Color(0xFF101010),
//                modifier = Modifier.fillMaxSize()
//                    ){
//                Box(
//                    contentAlignment = Alignment.Center
//                ){
//                    Timer(
//                        totalTime = currentTime,
//                        handleColour = Color.Green,
//                        inactiveBarColor = Color.DarkGray,
//                        activeBarColor = Color(0xFF37B900),
//                        modifier = Modifier.size(200.dp)
//                    )
//                }
//            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mBound) {
            isTimerRunning.postValue(mService.isTimerRunning)
            currentTimeLive.postValue(mService.currentTime)
            unbindService(connection)
            mBound = false
        }
    }

//    override fun onResume() {
//        Log.v("kean", "i am called")
//        if(mBound){
//            isTimerRunning = mService.isTimerRunning
//            currentTime = mService.currentTime
//        }
//        super.onResume()
//    }

    override fun onStop() {
        Log.v("kean", "onStop()")
        var serviceIntent = Intent(this, MainService::class.java)
        serviceIntent.action = "START"
        serviceIntent.putExtra("isTimerRunning", isTimerRunning.value)
        serviceIntent.putExtra("currentTime", currentTimeLive.value)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}

@Composable
fun Test2(
    time: LiveData<Long>,
    isTimerRunning: LiveData<Boolean>) {
    val test: Long? by time.observeAsState()
    val test2: Boolean? by isTimerRunning.observeAsState()
    Column() {
        Text(
            text = test.toString()
        )
        Text(
            text = test2.toString()
        )
    }
}

@Composable
fun Timer(
    totalTime: Long,
    handleColour: Color,
    inactiveBarColor: Color,
    activeBarColor: Color,
    modifier: Modifier = Modifier,
    initialValue: Float = 1f,
    strokeWidth: Dp = 5.dp
){
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    var value by remember {
        mutableStateOf(initialValue)
    }
    var currentTime by remember {
        mutableStateOf(totalTime)
    }
    var isTimerRunning by remember{
        mutableStateOf(false)
    }
    //whenever the key changes, rerun the code
    LaunchedEffect(key1 = currentTime, key2 = isTimerRunning){
        if(currentTime > 0 && isTimerRunning) {
            delay(100L)
            currentTime -= 100L
            value = currentTime / totalTime.toFloat()
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.onSizeChanged {
            size = it
        }
    ){
        Canvas(modifier = modifier){
            drawArc(
                color = inactiveBarColor,
                startAngle = -215f,
                sweepAngle = 250f,
                useCenter = false,
                size = Size(size.width.toFloat(), size.height.toFloat()),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)

            )
            drawArc(
                color = activeBarColor,
                startAngle = -215f,
                sweepAngle = 250f * value,
                useCenter = false,
                size = Size(size.width.toFloat(), size.height.toFloat()),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)

            )
            val center = Offset(size.width / 2f, size.height / 2f)
            val beta = (250f * value + 145f) * (PI / 180f).toFloat()
            val r = size.width / 2f
            val a = kotlin.math.cos(beta) * r
            val b = kotlin.math.sin(beta) * r
            drawPoints(
                listOf(Offset(center.x + a, center.y + b)),
                pointMode = PointMode.Points,
                color = handleColour,
                strokeWidth = (strokeWidth*3f).toPx(),
                cap = StrokeCap.Round
            )
        }
        Text(
            text=(currentTime/1000L).toString(),
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Button(
            onClick = {
                      if(currentTime <= 0L){
                          currentTime = totalTime
                          isTimerRunning = true
                      } else {
                          isTimerRunning = !isTimerRunning
                      }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(
                containerColor = if(!isTimerRunning || currentTime <= 0L){
                    Color.Green
                } else {
                    Color.Red
                }
            )
        ) {
            Text(text = if (isTimerRunning && currentTime > 0L) "Stop" else if (!isTimerRunning && currentTime > 0L) "Start" else "Restart")
        }
    }
}