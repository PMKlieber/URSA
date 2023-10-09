package org.lihi.ursa.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lihi.ursa.BasicPatron
import org.lihi.ursa.PatronFlagType
import org.lihi.ursa.ursa.R
import java.text.SimpleDateFormat
import kotlin.math.min


const val DATE_MASK = "##/##/####"
const val DATE_LENGTH = 8 // Equals to "##/##/####".count { it == '#'
const val INIT_BDAY=""

class NewPatronState {
    var name: String by mutableStateOf("")
    var birthday: String by mutableStateOf(INIT_BDAY)

}

class DateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return dateFilter(text)
    }
}

fun dateFilter(text: AnnotatedString): TransformedText {

    val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
    var out = ""
    for (i in trimmed.indices) {
        out += trimmed[i]
        if (i % 2 == 1 && i < 4) out += "/"
    }

    val numberOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 1) return offset
            if (offset <= 3) return offset +1
            if (offset <= 8) return offset +2
            return 10
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <=2) return offset
            if (offset <=5) return offset -1
            if (offset <=10) return offset -2
            return 8
        }
    }

    return TransformedText(AnnotatedString(out), numberOffsetTranslator)
}

@Composable
fun DateTextField(newpatron: NewPatronState, onAdd: () -> Unit) {
    var focusManager = LocalFocusManager.current

    TextField(
        value = newpatron.birthday,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            onAdd()
        }),
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp),
        modifier = Modifier.width(150.dp),
        onValueChange = {
            newpatron.birthday =
                if (it.length <= DATE_LENGTH) it else (it.substring(0, DATE_LENGTH))
            //     if (newpatron.birthday.substring(0, 1).toInt() > 1)
            //       newpatron.birthday = "0" + newpatron.birthday.substring(0, 1)
        },
        label = { Text(text = "Birthday") },
        visualTransformation = DateTransformation()
    )
}

fun formatBdayText(bday: String) : String {
    val fBday = when {
        bday.length < DATE_LENGTH -> bday + "0".repeat(DATE_LENGTH - bday.length)
        bday.length > DATE_LENGTH -> bday.substring(0, DATE_LENGTH)
        else -> bday
    }
    return fBday.substring(0, 2) + "/" + fBday.substring(2, 4) + "/" + fBday.substring(4)
}

data class scoredSuggestion(val text: AnnotatedString, val score: Int, val pat: BasicPatron)

fun scoreSuggestions(enteredText: String, pats: List<BasicPatron>): MutableList<scoredSuggestion> {
//    var r=AnnotatedString("", spanStyle = SpanStyle())
    var r = mutableListOf<scoredSuggestion>()
    val enteredTerms = enteredText.split(" ")
    for (pat in pats) {
        val name = pat.name
        val bday = pat.birthday
        val sugTerms = name.split(" ")
        var nmat = AnnotatedString("")
        var suggScore = 0
        for (sugTerm in sugTerms) {
            var matches = listOf("") + enteredTerms.map { it.commonPrefixWith(sugTerm) }
            val bestMatc = matches.maxBy { it.length }
            suggScore += bestMatc.length
            nmat = nmat + AnnotatedString(
                bestMatc, spanStyle = SpanStyle(fontWeight = FontWeight.Bold)
            ) + AnnotatedString(
                sugTerm.substring(bestMatc.length) + " ", spanStyle = SpanStyle()
            )
        }
        nmat += AnnotatedString("($bday)", spanStyle = SpanStyle(fontStyle = FontStyle.Italic))
        r.add(scoredSuggestion(nmat, suggScore, pat))
    }
    return r.sortedBy { it.score }.reversed() as MutableList<scoredSuggestion>
}

@Composable
fun NewPatronForm(onAdd: (addName: String, addBday: String) -> Unit) {
    var newpatron = remember { NewPatronState() }
    val focusManager = LocalFocusManager.current
    var fname by remember { mutableStateOf(TextFieldValue("")) }
    var suggs = mutableListOf<scoredSuggestion>()
    val suggNum = remember { mutableStateOf(0) }
    val maxSuggs = 4
    var bday by remember { mutableStateOf(TextFieldValue("")) }
    var dateFormatter = SimpleDateFormat("MM-dd-yy")
    val bPatrons = barList

    Surface(elevation = 8.dp, modifier = Modifier.padding(4.dp)) {
        Column(
            modifier = Modifier
                .padding(2.dp)
                .border(2.dp, Color.Black, shape = RoundedCornerShape(4.dp))
        ) {
            Row {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1.75f)
                ) {
                    TextField(value = newpatron.name, onValueChange = {
                        newpatron.name = it
                        suggs = scoreSuggestions(it, bPatrons)
                        suggNum.value = suggs.count { sugg -> (sugg.score > 0) }
                    }, label = { Text(text = "Name") },

                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Words
                        ),

                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Right)
                        }), textStyle = TextStyle(fontSize = 20.sp), modifier = Modifier.weight(3f)
                    )

                    DateTextField(newpatron) {
                        onAdd(newpatron.name, formatBdayText( newpatron.birthday))
                        newpatron.name = ""
                        newpatron.birthday = INIT_BDAY
                        suggNum.value = 0
                    }
                }

                Button(enabled = (newpatron.name != ""),
                    modifier = Modifier
                        .padding(all = 1.dp)
                        .align(Alignment.CenterVertically),
                    onClick = {
                        onAdd(newpatron.name, formatBdayText(newpatron.birthday))
                        newpatron.name = ""
                        newpatron.birthday = INIT_BDAY
                        suggNum.value = 0
                    }) {
                    Text(
                        "+ Add", fontWeight = FontWeight.Black, fontSize = 25.sp
                    )
                }


            }
            Row(
                modifier = Modifier.height(
                    40.dp * min(
                        if (newpatron.name == "") 0 else maxSuggs, suggNum.value
                    )
                )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(2.dp)
                        .background(
                            color = Color(220, 220, 220), shape = RoundedCornerShape(size = 4.dp)
                        )
                        .align(Alignment.CenterVertically)
                ) {
                    items(suggs) {
                        Row(
                            modifier = Modifier
                                .padding(6.dp)
                                .fillMaxWidth()
                        ) {
                            for (flag in it.pat.flags) {
                                var imageResource = when (flag.flagType) {
                                    PatronFlagType.PERM_BAR -> R.drawable.no_entry_256x255
                                    PatronFlagType.TEMP_BAR -> R.drawable.caution_256x256
                                    else -> R.drawable.caution_256x256
                                }
                                Image(
                                    painterResource(imageResource),
                                    "PERM BAR",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                it.text, fontSize = 20.sp, modifier = Modifier
                                    .clickable(onClick = {
                                        onAdd(it.pat.name, it.pat.birthday)
                                        newpatron.name = ""
                                        newpatron.birthday = INIT_BDAY
                                        suggNum.value = 0
                                    })
                                    .fillMaxWidth()
                            )
                        }
                        Divider(color = Color.LightGray, thickness = 2.dp)
                    }


                }
            }
        }
    }
}