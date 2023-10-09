package org.lihi.ursa

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalTime
import org.lihi.ursa.ui.testPatrons

const val NOLOAD=false
const val NUM_WASHERS = 9
const val STAFF_WASHERS = 2
const val DEFAULT_WASH_TIME = 28 * 60 * 1000L
const val LOAD_TIME = 10 * 60 * 1000L

class laundryTime {
    val localTime: LocalTime
    val timeStr: String

    constructor(timeString: String) {
        localTime = LocalTime.parse(timeString)
        timeStr = timeString
    }

    constructor(sTime: String, lTime: LocalTime) {
        localTime = lTime
        timeStr = sTime
    }
}

val WEEKDAY_LAUNDRY_TIMES = listOf(
    laundryTime("6:30 AM", LocalTime(6, 30)),
    laundryTime("7:30 AM", LocalTime(7, 30)),
    laundryTime("8:30 AM", LocalTime(8, 30)),
    laundryTime("9:15 AM", LocalTime(9, 30)),
    laundryTime("10:00 AM", LocalTime(10, 0)),
    laundryTime("10:45 AM", LocalTime(10, 45)),
    laundryTime("11:30 AM", LocalTime(11, 30)),
    laundryTime("12:15 PM", LocalTime(12, 15)),
    laundryTime("1:00 PM", LocalTime(13, 0)),
    laundryTime("1:45 PM", LocalTime(13, 45)),
    laundryTime("2:30 PM", LocalTime(14, 30)),
    laundryTime("3:15 PM", LocalTime(15, 15))
)

val WEEKEND_LAUNDRY_TIMES = listOf(
    laundryTime("8:30 AM", LocalTime(8, 30)),
    laundryTime("9:15 AM", LocalTime(9, 30)),
    laundryTime("10:00 AM", LocalTime(10, 0)),
    laundryTime("1:00 PM", LocalTime(13, 0)),
    laundryTime("1:45 PM", LocalTime(13, 45)),
    laundryTime("2:30 PM", LocalTime(14, 30)),
)

enum class WasherStatus {
    OPEN,
    LOADING,
    WASHING,
    FINISHED,
    NEEDS_SOAP,
    STAFF_WASHER
}

class LaundryPatron(
    name: String,
    birthday: String,
    var timeSlot: String,
    var signupTime: Long = 0,
    var washerNum: Int=-1,// MutableState<Int> = mutableStateOf(-1),
    var loadTime: Long=0L,//MutableState<Long> = mutableStateOf(0L),
    var startTime: Long=0L,//MutableState<Long> = mutableStateOf(0L),
    var washRunTime: Long= DEFAULT_WASH_TIME,//MutableState<Long> = mutableStateOf(DEFAULT_WASH_TIME),
    var outTime: Long=0L,//MutableState<Long> = mutableStateOf(0L),
    var washerRefilled: Boolean=false//MutableState<Boolean> = mutableStateOf(false)
) : BasicPatron(name, birthday)

class LaundryWasher (var num: Int, val status: MutableState<WasherStatus> = mutableStateOf(
    WasherStatus.OPEN
) ){
    var occupant: LaundryPatron? by mutableStateOf( null)
}

class LaundryListViewModel( dataFilePath: String="test", val appointmentTimes: List<laundryTime> = WEEKDAY_LAUNDRY_TIMES) {
    var laundryPatrons = mutableStateListOf<LaundryPatron>()
    var laundryWashers =
        Array<LaundryWasher>(NUM_WASHERS) { i ->
            LaundryWasher(
                NUM_WASHERS - i,
                status = mutableStateOf(if (i >= NUM_WASHERS - STAFF_WASHERS) WasherStatus.STAFF_WASHER else WasherStatus.OPEN)
            )
        }
    val laundryNumMap = laundryWashers.map { it.num to it }.toMap()
    var datarepo: CSVDataRepo? = null

    init{
        testPatrons.forEachIndexed{ ind, it -> addPatron(it.name,it.birthday, WEEKDAY_LAUNDRY_TIMES[ind% WEEKDAY_LAUNDRY_TIMES.size].timeStr)}
        datarepo = CSVDataRepo(dataFilePath)
        if (!NOLOAD) loadFromPatronList(datarepo!!.loadLaundryPatronList())
    }

    fun getPatronsAtTime(timeStr: String): List<LaundryPatron> {
        val filtPats = laundryPatrons.filter { it.timeSlot == timeStr }
        return filtPats
    }

    fun addPatron(name: String, bday: String, washTime: String) {
        val pat = LaundryPatron(name, bday, washTime, System.currentTimeMillis())
        val add = laundryPatrons.add(pat)
        datarepo?.saveLaundryPatronList(laundryPatrons)
    }

