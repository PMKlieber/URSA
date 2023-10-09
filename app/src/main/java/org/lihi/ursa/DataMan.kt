package org.lihi.ursa


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat

enum class PatronFlagType {
    TEMP_BAR,
    PERM_BAR,
    PAST_BAR,
    NEEDS_INTAKE,
    NOTICE,
    OTHER,
}

data class PatronFlag(var flagType: PatronFlagType, var note: String)

open class BasicPatron(
    var name: String,
    var birthday: String,
    var flags: List<PatronFlag> = listOf()
)

class CSVDataRepo(val dataDir: String) {


    val filenameDateFormatter = SimpleDateFormat("yyyyMMdd")
    val showerListFilename=filenameDateFormatter.format(System.currentTimeMillis()) + "ShowerList.tsv"
    val laundryListFilename=filenameDateFormatter.format(System.currentTimeMillis()) + "LaundryList.tsv"

    fun loadShowerPatronList(): List<ShowerPatron> {
        val g = Gson()
        val sfile= File(dataDir + "/" + showerListFilename)
        if (!sfile.exists()){
            return listOf<ShowerPatron>()
        }

        val reader = sfile.bufferedReader()
        val rtext = reader.readText()
        println("$sfile READ: $rtext")
        val itemType = object : TypeToken<List<ShowerPatron>>() {}.type
        val itemList = g.fromJson<List<ShowerPatron>>(rtext, itemType)
        val nsl = itemList.toList()
        return nsl
    }

    fun saveShowerPatronList(patrons: List<ShowerPatron>) {
        val g = Gson()
        var patal = ArrayList<ShowerPatron>()
        patal.addAll(patrons)
        val patJSON = g.toJson(patal)
        val swriter = PrintWriter(dataDir + "/" + showerListFilename)
        swriter.append(patJSON)
        swriter.close()
    }


    fun loadLaundryPatronList(): List<LaundryPatron> {
        val g = Gson()
        val lfile= File(dataDir + "/" + laundryListFilename)
        if (!lfile.exists() or NOLOAD){
            return listOf<LaundryPatron>()
        }
        val reader = lfile.bufferedReader()

        var rtext = reader.readText()
        println("READ: $rtext")
        var itemType = object : TypeToken<List<LaundryPatron>>() {}.type
        var itemList = g.fromJson<List<LaundryPatron>>(rtext, itemType)
        var nsl = itemList.toList()
        return nsl
    }

    fun saveLaundryPatronList(patrons: List<LaundryPatron>) {
        var g = Gson()
        var patal = ArrayList<LaundryPatron>()
        patal.addAll(patrons)
        val patJSON = g.toJson(patal)
        val swriter = PrintWriter(dataDir + "/" + laundryListFilename)
        swriter.append(patJSON)
        swriter.close()
    }


}

