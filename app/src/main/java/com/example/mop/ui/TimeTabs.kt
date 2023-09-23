package com.example.ursa.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ursa.R
import com.example.ursa.WEEKDAY_LAUNDRY_TIMES
import com.example.ursa.ui.theme.Purple200
import com.example.ursa.ui.theme.Shapes
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

const val NIGHT_TIME_DEBUG=false
val WASHTIME_GRACE_PERIOD = 15.minutes

@Composable
fun TimeTabs(selTime: MutableState<String>) {
    val washTimes = WEEKDAY_LAUNDRY_TIMES
    val nowInst = if (!NIGHT_TIME_DEBUG) {Clock.System.now()} else {Clock.System.now()+12.hours}
    val nowDate = nowInst.toLocalDateTime(TimeZone.currentSystemDefault()).date
    var nowTime = nowInst.toLocalDateTime(TimeZone.currentSystemDefault()).time
    if (NIGHT_TIME_DEBUG){
        nowTime= LocalTime((nowTime.hour+12)%24,nowTime.minute,nowTime.second,nowTime.nanosecond)
    }
    Column(
        verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier
            .width(128.dp)
            .padding(0.dp)
            .fillMaxHeight()
    ) {
        Divider(thickness = 2.dp)
        var prevTime: Instant? = null
        var hasCurTime = false
        washTimes.forEachIndexed { ind, washtime ->
            val timeStr = washtime.timeStr
            val washLocalTime = washtime.localTime
            val washInst =
                LocalDateTime(nowDate, washLocalTime).toInstant(TimeZone.currentSystemDefault())
            var isCurTime = false
            if (washInst > nowInst-WASHTIME_GRACE_PERIOD && !hasCurTime) {
                    hasCurTime=true
                        isCurTime = true
                }

            val topb = if (ind == 0) 2.dp else 1.dp
            val bottomb = if (ind == WEEKDAY_LAUNDRY_TIMES.size - 1) 2.dp else 1.dp
            val endb = if (selTime.value == timeStr) 0.dp else 2.dp

            Box(modifier = Modifier.weight(1f)) {
                Tab(
                    selected = (selTime.value == timeStr),
                    text = {
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {

                            Text(
                                timeStr,
                                fontWeight = if (selTime.value == timeStr) FontWeight.Black else FontWeight.Normal,
                                modifier = Modifier
                                    .padding(0.dp)
                                    .fillMaxWidth()
                            )
                        }
                    },
                    onClick = {
                        selTime.value = timeStr
                    },
                    selectedContentColor = Color(0, 0, 0),
                    unselectedContentColor = Color(0, 0, 0),
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    if (selTime.value == timeStr) Purple200 else MaterialTheme.colors.background,
                                    MaterialTheme.colors.background
                                )
                            ), shape = Shapes.large
                        )
                        .padding(1.dp)
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth()
                )
                Divider(
                    modifier = Modifier
                        .width(if (selTime.value == timeStr) 0.dp else 2.dp)
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                )


                if (isCurTime)
                {
                        Image(
                            alignment = Alignment.BottomStart,
                            painter = painterResource(R.drawable.baseline_arrow_circle_right_24),
                            contentDescription = "Current Time Slot:",
                            modifier= Modifier
                                .align(Alignment.CenterStart)
                                .padding(4.dp)
                        )
                }
            }
            Divider(thickness = 2.dp)
            prevTime = washInst
        }
    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
    val td = remember {mutableStateOf("12:00 PM")}
    TimeTabs(td)
}