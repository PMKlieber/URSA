@file:OptIn(ExperimentalMaterialApi::class)

package org.lihi.ursa.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import org.lihi.ursa.BasicPatron
import org.lihi.ursa.LaundryListViewModel
import org.lihi.ursa.MaskVisualTransformation
import org.lihi.ursa.NUM_WASHERS
import org.lihi.ursa.STAFF_WASHERS
import org.lihi.ursa.WEEKDAY_LAUNDRY_TIMES
import org.lihi.ursa.WasherStatus
import org.lihi.ursa.ui.theme.Shapes
import org.lihi.ursa.ursa.R
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.random.Random

val fakeNames = listOf<String>(
    "Quinn Pruitt",
    "Pamela Howard",
    "Cali Orr",
    "Heidi Smith",
    "Jerome Flowers",
    "Izaiah Gardner",
    "Emelia Wallace",
    "Madalynn Hunt",
    "Zion Mahoney",
    "Brady Montgomery",
    "Karina Schneider",
    "Conrad Ellis"
)

var testPatrons = fakeNames.map {
    BasicPatron(
        it,
        SimpleDateFormat("MM-dd-yy").format(Date(Random.nextLong(System.currentTimeMillis())))
    )
}


val numimgs =
    listOf(
        R.drawable.n1,
        R.drawable.n2,
        R.drawable.n3,
        R.drawable.n4,
        R.drawable.n5,
        R.drawable.n6,
        R.drawable.n7,
        R.drawable.n8,
        R.drawable.n9
    )


@Composable
fun WasherList(laundryvm: LaundryListViewModel) {
    LazyRow {
        itemsIndexed(laundryvm.laundryWashers) { index, washer ->
            val washNum = NUM_WASHERS - index
            WasherIcon(laundryvm, washNum, washer.status.value,washer.occupant,staffWasher = (washNum <= STAFF_WASHERS))
        }

    }
}

