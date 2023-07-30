package com.example.gymtimer

//import androidx.compose.foundation.gestures.ModifierLocalScrollableContainerProvider.value

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.data.EmptyGroup.data
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.delay
import java.lang.Math.PI
import java.time.chrono.MinguoChronology
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    private lateinit var mService: MainService
    private var mBound: Boolean = false

    var viewModel = MyViewModel()

    //define callbacks for service binding, passed to bindService()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.v("kean", "service connected")
            val binder = service as MainService.MainBinder
            mService = binder.getService()
            mBound = true
            viewModel.setInsideApp(0)
            mService.startRun(viewModel.getCurrentTime().value, viewModel.getTimerRunning().value)

        }

        override fun onServiceDisconnected(p0: ComponentName) {
            Log.v("kean", "service disconnected")
            mBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            Surface (
                color = Color.LightGray,
                modifier = Modifier.fillMaxSize()
                    ){
                Box(
                    contentAlignment = Alignment.Center,
                ){
                    Test2(viewModel)
                }
            }
        }
    }

    override fun onStart() {
        Log.v("kean", "onStart()")
        super.onStart()
        if (mBound) {
            viewModel.setTimerRunning(mService.isTimerRunning)
            viewModel.setCurrentTime(mService.currentTime)
            viewModel.setInsideApp(1)
            mService.pauseTimer()
            mService.clearNotification()
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
        serviceIntent.putExtra("TIMER_RUNNING", viewModel.getTimerRunning().value)
        serviceIntent.putExtra("currentTime", viewModel.getCurrentTime().value)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        viewModel.setInsideApp(0)

        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}

@Composable
fun Test2(
    viewModel: MyViewModel, ) {

    val currentTime: Long? by viewModel.getCurrentTime().observeAsState(10L)
    val isTimerRunning: Boolean? by viewModel.getTimerRunning().observeAsState(false)

//    //whenever the key changes, rerun the code
//    LaunchedEffect(key1 = currentTime, key2 = isTimerRunning){
//        if(currentTime!! > 0 && isTimerRunning == true) {
//            delay(100L)
//            val value = (currentTime!! - 100L)
//            viewModel.setCurrentTime(value)
//        }
//    }

    Column() {
//        var time = currentTime?.let { TimeUnit.MILLISECONDS.toSeconds(it) }
//        var minutes = time?.div(60)
//        var seconds = time?.rem(60)
//        var minutesText:String = if(minutes!! < 10){
//            "0${minutes}"
//        } else{
//            "$minutes"
//        }
//        var secondsText:String = if(seconds!! < 10){
//            "0${seconds}"
//        }else{
//            "$seconds"
//        }
//        var timerText = "$minutesText:$secondsText"
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box(
                modifier = Modifier.background(Color.Green),
                contentAlignment = Alignment.Center
            ){
                Row(verticalAlignment = Alignment.CenterVertically){
                    LimitedLazyColumn(items = (-2..61).toList(), limit = 5, timeUnit="m", viewModel)
                    Text(":", fontSize = 46.sp)
                    LimitedLazyColumn(items = (-2..61).toList(), limit = 5, timeUnit="s", viewModel)
                }
//                Text(
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .drawBehind {
//                            drawCircle(
//                                color = Color.Red,
//                                radius = this.size.maxDimension
//                            )
//                        },
//                    text=timerText,
//                    fontSize = 44.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White,
//                    textAlign = TextAlign.Center, )
            }

            Row(verticalAlignment = Alignment.CenterVertically){

                Button(onClick = { viewModel.getCurrentTime().value?.let { viewModel.setCurrentTime(it - 30000) } }){
                    Text(text = "-")
                }
                Button(onClick = { viewModel.setTimerRunning(!isTimerRunning!!)}) {
                    Text(text = if (isTimerRunning == true && currentTime!! > 0L) "Stop" else if (isTimerRunning == false && currentTime!! > 0L) "Start" else "Restart")
                }
                Button(onClick = { viewModel.getCurrentTime().value?.let { viewModel.setCurrentTime(it + 30000) } }){
                    Text(text = "+")
                }
            }
        }
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            LazyColumn() {
//                items(
//                    61,
//                    ) { index ->
//                    Text(text = if (index < 10) "0$index" else "$index",
//                            fontSize = 44.sp,)
//                }
//            }
//            Text(":", fontSize = 44.sp)
//            LazyColumn() {
//                items(61) { index ->
//                    Text(text = if (index < 10) "0$index" else "$index",
//                        fontSize = 44.sp,)
//                }
//            }
//        }




    }
}

@Composable
fun LimitedLazyColumn(items : List<Int>, limit : Int, timeUnit: String, viewModel: MyViewModel) {
    val itemHeightPixels = remember { mutableStateOf(0) }
    val listState = rememberLazyListState()

    val unit = remember{ mutableStateOf(-10) }

    var currentMin: Long
    var currentSec: Long
    var firstVisibleIndex by remember { mutableStateOf(-2) }

    val currentTime: Long? by viewModel.getCurrentTime().observeAsState(10L)
    val isTimerRunning: Boolean? by viewModel.getTimerRunning().observeAsState(false)
    val insideApp: Int? by viewModel.getInsideApp().observeAsState()

//    //whenever the key changes, rerun the code
//    LaunchedEffect(key1 = currentTime, key2 = isTimerRunning){
//        if(currentTime!! > 0 && isTimerRunning == true) {
//            delay(100L)
//            val value = (currentTime!! - 100L)
//            viewModel.setCurrentTime(value)
//        }
//    }

    LaunchedEffect(key1 = insideApp) {
        Log.v("kean", "the value has changed!!")
        if(currentTime != null && listState.layoutInfo.totalItemsCount != 0){
            currentMin = TimeUnit.MILLISECONDS.toSeconds(currentTime!!) / 60
            currentSec = TimeUnit.MILLISECONDS.toSeconds(currentTime!!)
            if (timeUnit == "m"){
                firstVisibleIndex = currentMin.toInt()
            }else{
                firstVisibleIndex = currentSec.toInt()
            }
            Log.v("kean", "here: "+firstVisibleIndex.toString())
            viewModel.setInsideApp(3)
        }
        listState.scrollToItem(firstVisibleIndex)
    }


    LazyColumn(
        state=listState,
        modifier = Modifier.height(pixelsToDp(pixels = itemHeightPixels.value * limit))
    ) {
        if (listState.layoutInfo.totalItemsCount != 0) {
            val centreItem = listState.layoutInfo.visibleItemsInfo.first().index
            if (unit.value != centreItem){
                if(timeUnit == "m"){
                    val currentTime = viewModel.getCurrentTime().value
                    if(currentTime != null){
                        var oldMinutes = TimeUnit.MILLISECONDS.toSeconds(currentTime) / 60
                        if (centreItem != oldMinutes.toInt()) {
                            val new = currentTime + (centreItem - oldMinutes) * 60 * 1000
                            viewModel.setCurrentTime(new)
                        }
                    }

                }else{
                    val currentTime = viewModel.getCurrentTime().value
                    if(currentTime != null){
                        var oldSeconds = TimeUnit.MILLISECONDS.toSeconds(currentTime)
                        if (centreItem != oldSeconds.toInt()) {
                            val new = currentTime + (centreItem - oldSeconds) *1000
                            viewModel.setCurrentTime(new)
                        }
                    }
                }
                unit.value = centreItem
            }

        }
        items(items) { item ->
            var textString = if(item < 0 || item > 59){
                ""
            }else if(item < 10){
                "0$item"
            }else{
                "$item"
            }
            Text(
                text = textString,
                        fontSize = 44.sp,
                modifier = Modifier.onSizeChanged { size -> itemHeightPixels.value = size.height }
            )
        }
    }
}

@Composable
private fun pixelsToDp(pixels: Int) = with(LocalDensity.current) { pixels.toDp() }

@Composable
fun Timer(
    viewModel: MyViewModel,
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

    val time: Long? by viewModel.getCurrentTime().observeAsState()

    var currentTime by remember{
        mutableStateOf(time)
    }

    val isTimerRunning by viewModel.getTimerRunning().observeAsState()
    var timerRunning by remember{
        mutableStateOf(isTimerRunning)
    }

    //whenever the key changes, rerun the code
    LaunchedEffect(key1 = currentTime, key2 = isTimerRunning){
        if(currentTime!! > 0 && timerRunning == true) {
            delay(100L)
            currentTime = currentTime!! - 100L
            value = currentTime!! / 180F
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
            text=(currentTime?.div(1000L)).toString(),
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Button(
            onClick = {
                      if(currentTime!! <= 0L){
                          currentTime = 180
                          timerRunning = true
                      } else {
                          timerRunning = !timerRunning!!
                      }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(
                containerColor = if(!timerRunning!! || currentTime!! <= 0L){
                    Color.Green
                } else {
                    Color.Red
                }
            )
        ) {
            Text(text = if (timerRunning!! && currentTime!! > 0L) "Stop" else if (timerRunning!! && currentTime!! > 0L) "Start" else "Restart")
        }
    }
}