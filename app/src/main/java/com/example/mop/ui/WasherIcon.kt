package com.example.ursa.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mop.ui.FacilityIcon
import com.example.mop.ui.TimeWidget
import com.example.ursa.LaundryListViewModel
import com.example.ursa.LaundryPatron
import com.example.ursa.R
import com.example.ursa.WasherStatus
import java.text.SimpleDateFormat

@Composable
fun WasherIcon(
    laundryvm: LaundryListViewModel,
    num: Int,
    status: WasherStatus,
    pat: LaundryPatron?,
    staffWasher: Boolean = false
) {
    val formatter = SimpleDateFormat("hh:mm a")
    val image = when (status) {
        WasherStatus.WASHING -> R.drawable.washerrun
        WasherStatus.LOADING -> R.drawable.washerload
        WasherStatus.FINISHED -> R.drawable.washerdone
        else -> (if (staffWasher) R.drawable.washerstaff else R.drawable.washeropen)
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .padding(5.dp)
            .width(112.dp)
    ) {
        var statusText = when (status) {
            WasherStatus.OPEN -> "Open"
            WasherStatus.LOADING, WasherStatus.WASHING, WasherStatus.FINISHED -> pat?.name
                ?: "Invalid Patron"

            WasherStatus.NEEDS_SOAP -> "Needs Soap/Cleaning"
            WasherStatus.STAFF_WASHER -> "Staff Washer"
        }
        var detailText: String = when (status) {
            WasherStatus.LOADING -> pat?.let { "Loading at ${formatter.format(pat.loadTime)}" }
                ?: "Error"

            WasherStatus.WASHING -> pat?.let { "Started at ${formatter.format(pat.startTime)}" }
                ?: "Error"

            WasherStatus.FINISHED -> pat?.let { "Finished at ${formatter.format(pat.startTime + pat.washRunTime)}" }
                ?: "Error"
            else -> ""
        }

        FacilityIcon(
            num = num,
            image = image,
            statusText = statusText,
            detailText = detailText,
            content = {
                when (status){
                    WasherStatus.OPEN -> {}
                    WasherStatus.LOADING -> {
                        if (pat != null) {
                            TimeWidget(targetTime = pat.loadTime, adjButtons = false, getTimeUpdate = {pat.loadTime}, countUp = true)
                            Button(onClick = { laundryvm.washerStarted(num) }) {
                                Text("Start")
                            }
                        } else {}
                    }
                    WasherStatus.WASHING -> {
                        if (pat != null) {
                            TimeWidget(targetTime = pat.loadTime+pat.washRunTime, adjButtons = true, onAdjButtonClick = {it -> laundryvm.adjWasherTime(num,it)}, getTimeUpdate = {pat.loadTime+pat.washRunTime})
                            Button(onClick = { laundryvm.washerFinished(num)  }) {
                                Text("Done")
                            }
                        } else {}
                    }
                    WasherStatus.FINISHED -> {
                        if (pat != null) {
                            TimeWidget(targetTime = pat.loadTime+pat.washRunTime, adjButtons = false, getTimeUpdate = {pat.loadTime+pat.washRunTime}, countUp = true)
                            Button(onClick = { laundryvm.washerUnloaded(num) }) {
                                Text("Unloaded")
                            }
                        } else {}
                    }
                    WasherStatus.NEEDS_SOAP -> {
                        Button(onClick = { laundryvm.washerReady(num) }) {
                            Text("Detergent / Clean", fontSize = 10.sp)
                        }
                    }
                    else -> {Text("")}
                }
            })
        }


        /*
                    Text("Loading at $inTime", textAlign = TextAlign.Center, fontSize = 10.sp)
                    var timeElapsed =
                        remember { mutableStateOf(timeStatusStr(pat.loadTime, true)) }
                    Text(
                        timeElapsed.value,
                        color = if (loadLate) Color.Red else Color.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    LaunchedEffect(0) { // 3
                        while (true) {
                            val timestr = timeStatusStr(pat.loadTime, true)
                            timeElapsed.value = timestr
                            delay(1000)
                        }
                    }
                    Button(onClick = { laundryvm.washerStarted(num) }) {
                        Text("Start")
                    }

            WasherStatus.FINISHED -> {
                if (pat != null) {
                    val doneTime = formatter.format(pat.startTime + pat.washRunTime)
                    Text(pat.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Finished at  $doneTime", textAlign = TextAlign.Center, fontSize = 10.sp)
                    var timeElapsed = remember {
                        mutableStateOf(
                            timeStatusStr(
                                pat.startTime + pat.washRunTime,
                                true
                            )
                        )
                    }
                    LaunchedEffect(0) { // 3
                        while (true) {
                            val pst = pat.startTime
                            val wrt = pat.washRunTime
                            val timestr =
                                timeStatusStr(pat.startTime + pat.washRunTime, true)
                            timeElapsed.value = "$timestr"
                            delay(1000)
                        }
                    }
                    Text(
                        timeElapsed.value,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Button(onClick = { laundryvm.washerUnloaded(num) }) {
                        Text("Unloaded")
                    }
                } else {
                    Text("ERROR - Patron not found")
                }
            }

            WasherStatus.WASHING -> {
                if (pat != null) {
                    val onTime = formatter.format(pat.startTime)
                    val loadDone =
                        System.currentTimeMillis() > pat.startTime + pat.washRunTime
                    Text(pat.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Started: $onTime", textAlign = TextAlign.Center, fontSize = 13.sp)

                    var timeLeft =
                        remember { mutableStateOf(timeStatusStr(pat.startTime + pat.washRunTime)) }
                    LaunchedEffect(0) { // 3
                        while (true) {
                            val timestr = timeStatusStr(pat.startTime + pat.washRunTime)
                            timeLeft.value = timestr
                            delay(1000)
                        }
                    }
                    Text(
                        timeLeft.value,
                        color = if (loadDone) Color.Red else Color.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Button(onClick = {
                            laundryvm.adjWasherTime(num, 1.0F)
                            val timestr = timeStatusStr(pat.startTime + pat.washRunTime)
                            timeLeft.value = timestr
                        }, content = { Text("+") }, modifier = Modifier.weight(1f).padding(0.dp))
                        Button(
                            onClick = {
                                laundryvm.adjWasherTime(num, -1.0F)
                                val timestr =
                                    timeStatusStr(pat.startTime + pat.washRunTime)
                                timeLeft.value = timestr
                            },
                            content = {
                                Text(
                                    "-",
                                    modifier = Modifier.padding(0.dp)
                                        .align(Alignment.CenterVertically)
                                )
                            },
                            modifier = Modifier.weight(1f).padding(0.dp)
                        )
                    }
                    Button(onClick = { laundryvm.washerFinished(num) }) {
                        Text("Done")
                    }
                } else {
                    Text("ERROR - Patron not found")
                }
            }

            WasherStatus.NEEDS_SOAP -> {
                Text("Empty")
                Button(onClick = { laundryvm.washerReady(num) }) {
                    Text("Detergent / Clean", fontSize = 10.sp)
                }
            }

            WasherStatus.STAFF_WASHER -> {
                Text("STAFF WASHER")
            }*/
    }

