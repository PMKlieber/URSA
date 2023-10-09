package org.lihi.ursa

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.lihi.ursa.ui.testPatrons

class ShowerPatron(
    name: String,
    birthday: String,
    var signupTime: Long,
    var showerNum: Int = 0,
    var inTime: Long = 0,
    var timeGiven: Long = 1000 * 60 * 20,
    var outTime: Long = 0,
    var roomCleaned: Boolean = false,
    var checkFromLaundryList: Boolean = false
) : BasicPatron(name, birthday)

enum class ShowerState {
    OPEN, INUSE, DIRTY
}

class ShowerRoom {
    var status: ShowerState by mutableStateOf(ShowerState.OPEN)
    var occupant: ShowerPatron? by mutableStateOf<ShowerPatron?>(null)
    var outtime: Long by mutableStateOf(0)

    constructor()
    constructor(status: ShowerState, occupant: ShowerPatron, outtime: Long) {
        this.status = status
        this.occupant = occupant
        this.outtime = outtime
    }


}

class ShowerListModel(dataFilePath: String="test") : ViewModel() {
    var datarepo: CSVDataRepo? = CSVDataRepo(dataFilePath)
    var showerPatrons = mutableStateListOf<ShowerPatron>()
    var showerRooms = Array<ShowerRoom>(5) { _ -> ShowerRoom() }

    init{
        if (!NOLOAD && dataFilePath!="test"){
            loadFromPatronList(datarepo!!.loadShowerPatronList())
        }
        else{
           testPatrons.forEach{showerPatrons.add(ShowerPatron(it.name,it.birthday,System.currentTimeMillis()))}
        }
    }

    fun containsPatron(name: String, birthday: String) : Boolean{
        for (pat in showerPatrons){
            if (pat.name==name && pat.birthday==birthday)
                return true
        }
        return false
    }

    fun loadFromPatronList(nsl: List<ShowerPatron>) {
        showerPatrons.clear()
        showerPatrons.addAll(nsl)
        nsl.filter { it.inTime > 0L && it.outTime == 0L }.forEach {
            val room = showerRooms[it.showerNum - 1]
            room.status = ShowerState.INUSE
            room.occupant = it
            room.outtime = it.inTime + it.timeGiven
        }
        nsl.filter { it.outTime > 0L && !it.roomCleaned }.forEach {
            val room = showerRooms[it.showerNum - 1]
            room.occupant = it
            room.status = ShowerState.DIRTY
        }
    }

    fun addPatronToQueue(name: String, bday: String) {
        showerPatrons.add(
            ShowerPatron(
                name, bday, System.currentTimeMillis()
            )
        )
        datarepo?.saveShowerPatronList(showerPatrons)
    }

    fun putPatronInShower(pat: ShowerPatron, showerNum: Int) {
        val room = showerRooms[showerNum - 1]
        room.status = ShowerState.INUSE
        room.occupant = pat
        pat.showerNum = showerNum
        pat.inTime = System.currentTimeMillis()
        room.outtime = System.currentTimeMillis() + 1000 * 60 * 20
        datarepo?.saveShowerPatronList(showerPatrons)
    }

    fun returnPatronToQueue(pat: ShowerPatron, showerNum: Int) {
        pat.showerNum=0
        pat.inTime=0
        pat.outTime=0
        if (showerNum > 0) {
            val room = showerRooms[showerNum - 1]
            room.status = ShowerState.OPEN
            room.occupant = ShowerPatron("", "", 0)
            room.outtime = 0
        }
        datarepo?.saveShowerPatronList(showerPatrons)
    }

    fun emptyShower(roomNum: Int) {
        val room = showerRooms[roomNum - 1]
        room.occupant?.outTime = System.currentTimeMillis()
        room.status = ShowerState.DIRTY
        datarepo?.saveShowerPatronList(showerPatrons)
    }

    fun cleanShower(roomNum: Int) {
        val room = showerRooms[roomNum - 1]
        room.occupant?.roomCleaned = true
        room.status = ShowerState.OPEN
        datarepo?.saveShowerPatronList(showerPatrons)
    }

    fun adjustTimeLimit(roomNum: Int, additionalMinutes: Float) {
        println("addjust $roomNum to $additionalMinutes")
        val room = showerRooms[roomNum - 1]
        if (room.occupant != null){
            room.occupant!!.timeGiven += (additionalMinutes * 1000 * 60).toLong()
            room.outtime = room.occupant!!.inTime + room.occupant!!.timeGiven
            datarepo?.saveShowerPatronList(showerPatrons)
        }
    }

    fun deletePatron(delPatronIndex: Int) {
        println("del pat # $delPatronIndex")
        showerPatrons.removeAt(delPatronIndex)
        datarepo?.saveShowerPatronList(showerPatrons)
    }

    fun editPatron(patronIndex: Int, patronName: String, patronBirthday: String)
    {
        val pat=showerPatrons[patronIndex]
        pat.name=patronName
        pat.birthday=patronBirthday
        datarepo?.saveShowerPatronList(showerPatrons)
    }

    fun checkLaundryListPatrons(laundryList: LaundryListViewModel){
        for (lpat in laundryList.laundryPatrons){
            if (lpat.startTime>0 && !this.containsPatron(lpat.name,lpat.birthday)){
            showerPatrons.add(
                ShowerPatron(lpat.name,lpat.birthday,lpat.startTime+lpat.washRunTime, checkFromLaundryList = true))
        }}
    }

}