package org.lihi.ursa.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.lihi.ursa.ui.theme.Purple700
import org.lihi.ursa.ui.theme.PurpleTint
import org.lihi.ursa.ui.theme.timerTextOk
import org.lihi.ursa.ui.theme.timerTextOver
import org.lihi.ursa.ui.theme.timerTextWarn
import org.lihi.ursa.ursa.R
import java.lang.Math.abs
import kotlin.time.DurationUnit
import kotlin.time.toDuration


val numimags = listOf(
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


fun timeStatusColor(timeOut: Long, warningTime: Long, isCountingDown: Boolean = true): Color {
   val timeNow = System.currentTimeMillis()
   return if (isCountingDown) {
      when {
         timeNow > timeOut -> timerTextOver
         timeNow > timeOut - warningTime -> timerTextWarn
         else -> timerTextOk
      }
   } else {
      when {
         timeNow > timeOut + warningTime -> timerTextWarn
         else -> timerTextOk
      }
   }
}

fun timeStatusStr(timeout: Long, noWords: Boolean = true, noSign: Boolean = false): String {
   val timenow = System.currentTimeMillis()
   var timeleft = abs(timeout - timenow)
   var durleft = timeleft.toDuration(DurationUnit.MILLISECONDS)
   var leftstr = durleft.toComponents { minutes, seconds, nanoseconds ->
      "$minutes:".padStart(2, '0') + "$seconds".padStart(2, '0')
   }
   return if (noWords) ((if ((timenow <= timeout) or noSign) "" else "-") + leftstr) else leftstr + (if (timenow <= timeout) " Left" else " Over")
}

@Composable
fun TimeWidget(
   targetTime: Long,
   countUp: Boolean = false,
   warningTime: Long = 5L * 1000 * 60,
   adjButtons: Boolean = true,
   onAdjButtonClick: ((adjMinutes: Float) -> Unit)? = null,
   getTimeUpdate: () -> Long
) {
   val timenow = System.currentTimeMillis()
   val initString = timeStatusStr(targetTime)
   var leftstr by remember { mutableStateOf(initString) }
   val timeColor = timeStatusColor(targetTime, warningTime, !countUp)
   var leftstrColor by remember { mutableStateOf(timeColor) }


   LaunchedEffect(0) { // 3
      while (true) {
         val timestr = timeStatusStr(getTimeUpdate(), noSign = countUp)
         leftstr = timestr
         var timenow = System.currentTimeMillis()
         leftstrColor = timeStatusColor(getTimeUpdate(), warningTime, !countUp)
         delay(1000)
      }
   }

   val buttons = (adjButtons && (onAdjButtonClick != null) && (getTimeUpdate != null))
   Surface(
      border = BorderStroke(2.dp, Purple700),
      shape = RoundedCornerShape(12.dp),
      color = PurpleTint,
      elevation = 0.dp,
      modifier = Modifier.fillMaxWidth()
   ) {
      Box {
         if (buttons) {
            Button(
               contentPadding = PaddingValues.Absolute(1.dp),
               onClick = {
                  onAdjButtonClick?.let { it(-1.0f) }
                  val timestr = timeStatusStr(targetTime - 60L * 1000, noSign = countUp)
                  leftstr = timestr
               }, modifier = Modifier
                  //.weight(1f)
                  .padding(0.dp)
                  .height(28.dp)
                  .width(28.dp)
                  .align(Alignment.CenterStart)
            ) {
               Image(
                  painter = painterResource(R.drawable.white_minus),
                  contentDescription = "Subtract one minute"
               )
            }
         }
         Text(
            text = leftstr,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = leftstrColor,
            modifier = Modifier.align(Alignment.Center)
         )
         if (adjButtons) {
            Button(
               contentPadding = PaddingValues.Absolute(1.dp),
               onClick = {
                  onAdjButtonClick?.let { it(1.0f) }
                  val timestr = timeStatusStr(targetTime + 60L * 1000)
                  leftstr = timestr
               }, modifier = Modifier
                  //.weight(1f)
                  .padding(0.dp)
                  .height(28.dp)
                  .width(28.dp)
                  .align(Alignment.CenterEnd)
            ) {
               Image(
                  painter = painterResource(R.drawable.white_plus),
                  contentDescription = "Add one minute"
               )
            }
         }
      }
   }
}


@Composable
fun FacilityIcon(
   num: Int,
   image: Int,
   statusText: String,
   detailText: String,
   content: @Composable ColumnScope.() -> Unit,
   modifier: Modifier = Modifier,
) {
   Column(
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier
         .padding(2.dp)
         .fillMaxHeight()
   ) {
      Box {
         Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center)
         )
         Image(
            painter = painterResource(numimags[num - 1]),
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center)
         )
      }
      Column(horizontalAlignment = Alignment.CenterHorizontally)
      {
         Text(
            text = statusText,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
         )


         Text(
            text = detailText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
         )


         content()
      }
      Divider()
   }
}