    fun loadFromPatronList(lpl: List<LaundryPatron>){
        // clear patron list an reset washers
        laundryPatrons.clear()
        for (i in 0..NUM_WASHERS -1) { laundryWashers[i].status.value = if (i >= NUM_WASHERS - STAFF_WASHERS) WasherStatus.STAFF_WASHER else WasherStatus.OPEN
                laundryWashers[i].occupant=null
            }
        lpl.forEach{ pat ->
            laundryPatrons.add(pat)
            if (pat.washerNum> 0 && pat.loadTime>0 && !pat.washerRefilled) {
                val washer=laundryWashers[NUM_WASHERS -pat.washerNum]
                washer.occupant=pat
                println("Wash#${pat.washerNum} -> $pat")
                // Patron using washer, calculate laundryWasher properties
                when {
                    pat.startTime==0L -> {
                        washer.status.value= WasherStatus.LOADING
                    }
                    pat.startTime+pat.washRunTime>System.currentTimeMillis() -> {
                        washer.status.value= WasherStatus.WASHING
                    }
                    pat.outTime==0L -> {
                        washer.status.value= WasherStatus.FINISHED
                    }
                    else -> {
                        washer.status.value= WasherStatus.NEEDS_SOAP
                    }
                }
                }
        }
    }

    fun deletePatron(timeSlot: String,delPatronIndex: Int) {
        val pat=getPatronsAtTime(timeSlot)[delPatronIndex]
        println("${laundryPatrons.size} ... del pat # $delPatronIndex")
        laundryPatrons.remove(pat)
        println("now ${laundryPatrons.size}  . $datarepo")
        datarepo?.saveLaundryPatronList(laundryPatrons)
    }

    fun editPatron(timeSlot: String, patronIndex: Int, patronName: String, patronBirthday: String, patronWashTime: String) {
            val pat=getPatronsAtTime(timeSlot)[patronIndex]
        pat.name=patronName
        pat.birthday=patronBirthday
        pat.timeSlot=patronWashTime
        datarepo?.saveLaundryPatronList(laundryPatrons)
    }

    fun assignWasher(pat: LaundryPatron, washNum: Int) {
        val wash = laundryNumMap.getOrDefault(washNum, laundryWashers[0])
        wash.status.value = WasherStatus.LOADING
        wash.occupant = pat
        pat.loadTime= System.currentTimeMillis()
        pat.washerNum= washNum
        datarepo?.saveLaundryPatronList(laundryPatrons)
    }

    fun unassignWasher(pat: LaundryPatron, washNum: Int) {
        val wash = laundryNumMap.getOrDefault(washNum, laundryWashers[0])
        wash.status.value = WasherStatus.OPEN
        wash.occupant = null
        pat.loadTime = 0
        pat.washerNum = -1
        datarepo?.saveLaundryPatronList(laundryPatrons)
    }

    fun washerStarted(washNum: Int) {
        val wash = laundryNumMap.getOrDefault(washNum, laundryWashers[0])
        val pat = wash.occupant
        if (pat != null) {
            wash.status.value = WasherStatus.WASHING
            pat.startTime = System.currentTimeMillis()
        } else {
            assert(pat != null)
        }
        datarepo?.saveLaundryPatronList(laundryPatrons)
    }

    fun adjWasherTime(washNum: Int, addedMinutes: Float) {
        val wash = laundryNumMap.getOrDefault(washNum, laundryWashers[0])
        val pat = wash.occupant
        if (pat != null) {
            pat.washRunTime += (addedMinutes * 1000 * 60).toLong()
        }
        datarepo?.saveLaundryPatronList(laundryPatrons)
    }

    fun washerFinished(washNum: Int) {
        val wash = laundryNumMap.getOrDefault(washNum, laundryWashers[0])
        wash.status.value = WasherStatus.FINISHED
        val pat = wash.occupant
        if (pat != null) {
            pat.washRunTime= System.currentTimeMillis() - pat.startTime
        }
        datarepo?.saveLaundryPatronList(laundryPatrons)
    }

    fun washerUnloaded(washNum: Int) {
        val wash = laundryNumMap.getOrDefault(washNum, laundryWashers[0])
        val pat = wash.occupant
        wash.status.value = WasherStatus.NEEDS_SOAP
        pat?.outTime = System.currentTimeMillis()
        datarepo?.saveLaundryPatronList(laundryPatrons)
    }

    fun washerReady(washNum: Int) {
        val wash = laundryNumMap.getOrDefault(washNum, laundryWashers[0])
        val pat = wash.occupant
        wash.status.value = WasherStatus.OPEN
        pat?.washerRefilled= true
        datarepo?.saveLaundryPatronList(laundryPatrons)

    }

}