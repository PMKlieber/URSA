package com.example.ursa.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mop.ui.FacilityIcon
import com.example.mop.ui.TimeWidget
import com.example.ursa.R
import com.example.ursa.ShowerListModel
import com.example.ursa.ShowerPatron
import com.example.ursa.ShowerRoom
import com.example.ursa.ShowerState
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date

@Composable
fun ShowerIcon(
    num: Int,
    status: ShowerState,
    occupant: String = "",
    timein: Long = 0,
    timeout: Long = 0,
    room: ShowerRoom,
    showerlm: ShowerListModel)  {
    var formatter = DateTimeFormatter.ofPattern("hh:mm")

    val image = when (status) {
        ShowerState.OPEN -> R.drawable.showeropen
        ShowerState.INUSE -> R.drawable.showerinuse
        ShowerState.DIRTY -> R.drawable.showerneedsclean
    }

    val statusText = when (status) {
        ShowerState.OPEN -> "Open"
        ShowerState.INUSE -> occupant
        ShowerState.DIRTY -> "Needs Cleaning"
    }

    var detailText = ""

    if (room.status == ShowerState.INUSE) {
        val timeformat = SimpleDateFormat("hh:mm a")
        val timenow = System.currentTimeMillis()
        val intime = timeformat.format(Date(timein))
        val outtime = timeformat.format(Date(room.outtime))
        val warningTime = 1000 * 60 * 5
        detailText = "In at $intime\nOut by $outtime"
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .padding(5.dp)
            .width(112.dp))
    {


            FacilityIcon(
                num, image, statusText, detailText,
                content = {
                    when (status) {
                        ShowerState.INUSE -> {
                            TimeWidget(
                                room.outtime,
                                onAdjButtonClick = { m -> showerlm.adjustTimeLimit(num, m) },
                                getTimeUpdate = { room.outtime })

                            Button(onClick = { showerlm.emptyShower(num) }) {
                                Text("Out")
                            }

                        }

                        ShowerState.DIRTY -> {
                            Button(onClick = {
                                showerlm.cleanShower(num)
                            }) {
                                Text("Clean")
                            }
                        }

                        else -> {}
                    }
                },
            )

        }
    }

@Preview
@Composable
fun ShowerPreview() {
    ShowerIcon(
        num = 1,
        status = ShowerState.INUSE,
        showerlm = ShowerListModel("test"),
        occupant = "Bo Narley",
        room = ShowerRoom(
            ShowerState.INUSE,
            ShowerPatron(
                "Bo Narley",
                "420/69",
                System.currentTimeMillis() - 60 * 10 * 1000,
                1,
                System.currentTimeMillis() - 60 * 5 * 1000
            ), 0
        )
    )
}