@Composable
fun LaundryPatronList(laundryvm: LaundryListViewModel, timeStr: MutableState<String>) {
    val selTime = timeStr.value
    val patrons = laundryvm.getPatronsAtTime(selTime)
    val selectedItemIndex = remember { mutableStateOf(-1) }
    val lfm = LocalFocusManager.current
    val delPatronIndex = remember { mutableStateOf(-1) }
    val edPatronIndex = remember { mutableStateOf(-1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

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
                                            color = Color.Magenta,
                                            radius = 30.0f
                                        )
                                    },
                                text = "$spotNum", textAlign = TextAlign.Center
                            )
                            Column(modifier = Modifier.weight(4f)) {
                                Row {
                                    Text(
                                        text = pat.name,
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isExpanded) {
                                        Image(
                                            painterResource(R.drawable.pen_square__1_),
                                            "Edit",
                                            modifier = Modifier
                                                .size(32.dp).padding(4.dp)
                                                .clickable(true, "Edit") {
                                                    edPatronIndex.value = positionIndex
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
                            Column {
                                if (pat.washerNum > 0) {
                                    Text("Assigned Washer ${pat.washerNum}")
                                }

                            }
                        }

                        if (isExpanded) {
                            if (pat.washerNum == -1) {
                                Text("Assign to Washer: ")
                                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                    laundryvm.laundryWashers.forEach { wash ->
                                        var i = wash.num
                                        val stat = wash.status.value
                                        val enabled = when (stat) {
                                            WasherStatus.OPEN -> true
                                            WasherStatus.STAFF_WASHER -> true
                                            WasherStatus.NEEDS_SOAP -> true
                                            else -> false
                                        }
                                        Button(
                                            enabled = enabled,
                                            onClick = { laundryvm.assignWasher(pat, i) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("#$i")
                                        }
                                    }
                                }
                            } else if (pat.startTime == 0L) {
                                Row{
                                Button(onClick = {
                                    laundryvm.unassignWasher(
                                        pat,
                                        pat.washerNum
                                    )
                                }) {
                                    Text("Unassign Washer")
                                }
                                Button(onClick = { laundryvm.washerStarted(pat.washerNum) }) {
                                    Text("Washer Started")
                                }}
                            }

                        }
                    }
                }
                if (isExpanded) {
                    Box(modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopEnd)) {
                        Image(painterResource(R.drawable.trash),
                            "Delete",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(0.dp)
                                .align(Alignment.TopEnd)
                                .clickable(true) {
                                    delPatronIndex.value = positionIndex
                                    showDeleteDialog = true
                                })
                    }
                }
            }
        }
    }
    if (showDeleteDialog && delPatronIndex.value >= 0) {
        val delPatName = laundryvm.laundryPatrons[delPatronIndex.value].name
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete $delPatName?") },
            confirmButton = {
                Button(onClick = {
                    laundryvm.deletePatron(selTime,delPatronIndex.value)
                    showDeleteDialog = false
                    delPatronIndex.value = -1
                }) { Text("Delete") }
            },
            dismissButton = { Button(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
        )
    }

    if (showEditDialog && edPatronIndex.value >= 0) {
        val pat = laundryvm.getPatronsAtTime(selTime)[edPatronIndex.value]
        val edPatName = remember { mutableStateOf(pat.name) }
        val edPatBday = remember { mutableStateOf(pat.birthday.filter { it.isDigit() }) }
        val edPatTime = remember { mutableStateOf(pat.timeSlot)}
        var expandedd by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Column {
                    Text(
                        "Edit Patron: ${pat.name}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            },
            text = {
                Column( horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = edPatName.value,
                        onValueChange = { edPatName.value = it },
                        singleLine = true,
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



                    TimeDropdown(edPatTime.value, onSelect = {edPatTime.value=it})
                    }},


            buttons = {
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier
                    .padding(8.dp)
                    .width(200.dp)){
                    Button(onClick = {
                        laundryvm.editPatron(
                            selTime,
                            edPatronIndex.value,
                            edPatName.value,
                            formatBdayText(edPatBday.value),
                            edPatTime.value
                        )
                        showDeleteDialog = false
                        edPatronIndex.value = -1
                    }) { Text("Confirm") }


                    Button(onClick = {
                        showEditDialog = false
                    }
                    ) { Text("Cancel") }
                }
            }, shape = Shapes.large

        )
    }
}

@Composable
fun LaundryListScreen(dataFilePath: String, selTime: MutableState<String> ) {
    val laundryvm = remember { LaundryListViewModel() }
    LaundryListScreen(dataFilePath, selTime, laundryvm)
}

@Composable
fun LaundryListScreen(dataFilePath: String, selTime: MutableState<String>, laundryvm: LaundryListViewModel) {
    val lfm = LocalFocusManager.current
    println("FILE PATH IS: $dataFilePath")
   /* if (dataFilePath=="test"){
        testPatrons.forEachIndexed{ind, it -> laundryvm.addPatron(it.name,it.birthday, WEEKDAY_LAUNDRY_TIMES[ind% WEEKDAY_LAUNDRY_TIMES.size].timeStr)}
    } else {
        var g = Gson()
        val datarepo = CSVDataRepo(dataFilePath)
        //val rfile = File("showerlist.json")
        //var rjsontxt=rfile.readText()
    }
*/
    Column{
        Row(modifier = Modifier.weight(2f)) {
            Column {
                TimeTabs(selTime,laundryvm.appointmentTimes)
            }
            Column(verticalArrangement = Arrangement.Top) {
                LaundryPatronList(laundryvm, selTime)
            }
        }
        Row(modifier = Modifier.weight(1f)) {
            WasherList(laundryvm)
        }
    }
}


@Preview
@Composable
fun LaundryScreenPreview(){
    val time = remember {mutableStateOf(WEEKDAY_LAUNDRY_TIMES[0].timeStr)}
    LaundryListScreen(dataFilePath = "test",time)
    }
