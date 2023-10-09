package org.lihi.ursa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import org.lihi.ursa.ui.MainScreen
import org.lihi.ursa.ui.barList
import org.lihi.ursa.ui.theme.MopTheme
import org.lihi.ursa.ursa.R


const val NEWM=true
class MainActivity : ComponentActivity() {

    enum class listScreens {
        SHOWER_SCREEN,
        LAUNDRY_SCREEN
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        barList.clear()
        val lbar = resources.openRawResource(R.raw.lifebars)
        val br = lbar.bufferedReader()

        while (br.ready()) {
            val ln = (br.readLine()).split("\t")
            val name = ln[0]
            val bday = ln[1]
            val barReason = ln[3]

            println("$name ($bday): $barReason")
            barList.add(
                BasicPatron(
                    name, bday, listOf(PatronFlag(PatronFlagType.PERM_BAR, barReason))
                )
            )


        }

        super.onCreate(savedInstanceState)
        setContent {
            val selScreen = rememberSaveable { mutableStateOf(listScreens.SHOWER_SCREEN) }
            MopTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    if (NEWM)
                    {
                        MainScreen(super.getFilesDir().absolutePath)
                    }
                }
            }
        }
    }
}