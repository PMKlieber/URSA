package com.example.ursa.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ursa.LaundryListViewModel
import com.example.ursa.R
import com.example.ursa.ShowerListModel
import com.example.ursa.WEEKDAY_LAUNDRY_TIMES
import com.example.ursa.ui.theme.Shapes

val ICONDP_SMALL = 48.dp
val ICONDP_LARGE = 128.dp- ICONDP_SMALL

enum class ListScreens {
    LAUNDRY_SCREEN,
    SHOWER_SCREEN
}

@Composable
fun ListNavIcons(selectedScreen: MutableState<ListScreens>, onScreenSelected:(screen: ListScreens) -> Unit) {
    var laundrydp by remember { mutableStateOf(if (selectedScreen.value==ListScreens.LAUNDRY_SCREEN) ICONDP_LARGE else ICONDP_SMALL) }
    var showerdp by remember { mutableStateOf(if (selectedScreen.value==ListScreens.SHOWER_SCREEN) ICONDP_LARGE else ICONDP_SMALL) }

    val laundryIconSize by animateDpAsState(laundrydp,tween(500))//spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
    val showerIconSize by animateDpAsState(showerdp,tween(500))//,spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(ICONDP_LARGE + 4.dp)
    ) {
        Box(modifier = Modifier.size(showerIconSize), contentAlignment = Alignment.Center) {
            Image(
                painterResource(R.drawable.showerinuse),
                "Shower List",
                alignment = Alignment.Center,
                modifier = Modifier
                    .size(showerIconSize)
                    .clickable { onScreenSelected(ListScreens.SHOWER_SCREEN)
                    laundrydp= ICONDP_SMALL
                    showerdp= ICONDP_LARGE})
        }
        Box(modifier = Modifier.size(laundryIconSize), contentAlignment = Alignment.Center) {
            Image(
                painterResource(R.drawable.washerrun),
                "Laundry List",
                alignment = Alignment.Center,
                modifier = Modifier
                    .size(laundryIconSize)
                    .clickable {  onScreenSelected(ListScreens.LAUNDRY_SCREEN)
                        laundrydp= ICONDP_LARGE
                        showerdp= ICONDP_SMALL}
            )
        }
    }
}

@Composable
fun MainScreen(dpat: String) {
    val wtime = rememberSaveable {mutableStateOf(WEEKDAY_LAUNDRY_TIMES[0].timeStr)}
    var selectedScreen = rememberSaveable{mutableStateOf(ListScreens.SHOWER_SCREEN)}
    val laundrylm = remember {LaundryListViewModel(dpat)}
    val showerlm = remember { ShowerListModel(dpat) }
    val lfm = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Surface(shape = Shapes.large, elevation = 4.dp) {
                ListNavIcons(selectedScreen, onScreenSelected = {selectedScreen.value=it})
            }
            NewPatronForm(onAdd = { n, b ->
                if (selectedScreen.value==ListScreens.LAUNDRY_SCREEN){
                    laundrylm.addPatron(n,b,wtime.value)
                } else if (selectedScreen.value==ListScreens.SHOWER_SCREEN) {
                    showerlm.addPatronToQueue(n,b)
                }
                lfm.clearFocus()

            } )
        }
        Box {
            Row {
                AnimatedVisibility(
                    selectedScreen.value == ListScreens.SHOWER_SCREEN,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it }, // small slide 300px
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = LinearEasing // interpolator
                        )
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = LinearEasing
                        )
                    )
                ) {
                    ShowerListScreen("test",showerlm)
                }
            }
            Row {
                AnimatedVisibility(
                    selectedScreen.value != ListScreens.SHOWER_SCREEN,
                    enter = slideInHorizontally(
                        initialOffsetX = { it }, // small slide 300px
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = LinearEasing // interpolator
                        )
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = LinearEasing
                        )
                    )
                ) {
                    LaundryListScreen(dpat,wtime,laundrylm)
                }
            }

        }
    }
}


@Preview
@Composable
fun MainScreenPreview() {
    MainScreen("test")
}