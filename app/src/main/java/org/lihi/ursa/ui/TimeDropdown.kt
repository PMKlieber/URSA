
package org.lihi.ursa.ui

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.lihi.ursa.WEEKDAY_LAUNDRY_TIMES

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimeDropdown(selTime: String, onSelect: (timeSelected: String) -> Unit) {
    val options = WEEKDAY_LAUNDRY_TIMES.map{it.timeStr}
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(selTime) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        TextField(
            readOnly = true,
            value = selectedOptionText,
            onValueChange = { },
            label = { Text("Laundry Time") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        selectedOptionText = selectionOption
                        expanded = false
                        onSelect(selectionOption)
                    }
                ) {
                    Text(text = selectionOption)
                }
            }
        }
    }
}