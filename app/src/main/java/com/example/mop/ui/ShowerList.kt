package com.example.ursa.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ursa.BasicPatron
import com.example.ursa.MaskVisualTransformation
import com.example.ursa.R
import com.example.ursa.ShowerListModel
import com.example.ursa.ShowerState
import com.example.ursa.ui.theme.MopTheme
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.abs
import kotlin.time.DurationUnit
import kotlin.time.toDuration


var barList = mutableListOf<BasicPatron>()


object BarList {
    init {

    }
}



@Composable
fun ShowerPatronList(showerlm: ShowerListModel) {
    var selectedItemIndex = remember { mutableStateOf(0) }
    var patrons = showerlm.showerPatrons
    val delPatronIndex = remember { mutableStateOf(-1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val formatter = SimpleDateFormat("hh:mm a")
    val lfm = LocalFocusManager.current

    Column {
        Row {
            LazyColumn {

                itemsIndexed(patrons) { positionIndex, pat ->
                    //ShowerListPatron(name=pat.name, birthday =pat.birthday , signupTime =pat.signupTime, positionIndex = index )
                    val timestamp = (System.currentTimeMillis())
                    val wait = (timestamp - pat.signupTime) / 1000 / 60
                    val spotNum = positionIndex + 1
                    var isExpanded =
                        (selectedItemIndex.value == positionIndex)//by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.padding(4.dp)) {
                        Surface(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(size = 4.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Column(modifier = Modifier
                                .focusable(true)
                                .fillMaxWidth()
                                .clickable {
                                    lfm.clearFocus()
                                    selectedItemIndex.value = positionIndex
                                }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .width(50.dp)
                                            .drawBehind {
                                                drawCircle(
                                                    color = Color.Magenta, radius = 30.0f
                                                )
                                            }, text = "$spotNum", textAlign = TextAlign.Center
                                    )
                                    Column() {
                                        Row {
                                            Text(
                                                text = pat.name,
                                                fontSize = 25.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (pat.outTime > 0) Color.Gray else Color.Black
                                            )
                                            if (isExpanded) {
                                                Image(
                                                    painterResource(R.drawable.edit_square_fill0_wght600_grad0_opsz40
                                                    ),
                                                    "Edit",
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .padding(4.dp)
                                                        .clickable(true, "Edit") {
                                                            delPatronIndex.value = positionIndex
                                                            showEditDialog = true
                                                        }
                                                )
                                            }
                                        }
                                        Text(
                                            text = pat.birthday,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(24.dp))

                                    if (pat.inTime == 0L) {

                                        Text(
                                            //modifier = Modifier.weight(3f),
                                            text = "waiting $wait min",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    } else {
                                        var snum = pat.showerNum
                                        var fintime = formatter.format(Date(pat.inTime))
                                        if (pat.outTime == 0L) {
                                            Text(
                                                modifier = Modifier.weight(3f),
                                                text = "In shower #$snum \n at $fintime.",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        } else {
                                            var fouttime = formatter.format(Date(pat.outTime))
                                            Text(
                                                modifier = Modifier.weight(3f),
                                                text = "Showered in #$snum. \n In at $fintime, out by $fouttime",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Row(horizontalArrangement=Arrangement.SpaceEvenly,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(all = 8.dp)
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                if (pat.inTime > 0) {
                                                    if (pat.outTime > 0) {
                                                        Row(horizontalArrangement=Arrangement.SpaceEvenly,
                                                            modifier = Modifier
                                                                .align(Alignment.CenterStart).fillMaxWidth()
                                                                .padding(6.dp)
                                                        ) {
                                                            Button(onClick = {if (pat.showerNum > 0) {
                                                                showerlm.showerRooms[pat.showerNum - 1].status =
                                                                    ShowerState.OPEN
                                                            }
                                                                pat.inTime = 0
                                                                pat.showerNum = 0}) { Text("Return to Queue") }

                                                        }
                                                    } else {
                                                        Row(horizontalArrangement=Arrangement.SpaceEvenly,
                                                            modifier = Modifier
                                                                .align(Alignment.CenterStart)
                                                                .padding(6.dp).fillMaxWidth()
                                                        ) {
                                                            Button(onClick = {
                                                                showerlm.emptyShower(
                                                                    pat.showerNum
                                                                )
                                                            }) { Text("Out") }
                                                            Button(onClick = {
                                                                if (pat.showerNum > 0) {
                                                                    showerlm.showerRooms[pat.showerNum - 1].status =
                                                                        ShowerState.OPEN
                                                                }
                                                                pat.inTime = 0
                                                                pat.showerNum = 0
                                                            }) { Text("Return to Queue") }
                                                        }
                                                    }
                                                } else {
                                                    Row(
                                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        listOf(1, 2, 3, 4, 5).forEach {
                                                            val room = showerlm.showerRooms[it - 1]
                                                            Button(enabled = (room.status == ShowerState.OPEN),
                                                                onClick = {
                                                                    showerlm.putPatronInShower(
                                                                        pat, it
                                                                    )
                                                                }) { Text(text = "Send to\nShower #$it") }
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (isExpanded) {
                                Box(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Image(painterResource(R.drawable.delete_fill0_wght600_grad0_opsz40),
                                        "Delete",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .padding(4.dp)
                                            .align(Alignment.Center)
                                            .clickable(true) {
                                                delPatronIndex.value = positionIndex
                                                showDeleteDialog = true
                                            })
                                }
                            }
                        }
                    }
                }
            }
            if (showDeleteDialog && delPatronIndex.value >= 0) {
                val delPatName = showerlm.showerPatrons[delPatronIndex.value].name
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you sure you want to delete $delPatName?") },
                    confirmButton = {
                        Button(onClick = {
                            showerlm.deletePatron(delPatronIndex.value)
                            showDeleteDialog = false
                            delPatronIndex.value = -1
                        }) { Text("Delete") }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showDeleteDialog = false
                        }) { Text("Cancel") }
                    },
                )
            }
            if (showEditDialog && delPatronIndex.value >= 0) {
                val pat = showerlm.showerPatrons[delPatronIndex.value]
                val edPatName = remember { mutableStateOf(pat.name) }
                val edPatBday = remember { mutableStateOf(pat.birthday.filter { it.isDigit() }) }
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = {
                        Text(
                            "Edit Patron: ${pat.name}",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = edPatName.value,
                                onValueChange = { edPatName.value = it },
                                label = { Text("Patron Name") })
                            OutlinedTextField(
                                value = edPatBday.value,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                                ),
                                onValueChange = {
                                    edPatBday.value =
                                        if (it.length <= DATE_LENGTH) it else (it.substring(
                                            0,
                                            DATE_LENGTH
                                        ))
                                },
                                label = { Text(text = "Birthday") },
                                visualTransformation = MaskVisualTransformation(DATE_MASK)
                            )

                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            showerlm.editPatron(
                                delPatronIndex.value, edPatName.value,
                                formatBdayText(edPatBday.value)
                            )
                            showDeleteDialog = false
                            delPatronIndex.value = -1
                        }) { Text("Confirm") }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showDeleteDialog = false
                        }) { Text("Cancel") }
                    },
                )
            }
        }
    }
}

@Composable
fun ShowerListScreen(dataFilePath: String) {
    var patronliststate = remember { ShowerListModel() }
    ShowerListScreen(dataFilePath, patronliststate)
}

@Composable
fun ShowerListScreen(dataFilePath: String, patronliststate: ShowerListModel) {
    val image = painterResource(R.drawable.showerneedsclean)
    var formatter = DateTimeFormatter.ofPattern("MM-dd-yy")
    var rooms = patronliststate.showerRooms
    println("FILE PATH IS: $dataFilePath")

    //print("$rjsontxt")


    Column() {
        Column(modifier = Modifier.weight(2f)) {
            ShowerPatronList(patronliststate)
        }
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            rooms.forEachIndexed { roomid, room ->
                Divider(
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxHeight(.95f)  //fill the max height
                        .width(2.dp)
                )
                ShowerIcon(
                    roomid + 1,
                    room.status,
                    occupant = room.occupant.name,
                    timein = room.occupant.inTime,
                    timeout = room.outtime,
                    room = room,
                    showerlm = patronliststate
                )

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MopTheme {
        ShowerListScreen("Android")
    }
}
